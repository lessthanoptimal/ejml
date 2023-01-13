/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestNormOps_DDRM extends EjmlStandardJUnit {
    DMatrixRMaj zeroMatrix = new DMatrixRMaj(3, 4);
    DMatrixRMaj unzeroMatrix = new DMatrixRMaj(3, 2, true, 0.2, 1, -2, 3, 6, 5);
    DMatrixRMaj unzeroVector = new DMatrixRMaj(5, 1, true, 0.3, 1, -2, 3, 4);
    DMatrixRMaj squareMatrix = new DMatrixRMaj(2, 2, true, 0.2, 1, -2, 3);

    /**
     * Tests against the condition number from octave.
     */
    @Test void conditionP() {
        double val = NormOps_DDRM.conditionP(squareMatrix, 1);

        assertEquals(7.6923, val, UtilEjml.TEST_F64_SQ);

        // check the non-square case
        val = NormOps_DDRM.conditionP(unzeroMatrix, 1);

        assertEquals(3.4325, val, UtilEjml.TEST_F64_SQ);

        // see if the other pseudo-inverse works
        DMatrixRMaj trans = unzeroMatrix.copy();
        CommonOps_DDRM.transpose(trans);
        val = NormOps_DDRM.conditionP(trans, 1);

        assertEquals(3.4887, val, UtilEjml.TEST_F64_SQ);
    }

    /**
     * Tests against the condition number from octave.
     */
    @Test void conditionP2() {
        double val = NormOps_DDRM.conditionP2(unzeroMatrix);

        assertEquals(2.1655, val, UtilEjml.TEST_F64_SQ);

        checkUncountable(NormOps_DDRM.conditionP2(zeroMatrix));
    }

    /**
     * Tested using the following operation in octave:<br>
     *
     * sum(abs(a(:)).^3.5)^(1/3.5)
     */
    @Test void elementP() {
        double val = NormOps_DDRM.elementP(unzeroMatrix, 3.5);

        assertEquals(6.9108, val, UtilEjml.TEST_F64_SQ);

        checkUncountable(NormOps_DDRM.elementP(zeroMatrix, 3.5));
    }

    @Test void fastElementP() {
        double val = NormOps_DDRM.fastElementP(unzeroMatrix, 3.5);

        assertEquals(6.9108, val, UtilEjml.TEST_F64_SQ);

        checkUncountable(NormOps_DDRM.fastElementP(zeroMatrix, 3.5));
    }

    @Test void normalizeF() {
        DMatrixRMaj a = unzeroVector.copy();

        NormOps_DDRM.normalizeF(a);

        assertEquals(1, NormOps_DDRM.normF(a), UtilEjml.TEST_F64);
    }

    @Test void fastNormF() {
        double val = NormOps_DDRM.fastNormF(unzeroMatrix);

        assertEquals(8.6626, val, UtilEjml.TEST_F64_SQ);

        checkUncountable(NormOps_DDRM.fastNormF(zeroMatrix));
    }

    @Test void normF() {
        double val = NormOps_DDRM.normF(unzeroMatrix);

        assertEquals(8.6626, val, UtilEjml.TEST_F64_SQ);

        checkUncountable(NormOps_DDRM.normF(zeroMatrix));
    }

    @Test void fastNormP2() {
        // check induced matrix norm
        double found = NormOps_DDRM.fastNormP2(unzeroMatrix);
        double expected = NormOps_DDRM.inducedP2(unzeroMatrix);
        assertEquals(expected, found, UtilEjml.TEST_F64_SQ);

        // check vector norm
        found = NormOps_DDRM.fastNormP2(unzeroVector);
        expected = NormOps_DDRM.normF(unzeroVector);
        assertEquals(expected, found, UtilEjml.TEST_F64_SQ);
    }

    @Test void normP() {
        // check induced matrix norm
        double found = NormOps_DDRM.normP(unzeroMatrix, 2);
        double expected = NormOps_DDRM.inducedP2(unzeroMatrix);
        assertEquals(expected, found, UtilEjml.TEST_F64_SQ);

        // check vector norm
        found = NormOps_DDRM.normP(unzeroVector, 2);
        expected = NormOps_DDRM.normF(unzeroVector);
        assertEquals(expected, found, UtilEjml.TEST_F64_SQ);
    }

    @Test void fastNormP() {
        // check induced matrix norm
        double found = NormOps_DDRM.fastNormP(unzeroMatrix, 2);
        double expected = NormOps_DDRM.inducedP2(unzeroMatrix);
        assertEquals(expected, found, UtilEjml.TEST_F64_SQ);

        // check vector norm
        found = NormOps_DDRM.fastNormP(unzeroVector, 2);
        expected = NormOps_DDRM.normF(unzeroVector);
        assertEquals(expected, found, UtilEjml.TEST_F64_SQ);
    }

    @Test void normP1() {
        // check induced matrix norm
        double found = NormOps_DDRM.normP1(unzeroMatrix);
        double expected = NormOps_DDRM.inducedP1(unzeroMatrix);
        assertEquals(expected, found, UtilEjml.TEST_F64_SQ);

        // check vector norm
        found = NormOps_DDRM.normP1(unzeroVector);
        expected = CommonOps_DDRM.elementSumAbs(unzeroVector);
        assertEquals(expected, found, UtilEjml.TEST_F64_SQ);
    }

    @Test void normP2() {
        // check induced matrix norm
        double found = NormOps_DDRM.normP2(unzeroMatrix);
        double expected = NormOps_DDRM.inducedP2(unzeroMatrix);
        assertEquals(expected, found, UtilEjml.TEST_F64_SQ);

        // check vector norm
        found = NormOps_DDRM.normP2(unzeroVector);
        expected = NormOps_DDRM.normF(unzeroVector);
        assertEquals(expected, found, UtilEjml.TEST_F64_SQ);
    }

    @Test void normPInf() {
        // check induced matrix norm
        double found = NormOps_DDRM.normPInf(unzeroMatrix);
        double expected = NormOps_DDRM.inducedPInf(unzeroMatrix);
        assertEquals(expected, found, UtilEjml.TEST_F64_SQ);

        // check vector norm
        found = NormOps_DDRM.normPInf(unzeroVector);
        expected = CommonOps_DDRM.elementMaxAbs(unzeroVector);
        assertEquals(expected, found, UtilEjml.TEST_F64_SQ);
    }

    @Test void inducedP1() {
        double val = NormOps_DDRM.inducedP1(unzeroMatrix);
        assertEquals(9, val, UtilEjml.TEST_F64_SQ);

        checkUncountable(NormOps_DDRM.inducedP1(zeroMatrix));
    }

    @Test void inducedP2() {
        double val = NormOps_DDRM.inducedP2(unzeroMatrix);
        assertEquals(7.8645, val, UtilEjml.TEST_F64_SQ);

        checkUncountable(NormOps_DDRM.inducedP2(zeroMatrix));

        // make sure the largest singular value is being returned not just the first
        for (int i = 0; i < 20; i++) {
            SimpleMatrix A = SimpleMatrix.random_DDRM(5, 5, -10, 10, rand);
            double largest = (double)A.svd().getW().get(0);

            assertEquals(largest, NormOps_DDRM.inducedP2(A.getDDRM()), UtilEjml.TEST_F64);
        }
    }

    @Test void inducedPInf() {
        double val = NormOps_DDRM.inducedPInf(unzeroMatrix);
        assertEquals(11, val, UtilEjml.TEST_F64_SQ);

        checkUncountable(NormOps_DDRM.inducedPInf(zeroMatrix));
    }

    private static void checkUncountable( double val ) {
        assertFalse(Double.isInfinite(val));
        assertFalse(Double.isNaN(val));
    }
}
