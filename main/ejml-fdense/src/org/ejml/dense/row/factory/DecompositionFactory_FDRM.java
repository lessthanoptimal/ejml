/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.FMatrix;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.EigenOps_FDRM;
import org.ejml.dense.row.NormOps_FDRM;
import org.ejml.dense.row.SpecializedOps_FDRM;
import org.ejml.dense.row.decomposition.chol.CholeskyDecompositionBlock_FDRM;
import org.ejml.dense.row.decomposition.chol.CholeskyDecompositionInner_FDRM;
import org.ejml.dense.row.decomposition.chol.CholeskyDecompositionLDL_FDRM;
import org.ejml.dense.row.decomposition.chol.CholeskyDecomposition_FDRB_to_FDRM;
import org.ejml.dense.row.decomposition.eig.SwitchingEigenDecomposition_FDRM;
import org.ejml.dense.row.decomposition.eig.SymmetricQRAlgorithmDecomposition_FDRM;
import org.ejml.dense.row.decomposition.eig.WatchedDoubleStepQRDecomposition_FDRM;
import org.ejml.dense.row.decomposition.hessenberg.TridiagonalDecompositionHouseholder_FDRM;
import org.ejml.dense.row.decomposition.hessenberg.TridiagonalDecomposition_FDRB_to_FDRM;
import org.ejml.dense.row.decomposition.lu.LUDecompositionAlt_FDRM;
import org.ejml.dense.row.decomposition.qr.QRColPivDecompositionHouseholderColumn_FDRM;
import org.ejml.dense.row.decomposition.qr.QRDecompositionHouseholderColumn_FDRM;
import org.ejml.dense.row.decomposition.svd.SvdImplicitQrDecompose_FDRM;
import org.ejml.interfaces.decomposition.*;


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
public class DecompositionFactory_FDRM {

    /**
     * <p>
     * Returns a {@link CholeskyDecomposition_F32} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param matrixSize Number of rows and columns that the returned decomposition is optimized for.
     * @param lower should a lower or upper triangular matrix be used. If not sure set to true.
     * @return A new CholeskyDecomposition.
     */
    public static CholeskyDecomposition_F32<FMatrixRMaj> chol(int matrixSize , boolean lower )
    {
        if( matrixSize < EjmlParameters.SWITCH_BLOCK64_CHOLESKY ) {
            return new CholeskyDecompositionInner_FDRM(lower);
        } else if( EjmlParameters.MEMORY == EjmlParameters.MemoryUsage.FASTER ){
            return new CholeskyDecomposition_FDRB_to_FDRM(lower);
        } else {
            return new CholeskyDecompositionBlock_FDRM(EjmlParameters.BLOCK_WIDTH_CHOL);
        }
    }

    /**
     * Returns a {@link CholeskyDecomposition_F32} that isn't specialized for any specific matrix size.
     * @param lower should a lower or upper triangular matrix be used. If not sure set to true.
     * @return A new CholeskyDecomposition.
     */
    public static CholeskyDecomposition_F32<FMatrixRMaj> chol( boolean lower ) {
        return chol(100,lower);
    }

    /**
     * <p>
     * Returns a {@link org.ejml.dense.row.decomposition.chol.CholeskyDecompositionLDL_FDRM} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param matrixSize Number of rows and columns that the returned decomposition is optimized for.
     * @return CholeskyLDLDecomposition_F32
     */
    public static CholeskyLDLDecomposition_F32<FMatrixRMaj> cholLDL(int matrixSize ) {
        return new CholeskyDecompositionLDL_FDRM();
    }

    public static CholeskyLDLDecomposition_F32<FMatrixRMaj> cholLDL() {
        return new CholeskyDecompositionLDL_FDRM();
    }

    /**
     * <p>
     * Returns a {@link org.ejml.interfaces.decomposition.LUDecomposition} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param numRows Shape of the matrix that the code should be targeted towards. Does not need to be exact.
     * @param numCol Shape of the matrix that the code should be targeted towards. Does not need to be exact.
     * @return LUDecomposition
     */
    public static LUDecomposition_F32<FMatrixRMaj> lu(int numRows , int numCol ) {
        return new LUDecompositionAlt_FDRM();
    }

