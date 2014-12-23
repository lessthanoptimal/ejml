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

package org.ejml.alg.dense.linsol.chol;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.EjmlUnitTests;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestLinearSolverCholLDL {

    Random rand = new Random(3466);

    @Test
    public void testInverseAndSolve() {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, 17, 97, 320);
        DenseMatrix64F x = RandomMatrices.createRandom(3,1,rand);

        LinearSolverCholLDL solver = new LinearSolverCholLDL();
        assertTrue(solver.setA(A));
        solver.invert(A);
        solver.solve(b,x);


        DenseMatrix64F A_inv = new DenseMatrix64F(3,3, true, 1.453515, -0.199546, -0.013605, -0.199546, 0.167800, -0.034014, -0.013605, -0.034014, 0.020408);
        DenseMatrix64F x_expected = new DenseMatrix64F(3,1, true, 1, 2, 3);

        EjmlUnitTests.assertEquals(A_inv,A,1e-5);
        EjmlUnitTests.assertEquals(x_expected,x,1e-6);
    }
}
