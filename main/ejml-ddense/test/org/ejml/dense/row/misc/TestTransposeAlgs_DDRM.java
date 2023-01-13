/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlStandardJUnit;
import org.ejml.EjmlUnitTests;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTransposeAlgs_DDRM extends EjmlStandardJUnit {
    @Test void square() {
        DMatrixRMaj mat = RandomMatrices_DDRM.rectangle(5,5,rand);
        DMatrixRMaj matTran = mat.copy();

        TransposeAlgs_DDRM.square(matTran);

        assertEquals(mat.getNumCols(),matTran.getNumRows());
        assertEquals(mat.getNumRows(),matTran.getNumCols());

        EjmlUnitTests.assertEqualsTrans(mat,matTran,0);
    }

    @Test void block() {
        // check various shapes to make sure blocking is handled correctly
        for( int numRows = 1; numRows < 15; numRows += 2 ) {
            for( int numCols = 1; numCols < 15; numCols += 2) {
                DMatrixRMaj mat = RandomMatrices_DDRM.rectangle(numRows,numCols,rand);
                DMatrixRMaj matTran = new DMatrixRMaj(numCols,numRows);

                TransposeAlgs_DDRM.block(mat,matTran,7);

                assertEquals(numCols,matTran.getNumRows());
                assertEquals(numRows,matTran.getNumCols());

                EjmlUnitTests.assertEqualsTrans(mat,matTran,0);
            }
        }
    }

    @Test void standard() {
        DMatrixRMaj mat = RandomMatrices_DDRM.rectangle(5,7,rand);
        DMatrixRMaj matTran = new DMatrixRMaj(7,5);

        TransposeAlgs_DDRM.standard(mat,matTran);

        assertEquals(mat.getNumCols(),matTran.getNumRows());
        assertEquals(mat.getNumRows(),matTran.getNumCols());

        EjmlUnitTests.assertEqualsTrans(mat,matTran,0);
    }
    
}
