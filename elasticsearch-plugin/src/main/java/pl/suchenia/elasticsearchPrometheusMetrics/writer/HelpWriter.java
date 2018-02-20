package pl.suchenia.elasticsearchPrometheusMetrics.writer;

import java.io.StringWriter;
import java.util.Locale;

import static java.util.Locale.ENGLISH;

class HelpWriter {
    public static void writeHelp(StringWriter writer, MetricType type, String name, String help) {
        writer.append("#HELP ");
        writer.append(name);

        if (help != null) {
            writer.append(" ");
            writeEscapedHelp(help, writer);
        }

        writer.append("\n#TYPE ");
        writer.append(type.name().toLowerCase(ENGLISH));
        writer.append("\n");
    }

    private static void writeEscapedHelp(String help, StringWriter writer) {
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
