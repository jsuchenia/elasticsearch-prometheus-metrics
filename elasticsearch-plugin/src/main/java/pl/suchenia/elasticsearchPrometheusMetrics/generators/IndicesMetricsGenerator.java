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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static javax.management.Query.value;

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
        logger.debug("Dumping data from indexes: {}", indexingStats.getTotal());

        IndexingStats.Stats totalStats = indexingStats.getTotal();
        writer.addCounter("es_indexing_doc_count")
                    .withHelp("Counter of indexing operations")
                    .value(totalStats.getIndexCount());
        writer.addGauge("es_indexing_doc_current")
                .withHelp("Number of active index operations")
                .value(totalStats.getIndexCurrent());
        writer.addCounter("es_indexing_failed_count")
                    .withHelp("Counter of failed indexing operations")
                    .value(totalStats.getIndexFailedCount());
        writer.addCounter("es_indexing_delete_count")
                    .withHelp("Number of delete operations")
                    .value(totalStats.getDeleteCount());
        writer.addGauge("es_indexing_delete_current")
                    .withHelp("Number of active delete operations")
                    .value(totalStats.getDeleteCurrent());
        writer.addGauge("es_docindex_isthrottled")
                    .withHelp("Flag to check is node throttled")
                    .value(totalStats.isThrottled()? 1.0 : 0.0);
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
                .value(getDynamicValue(docsStats, "getTotalSizeInBytes"));
    }

    private void writeStoreStats(PrometheusFormatWriter writer, StoreStats storeStats) {
        writer.addGauge("es_common_store_size")
                .withHelp("Elasticsearch storage size (in bytes)")
                .value(storeStats.getSizeInBytes());
    }
}
