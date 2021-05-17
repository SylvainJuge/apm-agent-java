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
package co.elastic.apm.agent.elasticsearch.rest;

import co.elastic.apm.agent.elasticsearch.ElasticsearchHelper;
import net.bytebuddy.asm.Advice;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.http.HttpServerTransport;
import org.elasticsearch.rest.RestChannel;

import javax.annotation.Nullable;

/**
 * Instruments {@link HttpServerTransport.Dispatcher#dispatchBadRequest(RestChannel, ThreadContext, Throwable)}
 */
public class DispatchBadRequestAdvice {

    public static final ElasticsearchHelper helper = ElasticsearchHelper.getInstance();

    @Nullable
    @Advice.OnMethodEnter(suppress = Throwable.class, inline = false)
    public static Object onEnter(@Advice.Argument(0) RestChannel channel) {
        return helper.spanStart();
    }

    @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class, inline = false)
    public static void onExit(@Advice.Enter @Nullable Object transactionObj,
                              @Advice.Argument(2) Throwable cause,
                              @Advice.Thrown @Nullable Throwable thrown) {

        helper.spanEnd("dispatch", transactionObj, cause, thrown);
    }

}
