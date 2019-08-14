package com.powsybl.cgmes.update.elements;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.update.CgmesPredicateDetails;
import com.powsybl.cgmes.update.ConversionMapper;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes14;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * For conversion onCreate of TwoWindingsTransformer we need to create
 * additional elements: End1 and End2, tapChangers, tables, if required. All
 * have distinct ID (Subject) and contain reference to the parent
 * PowerTransformer element.
 */
public class TwoWindingsTransformerToPowerTransformer extends IidmToCgmes14 implements ConversionMapper {

    public TwoWindingsTransformerToPowerTransformer(IidmChange change, CgmesModel cgmes) {
        super(change, cgmes);
    }

    @Override
    public Map<String, Object> mapIidmToCgmesPredicates() {
        return Collections.unmodifiableMap(Stream.of(
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false)),
            entry("b", new CgmesPredicateDetails("cim:TransformerWinding.b", "_EQ", false, idEnd1)),
            entry("r", new CgmesPredicateDetails("cim:TransformerWinding.r", "_EQ", false, idEnd1)),
            entry("x", new CgmesPredicateDetails("cim:TransformerWinding.x", "_EQ", false, idEnd1)),
            entry("g", new CgmesPredicateDetails("cim:TransformerWinding.g", "_EQ", false, idEnd1)),
            entry("ratedU1", new CgmesPredicateDetails("cim:TransformerWinding.ratedU", "_EQ", false, idEnd1)),
            entry("ratedU2", new CgmesPredicateDetails("cim:TransformerWinding.ratedU", "_EQ", false, idEnd2)),
            entry("ratioTapChanger.tapPosition",
                new CgmesPredicateDetails("cim:TapChanger.neutralStep", "_EQ", false, idRTCH)),
            entry("ratioTapChanger.lowTapPosition",
                new CgmesPredicateDetails("cim:TapChanger.lowStep", "_EQ", false, idRTCH)),
            entry("phaseTapChanger.tapPosition",
                new CgmesPredicateDetails("cim:TapChanger.neutralStep", "_EQ", false, idPHTC)),
            entry("phaseTapChanger.lowTapPosition",
                new CgmesPredicateDetails("cim:TapChanger.lowStep", "_EQ", false, idPHTC)))
            .collect(entriesToMap()));
    }

    @Override
    public Map<CgmesPredicateDetails, String> getAllCgmesDetailsOnCreate() {

        Map<CgmesPredicateDetails, String> allCgmesDetails = new HashMap<CgmesPredicateDetails, String>();

        TwoWindingsTransformer newTwoWindingsTransformer = (TwoWindingsTransformer) change.getIdentifiable();

        String ptId = newTwoWindingsTransformer.getId();

        CgmesPredicateDetails rdfType = new CgmesPredicateDetails("rdf:type", "_EQ", false);
        allCgmesDetails.put(rdfType, "cim:PowerTransformer");

        String name = newTwoWindingsTransformer.getName();
        allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("name"),
            name);

        String substationId = newTwoWindingsTransformer.getSubstation().getId();
        CgmesPredicateDetails equipmentContainer = new CgmesPredicateDetails(
            "cim:Equipment.MemberOf_EquipmentContainer", "_EQ",
            true);
        allCgmesDetails.put(equipmentContainer, substationId);

        /**
         * PowerTransformerEnd1
         */
        CgmesPredicateDetails rdfTypeEnd1 = new CgmesPredicateDetails("rdf:type", "_EQ", false, idEnd1);
        allCgmesDetails.put(rdfTypeEnd1, "cim:TransformerWinding");

        CgmesPredicateDetails powerTransformerEnd1 = new CgmesPredicateDetails(
            "cim:TransformerWinding.MemberOf_PowerTransformer",
            "_EQ", true, idEnd1);
        allCgmesDetails.put(powerTransformerEnd1, ptId);

        CgmesPredicateDetails end1Type = new CgmesPredicateDetails(
            "cim:TransformerWinding.windingType",
            "_EQ", false, idEnd1);
        allCgmesDetails.put(end1Type, "cim:WindingType.primary");

        double b = newTwoWindingsTransformer.getB();
        if (!String.valueOf(b).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("b"),
                String.valueOf(b));
        }

        double r = newTwoWindingsTransformer.getR();
        if (!String.valueOf(r).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("r"),
                String.valueOf(r));
        }

        double x = newTwoWindingsTransformer.getX();
        if (!String.valueOf(x).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("x"),
                String.valueOf(x));
        }

        double g = newTwoWindingsTransformer.getG();
        if (!String.valueOf(g).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("g"),
                String.valueOf(g));
        }

        double ratedU1 = newTwoWindingsTransformer.getRatedU1();
        if (!String.valueOf(ratedU1).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("ratedU1"),
                String.valueOf(ratedU1));
        }

        /**
         * PowerTransformerEnd2
         */

        CgmesPredicateDetails rdfTypeEnd2 = new CgmesPredicateDetails("rdf:type", "_EQ", false, idEnd2);
        allCgmesDetails.put(rdfTypeEnd2, "cim:TransformerWinding");

        CgmesPredicateDetails powerTransformerEnd2 = new CgmesPredicateDetails(
            "cim:TransformerWinding.MemberOf_PowerTransformer",
            "_EQ", true, idEnd2);
        allCgmesDetails.put(powerTransformerEnd2, ptId);

        CgmesPredicateDetails end2Type = new CgmesPredicateDetails(
            "cim:TransformerWinding.windingType",
            "_EQ", false, idEnd2);
        allCgmesDetails.put(end2Type, "cim:WindingType.secondary");

        CgmesPredicateDetails bEnd2 = new CgmesPredicateDetails("cim:TransformerWinding.b", "_EQ", false, idEnd2);
        allCgmesDetails.put(bEnd2, String.valueOf(0.0));

        CgmesPredicateDetails rEnd2 = new CgmesPredicateDetails("cim:TransformerWinding.r", "_EQ", false, idEnd2);
        allCgmesDetails.put(rEnd2, String.valueOf(0.0));

        CgmesPredicateDetails xEnd2 = new CgmesPredicateDetails("cim:TransformerWinding.x", "_EQ", false, idEnd2);
        allCgmesDetails.put(xEnd2, String.valueOf(0.0));

        CgmesPredicateDetails gEnd2 = new CgmesPredicateDetails("cim:TransformerWinding.g", "_EQ", false, idEnd2);
        allCgmesDetails.put(gEnd2, String.valueOf(0.0));

        double ratedU2 = newTwoWindingsTransformer.getRatedU2();
        if (!String.valueOf(ratedU1).equals("NaN")) {
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("ratedU2"),
                String.valueOf(ratedU2));
        }

        /**
         * PhaseTapChanger
         */
        PhaseTapChanger newPhaseTapChanger = newTwoWindingsTransformer.getPhaseTapChanger();

        if (newPhaseTapChanger != null) {
            CgmesPredicateDetails rdfTypePHTC = new CgmesPredicateDetails("rdf:type", "_EQ", false, idPHTC);
            allCgmesDetails.put(rdfTypePHTC, "cim:PhaseTapChangerTabular");

            CgmesPredicateDetails namePHTC = new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false,
                idPHTC);
            allCgmesDetails.put(namePHTC, name);

            CgmesPredicateDetails TransformerWindingPHTC = new CgmesPredicateDetails(
                "cim:PhaseTapChanger.TransformerWinding",
                "_EQ", true, idPHTC);
            allCgmesDetails.put(TransformerWindingPHTC, idEnd1);

            int lowTapPositionPHTC = newPhaseTapChanger.getLowTapPosition();
            allCgmesDetails.put(
                (CgmesPredicateDetails) mapIidmToCgmesPredicates().get("phaseTapChanger.lowTapPosition"),
                String.valueOf(lowTapPositionPHTC));

            int tapPositionPHTC = newPhaseTapChanger.getTapPosition();
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("phaseTapChanger.tapPosition"),
                String.valueOf(tapPositionPHTC + 1));
        }
        /**
         * RatioTapChanger
         */
        RatioTapChanger newRatioTapChanger = newTwoWindingsTransformer.getRatioTapChanger();

        if (newRatioTapChanger != null) {
            CgmesPredicateDetails rdfTypeRTCH = new CgmesPredicateDetails("rdf:type", "_EQ", false, idRTCH);
            allCgmesDetails.put(rdfTypeRTCH, "cim:RatioTapChanger");

            CgmesPredicateDetails nameRTCH = new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false,
                idRTCH);
            allCgmesDetails.put(nameRTCH, name);

            CgmesPredicateDetails TransformerWindingRTCH = new CgmesPredicateDetails(
                "cim:RatioTapChanger.TransformerWinding",
                "_EQ", true, idRTCH);
            allCgmesDetails.put(TransformerWindingRTCH, idEnd1);

            int lowTapPositionRTCH = newRatioTapChanger.getLowTapPosition();
            allCgmesDetails.put(
                (CgmesPredicateDetails) mapIidmToCgmesPredicates().get("ratioTapChanger.lowTapPosition"),
                String.valueOf(lowTapPositionRTCH));

            int tapPositionRTCH = newRatioTapChanger.getTapPosition();
            allCgmesDetails.put((CgmesPredicateDetails) mapIidmToCgmesPredicates().get("ratioTapChanger.tapPosition"),
                String.valueOf(tapPositionRTCH + 1));
        }
        return allCgmesDetails;
    }

    /**
     * Check if TransformerWinding elements already exist in grid, if yes - returns
     * the id.
     *
     */
    private Map<String, String> getEndsId() {
        PropertyBags transformerEnds = cgmes.transformerEnds();
        Map<String, String> ids = new HashMap<>();
        Iterator i = transformerEnds.iterator();

        while (i.hasNext()) {
            PropertyBag pb = (PropertyBag) i.next();
            String windingType = pb.get("windingType");
            if (pb.getId("PowerTransformer").equals(change.getIdentifiableId())
                && windingType.endsWith("primary")) {
                idEnd1 = pb.getId("TransformerWinding");
                ids.put("idEnd1", idEnd1);
            } else if (pb.getId("PowerTransformer").equals(change.getIdentifiableId())
                && windingType.endsWith("secondary")) {
                idEnd2 = pb.getId("TransformerWinding");
                ids.put("idEnd2", idEnd2);
            } else {
                continue;
            }
        }
        return ids;
    }

    /**
     * Check if TapChanger elements already exist in grid, if yes - returns the id.
     *
     */
    private String getTapChangerId(String type) {
        PropertyBags tapChangers = (type.equals("RatioTapChanger")) ? cgmes.ratioTapChangers()
            : cgmes.phaseTapChangers();
        Iterator i = tapChangers.iterator();

        while (i.hasNext()) {
            PropertyBag pb = (PropertyBag) i.next();
            if (pb.getId("TransformerWinding").equals(idEnd1)) {
                return pb.getId(type);
            } else {
                continue;
            }
        }
        return UUID.randomUUID().toString();
    }

    private String idEnd1 = (getEndsId().get("idEnd1") != null) ? getEndsId().get("idEnd1")
        : UUID.randomUUID().toString();
    private String idEnd2 = (getEndsId().get("idEnd2") != null) ? getEndsId().get("idEnd2")
        : UUID.randomUUID().toString();
    private String idRTCH = getTapChangerId("RatioTapChanger");
    private String idPHTC = getTapChangerId("PhaseTapChanger");
}
