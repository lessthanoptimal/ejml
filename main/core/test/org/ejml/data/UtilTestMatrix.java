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

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Contains functions useful for testing the results of matrices
 *
 * @author Peter Abeles
 */
public class UtilTestMatrix {

    public static void checkMat( DenseMatrix64F mat , double ...d )
    {
        double data[] = mat.getData();

        for( int i = 0; i < mat.getNumElements(); i++ ) {
            assertEquals(d[i],data[i],1e-6);
        }
    }

    public static void checkSameElements( double tol, int length , double a[], double b[] )
    {
        double aa[] = new double[ length ];
        double bb[] = new double[ length ];

        System.arraycopy(a,0,aa,0,length);
        System.arraycopy(b,0,bb,0,length);

        Arrays.sort(aa);
        Arrays.sort(bb);

        for( int i = 0; i < length; i++ ) {
            if( Math.abs(aa[i]-bb[i])> tol )
                fail("Mismatched elements");
        }
    }

    public static void checkNumFound( int expected , double tol , double value , double data[] )
    {
        int numFound = 0;

        for( int i = 0; i < data.length; i++ ) {
            if( Math.abs(data[i]-value) <= tol )
                numFound++;
        }

        assertEquals(expected,numFound);
    }

    /**
     * <p>
     * Sets each element in the matrix to a value drawn from an uniform distribution from 'min' to 'max' inclusive.
     * </p>
     *
     * @param min The minimum value each element can be.
     * @param max The maximum value each element can be.
     * @param rand Random number generator used to fill the matrix.
     */
    public static DenseMatrix64F random64( int numRows , int numCols , double min , double max , Random rand )
    {
        DenseMatrix64F mat = new DenseMatrix64F(numRows,numCols);
        double d[] = mat.getData();
        int size = mat.getNumElements();

        double r = max-min;

        for( int i = 0; i < size; i++ ) {
            d[i] = r*rand.nextDouble()+min;
        }

        return mat;
    }

    /**
     * <p>
     * Sets each element in the matrix to a value drawn from an uniform distribution from 'min' to 'max' inclusive.
     * </p>
     *
     * @param min The minimum value each element can be.
     * @param max The maximum value each element can be.
     * @param rand Random number generator used to fill the matrix.
     */
    public static DenseMatrix32F random32( int numRows , int numCols , float min , float max , Random rand )
    {
        DenseMatrix32F mat = new DenseMatrix32F(numRows,numCols);
        float d[] = mat.getData();
        int size = mat.getNumElements();

        float r = max-min;

        for( int i = 0; i < size; i++ ) {
            d[i] = r*rand.nextFloat()+min;
        }

        return mat;
    }

    
}
