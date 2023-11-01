/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.dynamicsimulation;

import java.util.Collections;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public interface CurvesSupplier extends SimulatorInputSupplier<Curve> {

    static CurvesSupplier empty() {
        return network -> Collections.emptyList();
    }
}
