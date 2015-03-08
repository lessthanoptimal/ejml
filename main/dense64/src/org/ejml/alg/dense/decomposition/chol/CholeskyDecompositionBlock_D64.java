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

package org.ejml.alg.dense.decomposition.chol;

import org.ejml.data.DenseMatrix64F;


/**
 * This is an implementation of Cholesky that processes internal submatrices as blocks.  This is
 * done to reduce the number of cache issues.
 *
 * @author Peter Abeles
 */
public class CholeskyDecompositionBlock_D64 extends CholeskyDecompositionCommon_D64 {

    private int blockWidth; // how wide the blocks should be
    private DenseMatrix64F B; // row rectangular matrix

    private CholeskyBlockHelper_D64 chol;

    /**
     * Creates a CholeksyDecomposition capable of decomposing a matrix that is
     * n by n, where n is the width.
     *
     * @param blockWidth The width of a block.
     */
    public CholeskyDecompositionBlock_D64(int blockWidth) {
        super(true);

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

        chol = new CholeskyBlockHelper_D64(blockWidth);
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
                // apply cholesky to the current block
                if( !chol.decompose(T,(i*blockWidth)* T.numCols + i*blockWidth,blockWidth) )  return false;

                int indexSrc = i*blockWidth* T.numCols + (i+1)*blockWidth;
                int indexDst = (i+1)*blockWidth* T.numCols + i*blockWidth;

                // B = L^(-1) * B
                solveL_special(chol.getL().data, T,indexSrc,indexDst,B);

                int indexL = (i+1)*blockWidth*n + (i+1)*blockWidth;

                // c = c - a^T*a
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
     * This is a variation on the {@link org.ejml.alg.dense.decomposition.TriangularSolver#solveL} function.
     * It grabs the input from the top right row rectangle of the source matrix then writes the results
     * to the lower bottom column rectangle.  The rectangle matrices just matrices are submatrices
     * of the matrix that is being decomposed.  The results are also written to B.
     *
     * @param L A lower triangular matrix.
     * @param b_src matrix with the vectors that are to be solved for
     * @param indexSrc First index of the submatrix where the inputs are coming from.
     * @param indexDst First index of the submatrix where the results are going to.
     * @param B
     */
    public static void solveL_special( final double L[] ,
                                       final DenseMatrix64F b_src,
                                       final int indexSrc , final int indexDst ,
                                       final DenseMatrix64F B )
    {
        final double dataSrc[] = b_src.data;

        final double b[]= B.data;
        final int m = B.numRows;
        final int n = B.numCols;
        final int widthL = m;

//        for( int j = 0; j < n; j++ ) {
//            for( int i = 0; i < widthL; i++ ) {
//                double sum = dataSrc[indexSrc+i*b_src.numCols+j];
//                for( int k=0; k<i; k++ ) {
//                    sum -= L[i*widthL+k]* b[k*n+j];
//                }
//                double val = sum / L[i*widthL+i];
//                dataSrc[indexDst+j*b_src.numCols+i] = val;
//                b[i*n+j] = val;
//            }
//        }

        for( int j = 0; j < n; j++ ) {
            int indexb = j;
            int rowL = 0;
            
            //for( int i = 0; i < widthL; i++
            for( int i = 0; i < widthL; i++ ,  indexb += n, rowL += widthL ) {
                double sum = dataSrc[indexSrc+i*b_src.numCols+j];

                int indexL = rowL;
                int endL = indexL + i;
                int indexB = j;
                //for( int k=0; k<i; k++ ) {
                for( ; indexL != endL; indexB += n) {
                    sum -= L[indexL++]* b[indexB];
                }
                double val = sum / L[i*widthL+i];
                dataSrc[indexDst+j*b_src.numCols+i] = val;
                b[indexb] = val;
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
     * Only the upper triangle is updated.
     *
     * @param a A matrix.
     * @param c A matrix.
     * @param startIndexC start of the submatrix in c.
     */
    public static void symmRankTranA_sub( DenseMatrix64F a , DenseMatrix64F c ,
                                          int startIndexC )
    {
        // TODO update so that it doesn't modify/read parts that it shouldn't
        final double dataA[] = a.data;
        final double dataC[] = c.data;

//        for( int i = 0; i < a.numCols; i++ ) {
//            for( int k = 0; k < a.numRows; k++ ) {
//                double valA = dataA[k*a.numCols+i];
//
//                for( int j = i; j < a.numCols; j++ ) {
//                    dataC[startIndexC+i*c.numCols+j] -= valA * dataA[k*a.numCols+j];
//                }
//            }
//        }

        final int strideC = c.numCols + 1;
        for( int i = 0; i < a.numCols; i++ ) {
            int indexA = i;
            int endR = a.numCols;

            for( int k = 0; k < a.numRows; k++ , indexA += a.numCols , endR += a.numCols) {
                int indexC = startIndexC;
                final double valA = dataA[indexA];
                int indexR = indexA;

                while( indexR < endR ) {
                    dataC[indexC++] -= valA * dataA[indexR++];
                }
            }
            startIndexC += strideC;
        }

    }
}