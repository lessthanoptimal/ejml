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
 * This is the first QR SVD alg implemented in EJML.  It computes singular values and then
 * computes uses those singular values to compute U and V.  For EVD the same technique produces
 * faster results.  However here it seems that it produces slower results in benchmarks.  Just
 * to be save the old code is stored here.
 * </p>
 *
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
public class SvdImplicitQrDecompose_Separate implements SingularValueDecomposition {

    private int numRows;
    private int numCols;

    private BidiagonalDecompositionRow bidiag = new BidiagonalDecompositionRow();
    private SvdImplicitQrAlgorithm qralg = new SvdImplicitQrAlgorithm();

    private DenseMatrix64F U;
    private DenseMatrix64F V;

    private double singularValues[];
    private int numSingular;

    // compute a compact SVD
    private boolean compact;
    // What is actually computed
    private boolean computeU;
    private boolean computeV;

    // What the user requested to be computed
    // If the transpose is computed instead then what is actually computed is swapped
    private boolean prefComputeU;
    private boolean prefComputeV;

    // stores the results of bidiagonalization
    private double diag[];
    private double off[];

    // Should it compute the transpose instead
    private boolean transposed;

    // should it compute singular values, U, and V all at the same time?
    private boolean allAtOnce;

    public SvdImplicitQrDecompose_Separate(boolean compact, boolean computeU, boolean computeV, boolean allAtOnce ) {
        this.compact = compact;
        this.prefComputeU = computeU;
        this.prefComputeV = computeV;
        this.allAtOnce = allAtOnce;
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
        if( !prefComputeU )
            throw new IllegalArgumentException("As requested U was not computed.");
        return U;
    }

    @Override
    public DenseMatrix64F getV() {
        if( !prefComputeV )
            throw new IllegalArgumentException("As requested V was not computed.");
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
        int smallSide = setup(orig);

        // change the matrix to bidiagonal form
         if( !bidiag.decompose(orig,transposed) )
             return false;

        if( allAtOnce ) {
            computeEverything(smallSide);
        } else {
            // find the singular values
            if (computeSingularValues(smallSide))
                return false;

            // compute the U and V matrices using the already computed
            // singular values.  This reduces the number of computations involved on average
            // since it converges very fast when the real singular values are used
            if (computeUandV())
                return false;
        }

        // make sure all the singular values or positive
        makeSingularPositive();

        // if transposed undo the transposition
        undoTranpose();

        return true;
    }

    /**
     * If the transpose was computed instead do some additional computations
     */
    private void undoTranpose() {
        if( computeU )
            CommonOps.transpose(U);
        if( computeV )
            CommonOps.transpose(V);

        if( transposed ) {
            DenseMatrix64F temp = V;
            V = U;
            U = temp;
        }
    }

    private boolean computeUandV() {
        // see if anything needs to be done
        if( !computeU && !computeV )
            return false;

        // compute U and V matrices
        if( computeU )
            U = bidiag.getU(U,true,compact);
        if( computeV )
            V = bidiag.getV(V,true,compact);

        // set up the qr algorithm, reusing the previous extraction
        qralg.setMatrix(bidiag.getUBV());
        if( transposed )
            qralg.initParam(numCols,numRows);
        else
            qralg.initParam(numRows,numCols);
        // after swapping diag will now have the singular values that were computed before
        diag = qralg.swapDiag(diag);
        off = qralg.swapOff(off);
        // set it up to compute both U and V matrices
        qralg.setFastValues(false);
        if( computeU )
            qralg.setUt(U);
        if( computeV )
            qralg.setVt(V);

        return !qralg.process(diag);
    }

    private boolean computeEverything(int smallSide) {
        // compute singular values
        qralg.setMatrix(bidiag.getUBV());

        // copy the diagonal elements
        // this way it doesn't need to be copied twice and will slightly speed it up
        System.arraycopy(qralg.getDiag(),0,diag,0,smallSide);
        System.arraycopy(qralg.getOff(),0,off,0,smallSide-1);

        // compute U and V matrices
        if( computeU )
            U = bidiag.getU(U,true,compact);
        if( computeV )
            V = bidiag.getV(V,true,compact);

        qralg.setFastValues(false);
        if( computeU )
            qralg.setUt(U);
        else
            qralg.setUt(null);
        if( computeV )
            qralg.setVt(V);
        else
            qralg.setVt(null);

        return !qralg.process();
    }

    private boolean computeSingularValues(int smallSide) {
        // compute singular values
        qralg.setMatrix(bidiag.getUBV());

        // copy the diagonal elements
        // this way it doesn't need to be copied twice and will slightly speed it up
        System.arraycopy(qralg.getDiag(),0,diag,0,smallSide);
        System.arraycopy(qralg.getOff(),0,off,0,smallSide-1);

        qralg.setFastValues(true);
        qralg.setUt(null);
        qralg.setVt(null);

        return !qralg.process();
    }

    private int setup(DenseMatrix64F orig) {
        transposed = orig.numCols > orig.numRows;

        // flag what should be computed and what should not be computed
        if( transposed ) {
            computeU = prefComputeV;
            computeV = prefComputeU;
        } else {
            computeU = prefComputeU;
            computeV = prefComputeV;
        }

        numRows = orig.numRows;
        numCols = orig.numCols;

        int smallSide = Math.min(numRows,numCols);
        if( diag == null || diag.length < smallSide ) {
            diag = new double[ smallSide ];
            off = new double[ smallSide -1];
        }
        return smallSide;
    }

    /**
     * With the QR algorithm it is possible for the found singular values to be native.  This
     * makes them all positive by multiplying it by a diagonal matrix that has
     */
    private void makeSingularPositive() {
        numSingular = qralg.getNumberOfSingularValues();
        singularValues = qralg.getSingularValues();

        for( int i = 0; i < numSingular; i++ ) {
            double val = qralg.getSingularValue(i);

            if( val < 0 ) {
                singularValues[i] = -val;

                if( computeU ) {
                    // compute the results of multiplying it by an element of -1 at this location in
                    // a diagonal matrix.
                    int start = i*U.numCols;
                    int stop = start+U.numCols;

                    for( int j = start; j < stop; j++ ) {
                        U.data[j] = -U.data[j];
                    }
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