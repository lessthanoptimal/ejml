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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.ejml.GenerateCode32.projectRelativePath;
import static org.ejml.RuntimeRegressionMasterApp.formatDate;

/**
 * Creates a new baseline results set by running the regression several times and selecting the best result
 * for each benchmark.
 *
 * @author Peter Abeles
 **/
public class CreateRuntimeRegressionBaseline {
    /** The maximum number of times it will run a test and see if it's within tolerance */
    public int maxIterations = 10;

    /** Path to output directory relative to project base */
    public String outputRelativePath = "tmp";

    /** If true it won't run benchmarks again but will only combine existing results */
    public boolean combineOnly = false;

    /** How long it took to run all the benchmarks in hours */
    private @Getter double timeBenchmarkHrs;
    /** How long it took to combine all the results in milliseconds */
    private @Getter double timeCombineMS;

    public final RunAllRuntimeBenchmarks benchmark = new RunAllRuntimeBenchmarks();

    /** Given the name give it the new results */
    @Getter private final Map<String, Double> nameToResults = new HashMap<>();

    protected PrintStream logTiming;

    /**
     * Runs the benchmark set several times, finds the best times for each benchmark, save results
     */
    public void process() {
        nameToResults.clear();

        // Path to the main directory all results are saved inside of
        final File homeDirectory = new File(projectRelativePath(outputRelativePath));
        if (!homeDirectory.exists()) {
            if (!homeDirectory.mkdirs())
                System.err.println("Can't create home directory. " + homeDirectory.getPath());
        }

        try {
            logTiming = new PrintStream(new FileOutputStream(new File(homeDirectory, "time.txt")));

            // Save info about what is being computed
            RuntimeRegressionUtils.saveSystemInfo(homeDirectory, System.out);

            long time0 = System.currentTimeMillis();
            // Compute all the results. This will take a while
            if (!combineOnly) {
                for (int trial = 0; trial < maxIterations; trial++) {
                    // Save the start time of each trial
                    logTiming.printf("trial%-2d  %s\n", trial, formatDate(new Date()));
                    logTiming.flush();
                    benchmark.outputRelativePath = outputRelativePath + "/" + "trial" + trial;
                    benchmark.process();
                    System.out.print("\n\nFinished Trial " + trial + "\n\n");
                }
            }
            long time1 = System.currentTimeMillis();
            timeBenchmarkHrs = (time1 - time0)/(double)(1000*60*60);

            // Load results and for each benchmark find the best result across all the trials
            combineTrialResults(homeDirectory);
            long time2 = System.currentTimeMillis();
            timeCombineMS = time2 - time1;

            // Save the results
            File file = new File(projectRelativePath(outputRelativePath),
                    RuntimeRegressionMasterApp.ALL_BENCHMARKS_FILE);
            RuntimeRegressionUtils.saveAllBenchmarks(nameToResults, file.getPath());

            // Save information on how long it took to compute
            logTiming.println();
            logTiming.printf("Benchmarks:  %.2f hrs\n", timeBenchmarkHrs);
            logTiming.printf("Combine:     %.2f ms\n", timeCombineMS);
            logTiming.println();
            logTiming.println("Finished:    " + formatDate(new Date()));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            logTiming.close();
        }
    }

    private void combineTrialResults( File homeDirectory ) throws IOException {

        // List of all directories containing the results
        String[] directories = homeDirectory.list(( current, name ) ->
                new File(current, name).isDirectory() && name.startsWith("trial"));

        System.out.println("Matching directories=" + directories.length);

        for (int idx = 0; idx < directories.length; idx++) {
            File trialDir = new File(homeDirectory, directories[idx]);
            Map<String, Double> results = RuntimeRegressionUtils.loadJmhResults(trialDir);
            for (var e : results.entrySet()) {
                if (nameToResults.containsKey(e.getKey())) {
                    double current = nameToResults.get(e.getKey());
                    double found = e.getValue();
                    if (found < current)
                        nameToResults.put(e.getKey(), found);
                } else {
                    nameToResults.put(e.getKey(), e.getValue());
                }
            }
        }
    }
}
