/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decompose;

import org.ejml.data.CDenseMatrix64F;
import org.ejml.ops.CCommonOps;
import org.ejml.ops.CMatrixFeatures;
import org.ejml.ops.CRandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestCTriangularSolver {

    Random rand = new Random(234);

    @Test
    public void solveU() {
        CDenseMatrix64F U = CRandomMatrices.createRandom(3, 3, -1 ,1 ,rand);
        for( int i = 0; i < U.numRows; i++ ) {
            for( int j = 0; j < i; j++ ) {
                U.set(i,j,0,0);
            }
        }

        CDenseMatrix64F X = CRandomMatrices.createRandom(3, 1, -1 ,1 ,rand);
        CDenseMatrix64F B = new CDenseMatrix64F(3,1);

        CCommonOps.mult(U, X, B);

        CTriangularSolver.solveU(U.data,B.data,3);

        assertTrue(CMatrixFeatures.isIdentical(X, B, 1e-8));
    }
}