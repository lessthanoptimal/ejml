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

package org.ejml.alg.dense.decomposition.bidiagonal;

import org.ejml.data.DenseMatrix64F;
import org.ejml.data.RowD1Matrix64F;
import org.ejml.data.SimpleMatrix;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.ejml.ops.SpecializedOps;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestBidiagonalDecompositionRow {

    Random rand = new Random(6455);

    /**
     * See if the modify and don't modify flags are respected
     */
    @Test
    public void testModify() {
        SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices.createRandom(3,5,rand));
        SimpleMatrix A_mod = A.copy();

        BidiagonalDecompositionRow decomp = new BidiagonalDecompositionRow();
        assertTrue(decomp.decompose(A_mod.getMatrix(),true));

        assertFalse(A.isIdentical(A_mod,1e-8));

        A_mod.set(A);
        assertTrue(decomp.decompose(A_mod.getMatrix(),false));
        assertTrue(A.isIdentical(A_mod,1e-10));
    }

    /**
     * See if the naive implementation and this version produce the same results.
     */
    @Test
    public void testAgainstNaive() {
        for( int i = 1; i <= 5; i++ ) {
            for( int j = 1; j <= 5; j++ ) {
                checkNaive(i,j);
            }
        }

//        checkNaive(2, 3);
    }

    private void checkNaive(int m, int n) {
        SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices.createRandom(m,n,rand));

        BidiagonalDecompositionRow decomp = new BidiagonalDecompositionRow();
        BidiagonalDecompositionNaive naive = new BidiagonalDecompositionNaive();

        assertTrue(decomp.decompose(A.getMatrix(),false));
        assertTrue(naive.decompose(A.getMatrix()));

        SimpleMatrix U = SimpleMatrix.wrap(decomp.getU(null,false,false));
        SimpleMatrix B = SimpleMatrix.wrap(decomp.getB(null,false));
        SimpleMatrix V = SimpleMatrix.wrap(decomp.getV(null,false,false));

//        U.print();
//        B.print();
//        naive.getB().print();
//        V.print();
//        naive.getV().print();

//        naive.getVTran().print();

        assertTrue(naive.getB().isIdentical(B,1e-8));
        assertTrue(naive.getU().isIdentical(U,1e-8));
        assertTrue(naive.getV().isIdentical(V,1e-8));

        // check the decomposition
        DenseMatrix64F foundA = U.mult(B).mult(V.transpose()).getMatrix();

