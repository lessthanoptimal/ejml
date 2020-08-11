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

package org.ejml.sparse.csc.decomposition.chol;

import org.ejml.data.Complex_F32;
import org.ejml.data.FGrowArray;
import org.ejml.data.FMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.interfaces.decomposition.CholeskySparseDecomposition_F32;
import org.ejml.sparse.csc.misc.ColumnCounts_FSCC;
import org.ejml.sparse.csc.misc.TriangularSolver_FSCC;

import static org.ejml.UtilEjml.adjust;

/**
 * Performs a Cholesky decomposition using an up looking algorthm on a {@link FMatrixSparseCSC}.
 *
 * <p>See page 59 in "Direct Methods for Sparse Linear Systems" by Tomothy A. Davis</p>
 *
 * @author Peter Abeles
 */
public class CholeskyUpLooking_FSCC implements
        CholeskySparseDecomposition_F32<FMatrixSparseCSC>
{
    private int N;

    // storage for decomposition
    FMatrixSparseCSC L = new FMatrixSparseCSC(1,1,0);

    // workspace storage
    IGrowArray gw = new IGrowArray(1);
    IGrowArray gs = new IGrowArray(1);
    FGrowArray gx = new FGrowArray(1);
    int []parent = new int[1];
    int []post = new int[1];
    int []counts = new int[1];
    ColumnCounts_FSCC columnCounter = new ColumnCounts_FSCC(false);

    // true if it has successfully decomposed a matrix
    private boolean decomposed = false;
    // if true then the structure is locked and won't be computed again
    private boolean locked = false;

    @Override
    public boolean decompose(FMatrixSparseCSC orig) {
        if( orig.numCols != orig.numRows )
            throw new IllegalArgumentException("Must be a square matrix");

        if( !locked || !decomposed)
            performSymbolic(orig);

        if( performDecomposition(orig) ) {
            decomposed = true;
            return true;
        } else {
            return false;
        }
    }

    public void performSymbolic(FMatrixSparseCSC A) {
        init(A.numCols);

        TriangularSolver_FSCC.eliminationTree(A,false,parent, gw);
        TriangularSolver_FSCC.postorder(parent,N,post, gw);
        columnCounter.process(A,parent,post,counts);
        L.reshape(A.numRows,A.numCols,0);
        L.histogramToStructure(counts);
    }

    private void init( int N ) {
        this.N = N;
        if( parent.length < N ) {
            parent = new int[N];
            post = new int[N];
            counts = new int[N];
            gw.reshape(3*N);
        }
    }

    private boolean performDecomposition(FMatrixSparseCSC A) {
        int []c = adjust(gw,N);
        int []s = adjust(gs,N);
        float []x = adjust(gx,N);

        System.arraycopy(L.col_idx, 0, c, 0, N);

        for (int k = 0; k < N; k++) {
            //----  Nonzero pattern of L(k,:)
            int top = TriangularSolver_FSCC.searchNzRowsElim(A,k,parent,s,c);

            // x(0:k) is now zero
            x[k] = 0;
            int idx0 = A.col_idx[k];
            int idx1 = A.col_idx[k+1];

            // x = full(triu(C(:,k)))
            for (int p = idx0; p < idx1; p++) {
                if( A.nz_rows[p] <= k) {
                    x[A.nz_rows[p]] = A.nz_values[p];
                }
            }
            float d = x[k]; // d = C(k,k)
            x[k] = 0; // clear x for k+1 iteration

            //---- Triangular Solve
            for(; top < N; top++ ) {
                int i = s[top];
                float lki = x[i]/L.nz_values[L.col_idx[i]]; // L(k,i) = x(i) / L(i,i)
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
            L.nz_values[p] = (float)Math.sqrt(d);
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
    public FMatrixSparseCSC getT(FMatrixSparseCSC T) {
        if( T == null ) {
            T = new FMatrixSparseCSC(L.numRows,L.numCols,L.nz_length);
        }
        T.set(L);
        return T;
    }

    @Override
    public Complex_F32 computeDeterminant() {
        float value = 1;
        for (int i = 0; i < N; i++) {
            value *= L.nz_values[L.col_idx[i]];
        }
        return new Complex_F32(value*value,0);
    }

    public FGrowArray getGx() {
        return gx;
    }


    public FMatrixSparseCSC getL() {
        return L;
    }

    public IGrowArray getGw() {
        return gw;
    }

    @Override
    public void setStructureLocked( boolean locked ) {
        this.locked = locked;
    }

    @Override
    public boolean isStructureLocked() {
        return locked;
    }
}
