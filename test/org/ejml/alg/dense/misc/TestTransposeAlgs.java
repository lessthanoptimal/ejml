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

package org.ejml.alg.dense.misc;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.EjmlUnitTests;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestTransposeAlgs {

    Random rand = new Random(234234);

    @Test
    public void square() {
        DenseMatrix64F mat = RandomMatrices.createRandom(5,5,rand);
        DenseMatrix64F matTran = mat.copy();

        TransposeAlgs.square(matTran);

        assertEquals(mat.getNumCols(),matTran.getNumRows());
        assertEquals(mat.getNumRows(),matTran.getNumCols());

        EjmlUnitTests.assertEqualsTrans(mat,matTran,0);
    }

    @Test
    public void block() {
        // check various shapes to make sure blocking is handled correctly
        for( int numRows = 1; numRows < 15; numRows += 2 ) {
            for( int numCols = 1; numCols < 15; numCols += 2) {
                DenseMatrix64F mat = RandomMatrices.createRandom(numRows,numCols,rand);
                DenseMatrix64F matTran = new DenseMatrix64F(numCols,numRows);

                TransposeAlgs.block(mat,matTran,7);

                assertEquals(numCols,matTran.getNumRows());
                assertEquals(numRows,matTran.getNumCols());

                EjmlUnitTests.assertEqualsTrans(mat,matTran,0);
            }
        }
    }

    @Test
    public void standard() {
        DenseMatrix64F mat = RandomMatrices.createRandom(5,7,rand);
        DenseMatrix64F matTran = new DenseMatrix64F(7,5);

        TransposeAlgs.standard(mat,matTran);

        assertEquals(mat.getNumCols(),matTran.getNumRows());
        assertEquals(mat.getNumRows(),matTran.getNumCols());

        EjmlUnitTests.assertEqualsTrans(mat,matTran,0);
    }
    
}
