/*
 * Copyright (c) 2009-2013, Peter Abeles. All Rights Reserved.
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

package org.ejml.example;import org.ejml.simple.SimpleMatrix;
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
