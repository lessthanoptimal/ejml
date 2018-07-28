/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

    // prefixes and suffices for files which are to be converted
    List<String> suffices64 = new ArrayList<>();
    List<String> suffices32 = new ArrayList<>();
    List<String> prefix64 = new ArrayList<>();
    List<String> prefix32 = new ArrayList<>();

    // file name keyword black list - ignore files with these names
    List<String> blacklist = new ArrayList<>();

    public GenerateCode32() {

        // TODO remove once F32 sparse code is generated
        blacklist.add("TestDMatrixSparseCSC");
        blacklist.add("TestConvertDMatrixStruct");
        blacklist.add("TestDEigenSparseCSC");
        blacklist.add("TestConvertDMatrixStruct");

        String[] sufficeRoot = new String[]{"DRM","DMA","DRB","SCC","STL","DF2","DF3","DF4","DF5","DF6","TRIPLET"};

        suffices64.add("_DDRB_to_DDRM");
        suffices64.add("_F64");
        suffices32.add("_FDRB_to_FDRM");
        suffices32.add("_F32");

        for( String suffice : sufficeRoot ) {
            suffices64.add("_D"+suffice);
            suffices32.add("_F"+suffice);
            suffices64.add("_Z"+suffice);
            suffices32.add("_C"+suffice);
        }

        suffices64.add("Features_D");
        suffices32.add("Features_F");

        prefix64.add("DGrow");
        prefix32.add("FGrow");
        prefix64.add("DMatrix");
        prefix32.add("FMatrix");
        prefix64.add("ZMatrix");
        prefix32.add("CMatrix");
        prefix64.add("DEigen");
        prefix32.add("FEigen");
        prefix64.add("DSubmatrix");
        prefix32.add("FSubmatrix");
        prefix64.add("ConvertD");
        prefix32.add("ConvertF");
        prefix64.add("GenericTestsDMatrix");
        prefix32.add("GenericTestsFMatrix");

        int N = prefix64.size();
        for (int i = 0; i < N; i++) {
            prefix64.add("Test"+prefix64.get(i));
            prefix32.add("Test"+prefix32.get(i));
        }

        converter = new ConvertFile32From64(false);

        converter.replacePattern("DoubleStep", "FIXED_STEP");
        converter.replacePattern("double", "float");
        converter.replacePattern("Double", "Float");

        for( String suffice : sufficeRoot) {
            converter.replacePattern("_D"+suffice, "_F"+suffice);
            converter.replacePattern("_Z"+suffice, "_C"+suffice);
            converter.replacePattern("MatrixType.D"+suffice, "MatrixType.F"+suffice);
            converter.replacePattern("MatrixType.Z"+suffice, "MatrixType.C"+suffice);
            converter.replacePattern(".getD"+suffice, ".getF"+suffice);
            converter.replacePattern(".getZ"+suffice, ".getC"+suffice);
        }

        converter.replacePattern("ConvertD", "ConvertF");
        converter.replacePattern("DGrowArray", "FGrowArray");
        converter.replacePattern("DMatrix", "FMatrix");
        converter.replacePattern("DSubmatrix", "FSubmatrix");
        converter.replacePattern("DEigen", "FEigen");
        converter.replacePattern("ZComplex", "CComplex");
        converter.replacePattern("ZMatrix", "CMatrix");
        converter.replacePattern("ZSubmatrix", "CSubmatrix");
        converter.replacePattern("Features_D.", "Features_F.");

        converter.replacePattern("F64", "F32");
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

            boolean blacklisted = false;
            for (int i = 0; i < blacklist.size(); i++) {
                if( n.contains(blacklist.get(i))) {
                    blacklisted = true;
                    break;
                }
            }

            if( blacklisted )
                continue;

            int matchedIndex = -1;

            boolean suffix = true;

            for (int i = 0; i < suffices64.size(); i++) {
                String s = suffices64.get(i);
                if( n.endsWith( s+".java" ) ) {
                    matchedIndex = i;
                    break;
                }
            }

            if( matchedIndex == -1 ) {
                for (int i = 0; i < prefix64.size(); i++) {
                    String s = prefix64.get(i);
                    if( n.startsWith( s ) && n.endsWith(".java") ) {
                        matchedIndex = i;
                        suffix = false;
                        break;
                    }
                }
            }

            if( matchedIndex == -1 )
                continue;

            if( suffix ) {
                String s64 = suffices64.get(matchedIndex);
                String s32 = suffices32.get(matchedIndex);

                n = n.substring(0, n.length() - s64.length() - 5) + s32 + ".java";
            } else {
                String s64 = prefix64.get(matchedIndex);
                String s32 = prefix32.get(matchedIndex);

                n = s32 + n.substring(s64.length(),n.length());
            }

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
                "main/ejml-simple/src/org/ejml/simple/ops",
                "main/ejml-core/src/org/ejml/data",
                "main/ejml-core/test/org/ejml/data",
                "main/ejml-core/src/org/ejml/ops",
                "main/ejml-core/test/org/ejml/ops",
                "main/ejml-experimental/src/org/ejml/dense/row/decomposition/bidiagonal/"
        };

        GenerateCode32 app = new GenerateCode32();
        for( String dir : coreDir ) {
            app.process(new File(path,dir) );
        }

        // remove any previously generated code
        for( String module : new String[]{"dense"}) {
            recursiveDelete(new File(path,"main/ejml-f"+module+"/src"), true);
            recursiveDelete(new File(path,"main/ejml-c"+module+"/src"), true);
            recursiveDelete(new File(path,"main/ejml-f"+module+"/test"), true);
            recursiveDelete(new File(path,"main/ejml-c"+module+"/test"), true);

            app.process(new File(path,"main/ejml-d"+module+"/src"), new File(path,"main/ejml-f"+module+"/src") );
            app.process(new File(path,"main/ejml-z"+module+"/src"), new File(path,"main/ejml-c"+module+"/src") );
            app.process(new File(path,"main/ejml-d"+module+"/test"), new File(path,"main/ejml-f"+module+"/test") );
            app.process(new File(path,"main/ejml-z"+module+"/test"), new File(path,"main/ejml-c"+module+"/test") );
        }
    }
}
