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
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition;
import org.ejml.ops.CommonOps;

/**
 * <p>
 * Performs a {@link org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition similar tridiagonal decomposition} on a square symmetric input matrix.
 * Householder vectors perform the similar operation and the symmetry is taken advantage
 * of for good performance.
 * </p>
 * <p>
 * Finds the decomposition of a matrix in the form of:<br>
 * <br>
 * A = O*T*O<sup>T</sup><br>
 * <br>
 * where A is a symmetric m by m matrix, O is an orthogonal matrix, and T is a tridiagonal matrix.
 * </p>
 * <p>
 * This implementation is based off of the algorithm described in:<br>
 * <br>
 * David S. Watkins, "Fundamentals of Matrix Computations," Second Edition.  Page 349-355
 * </p>
 *
 * @author Peter Abeles
 */
public class TridiagonalDecompositionHouseholder_D64
        implements TridiagonalSimilarDecomposition<DenseMatrix64F> {

    /**
     * Only the upper right triangle is used.  The Tridiagonal portion stores
     * the tridiagonal matrix.  The rows store householder vectors.
     */
    private DenseMatrix64F QT;

    // The size of the matrix
    private int N;

    // temporary storage
    private double w[];
    // gammas for the householder operations
    private double gammas[];
    // temporary storage
    private double b[];

    public TridiagonalDecompositionHouseholder_D64() {
        N = 1;
        w = new double[N];
        b = new double[N];
        gammas = new double[N];
    }

    /**
     * Returns the internal matrix where the decomposed results are stored.
     * @return
     */
    public DenseMatrix64F getQT() {
        return QT;
    }

    @Override
    public void getDiagonal(double[] diag, double[] off) {
        for( int i = 0; i < N; i++ ) {
            diag[i] = QT.data[i*N+i];

            if( i+1 < N ) {
                off[i] = QT.data[i*N+i+1];
            }
        }
    }

    /**
     * Extracts the tridiagonal matrix found in the decomposition.
     *
     * @param T If not null then the results will be stored here.  Otherwise a new matrix will be created.
     * @return The extracted T matrix.
     */
    @Override
    public DenseMatrix64F getT( DenseMatrix64F T ) {
        if( T == null ) {
            T = new DenseMatrix64F(N,N);
        } else if( N != T.numRows || N != T.numCols )
            throw new IllegalArgumentException("The provided H must have the same dimensions as the decomposed matrix.");
        else
            T.zero();


        T.data[0] = QT.data[0];

        for( int i = 1; i < N; i++ ) {
            T.set(i,i, QT.get(i,i));
            double a = QT.get(i-1,i);
            T.set(i-1,i,a);
            T.set(i,i-1,a);
        }

        if( N > 1 ) {
            T.data[(N-1)*N+N-1] = QT.data[(N-1)*N+N-1];
            T.data[(N-1)*N+N-2] = QT.data[(N-2)*N+N-1];
        }
            
        return T;
    }

    /**
     * An orthogonal matrix that has the following property: T = Q<sup>T</sup>AQ
     *
     * @param Q If not null then the results will be stored here.  Otherwise a new matrix will be created.
     * @return The extracted Q matrix.
     */
    @Override
    public DenseMatrix64F getQ( DenseMatrix64F Q , boolean transposed ) {
        if( Q == null ) {
            Q = CommonOps.identity(N);
        } else if( N != Q.numRows || N != Q.numCols )
            throw new IllegalArgumentException("The provided H must have the same dimensions as the decomposed matrix.");
        else
            CommonOps.setIdentity(Q);

        for( int i = 0; i < N; i++ ) w[i] = 0;

        if( transposed ) {
            for( int j = N-2; j >= 0; j-- ) {
                w[j+1] = 1;
                for( int i = j+2; i < N; i++ ) {
                    w[i] = QT.data[j*N+i];
                }
                QrHelperFunctions_D64.rank1UpdateMultL(Q, w, gammas[j + 1], j + 1, j + 1, N);
            }
        } else {
            for( int j = N-2; j >= 0; j-- ) {
                w[j+1] = 1;
                for( int i = j+2; i < N; i++ ) {
                    w[i] = QT.get(j,i);
                }
                QrHelperFunctions_D64.rank1UpdateMultR(Q, w, gammas[j + 1], j + 1, j + 1, N, b);
            }
        }

        return Q;
    }

    /**
     * Decomposes the provided symmetric matrix.
     *
     * @param A Symmetric matrix that is going to be decomposed.  Not modified.
     */
    @Override
    public boolean decompose( DenseMatrix64F A ) {
        init(A);

        for( int k = 1; k < N; k++ ) {
            similarTransform(k);
        }

        return true;
    }

    /**
     * Computes and performs the similar a transform for submatrix k.
     */
    private void similarTransform( int k) {
        double t[] = QT.data;

        // find the largest value in this column
        // this is used to normalize the column and mitigate overflow/underflow
        double max = 0;

        int rowU = (k-1)*N;

        for( int i = k; i < N; i++ ) {
            double val = Math.abs(t[rowU+i]);
            if( val > max )
                max = val;
        }

        if( max > 0 ) {
            // -------- set up the reflector Q_k

            double tau = QrHelperFunctions_D64.computeTauAndDivide(k, N, t, rowU, max);

            // write the reflector into the lower left column of the matrix
            double nu = t[rowU+k] + tau;
            QrHelperFunctions_D64.divideElements(k + 1, N, t, rowU, nu);
            t[rowU+k] = 1.0;

            double gamma = nu/tau;
            gammas[k] = gamma;

            // ---------- Specialized householder that takes advantage of the symmetry
            householderSymmetric(k,gamma);

            // since the first element in the householder vector is known to be 1
            // store the full upper hessenberg
            t[rowU+k] = -tau*max;
        } else {
            gammas[k] = 0;
        }
    }

    /**
     * Performs the householder operations on left and right and side of the matrix.  Q<sup>T</sup>AQ
     * @param row Specifies the submatrix.
     *
     * @param gamma The gamma for the householder operation
     */
    public void householderSymmetric( int row , double gamma )
    {
        int startU = (row-1)*N;

        // compute v = -gamma*A*u
        for( int i = row; i < N; i++ ) {
            double total = 0;
            // the lower triangle is not written to so it needs to traverse upwards
            // to get the information.  Reduces the number of matrix writes need
            // improving large matrix performance
            for( int j = row; j < i; j++ ) {
                total += QT.data[j*N+i]*QT.data[startU+j];
            }
            for( int j = i; j < N; j++ ) {
                total += QT.data[i*N+j]*QT.data[startU+j];
            }
            w[i] = -gamma*total;
        }
        // alpha = -0.5*gamma*u^T*v
        double alpha = 0;

        for( int i = row; i < N; i++ ) {
            alpha += QT.data[startU+i]*w[i];
        }
        alpha *= -0.5*gamma;

        // w = v + alpha*u
        for( int i = row; i < N; i++ ) {
            w[i] += alpha*QT.data[startU+i];
        }
        // A = A + w*u^T + u*w^T
        for( int i = row; i < N; i++ ) {

            double ww = w[i];
            double uu = QT.data[startU+i];

            int rowA = i*N;
            for( int j = i; j < N; j++ ) {
                // only write to the upper portion of the matrix
                // this reduces the number of cache misses
                QT.data[rowA+j] += ww*QT.data[startU+j] + w[j]*uu;
            }
        }

    }


    /**
     * If needed declares and sets up internal data structures.
     *
     * @param A Matrix being decomposed.
     */
    public void init( DenseMatrix64F A ) {
        if( A.numRows != A.numCols)
            throw new IllegalArgumentException("Must be square");

        if( A.numCols != N ) {
            N = A.numCols;

            if( w.length < N ) {
                w = new double[ N ];
                gammas = new double[N];
                b = new double[N];
            }
        }

        QT = A;
    }

    @Override
    public boolean inputModified() {
        return true;
    }
}
