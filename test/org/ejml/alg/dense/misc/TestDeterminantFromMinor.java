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

package org.ejml.alg.dense.misc;

import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


/**
 * @author Peter Abeles
 */
public class TestDeterminantFromMinor {

    /**
     * Compare it against the algorithm for 4 by 4 matrices.
     */
    @Test
    public void compareTo4x4() {
        double[] mat = new double[]{5 ,-2 ,-4 ,0.5, 0.1, 91, 8, 66, 1, -2, 10, -4, -0.2, 7, -4, 0.8};

        double val = UtilDeterminant.det4by4(mat);

        DeterminantFromMinor minor = new DeterminantFromMinor(4,3);
        double minorVal = minor.compute(new DenseMatrix64F(4,4, true, mat));

        assertEquals(val,minorVal,1e-6);
    }

    /**
     * Compare it against the results found using Octave.
     */
    @Test
    public void compareTo5x5() {
        double[] mat = new double[]{5 ,-2, -4, 0.5, -0.3, 0.1, 91, 8, 66, 13, 1, -2, 10, -4, -0.01, -0.2, 7, -4, 0.8, -22, 5, 19, -23, 0.001, 87};

        DeterminantFromMinor minor = new DeterminantFromMinor(5);
        double minorVal = minor.compute(new DenseMatrix64F(5,5, true, mat));

        assertEquals(-4745296.629148000851274,minorVal,1e-8);
    }

    @Test
    public void compareToNaive10x10() {
        Random rand = new Random(0xfff);

        int width = 10;

        DenseMatrix64F A = RandomMatrices.createRandom(width,width,rand);

        DeterminantFromMinor minor = new DeterminantFromMinor(width);
        double minorVal = minor.compute(new DenseMatrix64F(width,width, true, A.data));

        double recVal = UtilDeterminant.detRecursive(new DenseMatrix64F(width,width, true, A.data));

        assertEquals(recVal,minorVal,1e-6);
    }

    /**
     * Compare it against the naive algorithm and see if it gets the same results.
     */
    @Test
    public void computeMediumSized() {
        Random rand = new Random(0xfff);

        for( int width = 5; width < 12; width++ ) {
            DenseMatrix64F A = RandomMatrices.createRandom(width,width,rand);

            LUDecompositionAlt lu = new LUDecompositionAlt();
            lu.decompose(A);

            double luVal = lu.computeDeterminant();

            DeterminantFromMinor minor = new DeterminantFromMinor(width);
            double minorVal = minor.compute(new DenseMatrix64F(width,width, true, A.data));

            assertEquals(luVal,minorVal,1e-6);
        }
    }

    /**
     * Make sure it produces the same results when it is called twice
     */
    @Test
    public void testMultipleCalls() {
        Random rand = new Random(0xfff);

        int width = 6;

        DenseMatrix64F A = RandomMatrices.createRandom(width,width,rand);

        DeterminantFromMinor minor = new DeterminantFromMinor(width);
        double first = minor.compute(A);
        double second = minor.compute(A);

        assertEquals(first,second,1e-10);

        // does it produce the same results for a different matrix?
        DenseMatrix64F B = RandomMatrices.createRandom(width,width,rand);
        double third = minor.compute(B);

        assertFalse(first==third);

        // make sure it has a valid result the third time
        double recVal = UtilDeterminant.detRecursive(B);
        assertEquals(third,recVal,1e-6);
    }
}
