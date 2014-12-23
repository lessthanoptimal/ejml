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

import org.ejml.data.BlockMatrix64F;
import org.ejml.data.D1Submatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.ConvertMatrixType;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Various operations on {@link BlockMatrix64F}.
 *
 * @author Peter Abeles
 */
public class BlockMatrixOps {

    /**
     * Converts a row major matrix into a row major block matrix.
     *
     * @param src Original DenseMatrix64F.  Not modified.
     * @param dst Equivalent BlockMatrix64F. Modified.
     */
    public static void convert( DenseMatrix64F src , BlockMatrix64F dst )
    {
        ConvertMatrixType.convert(src,dst);
    }

    /**
     * <p>
     * Converts matrix data stored is a row major format into a block row major format in place.
     * </p>
     * 
     * @param numRows number of rows in the matrix.
     * @param numCols number of columns in the matrix.
     * @param blockLength Block size in the converted matrix.
     * @param data Matrix data in a row-major format. Modified.
     * @param tmp Temporary data structure that is to be the size of a block row.
     */
    public static void convertRowToBlock( int numRows , int numCols , int blockLength ,
                                          double[] data, double[] tmp )
    {
        int minLength = Math.min( blockLength , numRows ) * numCols;
        if( tmp.length < minLength ) {
            throw new IllegalArgumentException("tmp must be at least "+minLength+" long ");
        }

        for( int i = 0; i < numRows; i += blockLength ) {
            int blockHeight = Math.min( blockLength , numRows - i);

            System.arraycopy(data,i*numCols,tmp,0,blockHeight*numCols);


            for( int j = 0; j < numCols; j += blockLength ) {
                int blockWidth = Math.min( blockLength , numCols - j);

                int indexDst = i*numCols + blockHeight*j;
                int indexSrcRow = j;

                for( int k = 0; k < blockHeight; k++ ) {
                    System.arraycopy(tmp,indexSrcRow,data,indexDst,blockWidth);
                    indexDst += blockWidth;
                    indexSrcRow += numCols;
                }
            }
        }
    }

    /**
     * Converts a row major block matrix into a row major matrix.
     *
     * @param src Original BlockMatrix64F..  Not modified.
     * @param dst Equivalent DenseMatrix64F.  Modified.
     */
    public static DenseMatrix64F convert( BlockMatrix64F src , DenseMatrix64F dst )
    {
        return ConvertMatrixType.convert(src,dst);
    }

    /**
     * <p>
     * Converts matrix data stored is a block row major format into a row major format in place.
     * </p>
     *
     * @param numRows number of rows in the matrix.
     * @param numCols number of columns in the matrix.
     * @param blockLength Block size in the converted matrix.
     * @param data Matrix data in a block row-major format. Modified.
     * @param tmp Temporary data structure that is to be the size of a block row.
     */
    public static void convertBlockToRow( int numRows , int numCols , int blockLength ,
                                          double[] data, double[] tmp )
    {
        int minLength = Math.min( blockLength , numRows ) * numCols;
        if( tmp.length < minLength ) {
            throw new IllegalArgumentException("tmp must be at least "+minLength+" long and not "+tmp.length);
        }

        for( int i = 0; i < numRows; i += blockLength ) {
            int blockHeight = Math.min( blockLength , numRows - i);

            System.arraycopy(data,i*numCols,tmp,0,blockHeight*numCols);

            for( int j = 0; j < numCols; j += blockLength ) {
                int blockWidth = Math.min( blockLength , numCols - j);

                int indexSrc = blockHeight*j;
                int indexDstRow = i*numCols + j;

                for( int k = 0; k < blockHeight; k++ ) {
                    System.arraycopy(tmp,indexSrc,data,indexDstRow,blockWidth);
                    indexSrc += blockWidth;
                    indexDstRow += numCols;
                }
            }
        }
    }

