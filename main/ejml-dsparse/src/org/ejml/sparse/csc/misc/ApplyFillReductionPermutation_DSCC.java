/*
 * Copyright (c) 2009-2019, Peter Abeles. All Rights Reserved.
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
 * Applies the fill reduction row pivots to the input matrix to reduce fill in during decomposition/solve.
 *
 * P*A*Q where P are row pivots and Q are column pivots.
 *
 * @author Peter Abeles
 */
public class ApplyFillReductionPermutation_DSCC {
    // fill reduction permutation
    private ComputePermutation<DMatrixSparseCSC> fillReduce;

    // storage for permuted A matrix
    DMatrixSparseCSC Aperm = new DMatrixSparseCSC(1,1,0);
    int [] pinv = new int[1]; // inverse row pivots

    IGrowArray gw = new IGrowArray();

    boolean symmetric;

    public ApplyFillReductionPermutation_DSCC(ComputePermutation<DMatrixSparseCSC> fillReduce,
                                              boolean symmetric ) {
        this.fillReduce = fillReduce;
        this.symmetric = symmetric;
    }

    /**
     * Computes and applies the fill reduction permutation. Either A is returned (unmodified) or the permutated
     * version of A.
     * @param A Input matrix. unmodified.
     * @return A permuted matrix. Might be A or a different matrix.
     */
    public DMatrixSparseCSC apply( DMatrixSparseCSC A ) {
        if( fillReduce == null )
            return A;
        fillReduce.process(A);

        IGrowArray gp = fillReduce.getRow();

        if( pinv.length < gp.length)
            pinv = new int[ gp.length ];
        CommonOps_DSCC.permutationInverse(gp.data, pinv, gp.length);
        if( symmetric )
            CommonOps_DSCC.permuteSymmetric(A, pinv, Aperm, gw);
        else
            CommonOps_DSCC.permuteRowInv(pinv, A ,Aperm);
        return Aperm;
    }

    public int[] getArrayPinv() {
        return fillReduce == null ? null : pinv;
    }
    public int[] getArrayP() {
        return fillReduce == null ? null : fillReduce.getRow().data;
    }

    public int[] getArrayQ() {
        return fillReduce == null ? null : fillReduce.getColumn().data;
    }

    public IGrowArray getGw() {
        return gw;
    }

    public void setGw(IGrowArray gw) {
        this.gw = gw;
    }

    public ComputePermutation<DMatrixSparseCSC> getFillReduce() {
        return fillReduce;
    }

    public boolean isApplied() {
        return fillReduce != null;
    }
}
