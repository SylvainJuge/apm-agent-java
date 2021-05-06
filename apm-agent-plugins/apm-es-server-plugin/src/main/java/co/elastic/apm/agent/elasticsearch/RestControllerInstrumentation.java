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
public class RestControllerInstrumentation extends ElasticsearchInstrumentation {

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        // the only known implementation of org.elasticsearch.http.HttpServerTransport.Dispatcher
        return named("org.elasticsearch.rest.RestController");
    }

    // dispatchRequest
    // TODO dispatchBadRequest --> will need some wo
    @Override
    public ElementMatcher<? super MethodDescription> getMethodMatcher() {
        return named("dispatchRequest")
            .and(takesArgument(0, named("org.elasticsearch.rest.RestRequest")))
            .and(takesArgument(1, named("org.elasticsearch.rest.RestChannel")))
            .and(takesArgument(2, named("org.elasticsearch.common.util.concurrent.ThreadContext")));
    }

    // DefaultRestChannel constructor provides the link between all of the following
    // HttpChannel
    // HttpRequest
    // RestRequest

    // HttpRequest.createResponse is called to create the HTTP response
    // but it's called AFTER HttpRequest.release() has been called.
    // --> keeping the transaction active during sendResponse could help

    // then when DefaultRestChannel.sendResponse is called, we have the RestResponse provided as parameter
    // the HttpChannel.sendResponse is also a good way to detect transaction end
}
