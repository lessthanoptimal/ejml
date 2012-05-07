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

package org.ejml.alg.dense.decomposition.bidiagonal;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestBidiagonalDecompositionNaive {

    Random rand = new Random(6455);

    @Test
    public void testItAll() {

        checkAgainstRandom(7, 5);
        checkAgainstRandom(5, 7);
        checkAgainstRandom(2, 3);
    }

    private void checkAgainstRandom(int m, int n) {
        SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices.createRandom(m,n,rand));

        BidiagonalDecompositionNaive decomp = new BidiagonalDecompositionNaive();

        assertTrue(decomp.decompose(A.getMatrix()));

        SimpleMatrix U = decomp.getU();
        SimpleMatrix B = decomp.getB();
        SimpleMatrix V = decomp.getV();

//        U.print();
//        B.print();
//        V.print();

//        U.mult(A).mult(V).print();

        // check the decomposition
        DenseMatrix64F foundA = U.mult(B).mult(V.transpose()).getMatrix();

//        A.print();
//        foundA.print();

        assertTrue(MatrixFeatures.isIdentical(A.getMatrix(),foundA,1e-8));
    }

}