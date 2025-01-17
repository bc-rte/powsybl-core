/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.ast;

import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class BinaryMinCalc extends AbstractBinaryMinMax {

    static final String NAME = "binaryMin";

    public BinaryMinCalc(NodeCalc left, NodeCalc right) {
        super(left, right);
    }

    @Override
    public <R, A> R accept(NodeCalcVisitor<R, A> visitor, A arg, R leftValue, R rightValue) {
        return visitor.visit(this, arg, leftValue, rightValue);
    }

    @Override
    public <R, A> R acceptHandle(NodeCalcVisitor<R, A> visitor, A arg, R leftResult, R rightResult) {
        return visitor.visit(this, arg, leftResult, rightResult);
    }

    @Override
    protected String getJsonName() {
        return NAME;
    }

    static NodeCalc parseJson(JsonParser parser) throws IOException {
        ParsingContext context = parseJson2(parser);
        return new BinaryMinCalc(context.left, context.right);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BinaryMinCalc binaryMinCalc) {
            return binaryMinCalc.left.equals(left) && binaryMinCalc.right.equals(right);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return left.hashCode() + right.hashCode();
    }
}
