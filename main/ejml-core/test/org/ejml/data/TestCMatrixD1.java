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

package org.ejml.data;

import org.ejml.UtilEjml;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestCMatrixD1 {
    @Test
    public void set_matrix() {
        CMatrixD1 a = new CMatrixRMaj(3,4);
        a.set(1,3,9,2);

        CMatrixD1 b = new CMatrixRMaj(3,4);

        b.set(a);
        for (int i = 0; i < a.getDataLength(); i++) {
            assertEquals(a.data[i],b.data[i], UtilEjml.TEST_F32);
        }
        assertEquals(9, b.getReal(1, 3), UtilEjml.TEST_F32);
    }

    @Test
    public void getNumRows() {
        CMatrixD1 a = new CMatrixRMaj(3,4);
        assertEquals(3,a.getNumRows());
    }

    @Test
    public void getNumCols() {
        CMatrixD1 a = new CMatrixRMaj(3,4);
        assertEquals(4,a.getNumCols());
    }

    @Test
    public void setNumRows() {
        CMatrixD1 a = new CMatrixRMaj(3,4);
        a.setNumRows(6);
        assertEquals(6, a.getNumRows());
    }

    @Test
    public void setNumCols() {
        CMatrixD1 a = new CMatrixRMaj(3,4);
        a.setNumCols(6);
        assertEquals(6,a.getNumCols());
    }
}