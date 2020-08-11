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
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


/**
 * @author Peter Abeles
 */
public class TestNormOps_FDRM {

    Random rand = new Random(234);

    FMatrixRMaj zeroMatrix = new FMatrixRMaj(3,4);
    FMatrixRMaj unzeroMatrix = new FMatrixRMaj(3,2, true, 0.2f, 1, -2, 3, 6, 5);
    FMatrixRMaj unzeroVector = new FMatrixRMaj(5,1, true, 0.3f, 1, -2, 3, 4);
    FMatrixRMaj squareMatrix = new FMatrixRMaj(2,2, true, 0.2f, 1, -2, 3);


    /**
     * Tests against the condition number from octave.
     */
    @Test
    public void conditionP() {
        float val = NormOps_FDRM.conditionP(squareMatrix,1);

        assertEquals(7.6923f,val,UtilEjml.TEST_F32_SQ);

        // check the non-square case
        val = NormOps_FDRM.conditionP(unzeroMatrix,1);

        assertEquals(3.4325f,val,UtilEjml.TEST_F32_SQ);

        // see if the other pseudo-inverse works
        FMatrixRMaj trans = unzeroMatrix.copy();
        CommonOps_FDRM.transpose(trans);
        val = NormOps_FDRM.conditionP(trans,1);

        assertEquals(3.4887f,val,UtilEjml.TEST_F32_SQ);
    }

    /**
     * Tests against the condition number from octave.
     */
    @Test
    public void conditionP2() {
         float val = NormOps_FDRM.conditionP2(unzeroMatrix);

        assertEquals(2.1655f,val,UtilEjml.TEST_F32_SQ);

        checkUncountable(NormOps_FDRM.conditionP2(zeroMatrix));
    }

    /**
     * Tested using the following operation in octave:
     *
     * sum(abs(a(:)).^3.5f)^(1/3.5f)
     */
    @Test
    public void elementP() {
         float val = NormOps_FDRM.elementP(unzeroMatrix,3.5f);

        assertEquals(6.9108f,val,UtilEjml.TEST_F32_SQ);

        checkUncountable(NormOps_FDRM.elementP(zeroMatrix,3.5f));
    }

    @Test
    public void fastElementP() {
         float val = NormOps_FDRM.fastElementP(unzeroMatrix,3.5f);

        assertEquals(6.9108f,val,UtilEjml.TEST_F32_SQ);

        checkUncountable(NormOps_FDRM.fastElementP(zeroMatrix,3.5f));
    }

    @Test
    public void normalizeF() {
        FMatrixRMaj a = unzeroVector.copy();

        NormOps_FDRM.normalizeF(a);

        assertEquals(1, NormOps_FDRM.normF(a),UtilEjml.TEST_F32);
    }

    @Test
    public void fastNormF() {
        float val = NormOps_FDRM.fastNormF(unzeroMatrix);

        assertEquals(8.6626f,val,UtilEjml.TEST_F32_SQ);

        checkUncountable(NormOps_FDRM.fastNormF(zeroMatrix));
    }

    @Test
    public void normF() {
        float val = NormOps_FDRM.normF(unzeroMatrix);

        assertEquals(8.6626f,val,UtilEjml.TEST_F32_SQ);

        checkUncountable(NormOps_FDRM.normF(zeroMatrix));
    }

    @Test
    public void fastNormP2() {
        // check induced matrix norm
        float found = NormOps_FDRM.fastNormP2(unzeroMatrix);
        float expected = NormOps_FDRM.inducedP2(unzeroMatrix);
        assertEquals(expected,found,UtilEjml.TEST_F32_SQ);

        // check vector norm
        found = NormOps_FDRM.fastNormP2(unzeroVector);
        expected = NormOps_FDRM.normF(unzeroVector);
        assertEquals(expected,found,UtilEjml.TEST_F32_SQ);
    }

