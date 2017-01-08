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

package org.ejml.alg.dense.mult;

import org.ejml.alg.fixed.FixedOps3_R64;
import org.ejml.alg.fixed.FixedOps6_R64;
import org.ejml.data.FixedMatrix3x3_F64;
import org.ejml.data.FixedMatrix6x6_F64;
import org.ejml.data.RowMatrix_F64;

/**
 * @author Peter Abeles
 */
public class MatrixMultFixedBlock {
    private FixedMatrix6x6_F64 a0_6 = new FixedMatrix6x6_F64();
    private FixedMatrix6x6_F64 a1_6 = new FixedMatrix6x6_F64();

    private FixedMatrix6x6_F64 b00_6 = new FixedMatrix6x6_F64();
    private FixedMatrix6x6_F64 b01_6 = new FixedMatrix6x6_F64();
    private FixedMatrix6x6_F64 b10_6 = new FixedMatrix6x6_F64();
    private FixedMatrix6x6_F64 b11_6 = new FixedMatrix6x6_F64();

    private FixedMatrix6x6_F64 tmp_6 = new FixedMatrix6x6_F64();


    private FixedMatrix3x3_F64 a0_3 = new FixedMatrix3x3_F64();
    private FixedMatrix3x3_F64 a1_3 = new FixedMatrix3x3_F64();
    private FixedMatrix3x3_F64 a2_3 = new FixedMatrix3x3_F64();
    private FixedMatrix3x3_F64 a3_3 = new FixedMatrix3x3_F64();

    private FixedMatrix3x3_F64 b00_3 = new FixedMatrix3x3_F64();
    private FixedMatrix3x3_F64 b01_3 = new FixedMatrix3x3_F64();
    private FixedMatrix3x3_F64 b02_3 = new FixedMatrix3x3_F64();
    private FixedMatrix3x3_F64 b03_3 = new FixedMatrix3x3_F64();

    private FixedMatrix3x3_F64 b10_3 = new FixedMatrix3x3_F64();
    private FixedMatrix3x3_F64 b11_3 = new FixedMatrix3x3_F64();
    private FixedMatrix3x3_F64 b12_3 = new FixedMatrix3x3_F64();
    private FixedMatrix3x3_F64 b13_3 = new FixedMatrix3x3_F64();

    private FixedMatrix3x3_F64 b20_3 = new FixedMatrix3x3_F64();
    private FixedMatrix3x3_F64 b21_3 = new FixedMatrix3x3_F64();
    private FixedMatrix3x3_F64 b22_3 = new FixedMatrix3x3_F64();
    private FixedMatrix3x3_F64 b23_3 = new FixedMatrix3x3_F64();

    private FixedMatrix3x3_F64 b30_3 = new FixedMatrix3x3_F64();
    private FixedMatrix3x3_F64 b31_3 = new FixedMatrix3x3_F64();
    private FixedMatrix3x3_F64 b32_3 = new FixedMatrix3x3_F64();
    private FixedMatrix3x3_F64 b33_3 = new FixedMatrix3x3_F64();

    private FixedMatrix3x3_F64 tmp_3 = new FixedMatrix3x3_F64();

    public void mult_2x6(RowMatrix_F64 A , RowMatrix_F64 B , RowMatrix_F64 C ) {
        extract(A, a0_6,0,0); extract(A, a1_6,0,6);

        extract(B, b00_6,0,0); extract(B, b01_6,0,6);
        extract(B, b10_6,6,0); extract(B,b11_6,6,6);

        FixedOps6_R64.mult(a0_6, b00_6, tmp_6);
        FixedOps6_R64.multAdd(a1_6, b10_6, tmp_6);
        insert(tmp_6,C,0,0);

        FixedOps6_R64.mult(a0_6, b01_6, tmp_6);
        FixedOps6_R64.multAdd(a1_6,b11_6, tmp_6);
        insert(tmp_6,C,0,6);

        extract(A, a0_6,6,0); extract(A, a1_6,6,6);
        FixedOps6_R64.mult(a0_6, b01_6, tmp_6);
        FixedOps6_R64.multAdd(a1_6,b11_6, tmp_6);
        insert(tmp_6,C,6,6);

        FixedOps6_R64.mult(a0_6, b00_6, tmp_6);
        FixedOps6_R64.multAdd(a1_6, b10_6, tmp_6);
        insert(tmp_6,C,6,0);
    }

