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

package org.ejml.dense.row.decomposition.eig;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.NormOps_FDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestEigenPowerMethod_FDRM {

    Random rand = new Random(0x34234);

    /**
     * Test it against a case
     */
    @Test
    public void computeDirect() {
        float dataA[] = new float[]{
                0.499765f ,  0.626231f ,  0.759554f,
                0.850879f ,  0.104374f ,  0.247645f ,
                0.069614f ,  0.155754f  , 0.380435f };

        FMatrixRMaj A = new FMatrixRMaj(3,3, true, dataA);

        EigenPowerMethod_FDRM power = new EigenPowerMethod_FDRM(3);
        power.setOptions(100, UtilEjml.TEST_F32);

        assertTrue(power.computeDirect(A));

        FMatrixRMaj v = power.getEigenVector();

        NormOps_FDRM.normalizeF(v);

        assertEquals(0.75678f,v.get(0,0),UtilEjml.TEST_F32_SQ);
        assertEquals(0.62755f,v.get(1,0),UtilEjml.TEST_F32_SQ);
        assertEquals(0.18295f,v.get(2,0),UtilEjml.TEST_F32_SQ);
    }

    @Test
    public void computeShiftDirect() {
        float dataA[] = new float[]{
                0.499765f ,  0.626231f ,  0.759554f,
                0.850879f ,  0.104374f ,  0.247645f ,
                0.069614f ,  0.155754f  , 0.380435f };

        FMatrixRMaj A = new FMatrixRMaj(3,3, true, dataA);

        EigenPowerMethod_FDRM power = new EigenPowerMethod_FDRM(3);
        power.setOptions(100, UtilEjml.TEST_F32);

        assertTrue(power.computeShiftDirect(A,0.2f));

        FMatrixRMaj v = power.getEigenVector();

        NormOps_FDRM.normalizeF(v);

        assertEquals(0.75678f,v.get(0,0),UtilEjml.TEST_F32_SQ);
        assertEquals(0.62755f,v.get(1,0),UtilEjml.TEST_F32_SQ);
        assertEquals(0.18295f,v.get(2,0),UtilEjml.TEST_F32_SQ);
    }

    @Test
    public void computeShiftInvert() {
        float dataA[] = new float[]{
                0.499765f ,  0.626231f ,  0.759554f,
                0.850879f ,  0.104374f ,  0.247645f ,
                0.069614f ,  0.155754f  , 0.380435f };

        FMatrixRMaj A = new FMatrixRMaj(3,3, true, dataA);

        EigenPowerMethod_FDRM power = new EigenPowerMethod_FDRM(3);
        power.setOptions(100, UtilEjml.TEST_F32);

        // a tried a few values for psi until I found one that converged
        assertTrue(power.computeShiftInvert(A,1.1f));

        FMatrixRMaj v = power.getEigenVector();

        NormOps_FDRM.normalizeF(v);

        assertEquals(0.75678f,v.get(0,0),UtilEjml.TEST_F32_SQ);
        assertEquals(0.62755f,v.get(1,0),UtilEjml.TEST_F32_SQ);
        assertEquals(0.18295f,v.get(2,0),UtilEjml.TEST_F32_SQ);
    }
}
