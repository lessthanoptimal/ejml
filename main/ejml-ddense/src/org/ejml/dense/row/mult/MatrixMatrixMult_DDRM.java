/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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

import org.ejml.UtilEjml;
import org.ejml.data.DMatrix1Row;
import org.ejml.dense.row.CommonOps_DDRM;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Generated;
//CONCURRENT_INLINE import org.ejml.concurrency.EjmlConcurrency;

/**
 * <p>
 * This class contains various types of matrix matrix multiplication operations for {@link DMatrix1Row}.
 * </p>
 * <p>
 * Two algorithms that are equivalent can often have very different runtime performance.
 * This is because of how modern computers uses fast memory caches to speed up reading/writing to data.
 * Depending on the order in which variables are processed different algorithms can run much faster than others,
 * even if the number of operations is the same.
 * </p>
 *
 * <p>
 * Algorithms that are labeled as 'reorder' are designed to avoid caching jumping issues, some times at the cost
 * of increasing the number of operations. This is important for large matrices. The straight forward
 * implementation seems to be faster for small matrices.
 * </p>
 *
 * <p>
 * Algorithms that are labeled as 'aux' use an auxiliary array of length n. This array is used to create
 * a copy of an out of sequence column vector that is referenced several times. This reduces the number
 * of cache misses. If the 'aux' parameter passed in is null then the array is declared internally.
 * </p>
 *
 * <p>
 * Typically the straight forward implementation runs about 30% faster on smaller matrices and
 * about 5 times slower on larger matrices. This is all computer architecture and matrix shape/size specific.
 * </p>
 *
 * <p>DO NOT MODIFY. Automatically generated code created by GenerateMatrixMatrixMult_DDRM</p>
 *
 * @author Peter Abeles
 */
