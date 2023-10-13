/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.extensions.AbstractExtendable;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Parameters for security analysis computation.
 * Extensions may be added, for instance for implementation-specific parameters.
 *
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 * @author Sylvain LECLERC <sylvain.leclerc at rte-france.com>
 * @author Laurent Issertial <laurent.issertial at rte-france.com>
 */
public abstract class AbstractSecurityAnalysisParameters<T extends AbstractSecurityAnalysisParameters<T>> extends AbstractExtendable<T> {

    public static class IncreasedViolationsParameters {

        static final double DEFAULT_FLOW_PROPORTIONAL_THRESHOLD = 0.1; // meaning 10.0 %
        static final double DEFAULT_LOW_VOLTAGE_PROPORTIONAL_THRESHOLD = 0.0; // meaning 0.0 %
        static final double DEFAULT_HIGH_VOLTAGE_PROPORTIONAL_THRESHOLD = 0.0; // meaning 0.0 %
        static final double DEFAULT_LOW_VOLTAGE_ABSOLUTE_THRESHOLD = 0.0; // 0.0 kV
        static final double DEFAULT_HIGH_VOLTAGE_ABSOLUTE_THRESHOLD = 0.0; // 0.0 kV

        @JsonProperty("flow-proportional-threshold")
        private double flowProportionalThreshold = DEFAULT_FLOW_PROPORTIONAL_THRESHOLD;
        @JsonProperty("low-voltage-proportional-threshold")
        private double lowVoltageProportionalThreshold = DEFAULT_LOW_VOLTAGE_PROPORTIONAL_THRESHOLD;
        @JsonProperty("low-voltage-absolute-threshold")
        private double lowVoltageAbsoluteThreshold = DEFAULT_LOW_VOLTAGE_ABSOLUTE_THRESHOLD;
        @JsonProperty("high-voltage-proportional-threshold")
        private double highVoltageProportionalThreshold = DEFAULT_HIGH_VOLTAGE_PROPORTIONAL_THRESHOLD;
        @JsonProperty("high-voltage-absolute-threshold")
        private double highVoltageAbsoluteThreshold = DEFAULT_HIGH_VOLTAGE_ABSOLUTE_THRESHOLD;

        public IncreasedViolationsParameters(double lowVoltageAbsoluteThreshold, double lowVoltageProportionalThreshold,
                                             double highVoltageAbsoluteThreshold, double highVoltageProportionalThreshold,
                                             double flowProportionalThreshold) {
            this.lowVoltageAbsoluteThreshold = lowVoltageAbsoluteThreshold;
            this.lowVoltageProportionalThreshold = lowVoltageProportionalThreshold;
            this.highVoltageAbsoluteThreshold = highVoltageAbsoluteThreshold;
            this.highVoltageProportionalThreshold = highVoltageProportionalThreshold;
            this.flowProportionalThreshold = flowProportionalThreshold;
        }

        public IncreasedViolationsParameters() {
        }

        /**
         * After a contingency, only low voltage violations that are increased of more than the absolute threshold (in kV) compared to the pre-contingency state,
         * are listed in the limit violations, the other ones are filtered. This method gets the low voltage violations absolute threshold (in kV, should be positive).
         * The default value is 0.0, meaning that only violations that increase of more than 0.0 kV are listed in the limit violations (note that for low voltage violation,
         * it means that the voltage in post-contingency state is lower than the voltage in pre-contingency state).
         */
        public double getLowVoltageAbsoluteThreshold() {
            return lowVoltageAbsoluteThreshold;
        }

        /**
         * After a contingency, only low voltage violations that are increased of more than the proportional threshold (without unit) compared to the pre-contingency state,
         * are listed in the limit violations, the other ones are filtered. This method gets the low voltage violations proportional threshold (without unit, should be positive).
         * The default value is 0.0, meaning that only violations that increase of more than 0.0 % are listed in the limit violations (note that for low voltage violation,
         * it means that the voltage in post-contingency state is lower than the voltage in pre-contingency state).
         */
        public double getLowVoltageProportionalThreshold() {
            return lowVoltageProportionalThreshold;
        }

