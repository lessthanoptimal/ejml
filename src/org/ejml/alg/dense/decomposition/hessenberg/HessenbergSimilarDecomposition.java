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

package org.ejml.alg.dense.decomposition.hessenberg;

import org.ejml.alg.dense.decomposition.qr.QrHelperFunctions;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * <p>
 * Finds the decomposition of a matrix in the form of:<br>
 * <br>
 * A = OHO<sup>T</sup><br>
 * <br>
 * where A is an m by m matrix, O is an orthogonal matrix, and H is an upper Hessenberg matrix.
 * </p>
 *
 * <p>
 * A matrix is upper Hessenberg if a<sup>ij</sup> = 0 for all i > j+1. For example, the following matrix
 * is upper Hessenberg.<br>
 * <br>
 * WRITE IT OUT USING A TABLE
 * </p>
 *
 * <p>
 * This decomposition is primarily used as a step for computing the eigenvalue decomposition of a matrix.
 * The basic algorithm comes from David S. Watkins, "Fundamentals of MatrixComputations" Second Edition.
 * </p>
 */
// TODO create a column based one similar to what was done for QR decomposition?
public class HessenbergSimilarDecomposition {
    // A combined matrix that stores te upper Hessenberg matrix and the orthogonal matrix.
    private DenseMatrix64F QH;
    // number of rows and columns of the matrix being decompose
    private int N;

    // the first element in the orthogonal vectors
    private double gammas[];
    // temporary storage
    private double b[];
    private double u[];

    /**
     * Creates a decomposition that won't need to allocate new memory if it is passed matrices up to
     * the specified size.
     *
     * @param initialSize Expected size of the matrices it will decompose.
     */
    public HessenbergSimilarDecomposition( int initialSize ) {
        QH = new DenseMatrix64F(initialSize,initialSize);
        gammas = new double[ initialSize ];
        b = new double[ initialSize ];
        u = new double[ initialSize ];
    }

    public HessenbergSimilarDecomposition() {
        this(5);
    }

    /**
     * Computes the decomposition of the provided matrix.  If no errors are detected then true is returned,
     * false otherwise.
     * @param A  The matrix that is being decomposed.  Not modified.
     * @return If it detects any errors or not.
     */
    public boolean decompose( DenseMatrix64F A )
    {
        if( A.numRows != A.numCols )
            throw new IllegalArgumentException("A must be square.");

        QH.reshape(A.numRows,A.numCols, false);
        QH.set(A);

        N = A.numCols;

        if( b.length < N ) {
            b = new double[ N ];
            gammas = new double[ N ];
            u = new double[ N ];
        }
        return _decompose();
    }

    /**
     * The raw QH matrix that is stored internally.
     *
     * @return QH matrix.
     */
    public DenseMatrix64F getQH() {
        return QH;
    }

    /**
     * An upper Hessenberg matrix from the decompostion.
     *
     * @param H If not null then the results will be stored here.  Otherwise a new matrix will be created.
     * @return The extracted H matrix.
     */
    public DenseMatrix64F getH( DenseMatrix64F H ) {
        if( H == null ) {
            H = new DenseMatrix64F(N,N);
        } else if( N != H.numRows || N != H.numCols )
            throw new IllegalArgumentException("The provided H must have the same dimensions as the decomposed matrix.");
        else
            H.zero();

        // copy the first row
        System.arraycopy(QH.data, 0, H.data, 0, N);

        for( int i = 1; i < N; i++ ) {
            for( int j = i-1; j < N; j++ ) {
                H.set(i,j, QH.get(i,j));
            }
        }

        return H;
    }

    /**
     * An orthogonal matrix that has the following property: H = Q<sup>T</sup>AQ
     *
     * @param Q If not null then the results will be stored here.  Otherwise a new matrix will be created.
     * @return The extracted Q matrix.
     */
    public DenseMatrix64F getQ( DenseMatrix64F Q ) {
        if( Q == null ) {
            Q = new DenseMatrix64F(N,N);
            for( int i = 0; i < N; i++ ) {
                Q.unsafe_set(i,i, 1);
            }
        } else if( N != Q.numRows || N != Q.numCols )
            throw new IllegalArgumentException("The provided H must have the same dimensions as the decomposed matrix.");
        else
            CommonOps.setIdentity(Q);

        for( int j = N-2; j >= 0; j-- ) {
            u[j+1] = 1;
            for( int i = j+2; i < N; i++ ) {
                u[i] = QH.unsafe_get(i,j);
            }
            QrHelperFunctions.rank1UpdateMultR(Q,u,gammas[j],j+1,j+1,N,b);
        }

        return Q;
    }

    /**
     * Internal function for computing the decomposition.
     */
    private boolean _decompose() {

        for( int k = 0; k < N-2; k++ ) {
            // find the largest value in this column
            // this is used to normalize the column and mitigate overflow/underflow
            double max = 0;

            for( int i = k+1; i < N; i++ ) {
                // copy the householder vector to vector outside of the matrix to reduce caching issues
                // big improvement on larger matrices and a relatively small performance hit on small matrices.
                double val = u[i] = QH.unsafe_get(i,k);
                val = Math.abs(val);
                if( val > max )
                    max = val;
            }

            if( max > 0 ) {
                // -------- set up the reflector Q_k

                double tau = 0;
                // normalize to reduce overflow/underflow
                // and compute tau for the reflector
                for( int i = k+1; i < N; i++ ) {
                    double val = u[i] /= max;
                    tau += val*val;
                }

                tau = Math.sqrt(tau);

                if( u[k+1] < 0 )
                    tau = -tau;

                // write the reflector into the lower left column of the matrix
                double nu = u[k+1] + tau;
                u[k+1] = 1.0;

                for( int i = k+2; i < N; i++ ) {
                    QH.unsafe_set(i , k ,  u[i] /= nu );
                }

                double gamma = nu/tau;
                gammas[k] = gamma;

                // ---------- multiply on the left by Q_k
                QrHelperFunctions.rank1UpdateMultR(QH,u,gamma,k+1,k+1,N,b);

                // ---------- multiply on the right by Q_k
                QrHelperFunctions.rank1UpdateMultL(QH,u,gamma,0,k+1,N,b);

                // since the first element in the householder vector is known to be 1
                // store the full upper hessenberg
                QH.unsafe_set((k+1),k, -tau*max );

            } else {
                gammas[k] = 0;
            }

        }

        return true;
    }

    public double[] getGammas() {
        return gammas;
    }
}
