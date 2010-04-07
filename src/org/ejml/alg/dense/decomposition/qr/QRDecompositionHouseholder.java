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
 * This variation of QR decomposition uses reflections to compute the Q matrix.
 * Each reflection uses a householder operations, hence its name.  To provide a meaningful solution
 * the original matrix must have full rank.  This is intended for processing of small to medium matrices.
 * </p>
 * <p>
 * Both Q and R are stored in the same m by n matrix.  Q is not stored directly, instead the u from
 * Q<sub>k</sub>=(I-&gamma;*u*u<sup>T</sup>) is stored.  Decomposition requires about 2n*m<sup>2</sup>-2m<sup>2</sup>/3 flops.
 * </p>
 *
 * <p>
 * See the QR reflections algorithm described in:<br>
 * David S. Watkins, "Fundamentals of Matrix Computations" 2nd Edition, 2002
 * </p>
 *
 * <p>
 * For the most part this is a straight forward implementation.  To improve performance on large matrices a column is writen to an array and the order
 * of some of the loops has been changed.  This will degrade performance noticably on small matrices.  Since
 * it is unlikely that the QR decomposition would be a bottle neck when small matrices are involved only
 * one implementation is provided.
 * </p>
 *
 * @author Peter Abeles
 */
public class QRDecompositionHouseholder implements QRDecomposition {

    /**
     * Where the Q and R matrices are stored.  R is stored in the
     * upper triangulr portion and Q on the lower bit.  Lower columns
     * are where u is stored.  Q_k = (I - gamma_k*u_k*u_k^T).
     */
    protected DenseMatrix64F QR;

    // used internally to store temporary data
    protected double u[],v[];

    // the maximum possible dimensions
    protected int maxRows = -1;
    protected int maxCols = -1;

    // dimension of the decomposed matrices
    protected int numCols; // this is 'n'
    protected int numRows; // this is 'm'

    protected double dataQR[];

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

