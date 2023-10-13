/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.ContingenciesProviders;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;
import com.powsybl.security.execution.NetworkVariant;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Laurent Issertial <laurent.issertial at rte-france.com>
 */
public abstract class AbstractSecurityAnalysisInput<T extends AbstractSecurityAnalysisInput<T>> implements SecurityAnalysisInputInterface {

    private final NetworkVariant networkVariant;
    private Set<SecurityAnalysisInterceptor> interceptors;
    private LimitViolationFilter filter;
    private LimitViolationDetector detector;
    private ContingenciesProvider contingencies;

    public AbstractSecurityAnalysisInput(Network network, String variantId) {
        this(new NetworkVariant(network, variantId));
    }

    public AbstractSecurityAnalysisInput(NetworkVariant networkVariant) {
        this.networkVariant = Objects.requireNonNull(networkVariant);
        this.interceptors = new HashSet<>();
        this.filter = new LimitViolationFilter();
        this.detector = new DefaultLimitViolationDetector();
        this.contingencies = ContingenciesProviders.emptyProvider();
    }

    /**
     * Get specified {@link ContingenciesProvider}.
     */
    public ContingenciesProvider getContingenciesProvider() {
        return contingencies;
    }

    /**
     * Get specified {@link LimitViolationDetector}.
     */
    public LimitViolationDetector getLimitViolationDetector() {
        return detector;
    }

    public LimitViolationFilter getFilter() {
        return filter;
    }

    public Set<SecurityAnalysisInterceptor> getInterceptors() {
        return Collections.unmodifiableSet(interceptors);
    }

    public T setDetector(LimitViolationDetector detector) {
        Objects.requireNonNull(detector);
        this.detector = detector;
        return self();
    }

    public T setContingencies(ContingenciesProvider contingencies) {
        Objects.requireNonNull(contingencies);
        this.contingencies = contingencies;
        return self();
    }

    public T addInterceptor(SecurityAnalysisInterceptor interceptor) {
        interceptors.add(Objects.requireNonNull(interceptor));
        return self();
    }

    public T setFilter(LimitViolationFilter filter) {
        this.filter = Objects.requireNonNull(filter);
        return self();
    }

    public NetworkVariant getNetworkVariant() {
        return networkVariant;
    }

    protected abstract T self();
}