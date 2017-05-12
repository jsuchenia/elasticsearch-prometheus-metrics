package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.index.shard.IndexingStats;
import org.elasticsearch.indices.NodeIndicesStats;

import java.util.Map;

public class IndicesMetricsGenerator {
    private static final Logger logger = Loggers.getLogger(IndicesMetricsGenerator.class);

    public void generateMetrics(PrometheusFormatWriter writer, NodeIndicesStats indices) throws Exception {
        logger.debug("Generating output based on indicies stats: {}", indices);

        //StoreStats
        writer.generateGaugeHelp("es_common_store_size", "Elasticsearch storage size (in bytes)");
        writer.generateValue("es_common_store_size", indices.getStore().getSizeInBytes());
        writer.generateGaugeHelp("es_common_store_size", "Elasticsearch storage throttle time (in millis)");
        writer.generateValue("es_common_store_size", indices.getStore().getThrottleTime().millis());


        //DocsStats
        writer.generateCounterHelp("es_common_docs_count", "Elasticsearch documents counter");
        writer.generateValue("es_common_docs_count", indices.getDocs().getCount());
        writer.generateCounterHelp("es_common_docs_deleted_count", "Elasticsearch documents deleted");
        writer.generateValue("es_common_docs_deleted_count", indices.getDocs().getDeleted());

        //Indexing stats per index
        writer.generateCounterHelp("es_docindex_count", "Counter of indexing operations");
        writer.generateCounterHelp("es_docindexfailed_count", "Counter of failed indexing operations");
        writer.generateCounterHelp("es_docdelete_count", "Number of delete operations");
        writer.generateGaugeHelp("es_docdelete_current", "Number of active delete operations");
        writer.generateGaugeHelp("es_docindex_currnet", "Number of active index operations");
        writer.generateGaugeHelp("es_docindex_isthrotled", "Flag to check is node throttled");

        if (indices.getIndexing().getTypeStats() != null) {
            for (Map.Entry<String, IndexingStats.Stats> entry : indices.getIndexing().getTypeStats().entrySet()) {
                String index = entry.getKey();
                IndexingStats.Stats stats = entry.getValue();

                writer.generateValue("es_docindex_count", "index", index, stats.getIndexCount());
                writer.generateValue("es_docindexfailed_count", "index", index, stats.getIndexFailedCount());
                writer.generateValue("es_docdelete_count", "index", index, stats.getDeleteCount());
                writer.generateValue("es_docdelete_current", "index", index, stats.getDeleteCurrent());
                writer.generateValue("es_docindex_currnet", "index", index, stats.getIndexCurrent());
                writer.generateValue("es_docindex_isthrotled", "index", index, stats.isThrottled() ? 1 : 0);
            }
        }
    }
}
