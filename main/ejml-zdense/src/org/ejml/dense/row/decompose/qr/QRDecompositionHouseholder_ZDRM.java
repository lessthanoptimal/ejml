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

package org.ejml.dense.row.decompose.qr;

import org.ejml.data.Complex_F64;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.decompose.UtilDecompositons_ZDRM;
import org.ejml.interfaces.decomposition.QRDecomposition;


/**
 * <p>
 * This variation of complex QR decomposition uses reflections to compute the Q matrix.
 * Each reflection uses a householder operations, hence its name.  To provide a meaningful solution
 * the original matrix must have full rank.  This is intended for processing of small to medium matrices.
 * </p>
 * <p>
 * Both Q and R are stored in the same m by n matrix.  Q is not stored directly, instead the u from
 * Q<sub>k</sub>=(I-&gamma;*u*u<sup>H</sup>) is stored.  Decomposition requires about 2n*m<sup>2</sup>-2m<sup>2</sup>/3 flops.
 * </p>
 *
 * <p>
 * See the QR reflections algorithm described in:<br>
 * David S. Watkins, "Fundamentals of Matrix Computations" 2nd Edition, 2002
 * </p>
 *
 * <p>
 * For the most part this is a straight forward implementation.  To improve performance on large matrices a column is
 * written to an array and the order
 * of some of the loops has been changed.  This will degrade performance noticeably on small matrices.  Since
 * it is unlikely that the QR decomposition would be a bottle neck when small matrices are involved only
 * one implementation is provided.
 * </p>
 *
 * @author Peter Abeles
 */
public class QRDecompositionHouseholder_ZDRM implements QRDecomposition<ZMatrixRMaj> {

    /**
     * Where the Q and R matrices are stored.  R is stored in the
     * upper triangular portion and Q on the lower bit.  Lower columns
     * are where u is stored.  Q_k = (I - gamma_k*u_k*u_k^H).
     */
    protected ZMatrixRMaj QR;

    // used internally to store temporary data
    protected double u[],v[];

    // dimension of the decomposed matrices
    protected int numCols; // this is 'n'
    protected int numRows; // this is 'm'
    protected int minLength;

    protected double dataQR[];

    // the computed gamma for Q_k matrix
    protected double gammas[];
    protected Complex_F64 tau = new Complex_F64();


    // did it encounter an error?
    protected boolean error;

    public void setExpectedMaxSize( int numRows , int numCols ) {
        error = false;

        this.numCols = numCols;
        this.numRows = numRows;
        minLength = Math.min(numRows,numCols);
        int maxLength = Math.max(numRows,numCols);

        if( QR == null ) {
            QR = new ZMatrixRMaj(numRows,numCols);
            u = new double[ maxLength*2 ];
            v = new double[ maxLength*2 ];
            gammas = new double[ minLength ];
        } else {
            QR.reshape(numRows,numCols);
        }

        dataQR = QR.data;

        if( u.length < maxLength*2 ) {
            u = new double[ maxLength*2 ];
            v = new double[ maxLength*2 ];
        }

        if( gammas.length < minLength ) {
            gammas = new double[ minLength ];
        }
    }

    /**
     * Returns a single matrix which contains the combined values of Q and R.  This
     * is possible since Q is symmetric and R is upper triangular.
     *
     * @return The combined Q R matrix.
     */
    public ZMatrixRMaj getQR() {
        return QR;
    }

    /**
     * Computes the Q matrix from the information stored in the QR matrix.  This
     * operation requires about 4(m<sup>2</sup>n-mn<sup>2</sup>+n<sup>3</sup>/3) flops.
     *
     * @param Q The orthogonal Q matrix.
     */
    @Override
    public ZMatrixRMaj getQ(ZMatrixRMaj Q , boolean compact ) {
        if( compact )
            Q = UtilDecompositons_ZDRM.checkIdentity(Q,numRows,minLength);
        else
            Q = UtilDecompositons_ZDRM.checkIdentity(Q,numRows,numRows);

        for( int j = minLength-1; j >= 0; j-- ) {
            QrHelperFunctions_ZDRM.extractHouseholderColumn(QR,j,numRows,j,u,0);
            QrHelperFunctions_ZDRM.rank1UpdateMultR(Q,u,0,gammas[j],j,j,numRows,v);
        }

        return Q;
    }

