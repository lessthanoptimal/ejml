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

package org.ejml.dense.row.misc;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.decomposition.lu.LUDecompositionAlt_FDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


/**
 * @author Peter Abeles
 */
public class TestDeterminantFromMinor_FDRM {

    /**
     * Compare it against the algorithm for 4 by 4 matrices.
     */
    @Test
    public void compareTo4x4() {
        float[] mat = new float[]{5 ,-2 ,-4 ,0.5f, 0.1f, 91, 8, 66, 1, -2, 10, -4, -0.2f, 7, -4, 0.8f};

        float val = NaiveDeterminant.recursive(new FMatrixRMaj(4,4,true,mat));

        DeterminantFromMinor_FDRM minor = new DeterminantFromMinor_FDRM(4,3);
        float minorVal = minor.compute(new FMatrixRMaj(4,4, true, mat));

        assertEquals(val,minorVal, UtilEjml.TEST_F32_SQ);
    }

    /**
     * Compare it against the results found using Octave.
     */
    @Test
    public void compareTo5x5() {
        float[] mat = new float[]{5 ,-2, -4, 0.5f, -0.3f, 0.1f, 91, 8, 66, 13, 1, -2, 10, -4, -0.01f, -0.2f, 7, -4, 0.8f, -22, 5, 19, -23, 0.001f, 87};

        DeterminantFromMinor_FDRM minor = new DeterminantFromMinor_FDRM(5);
        float minorVal = minor.compute(new FMatrixRMaj(5,5, true, mat));

        assertEquals(-4745296.629148000851274f ,minorVal, 100*UtilEjml.TEST_F32_SQ);
    }

    @Test
    public void compareToNaive10x10() {
        Random rand = new Random(0xfff);

        int width = 10;

        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(width,width,rand);

        DeterminantFromMinor_FDRM minor = new DeterminantFromMinor_FDRM(width);
        float minorVal = minor.compute(new FMatrixRMaj(width,width, true, A.data));

        float recVal = NaiveDeterminant.recursive(new FMatrixRMaj(width,width, true, A.data));

        assertEquals(recVal,minorVal,UtilEjml.TEST_F32_SQ);
    }

    /**
     * Compare it against the naive algorithm and see if it gets the same results.
     */
    @Test
    public void computeMediumSized() {
        Random rand = new Random(0xfff);

        for( int width = 5; width < 12; width++ ) {
            FMatrixRMaj A = RandomMatrices_FDRM.rectangle(width,width,rand);

            LUDecompositionAlt_FDRM lu = new LUDecompositionAlt_FDRM();
            lu.decompose(A);

            float luVal = lu.computeDeterminant().real;

            DeterminantFromMinor_FDRM minor = new DeterminantFromMinor_FDRM(width);
            float minorVal = minor.compute(new FMatrixRMaj(width,width, true, A.data));

            assertEquals(luVal,minorVal,UtilEjml.TEST_F32_SQ);
        }
    }

    /**
     * Make sure it produces the same results when it is called twice
     */
    @Test
    public void testMultipleCalls() {
        Random rand = new Random(0xfff);

        int width = 6;

        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(width,width,rand);

        DeterminantFromMinor_FDRM minor = new DeterminantFromMinor_FDRM(width);
        float first = minor.compute(A);
        float second = minor.compute(A);

        assertEquals(first,second,1e-10);

        // does it produce the same results for a different matrix?
        FMatrixRMaj B = RandomMatrices_FDRM.rectangle(width,width,rand);
        float third = minor.compute(B);

        assertFalse(first==third);

        // make sure it has a valid result the third time
        float recVal = NaiveDeterminant.recursive(B);
        assertEquals(third,recVal,UtilEjml.TEST_F32_SQ);
    }
}
