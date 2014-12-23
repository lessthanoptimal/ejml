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

import org.ejml.alg.dense.mult.SubmatrixOps;
import org.ejml.alg.dense.mult.VectorVectorMult;
import org.ejml.data.D1Matrix64F;
import org.ejml.data.DenseMatrix64F;

import java.util.Random;


/**
 * Contains a list of functions for creating random dense real matrices and vectors with different structures.
 *
 * @author Peter Abeles
 */
public class RandomMatrices {

    /**
     * <p>
     * Creates a randomly generated set of orthonormal vectors.  At most it can generate the same
     * number of vectors as the dimension of the vectors.
     * </p>
     *
     * <p>
     * This is done by creating random vectors then ensuring that they are orthogonal
     * to all the ones previously created with reflectors.
     * </p>
     *
     * <p>
     * NOTE: This employs a brute force O(N<sup>3</sup>) algorithm.
     * </p>
     *
     * @param dimen dimension of the space which the vectors will span.
     * @param numVectors How many vectors it should generate.
     * @param rand Used to create random vectors.
     * @return Array of N random orthogonal vectors of unit length.
     */
    // is there a faster algorithm out there? This one is a bit sluggish
    public static DenseMatrix64F[] createSpan( int dimen, int numVectors , Random rand ) {
        if( dimen < numVectors )
            throw new IllegalArgumentException("The number of vectors must be less than or equal to the dimension");

        DenseMatrix64F u[] = new DenseMatrix64F[numVectors];

        u[0] = RandomMatrices.createRandom(dimen,1,-1,1,rand);
        NormOps.normalizeF(u[0]);

        for( int i = 1; i < numVectors; i++ ) {
//            System.out.println(" i = "+i);
            DenseMatrix64F a = new DenseMatrix64F(dimen,1);
            DenseMatrix64F r=null;

            for( int j = 0; j < i; j++ ) {
//                System.out.println("j = "+j);
                if( j == 0 )
                    r = RandomMatrices.createRandom(dimen,1,-1,1,rand);

                // find a vector that is normal to vector j
                // u[i] = (1/2)*(r + Q[j]*r)
                a.set(r);
                VectorVectorMult.householder(-2.0,u[j],r,a);
                CommonOps.add(r,a,a);
                CommonOps.scale(0.5,a);

//                UtilEjml.print(a);

                DenseMatrix64F t = a;
                a = r;
                r = t;

                // normalize it so it doesn't get too small
                double val = NormOps.normF(r);
                if( val == 0 || Double.isNaN(val) || Double.isInfinite(val))
                    throw new RuntimeException("Failed sanity check");
                CommonOps.divide(r,val);
            }

            u[i] = r;
        }

        return u;
    }

    /**
     * Creates a random vector that is inside the specified span.
     *
     * @param span The span the random vector belongs in.
     * @param rand RNG
     * @return A random vector within the specified span.
     */
    public static DenseMatrix64F createInSpan( DenseMatrix64F[] span , double min , double max , Random rand ) {
        DenseMatrix64F A = new DenseMatrix64F(span.length,1);

        DenseMatrix64F B = new DenseMatrix64F(span[0].getNumElements(),1);

        for( int i = 0; i < span.length; i++ ) {
            B.set(span[i]);
            double val = rand.nextDouble()*(max-min)+min;
            CommonOps.scale(val,B);

            CommonOps.add(A,B,A);

        }

        return A;
    }

    /**
     * <p>
     * Creates a random orthogonal or isometric matrix, depending on the number of rows and columns.
     * The number of rows must be more than or equal to the number of columns.
     * </p>
     *
     * @param numRows Number of rows in the generated matrix.
     * @param numCols Number of columns in the generated matrix.
     * @param rand Random number generator used to create matrices.
     * @return A new isometric matrix.
     */
    public static DenseMatrix64F createOrthogonal( int numRows , int numCols , Random rand ) {
        if( numRows < numCols ) {
            throw new IllegalArgumentException("The number of rows must be more than or equal to the number of columns");
        }

        DenseMatrix64F u[] = createSpan(numRows,numCols,rand);

        DenseMatrix64F ret = new DenseMatrix64F(numRows,numCols);
        for( int i = 0; i < numCols; i++ ) {
            SubmatrixOps.setSubMatrix(u[i],ret,0,0,0,i,numRows,1);
        }

        return ret;
    }

    /**
     * Creates a random diagonal matrix where the diagonal elements are selected from a uniform
     * distribution that goes from min to max.
     *
     * @param N Dimension of the matrix.
     * @param min Minimum value of a diagonal element.
     * @param max Maximum value of a diagonal element.
     * @param rand Random number generator.
     * @return A random diagonal matrix.
     */
    public static DenseMatrix64F createDiagonal( int N , double min , double max , Random rand ) {
        return createDiagonal(N,N,min,max,rand);
    }

