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
import org.ejml.interfaces.decomposition.DecompositionInterface;

import java.util.Arrays;

/**
 * <p>
 * Complex Hessenberg decomposition.  It find matrices O and P such that:<br>
 * <br>
 * A = OPO<sup>H</sup><br>
 * <br>
 * where A is an m by m matrix, O is an orthogonal matrix, and P is an upper Hessenberg matrix.
 * </p>
 *
 * <p>
 * A matrix is upper Hessenberg if a<sup>ij</sup> = 0 for all i &gt; j+1. For example, the following matrix
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
public class HessenbergSimilarDecomposition_CDRM
        implements DecompositionInterface<CMatrixRMaj> {
    // A combined matrix that stores te upper Hessenberg matrix and the orthogonal matrix.
    private CMatrixRMaj QH;
    // number of rows and columns of the matrix being decompose
    private int N;

    // the first element in the orthogonal vectors
    private float gammas[];
    // temporary storage
    private float b[];
    private float u[];
    private Complex_F32 tau = new Complex_F32();
    /**
     * Creates a decomposition that won't need to allocate new memory if it is passed matrices up to
     * the specified size.
     *
     * @param initialSize Expected size of the matrices it will decompose.
     */
    public HessenbergSimilarDecomposition_CDRM(int initialSize) {
        gammas = new float[ initialSize ];
        b = new float[ initialSize*2 ];
        u = new float[ initialSize*2 ];
    }

    public HessenbergSimilarDecomposition_CDRM() {
        this(5);
    }

    /**
     * Computes the decomposition of the provided matrix.  If no errors are detected then true is returned,
     * false otherwise.
     * @param A  The matrix that is being decomposed.  Not modified.
     * @return If it detects any errors or not.
     */
    @Override
    public boolean decompose( CMatrixRMaj A )
    {
        if( A.numRows != A.numCols )
            throw new IllegalArgumentException("A must be square.");
        if( A.numRows <= 0 )
            return false;

        QH = A;

        N = A.numCols;

        if( b.length < N*2 ) {
            b = new float[ N*2 ];
            gammas = new float[ N ];
            u = new float[ N*2 ];
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
    public CMatrixRMaj getQH() {
        return QH;
    }

    /**
     * An upper Hessenberg matrix from the decomposition.
     *
     * @param H If not null then the results will be stored here.  Otherwise a new matrix will be created.
     * @return The extracted H matrix.
     */
    public CMatrixRMaj getH(CMatrixRMaj H ) {
        H = UtilDecompositons_CDRM.checkZeros(H,N,N);

        // copy the first row
        System.arraycopy(QH.data, 0, H.data, 0, N*2);

        for( int i = 1; i < N; i++ ) {
            System.arraycopy(QH.data, (i*N+i-1)*2, H.data, (i*N+i-1)*2, (N-i+1)*2);
        }

        return H;
    }

    /**
     * An orthogonal matrix that has the following property: H = Q<sup>T</sup>AQ
     *
     * @param Q If not null then the results will be stored here.  Otherwise a new matrix will be created.
     * @return The extracted Q matrix.
     */
    public CMatrixRMaj getQ(CMatrixRMaj Q ) {
        Q = UtilDecompositons_CDRM.checkIdentity(Q,N,N);

        Arrays.fill(u,0,N*2,0);
        for( int j = N-2; j >= 0; j-- ) {
            QrHelperFunctions_CDRM.extractHouseholderColumn(QH,j+1,N,j,u,0);
            QrHelperFunctions_CDRM.rank1UpdateMultR(Q, u, 0,gammas[j], j + 1, j + 1, N, b);
        }

        return Q;
    }

    /**
     * Internal function for computing the decomposition.
     */
    private boolean _decompose() {
        float h[] = QH.data;

        for( int k = 0; k < N-2; k++ ) { // k = column
            u[k*2] = 0;
            u[k*2+1] = 0;
            float max = QrHelperFunctions_CDRM.extractColumnAndMax(QH,k+1,N,k,u,0);

            if( max > 0 ) {
                // -------- set up the reflector Q_k

                float gamma = QrHelperFunctions_CDRM.computeTauGammaAndDivide(k+1,N,u,max,tau);
                gammas[k] = gamma;

                // divide u by u_0
                float real_u_0 = u[(k+1)*2]   + tau.real;
                float imag_u_0 = u[(k+1)*2+1] + tau.imaginary;
                QrHelperFunctions_CDRM.divideElements(k + 2, N, u, 0, real_u_0,imag_u_0 );

                // write the reflector into the lower left column of the matrix
                for (int i = k+2; i < N; i++) {
                    h[(i*N+k)*2]   = u[i*2];
                    h[(i*N+k)*2+1] = u[i*2+1];
                }

                u[(k+1)*2]   = 1;
                u[(k+1)*2+1] = 0;

                // ---------- multiply on the left by Q_k
                QrHelperFunctions_CDRM.rank1UpdateMultR(QH, u,0, gamma, k + 1, k + 1, N, b);

                // ---------- multiply on the right by Q_k
                QrHelperFunctions_CDRM.rank1UpdateMultL(QH, u,0, gamma, 0, k + 1, N);

                // since the first element in the householder vector is known to be 1
                // store the full upper hessenberg
                h[((k+1)*N+k)*2]   = -tau.real*max;
                h[((k+1)*N+k)*2+1] = -tau.imaginary*max;

            } else {
                gammas[k] = 0;
            }

        }

        return true;
    }

    public float[] getGammas() {
        return gammas;
    }
}
