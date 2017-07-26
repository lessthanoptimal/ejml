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

import org.ejml.data.Complex_F64;
import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;
import org.ejml.sparse.DecompositionSparseInterface;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.misc.ColumnCounts_DSCC;
import org.ejml.sparse.csc.misc.TriangularSolver_DSCC;

import static org.ejml.sparse.csc.misc.TriangularSolver_DSCC.adjust;

/**
 * Performs a Cholesky decomposition using an up looking algorthm on a {@link DMatrixSparseCSC}.
 *
 * <p>See page 59 in "Direct Methods for Sparse Linear Systems" by Tomothy A. Davis</p>
 *
 * @author Peter Abeles
 */
public class CholeskyUpLooking_DSCC implements
        CholeskyDecomposition_F64<DMatrixSparseCSC>, // TODO create a sparse cholesky interface?
        DecompositionSparseInterface<DMatrixSparseCSC>
{
    private int N;

    private FillReducing permutation;

    // storage for permuted A matrix
    DMatrixSparseCSC Aperm = new DMatrixSparseCSC(1,1,0);
    // reference to Aperm or A
    DMatrixSparseCSC C;

    // storage for decomposition
    DMatrixSparseCSC L = new DMatrixSparseCSC(1,1,0);

    // workspace storage
    IGrowArray gw = new IGrowArray(1);
    IGrowArray gs = new IGrowArray(1);
    DGrowArray gx = new DGrowArray(1);
    int []parent = new int[1];
    int []post = new int[1];
    int []counts = new int[1];
    int []Pinv = new int[1]; // inverse permutation
    ColumnCounts_DSCC columnCounter = new ColumnCounts_DSCC(false);

    // storage for determinant results

    public CholeskyUpLooking_DSCC(FillReducing permutation ) {
        this.permutation = permutation;
    }

    @Override
    public boolean decompose(DMatrixSparseCSC orig) {
        if( orig.numCols != orig.numRows )
            throw new IllegalArgumentException("Must be a square matrix");

        performSymbolic(orig);

        return decompose();
    }

    public void performSymbolic(DMatrixSparseCSC A ) {
        init(A.numCols);

        if(permutation != FillReducing.NONE) {
            // create a dummy permutation vector as a place holder
            int[] P = new int[A.numRows];
            for (int i = 0; i < P.length; i++) {
                P[i] = i;
            }
            CommonOps_DSCC.permutationInverse(P, Pinv);
            CommonOps_DSCC.permuteSymmetric(A, Pinv, Aperm, gw);
            C = Aperm;
        } else {
            C = A;
        }
        TriangularSolver_DSCC.eliminationTree(C,false,parent, gw);
        TriangularSolver_DSCC.postorder(parent,N,post, gw);
        columnCounter.process(C,parent,post,counts);
        L.reshape(A.numRows,A.numCols,0);
        L.colsum(counts);
    }

    private void init( int N ) {
        this.N = N;
        if( parent.length < N ) {
            parent = new int[N];
            post = new int[N];
            counts = new int[N];
            Pinv = new int[N];
            gw.reshape(3*N);
        }
    }

    private boolean decompose() {
        int []c = adjust(gw,N);
        int []s = adjust(gs,N);
        double []x = adjust(gx,N);

        for (int k = 0; k < N; k++) {
            c[k] = L.col_idx[k];
        }
        for (int k = 0; k < N; k++) {
            //----  Nonzero pattern of L(k,:)
            int top = TriangularSolver_DSCC.searchNzRowsElim(C,k,parent,s,c);

            // x(0:k) is now zero
            x[k] = 0;
            int idx0 = C.col_idx[k];
            int idx1 = C.col_idx[k+1];

            // x = full(triu(C(:,k)))
            for (int p = idx0; p < idx1; p++) {
                if( C.nz_rows[p] <= k) {
                    x[C.nz_rows[p]] = C.nz_values[p];
                }
            }
            double d = x[k]; // d = C(k,k)
            x[k] = 0; // clear x for k+1 iteration

            //---- Triangular Solve
            for(; top < N; top++ ) {
                int i = s[top];
                double lki = x[i]/L.nz_values[L.col_idx[i]]; // L(k,i) = x(i) / L(i,i)
                x[i] = 0;
                for (int p = L.col_idx[i]+1; p < c[i]; p++) {
                    x[L.nz_rows[p]] -= L.nz_values[p]*lki;
                }
                d -= lki*lki; // d = d - L(k,i)**L(k,i)
                int p = c[i]++;
                L.nz_rows[p] = k;     // store L(k,i) in column i
                L.nz_values[p] = lki;
            }

            //----- Compute L(k,k)
            if( d <= 0 ) {
                // it's not positive definite
                return false;
            }
            int p = c[k]++;
            L.nz_rows[p] = k;
            L.nz_values[p] = Math.sqrt(d);
        }

        return true;
    }

    @Override
    public boolean inputModified() {
        return false;
    }

    @Override
    public boolean isLower() {
        return true;
    }

    @Override
    public DMatrixSparseCSC getT(DMatrixSparseCSC T) {
        if( T == null ) {
            T = new DMatrixSparseCSC(L.numRows,L.numCols,L.nz_length);
        }
        T.set(L);
        return T;
    }

    @Override
    public Complex_F64 computeDeterminant() {
        double value = 1;
        for (int i = 0; i < N; i++) {
            value *= L.nz_values[L.col_idx[i]];
        }
        return new Complex_F64(value*value,0);
    }

    public DGrowArray getGx() {
        return gx;
    }

    public int[] getPinv() {
        return Pinv;
    }

    public DMatrixSparseCSC getL() {
        return L;
    }

    public FillReducing getPermutation() {
        return permutation;
    }
}
