/*
 * Copyright (c) 2009-2013, Peter Abeles. All Rights Reserved.
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

    
}
