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

package org.ejml.ops;

import org.ejml.data.CD1Matrix64F;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;

/**
 * Common operations on complex numbers
 *
 * @author Peter Abeles
 */
public class CCommonOps {

    public static void convert( DenseMatrix64F input , CDenseMatrix64F output ) {

    }

    public static void stripReal( CDenseMatrix64F input , DenseMatrix64F output ) {

    }

    public static void stripImaginary( CDenseMatrix64F input , DenseMatrix64F output ) {

    }

    public static void magnitude( CDenseMatrix64F input , DenseMatrix64F output ) {

    }

    public static void add( CDenseMatrix64F a , CDenseMatrix64F b , CDenseMatrix64F c )
    {

    }

    public static void subtract( CDenseMatrix64F a , CDenseMatrix64F b , CDenseMatrix64F c )
    {

    }

    public static void multiply( CDenseMatrix64F a , CDenseMatrix64F b , CDenseMatrix64F c )
    {

    }

    public static void transpose( CDenseMatrix64F input , CDenseMatrix64F output )
    {

    }

    public static boolean invert( CDenseMatrix64F input , CDenseMatrix64F output )
    {
        return false;
    }

    public static boolean solve( CDenseMatrix64F a , CDenseMatrix64F b , CDenseMatrix64F x )
    {
        return false;
    }

    public static boolean det( CDenseMatrix64F input , Complex64F output )
    {
        return false;
    }

    public static void elementMultiply( CDenseMatrix64F a , double real , double imaginary, CDenseMatrix64F c )
    {

    }

    public static void elementDivide( CDenseMatrix64F a , double real , double imaginary, CDenseMatrix64F c )
    {

    }

    /**
     * <p>
     * Returns the value of the real element in the matrix that has the minimum value.<br>
     * <br>
     * Min{ a<sub>ij</sub> } for all i and j<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The the minimum value out of all the real values.
     */
    public static double elementMinReal( CD1Matrix64F a ) {
        final int size = a.getDataLength();

        double min = a.data[0];
        for( int i = 2; i < size; i += 2 ) {
            double val = a.data[i];
            if( val < min ) {
                min = val;
            }
        }

        return min;
    }

    /**
     * <p>
     * Returns the value of the imaginary element in the matrix that has the minimum value.<br>
     * <br>
     * Min{ a<sub>ij</sub> } for all i and j<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The the minimum value out of all the real values.
     */
    public static double elementMinImaginary( CD1Matrix64F a ) {
        final int size = a.getDataLength();

        double min = a.data[1];
        for( int i = 3; i < size; i += 2 ) {
            double val = a.data[i];
            if( val < min ) {
                min = val;
            }
        }

        return min;
    }

    /**
     * <p>
     * Returns the value of the real element in the matrix that has the minimum value.<br>
     * <br>
     * Min{ a<sub>ij</sub> } for all i and j<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The the minimum value out of all the real values.
     */
    public static double elementMaxReal( CD1Matrix64F a ) {
        final int size = a.getDataLength();

        double max = a.data[0];
        for( int i = 2; i < size; i += 2 ) {
            double val = a.data[i];
            if( val > max ) {
                max = val;
            }
        }

        return max;
    }

    /**
     * <p>
     * Returns the value of the imaginary element in the matrix that has the minimum value.<br>
     * <br>
     * Min{ a<sub>ij</sub> } for all i and j<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The the minimum value out of all the real values.
     */
    public static double elementMaxImaginary( CD1Matrix64F a ) {
        final int size = a.getDataLength();

        double max = a.data[1];
        for( int i = 3; i < size; i += 2 ) {
            double val = a.data[i];
            if( val > max ) {
                max = val;
            }
        }

        return max;
    }

}
