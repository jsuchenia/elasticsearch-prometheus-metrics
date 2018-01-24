package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.elasticsearch.Version;
import org.elasticsearch.monitor.os.OsStats;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;

public class PluginInfoMetricsGenerator implements MetricsGenerator<String> {
    @Override
    public void generateMetrics(PrometheusFormatWriter writer, String unused) {
        String version = PluginInfoMetricsGenerator.class.getPackage().getImplementationVersion();
        writer.addGauge("es_prometheus_version")
                .withHelp("Plugin version to track across a cluster")
                .value(1, "pluginVersion", version, "es_version", Version.CURRENT.toString());
    }
}
