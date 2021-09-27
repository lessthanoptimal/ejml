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

import com.peterabeles.LibrarySourceInfo;
import com.peterabeles.ProjectUtils;
import com.peterabeles.regression.RuntimeRegressionMasterApp;

import java.io.File;

/**
 * Master application for JMH runtime regression
 *
 * @author Peter Abeles
 */
public class EjmlRuntimeRegressionApp {
    public static void main( String[] args ) {
        // Set up the environment
        ProjectUtils.pathBenchmarks = "benchmarks/src";
        ProjectUtils.checkRoot = ( f ) ->
                new File(f, "README.md").exists() && new File(f, "settings.gradle").exists();

        ProjectUtils.sourceInfo = () -> {
            var info = new LibrarySourceInfo();
            info.version = EjmlVersion.VERSION;
            info.gitDate = EjmlVersion.GIT_DATE;
            info.gitSha = EjmlVersion.GIT_SHA;
            info.projectName = "EJML";
            return info;
        };

        // Specify which packages it should skip over
        String[] excluded = new String[]{"ejml-experimental"};
        ProjectUtils.skipTest = ( f ) -> {
            for (String name : excluded) {
                if (f.getName().equals(name))
                    return true;
            }
            return false;
        };

        RuntimeRegressionMasterApp.main(args);
    }
}
