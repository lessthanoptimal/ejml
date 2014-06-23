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

import org.ejml.alg.dense.decomposition.TriangularSolver;
import org.ejml.alg.dense.misc.DeterminantFromMinor;
import org.ejml.data.DenseMatrix64F;
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
        double luVal = alg.computeDeterminant();

        assertEquals(minorVal,luVal,1e-6);
    }

    @Test
    public void _solveVectorInternal() {
        int width = 10;
        DenseMatrix64F LU = RandomMatrices.createRandom(width,width,rand);

        double a[] = new double[]{1,2,3,4,5,6,7,8,9,10};
        double b[] = new double[]{1,2,3,4,5,6,7,8,9,10};
        for( int i = 0; i < width; i++ ) LU.set(i,i,1);

        TriangularSolver.solveL(LU.data,a,width);
        TriangularSolver.solveU(LU.data,a,width);

        DebugDecompose alg = new DebugDecompose(width);
        for( int i = 0; i < width; i++ ) alg.getIndx()[i] = i;
        alg.setLU(LU);
        
        alg._solveVectorInternal(b);

        for( int i = 0; i < width; i++ ) {
            assertEquals(a[i],b[i],1e-6);
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