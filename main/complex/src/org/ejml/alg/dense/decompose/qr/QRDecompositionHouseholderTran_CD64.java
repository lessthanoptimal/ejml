/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decompose.qr;

import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.ops.CCommonOps;


/**
 * <p>
 * Householder QR decomposition is rich in operations along the columns of the matrix.  This can be
 * taken advantage of by solving for the Q matrix in a column major format to reduce the number
 * of CPU cache misses and the number of copies that are performed.
 * </p>
 *
 * @see org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholder_D64
 *
 * @author Peter Abeles
 */
// TODO figure out why this is significantly slower than col
public class QRDecompositionHouseholderTran_CD64 implements QRDecomposition<CDenseMatrix64F> {

    /**
     * Where the Q and R matrices are stored.  For speed reasons
     * this is transposed
     */
    protected CDenseMatrix64F QR;

    // used internally to store temporary data
    protected double v[];

    // dimension of the decomposed matrices
    protected int numCols; // this is 'n'
    protected int numRows; // this is 'm'
    protected int minLength;

    // the computed gamma for Q_k matrix
    protected double gammas[];
    // local variables
    protected double gamma;
    protected Complex64F tau = new Complex64F();

    // did it encounter an error?
    protected boolean error;

    public void setExpectedMaxSize( int numRows , int numCols ) {
        this.numCols = numCols;
        this.numRows = numRows;
        minLength = Math.min(numCols,numRows);
        int maxLength = Math.max(numCols,numRows);

        if( QR == null ) {
            QR = new CDenseMatrix64F(numCols,numRows);
            v = new double[ maxLength*2 ];
            gammas = new double[ minLength ];
        } else {
            QR.reshape(numCols,numRows);
        }

        if( v.length < maxLength*2 ) {
            v = new double[ maxLength*2 ];
        }
        if( gammas.length < minLength ) {
            gammas = new double[ minLength ];
        }
    }

    /**
     * Inner matrix that stores the decomposition
     */
    public CDenseMatrix64F getQR() {
        return QR;
    }

    /**
     * Computes the Q matrix from the information stored in the QR matrix.  This
     * operation requires about 4(m<sup2</sup>n-mn<sup>2</sup>+n<sup>3</sup>/3) flops.
     *
     * @param Q The orthogonal Q matrix.
     */
    @Override
    public CDenseMatrix64F getQ( CDenseMatrix64F Q , boolean compact ) {
        if( compact ) {
            if( Q == null ) {
                Q = CCommonOps.identity(numRows, minLength);
            } else {
                if( Q.numRows != numRows || Q.numCols != minLength ) {
                    throw new IllegalArgumentException("Unexpected matrix dimension.");
                } else {
                    CCommonOps.setIdentity(Q);
                }
            }
        } else {
            if( Q == null ) {
                Q = CCommonOps.identity(numRows);
            } else {
                if( Q.numRows != numRows || Q.numCols != numRows ) {
                    throw new IllegalArgumentException("Unexpected matrix dimension.");
                } else {
                    CCommonOps.setIdentity(Q);
                }
            }
        }

        // Unlike applyQ() this takes advantage of zeros in the identity matrix
        // by not multiplying across all rows.
        for( int j = minLength-1; j >= 0; j-- ) {
            int diagIndex = (j*numRows+j)*2;
            double realBefore = QR.data[diagIndex];
            double imagBefore = QR.data[diagIndex+1];

            QR.data[diagIndex] = 1;
            QR.data[diagIndex+1] = 0;

            QrHelperFunctions_CD64.rank1UpdateMultR(Q, QR.data, j * numRows, gammas[j], j, j, numRows, v);

            QR.data[diagIndex] = realBefore;
            QR.data[diagIndex+1] = imagBefore;
        }

        return Q;
    }

    /**
     * A = Q*A
     *
     * @param A Matrix that is being multiplied by Q.  Is modified.
     */
    public void applyQ( CDenseMatrix64F A ) {
        if( A.numRows != numRows )
            throw new IllegalArgumentException("A must have at least "+numRows+" rows.");

        for( int j = minLength-1; j >= 0; j-- ) {
            int diagIndex = (j*numRows+j)*2;
            double realBefore = QR.data[diagIndex];
            double imagBefore = QR.data[diagIndex+1];

            QR.data[diagIndex] = 1;
            QR.data[diagIndex+1] = 0;

            QrHelperFunctions_CD64.rank1UpdateMultR(A, QR.data, j * numRows, gammas[j], 0, j, numRows, v);

            QR.data[diagIndex] = realBefore;
            QR.data[diagIndex+1] = imagBefore;
        }
    }

    /**
     * A = Q<sup>H</sup>*A
     *
     * @param A Matrix that is being multiplied by Q<sup>T</sup>.  Is modified.
     */
    public void applyTranQ( CDenseMatrix64F A ) {
        for( int j = 0; j < minLength; j++ ) {
            int diagIndex = (j*numRows+j)*2;
            double realBefore = QR.data[diagIndex];
            double imagBefore = QR.data[diagIndex+1];

            QR.data[diagIndex] = 1;
            QR.data[diagIndex+1] = 0;

            QrHelperFunctions_CD64.rank1UpdateMultR(A, QR.data, j * numRows, gammas[j], 0, j, numRows, v);

            QR.data[diagIndex] = realBefore;
            QR.data[diagIndex+1] = imagBefore;
        }
    }

