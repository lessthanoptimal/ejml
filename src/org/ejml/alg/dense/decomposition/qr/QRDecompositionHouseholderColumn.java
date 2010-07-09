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

package org.ejml.alg.dense.decomposition.qr;

import org.ejml.alg.dense.decomposition.QRDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;


/**
 * <p>
 * Houesholder QR decomposition is rich in operations along the columns of the matrix.  This can be
 * taken advantage of by solving for the Q matrix in a column major format to reduce the number
 * of CPU cache misses and the number of copies that are performed.
 * </p>
 *
 * @see QRDecompositionHouseholder 
 *
 * @author Peter Abeles
 */
public class QRDecompositionHouseholderColumn implements QRDecomposition {

    /**
     * Where the Q and R matrices are stored.  R is stored in the
     * upper triangulr portion and Q on the lower bit.  Lower columns
     * are where u is stored.  Q_k = (I - gamma_k*u_k*u_k^T).
     */
    protected double dataQR[][]; // [ column][ row ]

    // used internally to store temporary data
    protected double v[];

    // it can decompose a matrix up to this size
    protected int maxCols;
    protected int maxRows;

    // dimension of the decomposed matrices
    protected int numCols; // this is 'n'
    protected int numRows; // this is 'm'

    // the computed gamma for Q_k matrix
    protected double gammas[];
    // local variables
    protected double gamma;
    protected double tau;

    // did it encounter an error?
    protected boolean error;

    @Override
    public void setExpectedMaxSize( int numRows , int numCols ) {
        if( numRows < numCols ) {
            throw new IllegalArgumentException("The number of rows must be more than or equal to the number of columns");
        }

        this.maxCols = numCols;
        this.maxRows = numRows;

        dataQR = new double[ maxCols ][  maxRows ];
        v = new double[ maxRows ];
        gammas = new double[ maxCols ];
    }

    /**
     * Returns the combined QR matrix in a 2D array format that is column major.
     *
     * @return The QR matrix in a 2D matrix column major format. [ column ][ row ]
     */
    public double[][] getQR() {
        return dataQR;
    }

    /**
     * Computes the Q matrix from the imformation stored in the QR matrix.  This
     * operation requires about 4(m<sup>2</sup>n-mn<sup>2</sup>+n<sup>3</sup>/3) flops.
     *
     * @param Q The orthogonal Q matrix.
     */
    @Override
    public DenseMatrix64F getQ( DenseMatrix64F Q , boolean compact ) {
        if( compact ) {
            if( Q == null ) {
                Q = CommonOps.identity(numRows,numCols);
            } else {
                if( Q.numRows != numRows || Q.numCols != numCols ) {
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

        for( int j = numCols-1; j >= 0; j-- ) {
            double u[] = dataQR[j];

            double vv = u[j];
            u[j] = 1;
            QRDecompositionHouseholder.rank1UpdateMultR(Q,u,gammas[j],j,j,numRows,v);
            u[j] = vv;
        }

        return Q;
    }

    /**
     * Returns an upper triangular matrix which is the R in the QR decomposition.
     *
     * @param R An upper triangular matrix.
     * @param setZeros
     */
    @Override
    public DenseMatrix64F getR(DenseMatrix64F R, boolean setZeros) {
        if( R == null ) {
            if( setZeros ) {
                R = new DenseMatrix64F(numRows,numCols);
                // no need to set zeros since they are all zero already
                setZeros = false;
            } else
                R = new DenseMatrix64F(numCols,numCols);
        } else {
            if( R.numCols != numCols ) {
                throw new IllegalArgumentException("Unexpected number of columns.");
            } else if( setZeros && R.numRows < numRows ) {
                throw new IllegalArgumentException("Unexpected number of rows.");
            } else if( !setZeros && R.numRows < numCols ) {
                throw new IllegalArgumentException("Unexpected number of rows.");
            }
        }

        for( int j = 0; j < numCols; j++ ) {
            double colR[] = dataQR[j];
            for( int i = 0; i <= j; i++ ) {
                double val = colR[i];
                R.set(i,j,val);
            }
        }

        if( setZeros ) {
            for( int i = 0; i < numRows; i++ ) {
                int max = i < numCols ? i : numCols;
                for( int j = 0; j < max; j++ ) {
                    R.set(i,j,0);
                }
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
        if( A.numCols > A.numRows ) {
            throw new IllegalArgumentException("The number of rows must be more than or equal to the number of columns");
        } else if( A.numCols > maxCols || A.numRows > maxRows ) {
            setExpectedMaxSize(A.numRows, A.numCols);
        }

        numCols = A.numCols;
        numRows = A.numRows;

        convertToColumnMajor(A);

        error = false;

        for( int j = 0; j < numCols; j++ ) {
            householder(j);
            updateA(j);
        }

        return !error;
    }

    /**
     * Converts the standard row-major matrix into a column-major vector
     * that is advantageous for this problem.
     *
     * @param A original matrix that is to be decomposed.
     */
    protected void convertToColumnMajor(DenseMatrix64F A) {
        for( int x = 0; x < numCols; x++ ) {
            double colQ[] = dataQR[x];
            for( int y = 0; y < numRows; y++ ) {
                colQ[y] = A.data[y*numCols+x];
            }
        }
    }

    /**
     * <p>
     * Computes the householder vector "u" for the first column of submatrix j.  Note this is
     * a specialized householder for this problem.  There is some protection against
     * overfloaw and underflow.
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
    protected void householder( int j )
    {
        double u[] = dataQR[j];

        // find the element with the largest absolute value in the column and make a copy
        double max = 0;
        for( int i = j; i < numRows; i++ ) {
            double d = u[i];
            // absolute value of d
            if( d < 0 ) d = -d;
            if( max < d ) {
                max = d;
            }
        }

        if( max == 0.0 ) {
            gamma = 0;
            error = true;
        } else {
            // compute the norm2 of the matrix, with each element
            // normalized by the max value to avoid overflow problems
            tau = 0;
            for( int i = j; i < numRows; i++ ) {
                u[i] /= max;
                double d = u[i];
                tau += d*d;
            }
            tau = Math.sqrt(tau);

            if( u[j] < 0 )
                tau = -tau;

            double u_0 = u[j] + tau;
            gamma = u_0/tau;
            for( int i = j+1; i < numRows; i++ ) {
                u[i] /= u_0;
            }

            tau *= max;
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
    protected void updateA( int w )
    {
        double u[] = dataQR[w];

        for( int j = w+1; j < numCols; j++ ) {
            
            double colQ[] = dataQR[j];
            double val = colQ[w];

            for( int k = w+1; k < numRows; k++ ) {
                val += u[k]*colQ[k];
            }
            val *= gamma;

            colQ[w] -= val;
            for( int i = w+1; i < numRows; i++ ) {
                colQ[i] -= u[i]*val;
            }
        }

        if( w < numCols ) {
            u[w] = -tau;
        }
    }

    public double[] getGammas() {
        return gammas;
    }
}