/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.generic;

import org.ejml.data.Matrix64F;

import java.util.Random;

/**
 * @author Peter Abeles
 */
public class GenericMatrixOps {

//    public static DenseD2Matrix64F convertToD2( DenseMatrix64F orig ) {
//        DenseD2Matrix64F ret = new DenseD2Matrix64F(orig.numRows,orig.numCols);
//
//        copy(orig,ret);
//
//        return ret;
//    }

    public static boolean isEquivalent( Matrix64F a , Matrix64F b , double tol )
    {
        if( a.numRows != b.numRows || a.numCols != b.numCols )
            return false;

        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < a.numCols; j++ ) {
                double diff = Math.abs(a.get(i,j) - b.get(i,j));

                if( diff > tol )
                    return false;
            }
        }

        return true;
    }

    /**
     * Returns true if the provided matrix is has a value of 1 along the diagonal
     * elements and zero along all the other elements.
     *
     * @param a Matrix being inspected.
     * @param tol How close to zero or one each element needs to be.
     * @return If it is within tolerance to an identity matrix.
     */
    public static boolean isIdentity( Matrix64F a , double tol )
    {
        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < a.numCols; j++ ) {
                if( i == j ) {
                    if( Math.abs(a.get(i,j)-1.0) > tol )
                        return false;
                } else {
                    if( Math.abs(a.get(i,j)) > tol )
                        return false;
                }
            }
        }
        return true;
    }

    public static boolean isEquivalentTriangle( boolean upper , Matrix64F a , Matrix64F b , double tol )
    {
        if( a.numRows != b.numRows || a.numCols != b.numCols )
            return false;

        if( upper ) {
            for( int i = 0; i < a.numRows; i++ ) {
                for( int j = i; j < a.numCols; j++ ) {
                    double diff = Math.abs(a.get(i,j) - b.get(i,j));

                    if( diff > tol )
                        return false;
                }
            }
        } else {
            for( int j = 0; j < a.numCols; j++ ) {
                for( int i = j; i < a.numRows; i++ ) {
                    double diff = Math.abs(a.get(i,j) - b.get(i,j));

                    if( diff > tol )
                        return false;
                }
            }
        }

        return true;
    }

    public static void copy( Matrix64F from , Matrix64F to )
    {
        int numCols = from.getNumCols();
        int numRows = from.getNumRows();

        for( int i = 0; i < numRows; i++ ) {
            for( int j = 0; j < numCols; j++ ) {
                to.set(i,j,from.get(i,j));
            }
        }
    }

    public static void setRandom( Matrix64F a , double min , double max , Random rand )
    {
        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < a.numCols; j++ ) {
                double val = rand.nextDouble()*(max-min)+min;
                a.set(i,j,val);
            }
        }
    }
}
