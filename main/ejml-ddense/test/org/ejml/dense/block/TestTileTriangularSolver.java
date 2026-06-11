/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.block;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.SpecializedOps_DDRM;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class TestTileTriangularSolver extends EjmlStandardJUnit {
    @Test void invertArray() {
        Method[] methods = TileTriangularSolver_F64.class.getMethods();

        int numFound = 0;
        for (Method m : methods) {
            String name = m.getName();

            if (!name.startsWith("invert"))
                continue;

            boolean upper = name.contains("Upper");
            boolean tran = name.endsWith("Tran");
            boolean inPlace = m.getParameterCount() == 3;

//            System.out.println("name "+m.getName()+" "+upper+" "+tran+" "+inPlace);
            check_invert_array(m, 3, upper, tran, inPlace);
            check_invert_array(m, 8, upper, tran, inPlace);

            numFound++;
        }

        assertEquals(5, numFound);
    }

    private void check_invert_array( Method m, int size, boolean upper, boolean tran, boolean inPlace ) {
        int offset = 2;

        // Random matrix. This fills in the triangle and outside area so that we can verify that the outside
        // area has not been modified
        DMatrixRMaj T = RandomMatrices_DDRM.rectangle(size, size, -1, 1, rand);

        // Ensure the triangular system is not degenerate
        ensureNotDegenerate(T);

        double[] dataT = offsetArray(T.data, offset);
        double[] dataT_orig = dataT.clone();

        double[] dataTinv;
        if (inPlace) {
            // In-place: result overwrites input's triangle. Opposite triangle must remain untouched
            invokeInvert(m, dataT, dataT, size, offset, offset, true);
            dataTinv = dataT;
            assertOppositeTriangleEquals(dataT, dataT_orig, size, offset, upper);
        } else {
            // Two-array: prefill output with random values to verify untouched cells are preserved.
            DMatrixRMaj Tinv_init = RandomMatrices_DDRM.rectangle(size, size, -1, 1, rand);
            dataTinv = offsetArray(Tinv_init.data, offset);
            double[] dataTinv_orig = dataTinv.clone();

            invokeInvert(m, dataT, dataTinv, size, offset, offset, false);

            // Input must not have been modified.
            assertArrayEquals(dataT_orig, dataT, 0.0);

            // Output's opposite triangle must match its prefilled values.
            boolean outUpper = tran != upper;
            assertOppositeTriangleEquals(dataTinv, dataTinv_orig, size, offset, tran != upper);
        }

        // Verify correctness: T * T_inv = I (or T^T * T_inv = I for tran variants).
        DMatrixRMaj T_inv = toMatrix(dataTinv, size, offset);
        SpecializedOps_DDRM.fillTriangle(T_inv, tran == upper, 1, 0.0);

        DMatrixRMaj T_clean = T.copy();
        SpecializedOps_DDRM.fillTriangle(T_clean, !upper, 1, 0.0);

        var S = new DMatrixRMaj(size, size);
        if (tran) {
            CommonOps_DDRM.multTransA(T_clean, T_inv, S);
        } else {
            CommonOps_DDRM.mult(T_clean, T_inv, S);
        }
        assertTrue(MatrixFeatures_DDRM.isIdentity(S, UtilEjml.TEST_F64));

        // Two-array variants must also work when the same array is passed for input and output.
        if (!inPlace) {
            invokeInvert(m, dataT_orig, dataT_orig, size, offset, offset, false);
            DMatrixRMaj aliasedResult = toMatrix(dataT_orig, size, offset);
            assertTrue(MatrixFeatures_DDRM.isEqualsTriangle(T_inv, aliasedResult, tran != upper, UtilEjml.TEST_F64));
        }
    }

    private DMatrixRMaj toMatrix( double[] data, int size, int offset ) {
        DMatrixRMaj m = new DMatrixRMaj(size, size);
        System.arraycopy(data, offset, m.data, 0, size*size);
        return m;
    }

    private void invokeInvert( Method m, double[] dataT, double[] dataTinv,
                               int size, int offsetT, int offsetTinv, boolean inPlace ) {
        try {
            if (inPlace) {
                m.invoke(null, dataT, size, offsetT);
            } else {
                m.invoke(null, dataT, dataTinv, size, offsetT, offsetTinv);
            }
        } catch (IllegalAccessException e) {
            fail("invoke failed");
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    /// Verifies that elements outside the result triangle of an inverter's output match the
    /// reference array. Used to confirm the function did not write outside its declared region.
    ///
    /// @param resultUpper True if the result lives in the upper triangle (so the lower is checked).
    private void assertOppositeTriangleEquals( double[] data, double[] orig, int size, int offset, boolean resultUpper ) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                boolean inOpposite = resultUpper ? (j < i) : (j > i);
                if (inOpposite) {
                    assertEquals(orig[offset + i*size + j], data[offset + i*size + j], 0.0);
                }
            }
        }
    }

    /**
     * Test all inner block solvers using reflections to look up the functions
     */
    @Test void solveArray() {
        Method[] methods = TileTriangularSolver_F64.class.getMethods();

        int numFound = 0;
        for (Method m : methods) {
            String name = m.getName();

            if (!name.contains("solve") || name.compareTo("solve") == 0 || name.compareTo("solveBlock") == 0)
                continue;

//            System.out.println("name = "+name);

            boolean leftSolver = name.startsWith("lsolve");
            boolean solveL = name.substring(6).startsWith("Low");
            boolean transT = name.substring(9).startsWith("Trans");
            boolean transB = name.endsWith("BTrans");

            check_solve_array(m, 3, leftSolver, solveL, transT, transB);
            check_solve_array(m, 8, leftSolver, solveL, transT, transB);

            numFound++;
        }

        // make sure all the functions were in fact tested
        assertEquals(16, numFound);
    }

    /**
     * Checks to see if solve functions that use arrays as input work correctly.
     */
    private void check_solve_array( Method m, int triSize, boolean leftSolver,
                                    boolean solveL, boolean transT, boolean transB ) {
        int offsetL = 2;
        int offsetB = 3;

        int bLength = triSize + 1;
        DMatrixRMaj L = createRandomLowerTriangular(triSize);

        ensureNotDegenerate(L);

        int rowB = leftSolver ? triSize : bLength;
        int colB = leftSolver ? bLength : triSize;
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(rowB, colB, rand);
        DMatrixRMaj expected = RandomMatrices_DDRM.rectangle(rowB, colB, rand);

        if (!solveL) {
            CommonOps_DDRM.transpose(L);
        }

        if (transT) {
            CommonOps_DDRM.transpose(L);
        }

        if (leftSolver) {
            CommonOps_DDRM.mult(L, expected, B);
        } else {
            CommonOps_DDRM.mult(expected, L, B);
        }

        DMatrixRMaj found = B.copy();

        if (transT) {
            CommonOps_DDRM.transpose(L);
        }

        if (transB) {
            CommonOps_DDRM.transpose(found);
            CommonOps_DDRM.transpose(expected);
        }

        // create arrays that are offset from the original
        // use two different offsets to make sure it doesn't confuse them internally
        double[] dataL = offsetArray(L.data, offsetL);
        double[] dataB = offsetArray(found.data, offsetB);

        try {
            m.invoke(null, dataL, dataB, triSize, bLength, triSize, offsetL, offsetB);
        } catch (IllegalAccessException e) {
            fail("invoke failed");
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }

        // put the solution into B, minus the offset
        System.arraycopy(dataB, offsetB, found.data, 0, found.data.length);

        assertTrue(MatrixFeatures_DDRM.isIdentical(expected, found, UtilEjml.TEST_F64));
    }

    private static void ensureNotDegenerate( DMatrixRMaj L ) {
        // Ensure the triangular system is not degenerate
        for (int i = 0; i < L.numRows; i++) {
            double d = L.get(i, i);
            L.set(i, i, d >= 0 ? d + 1.5 : d - 1.5);
        }
    }

    private DMatrixRMaj createRandomLowerTriangular( int N ) {
        DMatrixRMaj U = RandomMatrices_DDRM.triangularUpper(N, 0, -1, 1, rand);

        CommonOps_DDRM.transpose(U);

        return U;
    }

    private double[] offsetArray( double[] orig, int offset ) {
        double[] ret = new double[orig.length + offset];

        System.arraycopy(orig, 0, ret, offset, orig.length);

        return ret;
    }
}
