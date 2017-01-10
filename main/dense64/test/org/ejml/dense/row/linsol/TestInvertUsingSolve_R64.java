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

package org.ejml.dense.row.linsol;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.row.CommonOps_R64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.ejml.dense.row.decomposition.lu.LUDecompositionAlt_R64;
import org.ejml.dense.row.linsol.lu.LinearSolverLu_R64;
import org.ejml.interfaces.linsol.LinearSolver;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestInvertUsingSolve_R64 {

    Random rand = new Random(0xff);
    double tol = UtilEjml.TEST_F64;

    /**
     * See if it can invert a matrix that is known to be invertable.
     */
    @Test
    public void invert() {
        DMatrixRow_F64 A = new DMatrixRow_F64(3,3, true, 0, 1, 2, -2, 4, 9, 0.5, 0, 5);
        DMatrixRow_F64 A_inv = RandomMatrices_R64.createRandom(3,3,rand);

        LUDecompositionAlt_R64 decomp = new LUDecompositionAlt_R64();
        LinearSolver solver = new LinearSolverLu_R64(decomp);

        solver.setA(A);
        InvertUsingSolve_R64.invert(solver,A,A_inv);

        DMatrixRow_F64 I = RandomMatrices_R64.createRandom(3,3,rand);

        CommonOps_R64.mult(A,A_inv,I);

        for( int i = 0; i < I.numRows; i++ ) {
            for( int j = 0; j < I.numCols; j++ ) {
                if( i == j )
                    assertEquals(1,I.get(i,j),tol);
                else
                    assertEquals(0,I.get(i,j),tol);
            }
        }
    }
}
