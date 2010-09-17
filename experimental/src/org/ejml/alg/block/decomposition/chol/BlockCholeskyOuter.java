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

package org.ejml.alg.block.decomposition.chol;

import org.ejml.alg.block.BlockInnerRankUpdate;
import org.ejml.alg.block.BlockInnerTriangularSolver;
import org.ejml.alg.block.decomposition.BlockCholeskyDecomposition;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.D1Submatrix64F;


/**
 * Block Cholesky using outer product form.
 *
 * CITE
 *
 * @author Peter Abeles
 */
// todo implement lower triangular decomposition
public class BlockCholeskyOuter implements BlockCholeskyDecomposition {

    boolean lower = false;
    BlockMatrix64F T;

    @Override
    public boolean decompose(BlockMatrix64F A) {
        if( A.numCols != A.numRows )
            throw new IllegalArgumentException("A must be square");

        this.T = A;

        if( lower )
            throw new IllegalArgumentException("Not implemented yet");
        else
            return decomposeUpper();
    }

    private boolean decomposeUpper() {
        int blockLength = T.blockLength;

        D1Submatrix64F subA = new D1Submatrix64F(T);
        D1Submatrix64F subB = new D1Submatrix64F(T);
        D1Submatrix64F subC = new D1Submatrix64F(T);

        for( int i = 0; i < T.numCols; i += blockLength ) {
            int widthA = Math.min(blockLength, T.numCols-i);

            subA.col0 = i;
            subA.col1 = i+widthA;
            subA.row0 = subA.col0;
            subA.row1 = subA.col1;

            subB.col0 = i+widthA;
            subB.col1 = T.numCols;
            subB.row0 = i;
            subB.row1 = i+widthA;

            subC.col0 = i+widthA;
            subC.col1 = T.numCols;
            subC.row0 = i+widthA;
            subC.row1 = T.numCols;


            // cholesky on inner block A
            if( !BlockInnerCholesky.upper(subA))
                return false;

            // on the last block these operations are not needed.
            if( widthA == blockLength ) {
                // B = U^-1 B
                BlockInnerTriangularSolver.solveTransU(blockLength,subA,subB);

                // C = C - B^T * B
                // TODO take advantage of symmetry
                BlockInnerRankUpdate.symmRankNUpdate_U(blockLength,subC,subB);
            }
        }

        return true;
    }

    @Override
    public boolean isLower() {
        return lower;
    }

    @Override
    public BlockMatrix64F getT() {
        return this.T;
    }

    @Override
    public boolean modifyInput() {
        return true;
    }
}
