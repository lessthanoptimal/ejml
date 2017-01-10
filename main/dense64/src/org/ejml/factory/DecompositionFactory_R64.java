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

package org.ejml.factory;

import org.ejml.EjmlParameters;
import org.ejml.UtilEjml;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionBlock_R64;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner_R64;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionLDL_R64;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecomposition_B64_to_R64;
import org.ejml.alg.dense.decomposition.eig.SwitchingEigenDecomposition_R64;
import org.ejml.alg.dense.decomposition.eig.SymmetricQRAlgorithmDecomposition_R64;
import org.ejml.alg.dense.decomposition.eig.WatchedDoubleStepQRDecomposition_R64;
import org.ejml.alg.dense.decomposition.hessenberg.TridiagonalDecompositionHouseholder_R64;
import org.ejml.alg.dense.decomposition.hessenberg.TridiagonalDecomposition_B64_to_R64;
import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt_R64;
import org.ejml.alg.dense.decomposition.qr.QRColPivDecompositionHouseholderColumn_R64;
import org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholderColumn_R64;
import org.ejml.alg.dense.decomposition.svd.SvdImplicitQrDecompose_R64;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.data.Matrix_F64;
import org.ejml.interfaces.decomposition.*;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.EigenOps_R64;
import org.ejml.ops.NormOps_R64;
import org.ejml.ops.SpecializedOps_R64;


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
 * @author Peter Abeles
 */
public class DecompositionFactory_R64 {

    /**
     * <p>
     * Returns a {@link CholeskyDecomposition_F64} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param matrixSize Number of rows and columns that the returned decomposition is optimized for.
     * @param lower should a lower or upper triangular matrix be used. If not sure set to true.
     * @return A new CholeskyDecomposition.
     */
    public static CholeskyDecomposition_F64<DMatrixRow_F64> chol(int matrixSize , boolean lower )
    {
        if( matrixSize < EjmlParameters.SWITCH_BLOCK64_CHOLESKY ) {
            return new CholeskyDecompositionInner_R64(lower);
        } else if( EjmlParameters.MEMORY == EjmlParameters.MemoryUsage.FASTER ){
            return new CholeskyDecomposition_B64_to_R64(lower);
        } else {
            return new CholeskyDecompositionBlock_R64(EjmlParameters.BLOCK_WIDTH_CHOL);
        }
    }

    /**
     * <p>
     * Returns a {@link org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionLDL_R64} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param matrixSize Number of rows and columns that the returned decomposition is optimized for.
     * @return CholeskyLDLDecomposition_F64
     */
    public static CholeskyLDLDecomposition_F64<DMatrixRow_F64> cholLDL(int matrixSize ) {
        return new CholeskyDecompositionLDL_R64();
    }

