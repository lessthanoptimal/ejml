/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.fixed;

import org.ejml.UtilEjml;
import org.ejml.data.FixedMatrix3_64F;
import org.ejml.data.FixedMatrix3x3_64F;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestFixedOps3_D64 extends CompareFixedToCommonOps_D64 {

    Random rand = new Random(234);

    public TestFixedOps3_D64() {
        super(FixedOps3_D64.class);
    }

    @Test
    public void diag() {
        FixedMatrix3x3_64F m = new FixedMatrix3x3_64F(1,2,3,4,5,6,7,8,9);
        FixedMatrix3_64F found = new FixedMatrix3_64F();

        FixedOps3_D64.diag(m,found);

        assertEquals(1,found.a1, UtilEjml.TEST_64F);
        assertEquals(5,found.a2,UtilEjml.TEST_64F);
        assertEquals(9,found.a3,UtilEjml.TEST_64F);
    }



}
