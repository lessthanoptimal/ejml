/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

package org.ejml.ops;

import org.ejml.data.*;

import java.util.Arrays;

/**
 * Functions for converting between matrix types.  Both matrices must be the same size and their values will
 * be copied.
 *
 * @author Peter Abeles
 */
public class ConvertDMatrixStruct {

    /**
     * Generic, but slow, conversion function.
     *
     * @param input Input matrix.
     * @param output Output matrix.
     */
    public static void convert(DMatrix input , DMatrix output ) {
        if( output instanceof ReshapeMatrix ) {
            ((ReshapeMatrix)output).reshape(input.getNumRows(),input.getNumCols());
        } else {
            if (input.getNumRows() != output.getNumRows())
                throw new IllegalArgumentException("Number of rows do not match");
            if (input.getNumCols() != output.getNumCols())
                throw new IllegalArgumentException("Number of columns do not match");
        }

        for( int i = 0; i < input.getNumRows(); i++  ) {
            for( int j = 0; j < input.getNumCols(); j++ ) {
                output.unsafe_set(i,j,input.unsafe_get(i,j));
            }
        }
    }

    /**
     * Converts {@link DMatrix2x2} into {@link DMatrixRMaj}.
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrixRMaj convert(DMatrix2x2 input , DMatrixRMaj output ) {
        if( output == null)
            output = new DMatrixRMaj(2,2);

        output.reshape(input.getNumRows(),input.getNumCols());

        output.data[0] = input.a11;
        output.data[1] = input.a12;
        output.data[2] = input.a21;
        output.data[3] = input.a22;

        return output;
    }

    /**
     * Converts {@link DMatrix3x3} into {@link DMatrixRMaj}.
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrixRMaj convert(DMatrix3x3 input , DMatrixRMaj output ) {
        if( output == null)
            output = new DMatrixRMaj(3,3);

        output.reshape(input.getNumRows(),input.getNumCols());

        output.data[0] = input.a11;
        output.data[1] = input.a12;
        output.data[2] = input.a13;
        output.data[3] = input.a21;
        output.data[4] = input.a22;
        output.data[5] = input.a23;
        output.data[6] = input.a31;
        output.data[7] = input.a32;
        output.data[8] = input.a33;

        return output;
    }

    /**
     * Converts {@link DMatrix4x4} into {@link DMatrixRMaj}.
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrixRMaj convert(DMatrix4x4 input , DMatrixRMaj output ) {
        if( output == null)
            output = new DMatrixRMaj(4,4);

        output.reshape(input.getNumRows(),input.getNumCols());

        output.data[0] = input.a11;
        output.data[1] = input.a12;
        output.data[2] = input.a13;
        output.data[3] = input.a14;
        output.data[4] = input.a21;
        output.data[5] = input.a22;
        output.data[6] = input.a23;
        output.data[7] = input.a24;
        output.data[8] = input.a31;
        output.data[9] = input.a32;
        output.data[10] = input.a33;
        output.data[11] = input.a34;
        output.data[12] = input.a41;
        output.data[13] = input.a42;
        output.data[14] = input.a43;
        output.data[15] = input.a44;

        return output;
    }

    /**
     * Converts {@link DMatrix5x5} into {@link DMatrixRMaj}.
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrixRMaj convert(DMatrix5x5 input , DMatrixRMaj output ) {
        if( output == null)
            output = new DMatrixRMaj(5,5);

        output.reshape(input.getNumRows(),input.getNumCols());

        output.data[0] = input.a11;
        output.data[1] = input.a12;
        output.data[2] = input.a13;
        output.data[3] = input.a14;
        output.data[4] = input.a15;
        output.data[5] = input.a21;
        output.data[6] = input.a22;
        output.data[7] = input.a23;
        output.data[8] = input.a24;
        output.data[9] = input.a25;
        output.data[10] = input.a31;
        output.data[11] = input.a32;
        output.data[12] = input.a33;
        output.data[13] = input.a34;
        output.data[14] = input.a35;
        output.data[15] = input.a41;
        output.data[16] = input.a42;
        output.data[17] = input.a43;
        output.data[18] = input.a44;
        output.data[19] = input.a45;
        output.data[20] = input.a51;
        output.data[21] = input.a52;
        output.data[22] = input.a53;
        output.data[23] = input.a54;
        output.data[24] = input.a55;

        return output;
    }

    /**
     * Converts {@link DMatrix6x6} into {@link DMatrixRMaj}.
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrixRMaj convert(DMatrix6x6 input , DMatrixRMaj output ) {
        if( output == null)
            output = new DMatrixRMaj(6,6);

        output.reshape(input.getNumRows(),input.getNumCols());

        output.data[0] = input.a11;
        output.data[1] = input.a12;
        output.data[2] = input.a13;
        output.data[3] = input.a14;
        output.data[4] = input.a15;
        output.data[5] = input.a16;
        output.data[6] = input.a21;
        output.data[7] = input.a22;
        output.data[8] = input.a23;
        output.data[9] = input.a24;
        output.data[10] = input.a25;
        output.data[11] = input.a26;
        output.data[12] = input.a31;
        output.data[13] = input.a32;
        output.data[14] = input.a33;
        output.data[15] = input.a34;
        output.data[16] = input.a35;
        output.data[17] = input.a36;
        output.data[18] = input.a41;
        output.data[19] = input.a42;
        output.data[20] = input.a43;
        output.data[21] = input.a44;
        output.data[22] = input.a45;
        output.data[23] = input.a46;
        output.data[24] = input.a51;
        output.data[25] = input.a52;
        output.data[26] = input.a53;
        output.data[27] = input.a54;
        output.data[28] = input.a55;
        output.data[29] = input.a56;
        output.data[30] = input.a61;
        output.data[31] = input.a62;
        output.data[32] = input.a63;
        output.data[33] = input.a64;
        output.data[34] = input.a65;
        output.data[35] = input.a66;

        return output;
    }

    /**
     * Converts {@link DMatrixRMaj} into {@link DMatrix2x2}
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrix2x2 convert(DMatrixRMaj input , DMatrix2x2 output ) {
        if( output == null)
            output = new DMatrix2x2();

        if( input.getNumRows() != output.getNumRows() )
            throw new IllegalArgumentException("Number of rows do not match");
        if( input.getNumCols() != output.getNumCols() )
            throw new IllegalArgumentException("Number of columns do not match");

        output.a11 = input.data[0];
        output.a12 = input.data[1];
        output.a21 = input.data[2];
        output.a22 = input.data[3];

        return output;
    }

    /**
     * Converts {@link DMatrixRMaj} into {@link DMatrix3x3}
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrix3x3 convert(DMatrixRMaj input , DMatrix3x3 output ) {
        if( output == null)
            output = new DMatrix3x3();

        if( input.getNumRows() != output.getNumRows() )
            throw new IllegalArgumentException("Number of rows do not match");
        if( input.getNumCols() != output.getNumCols() )
            throw new IllegalArgumentException("Number of columns do not match");

        output.a11 = input.data[0];
        output.a12 = input.data[1];
        output.a13 = input.data[2];
        output.a21 = input.data[3];
        output.a22 = input.data[4];
        output.a23 = input.data[5];
        output.a31 = input.data[6];
        output.a32 = input.data[7];
        output.a33 = input.data[8];

        return output;
    }

    /**
     * Converts {@link DMatrixRMaj} into {@link DMatrix4x4}
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrix4x4 convert(DMatrixRMaj input , DMatrix4x4 output ) {
        if( output == null)
            output = new DMatrix4x4();

        if( input.getNumRows() != output.getNumRows() )
            throw new IllegalArgumentException("Number of rows do not match");
        if( input.getNumCols() != output.getNumCols() )
            throw new IllegalArgumentException("Number of columns do not match");

        output.a11 = input.data[0];
        output.a12 = input.data[1];
        output.a13 = input.data[2];
        output.a14 = input.data[3];
        output.a21 = input.data[4];
        output.a22 = input.data[5];
        output.a23 = input.data[6];
        output.a24 = input.data[7];
        output.a31 = input.data[8];
        output.a32 = input.data[9];
        output.a33 = input.data[10];
        output.a34 = input.data[11];
        output.a41 = input.data[12];
        output.a42 = input.data[13];
        output.a43 = input.data[14];
        output.a44 = input.data[15];

        return output;
    }

    /**
     * Converts {@link DMatrixRMaj} into {@link DMatrix5x5}
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrix5x5 convert(DMatrixRMaj input , DMatrix5x5 output ) {
        if( output == null)
            output = new DMatrix5x5();

        if( input.getNumRows() != output.getNumRows() )
            throw new IllegalArgumentException("Number of rows do not match");
        if( input.getNumCols() != output.getNumCols() )
            throw new IllegalArgumentException("Number of columns do not match");

        output.a11 = input.data[0];
        output.a12 = input.data[1];
        output.a13 = input.data[2];
        output.a14 = input.data[3];
        output.a15 = input.data[4];
        output.a21 = input.data[5];
        output.a22 = input.data[6];
        output.a23 = input.data[7];
        output.a24 = input.data[8];
        output.a25 = input.data[9];
        output.a31 = input.data[10];
        output.a32 = input.data[11];
        output.a33 = input.data[12];
        output.a34 = input.data[13];
        output.a35 = input.data[14];
        output.a41 = input.data[15];
        output.a42 = input.data[16];
        output.a43 = input.data[17];
        output.a44 = input.data[18];
        output.a45 = input.data[19];
        output.a51 = input.data[20];
        output.a52 = input.data[21];
        output.a53 = input.data[22];
        output.a54 = input.data[23];
        output.a55 = input.data[24];

        return output;
    }

    /**
     * Converts {@link DMatrixRMaj} into {@link DMatrix6x6}
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrix6x6 convert(DMatrixRMaj input , DMatrix6x6 output ) {
        if( output == null)
            output = new DMatrix6x6();

        if( input.getNumRows() != output.getNumRows() )
            throw new IllegalArgumentException("Number of rows do not match");
        if( input.getNumCols() != output.getNumCols() )
            throw new IllegalArgumentException("Number of columns do not match");

        output.a11 = input.data[0];
        output.a12 = input.data[1];
        output.a13 = input.data[2];
        output.a14 = input.data[3];
        output.a15 = input.data[4];
        output.a16 = input.data[5];
        output.a21 = input.data[6];
        output.a22 = input.data[7];
        output.a23 = input.data[8];
        output.a24 = input.data[9];
        output.a25 = input.data[10];
        output.a26 = input.data[11];
        output.a31 = input.data[12];
        output.a32 = input.data[13];
        output.a33 = input.data[14];
        output.a34 = input.data[15];
        output.a35 = input.data[16];
        output.a36 = input.data[17];
        output.a41 = input.data[18];
        output.a42 = input.data[19];
        output.a43 = input.data[20];
        output.a44 = input.data[21];
        output.a45 = input.data[22];
        output.a46 = input.data[23];
        output.a51 = input.data[24];
        output.a52 = input.data[25];
        output.a53 = input.data[26];
        output.a54 = input.data[27];
        output.a55 = input.data[28];
        output.a56 = input.data[29];
        output.a61 = input.data[30];
        output.a62 = input.data[31];
        output.a63 = input.data[32];
        output.a64 = input.data[33];
        output.a65 = input.data[34];
        output.a66 = input.data[35];

        return output;
    }

    /**
     * Converts {@link DMatrix2} into {@link DMatrixRMaj}.
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrixRMaj convert(DMatrix2 input , DMatrixRMaj output ) {
        if( output == null)
            output = new DMatrixRMaj(2,1);

        if( output.getNumRows() != 1 && output.getNumCols() != 1 )
            throw new IllegalArgumentException("One row or column must have a length of 1 for it to be a vector");
        int length = Math.max(output.getNumRows(),output.getNumCols());
        if( length != 2 )
            throw new IllegalArgumentException("Length of input vector is not 2.  It is "+length);

        output.data[0] = input.a1;
        output.data[1] = input.a2;

        return output;
    }

    /**
     * Converts {@link DMatrix3} into {@link DMatrixRMaj}.
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrixRMaj convert(DMatrix3 input , DMatrixRMaj output ) {
        if( output == null)
            output = new DMatrixRMaj(3,1);

        if( output.getNumRows() != 1 && output.getNumCols() != 1 )
            throw new IllegalArgumentException("One row or column must have a length of 1 for it to be a vector");
        int length = Math.max(output.getNumRows(),output.getNumCols());
        if( length != 3 )
            throw new IllegalArgumentException("Length of input vector is not 3.  It is "+length);

        output.data[0] = input.a1;
        output.data[1] = input.a2;
        output.data[2] = input.a3;

        return output;
    }

    /**
     * Converts {@link DMatrix4} into {@link DMatrixRMaj}.
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrixRMaj convert(DMatrix4 input , DMatrixRMaj output ) {
        if( output == null)
            output = new DMatrixRMaj(4,1);

        if( output.getNumRows() != 1 && output.getNumCols() != 1 )
            throw new IllegalArgumentException("One row or column must have a length of 1 for it to be a vector");
        int length = Math.max(output.getNumRows(),output.getNumCols());
        if( length != 4 )
            throw new IllegalArgumentException("Length of input vector is not 4.  It is "+length);

        output.data[0] = input.a1;
        output.data[1] = input.a2;
        output.data[2] = input.a3;
        output.data[3] = input.a4;

        return output;
    }

    /**
     * Converts {@link DMatrix5} into {@link DMatrixRMaj}.
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrixRMaj convert(DMatrix5 input , DMatrixRMaj output ) {
        if( output == null)
            output = new DMatrixRMaj(5,1);

        if( output.getNumRows() != 1 && output.getNumCols() != 1 )
            throw new IllegalArgumentException("One row or column must have a length of 1 for it to be a vector");
        int length = Math.max(output.getNumRows(),output.getNumCols());
        if( length != 5 )
            throw new IllegalArgumentException("Length of input vector is not 5.  It is "+length);

        output.data[0] = input.a1;
        output.data[1] = input.a2;
        output.data[2] = input.a3;
        output.data[3] = input.a4;
        output.data[4] = input.a5;

        return output;
    }

    /**
     * Converts {@link DMatrix6} into {@link DMatrixRMaj}.
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrixRMaj convert(DMatrix6 input , DMatrixRMaj output ) {
        if( output == null)
            output = new DMatrixRMaj(6,1);

        if( output.getNumRows() != 1 && output.getNumCols() != 1 )
            throw new IllegalArgumentException("One row or column must have a length of 1 for it to be a vector");
        int length = Math.max(output.getNumRows(),output.getNumCols());
        if( length != 6 )
            throw new IllegalArgumentException("Length of input vector is not 6.  It is "+length);

        output.data[0] = input.a1;
        output.data[1] = input.a2;
        output.data[2] = input.a3;
        output.data[3] = input.a4;
        output.data[4] = input.a5;
        output.data[5] = input.a6;

        return output;
    }

    /**
     * Converts {@link DMatrixRMaj} into {@link DMatrix2}
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrix2 convert(DMatrixRMaj input , DMatrix2 output ) {
        if( output == null)
            output = new DMatrix2();

        if( input.getNumRows() != 1 && input.getNumCols() != 1 )
            throw new IllegalArgumentException("One row or column must have a length of 1 for it to be a vector");
        int length = Math.max(input.getNumRows(),input.getNumCols());
        if( length != 2 )
            throw new IllegalArgumentException("Length of input vector is not 2.  It is "+length);

        output.a1 = input.data[0];
        output.a2 = input.data[1];

        return output;
    }

    /**
     * Converts {@link DMatrixRMaj} into {@link DMatrix3}
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrix3 convert(DMatrixRMaj input , DMatrix3 output ) {
        if( output == null)
            output = new DMatrix3();

        if( input.getNumRows() != 1 && input.getNumCols() != 1 )
            throw new IllegalArgumentException("One row or column must have a length of 1 for it to be a vector");
        int length = Math.max(input.getNumRows(),input.getNumCols());
        if( length != 3 )
            throw new IllegalArgumentException("Length of input vector is not 3.  It is "+length);

        output.a1 = input.data[0];
        output.a2 = input.data[1];
        output.a3 = input.data[2];

        return output;
    }

    /**
     * Converts {@link DMatrixRMaj} into {@link DMatrix4}
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrix4 convert(DMatrixRMaj input , DMatrix4 output ) {
        if( output == null)
            output = new DMatrix4();

        if( input.getNumRows() != 1 && input.getNumCols() != 1 )
            throw new IllegalArgumentException("One row or column must have a length of 1 for it to be a vector");
        int length = Math.max(input.getNumRows(),input.getNumCols());
        if( length != 4 )
            throw new IllegalArgumentException("Length of input vector is not 4.  It is "+length);

        output.a1 = input.data[0];
        output.a2 = input.data[1];
        output.a3 = input.data[2];
        output.a4 = input.data[3];

        return output;
    }

    /**
     * Converts {@link DMatrixRMaj} into {@link DMatrix5}
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrix5 convert(DMatrixRMaj input , DMatrix5 output ) {
        if( output == null)
            output = new DMatrix5();

        if( input.getNumRows() != 1 && input.getNumCols() != 1 )
            throw new IllegalArgumentException("One row or column must have a length of 1 for it to be a vector");
        int length = Math.max(input.getNumRows(),input.getNumCols());
        if( length != 5 )
            throw new IllegalArgumentException("Length of input vector is not 5.  It is "+length);

        output.a1 = input.data[0];
        output.a2 = input.data[1];
        output.a3 = input.data[2];
        output.a4 = input.data[3];
        output.a5 = input.data[4];

        return output;
    }

    /**
     * Converts {@link DMatrixRMaj} into {@link DMatrix6}
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrix6 convert(DMatrixRMaj input , DMatrix6 output ) {
        if( output == null)
            output = new DMatrix6();

        if( input.getNumRows() != 1 && input.getNumCols() != 1 )
            throw new IllegalArgumentException("One row or column must have a length of 1 for it to be a vector");
        int length = Math.max(input.getNumRows(),input.getNumCols());
        if( length != 6 )
            throw new IllegalArgumentException("Length of input vector is not 6.  It is "+length);

        output.a1 = input.data[0];
        output.a2 = input.data[1];
        output.a3 = input.data[2];
        output.a4 = input.data[3];
        output.a5 = input.data[4];
        output.a6 = input.data[5];

        return output;
    }

    /**
     * Converts {@link DMatrixRMaj} into {@link DMatrixRBlock}
     *
     * Can't handle null output matrix since block size needs to be specified.
     *
     * @param src Input matrix.
     * @param dst Output matrix.
     */
    public static void convert(DMatrixRMaj src , DMatrixRBlock dst ) {
        if( src.numRows != dst.numRows || src.numCols != dst.numCols )
            throw new IllegalArgumentException("Must be the same size.");

        for( int i = 0; i < dst.numRows; i += dst.blockLength ) {
            int blockHeight = Math.min( dst.blockLength , dst.numRows - i);

            for( int j = 0; j < dst.numCols; j += dst.blockLength ) {
                int blockWidth = Math.min( dst.blockLength , dst.numCols - j);

                int indexDst = i*dst.numCols + blockHeight*j;
                int indexSrcRow = i*dst.numCols + j;

                for( int k = 0; k < blockHeight; k++ ) {
                    System.arraycopy(src.data,indexSrcRow,dst.data,indexDst,blockWidth);
                    indexDst += blockWidth;
                    indexSrcRow += dst.numCols;
                }
            }
        }
    }