    /**
     * Returns an upper triangular matrix which is the R in the QR decomposition.
     *
     * @param R An upper triangular matrix.
     * @param compact
     */
    @Override
    public ZMatrixRMaj getR(ZMatrixRMaj R, boolean compact) {
        if( compact )
            R = UtilDecompositons_ZDRM.checkZerosLT(R,minLength,numCols);
        else
            R = UtilDecompositons_ZDRM.checkZerosLT(R,numRows,numCols);

        for( int i = 0; i < minLength; i++ ) {
            for( int j = i; j < numCols; j++ ) {
                int indexQR = QR.getIndex(i,j);
                double realQR = QR.data[indexQR];
                double imagQR = QR.data[indexQR+1];

                R.set(i,j,realQR,imagQR);
            }
        }

        return R;
    }

    /**
     * <p>
     * In order to decompose the matrix 'A' it must have full rank.  'A' is a 'm' by 'n' matrix.
     * It requires about 2n*m<sup>2</sup>-2m<sup>2</sup>/3 flops.
     * </p>
     *
     * <p>
     * The matrix provided here can be of different
     * dimension than the one specified in the constructor.  It just has to be smaller than or equal
     * to it.
     * </p>
     */
    @Override
    public boolean decompose( ZMatrixRMaj A ) {
        commonSetup(A);

        for( int j = 0; j < minLength; j++ ) {
            householder(j);
        }

        return !error;
    }

    @Override
    public boolean inputModified() {
        return false;
    }

    /**
     * <p>
     * Computes the householder vector "u" for the first column of submatrix j.  Note this is
     * a specialized householder for this problem.  There is some protection against
     * overflow and underflow.
     * </p>
     * <p>
     * Q = I - &gamma;uu<sup>H</sup>
     * </p>
     * <p>
     * This function finds the values of 'u' and '&gamma;'.
     * </p>
     *
     * @param j Which submatrix to work off of.
     */
    protected void householder( int j )
    {
        // find the element with the largest absolute value in the column and make a copy
        double max = QrHelperFunctions_ZDRM.extractColumnAndMax(QR,j,numRows,j,u,0);

        if( max <= 0.0 ) {
            gammas[j] = 0;
            error = true;
        } else {
            double gamma = QrHelperFunctions_ZDRM.computeTauGammaAndDivide(j,numRows,u,max,tau);
            gammas[j] = gamma;

            // divide u by u_0
            double real_u_0 = u[j*2]   + tau.real;
            double imag_u_0 = u[j*2+1] + tau.imaginary;
            QrHelperFunctions_ZDRM.divideElements(j + 1, numRows, u, 0, real_u_0,imag_u_0 );

            // write the reflector into the lower left column of the matrix
            for (int i = j+1; i < numRows; i++) {
                dataQR[(i*numCols+j)*2]   = u[i*2];
                dataQR[(i*numCols+j)*2+1] = u[i*2+1];
            }

            u[j*2]   = 1;
            u[j*2+1] = 0;

            QrHelperFunctions_ZDRM.rank1UpdateMultR(QR,u,0,gamma,j+1,j,numRows,v);

            // since the first element in the householder vector is known to be 1
            // store the full upper hessenberg
            if( j < numCols ) {
                dataQR[(j * numCols + j) * 2] = -tau.real * max;
                dataQR[(j * numCols + j) * 2 + 1] = -tau.imaginary * max;
            }
        }
    }

    /**
     * This function performs sanity check on the input for decompose and sets up the QR matrix.
     *
     * @param A
     */
    protected void commonSetup(ZMatrixRMaj A) {
        setExpectedMaxSize(A.numRows,A.numCols);

        QR.set(A);
    }

    public double[] getGammas() {
        return gammas;
    }

}