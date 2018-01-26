package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.monitor.os.OsStats;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;

public class OsMetricsGenerator implements MetricsGenerator<OsStats> {
    private static final Logger logger = Loggers.getLogger(OsMetricsGenerator.class);

    @Override
    public void generateMetrics(PrometheusFormatWriter writer, OsStats osStats) {
        logger.debug("Generating output for OS stats: {}", osStats);

        writer.addGauge("es_cpu_percentage")
                .withHelp("ElasticSearch CPU percentage")
                .value(osStats.getCpu().getPercent());

        writer.addGauge("es_cpu_loadavg")
                .withHelp("Elasticsearch CPU loadavg")
                .value(osStats.getCpu().getLoadAverage()[0],"loadavg", "1m")
                .value(osStats.getCpu().getLoadAverage()[1],"loadavg", "5m")
                .value(osStats.getCpu().getLoadAverage()[2],"loadavg", "15m");

        writer.addGauge("es_memory")
                .withHelp("Elasticsearch memory stats")
                .value(osStats.getMem().getFree().getBytes(), "memtype", "free")
                .value(osStats.getMem().getUsed().getBytes(), "memtype", "used")
                .value(osStats.getMem().getTotal().getBytes(), "memtype", "total");

        writer.addGauge("es_memory_free_percentage")
                .withHelp("Elasticsearch memory free percentage")
                .value(osStats.getMem().getFreePercent());

        writer.addGauge("es_swap")
                .withHelp("Elasticsearch swap stats")
                .value(osStats.getSwap().getFree().getBytes(), "memtype", "free")
                .value(osStats.getSwap().getUsed().getBytes(), "memtype", "used")
                .value(osStats.getSwap().getTotal().getBytes(), "memtype", "total");
    }
}
