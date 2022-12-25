/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
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

package org.ejml.data;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public abstract class GenericTestsZMatrixD1 extends GenericTestsZMatrix {

    @Override
    protected abstract ZMatrixD1 createMatrix( int numRows, int numCols );

    @Override
    public void allTests() {
        super.allTests();
        testReshape();
        testReshape_Negative();
        testReshape_Zero();
    }

    public void testReshape() {
        ZMatrixD1 mat = createMatrix(3, 2);

        double[] origData = mat.getData();

        mat.reshape(6, 1);

        assertSame(origData, mat.getData());
        assertEquals(1, mat.getNumCols());
        assertEquals(6, mat.getNumRows());
    }


    public void testReshape_Negative() {
        ReshapeMatrix mat = createMatrix(2, 2);

        // set each axis to negative independently to ensure they are both checked
        try {
            mat.reshape(-1, 2);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException ignore) {
            // it should throw an exception. Now stop bothering me error prone!
        }

        try {
            mat.reshape(2, -1);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException ignore) {
            // it should throw an exception. Now stop bothering me error prone!
        }

        // Turns out it was only throwing an exception because the array size is negative when rows*cols
        try {
            mat.reshape(-1, -1);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException ignore) {
            // it should throw an exception. Now stop bothering me error prone!
        }
    }

    /**
     * 0x0 matrix should be allowed.
     */
    public void testReshape_Zero() {
        ReshapeMatrix mat = createMatrix(2, 3);
        mat.reshape(0, 0);
        assertEquals(0, mat.getNumRows());
        assertEquals(0, mat.getNumCols());
    }
}