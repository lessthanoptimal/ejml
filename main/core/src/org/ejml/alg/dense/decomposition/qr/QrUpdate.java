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

package org.ejml.alg.dense.decomposition.qr;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;


/**
 * <p>
 * The effects of adding and removing rows from the A matrix in a QR decomposition can
 * be computed much faster than simply recomputing the whole decomposition.  There are many real
 * world situations where this is useful.  For example, when computing a rolling solution to
 * the most recent N measurements.
 * </p>
 *
 * <p>
 * Definitions: A &isin; &real; <sup>m &times; n</sup>, m &ge; n, rank(A) = n and that A = QR, where
 * Q &isin; &real; <sup>m &times; m</sup> is orthogonal, and R &isin; &real; <sup>m &times; n</sup> is
 * upper triangular.
 * </p>
 * 
 * <p>
 * ** IMPORTANT USAGE NOTE ** If auto grow is set to true then the internal data structures will grow automatically
 * to accommodate the matrices passed in.  When adding elements to the decomposition the matrices must have enough
 * data elements to grow before hand.
 * </p>
 *
 * <p>
 * For more information see David S. Watkins, "Fundamentals of Matrix Computations" 2nd edition, pages 249-259.
 * It is also possible to add and remove columns efficiently, but this is less common and is not supported at
 * this time.
 * </p>
 * @author Peter Abeles
 */
public class QrUpdate {

    // the decomposition that is being adjusted
    private DenseMatrix64F Q,R;
    // product of planar multiplications
    private DenseMatrix64F U_tran; // using transpose of U reduces cache misses
    private DenseMatrix64F Qm;

    // used to temporarially store data
    private double r_row[];

    // it can process matrices up to this size
    private int maxCols;
    private int maxRows;

    // number of rows and columns in the original A matrix that was decomposed
    private int m,n;
    // number of rows in the adjusted matrices
    private int m_m;

    // should it declare new internal data when what currently exists is too small or throw
    // and exception.
    private boolean autoGrow;

    /**
     * Creates an update which can decompose matrices up to the specified size.  Autogrow
     * is set to false.
     *
     * @param maxRows
     * @param maxCols
     */
    public QrUpdate( int maxRows , int maxCols ) {
        autoGrow = false;
        declareInternalData(maxRows, maxCols);
    }

    /**
     * Creates an update which can decompose matrices up to the specified size.  Autogrow
     * is configurable.
     *
     * @param maxRows
     * @param maxCols
     * @param autoGrow
     */
    public QrUpdate( int maxRows , int maxCols , boolean autoGrow ) {
        this.autoGrow = autoGrow;
        declareInternalData(maxRows, maxCols);
    }

    /**
     * Does not predeclare data and it will autogrow.
     */
    public QrUpdate(){
        autoGrow = true;
    }

    /**
     * Declares the internal data structures so that it can process matrices up to the specified size.
     *
     * @param maxRows
     * @param maxCols
     */
    public void declareInternalData(int maxRows, int maxCols) {
        this.maxRows = maxRows;
        this.maxCols = maxCols;

        U_tran = new DenseMatrix64F(maxRows,maxRows);
        Qm = new DenseMatrix64F(maxRows,maxRows);

        r_row = new double[ maxCols ];
    }

    /**
     * <p>
     * Adjusts the values of the Q and R matrices to take in account the effects of inserting
     * a row to the 'A' matrix at the specified location.  This operation requires about 6mn + O(n) flops.
     * </p>
     *
     * <p>
     * If Q and/or R does not have enough data elements to grow then an exception is thrown.
     * </p>
     *
     * <p>
     * The adjustment done is by computing a series of planar Givens rotations that make the adjusted R
     * matrix upper triangular again.  This is then used to modify the Q matrix.
     * </p>
     *
     * @param Q The Q matrix which is to be modified, must be big enough to grow.  Must be n by n..  Is modified.
     * @param R The R matrix which is to be modified, must be big enough to grow.  Must be m by n.  Is modified.
     * @param row The row being inserted.  Not modified.
     * @param rowIndex Which row index it is to be inserted at.
     * @param resizeR Should the number of rows in R be changed?  The additional rows are all zero.
     */
    public void addRow( DenseMatrix64F Q , DenseMatrix64F R , double []row , int rowIndex , boolean resizeR ) {
        // memory management and check precoditions
        setQR(Q,R,1);
        m_m = m+1;

        if( Q.data.length < m_m*m_m )
            throw new IllegalArgumentException("Q matrix does not have enough data to grow");

        if( resizeR && R.data.length < m_m*n )
            throw new IllegalArgumentException("R matrix does not have enough data to grow");

        if( resizeR )
            R.reshape(m_m,n, false);

        U_tran.reshape(m_m,m_m, false);

        // apply givens rotation to the first two rows of the augmented R matrix
        applyFirstGivens(row);
        applyLaterGivens();
        // compute new Q matrix
        updateInsertQ(rowIndex);

        // discard the reference since it is no longer needed
        this.Q = this.R = null;
    }

