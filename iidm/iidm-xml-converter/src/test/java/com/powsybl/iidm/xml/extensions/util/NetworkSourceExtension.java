/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.xml.extensions.util;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;

/**
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
public interface NetworkSourceExtension extends Extension<Network> {
    String NAME = "networkSource";

    String getSourceData();

    @Override
    default String getName() {
        return NAME;
    }
}
