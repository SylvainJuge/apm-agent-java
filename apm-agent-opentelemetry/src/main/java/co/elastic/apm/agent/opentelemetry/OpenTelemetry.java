package co.elastic.apm.agent.opentelemetry;

import co.elastic.apm.agent.sdk.ElasticApmInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.instrumentation.api.SafeServiceLoader;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenTelemetry {

    public static List<ElasticApmInstrumentation> loadOTInstrumentations() {
        // TODO : use ordered variant (not released yet)
        // TODO : use isolated classloader for OT classes
        ClassLoader classLoader = OpenTelemetry.class.getClassLoader();

        List<ElasticApmInstrumentation> instrumentations = new ArrayList<>();

        for (InstrumentationModule im : SafeServiceLoader.load(InstrumentationModule.class, classLoader)) {
            try {
                instrumentations.addAll(toElasticInstrumentation(im));
            } catch (Exception | LinkageError e) {
                // TODO handle failure for proper investigation
                System.out.println("fail, TODO better");
            }
        }

        return instrumentations;
    }

    private static List<ElasticApmInstrumentation> toElasticInstrumentation(InstrumentationModule module) {
        List<ElasticApmInstrumentation> list = new ArrayList<>();
        for (TypeInstrumentation instrumentation : module.typeInstrumentations()) {
            Map<? extends ElementMatcher<? super MethodDescription>, String> transformers = instrumentation.transformers();
            for (Map.Entry<? extends ElementMatcher<? super MethodDescription>, String> transformer : transformers.entrySet()) {
                list.add(new OpenTelemetryInstrumentation(
                    module.instrumentationName(),
                    module.classLoaderMatcher(),
                    wrap(instrumentation),
                    transformer.getKey(),
                    transformer.getValue()));
            }
        }

        return list;
    }

    private static ElementMatcher<TypeDescription> wrap(final TypeInstrumentation instrumentation) {
        return new ElementMatcher<TypeDescription>() {
            @Override
            public boolean matches(TypeDescription target) {
                if (target.getName().startsWith("org.apache.http.impl.client") && target.getName().contains("HttpClient")) {
                    System.out.println("");
                }
                return instrumentation.typeMatcher().matches(target);
            }
        };
    }
}
