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

import java.io.FileNotFoundException;

/**
 * Automatic code generator for FixedOps
 *
 * @author Peter Abeles
 */
public class GenerateFixedNormOps extends GenerateFixed {

    public GenerateFixedNormOps() {
        super("FixedNormOps");
    }

    @Override
    public void generate() throws FileNotFoundException {
        for( int dimension = 2; dimension <= 6; dimension++ ){
            printPreable(dimension);

            normalizeF(dimension);
            normalizeF_vector(dimension);
            fastNormF(dimension);
            fastNormF_vector(dimension);
            normF(dimension);
            normF_vector(dimension);

            out.println("}\n");
            out.close();
        }
    }

    public void printPreable( int dimen ) throws FileNotFoundException {

        setClassNames(dimen);

        out.print("import org.ejml.data."+nameVector+";\n" +
                "import org.ejml.data."+nameMatrix+";\n" +
                "import org.ejml.ops.MatrixFeatures_D64;\n" +
                "import org.ejml.UtilEjml;\n" +
                "\n" +
                "/**\n" +
                " * <p>Matrix norm related operations for fixed sized matrices of size "+dimen+".</p>\n" +
                " * <p>DO NOT MODIFY.  Automatically generated code created by "+getClass().getSimpleName()+"</p>\n" +
                " *\n" +
                " * @author Peter Abeles\n" +
                " */\n" +
                "public class "+className+" {\n");
    }

    private void normalizeF(int dimen ){
        out.print("    public static void normalizeF( "+nameMatrix+" M ) {\n" +
                "        double val = normF(M);\n" +
                "        FixedOps"+dimen+"_D64.divide(M,val);\n" +
                "    }\n\n");
    }
    private void normalizeF_vector(int dimen ){
        out.print("    public static void normalizeF( "+nameVector+" M ) {\n" +
                "        double val = normF(M);\n" +
                "        FixedOps"+dimen+"_D64.divide(M,val);\n" +
                "    }\n\n");
    }

    private void fastNormF(int dimen ){
        out.print("    public static double fastNormF( "+nameMatrix+" M ) {\n" +
                "        double sum = 0;\n" +
                "\n");
        for( int row = 1; row <= dimen; row++ ) {
            out.print("        sum += ");
            for( int col = 1; col <= dimen; col++ ) {
                String element = "M.a"+row+""+col;
                out.print(element+"*"+element);
                if( col < dimen )
                    out.print(" + ");
                else
                    out.print(";\n");
            }
        }
        out.print("\n" +
                "        return Math.sqrt(sum);\n" +
                "    }\n\n");
    }

    private void fastNormF_vector(int dimen ){
        out.print("    public static double fastNormF( "+nameVector+" M ) {\n");
        out.print("        double sum = ");
        for( int col = 1; col <= dimen; col++ ) {
            String element = "M.a"+col;
            out.print(element+"*"+element);
            if( col < dimen )
                out.print(" + ");
            else
                out.print(";\n");
        }
        out.print(
                "        return Math.sqrt(sum);\n" +
                "    }\n\n");
    }

    private void normF(int dimen ) {
        out.print("    public static double normF( "+nameMatrix+" M ) {\n" +
                "        double scale = FixedOps"+dimen+"_D64.elementMaxAbs(M);\n" +
                "\n" +
                "        if( scale == 0.0 )\n" +
                "            return 0.0;\n" +
                "\n");
        for( int row = 1; row <= dimen; row++ ) {
            out.print("        double ");
            for( int col = 1; col <= dimen; col++ ) {
                String element = row+""+col;
                out.print("a"+element+" = M.a"+element+"/scale");
                if( col < dimen )
                    out.print(", ");
                else
                    out.print(";\n");
            }
        }
        out.print("\n");
        out.print("        double sum = 0;\n");
        for( int row = 1; row <= dimen; row++ ) {
            out.print("        sum += ");
            for( int col = 1; col <= dimen; col++ ) {
                String element = "a"+row+""+col;
                out.print(element+"*"+element);
                if( col < dimen )
                    out.print(" + ");
                else
                    out.print(";\n");
            }
        }
        out.print(
                "\n" +
                "        return scale*Math.sqrt(sum);\n" +
                "    }\n\n");
    }

    private void normF_vector(int dimen ) {
        out.print("    public static double normF( "+nameVector+" M ) {\n" +
                "        double scale = FixedOps"+dimen+"_D64.elementMaxAbs(M);\n" +
                "\n" +
                "        if( scale == 0.0 )\n" +
                "            return 0.0;\n" +
                "\n");

        out.print("        double ");
        for( int col = 1; col <= dimen; col++ ) {
            out.print("a"+col+" = M.a"+col+"/scale");
            if( col < dimen )
                out.print(", ");
            else
                out.print(";\n");
        }
        out.print("        double sum = ");
        for( int col = 1; col <= dimen; col++ ) {
            String a = "a"+col;
            out.print(a+"*"+a);
            if( col < dimen )
                out.print(" + ");
            else
                out.print(";\n");
        }

        out.print("\n" +
                "        return scale*Math.sqrt(sum);\n" +
                "    }\n\n");
    }


    public static void main( String args[] ) throws FileNotFoundException {
        GenerateFixedNormOps app = new GenerateFixedNormOps();

        app.generate();
    }

}