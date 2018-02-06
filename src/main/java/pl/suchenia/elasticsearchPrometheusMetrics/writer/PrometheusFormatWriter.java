package pl.suchenia.elasticsearchPrometheusMetrics.writer;

import java.io.StringWriter;
import java.util.Map;

public class PrometheusFormatWriter {
    private final StringWriter writer = new StringWriter();
    private final Map<String, String> globalLabels;

    public PrometheusFormatWriter(Map<String, String> globalLabels) {
        this.globalLabels = globalLabels;
    }

    public SingleMetricsDefinitionBuilder addGauge(String name) {
        return new SingleMetricsDefinitionBuilder(MetricType.GAUGE, writer, name, globalLabels);
    }

    public SingleMetricsDefinitionBuilder addCounter(String name) {
        return new SingleMetricsDefinitionBuilder(MetricType.COUNTER, writer, name, globalLabels);
    }

    public SummaryMetricsDefinitionBuilder addSummary(String name) {
        return new SummaryMetricsDefinitionBuilder(writer, name, globalLabels);
    }

    public String toString() {
        return writer.toString();
    }
}
