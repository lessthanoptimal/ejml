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

package org.ejml.alg.dense.decomposition.lu;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.misc.DeterminantFromMinor_D64;
import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.CommonOps_D64;
import org.ejml.ops.RandomMatrices_D64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestLUDecompositionBase_D64 {
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

        RowMatrix_F64 A = RandomMatrices_D64.createRandom(width,width,rand);

        DeterminantFromMinor_D64 minor = new DeterminantFromMinor_D64(width);
        double minorVal = minor.compute(A);

        LUDecompositionAlt_D64 alg = new LUDecompositionAlt_D64();
        alg.decompose(A);
        double luVal = alg.computeDeterminant().real;

        assertEquals(minorVal,luVal, UtilEjml.TEST_F64_SQ);
    }

    @Test
    public void _solveVectorInternal() {
        int width = 10;
        RowMatrix_F64 LU = RandomMatrices_D64.createRandom(width,width,rand);

        RowMatrix_F64 L = new RowMatrix_F64(width,width);
        RowMatrix_F64 U = new RowMatrix_F64(width,width);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                double real = LU.get(i, j);
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

        RowMatrix_F64 x = RandomMatrices_D64.createRandom(width, 1, -1, 1, rand);
        RowMatrix_F64 tmp = new RowMatrix_F64(width,1);
        RowMatrix_F64 b = new RowMatrix_F64(width,1);

        CommonOps_D64.mult(U, x, tmp);
        CommonOps_D64.mult(L,tmp,b);


        DebugDecompose alg = new DebugDecompose(width);
        for( int i = 0; i < width; i++ ) alg.getIndx()[i] = i;
        alg.setLU(LU);

        alg._solveVectorInternal(b.data);

        for( int i = 0; i < width; i++ ) {
            assertEquals(x.data[i],b.data[i],UtilEjml.TEST_F64_SQ);
        }
    }

    private static class DebugDecompose extends LUDecompositionBase_D64
    {
        public DebugDecompose(int width) {
            setExpectedMaxSize(width, width);
            m = n = width;
        }

        void setLU( RowMatrix_F64 LU ) {
            this.LU = LU;
            this.dataLU = LU.data;
        }

        @Override
        public boolean decompose(RowMatrix_F64 orig) {
            return false;
        }
    }
}