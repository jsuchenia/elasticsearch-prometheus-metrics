package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.tasks.PendingClusterTasksResponse;
import org.elasticsearch.cluster.service.PendingClusterTask;
import org.elasticsearch.common.logging.Loggers;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;

import java.util.List;

public class PendingTasksMetricsGenerator implements MetricsGenerator<PendingClusterTasksResponse> {
    private static final Logger logger = Loggers.getLogger(PendingTasksMetricsGenerator.class);

    @Override
    public PrometheusFormatWriter generateMetrics(PrometheusFormatWriter writer,
                                                  PendingClusterTasksResponse pendingClusterTasksResponse) {
        List<PendingClusterTask> pendingTasks = pendingClusterTasksResponse.getPendingTasks();

        logger.debug("Generating output for PendingTasks stats: {}", pendingTasks);

        return writer;
    }
}
