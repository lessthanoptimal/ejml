/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class GenerateCode32 {
    ConvertFile32From64 converter;

    // prefixes and suffices for files which are to be converted
    List<String> suffices64 = new ArrayList<>();
    List<String> suffices32 = new ArrayList<>();
    List<String> prefix64 = new ArrayList<>();
    List<String> prefix32 = new ArrayList<>();

    // file name keyword black list - ignore files with these names
    List<String> blacklist = new ArrayList<>();

    String codeSuffix;

    public GenerateCode32(String codeSuffix) {
        this.codeSuffix = codeSuffix;
    }

    public void process(File inputDirectory ) {
        process(inputDirectory,inputDirectory);
    }

    public void process( File inputDirectory , File outputDirectory ) {
        String fileSuffix = "."+codeSuffix;
        int lengthSuffix = fileSuffix.length();

        if( !inputDirectory.isDirectory() ) {
            System.err.println( "Input isn't a directory. "+inputDirectory );
            return;
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
                if( n.endsWith( s+fileSuffix ) ) {
                    matchedIndex = i;
                    break;
                }
            }

            if( matchedIndex == -1 ) {
                for (int i = 0; i < prefix64.size(); i++) {
                    String s = prefix64.get(i);
                    if( n.startsWith( s ) && n.endsWith(fileSuffix) ) {
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

                n = n.substring(0, n.length() - s64.length() - lengthSuffix) + s32 + fileSuffix;
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

    public static String findPathToProjectRoot() {
        String path = "./";
        while( true ) {
            File d = new File(path);
            if( new File(d,"main").exists() )
                break;
            path = "../"+path;
        }
        return Paths.get(path).normalize().toFile().getAbsolutePath();
    }

    public static void main(String[] args) {
        GenerateJavaCode32.main(args);
        GenerateKotlinCode32.main(args);
    }
}
