package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.common.settings.Settings;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.ValueWriter;

public class ClusterStateMetricsGenerator implements MetricsGenerator<ClusterState> {
    @Override
    public void generateMetrics(PrometheusFormatWriter writer, ClusterState clusterState) {
        String clusterName = clusterState.getClusterName().toString();

        ValueWriter persistentGauge = writer.addGauge("cluster_persistent_settings")
                .withHelp("Cluster persistent settings value visible from this node")
                .withSharedLabel("cluster", clusterName);
        fillSettings(persistentGauge, clusterState.getMetaData().persistentSettings());

        ValueWriter transientGauge = writer.addGauge("cluster_transient_settings")
                .withHelp("Cluster persistent settings value visible from this node")
                .withSharedLabel("cluster", clusterName);
        fillSettings(transientGauge, clusterState.getMetaData().transientSettings());

        ValueWriter settingsGauge = writer.addGauge("cluster_settings")
                .withHelp("Cluster effective settings value visible from this node")
                .withSharedLabel("cluster", clusterName);
        fillSettings(settingsGauge, clusterState.getMetaData().settings());
    }

    private void fillSettings(ValueWriter settingsGauge, Settings settings) {
        settings.keySet().forEach((key) -> {
            settingsGauge.value(1, key, settings.get(key));
        });
    }
}
