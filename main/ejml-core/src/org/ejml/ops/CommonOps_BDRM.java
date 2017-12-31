/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.BMatrixRMaj;

/**
 * @author Peter Abeles
 */
public class CommonOps_BDRM {
    /**
     * In-place transpose for a square matrix.  On most architectures it is faster than the standard transpose
     * algorithm, but on most modern computers it's slower than block transpose.
     *
     * @param mat The matrix that is transposed in-place.  Modified.
     */
    public static void transposeSquare( BMatrixRMaj mat )
    {
        if( mat.numCols != mat.numRows )
            throw new IllegalArgumentException("Must be sqare");

        int index = 1;
        int indexEnd = mat.numCols;
        for( int i = 0; i < mat.numRows;
             i++ , index += i+1 , indexEnd += mat.numCols ) {
            int indexOther = (i+1)*mat.numCols + i;
            for( ; index < indexEnd; index++, indexOther += mat.numCols) {
                boolean val = mat.data[ index ];
                mat.data[ index ] = mat.data[ indexOther ];
                mat.data[indexOther] = val;
            }
        }
    }
}
