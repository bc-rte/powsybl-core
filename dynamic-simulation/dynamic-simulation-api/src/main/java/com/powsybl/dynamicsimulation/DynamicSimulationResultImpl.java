/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.powsybl.timeseries.DoubleTimeSeries;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSimulationResultImpl implements DynamicSimulationResult {

    private final Status status;
    private final String statusText;
    private final Map<String, DoubleTimeSeries> curves;
    private final List<TimelineEvent> timeLine;

    public DynamicSimulationResultImpl(Status status, String statusText, Map<String, DoubleTimeSeries> curves, List<TimelineEvent> timeLine) {
        this.status = Objects.requireNonNull(status);
        this.statusText = Objects.requireNonNull(statusText);
        this.curves = Objects.requireNonNull(curves);
        this.timeLine = Objects.requireNonNull(timeLine);
        timeLine.forEach(Objects::requireNonNull);
    }

    public DynamicSimulationResultImpl(Status status, Map<String, DoubleTimeSeries> curves, List<TimelineEvent> timeLine) {
        this(status, "", curves, timeLine);
    }

    public static DynamicSimulationResultImpl createSuccessResult(Map<String, DoubleTimeSeries> curves, List<TimelineEvent> timeLine) {
        return new DynamicSimulationResultImpl(Status.SUCCESS, curves, timeLine);
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getStatusText() {
        return statusText;
    }

    @Override
    public Map<String, DoubleTimeSeries> getCurves() {
        return Collections.unmodifiableMap(curves);
    }

    @Override
    public List<TimelineEvent> getTimeLine() {
        return timeLine;
    }
}
