/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

package org.ejml.simple;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.MatrixType;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests which make sure simple matrix correctly can convert between different matrix types.
 *
 * These tests just see if it blows up and if the output is of the expected type. For sake of simplicity,
 * no tests for correctness.
 *
 * @author Peter Abeles
 */
public class TestSimpleMatrixConvertType {

    private final Random rand = new Random(234);

    @Test
    public void mult() {
        SimpleMatrix A = SimpleMatrix.wrap(new FMatrixRMaj(4,5));
        SimpleMatrix B = SimpleMatrix.wrap(new DMatrixRMaj(5,3));

        SimpleMatrix C = A.mult(B);
        assertEquals(MatrixType.FDRM,A.getType());
        assertEquals(MatrixType.DDRM,B.getType());
        assertEquals(MatrixType.DDRM,C.getType());

        A = SimpleMatrix.wrap(new DMatrixRMaj(4,5));
        B = SimpleMatrix.wrap(new FMatrixRMaj(5,3));

        C = A.mult(B);
        assertEquals(MatrixType.DDRM,A.getType());
        assertEquals(MatrixType.FDRM,B.getType());
        assertEquals(MatrixType.DDRM,C.getType());
    }

    @Test
    public void kron() {
        SimpleMatrix A = SimpleMatrix.wrap(new FMatrixRMaj(4,5));
        SimpleMatrix B = SimpleMatrix.wrap(new DMatrixRMaj(2,2));

        SimpleMatrix C = A.kron(B);
        assertEquals(MatrixType.FDRM,A.getType());
        assertEquals(MatrixType.DDRM,B.getType());
        assertEquals(MatrixType.DDRM,C.getType());
    }

    @Test
    public void plus() {
        SimpleMatrix A = SimpleMatrix.wrap(new FMatrixRMaj(4,5));
        SimpleMatrix B = SimpleMatrix.wrap(new DMatrixRMaj(4,5));

        SimpleMatrix C = A.plus(B);
        assertEquals(MatrixType.FDRM,A.getType());
        assertEquals(MatrixType.DDRM,B.getType());
        assertEquals(MatrixType.DDRM,C.getType());
    }

    @Test
    public void minus() {
        SimpleMatrix A = SimpleMatrix.wrap(new FMatrixRMaj(4,5));
        SimpleMatrix B = SimpleMatrix.wrap(new DMatrixRMaj(4,5));

        SimpleMatrix C = A.minus(B);
        assertEquals(MatrixType.FDRM,A.getType());
        assertEquals(MatrixType.DDRM,B.getType());
        assertEquals(MatrixType.DDRM,C.getType());
    }

    @Test
    public void plus_beta_b() {
        SimpleMatrix A = SimpleMatrix.wrap(new FMatrixRMaj(4,5));
        SimpleMatrix B = SimpleMatrix.wrap(new DMatrixRMaj(4,5));

        SimpleMatrix C = A.plus(12,B);
        assertEquals(MatrixType.FDRM,A.getType());
        assertEquals(MatrixType.DDRM,B.getType());
        assertEquals(MatrixType.DDRM,C.getType());
    }

    @Test
    public void dot() {
        SimpleMatrix A = SimpleMatrix.wrap(new FMatrixRMaj(4,1));
        SimpleMatrix B = SimpleMatrix.wrap(new DMatrixRMaj(4,1));

        A.dot(B);
        assertEquals(MatrixType.FDRM,A.getType());
        assertEquals(MatrixType.DDRM,B.getType());
    }

    @Test
    public void solve() {
        SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_FDRM.rectangle(6,5,rand));
        SimpleMatrix B = SimpleMatrix.wrap(RandomMatrices_DDRM.rectangle(6,2,rand));

        SimpleMatrix X = A.solve(B);
        assertEquals(MatrixType.FDRM,A.getType());
        assertEquals(MatrixType.DDRM,B.getType());
        assertEquals(MatrixType.DDRM,X.getType());
    }

    @Test
    public void set() {
        SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_FDRM.rectangle(6,5,rand));
        SimpleMatrix B = SimpleMatrix.wrap(RandomMatrices_DDRM.rectangle(6,2,rand));

        A.set(B);
        assertEquals(MatrixType.DDRM,A.getType());
    }

    @Test
    public void isIdentical() {
        SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_FDRM.rectangle(6,5,rand));
        SimpleMatrix B = SimpleMatrix.wrap(RandomMatrices_DDRM.rectangle(6,2,rand));

        assertFalse(A.isIdentical(B, UtilEjml.TEST_F32));
        assertEquals(MatrixType.FDRM,A.getType());
    }

    @Test
    public void insertIntoThis() {
        SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_FDRM.rectangle(6,5,rand));
        SimpleMatrix B = SimpleMatrix.wrap(RandomMatrices_DDRM.rectangle(6,2,rand));

        A.insertIntoThis(0,0,B);
        assertEquals(MatrixType.DDRM,A.getType());

        A = SimpleMatrix.wrap(RandomMatrices_DDRM.rectangle(6,5,rand));
        B = SimpleMatrix.wrap(RandomMatrices_FDRM.rectangle(6,2,rand));

        A.insertIntoThis(0,0,B);
        assertEquals(MatrixType.DDRM,A.getType());
    }

    @Test
    public void combine() {
        SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_FDRM.rectangle(6,5,rand));
        SimpleMatrix B = SimpleMatrix.wrap(RandomMatrices_DDRM.rectangle(6,2,rand));

        SimpleMatrix C = A.combine(0,1,B);
        assertEquals(MatrixType.FDRM,A.getType());
        assertEquals(MatrixType.DDRM,B.getType());
        assertEquals(MatrixType.DDRM,C.getType());
    }

    @Test
    public void elementMult() {
        SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_FDRM.rectangle(6,5,rand));
        SimpleMatrix B = SimpleMatrix.wrap(RandomMatrices_DDRM.rectangle(6,5,rand));

        SimpleMatrix C = A.elementMult(B);
        assertEquals(MatrixType.FDRM,A.getType());
        assertEquals(MatrixType.DDRM,B.getType());
        assertEquals(MatrixType.DDRM,C.getType());
    }

    @Test
    public void elementDiv() {
        SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_FDRM.rectangle(6,5,rand));
        SimpleMatrix B = SimpleMatrix.wrap(RandomMatrices_DDRM.rectangle(6,5,rand));

        SimpleMatrix C = A.elementDiv(B);
        assertEquals(MatrixType.FDRM,A.getType());
        assertEquals(MatrixType.DDRM,B.getType());
        assertEquals(MatrixType.DDRM,C.getType());
    }

    @Test
    public void elementPower() {
        SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_FDRM.rectangle(6,5,rand));
        SimpleMatrix B = SimpleMatrix.wrap(RandomMatrices_DDRM.rectangle(6,5,rand));

        SimpleMatrix C = A.elementPower(B);
        assertEquals(MatrixType.FDRM,A.getType());
        assertEquals(MatrixType.DDRM,B.getType());
        assertEquals(MatrixType.DDRM,C.getType());
    }
}
