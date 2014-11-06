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

package org.ejml.simple;

import org.ejml.UtilEjml;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.ops.SingularOps;


/**
 * <p>
 * Wrapper around SVD for simple matrix.  See {@link SingularValueDecomposition} for more details.
 * </p>
 * <p>
 * SVD is defined as the following decomposition:<br>
 * <div align=center> A = U * W * V <sup>T</sup> </div><br>
 * where A is m by n, and U and V are orthogonal matrices, and  W is a diagonal matrix
 * </p>
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
public class SimpleSVD<T extends SimpleMatrix> {

    private SingularValueDecomposition<DenseMatrix64F> svd;
    private T U;
    private T W;
    private T V;

    private DenseMatrix64F mat;

    // tolerance for singular values
    double tol;

    public SimpleSVD( DenseMatrix64F mat , boolean compact ) {
        this.mat = mat;
        svd = DecompositionFactory.svd(mat.numRows,mat.numCols,true,true,compact);
        if( !svd.decompose(mat) )
            throw new RuntimeException("Decomposition failed");
        U = (T)SimpleMatrix.wrap(svd.getU(null,false));
        W = (T)SimpleMatrix.wrap(svd.getW(null));
        V = (T)SimpleMatrix.wrap(svd.getV(null,false));

        // order singular values from largest to smallest
        SingularOps.descendingOrder(U.getMatrix(),false,W.getMatrix(),V.getMatrix(),false);

        tol = SingularOps.singularThreshold(svd);

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
    public double quality() {
        return DecompositionFactory.quality(mat,U.getMatrix(),W.getMatrix(),V.transpose().getMatrix());
    }

    /**
     * Computes the null space from an SVD.  For more information see {@link SingularOps#nullSpace}.
     * @return Null space vector.
     */
    public SimpleMatrix nullSpace() {
        // TODO take advantage of the singular values being ordered already
        return SimpleMatrix.wrap(SingularOps.nullSpace(svd,null,tol));
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
     * Returns the rank of the decomposed matrix.
     *
     * @see SingularOps#rank(org.ejml.interfaces.decomposition.SingularValueDecomposition, double)
     *
     * @return The matrix's rank
     */
    public int rank() {
        return SingularOps.rank(svd,tol);
    }

    /**
     * The nullity of the decomposed matrix.
     *
     * @see SingularOps#nullity(org.ejml.interfaces.decomposition.SingularValueDecomposition, double)
     *
     * @return The matrix's nullity
     */
    public int nullity() {
        return SingularOps.nullity(svd,10.0*UtilEjml.EPS);
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
