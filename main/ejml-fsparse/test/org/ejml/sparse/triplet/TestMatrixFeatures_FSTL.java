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

package org.ejml.sparse.triplet;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixSparseTriplet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestMatrixFeatures_FSTL {
    @Test
    public void isEquals() {
        FMatrixSparseTriplet a = new FMatrixSparseTriplet(4,5,3);
        a.addItem(3,1,2.5f);
        a.addItem(2,4,2.7f);
        a.addItem(2,2,1.5f);

        FMatrixSparseTriplet b = new FMatrixSparseTriplet(a);

        assertTrue(MatrixFeatures_FSTL.isEquals(a,b));

        b.numRows += 1;
        assertFalse(MatrixFeatures_FSTL.isEquals(a,b));
        b.numRows -= 1; b.numCols += 1;
        assertFalse(MatrixFeatures_FSTL.isEquals(a,b));

        // make it no longer exactly equal
        b.numCols -= 1;
        b.nz_value.data[0] += UtilEjml.TEST_F32*0.1f;
        assertFalse(MatrixFeatures_FSTL.isEquals(a,b));
    }

    @Test
    public void isEquals_tol() {
        FMatrixSparseTriplet a = new FMatrixSparseTriplet(4,5,3);
        a.addItem(3,1,2.5f);
        a.addItem(2,4,2.7f);
        a.addItem(2,2,1.5f);

        FMatrixSparseTriplet b = new FMatrixSparseTriplet(a);

        assertTrue(MatrixFeatures_FSTL.isEquals(a,b, UtilEjml.TEST_F32));

        b.numRows += 1;
        assertFalse(MatrixFeatures_FSTL.isEquals(a,b, UtilEjml.TEST_F32));
        b.numRows -= 1; b.numCols += 1;
        assertFalse(MatrixFeatures_FSTL.isEquals(a,b, UtilEjml.TEST_F32));

        // make it no longer exactly equal, but within tolerance
        b.numCols -= 1;
        b.nz_value.data[0] += UtilEjml.TEST_F32*0.1f;
        assertTrue(MatrixFeatures_FSTL.isEquals(a,b, UtilEjml.TEST_F32));

        // outside of tolerance
        b.nz_value.data[0] += UtilEjml.TEST_F32*10;
        assertFalse(MatrixFeatures_FSTL.isEquals(a,b, UtilEjml.TEST_F32));
    }
}
