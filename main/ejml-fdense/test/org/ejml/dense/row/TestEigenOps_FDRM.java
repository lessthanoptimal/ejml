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

package org.ejml.dense.row;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F32;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.factory.DecompositionFactory_FDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition_F32;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestEigenOps_FDRM {

    Random rand = new Random(12344);

    /**
     * Compute an eigen value and compare against a known solution from octave.
     */
    @Test
    public void computeEigenValue() {
        FMatrixRMaj A = new FMatrixRMaj(3,3,
                true, 0.053610f, 0.030405f, 0.892620f, 0.090954f, 0.074065f, 0.875797f, 0.105369f, 0.928981f, 0.965506f);

        FMatrixRMaj u = new FMatrixRMaj(3,1,
                true, -0.4502917f, -0.4655377f, -0.7619134f);

        float value = EigenOps_FDRM.computeEigenValue(A,u);

        assertEquals(1.59540f,value,1e-4);
    }

    /**
     * Give it a matrix that describes a Markov process and see if it produces 1
     */
    @Test
    public void boundLargestEigenValue_markov() {
        // create the matrix
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(3,3,rand);

        for( int i = 0; i < 3; i++ ) {
            float total = 0;
            for( int j = 0; j < 3; j++ ) {
                total += A.get(i,j);
            }

            for( int j = 0; j < 3; j++ ) {
                A.set(i,j,A.get(i,j)/total);
            }
        }

        float[] val = EigenOps_FDRM.boundLargestEigenValue(A,null);

        assertEquals(1.0f,val[0], UtilEjml.TEST_F32);
        assertEquals(1.0f,val[1],UtilEjml.TEST_F32);
    }

    @Test
    public void createMatrixV() {
        FMatrixRMaj A = RandomMatrices_FDRM.symmetric(3,-1,1,rand);

        EigenDecomposition_F32<FMatrixRMaj> decomp = DecompositionFactory_FDRM.eig(A.numRows,true);
        assertTrue(decomp.decompose(A));

        FMatrixRMaj V = EigenOps_FDRM.createMatrixV(decomp);

        for( int i = 0; i < 3; i++ ) {
            FMatrixRMaj v = decomp.getEigenVector(i);

            for( int j = 0; j < 3; j++ ) {
                assertEquals(V.get(j,i),v.get(j),UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void createMatrixD() {
        FMatrixRMaj A = RandomMatrices_FDRM.symmetric(3,-1,1,rand);

        EigenDecomposition_F32<FMatrixRMaj> decomp = DecompositionFactory_FDRM.eig(A.numRows,true);
        assertTrue(decomp.decompose(A));

        FMatrixRMaj D = EigenOps_FDRM.createMatrixD(decomp);

        for( int i = 0; i < 3; i++ ) {
            Complex_F32 e = decomp.getEigenvalue(i);

            if( e.isReal() ) {
                assertEquals(e.real,D.get(i,i),1e-10);
            }
        }
    }
}
