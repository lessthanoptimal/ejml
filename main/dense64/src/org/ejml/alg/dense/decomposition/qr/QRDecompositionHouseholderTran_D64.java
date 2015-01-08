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

package org.ejml.alg.dense.decomposition.qr;

import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.ops.CommonOps;


/**
 * <p>
 * Householder QR decomposition is rich in operations along the columns of the matrix.  This can be
 * taken advantage of by solving for the Q matrix in a column major format to reduce the number
 * of CPU cache misses and the number of copies that are performed.
 * </p>
 *
 * @see QRDecompositionHouseholder_D64
 *
 * @author Peter Abeles
 */
// TODO remove QR Col and replace with this one?
// -- On small matrices col seems to be about 10% faster
public class QRDecompositionHouseholderTran_D64 implements QRDecomposition<DenseMatrix64F> {

    /**
     * Where the Q and R matrices are stored.  For speed reasons
     * this is transposed
     */
    protected DenseMatrix64F QR;

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
    protected double tau;

    // did it encounter an error?
    protected boolean error;

    public void setExpectedMaxSize( int numRows , int numCols ) {
        this.numCols = numCols;
        this.numRows = numRows;
        minLength = Math.min(numCols,numRows);
        int maxLength = Math.max(numCols,numRows);

        if( QR == null ) {
            QR = new DenseMatrix64F(numCols,numRows);
            v = new double[ maxLength ];
            gammas = new double[ minLength ];
        } else {
            QR.reshape(numCols,numRows,false);
        }

        if( v.length < maxLength ) {
            v = new double[ maxLength ];
        }
        if( gammas.length < minLength ) {
            gammas = new double[ minLength ];
        }
    }

    /**
     * Inner matrix that stores the decomposition
     */
    public DenseMatrix64F getQR() {
        return QR;
    }

    /**
     * Computes the Q matrix from the information stored in the QR matrix.  This
     * operation requires about 4(m<sup2</sup>n-mn<sup>2</sup>+n<sup>3</sup>/3) flops.
     *
     * @param Q The orthogonal Q matrix.
     */
    @Override
    public DenseMatrix64F getQ( DenseMatrix64F Q , boolean compact ) {
        if( compact ) {
            if( Q == null ) {
                Q = CommonOps.identity(numRows,minLength);
            } else {
                if( Q.numRows != numRows || Q.numCols != minLength ) {
                    throw new IllegalArgumentException("Unexpected matrix dimension.");
                } else {
                    CommonOps.setIdentity(Q);
                }
            }
        } else {
            if( Q == null ) {
                Q = CommonOps.identity(numRows);
            } else {
                if( Q.numRows != numRows || Q.numCols != numRows ) {
                    throw new IllegalArgumentException("Unexpected matrix dimension.");
                } else {
                    CommonOps.setIdentity(Q);
                }
            }
        }

        // Unlike applyQ() this takes advantage of zeros in the identity matrix
        // by not multiplying across all rows.
        for( int j = minLength-1; j >= 0; j-- ) {
            int diagIndex = j*numRows+j;
            double before = QR.data[diagIndex];
            QR.data[diagIndex] = 1;
            QrHelperFunctions_D64.rank1UpdateMultR(Q, QR.data, j * numRows, gammas[j], j, j, numRows, v);
            QR.data[diagIndex] = before;
        }

        return Q;
    }

    /**
     * A = Q*A
     *
     * @param A Matrix that is being multiplied by Q.  Is modified.
     */
    public void applyQ( DenseMatrix64F A ) {
        if( A.numRows != numRows )
            throw new IllegalArgumentException("A must have at least "+numRows+" rows.");

        for( int j = minLength-1; j >= 0; j-- ) {
            int diagIndex = j*numRows+j;
            double before = QR.data[diagIndex];
            QR.data[diagIndex] = 1;
            QrHelperFunctions_D64.rank1UpdateMultR(A, QR.data, j * numRows, gammas[j], 0, j, numRows, v);
            QR.data[diagIndex] = before;
        }
    }

    /**
     * A = Q<sup>T</sup>*A
     *
     * @param A Matrix that is being multiplied by Q<sup>T</sup>.  Is modified.
     */
    public void applyTranQ( DenseMatrix64F A ) {
        for( int j = 0; j < minLength; j++ ) {
            int diagIndex = j*numRows+j;
            double before = QR.data[diagIndex];
            QR.data[diagIndex] = 1;
            QrHelperFunctions_D64.rank1UpdateMultR(A, QR.data, j * numRows, gammas[j], 0, j, numRows, v);
            QR.data[diagIndex] = before;
        }
    }

    /**
     * Returns an upper triangular matrix which is the R in the QR decomposition.
     *
     * @param R An upper triangular matrix.
     * @param compact
     */
    @Override
    public DenseMatrix64F getR(DenseMatrix64F R, boolean compact) {
        if( R == null ) {
            if( compact ) {
                R = new DenseMatrix64F(minLength,numCols);
            } else
                R = new DenseMatrix64F(numRows,numCols);
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
                    R.unsafe_set(i,j,0);
                }
            }
        }

        for( int i = 0; i < R.numRows; i++ ) {
            for( int j = i; j < R.numCols; j++ ) {
                R.unsafe_set(i,j,QR.unsafe_get(j,i));
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
    public boolean decompose( DenseMatrix64F A ) {
        setExpectedMaxSize(A.numRows, A.numCols);

        CommonOps.transpose(A,QR);

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
     * Q = I - &gamma;uu<sup>T</sup>
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

        final double max = QrHelperFunctions_D64.findMax(QR.data, startQR, numRows - j);

        if( max == 0.0 ) {
            gamma = 0;
            error = true;
        } else {
            // computes tau and normalizes u by max
            tau = QrHelperFunctions_D64.computeTauAndDivide(startQR, endQR, QR.data, max);

            // divide u by u_0
            double u_0 = QR.data[startQR] + tau;
            QrHelperFunctions_D64.divideElements(startQR + 1, endQR, QR.data, u_0);

            gamma = u_0/tau;
            tau *= max;

            QR.data[startQR] = -tau;
        }

        gammas[j] = gamma;
    }

    /**
     * <p>
     * Takes the results from the householder computation and updates the 'A' matrix.<br>
     * <br>
     * A = (I - &gamma;*u*u<sup>T</sup>)A
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
        final int rowW = w*numRows + w + 1;
        int rowJ = rowW + numRows;
        final int rowJEnd = rowJ + (numCols-w-1)*numRows;
        final int indexWEnd = rowW + numRows - w - 1;

        for( ; rowJEnd != rowJ; rowJ += numRows) {
            // assume the first element in u is 1
            double val = data[rowJ - 1];

            int indexW = rowW;
            int indexJ = rowJ;

            while( indexW != indexWEnd ) {
                val += data[indexW++]*data[indexJ++];
            }
            val *= gamma;

            data[rowJ - 1] -= val;
            indexW = rowW;
            indexJ = rowJ;
            while( indexW != indexWEnd ) {
                data[indexJ++] -= data[indexW++]*val;
            }
        }
    }

    public double[] getGammas() {
        return gammas;
    }
}