package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.cluster.health.ClusterIndexHealth;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.ValueWriter;

import java.util.Map;

public class ClusterMetricsGenerator implements MetricsGenerator<ClusterHealthResponse> {
    @Override
    public void generateMetrics(PrometheusFormatWriter writer, ClusterHealthResponse clusterHealth) {
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
                .value("type", "active", clusterHealth.getActiveShards())
                .value("type", "primary", clusterHealth.getActivePrimaryShards())
                .value("type", "relocating", clusterHealth.getRelocatingShards())
                .value("type", "initializing", clusterHealth.getInitializingShards())
                .value("type", "unassigned", clusterHealth.getUnassignedShards())
                .value("type", "delayed-unassigned", clusterHealth.getDelayedUnassignedShards());

        writer.addGauge("es_shards_active_percentage")
                .withHelp("Percentage of active shards")
                .value(clusterHealth.getActiveShardsPercent());

        writer.addGauge("es_nodes")
                .withHelp("Number of nodes in a cluster")
                .value(clusterHealth.getNumberOfNodes());

        writer.addGauge("es_data_nodes")
                .withHelp("Number of data nodes in a cluster")
                .value(clusterHealth.getNumberOfDataNodes());

        writer.addGauge("es_pending_tasks")
                .withHelp("Number of tasks pending")
                .value(clusterHealth.getNumberOfPendingTasks());

        writer.addGauge("es_status")
                .withHelp("Cluster status")
                .value(clusterHealth.getStatus().value());

        writer.addGauge("es_status_type")
                .withHelp("Cluster status per type")
                .value("status", clusterHealth.getStatus().name(), 1);

        ValueWriter es_index_active_shards = writer.addGauge("es_index_active_shards")
                .withHelp("Number of active shards assigned to index");

        ValueWriter es_index_active_primary_shards = writer.addGauge("es_index_active_primary_shards")
                .withHelp("Number of active primary shards assigned to index");

        ValueWriter es_index_initializing_shards = writer.addGauge("es_index_initializing_shards")
                .withHelp("Number of initializing shards assigned to index");

        ValueWriter es_index_relocating_shards = writer.addGauge("es_index_relocating_shards")
                .withHelp("Number of initializing shards assigned to index");

        ValueWriter es_index_unassigned_shards = writer.addGauge("es_index_unassigned_shards")
                .withHelp("Number of initializing shards assigned to index");

        ValueWriter es_index_status = writer.addGauge("es_index_status")
                .withHelp("Status of index");

        ValueWriter es_index_status_type = writer.addGauge("es_index_status_type")
                .withHelp("Inde status per type");

        for (Map.Entry<String, ClusterIndexHealth> indexStatusEntry : clusterHealth.getIndices().entrySet()) {
            String indexName = indexStatusEntry.getKey();
            ClusterIndexHealth indexStatus = indexStatusEntry.getValue();

            es_index_active_shards.value("index", indexName, indexStatus.getActiveShards());
            es_index_active_primary_shards.value("index", indexName, indexStatus.getActivePrimaryShards());
            es_index_initializing_shards.value("index", indexName, indexStatus.getInitializingShards());
            es_index_relocating_shards.value("index", indexName, indexStatus.getRelocatingShards());
            es_index_unassigned_shards.value("index", indexName, indexStatus.getUnassignedShards());
            es_index_status.value("index", indexName, indexStatus.getStatus().value());
        }
    }
}
