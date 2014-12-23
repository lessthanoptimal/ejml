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

package org.ejml.alg.dense.decomposition.lu;

import org.ejml.alg.dense.misc.DeterminantFromMinor;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;
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

        DenseMatrix64F A = RandomMatrices.createRandom(width,width,rand);

        DeterminantFromMinor minor = new DeterminantFromMinor(width);
        double minorVal = minor.compute(A);

        LUDecompositionAlt_D64 alg = new LUDecompositionAlt_D64();
        alg.decompose(A);
        double luVal = alg.computeDeterminant().real;

        assertEquals(minorVal,luVal,1e-6);
    }

    @Test
    public void _solveVectorInternal() {
        int width = 10;
        DenseMatrix64F LU = RandomMatrices.createRandom(width,width,rand);

        DenseMatrix64F L = new DenseMatrix64F(width,width);
        DenseMatrix64F U = new DenseMatrix64F(width,width);

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

        DenseMatrix64F x = RandomMatrices.createRandom(width, 1, -1, 1, rand);
        DenseMatrix64F tmp = new DenseMatrix64F(width,1);
        DenseMatrix64F b = new DenseMatrix64F(width,1);

        CommonOps.mult(U, x, tmp);
        CommonOps.mult(L,tmp,b);


        DebugDecompose alg = new DebugDecompose(width);
        for( int i = 0; i < width; i++ ) alg.getIndx()[i] = i;
        alg.setLU(LU);

        alg._solveVectorInternal(b.data);

        for( int i = 0; i < width; i++ ) {
            assertEquals(x.data[i],b.data[i],1e-6);
        }
    }

    private static class DebugDecompose extends LUDecompositionBase_D64
    {
        public DebugDecompose(int width) {
            setExpectedMaxSize(width, width);
            m = n = width;
        }

        void setLU( DenseMatrix64F LU ) {
            this.LU = LU;
            this.dataLU = LU.data;
        }

        @Override
        public boolean decompose(DenseMatrix64F orig) {
            return false;
        }
    }
}