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

package org.ejml.equation;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestMatrixConstructor {

    Random rand = new Random(234);

    @Test
    public void basicTest() {
        DenseMatrix64F A = RandomMatrices.createRandom(10,8,rand);

        DenseMatrix64F B = CommonOps.extract(A,0,5,0,3);
        DenseMatrix64F C = CommonOps.extract(A,0,5,3,8);
        DenseMatrix64F D = CommonOps.extract(A,5,10,0,8);

        MatrixConstructor alg = new MatrixConstructor(new ManagerTempVariables());

        alg.addToRow(new VariableMatrix(B));
        alg.addToRow(new VariableMatrix(C));
        alg.endRow();
        alg.addToRow(new VariableMatrix(D));

        alg.construct();

        DenseMatrix64F found = alg.getOutput().matrix;
        assertTrue(MatrixFeatures.isIdentical(A, found, 1e-8));
    }

    @Test
    public void setToRequiredSize_matrix() {
        MatrixConstructor alg = new MatrixConstructor(new ManagerTempVariables());

        alg.addToRow(new VariableMatrix(new DenseMatrix64F(2, 3)));
        alg.addToRow(new VariableMatrix(new DenseMatrix64F(2, 4)));
        alg.endRow();
        alg.addToRow(new VariableMatrix(new DenseMatrix64F(1, 7)));
        alg.endRow();

        DenseMatrix64F a = new DenseMatrix64F(1,1);

        alg.setToRequiredSize(a);

        assertEquals(7,a.numCols);
        assertEquals(3,a.numRows);
    }

    @Test
    public void setToRequiredSize_scalar() {
        MatrixConstructor alg = new MatrixConstructor(new ManagerTempVariables());

        alg.addToRow(new VariableDouble(123));
        alg.addToRow(new VariableDouble(1));
        alg.endRow();

        DenseMatrix64F a = new DenseMatrix64F(1,1);

        alg.setToRequiredSize(a);

        assertEquals(2,a.numCols);
        assertEquals(1,a.numRows);
    }
}