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

package org.ejml.alg.dense.mult;

import org.ejml.data.DenseMatrix64F;
import org.ejml.data.UtilTestMatrix;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class TestMatrixVectorMult {

    Random rand = new Random(0x7354);

    @Test
    public void checkShapesOfInput() {
        CheckMatrixVectorMultShape check = new CheckMatrixVectorMultShape(MatrixVectorMult.class);
        check.checkAll();
    }

    @Test
    public void mult() {
        double d[] = new double[]{0,1,2,3,4,5};
        DenseMatrix64F a = new DenseMatrix64F(2,3, true, d);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, d);
        DenseMatrix64F c = RandomMatrices.createRandom(2,1,rand);

        MatrixVectorMult.mult(a,b,c);

        UtilTestMatrix.checkMat(c,5,14);
    }

    @Test
    public void multAdd() {
        double d[] = new double[]{0,1,2,3,4,5};
        DenseMatrix64F a = new DenseMatrix64F(2,3, true, d);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, d);
        DenseMatrix64F c = new DenseMatrix64F(2,1, true, 2, 6);

        MatrixVectorMult.multAdd(a,b,c);

        UtilTestMatrix.checkMat(c,7,20);
    }

    @Test
    public void multTransA_small() {
        double d[] = new double[]{0,1,2,3,4,5};
        DenseMatrix64F a = new DenseMatrix64F(3,2, true, d);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, d);
        DenseMatrix64F c = RandomMatrices.createRandom(2,1,rand);

        MatrixVectorMult.multTransA_small(a,b,c);

        UtilTestMatrix.checkMat(c,10,13);
    }

    @Test
    public void multTransA_reorder() {
        double d[] = new double[]{0,1,2,3,4,5};
        DenseMatrix64F a = new DenseMatrix64F(3,2, true, d);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, d);
        DenseMatrix64F c = RandomMatrices.createRandom(2,1,rand);

        MatrixVectorMult.multTransA_reorder(a,b,c);

        UtilTestMatrix.checkMat(c,10,13);
    }

    @Test
    public void multAddTransA_small() {
        double d[] = new double[]{0,1,2,3,4,5};
        DenseMatrix64F a = new DenseMatrix64F(3,2, true, d);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, d);
        DenseMatrix64F c = new DenseMatrix64F(2,1, true, 2, 6);

        MatrixVectorMult.multAddTransA_small(a,b,c);

        UtilTestMatrix.checkMat(c,12,19);
    }

    @Test
    public void multAddTransA_reorder() {
        double d[] = new double[]{0,1,2,3,4,5};
        DenseMatrix64F a = new DenseMatrix64F(3,2, true, d);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, d);
        DenseMatrix64F c = new DenseMatrix64F(2,1, true, 2, 6);

        MatrixVectorMult.multAddTransA_reorder(a,b,c);

        UtilTestMatrix.checkMat(c,12,19);    }
}
