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

package org.ejml.dense.row.linsol.chol;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestLinearSolverCholLDL_R64 {

    Random rand = new Random(3466);

    @Test
    public void testInverseAndSolve() {
        DMatrixRow_F64 A = new DMatrixRow_F64(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);
        DMatrixRow_F64 b = new DMatrixRow_F64(3,1, true, 17, 97, 320);
        DMatrixRow_F64 x = RandomMatrices_R64.createRandom(3,1,rand);

        LinearSolverCholLDL_R64 solver = new LinearSolverCholLDL_R64();
        assertTrue(solver.setA(A));
        solver.invert(A);
        solver.solve(b,x);


        DMatrixRow_F64 A_inv = new DMatrixRow_F64(3,3, true, 1.453515, -0.199546, -0.013605, -0.199546, 0.167800, -0.034014, -0.013605, -0.034014, 0.020408);
        DMatrixRow_F64 x_expected = new DMatrixRow_F64(3,1, true, 1, 2, 3);

        EjmlUnitTests.assertEquals(A_inv,A, UtilEjml.TEST_F64_SQ);
        EjmlUnitTests.assertEquals(x_expected,x,UtilEjml.TEST_F64_SQ);
    }
}
