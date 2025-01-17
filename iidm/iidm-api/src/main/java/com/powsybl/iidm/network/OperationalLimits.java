/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 * @author Thibaut Vermeulen {@literal <thibaut.vermeulen at rte-france.com>}
 */
public interface OperationalLimits {
    /**
     * Get the operational limits' type (can be APPARENT_POWER, CURRENT or VOLTAGE)
     */
    LimitType getLimitType();

    default void remove() {
        // do nothing
    }
}
