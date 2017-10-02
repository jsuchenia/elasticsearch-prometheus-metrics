package pl.suchenia.elasticsearchPrometheusMetrics;

import org.elasticsearch.client.Response;
import org.elasticsearch.test.rest.ESRestTestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Stream;

public class PrometheusExporterRestIT extends ESRestTestCase {
    public void testIfRestEndpointExistsWithProperNumberOfEntries() throws IOException {
        Response response = client().performRequest("GET", "/_prometheus");

        assertEquals(200, response.getStatusLine().getStatusCode());

        Stream<String> data = getLines(response);
        assertEquals(12 + 10 + 19 + 5, data.filter((line) -> line.startsWith("#HELP ")).count());
    }

    public void testIfJvmContainsProperNumberOfEntries() throws IOException {
        Response response = client().performRequest("GET", "/_prometheus/jvm");

        assertEquals(200, response.getStatusLine().getStatusCode());

        Stream<String> data = getLines(response);
        assertEquals(12, data.filter((line) -> line.startsWith("#HELP ")).count());
    }

    public void testIfIndicesContainsProperNumberOfEntries() throws IOException {
        Response response = client().performRequest("GET", "/_prometheus/indices");

        assertEquals(200, response.getStatusLine().getStatusCode());

        Stream<String> data = getLines(response);
        assertEquals(10, data.filter((line) -> line.startsWith("#HELP ")).count());
    }

    public void testIfClusterContainsProperNumberOfEntries() throws IOException {
        Response response = client().performRequest("GET", "/_prometheus/cluster");

        assertEquals(200, response.getStatusLine().getStatusCode());

        Stream<String> data = getLines(response);
        assertEquals(19, data.filter((line) -> line.startsWith("#HELP ")).count());
    }

    public void testIfOsContainsProperNumberOfEntries() throws IOException {
        Response response = client().performRequest("GET", "/_prometheus/os");

        assertEquals(200, response.getStatusLine().getStatusCode());

        Stream<String> data = getLines(response);
        assertEquals(5, data.filter((line) -> line.startsWith("#HELP ")).count());
    }

    private Stream<String> getLines(Response response) throws IOException {
        return new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8")).lines();
    }

}
