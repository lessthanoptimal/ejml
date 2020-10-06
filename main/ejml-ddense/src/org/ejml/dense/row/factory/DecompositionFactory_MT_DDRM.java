/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.factory;

import org.ejml.EjmlParameters;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.decomposition.chol.CholeskyDecompositionBlock_MT_DDRM;
import org.ejml.dense.row.decomposition.eig.SwitchingEigenDecomposition_DDRM;
import org.ejml.dense.row.decomposition.eig.SymmetricQRAlgorithmDecomposition_DDRM;
import org.ejml.dense.row.decomposition.eig.WatchedDoubleStepQRDecomposition_DDRM;
import org.ejml.dense.row.decomposition.eig.watched.WatchedDoubleStepQREigen_DDRM;
import org.ejml.dense.row.decomposition.eig.watched.WatchedDoubleStepQREigen_MT_DDRM;
import org.ejml.dense.row.decomposition.hessenberg.HessenbergSimilarDecomposition_DDRM;
import org.ejml.dense.row.decomposition.hessenberg.HessenbergSimilarDecomposition_MT_DDRM;
import org.ejml.dense.row.decomposition.hessenberg.TridiagonalDecompositionHouseholder_MT_DDRM;
import org.ejml.dense.row.decomposition.qr.QRDecompositionHouseholderColumn_MT_DDRM;
import org.ejml.dense.row.decomposition.svd.SvdImplicitQrDecompose_MT_DDRM;
import org.ejml.interfaces.decomposition.*;

/**
 * <p>
 * Contains concurrent implementations of different decompositions.
 * </p>
 *
 * @author Peter Abeles
 */
public class DecompositionFactory_MT_DDRM {

    /**
     * <p>
     * Returns a {@link CholeskyDecomposition_F64} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param matrixSize Number of rows and columns that the returned decomposition is optimized for.
     * @param lower should a lower or upper triangular matrix be used. If not sure set to true.
     * @return A new CholeskyDecomposition.
     */
    public static CholeskyDecomposition_F64<DMatrixRMaj> chol( int matrixSize, boolean lower ) {
//        var blocks = new CholeskyOuterForm_MT_DDRB(lower);
//        return new CholeskyDecomposition_DDRB_to_DDRM(blocks,EjmlParameters.BLOCK_WIDTH);

        // The DDRM version was found to be just as fast and doesn't require converting the matrix
        return new CholeskyDecompositionBlock_MT_DDRM(EjmlParameters.BLOCK_WIDTH_CHOL);
    }

    /**
     * Returns a {@link CholeskyDecomposition_F64} that isn't specialized for any specific matrix size.
     *
     * @param lower should a lower or upper triangular matrix be used. If not sure set to true.
     * @return A new CholeskyDecomposition.
     */
    public static CholeskyDecomposition_F64<DMatrixRMaj> chol( boolean lower ) {
        return chol(100, lower);
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
     * @param compact Should it compute the SVD in compact form. If not sure set to false.
     * @return SVD
     */
    public static SingularValueDecomposition_F64<DMatrixRMaj> svd( int numRows, int numCols,
                                                                   boolean needU, boolean needV, boolean compact ) {
        // Don't allow the tall decomposition by default since it *might* be less stable
        return new SvdImplicitQrDecompose_MT_DDRM(compact, needU, needV, false);
    }

    /**
     * Returns a {@link SingularValueDecomposition} that is NOT optimized for any specified matrix size.
     *
     * @param needU Should it compute the U matrix. If not sure set to true.
     * @param needV Should it compute the V matrix. If not sure set to true.
     * @param compact Should it compute the SVD in compact form. If not sure set to false.
     * @return SVD
     */
    public static SingularValueDecomposition_F64<DMatrixRMaj> svd( boolean needU, boolean needV, boolean compact ) {
        return svd(100, 100, needU, needV, compact);
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
    public static QRDecomposition<DMatrixRMaj> qr( int numRows, int numCols ) {
        return new QRDecompositionHouseholderColumn_MT_DDRM();
    }

    public static QRDecomposition<DMatrixRMaj> qr() {
        return new QRDecompositionHouseholderColumn_MT_DDRM();
    }

    /**
     * <p>
     * Returns an {@link EigenDecomposition} that has been optimized for the specified matrix size.
     * If the input matrix is symmetric within tolerance then the symmetric algorithm will be used, otherwise
     * a general purpose eigenvalue decomposition is used.
     * </p>
     *
     * @param matrixSize Number of rows and columns that the returned decomposition is optimized for.
     * @param needVectors Should eigenvectors be computed or not. If not sure set to true.
     * @return A new EigenDecomposition
     */
    public static EigenDecomposition_F64<DMatrixRMaj> eig( int matrixSize, boolean needVectors ) {
        EigenDecomposition_F64<DMatrixRMaj> symm = eig(matrixSize, needVectors, true);
        EigenDecomposition_F64<DMatrixRMaj> general = eig(matrixSize, needVectors, false);

        return new SwitchingEigenDecomposition_DDRM(symm, general, UtilEjml.TEST_F64);
    }

    public static EigenDecomposition_F64<DMatrixRMaj> eig( boolean needVectors ) {
        return eig(100, needVectors);
    }

    /**
     * <p>
     * Returns an {@link EigenDecomposition} which is specialized for symmetric matrices or the general problem.
     * </p>
     *
     * @param matrixSize Number of rows and columns that the returned decomposition is optimized for.
     * @param computeVectors Should it compute the eigenvectors or just eigenvalues.
     * @param isSymmetric If true then the returned algorithm is specialized only for symmetric matrices, if false
     * then a general purpose algorithm is returned.
     * @return EVD for any matrix.
     */
    public static EigenDecomposition_F64<DMatrixRMaj> eig( int matrixSize, boolean computeVectors,
                                                           boolean isSymmetric ) {
        if (isSymmetric) {
            TridiagonalSimilarDecomposition_F64<DMatrixRMaj> decomp = DecompositionFactory_MT_DDRM.tridiagonal(matrixSize);
            return new SymmetricQRAlgorithmDecomposition_DDRM(decomp, computeVectors);
        } else {
            HessenbergSimilarDecomposition_DDRM hessenberg = new HessenbergSimilarDecomposition_MT_DDRM();
            WatchedDoubleStepQREigen_DDRM eigenQR = new WatchedDoubleStepQREigen_MT_DDRM();
            return new WatchedDoubleStepQRDecomposition_DDRM(hessenberg, eigenQR, computeVectors);
        }
    }

    public static EigenDecomposition_F64<DMatrixRMaj> eig( boolean computeVectors,
                                                           boolean isSymmetric ) {
        return eig(100, computeVectors, isSymmetric);
    }

    /**
     * Checks to see if the passed in tridiagonal decomposition is of the appropriate type
     * for the matrix of the provided size. Returns the same instance or a new instance.
     *
     * @param matrixSize Number of rows and columns that the returned decomposition is optimized for.
     */
    public static TridiagonalSimilarDecomposition_F64<DMatrixRMaj> tridiagonal( int matrixSize ) {
//        if (matrixSize >= 1800) {
//            throw new RuntimeException("IMplement");
////            return new TridiagonalDecomposition_DDRB_to_DDRM();
//        } else {
        return new TridiagonalDecompositionHouseholder_MT_DDRM();
//        }
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
    public static <T extends DMatrix> boolean decomposeSafe( DecompositionInterface<T> decomp, T M ) {
        if (decomp.inputModified()) {
            return decomp.decompose(M.<T>copy());
        } else {
            return decomp.decompose(M);
        }
    }
}
