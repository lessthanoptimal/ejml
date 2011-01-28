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

package org.ejml.alg.dense.decomposition.bidiagonal;

import org.ejml.alg.dense.decomposition.CheckDecompositionInterface;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.SimpleMatrix;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public abstract class GenericBidiagonalCheck {
    Random rand = new Random(0xff);

    abstract protected BidiagonalDecomposition<DenseMatrix64F> createQRDecomposition();

    @Test
    public void testModifiedInput() {
        CheckDecompositionInterface.checkModifiedInput(createQRDecomposition());
    }

    /**
     * Decomposes the matrix and then recomposes it.
     */
    @Test
    public void testUsingDefinition() {
        for( int i = 1; i <= 5; i++ ) {
            for( int j = 1; j <= 5; j++ ) {
                checkDefinition(i,j);
            }
        }
    }

    private void checkDefinition(int m, int n) {
        SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices.createRandom(m,n,rand));

        BidiagonalDecomposition<DenseMatrix64F> decomp = createQRDecomposition();

        assertTrue(decomp.decompose(A.getMatrix().copy()));

        SimpleMatrix U = SimpleMatrix.wrap(decomp.getU(null,false,false));
        SimpleMatrix B = SimpleMatrix.wrap(decomp.getB(null,false));
        SimpleMatrix V = SimpleMatrix.wrap(decomp.getV(null,false,false));

        DenseMatrix64F foundA = U.mult(B).mult(V.transpose()).getMatrix();

//        A.print();
//        foundA.print();

        assertTrue(MatrixFeatures.isIdentical(A.getMatrix(),foundA,1e-8));
    }

    /**
     * Sees if the compact and transpose flag are correctly handled.  Does
     * a permutation of each
     */
    @Test
    public void testCompactTranspose() {

        for( int i = 0; i < 2; i++ ) {
            boolean isTransposed = i == 0;
            for( int j = 0; j < 2; j++ ) {
                boolean isCompact = j == 0;

//                fail("Implement");
            }
        }

    }

}
