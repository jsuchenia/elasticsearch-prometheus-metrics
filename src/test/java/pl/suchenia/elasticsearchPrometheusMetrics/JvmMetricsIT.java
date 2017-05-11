package pl.suchenia.elasticsearchPrometheusMetrics;

import org.elasticsearch.test.ESTestCase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class JvmMetricsIT extends ESTestCase {
    public void tetsJvmOutput() throws Exception {
        String stringAddress = Objects.requireNonNull(System.getProperty("external.address"));
        URL url = new URL("http://" + stringAddress);
        InetAddress address = InetAddress.getByName(url.getHost());
        try (Socket socket = new Socket(address, url.getPort());
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            assertEquals("TEST", reader.readLine());
        }
    }
}
