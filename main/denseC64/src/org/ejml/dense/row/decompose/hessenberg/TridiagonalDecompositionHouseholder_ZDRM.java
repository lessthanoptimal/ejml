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

package org.ejml.dense.row.decompose.hessenberg;

import org.ejml.data.Complex_F64;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.decompose.UtilDecompositons_ZDRM;
import org.ejml.dense.row.decompose.qr.QrHelperFunctions_ZDRM;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition_F64;

import java.util.Arrays;

/**
 * <p>
 * Performs a complex {@link TridiagonalSimilarDecomposition_F64 similar tridiagonal decomposition} on a
 * square Hermitian matrix.  Householder vectors perform the similar operation and the symmetry
 * is taken advantage of for good performance.
 * </p>
 * <p>
 * Finds the decomposition of a matrix in the form of:<br>
 * <br>
 * A = O*T*O<sup>H</sup><br>
 * <br>
 * where A is a Hermitian m by m matrix, O is an orthogonal matrix, and T is a tridiagonal matrix.
 * </p>
 * <p>
 * This implementation is inspired by description of the real symmetric decomposition in:<br>
 * <br>
 * David S. Watkins, "Fundamentals of Matrix Computations," Second Edition.  Page 349-355
 * </p>
 *
 * @author Peter Abeles
 */
