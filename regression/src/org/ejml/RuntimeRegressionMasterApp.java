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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Master application which calls all the other processes. It will run the regression, compare results, compute the
 * summary, then publish the summary.
 *
 * @author Peter Abeles
 */
public class RuntimeRegressionMasterApp {
    @Option(name = "--SummaryOnly", usage = "If true it will only print out the summary from last time it ran")
    boolean doSummaryOnly = false;

    @Option(name = "-e", aliases = {"--EmailPath"}, usage = "Path to email login. If relative, relative to project.")
    String emailPath = "email_login.txt";

    @Option(name = "-r", aliases = {"--ResultsPath"}, usage = "Path to results directory. If relative, relative to project.")
    String resultsPath = RunAllBenchmarksApp.BENCHMARK_RESULTS_DIR;

    @Option(name = "--Timeout", usage = "JMH Timeout in minutes")
    long timeoutMin = RunAllBenchmarksApp.DEFAULT_TIMEOUT_MIN;

    @Option(name = "-b", aliases = {"--Benchmark"}, handler = StringArrayOptionHandler.class,
            usage = "Used to specify a subset of benchmarks to run. Default is to run them all.")
    List<String> benchmarkNames = new ArrayList<>();

    public void performRegression() {
        long time0 = System.currentTimeMillis();
        resultsPath = GenerateCode32.projectRelativePath(resultsPath);
        emailPath = GenerateCode32.projectRelativePath(emailPath);
        var email = new EmailResults();
        email.loadEmailFile(new File(emailPath));

        File outputDirectory;

        if (doSummaryOnly) {
            outputDirectory = selectMostRecentResults();
        } else {
            var measure = new RunAllBenchmarksApp();
            measure.resultsDirectory = resultsPath;
            measure.timeoutMin = timeoutMin;
            measure.benchmarkNames = benchmarkNames;
            measure.process();
            outputDirectory = measure.outputDirectory;
        }
        System.out.println("Current Results: " + outputDirectory.getPath());

        File baselineDir = new File(resultsPath, "baseline");
        if (!baselineDir.exists()) {
            System.out.println("Baseline doesn't exist. Making current results the baseline");
            if (!outputDirectory.renameTo(baselineDir)) {
                System.err.println("Failed to rename current results to baseline");
            }
            if (email.emailDestination != null) {
                email.send("EJML Runtime Regression: Initialized", "Created new baseline");
            }
            return;
        }

        // Compare the benchmark results and summarize
        var summary = new RuntimeRegressionSummaryApp();
        summary.processingTimeMS = System.currentTimeMillis()-time0;
        summary.baselineDirectory = baselineDir.getPath();
        summary.currentDirectory = outputDirectory.getPath();
        summary.process();

        // Log results
        String subject = String.format("EJML Runtime Regression: Flagged %3d Exceptions=%3d",
                summary.getFlagged().size(),
                summary.getExceptions().size());

        String text = summary.createSummary();

        if (email.emailDestination != null) {
            email.send(subject, text);
        }

        try {
            System.out.println("Saving to "+new File(summary.currentDirectory, "summary.txt").getAbsolutePath());
            var writer = new PrintWriter(new File(summary.currentDirectory, "summary.txt"));
            writer.println(text);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
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
            if (!f.isDirectory() && !new File(f, "exceptions.txt").exists())
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
