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

package org.ejml.ops;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.*;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.MatrixFeatures_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.ejml.sparse.triplet.MatrixFeatures_DSTL;
import org.ejml.sparse.triplet.RandomMatrices_DSTL;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestConvertDMatrixStruct {

    Random rand = new Random(234);

    @Test
    public void any_to_any() {
        DMatrixRMaj a = new DMatrixRMaj(2,3,true,1,2,3,4,5,6);
        DMatrixRMaj b = new DMatrixRMaj(2,3);

        ConvertDMatrixStruct.convert((DMatrix)a,(DMatrix)b);

        assertTrue(MatrixFeatures_DDRM.isIdentical(a,b,UtilEjml.TEST_F64));
    }

    @Test
    public void checkAll_Fixed_to_DM() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Method[] methods = ConvertDMatrixStruct.class.getMethods();

        int numFound = 0;

        for( Method m : methods ) {
            if( !m.getName().equals("convert") )
                continue;
            Class[]param = m.getParameterTypes();

            if( !DMatrixFixed.class.isAssignableFrom(param[0]) ) {
                continue;
            }

            DMatrixFixed a = (DMatrixFixed)param[0].newInstance();
            DMatrixRMaj b = new DMatrixRMaj(a.getNumRows(),a.getNumCols());

            for( int i = 0; i < b.numRows; i++ ) {
                for( int j = 0; j < b.numCols; j++ ) {
                    a.set(i, j, rand.nextDouble());
                }
            }

            Object[] input = new Object[param.length];
            input[0] = a;
            input[1] = b;

            m.invoke(null,input);

            checkIdentical(a,b);

            numFound++;
        }

        assertEquals(5+5,numFound);
    }

    @Test
    public void checkAll_DM_to_Fixed() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Method[] methods = ConvertDMatrixStruct.class.getMethods();

        int numFound = 0;

        for( Method m : methods ) {
            if( !m.getName().equals("convert") )
                continue;
            Class[]param = m.getParameterTypes();

            if( !DMatrixFixed.class.isAssignableFrom(param[1]) ) {
                continue;
            }

            DMatrixFixed b = (DMatrixFixed)param[1].newInstance();
            DMatrixRMaj a = new DMatrixRMaj(b.getNumRows(),b.getNumCols());

            for( int i = 0; i < a.numRows; i++ ) {
                for( int j = 0; j < a.numCols; j++ ) {
                    a.set(i, j, rand.nextDouble());
                }
            }

            Object[] input = new Object[param.length];
            input[0] = a;
            input[1] = b;

            m.invoke(null,input);

            checkIdentical(a,b);

            numFound++;
        }

        assertEquals(5+5,numFound);
    }

    @Test
    public void BM_to_DM() {
        for( int rows = 1; rows <= 8; rows++ ) {
            for( int cols = 1; cols <= 8; cols++ ) {
                DMatrixRBlock a = MatrixOps_DDRB.createRandom(rows,cols,-1,2,rand);
                DMatrixRMaj b = new DMatrixRMaj(rows,cols);

                ConvertDMatrixStruct.convert(a,b);

                checkIdentical(a,b);
            }
        }
    }

    @Test
    public void DM_to_BM() {
        for( int rows = 1; rows <= 8; rows++ ) {
            for( int cols = 1; cols <= 8; cols++ ) {
                DMatrixRMaj a = RandomMatrices_DDRM.rectangle(rows,cols,rand);
                DMatrixRBlock b = new DMatrixRBlock(rows,cols,3);

                ConvertDMatrixStruct.convert(a,b);

                checkIdentical(a,b);
            }
        }
    }


    private void checkIdentical(DMatrix a , DMatrix b ) {
        for( int i = 0; i < a.getNumRows(); i++  ) {
            for( int j = 0; j < a.getNumCols(); j++ ) {
                assertEquals(a.get(i,j),b.get(i,j), UtilEjml.TEST_F64);
            }
        }
    }

    private void checkIdenticalV(DMatrix a , DMatrix b ) {
        boolean columnVectorA = a.getNumRows() > a.getNumCols();
        boolean columnVectorB = b.getNumRows() > b.getNumCols();

        int length = Math.max(a.getNumRows(),b.getNumRows());

        for( int i = 0; i < length; i++  ) {

            double valueA,valueB;

            if( columnVectorA )
                valueA = a.get(i,0);
            else
                valueA = a.get(0,i);

            if( columnVectorB )
                valueB = b.get(i,0);
            else
                valueB = b.get(0,i);

            assertEquals(valueA,valueB,UtilEjml.TEST_F64);
        }
    }

    @Test
    public void DMatrixRow_SMatrixTriplet() {
        DMatrixRMaj a = RandomMatrices_DDRM.rectangle(5,6,-1,1,rand);

        a.set(4,3, 0);
        a.set(1,3, 0);
        a.set(2,3, 0);
        a.set(2,0, 0);

        DMatrixRow_SMatrixTriplet(a,null);
        DMatrixRow_SMatrixTriplet(a, new DMatrixSparseTriplet(1,1,2));
    }

    public void DMatrixRow_SMatrixTriplet(DMatrixRMaj a , DMatrixSparseTriplet b ) {
        b = ConvertDMatrixStruct.convert(a,b, UtilEjml.EPS);

        assertEquals(a.numRows, b.numRows);
        assertEquals(a.numCols, b.numCols);
        assertEquals(5*6-4, b.nz_length);
        for (int row = 0; row < a.numRows; row++) {
            for (int col = 0; col < a.numCols; col++) {
                int index = b.nz_index(row,col);

                if( a.get(row,col) == 0.0 ) {
                    assertTrue( -1 == index );
                } else {
                    assertEquals( a.get(row,col), b.nz_value.data[index], UtilEjml.TEST_F64);
                }
            }
        }

        // now try it the other direction
        DMatrixRMaj c = ConvertDMatrixStruct.convert(b,(DMatrixRMaj)null);
        assertTrue(MatrixFeatures_DDRM.isEquals(a,c, UtilEjml.TEST_F64));

        c = ConvertDMatrixStruct.convert(b,new DMatrixRMaj(1,1));
        assertTrue(MatrixFeatures_DDRM.isEquals(a,c, UtilEjml.TEST_F64));
    }

    @Test
    public void DMatrix_SMatrixTriplet() {
        DMatrixRMaj a = RandomMatrices_DDRM.rectangle(5,6,-1,1,rand);

        a.set(4,3, 0);
        a.set(1,3, 0);
        a.set(2,3, 0);
        a.set(2,0, 0);

        DMatrix_SMatrixTriplet(a,null);
        DMatrix_SMatrixTriplet(a, new DMatrixSparseTriplet(1,1,2));
    }

    public void DMatrix_SMatrixTriplet(DMatrix a , DMatrixSparseTriplet b ) {
        b = ConvertDMatrixStruct.convert(a,b, UtilEjml.EPS);

        assertEquals(a.getNumRows(), b.numRows);
        assertEquals(a.getNumCols(), b.numCols);
        assertEquals(5*6-4, b.nz_length);
        for (int row = 0; row < a.getNumRows(); row++) {
            for (int col = 0; col < a.getNumCols(); col++) {
                int index = b.nz_index(row,col);

                if( a.get(row,col) == 0.0 ) {
                    assertTrue( -1 == index );
                } else {
                    assertEquals( a.get(row,col), b.nz_value.data[index], UtilEjml.TEST_F64);
                }
            }
        }

        // now try it the other direction
        DMatrixRMaj c = ConvertDMatrixStruct.convert(b,(DMatrixRMaj)null);
        EjmlUnitTests.assertEquals(a,c, UtilEjml.TEST_F64);

        c = ConvertDMatrixStruct.convert(b,new DMatrixRMaj(1,1));
        EjmlUnitTests.assertEquals(a,c, UtilEjml.TEST_F64);
    }

    @Test
    public void DMatrixRow_SparseCSC() {
        DMatrixRMaj a = RandomMatrices_DDRM.rectangle(5,6,-1,1,rand);

        a.set(4,3, 0);
        a.set(1,3, 0);
        a.set(2,3, 0);
        a.set(2,0, 0);

        DMatrixRow_SparseCSC(a,null);
        DMatrixRow_SparseCSC(a, new DMatrixSparseCSC(1,1,2));
    }

    public void DMatrixRow_SparseCSC(DMatrixRMaj a , DMatrixSparseCSC b ) {
        b = ConvertDMatrixStruct.convert(a,b, UtilEjml.EPS);

        assertEquals(a.numRows, b.numRows);
        assertEquals(a.numCols, b.numCols);
        assertEquals(5*6-4, b.nz_length);
        for (int row = 0; row < a.numRows; row++) {
            for (int col = 0; col < a.numCols; col++) {
                int index = b.nz_index(row,col);

                if( a.get(row,col) == 0.0 ) {
                    assertTrue( -1 == index );
                } else {
                    assertEquals( a.get(row,col), b.nz_values[index], UtilEjml.TEST_F64);
                }
            }
        }

        // now try it the other direction
        DMatrixRMaj c = ConvertDMatrixStruct.convert(b,(DMatrixRMaj)null);
        assertTrue(MatrixFeatures_DDRM.isEquals(a,c, UtilEjml.TEST_F64));

        c = ConvertDMatrixStruct.convert(b,new DMatrixRMaj(1,1));
        assertTrue(MatrixFeatures_DDRM.isEquals(a,c, UtilEjml.TEST_F64));
    }

    @Test
    public void SMatrixCC_DMatrixRow() {
        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(5,6,10,-1,1,rand);

        SMatrixCC_DMatrixRow(a,null);
        SMatrixCC_DMatrixRow(a,new DMatrixRMaj(1,1));
    }

    public void SMatrixCC_DMatrixRow(DMatrixSparseCSC a , DMatrixRMaj b ) {
        b = ConvertDMatrixStruct.convert(a,b);

        assertEquals(a.numRows, b.numRows);
        assertEquals(a.numCols, b.numCols);

        int found = MatrixFeatures_DDRM.countNonZero(b);
        assertEquals(a.nz_length, found);
        EjmlUnitTests.assertEquals(a, b);

        // now try it the other direction
        DMatrixSparseCSC c = ConvertDMatrixStruct.convert(b,(DMatrixSparseCSC)null, UtilEjml.EPS);
        assertTrue(MatrixFeatures_DSCC.isEqualsSort(a,c, UtilEjml.TEST_F64));
        assertTrue(CommonOps_DSCC.checkIndicesSorted(c));

        c = ConvertDMatrixStruct.convert(b,new DMatrixSparseCSC(1,1,1), UtilEjml.EPS);
        assertTrue(MatrixFeatures_DSCC.isEqualsSort(a,c, UtilEjml.TEST_F64));
        assertTrue(CommonOps_DSCC.checkIndicesSorted(c));
    }

    @Test
    public void SMatrixTriplet_SMatrixCC() {
        DMatrixSparseTriplet a = RandomMatrices_DSTL.uniform(5,6,10,-1,1,rand);

        SMatrixTriplet_SMatrixCC(a,(DMatrixSparseCSC)null);
        SMatrixTriplet_SMatrixCC(a,new DMatrixSparseCSC(1,1,2));
    }

    public void SMatrixTriplet_SMatrixCC(DMatrixSparseTriplet a , DMatrixSparseCSC b ) {
        b = ConvertDMatrixStruct.convert(a,b);

        assertEquals(a.numRows, b.numRows);
        assertEquals(a.numCols, b.numCols);
        assertEquals(a.nz_length, b.nz_length);
        for (int i = 0; i < a.nz_length; i++) {
            int row = a.nz_rowcol.data[i*2];
            int col = a.nz_rowcol.data[i*2+1];
            double value = a.nz_value.data[i];

            assertEquals(value, b.get(row, col), UtilEjml.TEST_F64);
        }
        assertTrue(CommonOps_DSCC.checkSortedFlag(b));

        // now try it the other direction
        DMatrixSparseTriplet c = ConvertDMatrixStruct.convert(b,(DMatrixSparseTriplet)null);
        assertTrue(MatrixFeatures_DSTL.isEquals(a,c, UtilEjml.TEST_F64));

        c = ConvertDMatrixStruct.convert(b,new DMatrixSparseTriplet(1,1,1));
        assertTrue(MatrixFeatures_DSTL.isEquals(a,c, UtilEjml.TEST_F64));
    }

}
