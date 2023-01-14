/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

package org.ejml.example;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.interfaces.decomposition.LUSparseDecomposition_F64;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.ejml.sparse.csc.factory.DecompositionFactory_DSCC;
import org.ejml.sparse.csc.misc.TriangularSolver_DSCC;

import java.util.Random;

/**
 * In this example the results from a sparse LU decomposition is found and then used to solve. Code for dense
 * matrices is directly analogous.
 *
 * This is intended as an example for how to use a decomposition's high level interface. When solving a system
 * 99% if the time you want to use a built in solver instead of decomposing the matrix yourself and solving.
 * The built in solver will take full advantage of internal data structures and is likely to have a lower
 * memory foot print and be faster.
 *
 * @author Peter Abeles
 */
public class ExampleDecompositionSolve {
    public static void main(String[] args) {
        // create a random matrix that can be solved
        int N = 5;
        var rand = new Random(234);

        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(N,N,N*N/4,rand);
        RandomMatrices_DSCC.ensureNotSingular(A,rand);

        // Create the LU decomposition
        LUSparseDecomposition_F64<DMatrixSparseCSC> decompose =
                DecompositionFactory_DSCC.lu(FillReducing.NONE);

        // Decompose the matrix.
        // If you care about the A matrix being modified call decompose.inputModified()
        if( !decompose.decompose(A) )
            throw new RuntimeException("The matrix is singular");

        // Extract new copies of the L and U matrices
        DMatrixSparseCSC L = decompose.getLower(null);
        DMatrixSparseCSC U = decompose.getUpper(null);
        DMatrixSparseCSC P = decompose.getRowPivot(null);

        // Storage for an intermediate step
        DMatrixSparseCSC tmp = A.createLike();

        // Storage for the inverse matrix
        DMatrixSparseCSC Ainv = A.createLike();

        // Solve for the inverse: P*I = L*U*inv(A)
        TriangularSolver_DSCC.solve(L,true,P,tmp,null,null,null,null);
        TriangularSolver_DSCC.solve(U,false,tmp,Ainv,null,null,null,null);

        // Make sure the inverse has been found. A*inv(A) = identity should be an identity matrix
        DMatrixSparseCSC found = A.createLike();

        CommonOps_DSCC.mult(A,Ainv,found);
        found.print();

    }
}
