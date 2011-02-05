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
