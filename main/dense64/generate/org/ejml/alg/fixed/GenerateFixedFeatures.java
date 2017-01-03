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

package org.ejml.alg.fixed;

import org.ejml.CodeGeneratorBase;

import java.io.FileNotFoundException;

/**
 * Automatic code generator for FixedOps
 *
 * @author Peter Abeles
 */
public class GenerateFixedFeatures extends CodeGeneratorBase {

    String classPreamble = "FixedFeatures";

    String nameMatrix;
    String nameVector;

    @Override
    public void generate() throws FileNotFoundException {
        for( int dimension = 2; dimension <= 6; dimension++ ){
            printPreable(dimension);

            isIdentical(dimension);
            isIdentical_vector(dimension);
            hasUncountable(dimension);
            hasUncountable_vector(dimension);

            out.println("}\n");
        }
    }

    public void printPreable( int dimen ) throws FileNotFoundException {

        String className = classPreamble+dimen+"_D64";

        nameMatrix = "FixedMatrix"+dimen+"x"+dimen+"_64F";
        nameVector = "FixedMatrix"+dimen+"_64F";

        setOutputFile(className);

        out.print("import org.ejml.data."+nameVector+";\n" +
                "import org.ejml.data."+nameMatrix+";\n" +
                "import org.ejml.ops.MatrixFeatures_D64;\n" +
                "import org.ejml.UtilEjml;\n" +
                "\n" +
                "/**\n" +
                " * <p>Matrix features for fixed sized matrices which are "+dimen+" x "+dimen+" or "+dimen+" element vectors.</p>\n" +
                " * <p>DO NOT MODIFY.  Automatically generated code created by "+getClass().getSimpleName()+"</p>\n" +
                " *\n" +
                " * @author Peter Abeles\n" +
                " */\n" +
                "public class "+className+" {\n");
    }

    private void isIdentical(int dimen ){
        out.print("    public static boolean isIdentical("+nameMatrix+" a , "+nameMatrix+" b , double tol ) {\n");

        for( int y = 1; y <= dimen; y++ ) {
            for( int x = 1; x <= dimen; x++ ) {
                String n = y+""+x;
                out.print("        if( !MatrixFeatures_D64.isIdentical(a.a"+n+",b.a"+n+",tol))\n"+
                        "            return false;\n");
            }
        }
        out.print(
                "        return true;\n"+
                "    }\n\n");
    }

    private void isIdentical_vector( int dimen ) {
        out.print("    public static boolean isIdentical("+nameVector+" a , "+nameVector+" b , double tol ) {\n");

        for( int y = 1; y <= dimen; y++ ) {
            String n = y+"";
            out.print("        if( !MatrixFeatures_D64.isIdentical(a.a"+n+",b.a"+n+",tol))\n"+
                    "            return false;\n");
        }
        out.print(
                "        return true;\n"+
                        "    }\n\n");
    }

    private void hasUncountable(int dimen ){
        out.print("    public static boolean hasUncountable("+nameMatrix+" a ) {\n");

        for( int y = 1; y <= dimen; y++ ) {
            String row = "";

            for( int x = 1; x <= dimen; x++ ) {
                String n = y+""+x;
                if( x > 1 )
                    row += "+ ";
                row += "a.a"+n;
            }
            out.print("        if( UtilEjml.isUncountable("+row+"))\n"+
                    "            return true;\n");
        }
        out.print(
                "        return false;\n"+
                        "    }\n\n");
    }

    private void hasUncountable_vector( int dimen ) {
        out.print("    public static boolean hasUncountable("+nameVector+" a ) {\n");

        for( int y = 1; y <= dimen; y++ ) {
            String n = y+"";
            out.print("        if( UtilEjml.isUncountable(a.a"+n+"))\n"+
                    "            return true;\n");
        }
        out.print(
                "        return false;\n"+
                        "    }\n\n");
    }

    public static void main( String args[] ) throws FileNotFoundException {
        GenerateFixedFeatures app = new GenerateFixedFeatures();

        app.generate();
    }

}