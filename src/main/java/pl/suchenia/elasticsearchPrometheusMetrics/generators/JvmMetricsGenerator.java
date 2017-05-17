package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.monitor.jvm.JvmStats;
import pl.suchenia.elasticsearchPrometheusMetrics.PrometheusExporterPlugin;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;

public class JvmMetricsGenerator implements MetricsGenerator<JvmStats> {
    private static final Logger logger = Loggers.getLogger(PrometheusExporterPlugin.class);

    @Override
    public void generateMetrics(PrometheusFormatWriter writer, JvmStats jvmStats) {
        logger.info("Generating output for JVM stats: {}", jvmStats);

        //Aligned with prometheus client_java
        logger.info("Now memory: {}", jvmStats.getMem());
        writer.addGauge("jvm_memory_bytes_used")
                .withHelp("Used bytes of a given JVM memory area.")
                .value("area", "heap", jvmStats.getMem().getHeapUsed().getBytes())
                .value("area", "nonheap", jvmStats.getMem().getNonHeapUsed().getBytes());


        writer.addGauge("jvm_memory_bytes_committed")
                .withHelp("Committed (bytes) of a given JVM memory area")
                .value("area", "heap", jvmStats.getMem().getHeapCommitted().getBytes())
                .value("area", "nonheap", jvmStats.getMem().getNonHeapCommitted().getBytes());

        writer.addGauge("jvm_memory_bytes_max")
                .withHelp("Max (bytes) of a given JVM memory area.")
                .value("area", "heap", jvmStats.getMem().getHeapMax().getBytes());


        logger.info("Now threads: {}", jvmStats.getThreads());

        writer.addGauge("jvm_threads_current")
                .withHelp("Current thread count of a JVM")
                .value(jvmStats.getThreads().getCount());

        writer.addGauge("jvm_threads_peak")
                .withHelp("Peak thread count of a JVM")
                .value(jvmStats.getThreads().getPeakCount());

        logger.info("Now classes: {}", jvmStats.getClasses());

        writer.addGauge("jvm_classes_loaded")
                .withHelp("The number of classes that are currently loaded in the JVM")
                .value(jvmStats.getClasses().getLoadedClassCount());

        writer.addCounter("jvm_classes_loaded_total")
                .withHelp("The total number of classes that have been loaded since the JVM has started execution")
                .value(jvmStats.getClasses().getTotalLoadedClassCount());

        writer.addCounter("jvm_classes_unloaded_total")
                .withHelp("The total number of classes that have been unloaded since the JVM has started execution")
                .value(jvmStats.getClasses().getUnloadedClassCount());

        //ES custom fields
        logger.info("Now custom: {}", jvmStats.getClasses());

        writer.addGauge("ec_jvm_timestamp")
                .withHelp("Timestamp of last JVM status scrap")
                .value(jvmStats.getTimestamp());

        writer.addGauge("ec_jvm_uptime")
                .withHelp("Node uptime in millis")
                .value(jvmStats.getUptime().millis());

        writer.addGauge("ec_jvm_memory_heap_used_percent")
                .withHelp("Heap memory of JVM (in percentage)")
                .value(jvmStats.getMem().getHeapUsedPercent());

        //TODO: memory pools
    }
}
