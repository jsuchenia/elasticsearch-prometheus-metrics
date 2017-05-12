package pl.suchenia.elasticsearchPrometheusMetrics;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.node.stats.NodeStats;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsRequest;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsResponse;
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
import pl.suchenia.elasticsearchPrometheusMetrics.generators.IndicesMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.JvmMetricsGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.elasticsearch.rest.RestRequest.Method.GET;

public class PrometheusExporterPlugin extends Plugin implements ActionPlugin {
    private final Logger logger = Loggers.getLogger(PrometheusExporterPlugin.class);

    private final JvmMetricsGenerator jvmMetricsGenerator = new JvmMetricsGenerator();
    private final IndicesMetricsGenerator indicesMetricsGenerator = new IndicesMetricsGenerator();

    private final Map<String, StringBufferedRestHandler> handlers = new HashMap<>();

    public PrometheusExporterPlugin() {
        logger.info("Initializing prometheus plugin");

        handlers.put("/_prometheus", (writer, client) -> {
            logger.debug("Generating all stats in prometheus format");

            NodeStats nodeStats = getNodeStats(client);
            jvmMetricsGenerator.generateMetrics(writer, nodeStats.getJvm());
            indicesMetricsGenerator.generateMetrics(writer, nodeStats.getIndices());
        });

        handlers.put("/_prometheus/jvm", (writer, client) -> {
            logger.debug("Generating JVM stats in prometheus format");

            NodeStats nodeStats = getNodeStats(client);
            jvmMetricsGenerator.generateMetrics(writer, nodeStats.getJvm());
        });

        handlers.put("/_prometheus/indices", (writer, client) -> {
            logger.debug("Generating indices stats in prometheus format");

            NodeStats nodeStats = getNodeStats(client);
            indicesMetricsGenerator.generateMetrics(writer, nodeStats.getIndices());
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

        logger.trace("Registering REST handlers");

        handlers.forEach((key, value) -> restController.registerHandler(GET, key, value));
        return new ArrayList<>(handlers.values());
    }

    private static NodeStats getNodeStats(NodeClient client) {
        NodesStatsRequest nodesStatsRequest = new NodesStatsRequest("_local").all();
        NodesStatsResponse nodesStatsResponse = client.admin().cluster().nodesStats(nodesStatsRequest).actionGet();
        return nodesStatsResponse.getNodes().get(0);
    }
}
