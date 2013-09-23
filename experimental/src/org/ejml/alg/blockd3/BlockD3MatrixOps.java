/*
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

/*
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

package org.ejml.alg.blockd3;

import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.BlockD3Matrix64F;
import org.ejml.data.DenseMatrix64F;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BlockD3MatrixOps {

    public static BlockD3Matrix64F convert( DenseMatrix64F src , int blockLength )
    {
        BlockD3Matrix64F ret = new BlockD3Matrix64F(src.numRows,src.numCols,blockLength);

        convert(src,ret);

        return ret;
    }

    public static BlockD3Matrix64F convert( DenseMatrix64F src )
    {
        BlockD3Matrix64F ret = new BlockD3Matrix64F(src.numRows,src.numCols);

        convert(src,ret);

        return ret;
    }

    public static void convert( DenseMatrix64F src , BlockD3Matrix64F dst )
    {
        if( src.numRows != dst.numRows || src.numCols != dst.numCols )
            throw new IllegalArgumentException("Must be the same size.");

        for( int i = 0; i < dst.numRows; i += dst.blockLength ) {
            int blockHeight = Math.min( dst.blockLength , dst.numRows - i);

            for( int j = 0; j < dst.numCols; j += dst.blockLength ) {
                int blockWidth = Math.min( dst.blockLength , dst.numCols - j);

                int indexSrcRow = i*dst.numCols + j;

                double block[] = dst.blocks[ i / dst.blockLength][ j/dst.blockLength];
                int indexDstRow = 0;

                for( int k = 0; k < blockHeight; k++ ) {
                    int indexSrc = indexSrcRow;
                    int end = indexSrc + blockWidth;
                    int indexDst = indexDstRow;
                    for( ;indexSrc != end; ) {
                        block[indexDst++] = src.data[indexSrc++];
                    }
                    indexSrcRow += dst.numCols;
                    indexDstRow += dst.blockLength;
                }
            }
        }
    }

    public static void convert( BlockD3Matrix64F src , DenseMatrix64F dst )
    {
        if( dst.numRows != src.numRows || dst.numCols != src.numCols )
            throw new IllegalArgumentException("Must be the same size.");

        for( int i = 0; i < src.numRows; i += src.blockLength ) {
            int blockHeight = Math.min( src.blockLength , src.numRows - i);

            for( int j = 0; j < src.numCols; j += src.blockLength ) {
                int blockWidth = Math.min( src.blockLength , src.numCols - j);

                int indexSrcRow = 0;
                int indexDstRow = i*dst.numCols + j;

                double block[] = src.blocks[ i / src.blockLength][ j/src.blockLength];

                for( int k = 0; k < blockHeight; k++ ) {
                    int indexDst = indexDstRow;
                    int indexSrc = indexSrcRow;
                    int end = indexSrc + blockWidth;
                    for( ;indexSrc != end; ) {
                        dst.data[indexDst++] = block[indexSrc++];
                    }
                    indexDstRow += dst.numCols;
                    indexSrcRow += src.blockLength;
                }
            }
        }
    }

    public static BlockD3Matrix64F random( int numRows , int numCols ,
                                           double min , double max ,
                                           Random rand , int blockLength )
    {
        BlockD3Matrix64F ret = new BlockD3Matrix64F(numRows,numCols,blockLength);

        GenericMatrixOps.setRandom(ret,min,max,rand);

        return ret;
    }


    public static void mult( BlockD3Matrix64F A , BlockD3Matrix64F B , BlockD3Matrix64F C )
    {
        if( A.numCols != B.numRows )
            throw new IllegalArgumentException("Rows in A are incompatible with columns in B");
        if( A.numRows != C.numRows )
            throw new IllegalArgumentException("Rows in A are incompatible with rows in C");
        if( B.numCols != C.numCols )
            throw new IllegalArgumentException("Columns in B are incompatible with columns in C");
        if( A.blockLength != B.blockLength || A.blockLength != C.blockLength )
            throw new IllegalArgumentException("Block lengths are not all the same.");

        final int blockLength = A.blockLength;

        for( int i = 0; i < A.numRows; i += blockLength ) {
            int heightA = Math.min( blockLength , A.numRows - i);
            int blockI = i/blockLength;

            for( int j = 0; j < B.numCols; j += blockLength ) {
                int widthB = Math.min( blockLength , B.numCols-j);
                int blockJ = j/blockLength;

                double blockC[] = C.blocks[ blockI ][ blockJ ];

                for( int k = 0; k < A.numCols; k += blockLength ) {
                    int widthA = Math.min( blockLength , A.numCols - k);
                    int blockK = k/blockLength;

                    double blockA[] = A.blocks[ blockI ][ blockK ];
                    double blockB[] = B.blocks[ blockK ][ blockJ ];

                    // by inlining these functions it can be made to be comparable (but slightly
                    // slow than) mult_reorder().  Now its about 1.5x slower
                    if( k == 0 )
                        multBlockSet(blockA,blockB,blockC,heightA,widthA,widthB,blockLength);
                    else
                        multBlockAdd(blockA,blockB,blockC,heightA,widthA,widthB,blockLength);
                }
            }
        }
    }

    /**
     * Performs a matrix multiplication between inner block matrices.
     *
     * (m , o) += (m , n) * (n , o)
     */
    private static void multBlockAdd( double []blockA, double []blockB, double []blockC,
                                     final int m, final int n, final int o,
                                     final int blockLength ) {
//        for( int i = 0; i < m; i++ ) {
//            for( int j = 0; j < o; j++ ) {
//                double val = 0;
//                for( int k = 0; k < n; k++ ) {
//                    val += blockA[ i*blockLength + k]*blockB[ k*blockLength + j];
//                }
//
//                blockC[ i*blockLength + j] += val;
//            }
//        }

//        int rowA = 0;
//        for( int i = 0; i < m; i++ , rowA += blockLength) {
//            for( int j = 0; j < o; j++ ) {
//                double val = 0;
//                int indexB = j;
//                int indexA = rowA;
//                int end = indexA + n;
//                for( ; indexA != end; indexA++ , indexB += blockLength ) {
//                    val += blockA[ indexA ]*blockB[ indexB ];
//                }
//
//                blockC[ rowA + j] += val;
//            }
//        }

//        for( int k = 0; k < n; k++ ) {
//            for( int i = 0; i < m; i++ ) {
//                for( int j = 0; j < o; j++ ) {
//                    blockC[ i*blockLength + j] += blockA[ i*blockLength + k]*blockB[ k*blockLength + j];
//                }
//            }
//        }

        for( int k = 0; k < n; k++ ) {
            int rowB = k*blockLength;
            int endB = rowB+o;
            for( int i = 0; i < m; i++ ) {
                int indexC = i*blockLength;
                double valA = blockA[ indexC + k];
                int indexB = rowB;
                
                while( indexB != endB ) {
                    blockC[ indexC++ ] += valA*blockB[ indexB++];
                }
            }
        }
    }

    private static void multBlockSet( double []blockA, double []blockB, double []blockC,
                                     final int m, final int n, final int o,
                                     final int blockLength ) {
        int rowA = 0;
        for( int i = 0; i < m; i++ , rowA += blockLength) {
            for( int j = 0; j < o; j++ ) {
                double val = 0;
                int indexB = j;
                int indexA = rowA;
                int end = indexA + n;
                for( ; indexA != end; indexA++ , indexB += blockLength ) {
                    val += blockA[ indexA ]*blockB[ indexB ];
                }

                blockC[ rowA + j] = val;
            }
        }
    }
}
