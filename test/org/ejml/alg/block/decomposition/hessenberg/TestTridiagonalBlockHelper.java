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

package org.ejml.alg.block.decomposition.hessenberg;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.D1Submatrix64F;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestTridiagonalBlockHelper {

    Random rand = new Random(234324);
    int r = 3;


    @Test
    public void applyReflectorsToRow() {
        SimpleMatrix A = SimpleMatrix.random(2*r+2,2*r+2,-1,1,rand);
        A = A.mult(A.transpose());
        SimpleMatrix A_orig = A.copy();
        SimpleMatrix V = SimpleMatrix.random(r,A.numCols(),-1,1,rand);

        BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);
        BlockMatrix64F Vb = BlockMatrixOps.convert(V.getMatrix(),r);

        int row = r-1;

        // manually apply "reflectors" to A
        for( int i = 0; i < row; i++ ) {
            SimpleMatrix u = A_orig.extractVector(true,i);
            SimpleMatrix v = V.extractVector(true,i);

            for( int j = 0; j <= i; j++ ) {
                u.set(j,0.0);
            }
            u.set(i+1,1.0);

            A = A.minus(u.mult(v.transpose())).minus(v.mult(u.transpose()));
        }

        TridiagonalBlockHelper.applyReflectorsToRow(r,new D1Submatrix64F(Ab,0,r,0,A.numCols()),
                new D1Submatrix64F(Vb),row);
        for( int i = row; i < A.numCols(); i++ ) {
            assertEquals(A.get(row,i),Ab.get(row,i),1e-8);
        }
    }

    @Test
    public void multA_u() {
        SimpleMatrix A = SimpleMatrix.random(2*r+2,2*r+2,-1,1,rand);

        BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);
        BlockMatrix64F V = new BlockMatrix64F(r,Ab.numCols,r);

        int row = 1;

        SimpleMatrix u = A.extractVector(true,row);
        for( int i = 0; i <= row; i++ ) {
            u.set(i,0);
        }
        u.set(row+1,1);

        SimpleMatrix v = A.mult(u).transpose();

        TridiagonalBlockHelper.multA_u(r,new D1Submatrix64F(Ab),
                new D1Submatrix64F(V),row);

        for( int i = row+1; i < A.numCols(); i++ ) {
            assertEquals(v.get(i),V.get(row,i),1e-8);
        }
    }
}
