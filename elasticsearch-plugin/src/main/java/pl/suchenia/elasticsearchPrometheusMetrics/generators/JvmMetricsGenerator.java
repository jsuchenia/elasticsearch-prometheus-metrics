package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.monitor.jvm.JvmStats;
import org.elasticsearch.monitor.jvm.JvmStats.GarbageCollector;
import pl.suchenia.elasticsearchPrometheusMetrics.PrometheusExporterPlugin;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.SummaryValueWriter;

/*
 Aligned with process data returned by JVM client

 https://github.com/prometheus/client_java/blob/master/simpleclient_hotspot/src/main/java/io/prometheus/client/hotspot/
 including:
   StandardExports.java
   MemoryPoolsExports.java
*/


public class JvmMetricsGenerator extends MetricsGenerator<JvmStats> {
    private static final Logger logger = Loggers.getLogger(PrometheusExporterPlugin.class, "init");

    @Override
    public PrometheusFormatWriter generateMetrics(PrometheusFormatWriter writer, JvmStats jvmStats) {
        logger.debug("Generating output for JVM stats: {}", jvmStats);

        logger.debug("Now memory: {}", jvmStats.getMem());
        writer.addGauge("jvm_memory_bytes_used")
                .withHelp("Used bytes of a given JVM memory area.")
                .value(jvmStats.getMem().getHeapUsed().getBytes(), "area", "heap")
                .value(jvmStats.getMem().getNonHeapUsed().getBytes(), "area", "nonheap");


        writer.addGauge("jvm_memory_bytes_committed")
                .withHelp("Committed (bytes) of a given JVM memory area")
                .value(jvmStats.getMem().getHeapCommitted().getBytes(), "area", "heap")
                .value(jvmStats.getMem().getNonHeapCommitted().getBytes(), "area", "nonheap");

        writer.addGauge("jvm_memory_bytes_max")
                .withHelp("Max (bytes) of a given JVM memory area.")
                .value(jvmStats.getMem().getHeapMax().getBytes(), "area", "heap");


        //JVM threads
        logger.debug("Now threads: {}", jvmStats.getThreads());
        writer.addGauge("jvm_threads_current")
                .withHelp("Current thread count of a JVM")
                .value(jvmStats.getThreads().getCount());

        writer.addGauge("jvm_threads_peak")
                .withHelp("Peak thread count of a JVM")
                .value(jvmStats.getThreads().getPeakCount());

        //JVM classes
        logger.debug("Now classes: {}", jvmStats.getClasses());
        writer.addGauge("jvm_classes_loaded")
                .withHelp("The number of classes that are currently loaded in the JVM")
                .value(jvmStats.getClasses().getLoadedClassCount());

        writer.addCounter("jvm_classes_loaded_total")
                .withHelp("The total number of classes that have been loaded since the JVM has started execution")
                .value(jvmStats.getClasses().getTotalLoadedClassCount());

        writer.addCounter("jvm_classes_unloaded_total")
                .withHelp("The total number of classes that have been unloaded since the JVM has started execution")
                .value(jvmStats.getClasses().getUnloadedClassCount());

        SummaryValueWriter gcValueWriter = writer.addSummary("jvm_gc_collection_seconds")
                .withHelp("The total number of seconds spend on GC collection");

        for (GarbageCollector collector : jvmStats.getGc().getCollectors()) {
            gcValueWriter.summary(collector.getCollectionCount(), collector.getCollectionTime().getMillis(), "name", collector.getName());
        }
        
        //ES custom fields
        logger.debug("Now custom: {}", jvmStats.getClasses());

        writer.addGauge("es_jvm_timestamp")
                .withHelp("Timestamp of last JVM status scrap")
                .value(jvmStats.getTimestamp());

        writer.addGauge("es_jvm_uptime")
                .withHelp("Node uptime in millis")
                .value(jvmStats.getUptime().millis());

        writer.addGauge("es_jvm_memory_heap_used_percent")
                .withHelp("Heap memory of JVM (in percentage)")
                .value(jvmStats.getMem().getHeapUsedPercent());

        return writer;
    }
}
