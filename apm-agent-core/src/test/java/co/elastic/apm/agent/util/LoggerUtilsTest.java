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
package co.elastic.apm.agent.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.Marker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class LoggerUtilsTest {

    @Test
    void testLogOnce() {
        Logger mock = mock(Logger.class);
        Logger logger = LoggerUtils.logOnce(mock);

        logger.info("once");
        logger.warn("twice");

        verify(mock).info("once");
        verifyNoMoreInteractions(mock);
    }

    @Test
    void testDisabledAfterLogOnce() {
        Logger mock = mock(Logger.class);
        Marker marker = mock(Marker.class);
        doReturn(true).when(mock).isTraceEnabled();
        doReturn(true).when(mock).isTraceEnabled(marker);
        doReturn(true).when(mock).isDebugEnabled();
        doReturn(true).when(mock).isDebugEnabled(marker);
        doReturn(true).when(mock).isInfoEnabled();
        doReturn(true).when(mock).isInfoEnabled(marker);
        doReturn(true).when(mock).isWarnEnabled();
        doReturn(true).when(mock).isWarnEnabled(marker);
        doReturn(true).when(mock).isErrorEnabled();
        doReturn(true).when(mock).isErrorEnabled(marker);

        Logger logger = LoggerUtils.logOnce(mock);

        checkAllLevels(logger, marker, true);
        logger.info("once");
        checkAllLevels(logger, marker, false);
    }

    private void checkAllLevels(Logger logger, Marker marker, boolean isEnabled) {
        assertThat(logger.isTraceEnabled()).isEqualTo(isEnabled);
        assertThat(logger.isTraceEnabled(marker)).isEqualTo(isEnabled);
        assertThat(logger.isDebugEnabled()).isEqualTo(isEnabled);
        assertThat(logger.isDebugEnabled(marker)).isEqualTo(isEnabled);
        assertThat(logger.isInfoEnabled()).isEqualTo(isEnabled);
        assertThat(logger.isInfoEnabled(marker)).isEqualTo(isEnabled);
        assertThat(logger.isWarnEnabled()).isEqualTo(isEnabled);
        assertThat(logger.isWarnEnabled(marker)).isEqualTo(isEnabled);
        assertThat(logger.isErrorEnabled()).isEqualTo(isEnabled);
        assertThat(logger.isErrorEnabled(marker)).isEqualTo(isEnabled);
    }
}
