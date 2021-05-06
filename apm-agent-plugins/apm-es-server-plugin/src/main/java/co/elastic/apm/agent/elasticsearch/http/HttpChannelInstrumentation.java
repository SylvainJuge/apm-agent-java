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

package co.elastic.apm.agent.elasticsearch.http;

import co.elastic.apm.agent.elasticsearch.ElasticsearchInstrumentation;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.http.HttpResponse;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * Instruments
 * <ul>
 *     <li>{@link org.elasticsearch.http.HttpChannel#sendResponse(HttpResponse, ActionListener)}</li>
 *     <li>{@link org.elasticsearch.http.HttpChannel#close()}</li>
 * </ul>
 */
public abstract class HttpChannelInstrumentation extends ElasticsearchInstrumentation {

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return named("org.elasticsearch.http.netty4.Netty4HttpChannel")
            .or(named("org.elasticsearch.http.netty4.Netty4HttpChannel"));
    }

    public static class SendResponse extends HttpChannelInstrumentation {

        @Override
        public ElementMatcher<? super MethodDescription> getMethodMatcher() {
            return named("sendResponse")
                .and(takesArgument(0, named("org.elasticsearch.http.HttpResponse")));
        }

        @Override
        public String getAdviceClassName() {
            return "co.elastic.apm.agent.elasticsearch.http.SendResponseAdvice";
        }
    }

    public static class Close extends HttpChannelInstrumentation {

        @Override
        public ElementMatcher<? super MethodDescription> getMethodMatcher() {
            return named("close");
        }

        @Override
        public String getAdviceClassName() {
            return "co.elastic.apm.agent.elasticsearch.http.CloseAdvice";
        }
    }
}
