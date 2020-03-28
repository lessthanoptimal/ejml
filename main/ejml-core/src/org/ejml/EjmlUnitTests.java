/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.*;


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
    public static void assertCountable( DMatrix A ) {
        for( int i = 0; i < A.getNumRows(); i++ ){
            for( int j = 0; j < A.getNumCols(); j++ ) {
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
    public static void assertShape( Matrix A , Matrix B ) {
        assertTrue(  A.getNumRows() == B.getNumRows() , "Number of rows do not match");
        assertTrue(  A.getNumCols() == B.getNumCols() , "Number of columns do not match");
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
    public static void assertShape( Matrix A , int numRows , int numCols ) {
        assertTrue(  A.getNumRows() == numRows , "Unexpected number of rows.");
        assertTrue(  A.getNumCols() == numCols , "Unexpected number of columns.");
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
    public static void assertEqualsUncountable(DMatrix A , DMatrix B , double tol ) {
        assertShape(A, B);

        for (int i = 0; i < A.getNumRows(); i++) {
            for (int j = 0; j < A.getNumCols(); j++) {
                double valA = A.get(i, j);
                double valB = B.get(i, j);

                if (Double.isNaN(valA)) {
                    assertTrue(Double.isNaN(valB), "At (" + i + "," + j + ") A = " + valA + " B = " + valB);
                } else if (Double.isInfinite(valA)) {
                    assertTrue(Double.isInfinite(valB), "At (" + i + "," + j + ") A = " + valA + " B = " + valB);
                } else {
                    double diff = Math.abs(valA - valB);
                    assertTrue(diff <= tol, "At (" + i + "," + j + ") A = " + valA + " B = " + valB);
                }
            }
        }
    }

    public static void assertEquals( Matrix A , Matrix B ) {
        if( A instanceof DMatrix) {
            assertEquals((DMatrix)A, (DMatrix)B, UtilEjml.TEST_F64);
        } else {
            assertEquals((FMatrix)A, (FMatrix)B, UtilEjml.TEST_F32);
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
    public static void assertEquals(DMatrix A , DMatrix B , double tol ) {
        assertShape(A,B);

        for( int i = 0; i < A.getNumRows(); i++ ){
            for( int j = 0; j < A.getNumCols(); j++ ) {
                double valA = A.get(i,j);
                double valB = B.get(i,j);

                assertTrue(!Double.isNaN(valA) && !Double.isNaN(valB) ,"At ("+i+","+j+") A = "+valA+" B = "+valB);
                assertTrue(!Double.isInfinite(valA) && !Double.isInfinite(valB) ,"At ("+i+","+j+") A = "+valA+" B = "+valB);

                double error = Math.abs( valA-valB);
                assertTrue(error <= tol,"At ("+i+","+j+") A = "+valA+" B = "+valB+" error = "+error+" tol = "+tol);
            }
        }
    }

    /**
     * Assert equals with a relative error
     */
    public static void assertRelativeEquals(DMatrix expected , DMatrix found , double tol ) {
        assertShape(expected,found);

        for( int i = 0; i < expected.getNumRows(); i++ ){
            for( int j = 0; j < expected.getNumCols(); j++ ) {
                double expecEl = expected.get(i,j);
                double foundEl = found.get(i,j);

                if( (Double.isNaN(expecEl) != Double.isNaN(foundEl)) ||
                        (Double.isInfinite(expecEl) != Double.isInfinite(foundEl))) {
                    throw new AssertionError("At ("+i+","+j+") A = "+expecEl+" B = "+foundEl);
                }
                double error = Math.abs(expecEl - foundEl);
                // if it expected a perfect zero there is no scale information so use an implicit scale of 1
                if( expecEl != 0.0 ) {
                    double max = Math.max(Math.abs(expecEl), Math.abs(foundEl));
                    error /= max;
                }
                if( error > tol ) {
                    if( expected.getNumRows() <= 10 ) {
                        System.out.println("------------  A  -----------");
                        expected.print();
                        System.out.println("\n------------  B  -----------");
                        found.print();
                    }
                    throw new AssertionError("At (" + i + "," + j + ") expected = " + expecEl + " found = " + foundEl + " error = " + error+" tol = "+tol);
                }
            }
        }
    }


    /**
     * Assert equals with a relative error
     */
    public static void assertRelativeEquals(FMatrix expected , FMatrix found , double tol ) {
        assertShape(expected,found);

        for( int i = 0; i < expected.getNumRows(); i++ ){
            for( int j = 0; j < expected.getNumCols(); j++ ) {
                float expecEl = expected.get(i,j);
                float foundEl = found.get(i,j);

                if( (Float.isNaN(expecEl) != Float.isNaN(foundEl)) ||
                        (Double.isInfinite(expecEl) != Double.isInfinite(foundEl))) {
                    throw new AssertionError("At ("+i+","+j+") A = "+expecEl+" B = "+foundEl);
                }
                float error = Math.abs(expecEl - foundEl);
                // if it expected a perfect zero there is no scale information so use an implicit scale of 1
                if( expecEl != 0.0 ) {
                    float max = Math.max(Math.abs(expecEl), Math.abs(foundEl));
                    error /= max;
                }
                if( error > tol ) {
                    if( expected.getNumRows() <= 10 ) {
                        System.out.println("------------  A  -----------");
                        expected.print();
                        System.out.println("\n------------  B  -----------");
                        found.print();
                    }
                    throw new AssertionError("At (" + i + "," + j + ") expected = " + expecEl + " found = " + foundEl + " error = " + error+" tol = "+tol);
                }
            }
        }
    }
    public static void assertEquals(FMatrix A , FMatrix B , float tol ) {
        assertShape(A,B);

        for( int i = 0; i < A.getNumRows(); i++ ){
            for( int j = 0; j < A.getNumCols(); j++ ) {
                float valA = A.get(i,j);
                float valB = B.get(i,j);

                assertTrue(!Float.isNaN(valA) && !Float.isNaN(valB) ,"At ("+i+","+j+") A = "+valA+" B = "+valB);
                assertTrue(!Float.isInfinite(valA) && !Float.isInfinite(valB) ,"At ("+i+","+j+") A = "+valA+" B = "+valB);

                float error = Math.abs( valA-valB);
                assertTrue(error <= tol,"At ("+i+","+j+") A = "+valA+" B = "+valB+" error = "+error+" tol = "+tol);
            }
        }
    }

    public static void assertEquals(Complex_F64 a , Complex_F64 b , double tol ) {
        assertTrue(!Double.isNaN(a.real) && !Double.isNaN(b.real) ,"real a = "+a.real+" b = "+b.real);
        assertTrue(!Double.isInfinite(a.real) && !Double.isInfinite(b.real) ,"real a = "+a.real+" b = "+b.real);
        assertTrue(Math.abs( a.real-b.real) <= tol,"real a = "+a.real+" b = "+b.real);

        assertTrue(!Double.isNaN(a.imaginary) && !Double.isNaN(b.imaginary) ,"imaginary a = "+a.imaginary+" b = "+b.imaginary);
        assertTrue(!Double.isInfinite(a.imaginary) && !Double.isInfinite(b.imaginary) ,"imaginary a = "+a.imaginary+" b = "+b.imaginary);
        assertTrue(Math.abs( a.imaginary-b.imaginary) <= tol,"imaginary a = "+a.imaginary+" b = "+b.imaginary);
    }

    public static void assertEquals(Complex_F32 a , Complex_F32 b , float tol ) {
        assertTrue(!Float.isNaN(a.real) && !Float.isNaN(b.real) ,"real a = "+a.real+" b = "+b.real);
        assertTrue(!Float.isInfinite(a.real) && !Float.isInfinite(b.real) ,"real a = "+a.real+" b = "+b.real);
        assertTrue(Math.abs( a.real-b.real) <= tol,"real a = "+a.real+" b = "+b.real);

        assertTrue(!Float.isNaN(a.imaginary) && !Float.isNaN(b.imaginary) ,"imaginary a = "+a.imaginary+" b = "+b.imaginary);
        assertTrue(!Float.isInfinite(a.imaginary) && !Float.isInfinite(b.imaginary) ,"imaginary a = "+a.imaginary+" b = "+b.imaginary);
        assertTrue(Math.abs( a.imaginary-b.imaginary) <= tol,"imaginary a = "+a.imaginary+" b = "+b.imaginary);
    }

    public static void assertEquals(ZMatrix A , ZMatrix B , double tol ) {
        assertShape(A,B);

        Complex_F64 a = new Complex_F64();
        Complex_F64 b = new Complex_F64();

        for( int i = 0; i < A.getNumRows(); i++ ){
            for( int j = 0; j < A.getNumCols(); j++ ) {
                A.get(i, j, a);
                B.get(i, j, b);

                assertTrue(!Double.isNaN(a.real) && !Double.isNaN(b.real) ,"Real At ("+i+","+j+") A = "+a.real+" B = "+b.real);
                assertTrue(!Double.isInfinite(a.real) && !Double.isInfinite(b.real) ,"Real At ("+i+","+j+") A = "+a.real+" B = "+b.real);
                assertTrue(Math.abs( a.real-b.real) <= tol,"Real At ("+i+","+j+") A = "+a.real+" B = "+b.real);

                assertTrue(!Double.isNaN(a.imaginary) && !Double.isNaN(b.imaginary) ,"Img At ("+i+","+j+") A = "+a.imaginary+" B = "+b.imaginary);
                assertTrue(!Double.isInfinite(a.imaginary) && !Double.isInfinite(b.imaginary) ,"Img At ("+i+","+j+") A = "+a.imaginary+" B = "+b.imaginary);
                assertTrue(Math.abs( a.imaginary-b.imaginary) <= tol,"Img At ("+i+","+j+") A = "+a.imaginary+" B = "+b.imaginary);

            }
        }
    }

    public static void assertEquals(CMatrix A , CMatrix B , float tol ) {
        assertShape(A, B);

        Complex_F32 a = new Complex_F32();
        Complex_F32 b = new Complex_F32();

        for (int i = 0; i < A.getNumRows(); i++) {
            for (int j = 0; j < A.getNumCols(); j++) {
                A.get(i, j, a);
                B.get(i, j, b);

                assertTrue(!Float.isNaN(a.real) && !Float.isNaN(b.real), "Real At (" + i + "," + j + ") A = " + a.real + " B = " + b.real);
                assertTrue(!Float.isInfinite(a.real) && !Float.isInfinite(b.real), "Real At (" + i + "," + j + ") A = " + a.real + " B = " + b.real);
                assertTrue(Math.abs(a.real - b.real) <= tol, "Real At (" + i + "," + j + ") A = " + a.real + " B = " + b.real);

                assertTrue(!Float.isNaN(a.imaginary) && !Float.isNaN(b.imaginary), "Img At (" + i + "," + j + ") A = " + a.imaginary + " B = " + b.imaginary);
                assertTrue(!Float.isInfinite(a.imaginary) && !Float.isInfinite(b.imaginary), "Img At (" + i + "," + j + ") A = " + a.imaginary + " B = " + b.imaginary);
                assertTrue(Math.abs(a.imaginary - b.imaginary) <= tol, "Img At (" + i + "," + j + ") A = " + a.imaginary + " B = " + b.imaginary);
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
    public static void assertEqualsTrans(DMatrix A , DMatrix B , double tol ) {
        assertShape(A,B.getNumCols(),B.getNumRows());

        for( int i = 0; i < A.getNumRows(); i++ ){
            for( int j = 0; j < A.getNumCols(); j++ ) {
                double valA = A.get(i,j);
                double valB = B.get(j,i);

                assertTrue(!Double.isNaN(valA) && !Double.isNaN(valB) ,"A("+i+","+j+") = "+valA+") B("+j+","+i+") = "+valB);
                assertTrue(!Double.isInfinite(valA) && !Double.isInfinite(valB) ,"A("+i+","+j+") = "+valA+") B("+j+","+i+") = "+valB);
                assertTrue(Math.abs( valA-valB) <= tol,"A("+i+","+j+") = "+valA+") B("+j+","+i+") = "+valB);
            }
        }
    }

    public static void assertEqualsTrans(FMatrix A , FMatrix B , double tol ) {
        assertShape(A,B.getNumCols(),B.getNumRows());

        for( int i = 0; i < A.getNumRows(); i++ ){
            for( int j = 0; j < A.getNumCols(); j++ ) {
                Float valA = A.get(i,j);
                Float valB = B.get(j,i);

                assertTrue(!Float.isNaN(valA) && !Float.isNaN(valB) ,"A("+i+","+j+") = "+valA+") B("+j+","+i+") = "+valB);
                assertTrue(!Float.isInfinite(valA) && !Float.isInfinite(valB) ,"A("+i+","+j+") = "+valA+") B("+j+","+i+") = "+valB);
                assertTrue(Math.abs( valA-valB) <= tol,"A("+i+","+j+") = "+valA+") B("+j+","+i+") = "+valB);
            }
        }
    }

    @SuppressWarnings({"ConstantConditions"})
    private static void assertTrue( boolean result , String message ) {
        // if turned on use asserts
        assert result : message;
        // otherwise throw an exception
        if( !result ) throw new AssertionError(message);
    }
}
