/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesSubset;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 *
 */
public class LoadToAsynchronousMachine extends IidmToCgmes {

    LoadToAsynchronousMachine() {
        // Ignore changes on (p, q) from Load Terminal
        // They will be added directly as new objects in SV subset
        ignore("p");
        ignore("q");

        simpleUpdate("p0", "cim:RotatingMachine.p", CgmesSubset.STEADY_STATE_HYPOTHESIS);
        simpleUpdate("q0", "cim:RotatingMachine.q", CgmesSubset.STEADY_STATE_HYPOTHESIS);
    }
}