//        A.print();
//        foundA.print();

        assertTrue(MatrixFeatures.isIdentical(A.getMatrix(),foundA,1e-8));
    }

    @Test
    public void testRandomMatrices() {
        BidiagonalDecompositionRow decomp = new BidiagonalDecompositionRow();

        for( int i = 0; i < 10; i++ ) {
            for( int N = 2;  N <= 10; N++ ) {
                for( int tall = 0; tall <= 2; tall++ ) {
                    DenseMatrix64F A = RandomMatrices.createRandom(N+tall,N,rand);

                    assertTrue(decomp.decompose(A,false));

                    checkGeneric(A, decomp);
                }
                for( int wide = 1; wide <= 2; wide++ ) {
                    DenseMatrix64F A = RandomMatrices.createRandom(N,N+wide,rand);

                    assertTrue(decomp.decompose(A,false));

                    checkGeneric(A, decomp);
                }
            }
        }
    }

    @Test
    public void testIdentity() {
        SimpleMatrix A = SimpleMatrix.identity(5);

        BidiagonalDecompositionRow decomp = new BidiagonalDecompositionRow();

        assertTrue(decomp.decompose(A.getMatrix(),false));

        checkGeneric(A.getMatrix(), decomp);
    }

    private void checkGeneric(DenseMatrix64F a, BidiagonalDecompositionRow decomp) {
        // check the full version
        SimpleMatrix U = SimpleMatrix.wrap(decomp.getU(null,false,false));
        SimpleMatrix B = SimpleMatrix.wrap(decomp.getB(null,false));
        SimpleMatrix V = SimpleMatrix.wrap(decomp.getV(null,false,false));

        DenseMatrix64F foundA = U.mult(B).mult(V.transpose()).getMatrix();

        assertTrue(MatrixFeatures.isIdentical(a,foundA,1e-8));

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

        Ut = SimpleMatrix.wrap(decomp.getU(null,true,true));
        Vt = SimpleMatrix.wrap(decomp.getV(null,true,true));

        assertTrue(U.transpose().isIdentical(Ut,1e-8));
        assertTrue(V.transpose().isIdentical(Vt,1e-8));
    }

    @Test
    public void testZero() {
        SimpleMatrix A = new SimpleMatrix(5,5);

        BidiagonalDecompositionRow decomp = new BidiagonalDecompositionRow();

        assertTrue(decomp.decompose(A.getMatrix(),false));

        checkGeneric(A.getMatrix(), decomp);
    }

    @Test
    public void testComputeU()
    {
        int m = 7;
        int n = 5;

        DenseMatrix64F A = RandomMatrices.createRandom(m,n,rand);

        DebugBidiagonal alg = new DebugBidiagonal(A);

        DenseMatrix64F B = new DenseMatrix64F(A);

        DenseMatrix64F C = new DenseMatrix64F(m,n);
        DenseMatrix64F u = new DenseMatrix64F(m,1);

        RowD1Matrix64F UBV = alg.getUBV();

        for( int i = 0; i < n; i++ ) {
            alg.computeU(i);

            SpecializedOps.subvector(UBV,i+1,i,m-i-1,false,i+1,u);
            u.data[i] = 1;

            DenseMatrix64F Q = SpecializedOps.createReflector(u,alg.getGammasU()[i]);

            CommonOps.mult(Q,B,C);

//            u.print();
//            B.print();
//            UBV.print();
//            C.print();

            B.set(C);

            // make sure everything is as expected
            for( int j = i+1; j < m; j++ ) {
                assertEquals(0,C.get(j,i),1e-8);
            }

            for( int j = i+1; j < n; j++ ) {
                assertEquals(UBV.get(i,j),C.get(i,j),1e-8);
            }
            u.data[i] = 0;
        }
    }

    @Test
    public void testComputeV()
    {
        int m = 7;
        int n = 5;

        DenseMatrix64F A = RandomMatrices.createRandom(m,n,rand);

        DebugBidiagonal alg = new DebugBidiagonal(A);

        DenseMatrix64F B = new DenseMatrix64F(A);

        DenseMatrix64F C = new DenseMatrix64F(m,n);
        DenseMatrix64F u = new DenseMatrix64F(n,1);

        RowD1Matrix64F UBV = alg.getUBV();

//        A.print();

        for( int i = 0; i < n-2; i++ ) {
            alg.computeV(i);

            u.zero();
            SpecializedOps.subvector(UBV,i,i+2,n-i-2,true,i+2,u);
            u.data[i+1] = 1;

            DenseMatrix64F Q = SpecializedOps.createReflector(u,alg.getGammasV()[i]);

//            Q.print();

            CommonOps.mult(B,Q,C);

//            u.print();
//            B.print();
//            UBV.print();
//            C.print();

            B.set(C);

            // make sure everything is as expected
            for( int j = i+2; j < n; j++ ) {
                assertEquals(0,C.get(i,j),1e-8);
            }

            for( int j = i+2; j < m; j++ ) {
                assertEquals(UBV.get(j,i),C.get(j,i),1e-8);
            }
            u.data[i] = 0;
        }

    }

    private static class DebugBidiagonal extends BidiagonalDecompositionRow {


        public DebugBidiagonal( DenseMatrix64F A ) {
            init(A,false);
        }

        @Override
        protected void computeU(int k) {
            super.computeU(k);
        }

        @Override
        protected void computeV(int k) {
            super.computeV(k);
        }
    }
}
