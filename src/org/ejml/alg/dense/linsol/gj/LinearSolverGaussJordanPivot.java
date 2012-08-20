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

package org.ejml.alg.dense.linsol.gj;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.linsol.InvertUsingSolve;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.GjPivotDecomposition;
import org.ejml.factory.LinearSolver;
import org.ejml.ops.CommonOps;
import org.ejml.ops.SpecializedOps;

/**
 * Solves linear systems using Gauss-Jordan elimination
 *
 * @author Peter Abeles
 */
public class LinearSolverGaussJordanPivot implements LinearSolver<DenseMatrix64F> {

    GjPivotDecomposition<DenseMatrix64F> decomp;

    public LinearSolverGaussJordanPivot(GjPivotDecomposition<DenseMatrix64F> decomp) {
        this.decomp = decomp;
    }

    @Override
    public boolean setA(DenseMatrix64F A) {
        decomp.setTolerance(CommonOps.elementMaxAbs(A)* UtilEjml.EPS);
        return decomp.decompose(A);
    }

    @Override
    public double quality() {
        // can support be added?
        throw new IllegalArgumentException("Not supported by this solver.");
    }

    @Override
    public void solve(DenseMatrix64F B, DenseMatrix64F X) {
        DenseMatrix64F GJ = decomp.getDecomposition();


        int max = Math.min(GJ.numRows,GJ.numCols);
        int pivots[] = decomp.getRowPivots();

        for( int i = 0; i < max; i++ ) {
            int pivotRow = pivots[i];

            for( int row = 0; row < GJ.numRows; row++ ) {
                if( row == pivotRow ) continue;

                double alpha = GJ.get(row,i);
                for( int col = 0; col < B.numCols; col++ ) {
                    B.data[row*B.numCols + col] -= B.data[pivotRow*B.numCols + col] * alpha;
                }
            }

            double alpha = GJ.get(pivotRow,i);
            for( int col = 0; col < B.numCols; col++ ) {
                B.data[pivotRow*B.numCols + col] /= alpha;
            }
        }

        for( int i = 0; i < X.numRows; i++ ) {
            int row = pivots[i];

            System.arraycopy(B.data,i*B.numCols,X.data,row*X.numCols,X.numCols);
        }
    }


    @Override
    public void invert(DenseMatrix64F A_inv) {
        // TODO Reconstructing A is very slow
        DenseMatrix64F A = decomp.getDecomposition().copy();
        SpecializedOps.gaussJordanReconstruct(A,decomp.getRowPivots());

        InvertUsingSolve.invert(this, A, A_inv, new DenseMatrix64F(A.numRows,A.numCols));
    }

    @Override
    public boolean modifiesA() {
        return decomp.inputModified();
    }

    @Override
    public boolean modifiesB() {
        return true;
    }
}
