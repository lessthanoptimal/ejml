/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition;

import org.ejml.EjmlParameters;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionBasic;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionBlock;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionLDL;
import org.ejml.alg.dense.decomposition.eig.SwitchingEigenDecomposition;
import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt;
import org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholderColumn;
import org.ejml.alg.dense.decomposition.svd.SvdImplicitQrDecompose;


/**
 * Selecting which specific implementation of a decomposition to use can be difficult unless one
 * has an intimate understanding of each of them works.  This class provides static functions
 * that can be used to create a new instance of a decomposition that should work well for almost
 * all matrices and is reasonably fast.
 *
 * @author Peter Abeles
 */
public class DecompositionFactory {

    /**
     * <p> If you don't know which Cholesky algorithm to use, call this function to select what
     * is most likely the best one for you.
     * </p>
     * <p>
     * Creates a new instance of a CholeskyDecomposition algorithm.  It selects the best
     * algorithm depending on the size of the largest matrix it might decompose.
     * </p>
     * @param widthMax The maximum width of a matrix that can be processed.
     * @param decomposeOrig Should it decompose the matrix that is passed in or declare a new one?
     * @param lower should a lower or upper triangular matrix be used.
     * @return A new CholeskyDecomposition.
     */
    public static CholeskyDecomposition chol( int widthMax , boolean decomposeOrig, boolean lower )
    {
        if( widthMax >= EjmlParameters.SWITCH_BLOCK_CHOLESKY && lower ) {
            return new CholeskyDecompositionBlock(decomposeOrig, EjmlParameters.BLOCK_WIDTH);
        } else {
            return new CholeskyDecompositionBasic(decomposeOrig,lower);
        }
    }

    public static CholeskyDecomposition chol() {
        return chol(10,false,true);
    }

    /**
     * Creates a {@link CholeskyDecompositionLDL} decomposition. Cholesky LDL is a variant of
     * {@link CholeskyDecomposition} that avoids need to compute the square root.
     *
     * @return CholeskyDecompositionLDL
     */
    public static CholeskyDecompositionLDL cholLDL() {
        return new CholeskyDecompositionLDL();
    }

    /**
     * Returns a new instance of the Lower Upper (LU) decomposition.
     *
     * @return LUDecomposition
     */
    public static LUDecomposition lu() {
        return new LUDecompositionAlt();
    }

    /**
     * Returns a new instance of the Singular Value Decomposition (SVD).
     *
     * @return SingularValueDecomposition
     */
    public static SingularValueDecomposition svd() {
        return new SvdImplicitQrDecompose(true);
    }

    public static SingularValueDecomposition svd( boolean needU , boolean needV , boolean compact ) {
        return new SvdImplicitQrDecompose(compact);
    }

    /**
     * Returns a new instance of the QR decomposition.
     *
     * @return QRDecomposition
     */
    public static QRDecomposition qr() {
        return new QRDecompositionHouseholderColumn();
    }

    /**
     * Returns a new eigenvalue decomposition.  If it is known before hand if the matrix
     * is symmetric or not then a call should be made directly to either {@link org.ejml.ops.EigenOps#decompositionGeneral(boolean)}
     * or {@link org.ejml.ops.EigenOps#decompositionSymmetric(boolean)}.  That will avoid unnecessary checks.
     *
     * @return A new EigenDecomposition.
     */
    public static EigenDecomposition eig() {
        return new SwitchingEigenDecomposition();
    }

    public static EigenDecomposition eig( boolean needVectors ) {
        return new SwitchingEigenDecomposition(needVectors,1e-8);
    }
}
