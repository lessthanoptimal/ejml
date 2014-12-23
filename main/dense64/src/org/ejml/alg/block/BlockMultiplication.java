/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.block;

import org.ejml.data.D1Submatrix64F;

import static org.ejml.alg.block.BlockInnerMultiplication.*;

/**
 * <p>
 * Matrix multiplication for {@link org.ejml.data.BlockMatrix64F}.  All sub-matrices must be
 * block aligned.
 * </p>
 * 
 * @author Peter Abeles
 */
public class BlockMultiplication {

    /**
     * <p>
     * Performs a matrix multiplication on {@link org.ejml.data.BlockMatrix64F} submatrices.<br>
     * <br>
     * c = a * b <br>
     * <br>
     * </p>
     *
     * <p>
     * It is assumed that all submatrices start at the beginning of a block and end at the end of a block.
     * </p>
     *
     * @param blockLength Size of the blocks in the submatrix.
     * @param A A submatrix.  Not modified.
     * @param B A submatrix.  Not modified.
     * @param C Result of the operation.  Modified,
     */
    public static void mult( int blockLength ,
                             D1Submatrix64F A , D1Submatrix64F B ,
                             D1Submatrix64F C )
    {
        for( int i = A.row0; i < A.row1; i += blockLength ) {
            int heightA = Math.min( blockLength , A.row1 - i );

            for( int j = B.col0; j < B.col1; j += blockLength ) {
                int widthB = Math.min( blockLength , B.col1 - j );

                int indexC = (i-A.row0+C.row0)*C.original.numCols + (j-B.col0+C.col0)*heightA;

                for( int k = A.col0; k < A.col1; k += blockLength ) {
                    int widthA = Math.min( blockLength , A.col1 - k );

                    int indexA = i*A.original.numCols + k*heightA;
                    int indexB = (k-A.col0+B.row0)*B.original.numCols + j*widthA;

                    if( k == A.col0 )
                        blockMultSet(A.original.data,B.original.data,C.original.data,
                                indexA,indexB,indexC,heightA,widthA,widthB);
                    else
                        blockMultPlus(A.original.data,B.original.data,C.original.data,
                                indexA,indexB,indexC,heightA,widthA,widthB);
                }
            }
        }
    }

    /**
     * <p>
     * Performs a matrix multiplication on {@link org.ejml.data.BlockMatrix64F} submatrices.<br>
     * <br>
     * c = c + a * b <br>
     * <br>
     * </p>
     *
     * <p>
     * It is assumed that all submatrices start at the beginning of a block and end at the end of a block.
     * </p>
     *
     * @param blockLength Size of the blocks in the submatrix.
     * @param A A submatrix.  Not modified.
     * @param B A submatrix.  Not modified.
     * @param C Result of the operation.  Modified,
     */
    public static void multPlus( int blockLength ,
                                D1Submatrix64F A , D1Submatrix64F B ,
                                D1Submatrix64F C )
    {
//        checkInput( blockLength,A,B,C);

        for( int i = A.row0; i < A.row1; i += blockLength ) {
            int heightA = Math.min( blockLength , A.row1 - i );

            for( int j = B.col0; j < B.col1; j += blockLength ) {
                int widthB = Math.min( blockLength , B.col1 - j );

                int indexC = (i-A.row0+C.row0)*C.original.numCols + (j-B.col0+C.col0)*heightA;

                for( int k = A.col0; k < A.col1; k += blockLength ) {
                    int widthA = Math.min( blockLength , A.col1 - k );

                    int indexA = i*A.original.numCols + k*heightA;
                    int indexB = (k-A.col0+B.row0)*B.original.numCols + j*widthA;

                    blockMultPlus(A.original.data,B.original.data,C.original.data,
                            indexA,indexB,indexC,heightA,widthA,widthB);
                }
            }
        }
    }

