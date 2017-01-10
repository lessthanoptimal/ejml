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

package org.ejml.sparse;

import org.ejml.UtilEjml;
import org.ejml.data.RowMatrix_F64;
import org.ejml.data.SMatrixTriplet_64;
import org.ejml.ops.MatrixFeatures_R64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestConvertSparseMatrix_F64 {

    @Test
    public void DMatrixRow_SMatrixTriplet() {
        RowMatrix_F64 a = new RowMatrix_F64(3,5);

        a.set(0,0, 50);
        a.set(2,3, -0.3);

        DMatrixRow_SMatrixTriplet(a,null);
        DMatrixRow_SMatrixTriplet(a, new SMatrixTriplet_64(1,1,2));
    }

    public void DMatrixRow_SMatrixTriplet(RowMatrix_F64 a , SMatrixTriplet_64 b ) {
        b = ConvertSparseMatrix_F64.convert(a,b);

        for (int row = 0; row < a.numRows; row++) {
            for (int col = 0; col < a.numCols; col++) {
                SMatrixTriplet_64.Element e = b.find(row,col);

                if( a.get(row,col) == 0.0 ) {
                    assertTrue( null == e );
                } else {
                    assertEquals( a.get(row,col), e.value, UtilEjml.TEST_F64);
                }
            }
        }

        // now try it the other direction
        RowMatrix_F64 c = ConvertSparseMatrix_F64.convert(b,null);
        assertTrue(MatrixFeatures_R64.isEquals(a,c, UtilEjml.TEST_F64));

        c = ConvertSparseMatrix_F64.convert(b,new RowMatrix_F64(1,1));
        assertTrue(MatrixFeatures_R64.isEquals(a,c, UtilEjml.TEST_F64));
    }

    @Test
    public void SMatrixTriplet_SMatrixCC() {

    }
}
