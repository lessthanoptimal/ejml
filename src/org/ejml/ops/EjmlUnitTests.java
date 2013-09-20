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

package org.ejml.ops;

import org.ejml.data.ReshapeMatrix64F;


/**
 * Contains various functions related to unit testing matrix operations.
 *
 * @author Peter Abeles
 */
public class EjmlUnitTests {

    /**
     * Checks to see if every element in A is countable.  A doesn't have any element with
     * a value of NaN or infinite.
     *
     * @param A Matrix
     */
    public static void assertCountable(  ReshapeMatrix64F A ) {
        for( int i = 0; i < A.numRows; i++ ){
            for( int j = 0; j < A.numCols; j++ ) {
                assertTrue(  !Double.isNaN(A.get(i,j)) , "NaN found at "+i+" "+j );
                assertTrue(  !Double.isInfinite(A.get(i,j)) , "Infinite found at "+i+" "+j );
            }
        }
    }

    /**
     * <p>
     * Checks to see if A and B have the same shape.
     * </p>
     *
     * @param A Matrix
     * @param B Matrix
     */
    public static void assertShape( ReshapeMatrix64F A , ReshapeMatrix64F B ) {
        assertTrue(  A.numRows == B.numRows , "Number of rows do not match");
        assertTrue(  A.numCols == B.numCols , "Number of columns do not match");
    }

    /**
     * <p>
     * Checks to see if the matrix has the specified number of rows and columns.
     * </p>
     *
     * @param A Matrix
     * @param numRows expected number of rows in the matrix
     * @param numCols expected number of columns in the matrix
     */
    public static void assertShape( ReshapeMatrix64F A , int numRows , int numCols ) {
        assertTrue(  A.numRows == numRows , "Unexpected number of rows.");
        assertTrue(  A.numCols == numCols , "Unexpected number of columns.");
    }

    /**
     * <p>
     * Checks to see if each element in the matrix is within tolerance of each other:
     * </p>
     *
     * <p>
     * The two matrices are identical with in tolerance if:<br>
     * |a<sub>ij</sub> - b<sub>ij</sub>| &le; tol
     * </p>
     *
     * <p>
     * In addition if an element is NaN or infinite in one matrix it must be the same in the other.
     * </p>
     *
     * @param A Matrix A
     * @param B Matrix B
     * @param tol Tolerance
     */
    public static void assertEqualsUncountable( ReshapeMatrix64F A , ReshapeMatrix64F B , double tol ) {
        assertShape(A,B);

        for( int i = 0; i < A.numRows; i++ ){
            for( int j = 0; j < A.numCols; j++ ) {
                double valA = A.get(i,j);
                double valB = B.get(i,j);

                if( Double.isNaN(valA) ) {
                    assertTrue(Double.isNaN(valB),"At ("+i+","+j+") A = "+valA+" B = "+valB);
                } else if( Double.isInfinite(valA) ) {
                    assertTrue(Double.isInfinite(valB),"At ("+i+","+j+") A = "+valA+" B = "+valB);
                } else {
                    double diff = Math.abs( valA-valB);
                    assertTrue(diff <= tol,"At ("+i+","+j+") A = "+valA+" B = "+valB);
                }
            }
        }
    }

    /**
     * <p>
     * Checks to see if each element in the matrices are within tolerance of each other and countable:
     * </p>
     *
     * <p>
     * The two matrices are identical with in tolerance if:<br>
     * |a<sub>ij</sub> - b<sub>ij</sub>| &le; tol
     * </p>
     *
     * <p>
     * The test will fail if any element in either matrix is NaN or infinite.
     * </p>
     *
     * @param A Matrix A
     * @param B Matrix B
     * @param tol Tolerance
     */
    public static void assertEquals( ReshapeMatrix64F A , ReshapeMatrix64F B , double tol ) {
        assertShape(A,B);

        for( int i = 0; i < A.numRows; i++ ){
            for( int j = 0; j < A.numCols; j++ ) {
                double valA = A.get(i,j);
                double valB = B.get(i,j);

                assertTrue(!Double.isNaN(valA) && !Double.isNaN(valB) ,"At ("+i+","+j+") A = "+valA+" B = "+valB);
                assertTrue(!Double.isInfinite(valA) && !Double.isInfinite(valB) ,"At ("+i+","+j+") A = "+valA+" B = "+valB);
                assertTrue(Math.abs( valA-valB) <= tol,"At ("+i+","+j+") A = "+valA+" B = "+valB);
            }
        }
    }

    /**
     * <p>
     * Checks to see if the transpose of B is equal to A and countable:
     * </p>
     *
     * <p>
     * |a<sub>ij</sub> - b<sub>ji</sub>| &le; tol
     * </p>
     *
     * <p>
     * The test will fail if any element in either matrix is NaN or infinite.
     * </p>
     *
     * @param A Matrix A
     * @param B Matrix B
     * @param tol Tolerance
     */
    public static void assertEqualsTrans( ReshapeMatrix64F A , ReshapeMatrix64F B , double tol ) {
        assertShape(A,B.numCols,B.numRows);

        for( int i = 0; i < A.numRows; i++ ){
            for( int j = 0; j < A.numCols; j++ ) {
                double valA = A.get(i,j);
                double valB = B.get(j,i);

                assertTrue(!Double.isNaN(valA) && !Double.isNaN(valB) ,"A("+i+","+j+") = "+valA+") B("+j+","+i+") = "+valB);
                assertTrue(!Double.isInfinite(valA) && !Double.isInfinite(valB) ,"A("+i+","+j+") = "+valA+") B("+j+","+i+") = "+valB);
                assertTrue(Math.abs( valA-valB) <= tol,"A("+i+","+j+") = "+valA+") B("+j+","+i+") = "+valB);
            }
        }
    }

    @SuppressWarnings({"ConstantConditions"})
    private static void assertTrue( boolean result , String message ) {
        // if turned on use asserts
        assert result : message;
        // otherwise throw an exception
        if( !result ) throw new TestException(message);
    }

    public static class TestException extends RuntimeException {
        public TestException(String message) {
            super(message);
        }
    }
}
