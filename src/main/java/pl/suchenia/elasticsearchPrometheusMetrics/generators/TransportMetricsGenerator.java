package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.transport.TransportStats;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;

public class TransportMetricsGenerator implements MetricsGenerator<TransportStats>{
    private static final Logger logger = Loggers.getLogger(OsMetricsGenerator.class);

    @Override
    public PrometheusFormatWriter generateMetrics(PrometheusFormatWriter writer, TransportStats transportStats) {
        logger.debug("Generating output for transport stats: {}", transportStats);

        writer.addGauge("es_transport_server_connections")
                .withHelp("Number of opened server connections")
                .value(transportStats.getServerOpen());

        writer.addGauge("es_transport_rx_packets_count")
                .withHelp("Number of receieved packets")
                .value(transportStats.rxCount());
        writer.addGauge("es_transport_rx_bytes_count")
                .withHelp("Number of receieved packets")
                .value(transportStats.rxSize().getBytes());
        writer.addGauge("es_transport_tx_packets_count")
                .withHelp("Number of send packets")
                .value(transportStats.txCount());
        writer.addGauge("es_transport_tx_bytes_count")
                .withHelp("Total size of send packets")
                .value(transportStats.txSize().getBytes());

        return writer;
    }
}
