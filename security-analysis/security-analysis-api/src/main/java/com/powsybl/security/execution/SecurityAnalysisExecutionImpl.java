/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.execution;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.security.SecurityAnalysis;
import com.powsybl.security.SecurityAnalysisInput;
import com.powsybl.security.SecurityAnalysisReport;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

/**
 * A local execution of a security analysis. Before the actual execution,
 * security analysis inputs are built from the so-called execution inputs using a specified strategy,
 * including possible user defined transformation or preprocessing.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 * @author Laurent Issertial <laurent.issertial at rte-france.com>
 */
public class SecurityAnalysisExecutionImpl implements SecurityAnalysisExecution {

    private final String staticProviderName;
    private final SecurityAnalysisInputBuildStrategy inputBuildStrategy;

    /**
     * The execution will use the default security-analysis implementation defined in the platform.
     */
    public SecurityAnalysisExecutionImpl() {
        this(null, SecurityAnalysisExecutionImpl::buildDefault);
    }

    /**
     * The execution will use the {@literal providerName} implementation.
     */
    public SecurityAnalysisExecutionImpl(String staticProviderName) {
        this(staticProviderName, SecurityAnalysisExecutionImpl::buildDefault);
    }

    public SecurityAnalysisExecutionImpl(String staticProviderName, SecurityAnalysisInputBuildStrategy inputBuildStrategy) {
        this.staticProviderName = staticProviderName;
        this.inputBuildStrategy = requireNonNull(inputBuildStrategy);
    }

    private static SecurityAnalysisInput buildDefault(SecurityAnalysisExecutionInput executionInput) {
        return new SecurityAnalysisInput(executionInput.getNetworkVariant());
    }

    @Override
    public CompletableFuture<SecurityAnalysisReport> execute(ComputationManager computationManager, SecurityAnalysisExecutionInput data) {
        SecurityAnalysisInput input = inputBuildStrategy.buildFrom(data);
        SecurityAnalysis.Runner runner = SecurityAnalysis.find(staticProviderName);
        return runner.runAsync(input.getNetworkVariant().getNetwork(),
                input.getNetworkVariant().getVariantId(),
                input.getContingenciesProvider(),
                input.getParameters(),
                computationManager,
                input.getFilter(),
                input.getLimitViolationDetector(),
                new ArrayList<>(input.getInterceptors()),
                data.getOperatorStrategies(),
                data.getActions(),
                data.getMonitors(),
                Reporter.NO_OP);
    }
}
