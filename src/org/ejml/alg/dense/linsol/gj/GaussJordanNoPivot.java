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

import org.ejml.alg.dense.linsol.LinearSolverAbstract;
import org.ejml.data.DenseMatrix64F;


/**
 * This is an implementation of Gauss-Jordan elimination with no pivoting.  This can be used
 * to find the inverse of a matrix and solve systems of linear equations.  Without pivoting
 * it is numerically unstable and probably should not be used.  On the plus side it is very easy
 * to program.
 *
 * This is used to provide a testcase for more complex algortihms against trivial matrices
 *
 * A*x = b
 *
 * @author Peter Abeles
 */
public class GaussJordanNoPivot extends LinearSolverAbstract {

    @Override
    public boolean setA(DenseMatrix64F A) {
        _setA(A);
        return true;
    }

    @Override
    public double quality() {
        return Double.NaN;
    }

    @Override
    public void invert( DenseMatrix64F A )
    {
        GaussJordan.checkArgumentSquare(A,"A");

        if( A != this.A )
            A.set(this.A);

        final int dimen = A.numCols;

        double dataA[] = A.data;

        for( int i = 0; i < dimen; i++ ) {
            double valA = A.get(i,i);

            if( valA == 0 )
                throw new IllegalArgumentException("This algorithm only works if all the diagonal elements are not zero");

            dataA[i*dimen+i] = 1.0;
            // make the first element in this row 1
            for( int x = 0; x < dimen; x++ ) {
                dataA[i*dimen+x] /= valA;
            }

            // make all the i columns zero, except for row i
            for( int j = 0; j < dimen; j++ ) {
                if( i == j ) continue;
                double val = A.get(j,i);
                dataA[j*dimen+i] = 0;

                for( int x = 0; x < dimen; x++ ) {
                    dataA[j*dimen+x] -= val*dataA[i*dimen+x];
                }
            }
        }
    }
    
    /**
     * Computes the inverse of matrix A and solves for X for each column in B.  Both
     * matrices are modified.
     *
     */
    @Override
    public void solve( DenseMatrix64F B , DenseMatrix64F X )
    {
        GaussJordan.checkArgumentSquare(A,"A");

        if( A.getNumCols() != B.getNumRows() ) {
            throw new IllegalArgumentException("Dimensions of A and B are not compatible.");
        }

        X.set(B);

        final int dimen = A.numCols;

        double dataA[] = A.data;
        double dataX[] = X.data;

        for( int i = 0; i < dimen; i++ ) {
            double valA = A.get(i,i);

            if( valA == 0 )
                throw new IllegalArgumentException("This algorithm only works if all the diagonal elements are not zero");

            dataA[i*dimen+i] = 1.0;
            // make the first element in this row 1
            for( int x = 0; x < dimen; x++ ) {
                dataA[i*dimen+x] /= valA;
            }
            for( int x = 0; x < B.numCols; x++ ) {
                dataX[i*X.numCols+x] /= valA;
            }

            // make all the i columns zero, except for row i
            for( int j = 0; j < dimen; j++ ) {
                if( i == j ) continue;
                double val = A.get(j,i);
                dataA[j*dimen+i] = 0;

                for( int x = 0; x < dimen; x++ ) {
                    dataA[j*dimen+x] -= val*dataA[i*dimen+x];
                }

                for( int x = 0; x < X.numCols; x++ ) {
                    dataX[j*X.numCols+x] -= val*dataX[i*X.numCols+x];
                }
            }
        }
    }
}
