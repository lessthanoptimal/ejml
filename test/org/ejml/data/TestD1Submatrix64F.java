/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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

package org.ejml.data;

import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestD1Submatrix64F {

    Random rand = new Random(234234);

    @Test
    public void get() {
        DenseMatrix64F A = RandomMatrices.createRandom(5,10,-1,1,rand);

        D1Submatrix64F S = new D1Submatrix64F(A,2,4,1,10);

        assertEquals(A.get(3,2),S.get(1,1),1e-8);
    }

    @Test
    public void set() {
        DenseMatrix64F A = RandomMatrices.createRandom(5,10,-1,1,rand);

        D1Submatrix64F S = new D1Submatrix64F(A,2,4,1,10);

        S.set(1,1,5);

        assertEquals(A.get(3,2),5,1e-8);
    }

    @Test
    public void extract() {
        DenseMatrix64F A = RandomMatrices.createRandom(5,10,-1,1,rand);

        D1Submatrix64F S = new D1Submatrix64F(A,2,4,1,10);

        SimpleMatrix M = S.extract();

        DenseMatrix64F E = CommonOps.extract(A,2,4,1,10);

        assertTrue(MatrixFeatures.isEquals(E,M.getMatrix()));
    }
}
