/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.linsol.chol;

import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.SpecializedOps_ZDRM;
import org.ejml.dense.row.decompose.TriangularSolver_ZDRM;
import org.ejml.dense.row.decompose.chol.CholeskyDecompositionCommon_ZDRM;
import org.ejml.dense.row.linsol.LinearSolverAbstract_ZDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;

import java.util.Arrays;


/**
* @author Peter Abeles
*/
public class LinearSolverChol_ZDRM extends LinearSolverAbstract_ZDRM {

    CholeskyDecompositionCommon_ZDRM decomposer;
    int n;
    double vv[] = new double[0];
    double t[];

    public LinearSolverChol_ZDRM(CholeskyDecompositionCommon_ZDRM decomposer) {
        this.decomposer = decomposer;
    }

    @Override
    public boolean setA(ZMatrixRMaj A) {
        if( A.numRows != A.numCols )
            throw new IllegalArgumentException("Matrix must be square");

        _setA(A);

        if( decomposer.decompose(A) ){
            n = A.numCols;
            if( vv.length < n*2 )
                vv = new double[n*2];
            t = decomposer._getT().data;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public /**/double quality() {
        return SpecializedOps_ZDRM.qualityTriangular(decomposer._getT());
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
    public void solve(ZMatrixRMaj B , ZMatrixRMaj X ) {
        if( B.numCols != X.numCols || B.numRows != n || X.numRows != n) {
            throw new IllegalArgumentException("Unexpected matrix size");
        }

        int numCols = B.numCols;

        double dataB[] = B.data;
        double dataX[] = X.data;

        if(decomposer.isLower()) {
            for( int j = 0; j < numCols; j++ ) {
                for( int i = 0; i < n; i++ ) {
                    vv[i*2]   = dataB[(i*numCols+j)*2];
                    vv[i*2+1] = dataB[(i*numCols+j)*2+1];
                }
                solveInternalL();
                for( int i = 0; i < n; i++ ) {
                    dataX[(i*numCols+j)*2  ] = vv[i*2];
                    dataX[(i*numCols+j)*2+1] = vv[i*2+1];
                }
            }
        } else {
            throw new RuntimeException("Implement");
        }
    }

    /**
     * Used internally to find the solution to a single column vector.
     */
    private void solveInternalL() {
        // This takes advantage of the diagonal elements always being real numbers

        // solve L*y=b storing y in x
        TriangularSolver_ZDRM.solveL_diagReal(t, vv, n);

        // solve L^T*x=y
        TriangularSolver_ZDRM.solveConjTranL_diagReal(t, vv, n);
    }

    /**
     * Sets the matrix 'inv' equal to the inverse of the matrix that was decomposed.
     *
     * @param inv Where the value of the inverse will be stored.  Modified.
     */
    @Override
    public void invert( ZMatrixRMaj inv ) {
        if( inv.numRows != n || inv.numCols != n ) {
            throw new RuntimeException("Unexpected matrix dimension");
        }
        if( inv.data == t ) {
            throw new IllegalArgumentException("Passing in the same matrix that was decomposed.");
        }

        if(decomposer.isLower()) {
            setToInverseL(inv.data);
        } else {
            throw new RuntimeException("Implement");
        }
    }

    /**
     * Sets the matrix to the inverse using a lower triangular matrix.
     */
    public void setToInverseL( double a[] ) {

        // the more direct method which takes full advantage of the sparsity of the data structures proved to
        // be difficult to get right due to the conjugates and reordering.
        // See comparable real number code for an example.
        for (int col = 0; col < n; col++) {
            Arrays.fill(vv,0);
            vv[col*2] = 1;
            TriangularSolver_ZDRM.solveL_diagReal(t, vv, n);
            TriangularSolver_ZDRM.solveConjTranL_diagReal(t, vv, n);
            for( int i = 0; i < n; i++ ) {
                a[(i*numCols+col)*2  ] = vv[i*2];
                a[(i*numCols+col)*2+1] = vv[i*2+1];
            }
        }
        // NOTE: If you want to make inverse faster take advantage of the sparsity
    }

    @Override
    public boolean modifiesA() {
        return decomposer.inputModified();
    }

    @Override
    public boolean modifiesB() {
        return false;
    }

    @Override
    public CholeskyDecomposition_F64<ZMatrixRMaj> getDecomposition() {
        return decomposer;
    }
}