public class TridiagonalDecompositionHouseholder_ZDRM
        implements TridiagonalSimilarDecomposition_F64<ZMatrixRMaj> {

    /**
     * Only the upper right triangle is used.  The Tridiagonal portion stores
     * the tridiagonal matrix.  The rows store householder vectors.
     */
    private ZMatrixRMaj QT;

    // The size of the matrix
    private int N;

    // temporary storage
    private double w[];
    // gammas for the householder operations
    private double gammas[];
    // temporary storage
    private double b[];

    private Complex_F64 tau = new Complex_F64();

    public TridiagonalDecompositionHouseholder_ZDRM() {
        N = 1;
        w = new double[N*2];
        b = new double[N*2];
        gammas = new double[N];
    }

    /**
     * Returns the internal matrix where the decomposed results are stored.
     * @return
     */
    public ZMatrixRMaj getQT() {
        return QT;
    }

    @Override
    public void getDiagonal(double[] diag, double[] off) {
        for( int i = 0; i < N; i++ ) {
            diag[i*2]   = QT.data[(i*N+i)*2];
            diag[i*2+1] = QT.data[(i*N+i)*2+1];

            if( i+1 < N ) {
                off[i*2]   = QT.data[(i*N+i+1)*2];
                off[i*2+1] = QT.data[(i*N+i+1)*2+1];
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
    public ZMatrixRMaj getT(ZMatrixRMaj T ) {
        T = UtilDecompositons_ZDRM.checkZeros(T,N,N);

        T.data[0] = QT.data[0];
        T.data[1] = QT.data[1];

        for( int i = 1; i < N; i++ ) {
            T.set(i,i, QT.getReal(i,i), QT.getImag(i,i));
            double real = QT.getReal(i-1,i);
            double imag = QT.getImag(i-1,i);
            T.set(i-1,i,real,imag);
            T.set(i,i-1,real,-imag);
        }

        return T;
    }

    /**
     * An orthogonal matrix that has the following property: T = Q<sup>H</sup>AQ
     *
     * @param Q If not null then the results will be stored here.  Otherwise a new matrix will be created.
     * @return The extracted Q matrix.
     */
    @Override
    public ZMatrixRMaj getQ(ZMatrixRMaj Q , boolean transposed ) {
        Q = UtilDecompositons_ZDRM.checkIdentity(Q,N,N);

        Arrays.fill(w,0,N*2,0);

        if( transposed ) {
            for( int j = N-2; j >= 0; j-- ) {
                QrHelperFunctions_ZDRM.extractHouseholderRow(QT,j,j+1,N,w,0);
                QrHelperFunctions_ZDRM.rank1UpdateMultL(Q, w, 0, gammas[j], j+1, j+1 , N);
            }
        } else {
            for( int j = N-2; j >= 0; j-- ) {
                QrHelperFunctions_ZDRM.extractHouseholderRow(QT,j,j+1,N,w,0);
                QrHelperFunctions_ZDRM.rank1UpdateMultR(Q, w, 0, gammas[j], j+1, j+1 , N, b);
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
    public boolean decompose( ZMatrixRMaj A ) {
        init(A);

        for( int k = 0; k < N-1; k++ ) {
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
        double max = QrHelperFunctions_ZDRM.computeRowMax(QT,k,k+1,N);

        if( max > 0 ) {
            double gamma = QrHelperFunctions_ZDRM.computeTauGammaAndDivide(k*N+k+1, k*N+N, t, max, tau);
            gammas[k] = gamma;

            // divide u by u_0
            double real_u_0 = t[(k*N+k+1)*2]   + tau.real;
            double imag_u_0 = t[(k*N+k+1)*2+1] + tau.imaginary;
            QrHelperFunctions_ZDRM.divideElements(k+2, N, t, k*N, real_u_0,imag_u_0 );

            // A column is zeroed first.  However a row is being used to store because it reduces
            // cache misses.  Need to compute the conjugate to have the correct householder operation
            for (int i = k+2; i < N; i++) {
                t[(k*N+i)*2+1] = -t[(k*N+i)*2+1];
            }

            t[(k*N+k+1)*2]   = 1.0;
            t[(k*N+k+1)*2+1] = 0;

            // ---------- Specialized householder that takes advantage of the symmetry
//            QrHelperFunctions_ZDRM.rank1UpdateMultR(QT,QT.data,k*N,gamma,k+1,k+1,N,w);
//            QrHelperFunctions_ZDRM.rank1UpdateMultL(QT,QT.data,k*N,gamma,k+1,k+1,N);

            householderSymmetric(k,gamma);

            // since the first element in the householder vector is known to be 1
            // store the full upper hessenberg
            t[(k*N+k+1)*2]   = -tau.real*max;
            t[(k*N+k+1)*2+1] = -tau.imaginary*max;

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
        int startU = row*N;

        // compute v = -gamma*A*u
        for( int i = row+1; i < N; i++ ) {
            double totalReal = 0;
            double totalImag = 0;

            // the lower triangle is not written to so it needs to traverse upwards
            // to get the information.  Reduces the number of matrix writes need
            // improving large matrix performance
            for( int j = row+1; j < i; j++ ) {
                double realA = QT.data[(j*N+i)*2];
                double imagA = -QT.data[(j*N+i)*2+1];

                double realU = QT.data[(startU+j)*2];
                double imagU = QT.data[(startU+j)*2+1];

                totalReal += realA*realU - imagA*imagU;
                totalImag += realA*imagU + imagA*realU;
            }
            for( int j = i; j < N; j++ ) {
                double realA = QT.data[(i*N+j)*2];
                double imagA = QT.data[(i*N+j)*2+1];

                double realU = QT.data[(startU+j)*2];
                double imagU = QT.data[(startU+j)*2+1];

                totalReal += realA*realU - imagA*imagU;
                totalImag += realA*imagU + imagA*realU;
            }
            w[i*2]   = -gamma*totalReal;
            w[i*2+1] = -gamma*totalImag;
        }

        // alpha = -0.5*gamma*u^T*v
        double realAplha = 0;
        double imageAlpha = 0;

        for( int i = row+1; i < N; i++ ) {
            double realU = QT.data[(startU+i)*2];
            double imagU = -QT.data[(startU+i)*2+1];

            double realV = w[i*2];
            double imagV = w[i*2+1];

            realAplha += realU*realV - imagU*imagV;
            imageAlpha += realU*imagV + imagU*realV;
        }
        realAplha *= -0.5*gamma;
        imageAlpha *= -0.5*gamma;

        // w = v + alpha*u
        for( int i = row+1; i < N; i++ ) {
            double realU = QT.data[(startU+i)*2];
            double imagU = QT.data[(startU+i)*2+1];

            w[i*2]   += realAplha*realU - imageAlpha*imagU;
            w[i*2+1] += realAplha*imagU + imageAlpha*realU;
        }

        // A = A + w*u^T + u*w^T
        for( int i = row+1; i < N; i++ ) {

            double realWW = w[i*2];
            double imagWW = w[i*2+1];

            double realUU = QT.data[(startU+i)*2];
            double imagUU = QT.data[(startU+i)*2+1];

            int indA = (i*N+i)*2;
            for( int j = i; j < N; j++ ) {
                // only write to the upper portion of the matrix
                // this reduces the number of cache misses

                double realU = QT.data[(startU+j)*2];
                double imagU = -QT.data[(startU+j)*2+1];

                double realW = w[j*2];
                double imagW = -w[j*2+1];

                QT.data[indA++] += realWW*realU - imagWW*imagU + realW*realUU - imagW*imagUU;
                QT.data[indA++] += realWW*imagU + imagWW*realU + realW*imagUU + imagW*realUU;
            }
        }
    }


    /**
     * If needed declares and sets up internal data structures.
     *
     * @param A Matrix being decomposed.
     */
    public void init( ZMatrixRMaj A ) {
        if( A.numRows != A.numCols)
            throw new IllegalArgumentException("Must be square");

        if( A.numCols != N ) {
            N = A.numCols;

            if( w.length < N ) {
                w = new double[ N*2 ];
                gammas = new double[N*2];
                b = new double[N*2];
            }
        }

        QT = A;
    }

    @Override
    public boolean inputModified() {
        return true;
    }

    public double[] getGammas() {
        return gammas;
    }
}
