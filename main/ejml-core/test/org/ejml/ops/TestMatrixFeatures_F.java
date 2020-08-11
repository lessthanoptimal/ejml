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

package org.ejml.ops;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.junit.jupiter.api.Test;

import static org.ejml.UtilEjml.parse_FDRM;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestMatrixFeatures_F {
    @Test
    public void isEquals() {
        String a = "-0.779094f   1.682750f   0.039239f\n" +
                "   1.304014f  -1.880739f   1.438741f\n" +
                "  -0.746918f   1.382356f  -0.520416f";

        FMatrixRMaj m = parse_FDRM(a,3);
        FMatrixRMaj n = parse_FDRM(a,3);

        assertTrue(MatrixFeatures_FDRM.isEquals(m,n));

        n.set(2,1,-0.5f);
        assertFalse(MatrixFeatures_F.isEquals(m,n));

        m.set(2,1,Float.NaN);
        n.set(2, 1, Float.NaN);
        assertFalse(MatrixFeatures_F.isEquals(m, n));
        m.set(2, 1, Float.POSITIVE_INFINITY);
        n.set(2,1,Float.POSITIVE_INFINITY);
        assertTrue(MatrixFeatures_F.isEquals(m, n));
    }

    @Test
    public void isIdentical() {

        float values[] = new float[]{1.0f,Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY};

        for( int i = 0; i < values.length; i++ ) {
            for( int j = 0; j < values.length; j++ ) {
                checkIdentical(values[i],values[j], UtilEjml.TEST_F32,i==j);
            }
        }

        checkIdentical(1.0f,1.5f,UtilEjml.TEST_F32,false);
        checkIdentical(1.5f,1.0f,UtilEjml.TEST_F32,false);
        checkIdentical(1.0f,1.0000000001f,UtilEjml.TEST_F32,true);
        checkIdentical(1.0f,Float.NaN,UtilEjml.TEST_F32,false);
        checkIdentical(Float.NaN,1.0f,UtilEjml.TEST_F32,false);
    }

    private void checkIdentical( float valA , float valB , float tol , boolean expected ) {
        FMatrixRMaj A = new FMatrixRMaj(2,2);
        CommonOps_FDRM.fill(A, valA);
        FMatrixRMaj B = new FMatrixRMaj(2,2);
        CommonOps_FDRM.fill(B, valB);

        assertEquals(expected, MatrixFeatures_F.isIdentical(A,B,tol));
    }
}