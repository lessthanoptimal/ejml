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

package org.ejml.alg.dense.linsol.chol;

import org.ejml.alg.dense.decomposition.TriangularSolver;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionLDL;
import org.ejml.alg.dense.linsol.LinearSolverAbstract;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.SpecializedOps;


/**
 * @author Peter Abeles
 */
public class LinearSolverCholLDL extends LinearSolverAbstract {

    private CholeskyDecompositionLDL decomp;
    private int n;
    private DenseMatrix64F vv;
    private DenseMatrix64F L;
    private DenseMatrix64F d;

    public LinearSolverCholLDL( CholeskyDecompositionLDL decomp ) {
        this.decomp = decomp;
    }

    public LinearSolverCholLDL() {
        this.decomp = new CholeskyDecompositionLDL();
    }

    @Override
    public boolean setA(DenseMatrix64F A) {
        _setA(A);

        if( decomp.decompose(A) ){
            n = A.numCols;
            vv = decomp._getVV();
            L = decomp.getL();
            d = decomp.getD();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public double quality() {
        return Math.abs(SpecializedOps.diagProd(decomp.getL()));
    }

    /**
     * <p>
     * Using the decomposition, finds the value of 'X' in the linear equation below:<br>
     *
     * A*x = b<br>
     *
     * where A has dimension of n by n, x and b are n by m dimension.
     * </p>
     * <p>
     * *Note* that 'b' and 'x' can be the same matrix instance.
     * </p>
     *
     * @param B A matrix that is n by m.  Not modified.
     * @param X An n by m matrix where the solution is writen to.  Modified.
     */
    @Override
    public void solve( DenseMatrix64F B , DenseMatrix64F X ) {
        if( B.numCols != X.numCols && B.numRows != n && X.numRows != n) {
            throw new IllegalArgumentException("Unexpected matrix size");
        }

        for( int j = 0; j < B.numCols; j++ ) {
            for( int i = 0; i < n; i++ ) vv.set( i , B.unsafe_get( i, j ) );
            solveInternal();
            for( int i = 0; i < n; i++ ) X.unsafe_set( i , j , vv.get(i) );
        }
    }

    /**
     * Used internally to find the solution to a single column vector.
     */
    private void solveInternal() {
        // solve L*s=b storing y in x
        TriangularSolver.solveL(L,vv,n);

        // solve D*y=s
        for( int i = 0; i < n; i++ ) {
            vv.div( i, d.get(i) );
        }

        // solve L^T*x=y
        TriangularSolver.solveTranL(L,vv,n);
    }

    /**
     * Sets the matrix 'inv' equal to the inverse of the matrix that was decomposed.
     *
     * @param inv Where the value of the inverse will be stored.  Modified.
     */
    @Override
    public void invert( DenseMatrix64F inv ) {
        if( inv.numRows != n || inv.numCols != n ) {
            throw new RuntimeException("Unexpected matrix dimension");
        }

        // solve L*z = b
        for( int i =0; i < n; i++ ) {
            for( int j = 0; j <= i; j++ ) {
                double sum = (i==j) ? 1.0 : 0.0;
                for( int k=i-1; k >=j; k-- ) {
                    sum -= L.unsafe_get(i, k )*inv.unsafe_get( j, k );
                }
                inv.unsafe_set( j , i , sum );
            }
        }

        // solve D*y=z
        for( int i =0; i < n; i++ ) {
            double inv_d = 1.0/d.get(i);
            for( int j = 0; j <= i; j++ ) {
                inv.times( j*n+i , inv_d );
            }
        }

        // solve L^T*x = y
        for( int i=n-1; i>=0; i-- ) {
            for( int j = 0; j <= i; j++ ) {
                double sum = (i<j) ? 0 : inv.unsafe_get(j, i);
                for( int k=i+1;k<n;k++) {
                    sum -= L.unsafe_get( k, i )*inv.unsafe_get( j, k );
                }
                inv.unsafe_set(i,j,sum);
                inv.unsafe_set(j,i,sum);
            }
        }
    }
}