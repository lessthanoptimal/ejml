/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.linsol;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.decompose.lu.LUDecompositionAlt_CR64;
import org.ejml.alg.dense.linsol.lu.LinearSolverLu_CR64;
import org.ejml.data.RowMatrix_C64;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CommonOps_CR64;
import org.ejml.ops.MatrixFeatures_CR64;
import org.ejml.ops.RandomMatrices_CR64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestInvertUsingSolve_CR64 {

    Random rand = new Random(0xff);
    double tol = UtilEjml.TEST_F64;

    /**
     * See if it can invert a matrix that is known to be invertable.
     */
    @Test
    public void invert() {
        RowMatrix_C64 A = new RowMatrix_C64(3,3, true, 0,0, 1,0, 2,0, -2,0, 4,0, 9,0, 0.5,0, 0,0, 5,0);
        RowMatrix_C64 A_inv = RandomMatrices_CR64.createRandom(3, 3, rand);

        LUDecompositionAlt_CR64 decomp = new LUDecompositionAlt_CR64();
        LinearSolver<RowMatrix_C64> solver = new LinearSolverLu_CR64(decomp);

        solver.setA(A);
        InvertUsingSolve_CR64.invert(solver,A,A_inv);

        RowMatrix_C64 I = RandomMatrices_CR64.createRandom(3,3,rand);

        CommonOps_CR64.mult(A, A_inv, I);

        assertTrue(MatrixFeatures_CR64.isIdentity(I,tol));
    }
}
