package pl.suchenia.elasticsearchPrometheusMetrics;

import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.test.rest.ESRestTestCase;
import org.junit.Before;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PrometheusExporterRestIT extends ESRestTestCase {
    private static final int INFO_ENTRIES = 1;
    private static final int CLUSTER_SETTINGS_ENTRIES = 3;
    private static final int CLUSTER_HEALTH_ENTRIES = 20;
    private static final int INDICES_ENTRIES = 27;
    private static final int TRANSPORT_ENTRIES = 5;
    private static final int JVM_ENTRIES = 12;
    private static final int OS_ENTRIES = 5;
    private static final int INGEST_ENTRIES = 8;
    private static final int PROCESS_ENTRIES = 5;
    private static final int TASKS_ENTRIES = 2;
    private static final int BREAKER_ENTRIES = 4;
    private static final int FS_ENTRIES = 6;
    private static final int THREAD_ENTRIES = 6;
    private static final int NODE_OTHER = 4;
    private static final int NODE_HTTP = 2;
    private static final int NODE_SCRIPTS = 3;
    private static final int REST_ENTRIES = 1;

    private static final int NODE_ENTRIES = TRANSPORT_ENTRIES + JVM_ENTRIES + OS_ENTRIES + INGEST_ENTRIES
            + PROCESS_ENTRIES + BREAKER_ENTRIES + FS_ENTRIES + INDICES_ENTRIES + THREAD_ENTRIES + NODE_OTHER
            + NODE_HTTP + NODE_SCRIPTS;
    private static final int ALL_ENTRIES = NODE_ENTRIES + CLUSTER_HEALTH_ENTRIES
            + CLUSTER_SETTINGS_ENTRIES + TASKS_ENTRIES + REST_ENTRIES;

    @Before
    public void initDb() throws IOException {
        Request request = new Request("PUT", "/testindex/_doc/1");
        request.setEntity(new NStringEntity("{\"a\": 2}", ContentType.APPLICATION_JSON));
        client().performRequest(request);
    }

    public void testIfRestEndpointExistsWithProperNumberOfEntries() throws IOException {
        Response response = aRequest("GET", "/_prometheus");

        validateResponse(response, ALL_ENTRIES + INFO_ENTRIES);
    }

    public void testIfNodeContainsProperNumberOfEntries() throws IOException {
        Response response = aRequest("GET", "/_prometheus/node");

        validateResponse(response,NODE_ENTRIES + INFO_ENTRIES);
    }

    public void testIfClusterContainsProperNumberOfEntries() throws IOException {
        Response response = aRequest("GET", "/_prometheus/cluster");

        validateResponse(response,CLUSTER_HEALTH_ENTRIES + INFO_ENTRIES);
    }

    public void testIfSettingsContainsProperNumberOfEntries() throws IOException {
        Response response = aRequest("GET", "/_prometheus/cluster_settings");

        validateResponse(response,CLUSTER_SETTINGS_ENTRIES + INFO_ENTRIES);
    }

    public void testIfPendingTasksContainsProperNumberOfEntries() throws IOException {
        Response response = aRequest("GET", "/_prometheus/tasks");

        validateResponse(response,TASKS_ENTRIES + INFO_ENTRIES);
    }

    public void testIfRestUsageContainsProperNumberOfEntries() throws IOException {
        Response response = aRequest("GET", "/_prometheus/rest");

        validateResponse(response, REST_ENTRIES + INFO_ENTRIES);
    }

    private void validateResponse(Response response, int numberOfRequests) throws IOException {
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(numberOfRequests, countLines(response));

    }

    private Response aRequest(String method, String endpoint) throws IOException {
        return client().performRequest(new Request(method, endpoint));
    }
    private long countLines(Response response) throws IOException {
        return new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"))
                .lines()
                .filter((line) -> line.startsWith("#HELP "))
                .count();
    }
}