    /**
     * <p>
     * Adjusts the values of the Q and R matrices to take in account the effects of removing
     * a row from the 'A' matrix at the specified location.  This operation requires about 6mn + O(n) flops.
     * </p>
     *
     * <p>
     * The adjustment is done by computing a series of planar Givens rotations that make the removed row in Q
     * equal to [1 0 ... 0].
     * </p>
     *
     * @param Q The Q matrix.  Is modified.
     * @param R The R matrix.  Is modified.
     * @param rowIndex Which index of the row that is being removed.
     * @param resizeR should the shape of R be adjusted?
     */
    public void deleteRow( DenseMatrix64F Q , DenseMatrix64F R , int rowIndex , boolean resizeR ) {
        setQR(Q,R,0);
        if( m - 1 < n ) {
            throw new IllegalArgumentException("Removing any row would make the system under determined.");
        }

        m_m = m - 1;
        U_tran.reshape(m,m, false);

        if( resizeR )
            R.reshape(m_m,n, false);
        
        computeRemoveGivens(rowIndex);
        updateRemoveQ(rowIndex);

        updateRemoveR();

        // discard the reference since it is no longer needed
        this.Q = this.R = null;
    }

    /**
     * Provides the results of a QR decomposition.  These will be modified by adding or removing
     * rows from the original 'A' matrix.
     * 
     * @param Q The Q matrix which is to be modified.  Is modified later and reference saved.
     * @param R The R matrix which is to be modified.  Is modified later and reference saved.
     */
    private void setQR( DenseMatrix64F Q , DenseMatrix64F R , int growRows ) {
        if( Q.numRows != Q.numCols ) {
            throw new IllegalArgumentException("Q should be square.");
        }

        this.Q = Q;
        this.R = R;

        m = Q.numRows;
        n = R.numCols;

        if( m+growRows > maxRows || n > maxCols ) {
            if( autoGrow ) {
                declareInternalData(m+growRows,n);
            } else {
                throw new IllegalArgumentException("Autogrow has been set to false and the maximum number of rows" +
                        " or columns has been exceeded.");
            }
        }
    }

    /**
     * Updates the Q matrix to take in account the inserted matrix.
     *
     * @param rowIndex where the matrix has been inserted.
     */
    private void updateInsertQ( int rowIndex ) {
        Qm.set(Q);
        Q.reshape(m_m,m_m, false);

        for( int i = 0; i < rowIndex; i++ ) {
            for( int j = 0; j < m_m; j++ ) {
                double sum = 0;
                for( int k = 0; k < m; k++ ) {
                    sum += Qm.data[i*m+k]* U_tran.data[j*m_m+k+1];
                }
                Q.data[i*m_m+j] = sum;
            }
        }

        for( int j = 0; j < m_m; j++ ) {
            Q.data[rowIndex*m_m+j] = U_tran.data[j*m_m];
        }

        for( int i = rowIndex+1; i < m_m; i++ ) {
            for( int j = 0; j < m_m; j++ ) {
                double sum = 0;
                for( int k = 0; k < m; k++ ) {
                    sum += Qm.data[(i-1)*m+k]* U_tran.data[j*m_m+k+1];
                }
                Q.data[i*m_m+j] = sum;
            }
        }
    }