    public void mult_4x3(RowMatrix_F64 A , RowMatrix_F64 B , RowMatrix_F64 C ) {

        extract(B,b00_3,0,0); extract(B,b01_3,0,3); extract(B,b02_3,0,6); extract(B,b03_3,0,9);
        extract(B,b10_3,3,0); extract(B,b11_3,3,3); extract(B,b12_3,3,6); extract(B,b13_3,3,9);
        extract(B,b20_3,6,0); extract(B,b21_3,6,3); extract(B,b22_3,6,6); extract(B,b23_3,6,9);
        extract(B,b30_3,9,0); extract(B,b31_3,9,3); extract(B,b32_3,9,6); extract(B,b33_3,9,9);

        computeRow_4x3(A, C, 0);
        computeRow_4x3(A, C, 3);
        computeRow_4x3(A, C, 6);
        computeRow_4x3(A, C, 9);
    }

    private void computeRow_4x3(RowMatrix_F64 A, RowMatrix_F64 C, int row) {
        extract(A, a0_3,row,0); extract(A, a1_3,row,3); extract(A, a2_3,row,6); extract(A, a3_3,row,9);
        FixedOps3_R64.mult(a0_3, b00_3, tmp_3);
        FixedOps3_R64.multAdd(a1_3, b10_3, tmp_3);
        FixedOps3_R64.multAdd(a2_3, b20_3, tmp_3);
        FixedOps3_R64.multAdd(a3_3, b30_3, tmp_3);
        insert(tmp_3,C,row,0);

        FixedOps3_R64.mult(a0_3, b01_3, tmp_3);
        FixedOps3_R64.multAdd(a1_3, b11_3, tmp_3);
        FixedOps3_R64.multAdd(a2_3, b21_3, tmp_3);
        FixedOps3_R64.multAdd(a3_3, b31_3, tmp_3);
        insert(tmp_3,C,row,3);

        FixedOps3_R64.mult(a0_3, b02_3, tmp_3);
        FixedOps3_R64.multAdd(a1_3, b12_3, tmp_3);
        FixedOps3_R64.multAdd(a2_3, b22_3, tmp_3);
        FixedOps3_R64.multAdd(a3_3, b32_3, tmp_3);
        insert(tmp_3,C,row,6);

        FixedOps3_R64.mult(a0_3, b03_3, tmp_3);
        FixedOps3_R64.multAdd(a1_3, b13_3, tmp_3);
        FixedOps3_R64.multAdd(a2_3, b23_3, tmp_3);
        FixedOps3_R64.multAdd(a3_3, b33_3, tmp_3);
        insert(tmp_3,C,row,9);
    }

