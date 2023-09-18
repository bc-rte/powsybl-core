/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import java.util.OptionalDouble;

import com.powsybl.iidm.network.*;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 * @author Bertrand Rix <bertrand.rix at artelys.com>
 */
class VoltageAngleLimitImpl implements VoltageAngleLimit {

    private final String id;
    private final Terminal fromTerminal;
    private final Terminal toTerminal;
    private final double lowLimit;
    private final double highLimit;

    VoltageAngleLimitImpl(String id, Terminal fromTerminal, Terminal toTerminal, double lowLimit, double highLimit) {
        this.id = id;
        this.fromTerminal = fromTerminal;
        this.toTerminal = toTerminal;
        this.lowLimit = lowLimit;
        this.highLimit = highLimit;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Terminal getTerminalFrom() {
        return fromTerminal;
    }

    @Override
    public Terminal getTerminalTo() {
        return toTerminal;
    }

    @Override
    public OptionalDouble getLowLimit() {
        return Double.isNaN(lowLimit) ? OptionalDouble.empty() : OptionalDouble.of(lowLimit);
    }

    @Override
    public OptionalDouble getHighLimit() {
        return Double.isNaN(highLimit) ? OptionalDouble.empty() : OptionalDouble.of(highLimit);
    }
}
