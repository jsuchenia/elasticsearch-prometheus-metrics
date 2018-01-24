package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.common.settings.Settings;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.ValueWriter;

public class ClusterStateMetricsGenerator implements MetricsGenerator<ClusterState> {
    @Override
    public void generateMetrics(PrometheusFormatWriter writer, ClusterState clusterState) {

        ValueWriter persistentGauge = writer.addGauge("es_cluster_persistent_settings")
                .withHelp("Cluster persistent settings value visible from this node");
        fillSettings(persistentGauge, clusterState.getMetaData().persistentSettings());

        ValueWriter transientGauge = writer.addGauge("es_cluster_transient_settings")
                .withHelp("Cluster persistent settings value visible from this node");
        fillSettings(transientGauge, clusterState.getMetaData().transientSettings());

        ValueWriter settingsGauge = writer.addGauge("es_cluster_settings")
                .withHelp("Cluster effective settings value visible from this node");
        fillSettings(settingsGauge, clusterState.getMetaData().settings());
    }

    private void fillSettings(ValueWriter settingsGauge, Settings settings) {
        settings.keySet().forEach((key) -> {
            settingsGauge.value(1, key, settings.get(key));
        });
    }
}
