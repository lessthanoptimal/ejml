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

package org.ejml.factory;

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
 * Contains operations related to creating and evaluating the quality of common matrix decompositions.  Except
 * in specialized situations, matrix decompositions should be instantiated from this factory instead of being
 * directly constructed.  Low level implementations are more prone to changes and new algorithms will be
 * automatically placed here.
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
     * <p>
     * Returns a {@link CholeskyDecomposition} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param matrixSize Number of rows and columns that the returned decomposition is optimized for.
     * @param lower should a lower or upper triangular matrix be used. If not sure set to true.
     * @return A new CholeskyDecomposition.
     */
    public static CholeskyDecomposition<DenseMatrix64F> chol( int matrixSize , boolean lower )
    {
        if( matrixSize < EjmlParameters.SWITCH_BLOCK64_CHOLESKY ) {
            return new CholeskyDecompositionInner(lower);
        } else if( EjmlParameters.MEMORY == EjmlParameters.MemoryUsage.FASTER ){
            return new CholeskyDecompositionBlock64(lower);
        } else {
            return new CholeskyDecompositionBlock(EjmlParameters.BLOCK_WIDTH_CHOL);
        }
    }

    /**
     * <p>
     * Returns a {@link CholeskyDecompositionLDL} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param matrixSize Number of rows and columns that the returned decomposition is optimized for.
     * @return CholeskyDecompositionLDL
     */
    public static CholeskyDecompositionLDL cholLDL( int matrixSize ) {
        return new CholeskyDecompositionLDL();
    }

    /**
     * <p>
     * Returns a {@link LUDecomposition} that has been optimized for the specified matrix size.
     * </p>
     *
     * @parm matrixWidth The matrix size that the decomposition should be optimized for.
     * @return LUDecomposition
     */
    public static LUDecomposition<DenseMatrix64F> lu( int numRows , int numCol ) {
        return new LUDecompositionAlt();
    }

    /**
     * <p>
     * Returns a {@link SingularValueDecomposition} that has been optimized for the specified matrix size.
     * For improved performance only the portion of the decomposition that the user requests will be computed.
     * </p>
     *
     * @param numRows Number of rows the returned decomposition is optimized for.
     * @param numCols Number of columns that the returned decomposition is optimized for.
     * @param needU Should it compute the U matrix. If not sure set to true.
     * @param needV Should it compute the V matrix. If not sure set to true.
     * @param compact Should it compute the SVD in compact form.  If not sure set to false.
     * @return
     */
    public static SingularValueDecomposition<DenseMatrix64F> svd( int numRows , int numCols , 
                                                                  boolean needU , boolean needV , boolean compact ) {
        // Don't allow the tall decomposition by default since it *might* be less stable
        return new SvdImplicitQrDecompose(compact,needU,needV,false);
    }

    /**
     * <p>
     * Returns a {@link QRDecomposition} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param numRows Number of rows the returned decomposition is optimized for.
     * @param numCols Number of columns that the returned decomposition is optimized for.
     * @return QRDecomposition
     */
    public static QRDecomposition<DenseMatrix64F> qr( int numRows , int numCols ) {
        return new QRDecompositionHouseholderColumn();
    }

    /**
     * <p>
     * Returns a {@link QRPDecomposition} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param numRows Number of rows the returned decomposition is optimized for.
     * @param numCols Number of columns that the returned decomposition is optimized for.
     * @return QRPDecomposition
     */
    public static QRPDecomposition<DenseMatrix64F> qrp( int numRows , int numCols ) {
        return new QRColPivDecompositionHouseholderColumn();
    }

    /**
     * <p>
     * Returns an {@link EigenDecomposition} that has been optimized for the specified matrix size.
     * If the input matrix is symmetric within tolerance then the symmetric algorithm will be used, otherwise
     * a general purpose eigenvalue decomposition is used.
     * </p>
     *
     * @param matrixSize Number of rows and columns that the returned decomposition is optimized for.
     * @param needVectors Should eigenvectors be computed or not.  If not sure set to true.
     * @return A new EigenDecomposition
     */
    public static EigenDecomposition<DenseMatrix64F> eig( int matrixSize , boolean needVectors ) {
        return new SwitchingEigenDecomposition(matrixSize,needVectors,1e-8);
    }

    /**
     * <p>
     * Returns an {@link EigenDecomposition} which is specialized for symmetric matrices or the general problem.
     * </p>
     *
     * @param matrixSize Number of rows and columns that the returned decomposition is optimized for.
     * @param computeVectors Should it compute the eigenvectors or just eigenvalues.
     * @param isSymmetric If true then the returned algorithm is specialized only for symmetric matrices, if false
     *                    then a general purpose algorithm is returned.
     * @return EVD for any matrix.
     */
    public static EigenDecomposition<DenseMatrix64F> eig( int matrixSize , boolean computeVectors ,
                                                          boolean isSymmetric ) {
        if( isSymmetric ) {
            TridiagonalSimilarDecomposition<DenseMatrix64F> decomp = DecompositionFactory.tridiagonal(matrixSize);
            return new SymmetricQRAlgorithmDecomposition(decomp,computeVectors);
        } else
            return new WatchedDoubleStepQRDecomposition(computeVectors);
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
     *
     * @param matrixSize Number of rows and columns that the returned decomposition is optimized for.
     */
    public static TridiagonalSimilarDecomposition<DenseMatrix64F> tridiagonal(  int matrixSize ) {
        if( matrixSize >= 1800 ) {
            return new TridiagonalDecompositionBlock();
        } else {
            return new TridiagonalDecompositionHouseholder();
        }
    }

    /**
     * A simple convinience function that decomposes the matrix but automatically checks the input ti make
     * sure is not being modified.
     *
     * @param decomp Decomposition which is being wrapped
     * @param M THe matrix being decomposed.
     * @param <T> Matrix type.
     * @return If the decomposition was successful or not.
     */
    public static <T extends Matrix64F> boolean decomposeSafe( DecompositionInterface<T> decomp, T M ) {
        if( decomp.inputModified() ) {
            return decomp.decompose(M.<T>copy());
        } else {
            return decomp.decompose(M);
        }
    }
}
