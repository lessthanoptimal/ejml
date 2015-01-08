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

package org.ejml.alg.dense.mult;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.EjmlUnitTests;
import org.junit.Test;


/**
 * @author Peter Abeles
 */
public class TestSubmatrixOps {

    @Test
    public void setSubMatrix() {
        DenseMatrix64F A = new DenseMatrix64F(5,5);
        DenseMatrix64F B = new DenseMatrix64F(6,6);

        for( int i = 0; i < A.data.length; i++ ) {
            A.data[i] = 1;
        }

        SubmatrixOps.setSubMatrix(A,B,1,1,2,3,2,3);

        // create a matrix that should be identical to B
        DenseMatrix64F C = new DenseMatrix64F(6,6);
        for( int i = 2; i < 4; i++ ) {
            for( int j = 3; j < 6; j++ ) {
                C.set(i,j,1);
            }
        }

        // see if they are the same
        EjmlUnitTests.assertEquals(B,C,0);
    }
}
