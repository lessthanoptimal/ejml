/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
import org.ejml.ops.CCommonOps;
import org.ejml.ops.CRandomMatrices;
import org.ejml.ops.ComplexMath64F;
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

        CDenseMatrix64F LU = CRandomMatrices.createRandom(width,width,-1,1,rand);

        Complex64F expected = new Complex64F(1,0);
        Complex64F a = new Complex64F();
        Complex64F tmp = new Complex64F();
        for (int i = 0; i < width; i++) {
            LU.get(i, i, a);
            ComplexMath64F.multiply(expected,a,tmp);
            expected.set(tmp);
        }

        DebugDecompose alg = new DebugDecompose(width);
        alg.decomposeCommonInit(LU);
        for( int i = 0; i < width; i++ ) alg.getIndx()[i] = i;
        alg.setLU(LU);

        Complex64F found = alg.computeDeterminant();

        assertEquals(expected.real,found.real,1e-6);
        assertEquals(expected.imaginary,found.imaginary,1e-6);
    }

    @Test
    public void _solveVectorInternal() {
        int width = 10;
        CDenseMatrix64F LU = CRandomMatrices.createRandom(width, width,-1,1, rand);

        CDenseMatrix64F L = new CDenseMatrix64F(width,width);
        CDenseMatrix64F U = new CDenseMatrix64F(width,width);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                double real = LU.getReal(i,j);
                double imag = LU.getImaginary(i, j);

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

        CDenseMatrix64F x = CRandomMatrices.createRandom(width, 1,-1,1, rand);
        CDenseMatrix64F tmp = new CDenseMatrix64F(width,1);
        CDenseMatrix64F b = new CDenseMatrix64F(width,1);

        CCommonOps.mult(U, x, tmp);
        CCommonOps.mult(L, tmp, b);

        DebugDecompose alg = new DebugDecompose(width);
        alg.decomposeCommonInit(LU);
        for( int i = 0; i < width; i++ ) alg.getIndx()[i] = i;
        alg.setLU(LU);

        alg._solveVectorInternal(b.data);

        for( int i = 0; i < width; i++ ) {
            assertEquals(x.data[i],b.data[i],1e-6);
        }
    }

    @Test
    public void solveL() {
        int width = 10;
        CDenseMatrix64F LU = CRandomMatrices.createRandom(width, width,-1,1, rand);

        CDenseMatrix64F L = new CDenseMatrix64F(width,width);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                double real = LU.getReal(i,j);
                double imag = LU.getImaginary(i, j);

                if( j <= i ) {
                    if( j == i )
                        L.set(i,j,1,0);
                    else
                        L.set(i,j,real,imag);
                }
            }
        }

        CDenseMatrix64F x = CRandomMatrices.createRandom(width, 1,-1,1, rand);
        CDenseMatrix64F b = new CDenseMatrix64F(width,1);

        CCommonOps.mult(L, x, b);

        DebugDecompose alg = new DebugDecompose(width);
        alg.decomposeCommonInit(LU);
        for( int i = 0; i < width; i++ ) alg.getIndx()[i] = i;
        alg.setLU(LU);

        alg.solveL(b.data);

        for( int i = 0; i < width; i++ ) {
            assertEquals(x.data[i],b.data[i],1e-6);
        }
    }

    private static class DebugDecompose extends LUDecompositionBase_CD64
    {
        public DebugDecompose(int width) {
            setExpectedMaxSize(width, width);
            m = n = width;
        }

        void setLU( CDenseMatrix64F LU ) {
            this.LU = LU;
            this.dataLU = LU.data;
        }

        @Override
        public boolean decompose(CDenseMatrix64F orig) {
            return false;
        }
    }
}