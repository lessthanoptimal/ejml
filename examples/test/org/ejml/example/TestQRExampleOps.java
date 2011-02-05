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

package org.ejml.example;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestQRExampleOps {

    Random rand = new Random(23423);


    @Test
    public void basic() {
        checkMatrix(7,5);
        checkMatrix(5,5);
        checkMatrix(7,7);
    }

    private void checkMatrix( int numRows , int numCols ) {
        DenseMatrix64F A = RandomMatrices.createRandom(numRows,numCols,-1,1,rand);

        QRExampleOps alg = new QRExampleOps();

        alg.decompose(A);

        DenseMatrix64F Q = alg.getQ();
        DenseMatrix64F R = alg.getR();

        DenseMatrix64F A_found = new DenseMatrix64F(numRows,numCols);
        CommonOps.mult(Q,R,A_found);

        assertTrue( MatrixFeatures.isIdentical(A,A_found,1e-8));
    }


}