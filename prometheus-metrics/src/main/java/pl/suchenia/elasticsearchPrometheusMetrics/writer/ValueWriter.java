package pl.suchenia.elasticsearchPrometheusMetrics.writer;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

class ValueWriter {
    private final StringWriter writer;
    private final Map<String, String> sharedLabels;


    ValueWriter(StringWriter writer, Map<String, String> sharedLabels) {
        this.writer = writer;
        this.sharedLabels = new HashMap<>(sharedLabels);
    }

    void addSharedLabel(String labelName, String labelValue) {
        this.sharedLabels.put(labelName, labelValue);
    }

    void writeValue(String keyName, double value, Map<String, String> labels) {
        writer.append(keyName);
        if (isNonEmpty(sharedLabels) || isNonEmpty(labels)) {
            writer.append("{");
            writeLabelsMap(sharedLabels);
            writeLabelsMap(labels);
            writer.append("} ");
        } else {
            writer.append(" ");
        }
        writer.append(Double.toString(value));
        writer.append("\n");
    }

    private void writeEscapedLabelValue(String labelValue) {
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

    private void writeEscapedLabelName(String labelName) {
        for (int i = 0; i < labelName.length(); i++) {
            char c = labelName.charAt(i);
            switch (c) {
                case '.':
                case ':':
                case '-':
                    writer.append("_");
                    break;
                default:
                    writer.append(c);
            }
        }
    }

    private static boolean isNonEmpty(Map<String, String> labels) {
        return labels != null && labels.size() > 0;
    }

    private void writeLabelsMap(Map<String, String> labels) {
        if (isNonEmpty(labels)) {
            labels.forEach((labelName, labelValue) -> {
                writeEscapedLabelName(labelName);
                writer.append("=\"");
                writeEscapedLabelValue(labelValue);
                writer.append("\",");
            });
        }
    }
}
