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

package org.ejml.dense.fixed;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrix3;
import org.ejml.data.DMatrix3x3;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestCommonOps_DDF3 extends CompareFixedToCommonOps_DDRM {

    Random rand = new Random(234);

    public TestCommonOps_DDF3() {
        super(CommonOps_DDF3.class);
    }

    @Test
    public void diag() {
        DMatrix3x3 m = new DMatrix3x3(1,2,3,4,5,6,7,8,9);
        DMatrix3 found = new DMatrix3();

        CommonOps_DDF3.diag(m,found);

        assertEquals(1,found.a1, UtilEjml.TEST_F64);
        assertEquals(5,found.a2,UtilEjml.TEST_F64);
        assertEquals(9,found.a3,UtilEjml.TEST_F64);
    }



}
