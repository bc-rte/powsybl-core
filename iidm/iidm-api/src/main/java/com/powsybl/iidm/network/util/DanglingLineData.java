/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.TwoSides;
import org.apache.commons.math3.complex.Complex;

import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.util.LinkData.Flow;

import java.util.Objects;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class DanglingLineData {

    private static final String PROPERTY_V = "v";
    private static final String PROPERTY_ANGLE = "angle";

    private final DanglingLine danglingLine;

    private double networkFlowP;
    private double networkFlowQ;
    private double boundaryBusU;
    private double boundaryBusTheta;
    private double boundaryFlowP;
    private double boundaryFlowQ;

    public DanglingLineData(DanglingLine danglingLine) {
        this(danglingLine, true);
    }

    public DanglingLineData(DanglingLine danglingLine, boolean splitShuntAdmittance) {
        this.danglingLine = Objects.requireNonNull(danglingLine);

        if (isZ0(danglingLine)) {
            dlDataZ0(danglingLine);
        } else {
            dlData(danglingLine, splitShuntAdmittance);
        }
    }

    private void dlDataZ0(DanglingLine danglingLine) {
        double networkU = getV(danglingLine);
        double networkTheta = getTheta(danglingLine);
        Complex networkFlow = new Complex(danglingLine.getTerminal().getP(), danglingLine.getTerminal().getQ());

        boundaryBusU = networkU;
        boundaryBusTheta = networkTheta;
        boundaryFlowP = -networkFlow.getReal();
        boundaryFlowQ = -networkFlow.getImaginary();
        networkFlowP = networkFlow.getReal();
        networkFlowQ = networkFlow.getImaginary();
    }

    private void dlData(DanglingLine danglingLine, boolean splitShuntAdmittance) {
        double networkU = getV(danglingLine);
        double networkTheta = getTheta(danglingLine);

        Complex networkFlow = new Complex(danglingLine.getTerminal().getP(), danglingLine.getTerminal().getQ());

        // boundary voltage from properties, paired and not paired danglingLines are considered
        double boundaryV = getDoubleProperty(danglingLine, PROPERTY_V);
        double boundaryTheta = Math.toRadians(getDoubleProperty(danglingLine, PROPERTY_ANGLE));

        if (isVoltageValid(networkU, networkTheta) && isVoltageValid(boundaryV, boundaryTheta)) {
            dlDataWhenThereAreVoltagesAtBothSides(networkU, networkTheta, boundaryV, boundaryTheta, networkFlow, splitShuntAdmittance);
            return;
        }

        // Voltage and flow at the network side are available
        if (isVoltageValid(networkU, networkTheta) && isFlowValid(networkFlow)) {
            dlDataWhenThereAreVoltageAndFlowAtNetworkSide(networkU, networkTheta, networkFlow, splitShuntAdmittance);
            return;
        }

        // DcApproximation
        if (isDcVoltageValid(networkTheta) && isDcVoltageValid(boundaryTheta)) {
            dcApproximationDlDataWhenThereAreVoltagesAtBothSides(networkTheta, boundaryTheta, networkFlow);
            return;
        }

        if (isDcVoltageValid(networkTheta) && isDcFlowValid(networkFlow.getReal())) {
            dcApproximationDlDataWhenThereAreVoltageAndFlowAtNetworkSide(networkTheta, networkFlow, splitShuntAdmittance);
            return;
        }

        dlDataNotSupportedCases(networkFlow);
    }

    private void dlDataWhenThereAreVoltagesAtBothSides(double networkU, double networkTheta, double vBoundary,
        double tethaBoundary, Complex networkFlow, boolean splitShuntAdmittance) {

        double g1 = splitShuntAdmittance ? danglingLine.getG() * 0.5 : danglingLine.getG();
        double b1 = splitShuntAdmittance ? danglingLine.getB() * 0.5 : danglingLine.getB();
        double g2 = splitShuntAdmittance ? danglingLine.getG() * 0.5 : 0.0;
        double b2 = splitShuntAdmittance ? danglingLine.getB() * 0.5 : 0.0;

        LinkData.BranchAdmittanceMatrix adm = LinkData.calculateBranchAdmittance(danglingLine.getR(),
            danglingLine.getX(), 1.0, 0.0, 1.0, 0.0, new Complex(g1, b1), new Complex(g2, b2));
        Flow flow = LinkData.flowBothEnds(adm.y11(), adm.y12(), adm.y21(), adm.y22(), networkU, networkTheta, vBoundary,
            tethaBoundary);

        boundaryBusU = vBoundary;
        boundaryBusTheta = tethaBoundary;
        boundaryFlowP = flow.getToFrom().getReal();
        boundaryFlowQ = flow.getToFrom().getImaginary();
        networkFlowP = Double.isNaN(networkFlow.getReal()) ? flow.getFromTo().getReal() : networkFlow.getReal();
        networkFlowQ = Double.isNaN(networkFlow.getImaginary()) ? flow.getFromTo().getImaginary() : networkFlow.getImaginary();
    }

    private void dlDataWhenThereAreVoltageAndFlowAtNetworkSide(double networkU, double networkTheta,
        Complex networkFlow, boolean splitShuntAdmittance) {

        SV sv = new SV(networkFlow.getReal(), networkFlow.getImaginary(), networkU, Math.toDegrees(networkTheta), TwoSides.ONE)
            .otherSide(danglingLine, splitShuntAdmittance);

        boundaryBusU = sv.getU();
        boundaryBusTheta = Math.toRadians(sv.getA());
        boundaryFlowP = sv.getP();
        boundaryFlowQ = sv.getQ();
        networkFlowP = networkFlow.getReal();
        networkFlowQ = networkFlow.getImaginary();
    }

    private void dcApproximationDlDataWhenThereAreVoltagesAtBothSides(double networkTheta, double boundaryTheta,
        Complex networkFlow) {

        double xpu = danglingLine.getX() / (danglingLine.getTerminal().getVoltageLevel().getNominalV()
            * danglingLine.getTerminal().getVoltageLevel().getNominalV());
        double b = xpu / danglingLine.getX();

        double boundaryflow = b * (boundaryTheta - networkTheta);
        boundaryBusU = Double.NaN;
        boundaryBusTheta = boundaryTheta;
        boundaryFlowP = boundaryflow;
        boundaryFlowQ = Double.NaN;
        networkFlowP = Double.isNaN(networkFlow.getReal()) ? -boundaryflow : networkFlow.getReal();
        networkFlowQ = networkFlow.getImaginary();
    }

    private void dcApproximationDlDataWhenThereAreVoltageAndFlowAtNetworkSide(double networkTheta, Complex networkFlow,
        boolean splitShuntAdmittance) {

        SV sv = new SV(networkFlow.getReal(), Double.NaN, Double.NaN, Math.toDegrees(networkTheta), TwoSides.ONE)
            .otherSide(danglingLine, splitShuntAdmittance);

        boundaryBusU = sv.getU();
        boundaryBusTheta = Math.toRadians(sv.getA());
        boundaryFlowP = sv.getP();
        boundaryFlowQ = sv.getQ();
        networkFlowP = networkFlow.getReal();
        networkFlowQ = networkFlow.getImaginary();
    }

    // it is possible to calculate the boundary voltage by knowing the network side voltage
    // and the boundary injection (with and without voltage control), nevertheless
    // these configurations are not considered in the current version
    private void dlDataNotSupportedCases(Complex networkFlow) {
        boundaryBusU = Double.NaN;
        boundaryBusTheta = Double.NaN;
        boundaryFlowP = Double.NaN;
        boundaryFlowQ = Double.NaN;
        networkFlowP = networkFlow.getReal();
        networkFlowQ = networkFlow.getImaginary();
    }

    private static double getDoubleProperty(DanglingLine danglingLine, String name) {
        Objects.requireNonNull(danglingLine);
        String value = danglingLine.getProperty(name);
        return value != null ? Double.parseDouble(value) : Double.NaN;
    }

    private static double getV(DanglingLine danglingLine) {
        return danglingLine.getTerminal().isConnected() ? danglingLine.getTerminal().getBusView().getBus().getV()
            : Double.NaN;
    }

    private static double getTheta(DanglingLine danglingLine) {
        return danglingLine.getTerminal().isConnected()
            ? Math.toRadians(danglingLine.getTerminal().getBusView().getBus().getAngle())
            : Double.NaN;
    }

    private static boolean isVoltageValid(double v, double theta) {
        return !Double.isNaN(v) && v > 0.0 && !Double.isNaN(theta);
    }

    private static boolean isFlowValid(Complex flow) {
        return !Double.isNaN(flow.getReal()) && !Double.isNaN(flow.getImaginary());
    }

    private static boolean isDcVoltageValid(double theta) {
        return !Double.isNaN(theta);
    }

    private static boolean isDcFlowValid(double flow) {
        return !Double.isNaN(flow);
    }

    public static boolean isZ0(DanglingLine dl) {
        return dl.getR() == 0.0 && dl.getX() == 0.0 && dl.getG() == 0.0 && dl.getB() == 0.0;
    }

    public String getId() {
        return danglingLine.getId();
    }

    public double getBoundaryBusU() {
        return boundaryBusU;
    }

    public double getBoundaryBusTheta() {
        return boundaryBusTheta;
    }

    public double getBoundaryFlowP() {
        return boundaryFlowP;
    }

    public double getBoundaryFlowQ() {
        return boundaryFlowQ;
    }

    public double getNetworkFlowP() {
        return networkFlowP;
    }

    public double getNetworkFlowQ() {
        return networkFlowQ;
    }
}
