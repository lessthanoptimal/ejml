/*
 * Copyright (c) 2009-2019, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.mult;

import org.ejml.CodeGeneratorBase;

import java.io.FileNotFoundException;

/**
 * @author Peter Abeles
 */
public class GeneratorMatrixUnroll_MultJIK_DDRM extends CodeGeneratorBase {
    public static final int MAX = 24;

    @Override
    public void generate() throws FileNotFoundException {
        setOutputFile("MatrixUnroll_MultJIK_DDRM");
        String preamble =
                "import org.ejml.interfaces.mult.MatrixMultUnrolled;\n" +
                "\n" +
                "/**\n" +
                " * Matrix multiplication for an inner sub-matrix with an the inner most loop unrolled.\n" +
                " *\n" +
                " * @author Peter Abeles\n" +
                " */\n" +
                "public class "+className+" {\n" +
                "    public static final int MAX = 40;\n";
        out.print(preamble);
        defineArray();
        out.print(
                "    public static boolean mult( double[] A, int offsetA,  double[] B, int offsetB,  double[] C, int offsetC,\n" +
                "                                int rowA, int colA, int colB)\n" +
                "    {\n" +
                "        if( colA <= MAX ) {\n" +
                "            ops[colA-1].mult(A,offsetA,B,offsetB,C,offsetC,rowA,colB);\n" +
                "            return true;\n" +
                "        }\n" +
                "        return false;\n" +
                "    }\n\n");

        for (int i = 0; i < MAX; i++) {
            printOperator(i+1);
        }
        out.println("}");
    }

    private void defineArray() {
        out.print("    public static final MatrixMultUnrolled[] ops = new MatrixMultUnrolled[]{\n");
        for (int i = 1; i <= MAX; i++) {
            out.printf("            new M%d(),\n",i);
        }
        out.print("    };\n\n");
    }

    private void printOperator( int N ) {
        out.printf
                ("    public static class M%d implements MatrixMultUnrolled {\n" +
                "        @Override\n" +
                "        public void mult(final double[] A, final int offsetA, final double[] B, final int offsetB,  \n" +
                "                         final double[] C, final int offsetC, final int rowA, final int colB) {\n",N);


        out.print(
                "            for( int j = 0; j < colB; j++ ) {\n" +
                "                int idxB = offsetB+j;\n");
        for (int i = 1; i <= N; i += 5) {
            out.printf("                double b%-2d  = B[idxB+%d*colB]",i,i-1);
            for (int j = i+1; j < i + 5 && j <= N; j++) {
                out.printf(",b%-2d = B[idxB+%d*colB]",j,j-1);
            }
            out.print(";\n");
        }
        out.print(
                "                int idxA = offsetA;\n" +
                "                for( int i = 0; i < rowA; i++ ) {\n" +
                "                    double total = 0;\n");
        for (int i = 1; i <= N; i += 3) {
            out.print("                    ");
            for (int j = i; j < i + 3 && j <= N; j++) {
                out.printf("total += A[idxA++] * b%-2d; ", j);
            }
            out.println();
        }
        out.println(
                "                    C[ i*colB+j ] = total;\n" +
                "                }\n" +
                "            }\n"+
                "        }\n" +
                "    }\n"
                );
    }

    public static void main( String[] args ) throws FileNotFoundException {
        GeneratorMatrixUnroll_MultJIK_DDRM gen = new GeneratorMatrixUnroll_MultJIK_DDRM();

        gen.generate();
    }
}
