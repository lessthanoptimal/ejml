/*
 * Copyright (c) 2009-2013, Peter Abeles. All Rights Reserved.
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

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.data.*;

/**
 * Functions for converting between matrix types.  Both matrices must be the same size and their values will
 * be copied.
 *
 * @author Peter Abeles
 */
public class ConvertMatrixType {

    /**
     * Generic, but slow, conversion function.
     *
     * @param input Input matrix.
     * @param output Output matrix.
     */
    public static void convert( Matrix64F input , Matrix64F output ) {
        if( input.getNumRows() != output.getNumRows() )
            throw new IllegalArgumentException("Number of rows do not match");
        if( input.getNumCols() != output.getNumCols() )
            throw new IllegalArgumentException("Number of columns do not match");

        for( int i = 0; i < input.getNumRows(); i++  ) {
            for( int j = 0; j < input.getNumCols(); j++ ) {
                output.unsafe_set(i,j,input.unsafe_get(i,j));
            }
        }
    }

    /**
     * Converts {@link FixedMatrix2x2_64F} into {@link DenseMatrix64F}.
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DenseMatrix64F convert( FixedMatrix2x2_64F input , DenseMatrix64F output ) {
        if( output == null)
            output = new DenseMatrix64F(2,2);

        if( input.getNumRows() != output.getNumRows() )
            throw new IllegalArgumentException("Number of rows do not match");
        if( input.getNumCols() != output.getNumCols() )
            throw new IllegalArgumentException("Number of columns do not match");

        output.data[0] = input.a11;
        output.data[1] = input.a12;
        output.data[2] = input.a21;
        output.data[3] = input.a22;

        return output;
    }

    /**
     * Converts {@link FixedMatrix3x3_64F} into {@link DenseMatrix64F}.
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DenseMatrix64F convert( FixedMatrix3x3_64F input , DenseMatrix64F output ) {
        if( output == null)
            output = new DenseMatrix64F(3,3);

        if( input.getNumRows() != output.getNumRows() )
            throw new IllegalArgumentException("Number of rows do not match");
        if( input.getNumCols() != output.getNumCols() )
            throw new IllegalArgumentException("Number of columns do not match");

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
     * Converts {@link FixedMatrix4x4_64F} into {@link DenseMatrix64F}.
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DenseMatrix64F convert( FixedMatrix4x4_64F input , DenseMatrix64F output ) {
        if( output == null)
            output = new DenseMatrix64F(3,3);

        if( input.getNumRows() != output.getNumRows() )
            throw new IllegalArgumentException("Number of rows do not match");
        if( input.getNumCols() != output.getNumCols() )
            throw new IllegalArgumentException("Number of columns do not match");

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
     * Converts {@link FixedMatrix5x5_64F} into {@link DenseMatrix64F}.
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DenseMatrix64F convert( FixedMatrix5x5_64F input , DenseMatrix64F output ) {
        if( output == null)
            output = new DenseMatrix64F(3,3);

        if( input.getNumRows() != output.getNumRows() )
            throw new IllegalArgumentException("Number of rows do not match");
        if( input.getNumCols() != output.getNumCols() )
            throw new IllegalArgumentException("Number of columns do not match");

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
     * Converts {@link DenseMatrix64F} into {@link FixedMatrix2x2_64F}
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static FixedMatrix2x2_64F convert( DenseMatrix64F input , FixedMatrix2x2_64F output ) {
        if( output == null)
            output = new FixedMatrix2x2_64F();

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
     * Converts {@link DenseMatrix64F} into {@link FixedMatrix3x3_64F}
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static FixedMatrix3x3_64F convert( DenseMatrix64F input , FixedMatrix3x3_64F output ) {
        if( output == null)
            output = new FixedMatrix3x3_64F();

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
     * Converts {@link DenseMatrix64F} into {@link FixedMatrix4x4_64F}
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static FixedMatrix4x4_64F convert( DenseMatrix64F input , FixedMatrix4x4_64F output ) {
        if( output == null)
            output = new FixedMatrix4x4_64F();

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
     * Converts {@link DenseMatrix64F} into {@link FixedMatrix5x5_64F}
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static FixedMatrix5x5_64F convert( DenseMatrix64F input , FixedMatrix5x5_64F output ) {
        if( output == null)
            output = new FixedMatrix5x5_64F();

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
     * Converts {@link FixedMatrix2_64F} into {@link DenseMatrix64F}.
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DenseMatrix64F convert( FixedMatrix2_64F input , DenseMatrix64F output ) {
        if( output == null)
            output = new DenseMatrix64F(2,1);

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
     * Converts {@link FixedMatrix3_64F} into {@link DenseMatrix64F}.
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DenseMatrix64F convert( FixedMatrix3_64F input , DenseMatrix64F output ) {
        if( output == null)
            output = new DenseMatrix64F(3,1);

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
     * Converts {@link FixedMatrix4_64F} into {@link DenseMatrix64F}.
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DenseMatrix64F convert( FixedMatrix4_64F input , DenseMatrix64F output ) {
        if( output == null)
            output = new DenseMatrix64F(4,1);

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
     * Converts {@link FixedMatrix5_64F} into {@link DenseMatrix64F}.
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DenseMatrix64F convert( FixedMatrix5_64F input , DenseMatrix64F output ) {
        if( output == null)
            output = new DenseMatrix64F(5,1);

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
     * Converts {@link DenseMatrix64F} into {@link FixedMatrix2_64F}
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static FixedMatrix2_64F convert( DenseMatrix64F input , FixedMatrix2_64F output ) {
        if( output == null)
            output = new FixedMatrix2_64F();

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
     * Converts {@link DenseMatrix64F} into {@link FixedMatrix3_64F}
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static FixedMatrix3_64F convert( DenseMatrix64F input , FixedMatrix3_64F output ) {
        if( output == null)
            output = new FixedMatrix3_64F();

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
     * Converts {@link DenseMatrix64F} into {@link FixedMatrix4_64F}
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static FixedMatrix4_64F convert( DenseMatrix64F input , FixedMatrix4_64F output ) {
        if( output == null)
            output = new FixedMatrix4_64F();

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
     * Converts {@link DenseMatrix64F} into {@link FixedMatrix5_64F}
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static FixedMatrix5_64F convert( DenseMatrix64F input , FixedMatrix5_64F output ) {
        if( output == null)
            output = new FixedMatrix5_64F();

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
     * Converts {@link DenseMatrix64F} into {@link BlockMatrix64F}
     *
     * Can't handle null output matrix since block size needs to be specified.
     *
     * @param input Input matrix.
     * @param output Output matrix.
     */
    public static void convert( DenseMatrix64F input , BlockMatrix64F output ) {
        BlockMatrixOps.convert(input,output);
    }

    /**
     * Converts {@link BlockMatrix64F} into {@link DenseMatrix64F}
     *
     * @param input Input matrix.
     * @param output Output matrix.  If null a new matrix will be declared.
     * @return Converted matrix.
     */
    public static DenseMatrix64F convert( BlockMatrix64F input , DenseMatrix64F output ) {
        return BlockMatrixOps.convert(input,output);
    }
}
