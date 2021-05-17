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

package co.elastic.apm.agent.elasticsearch;

import co.elastic.apm.agent.impl.transaction.AbstractSpan;
import co.elastic.apm.agent.impl.transaction.Transaction;
import co.elastic.apm.agent.sdk.state.GlobalState;
import co.elastic.apm.agent.sdk.weakmap.WeakMapSupplier;
import com.blogspot.mydailyjava.weaklockfree.WeakConcurrentMap;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.http.HttpChannel;
import org.elasticsearch.http.HttpRequest;
import org.elasticsearch.http.HttpResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.tasks.Task;

/**
 * Provides storage for ES instrumentation global state
 * <br/>
 * We have to use a global state instance as many parts of ES server are loaded in different classloaders
 * which makes agent use one plugin CL per classloader.
 */
@GlobalState
public class ElasticsearchGlobalState {

    private static final ElasticsearchGlobalState INSTANCE = new ElasticsearchGlobalState();

    public static ElasticsearchGlobalState getInstance() {
        return INSTANCE;
    }

    // using types that refer to ES classes is fine here due to type erasure.
    public final WeakConcurrentMap<HttpChannel, Transaction> httpChannel2Transaction;
    public final WeakConcurrentMap<HttpRequest, Transaction> httpRequest2Transaction;
    public final WeakConcurrentMap<HttpResponse, Transaction> httpResponse2Transaction;
    public final WeakConcurrentMap<RestChannel, Transaction> restChannel2Transaction;
    public final WeakConcurrentMap<ActionListener<?>, Transaction> actionListener2Transaction;
    public final WeakConcurrentMap<Task, AbstractSpan<?>> activeTasks;

    // package-private for testing
    ElasticsearchGlobalState() {
        httpChannel2Transaction = WeakMapSupplier.createMap();
        restChannel2Transaction = WeakMapSupplier.createMap();
        httpRequest2Transaction = WeakMapSupplier.createMap();
        actionListener2Transaction = WeakMapSupplier.createMap();
        httpResponse2Transaction = WeakMapSupplier.createMap();
        activeTasks = WeakMapSupplier.createMap();
    }


}
