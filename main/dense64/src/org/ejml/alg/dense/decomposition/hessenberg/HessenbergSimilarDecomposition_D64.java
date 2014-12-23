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

package org.ejml.alg.dense.decomposition.hessenberg;

import org.ejml.alg.dense.decomposition.qr.QrHelperFunctions_D64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.decomposition.DecompositionInterface;
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
public class HessenbergSimilarDecomposition_D64
        implements DecompositionInterface<DenseMatrix64F> {
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
    public HessenbergSimilarDecomposition_D64(int initialSize) {
        gammas = new double[ initialSize ];
        b = new double[ initialSize ];
        u = new double[ initialSize ];
    }

    public HessenbergSimilarDecomposition_D64() {
        this(5);
    }

    /**
     * Computes the decomposition of the provided matrix.  If no errors are detected then true is returned,
     * false otherwise.
     * @param A  The matrix that is being decomposed.  Not modified.
     * @return If it detects any errors or not.
     */
    @Override
    public boolean decompose( DenseMatrix64F A )
    {
        if( A.numRows != A.numCols )
            throw new IllegalArgumentException("A must be square.");
        if( A.numRows <= 0 )
            return false;

        QH = A;

        N = A.numCols;

        if( b.length < N ) {
            b = new double[ N ];
            gammas = new double[ N ];
            u = new double[ N ];
        }
        return _decompose();
    }

    @Override
    public boolean inputModified() {
        return true;
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
                Q.data[i*N+i] = 1;
            }
        } else if( N != Q.numRows || N != Q.numCols )
            throw new IllegalArgumentException("The provided H must have the same dimensions as the decomposed matrix.");
        else
            CommonOps.setIdentity(Q);

        for( int j = N-2; j >= 0; j-- ) {
            u[j+1] = 1;
            for( int i = j+2; i < N; i++ ) {
                u[i] = QH.get(i,j);
            }
            QrHelperFunctions_D64.rank1UpdateMultR(Q, u, gammas[j], j + 1, j + 1, N, b);
        }

        return Q;
    }

    /**
     * Internal function for computing the decomposition.
     */
    private boolean _decompose() {
        double h[] = QH.data;

        for( int k = 0; k < N-2; k++ ) {
            // find the largest value in this column
            // this is used to normalize the column and mitigate overflow/underflow
            double max = 0;

            for( int i = k+1; i < N; i++ ) {
                // copy the householder vector to vector outside of the matrix to reduce caching issues
                // big improvement on larger matrices and a relatively small performance hit on small matrices.
                double val = u[i] = h[i*N+k];
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
                    h[i*N+k] = u[i] /= nu;
                }

                double gamma = nu/tau;
                gammas[k] = gamma;

                // ---------- multiply on the left by Q_k
                QrHelperFunctions_D64.rank1UpdateMultR(QH, u, gamma, k + 1, k + 1, N, b);

                // ---------- multiply on the right by Q_k
                QrHelperFunctions_D64.rank1UpdateMultL(QH, u, gamma, 0, k + 1, N);

                // since the first element in the householder vector is known to be 1
                // store the full upper hessenberg
                h[(k+1)*N+k] = -tau*max;

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
