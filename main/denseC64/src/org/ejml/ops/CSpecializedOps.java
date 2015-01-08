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

import org.ejml.alg.dense.mult.CVectorVectorMult;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;

/**
 * @author Peter Abeles
 */
public class CSpecializedOps {
    /**
     * <p>
     * Creates a pivot matrix that exchanges the rows in a matrix:
     * <br>
     * A' = P*A<br>
     * </p>
     * <p>
     * For example, if element 0 in 'pivots' is 2 then the first row in A' will be the 3rd row in A.
     * </p>
     *
     * @param ret If null then a new matrix is declared otherwise the results are written to it.  Is modified.
     * @param pivots Specifies the new order of rows in a matrix.
     * @param numPivots How many elements in pivots are being used.
     * @param transposed If the transpose of the matrix is returned.
     * @return A pivot matrix.
     */
    public static CDenseMatrix64F pivotMatrix(CDenseMatrix64F ret, int pivots[], int numPivots, boolean transposed ) {

        if( ret == null ) {
            ret = new CDenseMatrix64F(numPivots, numPivots);
        } else {
            if( ret.numCols != numPivots || ret.numRows != numPivots )
                throw new IllegalArgumentException("Unexpected matrix dimension");
            CCommonOps.fill(ret, 0,0);
        }

        if( transposed ) {
            for( int i = 0; i < numPivots; i++ ) {
                ret.set(pivots[i],i,1,0);
            }
        } else {
            for( int i = 0; i < numPivots; i++ ) {
                ret.set(i,pivots[i],1,0);
            }
        }

        return ret;
    }

    /**
     * <p>
     * Returns the magnitude squared of the complex element along the diagonal with the largest magnitude<br>
     * <br>
     * Max{ |a<sub>ij</sub>|^2 } for all i and j<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The max magnitude squared
     */
    public static double elementDiagMaxMagnitude2(CDenseMatrix64F a) {
        final int size = Math.min(a.numRows,a.numCols);

        int rowStride = a.getRowStride();
        double max = 0;
        for( int i = 0; i < size; i++ ) {
            int index = i*rowStride + i*2;

            double real = a.data[index];
            double imaginary = a.data[index+1];

            double m = real*real + imaginary*imaginary;

            if( m > max ) {
                max = m;
            }
        }

        return max;
    }

    /**
     * Computes the quality of a triangular matrix, where the quality of a matrix
     * is defined in {@link org.ejml.interfaces.linsol.LinearSolver#quality()}.  In
     * this situation the quality is the magnitude of the product of
     * each diagonal element divided by the magnitude of the largest diagonal element.
     * If all diagonal elements are zero then zero is returned.
     *
     * @return the quality of the system.
     */
    public static double qualityTriangular(CDenseMatrix64F T)
    {
        int N = Math.min(T.numRows,T.numCols);

        double max = elementDiagMaxMagnitude2(T);

        if( max == 0.0d )
            return 0.0d;

        max = Math.sqrt(max);
        int rowStride = T.getRowStride();

        double qualityR = 1.0;
        double qualityI = 0.0;

        for( int i = 0; i < N; i++ ) {
            int index = i*rowStride + i*2;

            double real = T.data[index]/max;
            double imaginary = T.data[index]/max;

            double r = qualityR*real - qualityI*imaginary;
            double img = qualityR*imaginary + real*qualityI;

            qualityR = r;
            qualityI = img;
        }

        return Math.sqrt(qualityR*qualityR + qualityI*qualityI);
    }

    /**
     * Q = I - gamma*u*u<sup>H</sup>
     */
    public static CDenseMatrix64F householder( CDenseMatrix64F u , double gamma ) {
        int N = u.getDataLength()/2;
        // u*u^H
        CDenseMatrix64F uut = new CDenseMatrix64F(N,N);
        CVectorVectorMult.outerProdH(u, u, uut);
        // foo = -gamma*u*u^H
        CCommonOps.elementMultiply(uut,-gamma,0,uut);

        // I + foo
        for (int i = 0; i < N; i++) {
            int index = (i*uut.numCols+i)*2;
            uut.data[index] = 1 + uut.data[index];
        }

        return uut;
    }

    /**
     * Computes the householder vector used in QR decomposition.
     *
     * u = x / max(x)
     * u(0) = u(0) + |u|
     * u = u / u(0)
     *
     * @param x Input vector.  Unmodified.
     * @return The found householder reflector vector
     */
    public static CDenseMatrix64F householderVector( CDenseMatrix64F x ) {
        CDenseMatrix64F u = x.copy();

        double max = CCommonOps.elementMaxAbs(u);

        CCommonOps.elementDivide(u, max, 0, u);

        double nx = CNormOps.normF(u);
        Complex64F c = new Complex64F();
        u.get(0,0,c);

        double realTau,imagTau;

        if( c.getMagnitude() == 0 ) {
            realTau = nx;
            imagTau = 0;
        } else {
            realTau = c.real/c.getMagnitude()*nx;
            imagTau = c.imaginary/c.getMagnitude()*nx;
        }

        u.set(0,0,c.real + realTau,c.imaginary + imagTau);
        CCommonOps.elementDivide(u,u.getReal(0,0),u.getImaginary(0,0),u);

        return u;
    }
}
