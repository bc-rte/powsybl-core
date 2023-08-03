/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.base.Functions;
import com.google.common.collect.*;
import com.google.common.primitives.Ints;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.components.AbstractConnectedComponentsManager;
import com.powsybl.iidm.network.components.AbstractSynchronousComponentsManager;
import com.powsybl.iidm.network.impl.util.RefChain;
import com.powsybl.iidm.network.impl.util.RefObj;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.iidm.network.util.TieLineUtil.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class NetworkImpl extends AbstractIdentifiable<Network> implements Network, VariantManagerHolder, MultiVariantObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkImpl.class);

    private final RefChain<NetworkImpl> ref = new RefChain<>(new RefObj<>(this));

    private DateTime caseDate = new DateTime(); // default is the time at which the network has been created

    private int forecastDistance = 0;

    private String sourceFormat;

    private ValidationLevel validationLevel = ValidationLevel.STEADY_STATE_HYPOTHESIS;
    private ValidationLevel minValidationLevel = ValidationLevel.STEADY_STATE_HYPOTHESIS;

    private final NetworkIndex index = new NetworkIndex();

    private final VariantManagerImpl variantManager;

    private final NetworkListenerList listeners = new NetworkListenerList();

    class BusBreakerViewImpl implements BusBreakerView {

        @Override
        public Iterable<Bus> getBuses() {
            return FluentIterable.from(getVoltageLevels())
                    .transformAndConcat(vl -> vl.getBusBreakerView().getBuses());
        }

        @Override
        public Stream<Bus> getBusStream() {
            return getVoltageLevelStream().flatMap(vl -> vl.getBusBreakerView().getBusStream());
        }

        @Override
        public int getBusCount() {
            return getVoltageLevelStream().mapToInt(vl -> vl.getBusBreakerView().getBusCount()).sum();
        }

        @Override
        public Iterable<Switch> getSwitches() {
            return FluentIterable.from(getVoltageLevels())
                    .transformAndConcat(vl -> vl.getBusBreakerView().getSwitches());
        }

        @Override
        public Stream<Switch> getSwitchStream() {
            return getVoltageLevelStream().flatMap(vl -> vl.getBusBreakerView().getSwitchStream());
        }

        @Override
        public int getSwitchCount() {
            return getVoltageLevelStream().mapToInt(vl -> vl.getBusBreakerView().getSwitchCount()).sum();
        }

        @Override
        public Bus getBus(String id) {
            Bus bus = index.get(id, Bus.class);
            if (bus != null) {
                return bus;
            }
            return variants.get().busBreakerViewCache.getBus(id);
        }

        void invalidateCache() {
            variants.get().busBreakerViewCache.invalidate();
        }
    }

    private final BusBreakerViewImpl busBreakerView = new BusBreakerViewImpl();

    class BusViewImpl implements BusView {

        @Override
        public Iterable<Bus> getBuses() {
            return FluentIterable.from(getVoltageLevels())
                    .transformAndConcat(vl -> vl.getBusView().getBuses());
        }

        @Override
        public Stream<Bus> getBusStream() {
            return getVoltageLevelStream().flatMap(vl -> vl.getBusView().getBusStream());
        }

        @Override
        public Collection<Component> getConnectedComponents() {
            return Collections.unmodifiableList(variants.get().connectedComponentsManager.getConnectedComponents());
        }

        @Override
        public Collection<Component> getSynchronousComponents() {
            return Collections.unmodifiableList(variants.get().synchronousComponentsManager.getConnectedComponents());
        }

        @Override
        public Bus getBus(String id) {
            return variants.get().busViewCache.getBus(id);
        }

        void invalidateCache() {
            variants.get().busViewCache.invalidate();
        }

    }

    private final BusViewImpl busView = new BusViewImpl();

    NetworkImpl(String id, String name, String sourceFormat) {
        super(id, name);
        Objects.requireNonNull(sourceFormat, "source format is null");
        this.sourceFormat = sourceFormat;
        variantManager = new VariantManagerImpl(this);
        variants = new VariantArray<>(ref, VariantImpl::new);
        // add the network the object list as it is a multi variant object
        // and it needs to be notified when and extension or a reduction of
        // the variant array is requested
        index.checkAndAdd(this);
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.NETWORK;
    }

    @Override
    public DateTime getCaseDate() {
        return caseDate;
    }

    @Override
    public NetworkImpl setCaseDate(DateTime caseDate) {
        ValidationUtil.checkCaseDate(this, caseDate);
        this.caseDate = caseDate;
        return this;
    }

    @Override
    public int getForecastDistance() {
        return forecastDistance;
    }

    @Override
    public NetworkImpl setForecastDistance(int forecastDistance) {
        ValidationUtil.checkForecastDistance(this, forecastDistance);
        this.forecastDistance = forecastDistance;
        return this;
    }

    @Override
    public String getSourceFormat() {
        return sourceFormat;
    }

    RefChain<NetworkImpl> getRef() {
        return ref;
    }

    NetworkListenerList getListeners() {
        return listeners;
    }

    public NetworkIndex getIndex() {
        return index;
    }

    @Override
    public NetworkImpl getNetwork() {
        return this;
    }

    @Override
    public VariantManagerImpl getVariantManager() {
        return variantManager;
    }

    @Override
    public int getVariantIndex() {
        return variantManager.getVariantContext().getVariantIndex();
    }

    @Override
    public Set<Country> getCountries() {
        return getSubstationStream()
                .map(Substation::getCountry)
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Country.class)));
    }

    @Override
    public int getCountryCount() {
        return getCountries().size();
    }

    @Override
    public SubstationAdder newSubstation() {
        return new SubstationAdderImpl(ref);
    }

    @Override
    public Iterable<Substation> getSubstations() {
        return Collections.unmodifiableCollection(index.getAll(SubstationImpl.class));
    }

    @Override
    public Stream<Substation> getSubstationStream() {
        return index.getAll(SubstationImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getSubstationCount() {
        return index.getAll(SubstationImpl.class).size();
    }

    @Override
    public Iterable<Substation> getSubstations(Country country, String tsoId, String... geographicalTags) {
        return getSubstations(Optional.ofNullable(country).map(Country::getName).orElse(null), tsoId, geographicalTags);
    }

    @Override
    public Iterable<Substation> getSubstations(String country, String tsoId, String... geographicalTags) {
        return Substations.filter(getSubstations(), country, tsoId, geographicalTags);
    }

    @Override
    public SubstationImpl getSubstation(String id) {
        return index.get(id, SubstationImpl.class);
    }

    @Override
    public VoltageLevelAdder newVoltageLevel() {
        return new VoltageLevelAdderImpl(ref);
    }

    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        return Iterables.concat(index.getAll(BusBreakerVoltageLevel.class),
                index.getAll(NodeBreakerVoltageLevel.class));
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return Stream.concat(index.getAll(BusBreakerVoltageLevel.class).stream(),
                index.getAll(NodeBreakerVoltageLevel.class).stream());
    }

    @Override
    public int getVoltageLevelCount() {
        return index.getAll(BusBreakerVoltageLevel.class).size()
                + index.getAll(NodeBreakerVoltageLevel.class).size();
    }

    @Override
    public VoltageLevelExt getVoltageLevel(String id) {
        return index.get(id, VoltageLevelExt.class);
    }

    @Override
    public LineAdderImpl newLine() {
        return new LineAdderImpl(this);
    }

    @Override
    public Iterable<Line> getLines() {
        return Collections.unmodifiableCollection(index.getAll(LineImpl.class));
    }

    @Override
    public Iterable<TieLine> getTieLines() {
        return Collections.unmodifiableCollection(index.getAll(TieLineImpl.class));
    }

    @Override
    public Branch getBranch(String branchId) {
        Objects.requireNonNull(branchId);
        Branch branch = getLine(branchId);
        if (branch == null) {
            branch = getTwoWindingsTransformer(branchId);
            if (branch == null) {
                branch = getTieLine(branchId);
            }
        }
        return branch;
    }

    @Override
    public Iterable<Branch> getBranches() {
        return Iterables.concat(getLines(), getTwoWindingsTransformers(), getTieLines());
    }

    @Override
    public Stream<Branch> getBranchStream() {
        return Stream.of(getLineStream(), getTwoWindingsTransformerStream(), getTieLineStream()).flatMap(Function.identity());
    }

    @Override
    public int getBranchCount() {
        return getLineCount() + getTwoWindingsTransformerCount() + getTieLineCount();
    }

    @Override
    public Stream<Line> getLineStream() {
        return index.getAll(LineImpl.class).stream().map(Function.identity());
    }

    @Override
    public Stream<TieLine> getTieLineStream() {
        return index.getAll(TieLineImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getLineCount() {
        return index.getAll(LineImpl.class).size();
    }

    @Override
    public int getTieLineCount() {
        return index.getAll(TieLineImpl.class).size();
    }

    @Override
    public Line getLine(String id) {
        return index.get(id, LineImpl.class);
    }

    @Override
    public TieLine getTieLine(String id) {
        return index.get(id, TieLineImpl.class);
    }

    @Override
    public TieLineAdderImpl newTieLine() {
        return new TieLineAdderImpl(this);
    }

    @Override
    public TwoWindingsTransformerAdderImpl newTwoWindingsTransformer() {
        return new TwoWindingsTransformerAdderImpl(ref);
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return Collections.unmodifiableCollection(index.getAll(TwoWindingsTransformerImpl.class));
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return index.getAll(TwoWindingsTransformerImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        return index.getAll(TwoWindingsTransformerImpl.class).size();
    }

    @Override
    public TwoWindingsTransformer getTwoWindingsTransformer(String id) {
        return index.get(id, TwoWindingsTransformerImpl.class);
    }

    @Override
    public ThreeWindingsTransformerAdderImpl newThreeWindingsTransformer() {
        return new ThreeWindingsTransformerAdderImpl(ref);
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return Collections.unmodifiableCollection(index.getAll(ThreeWindingsTransformerImpl.class));
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return index.getAll(ThreeWindingsTransformerImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        return index.getAll(ThreeWindingsTransformerImpl.class).size();
    }

    @Override
    public ThreeWindingsTransformer getThreeWindingsTransformer(String id) {
        return index.get(id, ThreeWindingsTransformerImpl.class);
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return Collections.unmodifiableCollection(index.getAll(GeneratorImpl.class));
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return index.getAll(GeneratorImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getGeneratorCount() {
        return index.getAll(GeneratorImpl.class).size();
    }

    @Override
    public GeneratorImpl getGenerator(String id) {
        return index.get(id, GeneratorImpl.class);
    }

    @Override
    public Iterable<Battery> getBatteries() {
        return Collections.unmodifiableCollection(index.getAll(BatteryImpl.class));
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        return index.getAll(BatteryImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getBatteryCount() {
        return index.getAll(BatteryImpl.class).size();
    }

    @Override
    public BatteryImpl getBattery(String id) {
        return index.get(id, BatteryImpl.class);
    }

    @Override
    public Iterable<Load> getLoads() {
        return Collections.unmodifiableCollection(index.getAll(LoadImpl.class));
    }

    @Override
    public Stream<Load> getLoadStream() {
        return index.getAll(LoadImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getLoadCount() {
        return index.getAll(LoadImpl.class).size();
    }

    @Override
    public LoadImpl getLoad(String id) {
        return index.get(id, LoadImpl.class);
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return Collections.unmodifiableCollection(index.getAll(ShuntCompensatorImpl.class));
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return index.getAll(ShuntCompensatorImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getShuntCompensatorCount() {
        return index.getAll(ShuntCompensatorImpl.class).size();
    }

    @Override
    public ShuntCompensatorImpl getShuntCompensator(String id) {
        return index.get(id, ShuntCompensatorImpl.class);
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines(DanglingLineFilter danglingLineFilter) {
        return getDanglingLineStream(danglingLineFilter).collect(Collectors.toList());
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream(DanglingLineFilter danglingLineFilter) {
        return index.getAll(DanglingLineImpl.class).stream().filter(danglingLineFilter.getPredicate()).map(Function.identity());
    }

    @Override
    public int getDanglingLineCount() {
        return index.getAll(DanglingLineImpl.class).size();
    }

    @Override
    public DanglingLineImpl getDanglingLine(String id) {
        return index.get(id, DanglingLineImpl.class);
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return Collections.unmodifiableCollection(index.getAll(StaticVarCompensatorImpl.class));
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return index.getAll(StaticVarCompensatorImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getStaticVarCompensatorCount() {
        return index.getAll(StaticVarCompensatorImpl.class).size();
    }

    @Override
    public StaticVarCompensatorImpl getStaticVarCompensator(String id) {
        return index.get(id, StaticVarCompensatorImpl.class);
    }

    @Override
    public Switch getSwitch(String id) {
        return index.get(id, SwitchImpl.class);
    }

    @Override
    public Iterable<Switch> getSwitches() {
        return Collections.unmodifiableCollection(index.getAll(SwitchImpl.class));
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        return index.getAll(SwitchImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getSwitchCount() {
        return index.getAll(SwitchImpl.class).size();
    }

    @Override
    public BusbarSection getBusbarSection(String id) {
        return index.get(id, BusbarSectionImpl.class);
    }

    @Override
    public Iterable<BusbarSection> getBusbarSections() {
        return Collections.unmodifiableCollection(index.getAll(BusbarSectionImpl.class));
    }

    @Override
    public Stream<BusbarSection> getBusbarSectionStream() {
        return index.getAll(BusbarSectionImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getBusbarSectionCount() {
        return index.getAll(BusbarSectionImpl.class).size();
    }

    @Override
    public Bus getConfiguredBus(String id) {
        return index.get(id, ConfiguredBusImpl.class);
    }

    @Override
    public Iterable<Bus> getConfiguredBuses() {
        return Collections.unmodifiableCollection(index.getAll(ConfiguredBusImpl.class));
    }

    @Override
    public Stream<Bus> getConfiguredBusStream() {
        return index.getAll(ConfiguredBusImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getConfiguredBusCount() {
        return index.getAll(ConfiguredBusImpl.class).size();
    }

    @Override
    public AbstractHvdcConverterStation<?> getHvdcConverterStation(String id) {
        AbstractHvdcConverterStation<?> converterStation = getLccConverterStation(id);
        if (converterStation == null) {
            converterStation = getVscConverterStation(id);
        }
        return converterStation;
    }

    @Override
    public int getHvdcConverterStationCount() {
        return getLccConverterStationCount() + getVscConverterStationCount();
    }

    @Override
    public Iterable<HvdcConverterStation<?>> getHvdcConverterStations() {
        return Iterables.concat(getLccConverterStations(), getVscConverterStations());
    }

    @Override
    public Stream<HvdcConverterStation<?>> getHvdcConverterStationStream() {
        return Stream.concat(getLccConverterStationStream(), getVscConverterStationStream());
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return Collections.unmodifiableCollection(index.getAll(LccConverterStationImpl.class));
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return index.getAll(LccConverterStationImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getLccConverterStationCount() {
        return index.getAll(LccConverterStationImpl.class).size();
    }

    @Override
    public LccConverterStationImpl getLccConverterStation(String id) {
        return index.get(id, LccConverterStationImpl.class);
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return Collections.unmodifiableCollection(index.getAll(VscConverterStationImpl.class));
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return index.getAll(VscConverterStationImpl.class).stream().map(Function.identity());
    }

    @Override
    public int getVscConverterStationCount() {
        return index.getAll(VscConverterStationImpl.class).size();
    }

    @Override
    public VscConverterStationImpl getVscConverterStation(String id) {
        return index.get(id, VscConverterStationImpl.class);
    }

    @Override
    public HvdcLine getHvdcLine(String id) {
        return index.get(id, HvdcLineImpl.class);
    }

    @Override
    public HvdcLine getHvdcLine(HvdcConverterStation converterStation) {
        return getHvdcLineStream()
                .filter(l -> l.getConverterStation1() == converterStation || l.getConverterStation2() == converterStation)
                .findFirst()
                .orElse(null);
    }

    @Override
    public int getHvdcLineCount() {
        return index.getAll(HvdcLineImpl.class).size();
    }

    @Override
    public Iterable<HvdcLine> getHvdcLines() {
        return Collections.unmodifiableCollection(index.getAll(HvdcLineImpl.class));
    }

    @Override
    public Stream<HvdcLine> getHvdcLineStream() {
        return index.getAll(HvdcLineImpl.class).stream().map(Function.identity());
    }

    @Override
    public HvdcLineAdder newHvdcLine() {
        return new HvdcLineAdderImpl(ref);
    }

    @Override
    public Identifiable<?> getIdentifiable(String id) {
        return index.get(id, Identifiable.class);
    }

    @Override
    public Collection<Identifiable<?>> getIdentifiables() {
        return index.getAll();
    }

    @Override
    public <C extends Connectable> Iterable<C> getConnectables(Class<C> clazz) {
        return getConnectableStream(clazz).collect(Collectors.toList());
    }

    @Override
    public <C extends Connectable> Stream<C> getConnectableStream(Class<C> clazz) {
        return index.getAll().stream().filter(clazz::isInstance).map(clazz::cast);
    }

    @Override
    public <C extends Connectable> int getConnectableCount(Class<C> clazz) {
        return Ints.checkedCast(getConnectableStream(clazz).count());
    }

    @Override
    public Iterable<Connectable> getConnectables() {
        return getConnectables(Connectable.class);
    }

    @Override
    public Stream<Connectable> getConnectableStream() {
        return getConnectableStream(Connectable.class);
    }

    @Override
    public Connectable<?> getConnectable(String id) {
        return index.get(id, Connectable.class);
    }

    @Override
    public int getConnectableCount() {
        return Ints.checkedCast(getConnectableStream().count());
    }

    @Override
    public BusBreakerViewImpl getBusBreakerView() {
        return busBreakerView;
    }

    @Override
    public BusViewImpl getBusView() {
        return busView;
    }

    static final class ConnectedComponentsManager extends AbstractConnectedComponentsManager<ConnectedComponentImpl> {

        private final NetworkImpl network;

        private ConnectedComponentsManager(NetworkImpl network) {
            this.network = Objects.requireNonNull(network);
        }

        @Override
        protected Network getNetwork() {
            return network;
        }

        @Override
        protected void setComponentNumber(Bus bus, int num) {
            Objects.requireNonNull(bus);
            ((BusExt) bus).setConnectedComponentNumber(num);
        }

        @Override
        protected ConnectedComponentImpl createComponent(int num, int size) {
            return new ConnectedComponentImpl(num, size, network.ref);
        }
    }

    static final class SynchronousComponentsManager extends AbstractSynchronousComponentsManager<SynchronousComponentImpl> {

        private final NetworkImpl network;

        private SynchronousComponentsManager(NetworkImpl network) {
            this.network = Objects.requireNonNull(network);
        }

        @Override
        protected Network getNetwork() {
            return network;
        }

        @Override
        protected void setComponentNumber(Bus bus, int num) {
            Objects.requireNonNull(bus);
            ((BusExt) bus).setSynchronousComponentNumber(num);
        }

        @Override
        protected SynchronousComponentImpl createComponent(int num, int size) {
            return new SynchronousComponentImpl(num, size, network.ref);
        }
    }

    /**
     * Caching buses by their ID :
     * the cache is fully builts on first call to {@link BusCache#getBus(String)},
     * and must be invalidated on any topology change.
     */
    private static final class BusCache {

        private final Supplier<Stream<Bus>> busStream;
        private Map<String, Bus> cache;

        private BusCache(Supplier<Stream<Bus>> busStream) {
            this.busStream = busStream;
        }

        private void buildCache() {
            cache = busStream.get().collect(ImmutableMap.toImmutableMap(Bus::getId, Functions.identity()));
        }

        synchronized void invalidate() {
            cache = null;
        }

        private synchronized Map<String, Bus> getCache() {
            if (cache == null) {
                buildCache();
            }
            return cache;
        }

        Bus getBus(String id) {
            return getCache().get(id);
        }
    }

    private class VariantImpl implements Variant {

        private final ConnectedComponentsManager connectedComponentsManager
                = new ConnectedComponentsManager(NetworkImpl.this);

        private final SynchronousComponentsManager synchronousComponentsManager
                = new SynchronousComponentsManager(NetworkImpl.this);

        private final BusCache busViewCache = new BusCache(() -> getVoltageLevelStream().flatMap(vl -> vl.getBusView().getBusStream()));

        //For bus breaker view, we exclude bus breaker topologies from the cache,
        //because thoses buses are already indexed in the NetworkIndex
        private final BusCache busBreakerViewCache = new BusCache(() -> getVoltageLevelStream()
                .filter(vl -> vl.getTopologyKind() != TopologyKind.BUS_BREAKER)
                .flatMap(vl -> vl.getBusBreakerView().getBusStream()));

        @Override
        public VariantImpl copy() {
            return new VariantImpl();
        }

    }

    private final VariantArray<VariantImpl> variants;

    ConnectedComponentsManager getConnectedComponentsManager() {
        return variants.get().connectedComponentsManager;
    }

    SynchronousComponentsManager getSynchronousComponentsManager() {
        return variants.get().synchronousComponentsManager;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, final int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);

        variants.push(number, () -> variants.copy(sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);

        variants.pop(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);

        variants.delete(index);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, final int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);

        variants.allocate(indexes, () -> variants.copy(sourceIndex));
    }

    @Override
    protected String getTypeDescription() {
        return "Network";
    }

    @Override
    public void merge(Network other) {
        NetworkImpl otherNetwork = (NetworkImpl) other;

        // this check must not be done on the number of variants but on the size
        // of the internal variant array because the network can have only
        // one variant but an internal array with a size greater that one and
        // some re-usable variants
        if (variantManager.getVariantArraySize() != 1 || otherNetwork.variantManager.getVariantArraySize() != 1) {
            throw new PowsyblException("Merging of multi-variants network is not supported");
        }

        long start = System.currentTimeMillis();

        // if the validation level of other is lower than the current network's minimum validation level, we can not incorporate it
        if (other.getValidationLevel().compareTo(getMinValidationLevel()) < 0) {
            throw new PowsyblException("Network " + other.getNetwork() + " cannot be merged: its validation level " +
                    "is lower than the minimum acceptable validation level of network " + getId() + " (" +
                    other.getValidationLevel() + " < " + getMinValidationLevel() + ")");
        }
        // update the validation level (without recomputing it)
        this.validationLevel = ValidationLevel.min(this.getValidationLevel(), other.getValidationLevel());

        // check mergeability
        Multimap<Class<? extends Identifiable>, String> intersection = index.intersection(otherNetwork.index);
        for (Map.Entry<Class<? extends Identifiable>, Collection<String>> entry : intersection.asMap().entrySet()) {
            Class<? extends Identifiable> clazz = entry.getKey();
            Collection<String> objs = entry.getValue();
            if (!objs.isEmpty()) {
                throw new PowsyblException("The following object(s) of type "
                        + clazz.getSimpleName() + " exist(s) in both networks: "
                        + objs);
            }
        }

        // try to find dangling lines couples
        List<DanglingLinePair> lines = new ArrayList<>();
        Map<String, List<DanglingLine>> dl1byXnodeCode = new HashMap<>();

        for (DanglingLine dl1 : getDanglingLines(DanglingLineFilter.ALL)) {
            if (dl1.getUcteXnodeCode() != null) {
                dl1byXnodeCode.computeIfAbsent(dl1.getUcteXnodeCode(), k -> new ArrayList<>()).add(dl1);
            }
        }
        for (DanglingLine dl2 : Lists.newArrayList(other.getDanglingLines(DanglingLineFilter.ALL))) {
            findAndAssociateDanglingLines(dl2, dl1byXnodeCode::get, (dll1, dll2) -> pairDanglingLines(lines, dll1, dll2, dl1byXnodeCode));
        }

        // do not forget to remove the other network from its index!!!
        otherNetwork.index.remove(otherNetwork);

        // merge the indexes
        index.merge(otherNetwork.index);

        // fix network back reference of the other network objects
        otherNetwork.ref.setRef(ref);

        replaceDanglingLineByLine(lines);

        // update the source format
        if (!sourceFormat.equals(otherNetwork.sourceFormat)) {
            sourceFormat = "hybrid";
        }

        LOGGER.info("Merging of {} done in {} ms", id, System.currentTimeMillis() - start);
    }

    private void pairDanglingLines(List<DanglingLinePair> danglingLinePairs, DanglingLine dl1, DanglingLine dl2, Map<String, List<DanglingLine>> dl1byXnodeCode) {
        if (dl1 != null) {
            if (dl1.getUcteXnodeCode() != null) {
                dl1byXnodeCode.get(dl1.getUcteXnodeCode()).remove(dl1);
            }
            DanglingLinePair l = new DanglingLinePair();
            l.id = buildMergedId(dl1.getId(), dl2.getId());
            l.name = buildMergedName(dl1.getId(), dl2.getId(), dl1.getOptionalName().orElse(null), dl2.getOptionalName().orElse(null));
            l.dl1Id = dl1.getId();
            l.dl2Id = dl2.getId();
            l.aliases = new HashMap<>();
            // No need to merge properties or aliases because we keep the original dangling lines after merge
            danglingLinePairs.add(l);

            if (dl1.getId().equals(dl2.getId())) { // if identical IDs, rename dangling lines
                ((DanglingLineImpl) dl1).replaceId(l.dl1Id + "_1");
                ((DanglingLineImpl) dl2).replaceId(l.dl2Id + "_2");
                l.dl1Id = dl1.getId();
                l.dl2Id = dl2.getId();
            }
        }
    }

    private void replaceDanglingLineByLine(List<DanglingLinePair> lines) {
        for (DanglingLinePair danglingLinePair : lines) {
            LOGGER.debug("Creating tie line '{}' between dangling line couple '{}' and '{}",
                    danglingLinePair.id, danglingLinePair.dl1Id, danglingLinePair.dl2Id);
            TieLineImpl l = newTieLine()
                    .setId(danglingLinePair.id)
                    .setEnsureIdUnicity(true)
                    .setName(danglingLinePair.name)
                    .setDanglingLine1(danglingLinePair.dl1Id)
                    .setDanglingLine2(danglingLinePair.dl2Id)
                    .add();
            danglingLinePair.properties.forEach((key, val) -> l.setProperty(key.toString(), val.toString()));
            danglingLinePair.aliases.forEach((alias, type) -> {
                if (type.isEmpty()) {
                    l.addAlias(alias);
                } else {
                    l.addAlias(alias, type);
                }
            });
        }
    }

    class DanglingLinePair {
        String id;
        String name;
        String dl1Id;
        String dl2Id;
        Map<String, String> aliases;
        Properties properties = new Properties();
    }

    @Override
    public void merge(Network... others) {
        for (Network other : others) {
            merge(other);
        }
    }

    @Override
    public void addListener(NetworkListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(NetworkListener listener) {
        listeners.remove(listener);
    }

    @Override
    public ValidationLevel runValidationChecks() {
        return runValidationChecks(true);
    }

    @Override
    public ValidationLevel runValidationChecks(boolean throwsException) {
        return runValidationChecks(throwsException, Reporter.NO_OP);
    }

    @Override
    public ValidationLevel runValidationChecks(boolean throwsException, Reporter reporter) {
        Reporter readReporter = Objects.requireNonNull(reporter).createSubReporter("IIDMValidation", "Running validation checks on IIDM network " + id);
        validationLevel = ValidationUtil.validate(Collections.unmodifiableCollection(index.getAll()),
                true, throwsException, validationLevel != null ? validationLevel : minValidationLevel, readReporter);
        return validationLevel;
    }

    @Override
    public ValidationLevel getValidationLevel() {
        if (validationLevel == null) {
            validationLevel = ValidationUtil.validate(Collections.unmodifiableCollection(index.getAll()), false, false, minValidationLevel, Reporter.NO_OP);
        }
        return validationLevel;
    }

    @Override
    public Network setMinimumAcceptableValidationLevel(ValidationLevel validationLevel) {
        Objects.requireNonNull(validationLevel);
        if (this.validationLevel == null) {
            this.validationLevel = ValidationUtil.validate(Collections.unmodifiableCollection(index.getAll()), false, false, this.validationLevel, Reporter.NO_OP);
        }
        if (this.validationLevel.compareTo(validationLevel) < 0) {
            throw new ValidationException(this, "Network should be corrected in order to correspond to validation level " + validationLevel);
        }
        this.minValidationLevel = validationLevel;
        return this;
    }

    ValidationLevel getMinValidationLevel() {
        return minValidationLevel;
    }

    void setValidationLevelIfGreaterThan(ValidationLevel validationLevel) {
        if (this.validationLevel != null) {
            this.validationLevel = ValidationLevel.min(this.validationLevel, validationLevel);
        }
    }

    void invalidateValidationLevel() {
        if (minValidationLevel.compareTo(ValidationLevel.STEADY_STATE_HYPOTHESIS) < 0) {
            validationLevel = null;
        }
    }
}
