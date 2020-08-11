/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row;

import org.ejml.EjmlParameters;
import org.ejml.LinearSolverSafe;
import org.ejml.data.*;
import org.ejml.dense.row.decompose.lu.LUDecompositionAlt_CDRM;
import org.ejml.dense.row.factory.LinearSolverFactory_CDRM;
import org.ejml.dense.row.misc.TransposeAlgs_CDRM;
import org.ejml.dense.row.mult.MatrixMatrixMult_CDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;

import java.util.Arrays;

/**
 * Common operations on complex numbers
 *
 * @author Peter Abeles
 */
public class CommonOps_CDRM {

    /**
     * <p>
     * Creates an identity matrix of the specified size.<br>
     * <br>
     * a<sub>ij</sub> = 0+0i   if i &ne; j<br>
     * a<sub>ij</sub> = 1+0i   if i = j<br>
     * </p>
     *
     * @param width The width and height of the identity matrix.
     * @return A new instance of an identity matrix.
     */
    public static CMatrixRMaj identity(int width ) {
        CMatrixRMaj A = new CMatrixRMaj(width,width);

        for (int i = 0; i < width; i++) {
            A.set(i,i,1,0);
        }

        return A;
    }

    /**
     * <p>
     * Creates a matrix with diagonal elements set to 1 and the rest 0.<br>
     * <br>
     * a<sub>ij</sub> = 0+0i   if i &ne; j<br>
     * a<sub>ij</sub> = 1+0i   if i = j<br>
     * </p>
     *
     * @param width The width of the identity matrix.
     * @param height The height of the identity matrix.
     * @return A new instance of an identity matrix.
     */
    public static CMatrixRMaj identity(int width , int height) {
        CMatrixRMaj A = new CMatrixRMaj(width,height);

        int m = Math.min(width,height);
        for (int i = 0; i < m; i++) {
            A.set(i,i,1,0);
        }

        return A;
    }

    /**
     * <p>
     * Creates a new square matrix whose diagonal elements are specified by data and all
     * the other elements are zero.<br>
     * <br>
     * a<sub>ij</sub> = 0         if i &le; j<br>
     * a<sub>ij</sub> = diag[i]   if i = j<br>
     * </p>
     *
     * @param data Contains the values of the diagonal elements of the resulting matrix.
     * @return A new complex matrix.
     */
    public static CMatrixRMaj diag(float... data ) {
        if( data.length%2 == 1 )
            throw new IllegalArgumentException("must be an even number of arguments");

        return diag(new CMatrixRMaj(1,1),data.length/2,data);
    }

    public static CMatrixRMaj diag(CMatrixRMaj output , int N, float... data ) {
        output.reshape(N,N);

        int index = 0;
        for (int i = 0; i < N; i++) {
            output.set(i,i,data[index++],data[index++]);
        }

        return output;
    }

    /**
     * <p>
     * Extracts the diagonal elements 'src' write it to the 'dst' vector.  'dst'
     * can either be a row or column vector.
     * <p>
     *
     * @param src Matrix whose diagonal elements are being extracted. Not modified.
     * @param dst A vector the results will be written into. Modified.
     */
    public static void extractDiag(CMatrixRMaj src, CMatrixRMaj dst )
    {
        int N = Math.min(src.numRows, src.numCols);

        // reshape if it's not the right size
        if( !MatrixFeatures_CDRM.isVector(dst) || dst.numCols*dst.numCols != N ) {
            dst.reshape(N,1);
        }

        for( int i = 0; i < N; i++ ) {
            int index = src.getIndex(i,i);
            dst.data[i*2]   = src.data[index];
            dst.data[i*2+1] = src.data[index+1];
        }
    }

    /**
     * Converts the real matrix into a complex matrix.
     *
     * @param input Real matrix. Not modified.
     * @param output Complex matrix. Modified.
     */
    public static void convert(FMatrixD1 input , CMatrixD1 output ) {
        if( input.numCols != output.numCols || input.numRows != output.numRows ) {
            throw new IllegalArgumentException("The matrices are not all the same dimension.");
        }

        Arrays.fill(output.data, 0, output.getDataLength(), 0);

        final int length = output.getDataLength();

        for( int i = 0; i < length; i += 2 ) {
            output.data[i] = input.data[i/2];
        }
    }

    /**
     * Places the real component of the input matrix into the output matrix.
     *
     * @param input Complex matrix. Not modified.
     * @param output real matrix. Modified.
     */
    public static FMatrixRMaj stripReal(CMatrixD1 input , FMatrixRMaj output ) {
        if( output == null ) {
            output = new FMatrixRMaj(input.numRows,input.numCols);
        } else if( input.numCols != output.numCols || input.numRows != output.numRows ) {
            throw new IllegalArgumentException("The matrices are not all the same dimension.");
        }

        final int length = input.getDataLength();

        for( int i = 0; i < length; i += 2 ) {
            output.data[i/2] = input.data[i];
        }
        return output;
    }

