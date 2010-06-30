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

package org.ejml.alg.dense.linsol.svd;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.decomposition.svd.SvdNumericalRecipes;
import org.ejml.alg.dense.linsol.LinearSolverAbstract;
import org.ejml.data.DenseMatrix64F;


/**
 * A LinearSolver for Singular Value Decomposition (SVD)
 *
 * @author Peter Abeles
 */
public class LinearSolverSvdNR extends LinearSolverAbstract {

    private SvdNumericalRecipes svd;
    private int m,n;
    private double []xx;
    private double []tmp;
    private double []w;
    private double []u,v;
    private double thresh;

    public LinearSolverSvdNR( SvdNumericalRecipes svd ) {
        this.svd = svd;
    }

    @Override
    public boolean setA(DenseMatrix64F A) {
        _setA(A);

        svd.decompose(A);
        m = A.numRows;
        n = A.numCols;
        xx = svd._getXX();
        tmp = svd._getTmp();
        w = svd.getSingularValues();
        u = svd.getU(false).data;
        v = svd.getV(false).data;
        thresh = UtilEjml.EPS*100;

        return true;
    }

    /**
     * Solves the linear equation:
     *
     * A*X = B
     *
     * B and X can be the same matrix instance.
     *
     * @param b An n by nx matrix. Not modified.
     * @param x An n by nx matrix. Modified.
     */
    @Override
    public void solve( DenseMatrix64F b , DenseMatrix64F x )
    {
        if( b.numRows != n || x.numRows != n || b.numCols != x.numCols ) {
            throw new IllegalArgumentException("Unexpected matrix size for 'b' and/or 'x'");
        }

        double dataB[] = b.data;
        double dataX[] = x.data;
        int nb = b.numCols;

        for( int j = 0; j < nb; j++ ) {
            for( int i = 0; i < n; i++ ) xx[i] = dataB[i*nb+j];
            solveInternal();
            for( int i = 0; i < n; i++ ) dataX[i*nb+j] = xx[i];
        }
    }

    /**
     * A function that's used internally to find the solution to a vector of B
     */
    private void solveInternal() {
        for( int j = 0; j < n; j++ ) {
            double s = 0.0;

            if( w[j] > thresh) {
                for( int i = 0; i < m; i++ ) {
                    s += u[i*n+j]*xx[i];
                }
                s /= w[j];
            }
            tmp[j] = s;
        }

        for( int j = 0; j < n; j++ ) {
            double s = 0.0;
            for( int jj=0; jj<n;jj++) {
                s += v[j*n+jj]*tmp[jj];
            }
            xx[j] = s;
        }
    }
}
