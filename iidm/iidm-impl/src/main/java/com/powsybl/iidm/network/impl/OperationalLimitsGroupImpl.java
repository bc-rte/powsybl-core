/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Pauline Jean-Marie {@literal <pauline.jean-marie at artelys.com>}
 */
class OperationalLimitsGroupImpl implements OperationalLimitsGroup, Validable {

    private final String id;
    private CurrentLimits currentLimits;
    private ActivePowerLimits activePowerLimits;
    private ApparentPowerLimits apparentPowerLimits;
    private final AbstractIdentifiable<?> identifiable;
    private final String attributeName;
    private final Ref<String> defaultGroupId;

    OperationalLimitsGroupImpl(String id, AbstractIdentifiable<?> identifiable, String attributeName, Ref<String> defaultGroupId) {
        this.id = Objects.requireNonNull(id);
        this.identifiable = Objects.requireNonNull(identifiable);
        this.attributeName = Objects.requireNonNull(attributeName);
        this.defaultGroupId = defaultGroupId;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits() {
        return Optional.ofNullable(currentLimits);
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits() {
        return Optional.ofNullable(activePowerLimits);
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits() {
        return Optional.ofNullable(apparentPowerLimits);
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        return new CurrentLimitsAdderImpl(this, identifiable, identifiable.getId());
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits() {
        return new ActivePowerLimitsAdderImpl(this, identifiable, identifiable.getId());
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits() {
        return new ApparentPowerLimitsAdderImpl(this, identifiable, identifiable.getId());
    }

    @Override
    public void removeCurrentLimits() {
        setCurrentLimits(null);
    }

    @Override
    public void removeActivePowerLimits() {
        setActivePowerLimits(null);
    }

    @Override
    public void removeApparentPowerLimits() {
        setApparentPowerLimits(null);
    }

    public void setCurrentLimits(CurrentLimits limits) {
        OperationalLimits oldValue = this.currentLimits;
        this.currentLimits = limits;
        notifyUpdateIfDefaultLimits(getId(), LimitType.CURRENT, oldValue, limits);
    }

    public void setActivePowerLimits(ActivePowerLimits limits) {
        OperationalLimits oldValue = this.activePowerLimits;
        this.activePowerLimits = limits;
        notifyUpdateIfDefaultLimits(getId(), LimitType.ACTIVE_POWER, oldValue, limits);
    }

    public void setApparentPowerLimits(ApparentPowerLimits limits) {
        OperationalLimits oldValue = this.apparentPowerLimits;
        this.apparentPowerLimits = limits;
        notifyUpdateIfDefaultLimits(getId(), LimitType.APPARENT_POWER, oldValue, limits);
    }

    public Validable getValidable() {
        return identifiable;
    }

    public void notifyUpdateIfDefaultLimits(LimitType limitType, String attribute, double oldValue, double newValue) {
        notifyUpdateIfDefaultLimits(getId(), limitType, attribute, oldValue, newValue);
    }

    public void notifyUpdateIfDefaultLimits(String id, LimitType limitType, String attribute, double oldValue, double newValue) {
        if (id.equals(defaultGroupId.get())) {
            identifiable.getNetwork().getListeners().notifyUpdate(identifiable, attributeName + "_" + limitType + "." + attribute, oldValue, newValue);
        }
    }

    public void notifyUpdateIfDefaultLimits(String id, LimitType limitType, OperationalLimits oldValue, OperationalLimits newValue) {
        if (id.equals(defaultGroupId.get())) {
            if (newValue == null) {
                Objects.requireNonNull(oldValue);

            }
            identifiable.getNetwork().getListeners().notifyUpdate(identifiable, attributeName + "_" + limitType, oldValue, newValue);
        }
    }

    @Override
    public String getMessageHeader() {
        return identifiable.getMessageHeader();
    }

    @Override
    public boolean isEmpty() {
        return currentLimits == null && apparentPowerLimits == null && activePowerLimits == null;
    }

}
