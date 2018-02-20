package pl.suchenia.elasticsearchPrometheusMetrics.writer;

import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SingleValueWriter {
    private final ValueWriter valueWriter;
    private final String name;

    SingleValueWriter(StringWriter writer, String name, Map<String, String> globalLabels) {
        this.valueWriter = new ValueWriter(writer, globalLabels);
        this.name = name;
    }

    public SingleValueWriter withSharedLabel(String labelName, String labelValue) {
        valueWriter.addSharedLabel(labelName, labelValue);
        return this;
    }

    public void value(double value) {
        this.value(value, Collections.emptyMap());
    }

    public SingleValueWriter value(double value, Map<String, String> labels) {
        valueWriter.writeValue(name, value, labels);
        return this;
    }


    public SingleValueWriter value(double value, String...labels) {
        if (labels.length % 2 != 0) {
            throw new IllegalArgumentException("Wrong number of labels, should be in pairs..");
        }

        Map<String, String> paramsMap = new LinkedHashMap<>();
        for (int i = 0; i < labels.length; i++) {
            paramsMap.put(labels[i], labels[++i]);
        }

        return this.value(value, paramsMap);
    }



    public void summary(long collectionCount, long millis, String...labels) {
        if (labels.length % 2 != 0) {
            throw new IllegalArgumentException("Wrong number of labels, should be in pairs..");
        }
        Map<String, String> paramsMap = new LinkedHashMap<>();
        for (int i = 0; i < labels.length; i++) {
            paramsMap.put(labels[i], labels[++i]);
        }
        valueWriter.writeValue(name + "_count", collectionCount, paramsMap);
        valueWriter.writeValue(name + "_sum", millis, paramsMap);
    }
}
