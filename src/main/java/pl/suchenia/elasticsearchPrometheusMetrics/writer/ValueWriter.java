package pl.suchenia.elasticsearchPrometheusMetrics.writer;

import java.io.IOException;
import java.io.StringWriter;

public final class ValueWriter {
    private final StringWriter writer;
    private final String name;

    ValueWriter(StringWriter writer, String name) {
        this.writer = writer;
        this.name = name;
    }

    public void value(double value) throws IOException {
        this.value(null, null, value);
    }

    public ValueWriter value(String labelName, String labelValue, double value) throws IOException {
        writer.append(name);
        if (labelName != null && labelValue != null) {
            writer.append("{");
            writer.append(labelName);
            writer.append("=\"");
            writeEscapedLabelValue(labelValue);
            writer.append("\",} ");
        } else {
            writer.append(" ");
        }
        writer.append(Double.toString(value));
        writer.append("\n");

        return this;
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
}
