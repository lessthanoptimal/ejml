/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

package org.ejml;

import org.ejml.data.DenseMatrix64F;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestUtilEjml {

    Random rand = new Random(23423);

    /**
     * Provide it a couple of different strings to parse.  Then compare
     * the results against the expect answer
     */
    @Test
    public void testParseMatrix() {
        String a = "-0.779094   1.682750\n" +
                 "   1.304014  -1.880739\n";

        DenseMatrix64F m = UtilEjml.parseMatrix(a,2);

        assertEquals(2,m.numCols);
        assertEquals(2,m.numRows);
        assertEquals(-0.779094 , m.get(0,0) , 1e-5);
        assertEquals(1.682750  , m.get(0,1)  , 1e-5);
        assertEquals(1.304014  , m.get(1,0)  , 1e-5);
        assertEquals(-1.880739 , m.get(1,1) , 1e-5);

        // give it a matrix with a space in the first element, see if that screws it up
        a = " -0.779094   1.682750  5\n" +
           "   1.304014  -1.880739  8\n";

        m = UtilEjml.parseMatrix(a,3);
        assertEquals(3,m.numCols);
        assertEquals(2,m.numRows);
        assertEquals(-0.779094 , m.get(0,0) , 1e-5);
    }
}