    /**
     * Converts the transpose of a row major matrix into a row major block matrix.
     *
     * @param src Original DenseMatrix64F.  Not modified.
     * @param dst Equivalent BlockMatrix64F. Modified.
     */
    public static void convertTranSrc( DenseMatrix64F src , BlockMatrix64F dst )
    {
        if( src.numRows != dst.numCols || src.numCols != dst.numRows )
            throw new IllegalArgumentException("Incompatible matrix shapes.");

        for( int i = 0; i < dst.numRows; i += dst.blockLength ) {
            int blockHeight = Math.min( dst.blockLength , dst.numRows - i);

            for( int j = 0; j < dst.numCols; j += dst.blockLength ) {
                int blockWidth = Math.min( dst.blockLength , dst.numCols - j);

                int indexDst = i*dst.numCols + blockHeight*j;
                int indexSrc = j*src.numCols + i;

                for( int l = 0; l < blockWidth; l++ ) {
                    int rowSrc = indexSrc + l*src.numCols;
                    int rowDst = indexDst + l;
                    for( int k = 0; k < blockHeight; k++ , rowDst += blockWidth ) {
                        dst.data[ rowDst ] = src.data[rowSrc++];
                    }
                }
            }
        }
    }

    // This can be speed up by inlining the multBlock* calls, reducing number of multiplications
    // and other stuff.  doesn't seem to have any speed advantage over mult_reorder()
    public static void mult( BlockMatrix64F A , BlockMatrix64F B , BlockMatrix64F C )
    {
        if( A.numCols != B.numRows )
            throw new IllegalArgumentException("Columns in A are incompatible with rows in B");
        if( A.numRows != C.numRows )
            throw new IllegalArgumentException("Rows in A are incompatible with rows in C");
        if( B.numCols != C.numCols )
            throw new IllegalArgumentException("Columns in B are incompatible with columns in C");
        if( A.blockLength != B.blockLength || A.blockLength != C.blockLength )
            throw new IllegalArgumentException("Block lengths are not all the same.");

        final int blockLength = A.blockLength;

        D1Submatrix64F Asub = new D1Submatrix64F(A,0, A.numRows, 0, A.numCols);
        D1Submatrix64F Bsub = new D1Submatrix64F(B,0, B.numRows, 0, B.numCols);
        D1Submatrix64F Csub = new D1Submatrix64F(C,0, C.numRows, 0, C.numCols);

        BlockMultiplication.mult(blockLength,Asub,Bsub,Csub);
    }

    public static void multTransA( BlockMatrix64F A , BlockMatrix64F B , BlockMatrix64F C )
    {
        if( A.numRows != B.numRows )
            throw new IllegalArgumentException("Rows in A are incompatible with rows in B");
        if( A.numCols != C.numRows )
            throw new IllegalArgumentException("Columns in A are incompatible with rows in C");
        if( B.numCols != C.numCols )
            throw new IllegalArgumentException("Columns in B are incompatible with columns in C");
        if( A.blockLength != B.blockLength || A.blockLength != C.blockLength )
            throw new IllegalArgumentException("Block lengths are not all the same.");

        final int blockLength = A.blockLength;

        D1Submatrix64F Asub = new D1Submatrix64F(A,0, A.numRows, 0, A.numCols);
        D1Submatrix64F Bsub = new D1Submatrix64F(B,0, B.numRows, 0, B.numCols);
        D1Submatrix64F Csub = new D1Submatrix64F(C,0, C.numRows, 0, C.numCols);

        BlockMultiplication.multTransA(blockLength,Asub,Bsub,Csub);
    }

    public static void multTransB( BlockMatrix64F A , BlockMatrix64F B , BlockMatrix64F C )
    {
        if( A.numCols != B.numCols )
            throw new IllegalArgumentException("Columns in A are incompatible with columns in B");
        if( A.numRows != C.numRows )
            throw new IllegalArgumentException("Rows in A are incompatible with rows in C");
        if( B.numRows != C.numCols )
            throw new IllegalArgumentException("Rows in B are incompatible with columns in C");
        if( A.blockLength != B.blockLength || A.blockLength != C.blockLength )
            throw new IllegalArgumentException("Block lengths are not all the same.");

        final int blockLength = A.blockLength;

        D1Submatrix64F Asub = new D1Submatrix64F(A,0, A.numRows, 0, A.numCols);
        D1Submatrix64F Bsub = new D1Submatrix64F(B,0, B.numRows, 0, B.numCols);
        D1Submatrix64F Csub = new D1Submatrix64F(C,0, C.numRows, 0, C.numCols);

        BlockMultiplication.multTransB(blockLength,Asub,Bsub,Csub);
    }

