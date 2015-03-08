/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

import org.ejml.alg.block.BlockInnerRankUpdate;
import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.block.BlockTriangularSolver;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.Complex64F;
import org.ejml.data.D1Submatrix64F;
import org.ejml.interfaces.decomposition.CholeskyDecomposition;


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
public class CholeskyOuterForm_B64 implements CholeskyDecomposition<BlockMatrix64F> {

    // if it should compute an upper or lower triangular matrix
    private boolean lower = false;
    // The decomposed matrix.
    private BlockMatrix64F T;

    // predeclare local work space
    private D1Submatrix64F subA = new D1Submatrix64F();
    private D1Submatrix64F subB = new D1Submatrix64F();
    private D1Submatrix64F subC = new D1Submatrix64F();

    // storage for the determinant
    private Complex64F det = new Complex64F();

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
    public boolean decompose(BlockMatrix64F A) {
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
                BlockTriangularSolver.solveBlock(blockLength,false,subA,subB,false,true);

                // C = C - B * B^T
                BlockInnerRankUpdate.symmRankNMinus_L(blockLength,subC,subB);
            }
        }

        BlockMatrixOps.zeroTriangle(true,T);

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
                BlockTriangularSolver.solveBlock(blockLength,true,subA,subB,true,false);

                // C = C - B^T * B
                BlockInnerRankUpdate.symmRankNMinus_U(blockLength,subC,subB);
            }
        }

        BlockMatrixOps.zeroTriangle(false,T);

        return true;
    }

    @Override
    public boolean isLower() {
        return lower;
    }

    @Override
    public BlockMatrix64F getT(BlockMatrix64F T) {
        if( T == null )
            return this.T;
        T.set(this.T);

        return T;
    }

    @Override
    public Complex64F computeDeterminant() {
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
