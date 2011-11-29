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

package org.ejml.alg.dense.decomposition.bidiagonal;

import org.ejml.alg.dense.decomposition.CheckDecompositionInterface;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public abstract class GenericBidiagonalCheck {
    protected Random rand = new Random(0xff);

    abstract protected BidiagonalDecomposition<DenseMatrix64F> createQRDecomposition();

    @Test
    public void testModifiedInput() {
        CheckDecompositionInterface.checkModifiedInput(createQRDecomposition());
    }

    @Test
    public void testRandomMatrices() {
        BidiagonalDecomposition<DenseMatrix64F> decomp = createQRDecomposition();

        for( int i = 0; i < 10; i++ ) {
            for( int N = 2;  N <= 10; N++ ) {
                for( int tall = 0; tall <= 2; tall++ ) {
                    DenseMatrix64F A = RandomMatrices.createRandom(N+tall,N,rand);

                    assertTrue(decomp.decompose(A.copy()));

                    checkGeneric(A, decomp);
                }
                for( int wide = 1; wide <= 2; wide++ ) {
                    DenseMatrix64F A = RandomMatrices.createRandom(N,N+wide,rand);

                    assertTrue(decomp.decompose(A.copy()));

                    checkGeneric(A, decomp);
                }
            }
        }
    }

    @Test
    public void testIdentity() {
        SimpleMatrix A = SimpleMatrix.identity(5);

        BidiagonalDecomposition<DenseMatrix64F> decomp = createQRDecomposition();

        assertTrue(decomp.decompose(A.getMatrix().copy()));

        checkGeneric(A.getMatrix(), decomp);
    }

    @Test
    public void testZero() {
        SimpleMatrix A = new SimpleMatrix(5,5);

        BidiagonalDecomposition<DenseMatrix64F> decomp = createQRDecomposition();

        assertTrue(decomp.decompose(A.getMatrix().copy()));

        checkGeneric(A.getMatrix(), decomp);
    }

    /**
     * Checks to see if the decomposition will reconstruct the original input matrix
     */
    protected void checkGeneric(DenseMatrix64F a,
                                BidiagonalDecomposition<DenseMatrix64F> decomp) {
        // check the full version
        SimpleMatrix U = SimpleMatrix.wrap(decomp.getU(null,false,false));
        SimpleMatrix B = SimpleMatrix.wrap(decomp.getB(null,false));
        SimpleMatrix V = SimpleMatrix.wrap(decomp.getV(null,false,false));

        DenseMatrix64F foundA = U.mult(B).mult(V.transpose()).getMatrix();

        assertTrue(MatrixFeatures.isIdentical(a,foundA,1e-8));

        //       check with transpose
        SimpleMatrix Ut = SimpleMatrix.wrap(decomp.getU(null,true,false));

        assertTrue(U.transpose().isIdentical(Ut,1e-8));

        SimpleMatrix Vt = SimpleMatrix.wrap(decomp.getV(null,true,false));

        assertTrue(V.transpose().isIdentical(Vt,1e-8));

//        U.print();
//        V.print();
//        B.print();
//        System.out.println("------------------------");

        // now test compact
        U = SimpleMatrix.wrap(decomp.getU(null,false,true));
        B = SimpleMatrix.wrap(decomp.getB(null,true));
        V = SimpleMatrix.wrap(decomp.getV(null,false,true));

//        U.print();
//        V.print();
//        B.print();

        foundA = U.mult(B).mult(V.transpose()).getMatrix();

        assertTrue(MatrixFeatures.isIdentical(a,foundA,1e-8));

        //       check with transpose
        Ut = SimpleMatrix.wrap(decomp.getU(null,true,true));
        Vt = SimpleMatrix.wrap(decomp.getV(null,true,true));

        assertTrue(U.transpose().isIdentical(Ut,1e-8));
        assertTrue(V.transpose().isIdentical(Vt,1e-8));
    }

}
