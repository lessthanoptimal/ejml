/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

import org.ejml.alg.dense.decompose.chol.CholeskyDecompositionInner_CD64;
import org.ejml.alg.dense.decompose.lu.LUDecompositionAlt_CD64;
import org.ejml.alg.dense.decompose.qr.QRDecompositionHouseholderColumn_CD64;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.interfaces.decomposition.CholeskyDecomposition;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.ejml.interfaces.decomposition.LUDecomposition;
import org.ejml.interfaces.decomposition.QRDecomposition;

/**
 * <p>
 * Contains operations related to creating and evaluating the quality of common matrix decompositions.  Except
 * in specialized situations, matrix decompositions should be instantiated from this factory instead of being
 * directly constructed.  Low level implementations are more prone to changes and new algorithms will be
 * automatically placed here.
 * </p>
 *
 * @author Peter Abeles
 */
public class CDecompositionFactory {
    /**
     * <p>
     * Returns a {@link org.ejml.interfaces.decomposition.LUDecomposition} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param numRows Number of rows the returned decomposition is optimized for.
     * @param numCols Number of columns that the returned decomposition is optimized for.
     * @return LUDecomposition
     */
    public static LUDecomposition<CDenseMatrix64F> lu( int numRows , int numCols ) {
        return new LUDecompositionAlt_CD64();
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
    public static QRDecomposition<CDenseMatrix64F> qr( int numRows , int numCols ) {
        return new QRDecompositionHouseholderColumn_CD64();
    }

    /**
     * <p>
     * Returns a {@link org.ejml.interfaces.decomposition.CholeskyDecomposition} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param size Number of rows and columns it should be optimized for
     * @param lower if true then it will be a lower cholesky.  false for upper.  Try lower.
     * @return QRDecomposition
     */
    public static CholeskyDecomposition<CDenseMatrix64F> chol( int size , boolean lower ) {
        return new CholeskyDecompositionInner_CD64(lower);
    }

    /**
     * Decomposes the input matrix 'a' and makes sure it isn't modified.
     */
    public static boolean decomposeSafe(DecompositionInterface<CDenseMatrix64F> decomposition, CDenseMatrix64F a) {

        if( decomposition.inputModified() ) {
            a = a.copy();
        }
        return decomposition.decompose(a);
    }
}
