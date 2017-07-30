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

package org.ejml.sparse.csc.decomposition.lu;

import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.interfaces.decomposition.LUDecomposition_F64;

/**
 * LU Decomposition using a left looking algorithm for {@link DMatrixSparseCSC}.
 *
 * <p>NOTE: Based mostly on the algorithm described on page 86 in csparse. cs_lu</p>
 *
 * @author Peter Abeles
 */
public class LuUpLooking_DSCC
    implements LUDecomposition_F64<DMatrixSparseCSC>
{

    // storage for LU decomposition
    DMatrixSparseCSC L = new DMatrixSparseCSC(0,0,0);
    DMatrixSparseCSC U = new DMatrixSparseCSC(0,0,0);

    // row pivot matrix, for numerical stability
    int pinv[] = new int[0];

    // column pivots for reducing fill in
    int q[];

    // work space variables
    double w[] = new double[0];


    @Override
    public boolean decompose(DMatrixSparseCSC A) {
        if( A.numRows != A.numCols )
            throw new IllegalArgumentException("Expected square matrix");

        int n = A.numRows;
        // number of non-zero elements can only be easily estimated because of pivots
        L.reshape(n,n,4*A.nz_length+n);
        L.nz_length = 0;
        U.reshape(n,n,4*A.nz_length+n);
        U.nz_length = 0;

        if( pinv.length != n ) {
            pinv = new int[n];
            w = new double[n];
        }

        for (int i = 0; i < n; i++) {
            w[i] = 0;
            pinv[i] = -1;
            L.col_idx[i] = 0;
        }


        // main loop for computing L and U
        for (int k = 0; k < n; k++) {
            //--------- Triangular Solve
            L.col_idx[k] = L.nz_length;  // start of column k
            U.col_idx[k] = U.nz_length;

            // grow storage in L and U if needed
            if( L.nz_length+n > L.nz_values.length )
                L.growMaxLength(2*L.nz_values.length+n, true);
            if( U.nz_length+n > U.nz_values.length )
                U.growMaxLength(2*U.nz_values.length+n, true);

            int col = q != null ? q[k] : k;
//            int op = TriangularSolver_DSCC.solve(L,A,col,xi,pinv,x,,i);

            //--------- Find the Next Pivot

        }

        return false;
    }

    @Override
    public Complex_F64 computeDeterminant() {
        return null;
    }

    @Override
    public DMatrixSparseCSC getLower(DMatrixSparseCSC lower) {
        return null;
    }

    @Override
    public DMatrixSparseCSC getUpper(DMatrixSparseCSC upper) {
        return null;
    }

    @Override
    public DMatrixSparseCSC getPivot(DMatrixSparseCSC pivot) {
        return null;
    }

    @Override
    public boolean isSingular() {
        return false;
    }

    @Override
    public boolean inputModified() {
        return false;
    }
}
