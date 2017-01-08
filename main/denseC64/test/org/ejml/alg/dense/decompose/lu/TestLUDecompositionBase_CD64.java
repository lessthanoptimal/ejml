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

package org.ejml.alg.dense.decompose.lu;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.RowMatrix_C64;
import org.ejml.ops.CommonOps_CD64;
import org.ejml.ops.ComplexMath_F64;
import org.ejml.ops.RandomMatrices_CD64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestLUDecompositionBase_CD64 {
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

        RowMatrix_C64 LU = RandomMatrices_CD64.createRandom(width,width,-1,1,rand);

        Complex_F64 expected = new Complex_F64(1,0);
        Complex_F64 a = new Complex_F64();
        Complex_F64 tmp = new Complex_F64();
        for (int i = 0; i < width; i++) {
            LU.get(i, i, a);
            ComplexMath_F64.multiply(expected,a,tmp);
            expected.set(tmp);
        }

        DebugDecompose alg = new DebugDecompose(width);
        alg.decomposeCommonInit(LU);
        for( int i = 0; i < width; i++ ) alg.getIndx()[i] = i;
        alg.setLU(LU);

        Complex_F64 found = alg.computeDeterminant();

        assertEquals(expected.real,found.real, UtilEjml.TEST_F64);
        assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F64);
    }

    @Test
    public void _solveVectorInternal() {
        int width = 10;
        RowMatrix_C64 LU = RandomMatrices_CD64.createRandom(width, width,-1,1, rand);

        RowMatrix_C64 L = new RowMatrix_C64(width,width);
        RowMatrix_C64 U = new RowMatrix_C64(width,width);

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

        RowMatrix_C64 x = RandomMatrices_CD64.createRandom(width, 1,-1,1, rand);
        RowMatrix_C64 tmp = new RowMatrix_C64(width,1);
        RowMatrix_C64 b = new RowMatrix_C64(width,1);

        CommonOps_CD64.mult(U, x, tmp);
        CommonOps_CD64.mult(L, tmp, b);

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
        RowMatrix_C64 LU = RandomMatrices_CD64.createRandom(width, width,-1,1, rand);

        RowMatrix_C64 L = new RowMatrix_C64(width,width);

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

        RowMatrix_C64 x = RandomMatrices_CD64.createRandom(width, 1,-1,1, rand);
        RowMatrix_C64 b = new RowMatrix_C64(width,1);

        CommonOps_CD64.mult(L, x, b);

        DebugDecompose alg = new DebugDecompose(width);
        alg.decomposeCommonInit(LU);
        for( int i = 0; i < width; i++ ) alg.getIndx()[i] = i;
        alg.setLU(LU);

        alg.solveL(b.data);

        for( int i = 0; i < width; i++ ) {
            assertEquals(x.data[i],b.data[i],UtilEjml.TEST_F64);
        }
    }

    private static class DebugDecompose extends LUDecompositionBase_CD64
    {
        public DebugDecompose(int width) {
            setExpectedMaxSize(width, width);
            m = n = width;
        }

        void setLU( RowMatrix_C64 LU ) {
            this.LU = LU;
            this.dataLU = LU.data;
        }

        @Override
        public boolean decompose(RowMatrix_C64 orig) {
            return false;
        }
    }
}