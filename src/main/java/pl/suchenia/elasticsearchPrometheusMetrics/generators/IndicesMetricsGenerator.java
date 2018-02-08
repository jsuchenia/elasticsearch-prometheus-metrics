package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.index.cache.query.QueryCacheStats;
import org.elasticsearch.index.fielddata.FieldDataStats;
import org.elasticsearch.index.recovery.RecoveryStats;
import org.elasticsearch.index.search.stats.SearchStats;
import org.elasticsearch.index.shard.DocsStats;
import org.elasticsearch.index.shard.IndexingStats;
import org.elasticsearch.index.store.StoreStats;
import org.elasticsearch.indices.NodeIndicesStats;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.SingleValueWriter;

import java.util.Map;

public class IndicesMetricsGenerator extends  MetricsGenerator<NodeIndicesStats> {
    private static final Logger logger = Loggers.getLogger(IndicesMetricsGenerator.class);

    @Override
    public PrometheusFormatWriter generateMetrics(PrometheusFormatWriter writer, NodeIndicesStats indicesStats) {
        logger.debug("Generating output based on indicies stats: {}", indicesStats);

        writeDocsStats(writer, indicesStats.getDocs());
        writeStoreStats(writer, indicesStats.getStore());
        writeIndexingStats(writer, indicesStats.getIndexing());
        writeFeldsDataStats(writer, indicesStats.getFieldData());
        writeQueryCacheStats(writer, indicesStats.getQueryCache());
        writeRecoveryStats(writer, indicesStats.getRecoveryStats());
        writeSearchStats(writer, indicesStats.getSearch());

        return writer;
    }

    private void writeSearchStats(PrometheusFormatWriter writer, SearchStats search) {
        writer.addGauge("es_search_query_current")
                .withHelp("Number of search queries executed in cluster")
                .value(search.getTotal().getQueryCurrent());
        writer.addSummary("es_search_query")
                .withHelp("Total number of search queries executed in cluster")
                .summary(search.getTotal().getQueryCount(), search.getTotal().getQueryTimeInMillis());
    }

    private void writeRecoveryStats(PrometheusFormatWriter writer, RecoveryStats recoveryStats) {
        writer.addGauge("es_recovery_source")
                .withHelp("Number of ongoing recoveries for which a shard serves as a source")
                .value(recoveryStats.currentAsSource());

        writer.addGauge("es_recovery_target")
                .withHelp("Number of ongoing recoveries for which a shard serves as a target")
                .value(recoveryStats.currentAsTarget());
    }

    private void writeQueryCacheStats(PrometheusFormatWriter writer, QueryCacheStats queryCache) {
        writer.addGauge("es_querycache_size")
                .withHelp("Number of documents that's are in the cache")
                .value(queryCache.getCacheSize());

        writer.addCounter("es_querycache_count")
                .withHelp("Number of documents cached")
                .value(queryCache.getCacheCount());

        writer.addCounter("es_querycache_evictions")
                .withHelp("Number of evicted document")
                .value(queryCache.getEvictions());

        writer.addCounter("es_querycache_hitcount")
                .withHelp("Number of documents found in a cache")
                .value(queryCache.getHitCount());

        writer.addGauge("es_querycache_memory_bytes")
                .withHelp("Size of query cache in memory")
                .value(queryCache.getMemorySizeInBytes());
    }

    private void writeFeldsDataStats(PrometheusFormatWriter writer, FieldDataStats fieldData) {
        writer.addGauge("es_fielddata_size_bytes")
                .withHelp("Size of total fielddata size")
                .value(fieldData.getMemorySizeInBytes());

        writer.addGauge("es_fielddata_eviction_count")
                .withHelp("Number of evictions in fielddata")
                .value(fieldData.getEvictions());
    }

    private void writeIndexingStats(PrometheusFormatWriter writer, IndexingStats indexingStats) {
        SingleValueWriter es_docindex_count = writer.addCounter("es_docindex_count")
                .withHelp("Counter of indexing operations");
        SingleValueWriter es_docindexfailed_count = writer.addCounter("es_docindexfailed_count")
                .withHelp("Counter of failed indexing operations");
        SingleValueWriter es_docdelete_count = writer.addCounter("es_docdelete_count")
                .withHelp("Number of delete operations");
        SingleValueWriter es_docdelete_current = writer.addGauge("es_docdelete_current")
                .withHelp("Number of active delete operations");
        SingleValueWriter es_docindex_current = writer.addGauge("es_docindex_current")
                .withHelp("Number of active index operations");
        SingleValueWriter es_docindex_isthrottled = writer.addGauge("es_docindex_isthrottled")
                .withHelp("Flag to check is node throttled");

        //Index data per each index
        if (indexingStats.getTypeStats() != null) {
            logger.debug("Dumping data from indexes: {}", indexingStats.getTypeStats());

            for (Map.Entry<String, IndexingStats.Stats> entry : indexingStats.getTypeStats().entrySet()) {
                logger.debug("Dumping data from index: {}", entry);

                String index = entry.getKey();
                IndexingStats.Stats stats = entry.getValue();

                es_docindex_count.value(stats.getIndexCount(), "index", index);
                es_docindexfailed_count.value(stats.getIndexFailedCount(), "index", index);
                es_docdelete_count.value(stats.getDeleteCount(), "index", index);
                es_docdelete_current.value(stats.getDeleteCurrent(), "index", index);
                es_docindex_current.value(stats.getIndexCurrent(), "index", index);
                es_docindex_isthrottled.value(stats.isThrottled() ? 1 : 0, "index", index);
            }
        }
    }

    private void writeDocsStats(PrometheusFormatWriter writer, DocsStats docsStats) {
        writer.addCounter("es_common_docs_count")
                .withHelp("Elasticsearch documents counter")
                .value(docsStats.getCount());
        writer.addCounter("es_common_docs_deleted_count")
                .withHelp("Elasticsearch documents deleted")
                .value(docsStats.getDeleted());
        writer.addGauge("es_common_docs_size_bytes")
                .withHelp("Size in bytes occupied by all docs")
                .value(docsStats.getTotalSizeInBytes());
    }

    private void writeStoreStats(PrometheusFormatWriter writer, StoreStats storeStats) {
        writer.addGauge("es_common_store_size")
                .withHelp("Elasticsearch storage size (in bytes)")
                .value(storeStats.getSizeInBytes());
    }
}
