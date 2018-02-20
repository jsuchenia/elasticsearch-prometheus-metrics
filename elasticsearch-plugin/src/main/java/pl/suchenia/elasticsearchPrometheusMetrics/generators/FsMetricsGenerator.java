package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.monitor.fs.FsInfo;
import org.elasticsearch.monitor.fs.FsInfo.Path;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.SingleValueWriter;

public class FsMetricsGenerator extends MetricsGenerator<FsInfo> {
    private static final Logger logger = Loggers.getLogger(FsMetricsGenerator.class);
    private static final String PATH_LABEL = "path";

    @Override
    public PrometheusFormatWriter generateMetrics(PrometheusFormatWriter writer, FsInfo fsInfo) {
        logger.debug("Generating data about FS stats: {}", fsInfo);

        writer.addGauge("es_fs_total_bytes")
                .withHelp("Total size of ES storage")
                .value(fsInfo.getTotal().getTotal().getBytes());
        writer.addGauge("es_fs_available_bytes")
                .withHelp("Available size of ES storage")
                .value(fsInfo.getTotal().getAvailable().getBytes());
        writer.addGauge("es_fs_free_bytes")
                .withHelp("Free size of ES storage")
                .value(fsInfo.getTotal().getFree().getBytes());

        SingleValueWriter pathTotal = writer.addGauge("es_fs_path_total_bytes")
                .withHelp("Total size of ES storage");
        SingleValueWriter pathAvailable = writer.addGauge("es_fs_path_available_bytes")
                .withHelp("Available size of ES storage");
        SingleValueWriter pathFree = writer.addGauge("es_fs_path_free_bytes")
                .withHelp("Free size of ES storage");

        for (Path pathStats : fsInfo) {
            String path = pathStats.getPath();

            pathTotal.value(pathStats.getTotal().getBytes(), PATH_LABEL, path);
            pathAvailable.value(pathStats.getAvailable().getBytes(), PATH_LABEL, path);
            pathFree.value(pathStats.getFree().getBytes(), PATH_LABEL, path);
        }
        return writer;
    }
}
