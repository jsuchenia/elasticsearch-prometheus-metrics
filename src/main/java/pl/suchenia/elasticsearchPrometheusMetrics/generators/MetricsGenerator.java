package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;

import java.io.IOException;

public interface MetricsGenerator<T> {
    void generateMetrics(PrometheusFormatWriter writer, T inputData);
}
