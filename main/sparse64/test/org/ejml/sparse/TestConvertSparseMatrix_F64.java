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

package org.ejml.sparse;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.data.SMatrixCmpC_F64;
import org.ejml.data.SMatrixTriplet_F64;
import org.ejml.dense.row.MatrixFeatures_R64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.ejml.sparse.cmpcol.MatrixFeatures_O64;
import org.ejml.sparse.cmpcol.RandomMatrices_O64;
import org.ejml.sparse.triplet.MatrixFeatures_T64;
import org.ejml.sparse.triplet.RandomMatrices_T64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestConvertSparseMatrix_F64 {

    Random rand = new Random(234);

    @Test
    public void DMatrixRow_SMatrixTriplet() {
        DMatrixRow_F64 a = RandomMatrices_R64.createRandom(5,6,-1,1,rand);

        a.set(4,3, 0);
        a.set(1,3, 0);
        a.set(2,3, 0);
        a.set(2,0, 0);

        DMatrixRow_SMatrixTriplet(a,null);
        DMatrixRow_SMatrixTriplet(a, new SMatrixTriplet_F64(1,1,2));
    }

    public void DMatrixRow_SMatrixTriplet(DMatrixRow_F64 a , SMatrixTriplet_F64 b ) {
        b = ConvertSparseMatrix_F64.convert(a,b);

        assertEquals(a.numRows, b.numRows);
        assertEquals(a.numCols, b.numCols);
        assertEquals(5*6-4, b.nz_length);
        for (int row = 0; row < a.numRows; row++) {
            for (int col = 0; col < a.numCols; col++) {
                SMatrixTriplet_F64.Element e = b.findItem(row,col);

                if( a.get(row,col) == 0.0 ) {
                    assertTrue( null == e );
                } else {
                    assertEquals( a.get(row,col), e.value, UtilEjml.TEST_F64);
                }
            }
        }

        // now try it the other direction
        DMatrixRow_F64 c = ConvertSparseMatrix_F64.convert(b,(DMatrixRow_F64)null);
        assertTrue(MatrixFeatures_R64.isEquals(a,c, UtilEjml.TEST_F64));

        c = ConvertSparseMatrix_F64.convert(b,new DMatrixRow_F64(1,1));
        assertTrue(MatrixFeatures_R64.isEquals(a,c, UtilEjml.TEST_F64));
    }

    @Test
    public void SMatrixCC_DMatrixRow() {
        SMatrixCmpC_F64 a = RandomMatrices_O64.uniform(5,6,10,-1,1,rand);

        SMatrixCC_DMatrixRow(a,null);
        SMatrixCC_DMatrixRow(a,new DMatrixRow_F64(1,1));
    }

    public void SMatrixCC_DMatrixRow(SMatrixCmpC_F64 a , DMatrixRow_F64 b ) {
        b = ConvertSparseMatrix_F64.convert(a,b);

        assertEquals(a.numRows, b.numRows);
        assertEquals(a.numCols, b.numCols);

        int found = MatrixFeatures_R64.countNonZero(b);
        assertEquals(a.nz_length, found);
        EjmlUnitTests.assertEquals(a, b);

        // now try it the other direction
        SMatrixCmpC_F64 c = ConvertSparseMatrix_F64.convert(b,(SMatrixCmpC_F64)null);
        assertTrue(MatrixFeatures_O64.isEquals(a,c, UtilEjml.TEST_F64));
        assertTrue(c.isRowOrderValid());

        c = ConvertSparseMatrix_F64.convert(b,new SMatrixCmpC_F64(1,1,1));
        assertTrue(MatrixFeatures_O64.isEquals(a,c, UtilEjml.TEST_F64));
        assertTrue(c.isRowOrderValid());
    }

    @Test
    public void SMatrixTriplet_SMatrixCC() {
        SMatrixTriplet_F64 a = RandomMatrices_T64.uniform(5,6,10,-1,1,rand);

        SMatrixTriplet_SMatrixCC(a,(SMatrixCmpC_F64)null);
        SMatrixTriplet_SMatrixCC(a,new SMatrixCmpC_F64(1,1,2));
    }

    public void SMatrixTriplet_SMatrixCC(SMatrixTriplet_F64 a , SMatrixCmpC_F64 b ) {
        b = ConvertSparseMatrix_F64.convert(a,b);

        assertEquals(a.numRows, b.numRows);
        assertEquals(a.numCols, b.numCols);
        assertEquals(a.nz_length, b.nz_length);
        for (int i = 0; i < a.nz_length; i++) {
            SMatrixTriplet_F64.Element e = a.nz_data[i];
            assertEquals(e.value, b.get(e.row, e.col), UtilEjml.TEST_F64);
        }
        assertTrue(b.isRowOrderValid());

        // now try it the other direction
        SMatrixTriplet_F64 c = ConvertSparseMatrix_F64.convert(b,(SMatrixTriplet_F64)null);
        assertTrue(MatrixFeatures_T64.isEquals(a,c, UtilEjml.TEST_F64));

        c = ConvertSparseMatrix_F64.convert(b,new SMatrixTriplet_F64(1,1,1));
        assertTrue(MatrixFeatures_T64.isEquals(a,c, UtilEjml.TEST_F64));
    }
}
