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

package org.ejml.sparse.csc.misc;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.sparse.ComputePermutation;
import org.ejml.sparse.csc.CommonOps_DSCC;

/**
 * Applies the fill reduction permutation to the input matrix
 *
 * @author Peter Abeles
 */
public class ApplyFillReductionPermutation {
    // fill reduction permutation
    private ComputePermutation<DMatrixSparseCSC> fillReduce;

    // storage for permuted A matrix
    DMatrixSparseCSC Aperm = new DMatrixSparseCSC(1,1,0);
    int []Pinv = new int[1]; // inverse permutation

    IGrowArray gw = new IGrowArray();

    public ApplyFillReductionPermutation(ComputePermutation<DMatrixSparseCSC> fillReduce) {
        this.fillReduce = fillReduce;
    }

    public DMatrixSparseCSC apply(DMatrixSparseCSC A ) {
        if( fillReduce == null )
            return A;
        if( Pinv.length < A.numRows )
            Pinv = new int[ A.numRows ];

        IGrowArray P = new IGrowArray();
        fillReduce.process(A,P);
        CommonOps_DSCC.permutationInverse(P.data, Pinv,A.numRows);
        CommonOps_DSCC.permuteSymmetric(A, Pinv, Aperm, gw);
        return Aperm;
    }

    public int[] getPinv() {
        return fillReduce == null ? null : Pinv;
    }

    public IGrowArray getGw() {
        return gw;
    }

    public void setGw(IGrowArray gw) {
        this.gw = gw;
    }
}
