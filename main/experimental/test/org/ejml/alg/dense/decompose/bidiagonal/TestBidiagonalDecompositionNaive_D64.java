/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml.alg.dense.decompose.bidiagonal;

import org.ejml.alg.dense.decomposition.bidiagonal.BidiagonalDecompositionNaive_D64;
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
public class TestBidiagonalDecompositionNaive_D64 {

    Random rand = new Random(6455);

    @Test
    public void testItAll() {

        checkAgainstRandom(7, 5);
        checkAgainstRandom(5, 7);
        checkAgainstRandom(2, 3);
    }

    private void checkAgainstRandom(int m, int n) {
        SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices.createRandom(m,n,rand));

        BidiagonalDecompositionNaive_D64 decomp = new BidiagonalDecompositionNaive_D64();

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

        assertTrue(MatrixFeatures.isIdentical(A.getMatrix(), foundA, 1e-8));
    }

}