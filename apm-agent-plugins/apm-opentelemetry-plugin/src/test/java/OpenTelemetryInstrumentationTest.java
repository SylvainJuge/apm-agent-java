/*-
 * #%L
 * Elastic APM Java agent
 * %%
 * Copyright (C) 2018 - 2021 Elastic and contributors
 * %%
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */

import co.elastic.apm.agent.AbstractInstrumentationTest;
import co.elastic.apm.agent.impl.transaction.AbstractSpan;
import co.elastic.apm.agent.impl.transaction.Transaction;
import co.elastic.apm.agent.opentelemetry.sdk.ElasticOpenTelemetry;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


public class OpenTelemetryInstrumentationTest  extends AbstractInstrumentationTest {

    @Test
    void spanOutsideTransaction() throws IOException {
        disableRecyclingValidation();
        executeGetRequest();

        assertThat(reporter.getNumReportedSpans()).isEqualTo(0);
        assertThat(reporter.getNumReportedTransactions()).isEqualTo(0);
        assertThat(reporter.getErrors()).isEmpty();
    }

    @Test
    void simpleGet() throws IOException, InterruptedException {
        disableRecyclingValidation();

        Transaction transaction = startTestRootTransaction("opentelemetry");

        try {
            executeGetRequest();

            reporter.awaitSpanCount(1);
        } finally {
            transaction.deactivate().end();
        }

    }

    @Test
    void elasticOtelRegistration() {
        OpenTelemetry otel = GlobalOpenTelemetry.get();
        // can't rely on class equality as it might be loaded from indy plugin CL
        assertThat(otel.getClass().getName()).isEqualTo(ElasticOpenTelemetry.class.getName());
    }

    @Test
    void basicOtelCreateSpan() {
        disableRecyclingValidation();

        Tracer otelTracer = GlobalOpenTelemetry.get().getTracer("any", "1.0.0");
        assertThat(otelTracer).isNotNull();

        Span otelSpan = otelTracer.spanBuilder("span").startSpan();

        Context beforeContext = Context.current();
        checkNoSpanActive();

        try (Scope scope = otelSpan.makeCurrent()) {

            assertThat(Context.current()).isNotSameAs(beforeContext);

            AbstractSpan<?> activeSpan = tracer.getActive();
            assertThat(activeSpan).isNotNull();
            assertThat(activeSpan.getNameAsString()).isEqualTo("span");

        } finally {
            otelSpan.end();
        }
        checkNoSpanActive();

        assertThat(Context.current()).isSameAs(beforeContext);

    }

    private void checkNoSpanActive() {
        assertThat(tracer.getActive()).isNull();
    }


    private void executeGetRequest() throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("http://httpbin.org/");

        HttpResponse response = httpclient.execute(httpget);

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
    }
}
