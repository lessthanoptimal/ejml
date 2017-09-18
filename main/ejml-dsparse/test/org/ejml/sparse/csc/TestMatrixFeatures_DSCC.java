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

package org.ejml.sparse.csc;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.ops.ConvertDMatrixStruct;
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
        DMatrixSparseTriplet orig = new DMatrixSparseTriplet(4,5,3);
        orig.addItem(3,1,2.5);
        orig.addItem(2,4,2.7);
        orig.addItem(2,2,1.5);

        DMatrixSparseCSC a = ConvertDMatrixStruct.convert(orig,(DMatrixSparseCSC)null);
        DMatrixSparseCSC b = ConvertDMatrixStruct.convert(orig,(DMatrixSparseCSC)null);

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
        DMatrixSparseTriplet orig = new DMatrixSparseTriplet(4,5,3);
        orig.addItem(3,1,2.5);
        orig.addItem(2,4,2.7);
        orig.addItem(2,2,1.5);

        DMatrixSparseCSC a = ConvertDMatrixStruct.convert(orig,(DMatrixSparseCSC)null);

        orig = new DMatrixSparseTriplet(4,5,3);
        orig.addItem(3,1,2.5);
        orig.addItem(2,2,1.5);
        orig.addItem(2,4,2.7);
        DMatrixSparseCSC b = ConvertDMatrixStruct.convert(orig,(DMatrixSparseCSC)null);

        // these require sorting and this will fail if not
        assertTrue(MatrixFeatures_DSCC.isEqualsSort(a,b, UtilEjml.TEST_F64));
    }

    @Test
    public void isEquals_tol() {
        DMatrixSparseTriplet orig = new DMatrixSparseTriplet(4,5,3);
        orig.addItem(3,1,2.5);
        orig.addItem(2,4,2.7);
        orig.addItem(2,2,1.5);

        DMatrixSparseCSC a = ConvertDMatrixStruct.convert(orig,(DMatrixSparseCSC)null);
        DMatrixSparseCSC b = ConvertDMatrixStruct.convert(orig,(DMatrixSparseCSC)null);

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
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,6,length,rand);

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
        assertTrue( MatrixFeatures_DSCC.isZeros(new DMatrixSparseCSC(10,12,0), UtilEjml.TEST_F64));

        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,4,12,rand);
        for (int i = 0; i < A.nz_length; i++) {
            A.nz_values[i] = UtilEjml.EPS;
        }

        assertTrue( MatrixFeatures_DSCC.isZeros(A, UtilEjml.TEST_F64));
    }

    @Test
    public void isIdentity() {
        for( int length : new int[]{1,2,6,15,30} ) {
            DMatrixSparseCSC A = CommonOps_DSCC.identity(length);

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
        DMatrixSparseCSC L = ConvertDMatrixStruct.convert(D,(DMatrixSparseCSC)null, UtilEjml.EPS);

        assertTrue(MatrixFeatures_DSCC.isLowerTriangle(L,0, UtilEjml.TEST_F64));
        assertFalse(MatrixFeatures_DSCC.isLowerTriangle(L,1, UtilEjml.TEST_F64));
        L.nz_values[L.nz_length -1] = UtilEjml.EPS;
        assertFalse(MatrixFeatures_DSCC.isLowerTriangle(L,0, UtilEjml.TEST_F64));

        // Hessenberg matrix of degree 1
        D = new DMatrixRMaj(4,4,true,
                1,1,0,0, 1,1,1,0, 0,0,0,1 , 1,0,1,1 );
        L = ConvertDMatrixStruct.convert(D,(DMatrixSparseCSC)null, UtilEjml.EPS);

        assertFalse(MatrixFeatures_DSCC.isLowerTriangle(L,0, UtilEjml.TEST_F64));
        assertTrue(MatrixFeatures_DSCC.isLowerTriangle(L,1, UtilEjml.TEST_F64));
        assertFalse(MatrixFeatures_DSCC.isLowerTriangle(L,2, UtilEjml.TEST_F64));
        L.set(0,1,UtilEjml.EPS);
        assertFalse(MatrixFeatures_DSCC.isLowerTriangle(L,1, UtilEjml.TEST_F64));

        // testing a case which failed.  first column was all zeros for hessenberg of 1
        D = new DMatrixRMaj(4,4,true,
                0,1,0,0, 0,0,1,0, 0,0,0,1 , 0,0,0,1 );
        L = ConvertDMatrixStruct.convert(D,(DMatrixSparseCSC)null, UtilEjml.EPS);

        assertFalse(MatrixFeatures_DSCC.isLowerTriangle(L,0, UtilEjml.TEST_F64));
        assertTrue(MatrixFeatures_DSCC.isLowerTriangle(L,1, UtilEjml.TEST_F64));
        assertFalse(MatrixFeatures_DSCC.isLowerTriangle(L,2, UtilEjml.TEST_F64));
        L.set(0,1,1);
        assertTrue(MatrixFeatures_DSCC.isLowerTriangle(L,1, UtilEjml.TEST_F64));
    }

    @Test
    public void isTranspose() {

        // do it first in dense matrices
        DMatrixRMaj Ad = RandomMatrices_DDRM.rectangle(5,4,-1,1,rand);
        Ad.data[5] = 0; Ad.data[10] = 0; Ad.data[11] = 0;

        DMatrixRMaj Ad_tran = new DMatrixRMaj(4,5);
        CommonOps_DDRM.transpose(Ad,Ad_tran);

        // convert to sparse
        DMatrixSparseCSC A = ConvertDMatrixStruct.convert(Ad,(DMatrixSparseCSC)null, UtilEjml.EPS);
        DMatrixSparseCSC At = ConvertDMatrixStruct.convert(Ad_tran,(DMatrixSparseCSC)null, UtilEjml.EPS);

        A.sortIndices(null);
        assertTrue(MatrixFeatures_DSCC.isTranspose(A,At, UtilEjml.TEST_F64));
        At.nz_values[4] += 0.01;
        assertFalse(MatrixFeatures_DSCC.isTranspose(A,At, UtilEjml.TEST_F64));

    }

    @Test
    public void isVector() {
        assertTrue(MatrixFeatures_DSCC.isVector(new DMatrixSparseCSC(10,1,5)));
        assertTrue(MatrixFeatures_DSCC.isVector(new DMatrixSparseCSC(1,10,5)));
        assertFalse(MatrixFeatures_DSCC.isVector(new DMatrixSparseCSC(10,10,5)));
        assertFalse(MatrixFeatures_DSCC.isVector(new DMatrixSparseCSC(1,1,5)));
    }

    @Test
    public void isSymmetric() {
        DMatrixSparseCSC A = new DMatrixSparseCSC(5,4,2);

        // not square
        assertFalse(MatrixFeatures_DSCC.isSymmetric(A,UtilEjml.TEST_F64));

        // a matrix with all zeros is symmetric
        A.reshape(5,5,2);
        assertTrue(MatrixFeatures_DSCC.isSymmetric(A,UtilEjml.TEST_F64));

        // test with various random matrices
        for (int N = 1; N < 10; N++) {
            for (int mc = 0; mc < 30; mc++) {
                int nz = (int)Math.ceil(N*N*(rand.nextDouble()*0.4+0.1));
                A = RandomMatrices_DSCC.rectangle(N,N,nz,rand);

                DMatrixSparseCSC C = new DMatrixSparseCSC(N,N,0);

                // C must be symmetric
                CommonOps_DSCC.multTransB(A,A,C,null,null);
                assertTrue(MatrixFeatures_DSCC.isSymmetric(C,UtilEjml.TEST_F64));

                // make it not symmetric
                if( N > 1 ) {
                    int index = rand.nextInt(C.nz_length);
                    C.nz_values[index] += 0.1;

                    // see if it modified a diagonal element. still will be diagonal if it did
                    int row =  C.nz_rows[index];
                    int col = 0;
                    while( col < C.numCols ) {
                        if( index < C.col_idx[col+1] )
                            break;
                        col++;
                    }
                    if( row != col )
                        assertFalse(MatrixFeatures_DSCC.isSymmetric(C, UtilEjml.TEST_F64));
                }
            }
        }
    }

    @Test
    public void isPositiveDefinite() {
        DMatrixSparseCSC a = UtilEjml.parse_DSCC("2 0 0 2",2);
        DMatrixSparseCSC b = UtilEjml.parse_DSCC("0 1 1 0",2);
        DMatrixSparseCSC c = UtilEjml.parse_DSCC("0 0 0 0",2);

        assertTrue(MatrixFeatures_DSCC.isPositiveDefinite(a));
        assertFalse(MatrixFeatures_DSCC.isPositiveDefinite(b));
        assertFalse(MatrixFeatures_DSCC.isPositiveDefinite(c));
    }

    @Test
    public void isOrthogonal() {
        // rotation matrices are orthogonal
        double c = Math.cos(0.1);
        double s = Math.sin(0.1);

        DMatrixSparseCSC A = new DMatrixSparseCSC(2,2,2);
        A.set(0,0,c);A.set(0,1,s);
        A.set(1,0,-s);A.set(1,1,c);

        assertTrue(MatrixFeatures_DSCC.isOrthogonal(A,UtilEjml.TEST_F64_SQ));

        // try a negative case now
        A.set(0,1,495);

        assertFalse(MatrixFeatures_DSCC.isOrthogonal(A,UtilEjml.TEST_F64_SQ));

        A.set(0,1,Double.NaN);

        assertFalse(MatrixFeatures_DSCC.isOrthogonal(A,UtilEjml.TEST_F64_SQ));
    }

}
