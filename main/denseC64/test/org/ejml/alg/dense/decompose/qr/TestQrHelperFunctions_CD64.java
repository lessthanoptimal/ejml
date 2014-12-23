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

package org.ejml.alg.dense.decompose.qr;

import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
import org.ejml.ops.CCommonOps;
import org.ejml.ops.CRandomMatrices;
import org.ejml.ops.ComplexMath64F;
import org.ejml.ops.EjmlUnitTests;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class TestQrHelperFunctions_CD64 {

    Random rand = new Random(234);

    @Test
    public void findMax() {
        double u[] = new double[50];

        for (int i = 0; i < u.length; i++) {
            u[i] = (rand.nextDouble()-0.5)*20;
        }

        int offset = 4;
        int length = 5;

        double max = 0;
        for (int i = 0; i < length; i++) {
            double real = u[i*2+offset*2];
            double img = u[i*2+offset*2+1];

            if( real*real + img*img > max ) {
                max = real*real + img*img;
            }
        }

        max = Math.sqrt(max);

        assertEquals(max,QrHelperFunctions_CD64.findMax(u,offset,length),1e-8);
    }

    @Test
    public void divideElements_startU() {
        double u[] = new double[12*2];
        for (int i = 0; i < u.length; i++ ) {
            u[i] = (rand.nextDouble()*0.5-1.0)*2;
        }
        double found[] = u.clone();

        Complex64F A = new Complex64F(rand.nextDouble(),rand.nextDouble());
        Complex64F U = new Complex64F();
        Complex64F expected = new Complex64F();

        int j = 3;
        int numRows = 8;
        int startU = 2;

        QrHelperFunctions_CD64.divideElements(j,numRows,found,startU,A.real,A.imaginary);

        for (int i = 0; i < 12; i++) {
            int index = i * 2;

            if( i >= j+startU && i < numRows+startU ) {
                U.real = u[index];
                U.imaginary = u[index + 1];
                ComplexMath64F.divide(U, A, expected);

                assertEquals(expected.real, found[index], 1e-8);
                assertEquals(expected.imaginary, found[index + 1], 1e-8);
            } else {
                assertEquals(u[index],found[index],1e-8);
                assertEquals(u[index+1],found[index+1],1e-8);
            }
        }
    }

    @Test
    public void divideElements_Brow() {
        fail("Implement");
    }

    @Test
    public void divideElements_Bcol() {
        fail("Implement");
    }

    @Test
    public void computeTauGammaAndDivide() {
        double u[] = new double[12*2];
        for (int i = 0; i < u.length; i++ ) {
            u[i] = (rand.nextDouble()*0.5-1.0)*2;
        }

        double max = 2.0;
        int j = 2;
        int numRows = 6;

        Complex64F expectedTau = new Complex64F();
        double expectedGamma = 0;
        double[] expectedU = u.clone();

        for (int i = j; i < numRows; i++) {
            Complex64F U = new Complex64F(u[i*2],u[i*2+1]);
            Complex64F div = new Complex64F();
            ComplexMath64F.divide(U,new Complex64F(max,0),div);

            expectedU[i*2] = div.real;
            expectedU[i*2+1] = div.imaginary;
        }
        double normX = 0;
        for (int i = j; i < numRows; i++) {
            normX += expectedU[i*2]*expectedU[i*2] + expectedU[i*2+1]*expectedU[i*2+1];
        }
        normX = Math.sqrt(normX);
        double realX0 = expectedU[j*2];
        double imagX0 = expectedU[j*2+1];

        double magX0 = Math.sqrt(realX0*realX0 + imagX0*imagX0);
        expectedTau.real      = realX0*normX/magX0;
        expectedTau.imaginary = imagX0*normX/magX0;

        double realU0 = realX0 + expectedTau.real;
        double imagU0 = imagX0 + expectedTau.imaginary;

        //
        double normU = 1;
        Complex64F B = new Complex64F(realU0,imagU0);
        for (int i = j+1; i < numRows; i++) {
            Complex64F A = new Complex64F( expectedU[i*2], expectedU[i*2+1]);
            Complex64F result = new Complex64F();
            ComplexMath64F.divide(A,B,result);
            normU += result.getMagnitude2();
        }
        expectedGamma = 2.0/normU;

        Complex64F foundTau = new Complex64F();
        double[] foundU = u.clone();
        double foundGamma = QrHelperFunctions_CD64.computeTauGammaAndDivide(j,numRows,foundU,max,foundTau);

        for (int i = 0; i < expectedU.length; i++) {
            assertEquals(expectedU[i],foundU[i],1e-8);
        }

        assertEquals(expectedTau.real,foundTau.real,1e-8);
        assertEquals(expectedTau.imaginary,foundTau.imaginary,1e-8);

        assertEquals(expectedGamma,foundGamma,1e-8);
    }

    @Test
    public void computeTauAndDivide_offsetU() {
        fail("Implement");
    }

    @Test
    public void rank1UpdateMultR() {
        double u[] = new double[12*2];
        double uoff[] = new double[12*2+2];
        double subU[] = new double[12*2];
        double _temp[] = new double[u.length];
        double gamma = 0.6;

        for (int i = 0; i < u.length; i++) {
            u[i] = uoff[i+2] = (rand.nextDouble()*0.5-1.0)*2;
        }

        for (int i = 1; i < 12; i++) {
            CDenseMatrix64F A = CRandomMatrices.createRandom(i,i,rand);

            for (int j = 1; j <= i; j += 2) {
                CDenseMatrix64F subA = CCommonOps.extract(A,A.numRows-j,A.numRows,A.numRows-j,A.numRows);
                System.arraycopy(u,(A.numRows-j)*2,subU,0,j*2);
                CDenseMatrix64F expected = rank1UpdateMultR(subA, gamma,subU);

                CDenseMatrix64F found = A.copy();
                QrHelperFunctions_CD64.rank1UpdateMultR(found, uoff, 1, gamma,
                        A.numRows - j, A.numRows - j, A.numRows, _temp);

                CDenseMatrix64F subFound = CCommonOps.extract(found,A.numRows-j,A.numRows,A.numRows-j,A.numRows);

                outsideIdentical(A, found, j);
                EjmlUnitTests.assertEquals(expected,subFound,1e-8);
            }
        }
    }

    private CDenseMatrix64F rank1UpdateMultR( CDenseMatrix64F A , double gamma,  double u[] ) {
        CDenseMatrix64F U = new CDenseMatrix64F(A.numCols,1);
        U.data = u;
        CDenseMatrix64F Ut = new CDenseMatrix64F(1,A.numCols);
        CCommonOps.transposeConjugate(U,Ut);

        CDenseMatrix64F UUt = new CDenseMatrix64F(A.numCols,A.numCols);
        CCommonOps.mult(gamma,0,U,Ut,UUt);

        CDenseMatrix64F I = CCommonOps.identity(A.numCols);
        CDenseMatrix64F inner = new CDenseMatrix64F(A.numCols,A.numCols);
        CDenseMatrix64F expected = new CDenseMatrix64F(A.numCols,A.numCols);

        CCommonOps.subtract(I,UUt,inner);
        CCommonOps.mult(inner,A,expected);

        return expected;
    }

    @Test
    public void rank1UpdateMultL() {
        double u[] = new double[12*2];
        double subU[] = new double[12*2];
        Complex64F gamma = new Complex64F(0.5,-0.2);

        for (int i = 0; i < u.length; i++) {
            u[i] = (rand.nextDouble()*0.5-1.0)*2;
        }

        for (int i = 1; i < 12; i++) {
            CDenseMatrix64F A = CRandomMatrices.createRandom(i,i,rand);

            for (int j = 1; j <= i; j += 2) {
                CDenseMatrix64F subA = CCommonOps.extract(A,A.numRows-j,A.numRows,A.numRows-j,A.numRows);
                System.arraycopy(u,(A.numRows-j)*2,subU,0,j*2);
                CDenseMatrix64F expected = rank1UpdateMultL(subA,gamma,subU);

                CDenseMatrix64F found = A.copy();
                QrHelperFunctions_CD64.rank1UpdateMultL(found,u,gamma.real,gamma.imaginary,
                        A.numRows-j,A.numRows-j,A.numRows);

                CDenseMatrix64F subFound = CCommonOps.extract(found,A.numRows-j,A.numRows,A.numRows-j,A.numRows);

                outsideIdentical(A, found, j);
                EjmlUnitTests.assertEquals(expected,subFound,1e-8);
            }
        }
    }

    private CDenseMatrix64F rank1UpdateMultL( CDenseMatrix64F A , Complex64F gamma,  double u[] ) {
        CDenseMatrix64F U = new CDenseMatrix64F(A.numCols,1);
        U.data = u;
        CDenseMatrix64F Ut = new CDenseMatrix64F(1,A.numCols);
        CCommonOps.transposeConjugate(U,Ut);

        CDenseMatrix64F UUt = new CDenseMatrix64F(A.numCols,A.numCols);
        CCommonOps.mult(gamma.real,gamma.imaginary,U,Ut,UUt);

        CDenseMatrix64F I = CCommonOps.identity(A.numCols);
        CDenseMatrix64F inner = new CDenseMatrix64F(A.numCols,A.numCols);
        CDenseMatrix64F expected = new CDenseMatrix64F(A.numCols,A.numCols);

        CCommonOps.subtract(I,UUt,inner);
        CCommonOps.mult(A,inner,expected);

        return expected;
    }

    private void outsideIdentical( CDenseMatrix64F A , CDenseMatrix64F B , int width ) {

        int outside = A.numRows-width;

        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < A.numCols; j++) {
                if( i < outside || j < outside ) {
                    assertEquals(A.getReal(i,j),B.getReal(i,j),1e-8);
                    assertEquals(A.getImaginary(i, j), B.getImaginary(i,j),1e-8);
                }
            }
        }
    }
}
