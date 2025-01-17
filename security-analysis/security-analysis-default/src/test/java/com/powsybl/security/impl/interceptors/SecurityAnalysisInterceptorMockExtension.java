/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.impl.interceptors;

import com.google.auto.service.AutoService;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptorExtension;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
@AutoService(SecurityAnalysisInterceptorExtension.class)
public class SecurityAnalysisInterceptorMockExtension implements SecurityAnalysisInterceptorExtension {
    @Override
    public String getName() {
        return "SecurityAnalysisInterceptorMock";
    }

    @Override
    public SecurityAnalysisInterceptor createInterceptor() {
        return new SecurityAnalysisInterceptorMock();
    }
}
