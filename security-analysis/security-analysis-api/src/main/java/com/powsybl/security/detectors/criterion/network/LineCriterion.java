package com.powsybl.security.detectors.criterion.network;

import com.powsybl.iidm.network.util.criterion.SingleNominalVoltageCriterion;
import com.powsybl.iidm.network.util.criterion.TwoCountriesCriterion;

import java.util.Set;

public class LineCriterion extends AbstractNetworkElementCriterion implements BranchCriterion {

    TwoCountriesCriterion twoCountriesCriterion = new TwoCountriesCriterion(null);
    SingleNominalVoltageCriterion singleNominalVoltageCriterion = new SingleNominalVoltageCriterion(null);

    public LineCriterion(Set<String> networkElementIds) {
        super(networkElementIds);
    }

    @Override
    public NetworkElementCriterionType getNetworkElementCriterionType() {
        return NetworkElementCriterionType.BRANCH;
    }

    @Override
    public BranchCriterionType getBranchCriterionType() {
        return BranchCriterionType.LINE;
    }

    public TwoCountriesCriterion getTwoCountriesCriterion() {
        return twoCountriesCriterion;
    }

    public LineCriterion setTwoCountriesCriterion(TwoCountriesCriterion twoCountriesCriterion) {
        this.twoCountriesCriterion = twoCountriesCriterion;
        return this;
    }

    public SingleNominalVoltageCriterion getSingleNominalVoltageCriterion() {
        return singleNominalVoltageCriterion;
    }

    public LineCriterion setSingleNominalVoltageCriterion(SingleNominalVoltageCriterion singleNominalVoltageCriterion) {
        this.singleNominalVoltageCriterion = singleNominalVoltageCriterion;
        return this;
    }

}
