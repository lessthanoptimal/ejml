/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.FMatrixSparseCSC;
import org.ejml.dense.row.NormOps_FDRM;
import org.ejml.ops.ConvertFMatrixStruct;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestNormOps_FSCC {

    Random rand = new Random(234);

    @Test
    public void fastNormF() {
        for( int length : new int[]{0,2,6,15,30} ) {
            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(6,6,length,rand);
            FMatrixRMaj  Ad = ConvertFMatrixStruct.convert(A,(FMatrixRMaj)null);

            float found = NormOps_FSCC.fastNormF(A);
            float expected = NormOps_FDRM.fastNormF(Ad);

            assertEquals(expected,found, UtilEjml.TEST_F32);
        }
    }

    @Test
    public void normF() {
        for( int length : new int[]{0,2,6,15,30} ) {
            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(6,6,length,rand);
            FMatrixRMaj  Ad = ConvertFMatrixStruct.convert(A,(FMatrixRMaj)null);

            float found = NormOps_FSCC.normF(A);
            float expected = NormOps_FDRM.normF(Ad);

            assertEquals(expected,found, UtilEjml.TEST_F32);
        }
    }
}