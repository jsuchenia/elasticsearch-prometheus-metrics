package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.tasks.PendingClusterTasksResponse;
import org.elasticsearch.cluster.service.PendingClusterTask;
import org.elasticsearch.common.logging.Loggers;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.SingleValueWriter;

import java.util.List;

public class PendingTasksMetricsGenerator extends MetricsGenerator<PendingClusterTasksResponse> {
    private static final Logger logger = Loggers.getLogger(PendingTasksMetricsGenerator.class, "init");

    @Override
    public PrometheusFormatWriter generateMetrics(PrometheusFormatWriter writer,
                                                  PendingClusterTasksResponse pendingClusterTasksResponse) {
        List<PendingClusterTask> pendingTasks = pendingClusterTasksResponse.getPendingTasks();

        logger.debug("Generating output for PendingTasks stats: {}", pendingTasks);

        writer.addGauge("es_tasks_count")
                .withHelp("Number of background tasks running")
                .value(pendingTasks.size());

        SingleValueWriter esTasksTimeInQueueSeconds = writer
                .addGauge("es_tasks_time_in_queue_millis")
                .withHelp("How long this task is in queue (in seconds)");

        for (PendingClusterTask pendingTask : pendingTasks) {
            esTasksTimeInQueueSeconds.value(pendingTask.getTimeInQueueInMillis(),
                    "id", pendingTask.getSource().toString(),
                    "priority", pendingTask.getPriority().name());
        }

        return writer;
    }
}
