/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml;

import org.ejml.data.DenseMatrix64F;

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
    public static String VERSION = "0.13";

    /**
     * Default tolerance.
     */
    public static double TOLERANCE = 1e-8;

    public static double EPS = Math.pow(2,-52);

    public static boolean isUncountable( double val ) {
        return Double.isNaN(val) || Double.isInfinite(val);
    }

    public static void memset( double[] data , double val ) {
        for( int i = 0; i < data.length; i++ ) {
            data[i] = val;
        }
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

    public static void print( DenseMatrix64F mat ) {
        print(mat,6,3);
    }

    public static void print(DenseMatrix64F mat , int numChar , int precision ) {
        System.out.println("DenseMatrix64F  numRows = "+mat.numRows+" numCols = "+mat.numCols);

        String format = "%"+numChar+"."+precision+"f ";

        for( int y = 0; y < mat.numRows; y++ ) {
            for( int x = 0; x < mat.numCols; x++ ) {
                System.out.printf(format,mat.get(y,x));
            }
            System.out.println();
        }
    }

    public static void print(DenseMatrix64F mat , String format ) {
        System.out.println("DenseMatrix64F  numRows = "+mat.numRows+" numCols = "+mat.numCols);

        format += " ";

        for( int y = 0; y < mat.numRows; y++ ) {
            for( int x = 0; x < mat.numCols; x++ ) {
                System.out.printf(format,mat.get(y,x));
            }
            System.out.println();
        }
    }

    /**
     * Give a string of numbers it returns a DenseMatrix
     */
    public static DenseMatrix64F parseMatrix( String s , int numColumns )
    {
        String []vals = s.split("(\\s)+");

        // there is the posibility the first element could be empty
        int start = vals[0].isEmpty() ? 1 : 0;

        // covert it from string to doubles
        int numRows = (vals.length-start) / numColumns;

        DenseMatrix64F ret = new DenseMatrix64F(numRows,numColumns);

        int index = start;
        for( int i = 0; i < numRows; i++ ) {
            for( int j = 0; j < numColumns; j++ ) {
                ret.set(i,j, Double.parseDouble(vals[ index++ ]));
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
