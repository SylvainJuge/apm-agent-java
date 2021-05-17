/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

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

import co.elastic.apm.agent.elasticsearch.ElasticsearchInstrumentation;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * Instruments {@link org.elasticsearch.rest.RestController#dispatchRequest(RestRequest, RestChannel, ThreadContext)}
 */
public abstract class RestControllerInstrumentation extends ElasticsearchInstrumentation {

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        // the only known implementation of org.elasticsearch.http.HttpServerTransport.Dispatcher
        return named("org.elasticsearch.rest.RestController");
    }

    public static class DispatchRequest extends RestControllerInstrumentation {
        @Override
        public ElementMatcher<? super MethodDescription> getMethodMatcher() {
            return named("dispatchRequest")
                .and(takesArgument(0, named("org.elasticsearch.rest.RestRequest")))
                .and(takesArgument(1, named("org.elasticsearch.rest.RestChannel")))
                .and(takesArgument(2, named("org.elasticsearch.common.util.concurrent.ThreadContext")));
        }

        @Override
        public String getAdviceClassName() {
            return "co.elastic.apm.agent.elasticsearch.rest.DispatchRequestAdvice";
        }
    }

    public static class DispatchBadRequest extends RestControllerInstrumentation {
        @Override
        public ElementMatcher<? super MethodDescription> getMethodMatcher() {
            return named("dispatchBadRequest")
                .and(takesArgument(0, named("org.elasticsearch.rest.RestChannel")))
                .and(takesArgument(1, named("org.elasticsearch.common.util.concurrent.ThreadContext")))
                .and(takesArgument(2, named("java.lang.Throwable")));
        }

        @Override
        public String getAdviceClassName() {
            return "co.elastic.apm.agent.elasticsearch.rest.DispatchBadRequestAdvice";
        }
    }

}