    /**
     * Creates a random matrix where all elements are zero but diagonal elements.  Diagonal elements
     * randomly drawn from a uniform distribution from min to max, inclusive.
     *
     * @param numRows Number of rows in the returned matrix..
     * @param numCols Number of columns in the returned matrix.
     * @param min Minimum value of a diagonal element.
     * @param max Maximum value of a diagonal element.
     * @param rand Random number generator.
     * @return A random diagonal matrix.
     */
    public static DenseMatrix64F createDiagonal( int numRows , int numCols , double min , double max , Random rand ) {
        if( max < min )
            throw new IllegalArgumentException("The max must be >= the min");

        DenseMatrix64F ret = new DenseMatrix64F(numRows,numCols);

        int N = Math.min(numRows,numCols);

        double r = max-min;

        for( int i = 0; i < N; i++ ) {
            ret.set(i,i, rand.nextDouble()*r+min);
        }

        return ret;
    }

    /**
     * <p>
     * Creates a random matrix which will have the provided singular values.  The length of sv
     * is assumed to be the rank of the matrix.  This can be useful for testing purposes when one
     * needs to ensure that a matrix is not singular but randomly generated.
     * </p>
     * 
     * @param numRows Number of rows in generated matrix.
     * @param numCols NUmber of columns in generated matrix.
     * @param rand Random number generator.
     * @param sv Singular values of the matrix.
     * @return A new matrix with the specified singular values.
     */
    public static DenseMatrix64F createSingularValues(int numRows, int numCols,
                                                      Random rand, double ...sv) {
        DenseMatrix64F U = RandomMatrices.createOrthogonal(numRows,numRows,rand);
        DenseMatrix64F V = RandomMatrices.createOrthogonal(numCols,numCols,rand);

        DenseMatrix64F S = new DenseMatrix64F(numRows,numCols);

        int min = Math.min(numRows,numCols);
        min = Math.min(min,sv.length);
        
        for( int i = 0; i < min; i++ ) {
            S.set(i,i,sv[i]);
        }

        DenseMatrix64F tmp = new DenseMatrix64F(numRows,numCols);
        CommonOps.mult(U,S,tmp);
        CommonOps.multTransB(tmp,V,S);

        return S;
    }

    /**
     * Creates a new random symmetric matrix that will have the specified real eigenvalues.
     *
     * @param num Dimension of the resulting matrix.
     * @param rand Random number generator.
     * @param eigenvalues Set of real eigenvalues that the matrix will have.
     * @return A random matrix with the specified eigenvalues.
     */
    public static DenseMatrix64F createEigenvaluesSymm( int num,  Random rand , double ...eigenvalues ) {
        DenseMatrix64F V = RandomMatrices.createOrthogonal(num,num,rand);
        DenseMatrix64F D = CommonOps.diag(eigenvalues);

        DenseMatrix64F temp = new DenseMatrix64F(num,num);

        CommonOps.mult(V,D,temp);
        CommonOps.multTransB(temp,V,D);

        return D;
    }

    /**
     * Returns a matrix where all the elements are selected independently from
     * a uniform distribution between 0 and 1 inclusive.
     *
     * @param numRow Number of rows in the new matrix.
     * @param numCol Number of columns in the new matrix.
     * @param rand Random number generator used to fill the matrix.
     * @return The randomly generated matrix.
     */
    public static DenseMatrix64F createRandom( int numRow , int numCol , Random rand ) {
        DenseMatrix64F mat = new DenseMatrix64F(numRow,numCol);

        setRandom(mat,0,1,rand);

        return mat;
    }

    /**
     * <p>
     * Adds random values to each element in the matrix from an uniform distribution.<br>
     * <br>
     * a<sub>ij</sub> = a<sub>ij</sub> + U(min,max)<br>
     * </p>
     *
     * @param A The matrix who is to be randomized. Modified
     * @param min The minimum value each element can be.
     * @param max The maximum value each element can be..
     * @param rand Random number generator used to fill the matrix.
     */
    public static void addRandom( DenseMatrix64F A , double min , double max , Random rand ) {
        double d[] = A.getData();
        int size = A.getNumElements();

        double r = max-min;

        for( int i = 0; i < size; i++ ) {
            d[i] += r*rand.nextDouble()+min;
        }
    }

    /**
     * <p>
     * Returns a matrix where all the elements are selected independently from
     * a uniform distribution between 'min' and 'max' inclusive.
     * </p>
     *
     * @param numRow Number of rows in the new matrix.
     * @param numCol Number of columns in the new matrix.
     * @param min The minimum value each element can be.
     * @param max The maximum value each element can be.
     * @param rand Random number generator used to fill the matrix.
     * @return The randomly generated matrix.
     */
    public static DenseMatrix64F createRandom( int numRow , int numCol , double min , double max , Random rand ) {
        DenseMatrix64F mat = new DenseMatrix64F(numRow,numCol);

        setRandom(mat,min,max,rand);

        return mat;
    }

    /**
     * <p>
     * Sets each element in the matrix to a value drawn from an uniform distribution from 0 to 1 inclusive.
     * </p>
     *
     * @param mat The matrix who is to be randomized. Modified.
     * @param rand Random number generator used to fill the matrix.
     */
    public static void setRandom( DenseMatrix64F mat , Random rand )
    {
        setRandom(mat,0,1,rand);
    }

