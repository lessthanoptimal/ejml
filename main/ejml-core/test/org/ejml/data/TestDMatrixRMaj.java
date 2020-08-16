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

package org.ejml.data;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public class TestDMatrixRMaj {

    Random rand = new Random(23432);

    @Test
    public void testGeneric() {
        GenericTestsDMatrixD1 g;
        g = new GenericTestsDMatrixD1() {
            @Override
            protected DMatrixD1 createMatrix(int numRows, int numCols) {
                return new DMatrixRMaj(numRows,numCols);
            }
        };

        g.allTests();
    }

    /**
     * Tests the following constructor:
     *
     * DMatrixRMaj( double data[] , int numCols , int numRows )
     */
    @Test
    public void testConstructorSingleArray()
    {
        double d[] = new double[]{1,2,3,4,5,6};

        DMatrixRMaj mat = new DMatrixRMaj(3,2, true, d);

        assertTrue( mat.data != d );

        assertEquals(1,mat.get(0,0),UtilEjml.TEST_F64);
        assertEquals(2,mat.get(0,1), UtilEjml.TEST_F64);
        assertEquals(3,mat.get(1,0),UtilEjml.TEST_F64);
        assertEquals(4,mat.get(1,1),UtilEjml.TEST_F64);
        assertEquals(5,mat.get(2,0),UtilEjml.TEST_F64);
        assertEquals(6,mat.get(2,1),UtilEjml.TEST_F64);
    }

    /**
     * Tests the following constructor:
     *
     * DMatrixRMaj( double data[][] )
     */
    @Test
    public void testConstruactorDoubleArray() {
        double d[][] = new double[][]{{1,2},{3,4},{5,6}};

        DMatrixRMaj mat = new DMatrixRMaj(d);

        assertEquals(1,mat.get(0,0),UtilEjml.TEST_F64);
        assertEquals(2,mat.get(0,1),UtilEjml.TEST_F64);
        assertEquals(3,mat.get(1,0),UtilEjml.TEST_F64);
        assertEquals(4,mat.get(1,1),UtilEjml.TEST_F64);
        assertEquals(5,mat.get(2,0),UtilEjml.TEST_F64);
        assertEquals(6,mat.get(2,1),UtilEjml.TEST_F64);
    }

    /**
     * Tests the following constructor:
     *
     * DMatrixRMaj( int numCols , int numRows )
     */
    @Test
    public void testConstructorShape() {
        DMatrixRMaj mat = new DMatrixRMaj(7,5);

        assertEquals(5,mat.getNumCols());
        assertEquals(7,mat.getNumRows());
        assertEquals(7*5,mat.data.length);
    }

    /**
     * Tests the following constructor:
     *
     * DMatrixRMaj( DMatrixRMaj orig )
     */
    @Test
    public void testConstructorCopy() {
        double d[][] = new double[][]{{1,2},{3,4},{5,6}};

        DMatrixRMaj mat = new DMatrixRMaj(d);
        DMatrixRMaj copy = new DMatrixRMaj(mat);

        assertTrue( mat.data != copy.data );

        assertEquals(mat.getNumCols(),copy.getNumCols());
        assertEquals(mat.getNumRows(),copy.getNumRows());

        for( int i = 0; i < mat.getNumElements(); i++ ) {
            assertEquals(mat.get(i),copy.get(i),1e-10);
        }
    }

    @Test
    public void wrap() {
        double d[] = new double[]{1,2,3,4,5,6};

        DMatrixRMaj mat = DMatrixRMaj.wrap(3,2,d);

        assertTrue(mat.data==d);

        assertEquals(1,mat.get(0,0),UtilEjml.TEST_F64);
        assertEquals(2,mat.get(0,1),UtilEjml.TEST_F64);
        assertEquals(3,mat.get(1,0),UtilEjml.TEST_F64);
        assertEquals(4,mat.get(1,1),UtilEjml.TEST_F64);
        assertEquals(5,mat.get(2,0),UtilEjml.TEST_F64);
        assertEquals(6,mat.get(2,1),UtilEjml.TEST_F64);
    }

    @Test
    public void testInBounds() {
        DMatrixRMaj mat = new DMatrixRMaj(2,3);

        assertTrue(mat.isInBounds(0,0));
        assertTrue(mat.isInBounds(1,2));
        assertTrue(mat.isInBounds(0,2));
        assertFalse(mat.isInBounds(2,0));
        assertFalse(mat.isInBounds(20,30));
    }

    @Test
    public void testSet_Matrix() {
        double d[][] = new double[][]{{1,2},{3,4},{5,6}};

        DMatrixRMaj mat = new DMatrixRMaj(d);
        DMatrixRMaj mat2 = new DMatrixRMaj(mat.numRows,mat.numCols);

        mat2.set(mat);

        EjmlUnitTests.assertEquals(mat,mat2,UtilEjml.TEST_F64);
    }

    @Test
    public void set_ColumnMajor() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(3,5,rand);

        DMatrixRMaj Atran = A.copy();
        CommonOps_DDRM.transpose(Atran);
        DMatrixRMaj Afound = new DMatrixRMaj(3,5);
        Afound.set(3,5, false, Atran.data);

        assertTrue(MatrixFeatures_DDRM.isIdentical(Afound,A,UtilEjml.TEST_F64));
    }

    @Test
    public void set_RowMajor() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(3,5,rand);

        DMatrixRMaj Afound = new DMatrixRMaj(3,5);
        Afound.set(3,5, true, A.data);

        assertTrue(MatrixFeatures_DDRM.isIdentical(Afound,A,UtilEjml.TEST_F64));
        assertTrue(A.data != Afound.data);
    }

    @Test
    public void testset_Matrix() {
        double d[][] = new double[][]{{1,2},{3,4},{5,6}};

        DMatrixRMaj mat = new DMatrixRMaj(d);
        DMatrixRMaj mat2 = new DMatrixRMaj(5,5);

        mat2.set(mat);

        assertEquals(mat.getNumCols(),mat2.getNumCols());
        assertEquals(mat.getNumRows(),mat2.getNumRows());

        EjmlUnitTests.assertEquals(mat,mat2,UtilEjml.TEST_F64);
    }

    @Test
    public void testReshape() {
        double d[][] = new double[][]{{1,2},{3,4},{5,6}};

        DMatrixRMaj mat = new DMatrixRMaj(d);
        DMatrixRMaj orig = mat.copy();

        // first see if reshape saves the data
        mat.reshape(10,10,true);

        for( int i = 0; i < 6; i++ ) {
            assertEquals(mat.data[i],orig.data[i],UtilEjml.TEST_F64);
        }

        // now make sure it doesn't
        mat.reshape(11,10,false);

        for( int i = 0; i < 6; i++ ) {
            assertTrue(Math.abs(mat.data[i]-orig.data[i])>UtilEjml.TEST_F64);
        }
    }

}
