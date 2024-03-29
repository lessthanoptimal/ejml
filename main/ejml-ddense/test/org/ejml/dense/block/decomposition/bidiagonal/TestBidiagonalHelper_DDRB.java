/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.block.decomposition.bidiagonal;

import org.ejml.EjmlStandardJUnit;
import org.junit.jupiter.api.Test;


/**
 * @author Peter Abeles
 */
public class TestBidiagonalHelper_DDRB extends EjmlStandardJUnit {
    final static int r = 3;

    @Test
    public void bidiagOuterBlocks() {
//        SimpleMatrix A = SimpleMatrix.random(r*2+r-1,r*2+r-1,-1,1,rand);
//        DMatrixRBlock Ab = DMatrixBlockOps.convert(A.getMatrix(),r);
//
//        A.print();
//        BidiagonalDecompositionRow decompTest = new BidiagonalDecompositionRow();
//        assertTrue( decompTest.decompose(A.getMatrix()) );
//
//        double gammasU[] = new double[ r*3 ];
//        double gammasV[] = new double[ r*3 ];
//
//        BidiagonalHelper.bidiagOuterBlocks(r,new DSubmatrixD1(Ab),gammasU,gammasV);
//
//        for( int i = 0; i < r; i++ ) {
//            assertEquals(decompTest.getGammasU()[i],gammasU[i],UtilEjml.TEST_64F);
//            assertEquals(decompTest.getGammasV()[i],gammasV[i],UtilEjml.TEST_64F);
//        }
//
//        for( int i = 0; i < A.numRows(); i++ ) {
//            for( int j = 0; j < A.numCols(); j++ ) {
//                if( i < r && j < r ) {
//                    assertEquals(A.get(i,j),Ab.get(i,j),UtilEjml.TEST_64F);
//                }
//            }
//        }
    }
}
