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

package org.ejml.sparse.csc.linsol.lu;

import org.ejml.EjmlUnitTests;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.ops.ConvertDMatrixStruct;
import org.ejml.sparse.ComputePermutation;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.ejml.sparse.csc.decomposition.lu.LuUpLooking_DSCC;
import org.ejml.sparse.csc.factory.FillReductionFactory_DSCC;
import org.ejml.sparse.csc.linsol.GenericLinearSolverSparseTests_DSCC;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestLinearSolverLu_DSCC extends GenericLinearSolverSparseTests_DSCC {

    public TestLinearSolverLu_DSCC() {
        canDecomposeZeros = false;
        canLockStructure = false;
    }

    @Override
    public LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> createSolver( FillReducing permutation ) {
        ComputePermutation<DMatrixSparseCSC> cp = FillReductionFactory_DSCC.create(permutation);
        LuUpLooking_DSCC lu = new LuUpLooking_DSCC(cp);
        return new LinearSolverLu_DSCC(lu);
    }

    @Override
    public DMatrixSparseCSC createA( int size ) {
        // easy to make a matrix which is square and not singular or nearly if it's SPD
        return RandomMatrices_DSCC.symmetricPosDef(size, 0.25, rand);
    }

    @Test
    public void testCase0() {
        DMatrixSparseCSC A = ConvertDMatrixStruct.convert(A0_dense, (DMatrixSparseCSC)null, 0);

        // sparse solvers appear to be more sensitive to numerical issues than their dense counter parts
        // skip this test if single precision
        if (A.getType().getBits() == 32)
            return;

        LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver = createSolver(FillReducing.NONE);

        DMatrixRMaj X = create(A.numCols, 3);
        DMatrixRMaj foundX = create(A.numCols, 3);
        DMatrixRMaj B = new DMatrixRMaj(A.numRows, 3);

        CommonOps_DSCC.mult(A, X, B);

        assertTrue(solver.setA(A));
        solver.solve(B, foundX);

        EjmlUnitTests.assertRelativeEquals(X, foundX, equalityTolerance);
    }

    // This matrix was found to cause the csparse algorithm to produce numerically unstable results due to poor
    // pivots being selected
    DMatrixRMaj A0_dense = DMatrixRMaj.wrap(24, 24, new double[]{
            0.0, 0.0, 0.0, 97.65625, -195.3125, -312.5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 39.0625, -62.5, -75.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 15.625, -18.75, -15.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 2.0, 6.25, -5.0,
            -2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0,
            0.0, 2.5, -1.0, -0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 1.0, 0.0, -0.0, 1.0, -0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 195.3125, 312.5, 97.65625, 3125.0, -3125.0, -2500.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 195.3125, 312.5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 62.5, 75.0, 39.0625,
            625.0, -500.0, -300.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 62.5, 75.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 18.75, 15.0, 15.625, 125.0, -75.0, -30.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 18.75, 15.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 5.0, 2.0, 6.25, 25.0, -10.0, -2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 5.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 2.5, 5.0, -1.0, -0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.0, 1.0, 1.0, -0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 3125.0, 2500.0, 3125.0, 23730.46875, -15820.3125, -8437.5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 3125.0,
            2500.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 500.0, 300.0, 625.0, 3164.0625, -1687.5,
            -675.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 500.0, 300.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 75.0,
            30.0, 125.0, 421.875, -168.75, -45.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 75.0, 30.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 10.0, 2.0, 25.0, 56.25, -15.0, -2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 10.0, 2.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 5.0, 7.5, -1.0, -0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.0, 1.0, 1.0, -0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, -0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 15820.3125,
            8437.5, 23730.46875, 100000.0, 50000.0, 20000.0, 0.0, 0.0, 0.0, 0.0, 15820.3125, 8437.5, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1687.5, 675.0, 3164.0625, 10000.0, 4000.0, 1200.0, 0.0, 0.0, 0.0,
            0.0, 1687.5, 675.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 168.75, 45.0, 421.875,
            1000.0, 300.0, 60.0, 0.0, 0.0, 0.0, 0.0, 168.75, 45.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 15.0, 2.0, 56.25, 100.0, 20.0, 2.0, 0.0, 0.0, 0.0, 0.0, 15.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 7.5, 10.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.0, 1.0, 1.0, 0.0, -0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.0
    });
}