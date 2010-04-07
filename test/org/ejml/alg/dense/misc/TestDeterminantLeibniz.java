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
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestDeterminantLeibniz {

    /**
     * Compares this formuation to the naive recursive formulation
     */
    @Test
    public void compareToRecursive() {
        Random rand = new Random(0xff);

        for( int i = 1; i <= 5; i++ ) {
            DenseMatrix64F A = RandomMatrices.createRandom(i,i,rand);

            double expected = UtilDeterminant.detRecursive(A);
            double found = DeterminantLeibniz.det(A);

            assertEquals(expected,found,1e-8);
        }
    }
}
