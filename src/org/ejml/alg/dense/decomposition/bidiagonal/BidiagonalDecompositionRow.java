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

package org.ejml.alg.dense.decomposition.bidiagonal;

import org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholder;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * <p>
 * Internally performs a {@link BidiagonalDecomposition} on a row major matrix.  This is efficient
 * on wide or square matrices.
 * </p>
 *
 * @author Peter Abeles
 */
// TODO create a transpose version
// todo don't store householder and explicity compute U and V upon request
public class BidiagonalDecompositionRow implements BidiagonalDecomposition {
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
    public BidiagonalDecompositionRow( int numElements ) {

        UBV = new DenseMatrix64F(numElements);
        gammasU = new double[ numElements ];
        gammasV = new double[ numElements ];
        b = new double[ numElements ];
        u = new double[ numElements ];
    }

    public BidiagonalDecompositionRow() {
        this(1);
    }

    /**
     * Computes the decomposition of the provided matrix.  If no errors are detected then true is returned,
     * false otherwise.
     * 
     * @param A  The matrix that is being decomposed.  Not modified.
     * @param transpose if true it will decompose the transpose of the matrix instead of the original.
     * @return If it detects any errors or not.
     */
    public boolean decompose( DenseMatrix64F A , boolean transpose )
    {
        init(A,transpose);
        return _decompose();
    }

    /**
     * Sets up internal data structures and creates a copy of the input matrix.
     *
     * @param A The input matrix.  Not modified.
     * @param transpose If the transposed should be copied or not
     */
    protected void init(DenseMatrix64F A, boolean transpose ) {
        if( transpose ) {
            UBV.reshape(A.numCols,A.numRows, false);
            CommonOps.transpose(A,UBV);
        }  else {
            UBV.reshape(A.numRows,A.numCols, false);
            UBV.set(A);
        }

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

    /**
     * Returns the bidiagonal matrix.
     *
     * @param B If not null the results are stored here, if null a new matrix is created.
     * @return The bidiagonal matrix.
     */
    public DenseMatrix64F getB( DenseMatrix64F B , boolean compact ) {
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

    /**
     * Returns the orthogonal U matrix.
     *
     * @param U If not null then the results will be stored here.  Otherwise a new matrix will be created.
     * @return The extracted Q matrix.
     */
    public DenseMatrix64F getU( DenseMatrix64F U , boolean transpose , boolean compact ) {
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

        CommonOps.setIdentity(U);

        for( int i = 0; i < m; i++ ) u[i] = 0;

        for( int j = min-1; j >= 0; j-- ) {
            u[j] = 1;
            for( int i = j+1; i < m; i++ ) {
                u[i] = UBV.get(i,j);
            }
            if( transpose )
                QRDecompositionHouseholder.rank1UpdateMultL(U,u,gammasU[j],j,j,m,this.b);
            else
                QRDecompositionHouseholder.rank1UpdateMultR(U,u,gammasU[j],j,j,m,this.b);
        }

        return U;
    }

    /**
     * Returns the orthogonal V matrix.
     *
     * @param V If not null then the results will be stored here.  Otherwise a new matrix will be created.
     * @return The extracted Q matrix.
     */
    public DenseMatrix64F getV( DenseMatrix64F V , boolean transpose , boolean compact ) {
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

        CommonOps.setIdentity(V);

//        UBV.print();

        for( int j = min-1; j >= 0; j-- ) {
            u[j+1] = 1;
            for( int i = j+2; i < n; i++ ) {
                u[i] = UBV.get(j,i);
            }
            if( transpose )
                QRDecompositionHouseholder.rank1UpdateMultL(V,u,gammasV[j],j+1,j+1,n,this.b);
            else
                QRDecompositionHouseholder.rank1UpdateMultR(V,u,gammasV[j],j+1,j+1,n,this.b);
        }

        return V;
    }

    /**
     * Internal function for computing the decomposition.
     */
    private boolean _decompose() {
        for( int k = 0; k < min; k++ ) {
            computeU(k);
            computeV(k);
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

            double tau = 0;
            // normalize to reduce overflow/underflow
            // and compute tau for the reflector
            for( int i = k; i < m; i++ ) {
                double val = u[i] /= max;
                tau += val*val;
            }

            tau = Math.sqrt(tau);

            if( u[k] < 0 )
                tau = -tau;

            // write the reflector into the lower left column of the matrix
            double nu = u[k] + tau;
            u[k] = 1.0;

            for( int i = k+1; i < m; i++ ) {
                b[i*n+k] = u[i] /= nu;
            }

            double gamma = nu/tau;
            gammasU[k] = gamma;

            // ---------- multiply on the left by Q_k
            QRDecompositionHouseholder.rank1UpdateMultR(UBV,u,gamma,k+1,k,m,this.b);

            b[k*n+k] = -tau*max;
        } else {
            gammasU[k] = 0;
        }
    }

    protected void computeV(int k) {
        double b[] = UBV.data;

        // find the largest value in this column
        // this is used to normalize the column and mitigate overflow/underflow
        double max = 0;

        int row = k*n;

        for( int i = k+1; i < n; i++ ) {
            // copy the householder vector to vector outside of the matrix to reduce caching issues
            // big improvement on larger matrices and a relatively small performance hit on small matrices.
            double val = b[row+i];
            val = Math.abs(val);
            if( val > max )
                max = val;
        }

        if( max > 0 ) {
            // -------- set up the reflector Q_k

            double tau = 0;
            // normalize to reduce overflow/underflow
            // and compute tau for the reflector
            for( int i = k+1; i < n; i++ ) {
                double val = b[row+i] /= max;
                tau += val*val;
            }

            tau = Math.sqrt(tau);

            if( b[row+k+1] < 0 )
                tau = -tau;

            // write the reflector into the lower left column of the matrix
            double nu = b[row+k+1] + tau;
            u[k+1] = 1.0;

            for( int i = k+2; i < n; i++ ) {
                u[i] = b[row+i] /= nu;
            }

            double gamma = nu/tau;
            gammasV[k] = gamma;

            // writing to u could be avoided by working directly with b.
            // requires writing a custom rank1Update function
            // ---------- multiply on the left by Q_k
            QRDecompositionHouseholder.rank1UpdateMultL(UBV,u,gamma,k+1,k+1,n,this.b);

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
}
