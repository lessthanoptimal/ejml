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
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.ops.CCommonOps;


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
public class QRDecompositionHouseholder_CD64 implements QRDecomposition<CDenseMatrix64F> {

    /**
     * Where the Q and R matrices are stored.  R is stored in the
     * upper triangular portion and Q on the lower bit.  Lower columns
     * are where u is stored.  Q_k = (I - gamma_k*u_k*u_k^H).
     */
    protected CDenseMatrix64F QR;

    // used internally to store temporary data
    protected double u[],v[];

    // dimension of the decomposed matrices
    protected int numCols; // this is 'n'
    protected int numRows; // this is 'm'
    protected int minLength;

    protected double dataQR[];

    // the computed gamma for Q_k matrix
    protected double gammas[];
    // local variables
    protected double realGamma; // gamma is always real
    protected double realTau,imagTau;


    // did it encounter an error?
    protected boolean error;

    public void setExpectedMaxSize( int numRows , int numCols ) {
        error = false;

        this.numCols = numCols;
        this.numRows = numRows;
        minLength = Math.min(numRows,numCols);
        int maxLength = Math.max(numRows,numCols);

        if( QR == null ) {
            QR = new CDenseMatrix64F(numRows,numCols);
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
    public CDenseMatrix64F getQR() {
        return QR;
    }

    /**
     * Computes the Q matrix from the information stored in the QR matrix.  This
     * operation requires about 4(m<sup>2</sup>n-mn<sup>2</sup>+n<sup>3</sup>/3) flops.
     *
     * @param Q The orthogonal Q matrix.
     */
    @Override
    public CDenseMatrix64F getQ( CDenseMatrix64F Q , boolean compact ) {
        if( compact ) {
            if( Q == null ) {
                Q = CCommonOps.identity(numRows,minLength);
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

        for( int j = minLength-1; j >= 0; j-- ) {
            u[2*j]   = 1;
            u[2*j+1] = 0;

            for( int i = j+1; i < numRows; i++ ) {
                int indexQR = QR.getIndex(i,j);
                u[i*2] = QR.data[indexQR];
                u[i*2+1] = QR.data[indexQR+1];
            }
            QrHelperFunctions_CD64.rank1UpdateMultR(Q,u,0,gammas[j],j,j,numRows,v);
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
                    R.set(i,j,0,0);
                }
            }
        }

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
    public boolean decompose( CDenseMatrix64F A ) {
        commonSetup(A);

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
    protected void householder( int j )
    {
        // find the element with the largest absolute value in the column and make a copy
        int indexQR = 2*(j+j*numCols);
        int indexU = 2*j;
        double max = 0;
        for( int i = j; i < numRows; i++ ) {

            double realD = u[indexU++] = dataQR[indexQR];
            double imagD = u[indexU++] = dataQR[indexQR+1];

            // absolute value of d
            double magD = realD*realD + imagD*imagD;
            if( max < magD ) {
                max = magD;
            }
            indexQR += numCols*2;
        }
        max = Math.sqrt(max);

        if( max == 0.0 ) {
            realGamma = 0;
            error = true;
        } else {
            // compute the norm2 of the vector, with each element
            // normalized by the max value to avoid overflow problems
            double nx = 0;
            indexU = 2*j;

            for( int i = j; i < numRows; i++ ) {
                double realD = u[indexU++] /= max;
                double imagD = u[indexU++] /= max;

                nx += realD*realD + imagD*imagD;
            }
            nx = Math.sqrt(nx);

            double real_x0 = u[2*j];
            double imag_x0 = u[2*j+1];
            double mag_x0 = Math.sqrt(real_x0*real_x0 + imag_x0*imag_x0);

            // TODO Could stability be improved by computing theta so that this
            // special case doesn't need to be handled?
            if( mag_x0 == 0 ) {
                realTau = nx;
                imagTau = 0;
            } else {
                realTau = real_x0 / mag_x0 * nx;
                imagTau = imag_x0 / mag_x0 * nx;
            }

            double top,bottom;

            // if there is a chance they can cancel swap the sign
            if ( real_x0*realTau<0) {
                realTau = -realTau;
                imagTau = -imagTau;
                top = nx * nx - nx *mag_x0;
                bottom = mag_x0*mag_x0 - 2.0* nx *mag_x0 + nx * nx;
            } else {
                top = nx * nx + nx *mag_x0;
                bottom = mag_x0*mag_x0 + 2.0* nx *mag_x0 + nx * nx;
            }

            realGamma = bottom/top;

            double real_u_0 = real_x0 + realTau;
            double imag_u_0 = imag_x0 + imagTau;
            double norm_u_0 = real_u_0*real_u_0 + imag_u_0*imag_u_0;

            indexU = (j+1)*2;
            for( int i = j+1; i < numRows; i++ ) {
                double realU = u[indexU];
                double imagU = u[indexU+1];

                u[indexU++] = (realU*real_u_0 + imagU*imag_u_0)/norm_u_0;
                u[indexU++] = (imagU*real_u_0 - realU*imag_u_0)/norm_u_0;
            }
            u[2*j  ] = 1;
            u[2*j+1] = 0;

            realTau *= max;
            imagTau *= max;
        }

        gammas[j] = realGamma;
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
    protected void updateA( int w )
    {
        // much of the code below is equivalent to the rank1Update function
        // however, since &tau; has already been computed there is no need to
        // recompute it, saving a few multiplication operations
//        for( int i = w+1; i < numCols; i++ ) {
//            double val = 0;
//
//            for( int k = w; k < numRows; k++ ) {
//                val += u[k]*dataQR[k*numCols +i];
//            }
//            v[i] = gamma*val;
//        }

        // This is functionally the same as the above code but the order has been changed
        // to avoid jumping the cpu cache

        int stride = numCols*2;
        double realU = u[w*2];
        double imagU = -u[w*2+1];

        int indexQR = w*stride+(w+1)*2;
        for( int i = w+1; i < numCols; i++ ) {

            double realQR = dataQR[indexQR++];
            double imagQR = dataQR[indexQR++];

            v[i*2]   = realU*realQR - imagU*imagQR;
            v[i*2+1] = realU*imagQR + imagU*realQR;
        }

        for( int k = w+1; k < numRows; k++ ) {
            realU = u[k*2];
            imagU = -u[k*2+1];

            indexQR = k*stride+(w+1)*2;
            for( int i = w+1; i < numCols; i++ ) {
                double realQR = dataQR[indexQR++];
                double imagQR = dataQR[indexQR++];

//                v[i] += u[k]*dataQR[k*numCols +i];
                v[i*2]   += realU*realQR - imagU*imagQR;
                v[i*2+1] += realU*imagQR + imagU*realQR;
            }
        }

        for( int i = w+1; i < numCols; i++ ) {
            v[i*2] *= realGamma;
            v[i*2+1] *= realGamma;
        }

        // end of reordered code

        for( int i = w; i < numRows; i++ ) {
            double realI = u[i*2];
            double imagI = u[i*2+1];

            indexQR = i*stride+(w+1)*2;
            for( int j = w+1; j < numCols; j++ ) {
                double realJ = v[j*2];
                double imagJ = v[j*2+1];

//                dataQR[i*numCols+j] -= valU*v[j];
                dataQR[indexQR++] -= realI*realJ - imagI*imagJ;
                dataQR[indexQR++] -= realI*imagJ + imagI*realJ;
            }
        }

        if( w < numCols ) {
            dataQR[2*w+w*stride] = -realTau;
            dataQR[2*w+w*stride+1] = -imagTau;
        }

        // save the Q matrix in the lower portion of QR
        for( int i = w+1; i < numRows; i++ ) {
            dataQR[2*w+i*stride]     = u[i*2];
            dataQR[2*w+i*stride + 1] = u[i*2 + 1];
        }
    }

    /**
     * This function performs sanity check on the input for decompose and sets up the QR matrix.
     *
     * @param A
     */
    protected void commonSetup(CDenseMatrix64F A) {
        setExpectedMaxSize(A.numRows,A.numCols);

        QR.set(A);
    }

    public double[] getGammas() {
        return gammas;
    }

}