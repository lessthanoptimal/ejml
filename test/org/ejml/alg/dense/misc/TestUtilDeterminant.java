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

package org.ejml.alg.dense.misc;

import org.ejml.data.DenseMatrix64F;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestUtilDeterminant {


    @Test
    public void test2x2() {
        double[] mat = new double[]{5 ,-2 , 0.1, 91};

        double val = UtilDeterminant.det2by2(mat);

        assertEquals(455.20,val,1e-6);
    }

    @Test
    public void test3x3() {
        double[] mat = new double[]{5 ,-2 ,-4, 0.1 ,91 ,8, 1 ,-2, 10};

        double val = UtilDeterminant.det3by3(mat);

        assertEquals(4980.8,val,1e-6);
    }

    @Test
    public void test4x4() {
        double[] mat = new double[]{5 ,-2 ,-4 ,0.5, 0.1, 91, 8, 66, 1, -2, 10, -4, -0.2, 7, -4, 0.8};

        double val = UtilDeterminant.det4by4(mat);

        assertEquals(-27288.86,val,1e-6);
    }

    @Test
    public void detRecursive() {
        double[] d = new double[]{5 ,-2 ,-4 ,0.5, 0.1, 91, 8, 66, 1, -2, 10, -4, -0.2, 7, -4, 0.8};

        DenseMatrix64F mat = new DenseMatrix64F(4,4, true, d);

        double val1 = UtilDeterminant.detRecursive(mat);
        double val2 = UtilDeterminant.det4by4(d);

        assertEquals(val2,val1,1e-6);
    }
}
