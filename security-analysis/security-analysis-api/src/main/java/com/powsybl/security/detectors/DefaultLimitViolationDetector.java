/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.detectors;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Implements the default behaviour for limit violation detection.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class DefaultLimitViolationDetector extends AbstractContingencyBlindDetector {

    private final float limitReduction;
    private final Set<LoadingLimitType> currentLimitTypes;

    public DefaultLimitViolationDetector(float limitReduction, Collection<LoadingLimitType> currentLimitTypes) {
        if (limitReduction <= 0) {
            throw new IllegalArgumentException("Bad limit reduction " + limitReduction);
        }
        this.limitReduction = limitReduction;
        this.currentLimitTypes = EnumSet.copyOf(Objects.requireNonNull(currentLimitTypes));
    }

    public DefaultLimitViolationDetector(Collection<LoadingLimitType> currentLimitTypes) {
        this(1.0f, currentLimitTypes);
    }

    public DefaultLimitViolationDetector() {
        this(EnumSet.allOf(LoadingLimitType.class));
    }

    @Override
    public void checkCurrent(Branch branch, Branch.Side side, double value, Consumer<LimitViolation> consumer) {

        checkLimitViolation(branch, side, value, consumer, LimitType.CURRENT);
    }

    @Override
    public void checkCurrent(TieLine tieLine, Branch.Side side, double value, Consumer<LimitViolation> consumer) {

        checkLimitViolation(tieLine, side, value, consumer, LimitType.CURRENT);
    }

    @Override
    public void checkActivePower(Branch branch, Branch.Side side, double value, Consumer<LimitViolation> consumer) {

        checkLimitViolation(branch, side, value, consumer, LimitType.ACTIVE_POWER);
    }

    @Override
    public void checkActivePower(TieLine tieLine, Branch.Side side, double value, Consumer<LimitViolation> consumer) {

        checkLimitViolation(tieLine, side, value, consumer, LimitType.ACTIVE_POWER);
    }

    @Override
    public void checkApparentPower(Branch branch, Branch.Side side, double value, Consumer<LimitViolation> consumer) {

        checkLimitViolation(branch, side, value, consumer, LimitType.APPARENT_POWER);
    }

    @Override
    public void checkApparentPower(TieLine tieLine, Branch.Side side, double value, Consumer<LimitViolation> consumer) {

        checkLimitViolation(tieLine, side, value, consumer, LimitType.APPARENT_POWER);
    }

    @Override
    public void checkVoltage(Bus bus, double value, Consumer<LimitViolation> consumer) {
        VoltageLevel vl = bus.getVoltageLevel();
        if (!Double.isNaN(vl.getLowVoltageLimit()) && value <= vl.getLowVoltageLimit()) {
            consumer.accept(new LimitViolation(vl.getId(), vl.getOptionalName().orElse(null), LimitViolationType.LOW_VOLTAGE,
                    vl.getLowVoltageLimit(), limitReduction, value));
        }

        if (!Double.isNaN(vl.getHighVoltageLimit()) && value >= vl.getHighVoltageLimit()) {
            consumer.accept(new LimitViolation(vl.getId(), vl.getOptionalName().orElse(null), LimitViolationType.HIGH_VOLTAGE,
                    vl.getHighVoltageLimit(), limitReduction, value));
        }
    }

    public void checkLimitViolation(Branch branch, Branch.Side side, double value, Consumer<LimitViolation> consumer, LimitType type) {
        Branch.Overload overload = LimitViolationUtils.checkTemporaryLimits(branch, side, limitReduction, value, type);
        if (currentLimitTypes.contains(LoadingLimitType.TATL) && (overload != null)) {
            checkTemporary(branch, side, limitReduction, value, consumer, type);
        } else if (currentLimitTypes.contains(LoadingLimitType.PATL)) {
            checkPermanentLimit(branch, side, limitReduction, value, consumer, type);
        }
    }

    public void checkLimitViolation(TieLine tieLine, Branch.Side side, double value, Consumer<LimitViolation> consumer, LimitType type) {
        Branch.Overload overload = LimitViolationUtils.checkTemporaryLimits(tieLine, side, limitReduction, value, type);
        if (currentLimitTypes.contains(LoadingLimitType.TATL) && (overload != null)) {
            checkTemporary(tieLine, side, limitReduction, value, consumer, type);
        } else if (currentLimitTypes.contains(LoadingLimitType.PATL)) {
            checkPermanentLimit(tieLine, side, limitReduction, value, consumer, type);
        }
    }
}
