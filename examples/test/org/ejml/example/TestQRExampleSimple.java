/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

package org.ejml.example;import org.ejml.data.SimpleMatrix;
import org.ejml.example.QRExampleSimple;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestQRExampleSimple {

    Random rand = new Random(23423);


    @Test
    public void basic() {
        checkMatrix(7,5);
        checkMatrix(5,5);
        checkMatrix(7,7);
    }

    private void checkMatrix( int numRows , int numCols ) {
        SimpleMatrix A = SimpleMatrix.random(numRows,numCols,-1,1,rand);

        QRExampleSimple alg = new QRExampleSimple();

        alg.decompose(A);

        SimpleMatrix Q = alg.getQ();
        SimpleMatrix R = alg.getR();

        SimpleMatrix A_found = Q.mult(R);

        assertTrue( A.isIdentical(A_found,1e-8));
    }


}
