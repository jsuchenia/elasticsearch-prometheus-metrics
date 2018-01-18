package pl.suchenia.elasticsearchPrometheusMetrics.writer;

import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ValueWriter {
    private final StringWriter writer;
    private final String name;
    private Map<String, String> sharedLabels;

    ValueWriter(StringWriter writer, String name) {
        this.writer = writer;
        this.name = name;
    }

    public ValueWriter withSharedLabel(String labelName, String labelValue) {
        if (sharedLabels == null) {
            sharedLabels = new HashMap<>();
        }

        sharedLabels.put(labelName, labelValue);
        return this;
    }

    public void value(double value) {
        this.value(value, Collections.emptyMap());
    }

    public ValueWriter value(double value, Map<String, String> labels) {
        writeValue(name, value, labels);
        return this;
    }

    private void writeValue(String keyName, double value, Map<String, String> labels) {
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

    public ValueWriter value(double value, String...labels) {
        if (labels.length % 2 != 0) {
            throw new IllegalArgumentException("Wrong number of labels, should be in pairs..");
        }

        Map<String, String> paramsMap = new LinkedHashMap<>();
        for (int i = 0; i < labels.length; i++) {
            paramsMap.put(labels[i], labels[++i]);
        }

        return this.value(value, paramsMap);
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

    public void summary(long collectionCount, long millis, String...labels) {
        if (labels.length % 2 != 0) {
            throw new IllegalArgumentException("Wrong number of labels, should be in pairs..");
        }
        Map<String, String> paramsMap = new LinkedHashMap<>();
        for (int i = 0; i < labels.length; i++) {
            paramsMap.put(labels[i], labels[++i]);
        }
        writeValue(name + "_count", collectionCount, paramsMap);
        writeValue(name + "_sum", millis, paramsMap);
    }
}
