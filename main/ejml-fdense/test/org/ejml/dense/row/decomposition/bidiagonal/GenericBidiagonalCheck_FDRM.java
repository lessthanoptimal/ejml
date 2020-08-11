/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.decomposition.CheckDecompositionInterface_FDRM;
import org.ejml.interfaces.decomposition.BidiagonalDecomposition_F32;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public abstract class GenericBidiagonalCheck_FDRM {
    protected Random rand = new Random(0xff);

    abstract protected BidiagonalDecomposition_F32<FMatrixRMaj> createQRDecomposition();

    @Test
    public void testModifiedInput() {
        CheckDecompositionInterface_FDRM.checkModifiedInput(createQRDecomposition());
    }

    @Test
    public void testRandomMatrices() {
        BidiagonalDecomposition_F32<FMatrixRMaj> decomp = createQRDecomposition();

        for( int i = 0; i < 10; i++ ) {
            for( int N = 2;  N <= 10; N++ ) {
                for( int tall = 0; tall <= 2; tall++ ) {
                    FMatrixRMaj A = RandomMatrices_FDRM.rectangle(N+tall,N,rand);

                    assertTrue(decomp.decompose(A.copy()));

                    checkGeneric(A, decomp);
                }
                for( int wide = 1; wide <= 2; wide++ ) {
                    FMatrixRMaj A = RandomMatrices_FDRM.rectangle(N,N+wide,rand);

                    assertTrue(decomp.decompose(A.copy()));

                    checkGeneric(A, decomp);
                }
            }
        }
    }

    @Test
    public void testIdentity() {
        SimpleMatrix A = SimpleMatrix.identity(5, FMatrixRMaj.class);

        BidiagonalDecomposition_F32<FMatrixRMaj> decomp = createQRDecomposition();

        assertTrue(decomp.decompose(A.getFDRM().copy()));

        checkGeneric(A.getFDRM(), decomp);
    }

    @Test
    public void testZero() {
        SimpleMatrix A = new SimpleMatrix(5,5, FMatrixRMaj.class);

        BidiagonalDecomposition_F32<FMatrixRMaj> decomp = createQRDecomposition();

        assertTrue(decomp.decompose(A.getFDRM().copy()));

        checkGeneric(A.getFDRM(), decomp);
    }

    /**
     * Checks to see if the decomposition will reconstruct the original input matrix
     */
    protected void checkGeneric(FMatrixRMaj a,
                                BidiagonalDecomposition_F32<FMatrixRMaj> decomp) {
        // check the full version
        SimpleMatrix U = SimpleMatrix.wrap(decomp.getU(null,false,false));
        SimpleMatrix B = SimpleMatrix.wrap(decomp.getB(null,false));
        SimpleMatrix V = SimpleMatrix.wrap(decomp.getV(null,false,false));

        FMatrixRMaj foundA = U.mult(B).mult(V.transpose()).getFDRM();

        assertTrue(MatrixFeatures_FDRM.isIdentical(a,foundA,UtilEjml.TEST_F32));

        //       check with transpose
        SimpleMatrix Ut = SimpleMatrix.wrap(decomp.getU(null,true,false));

        assertTrue(U.transpose().isIdentical(Ut, UtilEjml.TEST_F32));

        SimpleMatrix Vt = SimpleMatrix.wrap(decomp.getV(null,true,false));

        assertTrue(V.transpose().isIdentical(Vt,UtilEjml.TEST_F32));

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

        foundA = U.mult(B).mult(V.transpose()).getFDRM();

        assertTrue(MatrixFeatures_FDRM.isIdentical(a,foundA,UtilEjml.TEST_F32));

        //       check with transpose
        Ut = SimpleMatrix.wrap(decomp.getU(null,true,true));
        Vt = SimpleMatrix.wrap(decomp.getV(null,true,true));

        assertTrue(U.transpose().isIdentical(Ut,UtilEjml.TEST_F32));
        assertTrue(V.transpose().isIdentical(Vt,UtilEjml.TEST_F32));
    }

}
