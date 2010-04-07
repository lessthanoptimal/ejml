/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
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
public class TestLUDecompositionBase {
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

        LUDecompositionAlt alg = new LUDecompositionAlt();
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

    private static class DebugDecompose extends LUDecompositionBase
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