    /**
     * <p>
     * Returns a {@link org.ejml.interfaces.decomposition.LUDecomposition} that has been optimized for the specified matrix size.
     * </p>
     *
     * @parm matrixWidth The matrix size that the decomposition should be optimized for.
     * @return LUDecomposition
     */
    public static LUDecomposition_F64<DMatrixRow_F64> lu(int numRows , int numCol ) {
        return new LUDecompositionAlt_R64();
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
    public static SingularValueDecomposition_F64<DMatrixRow_F64> svd(int numRows , int numCols ,
                                                                    boolean needU , boolean needV , boolean compact ) {
        // Don't allow the tall decomposition by default since it *might* be less stable
        return new SvdImplicitQrDecompose_R64(compact,needU,needV,false);
    }

    /**
     * <p>
     * Returns a {@link org.ejml.interfaces.decomposition.QRDecomposition} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param numRows Number of rows the returned decomposition is optimized for.
     * @param numCols Number of columns that the returned decomposition is optimized for.
     * @return QRDecomposition
     */
    public static QRDecomposition<DMatrixRow_F64> qr(int numRows , int numCols ) {
        return new QRDecompositionHouseholderColumn_R64();
    }

    /**
     * <p>
     * Returns a {@link QRPDecomposition_F64} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param numRows Number of rows the returned decomposition is optimized for.
     * @param numCols Number of columns that the returned decomposition is optimized for.
     * @return QRPDecomposition_F64
     */
    public static QRPDecomposition_F64<DMatrixRow_F64> qrp(int numRows , int numCols ) {
        return new QRColPivDecompositionHouseholderColumn_R64();
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
    public static EigenDecomposition_F64<DMatrixRow_F64> eig(int matrixSize , boolean needVectors ) {
        return new SwitchingEigenDecomposition_R64(matrixSize,needVectors, UtilEjml.TEST_F64);
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
    public static EigenDecomposition_F64<DMatrixRow_F64> eig(int matrixSize , boolean computeVectors ,
                                                            boolean isSymmetric ) {
        if( isSymmetric ) {
            TridiagonalSimilarDecomposition_F64<DMatrixRow_F64> decomp = DecompositionFactory_R64.tridiagonal(matrixSize);
            return new SymmetricQRAlgorithmDecomposition_R64(decomp,computeVectors);
        } else
            return new WatchedDoubleStepQRDecomposition_R64(computeVectors);
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
    public static double quality(DMatrixRow_F64 orig , SingularValueDecomposition<DMatrixRow_F64> svd )
    {
        return quality(orig,svd.getU(null,false),svd.getW(null),svd.getV(null,true));
    }

    public static double quality(DMatrixRow_F64 orig , DMatrixRow_F64 U , DMatrixRow_F64 W , DMatrixRow_F64 Vt )
    {
        // foundA = U*W*Vt
        DMatrixRow_F64 UW = new DMatrixRow_F64(U.numRows,W.numCols);
        CommonOps_R64.mult(U,W,UW);
        DMatrixRow_F64 foundA = new DMatrixRow_F64(UW.numRows,Vt.numCols);
        CommonOps_R64.mult(UW,Vt,foundA);

        double normA = NormOps_R64.normF(foundA);

        return SpecializedOps_R64.diffNormF(orig,foundA)/normA;
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
    public static double quality(DMatrixRow_F64 orig , EigenDecomposition_F64<DMatrixRow_F64> eig )
    {
        DMatrixRow_F64 A = orig;
        DMatrixRow_F64 V = EigenOps_R64.createMatrixV(eig);
        DMatrixRow_F64 D = EigenOps_R64.createMatrixD(eig);

        // L = A*V
        DMatrixRow_F64 L = new DMatrixRow_F64(A.numRows,V.numCols);
        CommonOps_R64.mult(A,V,L);
        // R = V*D
        DMatrixRow_F64 R = new DMatrixRow_F64(V.numRows,D.numCols);
        CommonOps_R64.mult(V,D,R);

        DMatrixRow_F64 diff = new DMatrixRow_F64(L.numRows,L.numCols);
        CommonOps_R64.subtract(L,R,diff);

        double top = NormOps_R64.normF(diff);
        double bottom = NormOps_R64.normF(L);

        double error = top/bottom;

        return error;
    }

    /**
     * Checks to see if the passed in tridiagonal decomposition is of the appropriate type
     * for the matrix of the provided size.  Returns the same instance or a new instance.
     *
     * @param matrixSize Number of rows and columns that the returned decomposition is optimized for.
     */
    public static TridiagonalSimilarDecomposition_F64<DMatrixRow_F64> tridiagonal(int matrixSize ) {
        if( matrixSize >= 1800 ) {
            return new TridiagonalDecomposition_B64_to_R64();
        } else {
            return new TridiagonalDecompositionHouseholder_R64();
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
    public static <T extends Matrix_F64> boolean decomposeSafe(DecompositionInterface<T> decomp, T M ) {
        if( decomp.inputModified() ) {
            return decomp.decompose(M.<T>copy());
        } else {
            return decomp.decompose(M);
        }
    }
}
