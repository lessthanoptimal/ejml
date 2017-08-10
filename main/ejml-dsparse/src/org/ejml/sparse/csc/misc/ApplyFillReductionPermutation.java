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
 * Applies the fill reduction row pivots to the input matrix to reduce fill in during decomposition/solve.
 *
 * @author Peter Abeles
 */
public class ApplyFillReductionPermutation {
    // fill reduction permutation
    private ComputePermutation<DMatrixSparseCSC> fillReduce;

    // storage for permuted A matrix
    DMatrixSparseCSC Aperm = new DMatrixSparseCSC(1,1,0);
    int [] pinv = new int[1]; // inverse row pivots

    IGrowArray p = new IGrowArray(); // storage for row pivots
    IGrowArray gw = new IGrowArray();

    boolean symmetric;

    public ApplyFillReductionPermutation(ComputePermutation<DMatrixSparseCSC> fillReduce,
                                         boolean symmetric ) {
        this.fillReduce = fillReduce;
    }

    /**
     * Computes and applies the fill reduction permutation. Either A is returned (unmodified) or the permutated
     * version of A.
     * @param A Input matrx. unmodified.
     * @return A permuted matrix. Might be A or a different matrix.
     */
    public DMatrixSparseCSC apply(DMatrixSparseCSC A ) {
        if( fillReduce == null )
            return A;
        if( pinv.length < A.numRows )
            pinv = new int[ A.numRows ];
        fillReduce.process(A, p);
        if( p.length != A.numRows )
            throw new RuntimeException("Egads");
        CommonOps_DSCC.permutationInverse(p.data, pinv, p.length);
        if( symmetric )
            CommonOps_DSCC.permuteSymmetric(A, pinv, Aperm, gw);
        else
            CommonOps_DSCC.permuteRowInv(pinv, A ,Aperm);
        return Aperm;
    }

    public int[] getArrayPinv() {
        return fillReduce == null ? null : pinv;
    }

    public IGrowArray getGw() {
        return gw;
    }

    public void setGw(IGrowArray gw) {
        this.gw = gw;
    }

    public IGrowArray getP() {
        return p;
    }

    public int[] getArrayP() {
        return fillReduce == null ? null : p.data;
    }

    public ComputePermutation<DMatrixSparseCSC> getFillReduce() {
        return fillReduce;
    }
}
