/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlParameters;
import org.ejml.ops.EjmlUnitTests;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestDenseMatrix32F {

    Random rand = new Random(23432);

    @Test
    public void testGeneric() {
        GenericTestsD1Matrix32F g;
        g = new GenericTestsD1Matrix32F() {
            protected D1Matrix32F createMatrix(int numRows, int numCols) {
                return new DenseMatrix32F(numRows,numCols);
            }
        };

        g.allTests();
    }

    /**
     * Tests the following constructor:
     *
     * DenseMatrix32F( float data[] , int numCols , int numRows )
     */
    @Test
    public void testConstructorSingleArray()
    {
        float d[] = new float[]{1,2,3,4,5,6};

        DenseMatrix32F mat = new DenseMatrix32F(3,2, true, d);

        assertTrue( mat.data != d );

        assertEquals(1,mat.get(0,0),1e-8);
        assertEquals(2,mat.get(0,1),1e-8);
        assertEquals(3,mat.get(1,0),1e-8);
        assertEquals(4,mat.get(1,1),1e-8);
        assertEquals(5,mat.get(2,0),1e-8);
        assertEquals(6,mat.get(2,1),1e-8);
    }

    /**
     * Tests the following constructor:
     *
     * DenseMatrix32F( float data[][] )
     */
    @Test
    public void testConstruactorfloatArray() {
        float d[][] = new float[][]{{1,2},{3,4},{5,6}};

        DenseMatrix32F mat = new DenseMatrix32F(d);

        assertEquals(1,mat.get(0,0),1e-8);
        assertEquals(2,mat.get(0,1),1e-8);
        assertEquals(3,mat.get(1,0),1e-8);
        assertEquals(4,mat.get(1,1),1e-8);
        assertEquals(5,mat.get(2,0),1e-8);
        assertEquals(6,mat.get(2,1),1e-8);
    }

    /**
     * Tests the following constructor:
     *
     * DenseMatrix32F( int numCols , int numRows )
     */
    @Test
    public void testConstructorShape() {
        DenseMatrix32F mat = new DenseMatrix32F(7,5);

        assertEquals(5,mat.getNumCols());
        assertEquals(7,mat.getNumRows());
        assertEquals(7*5,mat.data.length);
    }

    /**
     * Tests the following constructor:
     *
     * DenseMatrix32F( DenseMatrix32F orig )
     */
    @Test
    public void testConstructorCopy() {
        float d[][] = new float[][]{{1,2},{3,4},{5,6}};

        DenseMatrix32F mat = new DenseMatrix32F(d);
        DenseMatrix32F copy = new DenseMatrix32F(mat);

        assertTrue( mat.data != copy.data );

        assertEquals(mat.getNumCols(),copy.getNumCols());
        assertEquals(mat.getNumRows(),copy.getNumRows());

        for( int i = 0; i < mat.getNumElements(); i++ ) {
            assertEquals(mat.get(i),copy.get(i),EjmlParameters.TOL32);
        }
    }

    @Test
    public void wrap() {
        float d[] = new float[]{1,2,3,4,5,6};

        DenseMatrix32F mat = DenseMatrix32F.wrap(3,2,d);

        assertTrue(mat.data==d);

        assertEquals(1,mat.get(0,0),1e-8);
        assertEquals(2,mat.get(0,1),1e-8);
        assertEquals(3,mat.get(1,0),1e-8);
        assertEquals(4,mat.get(1,1),1e-8);
        assertEquals(5,mat.get(2,0),1e-8);
        assertEquals(6,mat.get(2,1),1e-8);
    }

    @Test
    public void testInBounds() {
        DenseMatrix32F mat = new DenseMatrix32F(2,3);

        assertTrue(mat.isInBounds(0,0));
        assertTrue(mat.isInBounds(1,2));
        assertTrue(mat.isInBounds(0,2));
        assertFalse(mat.isInBounds(2,0));
        assertFalse(mat.isInBounds(20,30));
    }

    @Test
    public void testSet_Matrix() {
        float d[][] = new float[][]{{1,2},{3,4},{5,6}};

        DenseMatrix32F mat = new DenseMatrix32F(d);
        DenseMatrix32F mat2 = new DenseMatrix32F(mat.numRows,mat.numCols);

        mat2.set(mat);

        EjmlUnitTests.assertEquals(mat,mat2,EjmlParameters.TOL32);
    }

    @Test
    @Ignore
    public void set_ColumnMajor() {
        // todo implement later after basic 32bit operations are done
        fail("implement");
//        DenseMatrix32F A = UtilTestMatrix.random32(3,5,-1,1,rand);
//
//        DenseMatrix32F Atran = A.copy();
//        CommonOps.transpose(Atran);
//        DenseMatrix32F Afound = new DenseMatrix32F(3,5);
//        Afound.set(3,5, false, Atran.data);
//
//        assertTrue(MatrixFeatures.isIdentical(Afound,A,EjmlParameters.TOL32));
    }

    @Test
    @Ignore
    public void set_RowMajor() {
        // todo implement later after basic 32bit operations are done
        fail("implement");
//        DenseMatrix32F A = UtilTestMatrix.random32(3, 5, -1, 1, rand);
//
//        DenseMatrix32F Afound = new DenseMatrix32F(3,5);
//        Afound.set(3,5, true, A.data);
//
//        assertTrue(MatrixFeatures.isIdentical(Afound,A,EjmlParameters.TOL32));
//        assertTrue(A.data != Afound.data);
    }

    @Test
    public void testset_Matrix() {
        float d[][] = new float[][]{{1,2},{3,4},{5,6}};

        DenseMatrix32F mat = new DenseMatrix32F(d);
        DenseMatrix32F mat2 = new DenseMatrix32F(5,5);

        mat2.set(mat);

        assertEquals(mat.getNumCols(),mat2.getNumCols());
        assertEquals(mat.getNumRows(),mat2.getNumRows());

        EjmlUnitTests.assertEquals(mat,mat2, EjmlParameters.TOL32);
    }

    @Test
    public void testReshape() {
        float d[][] = new float[][]{{1,2},{3,4},{5,6}};

        DenseMatrix32F mat = new DenseMatrix32F(d);
        DenseMatrix32F orig = mat.copy();

        // first see if reshape saves the data
        mat.reshape(10,10,true);

        for( int i = 0; i < 6; i++ ) {
            assertEquals(mat.data[i],orig.data[i],EjmlParameters.TOL32);
        }

        // now make sure it doesn't
        mat.reshape(11,10,false);

        for( int i = 0; i < 6; i++ ) {
            assertTrue(Math.abs(mat.data[i]-orig.data[i])>EjmlParameters.TOL32);
        }
    }

}
