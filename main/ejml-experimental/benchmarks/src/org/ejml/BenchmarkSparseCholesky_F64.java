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

package org.ejml;

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;
import org.ejml.ops.ConvertDMatrixSparse;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.ejml.sparse.csc.decomposition.chol.CholeskyUpLooking_DSCC;

import java.util.Random;

public class BenchmarkSparseCholesky_F64 {


    public static long dense( DMatrixRMaj A , int numTrials) {

        CholeskyDecomposition_F64<DMatrixRMaj> cholesky = DecompositionFactory_DDRM.chol(A.numCols,true);

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            cholesky.decompose(A);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long sparse( DMatrixSparseCSC A , int numTrials) {

        CholeskyDecomposition_F64<DMatrixSparseCSC> cholesky = new CholeskyUpLooking_DSCC();

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            cholesky.decompose(A);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }


    public static void main(String[] args) {
        Random rand = new Random(234);

        DMatrixSparseCSC A = RandomMatrices_DSCC.symmetricPosDef(1000,200,rand);
        DMatrixRMaj A_dense = new DMatrixRMaj(A.numRows,A.numCols);
        ConvertDMatrixSparse.convert(A,A_dense);

        System.out.printf("total non-zero elements %d, fill in %08.4f%%\n",A.nz_length,100.0*A.nz_length/(double)(A.numCols*A.numRows));
        System.out.println();

        int trials = 3000;

        System.out.printf("%20s %d = %d\n","dense",1000,dense(A_dense,trials));
        System.out.printf("%20s %d = %d\n","sparse",1000,sparse(A,trials));

    }
}
