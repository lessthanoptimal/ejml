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

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.*;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Runs all JMH benchmarks and saves the results plus exceptions.
 *
 * NOTE: This finds benchmarks by scanning the source code and not by using reflections to scan classes. This is
 * preferable since if a new module is added or re-named unless it's updated correctly in this build.gradle it
 * will silently fail by skipping those benchmarks.
 */
public class RunAllBenchmarksApp {

    public static String BENCHMARK_RESULTS_DIR = "benchmark_results";
    public static long DEFAULT_TIMEOUT_MIN = 3;

    /**
     * How long a single JMH test has before it times out. This should be kept fairly small since this is designed
     * to catch regressions not evaluate performance on large datasets
     */
    public long timeoutMin = DEFAULT_TIMEOUT_MIN;

    String[] blackList = new String[]{"ejml-experimental"};

    public String resultsDirectory = BENCHMARK_RESULTS_DIR;

    // Directory it saved results too
    public File outputDirectory;

    // Print streams to different files
    PrintStream logExceptions;
    PrintStream logRuntimes;
    PrintStream logStderr;

    /**
     * Searches for all benchmarks, runs them, and saves the results while logging exceptions
     */
    public void process() {
        PrintStream stderr = System.err;
        logExceptions = null;
        logRuntimes = null;
        logStderr = null;
        try {
            long time0 = System.currentTimeMillis();
            String pathToMain = GenerateCode32.projectRelativePath("main");

            outputDirectory = new File(GenerateCode32.projectRelativePath(resultsDirectory), System.currentTimeMillis() + "");
            if (!outputDirectory.exists()) {
                if (!outputDirectory.mkdirs()) {
                    throw new UncheckedIOException(new IOException("Failed to mkdirs output: " + outputDirectory.getPath()));
                }
            }
            System.out.println("Output Directory: " + outputDirectory.getAbsolutePath());
            try {
                logExceptions = new PrintStream(new File(outputDirectory, "exceptions.txt"));
                logRuntimes = new PrintStream(new File(outputDirectory, "runtime.txt"));
                // print stderr to console and save to a file
                logStderr = new PrintStream(new File(outputDirectory, "stderr.txt"));
                System.setErr(new PrintStream(new MirrorStream(stderr, logStderr)));
                logRuntimes.println("# How long each benchmark took\n");
                logRuntimes.flush();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            File[] moduleDirectories = new File(pathToMain).listFiles();
            Objects.requireNonNull(moduleDirectories);

            for (File module : moduleDirectories) {
                // Skip directories that are in the black list
                boolean skip = false;
                for (String excluded : blackList) {
                    if (excluded.equals(module.getName())) {
                        skip = true;
                        break;
                    }
                }
                if (skip)
                    continue;

//			System.out.println("module "+module.getPath());
                File dirBenchmarks = new File(module, "benchmarks/src");

                if (!dirBenchmarks.exists())
                    continue;

                recursiveRunBenchmarks(dirBenchmarks, dirBenchmarks);
            }

            // Print out the total time the benchmark took
            long time1 = System.currentTimeMillis();
            long totalTimeMS = time1 - time0;
            int seconds = (int)(totalTimeMS/1000)%60;
            int minutes = (int)((totalTimeMS/(1000*60))%60);
            int hours = (int)((totalTimeMS/(1000*60*60))%24);
            logRuntimes.printf("\nTotal Elapsed Time is %2d:%2d:%2d\n", hours, minutes, seconds);
            System.out.printf("\nTotal Elapsed Time is %2d:%2d:%2d\n", hours, minutes, seconds);
        } finally {
            // Stop mirroring stderr
            System.setErr(stderr);

            // Close all log files
            logStderr.close();
            logExceptions.close();
            logRuntimes.close();

            System.out.println("Done!");
        }
    }

    /**
     * Looks for benchmarks inside of this directory then checks all the children
     */
    public void recursiveRunBenchmarks( File root, File directory ) {
        File[] children = directory.listFiles();
        if (children == null)
            return;

        for (File f : children) {
            if (!f.isFile() || !f.getName().startsWith("Benchmark"))
                continue;

            Path relativeFile = root.toPath().relativize(f.toPath());
            String classPath = relativeFile.toString().replace(File.separatorChar, '.').replace(".java", "");

            // Load the class
            Class<?> c;
            try {
                c = Class.forName(classPath);
            } catch (NoClassDefFoundError | ClassNotFoundException e) {
                logException(e.getClass().getSimpleName() + " " + classPath);
                continue;
            }

            runBenchmark(c);
        }

        // Depth first search through directories
        for (File f : children) {
            if (!f.isDirectory())
                continue;
            recursiveRunBenchmarks(root, f);
        }
    }

    /**
     * Runs the benchmark and saves the results to disk
     */
    public void runBenchmark( Class<?> benchmarkClass ) {
        System.out.println("Running " + benchmarkClass.getName());

        long time0 = System.currentTimeMillis();
        Options opt = new OptionsBuilder()
                .include(benchmarkClass.getName())
                // Do average and throughput to handle very very fast and very slow functions
                .mode(Mode.AverageTime)
//                .mode(Mode.Throughput)
//                .timeUnit(TimeUnit.SECONDS)
                .timeUnit(TimeUnit.NANOSECONDS)
                // The number of times the benchmark is run  is basically at the bare minimum to speed everything up.
                // Otherwise it would take an excessive amount of time
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(2)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(3)
                .timeout(TimeValue.minutes(timeoutMin))
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .resultFormat(ResultFormatType.CSV)
                .result(outputDirectory.getPath() + "/" + benchmarkClass.getName() + ".csv")
                .build();

        try {
            Runner runner = new Runner(opt);
            runner.run();
            // There is a weird halting issue after it runs for a while on one machine. This is an attempt to see
            // if it's GC related.
            System.out.println("System GC run = "+runner.runSystemGC());
        } catch (RunnerException e) {
            e.printStackTrace();
            logException("Exception running " + benchmarkClass.getName() + " : " + e.getMessage());
        }
        long time1 = System.currentTimeMillis();
        logStderr.flush();
        logRuntimes.printf("%-80s %6.1f (min)\n", benchmarkClass.getName().substring(9), (time1 - time0)/(60_000.0));
        logRuntimes.flush();
    }

    private void logException( String message ) {
        logExceptions.println(message);
        logExceptions.flush();
    }

    /** Copies the stream into two streams */
    public static class MirrorStream extends OutputStream {
        PrintStream outA, outB;
        public MirrorStream( PrintStream outA, PrintStream outB ) { this.outA = outA; this.outB = outB; }
        @Override public void write( int b ) { outA.write(b); outB.write(b); }
        @Override public void write( byte[] b, int off, int len ) { outA.write(b, off, len); outB.write(b, off, len); }
        @Override public void flush() { outA.flush(); outB.flush(); }
        @Override public void close() { outA.close(); outB.close(); }
    }

    public static void main( String[] args ) {
        new RunAllBenchmarksApp().process();
    }
}
