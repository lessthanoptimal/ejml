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

package org.ejml.alg.dense.linsol.gj;

import org.ejml.alg.dense.decomposition.SingularMatrixException;
import org.ejml.alg.dense.linsol.LinearSolverAbstract;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.RowD1Matrix64F;


/**
 * <p>
 * Gauss-Jordan elimination is an algorithm that can solve linear equations and invert
 * matrices.  This implementation includes full pivoting to improve numerical stability.  In
 * almost all situations this should be used instead of {@link GaussJordanNoPivot}.
 * </p>
 * <p>
 * Finding the solutions for the 'x' variables is less prone to numerical errors when done
 * at the same time as solving for the inverse of 'A'.
 * </p>
 *
 * <p>
 * Numerical Recipes The Art of Scientific Computing<br>
 * Third Edition<br>
 * Pages 44-45<br>
 * </P>
 *
 * @author Peter Abeles
 */
public class GaussJordan extends LinearSolverAbstract {

    private int ipiv[];
    private int indexRow[];
    private int indexCol[];

    public GaussJordan( int dimen ) {
        ipiv = new int[dimen];
        indexRow = new int[dimen];
        indexCol = new int[dimen];
    }

    @Override
    public boolean setA(DenseMatrix64F A) {
        _setA(A);

//        for( int i = 0; i < ipiv.length; i++ ) {
//            ipiv[i] = 0;
//            indexRow[i] = 0;
//            indexCol[i] = 0;
//        }

        return true;
    }

    @Override
    public double quality() {
        throw new IllegalArgumentException("Not supported by this solver.");
    }

    public static void checkArgumentSquare( RowD1Matrix64F mat , String name )
    {
        if( mat.numCols != mat.numRows)
            throw new IllegalArgumentException("'"+name+"' must be a square matrix.");
    }

    @Override
    public void invert( DenseMatrix64F A )
    {
        checkArgumentSquare(A,"A");

        if( A != this.A )
            A.set(this.A);
        
        final int N = A.numCols;


        for( int i = 0; i < N; i++ ) {
            ipiv[i] = 0;
        }

        // main loop over columns
        for( int i = 0; i < N; i++ ) {

            double bestVal = 0;
            int bestRow=-1;
            int bestCol=-1;

            // look for a pivot element in the columns that need
            // to be reduced
            for( int j = 0; j < N; j++ ) {
                if( ipiv[j] != 1 ) {
                    for( int k = 0; k < N; k++ ) {
                        if( ipiv[k] == 0 ) {
                            double val = A.unsafe_get(j,k);
                            if( val < 0 ) val = -val;
                            if( val > bestVal ) {
                                bestVal = val;
                                bestRow = j;
                                bestCol = k;
                            }
                        }
                    }
                }
            }
            if( bestCol < 0 ) {
                throw new RuntimeException();
            }
            ipiv[bestCol]++;

            if( bestRow != bestCol ) {
                swapRow(A,A.numCols,bestRow,bestCol);
            }

            indexRow[i] = bestRow;
            indexCol[i] = bestCol;

            double valA = A.get(bestCol,bestCol);
            if( valA == 0.0 ) {
                throw new SingularMatrixException();
            }

            A.set(bestCol*N+bestCol, 1.0);
            // make the first element in this row 1
            for( int x = 0; x < N; x++ ) {
                A.div(bestCol*N+x, valA);
            }

            // make all the i columns zero, except for row i
            for( int j = 0; j < N; j++ ) {
                if( bestCol == j ) continue;
                double val = A.get(j,bestCol);
                A.set(j*N+bestCol, 0);

                for( int x = 0; x < N; x++ ) {
                    A.minus(j*N+x, val*A.get(bestCol*N+x));
                }
            }
        }

        unscramble(N, A, indexRow, indexCol);
    }

    /**
     * Computes the inverse of matrix A and solves for X for each column in B.  Both
     * matrices are modified.
     */
    @Override
    public void solve( DenseMatrix64F B , DenseMatrix64F X )
    {
        checkArgumentSquare(A,"A");

        if( A.getNumCols() != B.getNumRows() ) {
            throw new IllegalArgumentException("Dimensions of A and B are not compatible.");
        }
        X.set(B);

        final int N = A.numCols;

        for( int i = 0; i < N; i++ ) {
            ipiv[i] = 0;
        }

        // main loop over columns
        for( int i = 0; i < N; i++ ) {

            double bestVal = 0;
            int bestRow=-1;
            int bestCol=-1;

            // look for a pivot element in the columns that need
            // to be reduced
            for( int j = 0; j < N; j++ ) {
                if( ipiv[j] != 1 ) {
                    for( int k = 0; k < N; k++ ) {
                        if( ipiv[k] == 0 ) {
                            double val = A.unsafe_get(j,k);
                            if( val < 0 ) val = -val;
                            if( val > bestVal ) {
                                bestVal = val;
                                bestRow = j;
                                bestCol = k;
                            }
                        }
                    }
                }
            }
            ipiv[bestCol]++;

            if( bestRow != bestCol ) {
                swapRow(A,A.numCols,bestRow,bestCol);
                swapRow(X,X.numCols,bestRow,bestCol);
            }

            indexRow[i] = bestRow;
            indexCol[i] = bestCol;

            double valA = A.get(bestCol,bestCol);
            if( valA == 0.0 ) {
                throw new RuntimeException("Singular Matrix");
            }
            valA = 1.0/valA;

            A.unsafe_set(bestCol,bestCol, 1.0);
            // make the first element in this row 1
            for( int x = 0; x < N; x++ ) {
                A.times(bestCol*N+x,  valA);
            }
            for( int x = 0; x < X.numCols; x++ ) {
                X.times(bestCol*X.numCols+x, valA);
            }

            // make all the i columns zero, except for row i
            for( int j = 0; j < N; j++ ) {
                if( bestCol == j ) continue;
                double val = A.get(j,bestCol);
                A.set(j*N+bestCol, 0);

                for( int x = 0; x < N; x++ ) {
                    A.minus(j*N+x, val*A.get(bestCol*N+x));
                }

                for( int x = 0; x < X.numCols; x++ ) {
                    X.minus(j*X.numCols+x, val*X.get(bestCol*X.numCols+x));
                }
            }
        }

        unscramble(N, A, indexRow, indexCol);
    }

    @Override
    public boolean modifiesA() {
        return false;
    }

    @Override
    public boolean modifiesB() {
        return false;
    }

    private static void unscramble(int N, RowD1Matrix64F data, int[] indexRow, int[] indexCol) {
        for( int i = N -1; i >= 0; i-- ) {
            if( indexRow[i] != indexCol[i]) {
                for( int k = 0; k < N; k++ ){
                    int row = k*N;
                    int ir = row + indexRow[i];
                    int ic = row + indexCol[i];
                    double temp = data.get(ir);
                    data.set(ir, data.get(ic));
                    data.set(ic, temp);
                }
            }
        }
    }

    private static void swapRow( RowD1Matrix64F data , int numCols , int fromRow , int toRow )
    {
        int indexFrom = fromRow*numCols;
        int indexTo = toRow*numCols;

        int end = indexFrom + numCols;

        for( ; indexFrom < end; indexFrom++ , indexTo++) {
            double temp = data.get(indexTo);
            data.set(indexTo, data.get(indexFrom));
            data.set(indexFrom, temp);
        }
    }
}
