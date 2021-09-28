/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.block.linsol.qr;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRBlock;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestQrHouseHolderSolver_MT_DDRB extends EjmlStandardJUnit {
    int r = 3;

    @Test
    void compareToSingle() {
        var single = new QrHouseHolderSolver_DDRB();
        var concurrent = new QrHouseHolderSolver_MT_DDRB();

        for( int i = 1; i <= r*3; i++ ) {
            for( int j = i; j <= r*3; j++ ) {
                for( int k = 1; k <= r*3; k++ ) {
//                    System.out.println("i = "+i+" j = "+j+" k = "+k);
                    DMatrixRBlock A = MatrixOps_DDRB.createRandom(j,i,-1,1,rand,r);
                    DMatrixRBlock AA = A.copy();
                    DMatrixRBlock B = MatrixOps_DDRB.createRandom(j,k,-1,1,rand,r);
                    DMatrixRBlock BB = B.copy();
                    DMatrixRBlock X = MatrixOps_DDRB.createRandom(i,k,-1,1,rand,r);
                    DMatrixRBlock XX = X.copy();

                    assertTrue(single.setA(A));
                    assertTrue(concurrent.setA(AA));
                    assertTrue(MatrixOps_DDRB.isEquals(A,AA, UtilEjml.TEST_F64));

                    single.solve(B,X);
                    concurrent.solve(BB,XX);
                    assertTrue(MatrixOps_DDRB.isEquals(B,BB, UtilEjml.TEST_F64));
                    assertTrue(MatrixOps_DDRB.isEquals(X,XX, UtilEjml.TEST_F64));
                }
            }
        }
    }
}

