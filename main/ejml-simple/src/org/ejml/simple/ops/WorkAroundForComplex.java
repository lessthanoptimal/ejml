/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
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

package org.ejml.simple.ops;

import org.ejml.data.CMatrixRMaj;
import org.ejml.data.Complex_F32;
import org.ejml.data.Complex_F64;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.CommonOps_ZDRM;

/**
 * Work around for auto code generation issues with complex matrices.
 */
public class WorkAroundForComplex {
    public static void elementSum_F32( CMatrixRMaj A, Complex_F64 output ) {
        var tmp = new Complex_F32();
        CommonOps_CDRM.elementSum(A, tmp);
        output.real = tmp.real;
        output.imaginary = tmp.imaginary;
    }

    public static void elementSum_F64( ZMatrixRMaj A, Complex_F64 output ) {
        CommonOps_ZDRM.elementSum(A, output);
    }
}
