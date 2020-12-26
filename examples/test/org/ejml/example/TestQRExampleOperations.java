/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestQRExampleOperations {

    Random rand = new Random(23423);

    @Test void basic() {
        checkMatrix(7,5);
        checkMatrix(5,5);
        checkMatrix(7,7);
    }

    private void checkMatrix( int numRows , int numCols ) {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(numRows,numCols,-1,1,rand);

        QRExampleOperations alg = new QRExampleOperations();

        alg.decompose(A);

        DMatrixRMaj Q = alg.getQ();
        DMatrixRMaj R = alg.getR();

        DMatrixRMaj A_found = new DMatrixRMaj(numRows,numCols);
        CommonOps_DDRM.mult(Q,R,A_found);

        assertTrue( MatrixFeatures_DDRM.isIdentical(A,A_found, UtilEjml.TEST_F64));
    }
}