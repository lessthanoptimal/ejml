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
import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition_F64;
import org.ejml.ops.CommonOps_CD64;
import org.ejml.ops.MatrixFeatures_CD64;
import org.ejml.ops.RandomMatrices_CD64;
import org.junit.Test;

import java.util.Random;

import static org.ejml.alg.dense.decompose.CheckDecompositionInterface_CD64.safeDecomposition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public abstract class StandardTridiagonalTests_CD64 {

    protected Random rand = new Random(2344);

    protected abstract TridiagonalSimilarDecomposition_F64<CDenseMatrix64F> createDecomposition();

    @Test
    public void fullTest() {

        for( int width = 1; width < 20; width += 2 ) {

            CDenseMatrix64F A = RandomMatrices_CD64.createHermitian(width,-1,1,rand);

            TridiagonalSimilarDecomposition_F64<CDenseMatrix64F> alg = createDecomposition();


            assertTrue(safeDecomposition(alg,A));

            // test the results using the decomposition's definition
            CDenseMatrix64F Q = alg.getQ(null,false);
            CDenseMatrix64F T = alg.getT(null);

            CDenseMatrix64F tmp = new CDenseMatrix64F(width,width);
            CDenseMatrix64F A_found = new CDenseMatrix64F(width,width);

            CommonOps_CD64.mult(Q,T,tmp);
            CommonOps_CD64.multTransB(tmp,Q,A_found);

            assertTrue("width = "+width, MatrixFeatures_CD64.isIdentical(A,A_found,UtilEjml.TEST_64F));
        }
    }

    @Test
    public void getDiagonal() {
        for( int width = 1; width < 20; width += 2 ) {

            CDenseMatrix64F A = RandomMatrices_CD64.createHermitian(width,-1,1,rand);

            TridiagonalSimilarDecomposition_F64<CDenseMatrix64F> alg = createDecomposition();

            assertTrue(safeDecomposition(alg,A));

            CDenseMatrix64F T = alg.getT(null);

            double diag[] = new double[width*2];
            double off[] = new double[width*2];

            alg.getDiagonal(diag,off);
            assertEquals(T.getReal(0,0)     ,diag[0],UtilEjml.TEST_64F);
            assertEquals(T.getImag(0,0),diag[1],UtilEjml.TEST_64F);
            for( int i = 1; i < width; i++ ) {
                assertEquals(T.getReal(i,i)  , diag[i*2]     ,UtilEjml.TEST_64F);
                assertEquals(T.getImag(i,i)  , diag[i*2+1]   , UtilEjml.TEST_64F);
                assertEquals(T.getReal(i-1,i), off[(i-1)*2]  ,UtilEjml.TEST_64F);
                assertEquals(T.getImag(i-1,i), off[(i-1)*2+1],UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void transposeFlagForQ() {
        for( int width = 1; width < 20; width += 2 ) {

            CDenseMatrix64F A = RandomMatrices_CD64.createHermitian(width,-1,1,rand);

            TridiagonalSimilarDecomposition_F64<CDenseMatrix64F> alg = createDecomposition();

            assertTrue(safeDecomposition(alg,A));

            CDenseMatrix64F Q = alg.getQ(null,false);
            CDenseMatrix64F Q_t = alg.getQ(null,true);

            Complex64F q = new Complex64F();
            for( int i = 0; i < Q.numRows; i++ ) {
                for( int j = 0; j < Q.numCols; j++ ) {
                    assertEquals(Q.getReal(i,j),Q_t.getReal(j,i),UtilEjml.TEST_64F);
                    assertEquals(Q.getImag(i,j),-Q_t.getImag(j,i),UtilEjml.TEST_64F);
                }
            }
        }
    }
}
