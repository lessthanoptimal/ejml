/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.bidiagonal;

import org.ejml.UtilEjml;
import org.ejml.data.D1MatrixRow_64;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.interfaces.decomposition.BidiagonalDecomposition_F64;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.MatrixFeatures_R64;
import org.ejml.ops.RandomMatrices_R64;
import org.ejml.ops.SpecializedOps_R64;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestBidiagonalDecompositionRow_R64 extends GenericBidiagonalCheck_R64 {


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
    }

    private void checkNaive(int m, int n) {
        SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_R64.createRandom(m,n,rand));

        BidiagonalDecompositionRow_R64 decomp = new BidiagonalDecompositionRow_R64();
        BidiagonalDecompositionNaive_R64 naive = new BidiagonalDecompositionNaive_R64();

        assertTrue(decomp.decompose(A.matrix_F64().copy()));
        assertTrue(naive.decompose(A.matrix_F64()));

        SimpleMatrix U = SimpleMatrix.wrap(decomp.getU(null,false,false));
        SimpleMatrix B = SimpleMatrix.wrap(decomp.getB(null,false));
        SimpleMatrix V = SimpleMatrix.wrap(decomp.getV(null,false,false));

//        U.print();
//        B.print();
//        naive.getB().print();
//        V.print();
//        naive.getV().print();

//        naive.getVTran().print();

        assertTrue(naive.getB().isIdentical(B,UtilEjml.TEST_F64));
        assertTrue(naive.getU().isIdentical(U,UtilEjml.TEST_F64));
        assertTrue(naive.getV().isIdentical(V,UtilEjml.TEST_F64));

        // check the decomposition
        DMatrixRow_F64 foundA = U.mult(B).mult(V.transpose()).matrix_F64();

//        A.print();
//        foundA.print();

        assertTrue(MatrixFeatures_R64.isIdentical(A.matrix_F64(),foundA, UtilEjml.TEST_F64));
    }

    @Test
    public void testComputeU()
    {
        int m = 7;
        int n = 5;

        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(m,n,rand);

        DebugBidiagonal alg = new DebugBidiagonal(A);

        DMatrixRow_F64 B = new DMatrixRow_F64(A);

        DMatrixRow_F64 C = new DMatrixRow_F64(m,n);
        DMatrixRow_F64 u = new DMatrixRow_F64(m,1);

        D1MatrixRow_64 UBV = alg.getUBV();

        for( int i = 0; i < n; i++ ) {
            alg.computeU(i);

            SpecializedOps_R64.subvector(UBV,i+1,i,m-i-1,false,i+1,u);
            u.data[i] = 1;

            DMatrixRow_F64 Q = SpecializedOps_R64.createReflector(u,alg.getGammasU()[i]);

            CommonOps_R64.mult(Q,B,C);

//            u.print();
//            B.print();
//            UBV.print();
//            C.print();

            B.set(C);

            // make sure everything is as expected
            for( int j = i+1; j < m; j++ ) {
                assertEquals(0,C.get(j,i),UtilEjml.TEST_F64);
            }

            for( int j = i+1; j < n; j++ ) {
                assertEquals(UBV.get(i,j),C.get(i,j),UtilEjml.TEST_F64);
            }
            u.data[i] = 0;
        }
    }

    @Test
    public void testComputeV()
    {
        int m = 7;
        int n = 5;

        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(m,n,rand);

        DebugBidiagonal alg = new DebugBidiagonal(A);

        DMatrixRow_F64 B = new DMatrixRow_F64(A);

        DMatrixRow_F64 C = new DMatrixRow_F64(m,n);
        DMatrixRow_F64 u = new DMatrixRow_F64(n,1);

        D1MatrixRow_64 UBV = alg.getUBV();

//        A.print();

        for( int i = 0; i < n-2; i++ ) {
            alg.computeV(i);

            u.zero();
            SpecializedOps_R64.subvector(UBV,i,i+2,n-i-2,true,i+2,u);
            u.data[i+1] = 1;

            DMatrixRow_F64 Q = SpecializedOps_R64.createReflector(u,alg.getGammasV()[i]);

//            Q.print();

            CommonOps_R64.mult(B,Q,C);

//            u.print();
//            B.print();
//            UBV.print();
//            C.print();

            B.set(C);

            // make sure everything is as expected
            for( int j = i+2; j < n; j++ ) {
                assertEquals(0,C.get(i,j),UtilEjml.TEST_F64);
            }

            for( int j = i+2; j < m; j++ ) {
                assertEquals(UBV.get(j,i),C.get(j,i),UtilEjml.TEST_F64);
            }
            u.data[i] = 0;
        }

    }

    @Override
    protected BidiagonalDecomposition_F64<DMatrixRow_F64> createQRDecomposition() {
        return new BidiagonalDecompositionRow_R64();
    }

    private static class DebugBidiagonal extends BidiagonalDecompositionRow_R64 {


        public DebugBidiagonal( DMatrixRow_F64 A ) {
            init(A.copy());
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
