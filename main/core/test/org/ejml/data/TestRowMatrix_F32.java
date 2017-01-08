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

package org.ejml.data;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.ops.CommonOps_R32;
import org.ejml.ops.MatrixFeatures_R32;
import org.ejml.ops.RandomMatrices_R32;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestRowMatrix_F32 {

    Random rand = new Random(23432);

    @Test
    public void testGeneric() {
        GenericTestsD1Matrix_F32 g;
        g = new GenericTestsD1Matrix_F32() {
            protected D1Matrix_F32 createMatrix(int numRows, int numCols) {
                return new RowMatrix_F32(numRows,numCols);
            }
        };

        g.allTests();
    }

    /**
     * Tests the following constructor:
     *
     * RowMatrix_F32( float data[] , int numCols , int numRows )
     */
    @Test
    public void testConstructorSingleArray()
    {
        float d[] = new float[]{1,2,3,4,5,6};

        RowMatrix_F32 mat = new RowMatrix_F32(3,2, true, d);

        assertTrue( mat.data != d );

        assertEquals(1,mat.get(0,0),UtilEjml.TEST_F32);
        assertEquals(2,mat.get(0,1), UtilEjml.TEST_F32);
        assertEquals(3,mat.get(1,0),UtilEjml.TEST_F32);
        assertEquals(4,mat.get(1,1),UtilEjml.TEST_F32);
        assertEquals(5,mat.get(2,0),UtilEjml.TEST_F32);
        assertEquals(6,mat.get(2,1),UtilEjml.TEST_F32);
    }

    /**
     * Tests the following constructor:
     *
     * RowMatrix_F32( float data[][] )
     */
    @Test
    public void testConstruactorFloatArray() {
        float d[][] = new float[][]{{1,2},{3,4},{5,6}};

        RowMatrix_F32 mat = new RowMatrix_F32(d);

        assertEquals(1,mat.get(0,0),UtilEjml.TEST_F32);
        assertEquals(2,mat.get(0,1),UtilEjml.TEST_F32);
        assertEquals(3,mat.get(1,0),UtilEjml.TEST_F32);
        assertEquals(4,mat.get(1,1),UtilEjml.TEST_F32);
        assertEquals(5,mat.get(2,0),UtilEjml.TEST_F32);
        assertEquals(6,mat.get(2,1),UtilEjml.TEST_F32);
    }

    /**
     * Tests the following constructor:
     *
     * RowMatrix_F32( int numCols , int numRows )
     */
    @Test
    public void testConstructorShape() {
        RowMatrix_F32 mat = new RowMatrix_F32(7,5);

        assertEquals(5,mat.getNumCols());
        assertEquals(7,mat.getNumRows());
        assertEquals(7*5,mat.data.length);
    }

    /**
     * Tests the following constructor:
     *
     * RowMatrix_F32( RowMatrix_F32 orig )
     */
    @Test
    public void testConstructorCopy() {
        float d[][] = new float[][]{{1,2},{3,4},{5,6}};

        RowMatrix_F32 mat = new RowMatrix_F32(d);
        RowMatrix_F32 copy = new RowMatrix_F32(mat);

        assertTrue( mat.data != copy.data );

        assertEquals(mat.getNumCols(),copy.getNumCols());
        assertEquals(mat.getNumRows(),copy.getNumRows());

        for( int i = 0; i < mat.getNumElements(); i++ ) {
            assertEquals(mat.get(i),copy.get(i),1e-10);
        }
    }

    @Test
    public void wrap() {
        float d[] = new float[]{1,2,3,4,5,6};

        RowMatrix_F32 mat = RowMatrix_F32.wrap(3,2,d);

        assertTrue(mat.data==d);

        assertEquals(1,mat.get(0,0),UtilEjml.TEST_F32);
        assertEquals(2,mat.get(0,1),UtilEjml.TEST_F32);
        assertEquals(3,mat.get(1,0),UtilEjml.TEST_F32);
        assertEquals(4,mat.get(1,1),UtilEjml.TEST_F32);
        assertEquals(5,mat.get(2,0),UtilEjml.TEST_F32);
        assertEquals(6,mat.get(2,1),UtilEjml.TEST_F32);
    }

    @Test
    public void testInBounds() {
        RowMatrix_F32 mat = new RowMatrix_F32(2,3);

        assertTrue(mat.isInBounds(0,0));
        assertTrue(mat.isInBounds(1,2));
        assertTrue(mat.isInBounds(0,2));
        assertFalse(mat.isInBounds(2,0));
        assertFalse(mat.isInBounds(20,30));
    }

    @Test
    public void testSet_Matrix() {
        float d[][] = new float[][]{{1,2},{3,4},{5,6}};

        RowMatrix_F32 mat = new RowMatrix_F32(d);
        RowMatrix_F32 mat2 = new RowMatrix_F32(mat.numRows,mat.numCols);

        mat2.set(mat);

        EjmlUnitTests.assertEquals(mat,mat2,UtilEjml.TEST_F32);
    }

    @Test
    public void set_ColumnMajor() {
        RowMatrix_F32 A = RandomMatrices_R32.createRandom(3,5,rand);

        RowMatrix_F32 Atran = A.copy();
        CommonOps_R32.transpose(Atran);
        RowMatrix_F32 Afound = new RowMatrix_F32(3,5);
        Afound.set(3,5, false, Atran.data);

        assertTrue(MatrixFeatures_R32.isIdentical(Afound,A,UtilEjml.TEST_F32));
    }

    @Test
    public void set_RowMajor() {
        RowMatrix_F32 A = RandomMatrices_R32.createRandom(3,5,rand);

        RowMatrix_F32 Afound = new RowMatrix_F32(3,5);
        Afound.set(3,5, true, A.data);

        assertTrue(MatrixFeatures_R32.isIdentical(Afound,A,UtilEjml.TEST_F32));
        assertTrue(A.data != Afound.data);
    }

    @Test
    public void testset_Matrix() {
        float d[][] = new float[][]{{1,2},{3,4},{5,6}};

        RowMatrix_F32 mat = new RowMatrix_F32(d);
        RowMatrix_F32 mat2 = new RowMatrix_F32(5,5);

        mat2.set(mat);

        assertEquals(mat.getNumCols(),mat2.getNumCols());
        assertEquals(mat.getNumRows(),mat2.getNumRows());

        EjmlUnitTests.assertEquals(mat,mat2,UtilEjml.TEST_F32);
    }

    @Test
    public void testReshape() {
        float d[][] = new float[][]{{1,2},{3,4},{5,6}};

        RowMatrix_F32 mat = new RowMatrix_F32(d);
        RowMatrix_F32 orig = mat.copy();

        // first see if reshape saves the data
        mat.reshape(10,10,true);

        for( int i = 0; i < 6; i++ ) {
            assertEquals(mat.data[i],orig.data[i],UtilEjml.TEST_F32);
        }

        // now make sure it doesn't
        mat.reshape(11,10,false);

        for( int i = 0; i < 6; i++ ) {
            assertTrue(Math.abs(mat.data[i]-orig.data[i])>UtilEjml.TEST_F32);
        }
    }

}
