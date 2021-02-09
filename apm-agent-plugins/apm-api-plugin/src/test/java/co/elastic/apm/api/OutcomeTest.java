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
package co.elastic.apm.api;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class OutcomeTest {

    @Test
    void apiOutcomeMatchesInternalOutcome() {

        Set<String> apiEnumNames = getEnumValues(Outcome.values());
        Set<String> internalEnumNames = getEnumValues(co.elastic.apm.agent.impl.transaction.Outcome.values());

        assertThat(apiEnumNames)
            .containsExactlyInAnyOrderElementsOf(internalEnumNames);

    }

    private Set<String> getEnumValues(Enum<?>[] values) {
        return Arrays.stream(values)
            .map(Enum::name)
            .collect(Collectors.toSet());
    }


}