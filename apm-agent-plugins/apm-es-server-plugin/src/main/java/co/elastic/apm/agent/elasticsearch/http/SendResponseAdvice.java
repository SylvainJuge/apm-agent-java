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

import co.elastic.apm.agent.elasticsearch.ElasticsearchHelper;
import co.elastic.apm.agent.impl.transaction.Transaction;
import net.bytebuddy.asm.Advice;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.http.HttpChannel;

import javax.annotation.Nullable;

public class SendResponseAdvice {

    private static final ElasticsearchHelper helper = ElasticsearchHelper.getInstance();

    @Advice.OnMethodEnter(suppress = Throwable.class, inline = false)
    public static void onEnter(@Advice.This HttpChannel channel,
                               @Advice.Argument(1) ActionListener<?> listener) {
        Transaction transaction = helper.getTransaction(channel);

        if (transaction == null) {
            return;
        }

//        helper.registerListener(listener, transaction);

        // make sure that transaction is active during HttpChannel#sendResponse method execution
        // so that HttpRequest#createResponse can lookup the active transaction
        transaction.activate();
    }

    @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class, inline = false)
    public static void onExit(@Advice.This HttpChannel channel,
                              @Advice.Thrown @Nullable Throwable thrown) {

        Transaction transaction = helper.getTransaction(channel);

        if (transaction == null) {
            return;
        }

        transaction.captureException(thrown)
            .deactivate();

    }
}
