package pl.suchenia.elasticsearchPrometheusMetrics.writer;

import java.io.IOException;
import java.io.StringWriter;

public final class MetricDefinitionBuilder {
    private final String type;
    private final StringWriter writer;
    private final String name;
    private String help;

    MetricDefinitionBuilder(StringWriter writer, String type, String name) {
        this.writer = writer;
        this.type = type;
        this.name = name;
    }

    public ValueWriter withHelp(String help) throws IOException {
        this.help = help;
        return this.noHelp();
    }

    public ValueWriter noHelp() throws IOException {
        writer.append("#HELP ");
        writer.append(name);

        if (help != null) {
            writer.append(" ");
            writeEscapedHelp(help);
        }

        writer.append("\n#TYPE ");
        writer.append(type);
        writer.append("\n");

        return new ValueWriter(writer, name);
    }

    private void writeEscapedHelp(String help) throws IOException {
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
