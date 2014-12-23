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

import static org.ejml.alg.block.BlockInnerMultiplication.blockMultMinus;


/**
 * <p>
 * Contains triangular solvers for {@link org.ejml.data.BlockMatrix64F} block aligned sub-matrices.
 * </p>
 *
 * <p>
 * For a more detailed description of a similar algorithm see:
 * Page 30 in "Fundamentals of Matrix Computations" 2nd Ed. by David S. Watkins
 * or any description of a block triangular solver in any other computational linear algebra book.
 * </p>
 *
 * @author Peter Abeles
 */
public class BlockTriangularSolver {

    /**
     * Inverts an upper or lower triangular block submatrix.
     *
     * @param blockLength
     * @param upper Is it upper or lower triangular.
     * @param T Triangular matrix that is to be inverted.  Must be block aligned.  Not Modified.
     * @param T_inv Where the inverse is stored.  This can be the same as T.  Modified.
     * @param temp Work space variable that is size blockLength*blockLength.  
     */
    public static void invert( final int blockLength ,
                               final boolean upper ,
                               final D1Submatrix64F T ,
                               final D1Submatrix64F T_inv ,
                               final double temp[] )
    {
        if( upper )
            throw new IllegalArgumentException("Upper triangular matrices not supported yet");

        if( temp.length < blockLength*blockLength )
            throw new IllegalArgumentException("Temp must be at least blockLength*blockLength long.");

        if( T.row0 != T_inv.row0 || T.row1 != T_inv.row1 || T.col0 != T_inv.col0 || T.col1 != T_inv.col1)
            throw new IllegalArgumentException("T and T_inv must be at the same elements in the matrix");

        final int M = T.row1-T.row0;

        final double dataT[] = T.original.data;
        final double dataX[] = T_inv.original.data;

        final int offsetT = T.row0*T.original.numCols+M*T.col0;

        for( int i = 0; i < M; i += blockLength ) {
            int heightT = Math.min(T.row1-(i+T.row0),blockLength);

            int indexII = offsetT + T.original.numCols*(i+T.row0) + heightT*(i+T.col0);

            for( int j = 0; j < i; j += blockLength ) {
                int widthX = Math.min(T.col1-(j+T.col0),blockLength);

                for( int w = 0; w < temp.length; w++ ) {
                    temp[w] = 0;
                }

                for( int k = j; k < i; k += blockLength ) {
                    int widthT = Math.min(T.col1-(k+T.col0),blockLength);

                    int indexL = offsetT + T.original.numCols*(i+T.row0) + heightT*(k+T.col0);
                    int indexX = offsetT + T.original.numCols*(k+T.row0) + widthT*(j+T.col0);

                    blockMultMinus(dataT,dataX,temp,indexL,indexX,0,heightT,widthT,widthX);
                }

                int indexX = offsetT + T.original.numCols*(i+T.row0) + heightT*(j+T.col0);

                BlockInnerTriangularSolver.solveL(dataT,temp,heightT,widthX,heightT,indexII,0);
                System.arraycopy(temp,0,dataX,indexX,widthX*heightT);
            }
            BlockInnerTriangularSolver.invertLower(dataT,dataX,heightT,indexII,indexII);
        }
    }

    /**
     * Inverts an upper or lower triangular block submatrix.
     *
     * @param blockLength
     * @param upper Is it upper or lower triangular.
     * @param T Triangular matrix that is to be inverted.  Overwritten with solution.  Modified.
     * @param temp Work space variable that is size blockLength*blockLength.
     */
    public static void invert( final int blockLength ,
                               final boolean upper ,
                               final D1Submatrix64F T ,
                               final double temp[] )
    {
        if( upper )
            throw new IllegalArgumentException("Upper triangular matrices not supported yet");

        if( temp.length < blockLength*blockLength )
            throw new IllegalArgumentException("Temp must be at least blockLength*blockLength long.");

        final int M = T.row1-T.row0;

        final double dataT[] = T.original.data;
        final int offsetT = T.row0*T.original.numCols+M*T.col0;

        for( int i = 0; i < M; i += blockLength ) {
            int heightT = Math.min(T.row1-(i+T.row0),blockLength);

            int indexII = offsetT + T.original.numCols*(i+T.row0) + heightT*(i+T.col0);

            for( int j = 0; j < i; j += blockLength ) {
                int widthX = Math.min(T.col1-(j+T.col0),blockLength);

                for( int w = 0; w < temp.length; w++ ) {
                    temp[w] = 0;
                }

                for( int k = j; k < i; k += blockLength ) {
                    int widthT = Math.min(T.col1-(k+T.col0),blockLength);

                    int indexL = offsetT + T.original.numCols*(i+T.row0) + heightT*(k+T.col0);
                    int indexX = offsetT + T.original.numCols*(k+T.row0) + widthT*(j+T.col0);

                    blockMultMinus(dataT,dataT,temp,indexL,indexX,0,heightT,widthT,widthX);
                }

                int indexX = offsetT + T.original.numCols*(i+T.row0) + heightT*(j+T.col0);

                BlockInnerTriangularSolver.solveL(dataT,temp,heightT,widthX,heightT,indexII,0);
                System.arraycopy(temp,0,dataT,indexX,widthX*heightT);
            }
            BlockInnerTriangularSolver.invertLower(dataT,heightT,indexII);
        }
    }


