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

package org.ejml.alg.dense.decomposition.gj;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.GjPivotDecomposition;

/**
 * <p>
 * Gauss-Jordan decomposition with row pivots.  The order in which rows are processed is based on the size
 * of the absolute value of the elements in the column being considered.  Should improve numerical stability.
 * </p>
 *
 * @author Peter Abeles
 */
public class GjDecompositionRowPivot implements GjPivotDecomposition<DenseMatrix64F> {

    // decompose matrix
    DenseMatrix64F A;

    // tolerance for singular matrix
    double tol;

    // stores the order in which the rows are processed.
    int pivot[] = new int[1];

    @Override
    public void setTolerance(double tol) {
        this.tol = tol;
    }

    @Override
    public boolean decompose(DenseMatrix64F orig) {
        // initialize data structures
        this.A = orig;

        int max = Math.min(orig.numCols,orig.numRows);

        if( pivot.length < A.numRows )
            pivot = new int[A.numRows];
        for( int i = 0; i < A.numRows; i++ )
            pivot[i] = i;

        // compute the decomposition
        for( int i = 0; i < max; i++ ) {
            // select the row to pivot by finding the row with the largest column 'i'
            int nextRow = -1;
            double maxValue = tol;

            for( int row = i; row < A.numRows; row++ ) {
                double v = Math.abs(A.data[pivot[row]*A.numCols + i]);

                if( v > maxValue ) {
                    maxValue = v;
                    nextRow = row;
                }
            }

            if( nextRow == -1 )
                return false;

            // perform the pivot
            int pivotRow = pivot[nextRow];
            pivot[nextRow] = pivot[i];
            pivot[i] = pivotRow;

            // zero column 'i' in all but the pivot row
            for( int row = 0; row < pivotRow; row++ ) {
                int indexPivot = pivotRow*A.numCols+i;
                int indexTarget = row*A.numCols+i;

                double alpha = A.data[indexTarget]/A.data[indexPivot++];
                A.data[indexTarget++] = alpha;
                for( int col = i+1; col < A.numCols; col++ ) {
                    A.data[indexTarget++] -= A.data[indexPivot++]*alpha;
                }
            }

            for( int row = pivotRow+1; row < A.numRows; row++ ) {
                int indexPivot = pivotRow*A.numCols+i;
                int indexTarget = row*A.numCols+i;

                double alpha = A.data[indexTarget]/A.data[indexPivot++];
                A.data[indexTarget++] = alpha;
                for( int col = i+1; col < A.numCols; col++ ) {
                    A.data[indexTarget++] -= A.data[indexPivot++]*alpha;
                }
            }

            // update the pivot row
            int indexPivot = pivotRow*A.numCols+i;
            double alpha = 1.0/A.data[indexPivot++];
            for( int col = i+1; col < A.numCols; col++ ) {
                A.data[indexPivot++] *= alpha;
            }

        }

        return true;
    }

    @Override
    public boolean inputModified() {
        return true;
    }

    @Override
    public int[] getRowPivots() {
        return pivot;
    }

    @Override
    public DenseMatrix64F getDecomposition() {
        return A;
    }

}
