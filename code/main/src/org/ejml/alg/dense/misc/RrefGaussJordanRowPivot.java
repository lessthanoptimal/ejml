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

package org.ejml.alg.dense.misc;

import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.linsol.ReducedRowEchelonForm;

/**
 * Reduction to RREF using Gauss-Jordan elimination with row (partial) pivots.
 *
 * @author Peter Abeles
 */
public class RrefGaussJordanRowPivot implements ReducedRowEchelonForm<DenseMatrix64F> {

    // tolerance for singular matrix
    double tol;

    @Override
    public void setTolerance(double tol) {
        this.tol = tol;
    }

    @Override
    public void reduce( DenseMatrix64F A , int coefficientColumns) {
        if( A.numCols < coefficientColumns)
            throw new IllegalArgumentException("The system must be at least as wide as A");

        // number of leading ones which have been found
        int leadIndex = 0;
        // compute the decomposition
        for( int i = 0; i < coefficientColumns; i++ ) {

            // select the row to pivot by finding the row with the largest column in 'i'
            int pivotRow = -1;
            double maxValue = tol;

            for( int row = leadIndex; row < A.numRows; row++ ) {
                double v = Math.abs(A.data[row*A.numCols + i]);

                if( v > maxValue ) {
                    maxValue = v;
                    pivotRow = row;
                }
            }

            if( pivotRow == -1 )
                continue;

            // perform the row pivot
            // NOTE: performance could be improved by delaying the physical swap of rows until the end
            //       and using a technique which does the minimal number of swaps
            if( leadIndex != pivotRow)
                swapRows(A,leadIndex,pivotRow);

            // zero column 'i' in all but the pivot row
            for( int row = 0; row < A.numRows; row++ ) {
                if( row == leadIndex ) continue;

                int indexPivot = leadIndex*A.numCols+i;
                int indexTarget = row*A.numCols+i;

                double alpha = A.data[indexTarget]/A.data[indexPivot++];
                A.data[indexTarget++] = 0;
                for( int col = i+1; col < A.numCols; col++ ) {
                    A.data[indexTarget++] -= A.data[indexPivot++]*alpha;
                }
            }

            // update the pivot row
            int indexPivot = leadIndex*A.numCols+i;
            double alpha = 1.0/A.data[indexPivot];
            A.data[indexPivot++] = 1;
            for( int col = i+1; col < A.numCols; col++ ) {
                A.data[indexPivot++] *= alpha;
            }
            leadIndex++;
        }
    }

    protected static void swapRows( DenseMatrix64F A , int rowA , int rowB ) {
        int indexA = rowA*A.numCols;
        int indexB = rowB*A.numCols;

        for( int i = 0; i < A.numCols; i++ , indexA++,indexB++) {
            double temp = A.data[indexA];
            A.data[indexA] = A.data[indexB];
            A.data[indexB] = temp;
        }
    }
}
