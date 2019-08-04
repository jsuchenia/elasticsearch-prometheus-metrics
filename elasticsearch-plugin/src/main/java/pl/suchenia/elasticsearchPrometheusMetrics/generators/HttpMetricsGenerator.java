package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.http.HttpStats;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;

public class HttpMetricsGenerator extends MetricsGenerator<HttpStats> {
    private static final Logger logger = Loggers.getLogger(HttpMetricsGenerator.class, "init");

    @Override
    public PrometheusFormatWriter generateMetrics(PrometheusFormatWriter writer, HttpStats httpStats) {
        writer.addGauge("es_http_server_open")
            .withHelp("Number of opened HTTP connection")
            .value(httpStats.getServerOpen());
        writer.addGauge("es_http_total_open")
                .withHelp("Number of totally opened HTTP connection")
                .value(httpStats.getTotalOpen());

        return writer;
    }
}
