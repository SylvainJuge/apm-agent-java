package co.elastic.apm.agent.opentelemetry.instrumentation;

import co.elastic.apm.agent.sdk.ElasticApmInstrumentation;
import co.elastic.apm.agent.sdk.ElasticApmInstrumentationProvider;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.instrumentation.api.SafeServiceLoader;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OpenTelemetryInstrumentationProvider implements ElasticApmInstrumentationProvider {

    @Override
    public List<ElasticApmInstrumentation> loadInstrumentations() {
        // TODO : use ordered variant (not released yet)
        // TODO : use isolated classloader for OT classes
        ClassLoader classLoader = OpenTelemetryInstrumentationProvider.class.getClassLoader();

        List<ElasticApmInstrumentation> instrumentations = new ArrayList<>();

        for (InstrumentationModule im : SafeServiceLoader.load(InstrumentationModule.class, classLoader)) {
            try {
                instrumentations.addAll(toElasticInstrumentation(im));
            } catch (Exception | LinkageError e) {
                throw new IllegalStateException(e);
            }
        }

        return instrumentations;
    }

    private static List<ElasticApmInstrumentation> toElasticInstrumentation(InstrumentationModule module) {
        List<ElasticApmInstrumentation> list = new ArrayList<>();
        for (TypeInstrumentation instrumentation : module.typeInstrumentations()) {
            Map<? extends ElementMatcher<? super MethodDescription>, String> transformers = instrumentation.transformers();
            for (Map.Entry<? extends ElementMatcher<? super MethodDescription>, String> transformer : transformers.entrySet()) {
                list.add(new OtelInstrumentation(
                    module.instrumentationName(),
                    module.classLoaderMatcher(),
                    instrumentation.typeMatcher(),
                    transformer.getKey(),
                    transformer.getValue()));
            }
        }

        return list;
    }

    private static class OtelInstrumentation extends ElasticApmInstrumentation {

        private final String name;
        private final ElementMatcher.Junction<ClassLoader> classLoaderMatcher;
        private final ElementMatcher<? super TypeDescription> typeMatcher;
        private final ElementMatcher<? super MethodDescription> methodMatcher;
        private final String adviceClassName;

        public OtelInstrumentation(String name,
                                   ElementMatcher.Junction<ClassLoader> classLoaderMatcher,
                                   ElementMatcher<? super TypeDescription> typeMatcher,
                                   ElementMatcher<? super MethodDescription> methodMatcher,
                                   String adviceClassName) {

            this.name = name;
            this.classLoaderMatcher = classLoaderMatcher;
            this.typeMatcher = typeMatcher;
            this.methodMatcher = methodMatcher;
            this.adviceClassName = adviceClassName;
        }

        @Override
        public ElementMatcher<? super TypeDescription> getTypeMatcher() {
            return typeMatcher;
        }

        @Override
        public ElementMatcher.Junction<ClassLoader> getClassLoaderMatcher() {
            return classLoaderMatcher;
        }

        @Override
        public ElementMatcher<? super MethodDescription> getMethodMatcher() {
            return methodMatcher;
        }

        @Override
        public String getAdviceClassName() {
            return adviceClassName;
        }

        @Override
        public Collection<String> getInstrumentationGroupNames() {
            return Collections.singleton(name);
        }
    }
}
