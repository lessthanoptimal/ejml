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

package org.ejml.alg.dense.decomposition.chol;

import org.ejml.data.DenseMatrix64F;


/**
 * This is an implementation of Cholesky that processes internal submatrices as blocks.  This is
 * done to reduce the number of cache issues.
 *
 * @author Peter Abeles
 */
public class CholeskyDecompositionBlock extends CholeskyDecompositionCommon {

    private int blockWidth; // how wide the blocks should be
    private DenseMatrix64F B; // row rectangular matrix

    private CholeskyBlockHelper chol;

    /**
     * Creates a CholeksyDecomposition capable of decomposing a matrix that is
     * n by n, where n is the width.
     *
     * @param decomposeOrig Should it decompose the matrix that is passed in or declare a new one?
     * @param blockWidth The width of a block.
     */
    public CholeskyDecompositionBlock( boolean decomposeOrig , int blockWidth ) {
        super(decomposeOrig,true);

        this.blockWidth = blockWidth;

    }

    /**
     * Declares additional internal data structures.
     */
    @Override
    public void setExpectedMaxSize( int numRows , int numCols ) {
        super.setExpectedMaxSize(numRows,numCols);

        // if the matrix that is being decomposed is smaller than the block we really don't
        // see the B matrix.
        if( numRows < blockWidth)
            B = new DenseMatrix64F(0,0);
        else
            B = new DenseMatrix64F(blockWidth,maxWidth);

        chol = new CholeskyBlockHelper(blockWidth);
    }

    /**
     * <p>
     * Performs Choleksy decomposition on the provided matrix.
     * </p>
     *
     * <p>
     * If the matrix is not positive definite then this function will return
     * false since it can't complete its computations.  Not all errors will be
     * found.
     * </p>
     * @return True if it was able to finish the decomposition.
     */
    @Override
    protected boolean decomposeLower() {

        if( n < blockWidth)
            B.reshape(0,0, false);
        else
            B.reshape(blockWidth,n-blockWidth, false);

        int numBlocks = n / blockWidth;
        int remainder = n % blockWidth;

        if( remainder > 0 ) {
            numBlocks++;
        }

        B.numCols = n;

        for( int i = 0; i < numBlocks; i++ ) {
            B.numCols -= blockWidth;

            if( B.numCols > 0 ) {
                if( !chol.decompose(T,(i*blockWidth)* T.numCols + i*blockWidth,blockWidth) )  return false;

                int indexSrc = i*blockWidth* T.numCols + (i+1)*blockWidth;
                int indexDst = (i+1)*blockWidth* T.numCols + i*blockWidth;

                solveL_special(chol.getL().data, T,indexSrc,indexDst,B);

                int indexL = (i+1)*blockWidth*n + (i+1)*blockWidth;
                symmRankTranA_sub(B, T,indexL);
            } else {
                int width = remainder > 0 ? remainder : blockWidth;
                if( !chol.decompose(T,(i*blockWidth)* T.numCols + i*blockWidth,width) )  return false;
            }
        }


        // zero the top right corner.
        for( int i = 0; i < n; i++ ) {
            for( int j = i+1; j < n; j++ ) {
                t[i*n+j] = 0.0;
            }
        }

        return true;
    }

    @Override
    protected boolean decomposeUpper() {
        throw new RuntimeException("Not implemented.  Do a lower decomposition and transpose it...");
    }

    /**
     * This is a variable on the {@link org.ejml.alg.dense.decomposition.TriangularSolver#solveL} function.
     * It grabs the input from the top right row rectangle of the source matrix then writes the results
     * to the lower bottom column rectangle.  The rectangle matrices just matrices are submatrices
     * of the matrix that is being decomposed.  The results are also writen to B.
     *
     * @param L A lower triangular matrix.
     * @param b_src matrix with the vectors that are to be solved for
     * @param indexSrc First index of the submatrix where the inputs are coming from.
     * @param indexDst First index of the submatrix where the results are going to.
     * @param B
     */
    public static void solveL_special( double L[] ,
                                       DenseMatrix64F b_src,
                                       int indexSrc , int indexDst ,
                                       DenseMatrix64F B )
    {
        double dataSrc[] = b_src.data;

        double b[]= B.data;
        final int m = B.numRows;
        final int n = B.numCols;
        int widthL = m;

        for( int j = 0; j < n; j++ ) {
            for( int i = 0; i < widthL; i++ ) {
                double sum = dataSrc[indexSrc+i*b_src.numCols+j];
                for( int k=0; k<i; k++ ) {
                    sum -= L[i*widthL+k]* b[k*n+j];
                }
                double val = sum / L[i*widthL+i];
                dataSrc[indexDst+j*b_src.numCols+i] = val;
                b[i*n+j] = val;
            }
        }
    }

    /**
     * <p>
     * Performs this operation:<br>
     * <br>
     * c = c - a<sup>T</sup>a <br>
     * where c is a submatrix.
     * </p>
     *
     * @param a A matrix.
     * @param c A matrix.
     * @param startIndexC start of the submatrix in c.
     */
    public static void symmRankTranA_sub( DenseMatrix64F a , DenseMatrix64F c , int startIndexC )
    {
        // todo doesn't only a triangular portion need to be updated?
        double dataA[] = a.data;
        double dataC[] = c.data;

        for( int i = 0; i < a.numCols; i++ ) {
            for( int k = 0; k < a.numRows; k++ ) {
                double valA = dataA[k*a.numCols+i];
                int indexC = startIndexC+i*c.numCols+i;
                int indexR = k*a.numCols+i;
                int end = k*a.numCols + a.numCols;
                for(; indexR < end; ) {
                    dataC[indexC++] -= valA * dataA[indexR++];
                }
//                for( int j = i; j < a.numCols; j++ ) {
//                    dataC[startIndexC+i*c.numCols+j] -= valA * dataA[k*a.numCols+j];
//                }

            }
        }

    }
}