    /**
     * Places the imaginary component of the input matrix into the output matrix.
     *
     * @param input Complex matrix. Not modified.
     * @param output real matrix. Modified.
     */
    public static FMatrixRMaj stripImaginary(CMatrixD1 input , FMatrixRMaj output ) {
        if( output == null ) {
            output = new FMatrixRMaj(input.numRows,input.numCols);
        } else if( input.numCols != output.numCols || input.numRows != output.numRows ) {
            throw new IllegalArgumentException("The matrices are not all the same dimension.");
        }

        final int length = input.getDataLength();

        for( int i = 1; i < length; i += 2 ) {
            output.data[i/2] = input.data[i];
        }
        return output;
    }

    /**
     * <p>
     * Computes the magnitude of the complex number in the input matrix and stores the results in the output
     * matrix.
     * </p>
     *
     * magnitude = sqrt(real^2 + imaginary^2)
     *
     * @param input Complex matrix. Not modified.
     * @param output real matrix. Modified.
     */
    public static void magnitude(CMatrixD1 input , FMatrixD1 output ) {
        if( input.numCols != output.numCols || input.numRows != output.numRows ) {
            throw new IllegalArgumentException("The matrices are not all the same dimension.");
        }

        final int length = input.getDataLength();

        for( int i = 0; i < length; i += 2 ) {
            float real = input.data[i];
            float imaginary = input.data[i+1];

            output.data[i/2] = (float)Math.sqrt(real*real + imaginary*imaginary);
        }
    }

    /**
     * <p>
     * Computes the complex conjugate of the input matrix.<br>
     * <br>
     * real<sub>i,j</sub> = real<sub>i,j</sub><br>
     * imaginary<sub>i,j</sub> = -1*imaginary<sub>i,j</sub><br>
     * </p>
     *
     * @param input Input matrix.  Not modified.
     * @param output The complex conjugate of the input matrix.  Modified.
     */
    public static void conjugate(CMatrixD1 input , CMatrixD1 output ) {
        if( input.numCols != output.numCols || input.numRows != output.numRows ) {
            throw new IllegalArgumentException("The matrices are not all the same dimension.");
        }

        final int length = input.getDataLength();

        for( int i = 0; i < length; i += 2 ) {
            output.data[i] = input.data[i];
            output.data[i+1] = -input.data[i+1];
        }
    }

    /**
     * <p>
     * Sets every element in the matrix to the specified value.<br>
     * <br>
     * a<sub>ij</sub> = value
     * <p>
     *
     * @param a A matrix whose elements are about to be set. Modified.
     * @param real The real component
     * @param imaginary The imaginary component
     */
    public static void fill(CMatrixD1 a, float real, float imaginary)
    {
        int N = a.getDataLength();
        for (int i = 0; i < N; i += 2) {
            a.data[i] = real;
            a.data[i+1] = imaginary;
        }
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = a + b <br>
     * c<sub>ij</sub> = a<sub>ij</sub> + b<sub>ij</sub> <br>
     * </p>
     *
     * <p>
     * Matrix C can be the same instance as Matrix A and/or B.
     * </p>
     *
     * @param a A Matrix. Not modified.
     * @param b A Matrix. Not modified.
     * @param c A Matrix where the results are stored. Modified.
     */
    public static void add(CMatrixD1 a , CMatrixD1 b , CMatrixD1 c )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows
                || a.numCols != c.numCols || a.numRows != c.numRows ) {
            throw new IllegalArgumentException("The matrices are not all the same dimension.");
        }

        final int length = a.getDataLength();

