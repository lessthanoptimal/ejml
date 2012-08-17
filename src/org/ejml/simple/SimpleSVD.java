/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.simple;

import org.ejml.UtilEjml;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.factory.SingularValueDecomposition;
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
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class SimpleSVD<T extends SimpleMatrix> {

    private SingularValueDecomposition<DenseMatrix64F> svd;
    private T U;
    private T W;
    private T V;

    private DenseMatrix64F mat;

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
        return SimpleMatrix.wrap(SingularOps.nullSpace(svd,null,UtilEjml.EPS));
    }

    /**
     * Returns the specified singular value.
     *
     * @param index Which singular value is to be returned.
     * @return A singular value.
     */
    public double getSingleValue( int index ) {
        return svd.getSingularValues()[index];
    }

    /**
     * Returns the rank of the decomposed matrix.
     *
     * @see SingularOps#rank(org.ejml.factory.SingularValueDecomposition, double)
     *
     * @return The matrix's rank
     */
    public int rank() {
        return SingularOps.rank(svd,10.0* UtilEjml.EPS);
    }

    /**
     * The nullity of the decomposed matrix.
     *
     * @see SingularOps#nullity(org.ejml.factory.SingularValueDecomposition, double)
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
