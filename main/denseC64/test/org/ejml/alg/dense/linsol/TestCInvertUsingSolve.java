/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

import org.ejml.alg.dense.decompose.lu.LUDecompositionAlt_CD64;
import org.ejml.alg.dense.linsol.lu.LinearSolverLu_CD64;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CCommonOps;
import org.ejml.ops.CMatrixFeatures;
import org.ejml.ops.CRandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestCInvertUsingSolve {

    Random rand = new Random(0xff);
    double tol = 1e-8;

    /**
     * See if it can invert a matrix that is known to be invertable.
     */
    @Test
    public void invert() {
        CDenseMatrix64F A = new CDenseMatrix64F(3,3, true, 0,0, 1,0, 2,0, -2,0, 4,0, 9,0, 0.5,0, 0,0, 5,0);
        CDenseMatrix64F A_inv = CRandomMatrices.createRandom(3, 3, rand);

        LUDecompositionAlt_CD64 decomp = new LUDecompositionAlt_CD64();
        LinearSolver<CDenseMatrix64F> solver = new LinearSolverLu_CD64(decomp);

        solver.setA(A);
        CInvertUsingSolve.invert(solver,A,A_inv);

        CDenseMatrix64F I = CRandomMatrices.createRandom(3,3,rand);

        CCommonOps.mult(A, A_inv, I);

        assertTrue(CMatrixFeatures.isIdentity(I,tol));
    }
}
