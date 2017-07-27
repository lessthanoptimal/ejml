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

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.interfaces.decomposition.QRPDecomposition_F64;
import org.ejml.sparse.ComputePermutation;
import org.ejml.sparse.DecompositionSparseInterface;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.mult.ImplSparseSparseMult_DSCC;

import java.util.Arrays;

/**
 * Left-looking QR decomposition algorithm for sparse matrices.
 *
 * <p>NOTE: See qr_left on page 71 and cs_qr() in csparse </p>
 *
 * @author Peter Abeles
 */
public class QrLeftLookingDecomposition_DSCC implements
        QRPDecomposition_F64<DMatrixSparseCSC>, // TODO create a sparse cholesky interface?
        DecompositionSparseInterface<DMatrixSparseCSC>
{
    int m,n,m2;
    ComputePermutation<DMatrixSparseCSC> permutation;
    IGrowArray gperm = new IGrowArray();
    IGrowArray ginvperm = new IGrowArray();
    DMatrixSparseCSC Aperm = new DMatrixSparseCSC(1,1,0);
    DMatrixSparseCSC C;

    DMatrixSparseCSC V = new DMatrixSparseCSC(1,1,0);
    DMatrixSparseCSC R = new DMatrixSparseCSC(1,1,0);
    double beta[] = new double[0];
    double x[] = new double[0];

    QrStructuralCounts_DSCC structure = new QrStructuralCounts_DSCC();
    IGrowArray gwork = new IGrowArray();

    public QrLeftLookingDecomposition_DSCC(ComputePermutation<DMatrixSparseCSC> permutation ) {
        this.permutation = permutation;

        // use the same work space to reduce the overall memory foot print
        this.structure.setGwork(gwork);
    }

    @Override
    public boolean decompose(DMatrixSparseCSC A) {
        if( permutation != null ) {
            Aperm.reshape(A.numRows,Aperm.numCols,A.nz_length);
            permutation.process(A, gperm);
            ginvperm.reshape(gperm.length);
            CommonOps_DSCC.permutationInverse(gperm.data, ginvperm.data, gperm.length);
            CommonOps_DSCC.permuteSymmetric(A, ginvperm.data, Aperm, gwork);
            C = Aperm;
        } else {
            C = A;
        }

        structure.process(A);

        initialize(A);

        int w[] = gwork.data;
        int perm[] = gperm.data;
        int parent[] = structure.getParent();
        int leftmost[] = structure.getLeftMost();

        int s = m2;

        // clear mark nodes
        Arrays.fill(w,0,m2,-1);

        int pinv[] = new int[0]; // TODO something something

        // the counts from structure are actually an upper limit. the actual counts can be lower
        R.nz_length = 0;
        V.nz_length = 0;

        // compute V and R
        for (int k = 0; k < n; k++) {
            R.col_idx[k] = R.nz_length;
            int p1 = V.col_idx[k] = V.nz_length;
            w[k] = k;
            int top = n;
            int col = permutation != null ? perm[k] : k;

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
                i = pinv[A.nz_rows[p]];
                x[i] = A.nz_values[p];
                if( i > k && w[i] < k) {
                    V.nz_rows[V.nz_length++] = i;
                    w[i] = k;
                }
            }
            for (int p = top; p < n; p++) {
                int i = w[s+p];
                QrHelperFunctions_DSCC.applyHouseholder(V,i,beta[i],x);
                R.nz_rows[R.nz_length] = i;
                R.nz_values[R.nz_length++] = x[i];
                x[i] = 0;
                if( parent[i] == k ) {
                    ImplSparseSparseMult_DSCC.addRowsInAInToC(V,i,V,k,w);
                }
            }
            for (int p = p1; p < V.nz_length; p++) {
                V.nz_values[p] = k;
                x[V.nz_rows[p]] = 0;
            }
            R.col_idx[n] = R.nz_length;
            V.col_idx[n] = V.nz_length;
        }

        return false;
    }

    private void initialize(DMatrixSparseCSC A ) {
        this.m2 = structure.getFicticousRowCount();
        this.m = A.numRows;
        this.n = A.numCols;

        if( beta.length < n ) {
            beta = new double[n];
        }


        V.reshape(m2,n,structure.nz_in_V);
        R.reshape(m2,n,structure.nz_in_R);
    }

    @Override
    public void setSingularThreshold(double threshold) {

    }

    @Override
    public int getRank() {
        return 0;
    }

    @Override
    public int[] getPivots() {
        return new int[0];
    }

    @Override
    public DMatrixSparseCSC getPivotMatrix(DMatrixSparseCSC P) {
        return null;
    }

    @Override
    public DMatrixSparseCSC getQ(DMatrixSparseCSC Q, boolean compact) {
        return null;
    }

    @Override
    public DMatrixSparseCSC getR(DMatrixSparseCSC R, boolean compact) {
        return null;
    }

    @Override
    public boolean inputModified() {
        return false;
    }
}
