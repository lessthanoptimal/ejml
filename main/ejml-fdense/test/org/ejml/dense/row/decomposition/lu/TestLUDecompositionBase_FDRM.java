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

package org.ejml.dense.row.decomposition.lu;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.misc.DeterminantFromMinor_FDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestLUDecompositionBase_FDRM {
    Random rand = new Random(0x3344);

    /**
     * Compare the determinant computed from LU to the value computed from the minor
     * matrix method.
     */
    @Test
    public void testDeterminant()
    {
        Random rand = new Random(0xfff);

        int width = 10;

        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(width,width,rand);

        DeterminantFromMinor_FDRM minor = new DeterminantFromMinor_FDRM(width);
        float minorVal = minor.compute(A);

        LUDecompositionAlt_FDRM alg = new LUDecompositionAlt_FDRM();
        alg.decompose(A);
        float luVal = alg.computeDeterminant().real;

        assertEquals(minorVal,luVal, UtilEjml.TEST_F32_SQ);
    }

    @Test
    public void _solveVectorInternal() {
        int width = 10;
        FMatrixRMaj LU = RandomMatrices_FDRM.rectangle(width,width,rand);

        FMatrixRMaj L = new FMatrixRMaj(width,width);
        FMatrixRMaj U = new FMatrixRMaj(width,width);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                float real = LU.get(i, j);
                if( j <= i ) {
                    if( j == i )
                        L.set(i,j,1);
                    else
                        L.set(i,j,real);
                }
                if( i <= j ) {
                    U.set(i,j,real);
                }
            }
        }

        FMatrixRMaj x = RandomMatrices_FDRM.rectangle(width, 1, -1, 1, rand);
        FMatrixRMaj tmp = new FMatrixRMaj(width,1);
        FMatrixRMaj b = new FMatrixRMaj(width,1);

        CommonOps_FDRM.mult(U, x, tmp);
        CommonOps_FDRM.mult(L,tmp,b);


        DebugDecompose alg = new DebugDecompose(width);
        for( int i = 0; i < width; i++ ) alg.getIndx()[i] = i;
        alg.setLU(LU);

        alg._solveVectorInternal(b.data);

        for( int i = 0; i < width; i++ ) {
            assertEquals(x.data[i],b.data[i],UtilEjml.TEST_F32_SQ);
        }
    }

    private static class DebugDecompose extends LUDecompositionBase_FDRM
    {
        public DebugDecompose(int width) {
            setExpectedMaxSize(width, width);
            m = n = width;
        }

        void setLU( FMatrixRMaj LU ) {
            this.LU = LU;
            this.dataLU = LU.data;
        }

        @Override
        public boolean decompose(FMatrixRMaj orig) {
            return false;
        }
    }
}