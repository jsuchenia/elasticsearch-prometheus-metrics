package pl.suchenia.elasticsearchPrometheusMetrics.writer;

import java.io.StringWriter;

public class PrometheusFormatWriter {
    private final StringWriter writer = new StringWriter();

    public MetricDefinitionBuilder addGauge(String name) {
        return new MetricDefinitionBuilder(writer, "gauge", name);
    }

    public MetricDefinitionBuilder addCounter(String name) {
        return new MetricDefinitionBuilder(writer, "counter", name);
    }

    public MetricDefinitionBuilder addSummary(String name) {
        return new MetricDefinitionBuilder(writer, "summary", name);
    }

    public String toString() {
        return writer.toString();
    }
}
