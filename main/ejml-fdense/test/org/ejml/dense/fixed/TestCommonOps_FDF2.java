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

package org.ejml.dense.fixed;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrix2;
import org.ejml.data.FMatrix2x2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestCommonOps_FDF2 extends CompareFixedToCommonOps_FDRM {

    public TestCommonOps_FDF2() {
        super(CommonOps_FDF2.class);
    }

    @Test
    public void diag() {
        FMatrix2x2 m = new FMatrix2x2(1,2,3,4);
        FMatrix2 found = new FMatrix2();

        CommonOps_FDF2.diag(m,found);

        assertEquals(1,found.a1, UtilEjml.TEST_F32);
        assertEquals(4,found.a2,UtilEjml.TEST_F32);
    }
}
