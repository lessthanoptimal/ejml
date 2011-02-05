/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.eig;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.NormOps;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestEigenPowerMethod {

    Random rand = new Random(0x34234);

    /**
     * Test it against a case
     */
    @Test
    public void computeDirect() {
        double dataA[] = new double[]{
                0.499765 ,  0.626231 ,  0.759554,
                0.850879 ,  0.104374 ,  0.247645 ,
                0.069614 ,  0.155754  , 0.380435 };

        DenseMatrix64F A = new DenseMatrix64F(3,3, true, dataA);

        EigenPowerMethod power = new EigenPowerMethod(3);
        power.setOptions(100,1e-10);

        assertTrue(power.computeDirect(A));

        DenseMatrix64F v = power.getEigenVector();

        NormOps.normalizeF(v);

        assertEquals(0.75678,v.get(0,0),1e-6);
        assertEquals(0.62755,v.get(1,0),1e-5);
        assertEquals(0.18295,v.get(2,0),1e-5);
    }

    @Test
    public void computeShiftDirect() {
        double dataA[] = new double[]{
                0.499765 ,  0.626231 ,  0.759554,
                0.850879 ,  0.104374 ,  0.247645 ,
                0.069614 ,  0.155754  , 0.380435 };

        DenseMatrix64F A = new DenseMatrix64F(3,3, true, dataA);

        EigenPowerMethod power = new EigenPowerMethod(3);
        power.setOptions(100,1e-10);

        assertTrue(power.computeShiftDirect(A,0.2));

        DenseMatrix64F v = power.getEigenVector();

        NormOps.normalizeF(v);

        assertEquals(0.75678,v.get(0,0),1e-6);
        assertEquals(0.62755,v.get(1,0),1e-5);
        assertEquals(0.18295,v.get(2,0),1e-5);
    }

    @Test
    public void computeShiftInvert() {
        double dataA[] = new double[]{
                0.499765 ,  0.626231 ,  0.759554,
                0.850879 ,  0.104374 ,  0.247645 ,
                0.069614 ,  0.155754  , 0.380435 };

        DenseMatrix64F A = new DenseMatrix64F(3,3, true, dataA);

        EigenPowerMethod power = new EigenPowerMethod(3);
        power.setOptions(100,1e-10);

        // a tried a few values for psi until I found one that converged
        assertTrue(power.computeShiftInvert(A,1.1));

        DenseMatrix64F v = power.getEigenVector();

        NormOps.normalizeF(v);

        assertEquals(0.75678,v.get(0,0),1e-6);
        assertEquals(0.62755,v.get(1,0),1e-5);
        assertEquals(0.18295,v.get(2,0),1e-5);
    }
}
