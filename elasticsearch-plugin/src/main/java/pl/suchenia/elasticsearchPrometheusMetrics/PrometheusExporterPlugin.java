package pl.suchenia.elasticsearchPrometheusMetrics;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.Version;
import org.elasticsearch.action.admin.cluster.node.stats.NodeStats;
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
import pl.suchenia.elasticsearchPrometheusMetrics.generators.CircuitBreakerMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.ClusterHealthMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.ClusterStateMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.FsMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.HttpMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.IndicesMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.IngestMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.JvmMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.NodeUsageGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.OsMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.PendingTasksMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.ProcessMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.StriptsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.ThreadPoolMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.TransportMetricsGenerator;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static pl.suchenia.elasticsearchPrometheusMetrics.async.AsyncRequests.getClusterHealth;
import static pl.suchenia.elasticsearchPrometheusMetrics.async.AsyncRequests.getClusterState;
import static pl.suchenia.elasticsearchPrometheusMetrics.async.AsyncRequests.getNodeStats;
import static pl.suchenia.elasticsearchPrometheusMetrics.async.AsyncRequests.getNodesUsage;
import static pl.suchenia.elasticsearchPrometheusMetrics.async.AsyncRequests.getPendingTasks;

public class PrometheusExporterPlugin extends Plugin implements ActionPlugin {
    private static final Logger logger = Loggers.getLogger(PrometheusExporterPlugin.class, "init");

    private final JvmMetricsGenerator jvmMetricsGenerator = new JvmMetricsGenerator();
    private final IndicesMetricsGenerator indicesMetricsGenerator = new IndicesMetricsGenerator();
    private final ClusterHealthMetricsGenerator clusterHealthMetricsGenerator = new ClusterHealthMetricsGenerator();
    private final OsMetricsGenerator osMetricsGenerator = new OsMetricsGenerator();
    private final ClusterStateMetricsGenerator clusterStateMetricsGenerator = new ClusterStateMetricsGenerator();
    private final TransportMetricsGenerator transportMetricsGenerator = new TransportMetricsGenerator();
    private final IngestMetricsGenerator ingestMetricsGenerator = new IngestMetricsGenerator();
    private final ProcessMetricsGenerator processMetricsGenerator = new ProcessMetricsGenerator();
    private final PendingTasksMetricsGenerator pendingTasksMetricsGenerator = new PendingTasksMetricsGenerator();
    private final CircuitBreakerMetricsGenerator circuitBreakerMetricsGenerator = new CircuitBreakerMetricsGenerator();
    private final FsMetricsGenerator fsMetricsGenerator = new FsMetricsGenerator();
    private final ThreadPoolMetricsGenerator threadPoolMetricsGenerator = new ThreadPoolMetricsGenerator();
    private final NodeUsageGenerator nodeUsageGenerator = new NodeUsageGenerator();
    private final HttpMetricsGenerator httpMetricsGenerator = new HttpMetricsGenerator();
    private final StriptsGenerator scriptsGenerator = new StriptsGenerator();

    private final Map<String, StringBufferedRestHandler> handlers = new HashMap<>();

    public PrometheusExporterPlugin() {
        logger.info("Initializing prometheus reporting plugin");
        handlers.put("/_prometheus/node", (channel, client) -> {
            logger.debug("Generating JVM stats in prometheus format");

            return createWriter(client).thenCombine(getNodeStats(client), this::generateNodeMetrics);
        });

        handlers.put("/_prometheus/cluster", (channel, client) -> {
            logger.debug("Generating Indices stats in prometheus format");

            return createWriter(client)
                    .thenCombine(getClusterHealth(client),clusterHealthMetricsGenerator::generateMetrics);
        });

        handlers.put("/_prometheus/cluster_settings", (channel, client) -> {
            logger.debug("Generating cluster settings results");

            return createWriter(client)
                    .thenCombine(getClusterState(client), clusterStateMetricsGenerator::generateMetrics);
        });

        handlers.put("/_prometheus/tasks", (channel, client) -> {
            logger.debug("Generating pending tasks results");


            return createWriter(client)
                    .thenCombine(getPendingTasks(client), pendingTasksMetricsGenerator::generateMetrics);
        });

        handlers.put("/_prometheus/rest", (channel, client) -> {
            logger.debug("Generating node usage results");


            return createWriter(client)
                    .thenCombine(getNodesUsage(client), nodeUsageGenerator::generateMetrics);
        });

        handlers.put("/_prometheus", (channel, client) -> {
            logger.debug("Generating ALL stats in prometheus format");
            return createWriter(client)
                    .thenCombine(getNodeStats(client), this::generateNodeMetrics)
                    .thenCombine(getClusterHealth(client), clusterHealthMetricsGenerator::generateMetrics)
                    .thenCombine(getClusterState(client), clusterStateMetricsGenerator::generateMetrics)
                    .thenCombine(getPendingTasks(client), pendingTasksMetricsGenerator::generateMetrics)
                    .thenCombine(getNodesUsage(client), nodeUsageGenerator::generateMetrics);
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

    private PrometheusFormatWriter generateNodeMetrics(PrometheusFormatWriter writer, NodeStats nodeStats) {
        jvmMetricsGenerator.generateMetrics(writer, nodeStats.getJvm());
        indicesMetricsGenerator.generateMetrics(writer, nodeStats.getIndices());
        osMetricsGenerator.generateMetrics(writer, nodeStats.getOs());
        transportMetricsGenerator.generateMetrics(writer, nodeStats.getTransport());
        ingestMetricsGenerator.generateMetrics(writer, nodeStats.getIngestStats());
        processMetricsGenerator.generateMetrics(writer, nodeStats.getProcess());
        circuitBreakerMetricsGenerator.generateMetrics(writer, nodeStats.getBreaker());
        fsMetricsGenerator.generateMetrics(writer, nodeStats.getFs());
        threadPoolMetricsGenerator.generateMetrics(writer, nodeStats.getThreadPool());
        httpMetricsGenerator.generateMetrics(writer, nodeStats.getHttp());
        scriptsGenerator.generateMetrics(writer, nodeStats.getScriptStats());

        return writer;
    }

    private static CompletableFuture<PrometheusFormatWriter> createWriter(NodeClient client) {
        String nodeName = client.settings().get("node.name");
        String clusterName = client.settings().get("cluster.name");

        Map<String, String> globalLabels = new HashMap<>();
        globalLabels.put("node", nodeName);
        globalLabels.put("cluster", clusterName);

        PrometheusFormatWriter writer = new PrometheusFormatWriter(globalLabels);

        String version = PrometheusExporterPlugin.class.getPackage().getImplementationVersion();
        writer.addGauge("es_prometheus_version")
                .withHelp("Plugin version to track across a cluster")
                .value(1, "pluginVersion", version, "es_version", Version.CURRENT.toString());

        return CompletableFuture.completedFuture(writer);
    }
}
