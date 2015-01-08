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

package org.ejml.alg.dense.misc;

import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt_D64;
import org.ejml.alg.dense.linsol.lu.LinearSolverLu_D64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.EjmlUnitTests;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestUnrolledInverseFromMinor {

    Random rand = new Random(234234);

    /**
     * Compare it against LU decomposition
     */
    @Test
    public void compareToLU() {

        for( int N = 2; N <= UnrolledInverseFromMinor.MAX; N++ ) {
            DenseMatrix64F A = RandomMatrices.createRandom(N,N,rand);

            DenseMatrix64F expected = new DenseMatrix64F(N,N);
            DenseMatrix64F found = new DenseMatrix64F(N,N);

            // first compute inverse by LU
            LUDecompositionAlt_D64 alg = new LUDecompositionAlt_D64();
            LinearSolverLu_D64 solver = new LinearSolverLu_D64(alg);

            assertTrue( solver.setA(A));
            solver.invert(expected);

            // compute the result from the algorithm being tested
            UnrolledInverseFromMinor.inv(A,found);

            EjmlUnitTests.assertEquals(expected,found,1e-8);
        }

    }
}
