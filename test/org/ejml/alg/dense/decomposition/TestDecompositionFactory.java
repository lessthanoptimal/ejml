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

package org.ejml.alg.dense.decomposition;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestDecompositionFactory {

    Random rand = new Random(234234);

    @Test
    public void quality_eig() {
        // I'm assuming it can process this matrix with no problems
        DenseMatrix64F A = RandomMatrices.createSymmetric(5,-1,1,rand);

        EigenDecomposition<DenseMatrix64F> eig = DecompositionFactory.eig(A.numRows);

        assertTrue(eig.decompose(A));

        double origQuality = DecompositionFactory.quality(A,eig);

        // Mess up the EVD so that it will be of poor quality
        eig.getEigenVector(2).set(2,0,5);

        double modQuality = DecompositionFactory.quality(A,eig);

        assertTrue(origQuality < modQuality);
        assertTrue(origQuality < 1e-14);
    }

    @Test
    public void quality_svd() {
        // I'm assuming it can process this matrix with no problems
        DenseMatrix64F A = RandomMatrices.createRandom(4,5,rand);

        SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(A.numRows,A.numCols);

        assertTrue(svd.decompose(A));

        double origQuality = DecompositionFactory.quality(A,svd);

        // Mess up the SVD so that it will be of poor quality
        svd.getSingularValues()[2] = 5;

        double modQuality = DecompositionFactory.quality(A,svd);

        assertTrue(origQuality < modQuality);
        assertTrue(origQuality < 1e-14);
    }
}
