/*
 * Copyright (c) 2009-2013, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.svd;

import org.ejml.alg.dense.decomposition.bidiagonal.BidiagonalDecompositionRow;
import org.ejml.alg.dense.decomposition.svd.implicitqr.SvdImplicitQrAlgorithm;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.SingularValueDecomposition;
import org.ejml.ops.CommonOps;


/**
 * <p>
 * Similar to {@link SvdImplicitQrDecompose} but it employs the
 * ultimate shift strategy.  Ultimate shift involves first computing singular values then uses those
 * to quickly compute the U and W matrices.  For EVD this strategy seems to work very well, but for
 * this problem it needs to have little benefit and makes the code more complex.
 * </p>
 *
 * NOTE: This code is much faster for 2x2 matrices since  it computes the eigenvalues in one step.
 *
 * @author Peter Abeles
 */
public class SvdImplicitQrDecompose_UltimateS
        implements SingularValueDecomposition<DenseMatrix64F> {

    private int numRows;
    private int numCols;
    private int smallSide;

    private BidiagonalDecompositionRow bidiag = new BidiagonalDecompositionRow();
    private SvdImplicitQrAlgorithm qralg = new SvdImplicitQrAlgorithmSmart();

    private double diag[];
    private double off[];

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

    // stores the results of bidiagonalization
    private double diagOld[];
    private double offOld[];

    // Either a copy of the input matrix or a copy of it transposed
    private DenseMatrix64F A_mod = new DenseMatrix64F(1,1);

    public SvdImplicitQrDecompose_UltimateS( boolean compact , boolean computeU , boolean computeV  ) {
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
    public DenseMatrix64F getU( DenseMatrix64F U ,boolean transpose) {
        if( !prefComputeU )
            throw new IllegalArgumentException("As requested U was not computed.");
        if( transpose )
            return Ut;

        U = new DenseMatrix64F(Ut.numCols,Ut.numRows);
        CommonOps.transpose(Ut,U);

        return U;
    }

    @Override
    public DenseMatrix64F getV( DenseMatrix64F V , boolean transpose ) {
        if( !prefComputeV )
            throw new IllegalArgumentException("As requested V was not computed.");
        if( transpose )
            return Vt;

        V = new DenseMatrix64F(Vt.numCols,Vt.numRows);
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
            W.data[i*W.numCols+i] = singularValues[i];
        }

        return W;
    }

    @Override
    public boolean decompose(DenseMatrix64F orig) {
        boolean transposed = orig.numCols > orig.numRows;

        init(orig, transposed);

        if (computeSingularValues(orig, transposed))
            return false;


        if( computeU || computeV ) {
            if (computeUandV(transposed))
                return false;
        }

        // make sure all the singular values or positive
        makeSingularPositive();

        return true;
    }

    @Override
    public boolean inputModified() {
        return false;
    }

    private void init(DenseMatrix64F orig, boolean transposed) {
        if( transposed ) {
            computeU = prefComputeV;
            computeV = prefComputeU;
        } else {
            computeU = prefComputeU;
            computeV = prefComputeV;
        }

        numRows = orig.numRows;
        numCols = orig.numCols;

        smallSide = Math.min(numRows,numCols);
        if( diagOld == null || diagOld.length < smallSide ) {
            diagOld = new double[ smallSide ];
            offOld = new double[ smallSide -1];
            diag = new double[ smallSide ];
            off = new double[ smallSide -1];
        }
    }

    private boolean computeUandV(boolean transposed) {
//        System.out.println("-------------- Computing U and V --------------------");

        long pointA = System.currentTimeMillis();

        // compute U and V matrices
        if( computeU )
            Ut = bidiag.getU(Ut,true,compact);
        if( computeV )
            Vt = bidiag.getV(Vt,true,compact);

        // set up the qr algorithm, reusing the previous extraction
        if( transposed )
            qralg.initParam(numCols,numRows);
        else
            qralg.initParam(numRows,numCols);
        diagOld = qralg.swapDiag(diagOld);
        offOld = qralg.swapOff(offOld);
        // set it up to compute both U and V matrices
        qralg.setFastValues(false);
        if( computeU )
            qralg.setUt(Ut);
        if( computeV )
            qralg.setVt(Vt);

        CommonOps.setIdentity(Ut);
        CommonOps.setIdentity(Vt);

        long pointB = System.currentTimeMillis();

        if( !qralg.process(diagOld) )
            return true;

        long pointC = System.currentTimeMillis();

        System.out.println("  bidiag UV "+(pointB-pointA)+" qr UV "+(pointC-pointB));

        if( transposed ) {
            DenseMatrix64F temp = Vt;
            Vt = Ut;
            Ut = temp;
        }
        return false;
    }

    private boolean computeSingularValues(DenseMatrix64F orig, boolean transposed) {
        long pointA = System.currentTimeMillis();

        // change the matrix to bidiagonal form
        if (bidiagonalization(orig, transposed))
            return false;

        long pointB = System.currentTimeMillis();

        // compute singular values
        bidiag.getDiagonal(diag,off);
        qralg.setMatrix(numRows,numCols,diag,off);

        // copy the diagonal elements
        // this way it doesn't need to be copied twice and will slightly speed it up
        System.arraycopy(diag,0, diagOld,0,smallSide);
        System.arraycopy(off,0, offOld,0,smallSide-1);

        qralg.setFastValues(true);
        qralg.setUt(null);
        qralg.setVt(null);

        boolean ret = !qralg.process();

        long pointC = System.currentTimeMillis();
        System.out.println("  bidiag "+(pointB-pointA)+" qr W "+(pointC-pointB));

        return ret;
    }

    private boolean bidiagonalization(DenseMatrix64F orig, boolean transposed) {
        if( transposed ) {
            A_mod.reshape(orig.numCols,orig.numRows,false);
            CommonOps.transpose(orig,A_mod);
        } else {
            A_mod.reshape(orig.numRows,orig.numCols,false);
            A_mod.set(orig);
        }
        if( !bidiag.decompose(A_mod) )
            return true;
        return false;
    }

    /**
     * With the QR algorithm it is possible for the found singular values to be native.  This
     * makes them all positive by multiplying it by a diagonal matrix that has
     */
    private void makeSingularPositive() {
        numSingular = qralg.getNumberOfSingularValues();
        singularValues = qralg.getSingularValues();

        for( int i = 0; i < numSingular; i++ ) {
            double val = singularValues[i];

            if( val < 0 ) {
                singularValues[i] = -val;

                if( computeU ) {
                    // compute the results of multiplying it by an element of -1 at this location in
                    // a diagonal matrix.
                    int start = i* Ut.numCols;
                    int stop = start+ Ut.numCols;

                    for( int j = start; j < stop; j++ ) {
                        Ut.data[j] = -Ut.data[j];
                    }
                }
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