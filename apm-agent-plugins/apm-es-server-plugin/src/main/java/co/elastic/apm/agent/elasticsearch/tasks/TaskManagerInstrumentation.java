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
/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package co.elastic.apm.agent.elasticsearch.tasks;

import co.elastic.apm.agent.elasticsearch.ElasticsearchInstrumentation;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

public abstract class TaskManagerInstrumentation extends ElasticsearchInstrumentation {

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return named("org.elasticsearch.tasks.TaskManager");
    }

    public static class Register extends TaskManagerInstrumentation {

        @Override
        public ElementMatcher<? super MethodDescription> getMethodMatcher() {
            return named("register")
                .and(takesArgument(0, String.class))
                .and(takesArgument(1, String.class))
                .and(takesArgument(2, named("org.elasticsearch.tasks.TaskAwareRequest")));
        }

        @Override
        public String getAdviceClassName() {
            return "co.elastic.apm.agent.elasticsearch.tasks.TaskRegisterAdvice";
        }
    }

    public static class Unregister extends TaskManagerInstrumentation {

        @Override
        public ElementMatcher<? super MethodDescription> getMethodMatcher() {
            return named("unregister")
                .and(takesArgument(0, named("org.elasticsearch.tasks.Task")));
        }

        @Override
        public String getAdviceClassName() {
            return "co.elastic.apm.agent.elasticsearch.tasks.TaskUnregisterAdvice";
        }
    }

}
