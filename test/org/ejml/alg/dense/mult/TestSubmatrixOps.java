/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
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
