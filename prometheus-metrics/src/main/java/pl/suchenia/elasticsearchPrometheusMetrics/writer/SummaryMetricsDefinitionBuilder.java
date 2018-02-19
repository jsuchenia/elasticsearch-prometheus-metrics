package pl.suchenia.elasticsearchPrometheusMetrics.writer;

import java.io.StringWriter;
import java.util.Map;

public class SummaryMetricsDefinitionBuilder {
    public SummaryMetricsDefinitionBuilder(StringWriter writer, String name, Map<String, String> globalLabels) {
        this.writer = writer;
        this.name = name;
        this.globalLabels = globalLabels;
    }

    private final StringWriter writer;
    private final String name;
    private final Map<String, String> globalLabels;

    public SummaryValueWriter withHelp(String help) {
        HelpWriter.writeHelp(writer, MetricType.SUMMARY, name, help);
        return new SummaryValueWriter(writer, name, globalLabels);
    }

    public SummaryValueWriter noHelp() {
        HelpWriter.writeHelp(writer, MetricType.SUMMARY, name, null);
        return new SummaryValueWriter(writer, name, globalLabels);
    }
}
