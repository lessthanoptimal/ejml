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

package org.ejml.sparse.csc;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.ops.DConvertMatrixStruct;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestNormOps_DSCC {

    Random rand = new Random(234);

    @Test
    public void fastNormF() {
        for( int length : new int[]{0,2,6,15,30} ) {
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,6,length,rand);
            DMatrixRMaj  Ad = DConvertMatrixStruct.convert(A,(DMatrixRMaj)null);

            double found = NormOps_DSCC.fastNormF(A);
            double expected = NormOps_DDRM.fastNormF(Ad);

            assertEquals(expected,found, UtilEjml.TEST_F64);
        }
    }

    @Test
    public void normF() {
        for( int length : new int[]{0,2,6,15,30} ) {
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,6,length,rand);
            DMatrixRMaj  Ad = DConvertMatrixStruct.convert(A,(DMatrixRMaj)null);

            double found = NormOps_DSCC.normF(A);
            double expected = NormOps_DDRM.normF(Ad);

            assertEquals(expected,found, UtilEjml.TEST_F64);
        }
    }
}