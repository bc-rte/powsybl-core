/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.powsybl.action.dsl.Rule;
import com.powsybl.commons.io.table.AbstractTableFormatter;
import com.powsybl.commons.io.table.AsciiTableFormatter;
import com.powsybl.commons.io.table.Column;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.Security;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class LoadFlowActionSimulatorLogPrinter extends DefaultLoadFlowActionSimulatorObserver {

    private final PrintStream out;

    private final PrintStream err;

    private final boolean verbose;

    public LoadFlowActionSimulatorLogPrinter(PrintStream out, PrintStream err, boolean verbose) {
        this.out = Objects.requireNonNull(out);
        this.err = Objects.requireNonNull(err);
        this.verbose = verbose;
    }

    @Override
    public void beforePreContingencyAnalysis(RunningContext runningContext) {
        out.println("Starting pre-contingency analysis");
    }

    @Override
    public void beforePostContingencyAnalysis(RunningContext runningContext) {
        out.println("Starting post-contingency '" + runningContext.getContingency().getId() + "' analysis");
    }

    @Override
    public void roundBegin(RunningContext runningContext) {
        out.println("    Round " + runningContext.getRound());
    }

    @Override
    public void loadFlowDiverged(RunningContext runningContext) {
        out.println("    Load flow diverged");
    }

    @Override
    public void loadFlowConverged(RunningContext runningContext, List<LimitViolation> violations) {
        if (!violations.isEmpty()) {
            out.println("        Violations:");
            out.println(Security.printLimitsViolations(violations, runningContext.getNetwork(), LoadFlowActionSimulator.NO_FILTER));
        }
    }

    @Override
    public void ruleChecked(RunningContext runningContext, Rule rule, RuleEvaluationStatus status, Map<String, Object> variables, Map<String, Boolean> actions) {
        if (verbose || status == RuleEvaluationStatus.TRUE) {
            out.println("        Rule '" + rule.getId() + "' evaluated to " + status);
        }
        if (verbose && variables.size() + actions.size() > 0) {
            Writer writer = new OutputStreamWriter(out);
            try (AbstractTableFormatter formatter = new AsciiTableFormatter(writer, null,
                         new Column("Variable"),
                         new Column("Value"))) {
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    formatter.writeCell(entry.getKey());
                    formatter.writeCell(Objects.toString(entry.getValue()));
                }
                for (Map.Entry<String, Boolean> entry : actions.entrySet()) {
                    formatter.writeCell(entry.getKey() + ".actionTaken");
                    formatter.writeCell(entry.getValue());
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    public void beforeAction(RunningContext runningContext, String actionId) {
        out.println("        Applying action '" + actionId + "'");
    }

    @Override
    public void beforeTest(RunningContext runningContext, String actionId) {
        out.println("        Testing action '" + actionId + "'");
    }

    @Override
    public void noMoreViolations(RunningContext runningContext) {
        out.println("        No more violation");
    }

    @Override
    public void violationsAnymoreAndNoRulesMatch(RunningContext runningContext) {
        err.println("        Still some violations and no rule match...");
    }

    @Override
    public void maxIterationsReached(RunningContext runningContext) {
        out.println("        Max number of iterations reached");
    }
}
