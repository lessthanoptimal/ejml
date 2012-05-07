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

package org.ejml.alg.dense.decomposition;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class CheckDecompositionInterface {

    /**
     * Performs a decomposition and makes sure the input matrix is not modified.
     */
    public static boolean safeDecomposition( DecompositionInterface decomp , DenseMatrix64F A ) {

        DenseMatrix64F A_orig = decomp.inputModified() ? A.copy() : A;

        return decomp.decompose(A_orig);
    }


    /**
     * Checks to see if the matrix is or is not modified as according to the modified
     * flag.
     *
     * @param decomp
     */
    public static void checkModifiedInput( DecompositionInterface decomp ) {
        DenseMatrix64F A = RandomMatrices.createSymmPosDef(4,new Random(0x434));
        DenseMatrix64F A_orig = A.copy();

        assertTrue(decomp.decompose(A));

        boolean modified = !MatrixFeatures.isEquals(A,A_orig);

        assertTrue(decomp.inputModified()==modified);
    }
}