        /**
         * After a contingency, only high voltage violations that are increased of more than the absolute threshold (in kV) compared to the pre-contingency state,
         * are listed in the limit violations, the other ones are filtered. This method gets the high voltage violations absolute threshold (in kV, should be positive).
         * The default value is 0.0, meaning that only violations that increase of more than 0.0 kV are listed in the limit violations.
         */
        public double getHighVoltageAbsoluteThreshold() {
            return highVoltageAbsoluteThreshold;
        }

        /**
         * After a contingency, only high voltage violations that are increased of more than the proportional threshold (without unit) compared to the pre-contingency state,
         * are listed in the limit violations, the other ones are filtered. This method gets the high voltage violations proportional threshold (without unit, should be positive).
         * The default value is 0.0, meaning that only violations that increase of more than 0.0 % are listed in the limit violations.
         */
        public double getHighVoltageProportionalThreshold() {
            return highVoltageProportionalThreshold;
        }

        /**
         * After a contingency, only flow violations (either current, active power or apparent power violations) that are increased in proportion,
         * compared to the pre-contingency state, than the threshold value (without unit, should be positive) are listed in the limit violations,
         * the other ones are filtered. This method gets the flow violations proportional threshold. The default value is 0.1, meaning that only violations that
         * increase of more than 10% are listed in the limit violations.
         */
        public double getFlowProportionalThreshold() {
            return flowProportionalThreshold;
        }

        public IncreasedViolationsParameters setLowVoltageAbsoluteThreshold(double lowVoltageAbsoluteThreshold) {
            this.lowVoltageAbsoluteThreshold = lowVoltageAbsoluteThreshold;
            return this;
        }

        public IncreasedViolationsParameters setLowVoltageProportionalThreshold(double lowVoltageProportionalThreshold) {
            this.lowVoltageProportionalThreshold = lowVoltageProportionalThreshold;
            return this;
        }

        public IncreasedViolationsParameters setHighVoltageAbsoluteThreshold(double highVoltageAbsoluteThreshold) {
            this.highVoltageAbsoluteThreshold = highVoltageAbsoluteThreshold;
            return this;
        }

        public IncreasedViolationsParameters setHighVoltageProportionalThreshold(double highVoltageProportionalThreshold) {
            this.highVoltageProportionalThreshold = highVoltageProportionalThreshold;
            return this;
        }

        public IncreasedViolationsParameters setFlowProportionalThreshold(double flowProportionalThreshold) {
            this.flowProportionalThreshold = flowProportionalThreshold;
            return this;
        }

        public void load(ModuleConfig config) {
            setFlowProportionalThreshold(config.getDoubleProperty("increased-flow-violations-proportional-threshold", DEFAULT_FLOW_PROPORTIONAL_THRESHOLD))
                    .setLowVoltageProportionalThreshold(config.getDoubleProperty("increased-low-voltage-violations-proportional-threshold", DEFAULT_LOW_VOLTAGE_PROPORTIONAL_THRESHOLD))
                    .setHighVoltageProportionalThreshold(config.getDoubleProperty("increased-high-voltage-violations-proportional-threshold", DEFAULT_HIGH_VOLTAGE_PROPORTIONAL_THRESHOLD))
                    .setLowVoltageAbsoluteThreshold(config.getDoubleProperty("increased-low-voltage-violations-absolute-threshold", DEFAULT_LOW_VOLTAGE_ABSOLUTE_THRESHOLD))
                    .setHighVoltageAbsoluteThreshold(config.getDoubleProperty("increased-high-voltage-violations-absolute-threshold", DEFAULT_HIGH_VOLTAGE_ABSOLUTE_THRESHOLD));
        }
    }

    protected IncreasedViolationsParameters increasedViolationsParameters = new IncreasedViolationsParameters();

    public IncreasedViolationsParameters getIncreasedViolationsParameters() {
        return increasedViolationsParameters;
    }

    public T setIncreasedViolationsParameters(IncreasedViolationsParameters increasedViolationsParameters) {
        this.increasedViolationsParameters = Objects.requireNonNull(increasedViolationsParameters);
        return self();
    }

    protected abstract T self();

    public abstract void write(Path parametersPath);

    public abstract void update(Path parametersPath);
}