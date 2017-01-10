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

package org.ejml.alg.block.decomposition.chol;

import org.ejml.alg.block.InnerRankUpdate_B64;
import org.ejml.alg.block.MatrixOps_B64;
import org.ejml.alg.block.TriangularSolver_B64;
import org.ejml.data.Complex_F64;
import org.ejml.data.D1Submatrix_F64;
import org.ejml.data.DMatrixBlock_F64;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;


/**
 * <p>
 * Block Cholesky using outer product form.  The original matrix is stored and modified.
 * </p>
 *
 * <p>
 * Based on the description provided in "Fundamentals of Matrix Computations" 2nd Ed. by David S. Watkins.
 * </p>
 *
 * @author Peter Abeles
 */
public class CholeskyOuterForm_B64 implements CholeskyDecomposition_F64<DMatrixBlock_F64> {

    // if it should compute an upper or lower triangular matrix
    private boolean lower = false;
    // The decomposed matrix.
    private DMatrixBlock_F64 T;

    // predeclare local work space
    private D1Submatrix_F64 subA = new D1Submatrix_F64();
    private D1Submatrix_F64 subB = new D1Submatrix_F64();
    private D1Submatrix_F64 subC = new D1Submatrix_F64();

    // storage for the determinant
    private Complex_F64 det = new Complex_F64();

    /**
     * Creates a new BlockCholeskyOuterForm
     *
     * @param lower Should it decompose it into a lower triangular matrix or not.
     */
    public CholeskyOuterForm_B64(boolean lower) {
        this.lower = lower;
    }

    /**
     * Decomposes the provided matrix and stores the result in the same matrix.
     *
     * @param A Matrix that is to be decomposed.  Modified.
     * @return If it succeeded or not.
     */
    @Override
    public boolean decompose(DMatrixBlock_F64 A) {
        if( A.numCols != A.numRows )
            throw new IllegalArgumentException("A must be square");

        this.T = A;

        if( lower )
            return decomposeLower();
        else
            return decomposeUpper();
    }

    private boolean decomposeLower() {
        int blockLength = T.blockLength;

        subA.set(T);
        subB.set(T);
        subC.set(T);

        for( int i = 0; i < T.numCols; i += blockLength ) {
            int widthA = Math.min(blockLength, T.numCols-i);

            subA.col0 = i;           subA.col1 = i+widthA;
            subA.row0 = subA.col0;   subA.row1 = subA.col1;

            subB.col0 = i;           subB.col1 = i+widthA;
            subB.row0 = i+widthA;    subB.row1 = T.numRows;

            subC.col0 = i+widthA;    subC.col1 = T.numRows;
            subC.row0 = i+widthA;    subC.row1 = T.numRows;
            
            // cholesky on inner block A
            if( !InnerCholesky_B64.lower(subA))
                return false;

            // on the last block these operations are not needed.
            if( widthA == blockLength ) {
                // B = L^-1 B
                TriangularSolver_B64.solveBlock(blockLength,false,subA,subB,false,true);

                // C = C - B * B^T
                InnerRankUpdate_B64.symmRankNMinus_L(blockLength,subC,subB);
            }
        }

        MatrixOps_B64.zeroTriangle(true,T);

        return true;
    }


    private boolean decomposeUpper() {
        int blockLength = T.blockLength;

        subA.set(T);
        subB.set(T);
        subC.set(T);

        for( int i = 0; i < T.numCols; i += blockLength ) {
            int widthA = Math.min(blockLength, T.numCols-i);

            subA.col0 = i;          subA.col1 = i+widthA;
            subA.row0 = subA.col0;  subA.row1 = subA.col1;

            subB.col0 = i+widthA;   subB.col1 = T.numCols;
            subB.row0 = i;          subB.row1 = i+widthA;

            subC.col0 = i+widthA;   subC.col1 = T.numCols;
            subC.row0 = i+widthA;   subC.row1 = T.numCols;

            // cholesky on inner block A
            if( !InnerCholesky_B64.upper(subA))
                return false;

            // on the last block these operations are not needed.
            if( widthA == blockLength ) {
                // B = U^-1 B
                TriangularSolver_B64.solveBlock(blockLength,true,subA,subB,true,false);

                // C = C - B^T * B
                InnerRankUpdate_B64.symmRankNMinus_U(blockLength,subC,subB);
            }
        }

        MatrixOps_B64.zeroTriangle(false,T);

        return true;
    }

    @Override
    public boolean isLower() {
        return lower;
    }

    @Override
    public DMatrixBlock_F64 getT(DMatrixBlock_F64 T) {
        if( T == null )
            return this.T;
        T.set(this.T);

        return T;
    }

    @Override
    public Complex_F64 computeDeterminant() {
        double prod = 1.0;

        int blockLength = T.blockLength;
        for( int i = 0; i < T.numCols; i += blockLength ) {
            // width of the submatrix
            int widthA = Math.min(blockLength, T.numCols-i);

            // index of the first element in the block
            int indexT = i*T.numCols + i*widthA;

            // product along the diagonal
            for (int j = 0; j < widthA; j++) {
                prod *= T.data[indexT];
                indexT += widthA+1;
            }
        }

        det.real = prod*prod;
        det.imaginary = 0;

        return det;
    }

    @Override
    public boolean inputModified() {
        return true;
    }
}
