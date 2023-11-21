/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.extensions.XmlReaderContext;
import com.powsybl.commons.extensions.XmlWriterContext;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.LinePosition;
import com.powsybl.iidm.network.extensions.LinePositionAdder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
@AutoService(ExtensionXmlSerializer.class)
public class LinePositionXmlSerializer<T extends Identifiable<T>> extends AbstractExtensionXmlSerializer<T, LinePosition<T>> {

    private static final String COORDINATE_ROOT_NODE = "coordinate";
    private static final String COORDINATE_ARRAY_NODE = "coordinates";

    public LinePositionXmlSerializer() {
        super(LinePosition.NAME, "network", LinePosition.class, "linePosition.xsd",
                "http://www.powsybl.org/schema/iidm/ext/line_position/1_0", "lp");
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of(COORDINATE_ARRAY_NODE, COORDINATE_ROOT_NODE);
    }

    @Override
    public void write(LinePosition<T> linePosition, XmlWriterContext context) {
        context.getWriter().writeStartNodes(COORDINATE_ARRAY_NODE);
        for (Coordinate point : linePosition.getCoordinates()) {
            context.getWriter().writeStartNode(getNamespaceUri(), COORDINATE_ROOT_NODE);
            context.getWriter().writeDoubleAttribute("longitude", point.getLongitude());
            context.getWriter().writeDoubleAttribute("latitude", point.getLatitude());
            context.getWriter().writeEndNode();
        }
        context.getWriter().writeEndNodes();
    }

    @Override
    public LinePosition<T> read(T line, XmlReaderContext context) {
        List<Coordinate> coordinates = new ArrayList<>();
        context.getReader().readChildNodes(elementName -> {
            if (!elementName.equals(COORDINATE_ROOT_NODE)) {
                throw new PowsyblException("Unknown element name '" + elementName + "' in 'linePosition'");
            }
            double longitude = context.getReader().readDoubleAttribute("longitude");
            double latitude = context.getReader().readDoubleAttribute("latitude");
            context.getReader().readEndNode();
            coordinates.add(new Coordinate(latitude, longitude));
        });
        LinePositionAdder<T> adder = line.newExtension(LinePositionAdder.class);
        return adder.withCoordinates(coordinates)
                .add();
    }
}
