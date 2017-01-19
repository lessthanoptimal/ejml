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

package org.ejml.dense.row.decompose.hessenberg;

import org.ejml.UtilEjml;
import org.ejml.data.ZComplex;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.dense.row.MatrixFeatures_ZDRM;
import org.ejml.dense.row.RandomMatrices_ZDRM;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecompositionD;
import org.junit.Test;

import java.util.Random;

import static org.ejml.dense.row.decompose.CheckDecompositionInterface_ZDRM.safeDecomposition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public abstract class StandardTridiagonalTests_ZDRM {

    protected Random rand = new Random(2344);

    protected abstract TridiagonalSimilarDecompositionD<ZMatrixRMaj> createDecomposition();

    @Test
    public void fullTest() {

        for( int width = 1; width < 20; width += 2 ) {

            ZMatrixRMaj A = RandomMatrices_ZDRM.hermitian(width,-1,1,rand);

            TridiagonalSimilarDecompositionD<ZMatrixRMaj> alg = createDecomposition();


            assertTrue(safeDecomposition(alg,A));

            // test the results using the decomposition's definition
            ZMatrixRMaj Q = alg.getQ(null,false);
            ZMatrixRMaj T = alg.getT(null);

            ZMatrixRMaj tmp = new ZMatrixRMaj(width,width);
            ZMatrixRMaj A_found = new ZMatrixRMaj(width,width);

            CommonOps_ZDRM.mult(Q,T,tmp);
            CommonOps_ZDRM.multTransB(tmp,Q,A_found);

            assertTrue("width = "+width, MatrixFeatures_ZDRM.isIdentical(A,A_found,UtilEjml.TEST_F64));
        }
    }

    @Test
    public void getDiagonal() {
        for( int width = 1; width < 20; width += 2 ) {

            ZMatrixRMaj A = RandomMatrices_ZDRM.hermitian(width,-1,1,rand);

            TridiagonalSimilarDecompositionD<ZMatrixRMaj> alg = createDecomposition();

            assertTrue(safeDecomposition(alg,A));

            ZMatrixRMaj T = alg.getT(null);

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

            ZMatrixRMaj A = RandomMatrices_ZDRM.hermitian(width,-1,1,rand);

            TridiagonalSimilarDecompositionD<ZMatrixRMaj> alg = createDecomposition();

            assertTrue(safeDecomposition(alg,A));

            ZMatrixRMaj Q = alg.getQ(null,false);
            ZMatrixRMaj Q_t = alg.getQ(null,true);

            ZComplex q = new ZComplex();
            for( int i = 0; i < Q.numRows; i++ ) {
                for( int j = 0; j < Q.numCols; j++ ) {
                    assertEquals(Q.getReal(i,j),Q_t.getReal(j,i),UtilEjml.TEST_F64);
                    assertEquals(Q.getImag(i,j),-Q_t.getImag(j,i),UtilEjml.TEST_F64);
                }
            }
        }
    }
}
