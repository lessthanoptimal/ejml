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

package org.ejml.dense.row.decomposition.eig;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.row.NormOps_R64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestEigenPowerMethod_R64 {

    Random rand = new Random(0x34234);

    /**
     * Test it against a case
     */
    @Test
    public void computeDirect() {
        double dataA[] = new double[]{
                0.499765 ,  0.626231 ,  0.759554,
                0.850879 ,  0.104374 ,  0.247645 ,
                0.069614 ,  0.155754  , 0.380435 };

        DMatrixRow_F64 A = new DMatrixRow_F64(3,3, true, dataA);

        EigenPowerMethod_R64 power = new EigenPowerMethod_R64(3);
        power.setOptions(100, UtilEjml.TEST_F64);

        assertTrue(power.computeDirect(A));

        DMatrixRow_F64 v = power.getEigenVector();

        NormOps_R64.normalizeF(v);

        assertEquals(0.75678,v.get(0,0),UtilEjml.TEST_F64_SQ);
        assertEquals(0.62755,v.get(1,0),UtilEjml.TEST_F64_SQ);
        assertEquals(0.18295,v.get(2,0),UtilEjml.TEST_F64_SQ);
    }

    @Test
    public void computeShiftDirect() {
        double dataA[] = new double[]{
                0.499765 ,  0.626231 ,  0.759554,
                0.850879 ,  0.104374 ,  0.247645 ,
                0.069614 ,  0.155754  , 0.380435 };

        DMatrixRow_F64 A = new DMatrixRow_F64(3,3, true, dataA);

        EigenPowerMethod_R64 power = new EigenPowerMethod_R64(3);
        power.setOptions(100, UtilEjml.TEST_F64);

        assertTrue(power.computeShiftDirect(A,0.2));

        DMatrixRow_F64 v = power.getEigenVector();

        NormOps_R64.normalizeF(v);

        assertEquals(0.75678,v.get(0,0),UtilEjml.TEST_F64_SQ);
        assertEquals(0.62755,v.get(1,0),UtilEjml.TEST_F64_SQ);
        assertEquals(0.18295,v.get(2,0),UtilEjml.TEST_F64_SQ);
    }

    @Test
    public void computeShiftInvert() {
        double dataA[] = new double[]{
                0.499765 ,  0.626231 ,  0.759554,
                0.850879 ,  0.104374 ,  0.247645 ,
                0.069614 ,  0.155754  , 0.380435 };

        DMatrixRow_F64 A = new DMatrixRow_F64(3,3, true, dataA);

        EigenPowerMethod_R64 power = new EigenPowerMethod_R64(3);
        power.setOptions(100, UtilEjml.TEST_F64);

        // a tried a few values for psi until I found one that converged
        assertTrue(power.computeShiftInvert(A,1.1));

        DMatrixRow_F64 v = power.getEigenVector();

        NormOps_R64.normalizeF(v);

        assertEquals(0.75678,v.get(0,0),UtilEjml.TEST_F64_SQ);
        assertEquals(0.62755,v.get(1,0),UtilEjml.TEST_F64_SQ);
        assertEquals(0.18295,v.get(2,0),UtilEjml.TEST_F64_SQ);
    }
}
