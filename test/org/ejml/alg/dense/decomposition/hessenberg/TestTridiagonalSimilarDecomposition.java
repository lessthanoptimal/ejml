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

package org.ejml.alg.dense.decomposition.hessenberg;

import org.ejml.data.SimpleMatrix;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.ejml.alg.dense.decomposition.CheckDecompositionInterface.safeDecomposition;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestTridiagonalSimilarDecomposition {

    Random rand = new Random(234345);

    @Test
    public void fullTest() {
        SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices.createSymmetric(5,-1,1,rand));

        TridiagonalDecompositionHouseholder alg = new TridiagonalDecompositionHouseholder();

        assertTrue(safeDecomposition(alg,A.getMatrix()));

        SimpleMatrix Q = SimpleMatrix.wrap(alg.getQ(null,false));
        SimpleMatrix T = SimpleMatrix.wrap(alg.getT(null));


//        A.print();
//        alg.getQT().print();
//        T.print();
        
//        System.out.println("Reconstructed diagonal");
//        Q.transpose().mult(A).mult(Q).print();
        SimpleMatrix A_found = Q.mult(T).mult(Q.transpose());

        assertTrue(MatrixFeatures.isIdentical(A.getMatrix(),A_found.getMatrix(),1e-8));
    }
}
