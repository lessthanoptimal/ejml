/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.linsol.chol;

import org.ejml.EjmlStandardJUnit;
import org.ejml.EjmlUnitTests;
import org.ejml.LinearSolverSafe;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public abstract class BaseCholeskySolveTests_DDRM extends EjmlStandardJUnit {
    public abstract LinearSolverDense<DMatrixRMaj> createSolver();

    public LinearSolverDense<DMatrixRMaj> createSafeSolver() {
        LinearSolverDense<DMatrixRMaj> solver = createSolver();
        return new LinearSolverSafe<DMatrixRMaj>(solver);
    }

    @Test
    public void setA_dimensionCheck() {

        LinearSolverDense<DMatrixRMaj> solver = createSafeSolver();

        try {
            DMatrixRMaj A = RandomMatrices_DDRM.rectangle(4,5,rand);
            assertTrue(solver.setA(A));
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    @Test
    public void solve_dimensionCheck() {

        LinearSolverDense<DMatrixRMaj> solver = createSafeSolver();

        DMatrixRMaj A = RandomMatrices_DDRM.symmetricPosDef(4, rand);
        assertTrue(solver.setA(A));

        {
            DMatrixRMaj x = RandomMatrices_DDRM.rectangle(4,3,rand);
            DMatrixRMaj b = RandomMatrices_DDRM.rectangle(4,2,rand);
            solver.solve(b,x);
            assertEquals(x.numCols,b.numCols);
        }

        {
            DMatrixRMaj x = RandomMatrices_DDRM.rectangle(5,2,rand);
            DMatrixRMaj b = RandomMatrices_DDRM.rectangle(4,2,rand);
            solver.solve(b,x);
            assertEquals(x.numRows,b.numRows);
        }

        try {
            DMatrixRMaj x = RandomMatrices_DDRM.rectangle(5,2,rand);
            DMatrixRMaj b = RandomMatrices_DDRM.rectangle(5,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    @Test
    public void testSolve() {

        LinearSolverDense<DMatrixRMaj> solver = createSafeSolver();

        DMatrixRMaj A = new DMatrixRMaj(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);
        DMatrixRMaj b = new DMatrixRMaj(3,1, true, 17, 97, 320);
        DMatrixRMaj x = RandomMatrices_DDRM.rectangle(3,1,rand);
        DMatrixRMaj A_orig = A.copy();
        DMatrixRMaj B_orig = b.copy();

        assertTrue(solver.setA(A));
        solver.solve(b,x);

        // see if the input got modified
        EjmlUnitTests.assertEquals(A,A_orig,UtilEjml.TEST_F64_SQ);
        EjmlUnitTests.assertEquals(b,B_orig,UtilEjml.TEST_F64_SQ);

        DMatrixRMaj x_expected = new DMatrixRMaj(3,1, true, 1, 2, 3);

        EjmlUnitTests.assertEquals(x_expected,x,UtilEjml.TEST_F64_SQ);
    }

    @Test
    public void testInvert() {

        LinearSolverDense<DMatrixRMaj> solver = createSafeSolver();

        DMatrixRMaj A = new DMatrixRMaj(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);
        DMatrixRMaj found = new DMatrixRMaj(A.numRows,A.numCols);

        assertTrue(solver.setA(A));
        solver.invert(found);

        DMatrixRMaj A_inv = new DMatrixRMaj(3,3, true, 1.453515, -0.199546, -0.013605, -0.199546, 0.167800, -0.034014, -0.013605, -0.034014, 0.020408);

        EjmlUnitTests.assertEquals(A_inv,found,UtilEjml.TEST_F64_SQ);
    }

    @Test
    public void testQuality() {

        LinearSolverDense<DMatrixRMaj> solver = createSafeSolver();

        DMatrixRMaj A = CommonOps_DDRM.diag(3,2,1);
        DMatrixRMaj B = CommonOps_DDRM.diag(3,2,0.001);

        assertTrue(solver.setA(A));
        double qualityA = (double)solver.quality();

        assertTrue(solver.setA(B));
        double qualityB = (double)solver.quality();

        assertTrue(qualityB < qualityA);
    }

    @Test
    public void testQuality_scale() {

        LinearSolverDense<DMatrixRMaj> solver = createSafeSolver();

        DMatrixRMaj A = CommonOps_DDRM.diag(3,2,1);
        DMatrixRMaj B = A.copy();
        CommonOps_DDRM.scale(0.001,B);

        assertTrue(solver.setA(A));
        double qualityA = (double)solver.quality();

        assertTrue(solver.setA(B));
        double qualityB = (double)solver.quality();

        assertEquals(qualityB,qualityA, UtilEjml.TEST_F64);
    }

    public DMatrixRMaj createA( int size ) {
        return RandomMatrices_DDRM.symmetricPosDef(size,rand);
    }

    @Test
    public void randomSolveable() {
        LinearSolverDense<DMatrixRMaj> solver = createSolver();

            for (int N : new int[]{1, 2, 5, 10, 20}) {
                for (int mc = 0; mc < 30; mc++) {
//                    System.out.println("-=-=-=-=-=-=-=-=      "+N+" mc "+mc);
                    DMatrixRMaj A = createA(N);
                    DMatrixRMaj A_cpy = A.copy();
                    DMatrixRMaj B = new DMatrixRMaj(A.numRows, 3);
                    DMatrixRMaj X = new DMatrixRMaj(A.numCols, 3);
                    DMatrixRMaj foundB = new DMatrixRMaj(A.numCols, 3);

                    DMatrixRMaj B_cpy = B.copy();

                    assertTrue(solver.setA(A));
                    solver.solve(B, X);
                    CommonOps_DDRM.mult(A, X, foundB);

                    EjmlUnitTests.assertEquals(B_cpy, foundB, UtilEjml.TEST_F64);

                    if( !solver.modifiesA() ) {
                        EjmlUnitTests.assertEquals(A, A_cpy, UtilEjml.TEST_F64);
                    }
                    if( !solver.modifiesB() ) {
                        EjmlUnitTests.assertEquals(B, B_cpy, UtilEjml.TEST_F64);
                    }
                }
            }
    }
}