    /**
     * <p>
     * Sets each element in the matrix to a value drawn from an uniform distribution from 'min' to 'max' inclusive.
     * </p>
     *
     * @param min The minimum value each element can be.
     * @param max The maximum value each element can be.
     * @param mat The matrix who is to be randomized. Modified.
     * @param rand Random number generator used to fill the matrix.
     */
    public static void setRandom( D1Matrix64F mat , double min , double max , Random rand )
    {
        double d[] = mat.getData();
        int size = mat.getNumElements();

        double r = max-min;

        for( int i = 0; i < size; i++ ) {
            d[i] = r*rand.nextDouble()+min;
        }
    }

    /**
     * <p>
     * Sets each element in the matrix to a value drawn from an Gaussian distribution with the specified mean and
     * standard deviation
     * </p>
     *
     *
     * @param numRow Number of rows in the new matrix.
     * @param numCol Number of columns in the new matrix.
     * @param mean Mean value in the distribution
     * @param stdev Standard deviation in the distribution
     * @param rand Random number generator used to fill the matrix.
     */
    public static DenseMatrix64F createGaussian( int numRow , int numCol , double mean , double stdev , Random rand )
    {
        DenseMatrix64F m = new DenseMatrix64F(numRow,numCol);
        setGaussian(m,mean,stdev,rand);
        return m;
    }

    /**
     * <p>
     * Sets each element in the matrix to a value drawn from an Gaussian distribution with the specified mean and
     * standard deviation
     * </p>
     *
     * @param mat The matrix who is to be randomized. Modified.
     * @param mean Mean value in the distribution
     * @param stdev Standard deviation in the distribution
     * @param rand Random number generator used to fill the matrix.
     */
    public static void setGaussian( D1Matrix64F mat , double mean , double stdev , Random rand )
    {
        double d[] = mat.getData();
        int size = mat.getNumElements();

        for( int i = 0; i < size; i++ ) {
            d[i] = mean + stdev*rand.nextGaussian();
        }
    }

    /**
     * Creates a random symmetric positive definite matrix.
     *
     * @param width The width of the square matrix it returns.
     * @param rand Random number generator used to make the matrix.
     * @return The random symmetric  positive definite matrix.
     */
    public static DenseMatrix64F createSymmPosDef(int width, Random rand) {
        // This is not formally proven to work.  It just seems to work.
        DenseMatrix64F a = new DenseMatrix64F(width,1);
        DenseMatrix64F b = new DenseMatrix64F(width,width);

        for( int i = 0; i < width; i++ ) {
            a.set(i,0,rand.nextDouble());
        }

        CommonOps.multTransB(a,a,b);

        for( int i = 0; i < width; i++ ) {
            b.add(i,i,1);
        }

        return b;
    }

    /**
     * Creates a random symmetric matrix whose values are selected from an uniform distribution
     * from min to max, inclusive.
     *
     * @param length Width and height of the matrix.
     * @param min Minimum value an element can have.
     * @param max Maximum value an element can have.
     * @param rand Random number generator.
     * @return A symmetric matrix.
     */
    public static DenseMatrix64F createSymmetric(int length, double min, double max, Random rand) {
        DenseMatrix64F A = new DenseMatrix64F(length,length);

        createSymmetric(A,min,max,rand);

        return A;
    }

    /**
     * Sets the provided square matrix to be a random symmetric matrix whose values are selected from an uniform distribution
     * from min to max, inclusive.
     *
     * @param A The matrix that is to be modified.  Must be square.  Modified.
     * @param min Minimum value an element can have.
     * @param max Maximum value an element can have.
     * @param rand Random number generator.
     */
    public static void createSymmetric(DenseMatrix64F A, double min, double max, Random rand) {
        if( A.numRows != A.numCols )
            throw new IllegalArgumentException("A must be a square matrix");

        double range = max-min;

        int length = A.numRows;

        for( int i = 0; i < length; i++ ) {
            for( int j = i; j < length; j++ ) {
                double val = rand.nextDouble()*range + min;
                A.set(i,j,val);
                A.set(j,i,val);
            }
        }
    }

    /**
     * Creates an upper triangular matrix whose values are selected from a uniform distribution.  If hessenberg
     * is greater than zero then a hessenberg matrix of the specified degree is created instead.
     *
     * @param dimen Number of rows and columns in the matrix..
     * @param hessenberg 0 for triangular matrix and > 0 for hessenberg matrix.
     * @param min minimum value an element can be.
     * @param max maximum value an element can be.
     * @param rand random number generator used.
     * @return The randomly generated matrix.
     */
    public static DenseMatrix64F createUpperTriangle( int dimen , int hessenberg , double min , double max , Random rand )
    {
        if( hessenberg < 0 )
            throw new RuntimeException("hessenberg must be more than or equal to 0");

        double range = max-min;

        DenseMatrix64F A = new DenseMatrix64F(dimen,dimen);

        for( int i = 0; i < dimen; i++ ) {
            int start = i <= hessenberg ? 0 : i-hessenberg;

            for( int j = start; j < dimen; j++ ) {
                A.set(i,j, rand.nextDouble()*range+min);
            }

        }

        return A;
    }
}
