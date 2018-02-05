package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.elasticsearch.Version;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;

import java.io.IOException;

public interface MetricsGenerator<T> {
    default PrometheusFormatWriter generateMetricsWithHeader(PrometheusFormatWriter writer, T inputParam) {
        String version = MetricsGenerator.class.getPackage().getImplementationVersion();
        writer.addGauge("es_prometheus_version")
                .withHelp("Plugin version to track across a cluster")
                .value(1, "pluginVersion", version, "es_version", Version.CURRENT.toString());

        return generateMetrics(writer, inputParam);
    }

    PrometheusFormatWriter generateMetrics(PrometheusFormatWriter writer, T inputData);
}
