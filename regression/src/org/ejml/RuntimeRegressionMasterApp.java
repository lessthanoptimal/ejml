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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Master application which calls all the other processes. It will run the regression, compare results, compute the
 * summary, then publish the summary.
 *
 * @author Peter Abeles
 */
public class RuntimeRegressionMasterApp {
    // name of directory with JMH results
    public static final String JMH_DIR = "jmh";
    // Storage for a file with combined benchmark results from everything
    public static final String ALL_BENCHMARKS_FILE = "all_benchmarks.csv";
    // Simplified summary of benchmark results
    public static final String SUMMARY_FILE = "summary.txt";
    // Results that get caught by the master application
    public static final String MASTER_EXCEPTIONS_FILE = "master_error_log.txt";

    @Option(name = "--SummaryOnly", usage = "If true it will only print out the summary from last time it ran")
    boolean doSummaryOnly = false;

    @Option(name = "--MinimumOnly", usage =
            "If true, it shouldn't compute main JMH results, but should re-run minimum finding")
    boolean doMinimumOnly = false;

    @Option(name = "-e", aliases = {"--EmailPath"}, usage = "Path to email login. If relative, relative to project.")
    String emailPath = "email_login.txt";

    @Option(name = "-r", aliases = {"--ResultsPath"}, usage = "Path to results directory. If relative, relative to project.")
    String resultsPath = RunAllBenchmarksApp.BENCHMARK_RESULTS_DIR;

    @Option(name = "--Timeout", usage = "JMH Timeout in minutes")
    long timeoutMin = RunAllBenchmarksApp.DEFAULT_TIMEOUT_MIN;

    @Option(name = "-b", aliases = {"--Benchmark"}, handler = StringArrayOptionHandler.class,
            usage = "Used to specify a subset of benchmarks to run. Default is to run them all.")
    List<String> benchmarkNames = new ArrayList<>();

    /** Tolerance used to decide if the difference in results are significant */
    public double significantFractionTol = 0.1;

    // Log error messages
    protected PrintStream logStderr;

    public void performRegression() {
        long startTime = System.currentTimeMillis();
        resultsPath = GenerateCode32.projectRelativePath(resultsPath);
        emailPath = GenerateCode32.projectRelativePath(emailPath);
        var email = new EmailResults();
        email.loadEmailFile(new File(emailPath));

        // Little bit of a hack, but if we want to just run the minimum search then this is the easiest way to do it
        if (doMinimumOnly)
            doSummaryOnly = true;

        File currentResultsDir;

        if (doSummaryOnly) {
            currentResultsDir = selectMostRecentResults();
        } else {
            currentResultsDir = new File(resultsPath,System.currentTimeMillis()+"");
            var measure = new RunAllBenchmarksApp();
            measure.outputRelativePath = new File(currentResultsDir,JMH_DIR).getPath();
            measure.timeoutMin = timeoutMin;
            measure.userBenchmarkNames = benchmarkNames;
            measure.process();
        }
        System.out.println("Current Results: " + currentResultsDir.getPath());
        File currentJmhDir = new File(currentResultsDir,"jmh");

        try {
            logStderr = new PrintStream(new FileOutputStream(new File(currentResultsDir,MASTER_EXCEPTIONS_FILE)));

            File baselineDir = new File(resultsPath, "baseline");

            // See if the baseline directory needs to be created
            if (!baselineDir.exists()) {
                createBaselineDirectory(email, currentResultsDir, baselineDir);
                return;
            }

            // Find the initial set of exceptions and attempt to determine if they are false positives
            Map<String, Double> currentResults, baselineResults;

            if (doMinimumOnly || !doSummaryOnly) {
                // Load JMH results
                currentResults = RuntimeRegressionUtils.loadJmhResults(currentJmhDir);
                baselineResults = RuntimeRegressionUtils.loadJmhResults(new File(baselineDir,JMH_DIR));

                // find exceptions and re-run them to see if they are false positives
                rerunFailedRegressionTests(currentResultsDir, currentResults, baselineResults);

                // Save all the combined results to a file
                RuntimeRegressionUtils.saveAllBenchmarks(currentResults,
                        new File(currentResultsDir, ALL_BENCHMARKS_FILE).getPath());
            } else {
                // Load previously saved results
                currentResults = checkLoadAllBenchmarks(currentResultsDir);
                baselineResults = checkLoadAllBenchmarks(baselineDir);
            }

            createSummary(email, currentResultsDir, currentResults, baselineResults,
                    System.currentTimeMillis()-startTime);
        } catch (IOException e) {
            e.printStackTrace(logStderr);
        } finally {
            if (logStderr!=null) logStderr.close();
        }

        logStderr = null;
    }