    /**
     * <p>
     * Performs a matrix multiplication on {@link org.ejml.data.BlockMatrix64F} submatrices.<br>
     * <br>
     * c = c - a * b <br>
     * <br>
     * </p>
     *
     * <p>
     * It is assumed that all submatrices start at the beginning of a block and end at the end of a block.
     * </p>
     *
     * @param blockLength Size of the blocks in the submatrix.
     * @param A A submatrix.  Not modified.
     * @param B A submatrix.  Not modified.
     * @param C Result of the operation.  Modified,
     */
    public static void multMinus( int blockLength ,
                                  D1Submatrix64F A , D1Submatrix64F B ,
                                  D1Submatrix64F C )
    {
        checkInput( blockLength,A,B,C);

        for( int i = A.row0; i < A.row1; i += blockLength ) {
            int heightA = Math.min( blockLength , A.row1 - i );

            for( int j = B.col0; j < B.col1; j += blockLength ) {
                int widthB = Math.min( blockLength , B.col1 - j );

                int indexC = (i-A.row0+C.row0)*C.original.numCols + (j-B.col0+C.col0)*heightA;

                for( int k = A.col0; k < A.col1; k += blockLength ) {
                    int widthA = Math.min( blockLength , A.col1 - k );

                    int indexA = i*A.original.numCols + k*heightA;
                    int indexB = (k-A.col0+B.row0)*B.original.numCols + j*widthA;

                    blockMultMinus(A.original.data,B.original.data,C.original.data,
                            indexA,indexB,indexC,heightA,widthA,widthB);
                }
            }
        }
    }

    private static void checkInput( int blockLength ,
                                    D1Submatrix64F A , D1Submatrix64F B ,
                                    D1Submatrix64F C )
    {
        int Arow = A.getRows();int Acol = A.getCols();
        int Brow = B.getRows();int Bcol = B.getCols();
        int Crow = C.getRows();int Ccol = C.getCols();

        if( Arow != Crow )
            throw new RuntimeException("Mismatch A and C rows");
        if( Bcol != Ccol )
            throw new RuntimeException("Mismatch B and C columns");
        if( Acol != Brow )
            throw new RuntimeException("Mismatch A columns and B rows");

        if( !BlockMatrixOps.blockAligned(blockLength,A))
            throw new RuntimeException("Sub-Matrix A is not block aligned");

        if( !BlockMatrixOps.blockAligned(blockLength,B))
            throw new RuntimeException("Sub-Matrix B is not block aligned");

        if( !BlockMatrixOps.blockAligned(blockLength,C))
            throw new RuntimeException("Sub-Matrix C is not block aligned");
    }

    /**
     * <p>
     * Performs a matrix multiplication with a transpose on {@link org.ejml.data.BlockMatrix64F} submatrices.<br>
     * <br>
     * c = a<sup>T</sup> * b <br>
     * <br>
     * </p>
     *
     * <p>
     * It is assumed that all submatrices start at the beginning of a block and end at the end of a block.
     * </p>
     *
     * @param blockLength Size of the blocks in the submatrix.
     * @param A A submatrix.  Not modified.
     * @param B A submatrix.  Not modified.
     * @param C Result of the operation.  Modified,
     */
    public static void multTransA( int blockLength ,
                                   D1Submatrix64F A , D1Submatrix64F B ,
                                   D1Submatrix64F C )
    {
        for( int i = A.col0; i < A.col1; i += blockLength ) {
            int widthA = Math.min( blockLength , A.col1 - i );

            for( int j = B.col0; j < B.col1; j += blockLength ) {
                int widthB = Math.min( blockLength , B.col1 - j );

                int indexC = (i-A.col0+C.row0)*C.original.numCols + (j-B.col0+C.col0)*widthA;

                for( int k = A.row0; k < A.row1; k += blockLength ) {
                    int heightA = Math.min( blockLength , A.row1 - k );

                    int indexA = k*A.original.numCols + i*heightA;
                    int indexB = (k-A.row0+B.row0)*B.original.numCols + j*heightA;

                    if( k == A.row0 )
                        blockMultSetTransA(A.original.data,B.original.data,C.original.data,
                                indexA,indexB,indexC,heightA,widthA,widthB);
                    else
                        blockMultPlusTransA(A.original.data,B.original.data,C.original.data,
                                indexA,indexB,indexC,heightA,widthA,widthB);
                }
            }
        }
    }

