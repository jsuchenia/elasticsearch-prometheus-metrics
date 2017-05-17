package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.index.shard.IndexingStats;
import org.elasticsearch.indices.NodeIndicesStats;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.ValueWriter;

import java.util.Map;

public class IndicesMetricsGenerator implements  MetricsGenerator<NodeIndicesStats> {
    private static final Logger logger = Loggers.getLogger(IndicesMetricsGenerator.class);

    @Override
    public void generateMetrics(PrometheusFormatWriter writer, NodeIndicesStats indicesStats) {
        logger.debug("Generating output based on indicies stats: {}", indicesStats);

        //StoreStats
        writer.addGauge("es_common_store_size")
                .withHelp("Elasticsearch storage size (in bytes)")
                .value(indicesStats.getStore().getSizeInBytes());
        writer.addGauge("es_common_store_size")
                .withHelp("Elasticsearch storage throttle time (in millis)")
                .value(indicesStats.getStore().getThrottleTime().millis());


        //DocsStats
        writer.addCounter("es_common_docs_count")
                .withHelp("Elasticsearch documents counter")
                .value(indicesStats.getDocs().getCount());
        writer.addCounter("es_common_docs_deleted_count")
                .withHelp("Elasticsearch documents deleted")
                .value(indicesStats.getDocs().getDeleted());

        //Indexing stats per index
        ValueWriter es_docindex_count = writer.addCounter("es_docindex_count")
                .withHelp("Counter of indexing operations");
        ValueWriter es_docindexfailed_count = writer.addCounter("es_docindexfailed_count")
                .withHelp("Counter of failed indexing operations");
        ValueWriter es_docdelete_count = writer.addCounter("es_docdelete_count")
                .withHelp("Number of delete operations");
        ValueWriter es_docdelete_current = writer.addGauge("es_docdelete_current")
                .withHelp("Number of active delete operations");
        ValueWriter es_docindex_current = writer.addGauge("es_docindex_current")
                .withHelp("Number of active index operations");
        ValueWriter es_docindex_isthrotled = writer.addGauge("es_docindex_isthrotled")
                .withHelp("Flag to check is node throttled");

        //Index data per each index
        if (indicesStats.getIndexing().getTypeStats() != null) {
            logger.debug("Dumping data from indexes: {}", indicesStats.getIndexing().getTypeStats());

            for (Map.Entry<String, IndexingStats.Stats> entry : indicesStats.getIndexing().getTypeStats().entrySet()) {
                logger.debug("Dumping data from index: {}", entry);

                String index = entry.getKey();
                IndexingStats.Stats stats = entry.getValue();

                es_docindex_count.value(stats.getIndexCount(), "index", index);
                es_docindexfailed_count.value(stats.getIndexFailedCount(), "index", index);
                es_docdelete_count.value(stats.getDeleteCount(), "index", index);
                es_docdelete_current.value(stats.getDeleteCurrent(), "index", index);
                es_docindex_current.value(stats.getIndexCurrent(), "index", index);
                es_docindex_isthrotled.value(stats.isThrottled() ? 1 : 0, "index", index);
            }
        }
    }
}
