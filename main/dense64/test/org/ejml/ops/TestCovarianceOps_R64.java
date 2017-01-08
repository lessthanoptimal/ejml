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

package org.ejml.ops;

import org.ejml.data.RowMatrix_F64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestCovarianceOps_R64 {
    @Test
    public void isValid() {
        // nothing is wrong with it
        RowMatrix_F64 m = CommonOps_R64.identity(3);
        assertEquals(0, CovarianceOps_R64.isValid(m));

        // negative diagonal term
        m.set(1,1,-3);
        assertEquals(1, CovarianceOps_R64.isValid(m));

        // not symetric
        m = CommonOps_R64.identity(3);
        m.set(1,0,30);
        assertEquals(2, CovarianceOps_R64.isValid(m));

        // not positive definite
        m = CommonOps_R64.identity(3);
        m.set(1,2,-400);
        m.set(2,1,-400);
        assertEquals(3, CovarianceOps_R64.isValid(m));
    }
}
