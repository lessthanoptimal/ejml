/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.decomposition.bidiagonal;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrix1Row;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.SpecializedOps_DDRM;
import org.ejml.interfaces.decomposition.BidiagonalDecomposition_F64;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestBidiagonalDecompositionRow_DDRM extends GenericBidiagonalCheck_DDRM {
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
        SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_DDRM.rectangle(m,n,rand));

        BidiagonalDecompositionRow_DDRM decomp = new BidiagonalDecompositionRow_DDRM();
        BidiagonalDecompositionNaive_DDRM naive = new BidiagonalDecompositionNaive_DDRM();

        assertTrue(decomp.decompose(A.getDDRM().copy()));
        assertTrue(naive.decompose(A.getDDRM()));

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
        DMatrixRMaj foundA = U.mult(B).mult(V.transpose()).getDDRM();

//        A.print();
//        foundA.print();

        assertTrue(MatrixFeatures_DDRM.isIdentical(A.getDDRM(),foundA, UtilEjml.TEST_F64));
    }

    @Test
    public void testComputeU()
    {
        int m = 7;
        int n = 5;

        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(m,n,rand);

        DebugBidiagonal alg = new DebugBidiagonal(A);

        DMatrixRMaj B = new DMatrixRMaj(A);

        DMatrixRMaj C = new DMatrixRMaj(m,n);
        DMatrixRMaj u = new DMatrixRMaj(m,1);

        DMatrix1Row UBV = alg.getUBV();

        for( int i = 0; i < n; i++ ) {
            alg.computeU(i);

            SpecializedOps_DDRM.subvector(UBV,i+1,i,m-i-1,false,i+1,u);
            u.data[i] = 1;

            DMatrixRMaj Q = SpecializedOps_DDRM.createReflector(u,alg.getGammasU()[i]);

            CommonOps_DDRM.mult(Q,B,C);

//            u.print();
//            B.print();
//            UBV.print();
//            C.print();

            B.setTo(C);

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

        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(m,n,rand);

        DebugBidiagonal alg = new DebugBidiagonal(A);

        DMatrixRMaj B = new DMatrixRMaj(A);

        DMatrixRMaj C = new DMatrixRMaj(m,n);
        DMatrixRMaj u = new DMatrixRMaj(n,1);

        DMatrix1Row UBV = alg.getUBV();

//        A.print();

        for( int i = 0; i < n-2; i++ ) {
            alg.computeV(i);

            u.zero();
            SpecializedOps_DDRM.subvector(UBV,i,i+2,n-i-2,true,i+2,u);
            u.data[i+1] = 1;

            DMatrixRMaj Q = SpecializedOps_DDRM.createReflector(u,alg.getGammasV()[i]);

//            Q.print();

            CommonOps_DDRM.mult(B,Q,C);

//            u.print();
//            B.print();
//            UBV.print();
//            C.print();

            B.setTo(C);

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
    protected BidiagonalDecomposition_F64<DMatrixRMaj> createQRDecomposition() {
        return new BidiagonalDecompositionRow_DDRM();
    }

    private static class DebugBidiagonal extends BidiagonalDecompositionRow_DDRM {


        public DebugBidiagonal( DMatrixRMaj A ) {
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
