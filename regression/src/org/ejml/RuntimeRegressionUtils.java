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

import org.ejml.ParseBenchmarkCsv.Result;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static org.ejml.RuntimeRegressionMasterApp.formatDate;

/**
 * Utility functions for dealing with JMh log results.
 *
 * @author Peter Abeles
 */
public class RuntimeRegressionUtils {

    /**
     * Short summary of system and EJML info
     */
    public static String createInfoSummaryText() {
        String text = "";
        text += "EJML Runtime Regression Baseline\n";
        text += "\n";
        text += "Hostname:      " + RuntimeRegressionUtils.getHostName() + "\n";
        text += "Machine Name:  " + SettingsLocal.machineName + "\n";
        text += "Date:          " + formatDate(new Date()) + "\n";
        text += "EJML Version:  " + EjmlVersion.VERSION + "\n";
        text += "EJML SHA:      " + EjmlVersion.GIT_SHA + "\n";
        text += "EJML GIT_DATE: " + EjmlVersion.GIT_DATE + "\n";
        return text;
    }

    /**
     * Adds useful information about the system it's being run on. Some of this will be system specific.
     */
    public static void saveSystemInfo( File directory, PrintStream err ) {
        try {
            PrintStream out = new PrintStream(new File(directory, "SystemInfo.txt"));

            out.println("Hostname:      " + getHostName());
            out.println("Machine Name:  " + SettingsLocal.machineName);
            out.println("Date:          " + formatDate(new Date()));
            out.println("EJML Version:  " + EjmlVersion.VERSION);
            out.println("EJML SHA:      " + EjmlVersion.GIT_SHA);
            out.println("EJML GIT_DATE: " + EjmlVersion.GIT_DATE);

            // This won't work on every system
            try {
                double[] load = SystemInfo.lookupSystemLoad();

                out.println("---- Native Access Info");
                out.println("OS: "+SystemInfo.readOSVersion());
                out.println("CPU: "+SystemInfo.readCpu());
                out.println("Ave Load: 1m="+load[0]+" 5m="+load[1]+" 15m="+load[2]);
            } catch( RuntimeException ignore ){}

            // This should work on every system
            out.println("----");
            out.println("Runtime.getRuntime().availableProcessors()," +Runtime.getRuntime().availableProcessors());
            out.println("Runtime.getRuntime().freeMemory()," +Runtime.getRuntime().freeMemory());
            out.println("Runtime.getRuntime().totalMemory()," + Runtime.getRuntime().totalMemory());

            String newLine = System.getProperty("line.separator");
            Properties properties = System.getProperties();
            Set<Object> keys = properties.keySet();
            for( Object key : keys ) {
                String property = properties.getProperty(key.toString());
                // Get rid of newlines since they screw up the formatting
                property = property.replaceAll(newLine,"");
                out.println("\""+key.toString()+"\",\""+property+"\"");
            }
        } catch (Exception e) {
            e.printStackTrace(err);
            err.println("Error saving system info");
        }
    }

    /**
     * Returns the name of the device this regression is run on
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown";
        }
    }

    /**
     * Loads all the JMH results in a directory and puts it into a map.
     */
    public static Map<String, Double> loadJmhResults( File directory ) throws IOException {
        Map<String, Double> results = new HashMap<>();
        var parser = new ParseBenchmarkCsv();

        File[] children = directory.listFiles();
        if (children == null)
            return results;

        for (int i = 0; i < children.length; i++) {
            File f = children[i];
            if (!f.isFile() || !f.getName().endsWith(".csv"))
                continue;
            parser.parse(new FileInputStream(f));
            for (Result r : parser.results) {
                results.put(r.getKey(), r.getMilliSecondsPerOp());
            }
        }

        return results;
    }

    /**
     * For every comparable result, see if the current performance shows any regressions
     *
     * @param tolerance fractional tolerance
     */
    public static Set<String> findRuntimeExceptions( Map<String, Double> baseline,
                                                     Map<String, Double> current,
                                                     double tolerance ) {
        Set<String> exceptions = new HashSet<>();

        for (String name : baseline.keySet()) {
            double valueBaseline = baseline.get(name);
            if (!current.containsKey(name))
                continue;
            double valueCurrent = current.get(name);

            if (valueCurrent/valueBaseline - 1.0 <= tolerance)
                continue;

            exceptions.add(name);
        }

        return exceptions;
    }

    public static String encodeAllBenchmarks( Map<String, Double> results ) {
        String text = "# Results Summary\n";
        for (String key : results.keySet()) {
            text += key + "," + results.get(key) + "\n";
        }
        return text;
    }

    public static void saveAllBenchmarks( Map<String, Double> results, String path ) {
        String text = encodeAllBenchmarks(results);

        try {
            System.out.println("Saving to " + path);
            var writer = new PrintWriter(path);
            writer.print(text);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Double> loadAllBenchmarks( File file ) {
        try {
            return loadAllBenchmarks(new FileInputStream(file));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Map<String, Double> loadAllBenchmarks( InputStream input ) {
        Map<String, Double> results = new HashMap<>();
        BufferedReader buffered = new BufferedReader(new InputStreamReader(input));
        try {
            while (true) {
                String line = buffered.readLine();
                if (line == null)
                    break;

                if (line.startsWith("#") || line.isEmpty())
                    continue;

                // find the last comma, that's where it needs to split
                int lastIdx = line.lastIndexOf(',');
                if (lastIdx==-1)
                    throw new IOException("No comma found");

                String key = line.substring(0,lastIdx);
                String value = line.substring(lastIdx+1);

                results.put(key, Double.parseDouble(value));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return results;
    }
}
