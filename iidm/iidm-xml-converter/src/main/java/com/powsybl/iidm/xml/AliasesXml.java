/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public final class AliasesXml {

    static final String ALIAS = "alias";

    public static void write(Identifiable<?> identifiable, String rootElementName, NetworkXmlWriterContext context) throws XMLStreamException {
        IidmXmlUtil.assertMinimumVersionIfNotDefault(!identifiable.getAliases().isEmpty(), rootElementName, ALIAS, IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
        for (String alias : identifiable.getAliases()) {
            context.getWriter().writeStartElement(context.getVersion().getNamespaceURI(context.isValid()), ALIAS);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_4, context, () -> identifiable.getAliasType(alias).ifPresent(type -> {
                try {
                    context.getWriter().writeAttribute("type", type);
                } catch (XMLStreamException e) {
                    throw new UncheckedXmlStreamException(e);
                }
            }));
            context.getWriter().writeCharacters(context.getAnonymizer().anonymizeString(alias));
            context.getWriter().writeEndElement();
        }
    }

    public static <T extends Identifiable> void read(T identifiable, NetworkXmlReaderContext context) throws XMLStreamException {
        read(context).accept(identifiable);
    }

    public static <T extends Identifiable> void read(List<Consumer<T>> toApply, NetworkXmlReaderContext context) throws XMLStreamException {
        toApply.add(read(context));
    }

    private static <T extends Identifiable> Consumer<T> read(NetworkXmlReaderContext context) throws XMLStreamException {
        if (!context.getReader().getLocalName().equals(ALIAS)) {
            throw new IllegalStateException();
        }
        String[] aliasType = new String[1];
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_4, context, () -> aliasType[0] = context.getReader().getAttributeValue(null, "type"));
        String alias = context.getAnonymizer().deanonymizeString(context.getReader().getElementText());
        return identifiable -> identifiable.addAlias(alias, aliasType[0]);
    }

    private AliasesXml() {
    }
}
