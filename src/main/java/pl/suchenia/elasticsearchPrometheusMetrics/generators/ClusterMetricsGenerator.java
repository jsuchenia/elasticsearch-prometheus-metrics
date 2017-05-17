package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.cluster.health.ClusterIndexHealth;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.ValueWriter;

import java.util.Map;

public class ClusterMetricsGenerator implements MetricsGenerator<ClusterHealthResponse> {
    @Override
    public void generateMetrics(PrometheusFormatWriter writer, ClusterHealthResponse clusterHealth) {
        String clusterName = clusterHealth.getClusterName();

        //Shards global data
        writer.addGauge("es_active_shards")
                .withHelp("Number of active shards")
                .withSharedLabel("cluster", clusterName)
                .value(clusterHealth.getActiveShards());

        writer.addGauge("es_active_primary_shards")
                .withHelp("Number of active primary")
                .withSharedLabel("cluster", clusterName)
                .value(clusterHealth.getActivePrimaryShards());

        writer.addGauge("es_relocating_shards")
                .withHelp("Number of relocating shards")
                .withSharedLabel("cluster", clusterName)
                .value(clusterHealth.getRelocatingShards());

        writer.addGauge("es_initializing_shards")
                .withHelp("Number of initializing shards")
                .withSharedLabel("cluster", clusterName)
                .value(clusterHealth.getInitializingShards());

        writer.addGauge("es_unassigned_shards")
                .withHelp("Number of unassigned shards")
                .withSharedLabel("cluster", clusterName)
                .value(clusterHealth.getUnassignedShards());

        writer.addGauge("es_shards_count")
                .withHelp("Number of shards in a cluster")
                .withSharedLabel("cluster", clusterName)
                .value(clusterHealth.getActiveShards(), "type", "active")
                .value(clusterHealth.getActivePrimaryShards(), "type", "primary")
                .value(clusterHealth.getRelocatingShards(), "type", "relocating")
                .value(clusterHealth.getInitializingShards(), "type", "initializing")
                .value(clusterHealth.getUnassignedShards(), "type", "unassigned")
                .value(clusterHealth.getDelayedUnassignedShards(), "type", "delayed-unassigned");

        writer.addGauge("es_shards_active_percentage")
                .withHelp("Percentage of active shards")
                .withSharedLabel("cluster", clusterName)
                .value(clusterHealth.getActiveShardsPercent());

        //Nodes data
        writer.addGauge("es_nodes")
                .withHelp("Number of nodes in a cluster")
                .withSharedLabel("cluster", clusterName)
                .value(clusterHealth.getNumberOfNodes());

        writer.addGauge("es_data_nodes")
                .withHelp("Number of data nodes in a cluster")
                .withSharedLabel("cluster", clusterName)
                .value(clusterHealth.getNumberOfDataNodes());

        //Tasks data
        writer.addGauge("es_pending_tasks")
                .withHelp("Number of tasks pending")
                .withSharedLabel("cluster", clusterName)
                .value(clusterHealth.getNumberOfPendingTasks());

        //Cluster status
        writer.addGauge("es_status")
                .withHelp("Cluster status")
                .withSharedLabel("cluster", clusterName)
                .value(clusterHealth.getStatus().value());

        writer.addGauge("es_status_type")
                .withHelp("Cluster status per type")
                .withSharedLabel("cluster", clusterName)
                .value(1, "status", clusterHealth.getStatus().name());

        //Status for each index
        ValueWriter es_index_active_shards = writer.addGauge("es_index_active_shards")
                .withHelp("Number of active shards assigned to index")
                .withSharedLabel("cluster", clusterName);

        ValueWriter es_index_active_primary_shards = writer.addGauge("es_index_active_primary_shards")
                .withHelp("Number of active primary shards assigned to index")
                .withSharedLabel("cluster", clusterName);

        ValueWriter es_index_initializing_shards = writer.addGauge("es_index_initializing_shards")
                .withHelp("Number of initializing shards assigned to index")
                .withSharedLabel("cluster", clusterName);

        ValueWriter es_index_relocating_shards = writer.addGauge("es_index_relocating_shards")
                .withHelp("Number of initializing shards assigned to index")
                .withSharedLabel("cluster", clusterName);

        ValueWriter es_index_unassigned_shards = writer.addGauge("es_index_unassigned_shards")
                .withHelp("Number of initializing shards assigned to index")
                .withSharedLabel("cluster", clusterName);

        ValueWriter es_index_status = writer.addGauge("es_index_status")
                .withHelp("Status of index")
                .withSharedLabel("cluster", clusterName);

        ValueWriter es_index_status_type = writer.addGauge("es_index_status_type")
                .withHelp("Inde status per type")
                .withSharedLabel("cluster", clusterName);

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
    }
}
