package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
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

        if (indicesStats.getIndexing().getTypeStats() != null) {
            for (Map.Entry<String, IndexingStats.Stats> entry : indicesStats.getIndexing().getTypeStats().entrySet()) {
                String index = entry.getKey();
                IndexingStats.Stats stats = entry.getValue();

                es_docindex_count.value("index", index, stats.getIndexCount());
                es_docindexfailed_count.value("index", index, stats.getIndexFailedCount());
                es_docdelete_count.value("index", index, stats.getDeleteCount());
                es_docdelete_current.value("index", index, stats.getDeleteCurrent());
                es_docindex_current.value("index", index, stats.getIndexCurrent());
                es_docindex_isthrotled.value("index", index, stats.isThrottled() ? 1 : 0);
            }
        }
    }
}
