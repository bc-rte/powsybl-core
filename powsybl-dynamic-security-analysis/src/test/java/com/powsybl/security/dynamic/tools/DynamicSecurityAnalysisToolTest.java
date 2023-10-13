/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.dynamic.tools;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.FileUtil;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationException;
import com.powsybl.computation.ComputationExceptionBuilder;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.dynamicsimulation.DynamicModelsSupplier;
import com.powsybl.dynamicsimulation.EventModelsSupplier;
import com.powsybl.dynamicsimulation.groovy.DynamicSimulationSupplierFactory;
import com.powsybl.iidm.network.ImportersLoaderList;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.*;
import com.powsybl.security.action.Action;
import com.powsybl.security.distributed.ExternalSecurityAnalysisConfig;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisInput;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisParameters;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisProvider;
import com.powsybl.security.dynamic.NetworkImporterMock;
import com.powsybl.security.dynamic.execution.DynamicSecurityAnalysisExecutionBuilder;
import com.powsybl.security.dynamic.execution.DynamicSecurityAnalysisExecutionInput;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessor;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessorFactory;
import com.powsybl.security.results.PreContingencyResult;
import com.powsybl.security.strategy.OperatorStrategy;
import com.powsybl.security.tools.SecurityAnalysisToolConstants;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolOptions;
import com.powsybl.tools.ToolRunningContext;
import com.powsybl.tools.test.AbstractToolTest;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class DynamicSecurityAnalysisToolTest extends AbstractToolTest {

    private static final String OUTPUT_LOG_FILENAME = "out.zip";

    private final String DYNAMIC_MODEL_FILENAME = "dynamicModel";

    private DynamicSecurityAnalysisTool tool;
    private ByteSource dynamicModels;
    private DynamicModelsSupplier dynamicModelsSupplier;


    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        tool = new DynamicSecurityAnalysisTool();
        Files.createFile(fileSystem.getPath("network.xml"));
        dynamicModels =  FileUtil.asByteSource(Files.write(fileSystem.getPath(DYNAMIC_MODEL_FILENAME), "test".getBytes()));
        dynamicModelsSupplier = DynamicSimulationSupplierFactory.createDynamicModelsSupplier(dynamicModels.openBufferedStream(), "DynamicSecurityAnalysisToolProviderMock");
    }

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(tool);
    }

    @Override
    public void assertCommand() {
        Command command = tool.getCommand();
        Options options = command.getOptions();
        assertCommand(command, "dynamic-security-analysis", 16, 2);
        assertOption(options, "case-file", true, true);
        assertOption(options, "dynamic-models-file", true, true);
        assertOption(options, "event-models-file", false, true);
        assertOption(options, "parameters-file", false, true);
        assertOption(options, "limit-types", false, true);
        assertOption(options, "output-file", false, true);
        assertOption(options, "output-format", false, true);
        assertOption(options, "contingencies-file", false, true);
        assertOption(options, "with-extensions", false, true);
        assertOption(options, "task-count", false, true);
        assertOption(options, "task", false, true);
        assertOption(options, "external", false, false);
        assertOption(options, "log-file", false, true);
        assertOption(options, "monitoring-file", false, true);
    }

    @Test
    void test() {
        assertCommand();
    }

    private static CommandLine mockCommandLine(Map<String, String> options, Set<String> flags) {
        CommandLine cli = mock(CommandLine.class);
        //when(cli.hasOption(anyString())).thenReturn(false);
       // when(cli.getOptionValue(anyString())).thenReturn(null);
        options.forEach((k, v) -> {
            when(cli.getOptionValue(k)).thenReturn(v);
            when(cli.hasOption(k)).thenReturn(true);
        });
        flags.forEach(f -> when(cli.hasOption(f)).thenReturn(true));
        when(cli.getOptionProperties(anyString())).thenReturn(new Properties());
        return cli;
    }

    private ToolOptions emptyOptions() {
        return mockOptions(Collections.emptyMap());
    }

    private ToolOptions mockOptions(Map<String, String> options) {
        return mockOptions(options, Collections.emptySet());
    }

    private ToolOptions mockOptions(Map<String, String> options, Set<String> flags) {
        return new ToolOptions(mockCommandLine(options, flags), fileSystem);
    }

    @Test
    void parseInputs() throws IOException {
        ToolOptions options = emptyOptions();

        DynamicSecurityAnalysisExecutionInput input = new DynamicSecurityAnalysisExecutionInput();
        DynamicSecurityAnalysisTool.updateInput(options, input);
        assertThat(input.getViolationTypes()).isEmpty();
        assertThat(input.getResultExtensions()).isEmpty();
        assertThat(input.getContingenciesSource()).isNotPresent();

        options = mockOptions(ImmutableMap.of(SecurityAnalysisToolConstants.LIMIT_TYPES_OPTION, "HIGH_VOLTAGE,CURRENT"));
        DynamicSecurityAnalysisTool.updateInput(options, input);
        assertThat(input.getViolationTypes()).containsExactly(LimitViolationType.CURRENT, LimitViolationType.HIGH_VOLTAGE);

        options = mockOptions(ImmutableMap.of(SecurityAnalysisToolConstants.WITH_EXTENSIONS_OPTION, "ext1,ext2"));
        DynamicSecurityAnalysisTool.updateInput(options, input);
        assertThat(input.getResultExtensions()).containsExactly("ext1", "ext2");

        parseOptionalFile(input, SecurityAnalysisToolConstants.CONTINGENCIES_FILE_OPTION, "contingencies", input::getContingenciesSource);
        parseOptionalFile(input, DynamicSecurityAnalysisToolConstants.EVENT_MODELS_FILE_OPTION, "eventModels", input::getEventModelsSource);
    }

    void parseOptionalFile(DynamicSecurityAnalysisExecutionInput input, String optionName, String fileName, Supplier<Optional<ByteSource>> getSource) throws IOException {
        ToolOptions invalidOptions = mockOptions(ImmutableMap.of(optionName, fileName));
        assertThatIllegalArgumentException().isThrownBy(() -> DynamicSecurityAnalysisTool.updateInput(invalidOptions, input));

        Files.write(fileSystem.getPath(fileName), "test".getBytes());
        ToolOptions options = mockOptions(ImmutableMap.of(optionName, fileName));
        DynamicSecurityAnalysisTool.updateInput(options, input);
        assertThat(getSource.get()).isPresent();
        if (getSource.get().isPresent()) {
            assertEquals("test", new String(getSource.get().get().read()));
        } else {
            fail();
        }
    }

    ToolOptions parseFile(DynamicSecurityAnalysisExecutionInput input, String optionName, String fileName, Supplier<ByteSource> getSource) throws IOException {
        ToolOptions invalidOptions = mockOptions(ImmutableMap.of(optionName, fileName));
        assertThatIllegalArgumentException().isThrownBy(() -> DynamicSecurityAnalysisTool.updateInput(invalidOptions, input));

        Files.write(fileSystem.getPath(fileName), "test".getBytes());
        ToolOptions options = mockOptions(ImmutableMap.of(optionName, fileName));
        DynamicSecurityAnalysisTool.updateInput(options, input);
        assertNotNull(getSource.get());
        assertEquals("test", new String(getSource.get().read()));
        return options;
    }

    @Test
    void buildPreprocessedInput() throws IOException {
        DynamicSecurityAnalysisExecutionInput executionInput = new DynamicSecurityAnalysisExecutionInput()
                .setDynamicModelsSource(dynamicModels)
                .setNetworkVariant(mock(Network.class), "")
                .setParameters(new DynamicSecurityAnalysisParameters());

        SecurityAnalysisPreprocessor preprocessor = mock(SecurityAnalysisPreprocessor.class);
        SecurityAnalysisPreprocessorFactory factory = mock(SecurityAnalysisPreprocessorFactory.class);
        when(factory.newPreprocessor(any())).thenReturn(preprocessor);

        DynamicSecurityAnalysisInput input = DynamicSecurityAnalysisTool.buildPreprocessedInput(executionInput, "", LimitViolationFilter::new, factory);

        assertSame(executionInput.getParameters(), input.getParameters());
        assertSame(executionInput.getNetworkVariant(), input.getNetworkVariant());

        verify(factory, times(0)).newPreprocessor(any());

        executionInput.setContingenciesSource(ByteSource.empty());
        DynamicSecurityAnalysisTool.buildPreprocessedInput(executionInput, "", LimitViolationFilter::new, factory);

        verify(factory, times(1)).newPreprocessor(any());
        verify(preprocessor, times(1)).preprocess(any());
    }

    @Test
    void readNetwork() throws IOException {
        ToolRunningContext context = new ToolRunningContext(mock(PrintStream.class), mock(PrintStream.class), fileSystem,
                mock(ComputationManager.class), mock(ComputationManager.class));

        CommandLine cli = mockCommandLine(ImmutableMap.of("case-file", "network.xml"), Collections.emptySet());
        DynamicSecurityAnalysisTool.readNetwork(cli, context, new ImportersLoaderList(new NetworkImporterMock()));
    }

    @Test
    void testRunWithLog() throws Exception {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             ByteArrayOutputStream berr = new ByteArrayOutputStream();
             PrintStream out = new PrintStream(bout);
             PrintStream err = new PrintStream(berr);
             ComputationManager cm = mock(ComputationManager.class)) {
            CommandLine cl = mockCommandLine(ImmutableMap.of("case-file", "network.xml",
                    DynamicSecurityAnalysisToolConstants.DYNAMIC_MODELS_FILE_OPTION, DYNAMIC_MODEL_FILENAME,
                    SecurityAnalysisToolConstants.OUTPUT_LOG_OPTION, OUTPUT_LOG_FILENAME),
                    ImmutableSet.of("skip-postproc"));

            ToolRunningContext context = new ToolRunningContext(out, err, fileSystem, cm, cm);

            DynamicSecurityAnalysisExecutionBuilder builderRun = new DynamicSecurityAnalysisExecutionBuilder(ExternalSecurityAnalysisConfig::new,
                    "DynamicSecurityAnalysisToolProviderMock",
                    (executionInput, providerName) -> new DynamicSecurityAnalysisInput(executionInput.getNetworkVariant(), dynamicModelsSupplier));

            // Check runWithLog execution
            tool.run(cl, context, builderRun,
                    DynamicSecurityAnalysisParameters::new,
                    new ImportersLoaderList(new NetworkImporterMock()),
                    TableFormatterConfig::new);
            // Check log-file creation
            Path logPath = context.getFileSystem().getPath(OUTPUT_LOG_FILENAME);
            assertTrue(Files.exists(logPath));
            // Need to clean for next test
            Files.delete(logPath);

            // Check run execution
            when(cl.hasOption("log-file")).thenReturn(false);

            tool.run(cl, context, builderRun,
                    DynamicSecurityAnalysisParameters::new,
                    new ImportersLoaderList(new NetworkImporterMock()),
                    TableFormatterConfig::new);

            // Check no log-file creation
            assertFalse(Files.exists(logPath));

            // exception happens
            DynamicSecurityAnalysisExecutionBuilder builderException = new DynamicSecurityAnalysisExecutionBuilder(ExternalSecurityAnalysisConfig::new,
                    "DynamicSecurityAnalysisToolExceptionProviderMock",
                    (executionInput, providerName) -> new DynamicSecurityAnalysisInput(executionInput.getNetworkVariant(), dynamicModelsSupplier));

            try {
                tool.run(cl, context, builderException,
                        DynamicSecurityAnalysisParameters::new,
                        new ImportersLoaderList(new NetworkImporterMock()),
                        TableFormatterConfig::new);
                fail();
            } catch (CompletionException exception) {
                assertTrue(exception.getCause() instanceof ComputationException);
                assertEquals("outLog", ((ComputationException) exception.getCause()).getOutLogs().get("out"));
                assertEquals("errLog", ((ComputationException) exception.getCause()).getErrLogs().get("err"));
            }
        }
    }

    @Test
    void testRunWithBuilderCreation() throws Exception {

        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             ByteArrayOutputStream berr = new ByteArrayOutputStream();
             PrintStream out = new PrintStream(bout);
             PrintStream err = new PrintStream(berr);
             ComputationManager cm = mock(ComputationManager.class)) {

            CommandLine cl = mockCommandLine(ImmutableMap.of("case-file", "network.xml",
                    DynamicSecurityAnalysisToolConstants.DYNAMIC_MODELS_FILE_OPTION, "groovy",
                    SecurityAnalysisToolConstants.OUTPUT_LOG_OPTION, OUTPUT_LOG_FILENAME), ImmutableSet.of("skip-postproc"));

            ToolRunningContext context = new ToolRunningContext(out, err, fileSystem, cm, cm);

            PowsyblException e = assertThrows(PowsyblException.class, () -> tool.run(cl, context));
            assertTrue(e.getMessage().startsWith("Property ContingenciesProviderFactory is not set"));
        }
    }

    @AutoService(DynamicSecurityAnalysisProvider.class)
    public static class DynamicSecurityAnalysisProviderMock implements DynamicSecurityAnalysisProvider {
        @Override
        public CompletableFuture<SecurityAnalysisReport> run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, String workingVariantId, LimitViolationDetector detector, LimitViolationFilter filter, ComputationManager computationManager, DynamicSecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider, List<SecurityAnalysisInterceptor> interceptors, List<OperatorStrategy> operatorStrategies, List<Action> actions, List<StateMonitor> monitors, Reporter reporter) {
            CompletableFuture<SecurityAnalysisReport> cfSar = mock(CompletableFuture.class);
            SecurityAnalysisReport report = mock(SecurityAnalysisReport.class);
            when(report.getResult()).thenReturn(mock(SecurityAnalysisResult.class));
            when(report.getResult().getPreContingencyResult()).thenReturn(mock(PreContingencyResult.class));
            when(report.getResult().getPreContingencyLimitViolationsResult()).thenReturn(mock(LimitViolationsResult.class));
            when(report.getLogBytes()).thenReturn(Optional.of("Hello world".getBytes()));
            when(cfSar.join()).thenReturn(report);
            return cfSar;
        }

        @Override
        public String getName() {
            return "DynamicSecurityAnalysisToolProviderMock";
        }

        @Override
        public String getVersion() {
            return "1.0";
        }
    }

    @AutoService(DynamicSecurityAnalysisProvider.class)
    private static class DynamicSecurityAnalysisExceptionProviderMock implements DynamicSecurityAnalysisProvider {
        @Override
        public CompletableFuture<SecurityAnalysisReport> run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, String workingVariantId, LimitViolationDetector detector, LimitViolationFilter filter, ComputationManager computationManager, DynamicSecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider, List<SecurityAnalysisInterceptor> interceptors, List<OperatorStrategy> operatorStrategies, List<Action> actions, List<StateMonitor> monitors, Reporter reporter) {
            ComputationExceptionBuilder ceb = new ComputationExceptionBuilder(new RuntimeException("test"));
            ceb.addOutLog("out", "outLog")
                    .addErrLog("err", "errLog");
            ComputationException computationException = ceb.build();
            throw new CompletionException(computationException);
        }

        @Override
        public String getName() {
            return "DynamicSecurityAnalysisToolExceptionProviderMock";
        }

        @Override
        public String getVersion() {
            return "1.0";
        }
    }
}
