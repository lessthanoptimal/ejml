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

package org.ejml.sparse.csc;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.FMatrixSparseCSC;
import org.ejml.data.FMatrixSparseTriplet;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.ops.ConvertFMatrixStruct;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestMatrixFeatures_FSCC {

    Random rand = new Random(234);

    @Test
    public void isEquals() {
        FMatrixSparseTriplet orig = new FMatrixSparseTriplet(4,5,3);
        orig.addItem(3,1,2.5f);
        orig.addItem(2,4,2.7f);
        orig.addItem(2,2,1.5f);

        FMatrixSparseCSC a = ConvertFMatrixStruct.convert(orig,(FMatrixSparseCSC)null);
        FMatrixSparseCSC b = ConvertFMatrixStruct.convert(orig,(FMatrixSparseCSC)null);

        a.sortIndices(null); b.sortIndices(null);
        assertTrue(MatrixFeatures_FSCC.isEquals(a,b));

        b.numRows += 1;
        assertFalse(MatrixFeatures_FSCC.isEquals(a,b));
        b.numRows -= 1; b.numCols += 1;
        assertFalse(MatrixFeatures_FSCC.isEquals(a,b));
        b.numCols -= 1; b.nz_rows[1]++;
        assertFalse(MatrixFeatures_FSCC.isEquals(a,b));
        b.nz_rows[1]--; b.col_idx[1]++;
        assertFalse(MatrixFeatures_FSCC.isEquals(a,b));
        b.col_idx[1]--;

        // make it no longer exactly equal
        b.nz_values[0] += UtilEjml.TEST_F32*0.1f;
        assertFalse(MatrixFeatures_FSCC.isEquals(a,b));
    }

    @Test
    public void isEqualsSort_tol() {
        FMatrixSparseTriplet orig = new FMatrixSparseTriplet(4,5,3);
        orig.addItem(3,1,2.5f);
        orig.addItem(2,4,2.7f);
        orig.addItem(2,2,1.5f);

        FMatrixSparseCSC a = ConvertFMatrixStruct.convert(orig,(FMatrixSparseCSC)null);

        orig = new FMatrixSparseTriplet(4,5,3);
        orig.addItem(3,1,2.5f);
        orig.addItem(2,2,1.5f);
        orig.addItem(2,4,2.7f);
        FMatrixSparseCSC b = ConvertFMatrixStruct.convert(orig,(FMatrixSparseCSC)null);

        // these require sorting and this will fail if not
        assertTrue(MatrixFeatures_FSCC.isEqualsSort(a,b, UtilEjml.TEST_F32));
    }

    @Test
    public void isIdenticalSort_tol() {
        FMatrixSparseTriplet orig = new FMatrixSparseTriplet(4,5,3);
        orig.addItem(3,1,2.5f);
        orig.addItem(2,4,2.7f);
        orig.addItem(2,2,1.5f);

        FMatrixSparseCSC a = ConvertFMatrixStruct.convert(orig,(FMatrixSparseCSC)null);

        orig = new FMatrixSparseTriplet(4,5,3);
        orig.addItem(3,1,2.5f);
        orig.addItem(2,2,1.5f);
        orig.addItem(2,4,2.7f);
        FMatrixSparseCSC b = ConvertFMatrixStruct.convert(orig,(FMatrixSparseCSC)null);

        // these require sorting and this will fail if not
        assertTrue(MatrixFeatures_FSCC.isIdenticalSort(a,b, UtilEjml.TEST_F32));

        float values[] = new float[]{1.0f,Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY};

        for( int i = 0; i < values.length; i++ ) {
            for( int j = 0; j < values.length; j++ ) {
                a.set(2,2,values[i]);
                b.set(2,2,values[j]);
                assertEquals(i==j,MatrixFeatures_FSCC.isIdenticalSort(a,b, UtilEjml.TEST_F32));
            }
        }
    }

    @Test
    public void isEquals_tol() {
        FMatrixSparseTriplet orig = new FMatrixSparseTriplet(4,5,3);
        orig.addItem(3,1,2.5f);
        orig.addItem(2,4,2.7f);
        orig.addItem(2,2,1.5f);

        FMatrixSparseCSC a = ConvertFMatrixStruct.convert(orig,(FMatrixSparseCSC)null);
        FMatrixSparseCSC b = ConvertFMatrixStruct.convert(orig,(FMatrixSparseCSC)null);

        assertTrue(MatrixFeatures_FSCC.isEqualsSort(a,b,UtilEjml.TEST_F32));

        b.numRows += 1;
        assertFalse(MatrixFeatures_FSCC.isEqualsSort(a,b,UtilEjml.TEST_F32));
        b.numRows -= 1; b.numCols += 1;
        assertFalse(MatrixFeatures_FSCC.isEqualsSort(a,b,UtilEjml.TEST_F32));
        b.numCols -= 1; b.nz_rows[1]++;
        assertFalse(MatrixFeatures_FSCC.isEqualsSort(a,b,UtilEjml.TEST_F32));
        b.nz_rows[1]--; b.col_idx[1]++;
        assertFalse(MatrixFeatures_FSCC.isEqualsSort(a,b,UtilEjml.TEST_F32));
        b.col_idx[1]--;;

        // make it no longer exactly equal, but within tolerance
        b.nz_values[0] += UtilEjml.TEST_F32*0.1f;
        assertTrue(MatrixFeatures_FSCC.isEqualsSort(a,b,UtilEjml.TEST_F32));

        // outside of tolerance
        b.nz_values[0] += UtilEjml.TEST_F32*10;
        assertFalse(MatrixFeatures_FSCC.isEqualsSort(a,b,UtilEjml.TEST_F32));
    }

    @Test
    public void hasUncountable() {
        for( int length : new int[]{0,2,6,15,30} ) {
            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(6,6,length,rand);

            assertFalse(MatrixFeatures_FSCC.hasUncountable(A));

            if( length == 0 )
                continue;
            int selected = rand.nextInt(length);

            A.nz_values[selected] = Float.NaN;
            assertTrue(MatrixFeatures_FSCC.hasUncountable(A));

            A.nz_values[selected] = Float.POSITIVE_INFINITY;
            assertTrue(MatrixFeatures_FSCC.hasUncountable(A));

            A.nz_values[selected] = Float.NEGATIVE_INFINITY;
            assertTrue(MatrixFeatures_FSCC.hasUncountable(A));
        }
    }

    @Test
    public void isZeros() {
        assertTrue( MatrixFeatures_FSCC.isZeros(new FMatrixSparseCSC(10,12,0), UtilEjml.TEST_F32));

        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(6,4,12,rand);
        for (int i = 0; i < A.nz_length; i++) {
            A.nz_values[i] = UtilEjml.F_EPS;
        }

        assertTrue( MatrixFeatures_FSCC.isZeros(A, UtilEjml.TEST_F32));
    }

    @Test
    public void isIdentity() {
        for( int length : new int[]{1,2,6,15,30} ) {
            FMatrixSparseCSC A = CommonOps_FSCC.identity(length);

            assertTrue(MatrixFeatures_FSCC.isIdentity(A, UtilEjml.TEST_F32));

            A.nz_values[0] = 1 + 2.0f*UtilEjml.F_EPS;
            assertTrue(MatrixFeatures_FSCC.isIdentity(A, UtilEjml.TEST_F32));

            A.nz_values[0] = 1.1f;
            assertFalse(MatrixFeatures_FSCC.isIdentity(A, UtilEjml.TEST_F32));
        }
    }

    @Test
    public void isLowerTriangle() {
        // normal triangular matrix
        FMatrixRMaj D = new FMatrixRMaj(4,4,true,
                1,0,0,0, 1,1,0,0, 0,0,1,0 , 1,0,1,1 );
        FMatrixSparseCSC L = ConvertFMatrixStruct.convert(D,(FMatrixSparseCSC)null, UtilEjml.F_EPS);

        assertTrue(MatrixFeatures_FSCC.isLowerTriangle(L,0, UtilEjml.TEST_F32));
        assertFalse(MatrixFeatures_FSCC.isLowerTriangle(L,1, UtilEjml.TEST_F32));
        L.nz_values[L.nz_length -1] = UtilEjml.F_EPS;
        assertFalse(MatrixFeatures_FSCC.isLowerTriangle(L,0, UtilEjml.TEST_F32));

        // Hessenberg matrix of degree 1
        D = new FMatrixRMaj(4,4,true,
                1,1,0,0, 1,1,1,0, 0,0,0,1 , 1,0,1,1 );
        L = ConvertFMatrixStruct.convert(D,(FMatrixSparseCSC)null, UtilEjml.F_EPS);

        assertFalse(MatrixFeatures_FSCC.isLowerTriangle(L,0, UtilEjml.TEST_F32));
        assertTrue(MatrixFeatures_FSCC.isLowerTriangle(L,1, UtilEjml.TEST_F32));
        assertFalse(MatrixFeatures_FSCC.isLowerTriangle(L,2, UtilEjml.TEST_F32));
        L.set(0,1,UtilEjml.F_EPS);
        assertFalse(MatrixFeatures_FSCC.isLowerTriangle(L,1, UtilEjml.TEST_F32));

        // testing a case which failed.  first column was all zeros for hessenberg of 1
        D = new FMatrixRMaj(4,4,true,
                0,1,0,0, 0,0,1,0, 0,0,0,1 , 0,0,0,1 );
        L = ConvertFMatrixStruct.convert(D,(FMatrixSparseCSC)null, UtilEjml.F_EPS);

        assertFalse(MatrixFeatures_FSCC.isLowerTriangle(L,0, UtilEjml.TEST_F32));
        assertTrue(MatrixFeatures_FSCC.isLowerTriangle(L,1, UtilEjml.TEST_F32));
        assertFalse(MatrixFeatures_FSCC.isLowerTriangle(L,2, UtilEjml.TEST_F32));
        L.set(0,1,1);
        assertTrue(MatrixFeatures_FSCC.isLowerTriangle(L,1, UtilEjml.TEST_F32));
    }

    @Test
    public void isTranspose() {

        // do it first in dense matrices
        FMatrixRMaj Ad = RandomMatrices_FDRM.rectangle(5,4,-1,1,rand);
        Ad.data[5] = 0; Ad.data[10] = 0; Ad.data[11] = 0;

        FMatrixRMaj Ad_tran = new FMatrixRMaj(4,5);
        CommonOps_FDRM.transpose(Ad,Ad_tran);

        // convert to sparse
        FMatrixSparseCSC A = ConvertFMatrixStruct.convert(Ad,(FMatrixSparseCSC)null, UtilEjml.F_EPS);
        FMatrixSparseCSC At = ConvertFMatrixStruct.convert(Ad_tran,(FMatrixSparseCSC)null, UtilEjml.F_EPS);

        A.sortIndices(null);
        assertTrue(MatrixFeatures_FSCC.isTranspose(A,At, UtilEjml.TEST_F32));
        At.nz_values[4] += 0.01f;
        assertFalse(MatrixFeatures_FSCC.isTranspose(A,At, UtilEjml.TEST_F32));

    }

    @Test
    public void isVector() {
        assertTrue(MatrixFeatures_FSCC.isVector(new FMatrixSparseCSC(10,1,5)));
        assertTrue(MatrixFeatures_FSCC.isVector(new FMatrixSparseCSC(1,10,5)));
        assertFalse(MatrixFeatures_FSCC.isVector(new FMatrixSparseCSC(10,10,5)));
        assertFalse(MatrixFeatures_FSCC.isVector(new FMatrixSparseCSC(1,1,5)));
    }

    @Test
    public void isSymmetric() {
        FMatrixSparseCSC A = new FMatrixSparseCSC(5,4,2);

        // not square
        assertFalse(MatrixFeatures_FSCC.isSymmetric(A,UtilEjml.TEST_F32));

        // a matrix with all zeros is symmetric
        A.reshape(5,5,2);
        assertTrue(MatrixFeatures_FSCC.isSymmetric(A,UtilEjml.TEST_F32));

        // test with various random matrices
        for (int N = 1; N < 10; N++) {
            for (int mc = 0; mc < 30; mc++) {
                int nz = (int)Math.ceil(N*N*(rand.nextFloat()*0.4f+0.1f));
                A = RandomMatrices_FSCC.rectangle(N,N,nz,rand);

                FMatrixSparseCSC C = new FMatrixSparseCSC(N,N,0);

                // C must be symmetric
                CommonOps_FSCC.multTransB(A,A,C,null,null);
                assertTrue(MatrixFeatures_FSCC.isSymmetric(C,UtilEjml.TEST_F32));

                // make it not symmetric
                if( N > 1 ) {
                    int index = rand.nextInt(C.nz_length);
                    C.nz_values[index] += 0.1f;

                    // see if it modified a diagonal element. still will be diagonal if it did
                    int row =  C.nz_rows[index];
                    int col = 0;
                    while( col < C.numCols ) {
                        if( index < C.col_idx[col+1] )
                            break;
                        col++;
                    }
                    if( row != col )
                        assertFalse(MatrixFeatures_FSCC.isSymmetric(C, UtilEjml.TEST_F32));
                }
            }
        }
    }

    @Test
    public void isPositiveDefinite() {
        FMatrixSparseCSC a = UtilEjml.parse_FSCC("2 0 0 2",2);
        FMatrixSparseCSC b = UtilEjml.parse_FSCC("0 1 1 0",2);
        FMatrixSparseCSC c = UtilEjml.parse_FSCC("0 0 0 0",2);

        assertTrue(MatrixFeatures_FSCC.isPositiveDefinite(a));
        assertFalse(MatrixFeatures_FSCC.isPositiveDefinite(b));
        assertFalse(MatrixFeatures_FSCC.isPositiveDefinite(c));
    }

    @Test
    public void isOrthogonal() {
        // rotation matrices are orthogonal
        float c = (float)Math.cos(0.1f);
        float s = (float)Math.sin(0.1f);

        FMatrixSparseCSC A = new FMatrixSparseCSC(2,2,2);
        A.set(0,0,c);A.set(0,1,s);
        A.set(1,0,-s);A.set(1,1,c);

        assertTrue(MatrixFeatures_FSCC.isOrthogonal(A,UtilEjml.TEST_F32_SQ));

        // try a negative case now
        A.set(0,1,495);

        assertFalse(MatrixFeatures_FSCC.isOrthogonal(A,UtilEjml.TEST_F32_SQ));

        A.set(0,1,Float.NaN);

        assertFalse(MatrixFeatures_FSCC.isOrthogonal(A,UtilEjml.TEST_F32_SQ));
    }

}
