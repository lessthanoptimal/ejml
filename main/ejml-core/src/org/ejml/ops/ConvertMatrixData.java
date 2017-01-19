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

import org.ejml.data.*;

/**
 * Convert between matrices with the same structure but different element data types
 *
 * @author Peter Abeles
 */
public class ConvertMatrixData {
    public static void convert(DMatrixRMaj src, FMatrixRMaj dst ) {
        int N = src.getNumElements();
        for (int i = 0; i < N; i++) {
            dst.data[i] = (float)src.data[i];
        }
    }

    public static void convert(FMatrixRMaj src, DMatrixRMaj dst ) {
        int N = src.getNumElements();
        for (int i = 0; i < N; i++) {
            dst.data[i] = src.data[i];
        }
    }

    public static void convert(DMatrix2x2 src, FMatrix2x2 dst ) {
        dst.a11 = (float)src.a11; dst.a12 = (float)src.a12;
        dst.a21 = (float)src.a21; dst.a22 = (float)src.a22;
    }

    public static void convert(DMatrix3x3 src, FMatrix3x3 dst ) {
        dst.a11 = (float)src.a11; dst.a12 = (float)src.a12; dst.a13 = (float)src.a13;
        dst.a21 = (float)src.a21; dst.a22 = (float)src.a22; dst.a23 = (float)src.a23;
        dst.a31 = (float)src.a31; dst.a32 = (float)src.a32; dst.a33 = (float)src.a33;
    }

    public static void convert(DMatrix4x4 src, FMatrix4x4 dst ) {
        dst.a11 = (float)src.a11; dst.a12 = (float)src.a12; dst.a13 = (float)src.a13; dst.a14 = (float)src.a14;
        dst.a21 = (float)src.a21; dst.a22 = (float)src.a22; dst.a23 = (float)src.a23; dst.a24 = (float)src.a24;
        dst.a31 = (float)src.a31; dst.a32 = (float)src.a32; dst.a33 = (float)src.a33; dst.a34 = (float)src.a34;
        dst.a41 = (float)src.a41; dst.a42 = (float)src.a42; dst.a43 = (float)src.a43; dst.a44 = (float)src.a44;
    }

    public static void convert(FMatrix2x2 src, DMatrix2x2 dst ) {
        dst.a11 = src.a11; dst.a12 = src.a12;
        dst.a21 = src.a21; dst.a22 = src.a22;
    }

    public static void convert(FMatrix3x3 src, DMatrix3x3 dst ) {
        dst.a11 = src.a11; dst.a12 = src.a12; dst.a13 = src.a13;
        dst.a21 = src.a21; dst.a22 = src.a22; dst.a23 = src.a23;
        dst.a31 = src.a31; dst.a32 = src.a32; dst.a33 = src.a33;
    }

    public static void convert(FMatrix4x4 src, DMatrix4x4 dst ) {
        dst.a11 = src.a11; dst.a12 = src.a12; dst.a13 = src.a13; dst.a14 = src.a14;
        dst.a21 = src.a21; dst.a22 = src.a22; dst.a23 = src.a23; dst.a24 = src.a24;
        dst.a31 = src.a31; dst.a32 = src.a32; dst.a33 = src.a33; dst.a34 = src.a34;
        dst.a41 = src.a41; dst.a42 = src.a42; dst.a43 = src.a43; dst.a44 = src.a44;
    }
}
