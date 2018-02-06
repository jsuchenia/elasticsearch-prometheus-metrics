package pl.suchenia.elasticsearchPrometheusMetrics.writer;

import org.elasticsearch.test.ESTestCase;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

public class SummaryMetricsDefinitionBuilderTests extends ESTestCase {
    private static Map<String, String> GLOBALS = Collections.singletonMap("a", "b");

    public void testCounterHeaderGeneratedWithHelp() {
        //given
        StringWriter writer = new StringWriter();

        //when
        new SummaryMetricsDefinitionBuilder(writer, "tag_name", GLOBALS).withHelp("Example help");

        //then
        assertEquals("#HELP tag_name Example help\n#TYPE summary\n", writer.toString());
    }

    public void testCounterHeaderGeneratedWithoutHelp() {
        //given
        StringWriter writer = new StringWriter();

        //when
        new SummaryMetricsDefinitionBuilder(writer, "tag_name", GLOBALS).noHelp();

        //then
        assertEquals("#HELP tag_name\n#TYPE summary\n", writer.toString());
    }

    public void testCounterWithSimpleValue() {
        //given
        StringWriter writer = new StringWriter();

        //when
        new SummaryMetricsDefinitionBuilder(writer, "tag_name", GLOBALS).noHelp().summary(10, 20);

        //then
        assertEquals("#HELP tag_name\n#TYPE summary\n"
                + "tag_name_count{a=\"b\",} 10.0\ntag_name_sum{a=\"b\",} 20.0\n", writer.toString());
    }

    public void testCounterWithLabeledValue() {
        //given
        StringWriter writer = new StringWriter();

        //when
        new SummaryMetricsDefinitionBuilder(writer, "tag_name", GLOBALS)
                .noHelp().summary(10, 20, "label", "value");

        //then
        assertEquals(
                "#HELP tag_name\n#TYPE summary\ntag_name_count{a=\"b\",label=\"value\",} 10.0\n"
                        +"tag_name_sum{a=\"b\",label=\"value\",} 20.0\n", writer.toString());
    }
    public void testCounterWithMultipleLabels() {
        //given
        StringWriter writer = new StringWriter();

        //when
        new SummaryMetricsDefinitionBuilder(writer, "tag_name", GLOBALS)
                .noHelp()
                .withSharedLabel("sharedLabel", "sharedValue")
                .summary(10, 20, "label", "value", "label2", "value2");

        //then
        assertEquals(
                "#HELP tag_name\n#TYPE summary\n"
                        + "tag_name_count{a=\"b\",sharedLabel=\"sharedValue\",label=\"value\",label2=\"value2\",} 10.0\n"
                        + "tag_name_sum{a=\"b\",sharedLabel=\"sharedValue\",label=\"value\",label2=\"value2\",} 20.0\n",
                writer.toString());
    }

}
