package pl.suchenia.elasticsearchPrometheusMetrics.writer;

import org.elasticsearch.test.ESTestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

import static pl.suchenia.elasticsearchPrometheusMetrics.writer.MetricType.COUNTER;


public class SingleMetricDefinitionBuilderTests extends ESTestCase {

    private static Map<String, String> GLOBALS = Collections.singletonMap("a", "b");


    public void testCounterHeaderGeneratedWithHelp() {
        //given
        StringWriter writer = new StringWriter();

        //when
        new SingleMetricsDefinitionBuilder(COUNTER, writer, "tag_name", GLOBALS).withHelp("Example help");

        //then
        assertEquals("#HELP tag_name Example help\n#TYPE counter\n", writer.toString());
    }

    public void testCounterHeaderGeneratedWithoutHelp() {
        //given
        StringWriter writer = new StringWriter();

        //when
        new SingleMetricsDefinitionBuilder(COUNTER, writer, "tag_name", GLOBALS).noHelp();

        //then
        assertEquals("#HELP tag_name\n#TYPE counter\n", writer.toString());
    }

    public void testCounterWithSimpleValue() {
        //given
        StringWriter writer = new StringWriter();

        //when
        new SingleMetricsDefinitionBuilder(COUNTER, writer, "tag_name", GLOBALS).noHelp().value(10);

        //then
        assertEquals("#HELP tag_name\n#TYPE counter\ntag_name{a=\"b\",} 10.0\n", writer.toString());
    }

    public void testCounterWithLabeledValue() {
        //given
        StringWriter writer = new StringWriter();

        //when
        new SingleMetricsDefinitionBuilder(COUNTER, writer, "tag_name", GLOBALS)
                .noHelp().value(10, "label", "value");

        //then
        assertEquals("#HELP tag_name\n#TYPE counter\ntag_name{a=\"b\",label=\"value\",} 10.0\n",

                writer.toString());
    }
    public void testCounterWithMultipleLabels() {
        //given
        StringWriter writer = new StringWriter();

        //when
        new SingleMetricsDefinitionBuilder(COUNTER, writer, "tag_name", GLOBALS)
                .noHelp()
                .withSharedLabel("sharedLabel", "sharedValue")
                .value(10, "label", "value", "label2", "value2");

        //then
        assertEquals(
                "#HELP tag_name\n#TYPE counter\n"
                + "tag_name{a=\"b\",sharedLabel=\"sharedValue\",label=\"value\",label2=\"value2\",} 10.0\n",
                writer.toString());
    }
}
