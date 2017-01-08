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

package org.ejml.alg.dense.decomposition.hessenberg;

import org.ejml.UtilEjml;
import org.ejml.data.RowMatrix_F64;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition_F64;
import org.ejml.ops.MatrixFeatures_R64;
import org.ejml.ops.RandomMatrices_R64;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.ejml.alg.dense.decomposition.CheckDecompositionInterface_R64.safeDecomposition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public abstract class StandardTridiagonalTests_R64 {

    protected Random rand = new Random(2344);

    protected abstract TridiagonalSimilarDecomposition_F64<RowMatrix_F64> createDecomposition();

    @Test
    public void fullTest() {

        for( int width = 1; width < 20; width += 2 ) {

            SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_R64.createSymmetric(width,-1,1,rand));

            TridiagonalSimilarDecomposition_F64<RowMatrix_F64> alg = createDecomposition();


            assertTrue(safeDecomposition(alg,(RowMatrix_F64)A.getMatrix()));

            // test the results using the decomposition's definition
            SimpleMatrix Q = SimpleMatrix.wrap(alg.getQ(null,false));
            SimpleMatrix T = SimpleMatrix.wrap(alg.getT(null));

            SimpleMatrix A_found = Q.mult(T).mult(Q.transpose());

            assertTrue("width = "+width, MatrixFeatures_R64.isIdentical(
                    (RowMatrix_F64)A.getMatrix(),(RowMatrix_F64)A_found.getMatrix(),UtilEjml.TEST_F64));
        }
    }

    @Test
    public void getDiagonal() {
        for( int width = 1; width < 20; width += 2 ) {

            RowMatrix_F64 A = RandomMatrices_R64.createSymmetric(width,-1,1,rand);

            TridiagonalSimilarDecomposition_F64<RowMatrix_F64> alg = createDecomposition();

            assertTrue(safeDecomposition(alg,A));

            RowMatrix_F64 T = alg.getT(null);

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

    @Test
    public void transposeFlagForQ() {
        for( int width = 1; width < 20; width += 2 ) {

            RowMatrix_F64 A = RandomMatrices_R64.createSymmetric(width,-1,1,rand);

            TridiagonalSimilarDecomposition_F64<RowMatrix_F64> alg = createDecomposition();

            assertTrue(safeDecomposition(alg,A));

            RowMatrix_F64 Q = alg.getQ(null,false);
            RowMatrix_F64 Q_t = alg.getQ(null,true);

            for( int i = 0; i < Q.numRows; i++ ) {
                for( int j = 0; j < Q.numCols; j++ ) {
                    assertEquals(Q.get(i,j),Q_t.get(j,i),UtilEjml.TEST_F64);
                }
            }
        }
    }
}
