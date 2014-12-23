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

package org.ejml.ops;

import org.ejml.alg.dense.mult.CMatrixMatrixMult;
import org.ejml.alg.dense.mult.TestCMatrixMatrixMult;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestCCommonOps {

    Random rand = new Random(234);

    @Test
    public void identity_one() {
        CDenseMatrix64F I = CCommonOps.identity(4);
        assertEquals(4,I.numRows);
        assertEquals(4,I.numCols);

        assertTrue(CMatrixFeatures.isIdentity(I,1e-8));
    }

    @Test
    public void identity_two() {
        CDenseMatrix64F I = CCommonOps.identity(4,5);
        assertEquals(4,I.numRows);
        assertEquals(5,I.numCols);

        assertTrue(CMatrixFeatures.isIdentity(I,1e-8));

        I = CCommonOps.identity(5,4);
        assertEquals(5,I.numRows);
        assertEquals(4,I.numCols);

        assertTrue(CMatrixFeatures.isIdentity(I,1e-8));
    }

    @Test
    public void diag() {
        CDenseMatrix64F m = CCommonOps.diag(1,2,3,4,5,6);

        assertEquals(3,m.numRows);
        assertEquals(3,m.numCols);

        Complex64F a = new Complex64F();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                m.get(i,j,a);

                if( i == j ) {
                    assertEquals(2*i+1,a.real,1e-8);
                    assertEquals(2*i+2,a.imaginary,1e-8);
                } else {
                    assertEquals(0,a.real,1e-8);
                    assertEquals(0,a.imaginary,1e-8);
                }
            }
        }
    }

    @Test
    public void convert() {
        DenseMatrix64F input = RandomMatrices.createRandom(5,7,-1,1,rand);
        CDenseMatrix64F output = new CDenseMatrix64F(5,7);

        Complex64F a = new Complex64F();

        CCommonOps.convert(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                output.get(i,j,a);

                assertEquals(input.get(i,j),a.getReal(),1e-8);
                assertEquals(0,a.getImaginary(),1e-8);
            }
        }
    }

    @Test
    public void stripReal() {
        CDenseMatrix64F input = CRandomMatrices.createRandom(5,7,-1,1,rand);
        DenseMatrix64F output = new DenseMatrix64F(5,7);

        Complex64F a = new Complex64F();

        CCommonOps.stripReal(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                input.get(i,j,a);

                assertEquals(a.getReal(),output.get(i,j),1e-8);
            }
        }
    }

    @Test
    public void stripImaginary() {
        CDenseMatrix64F input = CRandomMatrices.createRandom(5,7,-1,1,rand);
        DenseMatrix64F output = new DenseMatrix64F(5,7);

        Complex64F a = new Complex64F();

        CCommonOps.stripImaginary(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                input.get(i,j,a);

                assertEquals(a.getImaginary(),output.get(i,j),1e-8);
            }
        }
    }

    @Test
    public void magnitude() {
        CDenseMatrix64F input = CRandomMatrices.createRandom(5,7,-1,1,rand);
        DenseMatrix64F output = new DenseMatrix64F(5,7);

        Complex64F a = new Complex64F();

        CCommonOps.magnitude(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                input.get(i,j,a);

                assertEquals(a.getMagnitude(),output.get(i,j),1e-8);
            }
        }
    }

    @Test
    public void conjugate() {
        CDenseMatrix64F matrix = CRandomMatrices.createRandom(5,7,-1,1,rand);
        CDenseMatrix64F found = CRandomMatrices.createRandom(5,7,-1,1,rand);

        CCommonOps.conjugate(matrix,found);

        for (int i = 0; i < matrix.getDataLength(); i += 2) {
            double real = matrix.data[i];
            double img = matrix.data[i+1];

            assertEquals(real, found.data[i],1e-8);
            assertEquals(img, -found.data[i+1],1e-8);
        }
    }

    @Test
    public void fill() {
        CDenseMatrix64F matrix = CRandomMatrices.createRandom(5,7,-1,1,rand);

        CCommonOps.fill(matrix,2,-1);

        for (int i = 0; i < matrix.getDataLength(); i += 2) {
            double real = matrix.data[i];
            double img = matrix.data[i+1];

            assertEquals(2,real,1e-8);
            assertEquals(-1,img,1e-8);
        }
    }

    @Test
    public void add() {
        CDenseMatrix64F matrixA = CRandomMatrices.createRandom(5,7,-1,1,rand);
        CDenseMatrix64F matrixB = CRandomMatrices.createRandom(5,7,-1,1,rand);
        CDenseMatrix64F out = CRandomMatrices.createRandom(5,7,-1,1,rand);

        Complex64F a = new Complex64F();
        Complex64F b = new Complex64F();
        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        CCommonOps.add(matrixA, matrixB, out);

        for (int i = 0; i < matrixA.numRows; i++) {
            for (int j = 0; j < matrixA.numCols; j++) {
                matrixA.get(i,j,a);
                matrixB.get(i,j,b);
                out.get(i,j,found);

                ComplexMath64F.plus(a, b, expected);

                assertEquals(expected.real,found.real,1e-8);
                assertEquals(expected.imaginary,found.imaginary,1e-8);
            }
        }
    }

    @Test
    public void subtract() {
        CDenseMatrix64F matrixA = CRandomMatrices.createRandom(5,7,-1,1,rand);
        CDenseMatrix64F matrixB = CRandomMatrices.createRandom(5,7,-1,1,rand);
        CDenseMatrix64F out = CRandomMatrices.createRandom(5,7,-1,1,rand);

        Complex64F a = new Complex64F();
        Complex64F b = new Complex64F();
        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        CCommonOps.subtract(matrixA, matrixB, out);

        for (int i = 0; i < matrixA.numRows; i++) {
            for (int j = 0; j < matrixA.numCols; j++) {
                matrixA.get(i,j,a);
                matrixB.get(i,j,b);
                out.get(i,j,found);

                ComplexMath64F.minus(a, b, expected);

                assertEquals(expected.real,found.real,1e-8);
                assertEquals(expected.imaginary,found.imaginary,1e-8);
            }
        }
    }

    /**
     * Make sure the multiplication methods here have the same behavior as the ones in MatrixMatrixMult.
     */
    @Test
    public void checkAllMatrixMult() {
        int numChecked = 0;
        Method methods[] = CCommonOps.class.getMethods();

        for (Method method : methods) {
            String name = method.getName();

            if( !name.startsWith("mult"))
                continue;

            //            System.out.println(name);

            Class[] params = method.getParameterTypes();

            boolean add = name.contains("Add");
            boolean hasAlpha = double.class == params[0];

            try {
                TestCMatrixMatrixMult.check(method, add, hasAlpha);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            numChecked++;
        }

        assertEquals(4,numChecked);
    }

    @Test
    public void multiply() {
        for (int i = 1; i < 10; i++) {
            for (int j = 1; j < 10; j++) {
                CDenseMatrix64F A = CRandomMatrices.createRandom(i,j,-1,1,rand);
                for (int k = 1; k < 10; k++) {
                    CDenseMatrix64F B = CRandomMatrices.createRandom(j, k, -1, 1, rand);
                    CDenseMatrix64F found = CRandomMatrices.createRandom(i, k, -1, 1, rand);
                    CDenseMatrix64F expected = TestCMatrixMatrixMult.multiply(A, B);

                    CMatrixMatrixMult.mult_reorder(A, B, found);

                    assertTrue(i+" "+j+" "+k,CMatrixFeatures.isEquals(expected, found, 1e-8));
                }
            }
        }
    }

    @Test
    public void transpose_one() {

        CDenseMatrix64F a = CRandomMatrices.createRandom(4,4,-1,1,rand);
        CDenseMatrix64F b = a.copy();

        CCommonOps.transpose(b);

        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,1e-8);
                assertEquals(expected.imaginary,found.imaginary,1e-8);
            }
        }
    }

    @Test
    public void transposeConjugate_one() {

        CDenseMatrix64F a = CRandomMatrices.createRandom(4,4,-1,1,rand);
        CDenseMatrix64F b = a.copy();

        CCommonOps.transposeConjugate(b);

        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,1e-8);
                assertEquals(-expected.imaginary,found.imaginary,1e-8);
            }
        }
    }

    @Test
    public void transpose_two() {

        CDenseMatrix64F a = CRandomMatrices.createRandom(4,5,-1,1,rand);
        CDenseMatrix64F b = CRandomMatrices.createRandom(5,4,-1,1,rand);

        CCommonOps.transpose(a, b);

        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,1e-8);
                assertEquals(expected.imaginary,found.imaginary,1e-8);
            }
        }
    }

    @Test
    public void transposeConjugate_two() {

        CDenseMatrix64F a = CRandomMatrices.createRandom(4,5,-1,1,rand);
        CDenseMatrix64F b = CRandomMatrices.createRandom(5,4,-1,1,rand);

        CCommonOps.transposeConjugate(a, b);

        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,1e-8);
                assertEquals(-expected.imaginary,found.imaginary,1e-8);
            }
        }
    }

    @Test
    public void invert_1() {
        for (int i = 1; i < 10; i++) {
            CDenseMatrix64F A = CRandomMatrices.createRandom(i,i,rand);
            CDenseMatrix64F A_orig = A.copy();

            CDenseMatrix64F I = CRandomMatrices.createRandom(i,i,rand);

            assertTrue(CCommonOps.invert(A));
            CCommonOps.mult(A_orig,A,I);

            assertTrue(CMatrixFeatures.isIdentity(I, 1e-8));
        }
    }

    @Test
    public void invert_2() {
        for (int i = 1; i < 10; i++) {
            CDenseMatrix64F A = CRandomMatrices.createRandom(i, i, rand);
            CDenseMatrix64F A_orig = A.copy();
            CDenseMatrix64F A_inv = new CDenseMatrix64F(i, i);

            CDenseMatrix64F I = CRandomMatrices.createRandom(i, i, rand);

            assertTrue(CCommonOps.invert(A, A_inv));
            CCommonOps.mult(A, A_inv, I);

            assertTrue(CMatrixFeatures.isIdentity(I, 1e-8));
            assertTrue(CMatrixFeatures.isIdentical(A, A_orig, 0));
        }
    }

    @Test
    public void solve() {
        // square
        for (int i = 1; i < 10; i++) {
            CDenseMatrix64F A = CRandomMatrices.createRandom(i, i, rand);
            CDenseMatrix64F B = CRandomMatrices.createRandom(i, 1, rand);

            CDenseMatrix64F A_orig = A.copy();
            CDenseMatrix64F B_orig = B.copy();

            CDenseMatrix64F X = new CDenseMatrix64F(i, 1);

            assertTrue(CCommonOps.solve(A, B, X));

            CDenseMatrix64F found = new CDenseMatrix64F(i, 1);

            CCommonOps.mult(A, X, found);

            assertTrue(CMatrixFeatures.isIdentical(B, found, 1e-8));

            assertTrue(CMatrixFeatures.isIdentical(A, A_orig, 0));
            assertTrue(CMatrixFeatures.isIdentical(B, B_orig, 0));
        }

        // rectangular
        for (int i = 1; i < 10; i++) {
            CDenseMatrix64F A = CRandomMatrices.createRandom(2*i, i, rand);
            CDenseMatrix64F X = CRandomMatrices.createRandom(i, 1, rand);
            CDenseMatrix64F B = new CDenseMatrix64F(2*i,1);

            CCommonOps.mult(A,X,B);

            CDenseMatrix64F A_orig = A.copy();
            CDenseMatrix64F B_orig = B.copy();
            CDenseMatrix64F X_expected = X.copy();

            assertTrue(CCommonOps.solve(A, B, X));

            assertTrue(CMatrixFeatures.isIdentical(X, X_expected, 1e-8));

            assertTrue(CMatrixFeatures.isIdentical(B, B_orig, 0));
            assertTrue(CMatrixFeatures.isIdentical(A, A_orig, 0));
        }
    }

    @Test
    public void det() {
        CDenseMatrix64F A = new CDenseMatrix64F(3,3,true,
                0.854634 , 0.445620,  0.082836 , 0.212460 , 0.623783 , 0.037631,
                0.585408 , 0.768956 , 0.771067 , 0.897763 , 0.125793 , 0.432187,
                0.303789 , 0.044497 , 0.151182 , 0.034471 , 0.526770 , 0.570333);

        CDenseMatrix64F A_orig = A.copy();

        Complex64F found = CCommonOps.det(A);
        // from octave
        Complex64F expected = new Complex64F(-0.40548 , 0.54188);

        assertEquals(expected.real,found.real,1e-3);
        assertEquals(expected.imaginary,found.imaginary,1e-3);

        assertTrue(CMatrixFeatures.isIdentical(A,A_orig,0));
    }

    @Test
    public void elementMultiply() {
        CDenseMatrix64F in = CRandomMatrices.createRandom(5,7,-1,1,rand);
        CDenseMatrix64F out = CRandomMatrices.createRandom(5,7,-1,1,rand);

        Complex64F a = new Complex64F(1.2,-0.3);
        Complex64F b = new Complex64F();
        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        CCommonOps.elementMultiply(in,a.real,a.imaginary,out);

        for (int i = 0; i < in.numRows; i++) {
            for (int j = 0; j < in.numCols; j++) {
                in.get(i,j,b);
                out.get(i,j,found);

                ComplexMath64F.multiply(a,b,expected);

                assertEquals(expected.real,found.real,1e-8);
                assertEquals(expected.imaginary,found.imaginary,1e-8);
            }
        }
    }

    @Test
    public void elementDivide_right() {
        CDenseMatrix64F in = CRandomMatrices.createRandom(5,7,-1,1,rand);
        CDenseMatrix64F out = CRandomMatrices.createRandom(5,7,-1,1,rand);

        Complex64F a = new Complex64F();
        Complex64F b = new Complex64F(1.2,-0.3);
        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        CCommonOps.elementDivide(in,b.real,b.imaginary,out);

        for (int i = 0; i < in.numRows; i++) {
            for (int j = 0; j < in.numCols; j++) {
                in.get(i,j,a);
                out.get(i,j,found);

                ComplexMath64F.divide(a,b,expected);

                assertEquals(expected.real,found.real,1e-8);
                assertEquals(expected.imaginary,found.imaginary,1e-8);
            }
        }
    }

    @Test
    public void elementDivide_left() {
        CDenseMatrix64F in = CRandomMatrices.createRandom(5,7,-1,1,rand);
        CDenseMatrix64F out = CRandomMatrices.createRandom(5,7,-1,1,rand);

        Complex64F a = new Complex64F(1.2,-0.3);
        Complex64F b = new Complex64F();
        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        CCommonOps.elementDivide(a.real,a.imaginary,in,out);

        for (int i = 0; i < in.numRows; i++) {
            for (int j = 0; j < in.numCols; j++) {
                in.get(i,j,b);
                out.get(i,j,found);

                ComplexMath64F.divide(a,b,expected);

                assertEquals(expected.real,found.real,1e-8);
                assertEquals(expected.imaginary,found.imaginary,1e-8);
            }
        }
    }

    @Test
    public void elementMinReal() {
        CDenseMatrix64F m = new CDenseMatrix64F(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-6, CCommonOps.elementMinReal(m),1e-8);
    }

    @Test
    public void elementMinImaginary() {
        CDenseMatrix64F m = new CDenseMatrix64F(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-5, CCommonOps.elementMinImaginary(m), 1e-8);
    }

    @Test
    public void elementMaxReal() {
        CDenseMatrix64F m = new CDenseMatrix64F(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-6 + 11 * 2, CCommonOps.elementMaxReal(m), 1e-8);
    }

    @Test
    public void elementMaxImaginary() {
        CDenseMatrix64F m = new CDenseMatrix64F(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-5 + 11 * 2, CCommonOps.elementMaxImaginary(m), 1e-8);
    }

    @Test
    public void elementMaxMagnitude2() {
        CDenseMatrix64F m = CRandomMatrices.createRandom(4,5,-2,2,rand);
        DenseMatrix64F a = new DenseMatrix64F(m.numRows,m.numCols);

        CCommonOps.magnitude(m,a);

        double expected = CommonOps.elementMaxAbs(a);
        expected *= expected;

        double found = CCommonOps.elementMaxMagnitude2(m);

        assertEquals(expected,found,1e-8);
    }

    @Test
    public void setIdentity() {
        CDenseMatrix64F a = CRandomMatrices.createRandom(4,5,-2,2,rand);

        CCommonOps.setIdentity(a);

        Complex64F c = new Complex64F();
        for (int i = 0; i < a.numRows; i++) {
            for (int j = 0; j < a.numCols; j++) {
                a.get(i,j,c);
                if( i == j ) {
                    assertEquals(1,c.real,1e-8);
                    assertEquals(0,c.imaginary,1e-8);
                } else {
                    assertEquals(0,c.real,1e-8);
                    assertEquals(0,c.imaginary,1e-8);
                }
            }
        }
    }

    @Test
    public void extract_simplified() {
        CDenseMatrix64F a = CRandomMatrices.createRandom(10,12,-2,2,rand);
        CDenseMatrix64F b = CCommonOps.extract(a,2,5,3,8);

        Complex64F ca = new Complex64F();
        Complex64F cb = new Complex64F();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(2+i,j+3,ca);
                b.get(  i,j  , cb);

                assertEquals(ca.real,cb.real,1e-8);
                assertEquals(ca.imaginary,cb.imaginary,1e-8);
            }
        }
    }

    @Test
    public void extract_complex() {
        CDenseMatrix64F a = CRandomMatrices.createRandom(10,12,-2,2,rand);
        CDenseMatrix64F b = new CDenseMatrix64F(6,7);

        Complex64F ca = new Complex64F();
        Complex64F cb = new Complex64F();

        CCommonOps.extract(a,2,5,3,7,b,1,2);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(2+i,j+3,ca);
                b.get(1 + i, j + 2, cb);

                assertEquals(ca.real,cb.real,1e-8);
                assertEquals(ca.imaginary,cb.imaginary,1e-8);
            }
        }
    }

    @Test
    public void columnsToVector() {
        CDenseMatrix64F a = CRandomMatrices.createRandom(10,12,-2,2,rand);
        CDenseMatrix64F v[] = CCommonOps.columnsToVector(a,null);

        Complex64F ca = new Complex64F();
        Complex64F cc = new Complex64F();

        for (int i = 0; i < a.numCols; i++) {
            CDenseMatrix64F c = v[i];

            assertEquals(c.numRows,a.numRows);
            assertEquals(1,c.numCols);

            for (int j = 0; j < a.numRows; j++) {
                a.get(j,i,ca);
                c.get(j,0,cc);

                EjmlUnitTests.assertEquals(ca,cc,1e-8);
            }
        }
    }

    @Test
    public void elementMaxAbs() {
        CDenseMatrix64F a = CRandomMatrices.createRandom(10,12,-2,2,rand);
        a.set(5,6,10,12);

        double expected = Math.sqrt(10*10 + 12*12);
        double found = CCommonOps.elementMaxAbs(a);
        assertEquals(expected,found,1e-8);
    }
}