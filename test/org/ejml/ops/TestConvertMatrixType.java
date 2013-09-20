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

package org.ejml.ops;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.data.*;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestConvertMatrixType {

    Random rand = new Random(234);

    @Test
    public void any_to_any() {
        DenseMatrix64F a = new DenseMatrix64F(2,3,true,1,2,3,4,5,6);
        DenseMatrix64F b = new DenseMatrix64F(2,3);

        ConvertMatrixType.convert((Matrix64F)a,(Matrix64F)b);

        assertTrue(MatrixFeatures.isIdentical(a,b,1e-12));
    }

    @Test
    public void Fixed3x3_to_DM() {
        FixedMatrix3x3_64F a = new FixedMatrix3x3_64F(1,2,3,4,5,6,7,8,9);
        DenseMatrix64F b = new DenseMatrix64F(3,3);

        ConvertMatrixType.convert(a,b);

        checkIdentical(a,b);
    }

    @Test
    public void DM_to_Fixed3x3() {
        DenseMatrix64F a = new DenseMatrix64F(3,3,true,1,2,3,4,5,6,7,8,9);
        FixedMatrix3x3_64F b = new FixedMatrix3x3_64F();

        ConvertMatrixType.convert(a,b);

        checkIdentical(a,b);
    }

    @Test
    public void Fixed3_to_DM() {
        FixedMatrix3_64F a = new FixedMatrix3_64F(1,2,3);
        DenseMatrix64F b = new DenseMatrix64F(3,1);

        ConvertMatrixType.convert(a,b);

        checkIdenticalV(a,b);

        b = new DenseMatrix64F(1,3);

        ConvertMatrixType.convert(a,b);

        checkIdenticalV(a,b);
    }

    @Test
    public void DM_to_Fixed3() {
        DenseMatrix64F a = new DenseMatrix64F(3,1,true,1,2,3);
        FixedMatrix3_64F b = new FixedMatrix3_64F();

        ConvertMatrixType.convert(a,b);

        checkIdenticalV(a,b);

        a = new DenseMatrix64F(1,3,true,2,3,4);
        b = new FixedMatrix3_64F();

        ConvertMatrixType.convert(a,b);

        checkIdenticalV(a,b);
    }

    @Test
    public void BM_to_DM() {
        for( int rows = 1; rows <= 8; rows++ ) {
            for( int cols = 1; cols <= 8; cols++ ) {
                BlockMatrix64F a = BlockMatrixOps.createRandom(rows,cols,-1,2,rand);
                DenseMatrix64F b = new DenseMatrix64F(rows,cols);

                ConvertMatrixType.convert(a,b);

                checkIdentical(a,b);
            }
        }
    }

    @Test
    public void DM_to_BM() {
        for( int rows = 1; rows <= 8; rows++ ) {
            for( int cols = 1; cols <= 8; cols++ ) {
                DenseMatrix64F a = RandomMatrices.createRandom(rows,cols,rand);
                BlockMatrix64F b = new BlockMatrix64F(rows,cols,3);

                ConvertMatrixType.convert(a,b);

                checkIdentical(a,b);
            }
        }
    }


    private void checkIdentical( Matrix64F a , Matrix64F b ) {
        for( int i = 0; i < a.getNumRows(); i++  ) {
            for( int j = 0; j < a.getNumCols(); j++ ) {
                assertEquals(a.get(i,j),b.get(i,j),1e-8);
            }
        }
    }

    private void checkIdenticalV( Matrix64F a , Matrix64F b ) {
        boolean columnVectorA = a.getNumRows() > a.getNumCols();
        boolean columnVectorB = b.getNumRows() > b.getNumCols();

        int length = Math.max(a.getNumRows(),b.getNumRows());

        for( int i = 0; i < length; i++  ) {

            double valueA,valueB;

            if( columnVectorA )
                valueA = a.get(i,0);
            else
                valueA = a.get(0,i);

            if( columnVectorB )
                valueB = b.get(i,0);
            else
                valueB = b.get(0,i);

            assertEquals(valueA,valueB,1e-8);
        }



    }

}
