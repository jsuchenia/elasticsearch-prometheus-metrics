package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.monitor.jvm.JvmStats;
import pl.suchenia.elasticsearchPrometheusMetrics.PrometheusExporterPlugin;

import java.io.IOException;
import java.io.Writer;

public class JvmMetricsGenerator {
    private static final Logger logger = Loggers.getLogger(PrometheusExporterPlugin.class);

    public void generateMetrics(PrometheusFormatWriter writer, JvmStats jvmStats) throws Exception {
        logger.debug("Generating output for JVM stats: {}", jvmStats);

        //Aligned with prometheus client_java
        writer.generateGaugeHelp("jvm_memory_bytes_used", "Used bytes of a given JVM memory area.");
        writer.generateValue("jvm_memory_bytes_used", "area", "heap",
                jvmStats.getMem().getHeapUsed().getBytes());
        writer.generateValue("jvm_memory_bytes_used", "area", "nonheap",
                 jvmStats.getMem().getNonHeapUsed().getBytes());

        writer.generateGaugeHelp("jvm_memory_bytes_committed", "Committed (bytes) of a given JVM memory area");
        writer.generateValue("jvm_memory_bytes_committed", "area", "heap",
                jvmStats.getMem().getHeapCommitted().getBytes());
        writer.generateValue("jvm_memory_bytes_committed", "area", "nonheap",
                jvmStats.getMem().getNonHeapCommitted().getBytes());

        writer.generateGaugeHelp("jvm_memory_bytes_max", "Max (bytes) of a given JVM memory area.");
        writer.generateValue("jvm_memory_bytes_max", "area", "heap", jvmStats.getMem().getHeapMax().getBytes());


        writer.generateGaugeHelp("jvm_threads_current", "Current thread count of a JVM");
        writer.generateValue("jvm_threads_current", jvmStats.getThreads().getCount());

        writer.generateGaugeHelp("jvm_threads_peak", "Peak thread count of a JVM");
        writer.generateValue("jvm_threads_peak", jvmStats.getThreads().getPeakCount());

        //ES custom fields
        writer.generateGaugeHelp("ec_jvm_timestamp", "Timestamp of last JVM status scrap");
        writer.generateValue("ec_jvm_timestamp", jvmStats.getTimestamp());

        writer.generateGaugeHelp("ec_jvm_uptime", "Node uptime in millis");
        writer.generateValue("ec_jvm_uptime", jvmStats.getUptime().millis());

        writer.generateGaugeHelp("ec_jvm_memory_heap_used_percent", "Heap memory of JVM (in percentage)");
        writer.generateValue("ec_jvm_memory_heap_used_percent", jvmStats.getMem().getHeapUsedPercent());
        //TODO: memory pools and garbage collectors
    }
}
