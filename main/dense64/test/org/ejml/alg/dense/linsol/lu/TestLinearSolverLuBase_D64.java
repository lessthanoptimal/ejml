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

package org.ejml.alg.dense.linsol.lu;

import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt_D64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.EjmlUnitTests;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestLinearSolverLuBase_D64 {

    Random rand = new Random(0x334);

    /**
     * Make sure that improve solution doesn't make things worse.  This test does
     * not realy test to see if it makes things better.
     */
    @Test
    public void testImproveSol_noharm() {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 0, 1, 2, -2, 4, 9, 0.5, 0, 5);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, 8, 33, 15.5);
        DenseMatrix64F x = RandomMatrices.createRandom(3,1,rand);
        DenseMatrix64F x_improved = new DenseMatrix64F(3,1);

        LUDecompositionAlt_D64 alg = new LUDecompositionAlt_D64();

        x_improved.set(x);

        LinearSolverLu_D64 solver = new LinearSolverLu_D64(alg);
        assertTrue(solver.setA(A));
        solver.solve(x,b);
        solver.improveSol(x_improved,b);

//        DenseMatrix64F x_truth = new DenseMatrix64F(3,1,new double[]{1,2,3});

        EjmlUnitTests.assertEquals(x,x_improved,1e-8);
    }
}
