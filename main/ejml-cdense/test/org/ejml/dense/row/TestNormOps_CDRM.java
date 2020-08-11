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

package org.ejml.dense.row;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F32;
import org.ejml.data.CMatrixRMaj;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestNormOps_CDRM {

    Random rand = new Random(234);

    @Test
    public void normF() {
        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(1,7,rand);

        Complex_F32 b = new Complex_F32();
        float total = 0;
        for (int i = 0; i < a.numRows; i++) {
            for (int j = 0; j < a.numCols; j++) {
                a.get(i,j,b);
                total += b.real*b.real + b.imaginary*b.imaginary;
            }
        }

        float expected = (float)Math.sqrt(total);
        float found = NormOps_CDRM.normF(a);

        assertEquals(expected,found, UtilEjml.TEST_F32);
    }
}