/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.CDenseMatrix64F;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * @author Peter Abeles
 */
public class TestReadMatrixCsv {
    /**
     * Make sure incorrectly formatted data is handled gracefully
     */
    @Test(expected=IOException.class)
    public void bad_matrix_row() throws IOException {
        String s = "3 2 real\n0 0\n1 1";

        ReadMatrixCsv alg = new ReadMatrixCsv(new ByteArrayInputStream(s.getBytes()));

        alg.read();
        fail("Should have had an exception");
    }

    @Test(expected=IOException.class)
    public void bad_matrix_col() throws IOException {
        String s = "3 2 real\n0 0\n1\n0 3";

        ReadMatrixCsv alg = new ReadMatrixCsv(new ByteArrayInputStream(s.getBytes()));

        alg.read();
        fail("Should have had an exception");
    }

    public void complex() throws IOException {
        String s = "3 2 complex\n0 2 0 -1\n1 2 -1 -1\n0 2 3 10";

        ReadMatrixCsv alg = new ReadMatrixCsv(new ByteArrayInputStream(s.getBytes()));

        CDenseMatrix64F expected = new CDenseMatrix64F(3,2,true,0,2,0,-1,1,2,-1,-1,0,2,3,10);
        CDenseMatrix64F m = alg.read();

        assertTrue(CMatrixFeatures.isIdentical(expected,m,1e-8));
    }
}
