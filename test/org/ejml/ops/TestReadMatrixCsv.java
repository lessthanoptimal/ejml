/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.ops;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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
        String s = "3 2\n0 0\n1 1";

        ReadMatrixCsv alg = new ReadMatrixCsv(new ByteArrayInputStream(s.getBytes()));

        alg.read();
        fail("Should have had an exception");
    }

    @Test(expected=IOException.class)
    public void bad_matrix_col() throws IOException {
        String s = "3 2\n0 0\n1\n0 3";

        ReadMatrixCsv alg = new ReadMatrixCsv(new ByteArrayInputStream(s.getBytes()));

        alg.read();
        fail("Should have had an exception");
    }
}