    public static void extract(RowMatrix_F64 A , FixedMatrix6x6_F64 a , int row , int col ) {
        int idx = row*A.numCols + col;
        a.a11 = A.data[idx++]; a.a12 = A.data[idx++]; a.a13 = A.data[idx++];
        a.a14 = A.data[idx++]; a.a15 = A.data[idx++]; a.a16 = A.data[idx];

        idx = (row+1)*A.numCols + col;
        a.a21 = A.data[idx++]; a.a22 = A.data[idx++]; a.a23 = A.data[idx++];
        a.a24 = A.data[idx++]; a.a25 = A.data[idx++]; a.a26 = A.data[idx];

        idx = (row+2)*A.numCols + col;
        a.a31 = A.data[idx++]; a.a32 = A.data[idx++]; a.a33 = A.data[idx++];
        a.a34 = A.data[idx++]; a.a35 = A.data[idx++]; a.a36 = A.data[idx];

        idx = (row+3)*A.numCols + col;
        a.a41 = A.data[idx++]; a.a42 = A.data[idx++]; a.a43 = A.data[idx++];
        a.a44 = A.data[idx++]; a.a45 = A.data[idx++]; a.a46 = A.data[idx];

        idx = (row+3)*A.numCols + col;
        a.a51 = A.data[idx++]; a.a52 = A.data[idx++]; a.a53 = A.data[idx++];
        a.a54 = A.data[idx++]; a.a55 = A.data[idx++]; a.a56 = A.data[idx];

        idx = (row+4)*A.numCols + col;
        a.a61 = A.data[idx++]; a.a62 = A.data[idx++]; a.a63 = A.data[idx++];
        a.a64 = A.data[idx++]; a.a65 = A.data[idx++]; a.a66 = A.data[idx];
    }

    public static void insert(FixedMatrix6x6_F64 a , RowMatrix_F64 A, int row , int col ) {
        int idx = row*A.numCols + col;
        A.data[idx++] = a.a11; A.data[idx++] = a.a12; A.data[idx++] = a.a13;
        A.data[idx++] = a.a14; A.data[idx++] = a.a15; A.data[idx] = a.a16;

        idx = (row+1)*A.numCols + col;
        A.data[idx++] = a.a21; A.data[idx++] = a.a22; A.data[idx++] = a.a23;
        A.data[idx++] = a.a24; A.data[idx++] = a.a25; A.data[idx] = a.a26;

        idx = (row+2)*A.numCols + col;
        A.data[idx++] = a.a31; A.data[idx++] = a.a32; A.data[idx++] = a.a33;
        A.data[idx++] = a.a34; A.data[idx++] = a.a35; A.data[idx] = a.a36;

        idx = (row+3)*A.numCols + col;
        A.data[idx++] = a.a41; A.data[idx++] = a.a42; A.data[idx++] = a.a43;
        A.data[idx++] = a.a44; A.data[idx++] = a.a45; A.data[idx] = a.a46;

        idx = (row+3)*A.numCols + col;
        A.data[idx++] = a.a51; A.data[idx++] = a.a52; A.data[idx++] = a.a53;
        A.data[idx++] = a.a54; A.data[idx++] = a.a55; A.data[idx] = a.a56;

        idx = (row+4)*A.numCols + col;
        A.data[idx++] = a.a61; A.data[idx++] = a.a62; A.data[idx++] = a.a63;
        A.data[idx++] = a.a64; A.data[idx++] = a.a65; A.data[idx] = a.a66;
    }

    public static void extract(RowMatrix_F64 A , FixedMatrix3x3_F64 a , int row , int col ) {
        int idx = row * A.numCols + col;
        a.a11 = A.data[idx++]; a.a12 = A.data[idx++]; a.a12 = A.data[idx];
        idx = (row + 1) * A.numCols + col;
        a.a21 = A.data[idx++]; a.a22 = A.data[idx++]; a.a23 = A.data[idx];
        idx = (row + 2) * A.numCols + col;
        a.a31 = A.data[idx++]; a.a32 = A.data[idx++]; a.a33 = A.data[idx];
    }

    public static void insert(FixedMatrix3x3_F64 a , RowMatrix_F64 A, int row , int col ) {
        int idx = row * A.numCols + col;
        A.data[idx++] = a.a11; A.data[idx++] = a.a12; A.data[idx] = a.a13;

        idx = (row+1)*A.numCols + col;
        A.data[idx++] = a.a21; A.data[idx++] = a.a22; A.data[idx] = a.a23;

        idx = (row+2)*A.numCols + col;
        A.data[idx++] = a.a31; A.data[idx++] = a.a32; A.data[idx] = a.a33;

    }
}
