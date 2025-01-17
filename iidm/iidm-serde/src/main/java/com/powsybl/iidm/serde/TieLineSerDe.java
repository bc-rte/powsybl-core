/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.OptionalDouble;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class TieLineSerDe extends AbstractSimpleIdentifiableSerDe<TieLine, TieLineAdder, Network> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TieLineSerDe.class);

    static final TieLineSerDe INSTANCE = new TieLineSerDe();

    static final String ROOT_ELEMENT_NAME = "tieLine";
    static final String ARRAY_ELEMENT_NAME = "tieLines";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    private static void writeDanglingLine(DanglingLine danglingLine, NetworkSerializerContext context, int side) {
        Boundary boundary = danglingLine.getBoundary();
        context.getWriter().writeStringAttribute("id_" + side, context.getAnonymizer().anonymizeString(danglingLine.getId()));
        context.getWriter().writeStringAttribute("name_" + side, danglingLine.getOptionalName().map(n -> context.getAnonymizer().anonymizeString(n)).orElse(null));
        context.getWriter().writeDoubleAttribute("r_" + side, danglingLine.getR());
        context.getWriter().writeDoubleAttribute("x_" + side, danglingLine.getX());
        // TODO change serialization
        context.getWriter().writeDoubleAttribute("g1_" + side, danglingLine.getG() / 2);
        context.getWriter().writeDoubleAttribute("b1_" + side, danglingLine.getB() / 2);
        context.getWriter().writeDoubleAttribute("g2_" + side, danglingLine.getG() / 2);
        context.getWriter().writeDoubleAttribute("b2_" + side, danglingLine.getB() / 2);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_4, context, () -> {
            context.getWriter().writeDoubleAttribute("xnodeP_" + side, boundary.getP());
            context.getWriter().writeDoubleAttribute("xnodeQ_" + side, boundary.getQ());
        });

        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> context.getWriter().writeBooleanAttribute("fictitious_" + side, danglingLine.isFictitious(), false));
    }

    @Override
    protected void writeRootElementAttributes(TieLine tl, Network n, NetworkSerializerContext context) {
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_10, context, () -> {
            context.getWriter().writeStringAttribute("danglingLineId1", context.getAnonymizer().anonymizeString(tl.getDanglingLine1().getId()));
            context.getWriter().writeStringAttribute("danglingLineId2", context.getAnonymizer().anonymizeString(tl.getDanglingLine2().getId()));
        });
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_9, context, () -> {
            if (tl.getPairingKey() != null) {
                context.getWriter().writeStringAttribute("ucteXnodeCode", tl.getPairingKey());
            }
            writeNodeOrBus(1, tl.getDanglingLine1().getTerminal(), context);
            writeNodeOrBus(2, tl.getDanglingLine2().getTerminal(), context);
            writeOptionalPQ(1, tl.getDanglingLine1().getTerminal(), context.getWriter(), context.getOptions()::isWithBranchSV);
            writeOptionalPQ(2, tl.getDanglingLine2().getTerminal(), context.getWriter(), context.getOptions()::isWithBranchSV);
            writeDanglingLine(tl.getDanglingLine1(), context, 1);
            writeDanglingLine(tl.getDanglingLine2(), context, 2);
        });
    }

    @Override
    protected void writeSubElements(TieLine tl, Network n, NetworkSerializerContext context) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_9, context, () -> {
            Optional<ActivePowerLimits> activePowerLimits1 = tl.getDanglingLine1().getActivePowerLimits();
            if (activePowerLimits1.isPresent()) {
                IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmVersion.V_1_5, context);
                IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> writeActivePowerLimits(1, activePowerLimits1.get(), context.getWriter(),
                        context.getVersion(), context.isValid(), context.getOptions()));
            }
            Optional<ApparentPowerLimits> apparentPowerLimits1 = tl.getDanglingLine1().getApparentPowerLimits();
            if (apparentPowerLimits1.isPresent()) {
                IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmVersion.V_1_5, context);
                IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> writeApparentPowerLimits(1, apparentPowerLimits1.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
            }
            Optional<CurrentLimits> currentLimits1 = tl.getDanglingLine1().getCurrentLimits();
            if (currentLimits1.isPresent()) {
                writeCurrentLimits(1, currentLimits1.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions());
            }
            Optional<ActivePowerLimits> activePowerLimits2 = tl.getDanglingLine2().getActivePowerLimits();
            if (activePowerLimits2.isPresent()) {
                IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_2, IidmSerDeUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmVersion.V_1_5, context);
                IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> writeActivePowerLimits(2, activePowerLimits2.get(), context.getWriter(),
                        context.getVersion(), context.isValid(), context.getOptions()));
            }
            Optional<ApparentPowerLimits> apparentPowerLimits2 = tl.getDanglingLine2().getApparentPowerLimits();
            if (apparentPowerLimits2.isPresent()) {
                IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_2, IidmSerDeUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmVersion.V_1_5, context);
                IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> writeApparentPowerLimits(2, apparentPowerLimits2.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
            }
            Optional<CurrentLimits> currentLimits2 = tl.getDanglingLine2().getCurrentLimits();
            if (currentLimits2.isPresent()) {
                writeCurrentLimits(2, currentLimits2.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions());
            }
        });
    }

    @Override
    protected TieLineAdder createAdder(Network n) {
        return n.newTieLine();
    }

    private static DanglingLine readDanglingLine(DanglingLineAdder adder, String pairingKey, NetworkDeserializerContext context, int side) {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("id_" + side));
        String name = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("name_" + side));
        double r = context.getReader().readDoubleAttribute("r_" + side);
        double x = context.getReader().readDoubleAttribute("x_" + side);
        double g1 = context.getReader().readDoubleAttribute("g1_" + side);
        double b1 = context.getReader().readDoubleAttribute("b1_" + side);
        double g2 = context.getReader().readDoubleAttribute("g2_" + side);
        double b2 = context.getReader().readDoubleAttribute("b2_" + side);
        adder.setId(id)
                .setName(name)
                .setPairingKey(pairingKey)
                .setR(r)
                .setX(x)
                .setG(g1 + g2)
                .setB(b1 + b2)
                .setP0(0.0)
                .setQ0(0.0);

        double[] halfBoundaryP = new double[1];
        double[] halfBoundaryQ = new double[1];
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_4, context, () -> {
            halfBoundaryP[0] = context.getReader().readDoubleAttribute("xnodeP_" + side);
            halfBoundaryQ[0] = context.getReader().readDoubleAttribute("xnodeQ_" + side);
        });

        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> {
            boolean fictitious = context.getReader().readBooleanAttribute("fictitious_" + side, false);
            adder.setFictitious(fictitious);
        });

        DanglingLine dl = adder.add();

        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_4, context, () -> {
            checkBoundaryValue(halfBoundaryP[0], dl.getBoundary().getP(), "xnodeP_" + side, pairingKey);
            checkBoundaryValue(halfBoundaryQ[0], dl.getBoundary().getQ(), "xnodeQ_" + side, pairingKey);
        });

        return dl;
    }

    @Override
    protected TieLine readRootElementAttributes(TieLineAdder adder, Network network, NetworkDeserializerContext context) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_9, context, () -> {
            String pairingKey = context.getReader().readStringAttribute("ucteXnodeCode");
            DanglingLineAdder adderDl1 = readVlAndNodeOrBus(context, network, 1);
            DanglingLineAdder adderDl2 = readVlAndNodeOrBus(context, network, 2);
            OptionalDouble p1 = context.getReader().readOptionalDoubleAttribute("p1");
            OptionalDouble q1 = context.getReader().readOptionalDoubleAttribute("q1");
            OptionalDouble p2 = context.getReader().readOptionalDoubleAttribute("p2");
            OptionalDouble q2 = context.getReader().readOptionalDoubleAttribute("q2");
            DanglingLine dl1 = readDanglingLine(adderDl1, pairingKey, context, 1);
            DanglingLine dl2 = readDanglingLine(adderDl2, pairingKey, context, 2);
            p1.ifPresent(dl1.getTerminal()::setP);
            q1.ifPresent(dl1.getTerminal()::setQ);
            p2.ifPresent(dl2.getTerminal()::setP);
            q2.ifPresent(dl2.getTerminal()::setQ);
            adder.setDanglingLine1(dl1.getId()).setDanglingLine2(dl2.getId());
        });
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_10, context, () -> {
            String dl1Id = context.getReader().readStringAttribute("danglingLineId1");
            String dl2Id = context.getReader().readStringAttribute("danglingLineId2");
            adder.setDanglingLine1(dl1Id).setDanglingLine2(dl2Id);
        });
        return adder.add();
    }

    private static DanglingLineAdder readVlAndNodeOrBus(NetworkDeserializerContext context, Network network, int side) {
        String voltageLevelId = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("voltageLevelId" + side));
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        DanglingLineAdder adderDl1 = voltageLevel.newDanglingLine();
        readNodeOrBus(adderDl1, String.valueOf(side), context, voltageLevel.getTopologyKind());
        return adderDl1;
    }

    @Override
    protected void readSubElements(TieLine tl, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case ACTIVE_POWER_LIMITS_1 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.assertMaximumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_9, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readActivePowerLimits(tl.getDanglingLine1().newActivePowerLimits(), context.getReader(), context.getVersion(), context.getOptions()));
                }
                case APPARENT_POWER_LIMITS_1 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.assertMaximumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_9, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readApparentPowerLimits(tl.getDanglingLine1().newApparentPowerLimits(), context.getReader(), context.getVersion(), context.getOptions()));
                }
                case "currentLimits1" -> {
                    IidmSerDeUtil.assertMaximumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_9, context);
                    readCurrentLimits(tl.getDanglingLine1().newCurrentLimits(), context.getReader(), context.getVersion(), context.getOptions());
                }
                case ACTIVE_POWER_LIMITS_2 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_2, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.assertMaximumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_9, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readActivePowerLimits(tl.getDanglingLine2().newActivePowerLimits(), context.getReader(), context.getVersion(), context.getOptions()));
                }
                case APPARENT_POWER_LIMITS_2 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_2, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.assertMaximumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_9, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readApparentPowerLimits(tl.getDanglingLine2().newApparentPowerLimits(), context.getReader(), context.getVersion(), context.getOptions()));
                }
                case "currentLimits2" -> {
                    IidmSerDeUtil.assertMaximumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_9, context);
                    readCurrentLimits(tl.getDanglingLine2().newCurrentLimits(), context.getReader(), context.getVersion(), context.getOptions());
                }
                default -> readSubElement(elementName, tl, context);
            }
        });
    }

    private static void checkBoundaryValue(double imported, double calculated, String name, String ucteXnodeCode) {
        if (!Double.isNaN(imported) && imported != calculated) {
            LOGGER.info("{} of the TieLine with ucteXnodeCode {} is recalculated. Its imported value is not used (imported value = {}; calculated value = {})", name, ucteXnodeCode, imported, calculated);
        }
    }
}
