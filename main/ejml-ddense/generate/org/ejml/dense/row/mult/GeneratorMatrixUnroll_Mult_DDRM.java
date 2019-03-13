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
public class GeneratorMatrixUnroll_Mult_DDRM extends CodeGeneratorBase {
    public static final int MAX = 24;

    @Override
    public void generate() throws FileNotFoundException {
        setOutputFile("MatrixUnroll_Mult_DDRM");
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
                "                         final double[] C, final int offsetC, final int rowA, final int colB) {\n"+
                "            int indexA = offsetA;\n" +
                "            int cIndex = offsetC;\n" +
                "            for( int i = 0; i < rowA; i++ ) {\n",N);

        for (int i = 1; i <= N; i += 5) {
            out.printf("                double a%-2d = A[indexA++]",i);
            for (int j = i+1; j < i + 5 && j <= N; j++) {
                out.printf(",a%-2d = A[indexA++]",j);
            }
            out.print(";\n");
        }
        out.print(
                "\n" +
                "                for( int j = 0; j < colB; j++ ) {\n" +
                "                    int indexB = offsetB+j;\n" +
                "                    double total = 0;\n" +
                "\n");

        for (int i = 1; i <= N; i += 2) {
            out.print("                    ");
            for (int j = i; j < i + 2 && j <= N; j++) {
                out.printf("total += a%-2d * B[indexB];indexB += colB;", j);
            }
            out.println();
        }

        out.print(
                "\n" +
                "                    C[ cIndex++ ] = total;\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n\n");
    }

    public static void main( String[] args ) throws FileNotFoundException {
        GeneratorMatrixUnroll_Mult_DDRM gen = new GeneratorMatrixUnroll_Mult_DDRM();

        gen.generate();
    }
}
