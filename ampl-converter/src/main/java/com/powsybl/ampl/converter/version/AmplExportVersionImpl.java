/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter.version;

/**
 * @author Nicolas Pierre <nicolas.pierre at artelys.com>
 */
public enum AmplExportVersionImpl implements AmplExportVersion {
    /**
     * Legacy export
     */
    V1_LEGACY(BasicAmplExporter.getFactory());

    private final AmplExportVersion.Factory factory;

    AmplExportVersionImpl(AmplExportVersion.Factory factory) {
        this.factory = factory;
    }

    public AmplExportVersion.Factory getColumnsExporter() {
        return factory;
    }
}