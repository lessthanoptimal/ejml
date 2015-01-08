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

package org.ejml.factory;

import org.ejml.EjmlParameters;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionBlock_D64;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner_D64;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionLDL_D64;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecomposition_B64_to_D64;
import org.ejml.alg.dense.decomposition.eig.SwitchingEigenDecomposition;
import org.ejml.alg.dense.decomposition.eig.SymmetricQRAlgorithmDecomposition_D64;
import org.ejml.alg.dense.decomposition.eig.WatchedDoubleStepQRDecomposition_D64;
import org.ejml.alg.dense.decomposition.hessenberg.TridiagonalDecompositionHouseholder_D64;
import org.ejml.alg.dense.decomposition.hessenberg.TridiagonalDecomposition_B64_to_D64;
import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt_D64;
import org.ejml.alg.dense.decomposition.qr.QRColPivDecompositionHouseholderColumn_D64;
import org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholderColumn_D64;
import org.ejml.alg.dense.decomposition.svd.SvdImplicitQrDecompose_D64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.RealMatrix64F;
import org.ejml.interfaces.decomposition.*;
import org.ejml.ops.CommonOps;
import org.ejml.ops.EigenOps;
import org.ejml.ops.NormOps;
import org.ejml.ops.SpecializedOps;


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
            return new CholeskyDecompositionInner_D64(lower);
        } else if( EjmlParameters.MEMORY == EjmlParameters.MemoryUsage.FASTER ){
            return new CholeskyDecomposition_B64_to_D64(lower);
        } else {
            return new CholeskyDecompositionBlock_D64(EjmlParameters.BLOCK_WIDTH_CHOL);
        }
    }

    /**
     * <p>
     * Returns a {@link org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionLDL_D64} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param matrixSize Number of rows and columns that the returned decomposition is optimized for.
     * @return CholeskyLDLDecomposition
     */
    public static CholeskyLDLDecomposition<DenseMatrix64F> cholLDL( int matrixSize ) {
        return new CholeskyDecompositionLDL_D64();
    }

    /**
     * <p>
     * Returns a {@link org.ejml.interfaces.decomposition.LUDecomposition} that has been optimized for the specified matrix size.
     * </p>
     *
     * @parm matrixWidth The matrix size that the decomposition should be optimized for.
     * @return LUDecomposition
     */
    public static LUDecomposition<DenseMatrix64F> lu( int numRows , int numCol ) {
        return new LUDecompositionAlt_D64();
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
        return new SvdImplicitQrDecompose_D64(compact,needU,needV,false);
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
    public static QRDecomposition<DenseMatrix64F> qr( int numRows , int numCols ) {
        return new QRDecompositionHouseholderColumn_D64();
    }

    /**
     * <p>
     * Returns a {@link org.ejml.interfaces.decomposition.QRPDecomposition} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param numRows Number of rows the returned decomposition is optimized for.
     * @param numCols Number of columns that the returned decomposition is optimized for.
     * @return QRPDecomposition
     */
    public static QRPDecomposition<DenseMatrix64F> qrp( int numRows , int numCols ) {
        return new QRColPivDecompositionHouseholderColumn_D64();
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
            return new SymmetricQRAlgorithmDecomposition_D64(decomp,computeVectors);
        } else
            return new WatchedDoubleStepQRDecomposition_D64(computeVectors);
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
        return quality(orig,svd.getU(null,false),svd.getW(null),svd.getV(null,true));
    }

    public static double quality( DenseMatrix64F orig , DenseMatrix64F U , DenseMatrix64F W , DenseMatrix64F Vt )
    {
        // foundA = U*W*Vt
        DenseMatrix64F UW = new DenseMatrix64F(U.numRows,W.numCols);
        CommonOps.mult(U,W,UW);
        DenseMatrix64F foundA = new DenseMatrix64F(UW.numRows,Vt.numCols);
        CommonOps.mult(UW,Vt,foundA);

        double normA = NormOps.normF(foundA);

        return SpecializedOps.diffNormF(orig,foundA)/normA;
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
        DenseMatrix64F A = orig;
        DenseMatrix64F V = EigenOps.createMatrixV(eig);
        DenseMatrix64F D = EigenOps.createMatrixD(eig);

        // L = A*V
        DenseMatrix64F L = new DenseMatrix64F(A.numRows,V.numCols);
        CommonOps.mult(A,V,L);
        // R = V*D
        DenseMatrix64F R = new DenseMatrix64F(V.numRows,D.numCols);
        CommonOps.mult(V,D,R);

        DenseMatrix64F diff = new DenseMatrix64F(L.numRows,L.numCols);
        CommonOps.subtract(L,R,diff);

        double top = NormOps.normF(diff);
        double bottom = NormOps.normF(L);

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
            return new TridiagonalDecomposition_B64_to_D64();
        } else {
            return new TridiagonalDecompositionHouseholder_D64();
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
    public static <T extends RealMatrix64F> boolean decomposeSafe( DecompositionInterface<T> decomp, T M ) {
        if( decomp.inputModified() ) {
            return decomp.decompose(M.<T>copy());
        } else {
            return decomp.decompose(M);
        }
    }
}
