package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.SingleValueWriter;

public class ClusterStateMetricsGenerator extends MetricsGenerator<ClusterState> {
    private static final Logger logger = Loggers.getLogger(ClusterStateMetricsGenerator.class);

    @Override
    public PrometheusFormatWriter generateMetrics(PrometheusFormatWriter writer, ClusterState clusterState) {
        logger.debug("Generating data about cluster state: {}", clusterState);

        SingleValueWriter persistentGauge = writer.addGauge("es_cluster_persistent_settings")
                .withHelp("Cluster persistent settings value visible from this node");
        fillSettings(persistentGauge, clusterState.getMetaData().persistentSettings());

        SingleValueWriter transientGauge = writer.addGauge("es_cluster_transient_settings")
                .withHelp("Cluster persistent settings value visible from this node");
        fillSettings(transientGauge, clusterState.getMetaData().transientSettings());

        SingleValueWriter settingsGauge = writer.addGauge("es_cluster_settings")
                .withHelp("Cluster effective settings value visible from this node");
        fillSettings(settingsGauge, clusterState.getMetaData().settings());

        return writer;
    }

    private void fillSettings(SingleValueWriter settingsGauge, Settings settings) {
        settings.keySet().forEach((key) -> {
            settingsGauge.value(1, key, settings.get(key));
        });
    }
}
