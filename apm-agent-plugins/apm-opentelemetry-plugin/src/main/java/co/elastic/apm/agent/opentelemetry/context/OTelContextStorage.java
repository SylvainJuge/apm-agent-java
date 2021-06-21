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
package co.elastic.apm.agent.opentelemetry.context;

import co.elastic.apm.agent.impl.transaction.AbstractSpan;
import co.elastic.apm.agent.opentelemetry.sdk.OTelSpan;
import co.elastic.apm.agent.sdk.state.GlobalThreadLocal;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextStorage;
import io.opentelemetry.context.Scope;

import javax.annotation.Nullable;

public class OTelContextStorage implements ContextStorage {

    public static OTelContextStorage INSTANCE = new OTelContextStorage();

    private static final GlobalThreadLocal<Context> ACTIVE_CONTEXT = GlobalThreadLocal.get(OTelContextStorage.class, "elastic-otel-context");

    @Override
    public Scope attach(@Nullable Context toAttach) {
        // fail-safe when trying to attach to null context
        if (null == toAttach) {
            return Scope.noop();
        }

        // context already current
        Context contextBeforeAttach = ACTIVE_CONTEXT.get();
        if (toAttach == contextBeforeAttach) {
            return Scope.noop();
        }

        ACTIVE_CONTEXT.set(toAttach);

        // we are able to retrieve current span as it as been already stored into context
        Span span = Span.fromContextOrNull(toAttach);
        AbstractSpan<?> internalSpan = null;
        if (span instanceof OTelSpan) {
            internalSpan = ((OTelSpan) span).getInternalSpan();
        }

        return new OTelScope(internalSpan, contextBeforeAttach);
    }

    @Nullable
    @Override
    public Context current() {
        return ACTIVE_CONTEXT.get();
    }

    private static class OTelScope implements Scope {

        @Nullable
        private final AbstractSpan<?> span;

        @Nullable
        private final Context contextToRestore;

        public OTelScope(@Nullable AbstractSpan<?> span, @Nullable Context contextToRestore) {
            this.span = span;
            this.contextToRestore = contextToRestore;
            if (span != null) {
                span.activate();
            }
        }

        @Override
        public void close() {
            if (span != null) {
                span.deactivate();
            }

            if (contextToRestore != null) {
                ACTIVE_CONTEXT.set(contextToRestore);
            }
        }
    }
}