    public static LUDecomposition_F32<FMatrixRMaj> lu() {
        return new LUDecompositionAlt_FDRM();
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
     * @return SVD
     */
    public static SingularValueDecomposition_F32<FMatrixRMaj> svd(int numRows , int numCols ,
                                                                  boolean needU , boolean needV , boolean compact ) {
        // Don't allow the tall decomposition by default since it *might* be less stable
        return new SvdImplicitQrDecompose_FDRM(compact,needU,needV,false);
    }

    /**
     * Returns a {@link SingularValueDecomposition} that is NOT optimized for any specified matrix size.
     *
     * @param needU Should it compute the U matrix. If not sure set to true.
     * @param needV Should it compute the V matrix. If not sure set to true.
     * @param compact Should it compute the SVD in compact form.  If not sure set to false.
     * @return SVD
     */
    public static SingularValueDecomposition_F32<FMatrixRMaj> svd(boolean needU , boolean needV , boolean compact ) {
        return svd(100,100,needU,needV,compact);
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
    public static QRDecomposition<FMatrixRMaj> qr(int numRows , int numCols ) {
        return new QRDecompositionHouseholderColumn_FDRM();
    }

    public static QRDecomposition<FMatrixRMaj> qr() {
        return new QRDecompositionHouseholderColumn_FDRM();
    }

    /**
     * <p>
     * Returns a {@link QRPDecomposition_F32} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param numRows Number of rows the returned decomposition is optimized for.
     * @param numCols Number of columns that the returned decomposition is optimized for.
     * @return QRPDecomposition_F32
     */
    public static QRPDecomposition_F32<FMatrixRMaj> qrp(int numRows , int numCols ) {
        return new QRColPivDecompositionHouseholderColumn_FDRM();
    }

    public static QRPDecomposition_F32<FMatrixRMaj> qrp() {
        return new QRColPivDecompositionHouseholderColumn_FDRM();
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
    public static EigenDecomposition_F32<FMatrixRMaj> eig(int matrixSize , boolean needVectors ) {
        return new SwitchingEigenDecomposition_FDRM(matrixSize, needVectors, UtilEjml.TEST_F32);
    }

    public static EigenDecomposition_F32<FMatrixRMaj> eig(boolean needVectors ) {
        return eig(100,needVectors);
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
    public static EigenDecomposition_F32<FMatrixRMaj> eig(int matrixSize , boolean computeVectors ,
                                                          boolean isSymmetric ) {
        if( isSymmetric ) {
            TridiagonalSimilarDecomposition_F32<FMatrixRMaj> decomp = DecompositionFactory_FDRM.tridiagonal(matrixSize);
            return new SymmetricQRAlgorithmDecomposition_FDRM(decomp,computeVectors);
        } else
            return new WatchedDoubleStepQRDecomposition_FDRM(computeVectors);
    }
    public static EigenDecomposition_F32<FMatrixRMaj> eig( boolean computeVectors ,
                                                           boolean isSymmetric ) {
        return eig(100,computeVectors,isSymmetric);
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
    public static float quality(FMatrixRMaj orig , SingularValueDecomposition<FMatrixRMaj> svd )
    {
        return quality(orig,svd.getU(null,false),svd.getW(null),svd.getV(null,true));
    }

    public static float quality(FMatrixRMaj orig , FMatrixRMaj U , FMatrixRMaj W , FMatrixRMaj Vt )
    {
        // foundA = U*W*Vt
        FMatrixRMaj UW = new FMatrixRMaj(U.numRows,W.numCols);
        CommonOps_FDRM.mult(U,W,UW);
        FMatrixRMaj foundA = new FMatrixRMaj(UW.numRows,Vt.numCols);
        CommonOps_FDRM.mult(UW,Vt,foundA);

        float normA = NormOps_FDRM.normF(foundA);

        return SpecializedOps_FDRM.diffNormF(orig,foundA)/normA;
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
    public static float quality(FMatrixRMaj orig , EigenDecomposition_F32<FMatrixRMaj> eig )
    {
        FMatrixRMaj A = orig;
        FMatrixRMaj V = EigenOps_FDRM.createMatrixV(eig);
        FMatrixRMaj D = EigenOps_FDRM.createMatrixD(eig);

        // L = A*V
        FMatrixRMaj L = new FMatrixRMaj(A.numRows,V.numCols);
        CommonOps_FDRM.mult(A,V,L);
        // R = V*D
        FMatrixRMaj R = new FMatrixRMaj(V.numRows,D.numCols);
        CommonOps_FDRM.mult(V,D,R);

        FMatrixRMaj diff = new FMatrixRMaj(L.numRows,L.numCols);
        CommonOps_FDRM.subtract(L,R,diff);

        float top = NormOps_FDRM.normF(diff);
        float bottom = NormOps_FDRM.normF(L);

        float error = top/bottom;

        return error;
    }

    /**
     * Checks to see if the passed in tridiagonal decomposition is of the appropriate type
     * for the matrix of the provided size.  Returns the same instance or a new instance.
     *
     * @param matrixSize Number of rows and columns that the returned decomposition is optimized for.
     */
    public static TridiagonalSimilarDecomposition_F32<FMatrixRMaj> tridiagonal(int matrixSize ) {
        if( matrixSize >= 1800 ) {
            return new TridiagonalDecomposition_FDRB_to_FDRM();
        } else {
            return new TridiagonalDecompositionHouseholder_FDRM();
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
    public static <T extends FMatrix> boolean decomposeSafe(DecompositionInterface<T> decomp, T M ) {
        if( decomp.inputModified() ) {
            return decomp.decompose(M.<T>copy());
        } else {
            return decomp.decompose(M);
        }
    }
}
