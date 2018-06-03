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

package org.ejml.sparse.triplet;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparseTriplet;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestMatrixFeatures_DSTL {
    @Test
    public void isEquals() {
        DMatrixSparseTriplet a = new DMatrixSparseTriplet(4,5,3);
        a.addItem(3,1,2.5);
        a.addItem(2,4,2.7);
        a.addItem(2,2,1.5);

        DMatrixSparseTriplet b = new DMatrixSparseTriplet(a);

        assertTrue(MatrixFeatures_DSTL.isEquals(a,b));

        b.numRows += 1;
        assertFalse(MatrixFeatures_DSTL.isEquals(a,b));
        b.numRows -= 1; b.numCols += 1;
        assertFalse(MatrixFeatures_DSTL.isEquals(a,b));

        // make it no longer exactly equal
        b.numCols -= 1;
        b.nz_value.data[0] += UtilEjml.TEST_F64*0.1;
        assertFalse(MatrixFeatures_DSTL.isEquals(a,b));
    }

    @Test
    public void isEquals_tol() {
        DMatrixSparseTriplet a = new DMatrixSparseTriplet(4,5,3);
        a.addItem(3,1,2.5);
        a.addItem(2,4,2.7);
        a.addItem(2,2,1.5);

        DMatrixSparseTriplet b = new DMatrixSparseTriplet(a);

        assertTrue(MatrixFeatures_DSTL.isEquals(a,b, UtilEjml.TEST_F64));

        b.numRows += 1;
        assertFalse(MatrixFeatures_DSTL.isEquals(a,b, UtilEjml.TEST_F64));
        b.numRows -= 1; b.numCols += 1;
        assertFalse(MatrixFeatures_DSTL.isEquals(a,b, UtilEjml.TEST_F64));

        // make it no longer exactly equal, but within tolerance
        b.numCols -= 1;
        b.nz_value.data[0] += UtilEjml.TEST_F64*0.1;
        assertTrue(MatrixFeatures_DSTL.isEquals(a,b, UtilEjml.TEST_F64));

        // outside of tolerance
        b.nz_value.data[0] += UtilEjml.TEST_F64*10;
        assertFalse(MatrixFeatures_DSTL.isEquals(a,b, UtilEjml.TEST_F64));
    }
}