    /**
     * Convert the current results directory into the baseline directory that will be compared against
     */
    private void createBaselineDirectory( EmailResults email, File currentResultsDir, File baselineDir )
            throws IOException {
        System.out.println("Baseline doesn't exist. Making current results the baseline");
        if (!currentResultsDir.renameTo(baselineDir)) {
            logStderr.println("Failed to rename current results to baseline");
        }

        // Save the summary results
        Map<String, Double> results = RuntimeRegressionUtils.loadJmhResults(new File(baselineDir,JMH_DIR));
        RuntimeRegressionUtils.saveAllBenchmarks(results,
                new File(baselineDir, ALL_BENCHMARKS_FILE).getPath());

        if (email.emailDestination != null) {
            email.send("EJML Runtime Regression: Initialized", "Created new baseline");
        }
    }

    /**
     * If available, load the summary results
     */
    private static Map<String, Double> checkLoadAllBenchmarks(File directory) {
        File file = new File(directory, ALL_BENCHMARKS_FILE);
        if (!file.exists())
            return new HashMap<>();

        return RuntimeRegressionUtils.loadAllBenchmarks(file);
    }

    /**
     * Re-reun regression tests to see if they are false positives
     */
    private void rerunFailedRegressionTests( File currentResultsDir,
                                             Map<String, Double> currentResults,
                                             Map<String, Double> baselineResults ) {
        Set<String> exceptions = RuntimeRegressionUtils.findRuntimeExceptions(
                baselineResults, currentResults, significantFractionTol);

        var findMinimum = new RunExceptionsFindMinimum();
        findMinimum.outputRelativePath = new File(currentResultsDir,"minimum").getPath();
        findMinimum.significantFractionTol = significantFractionTol;
        for (String name : exceptions) {
            findMinimum.addBenchmark(name, baselineResults.get(name));
        }
        findMinimum.process();

        // Update the results with latest times and updated list of exceptions
        for (String name : exceptions) {
            currentResults.put(name, findMinimum.getNameToResults().get(name));
        }
        exceptions.clear();
        exceptions.addAll(findMinimum.getFailedNames());
    }

    private void createSummary(EmailResults email, File currentDirectory,
                               Map<String, Double> current, Map<String, Double> baseline, long elapsedTime) {
        // Compare the benchmark results and summarize
        var summary = new RuntimeRegressionSummary();
        summary.significantFractionTol = significantFractionTol;
        summary.processingTimeMS = elapsedTime;
        summary.process(current, baseline);

        // Log results
        String subject = String.format("EJML Runtime Regression: Degraded %3d Improved %d Exceptions %3d",
                summary.getDegraded().size(),
                summary.getImproved().size(),
                summary.getExceptions().size());

        String text = summary.createSummary();

        if (email.emailDestination != null) {
            email.send(subject, text);
        }

        try {
            System.out.println("Saving to "+new File(currentDirectory, SUMMARY_FILE).getAbsolutePath());
            var writer = new PrintWriter(new File(currentDirectory, SUMMARY_FILE));
            writer.println(text);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace(logStderr);
            logStderr.flush();
        }

        System.out.println(text);
    }

    /**
     * Selects the valid results directory which the highest name
     */
    public File selectMostRecentResults() {
        File directory = new File(resultsPath);
        File[] children = directory.listFiles();
        if (children == null)
            throw new RuntimeException("Results path is empty");
        File selected = null;
        for (int i = 0; i < children.length; i++) {
            File f = children[i];
            if (!f.isDirectory() && !new File(f, MASTER_EXCEPTIONS_FILE).exists())
                continue;
            if (f.getName().equals("baseline"))
                continue;
            if (selected == null || f.getName().compareTo(selected.getName()) > 0)
                selected = f;
        }
        if (selected == null)
            throw new RuntimeException("No valid results");
        return selected;
    }

    public static String formatDate( Date date ) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
        return dateFormat.format(date);
    }

    public static void main( String[] args ) {
        RuntimeRegressionMasterApp regression = new RuntimeRegressionMasterApp();
        CmdLineParser parser = new CmdLineParser(regression);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            parser.getProperties().withUsageWidth(120);
            parser.printUsage(System.out);
            return;
        }

        regression.performRegression();
    }
}
