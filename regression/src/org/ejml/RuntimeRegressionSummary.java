/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.DGrowArray;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.ejml.RuntimeRegressionMasterApp.formatDate;

/**
 * Compares current runtime results against the most recent results and flags large changes
 *
 * @author Peter Abeles
 */
public class RuntimeRegressionSummary {
    /** Input: How long it took to process in milliseconds */
    public double processingTimeMS = 0.0;

    /** Tolerance used to decide if the difference in results are significant */
    public double significantFractionTol = 0.4;

    /** Benchmarks that got worse */
    private @Getter final List<String> degraded = new ArrayList<>();
    /** Benchmarks that got better */
    private @Getter final List<String> improved = new ArrayList<>();
    /** Exception that occurred while processing */
    private @Getter final List<String> exceptions = new ArrayList<>();

    /** Total number of individual benchmarks compared */
    int countBenchmarks = 0;
    /** Number of files which were compared */
    int countFiles = 0;

    // Contains all the errors for computing summary metrics
    private final DGrowArray allErrors = new DGrowArray();

    /**
     * Processes the two sets of results and identifies parsing exception and significant differences
     */
    public void process(Map<String, Double> current, Map<String, Double> baseline) {
        countBenchmarks = 0;
        countFiles = 0;
        degraded.clear();
        improved.clear();
        exceptions.clear();
        allErrors.reset();

        // List of unique files found
        Set<String> uniqueFiles = new HashSet<>();

        // If it's known they are identical later on we can skip a check
        boolean identical = baseline.size() == current.size();

        // Go through each benchmark and compare the results
        for (String name : baseline.keySet()) {
            if (!current.containsKey(name)) {
                identical = false;
                exceptions.add("Not in current: " + name);
                continue;
            }

            double c = current.get(name);
            double b = baseline.get(name);

            // Can't be negative or zero.
            if (b <= 0.0 || c <= 0.0) {
                exceptions.add("Impossible result: b=" + b + " c=" + c + " in " + name);
                continue;
            }

            double fractionalError = Math.max(b/c - 1.0, c/b - 1.0);
            allErrors.add(fractionalError);

            if (fractionalError > significantFractionTol) {
                String message = String.format("%-5.1f%% %s", 100.0*c/b, name);
                if (c>b) {
                    degraded.add(message);
                } else {
                    improved.add(message);
                }
            }
            String path = name.substring(0,name.lastIndexOf("."));
            uniqueFiles.add(path);
            countBenchmarks++;
        }
        countFiles = uniqueFiles.size();

        if (identical)
            return;

        for (String benchmarkName : current.keySet()) {
            if (!baseline.containsKey(benchmarkName))
                exceptions.add("Not in baseline: " + benchmarkName);
        }
    }

    /**
     * Creates a simple string which summarizes the results
     */
    public String createSummary() {
        String summary = "";
        summary += "EJML Runtime Regression\n\n";
        summary += String.format("%10s %10s %10s %10s %10s\n",
                "files   ", "benchmarks", "degraded","improved", "exceptions");
        summary += String.format("%6s %10s %10s %10s %10s\n",
                countFiles, countBenchmarks, degraded.size(), improved.size(), exceptions.size());
        summary += "\n";
        summary += String.format("Duration: %.2f hrs\n", (processingTimeMS/(1000.0*60.0*60.0)));
        summary += "Date:     " + formatDate(new Date()) + "\n";
        summary += "Version:  " + EjmlVersion.VERSION + "\n";
        summary += "SHA:      " + EjmlVersion.GIT_SHA + "\n";
        summary += "GIT_DATE: " + EjmlVersion.GIT_DATE + "\n";
        summary += "\n";
        summary += String.format("Significant: %.1f%%\n",significantFractionTol*100.0);
        summary += "\n";
        summary += "java.runtime.version:  " + System.getProperty("java.runtime.version") + "\n";
        summary += "java.vendor:           " + System.getProperty("java.vendor") + "\n";
        summary += "os.name+arch:          " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + "\n";
        summary += "os.version:            " + System.getProperty("os.version") + "\n";

        Arrays.sort(allErrors.data,0,allErrors.length);
        double error50 = 100.0*allErrors.get((int)(allErrors.length*0.5));
        double error75 = 100.0*allErrors.get((int)(allErrors.length*0.75));
        double error90 = 100.0*allErrors.get((int)(allErrors.length*0.90));
        double error97 = 100.0*allErrors.get((int)(allErrors.length*0.97));
        double error99 = 100.0*allErrors.get((int)(allErrors.length*0.99));

        summary += String.format("\nError Summary: 50%% %5.1f, 75%% %5.1f, 90%% %5.1f, 97%% %5.1f, 99%% %5.1f\n",
                error50,error75,error90,error97,error99);


        if (!degraded.isEmpty()) {
            summary += "\nDegraded:\n";
            for (int i = 0; i < degraded.size(); i++) {
                summary += "  " + degraded.get(i) + "\n";
            }
        }

        if (!improved.isEmpty()) {
            summary += "\nImproved:\n";
            for (int i = 0; i < improved.size(); i++) {
                summary += "  " + improved.get(i) + "\n";
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

    public static void main( String[] args ) throws IOException {
        File pathCurrent = new File("runtime_regression/1610474495164");
        File pathBaseline = new File("runtime_regression/baseline");

        Map<String, Double> current = RuntimeRegressionUtils.loadJmhResults(pathCurrent);
        Map<String, Double> baseline = RuntimeRegressionUtils.loadJmhResults(pathBaseline);

        var app = new RuntimeRegressionSummary();
        app.process(current, baseline);
        System.out.println(app.createSummary());
    }
}
