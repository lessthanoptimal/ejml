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

package org.ejml.alg.dense.decomposition.chol;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.EjmlUnitTests;
import org.junit.Test;

import java.util.Random;

import static org.ejml.alg.dense.decomposition.CheckDecompositionInterface.checkModifiedInput;
import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestCholeskyDecompositionLDL {

    Random rand = new Random(0x45478);

        @Test
    public void checkModifyInput() {
        checkModifiedInput(new CholeskyDecompositionLDL());
    }

    @Test
    public void testDecompose() {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 1, 2, 4, 2, 7, 23, 4, 23, 98);


        DenseMatrix64F L = new DenseMatrix64F(3,3, true, 1, 0, 0, 2, 1, 0, 4, 5, 1);

        double D[] = new double[]{1,3,7};

        CholeskyDecompositionLDL cholesky = new CholeskyDecompositionLDL();
        assertTrue(cholesky.decompose(A));

        DenseMatrix64F foundL = cholesky.getL();

        EjmlUnitTests.assertEquals(L,foundL,1e-8);
        for( int i = 0; i < D.length; i++ ) {
            assertEquals(D[i],cholesky.getD()[i],1e-8);
        }
    }

    /**
     * If it is not positive definate it should fail
     */
    @Test
    public void testNotPositiveDefinate() {
        DenseMatrix64F A = new DenseMatrix64F(2,2, true, 1, -1, -1, -2);

        CholeskyDecompositionLDL alg = new CholeskyDecompositionLDL();
        assertFalse(alg.decompose(A));
    }
}