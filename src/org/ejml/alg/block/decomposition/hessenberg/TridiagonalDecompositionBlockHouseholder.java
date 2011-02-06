/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.block.decomposition.hessenberg;

import org.ejml.alg.dense.decomposition.hessenberg.TridiagonalSimilarDecomposition;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.D1Submatrix64F;
import org.ejml.ops.CommonOps;

import static org.ejml.alg.block.BlockInnerMultiplication.blockMultPlusTransA;


/**
 * <p>
 * Tridiagonal similar decomposition for block matrices.  Orthogonal matrices are computed using
 * householder vectors.
 * </p>
 *
 * <p>
 * Based off algorithm in section 2 of J. J. Dongarra, D. C. Sorensen, S. J. Hammarling,
 * "Block Reduction of Matrices to Condensed Forms for Eigenvalue Computations" Journal of
 * Computations and Applied Mathematics 27 (1989) 215-227<b>
 * <br>
 * Computations of Householder reflectors has been modified from what is presented in that paper to how 
 * it is performed in "Fundamentals of Matrix Computations" 2nd ed. by David S. Watkins.
 * </p>
 *
 * @author Peter Abeles
 */
// TODO take advantage of symmetry more
public class TridiagonalDecompositionBlockHouseholder
        implements TridiagonalSimilarDecomposition<BlockMatrix64F> {

    protected BlockMatrix64F A;
    protected BlockMatrix64F V = new BlockMatrix64F(1,1);
    protected double gammas[] = new double[1];

    @Override
    public BlockMatrix64F getT(BlockMatrix64F T) {
        if( T.numRows != A.numRows || T.numCols != A.numCols )
            throw new IllegalArgumentException("T must have the same dimensions as the input matrix");

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public BlockMatrix64F getQ(BlockMatrix64F Q, boolean transposed) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void getDiagonal(double[] diag, double[] off) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean decompose(BlockMatrix64F orig) {
        if( orig.numCols != orig.numRows )
            throw new IllegalArgumentException("Input matrix must be square.");

        init(orig);

        D1Submatrix64F subA = new D1Submatrix64F(A);
        D1Submatrix64F subV = new D1Submatrix64F(V);
        D1Submatrix64F subU = new D1Submatrix64F(A);

        int N = orig.numCols;

        for( int i = 0; i < N; i += A.blockLength ) {
            int height = Math.min(A.blockLength,A.numRows-i);

            subA.col0 = subU.col0 = i;
            subA.row0 = subU.row0 = i;

            subU.row1 = subU.row0 + height;

            subV.col0 = i;
            subV.row1 = height;

            // bidiagonalize the top row
            CommonOps.set(subV.original,0);
            TridiagonalBlockHelper.tridiagUpperRow(A.blockLength,subA,gammas,subV);

            // apply Householder reflectors to the lower portion using block multiplication

            if( subU.row1 < orig.numCols) {
                // take in account the 1 in the last row.  The others are skipped over.
                double before = subU.get(A.blockLength-1,A.blockLength);
                subU.set(A.blockLength-1,A.blockLength,1);

                // A = A + U*V^T + V*U^T
                multPlusTransA(A.blockLength,subU,subV,subA);
                multPlusTransA(A.blockLength,subV,subU,subA);

                subU.set(A.blockLength-1,A.blockLength,before);
            }
        }

        return true;
    }

    /**
     * C = C + A^T*B
     *
     * @param blockLength
     * @param A row block vector
     * @param B row block vector
     * @param C
     */
    public static void multPlusTransA( int blockLength ,
                                       D1Submatrix64F A , D1Submatrix64F B ,
                                       D1Submatrix64F C )
    {
        // TODO only do upper triangle
        int heightA = Math.min( blockLength , A.row1 - A.row0 );

        for( int i = C.row0+blockLength; i < C.row1; i += blockLength ) {
            int heightC = Math.min( blockLength , C.row1 - i );

            int indexA = A.row0*A.original.numCols + (i-C.row0+A.col0)*heightA;

//            for( int j = i; j < C.col1; j += blockLength ) {
            for( int j = C.col0+blockLength; j < C.col1; j += blockLength ) {
                int widthC = Math.min( blockLength , C.col1 - j );

                int indexC = i*C.original.numCols + j*heightC;
                int indexB = B.row0*B.original.numCols + (j-C.col0+B.col0)*heightA;

                blockMultPlusTransA(A.original.data,B.original.data,C.original.data,
                            indexA,indexB,indexC,heightA,heightC,widthC);
            }
        }
    }

    private void init( BlockMatrix64F orig ) {
        this.A = orig;

        int height = Math.min(A.blockLength,A.numRows);
        V.reshape(height,A.numCols,A.blockLength,false);

        if( gammas.length < A.numCols )
            gammas = new double[ A.numCols ];
    }

    @Override
    public boolean inputModified() {
        return true;
    }
}
