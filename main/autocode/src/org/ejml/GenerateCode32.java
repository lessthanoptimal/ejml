/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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
public class GenerateCode32 {

    private ConvertFile32From64 converter;

    List<String> suffices64 = new ArrayList<String>();
    List<String> suffices32 = new ArrayList<String>();


    public GenerateCode32() {

        suffices64.add("_B64_to_R64");
        suffices64.add("_R64");
        suffices64.add("_B64");
        suffices64.add("_F64");
        suffices64.add("_CR64");
        suffices64.add("_C64");

        for( String word : suffices64 ) {
            suffices32.add( word.replace("64","32"));
        }

        converter = new ConvertFile32From64(false);

        converter.replacePattern("/\\*\\*/double", "FIXED_DOUBLE");
        converter.replacePattern("DoubleStep", "FIXED_STEP");
        converter.replacePattern("double", "float");
        converter.replacePattern("Double", "Float");
        converter.replacePattern("B64", "B32");
        converter.replacePattern("R64", "R32");
        converter.replacePattern("F64", "F32");
        converter.replacePattern("C64", "C32");
        converter.replacePattern("CR64", "CR32");
        converter.replacePattern("CB64", "CB32");
        converter.replacePattern("random64", "random32");
        converter.replacePattern("64-bit", "32-bit");
        converter.replacePattern("UtilEjml.PI", "UtilEjml.F_PI");
        converter.replacePattern("UtilEjml.EPS", "UtilEjml.F_EPS");

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

    public static void recursiveDelete( File d , boolean first ) {
        if( first ) {
            System.out.println("Cleaning out " + d.getPath());
            if( !d.exists() )
                return;
        }
        if( !d.isDirectory() )
            throw new RuntimeException("Expected directory at "+d);

        File[] files = d.listFiles();
        for( File f : files ) {
            if( f.isDirectory() )
                recursiveDelete(f, false);
            if( !f.delete() )
                throw new RuntimeException("Failed to delete "+f.getPath());
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
                "main/experimental/src/org/ejml/dense/row/decomposition/bidiagonal/"
        };

        GenerateCode32 app = new GenerateCode32();
        for( String dir : coreDir ) {
            app.process(new File(path,dir) );
        }

        // remove any previously generated code
        for( String module : new String[]{"dense","denseC"}) {
            recursiveDelete(new File(path,"main/"+module+"32/src"), true);
            recursiveDelete(new File(path,"main/"+module+"32/test"), true);

            app.process(new File(path,"main/"+module+"64/src"), new File(path,"main/"+module+"32/src") );
            app.process(new File(path,"main/"+module+"64/test"), new File(path,"main/"+module+"32/test") );
        }
    }
}
