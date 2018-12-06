package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.ingest.IngestStats;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.SingleValueWriter;

public class IngestMetricsGenerator extends MetricsGenerator<IngestStats> {
    private static final Logger logger = Loggers.getLogger(IngestMetricsGenerator.class, "init");

    @Override
    public PrometheusFormatWriter generateMetrics(PrometheusFormatWriter writer, IngestStats ingestStats) {
        logger.debug("Generating metrics about ingest stats: {}", ingestStats);

        writer.addCounter("es_ingest_total_count")
                .withHelp("Total number of processed documents during ingestion phase")
                .value(ingestStats.getTotalStats().getIngestCount());

        writer.addCounter("es_ingest_total_time_seconds")
                .withHelp("Total time spend on processed documents during ingestion phase")
                .value(ingestStats.getTotalStats().getIngestTimeInMillis() / 1000);

        writer.addGauge("es_ingest_total_current")
                .withHelp("The total number of ingest preprocessing operations that have failed")
                .value(ingestStats.getTotalStats().getIngestCurrent());

        writer.addGauge("es_ingest_total_failed_count")
                .withHelp("The total number of ingest preprocessing operations that have failed")
                .value(ingestStats.getTotalStats().getIngestFailedCount());


        SingleValueWriter es_ingest_pipeline_count = writer.addCounter("es_ingest_pipeline_count")
                .withHelp("Total number of processed documents during ingestion phase in pipeline");

        SingleValueWriter es_ingest_pipeline_time_seconds = writer.addCounter("es_ingest_pipeline_time_seconds")
                .withHelp("Total time spend on processed documents during ingestion phase in pipeline");

        SingleValueWriter es_ingest_pipeline_current = writer.addGauge("es_ingest_pipeline_current")
                .withHelp("The total number of ingest preprocessing operations that have failed in pipeline");

        SingleValueWriter es_ingest_pipeline_failed_count = writer.addGauge("es_ingest_pipeline_failed_count")
                .withHelp("The total number of ingest preprocessing operations that have failed in pipeline");

        for (IngestStats.PipelineStat entry : ingestStats.getPipelineStats()) {
            String pipeline = entry.getPipelineId();
            IngestStats.Stats stats = entry.getStats();

            es_ingest_pipeline_count.value(stats.getIngestCount(), "pipeline", pipeline);
            es_ingest_pipeline_time_seconds.value(stats.getIngestTimeInMillis() / 1000, "pipeline", pipeline);
            es_ingest_pipeline_current.value(stats.getIngestCurrent(), "pipeline", pipeline);
            es_ingest_pipeline_failed_count.value(stats.getIngestFailedCount(), "pipeline", pipeline);
        }

        return writer;
    }
}