    public static void multPlusTransA( int blockLength ,
                                       D1Submatrix64F A , D1Submatrix64F B ,
                                       D1Submatrix64F C )
    {
        for( int i = A.col0; i < A.col1; i += blockLength ) {
            int widthA = Math.min( blockLength , A.col1 - i );

            for( int j = B.col0; j < B.col1; j += blockLength ) {
                int widthB = Math.min( blockLength , B.col1 - j );

                int indexC = (i-A.col0+C.row0)*C.original.numCols + (j-B.col0+C.col0)*widthA;

                for( int k = A.row0; k < A.row1; k += blockLength ) {
                    int heightA = Math.min( blockLength , A.row1 - k );

                    int indexA = k*A.original.numCols + i*heightA;
                    int indexB = (k-A.row0+B.row0)*B.original.numCols + j*heightA;

                    blockMultPlusTransA(A.original.data,B.original.data,C.original.data,
                            indexA,indexB,indexC,heightA,widthA,widthB);
                }
            }
        }
    }

    public static void multMinusTransA( int blockLength ,
                                        D1Submatrix64F A , D1Submatrix64F B ,
                                        D1Submatrix64F C )
    {
        for( int i = A.col0; i < A.col1; i += blockLength ) {
            int widthA = Math.min( blockLength , A.col1 - i );

            for( int j = B.col0; j < B.col1; j += blockLength ) {
                int widthB = Math.min( blockLength , B.col1 - j );

                int indexC = (i-A.col0+C.row0)*C.original.numCols + (j-B.col0+C.col0)*widthA;

                for( int k = A.row0; k < A.row1; k += blockLength ) {
                    int heightA = Math.min( blockLength , A.row1 - k );

                    int indexA = k*A.original.numCols + i*heightA;
                    int indexB = (k-A.row0+B.row0)*B.original.numCols + j*heightA;

                    blockMultMinusTransA(A.original.data,B.original.data,C.original.data,
                            indexA,indexB,indexC,heightA,widthA,widthB);

                }
            }
        }
    }

    /**
     * <p>
     * Performs a matrix multiplication with a transpose on {@link org.ejml.data.BlockMatrix64F} submatrices.<br>
     * <br>
     * c = a * b <sup>T</sup> <br>
     * <br>
     * </p>
     *
     * <p>
     * It is assumed that all submatrices start at the beginning of a block and end at the end of a block.
     * </p>
     *
     * @param blockLength Length of the blocks in the submatrix.
     * @param A A submatrix.  Not modified.
     * @param B A submatrix.  Not modified.
     * @param C Result of the operation.  Modified,
     */
    public static void multTransB( int blockLength ,
                                   D1Submatrix64F A , D1Submatrix64F B ,
                                   D1Submatrix64F C )
    {
        for( int i = A.row0; i < A.row1; i += blockLength ) {
            int heightA = Math.min( blockLength , A.row1 - i );

            for( int j = B.row0; j < B.row1; j += blockLength ) {
                int widthC = Math.min( blockLength , B.row1 - j );

                int indexC = (i-A.row0+C.row0)*C.original.numCols + (j-B.row0+C.col0)*heightA;

                for( int k = A.col0; k < A.col1; k += blockLength ) {
                    int widthA = Math.min( blockLength , A.col1 - k );

                    int indexA = i*A.original.numCols + k*heightA;
                    int indexB = j*B.original.numCols + (k-A.col0+B.col0)*widthC;

                    if( k == A.col0 )
                        blockMultSetTransB(A.original.data,B.original.data,C.original.data,
                                indexA,indexB,indexC,heightA,widthA,widthC);
                    else
                        blockMultPlusTransB(A.original.data,B.original.data,C.original.data,
                                indexA,indexB,indexC,heightA,widthA,widthC);
                }
            }
        }
    }

}
