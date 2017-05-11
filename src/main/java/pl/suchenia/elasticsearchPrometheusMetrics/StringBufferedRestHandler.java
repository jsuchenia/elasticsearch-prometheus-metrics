package pl.suchenia.elasticsearchPrometheusMetrics;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.rest.RestRequest;
import pl.suchenia.elasticsearchPrometheusMetrics.generators.PrometheusFormatWriter;

import java.io.StringWriter;
import java.io.Writer;

import static org.elasticsearch.rest.RestStatus.OK;

@FunctionalInterface
public interface StringBufferedRestHandler extends RestHandler {
    @Override
    default void handleRequest(RestRequest request, RestChannel channel, NodeClient client) throws Exception {
        PrometheusFormatWriter prometheusFormatWriterw = new PrometheusFormatWriter();
        generateStringResponse(prometheusFormatWriterw, client);
        channel.sendResponse(new BytesRestResponse(OK,
                "text/plain; version=0.0.4; charset=utf-8",
                prometheusFormatWriterw.toString()));
    }

    void generateStringResponse(PrometheusFormatWriter writer, NodeClient client) throws Exception;
}
