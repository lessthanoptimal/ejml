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

package org.ejml.alg.dense.mult;

import org.ejml.data.D1Matrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.RowD1Matrix64F;


/**
 * Operations that involve multiplication of two vectors.
 *
 * @author Peter Abeles
 */
public class VectorVectorMult {

    /**
     * <p>
     * Computes the inner product of the two vectors.  In geometry this is known as the dot product.<br>
     * <br>
     * &sum;<sub>k=1:n</sub> x<sub>k</sub> * y<sub>k</sub><br>
     * where x and y are vectors with n elements.
     * </p>
     *
     * <p>
     * These functions are often used inside of highly optimized code and therefor sanity checks are
     * kept to a minimum.  It is not recommended that any of these functions be used directly.
     * </p>
     *
     * @param x A vector with n elements. Not modified.
     * @param y A vector with n elements. Not modified.
     * @return The inner product of the two vectors.
     */
    public static double innerProd( D1Matrix64F x, D1Matrix64F y )
    {
        int m = x.getNumElements();

        double total = 0;
        for( int i = 0; i < m; i++ ) {
            total += x.get(i) * y.get(i);
        }

        return total;
    }

    /**
     * <p>
     * return = x<sup>T</sup>*A*y
     * </p>
     *
     * @param x  A vector with n elements. Not modified.
     * @param A  A matrix with n by m elements.  Not modified.
     * @param y  A vector with m elements. Not modified.
     * @return  The results.
     */
    public static double innerProdA( D1Matrix64F x, D1Matrix64F A , D1Matrix64F y )
    {
        int n = A.numRows;
        int m = A.numCols;

        if( x.getNumElements() != n )
            throw new IllegalArgumentException("Unexpected number of elements in x");
        if( y.getNumElements() != m )
            throw new IllegalArgumentException("Unexpected number of elements in y");

        double result = 0;

        for( int i = 0; i < m; i++ ) {
            double total = 0;

            for( int j = 0; j < n; j++ ) {
                total += x.get(j)*A.unsafe_get(j,i);
            }

            result += total*y.get(i);
        }

        return result;
    }


    /**
     * <p>
     * x<sup>T</sup>A<sup>T</sup>y
     * </p>
     *
     * @param x  A vector with n elements. Not modified.
     * @param A  A matrix with n by n elements.  Not modified.
     * @param y  A vector with n elements. Not modified.
     * @return  The results.
     */
    // TODO better name for this
    public static double innerProdTranA( D1Matrix64F x, D1Matrix64F A , D1Matrix64F y )
    {
        int n = A.numRows;

        if( n != A.numCols)
            throw new IllegalArgumentException("A must be square");

        if( x.getNumElements() != n )
            throw new IllegalArgumentException("Unexpected number of elements in x");
        if( y.getNumElements() != n )
            throw new IllegalArgumentException("Unexpected number of elements in y");

        double result = 0;

        for( int i = 0; i < n; i++ ) {
            double total = 0;

            for( int j = 0; j < n; j++ ) {
                total += x.get(j)*A.unsafe_get(i,j);
            }

            result += total*y.get(i);
        }

        return result;
    }

    /**
     * <p>
     * Sets A &isin; &real; <sup>m &times; n</sup> equal to an outer product multiplication of the two
     * vectors.  This is also known as a rank-1 operation.<br>
     * <br>
     * A = x * y'
     * where x &isin; &real; <sup>m</sup> and y &isin; &real; <sup>n</sup> are vectors.
     * </p>
     * <p>
     * Which is equivalent to: A<sub>ij</sub> = x<sub>i</sub>*y<sub>j</sub>
     * </p>
     *
     * <p>
     * These functions are often used inside of highly optimized code and therefor sanity checks are
     * kept to a minimum.  It is not recommended that any of these functions be used directly.
     * </p>
     *
     * @param x A vector with m elements. Not modified.
     * @param y A vector with n elements. Not modified.
     * @param A A Matrix with m by n elements. Modified.
     */
    public static void outerProd( D1Matrix64F x, D1Matrix64F y, RowD1Matrix64F A ) {
        int m = A.numRows;
        int n = A.numCols;

        int index = 0;
        for( int i = 0; i < m; i++ ) {
            double xdat = x.get(i);
            for( int j = 0; j < n; j++ ) {
                A.set(index++ ,  xdat*y.get(j) );
            }
        }
    }

