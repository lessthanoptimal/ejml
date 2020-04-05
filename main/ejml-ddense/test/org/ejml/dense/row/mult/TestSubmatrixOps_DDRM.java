/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.mult;

import org.ejml.EjmlUnitTests;
import org.ejml.data.DMatrixRMaj;
import org.junit.jupiter.api.Test;


/**
 * @author Peter Abeles
 */
public class TestSubmatrixOps_DDRM {

    @Test
    public void setSubMatrix() {
        DMatrixRMaj A = new DMatrixRMaj(5,5);
        DMatrixRMaj B = new DMatrixRMaj(6,6);

        for( int i = 0; i < A.data.length; i++ ) {
            A.data[i] = 1;
        }

        SubmatrixOps_DDRM.setSubMatrix(A,B,1,1,2,3,2,3);

        // create a matrix that should be identical to B
        DMatrixRMaj C = new DMatrixRMaj(6,6);
        for( int i = 2; i < 4; i++ ) {
            for( int j = 3; j < 6; j++ ) {
                C.set(i,j,1);
            }
        }

        // see if they are the same
        EjmlUnitTests.assertEquals(B,C,0);
    }
}
