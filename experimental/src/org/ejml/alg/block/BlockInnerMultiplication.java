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

package org.ejml.alg.block;

import org.ejml.data.D1Submatrix64F;

/**
 * Matrix multiplication for the inner blocks inside of a {@link org.ejml.data.BlockMatrix64F}.
 *
 * @author Peter Abeles
 */
// TODO Add a check to make sure the sub matrices are aligned along the blocks and of appropriate dimensions
    // make option if necessary
// TODO optimize the code.  Don't forget to simply comment out the current readable code
public class BlockInnerMultiplication {

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
                        multBlockSet(A.original.data,B.original.data,C.original.data,
                                indexA,indexB,indexC,heightA,widthA,widthB);
                    else
                        multBlockAdd(A.original.data,B.original.data,C.original.data,
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
    public static void multAdd( int blockLength ,
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

                    multBlockAdd(A.original.data,B.original.data,C.original.data,
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

        if( !blockAligned(blockLength,A))
            throw new RuntimeException("Sub-Matrix A is not block aligned");

        if( !blockAligned(blockLength,B))
            throw new RuntimeException("Sub-Matrix B is not block aligned");

        if( !blockAligned(blockLength,C))
            throw new RuntimeException("Sub-Matrix C is not block aligned");
    }

    private static boolean blockAligned( int blockLength , D1Submatrix64F A ) {
        if( A.col0 % blockLength != 0 )
            return false;
        if( A.row0 % blockLength != 0 )
            return false;

        if( A.col1 % blockLength != 0 ) {
            return A.col1 == A.original.numCols;
        }

        if( A.row1 % blockLength != 0 ) {
            return A.row1 == A.original.numRows;
        }

        return true;
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

//                    System.out.println("heightA "+heightA+" widthA "+widthA+" widthB "+widthB);

                    if( k == A.row0 )
                        multTransABlockSet(A.original.data,B.original.data,C.original.data,
                                indexA,indexB,indexC,heightA,widthA,widthB);
                    else
                        multTransABlockAdd(A.original.data,B.original.data,C.original.data,
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
                        multTransBBlockSet(A.original.data,B.original.data,C.original.data,
                                indexA,indexB,indexC,heightA,widthA,widthC);
                    else
                        multTransBBlockAdd(A.original.data,B.original.data,C.original.data,
                                indexA,indexB,indexC,heightA,widthA,widthC);
                }
            }
        }
    }

    /**
     * <p>
     * Performs the follow operation on individual inner blocks:<br>
     * <br>
     * C = A B
     * </p>
     */
    public static void multBlockSet( double[] dataA, double []dataB, double []dataC,
                                        int indexA, int indexB, int indexC,
                                        final int heightA, final int widthA, final int widthC) {
        for( int i = 0; i < heightA; i++ ) {
            for( int j = 0; j < widthC; j++ , indexC++ ) {
                int indexBB = indexB + j;
                int indexAA = indexA;

                double val = 0;

                int end = indexA + widthA;

                for( ; indexAA != end; indexAA++) {
                    val += dataA[ indexAA ] * dataB[indexBB];
                    indexBB += widthC;
                }

                dataC[ indexC ] = val;
            }

            indexA += widthA;
        }
    }

    /**
     * <p>
     * Performs the follow operation on individual inner blocks:<br>
     * <br>
     * C = C + A B
     * </p>
     */
    public static void multBlockAdd( double[] dataA, double []dataB, double []dataC,
                                      int indexA, int indexB, int indexC,
                                      final int heightA, final int widthA, final int widthC) {
//        for( int i = 0; i < heightA; i++ ) {
//            for( int j = 0; j < widthC; j++ ) {
//                double val = 0;
//
//                for( int k = 0; k < widthA; k++ ) {
//                    val += dataA[i*widthA + k + indexA] * dataB[k*widthC + j + indexB];
//                }
//
//                dataC[ i*widthC + j + indexC ] += val;
//            }
//        }

//        for( int i = 0; i < heightA; i++ ) {
//            int endJ = indexB+widthC;
//            int indexB_ = indexB;
//            // or( int j = 0; j < widthC; j++ )
//            for( ; indexB_ != endJ; indexB_++ , indexC++ ) {
//                int indexBB = indexB_;
//                int indexAA = indexA;
//
//                double val = 0;
//
//                int end = indexA + widthA;
//
//                //for( int k = 0; k < widthA; k++ ) {
//                for( ; indexAA != end; indexAA++, indexBB += widthC) {
//                    val += dataA[ indexAA ] * dataB[indexBB];
//                }
//
//                dataC[ indexC ] += val;
//            }
//
//            indexA += widthA;
//        }

//        for( int i = 0; i < heightA; i++ ) {
//            for( int k = 0; k < widthA; k++ ) {
//                for( int j = 0; j < widthC; j++ ) {
//                    dataC[ i*widthC + j + indexC ] += dataA[i*widthA + k + indexA] * dataB[k*widthC + j + indexB];
//                }
//            }
//        }


        int a = indexA;
        for( int i = 0; i < heightA; i++ ) {
            int b = indexB;
            int rowC = i*widthC + indexC;
            int endC = rowC + widthC;
            int endA = a + widthA;

            while( a != endA ) {//for( int k = 0; k < widthA; k++ ) {
                double valA = dataA[a++];

                int c = rowC;

                while( c != endC  ) {//for( int j = 0; j < widthC; j++ ) {
                    dataC[ c++ ] += valA * dataB[ b++ ];
                }
            }
        }
    }

