/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io;

import java.util.List;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface TreeDataReader extends AutoCloseable {

    interface ChildNodeReader {

        /**
         * The implementations must read the full node corresponding to the given name,
         * including the corresponding end node
         * @param nodeName field name of the started node
         */
        void onStartNode(String nodeName);
    }

    String readRootVersion();

    Map<String, String> readVersions();

    double readDoubleAttribute(String name);

    double readDoubleAttribute(String name, double defaultValue);

    float readFloatAttribute(String name);

    float readFloatAttribute(String name, float defaultValue);

    String readStringAttribute(String name);

    Integer readIntAttribute(String name);

    int readIntAttribute(String name, int defaultValue);

    Boolean readBooleanAttribute(String name);

    boolean readBooleanAttribute(String name, boolean defaultValue);

    <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz);

    <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz, T defaultValue);

    String readContent();

    List<Integer> readIntArrayAttribute(String name);

    void skipChildNodes();

    void readChildNodes(ChildNodeReader childNodeReader);

    void readEndNode();

    @Override
    void close();
}
