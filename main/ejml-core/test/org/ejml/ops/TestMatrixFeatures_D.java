/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.junit.jupiter.api.Test;

import static org.ejml.UtilEjml.parse_DDRM;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestMatrixFeatures_D extends EjmlStandardJUnit {
    @Test
    public void isEquals() {
        String a = "-0.779094   1.682750   0.039239\n" +
                "   1.304014  -1.880739   1.438741\n" +
                "  -0.746918   1.382356  -0.520416";

        DMatrixRMaj m = parse_DDRM(a,3);
        DMatrixRMaj n = parse_DDRM(a,3);

        assertTrue(MatrixFeatures_DDRM.isEquals(m,n));

        n.set(2,1,-0.5);
        assertFalse(MatrixFeatures_D.isEquals(m,n));

        m.set(2,1,Double.NaN);
        n.set(2, 1, Double.NaN);
        assertFalse(MatrixFeatures_D.isEquals(m, n));
        m.set(2, 1, Double.POSITIVE_INFINITY);
        n.set(2,1,Double.POSITIVE_INFINITY);
        assertTrue(MatrixFeatures_D.isEquals(m, n));
    }

    @Test
    public void isIdentical() {

        double values[] = new double[]{1.0,Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY};

        for( int i = 0; i < values.length; i++ ) {
            for( int j = 0; j < values.length; j++ ) {
                checkIdentical(values[i],values[j], UtilEjml.TEST_F64,i==j);
            }
        }

        checkIdentical(1.0,1.5,UtilEjml.TEST_F64,false);
        checkIdentical(1.5,1.0,UtilEjml.TEST_F64,false);
        checkIdentical(1.0,1.0000000001,UtilEjml.TEST_F64,true);
        checkIdentical(1.0,Double.NaN,UtilEjml.TEST_F64,false);
        checkIdentical(Double.NaN,1.0,UtilEjml.TEST_F64,false);
    }

    private void checkIdentical( double valA , double valB , double tol , boolean expected ) {
        DMatrixRMaj A = new DMatrixRMaj(2,2);
        CommonOps_DDRM.fill(A, valA);
        DMatrixRMaj B = new DMatrixRMaj(2,2);
        CommonOps_DDRM.fill(B, valB);

        assertEquals(expected, MatrixFeatures_D.isIdentical(A,B,tol));
    }
}