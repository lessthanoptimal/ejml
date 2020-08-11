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

package org.ejml.sparse.csc.decomposition.qr;

import org.ejml.data.FGrowArray;
import org.ejml.data.FMatrixSparseCSC;
import org.ejml.data.FScalar;
import org.ejml.data.IGrowArray;
import org.ejml.dense.row.decomposition.qr.QrHelperFunctions_FDRM;
import org.ejml.interfaces.decomposition.QRSparseDecomposition;
import org.ejml.sparse.ComputePermutation;
import org.ejml.sparse.csc.CommonOps_FSCC;
import org.ejml.sparse.csc.misc.ApplyFillReductionPermutation_FSCC;
import org.ejml.sparse.csc.mult.ImplSparseSparseMult_FSCC;

import java.util.Arrays;

/**
 * <p>Left-looking QR decomposition algorithm for sparse matrices. A=Q*R</p>
 *
 * <p>NOTE: See qr_left on page 71 and cs_qr() in csparse </p>
 *
 * @author Peter Abeles
 */
public class QrLeftLookingDecomposition_FSCC implements
        QRSparseDecomposition<FMatrixSparseCSC>
{
    // shape of matrix and m2 includes fictitious rows
    int m,n,m2;
    ApplyFillReductionPermutation_FSCC applyReduce;

    // storage for Householder vectors
    FMatrixSparseCSC V = new FMatrixSparseCSC(1,1,0);
    // Storage for R matrix in QR
    FMatrixSparseCSC R = new FMatrixSparseCSC(1,1,0);
    // storage for beta in (I - beta*v*v')
    float beta[] = new float[0];
    FScalar Beta = new FScalar(); // used to get return value from a function

    // local workspace
    float x[] = new float[0];

    QrStructuralCounts_FSCC structure = new QrStructuralCounts_FSCC();
    int structureP[] = new int[0];
    IGrowArray gwork = new IGrowArray();
    FGrowArray gx = new FGrowArray();

    // if true that means a singular matrix was detected
    boolean singular;

    // true if it has successfully decomposed a matrix
    private boolean decomposed = false;
    // if true then the structure is locked and won't be computed again
    private boolean locked = false;

    public QrLeftLookingDecomposition_FSCC(ComputePermutation<FMatrixSparseCSC> permutation ) {
        this.applyReduce = new ApplyFillReductionPermutation_FSCC(permutation,false);

        // use the same work space to reduce the overall memory foot print
        this.structure.setGwork(gwork);
    }

    @Override
    public boolean decompose(FMatrixSparseCSC A) {
        FMatrixSparseCSC C = applyReduce.apply(A);

        if( !decomposed || !locked ) {
            // compute the structure of V and R
            if (!structure.process(C))
                return false;

            // Initialize data structured used in the decomposition
            initializeDecomposition(C);
        }

        // perform the decomposition
        performDecomposition(C);

        decomposed = true;
        return true;
    }

    private void performDecomposition(FMatrixSparseCSC A) {
        int w[] = gwork.data;
        int permCol[] = applyReduce.getArrayQ();
        int parent[] = structure.getParent();
        int leftmost[] = structure.getLeftMost();
        // permutation that was done to ensure all rows have non-zero elements
        int pinv_structure[] = structure.getPinv();
        int s = m2;

        // clear mark nodes. See addRowsInAInToC
        Arrays.fill(w,0,m2,-1);
        Arrays.fill(x,0,m2,0);

        // the counts from structure are actually an upper limit. the actual counts can be lower
        R.nz_length = 0;
        V.nz_length = 0;

        // compute V and R
        for (int k = 0; k < n; k++) {
            R.col_idx[k] = R.nz_length;
            int p1 = V.col_idx[k] = V.nz_length;
            w[k] = k;
            V.nz_rows[V.nz_length++] = k;                       // Add V(k,k) to V's pattern
            int top = n;
            int col = permCol != null ? permCol[k] : k;

            int idx0 = A.col_idx[col];
            int idx1 = A.col_idx[col+1];

            for (int p = idx0; p <idx1 ; p++) {
                int i = leftmost[A.nz_rows[p]];
                int len;
                for (len = 0; w[i] != k ; i = parent[i]) {
                    w[s + len++] = i;
                    w[i] = k;
                }
                while( len > 0) {
                    w[s + --top] = w[s + --len];
                }
                i = pinv_structure[A.nz_rows[p]];
                x[i] = A.nz_values[p];
                if( i > k && w[i] < k) {
                    V.nz_rows[V.nz_length++] = i;
                    w[i] = k;
                }
            }
            // apply previously computed Householder vectors to the current columns
            for (int p = top; p < n; p++) {
                int i = w[s+p];
                QrHelperFunctions_FSCC.applyHouseholder(V,i,beta[i],x);
                R.nz_rows[R.nz_length] = i;
                R.nz_values[R.nz_length++] = x[i];
                x[i] = 0;
                if( parent[i] == k ) {
                    ImplSparseSparseMult_FSCC.addRowsInAInToC(V, i, V, k, w);
                }
            }
            for (int p = p1; p < V.nz_length; p++) {
                V.nz_values[p] = x[V.nz_rows[p]];
                x[V.nz_rows[p]] = 0;
            }
            R.nz_rows[R.nz_length] = k;
            float max = QrHelperFunctions_FDRM.findMax(V.nz_values,p1,V.nz_length-p1);
            if( max == 0.0f) {
                singular = true;
                R.nz_values[R.nz_length] = 0;
                beta[k] = 0;
            } else {
                R.nz_values[R.nz_length] = QrHelperFunctions_FSCC.computeHouseholder(V.nz_values, p1, V.nz_length, max, Beta);
                beta[k] = Beta.value;
            }
            R.nz_length++;
        }
        R.col_idx[n] = R.nz_length;
        V.col_idx[n] = V.nz_length;
    }

    private void initializeDecomposition(FMatrixSparseCSC A ) {
        this.singular = false;
        this.m2 = structure.getFicticousRowCount();
        this.m = A.numRows;
        this.n = A.numCols;

        if( beta.length < n ) {
            beta = new float[n];
        }
        if( x.length < m2 ) {
            x = new float[m2];
            structureP = new int[m2];
        }

        V.reshape(m2,n,structure.nz_in_V);
        R.reshape(m2,n,structure.nz_in_R);
    }

    @Override
    public FMatrixSparseCSC getQ(FMatrixSparseCSC Q, boolean compact) {
        if( Q == null )
            Q = new FMatrixSparseCSC(1,1,0);

        if( compact )
            Q.reshape(V.numRows,n,0);
        else
            Q.reshape(V.numRows,m,0);
        FMatrixSparseCSC I = CommonOps_FSCC.identity(V.numRows,Q.numCols);

        for (int i = V.numCols-1; i >= 0; i--) {
            QrHelperFunctions_FSCC.rank1UpdateMultR(V,i,beta[i],I,Q,gwork,gx);
            I.set(Q);
        }

        // Apply P transpose to Q
        CommonOps_FSCC.permutationInverse(structure.pinv,structureP,V.numRows);
        CommonOps_FSCC.permuteRowInv(structureP,Q,I);

        // Remove fictitious rows
        if( V.numRows > m )
            CommonOps_FSCC.extractRows(I,0,m,Q);
        else
            Q.set(I);

        return Q;
    }

    @Override
    public FMatrixSparseCSC getR(FMatrixSparseCSC R, boolean compact) {
        if( R == null )
            R = new FMatrixSparseCSC(0,0,0);

        R.set(this.R);
        if( m > n ) {
            // there should only be only zeros past row n
            R.numRows = compact ? n : m;
        } else if( n > m && V.numRows != m ) {
            FMatrixSparseCSC tmp = new FMatrixSparseCSC(m,n,0);
            CommonOps_FSCC.extractRows(R,0,m,tmp);
            R.set(tmp);
        }
        return R;
    }

    @Override
    public boolean inputModified() {
        return false;
    }

    public IGrowArray getGwork() {
        return gwork;
    }

    public FGrowArray getGx() {
        return gx;
    }

    public QrStructuralCounts_FSCC getStructure() {
        return structure;
    }

    public FMatrixSparseCSC getV() {
        return V;
    }

    public FMatrixSparseCSC getR() {
        return R;
    }

    public float[] getBeta() {
        return beta;
    }

    public float getBeta( int index ) {
        if( index >= n )
            throw new IllegalArgumentException("index is out of bounds");
        return beta[index];
    }

    public int[] getFillPermutation() {
        return applyReduce.getArrayP();
    }

    public boolean isFillPermutated() {
        return applyReduce.isApplied();
    }

    public boolean isSingular() {
        return singular;
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
