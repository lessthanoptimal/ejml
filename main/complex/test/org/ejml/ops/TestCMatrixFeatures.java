/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.CDenseMatrix64F;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestCMatrixFeatures {

    Random rand = new Random(234);

    @Test
    public void hasUncountable() {
        CDenseMatrix64F a = new CDenseMatrix64F(4,4);

        // check a negative case first
        assertFalse(CMatrixFeatures.hasUncountable(a));

        // check two positve cases with different types of uncountables
        a.set(2,2,Double.NaN,0);
        assertTrue(CMatrixFeatures.hasUncountable(a));

        a.set(2,2,Double.POSITIVE_INFINITY,0);
        assertTrue(CMatrixFeatures.hasUncountable(a));
    }

    @Test
    public void hasNaN() {
        CDenseMatrix64F m = new CDenseMatrix64F(3,3);
        assertFalse(CMatrixFeatures.hasNaN(m));

        m.set(1,2,-Double.NaN,0);
        assertTrue(CMatrixFeatures.hasNaN(m));
    }

    @Test
    public void isEquals() {
        CDenseMatrix64F m = CRandomMatrices.createRandom(3,4,-1,1,rand);
        CDenseMatrix64F n = m.copy();

        assertTrue(CMatrixFeatures.isEquals(m,n));

        n.set(2,1,-0.5,-0.6);
        assertFalse(CMatrixFeatures.isEquals(m,n));

        m.set(2,1,Double.NaN,1);
        n.set(2,1,Double.NaN,1);
        assertFalse(CMatrixFeatures.isEquals(m,n));
        m.set(2,1,Double.POSITIVE_INFINITY,1);
        n.set(2,1,Double.POSITIVE_INFINITY,1);
        assertTrue(CMatrixFeatures.isEquals(m,n));
    }

    @Test
    public void isEquals_tol() {
        CDenseMatrix64F m = CRandomMatrices.createRandom(3,4,-1,1,rand);
        CDenseMatrix64F n = m.copy();

        assertTrue(CMatrixFeatures.isEquals(m,n,1e-6));

        n.data[4] += 1e-25;
        assertTrue(CMatrixFeatures.isEquals(m,n,1e-6));

        n.data[4] += 1e-2;
        assertFalse(CMatrixFeatures.isEquals(m,n,1e-6));

        m.set(2,1,Double.NaN,1);
        n.set(2,1,Double.NaN,1);
        assertFalse(CMatrixFeatures.isEquals(m,n,1e-6));
        m.set(2,1,Double.POSITIVE_INFINITY,1);
        n.set(2,1,Double.POSITIVE_INFINITY,1);
        assertFalse(CMatrixFeatures.isEquals(m,n,1e-6));
    }

    @Test
    public void isIdentical() {

        double values[] = new double[]{1.0,Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY};

        for( int i = 0; i < values.length; i++ ) {
            for( int j = 0; j < values.length; j++ ) {
                checkIdentical(values[i],values[j],1e-8,i==j);
            }
        }

        checkIdentical(1.0,1.5,1e-8,false);
        checkIdentical(1.5,1.0,1e-8,false);
        checkIdentical(1.0,1.0000000001,1e-8,true);
        checkIdentical(1.0,Double.NaN,1e-8,false);
        checkIdentical(Double.NaN,1.0,1e-8,false);
    }

    private void checkIdentical( double valA , double valB , double tol , boolean expected ) {
        CDenseMatrix64F A = new CDenseMatrix64F(2,2);
        CDenseMatrix64F B = new CDenseMatrix64F(2,2);
        CCommonOps.fill(A, valA,0);
        CCommonOps.fill(B, valB,0);

        assertEquals(expected,CMatrixFeatures.isIdentical(A,B,tol));

        CCommonOps.fill(A, 0,valA);
        CCommonOps.fill(B, 0,valB);

        assertEquals(expected,CMatrixFeatures.isIdentical(A,B,tol));
    }
}
