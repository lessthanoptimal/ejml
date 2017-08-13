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
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.bidiagonal.BidiagonalDecompositionNaive_DDRM;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestBidiagonalDecompositionNaive_DDRM {

    Random rand = new Random(6455);

    @Test
    public void testItAll() {

        checkAgainstRandom(7, 5);
        checkAgainstRandom(5, 7);
        checkAgainstRandom(2, 3);
    }

    private void checkAgainstRandom(int m, int n) {
        SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_DDRM.rectangle(m,n,rand));

        BidiagonalDecompositionNaive_DDRM decomp = new BidiagonalDecompositionNaive_DDRM();

        assertTrue(decomp.decompose(A.getDDRM()));

        SimpleMatrix U = decomp.getU();
        SimpleMatrix B = decomp.getB();
        SimpleMatrix V = decomp.getV();

//        U.print();
//        B.print();
//        V.print();

//        U.mult(A).mult(V).print();

        // check the decomposition
        DMatrixRMaj foundA = U.mult(B).mult(V.transpose()).getDDRM();

//        A.print();
//        foundA.print();

        assertTrue(MatrixFeatures_DDRM.isIdentical(A.getDDRM(), foundA, UtilEjml.TEST_F64));
    }

}