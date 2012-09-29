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

package org.ejml.alg.dense.decomposition.svd;

import org.ejml.data.DenseMatrix64F;
import org.ejml.data.Matrix64F;
import org.ejml.factory.SingularValueDecomposition;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestSafeSvd {

    @Test
    public void getSafety() {
        DenseMatrix64F A = new DenseMatrix64F(3,4);

        // it will need to create a copy in this case
        Dummy dummy = new Dummy(2,true,true,2,3);

        SingularValueDecomposition decomp = new SafeSvd(dummy);
        assertFalse(decomp.inputModified());

        decomp.decompose(A);

        assertTrue(A != dummy.passedInMatrix);

        // now no need to make a copy
        dummy = new Dummy(2,true,false,2,3);
        decomp = new SafeSvd(dummy);
        assertFalse(decomp.inputModified());

        decomp.decompose(A);

        assertTrue(A == dummy.passedInMatrix);
    }

    @Test
    public void checkOtherFunctions() {
        Dummy dummy = new Dummy(2,true,true,2,3);

        SingularValueDecomposition decomp = new SafeSvd(dummy);

        assertTrue(decomp.isCompact());
        assertEquals(2, decomp.numberOfSingularValues());

        assertFalse(dummy.getU_called);
        assertFalse(dummy.getV_called);
        assertFalse(dummy.getW_called);

        decomp.getU(null,false);
        assertTrue(dummy.getU_called);
        decomp.getV(null, false);
        assertTrue(dummy.getV_called);
        decomp.getW(null);
        assertTrue(dummy.getW_called);

        assertEquals(2,decomp.numCols());
        assertEquals(3,decomp.numRows());
    }

    protected static class Dummy implements SingularValueDecomposition {

        Matrix64F passedInMatrix;

        boolean compact;
        double singular[];
        boolean getU_called;
        boolean getV_called;
        boolean getW_called;

        int numRow,numCol;
        boolean inputModified;

        private Dummy( int numSingular ,
                       boolean compact,
                       boolean inputModified,
                       int numCol, int numRow) {
            singular = new double[ numSingular ];
            this.compact = compact;
            this.inputModified = inputModified;
            this.numCol = numCol;
            this.numRow = numRow;
        }

        @Override
        public double[] getSingularValues() {
            return singular;
        }

        @Override
        public int numberOfSingularValues() {
            return singular.length;
        }

        @Override
        public boolean isCompact() {
            return compact;
        }

        @Override
        public Matrix64F getU(Matrix64F U, boolean transposed) {
            getU_called = true;
            return null;
        }

        @Override
        public Matrix64F getV(Matrix64F V, boolean transposed) {
            getV_called = true;
            return null;
        }

        @Override
        public Matrix64F getW(Matrix64F W) {
            getW_called = true;
            return null;
        }

        @Override
        public int numRows() {
            return numRow;
        }

        @Override
        public int numCols() {
            return numCol;
        }

        @Override
        public boolean decompose(Matrix64F orig) {
            this.passedInMatrix = orig;
            return true;
        }

        @Override
        public boolean inputModified() {
            return inputModified;
        }
    }
}
