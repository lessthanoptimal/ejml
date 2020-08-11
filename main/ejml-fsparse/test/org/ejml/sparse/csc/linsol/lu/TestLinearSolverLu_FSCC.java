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

package org.ejml.sparse.csc.linsol.lu;

import org.ejml.EjmlUnitTests;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.FMatrixSparseCSC;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.ops.ConvertFMatrixStruct;
import org.ejml.sparse.ComputePermutation;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.CommonOps_FSCC;
import org.ejml.sparse.csc.RandomMatrices_FSCC;
import org.ejml.sparse.csc.decomposition.lu.LuUpLooking_FSCC;
import org.ejml.sparse.csc.factory.FillReductionFactory_FSCC;
import org.ejml.sparse.csc.linsol.GenericLinearSolverSparseTests_FSCC;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestLinearSolverLu_FSCC extends GenericLinearSolverSparseTests_FSCC {

    public TestLinearSolverLu_FSCC() {
        canDecomposeZeros = false;
        canLockStructure = false;
    }

    @Override
    public LinearSolverSparse<FMatrixSparseCSC, FMatrixRMaj> createSolver(FillReducing permutation) {
        ComputePermutation<FMatrixSparseCSC> cp = FillReductionFactory_FSCC.create(permutation);
        LuUpLooking_FSCC lu = new LuUpLooking_FSCC(cp);
        return new LinearSolverLu_FSCC(lu);
    }

    @Override
    public FMatrixSparseCSC createA(int size) {
        // easy to make a matrix which is square and not singular or nearly if it's SPD
        return RandomMatrices_FSCC.symmetricPosDef(size,0.25f,rand);
    }

    @Test
    public void testCase0() {
        FMatrixSparseCSC A = ConvertFMatrixStruct.convert(A0_dense,(FMatrixSparseCSC)null,0);

        // sparse solvers appear to be more sensitive to numerical issues than their dense counter parts
        // skip this test if single precision
        if( A.getType().getBits() == 32 )
            return;

        LinearSolverSparse<FMatrixSparseCSC, FMatrixRMaj> solver = createSolver(FillReducing.NONE);

        FMatrixRMaj X = create(A.numCols, 3);
        FMatrixRMaj foundX = create(A.numCols, 3);
        FMatrixRMaj B = new FMatrixRMaj(A.numRows, 3);

        CommonOps_FSCC.mult(A, X, B);

        assertTrue(solver.setA(A));
        solver.solve(B, foundX);

        EjmlUnitTests.assertRelativeEquals(X, foundX, equalityTolerance);
    }

    // This matrix was found to cause the csparse algorithm to produce numerically unstable results due to poor
    // pivots being selected
    FMatrixRMaj A0_dense = FMatrixRMaj.wrap(24, 24, new float[]{
            0.0f, 0.0f, 0.0f, 97.65625f, -195.3125f, -312.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 39.0625f, -62.5f, -75.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 15.625f, -18.75f, -15.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2.0f, 6.25f, -5.0f,
            -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 2.5f, -1.0f, -0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, -0.0f, 1.0f, -0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 195.3125f, 312.5f, 97.65625f, 3125.0f, -3125.0f, -2500.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 195.3125f, 312.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 62.5f, 75.0f, 39.0625f,
            625.0f, -500.0f, -300.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 62.5f, 75.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 18.75f, 15.0f, 15.625f, 125.0f, -75.0f, -30.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 18.75f, 15.0f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 5.0f, 2.0f, 6.25f, 25.0f, -10.0f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 5.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 2.5f, 5.0f, -1.0f, -0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.0f, 1.0f, 1.0f, -0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 3125.0f, 2500.0f, 3125.0f, 23730.46875f, -15820.3125f, -8437.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 3125.0f,
            2500.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 500.0f, 300.0f, 625.0f, 3164.0625f, -1687.5f,
            -675.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 500.0f, 300.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 75.0f,
            30.0f, 125.0f, 421.875f, -168.75f, -45.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 75.0f, 30.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 10.0f, 2.0f, 25.0f, 56.25f, -15.0f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 10.0f, 2.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 5.0f, 7.5f, -1.0f, -0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.0f, 1.0f, 1.0f, -0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f, -0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 15820.3125f,
            8437.5f, 23730.46875f, 100000.0f, 50000.0f, 20000.0f, 0.0f, 0.0f, 0.0f, 0.0f, 15820.3125f, 8437.5f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1687.5f, 675.0f, 3164.0625f, 10000.0f, 4000.0f, 1200.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1687.5f, 675.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 168.75f, 45.0f, 421.875f,
            1000.0f, 300.0f, 60.0f, 0.0f, 0.0f, 0.0f, 0.0f, 168.75f, 45.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 15.0f, 2.0f, 56.25f, 100.0f, 20.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 15.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 7.5f, 10.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.0f, 1.0f, 1.0f, 0.0f, -0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.0f
    });
}