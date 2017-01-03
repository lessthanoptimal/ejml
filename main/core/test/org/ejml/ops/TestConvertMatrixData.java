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

package org.ejml.ops;

import org.ejml.UtilEjml;
import org.ejml.alg.fixed.FixedFeatures2_D64;
import org.ejml.alg.fixed.FixedFeatures3_D64;
import org.ejml.alg.fixed.FixedFeatures4_D64;
import org.ejml.data.*;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestConvertMatrixData {
    @Test
    public void DenseMatrix() {
        DenseMatrix64F A = new DenseMatrix64F(2,2,true,1,2,3,4);
        DenseMatrix32F B = new DenseMatrix32F(2,2);
        DenseMatrix64F C = new DenseMatrix64F(2,2);

        ConvertMatrixData.convert(A,B);
        ConvertMatrixData.convert(B,C);

        assertTrue(MatrixFeatures_D64.isIdentical(A,C, UtilEjml.TEST_64F));
    }

    @Test
    public void FixedMatrix2x2() {
        FixedMatrix2x2_64F A = new FixedMatrix2x2_64F(1,2,3,4);
        FixedMatrix2x2_32F B = new FixedMatrix2x2_32F();
        FixedMatrix2x2_64F C = new FixedMatrix2x2_64F();

        ConvertMatrixData.convert(A,B);
        ConvertMatrixData.convert(B,C);

        assertTrue(FixedFeatures2_D64.isIdentical(A,C, UtilEjml.TEST_64F));
    }

    @Test
    public void FixedMatrix3x3() {
        FixedMatrix3x3_64F A = new FixedMatrix3x3_64F(1,2,3,4,5,6,7,8,9);
        FixedMatrix3x3_32F B = new FixedMatrix3x3_32F();
        FixedMatrix3x3_64F C = new FixedMatrix3x3_64F();

        ConvertMatrixData.convert(A,B);
        ConvertMatrixData.convert(B,C);

        assertTrue(FixedFeatures3_D64.isIdentical(A,C, UtilEjml.TEST_64F));
    }

    @Test
    public void FixedMatrix4x4() {
        FixedMatrix4x4_64F A = new FixedMatrix4x4_64F(1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6);
        FixedMatrix4x4_32F B = new FixedMatrix4x4_32F();
        FixedMatrix4x4_64F C = new FixedMatrix4x4_64F();

        ConvertMatrixData.convert(A,B);
        ConvertMatrixData.convert(B,C);

        assertTrue(FixedFeatures4_D64.isIdentical(A,C, UtilEjml.TEST_64F));
    }

}
