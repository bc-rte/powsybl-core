/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Substation;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface EntsoeArea extends Extension<Substation> {

    String NAME = "entsoeArea";

    @Override
    default String getName() {
        return NAME;
    }

    EntsoeGeographicalCode getCode();

    EntsoeArea setCode(EntsoeGeographicalCode code);
}