    @Test
    public void normP() {
        // check induced matrix norm
        float found = NormOps_FDRM.normP(unzeroMatrix,2);
        float expected = NormOps_FDRM.inducedP2(unzeroMatrix);
        assertEquals(expected,found,UtilEjml.TEST_F32_SQ);

        // check vector norm
        found = NormOps_FDRM.normP(unzeroVector,2);
        expected = NormOps_FDRM.normF(unzeroVector);
        assertEquals(expected,found,UtilEjml.TEST_F32_SQ);
    }

    @Test
    public void fastNormP() {
        // check induced matrix norm
        float found = NormOps_FDRM.fastNormP(unzeroMatrix,2);
        float expected = NormOps_FDRM.inducedP2(unzeroMatrix);
        assertEquals(expected,found,UtilEjml.TEST_F32_SQ);

        // check vector norm
        found = NormOps_FDRM.fastNormP(unzeroVector,2);
        expected = NormOps_FDRM.normF(unzeroVector);
        assertEquals(expected,found,UtilEjml.TEST_F32_SQ);
    }

    @Test
    public void normP1() {
        // check induced matrix norm
        float found = NormOps_FDRM.normP1(unzeroMatrix);
        float expected = NormOps_FDRM.inducedP1(unzeroMatrix);
        assertEquals(expected,found,UtilEjml.TEST_F32_SQ);

        // check vector norm
        found = NormOps_FDRM.normP1(unzeroVector);
        expected = CommonOps_FDRM.elementSumAbs(unzeroVector);
        assertEquals(expected,found,UtilEjml.TEST_F32_SQ);
    }

    @Test
    public void normP2() {
        // check induced matrix norm
        float found = NormOps_FDRM.normP2(unzeroMatrix);
        float expected = NormOps_FDRM.inducedP2(unzeroMatrix);
        assertEquals(expected,found,UtilEjml.TEST_F32_SQ);

        // check vector norm
        found = NormOps_FDRM.normP2(unzeroVector);
        expected = NormOps_FDRM.normF(unzeroVector);
        assertEquals(expected,found,UtilEjml.TEST_F32_SQ);
    }

    @Test
    public void normPInf() {
        // check induced matrix norm
        float found = NormOps_FDRM.normPInf(unzeroMatrix);
        float expected = NormOps_FDRM.inducedPInf(unzeroMatrix);
        assertEquals(expected,found,UtilEjml.TEST_F32_SQ);

        // check vector norm
        found = NormOps_FDRM.normPInf(unzeroVector);
        expected = CommonOps_FDRM.elementMaxAbs(unzeroVector);
        assertEquals(expected,found,UtilEjml.TEST_F32_SQ);
    }

    @Test
    public void inducedP1() {
        float val = NormOps_FDRM.inducedP1(unzeroMatrix);
        assertEquals(9,val,UtilEjml.TEST_F32_SQ);

        checkUncountable(NormOps_FDRM.inducedP1(zeroMatrix));
    }

    @Test
    public void inducedP2() {
        float val = NormOps_FDRM.inducedP2(unzeroMatrix);
        assertEquals(7.8645f,val,UtilEjml.TEST_F32_SQ);

        checkUncountable(NormOps_FDRM.inducedP2(zeroMatrix));

        // make sure the largest singular value is being returned not just the first
        for( int i = 0; i < 20; i++ ) {
            SimpleMatrix A = SimpleMatrix.random_FDRM(5,5,-10,10,rand);
            float largest = (float)A.svd().getW().get(0);

            assertEquals(largest, NormOps_FDRM.inducedP2(A.getFDRM()), UtilEjml.TEST_F32);
        }
    }

    @Test
    public void inducedPInf() {
        float val = NormOps_FDRM.inducedPInf(unzeroMatrix);
        assertEquals(11,val,UtilEjml.TEST_F32_SQ);

        checkUncountable(NormOps_FDRM.inducedPInf(zeroMatrix));
    }

    private static void checkUncountable( float val ) {
        assertFalse(Float.isInfinite(val));
        assertFalse(Float.isNaN(val));
    }
}
