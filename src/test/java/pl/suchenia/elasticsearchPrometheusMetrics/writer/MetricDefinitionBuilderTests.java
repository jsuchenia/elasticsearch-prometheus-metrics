package pl.suchenia.elasticsearchPrometheusMetrics.writer;

import org.elasticsearch.test.ESTestCase;

import java.io.IOException;
import java.io.StringWriter;


public class MetricDefinitionBuilderTests extends ESTestCase {


    public void testCounterHeaderGeneratedWithHelp() throws IOException {
        //given
        StringWriter writer = new StringWriter();

        //when
        new MetricDefinitionBuilder(writer, "counter", "tag_name").withHelp("Example help");

        //then
        assertEquals("#HELP tag_name Example help\n#TYPE counter\n", writer.toString());
    }

    public void testCounterHeaderGeneratedWithoutHelp() throws IOException {
        //given
        StringWriter writer = new StringWriter();

        //when
        new MetricDefinitionBuilder(writer, "counter", "tag_name").noHelp();

        //then
        assertEquals("#HELP tag_name\n#TYPE counter\n", writer.toString());
    }

    public void testCounterWithSimpleValue() throws IOException {
        //given
        StringWriter writer = new StringWriter();

        //when
        new MetricDefinitionBuilder(writer, "counter", "tag_name").noHelp().value(10);

        //then
        assertEquals("#HELP tag_name\n#TYPE counter\ntag_name 10.0\n", writer.toString());
    }

    public void testCounterWithLabeledValue() throws IOException {
        //given
        StringWriter writer = new StringWriter();

        //when
        new MetricDefinitionBuilder(writer, "counter", "tag_name")
                .noHelp().value("label", "value", 10);

        //then
        assertEquals("#HELP tag_name\n#TYPE counter\ntag_name{label=\"value\",} 10.0\n",
                writer.toString());
    }
}
