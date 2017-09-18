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

package org.ejml.simple;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.Matrix;
import org.ejml.dense.row.SingularOps_DDRM;
import org.ejml.dense.row.SingularOps_FDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_FDRM;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F32;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F64;


/**
 * <p>
 * Wrapper around SVD for simple matrix.  See {@link SingularValueDecomposition} for more details.
 * </p>
 * SVD is defined as the following decomposition:<br>
 * <center> A = U * W * V <sup>T</sup> </center>
 * where A is m by n, and U and V are orthogonal matrices, and  W is a diagonal matrix
 *
 * <p>
 * Tolerance for singular values is
 * Math.max(mat.numRows,mat.numCols) * W.get(0,0) * UtilEjml.EPS;
 * where W.get(0,0) is the largest singular value.
 * </p>
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class SimpleSVD<T extends SimpleBase> {

    private SingularValueDecomposition svd;
    private T U;
    private T W;
    private T V;

    private Matrix mat;
    final boolean is64;

    // tolerance for singular values
    double tol;

    public SimpleSVD( Matrix mat , boolean compact ) {
        this.mat = mat;
        this.is64 = mat instanceof DMatrixRMaj;
        if( is64 ) {
            DMatrixRMaj m = (DMatrixRMaj)mat;
            svd = DecompositionFactory_DDRM.svd(m.numRows,m.numCols,true,true,compact);
        } else {
            FMatrixRMaj m = (FMatrixRMaj)mat;
            svd = DecompositionFactory_FDRM.svd(m.numRows,m.numCols,true,true,compact);
        }

        if( !svd.decompose(mat) )
            throw new RuntimeException("Decomposition failed");
        U = (T)SimpleMatrix.wrap(svd.getU(null,false));
        W = (T)SimpleMatrix.wrap(svd.getW(null));
        V = (T)SimpleMatrix.wrap(svd.getV(null,false));

        // order singular values from largest to smallest
        if( is64 ) {
            SingularOps_DDRM.descendingOrder(
                    (DMatrixRMaj)U.getMatrix(), false, (DMatrixRMaj)W.getMatrix(),
                    (DMatrixRMaj)V.getMatrix(), false);
            tol = SingularOps_DDRM.singularThreshold((SingularValueDecomposition_F64)svd);
        } else {
            SingularOps_FDRM.descendingOrder(
                    (FMatrixRMaj)U.getMatrix(), false, (FMatrixRMaj)W.getMatrix(),
                    (FMatrixRMaj)V.getMatrix(), false);
            tol = SingularOps_FDRM.singularThreshold((SingularValueDecomposition_F32)svd);
        }

    }

    /**
     * <p>
     * Returns the orthogonal 'U' matrix.
     * </p>
     *
     * @return An orthogonal m by m matrix.
     */
    public T getU() {
        return U;
    }

    /**
     * Returns a diagonal matrix with the singular values.  The singular values are ordered
     * from largest to smallest.
     *
     * @return Diagonal matrix with singular values along the diagonal.
     */
    public T getW() {
        return W;
    }

    /**
     * <p>
     * Returns the orthogonal 'V' matrix.
     * </p>
     *
     * @return An orthogonal n by n matrix.
     */
    public T getV() {
        return V;
    }

    /**
     * <p>
     * Computes the quality of the computed decomposition.  A value close to or less than 1e-15
     * is considered to be within machine precision.
     * </p>
     *
     * <p>
     * This function must be called before the original matrix has been modified or else it will
     * produce meaningless results.
     * </p>
     *
     * @return Quality of the decomposition.
     */
    public /**/double quality() {
        if( is64 ) {
            return DecompositionFactory_DDRM.quality((DMatrixRMaj)mat, (DMatrixRMaj)U.getMatrix(),
                    (DMatrixRMaj)W.getMatrix(), (DMatrixRMaj)V.transpose().getMatrix());
        } else {
            return DecompositionFactory_FDRM.quality((FMatrixRMaj)mat, (FMatrixRMaj)U.getMatrix(),
                    (FMatrixRMaj)W.getMatrix(), (FMatrixRMaj)V.transpose().getMatrix());        }
    }

    /**
     * Computes the null space from an SVD.  For more information see {@link SingularOps_DDRM#nullSpace}.
     * @return Null space vector.
     */
    public SimpleMatrix nullSpace() {
        // TODO take advantage of the singular values being ordered already
        if( is64 ) {
            return SimpleMatrix.wrap(SingularOps_DDRM.nullSpace((SingularValueDecomposition_F64)svd, null, tol));
        } else {
            return SimpleMatrix.wrap(SingularOps_FDRM.nullSpace((SingularValueDecomposition_F32)svd, null, (float)tol));
        }
    }

    /**
     * Returns the specified singular value.
     *
     * @param index Which singular value is to be returned.
     * @return A singular value.
     */
    public double getSingleValue( int index ) {
        return W.get(index,index);
    }

    /**
     * Returns an array of all the singular values
     */
    public double[] getSingularValues() {
        double ret[] = new double[W.numCols()];

        for (int i = 0; i < ret.length; i++) {
            ret[i] = getSingleValue(i);
        }
        return ret;
    }

    /**
     * Returns the rank of the decomposed matrix.
     *
     * @see SingularOps_DDRM#rank(SingularValueDecomposition_F64, double)
     *
     * @return The matrix's rank
     */
    public int rank() {
        if( is64 ) {
            return SingularOps_DDRM.rank((SingularValueDecomposition_F64)svd, tol);
        } else {
            return SingularOps_FDRM.rank((SingularValueDecomposition_F32)svd, (float)tol);
        }
    }

    /**
     * The nullity of the decomposed matrix.
     *
     * @see SingularOps_DDRM#nullity(SingularValueDecomposition_F64, double)
     *
     * @return The matrix's nullity
     */
    public int nullity() {
        if( is64 ) {
            return SingularOps_DDRM.nullity((SingularValueDecomposition_F64)svd, 10.0 * UtilEjml.EPS);
        } else {
            return SingularOps_FDRM.nullity((SingularValueDecomposition_F32)svd, 5.0f * UtilEjml.F_EPS);
        }
    }

    /**
     * Returns the underlying decomposition that this is a wrapper around.
     *
     * @return SingularValueDecomposition
     */
    public SingularValueDecomposition getSVD() {
        return svd;
    }
}
