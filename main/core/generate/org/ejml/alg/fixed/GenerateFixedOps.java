/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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
import org.ejml.alg.dense.misc.GenerateDeterminantFromMinor;
import org.ejml.alg.dense.misc.GenerateInverseFromMinor;

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

    // The maximize size it will do inverse on
    public static int maxInverseSize = 5;

    @Override
    public void generate() throws FileNotFoundException {
        for( int dimension = 2; dimension <= 6; dimension++ ){
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
            if( dimension <= maxInverseSize ) {
                invert(dimension);
                det(dimension);
            }
            trace(dimension);
            diag(dimension);
            elementMax(dimension);
            elementMaxAbs(dimension);
            elementMin(dimension);
            elementMinAbs(dimension);
            elementMult_two(dimension);
            elementMult_three(dimension);
            elementDiv_two(dimension);
            elementDiv_three(dimension);
            scale_two(dimension);
            scale_three(dimension);
            divide_two(dimension);
            divide_three(dimension);
            changeSign(dimension);
            fill(dimension);

            out.println("}\n");
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
                " * <p>Common matrix operations for fixed sized matrices which are "+dimen+" x "+dimen+" or "+dimen+" element vectors.</p>\n" +
                " * <p>DO NOT MODIFY.  Automatically generated code created by "+getClass().getSimpleName()+"</p>\n" +
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
        out.print("    /**\n" +
                "     * <p>Performs matrix to vector multiplication:<br>\n" +
                "     * <br>\n" +
                "     * c = a * b <br>\n" +
                "     * <br>\n" +
                "     * c<sub>i</sub> = &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>k</sub>}\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param a The left matrix in the multiplication operation. Not modified.\n" +
                "     * @param b The right vector in the multiplication operation. Not modified.\n" +
                "     * @param c Where the results of the operation are stored. Modified.\n" +
                "     */\n" +
                "    public static void mult( "+nameMatrix+" a , "+nameVector+" b , "+nameVector+" c) {\n");
        for( int y = 1; y <= dimen; y++ ) {
            out.print("        c.a"+y+" = ");
            for( int x = 1; x <= dimen; x++ ) {
                out.print("a.a"+y+""+x+"*b.a"+x);
                if( x < dimen )
                    out.print(" + ");
                else
                    out.print(";\n");
            }
        }
        out.printf("    }\n\n");

    }

    private void mult_v_m_v( int dimen ){
        out.print("    /**\n" +
                "     * <p>Performs vector to matrix multiplication:<br>\n" +
                "     * <br>\n" +
                "     * c = a * b <br>\n" +
                "     * <br>\n" +
                "     * c<sub>j</sub> = &sum;<sub>k=1:n</sub> { b<sub>k</sub> * a<sub>kj</sub> }\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param a The left vector in the multiplication operation. Not modified.\n" +
                "     * @param b The right matrix in the multiplication operation. Not modified.\n" +
                "     * @param c Where the results of the operation are stored. Modified.\n" +
                "     */\n" +
                "    public static void mult( "+nameVector+" a , "+nameMatrix+" b , "+nameVector+" c) {\n");

        for( int y = 1; y <= dimen; y++ ) {
            out.print("        c.a"+y+" = ");
            for( int x = 1; x <= dimen; x++ ) {
                out.print("a.a"+x+"*b.a"+x+""+y);
                if( x < dimen )
                    out.print(" + ");
                else
                    out.print(";\n");
            }
        }
        out.print("    }\n\n");

    }

    private void dot( int dimen ){
        out.print("    /**\n" +
                "     * <p>Performs the vector dot product:<br>\n" +
                "     * <br>\n" +
                "     * c = a * b <br>\n" +
                "     * <br>\n" +
                "     * c> = &sum;<sub>k=1:n</sub> { b<sub>k</sub> * a<sub>k</sub> }\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param a The left vector in the multiplication operation. Not modified.\n" +
                "     * @param b The right matrix in the multiplication operation. Not modified.\n" +
                "     * @return The dot product\n" +
                "     */\n" +
                "    public static double dot( "+nameVector+" a , "+nameVector+" b ) {\n");
        out.print("        return ");
        for( int i = 1; i <= dimen; i++ ) {
            out.print("a.a"+i+"*b.a"+i);
            if( i < dimen )
                out.print(" + ");
            else
                out.print(";\n");
        }
        out.print("    }\n\n");
    }

    private void setIdentity( int dimen ){
        out.print("    /**\n" +
                "     * Sets all the diagonal elements equal to one and everything else equal to zero.\n" +
                "     * If this is a square matrix then it will be an identity matrix.\n" +
                "     *\n" +
                "     * @param a A matrix.\n" +
                "     */\n" +
                "    public static void setIdentity( "+nameMatrix+" a ) {\n");
        for( int y = 1; y <= dimen; y++ ) {
            out.print("        ");
            for( int x = 1; x <= dimen; x++ ) {
                int val = x==y?1:0;
                out.print("a.a"+x+""+y+" = "+val+";");
                if( x < dimen )
                    out.print(" ");
                else
                    out.print("\n");
            }
        }
        out.print("    }\n\n");
    }

    private void invert( int dimen ){
        out.print("    /**\n" +
                "     * Inverts matrix 'a' using minor matrices and stores the results in 'inv'.  Scaling is applied to improve\n" +
                "     * stability against overflow and underflow.\n" +
                "     *\n" +
                "     * WARNING: Potentially less stable than using LU decomposition.\n" +
                "     *\n" +
                "     * @param a Input matrix. Not modified.\n" +
                "     * @param inv Inverted output matrix.  Modified.\n" +
                "     * @return true if it was successful or false if it failed.  Not always reliable.\n" +
                "     */\n" +
                "    public static boolean invert( "+nameMatrix+" a , "+nameMatrix+" inv ) {\n" +
                "\n" +
                "        double scale = 1.0/elementMaxAbs(a);\n" +
                "\n");

        int matrix[] = new int[dimen*dimen];
        int index = 0;
        for( int y = 1; y <= dimen; y++ ) {
            for( int x = 1; x <= dimen; x++ , index++) {
                matrix[index] = index;
                String coor = y+""+x;
                out.print("        double a"+coor+" = a.a"+coor+"*scale;\n");
            }
        }
        out.println();

        try {
            GenerateInverseFromMinor gen = new GenerateInverseFromMinor(false);
            gen.printMinors(matrix,dimen, out);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        out.println();

        for( int y = 1; y <= dimen; y++ ) {
            for( int x = 1; x <= dimen; x++ ) {
                String coor0 = y+""+x;
                String coor1 = x+""+y;
                out.print("        inv.a"+coor0+" = m"+coor1+"/det;\n");
            }
        }
        out.println();
        out.print("        return !Double.isNaN(det) && !Double.isInfinite(det);\n");
        out.print("    }\n\n");

    }

    private void trace( int dimen ){
        out.print("    /**\n" +
                "     * <p>\n" +
                "     * This computes the trace of the matrix:<br>\n" +
                "     * <br>\n" +
                "     * trace = &sum;<sub>i=1:n</sub> { a<sub>ii</sub> }\n" +
                "     * </p>\n" +
                "     * <p>\n" +
                "     * The trace is only defined for square matrices.\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param a A square matrix.  Not modified.\n" +
                "     */\n" +
                "    public static double trace( "+nameMatrix+" a ) {\n");
        out.print("        return ");
        for( int i = 1; i <= dimen; i++ ) {
            out.print("a.a"+i+""+1);
            if( i < dimen )
                out.print(" + ");
            else
                out.println(";");
        }
        out.print("    }\n\n");
    }

    private void det( int dimen ){
        out.print("    /**\n" +
                "     * Computes the determinant using minor matrices.\n" +
                "     * <p></p>\n" +
                "     * WARNING: Potentially less stable than using LU decomposition.\n" +
                "     *\n" +
                "     * @param mat Input matrix.  Not modified.\n" +
                "     * @return The determinant.\n" +
                "     */\n" +
                "    public static double det( "+nameMatrix+" mat ) {\n" +
                "\n");
        if( dimen == 2 ) {
            out.print("        return mat.a11*mat.a22 - mat.a12*mat.a21;\n");
        } else if( dimen == 3 ) {
            out.print( "        double a = mat.a11*(mat.a22*mat.a33 - mat.a23*mat.a32);\n" +
                    "        double b = mat.a12*(mat.a21*mat.a33 - mat.a23*mat.a31);\n" +
                    "        double c = mat.a13*(mat.a21*mat.a32 - mat.a31*mat.a22);\n" +
                    "\n" +
                    "        return a-b+c;\n");
        } else {
            GenerateDeterminantFromMinor helper = new GenerateDeterminantFromMinor(out) {
                @Override
                protected String getInputValue(int element) {
                    int row = element/(N+1) + 1;
                    int col = element%(N+1) + 1;
                    return "mat.a"+row+""+col;
                }
            };
            helper.printFunctionInner(dimen);
            out.print("\n        return ret;\n");
        }

        out.print("    }\n\n");
    }

    private void diag( int dimen ) {
        out.print("    /**\n" +
                "     * <p>\n" +
                "     * Extracts all diagonal elements from 'input' and places them inside the 'out' vector. Elements\n" +
                "     * are in sequential order.\n" +
                "     * </p>\n" +
                "     *\n" +
                "     *\n" +
                "     * @param input Matrix.  Not modified.\n" +
                "     * @param out Vector containing diagonal elements.  Modified.\n" +
                "     */\n" +
                "    public static void diag( "+nameMatrix+" input , "+nameVector+" out ) {\n");
        for( int i = 1; i <= dimen; i++ ) {
            out.print("        out.a"+i+" = input.a"+i+""+i+";\n");
        }
        out.print("    }\n\n");
    }

    private void elementMax( int dimen ) {
        out.print("    /**\n" +
                "     * <p>\n" +
                "     * Returns the value of the element in the matrix that has the largest value.<br>\n" +
                "     * <br>\n" +
                "     * Max{ a<sub>ij</sub> } for all i and j<br>\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param a A matrix. Not modified.\n" +
                "     * @return The max element value of the matrix.\n" +
                "     */\n" +
                "    public static double elementMax( "+nameMatrix+" a ) {\n");

        out.print("        double max = a.a11;\n");
        for( int y = 1; y <= dimen; y++ ) {
            for( int x = 1; x <= dimen; x++ ) {
                if( y == 1 && x == 1 )
                    continue;
                out.print("        max = Math.max(max,a.a"+y+""+x+");\n");
            }
        }
        out.print("\n" +
                "        return max;\n" +
                "    }\n\n");
    }

    private void elementMaxAbs( int dimen ) {
        out.print("    /**\n" +
                "     * <p>\n" +
                "     * Returns the absolute value of the element in the matrix that has the largest absolute value.<br>\n" +
                "     * <br>\n" +
                "     * Max{ |a<sub>ij</sub>| } for all i and j<br>\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param a A matrix. Not modified.\n" +
                "     * @return The max abs element value of the matrix.\n" +
                "     */\n" +
                "    public static double elementMaxAbs( "+nameMatrix+" a ) {\n");

        out.print("        double max = a.a11;\n");
        for( int y = 1; y <= dimen; y++ ) {
            for( int x = 1; x <= dimen; x++ ) {
                if( y == 1 && x == 1 )
                    continue;
                out.print("        max = Math.max(max,Math.abs(a.a"+y+""+x+"));\n");
            }
        }
        out.print("\n" +
                "        return max;\n" +
                "    }\n\n");
    }

    private void elementMin( int dimen ) {
        out.print("    /**\n" +
                "     * <p>\n" +
                "     * Returns the value of the element in the matrix that has the minimum value.<br>\n" +
                "     * <br>\n" +
                "     * Min{ a<sub>ij</sub> } for all i and j<br>\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param a A matrix. Not modified.\n" +
                "     * @return The value of element in the matrix with the minimum value.\n" +
                "     */\n" +
                "    public static double elementMin( "+nameMatrix+" a ) {\n");

        out.print("        double min = a.a11;\n");
        for( int y = 1; y <= dimen; y++ ) {
            for( int x = 1; x <= dimen; x++ ) {
                if( y == 1 && x == 1 )
                    continue;
                out.print("        min = Math.min(min,a.a"+y+""+x+");\n");
            }
        }
        out.print("\n" +
                "        return min;\n" +
                "    }\n\n");
    }

    private void elementMinAbs( int dimen ) {
        out.print("    /**\n" +
                "     * <p>\n" +
                "     * Returns the absolute value of the element in the matrix that has the smallest absolute value.<br>\n" +
                "     * <br>\n" +
                "     * Min{ |a<sub>ij</sub>| } for all i and j<br>\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param a A matrix. Not modified.\n" +
                "     * @return The max element value of the matrix.\n" +
                "     */\n" +
                "    public static double elementMinAbs( "+nameMatrix+" a ) {\n");

        out.print("        double min = a.a11;\n");
        for( int y = 1; y <= dimen; y++ ) {
            for( int x = 1; x <= dimen; x++ ) {
                if( y == 1 && x == 1 )
                    continue;
                out.print("        min = Math.min(min,Math.abs(a.a"+y+""+x+"));\n");
            }
        }
        out.print("\n" +
                "        return min;\n" +
                "    }\n\n");
    }

    private void elementMult_two( int dimen ) {
        out.print("    /**\n" +
                "     * <p>Performs the an element by element multiplication operation:<br>\n" +
                "     * <br>\n" +
                "     * a<sub>ij</sub> = a<sub>ij</sub> * b<sub>ij</sub> <br>\n" +
                "     * </p>\n" +
                "     * @param a The left matrix in the multiplication operation. Modified.\n" +
                "     * @param b The right matrix in the multiplication operation. Not modified.\n" +
                "     */\n" +
                "    public static void elementMult( "+nameMatrix+" a , "+nameMatrix+" b) {\n");
        for( int y = 1; y <= dimen; y++ ) {
            out.print("        ");
            for( int x = 1; x <= dimen; x++ ) {
                String w = "a"+y+""+x;
                out.print("a."+w+" *= b."+w+";");
                if( x < dimen )
                    out.print(" ");
                else
                    out.println();
            }
        }
        out.print("    }\n\n");
    }

    private void elementMult_three( int dimen ) {
        out.print("    /**\n" +
                "     * <p>Performs the an element by element multiplication operation:<br>\n" +
                "     * <br>\n" +
                "     * c<sub>ij</sub> = a<sub>ij</sub> * b<sub>ij</sub> <br>\n" +
                "     * </p>\n" +
                "     * @param a The left matrix in the multiplication operation. Not modified.\n" +
                "     * @param b The right matrix in the multiplication operation. Not modified.\n" +
                "     * @param c Where the results of the operation are stored. Modified.\n" +
                "     */\n" +
                "    public static void elementMult( "+nameMatrix+" a , "+nameMatrix+" b , "+nameMatrix+" c ) {\n");
        for( int y = 1; y <= dimen; y++ ) {
            out.print("        ");
            for( int x = 1; x <= dimen; x++ ) {
                String w = "a"+y+""+x;
                out.print("c."+w+" = a."+w+"*b."+w+";");
                if( x < dimen )
                    out.print(" ");
                else
                    out.println();
            }
        }
        out.print("    }\n\n");
    }

    private void elementDiv_two( int dimen ) {
        out.print("    /**\n" +
                "     * <p>Performs the an element by element division operation:<br>\n" +
                "     * <br>\n" +
                "     * a<sub>ij</sub> = a<sub>ij</sub> / b<sub>ij</sub> <br>\n" +
                "     * </p>\n" +
                "     * @param a The left matrix in the division operation. Modified.\n" +
                "     * @param b The right matrix in the division operation. Not modified.\n" +
                "     */\n" +
                "    public static void elementDiv( "+nameMatrix+" a , "+nameMatrix+" b) {\n");
        for( int y = 1; y <= dimen; y++ ) {
            out.print("        ");
            for( int x = 1; x <= dimen; x++ ) {
                String w = "a"+y+""+x;
                out.print("a."+w+" /= b."+w+";");
                if( x < dimen )
                    out.print(" ");
                else
                    out.println();
            }
        }
        out.print("    }\n\n");
    }

    private void elementDiv_three( int dimen ) {
        out.print("    /**\n" +
                "     * <p>Performs the an element by element division operation:<br>\n" +
                "     * <br>\n" +
                "     * c<sub>ij</sub> = a<sub>ij</sub> / b<sub>ij</sub> <br>\n" +
                "     * </p>\n" +
                "     * @param a The left matrix in the division operation. Not modified.\n" +
                "     * @param b The right matrix in the division operation. Not modified.\n" +
                "     * @param c Where the results of the operation are stored. Modified.\n" +
                "     */\n" +
                "    public static void elementDiv( "+nameMatrix+" a , "+nameMatrix+" b , "+nameMatrix+" c ) {\n");
        for( int y = 1; y <= dimen; y++ ) {
            out.print("        ");
            for( int x = 1; x <= dimen; x++ ) {
                String w = "a"+y+""+x;
                out.print("c."+w+" = a."+w+"/b."+w+";");
                if( x < dimen )
                    out.print(" ");
                else
                    out.println();
            }
        }
        out.print("    }\n\n");
    }

    private void scale_two( int dimen ) {
        out.print("    /**\n" +
                "     * <p>\n" +
                "     * Performs an in-place element by element scalar multiplication.<br>\n" +
                "     * <br>\n" +
                "     * a<sub>ij</sub> = &alpha;*a<sub>ij</sub>\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param a The matrix that is to be scaled.  Modified.\n" +
                "     * @param alpha the amount each element is multiplied by.\n" +
                "     */\n" +
                "    public static void scale( double alpha , "+nameMatrix+" a ) {\n");
        for( int y = 1; y <= dimen; y++ ) {
            out.print("        ");
            for( int x = 1; x <= dimen; x++ ) {
                String w = "a"+y+""+x;
                out.print("a."+w+" *= alpha;");
                if( x < dimen )
                    out.print(" ");
                else
                    out.println();
            }
        }
        out.print("    }\n\n");
    }

    private void scale_three( int dimen ) {
        out.print("    /**\n" +
                "     * <p>\n" +
                "     * Performs an element by element scalar multiplication.<br>\n" +
                "     * <br>\n" +
                "     * b<sub>ij</sub> = &alpha;*a<sub>ij</sub>\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param alpha the amount each element is multiplied by.\n" +
                "     * @param a The matrix that is to be scaled.  Not modified.\n" +
                "     * @param b Where the scaled matrix is stored. Modified.\n" +
                "     */\n" +
                "    public static void scale( double alpha , "+nameMatrix+" a , "+nameMatrix+" b ) {\n");
        for( int y = 1; y <= dimen; y++ ) {
            out.print("        ");
            for( int x = 1; x <= dimen; x++ ) {
                String w = "a"+y+""+x;
                out.print("b."+w+" = a."+w+"*alpha;");
                if( x < dimen )
                    out.print(" ");
                else
                    out.println();
            }
        }
        out.print("    }\n\n");
    }

    private void divide_two( int dimen ) {
        out.print("    /**\n" +
                "     * <p>\n" +
                "     * Performs an in-place element by element scalar division. Scalar denominator.<br>\n" +
                "     * <br>\n" +
                "     * a<sub>ij</sub> = a<sub>ij</sub>/&alpha;\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param a The matrix whose elements are to be divided.  Modified.\n" +
                "     * @param alpha the amount each element is divided by.\n" +
                "     */\n" +
                "    public static void divide( "+nameMatrix+" a , double alpha ) {\n");
        for( int y = 1; y <= dimen; y++ ) {
            out.print("        ");
            for( int x = 1; x <= dimen; x++ ) {
                String w = "a"+y+""+x;
                out.print("a."+w+" /= alpha;");
                if( x < dimen )
                    out.print(" ");
                else
                    out.println();
            }
        }
        out.print("    }\n\n");    }

    private void divide_three( int dimen ) {
        out.print("    /**\n" +
                "     * <p>\n" +
                "     * Performs an element by element scalar division.  Scalar denominator.<br>\n" +
                "     * <br>\n" +
                "     * b<sub>ij</sub> = *a<sub>ij</sub> /&alpha;\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param alpha the amount each element is divided by.\n" +
                "     * @param a The matrix whose elements are to be divided.  Not modified.\n" +
                "     * @param b Where the results are stored. Modified.\n" +
                "     */\n" +
                "    public static void divide( "+nameMatrix+" a , double alpha , "+nameMatrix+" b ) {\n");
        for( int y = 1; y <= dimen; y++ ) {
            out.print("        ");
            for( int x = 1; x <= dimen; x++ ) {
                String w = "a"+y+""+x;
                out.print("b."+w+" = a."+w+"/alpha;");
                if( x < dimen )
                    out.print(" ");
                else
                    out.println();
            }
        }
        out.print("    }\n\n");
    }

    private void changeSign( int dimen ) {
        out.print("    /**\n" +
                "     * <p>\n" +
                "     * Changes the sign of every element in the matrix.<br>\n" +
                "     * <br>\n" +
                "     * a<sub>ij</sub> = -a<sub>ij</sub>\n" +
                "     * </p>\n" +
                "     *\n" +
                "     * @param a A matrix. Modified.\n" +
                "     */\n" +
                "    public static void changeSign( "+nameMatrix+" a )\n" +
                "    {\n");
        for( int y = 1; y <= dimen; y++ ) {
            out.print("        ");
            for( int x = 1; x <= dimen; x++ ) {
                String w = "a"+y+""+x;
                out.print("a."+w+" = -a."+w+";");
                if( x < dimen )
                    out.print(" ");
                else
                    out.println();
            }
        }
        out.print("    }\n\n");
    }

    private void fill( int dimen ) {
        out.print("    /**\n" +
                "     * <p>\n" +
                "     * Sets every element in the matrix to the specified value.<br>\n" +
                "     * <br>\n" +
                "     * a<sub>ij</sub> = value\n" +
                "     * <p>\n" +
                "     *\n" +
                "     * @param a A matrix whose elements are about to be set. Modified.\n" +
                "     * @param v The value each element will have.\n" +
                "     */\n" +
                "    public static void fill( "+nameMatrix+" a , double v  ) {\n");
        for( int y = 1; y <= dimen; y++ ) {
            out.print("        ");
            for( int x = 1; x <= dimen; x++ ) {
                String w = "a"+y+""+x;
                out.print("a."+w+" = v;");
                if( x < dimen )
                    out.print(" ");
                else
                    out.println();
            }
        }
                out.print("    }\n\n");
    }

    public static void main( String args[] ) throws FileNotFoundException {
        GenerateFixedOps app = new GenerateFixedOps();

        app.generate();
    }

}