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

package org.ejml.alg.dense.decomposition.chol;

import org.ejml.alg.dense.decomposition.CholeskyDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.UtilTestMatrix;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public abstract class GenericCholeskyTests {
    Random rand = new Random(0x45478);

    boolean canL = true;
    boolean canR = true;

    public abstract CholeskyDecomposition create( boolean lower );

    @Test
    public void testDecomposeL() {
        if( !canL ) return;

        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);


        DenseMatrix64F L = new DenseMatrix64F(3,3, true, 1, 0, 0, 2, 3, 0, 4, 5, 7);

        CholeskyDecomposition cholesky = create(true);
        assertTrue(cholesky.decompose(A));

        DenseMatrix64F foundL = cholesky.getT(null);

        UtilTestMatrix.checkEquals(L,foundL);
    }

    @Test
    public void testDecomposeR() {
        if( !canR ) return;

        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);


        DenseMatrix64F R = new DenseMatrix64F(3,3, true, 1, 2, 4, 0, 3, 5, 0, 0, 7);

        CholeskyDecomposition cholesky = create(false);
        assertTrue(cholesky.decompose(A));

        DenseMatrix64F foundR = cholesky.getT(null);

        UtilTestMatrix.checkEquals(R,foundR);
    }

    /**
     * If it is not positive definate it should fail
     */
    @Test
    public void testNotPositiveDefinite() {
        DenseMatrix64F A = new DenseMatrix64F(2,2, true, 1, -1, -1, -2);

        CholeskyDecomposition alg = create(true);
        assertFalse(alg.decompose(A));
    }

    /**
     * The correctness of getT(null) has been tested else where effectively.  This
     * checks to see if it handles the case where an input is provided correctly.
     */
    @Test
    public void getT() {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);

        CholeskyDecomposition cholesky = create(true);

        assertTrue(cholesky.decompose(A));

        DenseMatrix64F L_null = cholesky.getT(null);
        DenseMatrix64F L_provided = RandomMatrices.createRandom(3,3,rand);
        assertTrue( L_provided == cholesky.getT(L_provided));

        assertTrue(MatrixFeatures.isIdentical(L_null,L_provided));
    }
}
