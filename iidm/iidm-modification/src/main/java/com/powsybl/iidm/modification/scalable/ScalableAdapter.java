/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.List;
import java.util.Objects;

class ScalableAdapter extends AbstractScalable {

    private final String id;

    public ScalableAdapter(String id) {
        super(-Double.MAX_VALUE, Double.MAX_VALUE, ScalingConvention.GENERATOR);
        this.id = Objects.requireNonNull(id);
    }

    private Scalable getScalable(Network n) {
        Objects.requireNonNull(n);
        Identifiable identifiable = n.getIdentifiable(id);
        if (identifiable instanceof Generator) {
            return new GeneratorScalable(id);
        } else if (identifiable instanceof Load) {
            return new LoadScalable(id);
        } else {
            throw new PowsyblException("Unable to create a scalable from " + identifiable.getClass());
        }
    }

    @Override
    public void setInitialInjectionToNetworkValue(Network n) {
        initialInjection = getScalable(n).getCurrentInjection(n, ScalingConvention.GENERATOR);
    }

    @Override
    public double getCurrentInjection(Network n, ScalingConvention scalingConvention) {
        return getScalable(n).getCurrentInjection(n, scalingConvention);
    }

    @Override
    public void reset(Network n) {
        Scalable scalable = getScalable(n);
        scalable.scale(n, getInitialInjection(ScalingConvention.GENERATOR) - scalable.getCurrentInjection(n, ScalingConvention.GENERATOR), ScalingConvention.GENERATOR);
    }

    @Override
    public double getMaximumInjection(Network n, ScalingConvention scalingConvention) {
        return getScalable(n).getMaximumInjection(n, scalingConvention);
    }

    @Override
    public double getMinimumInjection(Network n, ScalingConvention scalingConvention) {
        return getScalable(n).getMinimumInjection(n, scalingConvention);
    }

    @Override
    public void filterInjections(Network n, List<Injection> injections, List<String> notFound) {
        getScalable(n).filterInjections(n, injections, notFound);
    }

    @Override
    public double scale(Network n, double asked, ScalingConvention scalingConvention) {
        return getScalable(n).scale(n, asked, scalingConvention);
    }
}