    /**
     * <p>
     * Performs an in-place solve operation on the provided block aligned sub-matrices.<br>
     * <br>
     * B = T<sup>-1</sup> B<br>
     * <br>
     * where T is a triangular matrix. T or B can be transposed.  T is a square matrix of arbitrary
     * size and B has the same number of rows as T and an arbitrary number of columns.
     * </p>
     *
     * @param blockLength Size of the inner blocks.
     * @param upper If T is upper or lower triangular.
     * @param T An upper or lower triangular matrix. Not modified.
     * @param B A matrix whose height is the same as T's width. Solution is written here. Modified.
     */
    public static void solve( final int blockLength ,
                              final boolean upper ,
                              final D1Submatrix64F T ,
                              final D1Submatrix64F B ,
                              final boolean transT ) {

        if( upper ) {
            solveR(blockLength,T,B,transT);
        } else {
            solveL(blockLength,T,B,transT);
        }
    }


    /**
     * <p>
     * Performs an in-place solve operation where T is contained in a single block.<br>
     * <br>
     * B = T<sup>-1</sup> B<br>
     * <br>
     * where T is a triangular matrix contained in an inner block. T or B can be transposed.  T must be a single complete inner block
     * and B is either a column block vector or row block vector.
     * </p>
     *
     * @param blockLength Size of the inner blocks in the block matrix.
     * @param upper If T is upper or lower triangular.
     * @param T An upper or lower triangular matrix that is contained in an inner block. Not modified.
     * @param B A block aligned row or column submatrix. Modified.
     * @param transT If T is transposed or not.
     * @param transB If B is transposed or not.
     */
    public static void solveBlock( final int blockLength ,
                                   final boolean upper , final D1Submatrix64F T ,
                                   final D1Submatrix64F B ,
                                   final boolean transT ,final boolean transB )
    {
        int Trows = T.row1-T.row0;
        if( Trows > blockLength )
            throw new IllegalArgumentException("T can be at most the size of a block");
        // number of rows in a block.  The submatrix can be smaller than a block
        final int blockT_rows = Math.min(blockLength,T.original.numRows-T.row0);
        final int blockT_cols = Math.min(blockLength,T.original.numCols-T.col0);

        int offsetT = T.row0*T.original.numCols+blockT_rows*T.col0;

        final double dataT[] = T.original.data;
        final double dataB[] = B.original.data;

        if( transB ) {
            if( upper ) {
                if ( transT ) {
                    throw new IllegalArgumentException("Operation not yet supported");
                } else {
                    throw new IllegalArgumentException("Operation not yet supported");
                }
            } else {
                if ( transT ) {
                    throw new IllegalArgumentException("Operation not yet supported");
                } else {
                    for( int i = B.row0; i < B.row1; i += blockLength ) {
                        int N = Math.min(B.row1 , i + blockLength ) - i;

                        int offsetB = i*B.original.numCols + N*B.col0;

                        BlockInnerTriangularSolver.solveLTransB(dataT,dataB,blockT_rows,N,blockT_rows,offsetT,offsetB);
                    }
                }
            }
        } else {
            if( Trows != B.row1-B.row0 )
                throw new IllegalArgumentException("T and B must have the same number of rows.");

            if( upper ) {
                if ( transT ) {
                    for( int i = B.col0; i < B.col1; i += blockLength ) {
                        int offsetB = B.row0*B.original.numCols + Trows*i;

                        int N = Math.min(B.col1 , i + blockLength ) - i;
                        BlockInnerTriangularSolver.solveTransU(dataT,dataB,Trows,N,Trows,offsetT,offsetB);
                    }
                } else {
                    for( int i = B.col0; i < B.col1; i += blockLength ) {
                        int offsetB = B.row0*B.original.numCols + Trows*i;

                        int N = Math.min(B.col1 , i + blockLength ) - i;
                        BlockInnerTriangularSolver.solveU(dataT,dataB,Trows,N,Trows,offsetT,offsetB);
                    }
                }
            } else {
                if ( transT ) {
                    for( int i = B.col0; i < B.col1; i += blockLength ) {
                        int offsetB = B.row0*B.original.numCols + Trows*i;

                        int N = Math.min(B.col1 , i + blockLength ) - i;
                        BlockInnerTriangularSolver.solveTransL(dataT,dataB,Trows,N,blockT_cols,offsetT,offsetB);
                    }
                } else {
                    for( int i = B.col0; i < B.col1; i += blockLength ) {
                        int offsetB = B.row0*B.original.numCols + Trows*i;

                        int N = Math.min(B.col1 , i + blockLength ) - i;
                        BlockInnerTriangularSolver.solveL(dataT,dataB,Trows,N,blockT_cols,offsetT,offsetB);
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Solves lower triangular systems:<br>
     * <br>
     * B = L<sup>-1</sup> B<br>
     * <br>
     * </p>
     *
     * <p> Reverse or forward substitution is used depending upon L being transposed or not. </p>
     *
     * @param blockLength
     * @param L Lower triangular with dimensions m by m.  Not modified.
     * @param B A matrix with dimensions m by n.  Solution is written into here. Modified.
     * @param transL Is the triangular matrix transposed?
     */
    public static void solveL( final int blockLength ,
                               final D1Submatrix64F L,
                               final D1Submatrix64F B ,
                               boolean transL ) {

        D1Submatrix64F Y = new D1Submatrix64F(B.original);

        D1Submatrix64F Linner = new D1Submatrix64F(L.original);
        D1Submatrix64F Binner = new D1Submatrix64F(B.original);

        int lengthL = B.row1 - B.row0;

        int startI,stepI;

        if( transL ) {
            startI = lengthL - lengthL % blockLength;
            if( startI == lengthL && lengthL >= blockLength )
                startI -= blockLength;

            stepI = -blockLength;
        } else {
            startI = 0;
            stepI = blockLength;
        }

        for( int i = startI; ; i += stepI ) {
            if( transL ) {
                if( i < 0 ) break;
            } else {
                if( i >= lengthL ) break;
            }

            // width and height of the inner T(i,i) block
            int widthT = Math.min(blockLength, lengthL-i);

            Linner.col0 = L.col0 + i;    Linner.col1 = Linner.col0 + widthT;
            Linner.row0 = L.row0 + i;    Linner.row1 = Linner.row0 + widthT;

            Binner.col0 = B.col0;       Binner.col1 = B.col1;
            Binner.row0 = B.row0 + i;   Binner.row1 = Binner.row0 + widthT;

            // solve the top row block
            // B(i,:) = T(i,i)^-1 Y(i,:)
            solveBlock(blockLength,false, Linner,Binner,transL,false);

            boolean updateY;
            if( transL ) {
                updateY = Linner.row0 > 0;
            } else {
                updateY = Linner.row1 < L.row1;
            }
            if( updateY ) {
                // Y[i,:] = Y[i,:] - sum j=1:i-1 { T[i,j] B[j,i] }
                // where i is the next block down
                // The summation is a block inner product
                if( transL ) {
                    Linner.col1 = Linner.col0;
                    Linner.col0 = Linner.col1 - blockLength;
                    Linner.row1 = L.row1;
                    //Tinner.col1 = Tinner.col1;

//                    Binner.row0 = Binner.row0;
                    Binner.row1 = B.row1;

                    Y.row0 = Binner.row0-blockLength;
                    Y.row1 = Binner.row0;
                } else {
                    Linner.row0 = Linner.row1;
                    Linner.row1 = Math.min(Linner.row0+blockLength, L.row1);
                    Linner.col0 = L.col0;
                    //Tinner.col1 = Tinner.col1;

                    Binner.row0 = B.row0;
                    //Binner.row1 = Binner.row1;

                    Y.row0 = Binner.row1;
                    Y.row1 = Math.min(Y.row0+blockLength,B.row1);
                }

                // step through each block column
                for( int k = B.col0; k < B.col1; k += blockLength ) {

                    Binner.col0 = k;
                    Binner.col1 = Math.min(k+blockLength,B.col1);

                    Y.col0 = Binner.col0;
                    Y.col1 = Binner.col1;

                    if( transL ) {
                        // Y = Y - T^T * B
                        BlockMultiplication.multMinusTransA(blockLength, Linner,Binner,Y);
                    } else {

                        // Y = Y - T * B
                        BlockMultiplication.multMinus(blockLength, Linner,Binner,Y);
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Solves upper triangular systems:<br>
     * <br>
     * B = R<sup>-1</sup> B<br>
     * <br>
     * </p>
     *
     * <p>Only the first B.numRows rows in R will be processed.  Lower triangular elements are ignored.<p>
     *
     * <p> Reverse or forward substitution is used depending upon L being transposed or not. </p>
     *
     * @param blockLength
     * @param R Upper triangular with dimensions m by m.  Not modified.
     * @param B A matrix with dimensions m by n.  Solution is written into here. Modified.
     * @param transR Is the triangular matrix transposed?
     */
    public static void solveR( final int blockLength ,
                               final D1Submatrix64F R,
                               final D1Submatrix64F B ,
                               boolean transR ) {

        int lengthR = B.row1 - B.row0;
        if( R.getCols() != lengthR ) {
            throw new IllegalArgumentException("Number of columns in R must be equal to the number of rows in B");
        } else if( R.getRows() != lengthR ) {
            throw new IllegalArgumentException("Number of rows in R must be equal to the number of rows in B");
        }

        D1Submatrix64F Y = new D1Submatrix64F(B.original);

        D1Submatrix64F Rinner = new D1Submatrix64F(R.original);
        D1Submatrix64F Binner = new D1Submatrix64F(B.original);

        int startI,stepI;

        if( transR ) {
            startI = 0;
            stepI = blockLength;
        } else {
            startI = lengthR - lengthR % blockLength;
            if( startI == lengthR && lengthR >= blockLength )
                startI -= blockLength;

            stepI = -blockLength;
        }

        for( int i = startI; ; i += stepI ) {
            if( transR ) {
                if( i >= lengthR ) break;
            } else {
                if( i < 0 ) break;
            }

            // width and height of the inner T(i,i) block
            int widthT = Math.min(blockLength, lengthR-i);

            Rinner.col0 = R.col0 + i;    Rinner.col1 = Rinner.col0 + widthT;
            Rinner.row0 = R.row0 + i;    Rinner.row1 = Rinner.row0 + widthT;

            Binner.col0 = B.col0;       Binner.col1 = B.col1;
            Binner.row0 = B.row0 + i;   Binner.row1 = Binner.row0 + widthT;

            // solve the top row block
            // B(i,:) = T(i,i)^-1 Y(i,:)
            solveBlock(blockLength,true, Rinner,Binner,transR,false);

            boolean updateY;
            if( transR ) {
                updateY = Rinner.row1 < R.row1;
            } else {
                updateY = Rinner.row0 > 0;
            }
            if( updateY ) {
                // Y[i,:] = Y[i,:] - sum j=1:i-1 { T[i,j] B[j,i] }
                // where i is the next block down
                // The summation is a block inner product
                if( transR ) {
                    Rinner.col0 = Rinner.col1;
                    Rinner.col1 = Math.min(Rinner.col0+blockLength, R.col1);
                    Rinner.row0 = R.row0;
                    //Rinner.row1 = Rinner.row1;

                    Binner.row0 = B.row0;
                    //Binner.row1 = Binner.row1;

                    Y.row0 = Binner.row1;
                    Y.row1 = Math.min(Y.row0+blockLength,B.row1);
                } else {
                    Rinner.row1 = Rinner.row0;
                    Rinner.row0 = Rinner.row1 - blockLength;
                    Rinner.col1 = R.col1;

//                    Binner.row0 = Binner.row0;
                    Binner.row1 = B.row1;

                    Y.row0 = Binner.row0-blockLength;
                    Y.row1 = Binner.row0;
                }

                // step through each block column
                for( int k = B.col0; k < B.col1; k += blockLength ) {

                    Binner.col0 = k;
                    Binner.col1 = Math.min(k+blockLength,B.col1);

                    Y.col0 = Binner.col0;
                    Y.col1 = Binner.col1;

                    if( transR ) {
                        // Y = Y - T^T * B
                        BlockMultiplication.multMinusTransA(blockLength, Rinner,Binner,Y);
                    } else {
                        // Y = Y - T * B
                        BlockMultiplication.multMinus(blockLength, Rinner,Binner,Y);
                    }
                }
            }
        }
    }
}
