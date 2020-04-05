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

package org.ejml.dense.row.linsol;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.lu.LUDecompositionAlt_DDRM;
import org.ejml.dense.row.linsol.lu.LinearSolverLu_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestInvertUsingSolve_DDRM {

    Random rand = new Random(0xff);
    double tol = UtilEjml.TEST_F64;

    /**
     * See if it can invert a matrix that is known to be invertable.
     */
    @Test
    public void invert() {
        DMatrixRMaj A = new DMatrixRMaj(3,3, true, 0, 1, 2, -2, 4, 9, 0.5, 0, 5);
        DMatrixRMaj A_inv = RandomMatrices_DDRM.rectangle(3,3,rand);

        LUDecompositionAlt_DDRM decomp = new LUDecompositionAlt_DDRM();
        LinearSolverDense solver = new LinearSolverLu_DDRM(decomp);

        solver.setA(A);
        InvertUsingSolve_DDRM.invert(solver,A,A_inv);

        DMatrixRMaj I = RandomMatrices_DDRM.rectangle(3,3,rand);

        CommonOps_DDRM.mult(A,A_inv,I);

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
