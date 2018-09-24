/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlReaderContext;

import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkXmlReaderContext extends XmlContext implements XmlReaderContext {

    private final XMLStreamReader reader;

    private final List<Runnable> endTasks = new ArrayList<>();

    private final String version;

    public NetworkXmlReaderContext(Anonymizer anonymizer, XMLStreamReader reader, String version) {
        super(anonymizer);
        this.reader = Objects.requireNonNull(reader);
        this.version = Objects.requireNonNull(version);
    }

    @Override
    public XMLStreamReader getReader() {
        return reader;
    }

    public List<Runnable> getEndTasks() {
        return endTasks;
    }

    public String getVersion() {
        return version;
    }
}
