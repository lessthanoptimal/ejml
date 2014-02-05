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

package org.ejml.alg.dense.decomposition.hessenberg;

import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.ejml.alg.dense.decomposition.CheckDecompositionInterface.safeDecomposition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public abstract class StandardTridiagonalTests {

    protected Random rand = new Random(2344);

    protected abstract TridiagonalSimilarDecomposition<DenseMatrix64F> createDecomposition();

    @Test
    public void fullTest() {

        for( int width = 1; width < 20; width += 2 ) {

            SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices.createSymmetric(width,-1,1,rand));

            TridiagonalSimilarDecomposition<DenseMatrix64F> alg = createDecomposition();


            assertTrue(safeDecomposition(alg,A.getMatrix()));

            // test the results using the decomposition's definition
            SimpleMatrix Q = SimpleMatrix.wrap(alg.getQ(null,false));
            SimpleMatrix T = SimpleMatrix.wrap(alg.getT(null));

            SimpleMatrix A_found = Q.mult(T).mult(Q.transpose());

            assertTrue("width = "+width,MatrixFeatures.isIdentical(A.getMatrix(),A_found.getMatrix(),1e-8));
        }
    }

    @Test
    public void getDiagonal() {
        for( int width = 1; width < 20; width += 2 ) {

            DenseMatrix64F A = RandomMatrices.createSymmetric(width,-1,1,rand);

            TridiagonalSimilarDecomposition<DenseMatrix64F> alg = createDecomposition();

            assertTrue(safeDecomposition(alg,A));

            DenseMatrix64F T = alg.getT(null);

            double diag[] = new double[width];
            double off[] = new double[width];

            alg.getDiagonal(diag,off);
            assertEquals(T.get(0,0),diag[0],1e-8);
            for( int i = 1; i < width; i++ ) {
                assertEquals(T.get(i,i),diag[i],1e-8);
                assertEquals(T.get(i-1,i),off[i-1],1e-8);
            }
        }
    }

    @Test
    public void transposeFlagForQ() {
        for( int width = 1; width < 20; width += 2 ) {

            DenseMatrix64F A = RandomMatrices.createSymmetric(width,-1,1,rand);

            TridiagonalSimilarDecomposition<DenseMatrix64F> alg = createDecomposition();

            assertTrue(safeDecomposition(alg,A));

            DenseMatrix64F Q = alg.getQ(null,false);
            DenseMatrix64F Q_t = alg.getQ(null,true);

            for( int i = 0; i < Q.numRows; i++ ) {
                for( int j = 0; j < Q.numCols; j++ ) {
                    assertEquals(Q.get(i,j),Q_t.get(j,i),1e-8);
                }
            }
        }
    }
}
