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

package org.ejml.dense.row.decomposition.hessenberg;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition_F32;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.ejml.dense.row.decomposition.CheckDecompositionInterface_FDRM.safeDecomposition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public abstract class StandardTridiagonalTests_FDRM {

    protected Random rand = new Random(2344);

    protected abstract TridiagonalSimilarDecomposition_F32<FMatrixRMaj> createDecomposition();

    @Test
    public void fullTest() {

        for( int width = 1; width < 20; width += 2 ) {

            SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_FDRM.symmetric(width,-1,1,rand));

            TridiagonalSimilarDecomposition_F32<FMatrixRMaj> alg = createDecomposition();


            assertTrue(safeDecomposition(alg,(FMatrixRMaj)A.getMatrix()));

            // test the results using the decomposition's definition
            SimpleMatrix Q = SimpleMatrix.wrap(alg.getQ(null,false));
            SimpleMatrix T = SimpleMatrix.wrap(alg.getT(null));

            SimpleMatrix A_found = Q.mult(T).mult(Q.transpose());

            assertTrue(MatrixFeatures_FDRM.isIdentical(A.getMatrix(), A_found.getMatrix(),UtilEjml.TEST_F32),
                    "width = "+width);
        }
    }

    @Test
    public void getDiagonal() {
        for( int width = 1; width < 20; width += 2 ) {

            FMatrixRMaj A = RandomMatrices_FDRM.symmetric(width,-1,1,rand);

            TridiagonalSimilarDecomposition_F32<FMatrixRMaj> alg = createDecomposition();

            assertTrue(safeDecomposition(alg,A));

            FMatrixRMaj T = alg.getT(null);

            float diag[] = new float[width];
            float off[] = new float[width];

            alg.getDiagonal(diag,off);
            assertEquals(T.get(0,0),diag[0],UtilEjml.TEST_F32);
            for( int i = 1; i < width; i++ ) {
                assertEquals(T.get(i,i),diag[i], UtilEjml.TEST_F32);
                assertEquals(T.get(i-1,i),off[i-1],UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void transposeFlagForQ() {
        for( int width = 1; width < 20; width += 2 ) {

            FMatrixRMaj A = RandomMatrices_FDRM.symmetric(width,-1,1,rand);

            TridiagonalSimilarDecomposition_F32<FMatrixRMaj> alg = createDecomposition();

            assertTrue(safeDecomposition(alg,A));

            FMatrixRMaj Q = alg.getQ(null,false);
            FMatrixRMaj Q_t = alg.getQ(null,true);

            for( int i = 0; i < Q.numRows; i++ ) {
                for( int j = 0; j < Q.numCols; j++ ) {
                    assertEquals(Q.get(i,j),Q_t.get(j,i),UtilEjml.TEST_F32);
                }
            }
        }
    }
}
