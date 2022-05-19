/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsSet;
import com.powsybl.iidm.network.LimitType;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class CurrentLimitsSetImpl extends AbstractOperationalLimitsSet<CurrentLimits> implements CurrentLimitsSet {
    CurrentLimitsSetImpl(OperationalLimitsOwner owner) {
        super(owner);
    }

    @Override
    LimitType getType() {
        return LimitType.CURRENT;
    }
}
