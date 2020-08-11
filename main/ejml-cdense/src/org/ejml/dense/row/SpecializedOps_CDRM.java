/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.Complex_F32;
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.mult.VectorVectorMult_CDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;

/**
 * @author Peter Abeles
 */
public class SpecializedOps_CDRM {

    /**
     * <p>
     * Creates a reflector from the provided vector.<br>
     * <br>
     * Q = I - &gamma; u u<sup>T</sup><br>
     * &gamma; = 2/||u||<sup>2</sup>
     * </p>
     *
     * @param u A vector. Not modified.
     * @return An orthogonal reflector.
     */
    public static CMatrixRMaj createReflector(CMatrixRMaj u ) {
        if( !MatrixFeatures_CDRM.isVector(u))
            throw new IllegalArgumentException("u must be a vector");

        float norm = NormOps_CDRM.normF(u);
        float gamma = -2.0f/(norm*norm);

        CMatrixRMaj Q = CommonOps_CDRM.identity(u.getNumElements());

        CommonOps_CDRM.multAddTransB(gamma,0,u,u,Q);

        return Q;
    }

    /**
     * <p>
     * Creates a reflector from the provided vector and gamma.<br>
     * <br>
     * Q = I - &gamma; u u<sup>H</sup><br>
     * </p>
     *
     * @param u A vector.  Not modified.
     * @param gamma To produce a reflector gamma needs to be equal to 2/||u||.
     * @return An orthogonal reflector.
     */
    public static CMatrixRMaj createReflector(CMatrixRMaj u , float gamma) {
        if( !MatrixFeatures_CDRM.isVector(u))
            throw new IllegalArgumentException("u must be a vector");

        CMatrixRMaj Q = CommonOps_CDRM.identity(u.getNumElements());
        CommonOps_CDRM.multAddTransB(-gamma,0,u,u,Q);

        return Q;
    }

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
    public static CMatrixRMaj pivotMatrix(CMatrixRMaj ret, int pivots[], int numPivots, boolean transposed ) {

        if( ret == null ) {
            ret = new CMatrixRMaj(numPivots, numPivots);
        } else {
            if( ret.numCols != numPivots || ret.numRows != numPivots )
                throw new IllegalArgumentException("Unexpected matrix dimension");
            CommonOps_CDRM.fill(ret, 0,0);
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
    public static float elementDiagMaxMagnitude2(CMatrixRMaj a) {
        final int size = Math.min(a.numRows,a.numCols);

        int rowStride = a.getRowStride();
        float max = 0;
        for( int i = 0; i < size; i++ ) {
            int index = i*rowStride + i*2;

            float real = a.data[index];
            float imaginary = a.data[index+1];

            float m = real*real + imaginary*imaginary;

            if( m > max ) {
                max = m;
            }
        }

        return max;
    }

    /**
     * Computes the quality of a triangular matrix, where the quality of a matrix
     * is defined in {@link LinearSolverDense#quality()}.  In
     * this situation the quality is the magnitude of the product of
     * each diagonal element divided by the magnitude of the largest diagonal element.
     * If all diagonal elements are zero then zero is returned.
     *
     * @return the quality of the system.
     */
    public static float qualityTriangular(CMatrixRMaj T)
    {
        int N = Math.min(T.numRows,T.numCols);

        float max = elementDiagMaxMagnitude2(T);

        if( max == 0.0f )
            return 0.0f;

        max = (float)Math.sqrt(max);
        int rowStride = T.getRowStride();

        float qualityR = 1.0f;
        float qualityI = 0.0f;

        for( int i = 0; i < N; i++ ) {
            int index = i*rowStride + i*2;

            float real = T.data[index]/max;
            float imaginary = T.data[index]/max;

            float r = qualityR*real - qualityI*imaginary;
            float img = qualityR*imaginary + real*qualityI;

            qualityR = r;
            qualityI = img;
        }

        return (float)Math.sqrt(qualityR*qualityR + qualityI*qualityI);
    }

    /**
     * Q = I - gamma*u*u<sup>H</sup>
     */
    public static CMatrixRMaj householder(CMatrixRMaj u , float gamma ) {
        int N = u.getDataLength()/2;
        // u*u^H
        CMatrixRMaj uut = new CMatrixRMaj(N,N);
        VectorVectorMult_CDRM.outerProdH(u, u, uut);
        // foo = -gamma*u*u^H
        CommonOps_CDRM.elementMultiply(uut,-gamma,0,uut);

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
    public static CMatrixRMaj householderVector(CMatrixRMaj x ) {
        CMatrixRMaj u = x.copy();

        float max = CommonOps_CDRM.elementMaxAbs(u);

        CommonOps_CDRM.elementDivide(u, max, 0, u);

        float nx = NormOps_CDRM.normF(u);
        Complex_F32 c = new Complex_F32();
        u.get(0,0,c);

        float realTau,imagTau;

        if( c.getMagnitude() == 0 ) {
            realTau = nx;
            imagTau = 0;
        } else {
            realTau = c.real/c.getMagnitude()*nx;
            imagTau = c.imaginary/c.getMagnitude()*nx;
        }

        u.set(0,0,c.real + realTau,c.imaginary + imagTau);
        CommonOps_CDRM.elementDivide(u,u.getReal(0,0),u.getImag(0,0),u);

        return u;
    }
}
