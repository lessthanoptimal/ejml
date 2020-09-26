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

package org.ejml.dense.row.misc;

import org.ejml.CodeGeneratorBase;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * Generates unrolled matrix from minor analytical functions. these can run much faster than LU but will only
 * work for small matrices.
 *
 * When computing the determinants for each minor there are some repeat calculations going on. I manually
 * removed those by storing them in a local variable and only computing it once. Despite reducing the FLOP count
 * it didn't seem to noticeably improve performance in a runtime benchmark..
 *
 * @author Peter Abeles
 */
public class GenerateUnrolledInverseFromMinor_DDRM extends CodeGeneratorBase {

    int maxSize;

    public GenerateUnrolledInverseFromMinor_DDRM( int maxSize ) {
        this.maxSize = maxSize;
    }

    @Override
    public void generate() throws FileNotFoundException {
        setOutputFile("UnrolledInverseFromMinor_DDRM");
        printTop(maxSize);

        printCalls(maxSize);

        for (int i = 2; i <= maxSize; i++) {
            printFunction(i);
        }

        out.print("}\n");
    }

    private void printTop( int N ) {
        String foo =
                "import org.ejml.data.DMatrixRMaj;\n" +
                        "\n" +
                        "/**\n" +
                        " * Unrolled inverse from minor for DDRM type matrices.\n" +
                        " * The input matrix is scaled make it much less prone to overflow and underflow issues.\n" +
                        standardClassDocClosing("Peter Abeles") +
                        "public class " + className + " {\n" +
                        "\n" +
                        "    public static final int MAX = " + N + ";\n";

        out.print(foo);
    }

    private void printCalls( int N ) {
        out.print(
                "\n" +
                        "    public static void inv(DMatrixRMaj mat, DMatrixRMaj inv) {\n");
        out.print("        double max = Math.abs(mat.data[0]);\n" +
                "        int N = mat.getNumElements();\n" +
                "        \n" +
                "        for( int i = 1; i < N; i++ ) {\n" +
                "            double a = Math.abs(mat.data[i]);\n" +
                "            if( a > max ) max = a;\n" +
                "        }\n\n");
        out.print(
                "        switch( mat.numRows ) {\n");
        for (int dimen = 2; dimen <= N; dimen++) {
            out.print("            case " + dimen + ": inv" + dimen + "(mat,inv,1.0/max); break;\n");
        }
        out.print(
                "            default: throw new IllegalArgumentException(\"Not supported\");\n" +
                        "        }\n" +
                        "    }\n\n");
    }

    private void printFunction( int N ) {
        out.print("    public static void inv" + N + "(DMatrixRMaj mat,  DMatrixRMaj inv, double scale)\n" +
                "    {\n" +
                "        double []data = mat.data;\n" +
                "\n");

        // extracts the first minor
        int[] matrix = new int[N*N];
        int index = 0;
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= N; j++, index++) {
                matrix[index] = index;
                out.print("        double " + a(index, N) + " = " + "data[ " + index + " ]*scale;\n");
            }
        }
        out.println();

        printMinors(matrix, N, out);

        out.println();
        out.print("        data = inv.data;\n");

        index = 0;
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= N; j++, index++) {
                out.print("        " + "data[" + index + "] = m" + j + "" + i + " / det;\n");
            }
        }

        out.print("    }\n");
        out.print("\n");
    }

    /**
     * Put the core auto-code algorithm here so an external class can call it
     */
    public static void printMinors( int[] matrix, int N, PrintStream out ) {

        // compute all the minors
        int index = 0;
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= N; j++, index++) {
                out.print("        double m" + i + "" + j + " = ");
                if ((i + j)%2 == 1)
                    out.print("-( ");
                printTopMinor(matrix, i - 1, j - 1, N, out);
                if ((i + j)%2 == 1)
                    out.print(")");
                out.print(";\n");
            }
        }

        out.println();
        // compute the determinant
        out.print("        double det = (a11*m11");
        for (int i = 2; i <= N; i++) {
            out.print(" + " + a(i - 1, N) + "*m" + 1 + "" + i);
        }
        out.println(")/scale;");
    }

    private static void printTopMinor( int[] m, int row, int col, int N, PrintStream out ) {
        int[] d = createMinor(m, row, col, N);

        det(d, 0, N - 1, N - 1, out);
    }

    private static int[] createMinor( int[] m, int row, int col, int N ) {
        int M = N - 1;

        int[] ret = new int[M*M];

        int index = 0;
        for (int i = 0; i < N; i++) {
            if (i == row) continue;
            for (int j = 0; j < N; j++) {
                if (j == col) continue;

                ret[index++] = m[i*N + j];
            }
        }

        return ret;
    }

    private static void det( int[] m, int row, int N, int origN, PrintStream out ) {
        int NN = origN + 1;
        if (N == 1) {
            out.print(a(m[0], NN));
        } else if (N == 2) {
            out.print(a(m[0], NN) + "*" + a(m[3], NN) + " - " + a(m[1], NN) + "*" + a(m[2], NN));
        } else {
            int M = N - 1;

            for (int i = 0; i < N; i++) {
                int[] d = createMinor(m, 0, i, N);

                int pow = i;

                if (pow%2 == 0)
                    out.print(" + " + a(m[i], NN) + "*(");
                else
                    out.print(" - " + a(m[i], NN) + "*(");

                det(d, row + 1, M, origN, out);

                out.print(")");
            }
        }
    }

    private static String a( int index, int N ) {
        int i = index/N + 1;
        int j = index%N + 1;

        if (i > N || j > N)
            throw new RuntimeException("BUG");

        return "a" + i + "" + j;
    }

    public static void main( String[] args ) throws FileNotFoundException {
        var gen = new GenerateUnrolledInverseFromMinor_DDRM(5);
        gen.generate();
    }
}