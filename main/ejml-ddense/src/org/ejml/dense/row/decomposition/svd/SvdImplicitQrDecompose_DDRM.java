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

package org.ejml.dense.row.decomposition.svd;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.decomposition.bidiagonal.BidiagonalDecompositionRow_DDRM;
import org.ejml.dense.row.decomposition.bidiagonal.BidiagonalDecompositionTall_DDRM;
import org.ejml.dense.row.decomposition.svd.implicitqr.SvdImplicitQrAlgorithm_DDRM;
import org.ejml.interfaces.decomposition.BidiagonalDecomposition_F64;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
@SuppressWarnings("NullAway.Init")
public class SvdImplicitQrDecompose_DDRM implements SingularValueDecomposition_F64<DMatrixRMaj> {

    protected int numRows;
    protected int numCols;

    // dimensions of transposed matrix
    protected int numRowsT;
    protected int numColsT;

    // if true then it can use the special Bidiagonal decomposition
    protected boolean canUseTallBidiagonal;

    // If U is not being computed and the input matrix is 'tall' then a special bidiagonal decomposition
    // can be used which is faster.
    protected BidiagonalDecomposition_F64<DMatrixRMaj> bidiag;
    protected SvdImplicitQrAlgorithm_DDRM qralg = new SvdImplicitQrAlgorithm_DDRM();

    double[] diag;
    double[] off;

    private DMatrixRMaj Ut;
    private DMatrixRMaj Vt;

    private double[] singularValues;
    private int numSingular;

    // compute a compact SVD
    protected boolean compact;
    // What is actually computed
    protected boolean computeU;
    protected boolean computeV;

    // What the user requested to be computed
    // If the transpose is computed instead then what is actually computed is swapped
    protected boolean prefComputeU;
    protected boolean prefComputeV;

    // Should it compute the transpose instead
    protected boolean transposed;

    // Either a copy of the input matrix or a copy of it transposed
    private DMatrixRMaj A_mod = new DMatrixRMaj(1, 1);

