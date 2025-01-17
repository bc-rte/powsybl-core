/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.impl.interceptors;

import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptors;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class SecurityAnalysisInterceptorTest {

    @Test
    void test() {
        assertEquals(Collections.singleton("SecurityAnalysisInterceptorMock"), SecurityAnalysisInterceptors.getExtensionNames());

        SecurityAnalysisInterceptor interceptor = SecurityAnalysisInterceptors.createInterceptor("SecurityAnalysisInterceptorMock");
        assertNotNull(interceptor);
        assertEquals(SecurityAnalysisInterceptorMock.class, interceptor.getClass());

        try {
            interceptor = SecurityAnalysisInterceptors.createInterceptor(null);
            fail();
        } catch (NullPointerException e) {
            // Nothing to do
        }

        try {
            interceptor = SecurityAnalysisInterceptors.createInterceptor("unknown-security-analysis-interceptor");
            fail();
        } catch (IllegalArgumentException e) {
            // Nothing to do
        }
    }
}
