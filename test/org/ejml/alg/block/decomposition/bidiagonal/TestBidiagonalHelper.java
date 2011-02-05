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

package org.ejml.alg.block.decomposition.bidiagonal;

import org.junit.Test;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class TestBidiagonalHelper {

    final static int r = 3;
    Random rand = new Random(234);


    @Test
    public void bidiagOuterBlocks() {
//        SimpleMatrix A = SimpleMatrix.random(r*2+r-1,r*2+r-1,-1,1,rand);
//        BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);
//
//        A.print();
//        BidiagonalDecompositionRow decompTest = new BidiagonalDecompositionRow();
//        assertTrue( decompTest.decompose(A.getMatrix()) );
//
//        double gammasU[] = new double[ r*3 ];
//        double gammasV[] = new double[ r*3 ];
//
//        BidiagonalHelper.bidiagOuterBlocks(r,new D1Submatrix64F(Ab),gammasU,gammasV);
//
//        for( int i = 0; i < r; i++ ) {
//            assertEquals(decompTest.getGammasU()[i],gammasU[i],1e-8);
//            assertEquals(decompTest.getGammasV()[i],gammasV[i],1e-8);
//        }
//
//        for( int i = 0; i < A.numRows(); i++ ) {
//            for( int j = 0; j < A.numCols(); j++ ) {
//                if( i < r && j < r ) {
//                    assertEquals(A.get(i,j),Ab.get(i,j),1e-8);
//                }
//            }
//        }
    }
}
