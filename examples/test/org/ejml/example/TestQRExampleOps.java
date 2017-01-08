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

package org.ejml.example;

import org.ejml.UtilEjml;
import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.MatrixFeatures_R64;
import org.ejml.ops.RandomMatrices_R64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestQRExampleOps {

    Random rand = new Random(23423);


    @Test
    public void basic() {
        checkMatrix(7,5);
        checkMatrix(5,5);
        checkMatrix(7,7);
    }

    private void checkMatrix( int numRows , int numCols ) {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(numRows,numCols,-1,1,rand);

        QRExampleOperations alg = new QRExampleOperations();

        alg.decompose(A);

        RowMatrix_F64 Q = alg.getQ();
        RowMatrix_F64 R = alg.getR();

        RowMatrix_F64 A_found = new RowMatrix_F64(numRows,numCols);
        CommonOps_R64.mult(Q,R,A_found);

        assertTrue( MatrixFeatures_R64.isIdentical(A,A_found, UtilEjml.TEST_F64));
    }


}