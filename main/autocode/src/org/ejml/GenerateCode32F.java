/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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

import com.peterabeles.auto64fto32f.ConvertFile32From64;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Applications which will auto generate 32F code from 64F inside the core module
 * @author Peter Abeles
 */
public class GenerateCode32F {

    private ConvertFile32From64 converter;

    List<String> suffices64 = new ArrayList<String>();
    List<String> suffices32 = new ArrayList<String>();


    public GenerateCode32F() {

        suffices64.add("_B64_to_D64");
        suffices64.add("_D64");
        suffices64.add("_B64");
        suffices64.add("_F64");

        for( String word : suffices64 ) {
            suffices32.add( word.replace("64","32"));
        }

        converter = new ConvertFile32From64(false);

        converter.replacePattern("/\\*\\*/double", "FIXED_DOUBLE");
        converter.replacePattern("DoubleStep", "FIXED_STEP");
        converter.replacePattern("double", "float");
        converter.replacePattern("Double", "Float");
        converter.replacePattern("B64", "B32");
        converter.replacePattern("D64", "D32");
        converter.replacePattern("F64", "F32");
        converter.replacePattern("64F", "32F");
        converter.replacePattern("64-bit", "32-bit");
        converter.replacePattern("UtilEjml.PI", "UtilEjml.F_PI");
        converter.replacePattern("UtilEjml.EPS", "UtilEjml.F_EPS");
        converter.replacePattern("UtilEjml.TEST_64F", "UtilEjml.TEST_32F");
        converter.replacePattern("UtilEjml.TESTP_64F", "UtilEjml.TESTP_32F");

        converter.replaceStartsWith("Math.sqrt", "(float)Math.sqrt");
        converter.replaceStartsWith("Math.pow", "(float)Math.pow");
        converter.replaceStartsWith("Math.sin", "(float)Math.sin");
        converter.replaceStartsWith("Math.cos", "(float)Math.cos");
        converter.replaceStartsWith("Math.tan", "(float)Math.tan");
        converter.replaceStartsWith("Math.atan", "(float)Math.atan");
        converter.replaceStartsWith("Math.log", "(float)Math.log");
        converter.replaceStartsWith("Math.exp", "(float)Math.exp");

        converter.replacePatternAfter("FIXED_DOUBLE", "/\\*\\*/double");
        converter.replacePatternAfter("FIXED_STEP", "DoubleStep");
    }

    public void process( File inputDirectory ) {
        process(inputDirectory,inputDirectory);
    }

    public void process( File inputDirectory , File outputDirectory ) {
        if( !inputDirectory.isDirectory() ) {
            throw new IllegalArgumentException( "Input isn't a directory" );
        }
        if( !outputDirectory.exists() ) {
            if( !outputDirectory.mkdirs() ) {
                throw new RuntimeException("Can't create output directory");
            }
        } if( !outputDirectory.isDirectory() ) {
            throw new IllegalArgumentException( "Output isn't a directory" );
        }

        System.out.println( "---- Directory " + inputDirectory );

        // examine all the files in the directory first
        File[] files = inputDirectory.listFiles();
        if( files == null )
            return;

        for( File f : files ) {
            String n = f.getName();

            int matchedIndex = -1;

            for (int i = 0; i < suffices64.size(); i++) {
                String s = suffices64.get(i);
                if( n.endsWith( s+".java" ) ) {
                    matchedIndex = i;
                    break;
                }
            }

            if( matchedIndex == -1 )
                continue;

            String s64 = suffices64.get(matchedIndex);
            String s32 = suffices32.get(matchedIndex);

            n = n.substring(0, n.length() - s64.length()-5) + s32+".java";
            try {
                System.out.println( "Generating " + n );
                converter.process(f,new File(outputDirectory,n));
            } catch( IOException e ) {
                throw new RuntimeException( e );
            }
        }

        for( File f : files ) {
            if( f.isDirectory() && !f.isHidden() ) {
                process( f , new File(outputDirectory,f.getName()));
            }
        }
    }

    public static void main(String args[] ) {
        String path = "./";
        while( true ) {
            File d = new File(path);
            if( new File(d,"main").exists() )
                break;
            path = "../"+path;
        }
        System.out.println("Path to project root: "+path);

        String coreDir[] = new String[]{
                "main/core/src/org/ejml/data",
                "main/core/test/org/ejml/data",
                "main/core/src/org/ejml/ops",
                "main/core/test/org/ejml/ops",
                "main/experimental/src/org/ejml/alg/dense/decomposition/bidiagonal/"
        };


        GenerateCode32F app = new GenerateCode32F();
        for( String dir : coreDir ) {
            app.process(new File(path,dir) );
        }

        app.process(new File(path,"main/dense64/src"), new File(path,"main/dense32/src") );
        app.process(new File(path,"main/dense64/test"), new File(path,"main/dense32/test") );
    }
}
