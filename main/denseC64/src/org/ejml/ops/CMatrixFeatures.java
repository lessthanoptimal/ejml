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

package org.ejml.ops;

import org.ejml.alg.dense.decompose.chol.CholeskyDecompositionInner_CD64;
import org.ejml.alg.dense.mult.CVectorVectorMult;
import org.ejml.data.CD1Matrix64F;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
import org.ejml.data.ComplexMatrix64F;

/**
 * <p>
 * Functions for computing the features of complex matrices
 * <p>
 *
 * @author Peter Abeles
 */
public class CMatrixFeatures {

    /**
     * Checks to see if any element in the matrix is NaN.
     *
     * @param m A matrix. Not modified.
     * @return True if any element in the matrix is NaN.
     */
    public static boolean hasNaN( CD1Matrix64F m )
    {
        int length = m.getDataLength();

        for( int i = 0; i < length; i++ ) {
            if( Double.isNaN(m.data[i]))
                return true;
        }
        return false;
    }

    /**
     * Checks to see if any element in the matrix is NaN of Infinite.
     *
     * @param m A matrix. Not modified.
     * @return True if any element in the matrix is NaN of Infinite.
     */
    public static boolean hasUncountable( CD1Matrix64F m )
    {
        int length = m.getDataLength();

        for( int i = 0; i < length; i++ ) {
            double a = m.data[i];
            if( Double.isNaN(a) || Double.isInfinite(a))
                return true;
        }
        return false;
    }

    /**
     * <p>
     * Checks to see if each element in the two matrices are equal:
     * a<sub>ij</sub> == b<sub>ij</sub>
     * <p>
     *
     * <p>
     * NOTE: If any of the elements are NaN then false is returned.  If two corresponding
     * elements are both positive or negative infinity then they are equal.
     * </p>
     *
     * @param a A matrix. Not modified.
     * @param b A matrix. Not modified.
     * @return true if identical and false otherwise.
     */
    public static boolean isEquals( CD1Matrix64F a, CD1Matrix64F b ) {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            return false;
        }

        final int length = a.getDataLength();
        for( int i = 0; i < length; i++ ) {
            if( !(a.data[i] == b.data[i]) ) {
                return false;
            }
        }

