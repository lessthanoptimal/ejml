/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.decompose.bidiagonal;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.row.MatrixFeatures_R64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.ejml.dense.row.decomposition.bidiagonal.BidiagonalDecompositionNaive_R64;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestBidiagonalDecompositionNaive_R64 {

    Random rand = new Random(6455);

    @Test
    public void testItAll() {

        checkAgainstRandom(7, 5);
        checkAgainstRandom(5, 7);
        checkAgainstRandom(2, 3);
    }

    private void checkAgainstRandom(int m, int n) {
        SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_R64.createRandom(m,n,rand));

        BidiagonalDecompositionNaive_R64 decomp = new BidiagonalDecompositionNaive_R64();

        assertTrue(decomp.decompose(A.matrix_F64()));

        SimpleMatrix U = decomp.getU();
        SimpleMatrix B = decomp.getB();
        SimpleMatrix V = decomp.getV();

//        U.print();
//        B.print();
//        V.print();

//        U.mult(A).mult(V).print();

        // check the decomposition
        DMatrixRow_F64 foundA = U.mult(B).mult(V.transpose()).matrix_F64();

//        A.print();
//        foundA.print();

        assertTrue(MatrixFeatures_R64.isIdentical(A.matrix_F64(), foundA, UtilEjml.TEST_F64));
    }

}