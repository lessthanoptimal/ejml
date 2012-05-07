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

package org.ejml.alg.dense.decomposition;

import org.ejml.EjmlParameters;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionBlock;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionBlock64;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionLDL;
import org.ejml.alg.dense.decomposition.eig.SwitchingEigenDecomposition;
import org.ejml.alg.dense.decomposition.eig.SymmetricQRAlgorithmDecomposition;
import org.ejml.alg.dense.decomposition.eig.WatchedDoubleStepQRDecomposition;
import org.ejml.alg.dense.decomposition.hessenberg.TridiagonalDecompositionBlock;
import org.ejml.alg.dense.decomposition.hessenberg.TridiagonalDecompositionHouseholder;
import org.ejml.alg.dense.decomposition.hessenberg.TridiagonalSimilarDecomposition;
import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt;
import org.ejml.alg.dense.decomposition.qr.QRColPivDecompositionHouseholderColumn;
import org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholderColumn;
import org.ejml.alg.dense.decomposition.svd.SvdImplicitQrDecompose;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.Matrix64F;
import org.ejml.ops.EigenOps;
import org.ejml.ops.SpecializedOps;
import org.ejml.simple.SimpleMatrix;


/**
 * <p>
 * Contains operations related to creating and evaluating the quality of common matrix decompositions.
 * </p>
 *
 * <p>
 * In general this is the best place to create matrix decompositions from since directly calling the
 * algorithm is error prone since it require in depth knowledge of how the algorithm operators.  The exact
 * implementations created is subject to change as newer, faster and more accurate implementations are added.
 * </p>
 *
 * <p>
 * Several functions are also provided to evaluate the quality of a decomposition.  This is provided
 * as a way to sanity check a decomposition.  Often times a significant error in a decomposition will
 * result in a poor (larger) quality value. Typically a quality value of around 1e-15 means it is within
 * machine precision.
 * </p>
 *
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
     * @param widthWidth The matrix size that the decomposition should be optimized for.
     * @param lower should a lower or upper triangular matrix be used.
     * @return A new CholeskyDecomposition.
     */
    public static CholeskyDecomposition<DenseMatrix64F> chol( int widthWidth , boolean lower )
    {
        if( widthWidth < EjmlParameters.SWITCH_BLOCK64_CHOLESKY ) {
            return new CholeskyDecompositionInner(lower);
        } else if( EjmlParameters.MEMORY == EjmlParameters.MemoryUsage.FASTER ){
            return new CholeskyDecompositionBlock64(lower);
        } else {
            return new CholeskyDecompositionBlock(EjmlParameters.BLOCK_WIDTH_CHOL);
        }
    }

    /**
     * Creates a {@link CholeskyDecompositionLDL} decomposition. Cholesky LDL is a variant of
     * {@link CholeskyDecomposition} that avoids need to compute the square root.
     *
     * @param matrixWidth The matrix size that the decomposition should be optimized for.
     * @return CholeskyDecompositionLDL
     */
    public static CholeskyDecompositionLDL cholLDL( int matrixWidth) {
        return new CholeskyDecompositionLDL();
    }

    /**
     * Returns a new instance of the Lower Upper (LU) decomposition.
     *
     * @parm matrixWidth The matrix size that the decomposition should be optimized for.
     * @return LUDecomposition
     */
    public static LUDecomposition<DenseMatrix64F> lu( int matrixWidth ) {
        return new LUDecompositionAlt();
    }

    /**
     * Returns a new instance of a SingularValueDecomposition which will compute
     * the full decomposition..
     *
     * @param numRows The number of rows that the decomposition is optimized for.
     * @param numCols The number of columns that the decomposition is optimized for.
     * @return SingularValueDecomposition
     */
    public static SingularValueDecomposition<DenseMatrix64F> svd( int numRows , int numCols ) {
        return new SvdImplicitQrDecompose(false,true,true);
    }

    /**
     * Returns a new instance of a SingularValueDecomposition which can be configured to compute
     * U and V matrices or not, be in compact form.
     *
     * @param numRows The number of rows that the decomposition is optimized for.
     * @param numCols The number of columns that the decomposition is optimized for.
     * @param needU Should it compute the U matrix.
     * @param needV Should it compute the V matrix.
     * @param compact Should it compute the SVD in compact form.
     * @return
     */
    public static SingularValueDecomposition<DenseMatrix64F> svd( int numRows , int numCols , boolean needU , boolean needV , boolean compact ) {
        return new SvdImplicitQrDecompose(compact,needU,needV);
    }

    /**
     * Returns a new instance of the QR decomposition.
     *
     * @param numRows The number of rows that the decomposition is optimized for.
     * @param numCols The number of columns that the decomposition is optimized for.
     * @return QRDecomposition
     */
    public static QRDecomposition<DenseMatrix64F> qr( int numRows , int numCols ) {
        return new QRDecompositionHouseholderColumn();
    }

    /**
     * <p>
     * Returns a new instance of QR decomposition with column pivoting.<br>
     * A*P = Q*R<br>
     * where A is the input matrix, and P is the pivot matrix.
     * </p>
     *
     * @param numRows The number of rows that the decomposition is optimized for.
     * @param numCols The number of columns that the decomposition is optimized for.
     * @return QRPDecomposition
     */
    public static QRPDecomposition<DenseMatrix64F> qrp( int numRows , int numCols ) {
        return new QRColPivDecompositionHouseholderColumn();
    }

    /**
     * Returns a new eigenvalue decomposition.  If it is known before hand if the matrix
     * is symmetric or not then a call should be made directly to either {@link #eigGeneral(int,boolean)}
     * or {@link #eigSymm(int, boolean)}.  That will avoid unnecessary checks.
     *
     * @param matrixWidth The matrix size that the decomposition should be optimized for.
     * @return A new EigenDecomposition.
     */
    public static EigenDecomposition<DenseMatrix64F> eig( int matrixWidth ) {
        return new SwitchingEigenDecomposition(matrixWidth);
    }

    /**
     * Same as {@link #eig(int)} but can turn on and off computing eigen vectors
     *
     * @param matrixWidth The matrix size that the decomposition should be optimized for.
     * @param needVectors Should eigenvectors be computed or not.
     * @return A new EigenDecomposition
     */
    public static EigenDecomposition<DenseMatrix64F> eig( int matrixWidth , boolean needVectors ) {
        return new SwitchingEigenDecomposition(matrixWidth,needVectors,1e-8);
    }

    /**
     * Creates a new EigenDecomposition that will work with any matrix.
     *
     * @return EVD for any matrix.
     * @param computeVectors Should it compute the eigenvectors or just eigenvalues.
     */
    public static EigenDecomposition<DenseMatrix64F> eigGeneral( int matrixSize , boolean computeVectors ) {
        return new WatchedDoubleStepQRDecomposition(computeVectors);
    }

    /**
     * Creates a new EigenDecomposition that will only work with symmetric matrices.  This
     * will run much faster and be more accurate than the general purpose algorithm.
     *
     * @return EVD for symmetric matrices.
     * @param matrixWidth The number of rows/columns in the matrix.  Used to select the best algorithms.
     * @param computeVectors Should it compute the eigenvectors or just eigenvalues.
     */
    public static EigenDecomposition<DenseMatrix64F> eigSymm( int matrixWidth , boolean computeVectors ) {
        TridiagonalSimilarDecomposition<DenseMatrix64F> decomp = DecompositionFactory.tridiagonal(matrixWidth);

        return new SymmetricQRAlgorithmDecomposition(decomp,computeVectors);
    }

    /**
     * <p>
     * Computes a metric which measures the the quality of a singular value decomposition.  If a
     * value is returned that is close to or smaller than 1e-15 then it is within machine precision.
     * </p>
     *
     * <p>
     * SVD quality is defined as:<br>
     * <br>
     * Quality = || A - U W V<sup>T</sup>|| / || A || <br>
     * where A is the original matrix , U W V is the decomposition, and ||A|| is the norm-f of A.
     * </p>
     *
     * @param orig The original matrix which was decomposed. Not modified.
     * @param svd The decomposition after processing 'orig'. Not modified.
     * @return The quality of the decomposition.
     */
    public static double quality( DenseMatrix64F orig , SingularValueDecomposition<DenseMatrix64F> svd )
    {
        return quality(orig,svd.getU(false),svd.getW(null),svd.getV(true));
    }

    public static double quality( DenseMatrix64F orig , DenseMatrix64F U , DenseMatrix64F W , DenseMatrix64F Vt )
    {
        SimpleMatrix _U = SimpleMatrix.wrap(U);
        SimpleMatrix _W = SimpleMatrix.wrap(W);
        SimpleMatrix _Vt = SimpleMatrix.wrap(Vt);

        SimpleMatrix foundA = _U.mult(_W).mult(_Vt);

        return SpecializedOps.diffNormF(orig,foundA.getMatrix())/foundA.normF();
    }

    /**
     * <p>
     * Computes a metric which measures the the quality of an eigen value decomposition.  If a
     * value is returned that is close to or smaller than 1e-15 then it is within machine precision.
     * </p>
     * <p>
     * EVD quality is defined as:<br>
     * <br>
     * Quality = ||A*V - V*D|| / ||A*V||.
     *  </p>
     *
     * @param orig The original matrix. Not modified.
     * @param eig EVD of the original matrix. Not modified.
     * @return The quality of the decomposition.
     */
    public static double quality( DenseMatrix64F orig , EigenDecomposition<DenseMatrix64F> eig )
    {
        SimpleMatrix A = SimpleMatrix.wrap(orig);
        SimpleMatrix V = SimpleMatrix.wrap(EigenOps.createMatrixV(eig));
        SimpleMatrix D = SimpleMatrix.wrap(EigenOps.createMatrixD(eig));

        SimpleMatrix L = A.mult(V);
        SimpleMatrix R = V.mult(D);

        SimpleMatrix diff = L.minus(R);

        double top = diff.normF();
        double bottom = L.normF();

        double error = top/bottom;

        return error;
    }

    /**
     * Checks to see if the passed in tridiagonal decomposition is of the appropriate type
     * for the matrix of the provided size.  Returns the same instance or a new instance.
     */
    public static TridiagonalSimilarDecomposition<DenseMatrix64F> tridiagonal(  int matrixWidth ) {
        if( matrixWidth >= 1800 ) {
            return new TridiagonalDecompositionBlock();
        } else {
            return new TridiagonalDecompositionHouseholder();
        }
    }

    /**
     * Makes sure the decomposed matrix is not modified.
     *
     * @param decomp
     * @param M
     * @param <T>
     * @return
     */
    public static <T extends Matrix64F> boolean decomposeSafe( DecompositionInterface<T> decomp, T M ) {
        if( decomp.inputModified() ) {
            return decomp.decompose(M.<T>copy());
        } else {
            return decomp.decompose(M);
        }
    }
}
