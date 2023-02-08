/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.converter;

import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.shortcircuit.*;
import com.powsybl.shortcircuit.json.ShortCircuitAnalysisResultDeserializer;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class ShortCircuitAnalysisResultExportersTest extends AbstractConverterTest {

    @Test
    public void testGetFormats() {
        assertEquals("[ASCII, CSV, JSON]", ShortCircuitAnalysisResultExporters.getFormats().toString());
    }

    @Test
    public void testGetExporter() {
        assertEquals("ASCII", ShortCircuitAnalysisResultExporters.getExporter("ASCII").getFormat());
        assertEquals("CSV", ShortCircuitAnalysisResultExporters.getExporter("CSV").getFormat());
        assertEquals("JSON", ShortCircuitAnalysisResultExporters.getExporter("JSON").getFormat());
    }

    @Test
    public void testComment() {
        assertEquals("Export a result in ASCII tables", ShortCircuitAnalysisResultExporters.getExporter("ASCII").getComment());
        assertEquals("Export a result in a CSV-like format", ShortCircuitAnalysisResultExporters.getExporter("CSV").getComment());
        assertEquals("Export a result in JSON format", ShortCircuitAnalysisResultExporters.getExporter("JSON").getComment());
    }

    public void writeJson(ShortCircuitAnalysisResult results, Path path) {
        ShortCircuitAnalysisResultExporters.export(results, path, "JSON", null);
    }

    @Test
    public void testWriteJson() throws IOException {
        ShortCircuitAnalysisResult result = TestingResultFactory.createResultWithExtension();
        writeTest(result, this::writeJson, ComparisonUtils::compareTxt, "/shortcircuit-with-extensions-results.json");
    }

    @Test
    public void roundTripJson() throws IOException {
        ShortCircuitAnalysisResult result = TestingResultFactory.createResultWithExtension();
        roundTripTest(result, this::writeJson, ShortCircuitAnalysisResultDeserializer::read, "/shortcircuit-with-extensions-results.json");
    }

    @Test
    public void testJsonWithFeederResult() throws IOException {
        ShortCircuitAnalysisResult result = TestingResultFactory.createWithFeederResults();
        writeTest(result, this::writeJson, ComparisonUtils::compareTxt, "/shortcircuit-results-with-feeder-result.json");
    }

    @Test
    public void roundTripJsonWithFeederResult() throws IOException {
        ShortCircuitAnalysisResult result = TestingResultFactory.createWithFeederResults();
        roundTripTest(result, this::writeJson, ShortCircuitAnalysisResultDeserializer::read, "/shortcircuit-results-with-feeder-result.json");
    }

    @Test
    public void readJsonFaultResultVersion10() {
        ShortCircuitAnalysisResult result = ShortCircuitAnalysisResultDeserializer
                .read(getClass().getResourceAsStream("/shortcircuit-results-version10.json"));
        assertEquals(1, result.getFaultResults().size());
        FortescueFaultResult faultResult = (FortescueFaultResult) result.getFaultResult("id");
        assertEquals(1.0, faultResult.getCurrent().getDirectMagnitude(), 0);
        assertEquals(1, faultResult.getLimitViolations().size());
        assertEquals(1, faultResult.getFeederResults().size());
    }

    @Test
    public void readJsonFaultResultVersion11() {
        ShortCircuitAnalysisResult result = ShortCircuitAnalysisResultDeserializer
                .read(getClass().getResourceAsStream("/shortcircuit-results-version11.json"));
        assertEquals(1, result.getFaultResults().size());
        MagnitudeFaultResult faultResult = (MagnitudeFaultResult) result.getFaultResult("id");
        assertEquals(1.0, faultResult.getCurrent(), 0);
        assertEquals(1, faultResult.getLimitViolations().size());
        assertEquals(1, faultResult.getFeederResults().size());
        assertEquals(2.0, faultResult.getVoltage(), 0);
    }

    public void writeCsv(ShortCircuitAnalysisResult result, Path path) {
        Network network = EurostagTutorialExample1Factory.create();
        ShortCircuitAnalysisResultExporters.export(result, path, "CSV", network);
    }

    @Test
    public void testWriteCsv() throws IOException {
        ShortCircuitAnalysisResult result = TestingResultFactory.createResult();
        writeTest(result, this::writeCsv, ComparisonUtils::compareTxt, "/shortcircuit-results.csv");
    }

    @Test
    public void roundtripTestWithTwoFaults() throws IOException {
        ShortCircuitAnalysisResult result = TestingResultFactory.createResultWithTwoFaultResults();
        roundTripTest(result, this::writeJson, ShortCircuitAnalysisResultDeserializer::read, "/shortcircuit-results-with-two-faults.json");
    }

    @Test
    public void roundTripJsonMagnitudeResults() throws IOException {
        ShortCircuitAnalysisResult result = TestingResultFactory.createMagnitudeResult();
        roundTripTest(result, this::writeJson, ShortCircuitAnalysisResultDeserializer::read, "/shortcircuit-magnitude-results.json");
    }

    @Test
    public void roundTripJsonFortescueResults() throws IOException {
        ShortCircuitAnalysisResult result = TestingResultFactory.createFortescueResult();
        roundTripTest(result, this::writeJson, ShortCircuitAnalysisResultDeserializer::read, "/shortcircuit-fortescue-results.json");
    }

    @Test
    public void roundTripJsonFailedResults() throws IOException {
        ShortCircuitAnalysisResult result = new ShortCircuitAnalysisResult(Collections.singletonList(new FailedFaultResult(new BusFault("id", "elementId"), FaultResult.Status.FAILURE)));
        roundTripTest(result, this::writeJson, ShortCircuitAnalysisResultDeserializer::read, "/shortcircuit-failed-result.json");
    }
}
