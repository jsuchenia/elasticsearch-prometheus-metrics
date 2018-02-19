package pl.suchenia.elasticsearchPrometheusMetrics.generators;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.Version;
import org.elasticsearch.common.logging.Loggers;
import pl.suchenia.elasticsearchPrometheusMetrics.writer.PrometheusFormatWriter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class MetricsGenerator<T> {
    private static final Logger logger = Loggers.getLogger(MetricsGenerator.class);

    static double getDynamicValue(Object obj, String methodName) {
        try {
            Method method = obj.getClass().getMethod(methodName);
            Object value = method.invoke(obj);
            if (value.getClass().isAssignableFrom(Long.class)) {
                Long l = (Long) value;
                return l.doubleValue();
            }
            return (double) value;
        } catch (NoSuchMethodException e) {
            logger.error("There are no getTotalSizeInBytes method defined");
            return -1.0;
        } catch (IllegalAccessException | InvocationTargetException | ClassCastException e) {
            logger.error("Exception during method invocation: {}", e.getMessage());
            return -1.0;
        }
    }
    public abstract PrometheusFormatWriter generateMetrics(PrometheusFormatWriter writer, T inputData);
}