    /**
     * Transposes a block matrix.
     *
     * @param A Original matrix.  Not modified.
     * @param A_tran Transposed matrix.  Modified.
     */
    public static BlockMatrix64F transpose( BlockMatrix64F A , BlockMatrix64F A_tran )
    {
        if( A_tran != null ) {
            if( A.numRows != A_tran.numCols || A.numCols != A_tran.numRows )
                throw new IllegalArgumentException("Incompatible dimensions.");
            if( A.blockLength != A_tran.blockLength )
                throw new IllegalArgumentException("Incompatible block size.");
        } else {
            A_tran = new BlockMatrix64F(A.numCols,A.numRows,A.blockLength);

        }

        for( int i = 0; i < A.numRows; i += A.blockLength ) {
            int blockHeight = Math.min( A.blockLength , A.numRows - i);

            for( int j = 0; j < A.numCols; j += A.blockLength ) {
                int blockWidth = Math.min( A.blockLength , A.numCols - j);

                int indexA = i*A.numCols + blockHeight*j;
                int indexC = j*A_tran.numCols + blockWidth*i;

                transposeBlock( A , A_tran , indexA , indexC , blockWidth , blockHeight );
            }
        }

        return A_tran;
    }

    /**
     * Transposes an individual block inside a block matrix.
     */
    private static void transposeBlock( BlockMatrix64F A , BlockMatrix64F A_tran,
                                        int indexA , int indexC ,
                                        int width , int height )
    {
        for( int i = 0; i < height; i++ ) {
            int rowIndexC = indexC + i;
            int rowIndexA = indexA + width*i;
            int end = rowIndexA + width;
            for( ; rowIndexA < end; rowIndexC += height, rowIndexA++ ) {
                A_tran.data[ rowIndexC ] = A.data[ rowIndexA ];
            }
        }
    }

    public static BlockMatrix64F createRandom( int numRows , int numCols ,
                                               double min , double max , Random rand )
    {
        BlockMatrix64F ret = new BlockMatrix64F(numRows,numCols);

        RandomMatrices.setRandom(ret,min,max,rand);

        return ret;
    }

    public static BlockMatrix64F createRandom( int numRows , int numCols ,
                                               double min , double max , Random rand ,
                                               int blockLength )
    {
        BlockMatrix64F ret = new BlockMatrix64F(numRows,numCols,blockLength);

        RandomMatrices.setRandom(ret,min,max,rand);

        return ret;
    }


    public static BlockMatrix64F convert(DenseMatrix64F A , int blockLength ) {
        BlockMatrix64F ret = new BlockMatrix64F(A.numRows,A.numCols,blockLength);
        convert(A,ret);
        return ret;
    }

    public static BlockMatrix64F convert(DenseMatrix64F A ) {
        BlockMatrix64F ret = new BlockMatrix64F(A.numRows,A.numCols);
        convert(A,ret);
        return ret;
    }

    public static boolean isEquals( BlockMatrix64F A , BlockMatrix64F B )
    {
        if( A.blockLength != B.blockLength )
            return false;

        return MatrixFeatures.isEquals(A,B);
    }

    public static boolean isEquals( BlockMatrix64F A , BlockMatrix64F B , double tol )
    {
        if( A.blockLength != B.blockLength )
            return false;

        return MatrixFeatures.isEquals(A,B,tol);
    }

