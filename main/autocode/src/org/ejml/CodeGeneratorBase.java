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

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * <p>Base class for code generators.</p>
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public abstract class CodeGeneratorBase {

    public static String copyright = "/** Copyright Peter Abeles. Failed to load copyright.txt. */";

    protected PrintStream out;
    protected String className;

    static {
        try {
            File pathCopyright = new File(GenerateCode32.findPathToProjectRoot(), "docs/copyright.txt");
            // The trim is to make we know how much white space (i.e. none) is at the end, which can be variable
            copyright = readFile(pathCopyright.getAbsolutePath(), StandardCharsets.UTF_8).trim() + "\n";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the code
     */
    public abstract void generate() throws FileNotFoundException;

    protected String standardClassDocClosing(String ...authors) {
        return  " *\n" +
                " * <p>DO NOT MODIFY. Automatically generated code created by "+getClass().getSimpleName()+"</p>\n" +
                " *\n" +
                " * @author "+authors[0]+"\n" + // yes I was lazy here..
                " */\n" +
                "@Generated(\""+getClass().getCanonicalName()+"\")\n";
    }

    public void setOutputFile( String className ) throws FileNotFoundException {
        this.className = className;
        out = new PrintStream(new FileOutputStream(className + ".java"));
        out.print(copyright);
        out.println();
        out.println("package " + getPackage() + ";");
        out.println();
        out.println("import javax.annotation.Generated;");
    }

    public static String readFile( String path, Charset encoding ) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), encoding);
    }

    public String getPackage() {
        return getClass().getPackage().getName();
    }
}
