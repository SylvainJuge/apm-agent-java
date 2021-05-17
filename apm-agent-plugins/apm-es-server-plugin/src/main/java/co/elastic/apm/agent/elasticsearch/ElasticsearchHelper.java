/*-
 * #%L
 * Elastic APM Java agent
 * %%
 * Copyright (C) 2018 - 2020 Elastic and contributors
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
package co.elastic.apm.agent.elasticsearch;

import co.elastic.apm.agent.elasticsearch.action.ActionListenerInstrumentation;
import co.elastic.apm.agent.impl.GlobalTracer;
import co.elastic.apm.agent.impl.Tracer;
import co.elastic.apm.agent.impl.transaction.AbstractSpan;
import co.elastic.apm.agent.impl.transaction.Span;
import co.elastic.apm.agent.impl.transaction.Transaction;
import co.elastic.apm.agent.sdk.DynamicTransformer;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.http.HttpChannel;
import org.elasticsearch.http.HttpPipelinedResponse;
import org.elasticsearch.http.HttpRequest;
import org.elasticsearch.http.HttpResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.tasks.Task;

import javax.annotation.Nullable;

public class ElasticsearchHelper {

    private static final Tracer tracer = GlobalTracer.get();

    private static final ElasticsearchHelper INSTANCE = new ElasticsearchHelper(ElasticsearchGlobalState.getInstance());

    public static ElasticsearchHelper getInstance() {
        return INSTANCE;
    }

    private final ElasticsearchGlobalState globalState;

    // package-protected for testing
    ElasticsearchHelper(ElasticsearchGlobalState globalState) {
        this.globalState = globalState;
    }

    public void httpRequestStart(HttpRequest request, Transaction transaction, HttpChannel httpChannel) {

        transaction.appendToName(request.method().name())
            .appendToName(" ")
            .appendToName(request.uri());

        globalState.httpChannel2Transaction.put(httpChannel, transaction);
    }

    public void httpRequestRelease(HttpRequest httpRequest, @Nullable HttpRequest requestCopy) {
        Transaction transaction = globalState.httpRequest2Transaction.remove(httpRequest);
        if (transaction != null && requestCopy != null) {
            globalState.httpRequest2Transaction.put(requestCopy, transaction);
        }
    }

    public void registerRestChannel(HttpChannel httpChannel, RestChannel restChannel) {
        Transaction transaction = globalState.httpChannel2Transaction.get(httpChannel);
        if (null == transaction) {
            return;
        }

        // not required yet
        globalState.restChannel2Transaction.put(restChannel, transaction);
    }

    @Nullable
    public Transaction getTransaction(HttpChannel httpChannel) {
        return globalState.httpChannel2Transaction.get(httpChannel);
    }

    @Nullable
    public Transaction getTransaction(RestChannel restChannel) {
        return globalState.restChannel2Transaction.get(restChannel);
    }


    public void unregisterChannel(HttpChannel httpChannel) {
        globalState.httpChannel2Transaction.remove(httpChannel);
    }

    @Nullable
    public Transaction getTransaction(HttpRequest httpRequest) {
        return globalState.httpRequest2Transaction.get(httpRequest);
    }

    @Deprecated
    public void registerListener(ActionListener<?> listener, Transaction transaction) {
        DynamicTransformer.Accessor.get().ensureInstrumented(listener.getClass(), ActionListenerInstrumentation.ALL);

        globalState.actionListener2Transaction.put(listener, transaction);
    }

    @Deprecated
    @Nullable
    public Transaction listenerEnter(ActionListener<?> listener) {
        Transaction transaction = globalState.actionListener2Transaction.get(listener); // todo abstract span this !

        if (transaction != null) {
            transaction.activate();
        }

        return transaction;
    }

    @Deprecated
    public void listenerExit(ActionListener<?> listener,
                             @Nullable Object objSpan,
                             @Nullable Exception argException,
                             @Nullable Throwable thrownException) {
        if (!(objSpan instanceof AbstractSpan)) {
            return;
        }
        AbstractSpan<?> span = (AbstractSpan<?>) objSpan;

        globalState.actionListener2Transaction.remove(listener);

        span.captureException(argException)
            .captureException(thrownException)
            .deactivate();
    }


    @Nullable
    public Span spanStart() {
        AbstractSpan<?> active = GlobalTracer.requireTracerImpl().getActive();
        Span span = null;
        if (active != null) {
            span = active.createSpan().activate();
        }
        return span;
    }

    public void spanEnd(String name, @Nullable Object spanObj, @Nullable Throwable cause, @Nullable Throwable thrown) {
        if (!(spanObj instanceof Span)) {
            return;
        }
        ((Span) spanObj).captureException(thrown)
            .captureException(cause)
            .deactivate()
            .withName(name)
            .end();
    }

    public void registerResponse(HttpResponse httpResponse, Transaction transaction) {
        globalState.httpResponse2Transaction.put(httpResponse, transaction);
    }

    @Nullable
    public Span startWriteResponse(Object response) {
        if(!(response instanceof HttpResponse)){
            return null;
        }

        Transaction transaction = globalState.httpResponse2Transaction.get(getPipelineResponse(response));
        if (transaction == null) {
            return null;
        }

        return transaction.createSpan()
            .withName("response write")
            .activate();
    }

    public void endWriteResponse(HttpResponse httpResponse, @Nullable AbstractSpan<?> enterSpan, @Nullable Throwable thrown) {
        if (enterSpan != null) {
            enterSpan.captureException(thrown)
                .end();
        }


        Transaction transaction = globalState.httpResponse2Transaction.remove(getPipelineResponse(httpResponse));
        if (transaction == null) {
            return;
        }

        transaction.end();
    }

    private HttpResponse getPipelineResponse(Object httpResponse) {
        if (!(httpResponse instanceof HttpResponse)) {
            throw new IllegalArgumentException("unexpected http response" + httpResponse);
        }

        if (httpResponse instanceof HttpPipelinedResponse) {
            return ((HttpPipelinedResponse) httpResponse).getDelegateRequest();
        }
        return (HttpResponse) httpResponse;
    }

    @Nullable
    public AbstractSpan<?> createTaskSpanOrTransaction(@Nullable Task task){
        if (task == null) {
            return null;
        }
        AbstractSpan<?> created = null;

        AbstractSpan<?> parentActive = tracer.getActive();
        if (parentActive == null) {
            Transaction transaction = tracer.startRootTransaction(Task.class.getClassLoader());
            if (transaction != null) {
                created = transaction.withType("task")
                    .appendToName("Task ")
                    .appendToName(task.getType())
                    .appendToName(" ")
                    .appendToName(task.getAction());
            }
        } else {
            created = parentActive.createSpan()
                .withType("task")
                .withName(String.format("Task %s %s", task.getType(), task.getAction()))
                .withSubtype(task.getType());
        }

        globalState.activeTasks.put(task, created);

        return created;
    }

    public void endTask(@Nullable Task task) {
        if (task == null) {
            return;
        }

        AbstractSpan<?> abstractSpan = globalState.activeTasks.remove(task);
        if (abstractSpan == null) {
            return;
        }

        abstractSpan.end();
    }

}
