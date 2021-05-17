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


import co.elastic.apm.agent.elasticsearch.ElasticsearchInstrumentation;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.elasticsearch.http.HttpChannel;
import org.elasticsearch.http.HttpRequest;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * Instruments {@link org.elasticsearch.http.AbstractHttpServerTransport#incomingRequest(HttpRequest, HttpChannel)}
 */
public abstract class HttpServerTransportInstrumentation extends ElasticsearchInstrumentation {

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return named("org.elasticsearch.http.AbstractHttpServerTransport");
    }

    // todo onException todo onServerException

    public static class IncomingRequestInstrumentation extends HttpServerTransportInstrumentation {

        @Override
        public ElementMatcher<? super MethodDescription> getMethodMatcher() {
            return named("incomingRequest")
                .and(takesArgument(0, named("org.elasticsearch.http.HttpRequest")))
                .and(takesArgument(1, named("org.elasticsearch.http.HttpChannel")));
        }

        @Override
        public String getAdviceClassName() {
            return "co.elastic.apm.agent.elasticsearch.http.IncomingRequestAdvice";
        }
    }
}
