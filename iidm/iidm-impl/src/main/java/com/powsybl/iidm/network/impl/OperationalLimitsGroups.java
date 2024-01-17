/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.OperationalLimits;
import com.powsybl.iidm.network.OperationalLimitsGroup;
import com.powsybl.iidm.network.Validable;

import java.util.Optional;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface OperationalLimitsGroups {

    OperationalLimitsGroup newOperationalLimitsGroup(String id);

    void setDefault(String id);

    Optional<OperationalLimitsGroup> getDefaultOperationalLimitsGroup();

    interface GroupsValidable extends Validable {
        void notifyUpdateIfDefaultLimits(String id, LimitType limitType, String attribute, double oldValue, double newValue);

        void notifyUpdateIfDefaultLimits(String id, LimitType limitType, OperationalLimits oldValue, OperationalLimits newValue);
    }
}
