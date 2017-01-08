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

package org.ejml.alg.dense.decompose.hessenberg;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.RowMatrix_C64;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition_F64;
import org.ejml.ops.CommonOps_CR64;
import org.ejml.ops.MatrixFeatures_CR64;
import org.ejml.ops.RandomMatrices_CR64;
import org.junit.Test;

import java.util.Random;

import static org.ejml.alg.dense.decompose.CheckDecompositionInterface_CR64.safeDecomposition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public abstract class StandardTridiagonalTests_CR64 {

    protected Random rand = new Random(2344);

    protected abstract TridiagonalSimilarDecomposition_F64<RowMatrix_C64> createDecomposition();

    @Test
    public void fullTest() {

        for( int width = 1; width < 20; width += 2 ) {

            RowMatrix_C64 A = RandomMatrices_CR64.createHermitian(width,-1,1,rand);

            TridiagonalSimilarDecomposition_F64<RowMatrix_C64> alg = createDecomposition();


            assertTrue(safeDecomposition(alg,A));

            // test the results using the decomposition's definition
            RowMatrix_C64 Q = alg.getQ(null,false);
            RowMatrix_C64 T = alg.getT(null);

            RowMatrix_C64 tmp = new RowMatrix_C64(width,width);
            RowMatrix_C64 A_found = new RowMatrix_C64(width,width);

            CommonOps_CR64.mult(Q,T,tmp);
            CommonOps_CR64.multTransB(tmp,Q,A_found);

            assertTrue("width = "+width, MatrixFeatures_CR64.isIdentical(A,A_found,UtilEjml.TEST_F64));
        }
    }

    @Test
    public void getDiagonal() {
        for( int width = 1; width < 20; width += 2 ) {

            RowMatrix_C64 A = RandomMatrices_CR64.createHermitian(width,-1,1,rand);

            TridiagonalSimilarDecomposition_F64<RowMatrix_C64> alg = createDecomposition();

            assertTrue(safeDecomposition(alg,A));

            RowMatrix_C64 T = alg.getT(null);

            double diag[] = new double[width*2];
            double off[] = new double[width*2];

            alg.getDiagonal(diag,off);
            assertEquals(T.getReal(0,0)     ,diag[0],UtilEjml.TEST_F64);
            assertEquals(T.getImag(0,0),diag[1],UtilEjml.TEST_F64);
            for( int i = 1; i < width; i++ ) {
                assertEquals(T.getReal(i,i)  , diag[i*2]     ,UtilEjml.TEST_F64);
                assertEquals(T.getImag(i,i)  , diag[i*2+1]   , UtilEjml.TEST_F64);
                assertEquals(T.getReal(i-1,i), off[(i-1)*2]  ,UtilEjml.TEST_F64);
                assertEquals(T.getImag(i-1,i), off[(i-1)*2+1],UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void transposeFlagForQ() {
        for( int width = 1; width < 20; width += 2 ) {

            RowMatrix_C64 A = RandomMatrices_CR64.createHermitian(width,-1,1,rand);

            TridiagonalSimilarDecomposition_F64<RowMatrix_C64> alg = createDecomposition();

            assertTrue(safeDecomposition(alg,A));

            RowMatrix_C64 Q = alg.getQ(null,false);
            RowMatrix_C64 Q_t = alg.getQ(null,true);

            Complex_F64 q = new Complex_F64();
            for( int i = 0; i < Q.numRows; i++ ) {
                for( int j = 0; j < Q.numCols; j++ ) {
                    assertEquals(Q.getReal(i,j),Q_t.getReal(j,i),UtilEjml.TEST_F64);
                    assertEquals(Q.getImag(i,j),-Q_t.getImag(j,i),UtilEjml.TEST_F64);
                }
            }
        }
    }
}
