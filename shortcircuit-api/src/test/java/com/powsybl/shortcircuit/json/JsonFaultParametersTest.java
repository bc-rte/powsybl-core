/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.json;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.shortcircuit.FaultParameters;
import com.powsybl.shortcircuit.ShortCircuitConstants;
import com.powsybl.shortcircuit.StudyType;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class JsonFaultParametersTest extends AbstractConverterTest {

    @Test
    public void roundTrip() throws IOException {
        List<FaultParameters> parameters = new ArrayList<>();
        parameters.add(new FaultParameters("f00", false, false, true, StudyType.STEADY_STATE, 1.0, ShortCircuitConstants.InitialVoltageProfileType.NOMINAL, ShortCircuitConstants.InitialNominalVoltageProfileType.IEC_909_IMAX));
        parameters.add(new FaultParameters("f01", false, true, false, null, Double.NaN, ShortCircuitConstants.InitialVoltageProfileType.PREVIOUS, null));
        parameters.add(new FaultParameters("f10", true, false, false, null, Double.NaN, ShortCircuitConstants.InitialVoltageProfileType.NOMINAL, ShortCircuitConstants.InitialNominalVoltageProfileType.NONE));
        parameters.add(new FaultParameters("f11", true, true, false, null, Double.NaN, null, null));
        roundTripTest(parameters, FaultParameters::write, FaultParameters::read, "/FaultParametersFile.json");

        assertNotNull(parameters.get(0));
        assertNotEquals(parameters.get(0), parameters.get(1));
        assertNotEquals(parameters.get(0).hashCode(), parameters.get(2).hashCode());
        assertEquals(parameters.get(0), parameters.get(0));
    }

    @Test
    public void readVersion10() throws IOException {
        Files.copy(getClass().getResourceAsStream("/FaultParametersFileVersion10.json"), fileSystem.getPath("/FaultParametersFileVersion10.json"));
        List<FaultParameters> parameters = FaultParameters.read(fileSystem.getPath("/FaultParametersFileVersion10.json"));
        assertEquals(4, parameters.size());

        FaultParameters firstParam = parameters.get(0);
        assertEquals("f00", firstParam.getId());
        assertFalse(firstParam.isWithLimitViolations());
        assertEquals(StudyType.STEADY_STATE, firstParam.getStudyType());
        assertTrue(firstParam.isWithFeederResult());
        assertFalse(firstParam.isWithVoltageProfileResult());
        assertEquals(1.0, firstParam.getMinVoltageDropProportionalThreshold(), 0);

        FaultParameters secondParam = parameters.get(1);
        assertEquals("f01", secondParam.getId());
        assertFalse(secondParam.isWithLimitViolations());
        assertNull(secondParam.getStudyType());
        assertFalse(secondParam.isWithFeederResult());
        assertTrue(secondParam.isWithVoltageProfileResult());
        assertEquals(Double.NaN, secondParam.getMinVoltageDropProportionalThreshold(), 0);

        FaultParameters thirdParam = parameters.get(2);
        assertEquals("f10", thirdParam.getId());
        assertTrue(thirdParam.isWithLimitViolations());
        assertNull(thirdParam.getStudyType());
        assertFalse(thirdParam.isWithFeederResult());
        assertFalse(thirdParam.isWithVoltageProfileResult());
        assertEquals(Double.NaN, thirdParam.getMinVoltageDropProportionalThreshold(), 0);

        FaultParameters fourthParam = parameters.get(3);
        assertEquals("f11", fourthParam.getId());
        assertTrue(fourthParam.isWithLimitViolations());
        assertNull(fourthParam.getStudyType());
        assertFalse(fourthParam.isWithFeederResult());
        assertTrue(fourthParam.isWithVoltageProfileResult());
        assertEquals(Double.NaN, fourthParam.getMinVoltageDropProportionalThreshold(), 0);
    }

    @Test
    public void readVersion11() throws IOException {
        Files.copy(getClass().getResourceAsStream("/FaultParametersFileVersion11.json"), fileSystem.getPath("/FaultParametersFileVersion11.json"));
        List<FaultParameters> parameters = FaultParameters.read(fileSystem.getPath("/FaultParametersFileVersion11.json"));
        assertEquals(1, parameters.size());

        FaultParameters firstParam = parameters.get(0);
        assertEquals("f00", firstParam.getId());
        assertFalse(firstParam.isWithLimitViolations());
        assertEquals(StudyType.STEADY_STATE, firstParam.getStudyType());
        assertTrue(firstParam.isWithFeederResult());
        assertFalse(firstParam.isWithVoltageProfileResult());
        assertEquals(1.0, firstParam.getMinVoltageDropProportionalThreshold(), 0);
        assertEquals(ShortCircuitConstants.InitialVoltageProfileType.NOMINAL, firstParam.getInitialVoltageProfileType());
        assertEquals(ShortCircuitConstants.InitialNominalVoltageProfileType.IEC_909_IMAX, firstParam.getInitialNominalVoltageProfileType());

    }

    @Test
    public void readUnexpectedField() throws IOException {
        Files.copy(getClass().getResourceAsStream("/FaultParametersFileInvalid.json"), fileSystem.getPath("/FaultParametersFileInvalid.json"));

        expected.expect(AssertionError.class);
        expected.expectMessage("Unexpected field: unexpected");
        FaultParameters.read(fileSystem.getPath("/FaultParametersFileInvalid.json"));
    }
}
