/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestMatrixMultProduct {

    Random rand = new Random(2345);

    @Test
    public void outer() {
        DenseMatrix64F A = RandomMatrices.createRandom(20, 10, rand);
        DenseMatrix64F found = new DenseMatrix64F(20,20);
        DenseMatrix64F expected = new DenseMatrix64F(20,20);

        MatrixMatrixMult.multTransB(A, A, expected);
        MatrixMultProduct.outer(A, found);

        assertTrue(MatrixFeatures.isIdentical(expected, found, 1e-8));
    }

    @Test
    public void inner_small() {
        DenseMatrix64F A = RandomMatrices.createRandom(20, 10, rand);
        DenseMatrix64F found = new DenseMatrix64F(10,10);
        DenseMatrix64F expected = new DenseMatrix64F(10,10);

        MatrixMatrixMult.multTransA_reorder(A,A,expected);
        MatrixMultProduct.inner_small(A, found);

        assertTrue(MatrixFeatures.isIdentical(expected, found, 1e-8));
    }

    @Test
    public void inner_reorder() {
        DenseMatrix64F A = RandomMatrices.createRandom(20,10,rand);
        DenseMatrix64F found = new DenseMatrix64F(10,10);
        DenseMatrix64F expected = new DenseMatrix64F(10,10);

        MatrixMatrixMult.multTransA_reorder(A,A,expected);
        MatrixMultProduct.inner_reorder(A, found);

        assertTrue(MatrixFeatures.isIdentical(expected, found, 1e-8));
    }

    @Test
    public void inner_reorder_upper() {
        DenseMatrix64F A = RandomMatrices.createRandom(20,10,rand);
        DenseMatrix64F found = new DenseMatrix64F(10,10);
        DenseMatrix64F expected = new DenseMatrix64F(10,10);

        MatrixMatrixMult.multTransA_reorder(A,A,expected);
        MatrixMultProduct.inner_reorder_upper(A, found);

        // only check the upper triangle
        for( int i = 0; i < found.numRows; i++ ) {
            for( int j = i; j < found.numCols; j++ ) {
                assertEquals(expected.get(i,j),found.get(i,j),1e-8);
            }
        }
    }
}
