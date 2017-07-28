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

package org.ejml.sparse.csc.decomposition.qr;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestQrLeftLookingDecomposition_DSCC {

    Random rand = new Random(234);

    @Test
    public void stuff() {

        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(10,5,48,rand);

        A.print();

        QrLeftLookingDecomposition_DSCC alg = new QrLeftLookingDecomposition_DSCC(null);

        assertTrue(alg.decompose(A));

        DMatrixSparseCSC Q = alg.getQ(null,false);
        DMatrixSparseCSC R = alg.getR(null,false);

        DMatrixSparseCSC found = new DMatrixSparseCSC(10,5,0);
        CommonOps_DSCC.mult(Q,R,found);

        EjmlUnitTests.assertEquals(A,found, UtilEjml.TEST_F64);
    }
}