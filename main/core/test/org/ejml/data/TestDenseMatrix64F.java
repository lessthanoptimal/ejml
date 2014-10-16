/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

import org.ejml.ops.CommonOps;
import org.ejml.ops.EjmlUnitTests;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestDenseMatrix64F {

    Random rand = new Random(23432);

    @Test
    public void testGeneric() {
        GenericTestsD1Matrix64F g;
        g = new GenericTestsD1Matrix64F() {
            protected D1Matrix64F createMatrix(int numRows, int numCols) {
                return new DenseMatrix64F(numRows,numCols);
            }
        };

        g.allTests();
    }

    /**
     * Tests the following constructor:
     *
     * DenseMatrix64F( double data[] , int numCols , int numRows )
     */
    @Test
    public void testConstructorSingleArray()
    {
        double d[] = new double[]{1,2,3,4,5,6};

        DenseMatrix64F mat = new DenseMatrix64F(3,2, true, d);

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
     * DenseMatrix64F( double data[][] )
     */
    @Test
    public void testConstruactorDoubleArray() {
        double d[][] = new double[][]{{1,2},{3,4},{5,6}};

        DenseMatrix64F mat = new DenseMatrix64F(d);

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
     * DenseMatrix64F( int numCols , int numRows )
     */
    @Test
    public void testConstructorShape() {
        DenseMatrix64F mat = new DenseMatrix64F(7,5);

        assertEquals(5,mat.getNumCols());
        assertEquals(7,mat.getNumRows());
        assertEquals(7*5,mat.data.length);
    }

    /**
     * Tests the following constructor:
     *
     * DenseMatrix64F( DenseMatrix64F orig )
     */
    @Test
    public void testConstructorCopy() {
        double d[][] = new double[][]{{1,2},{3,4},{5,6}};

        DenseMatrix64F mat = new DenseMatrix64F(d);
        DenseMatrix64F copy = new DenseMatrix64F(mat);

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

        DenseMatrix64F mat = DenseMatrix64F.wrap(3,2,d);

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
        DenseMatrix64F mat = new DenseMatrix64F(2,3);

        assertTrue(mat.isInBounds(0,0));
        assertTrue(mat.isInBounds(1,2));
        assertTrue(mat.isInBounds(0,2));
        assertFalse(mat.isInBounds(2,0));
        assertFalse(mat.isInBounds(20,30));
    }

    @Test
    public void testSet_Matrix() {
        double d[][] = new double[][]{{1,2},{3,4},{5,6}};

        DenseMatrix64F mat = new DenseMatrix64F(d);
        DenseMatrix64F mat2 = new DenseMatrix64F(mat.numRows,mat.numCols);

        mat2.set(mat);

        EjmlUnitTests.assertEquals(mat,mat2,1e-10);
    }

    @Test
    public void set_ColumnMajor() {
        DenseMatrix64F A = RandomMatrices.createRandom(3,5,rand);

        DenseMatrix64F Atran = A.copy();
        CommonOps.transpose(Atran);
        DenseMatrix64F Afound = new DenseMatrix64F(3,5);
        Afound.set(3,5, false, Atran.data);

        assertTrue(MatrixFeatures.isIdentical(Afound,A,1e-10));
    }

    @Test
    public void set_RowMajor() {
        DenseMatrix64F A = RandomMatrices.createRandom(3,5,rand);

        DenseMatrix64F Afound = new DenseMatrix64F(3,5);
        Afound.set(3,5, true, A.data);

        assertTrue(MatrixFeatures.isIdentical(Afound,A,1e-10));
        assertTrue(A.data != Afound.data);
    }

    @Test
    public void testset_Matrix() {
        double d[][] = new double[][]{{1,2},{3,4},{5,6}};

        DenseMatrix64F mat = new DenseMatrix64F(d);
        DenseMatrix64F mat2 = new DenseMatrix64F(5,5);

        mat2.set(mat);

        assertEquals(mat.getNumCols(),mat2.getNumCols());
        assertEquals(mat.getNumRows(),mat2.getNumRows());

        EjmlUnitTests.assertEquals(mat,mat2,1e-10);
    }

    @Test
    public void testReshape() {
        double d[][] = new double[][]{{1,2},{3,4},{5,6}};

        DenseMatrix64F mat = new DenseMatrix64F(d);
        DenseMatrix64F orig = mat.copy();

        // first see if reshape saves the data
        mat.reshape(10,10,true);

        for( int i = 0; i < 6; i++ ) {
            assertEquals(mat.data[i],orig.data[i],1e-8);
        }

        // now make sure it doesn't
        mat.reshape(11,10,false);

        for( int i = 0; i < 6; i++ ) {
            assertTrue(Math.abs(mat.data[i]-orig.data[i])>1e-8);
        }
    }

}
