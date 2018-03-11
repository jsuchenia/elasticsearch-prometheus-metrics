package pl.suchenia.elasticsearchPrometheusMetrics;

import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.test.rest.ESRestTestCase;
import org.junit.Before;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.stream.Stream;

public class PrometheusExporterRestIT extends ESRestTestCase {
    private static final int INFO_ENTRIES = 1;
    private static final int CLUSTER_SETTINGS_ENTRIES = 3;
    private static final int CLUSTER_HEALTH_ENTRIES = 19;
    private static final int INDICES_ENTRIES = 21;
    private static final int TRANSPORT_ENTRIES = 5;
    private static final int JVM_ENTRIES = 12;
    private static final int OS_ENTRIES = 5;
    private static final int INGEST_ENTRIES = 8;
    private static final int PROCESS_ENTRIES = 5;
    private static final int TASKS_ENTRIES = 2;
    private static final int BREAKER_ENTRIES = 4;
    private static final int FS_ENTRIES = 6;
    private static final int THREAD_ENTRIES = 6;

    private static final int NODE_ENTRIES = TRANSPORT_ENTRIES + JVM_ENTRIES + OS_ENTRIES + INGEST_ENTRIES
            + PROCESS_ENTRIES + BREAKER_ENTRIES + FS_ENTRIES + INDICES_ENTRIES + THREAD_ENTRIES;
    private static final int ALL_ENTRIES = NODE_ENTRIES + CLUSTER_HEALTH_ENTRIES
            + CLUSTER_SETTINGS_ENTRIES + TASKS_ENTRIES;

    @Before
    public void initDb() throws IOException {
        NStringEntity entity = new NStringEntity("{\"a\": 2}", ContentType.APPLICATION_JSON);
        client().performRequest("PUT", "/testindex/_doc/1",
                Collections.<String, String>emptyMap(), entity);
    }

    public void testIfRestEndpointExistsWithProperNumberOfEntries() throws IOException {
        Response response = client().performRequest("GET", "/_prometheus");

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(ALL_ENTRIES + INFO_ENTRIES, countLines(response));
    }

    public void testIfNodeContainsProperNumberOfEntries() throws IOException {
        Response response = client().performRequest("GET", "/_prometheus/node");

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(NODE_ENTRIES + INFO_ENTRIES, countLines(response));
    }

    public void testIfClusterContainsProperNumberOfEntries() throws IOException {
        Response response = client().performRequest("GET", "/_prometheus/cluster");

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(CLUSTER_HEALTH_ENTRIES + INFO_ENTRIES, countLines(response));
    }

    public void testIfSettingsContainsProperNumberOfEntries() throws IOException {
        Response response = client().performRequest("GET", "/_prometheus/cluster_settings");

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(CLUSTER_SETTINGS_ENTRIES + INFO_ENTRIES, countLines(response));
    }

    public void testIfPendingTasksContainsProperNumberOfEntries() throws IOException {
        Response response = client().performRequest("GET", "/_prometheus/tasks");

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(TASKS_ENTRIES + INFO_ENTRIES, countLines(response));
    }

    private long countLines(Response response) throws IOException {
        return new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"))
                .lines()
                .filter((line) -> line.startsWith("#HELP "))
                .count();
    }
}
