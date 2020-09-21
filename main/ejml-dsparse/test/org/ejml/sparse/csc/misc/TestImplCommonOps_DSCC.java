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

package org.ejml.sparse.csc.misc;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.data.IGrowArray;
import org.ejml.ops.DConvertMatrixStruct;
import org.ejml.ops.IPredicateBinary;
import org.ejml.ops.IPredicatesBinary;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.MatrixFeatures_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.ejml.sparse.triplet.RandomMatrices_DSTL;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestImplCommonOps_DSCC {

    private final Random rand = new Random(324);

    @Test
    public void transpose() {
        for (int rows = 1; rows <= 10; rows++) {
            for (int cols = 1; rows <= 10; rows++) {
                for (int mc = 0; mc < 20; mc++) {
                    int N = (int)Math.round(rows*cols*rand.nextDouble());
                    DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(rows, cols, N, -1, 1, rand);
                    DMatrixSparseCSC b = new DMatrixSparseCSC(0, 0, 0);

                    ImplCommonOps_DSCC.transpose(a, b, null);
                    assertEquals(cols, b.numRows);
                    assertEquals(rows, b.numCols);

                    for (int row = 0; row < rows; row++) {
                        for (int col = 0; col < cols; col++) {
                            double expected = a.get(row, col);
                            double found = b.get(col, row);

                            assertEquals(expected, found, UtilEjml.TEST_F64, row + " " + col);
                        }
                    }
                    assertTrue(CommonOps_DSCC.checkSortedFlag(b));
                }
            }
        }
    }

    @Test
    public void add() {
        double alpha = 1.5;
        double beta = 2.3;

        for (int numRows : new int[]{2, 4, 6, 10}) {
            for (int numCols : new int[]{2, 4, 6, 10}) {
                DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(numRows, numCols, 7, -1, 1, rand);
                DMatrixSparseCSC b = RandomMatrices_DSCC.rectangle(numRows, numCols, 8, -1, 1, rand);
                DMatrixSparseCSC c = RandomMatrices_DSCC.rectangle(numRows, numCols, 3, -1, 1, rand);

                ImplCommonOps_DSCC.add(alpha, a, beta, b, c, null, null);

                for (int row = 0; row < numRows; row++) {
                    for (int col = 0; col < numCols; col++) {
                        double valA = a.get(row, col);
                        double valB = b.get(row, col);
                        double found = alpha*valA + beta*valB;

                        double expected = c.get(row, col);

                        assertEquals(expected, found, UtilEjml.TEST_F64, row + " " + col);
                    }
                }
                assertTrue(CommonOps_DSCC.checkStructure(c));
            }
        }
    }

    @Test
    public void addColAppend() {
        double alpha = 1.5;
        double beta = 2.3;

        for (int numRows : new int[]{2, 4, 6, 10}) {
            DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(numRows, 3, 15, -1, 1, rand);
            DMatrixSparseCSC b = RandomMatrices_DSCC.rectangle(numRows, 4, 15, -1, 1, rand);
            DMatrixSparseCSC c = new DMatrixSparseCSC(numRows, 0, 0);


            ImplCommonOps_DSCC.addColAppend(alpha, a, 1, beta, b, 0, c, null);
            assertTrue(CommonOps_DSCC.checkStructure(c));
            assertEquals(1, c.numCols);

            for (int row = 0; row < numRows; row++) {
                double valA = a.get(row, 1);
                double valB = b.get(row, 0);
                double found = alpha*valA + beta*valB;

                double expected = c.get(row, 0);
                assertEquals(expected, found, UtilEjml.TEST_F64);
            }

            ImplCommonOps_DSCC.addColAppend(alpha, a, 2, beta, b, 1, c, null);
            assertTrue(CommonOps_DSCC.checkStructure(c));
            assertEquals(2, c.numCols);

            for (int row = 0; row < numRows; row++) {
                double valA = a.get(row, 2);
                double valB = b.get(row, 1);
                double found = alpha*valA + beta*valB;

                double expected = c.get(row, 1);
                assertEquals(expected, found, UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void removeZeros_two() {
        DMatrixSparseCSC A = new DMatrixSparseCSC(2, 3, 2);
        A.indicesSorted = true;
        A.set(0, 1, 0.1);
        A.set(1, 1, 0.0);
        A.set(1, 2, 0.3);
        DMatrixSparseCSC A_orig = A.copy();

        DMatrixSparseCSC B = new DMatrixSparseCSC(1, 1, 1);

        ImplCommonOps_DSCC.removeZeros(A, B, 0);
        assertTrue(CommonOps_DSCC.checkStructure(B));
        assertEquals(A.numRows, B.numRows);
        assertEquals(A.numCols, B.numCols);
        assertEquals(2, B.nz_length);
        EjmlUnitTests.assertEquals(A, B, UtilEjml.TEST_F64);
        assertTrue(MatrixFeatures_DSCC.isEquals(A, A_orig, UtilEjml.TEST_F64));

        ImplCommonOps_DSCC.removeZeros(A, B, 0.1);
        assertEquals(1, B.nz_length);
        assertEquals(0, B.get(0, 1), UtilEjml.TEST_F64);
        assertEquals(A.get(1, 1), B.get(1, 1), UtilEjml.TEST_F64);
        assertTrue(MatrixFeatures_DSCC.isEquals(A, A_orig, UtilEjml.TEST_F64));
    }

    @Test
    public void removeZeros_one() {
        DMatrixSparseCSC A = new DMatrixSparseCSC(2, 3, 2);
        A.set(0, 1, 0.1);
        A.set(1, 1, 0.0);
        A.set(1, 2, 0.3);

        ImplCommonOps_DSCC.removeZeros(A, 0);
        assertTrue(CommonOps_DSCC.checkStructure(A));
        assertEquals(A.numRows, 2);
        assertEquals(A.numCols, 3);
        assertEquals(2, A.nz_length);
        assertEquals(0.1, A.get(0, 1), UtilEjml.TEST_F64);
        assertEquals(0.3, A.get(1, 2), UtilEjml.TEST_F64);

        ImplCommonOps_DSCC.removeZeros(A, 0.1);
        assertTrue(CommonOps_DSCC.checkStructure(A));
        assertEquals(A.numRows, 2);
        assertEquals(A.numCols, 3);
        assertEquals(1, A.nz_length);
        assertEquals(0.3, A.get(1, 2), UtilEjml.TEST_F64);
    }

    @Test
    public void removeZeros_one_random() {
        for (int i = 0; i < 40; i++) {
            int rows = rand.nextInt(10) + 1;
            int cols = rand.nextInt(10) + 1;

            int nz = RandomMatrices_DSCC.nonzero(rows, cols, 0.02, 0.5, rand);
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(rows, cols, nz, -1, 1, rand);

            ImplCommonOps_DSCC.removeZeros(A, 0.2);
            assertTrue(CommonOps_DSCC.checkStructure(A));
            assertEquals(A.numRows, rows);
            assertEquals(A.numCols, cols);
            for (int j = 0; j < rows; j++) {
                for (int k = 0; k < cols; k++) {
                    double val = A.get(j, k);
                    assertTrue(Math.abs(val) > 0.2 || val == 0, "val = " + val);
                }
            }
        }
    }

    @Test
    public void symmLowerToFull() {
        IGrowArray gw = new IGrowArray();
        int[] sizes = {1, 2, 5, 10};

        for (int i = 0; i < 20; i++) {
            for (int N : sizes) {
                int nz = RandomMatrices_DSCC.nonzero(N, N/2, 0.1, 1.0, rand);
                DMatrixSparseCSC A = RandomMatrices_DSCC.triangleLower(N, 0, nz, -1, 1, rand);
                DMatrixSparseCSC B = new DMatrixSparseCSC(0, 0);

                ImplCommonOps_DSCC.symmLowerToFull(A, B, gw);
                assertTrue(CommonOps_DSCC.checkStructure(B));

                for (int row = 0; row < N; row++) {
                    for (int col = 0; col <= row; col++) {
//                        System.out.println(row+" "+col);
                        assertEquals(A.get(row, col), B.get(row, col), UtilEjml.TEST_F64);
                        assertEquals(A.get(row, col), B.get(col, row), UtilEjml.TEST_F64);
                    }
                }
            }
        }
    }

    @Test
    void duplicatesAdd() {
        IGrowArray gw = new IGrowArray();
        int[] sizes = {1, 2, 5, 10};
        for (int i = 0; i < 20; i++) {
            for (int N : sizes) {
                DMatrixSparseTriplet triplet = RandomMatrices_DSTL.uniform(N, N, (N*N)/2, -1, 1, rand);
                DMatrixSparseCSC A = DConvertMatrixStruct.convert(triplet, (DMatrixSparseCSC)null);
                DMatrixSparseCSC B = A.copy();

                // first pass there should be no change
                ImplCommonOps_DSCC.duplicatesAdd(B, null);
                assertTrue(CommonOps_DSCC.checkStructure(B));
                A.sortIndices(null);
                B.sortIndices(null);
                assertTrue(MatrixFeatures_DSCC.isEquals(A, B));

                // Second pass there will be a lot of duplicates
                int nz = triplet.nz_length;
                for (int j = 0; j < nz; j++) {
                    int row = triplet.nz_rowcol.data[j*2];
                    int col = triplet.nz_rowcol.data[j*2 + 1];
                    double value = triplet.nz_value.data[j];
                    triplet.addItem(row, col, value);
                }
                DConvertMatrixStruct.convert(triplet, B);
                // Remove duplicates and see if B has elements that are twice the size of elements in A
                ImplCommonOps_DSCC.duplicatesAdd(B, gw);
                assertTrue(CommonOps_DSCC.checkStructure(B));
                CommonOps_DSCC.divide(B, 2.0, B);
                B.sortIndices(null);
                assertTrue(MatrixFeatures_DSCC.isEquals(A, B, UtilEjml.TEST_F64));
            }
        }
    }

    @Test
    public void select() {
        int dim = 5;
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(dim, dim, 10, 1, 2, rand);
        DMatrixSparseCSC B = A.copy();

        IPredicateBinary selector = IPredicatesBinary.higherTriangle;
        ImplCommonOps_DSCC.select(B, B, selector);

        for (int row = 0; row < dim; row++) {
            for (int col = 0; col < dim; col++) {
                if (selector.apply(row, col)) {
                    assertEquals(A.get(row, col), B.get(row, col));
                } else {
                    assertFalse(B.isAssigned(row, col));
                }
            }
        }
    }
}
