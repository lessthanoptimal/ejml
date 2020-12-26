/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml;

import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static org.ejml.RuntimeRegressionMasterApp.formatDate;

/**
 * Compares current runtime results against the most recent results and flags large changes
 *
 * @author Peter Abeles
 */
public class RuntimeRegressionSummaryApp {
    /** Stores the baseline results that are expected */
    public String baselineDirectory = "runtime_regression/baseline";

    /** Stores results from the most recent run that is being tested */
    public String currentDirectory = "runtime_regression/current";

    /** How long it took to process in milliseconds */
    public double processingTimeMS = 0.0;

    /** Tolerance used to decide if the difference in results are significant */
    public double significantFractionTol = 0.4;

    private final ParseBenchmarkCsv parseBaseline = new ParseBenchmarkCsv();
    private final ParseBenchmarkCsv parseCurrent = new ParseBenchmarkCsv();

    /** Results which were flagged */
    private @Getter final List<String> flagged = new ArrayList<>();
    /** Exception that occurred while processing */
    private @Getter final List<String> exceptions = new ArrayList<>();

    /** Total number of individual benchmarks compared */
    int countBenchmarks = 0;
    /** Number of files which were compared */
    int countFiles = 0;

    /**
     * Processes the two sets of results and identifies parsing exception and significant differences
     */
    public void process() {
        countBenchmarks = 0;
        countFiles = 0;
        flagged.clear();
        exceptions.clear();

        Set<String> setBaseline = loadResultsSet(baselineDirectory);
        Set<String> setCurrent = loadResultsSet(currentDirectory);

        // If it's known they are identical later on we can skip a check
        boolean identicalSets = setBaseline.size() == setCurrent.size();

        // Go through each benchmark and compare the results
        for (String benchmarkName : setBaseline) {
            if (!setCurrent.contains(benchmarkName)) {
                identicalSets = false;
                exceptions.add("Not in current: " + benchmarkName);
                continue;
            }

            if (!parse(parseBaseline, new File(baselineDirectory, benchmarkName)))
                continue;

            if (!parse(parseCurrent, new File(currentDirectory, benchmarkName)))
                continue;

            countFiles++;
            compareBenchmark(benchmarkName);
        }

        if (identicalSets)
            return;

        for (String benchmarkName : setCurrent) {
            if (!setBaseline.contains(benchmarkName))
                exceptions.add("Not in baseline: " + benchmarkName);
        }
    }

    /**
     * Creates a simple string which summarizes the results
     */
    public String createSummary() {
        String summary = "";
        summary += "EJML Runtime Regression\n\n";
        summary += String.format("%10s %10s %10s %10s\n", "files   ", "benchmarks", "flagged ", "exceptions");
        summary += String.format("%6s %10s %10s %10s\n",
                countFiles, countBenchmarks, flagged.size(), exceptions.size());
        summary += "\n";
        summary += String.format("Duration: %.2f hrs\n",(processingTimeMS/(1000.0*60.0*60.0)));
        summary += "Date:     " + formatDate(new Date()) + "\n";
        summary += "Version:  " + EjmlVersion.VERSION + "\n";
        summary += "SHA:      " + EjmlVersion.GIT_SHA + "\n";
        summary += "GIT_DATE: " + EjmlVersion.GIT_DATE + "\n";
        summary += "\n";
        summary += "java.runtime.version:  " + System.getProperty("java.runtime.version") + "\n";
        summary += "java.vendor:           " + System.getProperty("java.vendor") + "\n";
        summary += "os.name+arch:          " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + "\n";
        summary += "os.version:            " + System.getProperty("os.version") + "\n";
        if (!flagged.isEmpty()) {
            summary += "\nFlagged:\n";
            for (int i = 0; i < flagged.size(); i++) {
                summary += "  " + flagged.get(i) + "\n";
            }
        }

        if (!exceptions.isEmpty()) {
            summary += "\nExceptions:\n";
            for (int i = 0; i < exceptions.size(); i++) {
                summary += "  " + exceptions.get(i) + "\n";
            }
        }
        return summary;
    }

    private boolean parse( ParseBenchmarkCsv parser, File path ) {
        try {
            parser.parse(new FileInputStream(path));
            return true;
        } catch (IOException e) {
            exceptions.add("IOException reading " + path.getPath() + " : " + e.getMessage());
        }
        return false;
    }

    public Set<String> loadResultsSet( String pathToDirectory ) {
        Set<String> set = new HashSet<>();

        File file = new File(pathToDirectory);
        File[] children = file.listFiles();
        if (children == null)
            return set;

        for (int i = 0; i < children.length; i++) {
            File f = children[i];
            if (!f.isFile() || !f.getName().endsWith(".csv"))
                continue;
            set.add(f.getName());
        }

        return set;
    }

    /**
     * Compares results and looks for significant differences to flag
     */
    private void compareBenchmark( String benchmarkName ) {
        boolean identical = parseCurrent.results.size() == parseBaseline.results.size();
        for (String name : parseBaseline.mapResults.keySet()) {
            ParseBenchmarkCsv.Result baseline = parseBaseline.mapResults.get(name);
            ParseBenchmarkCsv.Result current = parseCurrent.mapResults.get(name);

            // Make sure it's in the current set too
            if (current == null) {
                identical = false;
                continue;
            }

            double b = baseline.getMilliSecondsPerOp();
            double c = current.getMilliSecondsPerOp();

            // Can't be negative or zero.
            if (b <= 0.0 || c <= 0.0) {
                exceptions.add("Impossible result: b=" + b + " c=" + c + " in " + baseline.getKey());
                continue;
            }

            if (b/c - 1.0 > significantFractionTol || c/b - 1.0 > significantFractionTol) {
                flagged.add(String.format("%5.1f%% %s", 100.0*c/b, benchmarkName + ":" + baseline.getKey()));
            }
            countBenchmarks++;
        }
        if (!identical)
            exceptions.add("Not identical: " + benchmarkName);
    }

    public static void main( String[] args ) {
        var app = new RuntimeRegressionSummaryApp();
        app.baselineDirectory = "benchmark_results/baseline";
        app.currentDirectory = "benchmark_results/current";
        app.process();
        System.out.println(app.createSummary());
    }
}
