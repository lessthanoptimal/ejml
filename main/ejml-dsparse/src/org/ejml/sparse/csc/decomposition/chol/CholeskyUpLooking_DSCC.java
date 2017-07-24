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

package org.ejml.sparse.csc.decomposition.chol;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.sparse.csc.decomposition.DecompositionSparseInterface;
import org.ejml.sparse.csc.misc.ColumnCounts_DSCC;
import org.ejml.sparse.csc.misc.TriangularSolver_DSCC;

/**
 * Performs a Cholesky decomposition using an up looking algorthm on a {@link DMatrixSparseCSC}.
 *
 * <p>See page 59 in "Direct Methods for Sparse Linear Systems" by Tomothy A. Davis</p>
 *
 * @author Peter Abeles
 */
public class CholeskyUpLooking_DSCC implements DecompositionSparseInterface<DMatrixSparseCSC>
{
    int N;

    IGrowArray work = new IGrowArray(1);
    int []parent = new int[1];
    int []post = new int[1];
    int []counts = new int[1];
    ColumnCounts_DSCC columnCounter = new ColumnCounts_DSCC(false);

    @Override
    public boolean decompose(DMatrixSparseCSC orig) {
        init(N);


        return false;
    }

    public void performSymbolic(DMatrixSparseCSC A) {
        TriangularSolver_DSCC.eliminationTree(A,false,parent,work);
        TriangularSolver_DSCC.postorder(parent,N,post,work);
        columnCounter.process(A,parent,post,counts);
    }

    private void init( int N ) {
        this.N = N;
        if( parent.length < N ) {
            parent = new int[N];
            post = new int[N];
            counts = new int[N];
            work.reshape(3*N);
        }
    }

    @Override
    public boolean inputModified() {
        return false;
    }
}
