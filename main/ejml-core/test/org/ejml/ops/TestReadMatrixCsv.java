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
import org.ejml.data.*;
import org.ejml.sparse.triplet.MatrixFeatures_DSTL;
import org.ejml.sparse.triplet.MatrixFeatures_FSTL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestReadMatrixCsv extends EjmlStandardJUnit {
    /**
     * Make sure incorrectly formatted data is handled gracefully
     */
    @Test
    void bad_matrix_row() {
        Assertions.assertThrows(IOException.class, () -> {
            String s = "3 2 real\n0 0\n1 1";

            ReadMatrixCsv alg = new ReadMatrixCsv(new ByteArrayInputStream(s.getBytes(UTF_8)));

            alg.read64();
            fail("Should have had an exception");
        });
    }

    @Test
    void bad_matrix_col() {
        Assertions.assertThrows(IOException.class, () -> {
            String s = "3 2 real\n0 0\n1\n0 3";

            ReadMatrixCsv alg = new ReadMatrixCsv(new ByteArrayInputStream(s.getBytes(UTF_8)));

            alg.read64();
            fail("Should have had an exception");
        });
    }

    @Test
    void readDSTR() throws IOException {
        String s = "3 2 3 real\n0 2 0.1\n0 0 -0.1\n2 1 12";

        ReadMatrixCsv alg = new ReadMatrixCsv(new ByteArrayInputStream(s.getBytes(UTF_8)));

        DMatrixSparseTriplet expected = new DMatrixSparseTriplet(3, 2, 3);
        expected.addItem(0, 2, 0.1);
        expected.addItem(0, 0, -0.1);
        expected.addItem(2, 1, 12);

        DMatrixSparseTriplet m = alg.read64();

        assertTrue(MatrixFeatures_DSTL.isEquals(expected, m, UtilEjml.TEST_F64));
    }

    @Test
    void readFSTR() throws IOException {
        String s = "3 2 3 real\n0 2 0.1\n0 0 -0.1\n2 1 12";

        ReadMatrixCsv alg = new ReadMatrixCsv(new ByteArrayInputStream(s.getBytes(UTF_8)));

        FMatrixSparseTriplet expected = new FMatrixSparseTriplet(3, 2, 3);
        expected.addItem(0, 2, 0.1f);
        expected.addItem(0, 0, -0.1f);
        expected.addItem(2, 1, 12.0f);

        FMatrixSparseTriplet m = alg.read32();

        assertTrue(MatrixFeatures_FSTL.isEquals(expected, m, UtilEjml.TEST_F32));
    }

    @Test
    void readDDRM() throws IOException {
        String s = "3 2 real\n0 -1\n1 -2\n0.9 -3.0";

        ReadMatrixCsv alg = new ReadMatrixCsv(new ByteArrayInputStream(s.getBytes(UTF_8)));

        DMatrixRMaj found = alg.read64();
        assertEquals(3, found.numRows);
        assertEquals(2, found.numCols);
        assertEquals(0.9, found.get(2, 0), UtilEjml.TEST_F64);
        assertEquals(-3.0, found.get(2, 1), UtilEjml.TEST_F64);
    }

    @Test
    void readFDRM() throws IOException {
        String s = "3 2 real\n0 -1\n1 -2\n0.9 -3.0";

        ReadMatrixCsv alg = new ReadMatrixCsv(new ByteArrayInputStream(s.getBytes(UTF_8)));

        FMatrixRMaj found = alg.read32();
        assertEquals(3, found.numRows);
        assertEquals(2, found.numCols);
        assertEquals(0.9f, found.get(2, 0), UtilEjml.TEST_F32);
        assertEquals(-3.0f, found.get(2, 1), UtilEjml.TEST_F32);
    }

    @Test
    void readZDRM() throws IOException {
        String s = "3 2 complex\n0 -1 2 -3\n1 -2 -1 -3.0\n0.9 -2 -1 -3.0";

        ReadMatrixCsv alg = new ReadMatrixCsv(new ByteArrayInputStream(s.getBytes(UTF_8)));

        ZMatrixRMaj found = alg.read64();
        assertEquals(3, found.numRows);
        assertEquals(2, found.numCols);
        assertEquals(0.9, found.getReal(2, 0), UtilEjml.TEST_F64);
        assertEquals(-2, found.getImag(2, 0), UtilEjml.TEST_F64);
    }

    @Test
    void readCDRM() throws IOException {
        String s = "3 2 complex\n0 -1 2 -3\n1 -2 -1 -3.0\n0.9 -2 -1 -3.0";

        ReadMatrixCsv alg = new ReadMatrixCsv(new ByteArrayInputStream(s.getBytes(UTF_8)));

        CMatrixRMaj found = alg.read32();
        assertEquals(3, found.numRows);
        assertEquals(2, found.numCols);
        assertEquals(0.9f, found.getReal(2, 0), UtilEjml.TEST_F32);
        assertEquals(-2.0f, found.getImag(2, 0), UtilEjml.TEST_F32);
    }
}
