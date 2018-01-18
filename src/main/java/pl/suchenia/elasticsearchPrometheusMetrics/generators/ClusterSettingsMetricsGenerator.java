package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.ValueWriter;

public class ClusterSettingsMetricsGenerator implements MetricsGenerator<Settings> {
    private static final Logger logger = Loggers.getLogger(ClusterSettingsMetricsGenerator.class);
    private static final String CLUSTER_SETTINGS_PREFIX = "cluster.";
    public static final String CLUSTER_NAME_KEY = "cluster.name";

    @Override
    public void generateMetrics(PrometheusFormatWriter writer, Settings settings) {
        String clusterName = settings.get(CLUSTER_NAME_KEY, "(empty)");

        ValueWriter settingsGauge = writer.addGauge("cluster_settings")
                .withHelp("Cluster settings value visible from this node")
                .withSharedLabel("cluster", clusterName);

        settings.keySet().stream().filter((key) -> key.startsWith(CLUSTER_SETTINGS_PREFIX)).forEach((key) -> {
            settingsGauge.value(1, key, settings.get(key));
        });
    }
}
