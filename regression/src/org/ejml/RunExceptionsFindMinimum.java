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
import org.ejml.ParseBenchmarkCsv.Parameter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Re-runs specific benchmarks again several times in an attempt to find their true minimum. This is an attempt
 * to avoid false positives.
 *
 * @author Peter Abeles
 */
public class RunExceptionsFindMinimum extends JmhRunnerBase {
    /** Tolerance used to decide if the difference in results are significant */
    public double significantFractionTol = 0.4;

    /** The maximum number of times it will run a test and see if it's within tolerance */
    public int maxIterations = 10;

    /** Given the name give it the new results */
    @Getter private final Map<String, Double> nameToResults = new HashMap<>();

    /** Which benchmarks still fail */
    @Getter private final List<String> failedNames = new ArrayList<>();

    // Storage for results and the value it needs to match/beat
    private final List<BenchmarkInfo> benchmarks = new ArrayList<>();

    PrintStream logMinimum;

    public void addBenchmark( String name, double targetTimeMS ) {
        benchmarks.add(new BenchmarkInfo(name, targetTimeMS));
    }

    @Override protected void performBenchmarks() throws IOException {
        System.out.println("re-running benchmarks.size=" + benchmarks.size() + " tol=" + significantFractionTol);

        // print to stdout and to a file
        PrintStream logFileMinimum = new PrintStream(new File(outputDirectory, "minimum_search.txt"));
        OutputStream mirror = new MirrorStream(logFileMinimum, System.out);
        logMinimum = new PrintStream(mirror);

        // Record the settings for better visibility when debugging
        logMinimum.println("# significantFractionTol=" + significantFractionTol);
        logMinimum.println("# maxIterations=" + maxIterations);

        try {
            nameToResults.clear();
            failedNames.clear();

            // If there's nothing to process log that
            if (benchmarks.isEmpty()) {
                logMinimum.println("No exceptions to examine.");
                return;
            }

            findMinimums();
            processResults();
        } finally {
            logFileMinimum.close();
            logMinimum = null;
        }
    }

    /**
     * Re-runs benchmarks until it meets the target value or the maximum number of iterations has been exceeded
     */
    private void findMinimums() throws IOException {
        // Run benchmarks by finding automatically or by manually specifying them
        final var parseResults = new ParseBenchmarkCsv();
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            for (int i = benchmarks.size() - 1; i >= 0; i--) {
                BenchmarkInfo info = benchmarks.get(i);
                String benchmarkName = rerunBenchmark(info);
                parseResults.parse(new FileInputStream(new File(outputDirectory, benchmarkName + ".csv")));
                int matchingIndex = findResult(parseResults, info.path);
                double score = parseResults.results.get(matchingIndex).getMilliSecondsPerOp();

                // save the best score for later
                info.bestFound = Math.min(score, info.bestFound);

                // If it is equal to or better than the target score, remove it from the list
                double fractionalDifference = score/info.targetScore - 1.0;
                if (fractionalDifference < significantFractionTol) {
                    // Save the updated results
                    nameToResults.put(info.path, info.bestFound);

                    logMinimum.printf("Accepted: Trial=%2d score=%7.3f name=%s\n", iteration, fractionalDifference, info.path);
                    benchmarks.remove(i);
                } else {
                    logMinimum.printf("Rejected: Trial=%2d score=%7.3f name=%s\n", iteration, fractionalDifference, info.path);
                }
            }
        }
    }

    private String rerunBenchmark( BenchmarkInfo info ) {
        // Everything after : are the parameters
        String[] segments = info.path.split(",");
        String benchmarkName = segments[0];
        // Add the segments so that it can re-run the specific tests
        List<Parameter> parameters = new ArrayList<>();
        for (int segIdx = 1; segIdx < segments.length; segIdx++) {
            String[] words = segments[segIdx].split(":");
            Parameter p = new Parameter();
            p.name = words[0];
            p.value = words[1];
            parameters.add(p);
        }
        runBenchmark(benchmarkName, true, parameters);
        return benchmarkName;
    }

    /**
     * A benchmark will match all the parameters. for the benchmark + parameters that match
     */
    private int findResult( ParseBenchmarkCsv parseResults, String benchmarkName ) {
        int matchingIndex = -1;
        for (int idx = 0; idx < parseResults.results.size(); idx++) {
            ParseBenchmarkCsv.Result r = parseResults.results.get(idx);
            if (r.getKey().equals(benchmarkName)) {
                matchingIndex = idx;
            }
        }
        if (matchingIndex == -1)
            throw new RuntimeException("Could not find the result. results.size=" + parseResults.results.size());
        return matchingIndex;
    }

    /**
     * Looks at what remains, logs it, adds it to output map.
     */
    private void processResults() {
        if (benchmarks.isEmpty()) {
            System.out.println("No remaining exceptions");
        } else {
            for (BenchmarkInfo info : benchmarks) {
                // Save the updated results
                nameToResults.put(info.path, info.bestFound);
                failedNames.add(info.path);

                double fractionalDifference = info.bestFound/info.targetScore - 1.0;
                logMinimum.printf("Failure: score=%7.3f name=%s\n", fractionalDifference, info.path);
            }
        }
    }

    private static class BenchmarkInfo {
        public String path;
        public double targetScore;
        public double bestFound;

        public BenchmarkInfo( String path, double targetScore ) {
            this.path = path;
            this.targetScore = targetScore;
            this.bestFound = Double.MAX_VALUE;
        }
    }

    public static void main( String[] args ) {
        var app = new RunExceptionsFindMinimum();
        app.addBenchmark("org.ejml.dense.block.BenchmarkMatrixMult_DDRB.mult", 242.377049833333);
        app.process();
    }
}
