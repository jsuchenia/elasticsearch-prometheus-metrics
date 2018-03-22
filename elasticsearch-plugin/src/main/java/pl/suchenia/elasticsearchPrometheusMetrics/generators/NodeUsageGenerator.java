package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.node.usage.NodeUsage;
import org.elasticsearch.common.logging.Loggers;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.SingleValueWriter;

public class NodeUsageGenerator extends MetricsGenerator<NodeUsage> {
    private static final Logger logger = Loggers.getLogger(NodeUsageGenerator.class);
    private static final String ENDPOINT_LABEL = "endpoint";

    @Override
    public PrometheusFormatWriter generateMetrics(PrometheusFormatWriter writer, NodeUsage nodeUsage) {
        logger.debug("Generating output for REST usage: {}", nodeUsage);

        SingleValueWriter restActions = writer.addCounter("es_rest_count")
                .withHelp("Number of REST endpoint executions");

        nodeUsage.getRestUsage()
                .forEach((key, value) -> restActions.value(value, ENDPOINT_LABEL, key));
        return writer;
    }
}
