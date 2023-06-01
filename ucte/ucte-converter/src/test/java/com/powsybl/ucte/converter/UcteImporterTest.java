/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.entsoe.util.EntsoeArea;
import com.powsybl.entsoe.util.EntsoeGeographicalCode;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.NetworkFactoryImpl;
import com.powsybl.ucte.converter.util.UcteConstants;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
class UcteImporterTest {

    @Test
    void trimIssueTest() {
        // Import network that could fail because of id conflicts due to trim mechanism
        ReadOnlyDataSource dataSource = new ResourceDataSource("importIssue", new ResourceSet("/", "importIssue.uct"));

        assertDoesNotThrow(() -> new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null));
    }

    @Test
    void countryAssociationIssueTest() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("countryIssue", new ResourceSet("/", "countryIssue.uct"));

        Network network = new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);

        assertEquals(Country.ES, network.getSubstation("EHORTA").getCountry().orElse(null));
        assertEquals(1, network.getSubstation("EHORTA").getVoltageLevelStream().count());
        assertEquals(Country.BE, network.getSubstation("BHORTA").getCountry().orElse(null));
        assertEquals(1, network.getSubstation("BHORTA").getVoltageLevelStream().count());

    }

    @Test
    void germanTsosImport() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("germanTsos", new ResourceSet("/", "germanTsos.uct"));

        Network network = new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);

        //Check D4 is correctly parsed
        EntsoeArea ext = network.getSubstation("D4NEUR").getExtension(EntsoeArea.class);
        assertNotNull(ext);
        assertEquals(EntsoeGeographicalCode.D4, ext.getCode());

        //Check that for other countries, no extension is added
        ext = network.getSubstation("BAVLGM").getExtension(EntsoeArea.class);
        assertNull(ext);

        //Check that for "D-nodes", no extension is added
        ext = network.getSubstation("DJA_KA").getExtension(EntsoeArea.class);
        assertNull(ext);

        //Check that for a "D-node" starting with "DE", no extension is added
        ext = network.getSubstation("DEA_KA").getExtension(EntsoeArea.class);
        assertNull(ext);

    }

    @Test
    void elementNameTest() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("elementName", new ResourceSet("/", "elementName.uct"));

        Network network = new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);
        // Test Element name Line
        assertEquals("Test Line", network.getLine("F_SU1_12 F_SU2_11 1").getProperty("elementName"));
        // Test Boundary line element name
        assertEquals("Test DL", network.getBoundaryLine("XG__F_21 F_SU1_21 1").getProperty("elementName"));
        // Test Switch element name
        assertEquals("Test Coupler", network.getSwitch("F_SU1_12 F_SU1_11 1").getProperty("elementName"));
        // Test 2WT element name
        assertEquals("Test 2WT 1", network.getBranch("F_SU1_11 F_SU1_21 1").getProperty("elementName"));
        assertEquals("Test 2WT 2", network.getBranch("B_SU1_11 B_SU1_21 1").getProperty("elementName"));
        // Test tie line
        // cannot refer to side of tieline directly cause order of half lines may change
        // at import : due to HashSet iterator on boundary lines ?
        TieLine tieLine1 = network.getTieLineStream()
                .filter(line -> line.getBoundaryLine1().getId().contains("XB__F_11 B_SU1_11 1") || line.getBoundaryLine2().getId().contains("XB__F_11 B_SU1_11 1")).findAny().orElseThrow();
        String expectedElementName1 = tieLine1.getBoundaryLine1().getId().contains("XB__F_11 B_SU1_11 1") ? "Test TL 1/2" : "Test TL 1/1";
        String expectedElementName2 = tieLine1.getBoundaryLine2().getId().contains("XB__F_11 B_SU1_11 1") ? "Test TL 1/2" : "Test TL 1/1";
        assertEquals(expectedElementName1, tieLine1.getProperty("elementName_1"));
        assertEquals(expectedElementName2, tieLine1.getProperty("elementName_2"));

        TieLine tieLine2 = network.getTieLineStream()
                .filter(line -> line.getBoundaryLine1().getId().contains("XB__F_21 B_SU1_21 1") || line.getBoundaryLine2().getId().contains("XB__F_21 B_SU1_21 1")).findAny().orElseThrow();
        expectedElementName1 = tieLine2.getBoundaryLine1().getId().contains("XB__F_21 B_SU1_21 1") ? "Test TL 2/2" : "Test TL 2/1";
        expectedElementName2 = tieLine2.getBoundaryLine2().getId().contains("XB__F_21 B_SU1_21 1") ? "Test TL 2/2" : "Test TL 2/1";
        assertEquals(expectedElementName1, tieLine2.getProperty("elementName_1"));
        assertEquals(expectedElementName2, tieLine2.getProperty("elementName_2"));
    }

    @Test
    void xnodeMergingIssueTest() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("mergedXnodeIssue", new ResourceSet("/", "mergedXnodeIssue.uct"));

        Network network = new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);

        assertEquals(2, network.getVoltageLevelCount());
        assertEquals(1, network.getBoundaryLineStream(BoundaryLineFilter.UNPAIRED).count());
        assertEquals(1, network.getTieLineCount());
        TieLine l = network.getTieLineStream().findFirst().orElseThrow(IllegalStateException::new);
        assertEquals("ESNODE11 XXNODE11 1 + FRNODE11 XXNODE11 1", l.getId());
        assertTrue(l.getBoundaryLine1().getCurrentLimits().isPresent());
        assertTrue(l.getBoundaryLine2().getCurrentLimits().isPresent());
    }

    @Test
    void lineAndTransformerSameId() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("sameId", new ResourceSet("/", "sameId.uct"));

        Network network = new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);

        assertEquals(0, network.getLineCount());
        assertEquals(1, network.getTwoWindingsTransformerCount());
        assertEquals(1, network.getSwitchStream().count());
    }

    @Test
    void testCouplerToXnodeImport() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("couplerToXnodeExample", new ResourceSet("/", "couplerToXnodeExample.uct"));
        Network network = new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);
        assertEquals(1, network.getBusBreakerView().getBusStream().count());
    }

    @Test
    void testEmptyLastCharacterOfLineImport() {
        ResourceDataSource dataSource = new ResourceDataSource("lastCharacterIssue", new ResourceSet("/", "lastCharacterIssue.uct"));
        Network network = new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);
        assertEquals(2, network.getBusBreakerView().getBusStream().count());
    }

    @Test
    void testImportLinesDifferentNominalvoltage() {
        ResourceDataSource dataSource = new ResourceDataSource("differentLinesVoltage", new ResourceSet("/", "differentLinesVoltage.uct"));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null));
        assertTrue(e.getMessage().contains("with two different nominal voltages"));
    }

    @Test
    void testVoltageRegulatingXnode() {
        ResourceDataSource dataSource = new ResourceDataSource("frVoltageRegulatingXnode", new ResourceSet("/", "frVoltageRegulatingXnode.uct"));
        Network network = new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);
        BoundaryLine bl = network.getBoundaryLine("FFFFFF13 XXXXXX14 1");
        assertTrue(bl.getGeneration().isVoltageRegulationOn());
        assertEquals(409.08, bl.getGeneration().getTargetV(), 0.01);
        assertEquals(1.0, bl.getGeneration().getTargetP(), 0.01);
        assertEquals(2.0, bl.getGeneration().getMaxP(), 0.01);
        assertEquals(-2.0, bl.getGeneration().getMinP(), 0.01);
        assertEquals(1.0, bl.getGeneration().getReactiveLimits().getMaxQ(bl.getGeneration().getTargetP()), 0.01);
        assertEquals(-1.0, bl.getGeneration().getReactiveLimits().getMinQ(bl.getGeneration().getTargetP()), 0.01);
    }

    @Test
    void testXnodeTransformer() {
        ResourceDataSource dataSource = new ResourceDataSource("xNodeTransformer", new ResourceSet("/", "xNodeTransformer.uct"));
        Network network = new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);
        assertEquals(2, network.getBusBreakerView().getBusStream().count());
    }

    @Test
    void substationNameInvariance() {
        ResourceDataSource dataSource = new ResourceDataSource("substationName", new ResourceSet("/", "substationName.uct"));

        Network network = new UcteImporter().importData(dataSource, new NetworkFactoryImpl(), null);
        assertEquals(2, network.getSubstationCount());
        assertNotNull(network.getSubstation("F1TEST"));
        assertNotNull(network.getSubstation("FTEST1"));
        assertNull(network.getSubstation("FTESTA"));
        assertNull(network.getSubstation("FTESTB"));
    }

    @Test
    void testInvalidRegulation() {
        ResourceDataSource dataSource = new ResourceDataSource("invalidRegulationNetwork", new ResourceSet("/", "invalidRegulationNetwork.uct"));
        assertDoesNotThrow(() -> new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null));
    }

    @Test
    void testInvalidVoltageReference() {
        ResourceDataSource dataSource = new ResourceDataSource("invalidVoltageReference", new ResourceSet("/", "invalidVoltageReference.uct"));
        assertDoesNotThrow(() -> new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null));
    }

    @Test
    void checkTapPositionsRangeIsExtended() {
        ResourceDataSource dataSource = new ResourceDataSource("tapPositionsRange", new ResourceSet("/", "tapPositionsRange.uct"));
        Network network = new UcteImporter().importData(dataSource, new NetworkFactoryImpl(), null);
        // Ratio tap with negative tap position higher than initial tap's number
        assertEquals(-7, network.getTwoWindingsTransformer("0BBBBB5  0AAAAA2  1").getRatioTapChanger().getLowTapPosition());
        assertEquals(7, network.getTwoWindingsTransformer("0BBBBB5  0AAAAA2  1").getRatioTapChanger().getHighTapPosition());
        // Phase tap with positive tap position higher than initial tap's number
        assertEquals(-9, network.getTwoWindingsTransformer("HDDDDD2  HCCCCC1  1").getPhaseTapChanger().getLowTapPosition());
        assertEquals(9, network.getTwoWindingsTransformer("HDDDDD2  HCCCCC1  1").getPhaseTapChanger().getHighTapPosition());
        // Phase tap with negative tap position equal to initial tap's number
        assertEquals(-8, network.getTwoWindingsTransformer("ZABCD221 ZEFGH221 1").getPhaseTapChanger().getLowTapPosition());
        assertEquals(8, network.getTwoWindingsTransformer("ZABCD221 ZEFGH221 1").getPhaseTapChanger().getHighTapPosition());
    }

    @Test
    void importOfNetworkWithXnodesConnectedToOneClosedLineMustSucceed() {
        ResourceDataSource dataSource = new ResourceDataSource("xnodeOneClosedLine", new ResourceSet("/", "xnodeOneClosedLine.uct"));
        Network network = new UcteImporter().importData(dataSource, new NetworkFactoryImpl(), null);
        assertNotNull(network.getBoundaryLine("FFFFFF12 XXXXXX11 1"));
    }

    @Test
    void importOfNetworkWithXnodesConnectedToTwoClosedLineMustSucceed() {
        ResourceDataSource dataSource = new ResourceDataSource("xnodeTwoClosedLine", new ResourceSet("/", "xnodeTwoClosedLine.uct"));
        Network network = new UcteImporter().importData(dataSource, new NetworkFactoryImpl(), null);
        assertNotNull(network.getTieLine("BEBBBB11 XXXXXX11 1 + FFFFFF12 XXXXXX11 1"));
        assertNotNull(network.getBoundaryLine("FFFFFF11 XXXXXX11 1"));
        assertNotNull(network.getBoundaryLine("BEBBBB12 XXXXXX11 1"));
    }

    @Test
    void importOfNetworkWithXnodesConnectedToMoreThanTwoClosedLineMustFail() {
        ResourceDataSource dataSource = new ResourceDataSource("xnodeThreeClosedLine", new ResourceSet("/", "xnodeTwoClosedLine.uct"));
        assertThrows(UcteException.class, () -> new UcteImporter().importData(dataSource, new NetworkFactoryImpl(), null));
    }

    @Test
    void checkPhaseShifterRegulationMode() {
        ResourceDataSource dataSource = new ResourceDataSource("phaseShifterActivePowerOn", new ResourceSet("/", "phaseShifterActivePowerOn.uct"));
        Network network = new UcteImporter().importData(dataSource, new NetworkFactoryImpl(), null);
        assertSame(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, network.getTwoWindingsTransformer("HDDDDD2  HCCCCC1  1").getPhaseTapChanger().getRegulationMode());
        assertSame(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, network.getTwoWindingsTransformer("ZABCD221 ZEFGH221 1").getPhaseTapChanger().getRegulationMode());
        assertEquals(0.0, network.getTwoWindingsTransformer("ZABCD221 ZEFGH221 1").getPhaseTapChanger().getTargetDeadband(), 1e-3);
        assertEquals(network.getTwoWindingsTransformer("ZABCD221 ZEFGH221 1").getTerminal1(), network.getTwoWindingsTransformer("ZABCD221 ZEFGH221 1").getPhaseTapChanger().getRegulationTerminal());
        assertEquals(-5.0, network.getTwoWindingsTransformer("ZABCD221 ZEFGH221 1").getPhaseTapChanger().getRegulationValue(), 1e-3);
        assertFalse(network.getTwoWindingsTransformer("ZABCD221 ZEFGH221 1").getPhaseTapChanger().isRegulating());
    }

    @Test
    void ignoreCoupler() {
        ResourceDataSource dataSource = new ResourceDataSource("ignoreCoupler", new ResourceSet("/", "ignoreCoupler.uct"));
        Network network = new UcteImporter().importData(dataSource, new NetworkFactoryImpl(), null);
        assertNull(network.getSwitch("BBBBBB11 BBBBBB11 1"));
    }

    @Test
    void emptyElementName() {
        ResourceDataSource dataSource = new ResourceDataSource("emptyElementName", new ResourceSet("/", "emptyElementName.uct"));
        Network network = new UcteImporter().importData(dataSource, new NetworkFactoryImpl(), null);
        Line l = network.getLine("F_SU1_12 F_SU1_11 1");
        assertNotNull(l);
        assertFalse(l.hasProperty(UcteConstants.ELEMENT_NAME_PROPERTY_KEY));
    }

    @Test
    void combineRtcAndPtc() {
        ResourceDataSource dataSource = new ResourceDataSource("combineRtcAndPtc", new ResourceSet("/", "combineRtcAndPtc.uct"));
        Network network = new UcteImporter().importData(dataSource, new NetworkFactoryImpl(), null);
        assertEquals(1.948, network.getTwoWindingsTransformer("BBE2AA1  BBE3AA1  1").getPhaseTapChanger().getCurrentStep().getAlpha(), 0.001);
        assertEquals(1.0, network.getTwoWindingsTransformer("BBE2AA1  BBE3AA1  1").getPhaseTapChanger().getCurrentStep().getRho(), 0.0000001);

        Properties parameters = new Properties();
        parameters.put("ucte.import.combine-phase-angle-regulation", "true");
        Network network2 = new UcteImporter().importData(dataSource, new NetworkFactoryImpl(), parameters);
        assertEquals(1.92419, network2.getTwoWindingsTransformer("BBE2AA1  BBE3AA1  1").getPhaseTapChanger().getCurrentStep().getAlpha(), 0.001);
        assertEquals(1.00000694, network2.getTwoWindingsTransformer("BBE2AA1  BBE3AA1  1").getPhaseTapChanger().getCurrentStep().getRho(), 0.0000001); // FIXME, symmetrical no impact
    }
}

