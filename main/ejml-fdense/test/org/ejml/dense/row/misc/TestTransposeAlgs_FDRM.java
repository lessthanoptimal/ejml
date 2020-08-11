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

import org.ejml.EjmlUnitTests;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestTransposeAlgs_FDRM {

    Random rand = new Random(234234);

    @Test
    public void square() {
        FMatrixRMaj mat = RandomMatrices_FDRM.rectangle(5,5,rand);
        FMatrixRMaj matTran = mat.copy();

        TransposeAlgs_FDRM.square(matTran);

        assertEquals(mat.getNumCols(),matTran.getNumRows());
        assertEquals(mat.getNumRows(),matTran.getNumCols());

        EjmlUnitTests.assertEqualsTrans(mat,matTran,0);
    }

    @Test
    public void block() {
        // check various shapes to make sure blocking is handled correctly
        for( int numRows = 1; numRows < 15; numRows += 2 ) {
            for( int numCols = 1; numCols < 15; numCols += 2) {
                FMatrixRMaj mat = RandomMatrices_FDRM.rectangle(numRows,numCols,rand);
                FMatrixRMaj matTran = new FMatrixRMaj(numCols,numRows);

                TransposeAlgs_FDRM.block(mat,matTran,7);

                assertEquals(numCols,matTran.getNumRows());
                assertEquals(numRows,matTran.getNumCols());

                EjmlUnitTests.assertEqualsTrans(mat,matTran,0);
            }
        }
    }

    @Test
    public void standard() {
        FMatrixRMaj mat = RandomMatrices_FDRM.rectangle(5,7,rand);
        FMatrixRMaj matTran = new FMatrixRMaj(7,5);

        TransposeAlgs_FDRM.standard(mat,matTran);

        assertEquals(mat.getNumCols(),matTran.getNumRows());
        assertEquals(mat.getNumRows(),matTran.getNumCols());

        EjmlUnitTests.assertEqualsTrans(mat,matTran,0);
    }
    
}
