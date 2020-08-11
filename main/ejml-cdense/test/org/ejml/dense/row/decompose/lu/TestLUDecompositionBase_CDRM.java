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

package org.ejml.dense.row.decompose.lu;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F32;
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.RandomMatrices_CDRM;
import org.ejml.ops.ComplexMath_F32;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestLUDecompositionBase_CDRM {
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

        CMatrixRMaj LU = RandomMatrices_CDRM.rectangle(width,width,-1,1,rand);

        Complex_F32 expected = new Complex_F32(1,0);
        Complex_F32 a = new Complex_F32();
        Complex_F32 tmp = new Complex_F32();
        for (int i = 0; i < width; i++) {
            LU.get(i, i, a);
            ComplexMath_F32.multiply(expected,a,tmp);
            expected.set(tmp);
        }

        DebugDecompose alg = new DebugDecompose(width);
        alg.decomposeCommonInit(LU);
        for( int i = 0; i < width; i++ ) alg.getIndx()[i] = i;
        alg.setLU(LU);

        Complex_F32 found = alg.computeDeterminant();

        assertEquals(expected.real,found.real, UtilEjml.TEST_F32);
        assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F32);
    }

    @Test
    public void _solveVectorInternal() {
        int width = 10;
        CMatrixRMaj LU = RandomMatrices_CDRM.rectangle(width, width,-1,1, rand);

        CMatrixRMaj L = new CMatrixRMaj(width,width);
        CMatrixRMaj U = new CMatrixRMaj(width,width);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                float real = LU.getReal(i,j);
                float imag = LU.getImag(i, j);

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

        CMatrixRMaj x = RandomMatrices_CDRM.rectangle(width, 1,-1,1, rand);
        CMatrixRMaj tmp = new CMatrixRMaj(width,1);
        CMatrixRMaj b = new CMatrixRMaj(width,1);

        CommonOps_CDRM.mult(U, x, tmp);
        CommonOps_CDRM.mult(L, tmp, b);

        DebugDecompose alg = new DebugDecompose(width);
        alg.decomposeCommonInit(LU);
        for( int i = 0; i < width; i++ ) alg.getIndx()[i] = i;
        alg.setLU(LU);

        alg._solveVectorInternal(b.data);

        for( int i = 0; i < width; i++ ) {
            assertEquals(x.data[i],b.data[i],UtilEjml.TEST_F32);
        }
    }

    @Test
    public void solveL() {
        int width = 10;
        CMatrixRMaj LU = RandomMatrices_CDRM.rectangle(width, width,-1,1, rand);

        CMatrixRMaj L = new CMatrixRMaj(width,width);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                float real = LU.getReal(i,j);
                float imag = LU.getImag(i, j);

                if( j <= i ) {
                    if( j == i )
                        L.set(i,j,1,0);
                    else
                        L.set(i,j,real,imag);
                }
            }
        }

        CMatrixRMaj x = RandomMatrices_CDRM.rectangle(width, 1,-1,1, rand);
        CMatrixRMaj b = new CMatrixRMaj(width,1);

        CommonOps_CDRM.mult(L, x, b);

        DebugDecompose alg = new DebugDecompose(width);
        alg.decomposeCommonInit(LU);
        for( int i = 0; i < width; i++ ) alg.getIndx()[i] = i;
        alg.setLU(LU);

        alg.solveL(b.data);

        for( int i = 0; i < width; i++ ) {
            assertEquals(x.data[i],b.data[i],UtilEjml.TEST_F32);
        }
    }

    private static class DebugDecompose extends LUDecompositionBase_CDRM
    {
        public DebugDecompose(int width) {
            setExpectedMaxSize(width, width);
            m = n = width;
        }

        void setLU( CMatrixRMaj LU ) {
            this.LU = LU;
            this.dataLU = LU.data;
        }

        @Override
        public boolean decompose(CMatrixRMaj orig) {
            return false;
        }
    }
}