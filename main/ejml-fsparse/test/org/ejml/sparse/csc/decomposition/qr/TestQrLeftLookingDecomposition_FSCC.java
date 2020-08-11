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

package org.ejml.sparse.csc.decomposition.qr;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.FMatrixSparseCSC;
import org.ejml.interfaces.decomposition.DecompositionSparseInterface;
import org.ejml.sparse.ComputePermutation;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.CommonOps_FSCC;
import org.ejml.sparse.csc.RandomMatrices_FSCC;
import org.ejml.sparse.csc.decomposition.GenericDecompositionTests_FSCC;
import org.ejml.sparse.csc.factory.FillReductionFactory_FSCC;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestQrLeftLookingDecomposition_FSCC extends GenericDecompositionTests_FSCC {

    protected FillReducing permutationTests[] =
            new FillReducing[]{FillReducing.NONE, FillReducing.IDENTITY};

    @Override
    public FMatrixSparseCSC createMatrix(int N) {
        return RandomMatrices_FSCC.rectangle(N*10/7,N,N/2+1,rand);
    }

    @Override
    public DecompositionSparseInterface<FMatrixSparseCSC> createDecomposition() {
        return new QrLeftLookingDecomposition_FSCC(null);
    }

    @Override
    public List<FMatrixSparseCSC> decompose(DecompositionSparseInterface<FMatrixSparseCSC> d, FMatrixSparseCSC A) {
        QrLeftLookingDecomposition_FSCC qr = (QrLeftLookingDecomposition_FSCC)d;

        assertTrue(qr.decompose(A));

        List<FMatrixSparseCSC> list = new ArrayList<>();
        list.add( qr.getQ(null, false));
        list.add( qr.getR(null, false));

        return list;
    }


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

        for( FillReducing reduce : permutationTests ) {
            for (int mc = 0; mc < 100; mc++) {
//            System.out.println("MC == "+mc);
                performTest(numRows, numCols, canBeSingular, alwaysHasSolution, reduce);
            }
        }
    }

    private void performTest(int numRows, int numCols, boolean canBeSingular,
                             boolean alwaysHasSolution, FillReducing reduce) {
        int nz = RandomMatrices_FSCC.nonzero(numRows, numCols, 0.05f, 0.8f, rand);
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(numRows, numCols, nz, rand);

        if (!canBeSingular) {
            int M = Math.min(numCols, numRows);
            for (int i = 0; i < M; i++) {
                A.set(i, i, 1.2f);
            }
        }
        FMatrixSparseCSC A_cpy = A.copy();

        ComputePermutation<FMatrixSparseCSC> reducePerm = FillReductionFactory_FSCC.create(reduce);
        QrLeftLookingDecomposition_FSCC alg = new QrLeftLookingDecomposition_FSCC(reducePerm);

        if (alwaysHasSolution)
            assertTrue(alg.decompose(A));
        else if (!alg.decompose(A))
            return;

        if (!alg.inputModified()) {
            EjmlUnitTests.assertEquals(A, A_cpy, UtilEjml.TEST_F32);
        }

        FMatrixSparseCSC Q = alg.getQ(null, false);
        FMatrixSparseCSC R = alg.getR(null, false);

        // reconstruct the input matrix, not taking in account pivots
        FMatrixSparseCSC found = new FMatrixSparseCSC(Q.numRows, R.numCols, 0);
        CommonOps_FSCC.mult(Q, R, found, null, null);

        EjmlUnitTests.assertEquals(A_cpy, found, UtilEjml.TEST_F32);
    }

    /**
     * See if the compact flag is honored
     */
    @Test
    public void checkCompact() {
        int n = 10, m = 5;
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(n,m,n*m*6/9,rand);
        FMatrixSparseCSC A_cpy = A.copy();

        QrLeftLookingDecomposition_FSCC alg = new QrLeftLookingDecomposition_FSCC(null);

        assertTrue(alg.decompose(A));

        FMatrixSparseCSC Q = alg.getQ(null,true);
        FMatrixSparseCSC R = alg.getR(null,true);

        assertEquals(Q.numRows,A.numRows);
        assertEquals(Q.numCols,A.numCols);
        assertEquals(R.numRows,A.numCols);
        assertEquals(R.numCols,A.numCols);

        FMatrixSparseCSC found = new FMatrixSparseCSC(A.numRows,A.numCols,0);
        CommonOps_FSCC.mult(Q,R,found);

        EjmlUnitTests.assertEquals(A_cpy, found, UtilEjml.TEST_F32);
    }
}