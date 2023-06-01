/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.BoundaryLineNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class BoundaryLineTrippingTest extends AbstractTrippingTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = BoundaryLineNetworkFactory.create();
    }

    @Test
    void dlTrippingTest() {
        assertTrue(network.getBoundaryLine("DL").getTerminal().isConnected());

        BoundaryLineTripping tripping = new BoundaryLineTripping("DL");
        tripping.apply(network);

        assertFalse(network.getBoundaryLine("DL").getTerminal().isConnected());
    }

    @Test
    void unknownDlTest() {
        BoundaryLineTripping tripping = new BoundaryLineTripping("DL_THAT_DO_NOT_EXIST");
        assertThrows(PowsyblException.class, () -> tripping.apply(network));
    }
}
