package pl.suchenia.elasticsearchPrometheusMetrics;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.ClusterHealthMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.ClusterStateMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.IndicesMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.IngestMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.JvmMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.OsMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.PluginInfoMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.ProcessMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.TransportMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static pl.suchenia.elasticsearchPrometheusMetrics.async.AsyncRequests.getClusterHealth;
import static pl.suchenia.elasticsearchPrometheusMetrics.async.AsyncRequests.getClusterState;
import static pl.suchenia.elasticsearchPrometheusMetrics.async.AsyncRequests.getNodeStats;

public class PrometheusExporterPlugin extends Plugin implements ActionPlugin {
    private static final Logger logger = Loggers.getLogger(PrometheusExporterPlugin.class);

    private final JvmMetricsGenerator jvmMetricsGenerator = new JvmMetricsGenerator();
    private final IndicesMetricsGenerator indicesMetricsGenerator = new IndicesMetricsGenerator();
    private final ClusterHealthMetricsGenerator clusterHealthMetricsGenerator = new ClusterHealthMetricsGenerator();
    private final OsMetricsGenerator osMetricsGenerator = new OsMetricsGenerator();
    private final PluginInfoMetricsGenerator infoMetricsGenerator = new PluginInfoMetricsGenerator();
    private final ClusterStateMetricsGenerator clusterStateMetricsGenerator = new ClusterStateMetricsGenerator();
    private final TransportMetricsGenerator transportMetricsGenerator = new TransportMetricsGenerator();
    private final IngestMetricsGenerator ingestMetricsGenerator = new IngestMetricsGenerator();
    private final ProcessMetricsGenerator processMetricsGenerator = new ProcessMetricsGenerator();

    private final Map<String, StringBufferedRestHandler> handlers = new HashMap<>();

    public PrometheusExporterPlugin() {
        logger.info("Initializing prometheus reporting plugin");
        handlers.put("/_prometheus/node", (channel, client) -> {
            logger.debug("Generating JVM stats in prometheus format");

            return getNodeStats(client).thenApply((nodeStats -> {
                PrometheusFormatWriter writer = createWriter(client);
                infoMetricsGenerator.generateMetrics(writer, "");
                transportMetricsGenerator.generateMetrics(writer, nodeStats.getTransport());
                jvmMetricsGenerator.generateMetrics(writer, nodeStats.getJvm());
                osMetricsGenerator.generateMetrics(writer, nodeStats.getOs());
                ingestMetricsGenerator.generateMetrics(writer, nodeStats.getIngestStats());
                processMetricsGenerator.generateMetrics(writer, nodeStats.getProcess());
                return writer;
            }));
        });

        handlers.put("/_prometheus/indices", (channel, client) -> {
            logger.debug("Generating Indices stats in prometheus format");

            return getNodeStats(client).thenApply((nodeStats -> {
                PrometheusFormatWriter writer = createWriter(client);
                infoMetricsGenerator.generateMetrics(writer, "");
                indicesMetricsGenerator.generateMetrics(writer, nodeStats.getIndices());
                return writer;
            }));
        });

        handlers.put("/_prometheus/cluster", (channel, client) -> {
            logger.debug("Generating Indices stats in prometheus format");

            return getClusterHealth(client).thenApply((clusterResponse -> {
                PrometheusFormatWriter writer = createWriter(client);
                infoMetricsGenerator.generateMetrics(writer, "");
                clusterHealthMetricsGenerator.generateMetrics(writer, clusterResponse);
                return writer;
            }));
        });

        handlers.put("/_prometheus/cluster_settings",
                (channel, client) -> getClusterState(client)
                        .thenApply((clusterState -> {
                            PrometheusFormatWriter writer = createWriter(client);
                            logger.error("Got settings: {}", client.settings());
                            infoMetricsGenerator.generateMetrics(writer, client.settings().get("cluster.name"));
                            clusterStateMetricsGenerator.generateMetrics(writer, clusterState);
                            return writer;
                        }
                        )
                )
        );

        handlers.put("/_prometheus", (channel, client) -> {
            logger.debug("Generating ALL stats in prometheus format");
            return getNodeStats(client).thenApply((nodeStats -> {
                PrometheusFormatWriter writer = createWriter(client);
                infoMetricsGenerator.generateMetrics(writer, client.settings().get("cluster.name"));
                jvmMetricsGenerator.generateMetrics(writer, nodeStats.getJvm());
                indicesMetricsGenerator.generateMetrics(writer, nodeStats.getIndices());
                osMetricsGenerator.generateMetrics(writer, nodeStats.getOs());
                transportMetricsGenerator.generateMetrics(writer, nodeStats.getTransport());
                ingestMetricsGenerator.generateMetrics(writer, nodeStats.getIngestStats());
                processMetricsGenerator.generateMetrics(writer, nodeStats.getProcess());
                return writer;
            })).thenCombine(getClusterHealth(client), (writer, clusterHealth) -> {
                clusterHealthMetricsGenerator.generateMetrics(writer, clusterHealth);
                return writer;
            }).thenCombine(getClusterState(client), (writer, clusterStats) -> {
                clusterStateMetricsGenerator.generateMetrics(writer, clusterStats);
                return writer;
            });
        });
    }

    @Override
    public List<RestHandler> getRestHandlers(Settings settings,
                                             RestController restController,
                                             ClusterSettings clusterSettings,
                                             IndexScopedSettings indexScopedSettings,
                                             SettingsFilter settingsFilter,
                                             IndexNameExpressionResolver indexNameExpressionResolver,
                                             Supplier<DiscoveryNodes> nodesInCluster) {

        logger.debug("Registering REST handlers");

        handlers.forEach((key, value) -> restController.registerHandler(GET, key, value));
        return new ArrayList<>(handlers.values());
    }

    private static PrometheusFormatWriter createWriter(NodeClient client) {
        String nodeName = client.settings().get("node.name");
        String clusterName = client.settings().get("cluster.name");

        Map<String, String> globalLabels = new HashMap<>();
        globalLabels.put("node", nodeName);
        globalLabels.put("cluster", clusterName);

        return new PrometheusFormatWriter(globalLabels);
    }
}
