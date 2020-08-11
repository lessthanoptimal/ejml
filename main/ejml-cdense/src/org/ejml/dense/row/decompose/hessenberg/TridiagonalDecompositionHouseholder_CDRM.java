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

import org.ejml.data.Complex_F32;
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.decompose.UtilDecompositons_CDRM;
import org.ejml.dense.row.decompose.qr.QrHelperFunctions_CDRM;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition_F32;

import java.util.Arrays;

/**
 * <p>
 * Performs a complex {@link TridiagonalSimilarDecomposition_F32 similar tridiagonal decomposition} on a
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
public class TridiagonalDecompositionHouseholder_CDRM
        implements TridiagonalSimilarDecomposition_F32<CMatrixRMaj> {

    /**
     * Only the upper right triangle is used.  The Tridiagonal portion stores
     * the tridiagonal matrix.  The rows store householder vectors.
     */
    private CMatrixRMaj QT;

    // The size of the matrix
    private int N;

    // temporary storage
    private float w[];
    // gammas for the householder operations
    private float gammas[];
    // temporary storage
    private float b[];

    private Complex_F32 tau = new Complex_F32();

    public TridiagonalDecompositionHouseholder_CDRM() {
        N = 1;
        w = new float[N*2];
        b = new float[N*2];
        gammas = new float[N];
    }

    /**
     * Returns the internal matrix where the decomposed results are stored.
     * @return
     */
    public CMatrixRMaj getQT() {
        return QT;
    }

    @Override
    public void getDiagonal(float[] diag, float[] off) {
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
    public CMatrixRMaj getT(CMatrixRMaj T ) {
        T = UtilDecompositons_CDRM.checkZeros(T,N,N);

        T.data[0] = QT.data[0];
        T.data[1] = QT.data[1];

        for( int i = 1; i < N; i++ ) {
            T.set(i,i, QT.getReal(i,i), QT.getImag(i,i));
            float real = QT.getReal(i-1,i);
            float imag = QT.getImag(i-1,i);
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
    public CMatrixRMaj getQ(CMatrixRMaj Q , boolean transposed ) {
        Q = UtilDecompositons_CDRM.checkIdentity(Q,N,N);

        Arrays.fill(w,0,N*2,0);

        if( transposed ) {
            for( int j = N-2; j >= 0; j-- ) {
                QrHelperFunctions_CDRM.extractHouseholderRow(QT,j,j+1,N,w,0);
                QrHelperFunctions_CDRM.rank1UpdateMultL(Q, w, 0, gammas[j], j+1, j+1 , N);
            }
        } else {
            for( int j = N-2; j >= 0; j-- ) {
                QrHelperFunctions_CDRM.extractHouseholderRow(QT,j,j+1,N,w,0);
                QrHelperFunctions_CDRM.rank1UpdateMultR(Q, w, 0, gammas[j], j+1, j+1 , N, b);
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
    public boolean decompose( CMatrixRMaj A ) {
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
        float t[] = QT.data;

        // find the largest value in this column
        // this is used to normalize the column and mitigate overflow/underflow
        float max = QrHelperFunctions_CDRM.computeRowMax(QT,k,k+1,N);

        if( max > 0 ) {
            float gamma = QrHelperFunctions_CDRM.computeTauGammaAndDivide(k*N+k+1, k*N+N, t, max, tau);
            gammas[k] = gamma;

            // divide u by u_0
            float real_u_0 = t[(k*N+k+1)*2]   + tau.real;
            float imag_u_0 = t[(k*N+k+1)*2+1] + tau.imaginary;
            QrHelperFunctions_CDRM.divideElements(k+2, N, t, k*N, real_u_0,imag_u_0 );

            // A column is zeroed first.  However a row is being used to store because it reduces
            // cache misses.  Need to compute the conjugate to have the correct householder operation
            for (int i = k+2; i < N; i++) {
                t[(k*N+i)*2+1] = -t[(k*N+i)*2+1];
            }

            t[(k*N+k+1)*2]   = 1.0f;
            t[(k*N+k+1)*2+1] = 0;

            // ---------- Specialized householder that takes advantage of the symmetry
//            QrHelperFunctions_CDRM.rank1UpdateMultR(QT,QT.data,k*N,gamma,k+1,k+1,N,w);
//            QrHelperFunctions_CDRM.rank1UpdateMultL(QT,QT.data,k*N,gamma,k+1,k+1,N);

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
    public void householderSymmetric( int row , float gamma )
    {
        int startU = row*N;

        // compute v = -gamma*A*u
        for( int i = row+1; i < N; i++ ) {
            float totalReal = 0;
            float totalImag = 0;

            // the lower triangle is not written to so it needs to traverse upwards
            // to get the information.  Reduces the number of matrix writes need
            // improving large matrix performance
            for( int j = row+1; j < i; j++ ) {
                float realA = QT.data[(j*N+i)*2];
                float imagA = -QT.data[(j*N+i)*2+1];

                float realU = QT.data[(startU+j)*2];
                float imagU = QT.data[(startU+j)*2+1];

                totalReal += realA*realU - imagA*imagU;
                totalImag += realA*imagU + imagA*realU;
            }
            for( int j = i; j < N; j++ ) {
                float realA = QT.data[(i*N+j)*2];
                float imagA = QT.data[(i*N+j)*2+1];

                float realU = QT.data[(startU+j)*2];
                float imagU = QT.data[(startU+j)*2+1];

                totalReal += realA*realU - imagA*imagU;
                totalImag += realA*imagU + imagA*realU;
            }
            w[i*2]   = -gamma*totalReal;
            w[i*2+1] = -gamma*totalImag;
        }

        // alpha = -0.5f*gamma*u^T*v
        float realAplha = 0;
        float imageAlpha = 0;

        for( int i = row+1; i < N; i++ ) {
            float realU = QT.data[(startU+i)*2];
            float imagU = -QT.data[(startU+i)*2+1];

            float realV = w[i*2];
            float imagV = w[i*2+1];

            realAplha += realU*realV - imagU*imagV;
            imageAlpha += realU*imagV + imagU*realV;
        }
        realAplha *= -0.5f*gamma;
        imageAlpha *= -0.5f*gamma;

        // w = v + alpha*u
        for( int i = row+1; i < N; i++ ) {
            float realU = QT.data[(startU+i)*2];
            float imagU = QT.data[(startU+i)*2+1];

            w[i*2]   += realAplha*realU - imageAlpha*imagU;
            w[i*2+1] += realAplha*imagU + imageAlpha*realU;
        }

        // A = A + w*u^T + u*w^T
        for( int i = row+1; i < N; i++ ) {

            float realWW = w[i*2];
            float imagWW = w[i*2+1];

            float realUU = QT.data[(startU+i)*2];
            float imagUU = QT.data[(startU+i)*2+1];

            int indA = (i*N+i)*2;
            for( int j = i; j < N; j++ ) {
                // only write to the upper portion of the matrix
                // this reduces the number of cache misses

                float realU = QT.data[(startU+j)*2];
                float imagU = -QT.data[(startU+j)*2+1];

                float realW = w[j*2];
                float imagW = -w[j*2+1];

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
    public void init( CMatrixRMaj A ) {
        if( A.numRows != A.numCols)
            throw new IllegalArgumentException("Must be square");

        if( A.numCols != N ) {
            N = A.numCols;

            if( w.length < N ) {
                w = new float[ N*2 ];
                gammas = new float[N*2];
                b = new float[N*2];
            }
        }

        QT = A;
    }

    @Override
    public boolean inputModified() {
        return true;
    }

    public float[] getGammas() {
        return gammas;
    }
}
