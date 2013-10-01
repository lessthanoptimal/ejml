/*
 * Copyright (c) 2009-2013, Peter Abeles. All Rights Reserved.
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
public class GenerateFixedOps extends CodeGeneratorBase {

    String classPreamble = "FixedOps";

    String nameMatrix;
    String nameVector;

    @Override
    public void generate() throws FileNotFoundException {
        for( int dimension = 2; dimension <= 5; dimension++ ){

            dimension = 4;

            printPreable(dimension);

            printAdd(dimension);
            addEquals(dimension);
            transpose_one(dimension);
            transpose_two(dimension);
            mult(dimension);
            multTransA(dimension);
            multTransAB(dimension);
            multTransB(dimension);
            mult_m_v_v(dimension);
            mult_v_m_v(dimension);
            dot(dimension);
            setIdentity(dimension);

            out.println("}\n");

            dimension = 10;
        }
    }

    public void printPreable( int dimen ) throws FileNotFoundException {

        String className = classPreamble+dimen;

        nameMatrix = "FixedMatrix"+dimen+"x"+dimen+"_64F";
        nameVector = "FixedMatrix"+dimen+"_64F";

        setOutputFile(className);

        out.print("import org.ejml.data."+nameVector+";\n" +
                "import org.ejml.data."+nameMatrix+";\n" +
                "\n" +
                "/**\n" +
                " * Common matrix operations for fixed sized matrices which are "+dimen+" x "+dimen+" or "+dimen+" element vectors.\n" +
                " *\n" +
                " * @author Peter Abeles\n" +
                " */\n" +
                "public class "+className+" {\n");
    }

    private void printAdd( int dimen ){
        out.print("    /**\n" +
                "     * <p>Performs the following operation:<br>\n" +
                "     * <br>\n" +
                "     * c = a + b <br>\n" +
                "     * c<sub>ij</sub> = a<sub>ij</sub> + b<sub>ij</sub> <br>\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * <p>\n" +
                "     * Matrix C can be the same instance as Matrix A and/or B.\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param a A Matrix. Not modified.\n" +
                "     * @param b A Matrix. Not modified.\n" +
                "     * @param c A Matrix where the results are stored. Modified.\n" +
                "     */\n" +
                "    public static void add( "+nameMatrix+" a , "+nameMatrix+" b , "+nameMatrix+" c ) {\n");
        for( int y = 1; y <= dimen; y++ ) {
            for( int x = 1; x <= dimen; x++ ) {
                String n = y+""+x;
                out.print("        c.a"+n+" = a.a"+n+" + b.a"+n+";\n");
            }
        }
        out.print("    }\n\n");
    }

    private void addEquals( int dimen ){
        out.print("    /**\n" +
                "     * <p>Performs the following operation:<br>\n" +
                "     * <br>\n" +
                "     * a = a + b <br>\n" +
                "     * a<sub>ij</sub> = a<sub>ij</sub> + b<sub>ij</sub> <br>\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param a A Matrix. Modified.\n" +
                "     * @param b A Matrix. Not modified.\n" +
                "     */\n" +
                "    public static void addEquals( "+nameMatrix+" a , "+nameMatrix+" b ) {\n");
        for( int y = 1; y <= dimen; y++ ) {
            for( int x = 1; x <= dimen; x++ ) {
                String n = y+""+x;
                out.print("        a.a"+n+" += b.a"+n+";\n");
            }
        }
        out.print("    }\n\n");
    }

    private void transpose_one( int dimen ){
        out.print("    /**\n" +
                "     * Performs an in-place transpose.  This algorithm is only efficient for square\n" +
                "     * matrices.\n" +
                "     *\n" +
                "     * @param m The matrix that is to be transposed. Modified.\n" +
                "     */\n" +
                "    public static void transpose( "+nameMatrix+" m ) {\n"  +
                "        double tmp;\n");
        for( int y = 1; y <= dimen; y++ ) {
            for( int x = y+1; x <= dimen; x++ ) {
                String f = +y+""+x;
                String t = +x+""+y;

                out.print("        tmp = m.a"+f+"; m.a"+f+" = m.a"+t+"; m.a"+t+" = tmp;\n");
            }
        }
        out.print("    }\n\n");
    }

    private void transpose_two( int dimen ){
        out.print("    /**\n" +
                "     * <p>\n" +
                "     * Transposes matrix 'a' and stores the results in 'b':<br>\n" +
                "     * <br>\n" +
                "     * b<sub>ij</sub> = a<sub>ji</sub><br>\n" +
                "     * where 'b' is the transpose of 'a'.\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param input The original matrix.  Not modified.\n" +
                "     * @param output Where the transpose is stored. If null a new matrix is created. Modified.\n" +
                "     * @return The transposed matrix.\n" +
                "     */\n" +
                "    public static "+nameMatrix+" transpose( "+nameMatrix+" input , "+nameMatrix+" output ) {\n" +
                "        if( input == null )\n" +
                "            input = new "+nameMatrix+"();\n\n");
        for( int y = 1; y <= dimen; y++ ) {
            for( int x = 1; x <= dimen; x++ ) {
                String f = +y+""+x;
                String t = +x+""+y;

                out.print("        output.a"+f+" = input.a"+t+";\n");
            }
        }

        out.print("\n        return output;\n" +
                "    }\n\n");
    }

    private void mult( int dimen ){
        out.print("    /**\n" +
                "     * <p>Performs the following operation:<br>\n" +
                "     * <br>\n" +
                "     * c = a * b <br>\n" +
                "     * <br>\n" +
                "     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>kj</sub>}\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param a The left matrix in the multiplication operation. Not modified.\n" +
                "     * @param b The right matrix in the multiplication operation. Not modified.\n" +
                "     * @param c Where the results of the operation are stored. Modified.\n" +
                "     */\n" +
                "    public static void mult( "+nameMatrix+" a , "+nameMatrix+" b , "+nameMatrix+" c) {\n");

        for( int y = 1; y <= dimen; y++ ) {
            for( int x = 1; x <= dimen; x++ ) {
                out.print("        c.a"+y+""+x+" = ");
                for( int k = 1; k <= dimen; k++ ) {
                    out.print("a.a"+y+""+k+"*b.a"+k+""+x);
                    if( k < dimen )
                        out.print(" + ");
                    else
                        out.print(";\n");
                }
            }
        }
        out.print("    }\n\n");
    }

    private void multTransA( int dimen ){
        out.print("    /**\n" +
                "     * <p>Performs the following operation:<br>\n" +
                "     * <br>\n" +
                "     * c = a<sup>T</sup> * b <br>\n" +
                "     * <br>\n" +
                "     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>kj</sub>}\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param a The left matrix in the multiplication operation. Not modified.\n" +
                "     * @param b The right matrix in the multiplication operation. Not modified.\n" +
                "     * @param c Where the results of the operation are stored. Modified.\n" +
                "     */\n" +
                "    public static void multTransA( "+nameMatrix+" a , "+nameMatrix+" b , "+nameMatrix+" c) {\n");
        for( int y = 1; y <= dimen; y++ ) {
            for( int x = 1; x <= dimen; x++ ) {
                out.print("        c.a"+y+""+x+" = ");
                for( int k = 1; k <= dimen; k++ ) {
                    out.print("a.a"+k+""+y+"*b.a"+k+""+x);
                    if( k < dimen )
                        out.print(" + ");
                    else
                        out.print(";\n");
                }
            }
        }
        out.printf("    }\n\n");
    }

    private void multTransAB( int dimen ){
        out.printf("    /**\n" +
                "     * <p>\n" +
                "     * Performs the following operation:<br>\n" +
                "     * <br>\n" +
                "     * c = a<sup>T</sup> * b<sup>T</sup><br>\n" +
                "     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>jk</sub>}\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param a The left matrix in the multiplication operation. Not modified.\n" +
                "     * @param b The right matrix in the multiplication operation. Not modified.\n" +
                "     * @param c Where the results of the operation are stored. Modified.\n" +
                "     */\n" +
                "    public static void multTransAB( "+nameMatrix+" a , "+nameMatrix+" b , "+nameMatrix+" c) {\n");
        for( int y = 1; y <= dimen; y++ ) {
            for( int x = 1; x <= dimen; x++ ) {
                out.print("        c.a"+y+""+x+" = ");
                for( int k = 1; k <= dimen; k++ ) {
                    out.print("a.a"+k+""+y+"*b.a"+x+""+k);
                    if( k < dimen )
                        out.print(" + ");
                    else
                        out.print(";\n");
                }
            }
        }
        out.print("    }\n\n");
    }

    private void multTransB( int dimen ){
        out.print("    /**\n" +
                "     * <p>\n" +
                "     * Performs the following operation:<br>\n" +
                "     * <br>\n" +
                "     * c = a * b<sup>T</sup> <br>\n" +
                "     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>jk</sub>}\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param a The left matrix in the multiplication operation. Not modified.\n" +
                "     * @param b The right matrix in the multiplication operation. Not modified.\n" +
                "     * @param c Where the results of the operation are stored. Modified.\n" +
                "     */\n" +
                "    public static void multTransB( "+nameMatrix+" a , "+nameMatrix+" b , "+nameMatrix+" c) {\n");
        for( int y = 1; y <= dimen; y++ ) {
            for( int x = 1; x <= dimen; x++ ) {
                out.print("        c.a"+y+""+x+" = ");
                for( int k = 1; k <= dimen; k++ ) {
                    out.print("a.a"+y+""+k+"*b.a"+x+""+k);
                    if( k < dimen )
                        out.print(" + ");
                    else
                        out.print(";\n");
                }
            }
        }
        out.print("    }\n\n");
    }

    private void mult_m_v_v( int dimen ){

    }

    private void mult_v_m_v( int dimen ){

    }

    private void dot( int dimen ){

    }

    private void setIdentity( int dimen ){

    }

    public static void main( String args[] ) throws FileNotFoundException {
        GenerateFixedOps app = new GenerateFixedOps();

        app.generate();
    }

}