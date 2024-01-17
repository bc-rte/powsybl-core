/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Collection;
import java.util.Optional;

/**
 * An equipment with two terminals.
 *
 * <p>
 *  Characteristics
 * </p>
 *
 * <table style="border: 1px solid black; border-collapse: collapse">
 *     <thead>
 *         <tr>
 *             <th style="border: 1px solid black">Attribute</th>
 *             <th style="border: 1px solid black">Type</th>
 *             <th style="border: 1px solid black">Unit</th>
 *             <th style="border: 1px solid black">Required</th>
 *             <th style="border: 1px solid black">Defaut value</th>
 *             <th style="border: 1px solid black">Description</th>
 *         </tr>
 *     </thead>
 *     <tbody>
 *         <tr>
 *             <td style="border: 1px solid black">Id</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Unique identifier of the branch</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the branch</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">R</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">&Omega;</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The series resistance</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">X</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">&Omega;</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The series reactance</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">G1</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The first side shunt conductance</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">B1</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The first side shunt susceptance</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">G2</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The second side shunt conductance</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">B2</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The second side shunt susceptance</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface Branch<I extends Branch<I>> extends Identifiable<I> {

    /**
     * Get the first terminal.
     */
    Terminal getTerminal1();

    /**
     * Get the second terminal.
     */
    Terminal getTerminal2();

    Terminal getTerminal(TwoSides side);

    Terminal getTerminal(String voltageLevelId);

    TwoSides getSide(Terminal terminal);

    Collection<OperationalLimitsGroup> getOperationalLimitsGroups1();

    Optional<String> getDefaultOperationalLimitsGroupId1();

    Optional<OperationalLimitsGroup> getOperationalLimitsGroup1(String id);

    Optional<OperationalLimitsGroup> getDefaultOperationalLimitsGroup1();

    OperationalLimitsGroup newOperationalLimitsGroup1(String id);

    void setDefaultOperationalLimitsGroup1(String id);

    void removeOperationalLimitsGroup1(String id);

    void cancelDefaultOperationalLimitsGroup1();

    default Optional<CurrentLimits> getCurrentLimits1() {
        return getDefaultOperationalLimitsGroup1().flatMap(OperationalLimitsGroup::getCurrentLimits);
    }

    default CurrentLimits getNullableCurrentLimits1() {
        return getCurrentLimits1().orElse(null);
    }

    default Optional<ActivePowerLimits> getActivePowerLimits1() {
        return getDefaultOperationalLimitsGroup1().flatMap(OperationalLimitsGroup::getActivePowerLimits);
    }

    default ActivePowerLimits getNullableActivePowerLimits1() {
        return getActivePowerLimits1().orElse(null);
    }

    default Optional<ApparentPowerLimits> getApparentPowerLimits1() {
        return getDefaultOperationalLimitsGroup1().flatMap(OperationalLimitsGroup::getApparentPowerLimits);
    }

    default ApparentPowerLimits getNullableApparentPowerLimits1() {
        return getApparentPowerLimits1().orElse(null);
    }

    CurrentLimitsAdder newCurrentLimits1();

    ActivePowerLimitsAdder newActivePowerLimits1();

    ApparentPowerLimitsAdder newApparentPowerLimits1();

    Collection<OperationalLimitsGroup> getOperationalLimitsGroups2();

    Optional<String> getDefaultOperationalLimitsGroupId2();

    Optional<OperationalLimitsGroup> getOperationalLimitsGroup2(String id);

    Optional<OperationalLimitsGroup> getDefaultOperationalLimitsGroup2();

    OperationalLimitsGroup newOperationalLimitsGroup2(String id);

    void setDefaultOperationalLimitsGroup2(String id);

    void removeOperationalLimitsGroup2(String id);

    void cancelDefaultOperationalLimitsGroup2();

    default Optional<CurrentLimits> getCurrentLimits2() {
        return getDefaultOperationalLimitsGroup2().flatMap(OperationalLimitsGroup::getCurrentLimits);
    }

    default CurrentLimits getNullableCurrentLimits2() {
        return getCurrentLimits2().orElse(null);
    }

    default Optional<ActivePowerLimits> getActivePowerLimits2() {
        return getDefaultOperationalLimitsGroup2().flatMap(OperationalLimitsGroup::getActivePowerLimits);
    }

    default ActivePowerLimits getNullableActivePowerLimits2() {
        return getActivePowerLimits2().orElse(null);
    }

    default Optional<ApparentPowerLimits> getApparentPowerLimits2() {
        return getDefaultOperationalLimitsGroup2().flatMap(OperationalLimitsGroup::getApparentPowerLimits);
    }

    default ApparentPowerLimits getNullableApparentPowerLimits2() {
        return getApparentPowerLimits2().orElse(null);
    }

    CurrentLimitsAdder newCurrentLimits2();

    ActivePowerLimitsAdder newActivePowerLimits2();

    ApparentPowerLimitsAdder newApparentPowerLimits2();

    default Optional<CurrentLimits> getCurrentLimits(TwoSides side) {
        switch (side) {
            case ONE:
                return getCurrentLimits1();
            case TWO:
                return getCurrentLimits2();
            default:
                throw new UnsupportedOperationException(String.format("Side %s not supported", side.name()));
        }
    }

    default Optional<ActivePowerLimits> getActivePowerLimits(TwoSides side) {
        switch (side) {
            case ONE:
                return getActivePowerLimits1();
            case TWO:
                return getActivePowerLimits2();
            default:
                throw new UnsupportedOperationException(String.format("Side %s not supported", side.name()));
        }
    }

    default Optional<ApparentPowerLimits> getApparentPowerLimits(TwoSides side) {
        switch (side) {
            case ONE:
                return getApparentPowerLimits1();
            case TWO:
                return getApparentPowerLimits2();
            default:
                throw new UnsupportedOperationException(String.format("Side %s not supported", side.name()));
        }
    }

    default Optional<? extends LoadingLimits> getLimits(LimitType type, TwoSides side) {
        switch (type) {
            case CURRENT:
                return getCurrentLimits(side);
            case ACTIVE_POWER:
                return getActivePowerLimits(side);
            case APPARENT_POWER:
                return getApparentPowerLimits(side);
            default:
                throw new UnsupportedOperationException(String.format("Getting %s limits is not supported.", type.name()));
        }
    }

    default CurrentLimits getNullableCurrentLimits(TwoSides side) {
        switch (side) {
            case ONE:
                return getNullableCurrentLimits1();
            case TWO:
                return getNullableCurrentLimits2();
            default:
                throw new UnsupportedOperationException(String.format("Side %s not supported", side.name()));
        }
    }

    default ActivePowerLimits getNullableActivePowerLimits(TwoSides side) {
        switch (side) {
            case ONE:
                return getNullableActivePowerLimits1();
            case TWO:
                return getNullableActivePowerLimits2();
            default:
                throw new UnsupportedOperationException(String.format("Side %s not supported", side.name()));
        }
    }

    default ApparentPowerLimits getNullableApparentPowerLimits(TwoSides side) {
        switch (side) {
            case ONE:
                return getNullableApparentPowerLimits1();
            case TWO:
                return getNullableApparentPowerLimits2();
            default:
                throw new UnsupportedOperationException(String.format("Side %s not supported", side.name()));
        }
    }

    default LoadingLimits getNullableLimits(LimitType type, TwoSides side) {
        switch (type) {
            case CURRENT:
                return getNullableCurrentLimits(side);
            case ACTIVE_POWER:
                return getNullableActivePowerLimits(side);
            case APPARENT_POWER:
                return getNullableApparentPowerLimits(side);
            default:
                throw new UnsupportedOperationException(String.format("Getting %s limits is not supported.", type.name()));
        }
    }

    /**
     * Only checks overloading for LimitType.Current and permanent limits
     */
    boolean isOverloaded();

    /**
     * Only checks overloading for LimitType.Current and permanent limits
     */
    boolean isOverloaded(float limitReduction);

    int getOverloadDuration();

    boolean checkPermanentLimit(TwoSides side, float limitReduction, LimitType type);

    boolean checkPermanentLimit(TwoSides side, LimitType type);

    boolean checkPermanentLimit1(float limitReduction, LimitType type);

    boolean checkPermanentLimit1(LimitType type);

    boolean checkPermanentLimit2(float limitReduction, LimitType type);

    boolean checkPermanentLimit2(LimitType type);

    Overload checkTemporaryLimits(TwoSides side, float limitReduction, LimitType type);

    Overload checkTemporaryLimits(TwoSides side, LimitType type);

    Overload checkTemporaryLimits1(float limitReduction, LimitType type);

    Overload checkTemporaryLimits1(LimitType type);

    Overload checkTemporaryLimits2(float limitReduction, LimitType type);

    Overload checkTemporaryLimits2(LimitType type);
}