    /**
     * <p>
     * Performs the follow operation on individual inner blocks:<br>
     * <br>
     * C = A <sup>T</sup>B
     * </p>
     */
    protected static void multTransABlockSet( double[] dataA, double []dataB, double []dataC,
                                              int indexA, int indexB, int indexC,
                                              final int heightA, final int widthA, final int widthC) {
        for( int i = 0; i < widthA; i++ ) {
            for( int j = 0; j < widthC; j++ ) {
                double val = 0;

                for( int k = 0; k < heightA; k++ ) {
                    val += dataA[k*widthA + i + indexA] * dataB[k*widthC + j + indexB];
                }

                dataC[ i*widthC + j + indexC ] = val;
            }
        }
    }

    /**
     * <p>
     * Performs the follow operation on individual inner blocks:<br>
     * <br>
     * C = C + A <sup>T</sup>B
     * </p>
     */
    public static void multTransABlockAdd( double[] dataA, double []dataB, double []dataC,
                                              int indexA, int indexB, int indexC,
                                              final int heightA, final int widthA, final int widthC ) {
        for( int i = 0; i < widthA; i++ ) {
            for( int j = 0; j < widthC; j++ ) {
                double val = 0;

                for( int k = 0; k < heightA; k++ ) {
                    val += dataA[k*widthA + i + indexA] * dataB[k*widthC + j + indexB];
                }

                dataC[ i*widthC + j + indexC ] += val;
            }
        }
    }

    /**
     * <p>
     * Performs the follow operation on individual inner blocks:<br>
     * <br>
     * C = C + &alpha; A <sup>T</sup>B
     * </p>
     */
    public static void multTransABlockAdd( double alpha , double[] dataA, double []dataB, double []dataC,
                                              int indexA, int indexB, int indexC,
                                              final int heightA, final int widthA, final int widthC ) {
//        for( int i = 0; i < widthA; i++ ) {
//            for( int j = 0; j < widthC; j++ ) {
//                double val = 0;
//
//                for( int k = 0; k < heightA; k++ ) {
//                    val += dataA[k*widthA + i + indexA] * dataB[k*widthC + j + indexB];
//                }
//
//                dataC[ i*widthC + j + indexC ] += alpha*val;
//            }
//        }

        int endOffset = heightA*widthA;
        for( int i = 0; i < widthA; i++ ) {
            int c = i*widthC + indexC;
            for( int j = 0; j < widthC; j++ ) {
                double val = 0;

                int a = i + indexA;
                int b = j + indexB;
                int end = a + endOffset;
                for( ; a != end; a += widthA , b += widthC ) {
                    val += dataA[a] * dataB[b];
                }

                dataC[ c++ ] += alpha*val;
            }
        }
    }

    /**
     * <p>
     * Performs the follow operation on individual inner blocks:<br>
     * <br>
     * C = A B<sup>T</sup>
     * </p>
     */
    protected static void multTransBBlockSet( double[] dataA, double []dataB, double []dataC,
                                              int indexA, int indexB, int indexC,
                                              final int heightA, final int widthA, final int widthC) {
        for( int i = 0; i < heightA; i++ ) {
            for( int j = 0; j < widthC; j++ ) {
                double val = 0;

                for( int k = 0; k < widthA; k++ ) {
                    val += dataA[i*widthA + k + indexA] * dataB[j*widthA + k + indexB];
                }

                dataC[ i*widthC + j + indexC ] = val;
            }
        }
    }

    /**
     * <p>
     * Performs the follow operation on individual inner blocks:<br>
     * <br>
     * C = C + A B<sup>T</sup>
     * </p>
     */
    protected static void multTransBBlockAdd( double[] dataA, double []dataB, double []dataC,
                                              int indexA, int indexB, int indexC,
                                              final int heightA, final int widthA, final int widthC) {
        for( int i = 0; i < heightA; i++ ) {
            for( int j = 0; j < widthC; j++ ) {
                double val = 0;

                for( int k = 0; k < widthA; k++ ) {
                    val += dataA[i*widthA + k + indexA] * dataB[j*widthA + k + indexB];
                }

                dataC[ i*widthC + j + indexC ] += val;
            }
        }
    }
}
