package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.monitor.process.ProcessStats;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;

/*
 Aligned with process data returned by JVM and golang clients

 https://github.com/prometheus/client_java/blob/master/simpleclient_hotspot/src/main/java/io/prometheus/client/hotspot/StandardExports.java
 https://github.com/prometheus/client_golang/blob/master/prometheus/process_collector.go
*/

public class ProcessMetricsGenerator implements MetricsGenerator<ProcessStats> {
    private static final Logger logger = Loggers.getLogger(OsMetricsGenerator.class);

    @Override
    public PrometheusFormatWriter generateMetrics(PrometheusFormatWriter writer, ProcessStats processStats) {
        logger.debug("Generating output for Process stats: {}", processStats);

        writer.addGauge("process_open_fds")
                .withHelp("Number of opened FD handles")
                .value(processStats.getOpenFileDescriptors());

        writer.addGauge("process_max_fds")
                .withHelp("Number of max FD handles available")
                .value(processStats.getOpenFileDescriptors());

        writer.addGauge("process_cpu_seconds_total")
                .withHelp("Total user and system CPU time spent in seconds.")
        .value(processStats.getCpu().getTotal().getSeconds());

        writer.addGauge("process_start_time_seconds")
                .withHelp("Start time of the process since unix epoch in seconds.")
                .value(processStats.getTimestamp());

        writer.addGauge("process_virtual_memory_bytes")
                .withHelp("Virtual memory size in bytes.")
                .value(processStats.getMem().getTotalVirtual().getBytes());

        return writer;
    }
}
