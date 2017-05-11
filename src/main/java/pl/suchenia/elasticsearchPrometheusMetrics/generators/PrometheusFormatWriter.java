package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class PrometheusFormatWriter {
    private final StringWriter writer = new StringWriter();

    public void generateCounterHelp(String entryName, String helpDescription) throws IOException {
        generateHelp(entryName, helpDescription, "counter");
    }

    public void generateGaugeHelp(String entryName, String helpDescription) throws IOException {
        generateHelp(entryName, helpDescription, "gauge");
    }

    public void generateHelp(String entryName, String helpDescription, String metricType) throws IOException {
        writer.append("#HELP ");
        writer.append(entryName);
        writer.append(" ");
        writeEscapedHelp(helpDescription);
        writer.append("\n#TYPE ");
        writer.append(metricType);
        writer.append("\n");
    }

    public void generateValue(String entryName, long value) throws IOException {
        writer.append(entryName);
        writer.append(" ");
        writer.append(Double.toString((double) value));

    }

    public void generateValue(String entryName, String labelName, String labelValue, long value) throws IOException {
        writer.append(entryName);
        writer.append("{");
        writer.append(labelName);
        writer.append("=\"");
        writeEscapedLabelValue(labelValue);
        writer.append("\",} ");
        writer.append(Double.toString((double) value));

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

    private void writeEscapedLabelValue(String labelValue) throws IOException {
        for (int i = 0; i < labelValue.length(); i++) {
            char c = labelValue.charAt(i);
            switch (c) {
                case '\\':
                    writer.append("\\\\");
                    break;
                case '\"':
                    writer.append("\\\"");
                    break;
                case '\n':
                    writer.append("\\n");
                    break;
                default:
                    writer.append(c);
            }
        }
    }

    public String toString() {
        return writer.toString();
    }
}
