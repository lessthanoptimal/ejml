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

package org.ejml.dense.row.decompose.hessenberg;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F32;
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.MatrixFeatures_CDRM;
import org.ejml.dense.row.RandomMatrices_CDRM;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition_F32;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.ejml.dense.row.decompose.CheckDecompositionInterface_CDRM.safeDecomposition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public abstract class StandardTridiagonalTests_CDRM {

    protected Random rand = new Random(2344);

    protected abstract TridiagonalSimilarDecomposition_F32<CMatrixRMaj> createDecomposition();

    @Test
    public void fullTest() {

        for( int width = 1; width < 20; width += 2 ) {

            CMatrixRMaj A = RandomMatrices_CDRM.hermitian(width,-1,1,rand);

            TridiagonalSimilarDecomposition_F32<CMatrixRMaj> alg = createDecomposition();


            assertTrue(safeDecomposition(alg,A));

            // test the results using the decomposition's definition
            CMatrixRMaj Q = alg.getQ(null,false);
            CMatrixRMaj T = alg.getT(null);

            CMatrixRMaj tmp = new CMatrixRMaj(width,width);
            CMatrixRMaj A_found = new CMatrixRMaj(width,width);

            CommonOps_CDRM.mult(Q,T,tmp);
            CommonOps_CDRM.multTransB(tmp,Q,A_found);

            assertTrue(MatrixFeatures_CDRM.isIdentical(A,A_found,UtilEjml.TEST_F32),"width = "+width);
        }
    }

    @Test
    public void getDiagonal() {
        for( int width = 1; width < 20; width += 2 ) {

            CMatrixRMaj A = RandomMatrices_CDRM.hermitian(width,-1,1,rand);

            TridiagonalSimilarDecomposition_F32<CMatrixRMaj> alg = createDecomposition();

            assertTrue(safeDecomposition(alg,A));

            CMatrixRMaj T = alg.getT(null);

            float diag[] = new float[width*2];
            float off[] = new float[width*2];

            alg.getDiagonal(diag,off);
            assertEquals(T.getReal(0,0)     ,diag[0],UtilEjml.TEST_F32);
            assertEquals(T.getImag(0,0),diag[1],UtilEjml.TEST_F32);
            for( int i = 1; i < width; i++ ) {
                assertEquals(T.getReal(i,i)  , diag[i*2]     ,UtilEjml.TEST_F32);
                assertEquals(T.getImag(i,i)  , diag[i*2+1]   , UtilEjml.TEST_F32);
                assertEquals(T.getReal(i-1,i), off[(i-1)*2]  ,UtilEjml.TEST_F32);
                assertEquals(T.getImag(i-1,i), off[(i-1)*2+1],UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void transposeFlagForQ() {
        for( int width = 1; width < 20; width += 2 ) {

            CMatrixRMaj A = RandomMatrices_CDRM.hermitian(width,-1,1,rand);

            TridiagonalSimilarDecomposition_F32<CMatrixRMaj> alg = createDecomposition();

            assertTrue(safeDecomposition(alg,A));

            CMatrixRMaj Q = alg.getQ(null,false);
            CMatrixRMaj Q_t = alg.getQ(null,true);

            Complex_F32 q = new Complex_F32();
            for( int i = 0; i < Q.numRows; i++ ) {
                for( int j = 0; j < Q.numCols; j++ ) {
                    assertEquals(Q.getReal(i,j),Q_t.getReal(j,i),UtilEjml.TEST_F32);
                    assertEquals(Q.getImag(i,j),-Q_t.getImag(j,i),UtilEjml.TEST_F32);
                }
            }
        }
    }
}
