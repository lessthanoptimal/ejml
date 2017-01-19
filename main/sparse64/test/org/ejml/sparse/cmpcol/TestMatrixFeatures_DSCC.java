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

package org.ejml.sparse.cmpcol;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.SMatrixCmpC_F64;
import org.ejml.data.SMatrixTriplet_F64;
import org.ejml.sparse.ConvertSparseDMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestMatrixFeatures_DSCC {

    Random rand = new Random(234);

    @Test
    public void isEquals() {
        SMatrixTriplet_F64 orig = new SMatrixTriplet_F64(4,5,3);
        orig.addItem(3,1,2.5);
        orig.addItem(2,4,2.7);
        orig.addItem(2,2,1.5);

        SMatrixCmpC_F64 a = ConvertSparseDMatrix.convert(orig,(SMatrixCmpC_F64)null);
        SMatrixCmpC_F64 b = ConvertSparseDMatrix.convert(orig,(SMatrixCmpC_F64)null);

        a.sortIndices(null); b.sortIndices(null);
        assertTrue(MatrixFeatures_DSCC.isEquals(a,b));

        b.numRows += 1;
        assertFalse(MatrixFeatures_DSCC.isEquals(a,b));
        b.numRows -= 1; b.numCols += 1;
        assertFalse(MatrixFeatures_DSCC.isEquals(a,b));
        b.numCols -= 1; b.nz_rows[1]++;
        assertFalse(MatrixFeatures_DSCC.isEquals(a,b));
        b.nz_rows[1]--; b.col_idx[1]++;
        assertFalse(MatrixFeatures_DSCC.isEquals(a,b));
        b.col_idx[1]--;

        // make it no longer exactly equal
        b.nz_values[0] += UtilEjml.TEST_F64*0.1;
        assertFalse(MatrixFeatures_DSCC.isEquals(a,b));
    }

    @Test
    public void isEqualsSort_tol() {
        SMatrixTriplet_F64 orig = new SMatrixTriplet_F64(4,5,3);
        orig.addItem(3,1,2.5);
        orig.addItem(2,4,2.7);
        orig.addItem(2,2,1.5);

        SMatrixCmpC_F64 a = ConvertSparseDMatrix.convert(orig,(SMatrixCmpC_F64)null);

        orig = new SMatrixTriplet_F64(4,5,3);
        orig.addItem(3,1,2.5);
        orig.addItem(2,2,1.5);
        orig.addItem(2,4,2.7);
        SMatrixCmpC_F64 b = ConvertSparseDMatrix.convert(orig,(SMatrixCmpC_F64)null);

        // these require sorting and this will fail if not
        assertTrue(MatrixFeatures_DSCC.isEqualsSort(a,b, UtilEjml.TEST_F64));
    }

    @Test
    public void isEquals_tol() {
        SMatrixTriplet_F64 orig = new SMatrixTriplet_F64(4,5,3);
        orig.addItem(3,1,2.5);
        orig.addItem(2,4,2.7);
        orig.addItem(2,2,1.5);

        SMatrixCmpC_F64 a = ConvertSparseDMatrix.convert(orig,(SMatrixCmpC_F64)null);
        SMatrixCmpC_F64 b = ConvertSparseDMatrix.convert(orig,(SMatrixCmpC_F64)null);

        assertTrue(MatrixFeatures_DSCC.isEqualsSort(a,b,UtilEjml.TEST_F64));

        b.numRows += 1;
        assertFalse(MatrixFeatures_DSCC.isEqualsSort(a,b,UtilEjml.TEST_F64));
        b.numRows -= 1; b.numCols += 1;
        assertFalse(MatrixFeatures_DSCC.isEqualsSort(a,b,UtilEjml.TEST_F64));
        b.numCols -= 1; b.nz_rows[1]++;
        assertFalse(MatrixFeatures_DSCC.isEqualsSort(a,b,UtilEjml.TEST_F64));
        b.nz_rows[1]--; b.col_idx[1]++;
        assertFalse(MatrixFeatures_DSCC.isEqualsSort(a,b,UtilEjml.TEST_F64));
        b.col_idx[1]--;;

        // make it no longer exactly equal, but within tolerance
        b.nz_values[0] += UtilEjml.TEST_F64*0.1;
        assertTrue(MatrixFeatures_DSCC.isEqualsSort(a,b,UtilEjml.TEST_F64));

        // outside of tolerance
        b.nz_values[0] += UtilEjml.TEST_F64*10;
        assertFalse(MatrixFeatures_DSCC.isEqualsSort(a,b,UtilEjml.TEST_F64));
    }

    @Test
    public void hasUncountable() {
        for( int length : new int[]{0,2,6,15,30} ) {
            SMatrixCmpC_F64 A = RandomMatrices_DSCC.rectangle(6,6,length,rand);

            assertFalse(MatrixFeatures_DSCC.hasUncountable(A));

            if( length == 0 )
                continue;
            int selected = rand.nextInt(length);

            A.nz_values[selected] = Double.NaN;
            assertTrue(MatrixFeatures_DSCC.hasUncountable(A));

            A.nz_values[selected] = Double.POSITIVE_INFINITY;
            assertTrue(MatrixFeatures_DSCC.hasUncountable(A));

            A.nz_values[selected] = Double.NEGATIVE_INFINITY;
            assertTrue(MatrixFeatures_DSCC.hasUncountable(A));
        }
    }

    @Test
    public void isZeros() {
        assertTrue( MatrixFeatures_DSCC.isZeros(new SMatrixCmpC_F64(10,12,0), UtilEjml.TEST_F64));

        SMatrixCmpC_F64 A = RandomMatrices_DSCC.rectangle(6,4,12,rand);
        for (int i = 0; i < A.nz_length; i++) {
            A.nz_values[i] = UtilEjml.EPS;
        }

        assertTrue( MatrixFeatures_DSCC.isZeros(A, UtilEjml.TEST_F64));
    }

    @Test
    public void isIdentity() {
        for( int length : new int[]{1,2,6,15,30} ) {
            SMatrixCmpC_F64 A = CommonOps_DSCC.identity(length);

            assertTrue(MatrixFeatures_DSCC.isIdentity(A, UtilEjml.TEST_F64));

            A.nz_values[0] = 1 + 2.0*UtilEjml.EPS;
            assertTrue(MatrixFeatures_DSCC.isIdentity(A, UtilEjml.TEST_F64));

            A.nz_values[0] = 1.1;
            assertFalse(MatrixFeatures_DSCC.isIdentity(A, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void isLowerTriangle() {
        // normal triangular matrix
        DMatrixRMaj D = new DMatrixRMaj(4,4,true,
                1,0,0,0, 1,1,0,0, 0,0,1,0 , 1,0,1,1 );
        SMatrixCmpC_F64 L = ConvertSparseDMatrix.convert(D,(SMatrixCmpC_F64)null);

        assertTrue(MatrixFeatures_DSCC.isLowerTriangle(L,0, UtilEjml.TEST_F64));
        assertFalse(MatrixFeatures_DSCC.isLowerTriangle(L,1, UtilEjml.TEST_F64));
        L.nz_values[L.nz_length -1] = UtilEjml.EPS;
        assertFalse(MatrixFeatures_DSCC.isLowerTriangle(L,0, UtilEjml.TEST_F64));

        // Hessenberg matrix of degree 1
        D = new DMatrixRMaj(4,4,true,
                1,1,0,0, 1,1,1,0, 0,0,0,1 , 1,0,1,1 );
        L = ConvertSparseDMatrix.convert(D,(SMatrixCmpC_F64)null);

        assertFalse(MatrixFeatures_DSCC.isLowerTriangle(L,0, UtilEjml.TEST_F64));
        assertTrue(MatrixFeatures_DSCC.isLowerTriangle(L,1, UtilEjml.TEST_F64));
        assertFalse(MatrixFeatures_DSCC.isLowerTriangle(L,2, UtilEjml.TEST_F64));
        L.set(0,1,UtilEjml.EPS);
        assertFalse(MatrixFeatures_DSCC.isLowerTriangle(L,1, UtilEjml.TEST_F64));

        // testing a case which failed.  first column was all zeros for hessenberg of 1
        D = new DMatrixRMaj(4,4,true,
                0,1,0,0, 0,0,1,0, 0,0,0,1 , 0,0,0,1 );
        L = ConvertSparseDMatrix.convert(D,(SMatrixCmpC_F64)null);

        assertFalse(MatrixFeatures_DSCC.isLowerTriangle(L,0, UtilEjml.TEST_F64));
        assertTrue(MatrixFeatures_DSCC.isLowerTriangle(L,1, UtilEjml.TEST_F64));
        assertFalse(MatrixFeatures_DSCC.isLowerTriangle(L,2, UtilEjml.TEST_F64));
        L.set(0,1,1);
        assertTrue(MatrixFeatures_DSCC.isLowerTriangle(L,1, UtilEjml.TEST_F64));

    }

}
