package pl.suchenia.elasticsearchPrometheusMetrics;

import org.elasticsearch.client.Response;
import org.elasticsearch.test.rest.ESRestTestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Stream;

public class PrometheusExporterRestIT extends ESRestTestCase {
    private static final int INFO_ENTRIES = 1;
    private static final int CLUSTER_SETTINGS_ENTRIES = 3;
    private static final int CLUSTER_HEALTH_ENTRIES = 19;
    private static final int INDICES_ENTRIES = 11;
    private static final int TRANSPORT_ENTRIES = 5;
    private static final int JVM_ENTRIES = 12;
    private static final int OS_ENTRIES = 5;
    private static final int INGEST_ENTRIES = 8;
    private static final int PROCESS_ENTRIES = 5;

    private static final int NODE_ENTRIES = TRANSPORT_ENTRIES + JVM_ENTRIES + OS_ENTRIES + INGEST_ENTRIES + PROCESS_ENTRIES;
    private static final int ALL_ENTRIES = NODE_ENTRIES + INDICES_ENTRIES + CLUSTER_HEALTH_ENTRIES + CLUSTER_SETTINGS_ENTRIES;


    public void testIfRestEndpointExistsWithProperNumberOfEntries() throws IOException {
        Response response = client().performRequest("GET", "/_prometheus");

        assertEquals(200, response.getStatusLine().getStatusCode());

        Stream<String> data = getLines(response);
        assertEquals(ALL_ENTRIES + INFO_ENTRIES, data.filter((line) -> line.startsWith("#HELP ")).count());
    }

    public void testIfNodeContainsProperNumberOfEntries() throws IOException {
        Response response = client().performRequest("GET", "/_prometheus/node");

        assertEquals(200, response.getStatusLine().getStatusCode());

        Stream<String> data = getLines(response);
        assertEquals(NODE_ENTRIES + INFO_ENTRIES, data.filter((line) -> line.startsWith("#HELP ")).count());
    }

    public void testIfIndicesContainsProperNumberOfEntries() throws IOException {
        Response response = client().performRequest("GET", "/_prometheus/indices");

        assertEquals(200, response.getStatusLine().getStatusCode());

        Stream<String> data = getLines(response);
        assertEquals(INDICES_ENTRIES + INFO_ENTRIES, data.filter((line) -> line.startsWith("#HELP ")).count());
    }

    public void testIfClusterContainsProperNumberOfEntries() throws IOException {
        Response response = client().performRequest("GET", "/_prometheus/cluster");

        assertEquals(200, response.getStatusLine().getStatusCode());

        Stream<String> data = getLines(response);
        assertEquals(CLUSTER_HEALTH_ENTRIES + INFO_ENTRIES, data.filter((line) -> line.startsWith("#HELP ")).count());
    }

    public void testIfSettingsContainsProperNumberOfEntries() throws IOException {
        Response response = client().performRequest("GET", "/_prometheus/cluster_settings");

        assertEquals(200, response.getStatusLine().getStatusCode());

        Stream<String> data = getLines(response);
        assertEquals(CLUSTER_SETTINGS_ENTRIES + INFO_ENTRIES, data.filter((line) -> line.startsWith("#HELP ")).count());
    }

    private Stream<String> getLines(Response response) throws IOException {
        return new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8")).lines();
    }

}