    /**
     * <p>
     * Adds to A &isin; &real; <sup>m &times; n</sup> the results of an outer product multiplication
     * of the two vectors.  This is also known as a rank 1 update.<br>
     * <br>
     * A = A + &gamma; x * y<sup>T</sup>
     * where x &isin; &real; <sup>m</sup> and y &isin; &real; <sup>n</sup> are vectors.
     * </p>
     * <p>
     * Which is equivalent to: A<sub>ij</sub> = A<sub>ij</sub> + &gamma; x<sub>i</sub>*y<sub>j</sub>
     * </p>
     *
     * <p>
     * These functions are often used inside of highly optimized code and therefor sanity checks are
     * kept to a minimum.  It is not recommended that any of these functions be used directly.
     * </p>
     *
     * @param gamma A multiplication factor for the outer product.
     * @param x A vector with m elements. Not modified.
     * @param y A vector with n elements. Not modified.
     * @param A A Matrix with m by n elements. Modified.
     */
    public static void addOuterProd( double gamma , D1Matrix64F x, D1Matrix64F y, RowD1Matrix64F A ) {
        int m = A.numRows;
        int n = A.numCols;

        int index = 0;
        if( gamma == 1.0 ) {
            for( int i = 0; i < m; i++ ) {
                double xdat = x.get(i);
                for( int j = 0; j < n; j++ ) {
                    A.plus( index++ , xdat*y.get(j) );
                }
            }
        } else {
            for( int i = 0; i < m; i++ ) {
                double xdat = x.get(i);
                for( int j = 0; j < n; j++ ) {
                    A.plus( index++ , gamma*xdat*y.get(j));
                }
            }
        }
    }


    /**
     * <p>
     * Multiplies a householder reflection against a vector:<br>
     * <br>
     * y = (I + &gamma; u u<sup>T</sup>)x<br>
     * </p>
     * <p>
     * The Householder reflection is used in some implementations of QR decomposition.
     * </p>
     * @param u A vector. Not modified.
     * @param x a vector. Not modified.
     * @param y Vector where the result are written to.
     */
    public static void householder( double gamma,
                                    D1Matrix64F u ,
                                    D1Matrix64F x , D1Matrix64F y )
    {
        int n = u.getNumElements();

        double sum = 0;
        for( int i = 0; i < n; i++ ) {
            sum += u.get(i)*x.get(i);
        }
        for( int i = 0; i < n; i++ ) {
            y.set( i , x.get(i) + gamma*u.get(i)*sum);
        }
    }

    /**
     * <p>
     * Performs a rank one update on matrix A using vectors u and w.  The results are stored in B.<br>
     * <br>
     * B = A + &gamma; u w<sup>T</sup><br>
     * </p>
     * <p>
     * This is called a rank1 update because the matrix u w<sup>T</sup> has a rank of 1.  Both A and B
     * can be the same matrix instance, but there is a special rank1Update for that.
     * </p>
     *
     * @param gamma A scalar.
     * @param A A m by m matrix. Not modified.
     * @param u A vector with m elements.  Not modified.
     * @param w A vector with m elements.  Not modified.
     * @param B A m by m matrix where the results are stored. Modified.
     */
    public static void rank1Update( double gamma,
                                    DenseMatrix64F A ,
                                    DenseMatrix64F u , DenseMatrix64F w ,
                                    DenseMatrix64F B )
    {
        int n = u.getNumElements();

        int matrixIndex = 0;
        for( int i = 0; i < n; i++ ) {
            double elementU = u.data[i];

            for( int j = 0; j < n; j++ , matrixIndex++) {
                B.data[matrixIndex] = A.data[matrixIndex] + gamma*elementU*w.data[j];
            }
        }
    }

    /**
     * <p>
     * Performs a rank one update on matrix A using vectors u and w.  The results are stored in A.<br>
     * <br>
     * A = A + &gamma; u w<sup>T</sup><br>
     * </p>
     * <p>
     * This is called a rank1 update because the matrix u w<sup>T</sup> has a rank of 1.
     * </p>
     *
     * @param gamma A scalar.
     * @param A A m by m matrix. Modified.
     * @param u A vector with m elements.  Not modified.
     */
    public static void rank1Update( double gamma,
                                    DenseMatrix64F A ,
                                    DenseMatrix64F u ,
                                    DenseMatrix64F w )
    {
        int n = u.getNumElements();

        int matrixIndex = 0;
        for( int i = 0; i < n; i++ ) {
            double elementU = u.data[i];

            for( int j = 0; j < n; j++ ) {
                A.data[matrixIndex++] += gamma*elementU*w.data[j];
            }
        }
    }
}
