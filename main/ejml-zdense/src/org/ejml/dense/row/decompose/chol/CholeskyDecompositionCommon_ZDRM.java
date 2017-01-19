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

package org.ejml.dense.row.decompose.chol;


import org.ejml.data.Complex_F64;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.decompose.UtilDecompositons_ZDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;


/**
 *
 * <p>
 * This is an abstract class for a Cholesky decomposition.  It provides the solvers, but the actual
 * decomposition is provided in other classes.
 * </p>
 *
 * @see CholeskyDecomposition_F64
 * @author Peter Abeles
 */
public abstract class CholeskyDecompositionCommon_ZDRM
        implements CholeskyDecomposition_F64<ZMatrixRMaj> {

    // width and height of the matrix
    protected int n;

    // the decomposed matrix
    protected ZMatrixRMaj T;
    protected double[] t;


    // is it a lower triangular matrix or an upper triangular matrix
    protected boolean lower;

    // storage for the determinant
    protected Complex_F64 det = new Complex_F64();

    /**
     * Specifies if a lower or upper variant should be constructed.
     *
     * @param lower should a lower or upper triangular matrix be used.
     */
    public CholeskyDecompositionCommon_ZDRM(boolean lower) {
        this.lower = lower;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLower() {
        return lower;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean decompose( ZMatrixRMaj mat ) {
        if( mat.numRows != mat.numCols ) {
            throw new IllegalArgumentException("Must be a square matrix.");
        }

        n = mat.numRows;

        T = mat;
        t = T.data;

        if(lower) {
            return decomposeLower();
        } else {
            return decomposeUpper();
        }
    }

    @Override
    public boolean inputModified() {
        return true;
    }

    /**
     * Performs an lower triangular decomposition.
     *
     * @return true if the matrix was decomposed.
     */
    protected abstract boolean decomposeLower();

    /**
     * Performs an upper triangular decomposition.
     *
     * @return true if the matrix was decomposed.
     */
    protected abstract boolean decomposeUpper();

    @Override
    public ZMatrixRMaj getT(ZMatrixRMaj T ) {
        // write the values to T
        if( lower ) {
            T = UtilDecompositons_ZDRM.checkZerosUT(T,n,n);
            for( int i = 0; i < n; i++ ) {
                int index = i*n*2;
                for( int j = 0; j <= i; j++ ) {
                    T.data[index] = this.T.data[index];
                    index++;
                    T.data[index] = this.T.data[index];
                    index++;
                }
            }
        } else {
            T = UtilDecompositons_ZDRM.checkZerosLT(T,n,n);
            for( int i = 0; i < n; i++ ) {
                int index = (i*n + i)*2;
                for( int j = i; j < n; j++ ) {
                    T.data[index] = this.T.data[index];
                    index++;
                    T.data[index] = this.T.data[index];
                    index++;
                }
            }
        }

        return T;
    }

    /**
     * Returns the raw decomposed matrix.
     *
     * @return A lower or upper triangular matrix.
     */
    public ZMatrixRMaj _getT() {
        return T;
    }

    @Override
    public Complex_F64 computeDeterminant() {
        double prod = 1;

        // take advantage of the diagonal elements all being real
        int total = n*n*2;
        for( int i = 0; i < total; i += 2*(n + 1) ) {
            prod *= t[i];
        }

        det.real = prod*prod;
        det.imaginary = 0;

        return det;
    }
}