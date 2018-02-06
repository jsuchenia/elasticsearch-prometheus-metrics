package pl.suchenia.elasticsearchPrometheusMetrics.writer;

import java.io.StringWriter;
import java.util.Map;

public class SingleMetricsDefinitionBuilder {
    public SingleMetricsDefinitionBuilder(MetricType type, StringWriter writer, String name, Map<String, String> globalLabels) {
        this.type = type;
        this.writer = writer;
        this.name = name;
        this.globalLabels = globalLabels;
    }

    private final MetricType type;
    private final StringWriter writer;
    private final String name;
    private final Map<String, String> globalLabels;

    public SingleValueWriter withHelp(String help) {
        HelpWriter.writeHelp(writer, type, name, help);
        return new SingleValueWriter(writer, name, globalLabels);
    }

    public SingleValueWriter noHelp() {
        HelpWriter.writeHelp(writer, type, name, null);
        return new SingleValueWriter(writer, name, globalLabels);
    }
}