        QR = new DenseMatrix64F(numRows,numCols);
        dataQR = QR.data;
        this.maxRows = numRows;
        this.maxCols = numCols;
        u = new double[ numRows ];
        v = new double[ numRows ];
        gammas = new double[ numRows ];
    }

    /**
     * Returns a single matrix which contains the combined values of Q and R.  This
     * is possible since Q is symmetric and R is upper triangular.
     *
     * @return The combined Q R matrix.
     */
    public DenseMatrix64F getQR() {
        return QR;
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
            u[j] = 1;
            for( int i = j+1; i < numRows; i++ ) {
                u[i] = QR.get(i,j);
            }
            rank1UpdateMultR(Q,u,gammas[j],j,j,numRows,v);
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

        for( int i = 0; i < numCols; i++ ) {
            for( int j = i; j < numCols; j++ ) {
                double val = QR.get(i,j);
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
     * Inorder to decompose the matrix 'A' it must have full rank.  'A' is a 'm' by 'n' matrix.
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
        commonSetup(A);

        for( int j = 0; j < numCols; j++ ) {
            householder(j);
            updateA(j);
        }

        return !error;
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
        // find the element with the largest absolute value in the column and make a copy
        int index = j+j*numCols;
        double max = 0;
        for( int i = j; i < numRows; i++ ) {

            double d = u[i] = dataQR[index];

            // absolute value of d
            if( d < 0 ) d = -d;
            if( max < d ) {
                max = d;
            }
            index += numCols;
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
            u[j] = 1;
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
        for( int i = w+1; i < numCols; i++ ) {
            v[i] = u[w]*dataQR[w*numCols +i];
        }

        for( int k = w+1; k < numRows; k++ ) {
            int indexQR = k*numCols+w+1;
            for( int i = w+1; i < numCols; i++ ) {
//                v[i] += u[k]*dataQR[k*numCols +i];
                v[i] += u[k]*dataQR[indexQR++];
            }
        }

        for( int i = w+1; i < numCols; i++ ) {
            v[i] *= gamma;
        }

        // end of reordered code

        for( int i = w; i < numRows; i++ ) {
            double valU = u[i];

            int indexQR = i*numCols+w+1;
            for( int j = w+1; j < numCols; j++ ) {
//                dataQR[i*numCols+j] -= valU*v[j];
                dataQR[indexQR++] -= valU*v[j];
            }
        }

        if( w < numCols ) {
            dataQR[w+w*numCols] = -tau;
        }

        // save the Q matrix in the lower portion of QR
        for( int i = w+1; i < numRows; i++ ) {
            dataQR[w+i*numCols] = u[i];
        }
    }

    /**
     * This function performs sanity check on the input for decompose and sets up the QR matrix.
     *
     * @param A
     */
    protected void commonSetup(DenseMatrix64F A) {
        if( A.numCols > A.numRows ) {
            throw new IllegalArgumentException("The number of rows must be more than or equal to the number of columns");
        } else if( A.numCols > maxCols || A.numRows > maxRows ) {
           setExpectedMaxSize(A.numRows,A.numCols);
        }

        QR.setReshape(A);

        numCols = A.numCols;
        numRows = A.numRows;
        error = false;
    }

    public double[] getGammas() {
        return gammas;
    }

    /**
     * <p>
     * Performs a rank-1 update operation on the submatrix specified by w with the multiply on the right.<br>
     * <br>
     * A = (I - &gamma;*u*u<sup>T</sup>)*A<br>
     * </p>
     * <p>
     * The order that matrix multiplies are performed has been carefully selected
     * to minimize the number of operations.
     * </p>
     *
     * <p>
     * Before this can become a truly generic operation the submatrix specification needs
     * to be made more generic.
     * </p>
     */
    public static void rank1UpdateMultR( DenseMatrix64F A , double u[] , double gamma ,
                                         int colA0,
                                         int w0, int w1 ,
                                         double _temp[] )
    {
//        for( int i = colA0; i < A.numCols; i++ ) {
//            double val = 0;
//
//            for( int k = w0; k < w1; k++ ) {
//                val += u[k]*A.data[k*A.numCols +i];
//            }
//            _temp[i] = gamma*val;
//        }

        // reordered to reduce cpu cache issues
        for( int i = colA0; i < A.numCols; i++ ) {
            _temp[i] = u[w0]*A.data[w0 *A.numCols +i];
        }

        for( int k = w0+1; k < w1; k++ ) {
            int row = k*A.numCols;
            double valU = u[k];
            for( int i = colA0; i < A.numCols; i++ ) {
                _temp[i] += valU*A.data[row +i];
            }
        }
        for( int i = colA0; i < A.numCols; i++ ) {
            _temp[i] *= gamma;
        }

        // end of reorder

        for( int i = w0; i < w1; i++ ) {
            double valU = u[i];

            for( int j = colA0; j < A.numCols; j++ ) {
                A.data[i*A.numCols+j] -= valU*_temp[j];
            }
        }
    }

    /**
     * <p>
     * Performs a rank-1 update operation on the submatrix specified by w with the multiply on the left.<br>
     * <br>
     * A = A(I - &gamma;*u*u<sup>T</sup>)<br>
     * </p>
     * <p>
     * The order that matrix multiplies are performed has been carefully selected
     * to minimize the number of operations.
     * </p>
     *
     * <p>
     * Before this can become a truly generic operation the submatrix specification needs
     * to be made more generic.
     * </p>
     */
    public static void rank1UpdateMultL( DenseMatrix64F A , double u[] ,
                                         double gamma ,
                                         int colA0,
                                         int w0 , int w1 ,
                                         double _temp[] )
    {
        for( int i = colA0; i < A.numRows; i++ ) {
            double sum = 0;
            for( int j = w0; j < w1; j++ ) {
                sum += A.data[i*A.numCols+j]*u[j];
            }
            _temp[i] = -gamma*sum;
        }
        
        for( int i = colA0; i < A.numRows; i++ ) {
            double a = _temp[i];
            for( int j = w0; j < w1; j++ ) {
                A.data[i*A.numCols+j] += a*u[j];
            }
        }
    }
}