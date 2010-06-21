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

package org.ejml.alg.dense.mult;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestUnrolledMatrixMult {

    Random rand = new Random(234324);

    int max = UnrolledMatrixMult.NUM_UNROLLED+1;

    @Test
    public void testMult() {
        for( int k = 1; k <= UnrolledMatrixMult.NUM_UNROLLED; k++ ) {
            for( int i = 1; i <= max; i++ ) {
                for( int j = 1; j <= max; j++ ) {
                    DenseMatrix64F A = RandomMatrices.createRandom(i,k,rand);
                    DenseMatrix64F B = RandomMatrices.createRandom(k,j,rand);
                    DenseMatrix64F found = RandomMatrices.createRandom(i,j,rand);
                    DenseMatrix64F expected = RandomMatrices.createRandom(i,j,rand);

                    MatrixMatrixMult.mult_aux(A,B,expected,null);
                    UnrolledMatrixMult.mult(A,B,found);

                    assertTrue(MatrixFeatures.isIdentical(expected,found,1e-8));
                }
            }
        }
    }
}
