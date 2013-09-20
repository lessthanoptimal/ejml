/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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
