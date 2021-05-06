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

package co.elastic.apm.agent.elasticsearch.rest;

import co.elastic.apm.agent.elasticsearch.ElasticsearchInstrumentation;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.isConstructor;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

public abstract class DefaultRestChannelInstrumentation extends ElasticsearchInstrumentation {

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return named("org.elasticsearch.http.DefaultRestChannel");
    }

    public static class ConstructorInstrumentation extends DefaultRestChannelInstrumentation {
        @Override
        public ElementMatcher<? super MethodDescription> getMethodMatcher() {
            return isConstructor();
        }

        @Override
        public String getAdviceClassName() {
            return "co.elastic.apm.agent.elasticsearch.rest.ConstructorAdvice";
        }
    }

    public static class SendResponseInstrumentation extends DefaultRestChannelInstrumentation {
        @Override
        public ElementMatcher<? super MethodDescription> getMethodMatcher() {
            return named("sendResponse")
                .and(takesArgument(0, named("org.elasticsearch.rest.RestResponse")));
        }

        @Override
        public String getAdviceClassName() {
            return "co.elastic.apm.agent.elasticsearch.rest.SendResponseAdvice";
        }
    }


}
