package pl.suchenia.elasticsearchPrometheusMetrics;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.rest.RestRequest;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;

import java.util.concurrent.CompletableFuture;

import static org.elasticsearch.rest.RestStatus.INTERNAL_SERVER_ERROR;
import static org.elasticsearch.rest.RestStatus.OK;

@FunctionalInterface
public interface StringBufferedRestHandler extends RestHandler {
    @Override
    default void handleRequest(RestRequest request, RestChannel channel, NodeClient client) throws Exception {
        generateResponse(channel, client)
                .thenAccept((writer) ->channel.sendResponse(new BytesRestResponse(OK,
                        "text/plain; version=0.0.4; charset=utf-8",
                        writer.toString())))
                .exceptionally((e) -> {
                        channel.sendResponse(new BytesRestResponse(INTERNAL_SERVER_ERROR,
                            "text/plain; charset=utf-8",
                            e.toString()));
                        return null;
                });
    }

    CompletableFuture<PrometheusFormatWriter> generateResponse(RestChannel channel, NodeClient client);
}
