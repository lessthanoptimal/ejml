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

package org.ejml.ops;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.alg.dense.mult.MatrixMatrixMult_CD64;
import org.ejml.alg.dense.mult.TestMatrixMatrixMult_CD64;
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
public class TestCommonOps_CD64 {

    Random rand = new Random(234);

    @Test
    public void identity_one() {
        CDenseMatrix64F I = CommonOps_CD64.identity(4);
        assertEquals(4,I.numRows);
        assertEquals(4,I.numCols);

        assertTrue(MatrixFeatures_CD64.isIdentity(I, UtilEjml.TEST_64F));
    }

    @Test
    public void identity_two() {
        CDenseMatrix64F I = CommonOps_CD64.identity(4,5);
        assertEquals(4,I.numRows);
        assertEquals(5,I.numCols);

        assertTrue(MatrixFeatures_CD64.isIdentity(I,UtilEjml.TEST_64F));

        I = CommonOps_CD64.identity(5,4);
        assertEquals(5,I.numRows);
        assertEquals(4,I.numCols);

        assertTrue(MatrixFeatures_CD64.isIdentity(I,UtilEjml.TEST_64F));
    }

    @Test
    public void diag() {
        CDenseMatrix64F m = CommonOps_CD64.diag(1,2,3,4,5,6);

        assertEquals(3,m.numRows);
        assertEquals(3,m.numCols);

        Complex64F a = new Complex64F();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                m.get(i,j,a);

                if( i == j ) {
                    assertEquals(2*i+1,a.real,UtilEjml.TEST_64F);
                    assertEquals(2*i+2,a.imaginary,UtilEjml.TEST_64F);
                } else {
                    assertEquals(0,a.real,UtilEjml.TEST_64F);
                    assertEquals(0,a.imaginary,UtilEjml.TEST_64F);
                }
            }
        }
    }

    @Test
    public void convert() {
        DenseMatrix64F input = RandomMatrices_D64.createRandom(5,7,-1,1,rand);
        CDenseMatrix64F output = new CDenseMatrix64F(5,7);

        Complex64F a = new Complex64F();

        CommonOps_CD64.convert(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                output.get(i,j,a);

                assertEquals(input.get(i,j),a.getReal(),UtilEjml.TEST_64F);
                assertEquals(0,a.getImaginary(),UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void stripReal() {
        CDenseMatrix64F input = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        DenseMatrix64F output = new DenseMatrix64F(5,7);

        Complex64F a = new Complex64F();

        CommonOps_CD64.stripReal(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                input.get(i,j,a);

                assertEquals(a.getReal(),output.get(i,j),UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void stripImaginary() {
        CDenseMatrix64F input = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        DenseMatrix64F output = new DenseMatrix64F(5,7);

        Complex64F a = new Complex64F();

        CommonOps_CD64.stripImaginary(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                input.get(i,j,a);

                assertEquals(a.getImaginary(),output.get(i,j),UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void magnitude() {
        CDenseMatrix64F input = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        DenseMatrix64F output = new DenseMatrix64F(5,7);

        Complex64F a = new Complex64F();

        CommonOps_CD64.magnitude(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                input.get(i,j,a);

                assertEquals(a.getMagnitude(),output.get(i,j),UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void conjugate() {
        CDenseMatrix64F matrix = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        CDenseMatrix64F found = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);

        CommonOps_CD64.conjugate(matrix,found);

        for (int i = 0; i < matrix.getDataLength(); i += 2) {
            double real = matrix.data[i];
            double img = matrix.data[i+1];

            assertEquals(real, found.data[i],UtilEjml.TEST_64F);
            assertEquals(img, -found.data[i+1],UtilEjml.TEST_64F);
        }
    }

    @Test
    public void fill() {
        CDenseMatrix64F matrix = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);

        CommonOps_CD64.fill(matrix,2,-1);

        for (int i = 0; i < matrix.getDataLength(); i += 2) {
            double real = matrix.data[i];
            double img = matrix.data[i+1];

            assertEquals(2,real,UtilEjml.TEST_64F);
            assertEquals(-1,img,UtilEjml.TEST_64F);
        }
    }

    @Test
    public void add() {
        CDenseMatrix64F matrixA = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        CDenseMatrix64F matrixB = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        CDenseMatrix64F out = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);

        Complex64F a = new Complex64F();
        Complex64F b = new Complex64F();
        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        CommonOps_CD64.add(matrixA, matrixB, out);

        for (int i = 0; i < matrixA.numRows; i++) {
            for (int j = 0; j < matrixA.numCols; j++) {
                matrixA.get(i,j,a);
                matrixB.get(i,j,b);
                out.get(i,j,found);

                ComplexMath64F.plus(a, b, expected);

                assertEquals(expected.real,found.real,UtilEjml.TEST_64F);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void subtract() {
        CDenseMatrix64F matrixA = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        CDenseMatrix64F matrixB = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        CDenseMatrix64F out = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);

        Complex64F a = new Complex64F();
        Complex64F b = new Complex64F();
        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        CommonOps_CD64.subtract(matrixA, matrixB, out);

        for (int i = 0; i < matrixA.numRows; i++) {
            for (int j = 0; j < matrixA.numCols; j++) {
                matrixA.get(i,j,a);
                matrixB.get(i,j,b);
                out.get(i,j,found);

                ComplexMath64F.minus(a, b, expected);

                assertEquals(expected.real,found.real,UtilEjml.TEST_64F);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void scale() {
        Complex64F scale = new Complex64F(2.5,0.4);

        CDenseMatrix64F mat = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        CDenseMatrix64F orig = mat.copy();

        CommonOps_CD64.scale(scale.real, scale.imaginary, mat);

        Complex64F value = new Complex64F();
        Complex64F expected = new Complex64F();
        for (int i = 0; i < mat.numRows; i++) {
            for (int j = 0; j < mat.numCols; j++) {
//                System.out.println("i "+i+" j "+j);
                orig.get(i,j,value);

                ComplexMath64F.multiply(scale,value,expected);
                assertEquals(expected.real, mat.getReal(i,j), UtilEjml.TEST_64F);
                assertEquals(expected.imaginary, mat.getImag(i,j), UtilEjml.TEST_64F);
            }
        }
    }

    /**
     * Make sure the multiplication methods here have the same behavior as the ones in MatrixMatrixMult.
     */
    @Test
    public void checkAllMatrixMult() {
        int numChecked = 0;
        Method methods[] = CommonOps_CD64.class.getMethods();

        for (Method method : methods) {
            String name = method.getName();

            if( !name.startsWith("mult"))
                continue;

            //            System.out.println(name);

            Class[] params = method.getParameterTypes();

            boolean add = name.contains("Add");
            boolean hasAlpha = double.class == params[0];
            boolean transA = name.contains("TransA");
            boolean transB = name.contains("TransB");
            if( name.contains("TransAB") )
                transA = transB = true;

            try {
                TestMatrixMatrixMult_CD64.check(method, add, hasAlpha, transA, transB);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            numChecked++;
        }

        assertEquals(16,numChecked);
    }

    @Test
    public void multiply() {
        for (int i = 1; i < 10; i++) {
            for (int j = 1; j < 10; j++) {
                CDenseMatrix64F A = RandomMatrices_CD64.createRandom(i,j,-1,1,rand);
                for (int k = 1; k < 10; k++) {
                    CDenseMatrix64F B = RandomMatrices_CD64.createRandom(j, k, -1, 1, rand);
                    CDenseMatrix64F found = RandomMatrices_CD64.createRandom(i, k, -1, 1, rand);
                    CDenseMatrix64F expected = TestMatrixMatrixMult_CD64.multiply(A, B, false, false);

                    MatrixMatrixMult_CD64.mult_reorder(A, B, found);

                    assertTrue(i+" "+j+" "+k, MatrixFeatures_CD64.isEquals(expected, found, UtilEjml.TEST_64F));
                }
            }
        }
    }

    @Test
    public void transpose_one() {

        CDenseMatrix64F a = RandomMatrices_CD64.createRandom(4,4,-1,1,rand);
        CDenseMatrix64F b = a.copy();

        CommonOps_CD64.transpose(b);

        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,UtilEjml.TEST_64F);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void transposeConjugate_one() {

        CDenseMatrix64F a = RandomMatrices_CD64.createRandom(4,4,-1,1,rand);
        CDenseMatrix64F b = a.copy();

        CommonOps_CD64.transposeConjugate(b);

        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,UtilEjml.TEST_64F);
                assertEquals(-expected.imaginary,found.imaginary,UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void transpose_two() {

        CDenseMatrix64F a = RandomMatrices_CD64.createRandom(4,5,-1,1,rand);
        CDenseMatrix64F b = RandomMatrices_CD64.createRandom(5,4,-1,1,rand);

        CommonOps_CD64.transpose(a, b);

        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,UtilEjml.TEST_64F);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void transposeConjugate_two() {

        CDenseMatrix64F a = RandomMatrices_CD64.createRandom(4,5,-1,1,rand);
        CDenseMatrix64F b = RandomMatrices_CD64.createRandom(5,4,-1,1,rand);

        CommonOps_CD64.transposeConjugate(a, b);

        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,UtilEjml.TEST_64F);
                assertEquals(-expected.imaginary,found.imaginary,UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void invert_1() {
        for (int i = 1; i < 10; i++) {
            CDenseMatrix64F A = RandomMatrices_CD64.createRandom(i,i,rand);
            CDenseMatrix64F A_orig = A.copy();

            CDenseMatrix64F I = RandomMatrices_CD64.createRandom(i,i,rand);

            assertTrue(CommonOps_CD64.invert(A));
            CommonOps_CD64.mult(A_orig,A,I);

            assertTrue(MatrixFeatures_CD64.isIdentity(I, UtilEjml.TEST_64F));
        }
    }

    @Test
    public void invert_2() {
        for (int i = 1; i < 10; i++) {
            CDenseMatrix64F A = RandomMatrices_CD64.createRandom(i, i, rand);
            CDenseMatrix64F A_orig = A.copy();
            CDenseMatrix64F A_inv = new CDenseMatrix64F(i, i);

            CDenseMatrix64F I = RandomMatrices_CD64.createRandom(i, i, rand);

            assertTrue(CommonOps_CD64.invert(A, A_inv));
            CommonOps_CD64.mult(A, A_inv, I);

            assertTrue(MatrixFeatures_CD64.isIdentity(I, UtilEjml.TEST_64F));
            assertTrue(MatrixFeatures_CD64.isIdentical(A, A_orig, 0));
        }
    }

    @Test
    public void solve() {
        // square
        for (int i = 1; i < 10; i++) {
            CDenseMatrix64F A = RandomMatrices_CD64.createRandom(i, i, rand);
            CDenseMatrix64F B = RandomMatrices_CD64.createRandom(i, 1, rand);

            CDenseMatrix64F A_orig = A.copy();
            CDenseMatrix64F B_orig = B.copy();

            CDenseMatrix64F X = new CDenseMatrix64F(i, 1);

            assertTrue(CommonOps_CD64.solve(A, B, X));

            CDenseMatrix64F found = new CDenseMatrix64F(i, 1);

            CommonOps_CD64.mult(A, X, found);

            assertTrue(MatrixFeatures_CD64.isIdentical(B, found, UtilEjml.TEST_64F));

            assertTrue(MatrixFeatures_CD64.isIdentical(A, A_orig, 0));
            assertTrue(MatrixFeatures_CD64.isIdentical(B, B_orig, 0));
        }

        // rectangular
        for (int i = 1; i < 10; i++) {
            CDenseMatrix64F A = RandomMatrices_CD64.createRandom(2*i, i, rand);
            CDenseMatrix64F X = RandomMatrices_CD64.createRandom(i, 1, rand);
            CDenseMatrix64F B = new CDenseMatrix64F(2*i,1);

            CommonOps_CD64.mult(A,X,B);

            CDenseMatrix64F A_orig = A.copy();
            CDenseMatrix64F B_orig = B.copy();
            CDenseMatrix64F X_expected = X.copy();

            assertTrue(CommonOps_CD64.solve(A, B, X));

            assertTrue(MatrixFeatures_CD64.isIdentical(X, X_expected, UtilEjml.TEST_64F));

            assertTrue(MatrixFeatures_CD64.isIdentical(B, B_orig, 0));
            assertTrue(MatrixFeatures_CD64.isIdentical(A, A_orig, 0));
        }
    }

    @Test
    public void det() {
        CDenseMatrix64F A = new CDenseMatrix64F(3,3,true,
                0.854634 , 0.445620,  0.082836 , 0.212460 , 0.623783 , 0.037631,
                0.585408 , 0.768956 , 0.771067 , 0.897763 , 0.125793 , 0.432187,
                0.303789 , 0.044497 , 0.151182 , 0.034471 , 0.526770 , 0.570333);

        CDenseMatrix64F A_orig = A.copy();

        Complex64F found = CommonOps_CD64.det(A);
        // from octave
        Complex64F expected = new Complex64F(-0.40548 , 0.54188);

        assertEquals(expected.real,found.real,1e-3);
        assertEquals(expected.imaginary,found.imaginary,1e-3);

        assertTrue(MatrixFeatures_CD64.isIdentical(A,A_orig,0));
    }

    @Test
    public void elementMultiply() {
        CDenseMatrix64F in = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        CDenseMatrix64F out = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);

        Complex64F a = new Complex64F(1.2,-0.3);
        Complex64F b = new Complex64F();
        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        CommonOps_CD64.elementMultiply(in,a.real,a.imaginary,out);

        for (int i = 0; i < in.numRows; i++) {
            for (int j = 0; j < in.numCols; j++) {
                in.get(i,j,b);
                out.get(i,j,found);

                ComplexMath64F.multiply(a,b,expected);

                assertEquals(expected.real,found.real,UtilEjml.TEST_64F);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void elementDivide_right() {
        CDenseMatrix64F in = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        CDenseMatrix64F out = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);

        Complex64F a = new Complex64F();
        Complex64F b = new Complex64F(1.2,-0.3);
        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        CommonOps_CD64.elementDivide(in,b.real,b.imaginary,out);

        for (int i = 0; i < in.numRows; i++) {
            for (int j = 0; j < in.numCols; j++) {
                in.get(i,j,a);
                out.get(i,j,found);

                ComplexMath64F.divide(a,b,expected);

                assertEquals(expected.real,found.real,UtilEjml.TEST_64F);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void elementDivide_left() {
        CDenseMatrix64F in = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        CDenseMatrix64F out = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);

        Complex64F a = new Complex64F(1.2,-0.3);
        Complex64F b = new Complex64F();
        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        CommonOps_CD64.elementDivide(a.real,a.imaginary,in,out);

        for (int i = 0; i < in.numRows; i++) {
            for (int j = 0; j < in.numCols; j++) {
                in.get(i,j,b);
                out.get(i,j,found);

                ComplexMath64F.divide(a,b,expected);

                assertEquals(expected.real,found.real,UtilEjml.TEST_64F);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void elementMinReal() {
        CDenseMatrix64F m = new CDenseMatrix64F(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-6, CommonOps_CD64.elementMinReal(m),UtilEjml.TEST_64F);
    }

    @Test
    public void elementMinImaginary() {
        CDenseMatrix64F m = new CDenseMatrix64F(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-5, CommonOps_CD64.elementMinImaginary(m), UtilEjml.TEST_64F);
    }

    @Test
    public void elementMaxReal() {
        CDenseMatrix64F m = new CDenseMatrix64F(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-6 + 11 * 2, CommonOps_CD64.elementMaxReal(m), UtilEjml.TEST_64F);
    }

    @Test
    public void elementMaxImaginary() {
        CDenseMatrix64F m = new CDenseMatrix64F(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-5 + 11 * 2, CommonOps_CD64.elementMaxImaginary(m), UtilEjml.TEST_64F);
    }

    @Test
    public void elementMaxMagnitude2() {
        CDenseMatrix64F m = RandomMatrices_CD64.createRandom(4,5,-2,2,rand);
        DenseMatrix64F a = new DenseMatrix64F(m.numRows,m.numCols);

        CommonOps_CD64.magnitude(m,a);

        double expected = CommonOps_D64.elementMaxAbs(a);
        expected *= expected;

        double found = CommonOps_CD64.elementMaxMagnitude2(m);

        assertEquals(expected,found,UtilEjml.TEST_64F);
    }

    @Test
    public void setIdentity() {
        CDenseMatrix64F a = RandomMatrices_CD64.createRandom(4,5,-2,2,rand);

        CommonOps_CD64.setIdentity(a);

        Complex64F c = new Complex64F();
        for (int i = 0; i < a.numRows; i++) {
            for (int j = 0; j < a.numCols; j++) {
                a.get(i,j,c);
                if( i == j ) {
                    assertEquals(1,c.real,UtilEjml.TEST_64F);
                    assertEquals(0,c.imaginary,UtilEjml.TEST_64F);
                } else {
                    assertEquals(0,c.real,UtilEjml.TEST_64F);
                    assertEquals(0,c.imaginary,UtilEjml.TEST_64F);
                }
            }
        }
    }

    @Test
    public void extract_simplified() {
        CDenseMatrix64F a = RandomMatrices_CD64.createRandom(10,12,-2,2,rand);
        CDenseMatrix64F b = CommonOps_CD64.extract(a,2,5,3,8);

        Complex64F ca = new Complex64F();
        Complex64F cb = new Complex64F();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(2+i,j+3,ca);
                b.get(  i,j  , cb);

                assertEquals(ca.real,cb.real,UtilEjml.TEST_64F);
                assertEquals(ca.imaginary,cb.imaginary,UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void extract_complex() {
        CDenseMatrix64F a = RandomMatrices_CD64.createRandom(10,12,-2,2,rand);
        CDenseMatrix64F b = new CDenseMatrix64F(6,7);

        Complex64F ca = new Complex64F();
        Complex64F cb = new Complex64F();

        CommonOps_CD64.extract(a,2,5,3,7,b,1,2);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(2+i,j+3,ca);
                b.get(1 + i, j + 2, cb);

                assertEquals(ca.real,cb.real,UtilEjml.TEST_64F);
                assertEquals(ca.imaginary,cb.imaginary,UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void columnsToVector() {
        CDenseMatrix64F a = RandomMatrices_CD64.createRandom(10,12,-2,2,rand);
        CDenseMatrix64F v[] = CommonOps_CD64.columnsToVector(a,null);

        Complex64F ca = new Complex64F();
        Complex64F cc = new Complex64F();

        for (int i = 0; i < a.numCols; i++) {
            CDenseMatrix64F c = v[i];

            assertEquals(c.numRows,a.numRows);
            assertEquals(1,c.numCols);

            for (int j = 0; j < a.numRows; j++) {
                a.get(j,i,ca);
                c.get(j,0,cc);

                EjmlUnitTests.assertEquals(ca,cc,UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void elementMaxAbs() {
        CDenseMatrix64F a = RandomMatrices_CD64.createRandom(10,12,-2,2,rand);
        a.set(5,6,10,12);

        double expected = Math.sqrt(10*10 + 12*12);
        double found = CommonOps_CD64.elementMaxAbs(a);
        assertEquals(expected,found,UtilEjml.TEST_64F);
    }
}