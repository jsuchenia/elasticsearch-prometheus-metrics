package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.elasticsearch.script.ScriptStats;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;

public class StriptsGenerator extends MetricsGenerator<ScriptStats> {
    @Override
    public PrometheusFormatWriter generateMetrics(PrometheusFormatWriter writer, ScriptStats scriptsStats) {
        writer.addGauge("es_scripts_compilations")
                .withHelp("Number of compilations")
                .value(scriptsStats.getCompilations());

        writer.addGauge("es_scripts_cache_evictions")
                .withHelp("Number of scripts cache evictions")
                .value(scriptsStats.getCacheEvictions());

        writer.addGauge("es_scripts_compilations_limits")
                .withHelp("Number of compilations timeout reached")
                .value(scriptsStats.getCompilationLimitTriggered());
        return writer;
    }
}
