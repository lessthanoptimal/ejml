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
import org.ejml.data.*;
import org.ejml.dense.fixed.FixedFeatures2_F64;
import org.ejml.dense.fixed.FixedFeatures3_F64;
import org.ejml.dense.fixed.FixedFeatures4_F64;
import org.ejml.dense.row.MatrixFeatures_R64;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestConvertMatrixData {
    @Test
    public void DenseMatrix() {
        DMatrixRow_F64 A = new DMatrixRow_F64(2,2,true,1,2,3,4);
        DMatrixRow_F32 B = new DMatrixRow_F32(2,2);
        DMatrixRow_F64 C = new DMatrixRow_F64(2,2);

        ConvertMatrixData.convert(A,B);
        ConvertMatrixData.convert(B,C);

        assertTrue(MatrixFeatures_R64.isIdentical(A,C, UtilEjml.TEST_F64));
    }

    @Test
    public void DMatrixFixed2x2() {
        DMatrixFixed2x2_F64 A = new DMatrixFixed2x2_F64(1,2,3,4);
        DMatrixFixed2x2_F32 B = new DMatrixFixed2x2_F32();
        DMatrixFixed2x2_F64 C = new DMatrixFixed2x2_F64();

        ConvertMatrixData.convert(A,B);
        ConvertMatrixData.convert(B,C);

        assertTrue(FixedFeatures2_F64.isIdentical(A,C, UtilEjml.TEST_F64));
    }

    @Test
    public void DMatrixFixed3x3() {
        DMatrixFixed3x3_F64 A = new DMatrixFixed3x3_F64(1,2,3,4,5,6,7,8,9);
        DMatrixFixed3x3_F32 B = new DMatrixFixed3x3_F32();
        DMatrixFixed3x3_F64 C = new DMatrixFixed3x3_F64();

        ConvertMatrixData.convert(A,B);
        ConvertMatrixData.convert(B,C);

        assertTrue(FixedFeatures3_F64.isIdentical(A,C, UtilEjml.TEST_F64));
    }

    @Test
    public void DMatrixFixed4x4() {
        DMatrixFixed4x4_F64 A = new DMatrixFixed4x4_F64(1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6);
        DMatrixFixed4x4_F32 B = new DMatrixFixed4x4_F32();
        DMatrixFixed4x4_F64 C = new DMatrixFixed4x4_F64();

        ConvertMatrixData.convert(A,B);
        ConvertMatrixData.convert(B,C);

        assertTrue(FixedFeatures4_F64.isIdentical(A,C, UtilEjml.TEST_F64));
    }

}
