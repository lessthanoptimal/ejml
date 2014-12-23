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

package org.ejml.alg.dense.decomposition.bidiagonal;

import org.ejml.alg.dense.decomposition.qr.QrHelperFunctions_D64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.decomposition.BidiagonalDecomposition;
import org.ejml.ops.CommonOps;

/**
 * <p>
 * Performs a {@link org.ejml.interfaces.decomposition.BidiagonalDecomposition} using
 * householder reflectors.  This is efficient on wide or square matrices.
 * </p>
 *
 * @author Peter Abeles
 */
public class BidiagonalDecompositionRow_D64
        implements BidiagonalDecomposition<DenseMatrix64F>
{
    // A combined matrix that stores te upper Hessenberg matrix and the orthogonal matrix.
    private DenseMatrix64F UBV;

    // number of rows
    private int m;
    // number of columns
    private int n;
    // the smaller of m or n
    private int min;

    // the first element in the orthogonal vectors
    private double gammasU[];
    private double gammasV[];
    // temporary storage
    private double b[];
    private double u[];

    /**
     * Creates a decompose that defines the specified amount of memory.
     *
     * @param numElements number of elements in the matrix.
     */
    public BidiagonalDecompositionRow_D64(int numElements) {

        UBV = new DenseMatrix64F(numElements);
        gammasU = new double[ numElements ];
        gammasV = new double[ numElements ];
        b = new double[ numElements ];
        u = new double[ numElements ];
    }

    public BidiagonalDecompositionRow_D64() {
        this(1);
    }

    /**
     * Computes the decomposition of the provided matrix.  If no errors are detected then true is returned,
     * false otherwise.
     *
     * @param A  The matrix that is being decomposed.  Not modified.
     * @return If it detects any errors or not.
     */
    @Override
    public boolean decompose( DenseMatrix64F A  )
    {
        init(A);
        return _decompose();
    }

    /**
     * Sets up internal data structures and creates a copy of the input matrix.
     *
     * @param A The input matrix.  Not modified.
     */
    protected void init(DenseMatrix64F A ) {
        UBV = A;

        m = UBV.numRows;
        n = UBV.numCols;

        min = Math.min(m,n);
        int max = Math.max(m,n);

        if( b.length < max+1 ) {
            b = new double[ max+1 ];
            u = new double[ max+1 ];
        }
        if( gammasU.length < m ) {
            gammasU = new double[ m ];
        }
        if( gammasV.length < n ) {
            gammasV = new double[ n ];
        }
    }

    /**
     * The raw UBV matrix that is stored internally.
     *
     * @return UBV matrix.
     */
    public DenseMatrix64F getUBV() {
        return UBV;
    }

    @Override
    public void getDiagonal(double[] diag, double[] off) {
        diag[0] = UBV.get(0);
        for( int i = 1; i < n; i++ ) {
            diag[i] = UBV.unsafe_get(i,i);
            off[i-1] = UBV.unsafe_get(i-1,i);
        }
    }

    /**
     * Returns the bidiagonal matrix.
     *
     * @param B If not null the results are stored here, if null a new matrix is created.
     * @return The bidiagonal matrix.
     */
    @Override
    public DenseMatrix64F getB( DenseMatrix64F B , boolean compact ) {
        B = handleB(B, compact,m,n,min);

        //System.arraycopy(UBV.data, 0, B.data, 0, UBV.getNumElements());

        B.set(0,0,UBV.get(0,0));
        for( int i = 1; i < min; i++ ) {
            B.set(i,i, UBV.get(i,i));
            B.set(i-1,i, UBV.get(i-1,i));
        }
        if( n > m )
            B.set(min-1,min,UBV.get(min-1,min));

        return B;
    }

    public static DenseMatrix64F handleB(DenseMatrix64F B, boolean compact,
                                          int m , int n , int min ) {
        int w = n > m ? min + 1 : min;

        if( compact ) {
            if( B == null ) {
                B = new DenseMatrix64F(min,w);
            } else {
                B.reshape(min,w, false);
                B.zero();
            }
        } else {
            if( B == null ) {
                B = new DenseMatrix64F(m,n);
            } else {
                B.reshape(m,n, false);
                B.zero();
            }
        }
        return B;
    }

    /**
     * Returns the orthogonal U matrix.
     *
     * @param U If not null then the results will be stored here.  Otherwise a new matrix will be created.
     * @return The extracted Q matrix.
     */
    @Override
    public DenseMatrix64F getU( DenseMatrix64F U , boolean transpose , boolean compact ) {
        U = handleU(U, transpose, compact,m,n,min);
        CommonOps.setIdentity(U);

        for( int i = 0; i < m; i++ ) u[i] = 0;

        for( int j = min-1; j >= 0; j-- ) {
            u[j] = 1;
            for( int i = j+1; i < m; i++ ) {
                u[i] = UBV.get(i,j);
            }
            if( transpose )
                QrHelperFunctions_D64.rank1UpdateMultL(U, u, gammasU[j], j, j, m);
            else
                QrHelperFunctions_D64.rank1UpdateMultR(U, u, gammasU[j], j, j, m, this.b);
        }

        return U;
    }

    public static DenseMatrix64F handleU(DenseMatrix64F U,
                                         boolean transpose, boolean compact,
                                         int m, int n , int min ) {
        if( compact ){
            if( transpose ) {
                if( U == null )
                    U = new DenseMatrix64F(min,m);
                else {
                    U.reshape(min,m, false);
                }
            } else {
                if( U == null )
                    U = new DenseMatrix64F(m,min);
                else
                    U.reshape(m,min, false);
            }
        } else  {
            if( U == null )
                U = new DenseMatrix64F(m,m);
            else
                U.reshape(m,m, false);
        }

        return U;
    }

    /**
     * Returns the orthogonal V matrix.
     *
     * @param V If not null then the results will be stored here.  Otherwise a new matrix will be created.
     * @return The extracted Q matrix.
     */
    @Override
    public DenseMatrix64F getV( DenseMatrix64F V , boolean transpose , boolean compact ) {
        V = handleV(V, transpose, compact,m,n,min);
        CommonOps.setIdentity(V);

//        UBV.print();

        // todo the very first multiplication can be avoided by setting to the rank1update output
        for( int j = min-1; j >= 0; j-- ) {
            u[j+1] = 1;
            for( int i = j+2; i < n; i++ ) {
                u[i] = UBV.get(j,i);
            }
            if( transpose )
                QrHelperFunctions_D64.rank1UpdateMultL(V, u, gammasV[j], j + 1, j + 1, n);
            else
                QrHelperFunctions_D64.rank1UpdateMultR(V, u, gammasV[j], j + 1, j + 1, n, this.b);
        }

        return V;
    }

    public static DenseMatrix64F handleV(DenseMatrix64F V, boolean transpose, boolean compact,
                                   int m , int n , int min ) {
        int w = n > m ? min + 1 : min;

        if( compact ) {
            if( transpose ) {
                if( V == null ) {
                    V = new DenseMatrix64F(w,n);
                } else
                    V.reshape(w,n, false);
            } else {
                if( V == null ) {
                    V = new DenseMatrix64F(n,w);
                } else
                    V.reshape(n,w, false);
            }
        } else {
            if( V == null ) {
                V = new DenseMatrix64F(n,n);
            } else
                V.reshape(n,n, false);
        }

        return V;
    }

    /**
     * Internal function for computing the decomposition.
     */
    private boolean _decompose() {
        for( int k = 0; k < min; k++ ) {
//            UBV.print();
            computeU(k);
//            System.out.println("--- after U");
//            UBV.print();
            computeV(k);
//            System.out.println("--- after V");
//            UBV.print();
        }

        return true;
    }

    protected void computeU( int k) {
        double b[] = UBV.data;

        // find the largest value in this column
        // this is used to normalize the column and mitigate overflow/underflow
        double max = 0;

        for( int i = k; i < m; i++ ) {
            // copy the householder vector to vector outside of the matrix to reduce caching issues
            // big improvement on larger matrices and a relatively small performance hit on small matrices.
            double val = u[i] = b[i*n+k];
            val = Math.abs(val);
            if( val > max )
                max = val;
        }

        if( max > 0 ) {
            // -------- set up the reflector Q_k
            double tau = QrHelperFunctions_D64.computeTauAndDivide(k, m, u, max);

            // write the reflector into the lower left column of the matrix
            // while dividing u by nu
            double nu = u[k] + tau;
            QrHelperFunctions_D64.divideElements_Bcol(k + 1, m, n, u, b, k, nu);
            u[k] = 1.0;

            double gamma = nu/tau;
            gammasU[k] = gamma;

            // ---------- multiply on the left by Q_k
            QrHelperFunctions_D64.rank1UpdateMultR(UBV, u, gamma, k + 1, k, m, this.b);

            b[k*n+k] = -tau*max;
        } else {
            gammasU[k] = 0;
        }
    }

    protected void computeV(int k) {
        double b[] = UBV.data;

        int row = k*n;

        // find the largest value in this column
        // this is used to normalize the column and mitigate overflow/underflow
        double max = QrHelperFunctions_D64.findMax(b, row + k + 1, n - k - 1);

        if( max > 0 ) {
            // -------- set up the reflector Q_k

            double tau = QrHelperFunctions_D64.computeTauAndDivide(k + 1, n, b, row, max);

            // write the reflector into the lower left column of the matrix
            double nu = b[row+k+1] + tau;
            QrHelperFunctions_D64.divideElements_Brow(k + 2, n, u, b, row, nu);

            u[k+1] = 1.0;

            double gamma = nu/tau;
            gammasV[k] = gamma;

            // writing to u could be avoided by working directly with b.
            // requires writing a custom rank1Update function
            // ---------- multiply on the left by Q_k
            QrHelperFunctions_D64.rank1UpdateMultL(UBV, u, gamma, k + 1, k + 1, n);

            b[row+k+1] = -tau*max;
        } else {
            gammasV[k] = 0;
        }
    }

    /**
     * Returns gammas from the householder operations for the U matrix.
     *
     * @return gammas for householder operations
     */
    public double[] getGammasU() {
        return gammasU;
    }

    /**
     * Returns gammas from the householder operations for the V matrix.
     *
     * @return gammas for householder operations
     */
    public double[] getGammasV() {
        return gammasV;
    }

    @Override
    public boolean inputModified() {
        return true;
    }
}