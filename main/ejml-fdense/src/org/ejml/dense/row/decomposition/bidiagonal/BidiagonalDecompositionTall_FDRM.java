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

package org.ejml.dense.row.decomposition.bidiagonal;

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.factory.DecompositionFactory_FDRM;
import org.ejml.interfaces.decomposition.BidiagonalDecomposition_F32;
import org.ejml.interfaces.decomposition.QRPDecomposition_F32;


/**
 * <p>
 * {@link BidiagonalDecomposition_F32} specifically designed for tall matrices.
 * First step is to perform QR decomposition on the input matrix.  Then R is decomposed using
 * a bidiagonal decomposition.  By performing the bidiagonal decomposition on the smaller matrix
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
 * A QRP decomposition is used internally.  That decomposition relies an a fixed threshold for selecting singular
 * values and is known to be less stable than SVD.  There is the potential for a degregation of stability
 * by using BidiagonalDecompositionTall instead of BidiagonalDecomposition_F32. A few simple tests have shown
 * that loss in stability to be insignificant.
 * </p>
 *
 * <p>
 * See page 404 in "Fundamentals of Matrix Computations", 2nd by David S. Watkins.
 * </p>
 *
 *
 * @author Peter Abeles
 */
// TODO optimize this code
public class BidiagonalDecompositionTall_FDRM
        implements BidiagonalDecomposition_F32<FMatrixRMaj>
{
    QRPDecomposition_F32<FMatrixRMaj> decompQRP = DecompositionFactory_FDRM.qrp(500, 100); // todo this should be passed in
    BidiagonalDecomposition_F32<FMatrixRMaj> decompBi = new BidiagonalDecompositionRow_FDRM();

    FMatrixRMaj B = new FMatrixRMaj(1,1);

    // number of rows
    int m;
    // number of column
    int n;
    // min(m,n)
    int min;

    @Override
    public void getDiagonal(float[] diag, float[] off) {
        diag[0] = B.get(0);
        for( int i = 1; i < n; i++ ) {
            diag[i] = B.unsafe_get(i,i);
            off[i-1] = B.unsafe_get(i-1,i);
        }
    }

    @Override
    public FMatrixRMaj getB(FMatrixRMaj B, boolean compact) {
        B = BidiagonalDecompositionRow_FDRM.handleB(B, compact, m, n, min);

        B.set(0,0,this.B.get(0,0));
        for( int i = 1; i < min; i++ ) {
            B.set(i,i, this.B.get(i,i));
            B.set(i-1,i, this.B.get(i-1,i));
        }
        if( n > m)
            B.set(min-1,min,this.B.get(min-1,min));

        return B;
    }

    @Override
    public FMatrixRMaj getU(FMatrixRMaj U, boolean transpose, boolean compact) {
        U = BidiagonalDecompositionRow_FDRM.handleU(U, false, compact, m, n, min);

        if( compact ) {
            // U = Q*U1
            FMatrixRMaj Q1 = decompQRP.getQ(null,true);
            FMatrixRMaj U1 = decompBi.getU(null,false,true);
            CommonOps_FDRM.mult(Q1,U1,U);
        } else {
           // U = [Q1*U1 Q2]
            FMatrixRMaj Q = decompQRP.getQ(U,false);
            FMatrixRMaj U1 = decompBi.getU(null,false,true);
            FMatrixRMaj Q1 = CommonOps_FDRM.extract(Q,0,Q.numRows,0,min);
            FMatrixRMaj tmp = new FMatrixRMaj(Q1.numRows,U1.numCols);
            CommonOps_FDRM.mult(Q1,U1,tmp);
            CommonOps_FDRM.insert(tmp,Q,0,0);
        }

        if( transpose )
            CommonOps_FDRM.transpose(U);

        return U;
    }

    @Override
    public FMatrixRMaj getV(FMatrixRMaj V, boolean transpose, boolean compact) {
        return decompBi.getV(V,transpose,compact);
    }

    @Override
    public boolean decompose(FMatrixRMaj orig) {

        if( !decompQRP.decompose(orig) ) {
            return false;
        }

        m = orig.numRows;
        n = orig.numCols;
        min = Math.min(m, n);
        B.reshape(min, n,false);

        decompQRP.getR(B,true);

        // apply the column pivots.
        // TODO this is horribly inefficient
        FMatrixRMaj result = new FMatrixRMaj(min,n);
        FMatrixRMaj P = decompQRP.getColPivotMatrix(null);
        CommonOps_FDRM.multTransB(B, P, result);
        B.set(result);

        return decompBi.decompose(B);
    }

    @Override
    public boolean inputModified() {
        return decompQRP.inputModified();
    }
}
