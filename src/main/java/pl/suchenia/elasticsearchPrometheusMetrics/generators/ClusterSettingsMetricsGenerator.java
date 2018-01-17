package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.ValueWriter;

public class ClusterSettingsMetricsGenerator implements MetricsGenerator<Settings> {
    private static final Logger logger = Loggers.getLogger(ClusterSettingsMetricsGenerator.class);
    private static final String CLUSTER_PREFIX = "cluster.";

    @Override
    public void generateMetrics(PrometheusFormatWriter writer, Settings settings) {
        ValueWriter settingsGauge = writer.addGauge("cluster_settings")
                .withHelp("Cluster settings value visible from this node");

        settings.keySet().stream().filter((key) -> key.startsWith(CLUSTER_PREFIX)).forEach((key) -> {
            settingsGauge.value(1, key, settings.get(key));
        });
    }
}