    /**
     * Updates the Q matrix to take inaccount the row that was removed by only multiplying e
     * lements that need to be.  There is still some room for improvement here...
     * @param rowIndex
     */
    private void updateRemoveQ( int rowIndex ) {
        Qm.set(Q);
        Q.reshape(m_m,m_m, false);

        for( int i = 0; i < rowIndex; i++ ) {
            for( int j = 1; j < m; j++ ) {
                double sum = 0;
                for( int k = 0; k < m; k++ ) {
                    sum += Qm.data[i*m+k]* U_tran.data[j*m+k];
                }
                Q.data[i*m_m+j-1] = sum;
            }
        }

        for( int i = rowIndex+1; i < m; i++ ) {
            for( int j = 1; j < m; j++ ) {
                double sum = 0;
                for( int k = 0; k < m; k++ ) {
                    sum += Qm.data[i*m+k]* U_tran.data[j*m+k];
                }
                Q.data[(i-1)*m_m+j-1] = sum;
            }
        }
    }

    /**
     * Updates the R matrix to take in account the removed row.
     */
    private void updateRemoveR() {
        for( int i = 1; i < n+1; i++ ) {
            for( int j = 0; j < n; j++ ) {
                double sum = 0;
                for( int k = i-1; k <= j; k++ ) {
                    sum += U_tran.data[i*m+k] * R.data[k*n+j];
                }
                R.data[(i-1)*n+j] = sum;
            }
        }
    }

    private void applyFirstGivens(double[] row) {
        double c,s;
        double xi = row[0];
        double xj = R.data[0];

        double r = xi*xi + xj*xj;
        if( r != 0 ) {
            r = Math.sqrt(r);
            c = xi/r;
            s = xj/r;

        } else {
            c = 1;
            s = 0;
        }

        R.data[0] = r;
        for( int col = 1; col < n; col++ ) {
            double vali = row[col];
            double valj = R.data[col];

            R.data[col] = c*vali + s*valj;
            r_row[col] = c*valj - s*vali;
        }

        // set U to its initial values
        CommonOps.setIdentity(U_tran);
        U_tran.data[0] = c;
        U_tran.data[1] = s;
        U_tran.data[m_m] = -s;
        U_tran.data[m_m+1] = c;
    }

    private void applyLaterGivens()
    {
        for( int row = 1; row < n; row++ ) {
            // first compute the rotation
            double c,s;
            double xi = r_row[row];
            double xj = R.data[n*row+row];

            double r = xi*xi + xj*xj;
            if( r != 0 ) {
                r = Math.sqrt(r);
                c = xi/r;
                s = xj/r;

            } else {
                c = 1;
                s = 0;
            }

            // update R matrix
            R.data[n*row+row] = r;
            for( int col = row+1; col < n; col++ ) {
                double vali = r_row[col];
                double valj = R.data[n*row+col];

                R.data[n*row+col] = c*vali + s*valj;
                r_row[col] = c*valj - s*vali;
            }

            // compute U^T = U^T_(x+1) * U^T_x
            for( int col = 0; col <= row+1; col++ ) {
                double q1 = U_tran.data[row*m_m+col];
                double q2 = U_tran.data[(row+1)*m_m+col];

                U_tran.data[row*m_m+col] = c*q1 + s*q2;
                U_tran.data[(row+1)*m_m+col] = c*q2 - s*q1;
            }
        }
    }

    private void computeRemoveGivens( int selectedRow )
    {
        CommonOps.setIdentity(U_tran);

        double xj = Q.data[selectedRow*m+m-1];

        for( int j = m-2; j >= 0; j-- ) {
            // first compute the rotation
            double c,s;
            double xi = Q.data[selectedRow*m+j];

            double r = xi*xi + xj*xj;
            if( r != 0 ) {
                r = Math.sqrt(r);
                c = xi/r;
                s = xj/r;
            } else {
                c = 1;
                s = 0;
            }

            // in the next iteration xj is r
            xj = r;

            // compute U^T = U^T_(x+1) * U^T_x
            for( int col = j; col < m; col++ ) {
                double q1 = U_tran.data[j*m+col];
                double q2 = U_tran.data[(j+1)*m+col];

                U_tran.data[j*m+col] = c*q1 + s*q2;
                U_tran.data[(j+1)*m+col] = c*q2 - s*q1;
            }
        }
    }

    public DenseMatrix64F getU_tran() {
        return U_tran;
    }
}
