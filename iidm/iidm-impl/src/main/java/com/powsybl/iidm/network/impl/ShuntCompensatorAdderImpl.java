/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ShuntCompensatorAdderImpl extends AbstractInjectionAdder<ShuntCompensatorAdderImpl> implements ShuntCompensatorAdder {

    private final VoltageLevelExt voltageLevel;

    private ShuntCompensatorModelBuilder modelBuilder;

    private int sectionCount = -1;

    private double targetV = Double.NaN;

    private double targetDeadband = Double.NaN;

    private TerminalExt regulatingTerminal;

    private boolean voltageRegulatorOn = false;

    private boolean useLocalRegulation = false;

    ShuntCompensatorAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
    }

    @Override
    protected String getTypeDescription() {
        return "Shunt compensator";
    }

    interface ShuntCompensatorModelBuilder {

        int getMaximumSectionCount();

        ShuntCompensatorModelExt build();

    }

    class ShuntCompensatorLinearModelAdderImpl implements ShuntCompensatorLinearModelAdder, ShuntCompensatorModelBuilder {

        private double bPerSection = Double.NaN;

        private double gPerSection = Double.NaN;

        private int maximumSectionCount = -1;

        @Override
        public ShuntCompensatorLinearModelAdder setBPerSection(double bPerSection) {
            this.bPerSection = bPerSection;
            return this;
        }

        @Override
        public ShuntCompensatorLinearModelAdder setGPerSection(double gPerSection) {
            this.gPerSection = gPerSection;
            return this;
        }

        @Override
        public int getMaximumSectionCount() {
            return maximumSectionCount;
        }

        @Override
        public ShuntCompensatorLinearModelAdder setMaximumSectionCount(int maximumSectionCount) {
            this.maximumSectionCount = maximumSectionCount;
            return this;
        }

        @Override
        public ShuntCompensatorAdder add() {
            ValidationUtil.checkBPerSection(ShuntCompensatorAdderImpl.this, bPerSection);
            ValidationUtil.checkMaximumSectionCount(ShuntCompensatorAdderImpl.this, maximumSectionCount);
            modelBuilder = this;
            return ShuntCompensatorAdderImpl.this;
        }

        @Override
        public ShuntCompensatorModelExt build() {
            return new ShuntCompensatorLinearModelImpl(bPerSection, gPerSection, maximumSectionCount);
        }
    }

    class ShuntCompensatorNonLinearModelAdderImpl implements ShuntCompensatorNonLinearModelAdder, ShuntCompensatorModelBuilder {

        private final List<SectionAdderImpl> sectionAdders = new ArrayList<>();

        class SectionAdderImpl implements SectionAdder {

            private double b = Double.NaN;

            private double g = Double.NaN;

            @Override
            public SectionAdder setB(double b) {
                this.b = b;
                return this;
            }

            @Override
            public SectionAdder setG(double g) {
                this.g = g;
                return this;
            }

            @Override
            public ShuntCompensatorNonLinearModelAdder endSection() {
                ValidationUtil.checkBPerSection(ShuntCompensatorAdderImpl.this, b);
                if (Double.isNaN(g))  {
                    if (sectionAdders.isEmpty()) {
                        g = 0;
                    } else {
                        g = sectionAdders.get(sectionAdders.size() - 1).g;
                    }
                }
                sectionAdders.add(this);
                return ShuntCompensatorNonLinearModelAdderImpl.this;
            }
        }

        @Override
        public SectionAdder beginSection() {
            return new SectionAdderImpl();
        }

        @Override
        public ShuntCompensatorAdder add() {
            if (sectionAdders.isEmpty()) {
                throw new ValidationException(ShuntCompensatorAdderImpl.this, "a shunt compensator must have at least one section");
            }
            modelBuilder = this;
            return ShuntCompensatorAdderImpl.this;
        }

        @Override
        public ShuntCompensatorModelExt build() {
            List<ShuntCompensatorNonLinearModelImpl.SectionImpl> sections = IntStream.range(0, sectionAdders.size()).mapToObj(s -> {
                SectionAdderImpl adder = sectionAdders.get(s);
                return new ShuntCompensatorNonLinearModelImpl.SectionImpl(s + 1, adder.b, adder.g);
            }).collect(Collectors.toList());

            return new ShuntCompensatorNonLinearModelImpl(sections);
        }

        @Override
        public int getMaximumSectionCount() {
            return sectionAdders.size();
        }
    }

    @Override
    public ShuntCompensatorLinearModelAdder newLinearModel() {
        return new ShuntCompensatorLinearModelAdderImpl();
    }

    @Override
    public ShuntCompensatorNonLinearModelAdder newNonLinearModel() {
        return new ShuntCompensatorNonLinearModelAdderImpl();
    }

    @Override
    public ShuntCompensatorAdder setSectionCount(int sectionCount) {
        this.sectionCount = sectionCount;
        return this;
    }

    @Override
    public ShuntCompensatorAdder setRegulatingTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = (TerminalExt) regulatingTerminal;
        return this;
    }

    @Override
    public ShuntCompensatorAdder useLocalRegulation(boolean use) {
        this.useLocalRegulation = use;
        return this;
    }

    @Override
    public ShuntCompensatorAdder setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn = voltageRegulatorOn;
        return this;
    }

    @Override
    public ShuntCompensatorAdder setTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public ShuntCompensatorAdder setTargetDeadband(double targetDeadband) {
        this.targetDeadband = targetDeadband;
        return this;
    }

    @Override
    public ShuntCompensatorImpl add() {
        String id = checkAndGetUniqueId();
        TerminalExt terminal = checkAndGetTerminal();

        boolean validateRegulatingTerminal = true;
        if (useLocalRegulation) {
            regulatingTerminal = terminal;
            validateRegulatingTerminal = false;
        }

        // The validation method of the regulating terminal (validation.validRegulatingTerminal)
        // checks that the terminal is not null and its network is the same as the object being added.
        // The network for the terminal is obtained from its voltage level but the terminal voltage level
        // is set after the validation is performed by the method voltageLevel.attach(terminal, false).
        // As we do not want to move the order of validation and terminal attachment
        // we do not check the regulating terminal if useLocalRegulation is true.
        // We assume the terminal will be ok since it will be the one of the equipment.
        ValidationUtil.checkDiscreteVoltageControl(this, regulatingTerminal, targetV, targetDeadband,
            voltageRegulatorOn, getNetwork(), validateRegulatingTerminal);

        if (modelBuilder == null) {
            throw new ValidationException(this, "the shunt compensator model has not been defined");
        }
        ValidationUtil.checkSections(this, sectionCount, modelBuilder.getMaximumSectionCount());

        ShuntCompensatorImpl shunt = new ShuntCompensatorImpl(getNetwork().getRef(),
                id, getName(), isFictitious(), modelBuilder.build(), sectionCount,
                regulatingTerminal, voltageRegulatorOn, targetV, targetDeadband);

        shunt.addTerminal(terminal);
        voltageLevel.attach(terminal, false);
        getNetwork().getIndex().checkAndAdd(shunt);
        getNetwork().getListeners().notifyCreation(shunt);
        return shunt;
    }

}
