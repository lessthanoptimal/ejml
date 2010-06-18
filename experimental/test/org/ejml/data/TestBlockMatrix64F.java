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

package org.ejml.data;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestBlockMatrix64F {

    @Test
    public void testGeneric() {
        GenericTestsD1Matrix64F g;
        g = new GenericTestsD1Matrix64F() {
            protected D1Matrix64F createMatrix(int numRows, int numCols) {
                return new BlockMatrix64F(numRows,numCols);
            }
        };

        g.allTests();
    }

    @Test
    public void setDenseMatrix64F() {
        double d[][] = new double[][]{{1,2},{3,4},{5,6}};

        DenseMatrix64F rowMat = new DenseMatrix64F(d);

        BlockMatrix64F blockMat;

        // try differnt sizes of blocks and see if they all work
        blockMat = new BlockMatrix64F(rowMat.numRows,rowMat.numCols,10);
        blockMat.set(rowMat);
        checkIdentical(blockMat,rowMat);

        blockMat = new BlockMatrix64F(rowMat.numRows,rowMat.numCols,2);
        blockMat.set(rowMat);
        checkIdentical(blockMat,rowMat);

        blockMat = new BlockMatrix64F(rowMat.numRows,rowMat.numCols,3);
        blockMat.set(rowMat);
        checkIdentical(blockMat,rowMat);
    }

    private void checkIdentical( Matrix64F a , Matrix64F b ) {
        assertTrue(a.getNumRows() == b.getNumRows());
        assertTrue(a.getNumCols() == b.getNumCols());

        int numRows = a.getNumRows();
        int numCols = a.getNumCols();

        for( int i = 0; i < numRows; i++ ) {
            for( int j = 0; j < numCols; j++ ) {
                assertTrue(a.get(i,j)==b.get(i,j));
            }
        }
    }
}
