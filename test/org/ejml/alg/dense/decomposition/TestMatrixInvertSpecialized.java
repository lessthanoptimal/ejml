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

package org.ejml.alg.dense.decomposition;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class TestMatrixInvertSpecialized {

    @Test
    public void test2x2() {
        double a[] = new double[]
                { 0.784651,   0.303882,
                        0.277953,   0.067404};
        double a_inv[] = new double[]
                { -2.1346,    9.6238,
                        8.8026,  -24.8495 };

        MatrixInvertSpecialized.invert2x2(a);

        for( int i = 0; i < a.length; i++ ) {
            assertEquals(a_inv[i],a[i],1e-3);
        }
    }

    /**
     * A random matrix was generated and its inverse found using octave.
     */
    @Test
    public void test3x3() {
        double a[] = new double[]
                       {-0.87988 , 1.77209 ,1.99559 ,
                        2.42698 ,  2.42180  , 0.41228 ,
                        -1.56637 ,  1.92917  ,-2.42619 };
        double a_inv[] = new double[]
                {
                        -0.207994 ,  0.254080 , -0.127903 ,
                        0.163453 ,  0.164016 ,  0.162315 ,
                        0.264251 , -0.033620 , -0.200530
                };

        MatrixInvertSpecialized.invert3x3(a);

        for( int i = 0; i < a.length; i++ ) {
            assertEquals(a_inv[i],a[i],1e-6);
        }
    }
}
