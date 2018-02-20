package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.cluster.health.ClusterIndexHealth;
import org.elasticsearch.common.logging.Loggers;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.SingleValueWriter;

import java.util.Map;

public class ClusterHealthMetricsGenerator extends MetricsGenerator<ClusterHealthResponse> {
    private static final Logger logger = Loggers.getLogger(ClusterHealthMetricsGenerator.class);
    @Override
    public PrometheusFormatWriter generateMetrics(PrometheusFormatWriter writer, ClusterHealthResponse clusterHealth) {
        logger.debug("Generating data about cluster health: {}", clusterHealth);

        //Shards global data
        writer.addGauge("es_active_shards")
                .withHelp("Number of active shards")
                .value(clusterHealth.getActiveShards());

        writer.addGauge("es_active_primary_shards")
                .withHelp("Number of active primary")
                .value(clusterHealth.getActivePrimaryShards());

        writer.addGauge("es_relocating_shards")
                .withHelp("Number of relocating shards")
                .value(clusterHealth.getRelocatingShards());

        writer.addGauge("es_initializing_shards")
                .withHelp("Number of initializing shards")
                .value(clusterHealth.getInitializingShards());

        writer.addGauge("es_unassigned_shards")
                .withHelp("Number of unassigned shards")
                .value(clusterHealth.getUnassignedShards());

        writer.addGauge("es_shards_count")
                .withHelp("Number of shards in a cluster")
                .value(clusterHealth.getActiveShards(), "type", "active")
                .value(clusterHealth.getActivePrimaryShards(), "type", "primary")
                .value(clusterHealth.getRelocatingShards(), "type", "relocating")
                .value(clusterHealth.getInitializingShards(), "type", "initializing")
                .value(clusterHealth.getUnassignedShards(), "type", "unassigned")
                .value(clusterHealth.getDelayedUnassignedShards(), "type", "delayed-unassigned");

        writer.addGauge("es_shards_active_percentage")
                .withHelp("Percentage of active shards")
                .value(clusterHealth.getActiveShardsPercent());

        //Nodes data
        writer.addGauge("es_nodes")
                .withHelp("Number of nodes in a cluster")
                .value(clusterHealth.getNumberOfNodes());

        writer.addGauge("es_data_nodes")
                .withHelp("Number of data nodes in a cluster")
                .value(clusterHealth.getNumberOfDataNodes());

        //Tasks data
        writer.addGauge("es_pending_tasks")
                .withHelp("Number of tasks pending")
                .value(clusterHealth.getNumberOfPendingTasks());

        //Cluster status
        writer.addGauge("es_status")
                .withHelp("Cluster status")
                .value(clusterHealth.getStatus().value());

        writer.addGauge("es_status_type")
                .withHelp("Cluster status per type")
                .value(1, "status", clusterHealth.getStatus().name());

        //Status for each index
        SingleValueWriter es_index_active_shards = writer.addGauge("es_index_active_shards")
                .withHelp("Number of active shards assigned to index");

        SingleValueWriter es_index_active_primary_shards = writer.addGauge("es_index_active_primary_shards")
                .withHelp("Number of active primary shards assigned to index");

        SingleValueWriter es_index_initializing_shards = writer.addGauge("es_index_initializing_shards")
                .withHelp("Number of initializing shards assigned to index");

        SingleValueWriter es_index_relocating_shards = writer.addGauge("es_index_relocating_shards")
                .withHelp("Number of initializing shards assigned to index");

        SingleValueWriter es_index_unassigned_shards = writer.addGauge("es_index_unassigned_shards")
                .withHelp("Number of initializing shards assigned to index");

        SingleValueWriter es_index_status = writer.addGauge("es_index_status")
                .withHelp("Status of index");

        SingleValueWriter es_index_status_type = writer.addGauge("es_index_status_type")
                .withHelp("Inde status per type");

        for (Map.Entry<String, ClusterIndexHealth> indexStatusEntry : clusterHealth.getIndices().entrySet()) {
            String indexName = indexStatusEntry.getKey();
            ClusterIndexHealth indexStatus = indexStatusEntry.getValue();

            es_index_active_shards.value(indexStatus.getActiveShards(), "index", indexName);
            es_index_active_primary_shards.value(indexStatus.getActivePrimaryShards(), "index", indexName);
            es_index_initializing_shards.value(indexStatus.getInitializingShards(), "index", indexName);
            es_index_relocating_shards.value(indexStatus.getRelocatingShards(), "index", indexName);
            es_index_unassigned_shards.value(indexStatus.getUnassignedShards(), "index", indexName);
            es_index_status.value(indexStatus.getStatus().value(), "index", indexName);
            es_index_status_type.value(1, "index", indexName, "status", indexStatus.getStatus().name());
        }

        return writer;
    }
}
