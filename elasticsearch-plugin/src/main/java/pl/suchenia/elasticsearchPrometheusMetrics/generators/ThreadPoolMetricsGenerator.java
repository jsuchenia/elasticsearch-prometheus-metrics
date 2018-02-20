package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.threadpool.ThreadPoolStats;
import org.elasticsearch.threadpool.ThreadPoolStats.Stats;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.SingleValueWriter;

public class ThreadPoolMetricsGenerator extends MetricsGenerator<ThreadPoolStats> {
    private static final Logger logger = Loggers.getLogger(ThreadPoolMetricsGenerator.class);
    private static final String LABEL_NAME = "threadpool";

    @Override
    public PrometheusFormatWriter generateMetrics(PrometheusFormatWriter writer, ThreadPoolStats threadPoolStats) {
        logger.debug("Generating output for ThreadPool stats: {}", threadPoolStats);

        SingleValueWriter threads = writer.addGauge("es_threadpool_threads")
                .withHelp("Number of configured threads in the threadpool");
        SingleValueWriter queue = writer.addGauge("es_threadpool_queue")
                .withHelp("Size of queue configured in the threadpool");
        SingleValueWriter active = writer.addGauge("es_threadpool_active")
                .withHelp("Active threads in the threadpool");
        SingleValueWriter largest = writer.addGauge("es_threadpool_largest")
                .withHelp("Largest number of threads in the threadpool");

        SingleValueWriter completed = writer.addCounter("es_threadpool_completed")
                .withHelp("Number of completed tasks in the threadpool");
        SingleValueWriter rejected = writer.addGauge("es_threadpool_rejected")
                .withHelp("Number of rejected tasks in the threadpool");



        for (Stats threadPoolStat : threadPoolStats) {
            String name = threadPoolStat.getName();

            threads.value(threadPoolStat.getThreads(), LABEL_NAME, name);
            queue.value(threadPoolStat.getQueue(), LABEL_NAME, name);
            active.value(threadPoolStat.getActive(), LABEL_NAME, name);
            largest.value(threadPoolStat.getLargest(), LABEL_NAME, name);

            completed.value(threadPoolStat.getCompleted(), LABEL_NAME, name);
            rejected.value(threadPoolStat.getRejected(), LABEL_NAME, name);
        }
        return writer;
    }
}
