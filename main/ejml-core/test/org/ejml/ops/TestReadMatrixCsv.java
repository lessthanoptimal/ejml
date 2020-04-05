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

package org.ejml.ops;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_ZDRM;
import org.ejml.sparse.triplet.MatrixFeatures_DSTL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * @author Peter Abeles
 */
public class TestReadMatrixCsv {
    /**
     * Make sure incorrectly formatted data is handled gracefully
     */
    @Test
    public void bad_matrix_row() {
        Assertions.assertThrows(IOException.class, () -> {
            String s = "3 2 real\n0 0\n1 1";

            ReadMatrixCsv alg = new ReadMatrixCsv(new ByteArrayInputStream(s.getBytes()));

            alg.read64();
            fail("Should have had an exception");
        });

    }

    @Test
    public void bad_matrix_col() throws IOException {
        Assertions.assertThrows(IOException.class, () -> {
            String s = "3 2 real\n0 0\n1\n0 3";

            ReadMatrixCsv alg = new ReadMatrixCsv(new ByteArrayInputStream(s.getBytes()));

            alg.read64();
            fail("Should have had an exception");
        });
    }

    public void dense_complex() throws IOException {
        String s = "3 2 complex\n0 2 0 -1\n1 2 -1 -1\n0 2 3 10";

        ReadMatrixCsv alg = new ReadMatrixCsv(new ByteArrayInputStream(s.getBytes()));

        ZMatrixRMaj expected = new ZMatrixRMaj(3,2,true,0,2,0,-1,1,2,-1,-1,0,2,3,10);
        ZMatrixRMaj m = alg.read64();

        assertTrue(MatrixFeatures_ZDRM.isIdentical(expected,m, UtilEjml.TEST_F64));
    }

    public void real_sparse() throws IOException {
        String s = "3 2 3 real\n0 2 0.1\n0 0 -0.1\n2 1 12";

        ReadMatrixCsv alg = new ReadMatrixCsv(new ByteArrayInputStream(s.getBytes()));

        DMatrixSparseTriplet expected = new DMatrixSparseTriplet(3,2,3);
        expected.addItem(0,2,0.1);
        expected.addItem(0,0,-0.1);
        expected.addItem(2,1,12);

        DMatrixSparseTriplet m = alg.read64();

        assertTrue(MatrixFeatures_DSTL.isEquals(expected,m, UtilEjml.TEST_F64));
    }
}
