/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.powsybl.iidm.network.ContainerType;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class SubstationAdapter extends AbstractIdentifiableAdapter<Substation> implements Substation {

    SubstationAdapter(final Substation delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public VoltageLevelAdderAdapter newVoltageLevel() {
        return new VoltageLevelAdderAdapter(getDelegate().newVoltageLevel(), getIndex());
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return getDelegate().getVoltageLevelStream().map(getIndex()::getVoltageLevel);
    }

    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        return Collections.unmodifiableSet(getVoltageLevelStream().collect(Collectors.toSet()));
    }

    @Override
    public TwoWindingsTransformerAdderAdapter newTwoWindingsTransformer() {
        return new TwoWindingsTransformerAdderAdapter(getDelegate().newTwoWindingsTransformer(), getIndex());
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return Collections.unmodifiableSet(getTwoWindingsTransformerStream().collect(Collectors.toSet()));
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return getDelegate().getTwoWindingsTransformerStream().map(getIndex()::getTwoWindingsTransformer);
    }

    @Override
    public ThreeWindingsTransformerAdderAdapter newThreeWindingsTransformer() {
        return new ThreeWindingsTransformerAdderAdapter(getDelegate().newThreeWindingsTransformer(), getIndex());
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return Collections.unmodifiableSet(getThreeWindingsTransformerStream().collect(Collectors.toSet()));
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return getDelegate().getThreeWindingsTransformerStream().map(getIndex()::getThreeWindingsTransformer);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public ContainerType getContainerType() {
        return getDelegate().getContainerType();
    }

    @Override
    public Optional<Country> getCountry() {
        return getDelegate().getCountry();
    }

    @Override
    public Country getNullableCountry() {
        return getDelegate().getNullableCountry();
    }

    @Override
    public SubstationAdapter setCountry(final Country country) {
        getDelegate().setCountry(country);
        return this;
    }

    @Override
    public String getTso() {
        return getDelegate().getTso();
    }

    @Override
    public SubstationAdapter setTso(final String tso) {
        getDelegate().setTso(tso);
        return this;
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        return getDelegate().getTwoWindingsTransformerCount();
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        return getDelegate().getThreeWindingsTransformerCount();
    }

    @Override
    public Set<String> getGeographicalTags() {
        return getDelegate().getGeographicalTags();
    }

    @Override
    public SubstationAdapter addGeographicalTag(final String tag) {
        getDelegate().addGeographicalTag(tag);
        return this;
    }
}
