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

package org.ejml.dense.row.misc;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestImplCommonOps_FDRM {

    Random rand = new Random(234324);

    @Test
    public void extract() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5, 5, 0, 1, rand);

        FMatrixRMaj B = new FMatrixRMaj(3,3);

        ImplCommonOps_FDRM.extract(A, 1, 2, B, 1, 0,2,3);

        for( int i = 1; i < 3; i++ ) {
            for( int j = 2; j < 5; j++ ) {
                assertEquals(A.get(i,j),B.get(i,j-2), UtilEjml.TEST_F32);
            }
        }
    }
}
