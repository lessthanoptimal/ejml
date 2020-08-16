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

package org.ejml.data;

import org.ejml.UtilEjml;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;


/**
 * @author Peter Abeles
 */
public class TestDMatrixRBlock {

    @Test
    public void testGeneric() {
        GenericTestsDMatrixD1 g;
        g = new GenericTestsDMatrixD1() {
            @Override
            protected DMatrixD1 createMatrix(int numRows, int numCols) {
                return new DMatrixRBlock(numRows,numCols,10);
            }
        };

        g.allTests();
    }

    @Test
    public void constructor_double_array() {
        double[] foo = new double[]{1,2,3};

        DMatrixRMaj m = new DMatrixRMaj(foo);
        assertEquals(3,m.numRows);
        assertEquals(1,m.numCols);
        assertNotSame(foo, m.data);
        for (int i = 0; i < foo.length; i++) {
            assertEquals(foo[i],m.get(i), UtilEjml.TEST_F64);
        }
    }

    @Test
    public void constructor_double2_array() {
        double[][] foo = new double[][]{{1},{2},{3}};

        DMatrixRMaj m = new DMatrixRMaj(foo);
        assertEquals(3,m.numRows);
        assertEquals(1,m.numCols);
        for (int i = 0; i < foo.length; i++) {
            assertEquals(foo[i][0],m.get(i), UtilEjml.TEST_F64);
        }
    }

}
