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
import org.ejml.data.FMatrix3;
import org.ejml.data.FMatrix3x3;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestCommonOps_FDF3 extends CompareFixedToCommonOps_FDRM {

    Random rand = new Random(234);

    public TestCommonOps_FDF3() {
        super(CommonOps_FDF3.class);
    }

    @Test
    public void diag() {
        FMatrix3x3 m = new FMatrix3x3(1,2,3,4,5,6,7,8,9);
        FMatrix3 found = new FMatrix3();

        CommonOps_FDF3.diag(m,found);

        assertEquals(1,found.a1, UtilEjml.TEST_F32);
        assertEquals(5,found.a2,UtilEjml.TEST_F32);
        assertEquals(9,found.a3,UtilEjml.TEST_F32);
    }



}
