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
import org.ejml.alg.dense.decomposition.bidiagonal.BidiagonalDecomposition;
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

    private int numRows;
    private int numCols;

    private BidiagonalDecomposition bidiag = new BidiagonalDecompositionRow();
    private SvdImplicitQrAlgorithm qralg = new SvdImplicitQrAlgorithm();

    private DenseMatrix64F Ut;
    private DenseMatrix64F Vt;

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

    // Should it compute the transpose instead
    private boolean transposed;

    // Either a copy of the input matrix or a copy of it transposed
    private DenseMatrix64F A_mod = new DenseMatrix64F(1,1);

    public SvdImplicitQrDecompose(boolean compact, boolean computeU, boolean computeV) {
        this.compact = compact;
        this.prefComputeU = computeU;
        this.prefComputeV = computeV;
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
    public DenseMatrix64F getU(boolean transpose) {
        if( !prefComputeU )
            throw new IllegalArgumentException("As requested U was not computed.");
        if( transpose )
            return Ut;

        DenseMatrix64F U = new DenseMatrix64F(Ut.numCols,Ut.numRows);
        CommonOps.transpose(Ut,U);

        return U;
    }

    @Override
    public DenseMatrix64F getV( boolean transpose ) {
        if( !prefComputeV )
            throw new IllegalArgumentException("As requested V was not computed.");
        if( transpose )
            return Vt;

        DenseMatrix64F V = new DenseMatrix64F(Vt.numCols,Vt.numRows);
        CommonOps.transpose(Vt,V);

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
            W.unsafe_set(i,i, singularValues[i]);
        }

        return W;
    }

    @Override
    public boolean decompose(DenseMatrix64F orig) {
         setup(orig);

//        long before = System.currentTimeMillis();

        if (bidiagonalization(orig))
            return false;

//        long after = System.currentTimeMillis();
//        System.out.println("  bidiag time = "+(after-before));

        if( computeUWV() )
            return false;

        // make sure all the singular values or positive
        makeSingularPositive();

        // if transposed undo the transposition
        undoTranspose();

        return true;
    }

    @Override
    public boolean modifyInput() {
        return false;
    }

    private boolean bidiagonalization(DenseMatrix64F orig) {
        // change the matrix to bidiagonal form
        if( transposed ) {
            A_mod.reshape(orig.numCols,orig.numRows,false);
            CommonOps.transpose(orig,A_mod);
        } else {
            A_mod.reshape(orig.numRows,orig.numCols,false);
            A_mod.set(orig);
        }
        return !bidiag.decompose(A_mod, true);
    }

    /**
     * If the transpose was computed instead do some additional computations
     */
    private void undoTranspose() {
        if( transposed ) {
            DenseMatrix64F temp = Vt;
            Vt = Ut;
            Ut = temp;
        }
    }

    /**
     * Compute singular values and U and V at the same time
     */
    private boolean computeUWV() {
        qralg.setMatrix(A_mod);

//        long pointA = System.currentTimeMillis();
        // compute U and V matrices
        if( computeU )
            Ut = bidiag.getU(Ut,true,compact);
        if( computeV )
            Vt = bidiag.getV(Vt,true,compact);

        qralg.setFastValues(false);
        if( computeU )
            qralg.setUt(Ut);
        else
            qralg.setUt(null);
        if( computeV )
            qralg.setVt(Vt);
        else
            qralg.setVt(null);

//        long pointB = System.currentTimeMillis();

        boolean ret = !qralg.process();

//        long pointC = System.currentTimeMillis();
//        System.out.println("  compute UV "+(pointB-pointA)+"  QR = "+(pointC-pointB));

        return ret;
    }

    private void setup(DenseMatrix64F orig) {
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
                singularValues[i] = 0.0d - val;

                if( computeU ) {
                    // compute the results of multiplying it by an element of -1 at this location in
                    // a diagonal matrix.
                    int start = i* Ut.numCols;
                    int stop = start+ Ut.numCols;

                    for( int j = start; j < stop; j++ ) {
                        Ut.set(j, 0.0d - Ut.get(j));
                    }
                }
            } else {
                singularValues[i] = val;
            }
        }
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
