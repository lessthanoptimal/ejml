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

package org.ejml.sparse.csc.decomposition.qr;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class TestQrLeftLookingDecomposition_DSCC {

    Random rand = new Random(234);

    @Test
    public void process_tall() {
        proces_random(10, 5,false,true);
        proces_random(10, 5,true,true);
    }

    @Test
    public void process_wide() {
        proces_random(5, 10,false, true);
        proces_random(5, 10,true,false);
    }

    private void proces_random(int numRows, int numCols , boolean canBeSingular, boolean alwaysHasSolution ) {
        for (int mc = 0; mc < 100; mc++) {
//            System.out.println("MC == "+mc);

            int nz = RandomMatrices_DSCC.nonzero(numRows,numCols,0.05,0.8,rand);
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(numRows,numCols,nz,rand);

            if( !canBeSingular ) {
                int M = Math.min(numCols,numRows);
                for (int i = 0; i < M; i++) {
                    A.set(i,i,1.2);
                }
            }

            QrLeftLookingDecomposition_DSCC alg = new QrLeftLookingDecomposition_DSCC(null);

            if( alwaysHasSolution )
                assertTrue(alg.decompose(A));
            else if( !alg.decompose(A))
                continue;

            DMatrixSparseCSC Q = alg.getQ(null,false);
            DMatrixSparseCSC R = alg.getR(null,false);

            // reconstruct the input matrix, not taking in account pivots
            DMatrixSparseCSC found = new DMatrixSparseCSC(Q.numRows,R.numCols,0);
            CommonOps_DSCC.mult(Q,R,found,null,null);

            EjmlUnitTests.assertEquals(A,found, UtilEjml.TEST_F64);
        }
    }

    /**
     * See if the compact flag is honored
     */
    @Test
    public void checkCompact() {
        fail("Implement");
    }

    @Test
    public void checkRowReductionPermutation() {
        fail("Implement");
    }

    @Test
    public void checkInputModified() {
        fail("Implement");
    }
}