    /**
     * Sets either the upper or low triangle of a matrix to zero
     */
    public static void zeroTriangle( boolean upper , BlockMatrix64F A )
    {
        int blockLength = A.blockLength;

        if( upper ) {
            for( int i = 0; i < A.numRows; i += blockLength ) {
                int h = Math.min(blockLength,A.numRows-i);

                for( int j = i; j < A.numCols; j += blockLength ) {
                    int w = Math.min(blockLength,A.numCols-j);

                    int index = i*A.numCols + h*j;

                    if( j == i ) {
                        for( int k = 0; k < h; k++ ) {
                            for( int l = k+1; l < w; l++ ) {
                                A.data[index + w*k+l ] = 0;
                            }
                        }
                    } else {
                        for( int k = 0; k < h; k++ ) {
                            for( int l = 0; l < w; l++ ) {
                                A.data[index + w*k+l ] = 0;
                            }
                        }
                    }
                }
            }
        } else {
            for( int i = 0; i < A.numRows; i += blockLength ) {
                int h = Math.min(blockLength,A.numRows-i);

                for( int j = 0; j <= i; j += blockLength ) {
                    int w = Math.min(blockLength,A.numCols-j);

                    int index = i*A.numCols + h*j;

                    if( j == i ) {
                        for( int k = 0; k < h; k++ ) {
                            int z = Math.min(k,w);
                            for( int l = 0; l < z; l++ ) {
                                A.data[index + w*k+l ] = 0;
                            }
                        }
                    } else {
                        for( int k = 0; k < h; k++ ) {
                            for( int l = 0; l < w; l++ ) {
                                A.data[index + w*k+l ] = 0;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Copies either the upper or lower triangular portion of src into dst.  Dst can be smaller
     * than src.
     *
     * @param upper If the upper or lower triangle is copied.
     * @param src The source matrix. Not modified.
     * @param dst The destination matrix. Modified.
     */
    public static void copyTriangle( boolean upper , BlockMatrix64F src , BlockMatrix64F dst )
    {
        if( src.blockLength != dst.blockLength )
            throw new IllegalArgumentException("Block size is different");
        if( src.numRows < dst.numRows )
            throw new IllegalArgumentException("The src has fewer rows than dst");
        if( src.numCols < dst.numCols )
            throw new IllegalArgumentException("The src has fewer columns than dst");

        int blockLength = src.blockLength;

        int numRows = Math.min(src.numRows,dst.numRows);
        int numCols = Math.min(src.numCols,dst.numCols);

        if( upper ) {
            for( int i = 0; i < numRows; i += blockLength ) {
                int heightSrc = Math.min(blockLength,src.numRows-i);
                int heightDst = Math.min(blockLength,dst.numRows-i);

                for( int j = i; j < numCols; j += blockLength ) {
                    int widthSrc = Math.min(blockLength,src.numCols-j);
                    int widthDst = Math.min(blockLength,dst.numCols-j);

                    int indexSrc = i*src.numCols + heightSrc*j;
                    int indexDst = i*dst.numCols + heightDst*j;

                    if( j == i ) {
                        for( int k = 0; k < heightDst; k++ ) {
                            for( int l = k; l < widthDst; l++ ) {
                                dst.data[indexDst + widthDst*k+l ] = src.data[indexSrc + widthSrc*k+l ];
                            }
                        }
                    } else {
                        for( int k = 0; k < heightDst; k++ ) {
                            System.arraycopy(src.data, indexSrc + widthSrc * k, dst.data, indexDst + widthDst * k, widthDst);
                        }
                    }
                }
            }
        } else {
            for( int i = 0; i < numRows; i += blockLength ) {
                int heightSrc = Math.min(blockLength,src.numRows-i);
                int heightDst = Math.min(blockLength,dst.numRows-i);

                for( int j = 0; j <= i; j += blockLength ) {
                    int widthSrc = Math.min(blockLength,src.numCols-j);
                    int widthDst = Math.min(blockLength,dst.numCols-j);

                    int indexSrc = i*src.numCols + heightSrc*j;
                    int indexDst = i*dst.numCols + heightDst*j;

                    if( j == i ) {
                        for( int k = 0; k < heightDst; k++ ) {
                            int z = Math.min(k+1,widthDst);
                            for( int l = 0; l < z; l++ ) {
                                dst.data[indexDst + widthDst*k+l ] = src.data[indexSrc + widthSrc*k+l ];
                            }
                        }
                    } else {
                        for( int k = 0; k < heightDst; k++ ) {
                            System.arraycopy(src.data, indexSrc + widthSrc * k, dst.data, indexDst + widthDst * k, widthDst);
                        }
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Sets every element in the matrix to the specified value.<br>
     * <br>
     * a<sub>ij</sub> = value
     * <p>
     *
     * @param A A matrix whose elements are about to be set. Modified.
     * @param value The value each element will have.
     */
    public static void set( BlockMatrix64F A , double value ) {
        CommonOps.fill(A, value);
    }

    /**
     * <p>Sets the value of A to all zeros except along the diagonal.</p>
     *
     * @param A Block matrix.
     */
    public static void setIdentity( BlockMatrix64F A )
    {
        int minLength = Math.min(A.numRows,A.numCols);

        CommonOps.fill(A, 0);

        int blockLength = A.blockLength;

        for( int i = 0; i < minLength; i += blockLength ) {
            int h = Math.min(blockLength,A.numRows-i);
            int w = Math.min(blockLength,A.numCols-i);

            int index = i*A.numCols + h*i;

            int m = Math.min(h,w);
            for( int k = 0; k < m; k++ ) {
                A.data[index + k*w + k ] = 1;
            }
        }
    }

    /**
     * <p>
     * Returns a new matrix with ones along the diagonal and zeros everywhere else.
     * </p>
     *
     * @param numRows Number of rows.
     * @param numCols NUmber of columns.
     * @param blockLength Block length.
     * @return An identify matrix.
     */
    public static BlockMatrix64F identity(int numRows, int numCols, int blockLength ) {
        BlockMatrix64F A = new BlockMatrix64F(numRows,numCols,blockLength);

        int minLength = Math.min(numRows,numCols);

        for( int i = 0; i < minLength; i += blockLength ) {
            int h = Math.min(blockLength,A.numRows-i);
            int w = Math.min(blockLength,A.numCols-i);

            int index = i*A.numCols + h*i;

            int m = Math.min(h,w);
            for( int k = 0; k < m; k++ ) {
                A.data[index + k*w + k ] = 1;
            }
        }

        return A;
    }

    /**
     * <p>
     * Checks to see if the two matrices have an identical shape an block size.
     * </p>
     *
     * @param A Matrix.
     * @param B Matrix.
     */
    public static void checkIdenticalShape( BlockMatrix64F A , BlockMatrix64F B ) {
        if( A.blockLength != B.blockLength )
            throw new IllegalArgumentException("Block size is different");
        if( A.numRows != B.numRows )
            throw new IllegalArgumentException("Number of rows is different");
        if( A.numCols != B.numCols )
            throw new IllegalArgumentException("NUmber of columns is different");
    }

    /**
     * <p>
     * Extracts a matrix from src into dst.  The submatrix which is copied has its initial coordinate
     * at (0,0) and ends at (dst.numRows,dst.numCols). The end rows/columns must be aligned along blocks
     * or else it will silently screw things up.
     * </p>
     *
     * @param src Matrix which a submatrix is being extracted from. Not modified.
     * @param dst Where the submatrix is written to.  Its rows and columns be less than or equal to 'src'.  Modified.
     */
    public static void extractAligned(BlockMatrix64F src, BlockMatrix64F dst) {
        if( src.blockLength != dst.blockLength )
            throw new IllegalArgumentException("Block size is different");
        if( src.numRows < dst.numRows )
            throw new IllegalArgumentException("The src has fewer rows than dst");
        if( src.numCols < dst.numCols )
            throw new IllegalArgumentException("The src has fewer columns than dst");

        int blockLength = src.blockLength;

        int numRows = Math.min(src.numRows,dst.numRows);
        int numCols = Math.min(src.numCols,dst.numCols);

        for( int i = 0; i < numRows; i += blockLength ) {
            int heightSrc = Math.min(blockLength,src.numRows-i);
            int heightDst = Math.min(blockLength,dst.numRows-i);

            for( int j = 0; j < numCols; j += blockLength ) {
                int widthSrc = Math.min(blockLength,src.numCols-j);
                int widthDst = Math.min(blockLength,dst.numCols-j);

                int indexSrc = i*src.numCols + heightSrc*j;
                int indexDst = i*dst.numCols + heightDst*j;

                for( int k = 0; k < heightDst; k++ ) {
                    System.arraycopy(src.data, indexSrc + widthSrc * k, dst.data, indexDst + widthDst * k, widthDst);
                }
            }
        }
    }

    /**
     * Checks to see if the submatrix has its boundaries along inner blocks.
     *
     * @param blockLength Size of an inner block.
     * @param A Submatrix.
     * @return If it is block aligned or not.
     */
    public static boolean blockAligned( int blockLength , D1Submatrix64F A ) {
        if( A.col0 % blockLength != 0 )
            return false;
        if( A.row0 % blockLength != 0 )
            return false;

        if( A.col1 % blockLength != 0 && A.col1 != A.original.numCols ) {
            return false;
        }

        if( A.row1 % blockLength != 0 && A.row1 != A.original.numRows) {
            return false;
        }

        return true;
    }
}
