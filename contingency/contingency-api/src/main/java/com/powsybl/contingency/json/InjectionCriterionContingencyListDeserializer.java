/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.contingency.list.InjectionCriterionContingencyList;
import com.powsybl.contingency.contingency.list.criterion.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class InjectionCriterionContingencyListDeserializer extends StdDeserializer<InjectionCriterionContingencyList> {

    public InjectionCriterionContingencyListDeserializer() {
        super(InjectionCriterionContingencyList.class);
    }

    @Override
    public InjectionCriterionContingencyList deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String name = null;
        String identifiableType = null;
        SingleCountryCriterion countryCriterion = null;
        SingleNominalVoltageCriterion nominalVoltageCriterion = null;
        List<PropertyCriterion> propertyCriteria = Collections.emptyList();
        RegexCriterion regexCriterion = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken();
                    break;

                case "name":
                    name = parser.nextTextValue();
                    break;

                case "type":
                    parser.nextToken();
                    break;

                case "identifiableType":
                    identifiableType = parser.nextTextValue();
                    break;

                case "countryCriterion":
                    parser.nextToken();
                    countryCriterion = deserializationContext.readValue(parser, deserializationContext.constructType(Criterion.class));
                    break;
                case "nominalVoltageCriterion":
                    parser.nextToken();
                    nominalVoltageCriterion = deserializationContext.readValue(parser, deserializationContext.constructType(Criterion.class));
                    break;
                case "propertyCriteria":
                    parser.nextToken();
                    propertyCriteria = JsonUtil.readList(deserializationContext, parser, Criterion.class);
                    break;
                case "regexCriterion":
                    parser.nextToken();
                    regexCriterion = deserializationContext.readValue(parser, deserializationContext.constructType(Criterion.class));
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        return new InjectionCriterionContingencyList(name, identifiableType, countryCriterion,
                nominalVoltageCriterion, propertyCriteria, regexCriterion);
    }
}
