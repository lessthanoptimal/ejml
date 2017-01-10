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

package org.ejml.alg.dense.misc;

import org.ejml.EjmlUnitTests;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.ops.RandomMatrices_R64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestTransposeAlgs_R64 {

    Random rand = new Random(234234);

    @Test
    public void square() {
        DMatrixRow_F64 mat = RandomMatrices_R64.createRandom(5,5,rand);
        DMatrixRow_F64 matTran = mat.copy();

        TransposeAlgs_R64.square(matTran);

        assertEquals(mat.getNumCols(),matTran.getNumRows());
        assertEquals(mat.getNumRows(),matTran.getNumCols());

        EjmlUnitTests.assertEqualsTrans(mat,matTran,0);
    }

    @Test
    public void block() {
        // check various shapes to make sure blocking is handled correctly
        for( int numRows = 1; numRows < 15; numRows += 2 ) {
            for( int numCols = 1; numCols < 15; numCols += 2) {
                DMatrixRow_F64 mat = RandomMatrices_R64.createRandom(numRows,numCols,rand);
                DMatrixRow_F64 matTran = new DMatrixRow_F64(numCols,numRows);

                TransposeAlgs_R64.block(mat,matTran,7);

                assertEquals(numCols,matTran.getNumRows());
                assertEquals(numRows,matTran.getNumCols());

                EjmlUnitTests.assertEqualsTrans(mat,matTran,0);
            }
        }
    }

    @Test
    public void standard() {
        DMatrixRow_F64 mat = RandomMatrices_R64.createRandom(5,7,rand);
        DMatrixRow_F64 matTran = new DMatrixRow_F64(7,5);

        TransposeAlgs_R64.standard(mat,matTran);

        assertEquals(mat.getNumCols(),matTran.getNumRows());
        assertEquals(mat.getNumRows(),matTran.getNumCols());

        EjmlUnitTests.assertEqualsTrans(mat,matTran,0);
    }
    
}
