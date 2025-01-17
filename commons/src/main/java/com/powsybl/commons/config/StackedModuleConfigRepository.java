/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link ModuleConfigRepository} backed by a list of modules repositories.
 * Configuration property values encountered first in this list of repositories
 * take precedence over values defined in subsequent repositories.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class StackedModuleConfigRepository implements ModuleConfigRepository {

    private final List<ModuleConfigRepository> repositories;

    public StackedModuleConfigRepository(ModuleConfigRepository... repositories) {
        this(Arrays.asList(repositories));
    }

    public StackedModuleConfigRepository(List<ModuleConfigRepository> repositories) {
        this.repositories = Objects.requireNonNull(repositories);
    }

    @Override
    public Optional<ModuleConfig> getModuleConfig(String name) {
        ModuleConfig stackedConfig = null;
        for (ModuleConfigRepository repository : repositories) {
            Optional<ModuleConfig> config = repository.getModuleConfig(name);
            if (config.isPresent()) {
                if (stackedConfig == null) {
                    stackedConfig = config.get();
                } else {
                    stackedConfig = new StackedModuleConfig(stackedConfig, config.get());
                }
            }
        }
        return Optional.ofNullable(stackedConfig);
    }
}
