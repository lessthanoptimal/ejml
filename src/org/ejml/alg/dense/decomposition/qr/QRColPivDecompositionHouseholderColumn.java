/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.qr;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.decomposition.QRPDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * <p>
 * Performs QR decomposition with column pivoting.  To prevent overflow/underflow the whole matrix
 * is normalized by the max value, but columns are not normalized individually any more. To enable
 * code reuse it extends {@link QRDecompositionHouseholderColumn} and functions from that class
 * are used whenever possible.  Columns are transposed into single arrays, which allow for
 * fast pivots.
 * </p>
 *
 * <p>
 * Decomposition: A*P = Q*R
 * </p>
 *
 * <p>
 * Based off the description in "Fundamentals of Matrix Computations", 2nd by David S. Watkins.
 * </p>
 *
 * @author Peter Abeles
 */
public class QRColPivDecompositionHouseholderColumn
        extends QRDecompositionHouseholderColumn
        implements QRPDecomposition<DenseMatrix64F>
{
    // the ordering of each column, the current column i is the original column pivots[i]
    protected int pivots[];
    // F-norm  squared for each column
    protected double normsCol[];

    // value of the maximum abs element
    protected double maxAbs;
    
    // threshold used to determine when a column is considered to be singular
    // Threshold is relative to the maxAbs
    protected double singularThreshold;

    // the matrix's rank
    protected int rank;

    /**
     * Configure parameters.
     *
     * @param singularThreshold Specify the threshold that selects if column is singular or not.  Typically around EPS
     */
    public QRColPivDecompositionHouseholderColumn(double singularThreshold) {
        this.singularThreshold = singularThreshold;
    }

    public QRColPivDecompositionHouseholderColumn() {
        this(UtilEjml.EPS);
    }

    @Override
    public void setExpectedMaxSize( int numRows , int numCols ) {
        super.setExpectedMaxSize(numRows,numCols);

        if( pivots == null || pivots.length < numCols  ) {
            pivots = new int[numCols];
            normsCol = new double[numCols];
        }
    }

    /**
     * Computes the Q matrix from the information stored in the QR matrix.  This
     * operation requires about 4(m<sup>2</sup>n-mn<sup>2</sup>+n<sup>3</sup>/3) flops.
     *
     * @param Q The orthogonal Q matrix.
     */
    @Override
    public DenseMatrix64F getQ( DenseMatrix64F Q , boolean compact ) {
        if( compact ) {
            if( Q == null ) {
                Q = CommonOps.identity(numRows,minLength);
            } else {
                if( Q.numRows != numRows || Q.numCols != minLength ) {
                    throw new IllegalArgumentException("Unexpected matrix dimension.");
                } else {
                    CommonOps.setIdentity(Q);
                }
            }
        } else {
            if( Q == null ) {
                Q = CommonOps.identity(numRows);
            } else {
                if( Q.numRows != numRows || Q.numCols != numRows ) {
                    throw new IllegalArgumentException("Unexpected matrix dimension.");
                } else {
                    CommonOps.setIdentity(Q);
                }
            }
        }

        for( int j = rank-1; j >= 0; j-- ) {
            double u[] = dataQR[j];

            double vv = u[j];
            u[j] = 1;
            QrHelperFunctions.rank1UpdateMultR(Q,u,gammas[j],j,j,numRows,v);
            u[j] = vv;
        }

        return Q;
    }

    /**
     * <p>
     * To decompose the matrix 'A' it must have full rank.  'A' is a 'm' by 'n' matrix.
     * It requires about 2n*m<sup>2</sup>-2m<sup>2</sup>/3 flops.
     * </p>
     *
     * <p>
     * The matrix provided here can be of different
     * dimension than the one specified in the constructor.  It just has to be smaller than or equal
     * to it.
     * </p>
     */
    @Override
    public boolean decompose( DenseMatrix64F A ) {
        setExpectedMaxSize(A.numRows, A.numCols);

        convertToColumnMajor(A);

        maxAbs = CommonOps.elementMaxAbs(A);
        // initialize pivot variables
        setupPivotInfo();

        // go through each column and perform the decomposition
        for( int j = 0; j < minLength; j++ ) {
            if( j > 0 )
                updateNorms(j);
            swapColumns(j);
            // if its degenerate stop processing
            if( !householderPivot(j) )
                break;
            updateA(j);
            rank = j+1;
        }

        return true;
    }

    /**
     * Sets the initial pivot ordering and compute the F-norm squared for each column
     */
    private void setupPivotInfo() {
        for( int col = 0; col < numCols; col++ ) {
            pivots[col] = col;
            double c[] = dataQR[col];
            double norm = 0;
            for( int row = 0; row < numRows; row++ ) {
                double element = c[row];
                norm += element*element;
            }
            normsCol[col] = norm;
        }
    }


    /**
     * Performs an efficient update of each columns' norm
     */
    private void updateNorms( int j ) {
        boolean foundNegative = false;
        for( int col = j; col < numCols; col++ ) {
            double e = dataQR[col][j-1];
            normsCol[col] -= e*e;

            if( normsCol[col] < 0 ) {
                foundNegative = true;
                break;
            }
        }

        // if a negative sum has been found then clearly too much precision has been last
        // and it should recompute the column norms from scratch
        if( foundNegative ) {
            for( int col = j; col < numCols; col++ ) {
                double u[] = dataQR[col];
                double actual = 0;
                for( int i=j; i < numRows; i++ ) {
                    double v = u[i];
                    actual += v*v;
                }
                normsCol[col] = actual;
            }
        }
    }

    /**
     * Finds the column with the largest normal and makes that the first column
     *
     * @param j Current column being inspected
     */
    private void swapColumns( int j ) {

        // find the column with the largest norm
        int largestIndex = j;
        double largestNorm = normsCol[j];
        for( int col = j+1; col < numCols; col++ ) {
            double n = normsCol[col];
            if( n > largestNorm ) {
                largestNorm = n;
                largestIndex = col;
            }
        }
        // swap the columns
        double []tempC = dataQR[j];
        dataQR[j] = dataQR[largestIndex];
        dataQR[largestIndex] = tempC;
        double tempN = normsCol[j];
        normsCol[j] = normsCol[largestIndex];
        normsCol[largestIndex] = tempN;
        int tempP = pivots[j];
        pivots[j] = pivots[largestIndex];
        pivots[largestIndex] = tempP;
    }

    /**
     * <p>
     * Computes the householder vector "u" for the first column of submatrix j. The already computed
     * norm is used and checks to see if the matrix is singular at this point.
     * </p>
     * <p>
     * Q = I - &gamma;uu<sup>T</sup>
     * </p>
     * <p>
     * This function finds the values of 'u' and '&gamma;'.
     * </p>
     *
     * @param j Which submatrix to work off of.
     * @return false if it is degenerate
     */
    protected boolean householderPivot(int j)
    {
        final double u[] = dataQR[j];

        // find the largest value in this column
        // this is used to normalize the column and mitigate overflow/underflow
        final double max = QrHelperFunctions.findMax(u,j,numRows-j);

        if( max <= 0 ) {
            return false;
        } else {
            // computes tau and normalizes u by max
            tau = QrHelperFunctions.computeTauAndDivide(j, numRows , u, max);
            
            // divide u by u_0
            double u_0 = u[j] + tau;
            QrHelperFunctions.divideElements(j+1,numRows , u, u_0 );

            gamma = u_0/tau;
            tau *= max;

            u[j] = -tau;

            if( Math.abs(tau) <= singularThreshold*maxAbs ) {
                return false;
            }
        }

        gammas[j] = gamma;

        return true;
    }

    @Override
    public int getRank() {
        return rank;
    }

    @Override
    public int[] getPivots() {
        return pivots;
    }

    @Override
    public DenseMatrix64F getPivotMatrix(DenseMatrix64F P) {
        if( P == null )
            P = new DenseMatrix64F(numCols,numCols);
        else if( P.numRows != numCols )
            throw new IllegalArgumentException("Number of rows must be "+numCols);
        else if( P.numCols != numCols )
            throw new IllegalArgumentException("Number of columns must be "+numCols);
        else {
            P.zero();
        }

        for( int i = 0; i < numCols; i++ ) {
            P.set(pivots[i],i,1);
        }

        return P;
    }
}
