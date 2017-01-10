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

package org.ejml.sparse.compcol;

import org.ejml.UtilEjml;
import org.ejml.data.SMatrixCC_F64;
import org.ejml.data.SMatrixTriplet_F64;
import org.ejml.sparse.ConvertSparseMatrix_F64;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestMatrixFeatures_CC64 {
    @Test
    public void isEquals() {
        SMatrixTriplet_F64 orig = new SMatrixTriplet_F64(4,5,3);
        orig.addItem(3,1,2.5);
        orig.addItem(2,4,2.7);
        orig.addItem(2,2,1.5);

        SMatrixCC_F64 a = ConvertSparseMatrix_F64.convert(orig,(SMatrixCC_F64)null,null);
        SMatrixCC_F64 b = ConvertSparseMatrix_F64.convert(orig,(SMatrixCC_F64)null,null);

        assertTrue(MatrixFeatures_CC64.isEquals(a,b));

        b.numRows += 1;
        assertFalse(MatrixFeatures_CC64.isEquals(a,b));
        b.numRows -= 1; b.numCols += 1;
        assertFalse(MatrixFeatures_CC64.isEquals(a,b));

        // make it no longer exactly equal
        b.numCols -= 1;
        b.data[0] += UtilEjml.TEST_F64*0.1;
        assertFalse(MatrixFeatures_CC64.isEquals(a,b));
    }

    @Test
    public void isEquals_tol() {
        SMatrixTriplet_F64 orig = new SMatrixTriplet_F64(4,5,3);
        orig.addItem(3,1,2.5);
        orig.addItem(2,4,2.7);
        orig.addItem(2,2,1.5);

        SMatrixCC_F64 a = ConvertSparseMatrix_F64.convert(orig,(SMatrixCC_F64)null,null);
        SMatrixCC_F64 b = ConvertSparseMatrix_F64.convert(orig,(SMatrixCC_F64)null,null);

        assertTrue(MatrixFeatures_CC64.isEquals(a,b,UtilEjml.TEST_F64));

        b.numRows += 1;
        assertFalse(MatrixFeatures_CC64.isEquals(a,b,UtilEjml.TEST_F64));
        b.numRows -= 1; b.numCols += 1;
        assertFalse(MatrixFeatures_CC64.isEquals(a,b,UtilEjml.TEST_F64));

        // make it no longer exactly equal, but within tolerance
        b.numCols -= 1;
        b.data[0] += UtilEjml.TEST_F64*0.1;
        assertTrue(MatrixFeatures_CC64.isEquals(a,b,UtilEjml.TEST_F64));

        // outside of tolerance
        b.data[0] += UtilEjml.TEST_F64*10;
        assertFalse(MatrixFeatures_CC64.isEquals(a,b,UtilEjml.TEST_F64));
    }
}
