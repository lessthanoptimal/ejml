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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Runs all JMH benchmarks and saves the results plus exceptions.
 *
 * NOTE: This finds benchmarks by scanning the source code and not by using reflections to scan classes. This is
 * preferable since if a new module is added or re-named unless it's updated correctly in this build.gradle it
 * will silently fail by skipping those benchmarks.
 */
public class RunAllRuntimeBenchmarks extends JmhRunnerBase {
    public static String BENCHMARK_RESULTS_DIR = "runtime_regression";

    /** Manually specify which benchmarks to run based on class name */
    public List<String> userBenchmarkNames = new ArrayList<>();

    /**
     * The order in which benchmarks are run is randomized. This is intended to reduce systematic bias. E.g.
     * If a heavy task is run first it could heat up the computer causing it to throttle.
     */
    public boolean randomizedOrder = true;

    String[] blackListPackages = new String[]{"ejml-experimental"};

    @Override protected void performBenchmarks() {
        String pathToMain = GenerateCode32.projectRelativePath("main");
        List<String> benchmarkNames = new ArrayList<>();
        if (userBenchmarkNames.isEmpty()) {
            findBenchmarksByModule(pathToMain, benchmarkNames);
        } else {
            benchmarkNames.addAll(userBenchmarkNames);
        }
        // Randomize the order to reduce systematic bias if requested
        if (randomizedOrder) {
            Collections.shuffle(benchmarkNames);
        }
        for (String benchmarkName : benchmarkNames) {
            runBenchmark(benchmarkName, false, null);
        }
    }

    /**
     * Recursively searches each module by file path to find benchmarks then runs them
     */
    private void findBenchmarksByModule( String pathToMain, List<String> benchmarkNames ) {
        File[] moduleDirectories = new File(pathToMain).listFiles();
        Objects.requireNonNull(moduleDirectories);

        for (File module : moduleDirectories) {
            // Skip directories that are in the black list
            boolean skip = false;
            for (String excluded : blackListPackages) {
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

            recursiveFindBenchmarks(dirBenchmarks, dirBenchmarks, benchmarkNames);
        }
    }

    /**
     * Looks for benchmarks inside of this directory then checks all the children
     */
    public void recursiveFindBenchmarks( File root, File directory, List<String> benchmarkNames ) {
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

            benchmarkNames.add(c.getName());
        }

        // Depth first search through directories
        for (File f : children) {
            if (!f.isDirectory())
                continue;
            recursiveFindBenchmarks(root, f, benchmarkNames);
        }
    }

    public static void main( String[] args ) {
        new RunAllRuntimeBenchmarks().process();
    }
}
