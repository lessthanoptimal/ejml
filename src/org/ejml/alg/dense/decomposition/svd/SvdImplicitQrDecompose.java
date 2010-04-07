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

package org.ejml.alg.dense.decomposition.svd;

import org.ejml.alg.dense.decomposition.SingularValueDecomposition;
import org.ejml.alg.dense.decomposition.bidiagonal.BidiagonalDecompositionRow;
import org.ejml.alg.dense.decomposition.svd.implicitqr.SvdImplicitQrAlgorithm;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;


/**
 * <p>
 * Computes the Singular value decomposition of a matrix using the implicit QR algorithm
 * for singular value decomposition.  It works by first by transforming the matrix
 * to a bidiagonal A=U*B*V<sup>T</sup> form, then it implicitly computing the eigenvalues of the B<sup>T</sup>B matrix,
 * which are the same as the singular values in the original A matrix.
 * </p>
 *
 * <p>
 * Based off of the description provided in:<br>
 * <br>
 * David S. Watkins, "Fundamentals of Matrix Computations," Second Edition. Page 404-411
 * </p>
 *
 * @author Peter Abeles
 */
public class SvdImplicitQrDecompose implements SingularValueDecomposition {

    int numRows;
    int numCols;

    BidiagonalDecompositionRow bidiag = new BidiagonalDecompositionRow();
    SvdImplicitQrAlgorithm qralg = new SvdImplicitQrAlgorithm();

    DenseMatrix64F U;
    DenseMatrix64F V;

    double singularValues[];
    int numSingular;

    boolean compact;

    // stores the results of bidiagonalization
    double diag[];
    double off[];

    public SvdImplicitQrDecompose( boolean compact ) {
        this.compact = compact;
    }

    @Override
    public double[] getSingularValues() {
        return singularValues;
    }

    @Override
    public int numberOfSingularValues() {
        return numSingular;
    }

    @Override
    public boolean isCompact() {
        return compact;
    }

    @Override
    public DenseMatrix64F getU() {
        return U;
    }

    @Override
    public DenseMatrix64F getV() {
        return V;
    }

    @Override
    public DenseMatrix64F getW( DenseMatrix64F W ) {
        int m = compact ? numSingular : numRows;
        int n = compact ? numSingular : numCols;

        if( W == null )
            W = new DenseMatrix64F(m,n);
        else {
            W.reshape(m,n, false);
            W.zero();
        }

        for( int i = 0; i < numSingular; i++ ) {
            W.data[i*W.numCols+i] = singularValues[i];
        }

        return W;
    }

    @Override
    public boolean decompose(DenseMatrix64F orig) {
        boolean transposed = orig.numCols > orig.numRows;

        numRows = orig.numRows;
        numCols = orig.numCols;

        int smallSide = Math.min(numRows,numCols);
        if( diag == null || diag.length < smallSide ) {
            diag = new double[ smallSide ];
            off = new double[ smallSide -1];
        }

        // change the matrix to bidiagonal form
         if( !bidiag.decompose(orig,transposed) )
             return false;

        // compute singular values
        qralg.setMatrix(bidiag.getUBV());

        // copy the diagonal elements
        // this way it doesn't need to be copied twice and will slightly speed it up
        System.arraycopy(qralg.getDiag(),0,diag,0,smallSide);
        System.arraycopy(qralg.getOff(),0,off,0,smallSide-1);

        qralg.setFastValues(true);
        qralg.setUt(null);
        qralg.setVt(null);

        if( !qralg.process() )
            return false;

        copySingularValues();

        // compute U and V matrices
        U = bidiag.getU(U,true,compact);
        V = bidiag.getV(V,true,compact);

        // set up the qr algorithm, reusing the previous extraction
        qralg.setMatrix(bidiag.getUBV());
        if( transposed )
            qralg.initParam(numCols,numRows);
        else
            qralg.initParam(numRows,numCols);
        diag = qralg.swapDiag(diag);
        off = qralg.swapOff(off);
        // set it up to compute both U and V matrices
        qralg.setFastValues(false);
        qralg.setUt(U);
        qralg.setVt(V);

        if( !qralg.process(singularValues) )
            return false;

        // make sure all the singular values or positive
        makeSingularPositive();

        CommonOps.transpose(U);
        CommonOps.transpose(V);

        if( transposed ) {
            DenseMatrix64F temp = V;
            V = U;
            U = temp;
        }

        return true;
    }

    /**
     * Copies singular values into a local array
     */
    private void copySingularValues() {
        numSingular = qralg.getNumberOfSingularValues();
        if( singularValues == null || singularValues.length < numSingular )
            singularValues = new double[ numSingular ];

        System.arraycopy(qralg.getSingularValues(),0,singularValues,0,numSingular);
    }

    /**
     * With the QR algorithm it is possible for the found singular values to be native.  This
     * makes them all positive by multiplying it by a diagonal matrix that has
     */
    // TODO could make more efficient by doing this to U or V, depending which one is smaller
    private void makeSingularPositive() {
        for( int i = 0; i < numSingular; i++ ) {
            double val = qralg.getSingularValue(i);

            if( val < 0 ) {
                singularValues[i] = -val;

                // compute the results of multiplying it by an element of -1 at this location in
                // a diagonal matrix.
                int start = i*U.numCols;
                int stop = start+U.numCols;

                for( int j = start; j < stop; j++ ) {
                    U.data[j] = -U.data[j];
                }
            } else {
                singularValues[i] = val;
            }
        }
    }

    @Override
    public void setExpectedMaxSize(int numRows, int numCols) {
    }

    @Override
    public int numRows() {
        return numRows;
    }

    @Override
    public int numCols() {
        return numCols;
    }
}
