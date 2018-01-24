package pl.suchenia.elasticsearchPrometheusMetrics.writer;

import java.io.StringWriter;
import java.util.Map;

public final class MetricDefinitionBuilder {
    private final String type;
    private final StringWriter writer;
    private final String name;
    private final Map<String, String> globalLabels;
    private String help;

    MetricDefinitionBuilder(StringWriter writer, String type, String name, Map<String, String> globalLabels) {
        this.writer = writer;
        this.type = type;
        this.name = name;
        this.globalLabels = globalLabels;
    }

    public ValueWriter withHelp(String help) {
        this.help = help;
        return this.noHelp();
    }

    public ValueWriter noHelp() {
        writer.append("#HELP ");
        writer.append(name);

        if (help != null) {
            writer.append(" ");
            writeEscapedHelp(help);
        }

        writer.append("\n#TYPE ");
        writer.append(type);
        writer.append("\n");

        return new ValueWriter(writer, name, globalLabels);
    }

    private void writeEscapedHelp(String help) {
        for (int i = 0; i < help.length(); i++) {
            char c = help.charAt(i);
            switch (c) {
                case '\\':
                    writer.append("\\\\");
                    break;
                case '\n':
                    writer.append("\\n");
                    break;
                default:
                    writer.append(c);
            }
        }
    }

}
