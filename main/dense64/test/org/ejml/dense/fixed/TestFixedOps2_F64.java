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
import org.ejml.data.DMatrixFixed2_F64;
import org.ejml.data.DMatrixFixed2x2_F64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestFixedOps2_F64 extends CompareFixedToCommonOps_F64 {

    public TestFixedOps2_F64() {
        super(FixedOps2_F64.class);
    }

    @Test
    public void diag() {
        DMatrixFixed2x2_F64 m = new DMatrixFixed2x2_F64(1,2,3,4);
        DMatrixFixed2_F64 found = new DMatrixFixed2_F64();

        FixedOps2_F64.diag(m,found);

        assertEquals(1,found.a1, UtilEjml.TEST_F64);
        assertEquals(4,found.a2,UtilEjml.TEST_F64);
    }
}