        return true;
    }

    /**
     * <p>
     * Checks to see if each element in the two matrices are within tolerance of
     * each other: tol &ge; |a<sub>ij</sub> - b<sub>ij</sub>|.
     * <p>
     *
     * <p>
     * NOTE: If any of the elements are not countable then false is returned.<br>
     * NOTE: If a tolerance of zero is passed in this is equivalent to calling
     * {@link #isEquals(org.ejml.data.CD1Matrix64F, org.ejml.data.CD1Matrix64F)}
     * </p>
     *
     * @param a A matrix. Not modified.
     * @param b A matrix. Not modified.
     * @param tol How close to being identical each element needs to be.
     * @return true if equals and false otherwise.
     */
    public static boolean isEquals( CD1Matrix64F a , CD1Matrix64F b , double tol )
    {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            return false;
        }

        if( tol == 0.0 )
            return isEquals(a,b);

        final int length = a.getDataLength();

        for( int i = 0; i < length; i++ ) {
            if( !(tol >= Math.abs(a.data[i] - b.data[i])) ) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>
     * Checks to see if each corresponding element in the two matrices are
     * within tolerance of each other or have the some symbolic meaning.  This
     * can handle NaN and Infinite numbers.
     * <p>
     *
     * <p>
     * If both elements are countable then the following equality test is used:<br>
     * |a<sub>ij</sub> - b<sub>ij</sub>| &le; tol.<br>
     * Otherwise both numbers must both be Double.NaN, Double.POSITIVE_INFINITY, or
     * Double.NEGATIVE_INFINITY to be identical.
     * </p>
     *
     * @param a A matrix. Not modified.
     * @param b A matrix. Not modified.
     * @param tol Tolerance for equality.
     * @return true if identical and false otherwise.
     */
    public static boolean isIdentical( CD1Matrix64F a, CD1Matrix64F b , double tol ) {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            return false;
        }
        if( tol < 0 )
            throw new IllegalArgumentException("Tolerance must be greater than or equal to zero.");

        final int length = a.getDataLength();
        for( int i = 0; i < length; i++ ) {
            double valA = a.data[i];
            double valB = b.data[i];

            // if either is negative or positive infinity the result will be positive infinity
            // if either is NaN the result will be NaN
            double diff = Math.abs(valA-valB);

            // diff = NaN == false
            // diff = infinity == false
            if( tol >= diff )
                continue;

            if( Double.isNaN(valA) ) {
                return Double.isNaN(valB);
            } else if( Double.isInfinite(valA) ) {
                return valA == valB;
            } else {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks to see if the provided matrix is within tolerance to an identity matrix.
     *
     * @param mat Matrix being examined.  Not modified.
     * @param tol Tolerance.
     * @return True if it is within tolerance to an identify matrix.
     */
    public static boolean isIdentity( ComplexMatrix64F mat , double tol ) {
        // see if the result is an identity matrix
        Complex64F c = new Complex64F();
        for (int i = 0; i < mat.getNumRows(); i++) {
            for (int j = 0; j < mat.getNumCols(); j++) {
                mat.get(i, j, c);
                if (i == j) {
                    if (!(Math.abs(c.real - 1) <= tol))
                        return false;
                    if (!(Math.abs(c.imaginary) <= tol))
                        return false;
                } else {
                    if (!(Math.abs(c.real) <= tol))
                        return false;
                    if (!(Math.abs(c.imaginary) <= tol))
                        return false;
                }
            }
        }

        return true;
    }

    /**
     * Hermitian matrix is a square matrix with complex entries that is equal to its own conjugate transpose.
     *
     * @param Q The matrix being tested. Not modified.
     * @param tol Tolerance.
     * @return True if it passes the test.
     */
    public static boolean isHermitian( CDenseMatrix64F Q , double tol ) {
        if( Q.numCols != Q.numRows )
            return false;

        Complex64F a = new Complex64F();
        Complex64F b = new Complex64F();


        for( int i = 0; i < Q.numCols; i++ ) {
            for( int j = i+1; j < Q.numCols; j++ ) {
                Q.get(i,j,a);
                Q.get(j,i,b);

                if( Math.abs(a.real-b.real)>tol)
                    return false;
                if( Math.abs(a.imaginary+b.imaginary)>tol)
                    return false;
            }
        }

        return true;
    }

    /**
     * <p>
     * Unitary matrices have the following properties:<br><br>
     * Q*Q<sup>H</sup> = I
     * </p>
     * <p>
     * This is the complex equivalent of orthogonal matrix.
     * </p>
     * @param Q The matrix being tested. Not modified.
     * @param tol Tolerance.
     * @return True if it passes the test.
     */
    public static boolean isUnitary( CDenseMatrix64F Q , double tol ) {
        if( Q.numRows < Q.numCols ) {
            throw new IllegalArgumentException("The number of rows must be more than or equal to the number of columns");
        }

        Complex64F prod = new Complex64F();

        CDenseMatrix64F u[] = CCommonOps.columnsToVector(Q, null);

        for( int i = 0; i < u.length; i++ ) {
            CDenseMatrix64F a = u[i];

            CVectorVectorMult.innerProdH(a, a, prod);

            if( Math.abs(prod.real-1) > tol)
                return false;
            if( Math.abs(prod.imaginary) > tol)
                return false;

            for( int j = i+1; j < u.length; j++ ) {
                CVectorVectorMult.innerProdH(a, u[j], prod);

                if( !(prod.getMagnitude2() <= tol*tol))
                    return false;
            }
        }

        return true;
    }

    /**
     * <p>
     * Checks to see if the matrix is positive definite.
     * </p>
     * <p>
     * x<sup>T</sup> A x > 0<br>
     * for all x where x is a non-zero vector and A is a hermitian matrix.
     * </p>
     *
     * @param A square hermitian matrix. Not modified.
     *
     * @return True if it is positive definite and false if it is not.
     */
    public static boolean isPositiveDefinite( CDenseMatrix64F A ) {
        if( A.numCols != A.numRows)
            return false;

        CholeskyDecompositionInner_CD64 chol = new CholeskyDecompositionInner_CD64(true);
        if( chol.inputModified() )
            A = A.copy();

        return chol.decompose(A);
    }
}