    /**
     * Returns an upper triangular matrix which is the R in the QR decomposition.
     *
     * @param R An upper triangular matrix.
     * @param compact
     */
    @Override
    public CDenseMatrix64F getR(CDenseMatrix64F R, boolean compact) {
        if( R == null ) {
            if( compact ) {
                R = new CDenseMatrix64F(minLength,numCols);
            } else
                R = new CDenseMatrix64F(numRows,numCols);
        } else {
            if( compact ) {
                if( R.numCols != numCols || R.numRows != minLength )
                    throw new IllegalArgumentException("Unexpected dimensions");
            } else {
                if( R.numCols != numCols || R.numRows != numRows )
                    throw new IllegalArgumentException("Unexpected dimensions");
            }

            for( int i = 0; i < R.numRows; i++ ) {
                int min = Math.min(i,R.numCols);
                for( int j = 0; j < min; j++ ) {
                    R.set(i, j, 0, 0);
                }
            }
        }

        for( int i = 0; i < R.numRows; i++ ) {
            for( int j = i; j < R.numCols; j++ ) {
                int index = QR.getIndex(j,i);
                R.set(i,j,QR.data[index],QR.data[index+1]);
            }
        }

        return R;
    }

    /**
     * <p>
     * To decompose the matrix 'A' it must have full rank.  'A' is a 'm' by 'n' matrix.
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
    public boolean decompose( CDenseMatrix64F A ) {
        setExpectedMaxSize(A.numRows, A.numCols);

        CCommonOps.transpose(A, QR);

        error = false;

        for( int j = 0; j < minLength; j++ ) {
            householder(j);
            updateA(j);
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
    protected void householder( final int j )
    {
        int startQR = j*numRows;
        int endQR = startQR+numRows;
        startQR += j;

        final double max = QrHelperFunctions_CD64.findMax(QR.data, startQR, numRows - j);

        if( max == 0.0 ) {
            gamma = 0;
            error = true;
        } else {
            // computes tau and normalizes u by max
            gamma = QrHelperFunctions_CD64.computeTauGammaAndDivide(startQR, endQR, QR.data, max,tau);

            // divide u by u_0
            double realU0 = QR.data[startQR*2] + tau.real;
            double imagU0 = QR.data[startQR*2+1] + tau.imaginary;

            QrHelperFunctions_CD64.divideElements(startQR + 1, endQR, QR.data,0,realU0, imagU0);

            tau.real *= max;
            tau.imaginary *= max;

            QR.data[startQR*2] = -tau.real;
            QR.data[startQR*2+1] = -tau.imaginary;
        }

        gammas[j] = gamma;
    }

    /**
     * <p>
     * Takes the results from the householder computation and updates the 'A' matrix.<br>
     * <br>
     * A = (I - &gamma;*u*u<sup>H</sup>)A
     * </p>
     *
     * @param w The submatrix.
     */
    protected void updateA( final int w )
    {
//        int rowW = w*numRows;
//        int rowJ = rowW + numRows;
//
//        for( int j = w+1; j < numCols; j++ , rowJ += numRows) {
//            double val = QR.data[rowJ + w];
//
//            // val = gamma*u^T * A
//            for( int k = w+1; k < numRows; k++ ) {
//                val += QR.data[rowW + k]*QR.data[rowJ + k];
//            }
//            val *= gamma;
//
//            // A - val*u
//            QR.data[rowJ + w] -= val;
//            for( int i = w+1; i < numRows; i++ ) {
//                QR.data[rowJ + i] -= QR.data[rowW + i]*val;
//            }
//        }

        final double data[] = QR.data;
        int rowW = w*numRows + w + 1;
        int rowJ = rowW + numRows;
        final int rowJEnd = 2*(rowJ + (numCols-w-1)*numRows);
        final int indexWEnd = 2*(rowW + numRows - w - 1);

        rowJ = 2*rowJ;
        rowW = 2*rowW;

        for( ; rowJEnd != rowJ; rowJ += numRows*2) {
            // assume the first element in u is 1
            double realVal = data[rowJ - 2];
            double imagVal = data[rowJ - 1];

            int indexW = rowW;
            int indexJ = rowJ;

            while( indexW != indexWEnd ) {
                double realW = data[indexW++];
                double imagW = -data[indexW++];

                double realJ = data[indexJ++];
                double imagJ = data[indexJ++];

                realVal += realW*realJ - imagW*imagJ;
                imagVal += realW*imagJ + imagW*realJ;
            }
            realVal *= gamma;
            imagVal *= gamma;

            data[rowJ - 2] -= realVal;
            data[rowJ - 1] -= imagVal;

            indexW = rowW;
            indexJ = rowJ;
            while( indexW != indexWEnd ) {
                double realW = data[indexW++];
                double imagW = data[indexW++];

                data[indexJ++] -= realW*realVal - imagW*imagVal;
                data[indexJ++] -= realW*imagVal + imagW*realVal;
            }
        }
    }

    public double[] getGammas() {
        return gammas;
    }
}