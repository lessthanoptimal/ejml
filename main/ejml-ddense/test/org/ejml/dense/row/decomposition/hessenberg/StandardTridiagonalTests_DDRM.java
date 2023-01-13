/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.decomposition.hessenberg;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition_F64;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import static org.ejml.dense.row.decomposition.CheckDecompositionInterface_DDRM.safeDecomposition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public abstract class StandardTridiagonalTests_DDRM extends EjmlStandardJUnit {
    protected abstract TridiagonalSimilarDecomposition_F64<DMatrixRMaj> createDecomposition();

    @Test void fullTest() {

        for( int width = 1; width < 20; width += 2 ) {

            SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_DDRM.symmetric(width,-1,1,rand));

            TridiagonalSimilarDecomposition_F64<DMatrixRMaj> alg = createDecomposition();


            assertTrue(safeDecomposition(alg,(DMatrixRMaj)A.getMatrix()));

            // test the results using the decomposition's definition
            SimpleMatrix Q = SimpleMatrix.wrap(alg.getQ(null,false));
            SimpleMatrix T = SimpleMatrix.wrap(alg.getT(null));

            SimpleMatrix A_found = Q.mult(T).mult(Q.transpose());

            assertTrue(MatrixFeatures_DDRM.isIdentical(A.getMatrix(), A_found.getMatrix(),UtilEjml.TEST_F64),
                    "width = "+width);
        }
    }

    @Test void getDiagonal() {
        for( int width = 1; width < 20; width += 2 ) {

            DMatrixRMaj A = RandomMatrices_DDRM.symmetric(width,-1,1,rand);

            TridiagonalSimilarDecomposition_F64<DMatrixRMaj> alg = createDecomposition();

            assertTrue(safeDecomposition(alg,A));

            DMatrixRMaj T = alg.getT(null);

            double diag[] = new double[width];
            double off[] = new double[width];

            alg.getDiagonal(diag,off);
            assertEquals(T.get(0,0),diag[0],UtilEjml.TEST_F64);
            for( int i = 1; i < width; i++ ) {
                assertEquals(T.get(i,i),diag[i], UtilEjml.TEST_F64);
                assertEquals(T.get(i-1,i),off[i-1],UtilEjml.TEST_F64);
            }
        }
    }

    @Test void transposeFlagForQ() {
        for( int width = 1; width < 20; width += 2 ) {

            DMatrixRMaj A = RandomMatrices_DDRM.symmetric(width,-1,1,rand);

            TridiagonalSimilarDecomposition_F64<DMatrixRMaj> alg = createDecomposition();

            assertTrue(safeDecomposition(alg,A));

            DMatrixRMaj Q = alg.getQ(null,false);
            DMatrixRMaj Q_t = alg.getQ(null,true);

            for( int i = 0; i < Q.numRows; i++ ) {
                for( int j = 0; j < Q.numCols; j++ ) {
                    assertEquals(Q.get(i,j),Q_t.get(j,i),UtilEjml.TEST_F64);
                }
            }
        }
    }
}
