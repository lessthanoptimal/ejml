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

package org.ejml.dense.row.misc;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F32;
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_CDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestTransposeAlgs_CDRM {

    Random rand = new Random(234);

    @Test
    public void square() {
        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(4,4,-1,1,rand);
        CMatrixRMaj b = a.copy();

        TransposeAlgs_CDRM.square(b);

        Complex_F32 found = new Complex_F32();
        Complex_F32 expected = new Complex_F32();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(j,i,expected);
                b.get(i,j,found);

                assertEquals(expected.real,found.real, UtilEjml.TEST_F32);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void squareConjugate() {
        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(4,4,-1,1,rand);
        CMatrixRMaj b = a.copy();

        TransposeAlgs_CDRM.squareConjugate(b);

        Complex_F32 found = new Complex_F32();
        Complex_F32 expected = new Complex_F32();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(j,i,expected);
                b.get(i,j,found);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F32);
                assertEquals(-expected.imaginary,found.imaginary,UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void standard() {
        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(4,5,-1,1,rand);
        CMatrixRMaj b = RandomMatrices_CDRM.rectangle(5, 4, -1, 1, rand);

        TransposeAlgs_CDRM.standard(a, b);

        Complex_F32 found = new Complex_F32();
        Complex_F32 expected = new Complex_F32();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F32);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void standardConjugate() {
        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(4,5,-1,1,rand);
        CMatrixRMaj b = RandomMatrices_CDRM.rectangle(5, 4, -1, 1, rand);

        TransposeAlgs_CDRM.standardConjugate(a, b);

        Complex_F32 found = new Complex_F32();
        Complex_F32 expected = new Complex_F32();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F32);
                assertEquals(-expected.imaginary,found.imaginary,UtilEjml.TEST_F32);
            }
        }
    }
}