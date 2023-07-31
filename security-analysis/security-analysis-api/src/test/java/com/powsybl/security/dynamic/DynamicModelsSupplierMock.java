/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic;

import com.powsybl.dynamicsimulation.DynamicModelsSupplier;

import java.util.Collections;

/**
 * @author Laurent Issertial <laurent.issertial at rte-france.com>
 */
final class DynamicModelsSupplierMock {

    private DynamicModelsSupplierMock() {
    }

    static DynamicModelsSupplier empty() {
        return network -> Collections.emptyList();
    }

}
