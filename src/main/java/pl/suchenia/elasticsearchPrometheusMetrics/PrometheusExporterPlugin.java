package pl.suchenia.elasticsearchPrometheusMetrics;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.node.stats.NodeStats;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsRequest;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsResponse;
import org.elasticsearch.client.Client;
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
import pl.suchenia.elasticsearchPrometheusMetrics.generators.ClusterMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.IndicesMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.JvmMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.OsMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.PluginInfoMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.elasticsearch.rest.RestRequest.Method.GET;

public class PrometheusExporterPlugin extends Plugin implements ActionPlugin {
    private static final Logger logger = Loggers.getLogger(PrometheusExporterPlugin.class);

    private final JvmMetricsGenerator jvmMetricsGenerator = new JvmMetricsGenerator();
    private final IndicesMetricsGenerator indicesMetricsGenerator = new IndicesMetricsGenerator();
    private final ClusterMetricsGenerator clusterMetricsGenerator = new ClusterMetricsGenerator();
    private final OsMetricsGenerator osMetricsGenerator = new OsMetricsGenerator();
    private final PluginInfoMetricsGenerator infoMetricsGenerator = new PluginInfoMetricsGenerator();

    private final Map<String, StringBufferedRestHandler> handlers = new HashMap<>();

    public PrometheusExporterPlugin() {
        logger.info("Initializing prometheus reporting plugin");

        handlers.put("/_prometheus/jvm", (channel, client) -> {
            logger.debug("Generating JVM stats in prometheus format");

            return getNodeStats(client).thenApply((nodeStats -> {
                PrometheusFormatWriter writer = new PrometheusFormatWriter();
                infoMetricsGenerator.generateMetrics(writer, "");
                jvmMetricsGenerator.generateMetrics(writer, nodeStats.getJvm());
                return writer;
            }));
        });

        handlers.put("/_prometheus/os", (channel, client) -> {
            logger.debug("Generating OS stats in prometheus format");

            return getNodeStats(client).thenApply((nodeStats -> {
                PrometheusFormatWriter writer = new PrometheusFormatWriter();
                infoMetricsGenerator.generateMetrics(writer, "");
                osMetricsGenerator.generateMetrics(writer, nodeStats.getOs());
                return writer;
            }));
        });

        handlers.put("/_prometheus/indices", (channel, client) -> {
            logger.debug("Generating Indices stats in prometheus format");

            return getNodeStats(client).thenApply((nodeStats -> {
                PrometheusFormatWriter writer = new PrometheusFormatWriter();
                infoMetricsGenerator.generateMetrics(writer, "");
                indicesMetricsGenerator.generateMetrics(writer, nodeStats.getIndices());
                return writer;
            }));
        });

        handlers.put("/_prometheus/cluster", (channel, client) -> {
            logger.debug("Generating Indices stats in prometheus format");

            return getClusterStats(client).thenApply((clusterResponse -> {
                PrometheusFormatWriter writer = new PrometheusFormatWriter();
                infoMetricsGenerator.generateMetrics(writer, "");
                clusterMetricsGenerator.generateMetrics(writer, clusterResponse);
                return writer;
            }));
        });

        handlers.put("/_prometheus", (channel, client) -> {
            logger.debug("Generating ALL stats in prometheus format");

            return getNodeStats(client).thenApply((nodeStats -> {
                PrometheusFormatWriter writer = new PrometheusFormatWriter();
                infoMetricsGenerator.generateMetrics(writer, "");
                jvmMetricsGenerator.generateMetrics(writer, nodeStats.getJvm());
                indicesMetricsGenerator.generateMetrics(writer, nodeStats.getIndices());
                osMetricsGenerator.generateMetrics(writer, nodeStats.getOs());

                return writer;
            })).thenCombine(getClusterStats(client), (writer, clusterStats) -> {
                clusterMetricsGenerator.generateMetrics(writer, clusterStats);
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

    private static CompletableFuture<NodeStats> getNodeStats(final Client client) {
        CompletableFuture<NodeStats> result = new CompletableFuture<>();

        NodesStatsRequest nodesStatsRequest = new NodesStatsRequest("_local").all();
        client.admin().cluster().nodesStats(nodesStatsRequest, new ActionListener<NodesStatsResponse>() {
            @Override
            public void onResponse(NodesStatsResponse nodesStatsResponse) {
                NodeStats stats = nodesStatsResponse.getNodes().get(0);
                result.complete(stats);
            }

            @Override
            public void onFailure(Exception e) {
                result.completeExceptionally(e);
            }
        });
        return result;
    }

    private static CompletableFuture<ClusterHealthResponse> getClusterStats(final Client client) {
        CompletableFuture<ClusterHealthResponse> result = new CompletableFuture<>();

        ClusterHealthRequest clusterHealthRequest = new ClusterHealthRequest();
        client.admin().cluster().health(clusterHealthRequest, new ActionListener<ClusterHealthResponse>() {
            @Override
            public void onResponse(ClusterHealthResponse clusterHealthResponse) {
                result.complete(clusterHealthResponse);
            }

            @Override
            public void onFailure(Exception e) {
                result.completeExceptionally(e);
            }
        });
        return result;
    }
}
