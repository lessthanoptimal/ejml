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

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.CheckDecompositionInterface_DDRM;
import org.ejml.interfaces.decomposition.BidiagonalDecomposition_F64;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public abstract class GenericBidiagonalCheck_DDRM extends EjmlStandardJUnit {
    abstract protected BidiagonalDecomposition_F64<DMatrixRMaj> createQRDecomposition();

    @Test
    public void testModifiedInput() {
        CheckDecompositionInterface_DDRM.checkModifiedInput(createQRDecomposition());
    }

    @Test
    public void testRandomMatrices() {
        BidiagonalDecomposition_F64<DMatrixRMaj> decomp = createQRDecomposition();

        for( int i = 0; i < 10; i++ ) {
            for( int N = 2;  N <= 10; N++ ) {
                for( int tall = 0; tall <= 2; tall++ ) {
                    DMatrixRMaj A = RandomMatrices_DDRM.rectangle(N+tall,N,rand);

                    assertTrue(decomp.decompose(A.copy()));

                    checkGeneric(A, decomp);
                }
                for( int wide = 1; wide <= 2; wide++ ) {
                    DMatrixRMaj A = RandomMatrices_DDRM.rectangle(N,N+wide,rand);

                    assertTrue(decomp.decompose(A.copy()));

                    checkGeneric(A, decomp);
                }
            }
        }
    }

    @Test
    public void testIdentity() {
        SimpleMatrix A = SimpleMatrix.identity(5, DMatrixRMaj.class);

        BidiagonalDecomposition_F64<DMatrixRMaj> decomp = createQRDecomposition();

        assertTrue(decomp.decompose(A.getDDRM().copy()));

        checkGeneric(A.getDDRM(), decomp);
    }

    @Test
    public void testZero() {
        SimpleMatrix A = new SimpleMatrix(5,5, DMatrixRMaj.class);

        BidiagonalDecomposition_F64<DMatrixRMaj> decomp = createQRDecomposition();

        assertTrue(decomp.decompose(A.getDDRM().copy()));

        checkGeneric(A.getDDRM(), decomp);
    }

    /**
     * Checks to see if the decomposition will reconstruct the original input matrix
     */
    protected void checkGeneric(DMatrixRMaj a,
                                BidiagonalDecomposition_F64<DMatrixRMaj> decomp) {
        // check the full version
        SimpleMatrix U = SimpleMatrix.wrap(decomp.getU(null,false,false));
        SimpleMatrix B = SimpleMatrix.wrap(decomp.getB(null,false));
        SimpleMatrix V = SimpleMatrix.wrap(decomp.getV(null,false,false));

        DMatrixRMaj foundA = U.mult(B).mult(V.transpose()).getDDRM();

        assertTrue(MatrixFeatures_DDRM.isIdentical(a,foundA,UtilEjml.TEST_F64));

        //       check with transpose
        SimpleMatrix Ut = SimpleMatrix.wrap(decomp.getU(null,true,false));

        assertTrue(U.transpose().isIdentical(Ut, UtilEjml.TEST_F64));

        SimpleMatrix Vt = SimpleMatrix.wrap(decomp.getV(null,true,false));

        assertTrue(V.transpose().isIdentical(Vt,UtilEjml.TEST_F64));

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

        foundA = U.mult(B).mult(V.transpose()).getDDRM();

        assertTrue(MatrixFeatures_DDRM.isIdentical(a,foundA,UtilEjml.TEST_F64));

        //       check with transpose
        Ut = SimpleMatrix.wrap(decomp.getU(null,true,true));
        Vt = SimpleMatrix.wrap(decomp.getV(null,true,true));

        assertTrue(U.transpose().isIdentical(Ut,UtilEjml.TEST_F64));
        assertTrue(V.transpose().isIdentical(Vt,UtilEjml.TEST_F64));
    }

}
