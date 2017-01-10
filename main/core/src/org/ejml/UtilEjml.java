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

package org.ejml;

import org.ejml.data.DMatrixRow_F32;
import org.ejml.data.DMatrixRow_F64;

import java.util.Arrays;
import java.util.Comparator;


/**
 * Various functions that are useful but don't have a clear location that they belong in.
 *
 * @author Peter Abeles
 */
public class UtilEjml {

    /**
     * Version string used to indicate which version of EJML is being used.
     */
    public static String VERSION = "0.31-SNAPSHOT";

    public static double EPS = Math.pow(2,-52);
    public static float F_EPS = (float)Math.pow(2,-21);

    public static double PI = Math.PI;
    public static double PI2 = 2.0*Math.PI;
    public static double PId2 = Math.PI/2.0;

    public static float F_PI = (float)Math.PI;
    public static float F_PI2 = (float)(2.0*Math.PI);
    public static float F_PId2 = (float)(Math.PI/2.0);

    // tolerances for unit tests
    public static float TEST_F32 = 1e-4f;
    public static double TEST_F64 = 1e-8;
    public static float TESTP_F32 = 1e-6f;
    public static double TESTP_F64 = 1e-12;
    public static float TEST_F32_SQ = (float)Math.sqrt(TEST_F32);
    public static double TEST_F64_SQ = Math.sqrt(TEST_F64);

    // The maximize size it will do inverse on
    public static int maxInverseSize = 5;


    public static boolean isUncountable( double val ) {
        return Double.isNaN(val) || Double.isInfinite(val);
    }

    public static boolean isUncountable( float val ) {
        return Float.isNaN(val) || Float.isInfinite(val);
    }

    public static void memset( double[] data , double val , int length ) {
        for( int i = 0; i < length; i++ ) {
            data[i] = val;
        }
    }

    public static void memset( int[] data , int val , int length ) {
        for( int i = 0; i < length; i++ ) {
            data[i] = val;
        }
    }

    public static <T> void setnull( T[] array )  {
        for( int i = 0; i < array.length; i++ ) {
            array[i] = null;
        }
    }

    public static double max( double array[], int start , int length ) {
        double max = array[start];
        final int end = start+length;

        for( int i = start+1; i < end; i++ ) {
            double v = array[i];
            if( v > max ) {
                max = v;
            }
        }

        return max;
    }

    public static float max( float array[], int start , int length ) {
        float max = array[start];
        final int end = start+length;

        for( int i = start+1; i < end; i++ ) {
            float v = array[i];
            if( v > max ) {
                max = v;
            }
        }

        return max;
    }

    /**
     * Give a string of numbers it returns a DenseMatrix
     */
    public static DMatrixRow_F64 parse_R64(String s , int numColumns )
    {
        String []vals = s.split("(\\s)+");

        // there is the possibility the first element could be empty
        int start = vals[0].isEmpty() ? 1 : 0;

        // covert it from string to doubles
        int numRows = (vals.length-start) / numColumns;

        DMatrixRow_F64 ret = new DMatrixRow_F64(numRows,numColumns);

        int index = start;
        for( int i = 0; i < numRows; i++ ) {
            for( int j = 0; j < numColumns; j++ ) {
                ret.set(i,j, Double.parseDouble(vals[ index++ ]));
            }
        }

        return ret;
    }

    /**
     * Give a string of numbers it returns a DenseMatrix
     */
    public static DMatrixRow_F32 parse_R32(String s , int numColumns )
    {
        String []vals = s.split("(\\s)+");

        // there is the possibility the first element could be empty
        int start = vals[0].isEmpty() ? 1 : 0;

        // covert it from string to doubles
        int numRows = (vals.length-start) / numColumns;

        DMatrixRow_F32 ret = new DMatrixRow_F32(numRows,numColumns);

        int index = start;
        for( int i = 0; i < numRows; i++ ) {
            for( int j = 0; j < numColumns; j++ ) {
                ret.set(i,j, Float.parseFloat(vals[ index++ ]));
            }
        }

        return ret;
    }

    public static Integer[] sortByIndex( final double []data , int size ) {
        Integer[] idx = new Integer[size];
        for( int i = 0; i < size; i++ ) {
            idx[i] = i;
        }

        Arrays.sort(idx, new Comparator<Integer>() {
            @Override public int compare(final Integer o1, final Integer o2) {
                return Double.compare(data[o1], data[o2]);
            }
        });

        return idx;
    }
}
