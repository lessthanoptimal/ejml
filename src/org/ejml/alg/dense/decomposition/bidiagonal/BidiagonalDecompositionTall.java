/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.dense.decomposition.bidiagonal;

import org.ejml.alg.dense.decomposition.DecompositionFactory;
import org.ejml.alg.dense.decomposition.QRDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;


/**
 * <p>
 * {@link BidiagonalDecomposition} specifically designed for tall matrices.
 * First step is to perform QR decomposition on the input matrix.  Then R is decomposed using
 * a bidiagonal decomposition.  By performing the bidiagonal decomposition on the smaller matrix
 * computations can be saved if m/n > 5/3 and if U is NOT needed.
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
 * See page 404 in "Fundamentals of Matrix Computations", 2nd by David S. Watkins.
 * </p>
 *
 *
 * @author Peter Abeles
 */
public class BidiagonalDecompositionTall
        implements BidiagonalDecomposition<DenseMatrix64F>
{
    QRDecomposition<DenseMatrix64F> decompQR = DecompositionFactory.qr();
    BidiagonalDecomposition<DenseMatrix64F> decompBi = new BidiagonalDecompositionRow();

    DenseMatrix64F B = new DenseMatrix64F(1,1);

    int m;
    int n;
    int min;

    @Override
    public void getDiagonal(double[] diag, double[] off) {
        diag[0] = B.get(0);
        for( int i = 1; i < n; i++ ) {
            diag[i] = B.unsafe_get(i,i);
            off[i-1] = B.unsafe_get(i-1,i);
        }
    }

    @Override
    public DenseMatrix64F getB(DenseMatrix64F B, boolean compact) {
        B = BidiagonalDecompositionRow.handleB(B,compact, m, n,min);

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
    public DenseMatrix64F getU(DenseMatrix64F U, boolean transpose, boolean compact) {
        U = BidiagonalDecompositionRow.handleU(U,transpose,compact, m, n,min);

        if( compact ) {
            // U = Q*U1
            DenseMatrix64F Q1 = decompQR.getQ(null,true);
            DenseMatrix64F U1 = decompBi.getU(null,false,true);
            CommonOps.mult(Q1,U1,U);

            if( transpose )
                CommonOps.transpose(U);
        } else {
           // U = [Q1*U1 Q2]
            DenseMatrix64F Q = decompQR.getQ(U,false);
            DenseMatrix64F U1 = decompBi.getU(null,false,true);
            DenseMatrix64F Q1 = CommonOps.extract(Q,0,Q.numRows,0,min);
            DenseMatrix64F tmp = new DenseMatrix64F(Q1.numRows,U1.numCols);
            CommonOps.mult(Q1,U1,tmp);
            CommonOps.insert(tmp,Q,0,0);
        }

        return U;
    }

    @Override
    public DenseMatrix64F getV(DenseMatrix64F V, boolean transpose, boolean compact) {
        return decompBi.getV(V,transpose,compact);
    }

    @Override
    public boolean decompose(DenseMatrix64F orig) {
        if( !decompQR.decompose(orig) )
            return false;

        m = orig.numRows;
        n = orig.numCols;
        min = Math.min(m, n);
        B.reshape(min, n,false);

        decompQR.getR(B,true);

        return decompBi.decompose(B);
    }

    @Override
    public boolean inputModified() {
        return decompQR.inputModified();
    }
}
