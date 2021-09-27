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

import com.peterebeles.autocode.AutocodeConcurrent;

import java.io.File;
import java.io.IOException;

/**
 * Generates concurrent implementations of classes using comment based hints
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway")
public class AutocodeConcurrentApp {
    public static void main( String[] args ) throws IOException {
        AutocodeConcurrent.tab = "    ";
        AutocodeConcurrent.sourceRootName = "src";
        AutocodeConcurrent.pathRootToTest = "../test";
        AutocodeConcurrent.originalToMT = className -> {
            StringBuilder name = new StringBuilder(className);
            String[] words = name.toString().split("_");
            name = new StringBuilder(words[0]);
            for (int i = 1; i < words.length; i++) {
                if (i == words.length - 1) {
                    name.append("_MT");
                }
                name.append("_").append(words[i]);
            }
            return name + ".java";
        };

        String[] directories = new String[]{
                "main/ejml-ddense/src/org/ejml/dense/row/mult",
                "main/ejml-ddense/src/org/ejml/dense/row/misc",
                "main/ejml-ddense/src/org/ejml/dense/row/decomposition/bidiagonal",
                "main/ejml-ddense/src/org/ejml/dense/row/decomposition/hessenberg",
                "main/ejml-ddense/src/org/ejml/dense/row/decomposition/qr",
                "main/ejml-ddense/src/org/ejml/dense/block/",
                "main/ejml-ddense/src/org/ejml/dense/block/decomposition/chol",
                "main/ejml-ddense/src/org/ejml/dense/block/decomposition/qr",
                "main/ejml-ddense/src/org/ejml/dense/block/decomposition/hessenberg",
                "main/ejml-ddense/src/org/ejml/dense/block/linsol/chol",
                "main/ejml-ddense/src/org/ejml/dense/block/linsol/qr",
        };

        String[] files = new String[]{
//				"main/boofcv-ip/src/main/java/boofcv/alg/enhance/impl/ImplEnhanceHistogram.java"
        };

        File rootDir = new File(GenerateCode32.findPathToProjectRoot());
        System.out.println("Autocode Concurrent: current=" + new File(".").getAbsolutePath());
        System.out.println("                     root=" + rootDir.getAbsolutePath());

        for (String f : directories) {
            System.out.println("directory " + f);
            AutocodeConcurrent.convertDir(new File(rootDir, f), "\\S+\\.java", "\\S+MT\\S+");
        }

        for (String f : files) {
            System.out.println("File " + f);
            AutocodeConcurrent.convertFile(new File(rootDir, f));
        }
    }
}