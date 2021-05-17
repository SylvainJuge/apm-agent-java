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

package co.elastic.apm.agent.elasticsearch.rest;

import co.elastic.apm.agent.elasticsearch.ElasticsearchHelper;
import net.bytebuddy.asm.Advice;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;

import javax.annotation.Nullable;

public class HandleRequestAdvice {

    public static final ElasticsearchHelper helper = ElasticsearchHelper.getInstance();

    @Nullable
    @Advice.OnMethodEnter(suppress = Throwable.class, inline = false)
    public static Object onEnter(@Advice.Argument(1) RestChannel restChannel) {
        return helper.spanStart();
    }

    @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class, inline = false)
    public static void onExit(@Advice.Enter @Nullable Object spanObj,
                              @Advice.This BaseRestHandler restHandler,
                              @Advice.Thrown @Nullable Throwable thrown) {

        helper.spanEnd(restHandler.getName(), spanObj, null, thrown);
    }
}
