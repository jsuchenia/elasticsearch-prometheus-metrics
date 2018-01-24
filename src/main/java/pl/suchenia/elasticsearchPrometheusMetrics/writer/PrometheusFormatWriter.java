package pl.suchenia.elasticsearchPrometheusMetrics.writer;

import java.io.StringWriter;
import java.util.Map;

public class PrometheusFormatWriter {
    private final StringWriter writer = new StringWriter();
    private final Map<String, String> globalLabels;

    public PrometheusFormatWriter(Map<String, String> globalLabels) {
        this.globalLabels = globalLabels;
    }

    public MetricDefinitionBuilder addGauge(String name) {
        return new MetricDefinitionBuilder(writer, "gauge", name, globalLabels);
    }

    public MetricDefinitionBuilder addCounter(String name) {
        return new MetricDefinitionBuilder(writer, "counter", name, globalLabels);
    }

    public MetricDefinitionBuilder addSummary(String name) {
        return new MetricDefinitionBuilder(writer, "summary", name, globalLabels);
    }

    public String toString() {
        return writer.toString();
    }
}
