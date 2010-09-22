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

package org.ejml.alg.block.decomposition.chol;

import org.ejml.alg.dense.decomposition.CholeskyDecomposition;
import org.ejml.alg.dense.decomposition.DecompositionFactory;
import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestBlockInnerCholesky {
    Random rand = new Random(234234);

    @Test
    public void upper() {
        int N = 5;

        DenseMatrix64F A = RandomMatrices.createSymmPosDef(N,rand);

        // decompose a DenseMatrix64F to find expected solution
        CholeskyDecomposition chol = DecompositionFactory.chol(N,false,false);
        assertTrue(chol.decompose(A));

        DenseMatrix64F expected = chol.getT(null);

        // copy the original data by an offset
        double data[] = new double[ A.getNumElements() + 2 ];
        System.arraycopy(A.data,0,data,2,A.getNumElements());

        // decompose using the algorithm
        assertTrue(BlockInnerCholesky.upper(data,2,N));

        DenseMatrix64F found = new DenseMatrix64F(N,N);
        System.arraycopy(data,2,found.data,0,found.data.length);

        // set lower triangular potion to be zero so that it is exactly the same
        assertTrue(GenericMatrixOps.isEquivalentTriangle(true,expected,found,1e-10));
    }
}