    /**
     * Configures the class
     *
     * @param compact Compute a compact SVD
     * @param computeU If true it will compute the U matrix
     * @param computeV If true it will compute the V matrix
     * @param canUseTallBidiagonal If true then it can choose to use a tall Bidiagonal decomposition to improve runtime performance.
     */
    public SvdImplicitQrDecompose_DDRM( boolean compact, boolean computeU, boolean computeV,
                                        boolean canUseTallBidiagonal ) {
        this.compact = compact;
        this.prefComputeU = computeU;
        this.prefComputeV = computeV;
        this.canUseTallBidiagonal = canUseTallBidiagonal;
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
    public DMatrixRMaj getU( @Nullable DMatrixRMaj U, boolean transpose ) {
        if (!prefComputeU)
            throw new IllegalArgumentException("As requested U was not computed.");
        if (transpose) {
            if (U == null)
                return Ut;
            U.setTo(Ut);
        } else {
            U = UtilEjml.reshapeOrDeclare(U, Ut.numCols, Ut.numRows);
            transpose(U, Ut);
        }

        return U;
    }

    @Override
    public DMatrixRMaj getV( @Nullable DMatrixRMaj V, boolean transpose ) {
        if (!prefComputeV)
            throw new IllegalArgumentException("As requested V was not computed.");
        if (transpose) {
            if (V == null)
                return Vt;

            V.setTo(Vt);
        } else {
            V = UtilEjml.reshapeOrDeclare(V, Vt.numCols, Vt.numRows);
            transpose(V, Vt);
        }

        return V;
    }

    protected void transpose( @NotNull DMatrixRMaj V, DMatrixRMaj Vt ) {
        CommonOps_DDRM.transpose(Vt, V);
    }

    @Override
    public DMatrixRMaj getW( @Nullable DMatrixRMaj W ) {
        int m = compact ? numSingular : numRows;
        int n = compact ? numSingular : numCols;

        if (W == null)
            W = new DMatrixRMaj(m, n);
        else {
            W.reshape(m, n, false);
            W.zero();
        }

        for (int i = 0; i < numSingular; i++) {
            W.unsafe_set(i, i, singularValues[i]);
        }

        return W;
    }

    @Override
    public boolean decompose( DMatrixRMaj orig ) {
        if (!setup(orig))
            return false;

        if (bidiagonalization(orig))
            return false;

        if (computeUWV())
            return false;

        // make sure all the singular values or positive
        makeSingularPositive();

        // if transposed undo the transposition
        undoTranspose();

        return true;
    }

    @Override
    public boolean inputModified() {
        return false;
    }

    private boolean bidiagonalization( DMatrixRMaj orig ) {
        // change the matrix to bidiagonal form
        if (transposed) {
            A_mod.reshape(orig.numCols, orig.numRows, false);
            transpose(A_mod, orig);
        } else {
            A_mod.reshape(orig.numRows, orig.numCols, false);
            A_mod.setTo(orig);
        }
        return !bidiag.decompose(A_mod);
    }

    /**
     * If the transpose was computed instead do some additional computations
     */
    private void undoTranspose() {
        if (transposed) {
            DMatrixRMaj temp = Vt;
            Vt = Ut;
            Ut = temp;
        }
    }

    /**
     * Compute singular values and U and V at the same time
     */
    private boolean computeUWV() {
        bidiag.getDiagonal(diag, off);
        qralg.setMatrix(numRowsT, numColsT, diag, off);

//        long pointA = System.currentTimeMillis();
        // compute U and V matrices
        if (computeU)
            Ut = bidiag.getU(Ut, true, compact);
        if (computeV)
            Vt = bidiag.getV(Vt, true, compact);

        qralg.setFastValues(false);
        if (computeU)
            qralg.setUt(Ut);
        else
            qralg.setUt(null);
        if (computeV)
            qralg.setVt(Vt);
        else
            qralg.setVt(null);

//        long pointB = System.currentTimeMillis();

        boolean ret = !qralg.process();

//        long pointC = System.currentTimeMillis();
//        System.out.println("  compute UV "+(pointB-pointA)+"  QR = "+(pointC-pointB));

        return ret;
    }

    private boolean setup( DMatrixRMaj orig ) {
        transposed = orig.numCols > orig.numRows;

        // flag what should be computed and what should not be computed
        if (transposed) {
            computeU = prefComputeV;
            computeV = prefComputeU;
            numRowsT = orig.numCols;
            numColsT = orig.numRows;
        } else {
            computeU = prefComputeU;
            computeV = prefComputeV;
            numRowsT = orig.numRows;
            numColsT = orig.numCols;
        }

        numRows = orig.numRows;
        numCols = orig.numCols;

        if (numRows == 0 || numCols == 0)
            return false;

        if (diag == null || diag.length < numColsT) {
            diag = new double[numColsT];
            off = new double[numColsT - 1];
        }

        // if it is a tall matrix and U is not needed then there is faster decomposition algorithm
        declareBidiagonalDecomposition();

        return true;
    }

    protected void declareBidiagonalDecomposition() {
        if (canUseTallBidiagonal && numRows > numCols*2 && !computeU) {
            if (bidiag == null || !(bidiag instanceof BidiagonalDecompositionTall_DDRM)) {
                bidiag = new BidiagonalDecompositionTall_DDRM();
            }
        } else if (bidiag == null || !(bidiag instanceof BidiagonalDecompositionRow_DDRM)) {
            bidiag = new BidiagonalDecompositionRow_DDRM();
        }
    }

    /**
     * With the QR algorithm it is possible for the found singular values to be negative.  This
     * makes them all positive by multiplying it by a diagonal matrix that has
     */
    private void makeSingularPositive() {
        numSingular = qralg.getNumberOfSingularValues();
        singularValues = qralg.getSingularValues();

        for (int i = 0; i < numSingular; i++) {
            double val = qralg.getSingularValue(i);

            if (val < 0) {
                singularValues[i] = 0.0 - val;

                if (computeU) {
                    // compute the results of multiplying it by an element of -1 at this location in
                    // a diagonal matrix.
                    int start = i*Ut.numCols;
                    int stop = start + Ut.numCols;

                    for (int j = start; j < stop; j++) {
                        Ut.set(j, 0.0 - Ut.get(j));
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
