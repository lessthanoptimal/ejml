/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decompose.hessenberg;

import org.ejml.alg.dense.decompose.qr.QrHelperFunctions_CD64;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.ejml.ops.CCommonOps;

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
public class HessenbergSimilarDecomposition_CD64
        implements DecompositionInterface<CDenseMatrix64F> {
    // A combined matrix that stores te upper Hessenberg matrix and the orthogonal matrix.
    private CDenseMatrix64F QH;
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
    public HessenbergSimilarDecomposition_CD64(int initialSize) {
        gammas = new double[ initialSize ];
        b = new double[ initialSize*2 ];
        u = new double[ initialSize*2 ];
    }

    public HessenbergSimilarDecomposition_CD64() {
        this(5);
    }

    /**
     * Computes the decomposition of the provided matrix.  If no errors are detected then true is returned,
     * false otherwise.
     * @param A  The matrix that is being decomposed.  Not modified.
     * @return If it detects any errors or not.
     */
    @Override
    public boolean decompose( CDenseMatrix64F A )
    {
        if( A.numRows != A.numCols )
            throw new IllegalArgumentException("A must be square.");
        if( A.numRows <= 0 )
            return false;

        QH = A;

        N = A.numCols;

        if( b.length < N*2 ) {
            b = new double[ N*2 ];
            gammas = new double[ N ];
            u = new double[ N*2 ];
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
    public CDenseMatrix64F getQH() {
        return QH;
    }

    /**
     * An upper Hessenberg matrix from the decomposition.
     *
     * @param H If not null then the results will be stored here.  Otherwise a new matrix will be created.
     * @return The extracted H matrix.
     */
    public CDenseMatrix64F getH( CDenseMatrix64F H ) {
        if( H == null ) {
            H = new CDenseMatrix64F(N,N);
        } else if( N != H.numRows || N != H.numCols )
            throw new IllegalArgumentException("The provided H must have the same dimensions as the decomposed matrix.");
        else
            CCommonOps.fill(H,0,0);

        // copy the first row
        System.arraycopy(QH.data, 0, H.data, 0, N*2);

        for( int i = 1; i < N; i++ ) {
            for( int j = i-1; j < N; j++ ) {
                int indexQH = QH.getIndex(i,j);
                H.set(i,j, QH.data[indexQH],QH.data[indexQH+1]);
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
    public CDenseMatrix64F getQ( CDenseMatrix64F Q ) {
        if( Q == null ) {
            Q = new CDenseMatrix64F(N,N);
            for( int i = 0; i < N; i++ ) {
                Q.data[(i*N+i)*2] = 1;
                Q.data[(i*N+i)*2+1] = 0;
            }
        } else if( N != Q.numRows || N != Q.numCols )
            throw new IllegalArgumentException("The provided H must have the same dimensions as the decomposed matrix.");
        else
            CCommonOps.setIdentity(Q);

        for( int j = N-2; j >= 0; j-- ) {
            u[(j+1)*2] = 1;
            u[(j+1)*2+1] = 0;

            for( int i = j+2; i < N; i++ ) {
                int indexQH = QH.getIndex(i,j);
                u[i*2] = QH.data[indexQH];
                u[i*2+1] = QH.data[indexQH+1];
            }
            QrHelperFunctions_CD64.rank1UpdateMultR(Q, u, j+1,gammas[j], j + 1, j + 1, N, b);
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
                double realVal = u[i*2] = h[i*N*2+k*2];
                double imagVal = u[i*2+1] = h[i*N*2+k*2+1];

                double magVal = realVal*realVal + imagVal*imagVal;
                if( max < magVal ) {
                    max = magVal;
                }
            }
            max = Math.sqrt(max);

            if( max > 0 ) {
                // -------- set up the reflector Q_k

                double nx = 0;
                // normalize to reduce overflow/underflow and compute tau for the reflector
                for( int i = k+1; i < N; i++ ) {
                    double realVal = u[i*2] /= max;
                    double imagVal = u[i*2+1] /= max;

                    nx += realVal*realVal + imagVal*imagVal;
                }

                nx = Math.sqrt(nx);

                // write the reflector into the lower left column of the matrix
                double real_nu = u[(k+1)*2];
                double imag_nu = u[(k+1)*2+1];
                double mag_nu = real_nu*real_nu + imag_nu*imag_nu;

                double realTau,imagTau;
                if( mag_nu == 0 ) {
                    realTau = nx;
                    imagTau = 0;
                } else {
                    realTau = real_nu / mag_nu * nx;
                    imagTau = imag_nu / mag_nu * nx;
                }

                double top,bottom;

                // if there is a chance they can cancel swap the sign
                if ( real_nu*realTau<0) {
                    realTau = -realTau;
                    imagTau = -imagTau;
                    top = nx * nx - nx *mag_nu;
                    bottom = mag_nu*mag_nu - 2.0* nx *mag_nu + nx * nx;
                } else {
                    top = nx * nx + nx *mag_nu;
                    bottom = mag_nu*mag_nu + 2.0* nx *mag_nu + nx * nx;
                }

                double realGamma = bottom/top;
                gammas[k] = realGamma;

                double real_u_0 = real_nu + realTau;
                double imag_u_0 = imag_nu + imagTau;
                double norm_u_0 = real_u_0*real_u_0 + imag_u_0*imag_u_0;

                int indexU = (k+2)*2;
                for( int i = k+2; i < N; i++ ) {
                    double realU = u[indexU];
                    double imagU = u[indexU+1];

                    u[indexU++] = (realU*real_u_0 + imagU*imag_u_0)/norm_u_0;
                    u[indexU++] = (imagU*real_u_0 - realU*imag_u_0)/norm_u_0;
                }
                u[2*(k+1)]   = 1;
                u[2*(k+1)+1] = 0;


                // ---------- multiply on the left by Q_k
                QrHelperFunctions_CD64.rank1UpdateMultR(QH, u,k+1, realGamma, k + 1, k + 1, N, b);

                // ---------- multiply on the right by Q_k
                QrHelperFunctions_CD64.rank1UpdateMultL(QH, u,k+1, realGamma, 0, k + 1, N);

                // since the first element in the householder vector is known to be 1
                // store the full upper hessenberg
                h[((k+1)*N+k)*2]   = -realGamma*max;
                h[((k+1)*N+k)*2+1] = -imagTau*max;

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
