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

import org.ejml.alg.dense.misc.CTransposeAlgs;
import org.ejml.data.CD1Matrix64F;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
import org.ejml.data.D1Matrix64F;

import java.util.Arrays;

/**
 * Common operations on complex numbers
 *
 * @author Peter Abeles
 */
public class CCommonOps {

    /**
     * Converts the real matrix into a complex matrix.
     *
     * @param input Real matrix. Not modified.
     * @param output Complex matrix. Modified.
     */
    public static void convert( D1Matrix64F input , CD1Matrix64F output ) {
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
    public static void stripReal( CD1Matrix64F input , D1Matrix64F output ) {
        if( input.numCols != output.numCols || input.numRows != output.numRows ) {
            throw new IllegalArgumentException("The matrices are not all the same dimension.");
        }

        final int length = input.getDataLength();

        for( int i = 0; i < length; i += 2 ) {
            output.data[i/2] = input.data[i];
        }
    }

    /**
     * Places the imaginary component of the input matrix into the output matrix.
     *
     * @param input Complex matrix. Not modified.
     * @param output real matrix. Modified.
     */
    public static void stripImaginary( CD1Matrix64F input , D1Matrix64F output ) {
        if( input.numCols != output.numCols || input.numRows != output.numRows ) {
            throw new IllegalArgumentException("The matrices are not all the same dimension.");
        }

        final int length = input.getDataLength();

        for( int i = 1; i < length; i += 2 ) {
            output.data[i/2] = input.data[i];
        }
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
    public static void magnitude( CD1Matrix64F input , D1Matrix64F output ) {
        if( input.numCols != output.numCols || input.numRows != output.numRows ) {
            throw new IllegalArgumentException("The matrices are not all the same dimension.");
        }

        final int length = input.getDataLength();

        for( int i = 0; i < length; i += 2 ) {
            double real = input.data[i];
            double imaginary = input.data[i+1];

            output.data[i/2] = Math.sqrt(real*real + imaginary*imaginary);
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
    public static void fill(CD1Matrix64F a, double real, double imaginary)
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
    public static void add( CD1Matrix64F a , CD1Matrix64F b , CD1Matrix64F c )
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
    public static void subtract( CD1Matrix64F a , CD1Matrix64F b , CD1Matrix64F c )
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

    public static void multiply( CDenseMatrix64F a , CDenseMatrix64F b , CDenseMatrix64F c )
    {

    }

    /**
     * <p>Performs an "in-place" transpose.</p>
     *
     * <p>
     * For square matrices the transpose is truly in-place and does not require
     * additional memory.  For non-square matrices, internally a temporary matrix is declared and
     * {@link #transpose(org.ejml.data.CDenseMatrix64F, org.ejml.data.CDenseMatrix64F)} is invoked.
     * </p>
     *
     * @param mat The matrix that is to be transposed. Modified.
     */
    public static void transpose( CDenseMatrix64F mat ) {
        if( mat.numCols == mat.numRows ){
            CTransposeAlgs.square(mat);
        } else {
            CDenseMatrix64F b = new CDenseMatrix64F(mat.numCols,mat.numRows);
            transpose(mat, b);
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
    public static CDenseMatrix64F transpose( CDenseMatrix64F input , CDenseMatrix64F output )
    {
        if( output == null ) {
            output = new CDenseMatrix64F(input.numCols,input.numRows);
        } else if( input.numCols != output.numRows || input.numRows != output.numCols ) {
            throw new IllegalArgumentException("Input and output shapes are not compatible");
        }

        CTransposeAlgs.standard(input,output);

        return output;
    }

    public static boolean invert( CDenseMatrix64F input , CDenseMatrix64F output )
    {
        return false;
    }

    public static boolean invert( CDenseMatrix64F a , CDenseMatrix64F b , CDenseMatrix64F x )
    {
        return false;
    }

    public static boolean det( CDenseMatrix64F input , Complex64F output )
    {
        return false;
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
    public static void elementMultiply( CD1Matrix64F input , double real , double imaginary, CD1Matrix64F output )
    {
        if( input.numCols != output.numCols || input.numRows != output.numRows ) {
            throw new IllegalArgumentException("The 'input' and 'output' matrices do not have compatible dimensions");
        }

        int N = input.getDataLength();
        for (int i = 0; i < N; i += 2 ) {
            double inReal = input.data[i];
            double intImag = input.data[i+1];

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
    public static void elementDivide( CD1Matrix64F input , double real , double imaginary, CD1Matrix64F output )
    {
        if( input.numCols != output.numCols || input.numRows != output.numRows ) {
            throw new IllegalArgumentException("The 'input' and 'output' matrices do not have compatible dimensions");
        }

        double norm = real*real + imaginary*imaginary;

        int N = input.getDataLength();
        for (int i = 0; i < N; i += 2 ) {
            double inReal = input.data[i];
            double inImag = input.data[i+1];

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
    public static void elementDivide( double real , double imaginary, CD1Matrix64F input , CD1Matrix64F output )
    {
        if( input.numCols != output.numCols || input.numRows != output.numRows ) {
            throw new IllegalArgumentException("The 'input' and 'output' matrices do not have compatible dimensions");
        }

        int N = input.getDataLength();
        for (int i = 0; i < N; i += 2 ) {
            double inReal = input.data[i];
            double inImag = input.data[i+1];

            double norm = inReal*inReal + inImag*inImag;

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
    public static double elementMaxMagnitude2( CD1Matrix64F a ) {
        final int size = a.getDataLength();

        double max = 0;
        for( int i = 0; i < size; ) {
            double real = a.data[i++];
            double imaginary = a.data[i++];

            double m = real*real + imaginary*imaginary;

            if( m > max ) {
                max = m;
            }
        }

        return max;
    }

}
