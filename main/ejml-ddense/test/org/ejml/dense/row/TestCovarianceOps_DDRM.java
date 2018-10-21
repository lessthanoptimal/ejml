/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.DMatrixRMaj;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestCovarianceOps_DDRM {
    @Test
    public void invert_1x1() {
        DMatrixRMaj m = new DMatrixRMaj(1,1);
        m.set(0,0,2);
        DMatrixRMaj n = new DMatrixRMaj(1,1);
        CovarianceOps_DDRM.invert(m,n);
        assertEquals(0.5,n.get(0,0), UtilEjml.TEST_F64);
    }

    @Test
    public void isValid() {
        // nothing is wrong with it
        DMatrixRMaj m = CommonOps_DDRM.identity(3);
        assertEquals(0, CovarianceOps_DDRM.isValid(m));

        // negative diagonal term
        m.set(1,1,-3);
        assertEquals(1, CovarianceOps_DDRM.isValid(m));

        // not symetric
        m = CommonOps_DDRM.identity(3);
        m.set(1,0,30);
        assertEquals(2, CovarianceOps_DDRM.isValid(m));

        // not positive definite
        m = CommonOps_DDRM.identity(3);
        m.set(1,2,-400);
        m.set(2,1,-400);
        assertEquals(3, CovarianceOps_DDRM.isValid(m));
    }
}
