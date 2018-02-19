package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.indices.breaker.AllCircuitBreakerStats;
import org.elasticsearch.indices.breaker.CircuitBreakerStats;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.SingleValueWriter;

public class CircuitBreakerMetricsGenerator extends MetricsGenerator<AllCircuitBreakerStats> {
    private static final Logger logger = Loggers.getLogger(CircuitBreakerMetricsGenerator.class);
    private static final String LABEL_NAME = "circuit_name";

    @Override
    public PrometheusFormatWriter generateMetrics(PrometheusFormatWriter writer, AllCircuitBreakerStats allStats) {
        logger.debug("Generating data about circuit breaker: {}", allStats);

        SingleValueWriter circuitBreakerLimit = writer
                .addGauge("es_breaker_limit_bytes")
                .withHelp("Memory limit of circuit breaker in bytes");

        SingleValueWriter circuitBreakerEstimated = writer
                .addGauge("es_breaker_estimated_bytes")
                .withHelp("Estimated memory of circuit breaker in bytes");

        SingleValueWriter circuitBreakerTripped = writer
                .addCounter("es_breaker_tripped")
                .withHelp("Counter of how many times circuit breaker tripped");


        SingleValueWriter circuitBreakerOverhead = writer.addGauge("es_breaker_overhead")
                .withHelp("Overhead factor for circuit breaker");

        for (CircuitBreakerStats cbStats : allStats.getAllStats()) {
            String name = cbStats.getName();

            circuitBreakerEstimated.value(cbStats.getEstimated(), LABEL_NAME, name);
            circuitBreakerLimit.value(cbStats.getLimit(), LABEL_NAME, name);
            circuitBreakerTripped.value(cbStats.getTrippedCount(), LABEL_NAME, name);
            circuitBreakerOverhead.value(cbStats.getOverhead(), LABEL_NAME, name);
        }
        return writer;
    }
}
