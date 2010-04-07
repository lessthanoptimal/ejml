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

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class CheckDecompositionInterface {

    /**
     * See if it explodes when a matrix that is larger or smaller than
     * the expected size is decomposed.
     *
     * @param decomp The implementation being tested.
     */
    public static void checkExpectedMaxSize( DecompositionInterface decomp )
    {
        decomp.setExpectedMaxSize(2,2);

        DenseMatrix64F A = RandomMatrices.createRandom(4,4,new Random(0x434));
        decomp.decompose(A);

        decomp.setExpectedMaxSize(6,6);
        decomp.decompose(A);
    }
}
