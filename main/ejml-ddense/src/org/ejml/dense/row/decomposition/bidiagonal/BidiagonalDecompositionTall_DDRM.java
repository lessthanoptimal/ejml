/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.decomposition.bidiagonal;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.interfaces.decomposition.BidiagonalDecomposition_F64;
import org.ejml.interfaces.decomposition.QRPDecomposition_F64;
import org.jetbrains.annotations.Nullable;

//CONCURRENT_INLINE import org.ejml.concurrency.EjmlConcurrency;
//CONCURRENT_INLINE import org.ejml.dense.row.CommonOps_MT_DDRM;;

/**
 * <p>
 * {@link BidiagonalDecomposition_F64} specifically designed for tall matrices.
 * First step is to perform QR decomposition on the input matrix. Then R is decomposed using
 * a bidiagonal decomposition. By performing the bidiagonal decomposition on the smaller matrix
 * computations can be saved if m/n &gt; 5/3 and if U is NOT needed.
 * </p>
 *
 * <p>
 * A = [Q<sub>1</sub> Q<sub>2</sub>][U1 0; 0 I] [B1;0] V<sup>T</sup><br>
 * U=[Q<sub>1</sub>*U1 Q<sub>2</sub>]<br>
 * B=[B1;0]<br>
 * A = U*B*V<sup>T</sup>
 * </p>
 *
 * <p>
 * A QRP decomposition is used internally. That decomposition relies an a fixed threshold for selecting singular
 * values and is known to be less stable than SVD. There is the potential for a degregation of stability
 * by using BidiagonalDecompositionTall instead of BidiagonalDecomposition_F64. A few simple tests have shown
 * that loss in stability to be insignificant.
 * </p>
 *
 * <p>
 * See page 404 in "Fundamentals of Matrix Computations", 2nd by David S. Watkins.
 * </p>
 *
 * @author Peter Abeles
 */
// TODO optimize this code
public class BidiagonalDecompositionTall_DDRM
        implements BidiagonalDecomposition_F64<DMatrixRMaj> {
    QRPDecomposition_F64<DMatrixRMaj> decompQRP = DecompositionFactory_DDRM.qrp(500, 100); // todo this should be passed in
    //CONCURRENT_BELOW BidiagonalDecomposition_F64<DMatrixRMaj> decompBi = new BidiagonalDecompositionRow_MT_DDRM();
    BidiagonalDecomposition_F64<DMatrixRMaj> decompBi = new BidiagonalDecompositionRow_DDRM();

    DMatrixRMaj B = new DMatrixRMaj(1, 1);

    // number of rows
    int m;
    // number of column
    int n;
    // min(m,n)
    int min;

    @Override
    public void getDiagonal( double[] diag, double[] off ) {
        diag[0] = B.get(0);
        for (int i = 1; i < n; i++) {
            diag[i] = B.unsafe_get(i, i);
            off[i - 1] = B.unsafe_get(i - 1, i);
        }
    }

    @Override
    public DMatrixRMaj getB( @Nullable DMatrixRMaj B, boolean compact ) {
        B = BidiagonalDecompositionRow_DDRM.handleB(B, compact, m, n, min);

        B.set(0, 0, this.B.get(0, 0));
        for (int i = 1; i < min; i++) {
            B.set(i, i, this.B.get(i, i));
            B.set(i - 1, i, this.B.get(i - 1, i));
        }
        if (n > m)
            B.set(min - 1, min, this.B.get(min - 1, min));

        return B;
    }

    @Override
    public DMatrixRMaj getU( @Nullable DMatrixRMaj U, boolean transpose, boolean compact ) {
        U = BidiagonalDecompositionRow_DDRM.handleU(U, false, compact, m, n, min);

        if (compact) {
            // U = Q*U1
            DMatrixRMaj Q1 = decompQRP.getQ(null, true);
            DMatrixRMaj U1 = decompBi.getU(null, false, true);
            //CONCURRENT_BELOW CommonOps_MT_DDRM.mult(Q1,U1,U);
            CommonOps_DDRM.mult(Q1, U1, U);
        } else {
            // U = [Q1*U1 Q2]
            DMatrixRMaj Q = decompQRP.getQ(U, false);
            DMatrixRMaj U1 = decompBi.getU(null, false, true);
            DMatrixRMaj Q1 = CommonOps_DDRM.extract(Q, 0, Q.numRows, 0, min);
            DMatrixRMaj tmp = new DMatrixRMaj(Q1.numRows, U1.numCols);
            //CONCURRENT_BELOW CommonOps_MT_DDRM.mult(Q1,U1,tmp);
            CommonOps_DDRM.mult(Q1, U1, tmp);
            CommonOps_DDRM.insert(tmp, Q, 0, 0);
        }

        if (transpose)
            CommonOps_DDRM.transpose(U);

        return U;
    }

    @Override
    public DMatrixRMaj getV( @Nullable DMatrixRMaj V, boolean transpose, boolean compact ) {
        return decompBi.getV(V, transpose, compact);
    }

    @Override
    public boolean decompose( DMatrixRMaj orig ) {

        if (!decompQRP.decompose(orig)) {
            return false;
        }

        m = orig.numRows;
        n = orig.numCols;
        min = Math.min(m, n);
        B.reshape(min, n, false);

        decompQRP.getR(B, true);

        // apply the column pivots.
        // TODO this is horribly inefficient
        DMatrixRMaj result = new DMatrixRMaj(min, n);
        DMatrixRMaj P = decompQRP.getColPivotMatrix(null);
        //CONCURRENT_BELOW CommonOps_MT_DDRM.multTransB(B, P, result);
        CommonOps_DDRM.multTransB(B, P, result);
        B.setTo(result);

        return decompBi.decompose(B);
    }

    @Override
    public boolean inputModified() {
        return decompQRP.inputModified();
    }
}
