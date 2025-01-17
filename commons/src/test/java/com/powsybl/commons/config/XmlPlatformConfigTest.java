/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class XmlPlatformConfigTest {

    XmlPlatformConfigTest() {
    }

    @Test
    void properties2XmlConvertionTest() throws IOException, XMLStreamException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path cfgDir = Files.createDirectory(fileSystem.getPath("config"));
            Properties prop1 = new Properties();
            prop1.setProperty("a", "hello");
            prop1.setProperty("b", "bye");
            try (Writer w = Files.newBufferedWriter(cfgDir.resolve("mod1.properties"), StandardCharsets.UTF_8)) {
                prop1.store(w, null);
            }
            Properties prop2 = new Properties();
            prop2.setProperty("c", "thanks");
            try (Writer w = Files.newBufferedWriter(cfgDir.resolve("mod2.properties"), StandardCharsets.UTF_8)) {
                prop2.store(w, null);
            }
            PlatformConfig propsConfig = new PlatformConfig(new PropertiesModuleConfigRepository(cfgDir), cfgDir);
            assertEquals("hello", propsConfig.getOptionalModuleConfig("mod1").flatMap(c -> c.getOptionalStringProperty("a")).orElse(""));
            assertEquals("bye", propsConfig.getOptionalModuleConfig("mod1").flatMap(c -> c.getOptionalStringProperty("b")).orElse(""));
            assertEquals("thanks", propsConfig.getOptionalModuleConfig("mod2").flatMap(c -> c.getOptionalStringProperty("c")).orElse(""));
            Path xmlConfigFile = cfgDir.resolve("config.xml");
            PropertiesModuleConfigRepository.writeXml(cfgDir, xmlConfigFile);

            PlatformConfig xmlConfig = new PlatformConfig(new XmlModuleConfigRepository(xmlConfigFile), cfgDir);
            assertEquals("hello", xmlConfig.getOptionalModuleConfig("mod1").flatMap(c -> c.getOptionalStringProperty("a")).orElse(""));
            assertEquals("bye", xmlConfig.getOptionalModuleConfig("mod1").flatMap(c -> c.getOptionalStringProperty("b")).orElse(""));
            assertEquals("thanks", xmlConfig.getOptionalModuleConfig("mod2").flatMap(c -> c.getOptionalStringProperty("c")).orElse(""));
        }
    }

}
