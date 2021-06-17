package co.elastic.apm.agent.sdk;

import java.util.List;

/**
 * Service provider interface for instrumentation providers
 */
public interface ElasticApmInstrumentationProvider {

    /**
     * @return ordered set of instrumentation
     */
    List<ElasticApmInstrumentation> loadInstrumentations();
}
