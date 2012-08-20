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

package org.ejml.alg.dense.decomposition.gj;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.SpecializedOps;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestGJDecompositionRowPivot {

    /**
     * Checks to see if it can decompose a square matrix that requires no pivots
     */
    @Test
    public void basicTest() {
        DenseMatrix64F A = new DenseMatrix64F(3,3,true,4,5,6,2,12,20,1,2,3);

        GjDecompositionRowPivot alg = new GjDecompositionRowPivot();

        assertTrue(alg.inputModified());
        assertTrue(alg.decompose(A.copy()));

        DenseMatrix64F found = alg.getDecomposition();
        SpecializedOps.gaussJordanReconstruct(found,alg.getRowPivots());

        assertTrue(MatrixFeatures.isIdentical(A,found,1e-8));
    }

    /**
     * Square matrix which will require pivots
     */
    @Test
    public void checkPivot() {
        DenseMatrix64F A = new DenseMatrix64F(3,3,true,1,2,3,4,5,6,2,12,20);

        GjDecompositionRowPivot alg = new GjDecompositionRowPivot();

        assertTrue(alg.decompose(A.copy()));

        DenseMatrix64F found = alg.getDecomposition();
        SpecializedOps.gaussJordanReconstruct(found,alg.getRowPivots());

        assertTrue(MatrixFeatures.isIdentical(A, found, 1e-8));
    }

    /**
     * Test its ability to detect a singular matrix
     */
    @Test
    public void checkSingular() {
        DenseMatrix64F A = new DenseMatrix64F(3,3,true,1,2,3,2,4,6,2,12,20);

        GjDecompositionRowPivot alg = new GjDecompositionRowPivot();
        alg.setTolerance(1e-15);

        assertFalse(alg.decompose(A));
    }

    @Test
    public void checkTall() {
        DenseMatrix64F A = new DenseMatrix64F(4,3,true,4,10,12,5,2,3,4,8,1,2,5,6);

        GjDecompositionRowPivot alg = new GjDecompositionRowPivot();

        assertTrue(alg.decompose(A.copy()));

        DenseMatrix64F found = alg.getDecomposition();
        SpecializedOps.gaussJordanReconstruct(found,alg.getRowPivots());

        assertTrue(MatrixFeatures.isIdentical(A,found,1e-8));
    }

    @Test
    public void checkWide() {
        DenseMatrix64F A = new DenseMatrix64F(3,4,true,4,10,12,5,2,3,4,8,1,2,5,6);

        GjDecompositionRowPivot alg = new GjDecompositionRowPivot();

        assertTrue(alg.decompose(A.copy()));

        DenseMatrix64F found = alg.getDecomposition();
        SpecializedOps.gaussJordanReconstruct(found,alg.getRowPivots());

        assertTrue(MatrixFeatures.isIdentical(A,found,1e-8));
    }
}
