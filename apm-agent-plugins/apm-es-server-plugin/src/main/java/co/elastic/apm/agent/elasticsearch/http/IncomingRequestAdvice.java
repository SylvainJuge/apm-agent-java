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

package co.elastic.apm.agent.elasticsearch.http;

import co.elastic.apm.agent.elasticsearch.ElasticsearchHelper;
import co.elastic.apm.agent.impl.ElasticApmTracer;
import co.elastic.apm.agent.impl.GlobalTracer;
import co.elastic.apm.agent.impl.transaction.Transaction;
import net.bytebuddy.asm.Advice;
import org.elasticsearch.http.HttpChannel;
import org.elasticsearch.http.HttpRequest;

import javax.annotation.Nullable;

public class IncomingRequestAdvice {

    private static final ElasticApmTracer tracer = GlobalTracer.requireTracerImpl();
    private static final ElasticsearchHelper helper = ElasticsearchHelper.getInstance();

    @Nullable
    @Advice.OnMethodEnter(suppress = Throwable.class, inline = false)
    public static Object onEnter(@Advice.Argument(0) HttpRequest request,
                                 @Advice.Argument(1) HttpChannel channel) {

        Transaction transaction = tracer.startRootTransaction(request.getClass().getClassLoader());
        if (transaction == null) {
            return null;
        }

        helper.httpRequestStart(request, transaction.activate(), channel);

        return transaction;
    }

    @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class, inline = false)
    public static void onExit(@Advice.Enter @Nullable Object enterTransaction,
                              @Advice.Thrown @Nullable Throwable thrown) {

        if (!(enterTransaction instanceof Transaction)) {
            return;
        }
//        Transaction transaction = (Transaction) enterTransaction;
//        transaction.end();
    }
}
