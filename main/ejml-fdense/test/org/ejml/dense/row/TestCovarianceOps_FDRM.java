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
import org.ejml.data.FMatrixRMaj;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestCovarianceOps_FDRM {
    @Test
    public void invert_1x1() {
        FMatrixRMaj m = new FMatrixRMaj(1,1);
        m.set(0,0,2);
        FMatrixRMaj n = new FMatrixRMaj(1,1);
        CovarianceOps_FDRM.invert(m,n);
        assertEquals(0.5f,n.get(0,0), UtilEjml.TEST_F32);
    }

    @Test
    public void isValid() {
        // nothing is wrong with it
        FMatrixRMaj m = CommonOps_FDRM.identity(3);
        assertEquals(0, CovarianceOps_FDRM.isValid(m));

        // negative diagonal term
        m.set(1,1,-3);
        assertEquals(1, CovarianceOps_FDRM.isValid(m));

        // not symetric
        m = CommonOps_FDRM.identity(3);
        m.set(1,0,30);
        assertEquals(2, CovarianceOps_FDRM.isValid(m));

        // not positive definite
        m = CommonOps_FDRM.identity(3);
        m.set(1,2,-400);
        m.set(2,1,-400);
        assertEquals(3, CovarianceOps_FDRM.isValid(m));
    }
}
