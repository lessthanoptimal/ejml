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

package org.ejml.alg.dense.misc;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt_D64;
import org.ejml.alg.dense.linsol.lu.LinearSolverLu_D64;
import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.RandomMatrices_D64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestUnrolledInverseFromMinor_D64 {

    Random rand = new Random(234234);

    /**
     * Compare it against LU decomposition
     */
    @Test
    public void compareToLU() {

        for(int N = 2; N <= UnrolledInverseFromMinor_D64.MAX; N++ ) {
            RowMatrix_F64 A = RandomMatrices_D64.createRandom(N,N,rand);

            RowMatrix_F64 expected = new RowMatrix_F64(N,N);
            RowMatrix_F64 found = new RowMatrix_F64(N,N);

            // first compute inverse by LU
            LUDecompositionAlt_D64 alg = new LUDecompositionAlt_D64();
            LinearSolverLu_D64 solver = new LinearSolverLu_D64(alg);

            assertTrue( solver.setA(A));
            solver.invert(expected);

            // compute the result from the algorithm being tested
            UnrolledInverseFromMinor_D64.inv(A,found);

            EjmlUnitTests.assertEquals(expected,found, UtilEjml.TEST_F64);
        }

    }
}
