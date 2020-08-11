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

import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.SpecializedOps_CDRM;
import org.ejml.dense.row.decompose.TriangularSolver_CDRM;
import org.ejml.dense.row.decompose.chol.CholeskyDecompositionCommon_CDRM;
import org.ejml.dense.row.linsol.LinearSolverAbstract_CDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F32;

import java.util.Arrays;


/**
* @author Peter Abeles
*/
public class LinearSolverChol_CDRM extends LinearSolverAbstract_CDRM {

    CholeskyDecompositionCommon_CDRM decomposer;
    int n;
    float vv[] = new float[0];
    float t[];

    public LinearSolverChol_CDRM(CholeskyDecompositionCommon_CDRM decomposer) {
        this.decomposer = decomposer;
    }

    @Override
    public boolean setA(CMatrixRMaj A) {
        if( A.numRows != A.numCols )
            throw new IllegalArgumentException("Matrix must be square");

        _setA(A);

        if( decomposer.decompose(A) ){
            n = A.numCols;
            if( vv.length < n*2 )
                vv = new float[n*2];
            t = decomposer._getT().data;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public /**/double quality() {
        return SpecializedOps_CDRM.qualityTriangular(decomposer._getT());
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
    public void solve(CMatrixRMaj B , CMatrixRMaj X ) {
        if( B.numCols != X.numCols || B.numRows != n || X.numRows != n) {
            throw new IllegalArgumentException("Unexpected matrix size");
        }

        int numCols = B.numCols;

        float dataB[] = B.data;
        float dataX[] = X.data;

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
        TriangularSolver_CDRM.solveL_diagReal(t, vv, n);

        // solve L^T*x=y
        TriangularSolver_CDRM.solveConjTranL_diagReal(t, vv, n);
    }

    /**
     * Sets the matrix 'inv' equal to the inverse of the matrix that was decomposed.
     *
     * @param inv Where the value of the inverse will be stored.  Modified.
     */
    @Override
    public void invert( CMatrixRMaj inv ) {
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
    public void setToInverseL( float a[] ) {

        // the more direct method which takes full advantage of the sparsity of the data structures proved to
        // be difficult to get right due to the conjugates and reordering.
        // See comparable real number code for an example.
        for (int col = 0; col < n; col++) {
            Arrays.fill(vv,0);
            vv[col*2] = 1;
            TriangularSolver_CDRM.solveL_diagReal(t, vv, n);
            TriangularSolver_CDRM.solveConjTranL_diagReal(t, vv, n);
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
    public CholeskyDecomposition_F32<CMatrixRMaj> getDecomposition() {
        return decomposer;
    }
}
