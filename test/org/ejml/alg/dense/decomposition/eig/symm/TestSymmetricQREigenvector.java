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

package org.ejml.alg.dense.decomposition.eig.symm;

import org.ejml.data.DenseMatrix64F;
import org.ejml.data.SimpleMatrix;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestSymmetricQREigenvector {

    @Test
    public void testAll() {
        DenseMatrix64F A = CommonOps.diag(2,3,4,5,6);
        for( int i = 1; i < A.numCols; i++ ) {
            A.set(i-1,i,i+0.5);
            A.set(i,i-1,i+0.5);
        }

        A.print();

        SymmetricQREigenvalue value = new SymmetricQREigenvalue();
        value.process(A);

        SymmetricQREigenvector vector = new SymmetricQREigenvector();
        vector.process(A,value.getValues());

        SimpleMatrix Q = SimpleMatrix.wrap(vector.getQ());

        SimpleMatrix D = SimpleMatrix.wrap(CommonOps.diagR(5,5,value.getValues()));

        SimpleMatrix A_found = Q.transpose().mult(D).mult(Q);

        D.print();
        Q.print();

        A_found.print();
        A.print();

        assertTrue(MatrixFeatures.isIdentical(A,A_found.getMatrix(),1e-8));

    }

}
