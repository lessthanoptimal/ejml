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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads CSV output from JMH Benchmarks and makes the results accessible
 *
 * @author Peter Abeles
 */
public class ParseBenchmarkCsv {
    // What each column in the CSV contains
    public final List<String> columns = new ArrayList<>();
    // Parsed results
    public final List<Result> results = new ArrayList<>();

    // Line being parsed
    int lineNumber = 0;

    //----------------- Internal Workspace
    StringBuilder buffer = new StringBuilder(1024);
    // used for quick lookup
    Map<String, Result> mapResults = new HashMap<>();

    /**
     * Parses the input stream
     *
     * @throws IOException Thrown if anything goes wrong
     */
    public void parse( InputStream input ) throws IOException {
        lineNumber = 0;
        results.clear();
        mapResults.clear();
        parseColumnTypes(input);
        lineNumber++;

        int indexBenchmark = findColumn("Benchmark");
        int indexUnit = findColumn("Unit");
        int indexScore = findColumn("Score");
        int indexFirstParam = findFirstParameterIndex();

        while (input.available() != 0) {
            String[] words = GenerateCode32.readLine(input, buffer).split(",");
            if (words.length != columns.size())
                throw new IOException("Line " + lineNumber + ": Results and columns length do not match. " +
                        words.length + " vs " + columns.size());

            String unit = stripQuotes(words[indexUnit]);

            Result result = new Result();
            result.benchmark = stripQuotes(words[indexBenchmark]);
            for (int i = indexFirstParam; i < words.length; i++) {
                result.parameters.add(words[i]);
            }

            // see if this result already exists
            Result existing = mapResults.get(result.getKey());
            if (existing != null)
                result = existing;
            else {
                results.add(result);
                mapResults.put(result.getKey(), result);
            }

            if (unit.equals("ops/ms")) {
                result.ops_per_ms = Double.parseDouble(words[indexScore]);
            } else if (unit.equals("ms/op")) {
                result.ms_per_op = Double.parseDouble(words[indexScore]);
            } else if (unit.equals("ns/op")) {
                result.ms_per_op = Double.parseDouble(words[indexScore])*1e-6;
            } else {
                throw new IOException("Unknown unit: " + unit);
            }
        }

        // sanity check results
        for (Result r : results) {
            if (r.ms_per_op == -1 && r.ops_per_ms == -1)
                throw new IOException("Could not found measurement for " + r.benchmark);
        }
    }

    /**
     * Finds the column which contains a string (without quotes) that matches 'type'
     */
    private int findColumn( String type ) throws IOException {
        int column = columns.indexOf(type);
        if (column < 0)
            throw new IOException("Line " + lineNumber + ": Couldn't find '" + type + "' in columns");
        return column;
    }

    /**
     * Finds the first column that describes the parameters used in this benchmark
     */
    private int findFirstParameterIndex() throws IOException {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).startsWith("Param: "))
                return i;
        }
        throw new IOException("Couldn't find a parameter");
    }

    private void parseColumnTypes( InputStream input ) throws IOException {
        columns.clear();
        String line = GenerateCode32.readLine(input, buffer);
        String[] words = line.split(",");
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            if (w.length() == 0) {
                throw new IOException("parse error! zero length word. line=" + line);
            }
            // Strip the quotes
            columns.add(stripQuotes(w));
        }
    }

    /** Removes the first and last character in the string */
    private static String stripQuotes( String word ) {
        return word.substring(1, word.length() - 1);
    }

    /** Storage for parsed results */
    public static class Result {
        // which benchmark
        public String benchmark;
        // How it was configured
        public final List<String> parameters = new ArrayList<>();
        // results stored two different ways. Due to limits on precision the one with the largest value
        // should be trusted
        public double ops_per_ms = -1;
        public double ms_per_op = -1;

        public String getKey() {
            return benchmark + ":" + getParametersString();
        }

        public String getParametersString() {
            String key = "";
            for (int i = 0; i < parameters.size(); i++) {
                key += parameters.get(i);
                if (i < parameters.size() - 1)
                    key += ":";
            }
            return key;
        }

        public double getMilliSecondsPerOp() {
            if (ops_per_ms > ms_per_op)
                return 1.0/ops_per_ms;
            else
                return ms_per_op;
        }
    }
}