@Generated("org.ejml.dense.row.mult.GenerateMatrixMatrixMult_DDRM")
public class MatrixMatrixMult_DDRM {
    /**
     * @see CommonOps_DDRM#mult(org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void mult_reorder( DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numCols, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        C.reshape(A.numRows, B.numCols);

        if (A.numCols == 0 || A.numRows == 0) {
            CommonOps_DDRM.fill(C, 0);
            return;
        }
        final int endOfKLoop = B.numRows*B.numCols;

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numRows, i -> {
        for (int i = 0; i < A.numRows; i++) {
            int indexCbase = i*C.numCols;
            int indexA = i*A.numCols;

            // need to assign C.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + B.numCols;

            double valA = A.data[indexA++];

            while (indexB < end) {
                C.set(indexC++, valA*B.data[indexB++]);
            }

            // now add to it
            while (indexB != endOfKLoop) { // k loop
                indexC = indexCbase;
                end = indexB + B.numCols;

                valA = A.data[indexA++];

                while (indexB < end) { // j loop
                    C.data[indexC++] += valA*B.data[indexB++];
                }
            }
        }
        //CONCURRENT_ABOVE });
    }

    /**
     * @see CommonOps_DDRM#mult(org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void mult_small( DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numCols, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        C.reshape(A.numRows, B.numCols);

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numRows, i -> {
        for (int i = 0; i < A.numRows; i++) {
            int cIndex = i*B.numCols;
            int aIndexStart = i*A.numCols;
            for (int j = 0; j < B.numCols; j++) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + B.numRows;
                while (indexA < end) {
                    total += A.data[indexA++]*B.data[indexB];
                    indexB += B.numCols;
                }

                C.set(cIndex++, total);
            }
        }
        //CONCURRENT_ABOVE });
    }

    //CONCURRENT_OMIT_BEGIN

    /**
     * @see CommonOps_DDRM#mult(org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void mult_aux( DMatrix1Row A, DMatrix1Row B, DMatrix1Row C, @Nullable double[] aux ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numCols, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        C.reshape(A.numRows, B.numCols);

        if (aux == null) aux = new double[B.numRows];

        for (int j = 0; j < B.numCols; j++) {
            // create a copy of the column in B to avoid cache issues
            for (int k = 0; k < B.numRows; k++) {
                aux[k] = B.unsafe_get(k, j);
            }

            int indexA = 0;
            for (int i = 0; i < A.numRows; i++) {
                double total = 0;
                for (int k = 0; k < B.numRows; ) {
                    total += A.data[indexA++]*aux[k++];
                }
                C.set(i*C.numCols + j, total);
            }
        }
    }
    //CONCURRENT_OMIT_END

    /**
     * @see CommonOps_DDRM#multTransA(org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multTransA_reorder( DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numRows, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        C.reshape(A.numCols, B.numCols);

        if (A.numCols == 0 || A.numRows == 0) {
            CommonOps_DDRM.fill(C, 0);
            return;
        }
        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numCols, i -> {
        for (int i = 0; i < A.numCols; i++) {
            int indexC_start = i*C.numCols;

            // first assign R
            double valA = A.data[i];
            int indexB = 0;
            int end = indexB + B.numCols;
            int indexC = indexC_start;
            while (indexB < end) {
                C.set(indexC++, valA*B.data[indexB++]);
            }
            // now increment it
            for (int k = 1; k < A.numRows; k++) {
                valA = A.unsafe_get(k, i);
                end = indexB + B.numCols;
                indexC = indexC_start;
                // this is the loop for j
                while (indexB < end) {
                    C.data[indexC++] += valA*B.data[indexB++];
                }
            }
        }
        //CONCURRENT_ABOVE });
    }

    /**
     * @see CommonOps_DDRM#multTransA(org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multTransA_small( DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numRows, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        C.reshape(A.numCols, B.numCols);

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numCols, i -> {
        for (int i = 0; i < A.numCols; i++) {
            int cIndex = i*B.numCols;
            for (int j = 0; j < B.numCols; j++) {
                int indexA = i;
                int indexB = j;
                int end = indexB + B.numRows*B.numCols;

                double total = 0;
                // loop for k
                for (; indexB < end; indexB += B.numCols) {
                    total += A.data[indexA]*B.data[indexB];
                    indexA += A.numCols;
                }

                C.set(cIndex++, total);
            }
        }
        //CONCURRENT_ABOVE });
    }

    /**
     * @see CommonOps_DDRM#multTransAB(org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multTransAB( DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numRows, B.numCols, "The 'A' and 'B' matrices do not have compatible dimensions");
        C.reshape(A.numCols, B.numRows);


        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numCols, i -> {
        for (int i = 0; i < A.numCols; i++) {
            int cIndex = i*B.numRows;
            int indexB = 0;
            for (int j = 0; j < B.numRows; j++) {
                int indexA = i;
                int end = indexB + B.numCols;

                double total = 0;

                while (indexB < end) {
                    total += A.data[indexA]*B.data[indexB++];
                    indexA += A.numCols;
                }

                C.set(cIndex++, total);
            }
        }
        //CONCURRENT_ABOVE });
    }

    //CONCURRENT_OMIT_BEGIN

    /**
     * @see CommonOps_DDRM#multTransAB(org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multTransAB_aux( DMatrix1Row A, DMatrix1Row B, DMatrix1Row C, @Nullable double[] aux ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numRows, B.numCols, "The 'A' and 'B' matrices do not have compatible dimensions");
        C.reshape(A.numCols, B.numRows);

        if (aux == null) aux = new double[A.numRows];

        if (A.numCols == 0 || A.numRows == 0) {
            CommonOps_DDRM.fill(C, 0);
            return;
        }
        int indexC = 0;
        for (int i = 0; i < A.numCols; i++) {
            for (int k = 0; k < B.numCols; k++) {
                aux[k] = A.unsafe_get(k, i);
            }

            for (int j = 0; j < B.numRows; j++) {
                double total = 0;

                for (int k = 0; k < B.numCols; k++) {
                    total += aux[k]*B.unsafe_get(j, k);
                }
                C.set(indexC++, total);
            }
        }
    }
    //CONCURRENT_OMIT_END

    /**
     * @see CommonOps_DDRM#multTransB(org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multTransB( DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numCols, B.numCols, "The 'A' and 'B' matrices do not have compatible dimensions");
        C.reshape(A.numRows, B.numRows);

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numRows, xA -> {
        for (int xA = 0; xA < A.numRows; xA++) {
            int cIndex = xA*B.numRows;
            int aIndexStart = xA*B.numCols;
            int end = aIndexStart + B.numCols;
            int indexB = 0;
            for (int xB = 0; xB < B.numRows; xB++) {
                int indexA = aIndexStart;

                double total = 0;
                while (indexA < end) {
                    total += A.data[indexA++]*B.data[indexB++];
                }

                C.set(cIndex++, total);
            }
        }
        //CONCURRENT_ABOVE });
    }

    /**
     * @see CommonOps_DDRM#multAdd(org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multAdd_reorder( DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numCols, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        UtilEjml.assertShape(A.numRows == C.numRows && B.numCols == C.numCols, "C is not compatible with A and B");

        if (A.numCols == 0 || A.numRows == 0) {
            return;
        }
        final int endOfKLoop = B.numRows*B.numCols;

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numRows, i -> {
        for (int i = 0; i < A.numRows; i++) {
            int indexCbase = i*C.numCols;
            int indexA = i*A.numCols;

            // need to assign C.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + B.numCols;

            double valA = A.data[indexA++];

            while (indexB < end) {
                C.plus(indexC++, valA*B.data[indexB++]);
            }

            // now add to it
            while (indexB != endOfKLoop) { // k loop
                indexC = indexCbase;
                end = indexB + B.numCols;

                valA = A.data[indexA++];

                while (indexB < end) { // j loop
                    C.data[indexC++] += valA*B.data[indexB++];
                }
            }
        }
        //CONCURRENT_ABOVE });
    }

    /**
     * @see CommonOps_DDRM#multAdd(org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multAdd_small( DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numCols, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        UtilEjml.assertShape(A.numRows == C.numRows && B.numCols == C.numCols, "C is not compatible with A and B");

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numRows, i -> {
        for (int i = 0; i < A.numRows; i++) {
            int cIndex = i*B.numCols;
            int aIndexStart = i*A.numCols;
            for (int j = 0; j < B.numCols; j++) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + B.numRows;
                while (indexA < end) {
                    total += A.data[indexA++]*B.data[indexB];
                    indexB += B.numCols;
                }

                C.plus(cIndex++, total);
            }
        }
        //CONCURRENT_ABOVE });
    }

    //CONCURRENT_OMIT_BEGIN

    /**
     * @see CommonOps_DDRM#multAdd(org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multAdd_aux( DMatrix1Row A, DMatrix1Row B, DMatrix1Row C, @Nullable double[] aux ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numCols, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        UtilEjml.assertShape(A.numRows == C.numRows && B.numCols == C.numCols, "C is not compatible with A and B");

        if (aux == null) aux = new double[B.numRows];

        for (int j = 0; j < B.numCols; j++) {
            // create a copy of the column in B to avoid cache issues
            for (int k = 0; k < B.numRows; k++) {
                aux[k] = B.unsafe_get(k, j);
            }

            int indexA = 0;
            for (int i = 0; i < A.numRows; i++) {
                double total = 0;
                for (int k = 0; k < B.numRows; ) {
                    total += A.data[indexA++]*aux[k++];
                }
                C.plus(i*C.numCols + j, total);
            }
        }
    }
    //CONCURRENT_OMIT_END

    /**
     * @see CommonOps_DDRM#multAddTransA(org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multAddTransA_reorder( DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numRows, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        UtilEjml.assertShape(A.numCols == C.numRows && B.numCols == C.numCols, "C is not compatible with A and B");

        if (A.numCols == 0 || A.numRows == 0) {
            return;
        }
        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numRows, i -> {
        for (int i = 0; i < A.numCols; i++) {
            int indexC_start = i*C.numCols;

            // first assign R
            double valA = A.data[i];
            int indexB = 0;
            int end = indexB + B.numCols;
            int indexC = indexC_start;
            while (indexB < end) {
                C.plus(indexC++, valA*B.data[indexB++]);
            }
            // now increment it
            for (int k = 1; k < A.numRows; k++) {
                valA = A.unsafe_get(k, i);
                end = indexB + B.numCols;
                indexC = indexC_start;
                // this is the loop for j
                while (indexB < end) {
                    C.data[indexC++] += valA*B.data[indexB++];
                }
            }
        }
        //CONCURRENT_ABOVE });
    }

    /**
     * @see CommonOps_DDRM#multAddTransA(org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multAddTransA_small( DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numRows, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        UtilEjml.assertShape(A.numCols == C.numRows && B.numCols == C.numCols, "C is not compatible with A and B");

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numCols, i -> {
        for (int i = 0; i < A.numCols; i++) {
            int cIndex = i*B.numCols;
            for (int j = 0; j < B.numCols; j++) {
                int indexA = i;
                int indexB = j;
                int end = indexB + B.numRows*B.numCols;

                double total = 0;
                // loop for k
                for (; indexB < end; indexB += B.numCols) {
                    total += A.data[indexA]*B.data[indexB];
                    indexA += A.numCols;
                }

                C.plus(cIndex++, total);
            }
        }
        //CONCURRENT_ABOVE });
    }

    /**
     * @see CommonOps_DDRM#multAddTransAB(org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multAddTransAB( DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numRows, B.numCols, "The 'A' and 'B' matrices do not have compatible dimensions");
        UtilEjml.assertShape(A.numCols == C.numRows && B.numRows == C.numCols, "C is not compatible with A and B");


        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numCols, i -> {
        for (int i = 0; i < A.numCols; i++) {
            int cIndex = i*B.numRows;
            int indexB = 0;
            for (int j = 0; j < B.numRows; j++) {
                int indexA = i;
                int end = indexB + B.numCols;

                double total = 0;

                while (indexB < end) {
                    total += A.data[indexA]*B.data[indexB++];
                    indexA += A.numCols;
                }

                C.plus(cIndex++, total);
            }
        }
        //CONCURRENT_ABOVE });
    }

    //CONCURRENT_OMIT_BEGIN

    /**
     * @see CommonOps_DDRM#multAddTransAB(org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multAddTransAB_aux( DMatrix1Row A, DMatrix1Row B, DMatrix1Row C, @Nullable double[] aux ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numRows, B.numCols, "The 'A' and 'B' matrices do not have compatible dimensions");
        UtilEjml.assertShape(A.numCols == C.numRows && B.numRows == C.numCols, "C is not compatible with A and B");

        if (aux == null) aux = new double[A.numRows];

        if (A.numCols == 0 || A.numRows == 0) {
            return;
        }
        int indexC = 0;
        for (int i = 0; i < A.numCols; i++) {
            for (int k = 0; k < B.numCols; k++) {
                aux[k] = A.unsafe_get(k, i);
            }

            for (int j = 0; j < B.numRows; j++) {
                double total = 0;

                for (int k = 0; k < B.numCols; k++) {
                    total += aux[k]*B.unsafe_get(j, k);
                }
                C.plus(indexC++, total);
            }
        }
    }
    //CONCURRENT_OMIT_END

    /**
     * @see CommonOps_DDRM#multAddTransB(org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multAddTransB( DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numCols, B.numCols, "The 'A' and 'B' matrices do not have compatible dimensions");
        UtilEjml.assertShape(A.numRows == C.numRows && B.numRows == C.numCols, "C is not compatible with A and B");

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numRows, xA -> {
        for (int xA = 0; xA < A.numRows; xA++) {
            int cIndex = xA*B.numRows;
            int aIndexStart = xA*B.numCols;
            int end = aIndexStart + B.numCols;
            int indexB = 0;
            for (int xB = 0; xB < B.numRows; xB++) {
                int indexA = aIndexStart;

                double total = 0;
                while (indexA < end) {
                    total += A.data[indexA++]*B.data[indexB++];
                }

                C.plus(cIndex++, total);
            }
        }
        //CONCURRENT_ABOVE });
    }

    /**
     * @see CommonOps_DDRM#mult(double, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void mult_reorder( double alpha, DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numCols, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        C.reshape(A.numRows, B.numCols);

        if (A.numCols == 0 || A.numRows == 0) {
            CommonOps_DDRM.fill(C, 0);
            return;
        }
        final int endOfKLoop = B.numRows*B.numCols;

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numRows, i -> {
        for (int i = 0; i < A.numRows; i++) {
            int indexCbase = i*C.numCols;
            int indexA = i*A.numCols;

            // need to assign C.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + B.numCols;

            double valA = alpha*A.data[indexA++];

            while (indexB < end) {
                C.set(indexC++, valA*B.data[indexB++]);
            }

            // now add to it
            while (indexB != endOfKLoop) { // k loop
                indexC = indexCbase;
                end = indexB + B.numCols;

                valA = alpha*A.data[indexA++];

                while (indexB < end) { // j loop
                    C.data[indexC++] += valA*B.data[indexB++];
                }
            }
        }
        //CONCURRENT_ABOVE });
    }

    /**
     * @see CommonOps_DDRM#mult(double, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void mult_small( double alpha, DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numCols, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        C.reshape(A.numRows, B.numCols);

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numRows, i -> {
        for (int i = 0; i < A.numRows; i++) {
            int cIndex = i*B.numCols;
            int aIndexStart = i*A.numCols;
            for (int j = 0; j < B.numCols; j++) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + B.numRows;
                while (indexA < end) {
                    total += A.data[indexA++]*B.data[indexB];
                    indexB += B.numCols;
                }

                C.set(cIndex++, alpha*total);
            }
        }
        //CONCURRENT_ABOVE });
    }

    //CONCURRENT_OMIT_BEGIN

    /**
     * @see CommonOps_DDRM#mult(double, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void mult_aux( double alpha, DMatrix1Row A, DMatrix1Row B, DMatrix1Row C, @Nullable double[] aux ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numCols, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        C.reshape(A.numRows, B.numCols);

        if (aux == null) aux = new double[B.numRows];

        for (int j = 0; j < B.numCols; j++) {
            // create a copy of the column in B to avoid cache issues
            for (int k = 0; k < B.numRows; k++) {
                aux[k] = B.unsafe_get(k, j);
            }

            int indexA = 0;
            for (int i = 0; i < A.numRows; i++) {
                double total = 0;
                for (int k = 0; k < B.numRows; ) {
                    total += A.data[indexA++]*aux[k++];
                }
                C.set(i*C.numCols + j, alpha*total);
            }
        }
    }
    //CONCURRENT_OMIT_END

    /**
     * @see CommonOps_DDRM#multTransA(double, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multTransA_reorder( double alpha, DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numRows, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        C.reshape(A.numCols, B.numCols);

        if (A.numCols == 0 || A.numRows == 0) {
            CommonOps_DDRM.fill(C, 0);
            return;
        }
        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numRows, i -> {
        for (int i = 0; i < A.numCols; i++) {
            int indexC_start = i*C.numCols;

            // first assign R
            double valA = alpha*A.data[i];
            int indexB = 0;
            int end = indexB + B.numCols;
            int indexC = indexC_start;
            while (indexB < end) {
                C.set(indexC++, valA*B.data[indexB++]);
            }
            // now increment it
            for (int k = 1; k < A.numRows; k++) {
                valA = alpha*A.unsafe_get(k, i);
                end = indexB + B.numCols;
                indexC = indexC_start;
                // this is the loop for j
                while (indexB < end) {
                    C.data[indexC++] += valA*B.data[indexB++];
                }
            }
        }
        //CONCURRENT_ABOVE });
    }

    /**
     * @see CommonOps_DDRM#multTransA(double, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multTransA_small( double alpha, DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numRows, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        C.reshape(A.numCols, B.numCols);

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numCols, i -> {
        for (int i = 0; i < A.numCols; i++) {
            int cIndex = i*B.numCols;
            for (int j = 0; j < B.numCols; j++) {
                int indexA = i;
                int indexB = j;
                int end = indexB + B.numRows*B.numCols;

                double total = 0;
                // loop for k
                for (; indexB < end; indexB += B.numCols) {
                    total += A.data[indexA]*B.data[indexB];
                    indexA += A.numCols;
                }

                C.set(cIndex++, alpha*total);
            }
        }
        //CONCURRENT_ABOVE });
    }

    /**
     * @see CommonOps_DDRM#multTransAB(double, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multTransAB( double alpha, DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numRows, B.numCols, "The 'A' and 'B' matrices do not have compatible dimensions");
        C.reshape(A.numCols, B.numRows);


        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numCols, i -> {
        for (int i = 0; i < A.numCols; i++) {
            int cIndex = i*B.numRows;
            int indexB = 0;
            for (int j = 0; j < B.numRows; j++) {
                int indexA = i;
                int end = indexB + B.numCols;

                double total = 0;

                while (indexB < end) {
                    total += A.data[indexA]*B.data[indexB++];
                    indexA += A.numCols;
                }

                C.set(cIndex++, alpha*total);
            }
        }
        //CONCURRENT_ABOVE });
    }

    //CONCURRENT_OMIT_BEGIN

    /**
     * @see CommonOps_DDRM#multTransAB(double, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multTransAB_aux( double alpha, DMatrix1Row A, DMatrix1Row B, DMatrix1Row C, @Nullable double[] aux ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numRows, B.numCols, "The 'A' and 'B' matrices do not have compatible dimensions");
        C.reshape(A.numCols, B.numRows);

        if (aux == null) aux = new double[A.numRows];

        if (A.numCols == 0 || A.numRows == 0) {
            CommonOps_DDRM.fill(C, 0);
            return;
        }
        int indexC = 0;
        for (int i = 0; i < A.numCols; i++) {
            for (int k = 0; k < B.numCols; k++) {
                aux[k] = A.unsafe_get(k, i);
            }

            for (int j = 0; j < B.numRows; j++) {
                double total = 0;

                for (int k = 0; k < B.numCols; k++) {
                    total += aux[k]*B.unsafe_get(j, k);
                }
                C.set(indexC++, alpha*total);
            }
        }
    }
    //CONCURRENT_OMIT_END

    /**
     * @see CommonOps_DDRM#multTransB(double, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multTransB( double alpha, DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numCols, B.numCols, "The 'A' and 'B' matrices do not have compatible dimensions");
        C.reshape(A.numRows, B.numRows);

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numRows, xA -> {
        for (int xA = 0; xA < A.numRows; xA++) {
            int cIndex = xA*B.numRows;
            int aIndexStart = xA*B.numCols;
            int end = aIndexStart + B.numCols;
            int indexB = 0;
            for (int xB = 0; xB < B.numRows; xB++) {
                int indexA = aIndexStart;

                double total = 0;
                while (indexA < end) {
                    total += A.data[indexA++]*B.data[indexB++];
                }

                C.set(cIndex++, alpha*total);
            }
        }
        //CONCURRENT_ABOVE });
    }

    /**
     * @see CommonOps_DDRM#multAdd(double, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multAdd_reorder( double alpha, DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numCols, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        UtilEjml.assertShape(A.numRows == C.numRows && B.numCols == C.numCols, "C is not compatible with A and B");

        if (A.numCols == 0 || A.numRows == 0) {
            return;
        }
        final int endOfKLoop = B.numRows*B.numCols;

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numRows, i -> {
        for (int i = 0; i < A.numRows; i++) {
            int indexCbase = i*C.numCols;
            int indexA = i*A.numCols;

            // need to assign C.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + B.numCols;

            double valA = alpha*A.data[indexA++];

            while (indexB < end) {
                C.plus(indexC++, valA*B.data[indexB++]);
            }

            // now add to it
            while (indexB != endOfKLoop) { // k loop
                indexC = indexCbase;
                end = indexB + B.numCols;

                valA = alpha*A.data[indexA++];

                while (indexB < end) { // j loop
                    C.data[indexC++] += valA*B.data[indexB++];
                }
            }
        }
        //CONCURRENT_ABOVE });
    }

    /**
     * @see CommonOps_DDRM#multAdd(double, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multAdd_small( double alpha, DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numCols, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        UtilEjml.assertShape(A.numRows == C.numRows && B.numCols == C.numCols, "C is not compatible with A and B");

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numRows, i -> {
        for (int i = 0; i < A.numRows; i++) {
            int cIndex = i*B.numCols;
            int aIndexStart = i*A.numCols;
            for (int j = 0; j < B.numCols; j++) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + B.numRows;
                while (indexA < end) {
                    total += A.data[indexA++]*B.data[indexB];
                    indexB += B.numCols;
                }

                C.plus(cIndex++, alpha*total);
            }
        }
        //CONCURRENT_ABOVE });
    }

    //CONCURRENT_OMIT_BEGIN

    /**
     * @see CommonOps_DDRM#multAdd(double, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multAdd_aux( double alpha, DMatrix1Row A, DMatrix1Row B, DMatrix1Row C, @Nullable double[] aux ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numCols, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        UtilEjml.assertShape(A.numRows == C.numRows && B.numCols == C.numCols, "C is not compatible with A and B");

        if (aux == null) aux = new double[B.numRows];

        for (int j = 0; j < B.numCols; j++) {
            // create a copy of the column in B to avoid cache issues
            for (int k = 0; k < B.numRows; k++) {
                aux[k] = B.unsafe_get(k, j);
            }

            int indexA = 0;
            for (int i = 0; i < A.numRows; i++) {
                double total = 0;
                for (int k = 0; k < B.numRows; ) {
                    total += A.data[indexA++]*aux[k++];
                }
                C.plus(i*C.numCols + j, alpha*total);
            }
        }
    }
    //CONCURRENT_OMIT_END

    /**
     * @see CommonOps_DDRM#multAddTransA(double, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multAddTransA_reorder( double alpha, DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numRows, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        UtilEjml.assertShape(A.numCols == C.numRows && B.numCols == C.numCols, "C is not compatible with A and B");

        if (A.numCols == 0 || A.numRows == 0) {
            return;
        }
        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numRows, i -> {
        for (int i = 0; i < A.numCols; i++) {
            int indexC_start = i*C.numCols;

            // first assign R
            double valA = alpha*A.data[i];
            int indexB = 0;
            int end = indexB + B.numCols;
            int indexC = indexC_start;
            while (indexB < end) {
                C.plus(indexC++, valA*B.data[indexB++]);
            }
            // now increment it
            for (int k = 1; k < A.numRows; k++) {
                valA = alpha*A.unsafe_get(k, i);
                end = indexB + B.numCols;
                indexC = indexC_start;
                // this is the loop for j
                while (indexB < end) {
                    C.data[indexC++] += valA*B.data[indexB++];
                }
            }
        }
        //CONCURRENT_ABOVE });
    }

    /**
     * @see CommonOps_DDRM#multAddTransA(double, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multAddTransA_small( double alpha, DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numRows, B.numRows, "The 'A' and 'B' matrices do not have compatible dimensions");
        UtilEjml.assertShape(A.numCols == C.numRows && B.numCols == C.numCols, "C is not compatible with A and B");

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numCols, i -> {
        for (int i = 0; i < A.numCols; i++) {
            int cIndex = i*B.numCols;
            for (int j = 0; j < B.numCols; j++) {
                int indexA = i;
                int indexB = j;
                int end = indexB + B.numRows*B.numCols;

                double total = 0;
                // loop for k
                for (; indexB < end; indexB += B.numCols) {
                    total += A.data[indexA]*B.data[indexB];
                    indexA += A.numCols;
                }

                C.plus(cIndex++, alpha*total);
            }
        }
        //CONCURRENT_ABOVE });
    }

    /**
     * @see CommonOps_DDRM#multAddTransAB(double, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multAddTransAB( double alpha, DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numRows, B.numCols, "The 'A' and 'B' matrices do not have compatible dimensions");
        UtilEjml.assertShape(A.numCols == C.numRows && B.numRows == C.numCols, "C is not compatible with A and B");


        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numCols, i -> {
        for (int i = 0; i < A.numCols; i++) {
            int cIndex = i*B.numRows;
            int indexB = 0;
            for (int j = 0; j < B.numRows; j++) {
                int indexA = i;
                int end = indexB + B.numCols;

                double total = 0;

                while (indexB < end) {
                    total += A.data[indexA]*B.data[indexB++];
                    indexA += A.numCols;
                }

                C.plus(cIndex++, alpha*total);
            }
        }
        //CONCURRENT_ABOVE });
    }

    //CONCURRENT_OMIT_BEGIN

    /**
     * @see CommonOps_DDRM#multAddTransAB(double, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multAddTransAB_aux( double alpha, DMatrix1Row A, DMatrix1Row B, DMatrix1Row C, @Nullable double[] aux ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numRows, B.numCols, "The 'A' and 'B' matrices do not have compatible dimensions");
        UtilEjml.assertShape(A.numCols == C.numRows && B.numRows == C.numCols, "C is not compatible with A and B");

        if (aux == null) aux = new double[A.numRows];

        if (A.numCols == 0 || A.numRows == 0) {
            return;
        }
        int indexC = 0;
        for (int i = 0; i < A.numCols; i++) {
            for (int k = 0; k < B.numCols; k++) {
                aux[k] = A.unsafe_get(k, i);
            }

            for (int j = 0; j < B.numRows; j++) {
                double total = 0;

                for (int k = 0; k < B.numCols; k++) {
                    total += aux[k]*B.unsafe_get(j, k);
                }
                C.plus(indexC++, alpha*total);
            }
        }
    }
    //CONCURRENT_OMIT_END

    /**
     * @see CommonOps_DDRM#multAddTransB(double, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row, org.ejml.data.DMatrix1Row)
     */
    public static void multAddTransB( double alpha, DMatrix1Row A, DMatrix1Row B, DMatrix1Row C ) {
        UtilEjml.assertTrue(A != C && B != C, "Neither 'A' or 'B' can be the same matrix as 'C'");
        UtilEjml.assertShape(A.numCols, B.numCols, "The 'A' and 'B' matrices do not have compatible dimensions");
        UtilEjml.assertShape(A.numRows == C.numRows && B.numRows == C.numCols, "C is not compatible with A and B");

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, A.numRows, xA -> {
        for (int xA = 0; xA < A.numRows; xA++) {
            int cIndex = xA*B.numRows;
            int aIndexStart = xA*B.numCols;
            int end = aIndexStart + B.numCols;
            int indexB = 0;
            for (int xB = 0; xB < B.numRows; xB++) {
                int indexA = aIndexStart;

                double total = 0;
                while (indexA < end) {
                    total += A.data[indexA++]*B.data[indexB++];
                }

                C.plus(cIndex++, alpha*total);
            }
        }
        //CONCURRENT_ABOVE });
    }
}