/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.ops;

import org.ejml.data.DenseMatrix64F;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestNormOps {

    DenseMatrix64F zeroMatrix = new DenseMatrix64F(3,4);
    DenseMatrix64F unzeroMatrix = new DenseMatrix64F(3,2, true, 0.2, 1, -2, 3, 6, 5);
    DenseMatrix64F unzeroVector = new DenseMatrix64F(5,1, true, 0.3, 1, -2, 3, 4);
    DenseMatrix64F squareMatrix = new DenseMatrix64F(2,2, true, 0.2, 1, -2, 3);


    /**
     * Tests against the condition number from octave.
     */
    @Test
    public void conditionP() {
        double val = NormOps.conditionP(squareMatrix,1);

        assertEquals(7.6923,val,1e-3);

        // check the non-square case
        val = NormOps.conditionP(unzeroMatrix,1);

        assertEquals(3.4325,val,1e-3);

        // see if the other pseudo-inverse works
        DenseMatrix64F trans = unzeroMatrix.copy();
        CommonOps.transpose(trans);
        val = NormOps.conditionP(trans,1);

        assertEquals(3.4887,val,1e-3);
    }

    /**
     * Tests against the condition number from octave.
     */
    @Test
    public void conditionP2() {
         double val = NormOps.conditionP2(unzeroMatrix);

        assertEquals(2.1655,val,1e-3);

        checkUncountable(NormOps.conditionP2(zeroMatrix));
    }

    /**
     * Tested using the following operation in octave:
     *
     * sum(abs(a(:)).^3.5)^(1/3.5)
     */
    @Test
    public void elementP() {
         double val = NormOps.elementP(unzeroMatrix,3.5);

        assertEquals(6.9108,val,1e-3);

        checkUncountable(NormOps.elementP(zeroMatrix,3.5));
    }

    @Test
    public void fastElementP() {
         double val = NormOps.fastElementP(unzeroMatrix,3.5);

        assertEquals(6.9108,val,1e-3);

        checkUncountable(NormOps.fastElementP(zeroMatrix,3.5));
    }

    @Test
    public void normalizeF() {
        DenseMatrix64F a = unzeroVector.copy();

        NormOps.normalizeF(a);

        assertEquals(1,NormOps.normF(a),1e-6);
    }

    @Test
    public void fastNormF() {
        double val = NormOps.fastNormF(unzeroMatrix);

        assertEquals(8.6626,val,1e-3);

        checkUncountable(NormOps.fastNormF(zeroMatrix));
    }

    @Test
    public void normF() {
        double val = NormOps.normF(unzeroMatrix);

        assertEquals(8.6626,val,1e-3);

        checkUncountable(NormOps.normF(zeroMatrix));
    }

    @Test
    public void preciseNormF() {
        fail("Implement");
    }

    @Test
    public void fastNormP2() {
        // check induced matrix norm
        double found = NormOps.fastNormP2(unzeroMatrix);
        double expected = NormOps.inducedP2(unzeroMatrix);
        assertEquals(expected,found,1e-3);

        // check vector norm
        found = NormOps.fastNormP2(unzeroVector);
        expected = NormOps.normF(unzeroVector);
        assertEquals(expected,found,1e-3);
    }

    @Test
    public void normP() {
        // check induced matrix norm
        double found = NormOps.normP(unzeroMatrix,2);
        double expected = NormOps.inducedP2(unzeroMatrix);
        assertEquals(expected,found,1e-3);

        // check vector norm
        found = NormOps.normP(unzeroVector,2);
        expected = NormOps.normF(unzeroVector);
        assertEquals(expected,found,1e-3);
    }

    @Test
    public void fastNormP() {
        // check induced matrix norm
        double found = NormOps.fastNormP(unzeroMatrix,2);
        double expected = NormOps.inducedP2(unzeroMatrix);
        assertEquals(expected,found,1e-3);

        // check vector norm
        found = NormOps.fastNormP(unzeroVector,2);
        expected = NormOps.normF(unzeroVector);
        assertEquals(expected,found,1e-3);
    }

    @Test
    public void normP1() {
        // check induced matrix norm
        double found = NormOps.normP1(unzeroMatrix);
        double expected = NormOps.inducedP1(unzeroMatrix);
        assertEquals(expected,found,1e-3);

        // check vector norm
        found = NormOps.normP1(unzeroVector);
        expected = CommonOps.elementSumAbs(unzeroVector);
        assertEquals(expected,found,1e-3);
    }

    @Test
    public void normP2() {
        // check induced matrix norm
        double found = NormOps.normP2(unzeroMatrix);
        double expected = NormOps.inducedP2(unzeroMatrix);
        assertEquals(expected,found,1e-3);

        // check vector norm
        found = NormOps.normP2(unzeroVector);
        expected = NormOps.normF(unzeroVector);
        assertEquals(expected,found,1e-3);
    }

    @Test
    public void normPInf() {
        // check induced matrix norm
        double found = NormOps.normPInf(unzeroMatrix);
        double expected = NormOps.inducedPInf(unzeroMatrix);
        assertEquals(expected,found,1e-3);

        // check vector norm
        found = NormOps.normPInf(unzeroVector);
        expected = CommonOps.elementMaxAbs(unzeroVector);
        assertEquals(expected,found,1e-3);
    }

    @Test
    public void inducedP1() {
        double val = NormOps.inducedP1(unzeroMatrix);
        assertEquals(9,val,1e-3);

        checkUncountable(NormOps.inducedP1(zeroMatrix));
    }

    @Test
    public void inducedP2() {
        double val = NormOps.inducedP2(unzeroMatrix);
        assertEquals(7.8645,val,1e-3);

        checkUncountable(NormOps.inducedP2(zeroMatrix));
    }

    @Test
    public void inducedPInf() {
        double val = NormOps.inducedPInf(unzeroMatrix);
        assertEquals(11,val,1e-3);

        checkUncountable(NormOps.inducedPInf(zeroMatrix));
    }

    private static void checkUncountable( double val ) {
        assertFalse(Double.isInfinite(val));
        assertFalse(Double.isNaN(val));
    }
}
