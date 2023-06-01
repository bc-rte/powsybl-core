/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.powsybl.iidm.network.BoundaryLine;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class BoundaryLineDataTest {

    @Test
    void test() {
        BoundaryLine boundaryLine = new BoundaryLineTestData().getBoundaryLine();
        BoundaryLineData dlData = new BoundaryLineData(boundaryLine);

        boolean ok = dlCompareBoundaryBusVoltage(dlData, 406.63382758266334, -8.573434828294932);
        assertTrue(ok);
    }

    @Test
    void testP0Q0zero() {
        BoundaryLineTestData dlTestData = new BoundaryLineTestData();
        dlTestData.setP0Zero();
        dlTestData.setQ0Zero();
        BoundaryLine boundaryLine = dlTestData.getBoundaryLine();
        BoundaryLineData dlData = new BoundaryLineData(boundaryLine);

        boolean ok = dlCompareBoundaryBusVoltage(dlData, 406.6200406620039, -8.60000143239463);
        assertTrue(ok);
    }

    private static boolean dlCompareBoundaryBusVoltage(BoundaryLineData dlData, double boundaryBusU, double boundaryBusAngle) {
        double tol = 0.00001;
        if (Math.abs(dlData.getBoundaryBusU() - boundaryBusU) > tol || Math.abs(Math.toDegrees(dlData.getBoundaryBusTheta()) - boundaryBusAngle) > tol) {
            LOG.info("BoundaryLine {} Expected {} {} Actual {} {}", dlData.getId(),
                boundaryBusU, boundaryBusAngle, dlData.getBoundaryBusU(), Math.toDegrees(dlData.getBoundaryBusTheta()));
            return false;
        }
        return true;
    }

    private static final Logger LOG = LoggerFactory.getLogger(BoundaryLineDataTest.class);
}