    /**
     * Converts {@link DMatrixRBlock} into {@link DMatrixRMaj}
     *
     * @param src Input matrix.
     * @param dst Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DMatrixRMaj convert(DMatrixRBlock src , DMatrixRMaj dst ) {
        if( dst != null ) {
            dst.reshape(src.getNumRows(),src.getNumCols());
        } else {
            dst = new DMatrixRMaj(src.numRows,src.numCols);
        }

        for( int i = 0; i < src.numRows; i += src.blockLength ) {
            int blockHeight = Math.min( src.blockLength , src.numRows - i);

            for( int j = 0; j < src.numCols; j += src.blockLength ) {
                int blockWidth = Math.min( src.blockLength , src.numCols - j);

                int indexSrc = i*src.numCols + blockHeight*j;
                int indexDstRow = i*dst.numCols + j;

                for( int k = 0; k < blockHeight; k++ ) {
                    System.arraycopy(src.data,indexSrc,dst.data,indexDstRow,blockWidth);
                    indexSrc += blockWidth;
                    indexDstRow += dst.numCols;
                }
            }
        }

        return dst;
    }

    public static DMatrixSparseTriplet convert(DMatrix src , DMatrixSparseTriplet dst , double tol ) {
        if( dst == null )
            dst = new DMatrixSparseTriplet(src.getNumRows(), src.getNumCols(), 1);
        else
            dst.reshape(src.getNumRows(), src.getNumCols());

        for (int row = 0; row < src.getNumRows(); row++) {
            for (int col = 0; col < src.getNumCols(); col++) {
                double value = src.unsafe_get(row,col);
                if( Math.abs(value) > tol )
                    dst.addItem(row,col,value);
            }
        }

        return dst;
    }

    public static DMatrixSparseTriplet convert(DMatrixRMaj src , DMatrixSparseTriplet dst , double tol ) {
        if( dst == null )
            dst = new DMatrixSparseTriplet(src.numRows, src.numCols,src.numRows*src.numCols);
        else
            dst.reshape(src.numRows, src.numCols);

        int index = 0;
        for (int row = 0; row < src.numRows; row++) {
            for (int col = 0; col < src.numCols; col++) {
                double value = src.data[index++];
                if( Math.abs(value) > tol )
                    dst.addItem(row,col,value);
            }
        }

        return dst;
    }

    public static DMatrixRMaj convert(DMatrixSparseTriplet src , DMatrixRMaj dst ) {
        if( dst == null )
            dst = new DMatrixRMaj(src.numRows, src.numCols);
        else {
            dst.reshape(src.numRows, src.numCols);
            dst.zero();
        }

        for (int i = 0; i < src.nz_length; i++) {
            int row = src.nz_rowcol.data[i*2];
            int col = src.nz_rowcol.data[i*2+1];
            double value = src.nz_value.data[i];

            dst.unsafe_set(row, col, value);
        }

        return dst;
    }

    public static DMatrixRMaj convert(DMatrixSparseCSC src , DMatrixRMaj dst ) {
        if( dst == null )
            dst = new DMatrixRMaj(src.numRows, src.numCols);
        else {
            dst.reshape(src.numRows, src.numCols);
            dst.zero();
        }

        int idx0 = src.col_idx[0];
        for (int j = 1; j <= src.numCols; j++) {
            int idx1 = src.col_idx[j];

            for (int i = idx0; i < idx1; i++) {
                int row = src.nz_rows[i];
                double val = src.nz_values[i];

                dst.unsafe_set(row,j-1, val);
            }
            idx0 = idx1;
        }

        return dst;
    }

    /**
     *
     *
     * @param src Original matrix that is to be converted.
     * @param dst Storage for the converted matrix.  If null a new instance will be returned.
     * @return The converted matrix
     */
    public static DMatrixSparseCSC convert(DMatrixRMaj src , DMatrixSparseCSC dst , double tol ) {
        int nonzero = 0;
        int N = src.numRows*src.numCols;
        for (int i = 0; i < N; i++) {
            if( src.data[i] != 0 )
                nonzero++;
        }

        if( dst == null )
            dst = new DMatrixSparseCSC(src.numRows, src.numCols, nonzero);
        else
            dst.reshape(src.numRows, src.numCols, nonzero);
        dst.nz_length = 0;

        dst.col_idx[0] = 0;
        for (int col = 0; col < src.numCols; col++) {
            for (int row = 0; row < src.numRows; row++) {
                double value = src.data[row*src.numCols+col];
                if( Math.abs(value) <= tol )
                    continue;

                dst.nz_rows[dst.nz_length] = row;
                dst.nz_values[dst.nz_length] = value;
                dst.nz_length += 1;
            }
            dst.col_idx[col+1] = dst.nz_length;
        }

        return dst;
    }

