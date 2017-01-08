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

package org.ejml;

import org.ejml.data.RowMatrix_F64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestUtilEjml {

    Random rand = new Random(23423);


    @Test
    public void max_array() {
        double a[] = new double[]{-1,2,3,4,5,6,3,4,5,7,8,2,3,-5,-6};

        assertTrue(8==UtilEjml.max(a,0,a.length));
        assertTrue(5==UtilEjml.max(a,6,3));
    }

    /**
     * Provide it a couple of different strings to parse.  Then compare
     * the results against the expect answer
     */
    @Test
    public void testParseMatrix() {
        String a = "-0.779094   1.682750\n" +
                 "   1.304014  -1.880739\n";

        RowMatrix_F64 m = UtilEjml.parse_R64(a,2);

        assertEquals(2,m.numCols);
        assertEquals(2,m.numRows);
        assertEquals(-0.779094 , m.get(0,0) , 1e-5);
        assertEquals(1.682750  , m.get(0,1)  , 1e-5);
        assertEquals(1.304014  , m.get(1,0)  , 1e-5);
        assertEquals(-1.880739 , m.get(1,1) , 1e-5);

        // give it a matrix with a space in the first element, see if that screws it up
        a = " -0.779094   1.682750  5\n" +
           "   1.304014  -1.880739  8\n";

        m = UtilEjml.parse_R64(a,3);
        assertEquals(3,m.numCols);
        assertEquals(2,m.numRows);
        assertEquals(-0.779094 , m.get(0,0) , 1e-5);
    }
}
