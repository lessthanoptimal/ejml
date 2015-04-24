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

package org.ejml.alg.dense.mult;

import org.ejml.alg.fixed.FixedOps4;
import org.ejml.data.FixedMatrix4x4_64F;

/**
 * @author Peter Abeles
 */
public class MatrixMultFixed4x4Block {
    public static void mult( Fixed4x4Block A , Fixed4x4Block B , Fixed4x4Block C ) {
        if( A.numBlockCols != B.numBlockRows )
            throw new IllegalArgumentException("A B miss match");
        if( A.numBlockRows != C.numBlockRows )
            throw new IllegalArgumentException("A C miss match");
        if( B.numBlockCols != C.numBlockCols )
            throw new IllegalArgumentException("B C miss match");

        C.fill(0);

        for (int i = 0; i < A.numBlockRows; i++) {
            for (int j = 0; j < B.numBlockCols; j++) {
                FixedMatrix4x4_64F c = C.getBlock(i,j);
                for (int k = 0; k < A.numBlockCols; k++) {
                    FixedMatrix4x4_64F a = A.getBlock(i,k);
                    FixedMatrix4x4_64F b = B.getBlock(k,j);

                    FixedOps4.multAdd(a,b,c);
                }
            }
        }
    }
}
