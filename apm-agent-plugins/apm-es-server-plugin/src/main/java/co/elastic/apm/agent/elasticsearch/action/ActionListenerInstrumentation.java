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

package co.elastic.apm.agent.elasticsearch.action;

import co.elastic.apm.agent.elasticsearch.ElasticsearchInstrumentation;
import co.elastic.apm.agent.sdk.ElasticApmInstrumentation;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.any;
import static net.bytebuddy.matcher.ElementMatchers.named;

@Deprecated
public abstract class ActionListenerInstrumentation extends ElasticsearchInstrumentation {

    public static List<Class<? extends ElasticApmInstrumentation>> ALL = Arrays.<Class<? extends ElasticApmInstrumentation>>asList(OnFailure.class, OnResponse.class);

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        // applied at runtime
        return any();
    }

    public static class OnFailure extends ActionListenerInstrumentation {
        @Override
        public ElementMatcher<? super MethodDescription> getMethodMatcher() {
            return named("onFailure");
        }

        @Override
        public String getAdviceClassName() {
            return "co.elastic.apm.agent.elasticsearch.action.OnFailureAdvice";
        }
    }

    public static class OnResponse extends ActionListenerInstrumentation {
        @Override
        public ElementMatcher<? super MethodDescription> getMethodMatcher() {
            return named("onResponse");
        }

        @Override
        public String getAdviceClassName() {
            return "co.elastic.apm.agent.elasticsearch.action.OnResponseAdvice";
        }
    }


}