    /**
     * Converts SMatrixTriplet_64 into a SMatrixCC_64.
     *
     * @param src Original matrix which is to be copied.  Not modified.
     * @param dst Destination. Will be a copy.  Modified.
     * @param hist Workspace.  Should be at least as long as the number of columns.  Can be null.
     */
    public static DMatrixSparseCSC convert(DMatrixSparseTriplet src , DMatrixSparseCSC dst , int hist[] ) {
        if( dst == null )
            dst = new DMatrixSparseCSC(src.numRows, src.numCols , src.nz_length);
        else
            dst.reshape(src.numRows, src.numCols, src.nz_length);

        if( hist == null )
            hist = new int[ src.numCols ];
        else if( hist.length >= src.numCols )
            Arrays.fill(hist,0,src.numCols, 0);
        else
            throw new IllegalArgumentException("Length of hist must be at least numCols");

        // compute the number of elements in each columns
        for (int i = 0; i < src.nz_length; i++) {
            hist[src.nz_rowcol.data[i*2+1]]++;
        }

        // define col_idx
        dst.histogramToStructure(hist);
        System.arraycopy(dst.col_idx,0,hist,0,dst.numCols);

        // now write the row indexes and the values
        for (int i = 0; i < src.nz_length; i++) {
            int row = src.nz_rowcol.data[i*2];
            int col = src.nz_rowcol.data[i*2+1];
            double value = src.nz_value.data[i];

            int index = hist[col]++;
            dst.nz_rows[index] = row;
            dst.nz_values[index] = value;
        }
        dst.indicesSorted = false;

        return dst;
    }

    public static DMatrixSparseCSC convert(DMatrixSparseTriplet src , DMatrixSparseCSC dst ) {
        return convert(src,dst,null);
    }

    public static DMatrixSparseTriplet convert(DMatrixSparseCSC src , DMatrixSparseTriplet dst ) {
        if( dst == null )
            dst = new DMatrixSparseTriplet(src.numRows, src.numCols, src.nz_length);
        else
            dst.reshape( src.numRows , src.numCols );

        int i0 = src.col_idx[0];
        for (int col = 0; col < src.numCols; col++) {
            int i1 = src.col_idx[col+1];

            for (int i = i0; i < i1; i++) {
                int row = src.nz_rows[i];
                dst.addItem(row,col, src.nz_values[i]);
            }
            i0 = i1;
        }

        return dst;
    }
}
