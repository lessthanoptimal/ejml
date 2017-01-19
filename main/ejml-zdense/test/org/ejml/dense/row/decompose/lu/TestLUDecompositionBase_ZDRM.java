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

package org.ejml.dense.row.decompose.lu;

import org.ejml.UtilEjml;
import org.ejml.data.ZComplex;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.dense.row.RandomMatrices_ZDRM;
import org.ejml.ops.ComplexMathZ;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestLUDecompositionBase_ZDRM {
    Random rand = new Random(0x3344);

    /**
     * Compare the determinant computed from LU to the value computed from the minor
     * matrix method.
     */
    @Test
    public void testDeterminant()
    {
        Random rand = new Random(0xfff);

        int width = 10;

        ZMatrixRMaj LU = RandomMatrices_ZDRM.rectangle(width,width,-1,1,rand);

        ZComplex expected = new ZComplex(1,0);
        ZComplex a = new ZComplex();
        ZComplex tmp = new ZComplex();
        for (int i = 0; i < width; i++) {
            LU.get(i, i, a);
            ComplexMathZ.multiply(expected,a,tmp);
            expected.set(tmp);
        }

        DebugDecompose alg = new DebugDecompose(width);
        alg.decomposeCommonInit(LU);
        for( int i = 0; i < width; i++ ) alg.getIndx()[i] = i;
        alg.setLU(LU);

        ZComplex found = alg.computeDeterminant();

        assertEquals(expected.real,found.real, UtilEjml.TEST_F64);
        assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F64);
    }

    @Test
    public void _solveVectorInternal() {
        int width = 10;
        ZMatrixRMaj LU = RandomMatrices_ZDRM.rectangle(width, width,-1,1, rand);

        ZMatrixRMaj L = new ZMatrixRMaj(width,width);
        ZMatrixRMaj U = new ZMatrixRMaj(width,width);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                double real = LU.getReal(i,j);
                double imag = LU.getImag(i, j);

                if( j <= i ) {
                    if( j == i )
                        L.set(i,j,1,0);
                    else
                        L.set(i,j,real,imag);
                }
                if( i <= j ) {
                    U.set(i,j,real,imag);
                }
            }
        }

        ZMatrixRMaj x = RandomMatrices_ZDRM.rectangle(width, 1,-1,1, rand);
        ZMatrixRMaj tmp = new ZMatrixRMaj(width,1);
        ZMatrixRMaj b = new ZMatrixRMaj(width,1);

        CommonOps_ZDRM.mult(U, x, tmp);
        CommonOps_ZDRM.mult(L, tmp, b);

        DebugDecompose alg = new DebugDecompose(width);
        alg.decomposeCommonInit(LU);
        for( int i = 0; i < width; i++ ) alg.getIndx()[i] = i;
        alg.setLU(LU);

        alg._solveVectorInternal(b.data);

        for( int i = 0; i < width; i++ ) {
            assertEquals(x.data[i],b.data[i],UtilEjml.TEST_F64);
        }
    }

    @Test
    public void solveL() {
        int width = 10;
        ZMatrixRMaj LU = RandomMatrices_ZDRM.rectangle(width, width,-1,1, rand);

        ZMatrixRMaj L = new ZMatrixRMaj(width,width);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                double real = LU.getReal(i,j);
                double imag = LU.getImag(i, j);

                if( j <= i ) {
                    if( j == i )
                        L.set(i,j,1,0);
                    else
                        L.set(i,j,real,imag);
                }
            }
        }

        ZMatrixRMaj x = RandomMatrices_ZDRM.rectangle(width, 1,-1,1, rand);
        ZMatrixRMaj b = new ZMatrixRMaj(width,1);

        CommonOps_ZDRM.mult(L, x, b);

        DebugDecompose alg = new DebugDecompose(width);
        alg.decomposeCommonInit(LU);
        for( int i = 0; i < width; i++ ) alg.getIndx()[i] = i;
        alg.setLU(LU);

        alg.solveL(b.data);

        for( int i = 0; i < width; i++ ) {
            assertEquals(x.data[i],b.data[i],UtilEjml.TEST_F64);
        }
    }

    private static class DebugDecompose extends LUDecompositionBase_ZDRM
    {
        public DebugDecompose(int width) {
            setExpectedMaxSize(width, width);
            m = n = width;
        }

        void setLU( ZMatrixRMaj LU ) {
            this.LU = LU;
            this.dataLU = LU.data;
        }

        @Override
        public boolean decompose(ZMatrixRMaj orig) {
            return false;
        }
    }
}