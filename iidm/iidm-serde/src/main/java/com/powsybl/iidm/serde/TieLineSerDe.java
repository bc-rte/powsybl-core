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
        danglingLine.getOptionalName().ifPresent(name -> context.getWriter().writeStringAttribute("name_" + side, context.getAnonymizer().anonymizeString(name)));
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
            if (context.getOptions().isWithBranchSV()) {
                writePQ(1, tl.getDanglingLine1().getTerminal(), context.getWriter());
                writePQ(2, tl.getDanglingLine2().getTerminal(), context.getWriter());
            }
            writeDanglingLine(tl.getDanglingLine1(), context, 1);
            writeDanglingLine(tl.getDanglingLine2(), context, 2);
        });
    }

    @Override
    protected void writeSubElements(TieLine tl, Network n, NetworkSerializerContext context) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_9, context, () -> {
            writeLimits(context, 1, ROOT_ELEMENT_NAME, tl.getActivePowerLimits1(), tl.getApparentPowerLimits1(), tl.getCurrentLimits1());
            writeLimits(context, 2, ROOT_ELEMENT_NAME, tl.getActivePowerLimits2(), tl.getApparentPowerLimits2(), tl.getCurrentLimits2());
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
            double p1 = context.getReader().readDoubleAttribute("p1");
            double q1 = context.getReader().readDoubleAttribute("q1");
            double p2 = context.getReader().readDoubleAttribute("p2");
            double q2 = context.getReader().readDoubleAttribute("q2");
            DanglingLine dl1 = readDanglingLine(adderDl1, pairingKey, context, 1);
            DanglingLine dl2 = readDanglingLine(adderDl2, pairingKey, context, 2);
            dl1.getTerminal().setP(p1).setQ(q1);
            dl2.getTerminal().setP(p2).setQ(q2);
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
        DanglingLineAdder adderDl1 = network.getVoltageLevel(voltageLevelId).newDanglingLine();
        readNodeOrBus(adderDl1, String.valueOf(side), context);
        return adderDl1;
    }

    @Override
    protected void readSubElements(TieLine tl, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case ACTIVE_POWER_LIMITS_1 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.assertMaximumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_9, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readActivePowerLimits(tl.getDanglingLine1().newActivePowerLimits(), context.getReader()));
                }
                case APPARENT_POWER_LIMITS_1 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.assertMaximumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_9, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readApparentPowerLimits(tl.getDanglingLine1().newApparentPowerLimits(), context.getReader()));
                }
                case "currentLimits1" -> {
                    IidmSerDeUtil.assertMaximumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_9, context);
                    readCurrentLimits(tl.getDanglingLine1().newCurrentLimits(), context.getReader());
                }
                case ACTIVE_POWER_LIMITS_2 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_2, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.assertMaximumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_9, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readActivePowerLimits(tl.getDanglingLine2().newActivePowerLimits(), context.getReader()));
                }
                case APPARENT_POWER_LIMITS_2 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_2, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.assertMaximumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_9, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readApparentPowerLimits(tl.getDanglingLine2().newApparentPowerLimits(), context.getReader()));
                }
                case "currentLimits2" -> {
                    IidmSerDeUtil.assertMaximumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_9, context);
                    readCurrentLimits(tl.getDanglingLine2().newCurrentLimits(), context.getReader());
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
