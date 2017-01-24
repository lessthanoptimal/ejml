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

package org.ejml.dense.row.misc;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.lu.LUDecompositionAlt_DDRM;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


/**
 * @author Peter Abeles
 */
public class TestDeterminantFromMinor_DDRM {

    /**
     * Compare it against the algorithm for 4 by 4 matrices.
     */
    @Test
    public void compareTo4x4() {
        double[] mat = new double[]{5 ,-2 ,-4 ,0.5, 0.1, 91, 8, 66, 1, -2, 10, -4, -0.2, 7, -4, 0.8};

        double val = NaiveDeterminant.recursive(new DMatrixRMaj(4,4,true,mat));

        DeterminantFromMinor_DDRM minor = new DeterminantFromMinor_DDRM(4,3);
        double minorVal = minor.compute(new DMatrixRMaj(4,4, true, mat));

        assertEquals(val,minorVal, UtilEjml.TEST_F64_SQ);
    }

    /**
     * Compare it against the results found using Octave.
     */
    @Test
    public void compareTo5x5() {
        double[] mat = new double[]{5 ,-2, -4, 0.5, -0.3, 0.1, 91, 8, 66, 13, 1, -2, 10, -4, -0.01, -0.2, 7, -4, 0.8, -22, 5, 19, -23, 0.001, 87};

        DeterminantFromMinor_DDRM minor = new DeterminantFromMinor_DDRM(5);
        double minorVal = minor.compute(new DMatrixRMaj(5,5, true, mat));

        assertEquals(-4745296.629148000851274 ,minorVal, 100*UtilEjml.TEST_F64_SQ);
    }

    @Test
    public void compareToNaive10x10() {
        Random rand = new Random(0xfff);

        int width = 10;

        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(width,width,rand);

        DeterminantFromMinor_DDRM minor = new DeterminantFromMinor_DDRM(width);
        double minorVal = minor.compute(new DMatrixRMaj(width,width, true, A.data));

        double recVal = NaiveDeterminant.recursive(new DMatrixRMaj(width,width, true, A.data));

        assertEquals(recVal,minorVal,UtilEjml.TEST_F64_SQ);
    }

    /**
     * Compare it against the naive algorithm and see if it gets the same results.
     */
    @Test
    public void computeMediumSized() {
        Random rand = new Random(0xfff);

        for( int width = 5; width < 12; width++ ) {
            DMatrixRMaj A = RandomMatrices_DDRM.rectangle(width,width,rand);

            LUDecompositionAlt_DDRM lu = new LUDecompositionAlt_DDRM();
            lu.decompose(A);

            double luVal = lu.computeDeterminant().real;

            DeterminantFromMinor_DDRM minor = new DeterminantFromMinor_DDRM(width);
            double minorVal = minor.compute(new DMatrixRMaj(width,width, true, A.data));

            assertEquals(luVal,minorVal,UtilEjml.TEST_F64_SQ);
        }
    }

    /**
     * Make sure it produces the same results when it is called twice
     */
    @Test
    public void testMultipleCalls() {
        Random rand = new Random(0xfff);

        int width = 6;

        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(width,width,rand);

        DeterminantFromMinor_DDRM minor = new DeterminantFromMinor_DDRM(width);
        double first = minor.compute(A);
        double second = minor.compute(A);

        assertEquals(first,second,1e-10);

        // does it produce the same results for a different matrix?
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(width,width,rand);
        double third = minor.compute(B);

        assertFalse(first==third);

        // make sure it has a valid result the third time
        double recVal = NaiveDeterminant.recursive(B);
        assertEquals(third,recVal,UtilEjml.TEST_F64_SQ);
    }
}
