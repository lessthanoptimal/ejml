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
import org.ejml.ops.CommonOps;

/**
 * <p>
 * A straight forward implementation from "Fundamentals of Matrix Computations," Second Edition.<br>
 * <br>
 * This is only saved to provide a point of reference in benchmarks.
 * </p>
 *
 * @author Peter Abeles
 */
public class TridiagonalDecompositionHouseholderOrig_D64 {

    /**
     * Internal storage of decomposed matrix.  The tridiagonal matrix is stored in the
     * upper tridiagonal portion of the matrix.  The householder vectors are stored
     * in the upper rows.
     */
    DenseMatrix64F QT;

    // The size of the matrix
    int N;

    // temporary storage
    double w[];
    // gammas for the householder operations
    double gammas[];
    // temporary storage
    double b[];

    public TridiagonalDecompositionHouseholderOrig_D64() {
        N = 1;
        QT = new DenseMatrix64F(N,N);
        w = new double[N];
        b = new double[N];
        gammas = new double[N];
    }

    /**
     * Returns the interal matrix where the decomposed results are stored.
     * @return
     */
    public DenseMatrix64F getQT() {
        return QT;
    }

    /**
     * Extracts the tridiagonal matrix found in the decomposition.
     *
     * @param T If not null then the results will be stored here.  Otherwise a new matrix will be created.
     * @return The extracted T matrix.
     */
    public DenseMatrix64F getT( DenseMatrix64F T) {
        if( T == null ) {
            T = new DenseMatrix64F(N,N);
        } else if( N != T.numRows || N != T.numCols )
            throw new IllegalArgumentException("The provided H must have the same dimensions as the decomposed matrix.");
        else
            T.zero();


        T.data[0] = QT.data[0];
        T.data[1] = QT.data[1];


        for( int i = 1; i < N-1; i++ ) {
            T.set(i,i, QT.get(i,i));
            T.set(i,i+1,QT.get(i,i+1));
            T.set(i,i-1,QT.get(i-1,i));
        }

        T.data[(N-1)*N+N-1] = QT.data[(N-1)*N+N-1];
        T.data[(N-1)*N+N-2] = QT.data[(N-2)*N+N-1];

        return T;
    }

    /**
     * An orthogonal matrix that has the following property: T = Q<sup>T</sup>AQ
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

        for( int i = 0; i < N; i++ ) w[i] = 0;

        for( int j = N-2; j >= 0; j-- ) {
            w[j+1] = 1;
            for( int i = j+2; i < N; i++ ) {
                w[i] = QT.get(j,i);
            }
            QrHelperFunctions_D64.rank1UpdateMultR(Q, w, gammas[j + 1], j + 1, j + 1, N, b);
//            Q.print();
        }

        return Q;
    }

    /**
     * Decomposes the provided symmetric matrix.
     *
     * @param A Symmetric matrix that is going to be decomposed.  Not modified.
     */
    public void decompose( DenseMatrix64F A ) {
        init(A);

        for( int k = 1; k < N; k++ ) {
            similarTransform(k);
//            System.out.println("k=="+k);
//            QT.print();
        }
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

            double tau = 0;
            // normalize to reduce overflow/underflow
            // and compute tau for the reflector
            for( int i = k; i < N; i++ ) {
                double val = t[rowU+i] /= max;
                tau += val*val;
            }

            tau = Math.sqrt(tau);

            if( t[rowU+k] < 0 )
                tau = -tau;

            // write the reflector into the lower left column of the matrix
            double nu = t[rowU+k] + tau;
            t[rowU+k] = 1.0;

            for( int i = k+1; i < N; i++ ) {
                t[rowU+i] /= nu;
            }

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
            for( int j = row; j < N; j++ ) {
                total += QT.data[i*N+j]*QT.data[startU+j];
            }
            w[i] = -gamma*total;
//            System.out.println("y["+i+"] = "+w[i]);
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
//            System.out.println("w["+i+"] = "+w[i]);
        }
        // A = A + w*u^T + u*w^T
        for( int i = row; i < N; i++ ) {

            double ww = w[i];
            double uu = QT.data[startU+i];
//            System.out.println("u["+i+"] = "+uu);

            for( int j = i; j < N; j++ ) {
                QT.data[j*N+i] = QT.data[i*N+j] += ww*QT.data[startU+j] + w[j]*uu;
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
            QT.reshape(N,N, false);

            if( w.length < N ) {
                w = new double[ N ];
                gammas = new double[N];
                b = new double[N];
            }
        }

        // just copy the top right triangle
        QT.set(A);
    }

    public double getGamma( int index ) {
        return gammas[index];
    }
}