        for( int i = 0; i < length; i++ ) {
            c.data[i] = a.data[i]+b.data[i];
        }
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = a - b <br>
     * c<sub>ij</sub> = a<sub>ij</sub> - b<sub>ij</sub> <br>
     * </p>
     *
     * <p>
     * Matrix C can be the same instance as Matrix A and/or B.
     * </p>
     *
     * @param a A Matrix. Not modified.
     * @param b A Matrix. Not modified.
     * @param c A Matrix where the results are stored. Modified.
     */
    public static void subtract(CMatrixD1 a , CMatrixD1 b , CMatrixD1 c )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows
                || a.numCols != c.numCols || a.numRows != c.numRows ) {
            throw new IllegalArgumentException("The matrices are not all the same dimension.");
        }

        final int length = a.getDataLength();

        for( int i = 0; i < length; i++ ) {
            c.data[i] = a.data[i]-b.data[i];
        }
    }

    /**
     * <p>
     * Performs an in-place element by element scalar multiplication.<br>
     * <br>
     * a<sub>ij</sub> = &alpha;*a<sub>ij</sub>
     * </p>
     *
     * @param a The matrix that is to be scaled.  Modified.
     * @param alphaReal real component of scale factor
     * @param alphaImag imaginary component of scale factor
     */
    public static void scale( float alphaReal, float alphaImag , CMatrixD1 a )
    {
        // on very small matrices (2 by 2) the call to getNumElements() can slow it down
        // slightly compared to other libraries since it involves an extra multiplication.
        final int size = a.getNumElements()*2;

        for( int i = 0; i < size; i += 2 ) {
            float real = a.data[i];
            float imag = a.data[i+1];

            a.data[i] = real*alphaReal - imag*alphaImag;
            a.data[i+1] = real*alphaImag + imag*alphaReal;
        }
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = a * b <br>
     * <br>
     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { * a<sub>ik</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void mult(CMatrixRMaj a, CMatrixRMaj b, CMatrixRMaj c)
    {
        if( b.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH) {
            MatrixMatrixMult_CDRM.mult_reorder(a, b, c);
        } else {
            MatrixMatrixMult_CDRM.mult_small(a, b, c);
        }
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = &alpha; * a * b <br>
     * <br>
     * c<sub>ij</sub> = &alpha; &sum;<sub>k=1:n</sub> { * a<sub>ik</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param realAlpha real component of scaling factor.
     * @param imgAlpha imaginary component of scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void mult(float realAlpha , float imgAlpha , CMatrixRMaj a , CMatrixRMaj b , CMatrixRMaj c )
    {
        if( b.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH ) {
            MatrixMatrixMult_CDRM.mult_reorder(realAlpha,imgAlpha,a,b,c);
        } else {
            MatrixMatrixMult_CDRM.mult_small(realAlpha,imgAlpha,a,b,c);
        }
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + a * b<br>
     * c<sub>ij</sub> = c<sub>ij</sub> + &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAdd(CMatrixRMaj a , CMatrixRMaj b , CMatrixRMaj c )
    {
        if( b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
            MatrixMatrixMult_CDRM.multAdd_reorder(a, b, c);
        } else {
            MatrixMatrixMult_CDRM.multAdd_small(a,b,c);
        }
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + &alpha; * a * b<br>
     * c<sub>ij</sub> = c<sub>ij</sub> +  &alpha; * &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param realAlpha real component of scaling factor.
     * @param imgAlpha imaginary component of scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAdd(float realAlpha , float imgAlpha , CMatrixRMaj a , CMatrixRMaj b , CMatrixRMaj c )
    {
        if( b.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH ) {
            MatrixMatrixMult_CDRM.multAdd_reorder(realAlpha,imgAlpha,a,b,c);
        } else {
            MatrixMatrixMult_CDRM.multAdd_small(realAlpha,imgAlpha,a,b,c);
        }
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = a<sup>H</sup> * b <br>
     * <br>
     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransA(CMatrixRMaj a , CMatrixRMaj b , CMatrixRMaj c )
    {
        if( a.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH ||
                b.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH  ) {
            MatrixMatrixMult_CDRM.multTransA_reorder(a, b, c);
        } else {
            MatrixMatrixMult_CDRM.multTransA_small(a, b, c);
        }
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = &alpha; * a<sup>H</sup> * b <br>
     * <br>
     * c<sub>ij</sub> = &alpha; &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param realAlpha Real component of scaling factor.
     * @param imagAlpha Imaginary component of scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransA(float realAlpha , float imagAlpha, CMatrixRMaj a , CMatrixRMaj b , CMatrixRMaj c )
    {
        // TODO add a matrix vectory multiply here
        if( a.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH ||
                b.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH ) {
            MatrixMatrixMult_CDRM.multTransA_reorder(realAlpha, imagAlpha, a, b, c);
        } else {
            MatrixMatrixMult_CDRM.multTransA_small(realAlpha, imagAlpha, a, b, c);
        }
    }

        /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = a * b<sup>H</sup> <br>
     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransB(CMatrixRMaj a , CMatrixRMaj b , CMatrixRMaj c )
    {
        MatrixMatrixMult_CDRM.multTransB(a, b, c);
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c =  &alpha; * a * b<sup>H</sup> <br>
     * c<sub>ij</sub> = &alpha; &sum;<sub>k=1:n</sub> {  a<sub>ik</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param realAlpha Real component of scaling factor.
     * @param imagAlpha Imaginary component of scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransB(float realAlpha , float imagAlpha, CMatrixRMaj a , CMatrixRMaj b , CMatrixRMaj c )
    {
        // TODO add a matrix vectory multiply here
        MatrixMatrixMult_CDRM.multTransB(realAlpha,imagAlpha,a,b,c);
    }

        /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = a<sup>T</sup> * b<sup>T</sup><br>
     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransAB(CMatrixRMaj a , CMatrixRMaj b , CMatrixRMaj c )
    {
        if( a.numCols >= EjmlParameters.CMULT_TRANAB_COLUMN_SWITCH ) {
            MatrixMatrixMult_CDRM.multTransAB_aux(a, b, c, null);
        } else {
            MatrixMatrixMult_CDRM.multTransAB(a, b, c);
        }
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = &alpha; * a<sup>H</sup> * b<sup>H</sup><br>
     * c<sub>ij</sub> = &alpha; &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param realAlpha Real component of scaling factor.
     * @param imagAlpha Imaginary component of scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransAB(float realAlpha , float imagAlpha , CMatrixRMaj a , CMatrixRMaj b , CMatrixRMaj c )
    {
        // TODO add a matrix vectory multiply here
        if( a.numCols >= EjmlParameters.CMULT_TRANAB_COLUMN_SWITCH ) {
            MatrixMatrixMult_CDRM.multTransAB_aux(realAlpha, imagAlpha, a, b, c, null);
        } else {
            MatrixMatrixMult_CDRM.multTransAB(realAlpha, imagAlpha, a, b, c);
        }
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + a<sup>H</sup> * b<br>
     * c<sub>ij</sub> = c<sub>ij</sub> + &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransA(CMatrixRMaj a , CMatrixRMaj b , CMatrixRMaj c )
    {
        if( a.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH ||
                b.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH  ) {
            MatrixMatrixMult_CDRM.multAddTransA_reorder(a, b, c);
        } else {
            MatrixMatrixMult_CDRM.multAddTransA_small(a, b, c);
        }
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + &alpha; * a<sup>H</sup> * b<br>
     * c<sub>ij</sub> =c<sub>ij</sub> +  &alpha; * &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param realAlpha Real component of scaling factor.
     * @param imagAlpha Imaginary component of scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransA(float realAlpha , float imagAlpha , CMatrixRMaj a , CMatrixRMaj b , CMatrixRMaj c )
    {
        // TODO add a matrix vectory multiply here
        if( a.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH ||
                b.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH ) {
            MatrixMatrixMult_CDRM.multAddTransA_reorder(realAlpha, imagAlpha, a, b, c);
        } else {
            MatrixMatrixMult_CDRM.multAddTransA_small(realAlpha, imagAlpha, a, b, c);
        }
    }


    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + a * b<sup>H</sup> <br>
     * c<sub>ij</sub> = c<sub>ij</sub> + &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransB(CMatrixRMaj a , CMatrixRMaj b , CMatrixRMaj c )
    {
        MatrixMatrixMult_CDRM.multAddTransB(a,b,c);
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + &alpha; * a * b<sup>H</sup><br>
     * c<sub>ij</sub> = c<sub>ij</sub> + &alpha; * &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param realAlpha Real component of scaling factor.
     * @param imagAlpha Imaginary component of scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransB(float realAlpha , float imagAlpha , CMatrixRMaj a , CMatrixRMaj b , CMatrixRMaj c )
    {
        // TODO add a matrix vectory multiply here
        MatrixMatrixMult_CDRM.multAddTransB(realAlpha,imagAlpha,a,b,c);
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + a<sup>H</sup> * b<sup>H</sup><br>
     * c<sub>ij</sub> = c<sub>ij</sub> + &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not Modified.
     * @param b The right matrix in the multiplication operation. Not Modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransAB(CMatrixRMaj a , CMatrixRMaj b , CMatrixRMaj c )
    {
        if( a.numCols >= EjmlParameters.CMULT_TRANAB_COLUMN_SWITCH ) {
            MatrixMatrixMult_CDRM.multAddTransAB_aux(a,b,c,null);
        } else {
            MatrixMatrixMult_CDRM.multAddTransAB(a,b,c);
        }
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + &alpha; * a<sup>H</sup> * b<sup>H</sup><br>
     * c<sub>ij</sub> = c<sub>ij</sub> + &alpha; * &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param realAlpha Real component of scaling factor.
     * @param imagAlpha Imaginary component of scaling factor.
     * @param a The left matrix in the multiplication operation. Not Modified.
     * @param b The right matrix in the multiplication operation. Not Modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransAB(float realAlpha , float imagAlpha , CMatrixRMaj a , CMatrixRMaj b , CMatrixRMaj c )
    {
        // TODO add a matrix vectory multiply here
        if( a.numCols >= EjmlParameters.CMULT_TRANAB_COLUMN_SWITCH ) {
            MatrixMatrixMult_CDRM.multAddTransAB_aux(realAlpha,imagAlpha, a, b, c, null);
        } else {
            MatrixMatrixMult_CDRM.multAddTransAB(realAlpha,imagAlpha, a, b, c);
        }
    }

    /**
     * <p>Performs an "in-place" transpose.</p>
     *
     * <p>
     * For square matrices the transpose is truly in-place and does not require
     * additional memory.  For non-square matrices, internally a temporary matrix is declared and
     * {@link #transpose(CMatrixRMaj, CMatrixRMaj)} is invoked.
     * </p>
     *
     * @param mat The matrix that is to be transposed. Modified.
     */
    public static void transpose( CMatrixRMaj mat ) {
        if( mat.numCols == mat.numRows ){
            TransposeAlgs_CDRM.square(mat);
        } else {
            CMatrixRMaj b = new CMatrixRMaj(mat.numCols,mat.numRows);
            transpose(mat, b);
            mat.reshape(b.numRows, b.numCols);
            mat.set(b);
        }
    }

    /**
     * <p>Performs an "in-place" conjugate transpose.</p>
     *
     * @see #transpose(CMatrixRMaj)
     *
     * @param mat The matrix that is to be transposed. Modified.
     */
    public static void transposeConjugate( CMatrixRMaj mat ) {
        if( mat.numCols == mat.numRows ){
            TransposeAlgs_CDRM.squareConjugate(mat);
        } else {
            CMatrixRMaj b = new CMatrixRMaj(mat.numCols,mat.numRows);
            transposeConjugate(mat, b);
            mat.reshape(b.numRows, b.numCols);
            mat.set(b);
        }
    }

    /**
     * <p>
     * Transposes input matrix 'a' and stores the results in output matrix 'b':<br>
     * <br>
     * b<sub>ij</sub> = a<sub>ji</sub><br>
     * where 'b' is the transpose of 'a'.
     * </p>
     *
     * @param input The original matrix.  Not modified.
     * @param output Where the transpose is stored. If null a new matrix is created. Modified.
     * @return The transposed matrix.
     */
    public static CMatrixRMaj transpose(CMatrixRMaj input , CMatrixRMaj output )
    {
        if( output == null ) {
            output = new CMatrixRMaj(input.numCols,input.numRows);
        } else if( input.numCols != output.numRows || input.numRows != output.numCols ) {
            throw new IllegalArgumentException("Input and output shapes are not compatible");
        }

        TransposeAlgs_CDRM.standard(input,output);

        return output;
    }

    /**
     * <p>
     * Conjugate transposes input matrix 'a' and stores the results in output matrix 'b':<br>
     * <br>
     * b-real<sub>i,j</sub> = a-real<sub>j,i</sub><br>
     * b-imaginary<sub>i,j</sub> = -1*a-imaginary<sub>j,i</sub><br>
     * where 'b' is the transpose of 'a'.
     * </p>
     *
     * @param input The original matrix.  Not modified.
     * @param output Where the transpose is stored. If null a new matrix is created. Modified.
     * @return The transposed matrix.
     */
    public static CMatrixRMaj transposeConjugate(CMatrixRMaj input , CMatrixRMaj output )
    {
        if( output == null ) {
            output = new CMatrixRMaj(input.numCols,input.numRows);
        } else if( input.numCols != output.numRows || input.numRows != output.numCols ) {
            throw new IllegalArgumentException("Input and output shapes are not compatible");
        }

        TransposeAlgs_CDRM.standardConjugate(input, output);

        return output;
    }

    /**
     * <p>
     * Performs a matrix inversion operation on the specified matrix and stores the results
     * in the same matrix.<br>
     * <br>
     * a = a<sup>-1</sup>
     * </p>
     *
     * <p>
     * If the algorithm could not invert the matrix then false is returned.  If it returns true
     * that just means the algorithm finished.  The results could still be bad
     * because the matrix is singular or nearly singular.
     * </p>
     *
     * @param A The matrix that is to be inverted.  Results are stored here.  Modified.
     * @return true if it could invert the matrix false if it could not.
     */
    public static boolean invert( CMatrixRMaj A )
    {
        LinearSolverDense<CMatrixRMaj> solver = LinearSolverFactory_CDRM.lu(A.numRows);

        if( solver.setA(A) ) {
            solver.invert(A);
        } else {
            return false;
        }
        return true;
    }

    /**
     * <p>
     * Performs a matrix inversion operation that does not modify the original
     * and stores the results in another matrix.  The two matrices must have the
     * same dimension.<br>
     * <br>
     * b = a<sup>-1</sup>
     * </p>
     *
     * <p>
     * If the algorithm could not invert the matrix then false is returned.  If it returns true
     * that just means the algorithm finished.  The results could still be bad
     * because the matrix is singular or nearly singular.
     * </p>
     *
     * <p>
     * For medium to large matrices there might be a slight performance boost to using
     * {@link LinearSolverFactory_CDRM} instead.
     * </p>
     *
     * @param input The matrix that is to be inverted. Not modified.
     * @param output Where the inverse matrix is stored.  Modified.
     * @return true if it could invert the matrix false if it could not.
     */
    public static boolean invert(CMatrixRMaj input , CMatrixRMaj output )
    {
        LinearSolverDense<CMatrixRMaj> solver = LinearSolverFactory_CDRM.lu(input.numRows);

        if( solver.modifiesA() )
            input = input.copy();

        if( !solver.setA(input))
            return false;
        solver.invert(output);
        return true;
    }

    /**
     * <p>
     * Solves for x in the following equation:<br>
     * <br>
     * A*x = b
     * </p>
     *
     * <p>
     * If the system could not be solved then false is returned.  If it returns true
     * that just means the algorithm finished operating, but the results could still be bad
     * because 'A' is singular or nearly singular.
     * </p>
     *
     * <p>
     * If repeat calls to solve are being made then one should consider using {@link LinearSolverFactory_CDRM}
     * instead.
     * </p>
     *
     * <p>
     * It is ok for 'b' and 'x' to be the same matrix.
     * </p>
     *
     * @param a A matrix that is m by n. Not modified.
     * @param b A matrix that is n by k. Not modified.
     * @param x A matrix that is m by k. Modified.
     *
     * @return true if it could invert the matrix false if it could not.
     */
    public static boolean solve(CMatrixRMaj a , CMatrixRMaj b , CMatrixRMaj x )
    {
        LinearSolverDense<CMatrixRMaj> solver;
        if( a.numCols == a.numRows ) {
            solver = LinearSolverFactory_CDRM.lu(a.numRows);
        } else {
            solver = LinearSolverFactory_CDRM.qr(a.numRows, a.numCols);
        }

        // make sure the inputs 'a' and 'b' are not modified
        solver = new LinearSolverSafe<CMatrixRMaj>(solver);

        if( !solver.setA(a) )
            return false;

        solver.solve(b,x);
        return true;
    }

    /**
     * Returns the determinant of the matrix.  If the inverse of the matrix is also
     * needed, then using {@link LUDecompositionAlt_CDRM} directly (or any
     * similar algorithm) can be more efficient.
     *
     * @param mat The matrix whose determinant is to be computed.  Not modified.
     * @return The determinant.
     */
    public static Complex_F32 det(CMatrixRMaj mat  )
    {
        LUDecompositionAlt_CDRM alg = new LUDecompositionAlt_CDRM();

        if( alg.inputModified() ) {
            mat = mat.copy();
        }

        if( !alg.decompose(mat) )
            return new Complex_F32();
        return alg.computeDeterminant();
    }

    /**
     * <p>Performs  element by element multiplication operation with a complex numbert<br>
     * <br>
     * output<sub>ij</sub> = input<sub>ij</sub> * (real + imaginary*i) <br>
     * </p>
     * @param input The left matrix in the multiplication operation. Not modified.
     * @param real Real component of the number it is multiplied by
     * @param imaginary Imaginary component of the number it is multiplied by
     * @param output Where the results of the operation are stored. Modified.
     */
    public static void elementMultiply(CMatrixD1 input , float real , float imaginary, CMatrixD1 output )
    {
        if( input.numCols != output.numCols || input.numRows != output.numRows ) {
            throw new IllegalArgumentException("The 'input' and 'output' matrices do not have compatible dimensions");
        }

        int N = input.getDataLength();
        for (int i = 0; i < N; i += 2 ) {
            float inReal = input.data[i];
            float intImag = input.data[i+1];

            output.data[i] = inReal*real - intImag*imaginary;
            output.data[i+1] = inReal*imaginary + intImag*real;
        }
    }

    /**
     * <p>Performs  element by element division operation with a complex number on the right<br>
     * <br>
     * output<sub>ij</sub> = input<sub>ij</sub> / (real + imaginary*i) <br>
     * </p>
     * @param input The left matrix in the multiplication operation. Not modified.
     * @param real Real component of the number it is multiplied by
     * @param imaginary Imaginary component of the number it is multiplied by
     * @param output Where the results of the operation are stored. Modified.
     */
    public static void elementDivide(CMatrixD1 input , float real , float imaginary, CMatrixD1 output )
    {
        if( input.numCols != output.numCols || input.numRows != output.numRows ) {
            throw new IllegalArgumentException("The 'input' and 'output' matrices do not have compatible dimensions");
        }

        float norm = real*real + imaginary*imaginary;

        int N = input.getDataLength();
        for (int i = 0; i < N; i += 2 ) {
            float inReal = input.data[i];
            float inImag = input.data[i+1];

            output.data[i]   = (inReal*real + inImag*imaginary)/norm;
            output.data[i+1] = (inImag*real - inReal*imaginary)/norm;
        }
    }

    /**
     * <p>Performs  element by element division operation with a complex number on the right<br>
     * <br>
     * output<sub>ij</sub> = (real + imaginary*i) / input<sub>ij</sub> <br>
     * </p>
     * @param real Real component of the number it is multiplied by
     * @param imaginary Imaginary component of the number it is multiplied by
     * @param input The right matrix in the multiplication operation. Not modified.
     * @param output Where the results of the operation are stored. Modified.
     */
    public static void elementDivide(float real , float imaginary, CMatrixD1 input , CMatrixD1 output )
    {
        if( input.numCols != output.numCols || input.numRows != output.numRows ) {
            throw new IllegalArgumentException("The 'input' and 'output' matrices do not have compatible dimensions");
        }

        int N = input.getDataLength();
        for (int i = 0; i < N; i += 2 ) {
            float inReal = input.data[i];
            float inImag = input.data[i+1];

            float norm = inReal*inReal + inImag*inImag;

            output.data[i]   = (real*inReal + imaginary*inImag)/norm;
            output.data[i+1] = (imaginary*inReal - real*inImag)/norm;
        }
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
    public static float elementMinReal( CMatrixD1 a ) {
        final int size = a.getDataLength();

        float min = a.data[0];
        for( int i = 2; i < size; i += 2 ) {
            float val = a.data[i];
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
    public static float elementMinImaginary( CMatrixD1 a ) {
        final int size = a.getDataLength();

        float min = a.data[1];
        for( int i = 3; i < size; i += 2 ) {
            float val = a.data[i];
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
    public static float elementMaxReal( CMatrixD1 a ) {
        final int size = a.getDataLength();

        float max = a.data[0];
        for( int i = 2; i < size; i += 2 ) {
            float val = a.data[i];
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
    public static float elementMaxImaginary( CMatrixD1 a ) {
        final int size = a.getDataLength();

        float max = a.data[1];
        for( int i = 3; i < size; i += 2 ) {
            float val = a.data[i];
            if( val > max ) {
                max = val;
            }
        }

        return max;
    }

    /**
     * <p>
     * Returns the magnitude squared of the complex element with the largest magnitude<br>
     * <br>
     * Max{ |a<sub>ij</sub>|^2 } for all i and j<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The max magnitude squared
     */
    public static float elementMaxMagnitude2( CMatrixD1 a ) {
        final int size = a.getDataLength();

        float max = 0;
        for( int i = 0; i < size; ) {
            float real = a.data[i++];
            float imaginary = a.data[i++];

            float m = real*real + imaginary*imaginary;

            if( m > max ) {
                max = m;
            }
        }

        return max;
    }

    /**
     * Sets all the diagonal elements equal to one and everything else equal to zero.
     * If this is a square matrix then it will be an identity matrix.
     *
     * @param mat A square matrix.
     */
    public static void setIdentity( CMatrixRMaj mat )
    {
        int width = mat.numRows < mat.numCols ? mat.numRows : mat.numCols;

        Arrays.fill(mat.data,0,mat.getDataLength(),0);

        int index = 0;
        int stride = mat.getRowStride();

        for( int i = 0; i < width; i++ , index += stride + 2) {
            mat.data[index] = 1;
        }
    }

    /**
     * <p>
     * Creates a new matrix which is the specified submatrix of 'src'
     * </p>
     * <p>
     * s<sub>i-y0 , j-x0</sub> = o<sub>ij</sub> for all y0 &le; i &lt; y1 and x0 &le; j &lt; x1 <br>
     * <br>
     * where 's<sub>ij</sub>' is an element in the submatrix and 'o<sub>ij</sub>' is an element in the
     * original matrix.
     * </p>
     *
     * @param src The original matrix which is to be copied.  Not modified.
     * @param srcX0 Start column.
     * @param srcX1 Stop column+1.
     * @param srcY0 Start row.
     * @param srcY1 Stop row+1.
     * @return Extracted submatrix.
     */
    public static CMatrixRMaj extract(CMatrixRMaj src,
                                        int srcY0, int srcY1,
                                        int srcX0, int srcX1 )
    {
        if( srcY1 <= srcY0 || srcY0 < 0 || srcY1 > src.numRows )
            throw new IllegalArgumentException("srcY1 <= srcY0 || srcY0 < 0 || srcY1 > src.numRows");
        if( srcX1 <= srcX0 || srcX0 < 0 || srcX1 > src.numCols )
            throw new IllegalArgumentException("srcX1 <= srcX0 || srcX0 < 0 || srcX1 > src.numCols");

        int w = srcX1-srcX0;
        int h = srcY1-srcY0;

        CMatrixRMaj dst = new CMatrixRMaj(h,w);

        extract(src, srcY0, srcY1, srcX0, srcX1, dst, 0, 0);

        return dst;
    }

    /**
     * <p>
     * Extracts a submatrix from 'src' and inserts it in a submatrix in 'dst'.
     * </p>
     * <p>
     * s<sub>i-y0 , j-x0</sub> = o<sub>ij</sub> for all y0 &le; i &lt; y1 and x0 &le; j &lt; x1 <br>
     * <br>
     * where 's<sub>ij</sub>' is an element in the submatrix and 'o<sub>ij</sub>' is an element in the
     * original matrix.
     * </p>
     *
     * @param src The original matrix which is to be copied.  Not modified.
     * @param srcX0 Start column.
     * @param srcX1 Stop column+1.
     * @param srcY0 Start row.
     * @param srcY1 Stop row+1.
     * @param dst Where the submatrix are stored.  Modified.
     * @param dstY0 Start row in dst.
     * @param dstX0 start column in dst.
     */
    public static void extract(CMatrixRMaj src,
                               int srcY0, int srcY1,
                               int srcX0, int srcX1,
                               CMatrixRMaj dst,
                               int dstY0, int dstX0 )
    {
        int numRows = srcY1 - srcY0;
        int stride = (srcX1 - srcX0)*2;

        for( int y = 0; y < numRows; y++ ) {
            int indexSrc = src.getIndex(y+srcY0,srcX0);
            int indexDst = dst.getIndex(y+dstY0,dstX0);
            System.arraycopy(src.data,indexSrc,dst.data,indexDst, stride);
        }
    }

    /**
     * Converts the columns in a matrix into a set of vectors.
     *
     * @param A Matrix.  Not modified.
     * @param v Optional storage for columns.
     * @return An array of vectors.
     */
    public static CMatrixRMaj[] columnsToVector(CMatrixRMaj A, CMatrixRMaj[] v)
    {
        CMatrixRMaj[]ret;
        if( v == null || v.length < A.numCols ) {
            ret = new CMatrixRMaj[ A.numCols ];
        } else {
            ret = v;
        }

        for( int i = 0; i < ret.length; i++ ) {
            if( ret[i] == null ) {
                ret[i] = new CMatrixRMaj(A.numRows,1);
            } else {
                ret[i].reshape(A.numRows,1);
            }

            CMatrixRMaj u = ret[i];

            int indexU = 0;
            for( int j = 0; j < A.numRows; j++ ) {
                int indexA = A.getIndex(j,i);
                u.data[indexU++] = A.data[indexA++];
                u.data[indexU++] = A.data[indexA];
            }
        }

        return ret;
    }

    /**
     * <p>
     * Returns the largest absolute value of any element in the matrix.<br>
     * <br>
     * Max{ |a<sub>ij</sub>| } for all i and j<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The max abs element value in the matrix.
     */
    public static float elementMaxAbs( CMatrixRMaj a ) {
        final int size = a.getDataLength();

        float max = 0;
        for( int i = 0; i < size; i += 2 ) {
            float real = a.data[i];
            float imag = a.data[i+1];

            float val = real*real + imag*imag;

            if( val > max ) {
                max = val;
            }
        }

        return (float)Math.sqrt(max);
    }

    /**
     * <p>
     * Returns the smallest absolute value of any element in the matrix.<br>
     * <br>
     * Min{ |a<sub>ij</sub>| } for all i and j<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The min abs element value in the matrix.
     */
    public static float elementMinAbs( CMatrixRMaj a ) {
        final int size = a.getDataLength();

        float min = Float.MAX_VALUE;
        for( int i = 0; i < size; i += 2 ) {
            float real = a.data[i];
            float imag = a.data[i+1];

            float val = real*real + imag*imag;

            if( val < min ) {
                min = val;
            }
        }

        return (float)Math.sqrt(min);
    }
}
