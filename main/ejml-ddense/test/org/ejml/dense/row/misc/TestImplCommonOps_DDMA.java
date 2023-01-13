/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.misc;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestImplCommonOps_DDMA extends EjmlStandardJUnit {
    @Test void extract() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5, 5, 0, 1, rand);

        DMatrixRMaj B = new DMatrixRMaj(3,3);

        ImplCommonOps_DDMA.extract(A, 1, 2, B, 1, 0,2,3);

        for( int i = 1; i < 3; i++ ) {
            for( int j = 2; j < 5; j++ ) {
                assertEquals(A.get(i,j),B.get(i,j-2), UtilEjml.TEST_F64);
            }
        }
    }
}
