package co.elastic.apm.agent.opentelemetry;

import co.elastic.apm.agent.sdk.ElasticApmInstrumentation;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Collection;
import java.util.Collections;

public class OpenTelemetryInstrumentation extends ElasticApmInstrumentation {

    private final String name;
    private final ElementMatcher.Junction<ClassLoader> classLoaderMatcher;
    private final ElementMatcher<? super TypeDescription> typeMatcher;
    private final ElementMatcher<? super MethodDescription> methodMatcher;
    private final String adviceClassName;

    public OpenTelemetryInstrumentation(String name,
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
