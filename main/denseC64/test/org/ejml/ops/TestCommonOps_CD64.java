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
import org.ejml.data.Complex_F64;
import org.ejml.data.RowMatrix_C64;
import org.ejml.data.RowMatrix_F64;
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
        RowMatrix_C64 I = CommonOps_CD64.identity(4);
        assertEquals(4,I.numRows);
        assertEquals(4,I.numCols);

        assertTrue(MatrixFeatures_CD64.isIdentity(I, UtilEjml.TEST_F64));
    }

    @Test
    public void identity_two() {
        RowMatrix_C64 I = CommonOps_CD64.identity(4,5);
        assertEquals(4,I.numRows);
        assertEquals(5,I.numCols);

        assertTrue(MatrixFeatures_CD64.isIdentity(I,UtilEjml.TEST_F64));

        I = CommonOps_CD64.identity(5,4);
        assertEquals(5,I.numRows);
        assertEquals(4,I.numCols);

        assertTrue(MatrixFeatures_CD64.isIdentity(I,UtilEjml.TEST_F64));
    }

    @Test
    public void diag() {
        RowMatrix_C64 m = CommonOps_CD64.diag(1,2,3,4,5,6);

        assertEquals(3,m.numRows);
        assertEquals(3,m.numCols);

        Complex_F64 a = new Complex_F64();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                m.get(i,j,a);

                if( i == j ) {
                    assertEquals(2*i+1,a.real,UtilEjml.TEST_F64);
                    assertEquals(2*i+2,a.imaginary,UtilEjml.TEST_F64);
                } else {
                    assertEquals(0,a.real,UtilEjml.TEST_F64);
                    assertEquals(0,a.imaginary,UtilEjml.TEST_F64);
                }
            }
        }
    }

    @Test
    public void convert() {
        RowMatrix_F64 input = RandomMatrices_D64.createRandom(5,7,-1,1,rand);
        RowMatrix_C64 output = new RowMatrix_C64(5,7);

        Complex_F64 a = new Complex_F64();

        CommonOps_CD64.convert(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                output.get(i,j,a);

                assertEquals(input.get(i,j),a.getReal(),UtilEjml.TEST_F64);
                assertEquals(0,a.getImaginary(),UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void stripReal() {
        RowMatrix_C64 input = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        RowMatrix_F64 output = new RowMatrix_F64(5,7);

        Complex_F64 a = new Complex_F64();

        CommonOps_CD64.stripReal(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                input.get(i,j,a);

                assertEquals(a.getReal(),output.get(i,j),UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void stripImaginary() {
        RowMatrix_C64 input = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        RowMatrix_F64 output = new RowMatrix_F64(5,7);

        Complex_F64 a = new Complex_F64();

        CommonOps_CD64.stripImaginary(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                input.get(i,j,a);

                assertEquals(a.getImaginary(),output.get(i,j),UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void magnitude() {
        RowMatrix_C64 input = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        RowMatrix_F64 output = new RowMatrix_F64(5,7);

        Complex_F64 a = new Complex_F64();

        CommonOps_CD64.magnitude(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                input.get(i,j,a);

                assertEquals(a.getMagnitude(),output.get(i,j),UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void conjugate() {
        RowMatrix_C64 matrix = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        RowMatrix_C64 found = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);

        CommonOps_CD64.conjugate(matrix,found);

        for (int i = 0; i < matrix.getDataLength(); i += 2) {
            double real = matrix.data[i];
            double img = matrix.data[i+1];

            assertEquals(real, found.data[i],UtilEjml.TEST_F64);
            assertEquals(img, -found.data[i+1],UtilEjml.TEST_F64);
        }
    }

    @Test
    public void fill() {
        RowMatrix_C64 matrix = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);

        CommonOps_CD64.fill(matrix,2,-1);

        for (int i = 0; i < matrix.getDataLength(); i += 2) {
            double real = matrix.data[i];
            double img = matrix.data[i+1];

            assertEquals(2,real,UtilEjml.TEST_F64);
            assertEquals(-1,img,UtilEjml.TEST_F64);
        }
    }

    @Test
    public void add() {
        RowMatrix_C64 matrixA = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        RowMatrix_C64 matrixB = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        RowMatrix_C64 out = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);

        Complex_F64 a = new Complex_F64();
        Complex_F64 b = new Complex_F64();
        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        CommonOps_CD64.add(matrixA, matrixB, out);

        for (int i = 0; i < matrixA.numRows; i++) {
            for (int j = 0; j < matrixA.numCols; j++) {
                matrixA.get(i,j,a);
                matrixB.get(i,j,b);
                out.get(i,j,found);

                ComplexMath_F64.plus(a, b, expected);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F64);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void subtract() {
        RowMatrix_C64 matrixA = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        RowMatrix_C64 matrixB = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        RowMatrix_C64 out = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);

        Complex_F64 a = new Complex_F64();
        Complex_F64 b = new Complex_F64();
        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        CommonOps_CD64.subtract(matrixA, matrixB, out);

        for (int i = 0; i < matrixA.numRows; i++) {
            for (int j = 0; j < matrixA.numCols; j++) {
                matrixA.get(i,j,a);
                matrixB.get(i,j,b);
                out.get(i,j,found);

                ComplexMath_F64.minus(a, b, expected);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F64);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void scale() {
        Complex_F64 scale = new Complex_F64(2.5,0.4);

        RowMatrix_C64 mat = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        RowMatrix_C64 orig = mat.copy();

        CommonOps_CD64.scale(scale.real, scale.imaginary, mat);

        Complex_F64 value = new Complex_F64();
        Complex_F64 expected = new Complex_F64();
        for (int i = 0; i < mat.numRows; i++) {
            for (int j = 0; j < mat.numCols; j++) {
//                System.out.println("i "+i+" j "+j);
                orig.get(i,j,value);

                ComplexMath_F64.multiply(scale,value,expected);
                assertEquals(expected.real, mat.getReal(i,j), UtilEjml.TEST_F64);
                assertEquals(expected.imaginary, mat.getImag(i,j), UtilEjml.TEST_F64);
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
                RowMatrix_C64 A = RandomMatrices_CD64.createRandom(i,j,-1,1,rand);
                for (int k = 1; k < 10; k++) {
                    RowMatrix_C64 B = RandomMatrices_CD64.createRandom(j, k, -1, 1, rand);
                    RowMatrix_C64 found = RandomMatrices_CD64.createRandom(i, k, -1, 1, rand);
                    RowMatrix_C64 expected = TestMatrixMatrixMult_CD64.multiply(A, B, false, false);

                    MatrixMatrixMult_CD64.mult_reorder(A, B, found);

                    assertTrue(i+" "+j+" "+k, MatrixFeatures_CD64.isEquals(expected, found, UtilEjml.TEST_F64));
                }
            }
        }
    }

    @Test
    public void transpose_one() {

        RowMatrix_C64 a = RandomMatrices_CD64.createRandom(4,4,-1,1,rand);
        RowMatrix_C64 b = a.copy();

        CommonOps_CD64.transpose(b);

        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F64);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void transposeConjugate_one() {

        RowMatrix_C64 a = RandomMatrices_CD64.createRandom(4,4,-1,1,rand);
        RowMatrix_C64 b = a.copy();

        CommonOps_CD64.transposeConjugate(b);

        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F64);
                assertEquals(-expected.imaginary,found.imaginary,UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void transpose_two() {

        RowMatrix_C64 a = RandomMatrices_CD64.createRandom(4,5,-1,1,rand);
        RowMatrix_C64 b = RandomMatrices_CD64.createRandom(5,4,-1,1,rand);

        CommonOps_CD64.transpose(a, b);

        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F64);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void transposeConjugate_two() {

        RowMatrix_C64 a = RandomMatrices_CD64.createRandom(4,5,-1,1,rand);
        RowMatrix_C64 b = RandomMatrices_CD64.createRandom(5,4,-1,1,rand);

        CommonOps_CD64.transposeConjugate(a, b);

        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F64);
                assertEquals(-expected.imaginary,found.imaginary,UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void invert_1() {
        for (int i = 1; i < 10; i++) {
            RowMatrix_C64 A = RandomMatrices_CD64.createRandom(i,i,rand);
            RowMatrix_C64 A_orig = A.copy();

            RowMatrix_C64 I = RandomMatrices_CD64.createRandom(i,i,rand);

            assertTrue(CommonOps_CD64.invert(A));
            CommonOps_CD64.mult(A_orig,A,I);

            assertTrue(MatrixFeatures_CD64.isIdentity(I, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void invert_2() {
        for (int i = 1; i < 10; i++) {
            RowMatrix_C64 A = RandomMatrices_CD64.createRandom(i, i, rand);
            RowMatrix_C64 A_orig = A.copy();
            RowMatrix_C64 A_inv = new RowMatrix_C64(i, i);

            RowMatrix_C64 I = RandomMatrices_CD64.createRandom(i, i, rand);

            assertTrue(CommonOps_CD64.invert(A, A_inv));
            CommonOps_CD64.mult(A, A_inv, I);

            assertTrue(MatrixFeatures_CD64.isIdentity(I, UtilEjml.TEST_F64));
            assertTrue(MatrixFeatures_CD64.isIdentical(A, A_orig, 0));
        }
    }

    @Test
    public void solve() {
        // square
        for (int i = 1; i < 10; i++) {
            RowMatrix_C64 A = RandomMatrices_CD64.createRandom(i, i, rand);
            RowMatrix_C64 B = RandomMatrices_CD64.createRandom(i, 1, rand);

            RowMatrix_C64 A_orig = A.copy();
            RowMatrix_C64 B_orig = B.copy();

            RowMatrix_C64 X = new RowMatrix_C64(i, 1);

            assertTrue(CommonOps_CD64.solve(A, B, X));

            RowMatrix_C64 found = new RowMatrix_C64(i, 1);

            CommonOps_CD64.mult(A, X, found);

            assertTrue(MatrixFeatures_CD64.isIdentical(B, found, UtilEjml.TEST_F64));

            assertTrue(MatrixFeatures_CD64.isIdentical(A, A_orig, 0));
            assertTrue(MatrixFeatures_CD64.isIdentical(B, B_orig, 0));
        }

        // rectangular
        for (int i = 1; i < 10; i++) {
            RowMatrix_C64 A = RandomMatrices_CD64.createRandom(2*i, i, rand);
            RowMatrix_C64 X = RandomMatrices_CD64.createRandom(i, 1, rand);
            RowMatrix_C64 B = new RowMatrix_C64(2*i,1);

            CommonOps_CD64.mult(A,X,B);

            RowMatrix_C64 A_orig = A.copy();
            RowMatrix_C64 B_orig = B.copy();
            RowMatrix_C64 X_expected = X.copy();

            assertTrue(CommonOps_CD64.solve(A, B, X));

            assertTrue(MatrixFeatures_CD64.isIdentical(X, X_expected, UtilEjml.TEST_F64));

            assertTrue(MatrixFeatures_CD64.isIdentical(B, B_orig, 0));
            assertTrue(MatrixFeatures_CD64.isIdentical(A, A_orig, 0));
        }
    }

    @Test
    public void det() {
        RowMatrix_C64 A = new RowMatrix_C64(3,3,true,
                0.854634 , 0.445620,  0.082836 , 0.212460 , 0.623783 , 0.037631,
                0.585408 , 0.768956 , 0.771067 , 0.897763 , 0.125793 , 0.432187,
                0.303789 , 0.044497 , 0.151182 , 0.034471 , 0.526770 , 0.570333);

        RowMatrix_C64 A_orig = A.copy();

        Complex_F64 found = CommonOps_CD64.det(A);
        // from octave
        Complex_F64 expected = new Complex_F64(-0.40548 , 0.54188);

        assertEquals(expected.real,found.real,1e-3);
        assertEquals(expected.imaginary,found.imaginary,1e-3);

        assertTrue(MatrixFeatures_CD64.isIdentical(A,A_orig,0));
    }

    @Test
    public void elementMultiply() {
        RowMatrix_C64 in = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        RowMatrix_C64 out = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);

        Complex_F64 a = new Complex_F64(1.2,-0.3);
        Complex_F64 b = new Complex_F64();
        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        CommonOps_CD64.elementMultiply(in,a.real,a.imaginary,out);

        for (int i = 0; i < in.numRows; i++) {
            for (int j = 0; j < in.numCols; j++) {
                in.get(i,j,b);
                out.get(i,j,found);

                ComplexMath_F64.multiply(a,b,expected);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F64);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void elementDivide_right() {
        RowMatrix_C64 in = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        RowMatrix_C64 out = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);

        Complex_F64 a = new Complex_F64();
        Complex_F64 b = new Complex_F64(1.2,-0.3);
        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        CommonOps_CD64.elementDivide(in,b.real,b.imaginary,out);

        for (int i = 0; i < in.numRows; i++) {
            for (int j = 0; j < in.numCols; j++) {
                in.get(i,j,a);
                out.get(i,j,found);

                ComplexMath_F64.divide(a,b,expected);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F64);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void elementDivide_left() {
        RowMatrix_C64 in = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);
        RowMatrix_C64 out = RandomMatrices_CD64.createRandom(5,7,-1,1,rand);

        Complex_F64 a = new Complex_F64(1.2,-0.3);
        Complex_F64 b = new Complex_F64();
        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        CommonOps_CD64.elementDivide(a.real,a.imaginary,in,out);

        for (int i = 0; i < in.numRows; i++) {
            for (int j = 0; j < in.numCols; j++) {
                in.get(i,j,b);
                out.get(i,j,found);

                ComplexMath_F64.divide(a,b,expected);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F64);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void elementMinReal() {
        RowMatrix_C64 m = new RowMatrix_C64(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-6, CommonOps_CD64.elementMinReal(m),UtilEjml.TEST_F64);
    }

    @Test
    public void elementMinImaginary() {
        RowMatrix_C64 m = new RowMatrix_C64(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-5, CommonOps_CD64.elementMinImaginary(m), UtilEjml.TEST_F64);
    }

    @Test
    public void elementMaxReal() {
        RowMatrix_C64 m = new RowMatrix_C64(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-6 + 11 * 2, CommonOps_CD64.elementMaxReal(m), UtilEjml.TEST_F64);
    }

    @Test
    public void elementMaxImaginary() {
        RowMatrix_C64 m = new RowMatrix_C64(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-5 + 11 * 2, CommonOps_CD64.elementMaxImaginary(m), UtilEjml.TEST_F64);
    }

    @Test
    public void elementMaxMagnitude2() {
        RowMatrix_C64 m = RandomMatrices_CD64.createRandom(4,5,-2,2,rand);
        RowMatrix_F64 a = new RowMatrix_F64(m.numRows,m.numCols);

        CommonOps_CD64.magnitude(m,a);

        double expected = CommonOps_D64.elementMaxAbs(a);
        expected *= expected;

        double found = CommonOps_CD64.elementMaxMagnitude2(m);

        assertEquals(expected,found,UtilEjml.TEST_F64);
    }

    @Test
    public void setIdentity() {
        RowMatrix_C64 a = RandomMatrices_CD64.createRandom(4,5,-2,2,rand);

        CommonOps_CD64.setIdentity(a);

        Complex_F64 c = new Complex_F64();
        for (int i = 0; i < a.numRows; i++) {
            for (int j = 0; j < a.numCols; j++) {
                a.get(i,j,c);
                if( i == j ) {
                    assertEquals(1,c.real,UtilEjml.TEST_F64);
                    assertEquals(0,c.imaginary,UtilEjml.TEST_F64);
                } else {
                    assertEquals(0,c.real,UtilEjml.TEST_F64);
                    assertEquals(0,c.imaginary,UtilEjml.TEST_F64);
                }
            }
        }
    }

    @Test
    public void extract_simplified() {
        RowMatrix_C64 a = RandomMatrices_CD64.createRandom(10,12,-2,2,rand);
        RowMatrix_C64 b = CommonOps_CD64.extract(a,2,5,3,8);

        Complex_F64 ca = new Complex_F64();
        Complex_F64 cb = new Complex_F64();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(2+i,j+3,ca);
                b.get(  i,j  , cb);

                assertEquals(ca.real,cb.real,UtilEjml.TEST_F64);
                assertEquals(ca.imaginary,cb.imaginary,UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void extract_complex() {
        RowMatrix_C64 a = RandomMatrices_CD64.createRandom(10,12,-2,2,rand);
        RowMatrix_C64 b = new RowMatrix_C64(6,7);

        Complex_F64 ca = new Complex_F64();
        Complex_F64 cb = new Complex_F64();

        CommonOps_CD64.extract(a,2,5,3,7,b,1,2);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(2+i,j+3,ca);
                b.get(1 + i, j + 2, cb);

                assertEquals(ca.real,cb.real,UtilEjml.TEST_F64);
                assertEquals(ca.imaginary,cb.imaginary,UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void columnsToVector() {
        RowMatrix_C64 a = RandomMatrices_CD64.createRandom(10,12,-2,2,rand);
        RowMatrix_C64 v[] = CommonOps_CD64.columnsToVector(a,null);

        Complex_F64 ca = new Complex_F64();
        Complex_F64 cc = new Complex_F64();

        for (int i = 0; i < a.numCols; i++) {
            RowMatrix_C64 c = v[i];

            assertEquals(c.numRows,a.numRows);
            assertEquals(1,c.numCols);

            for (int j = 0; j < a.numRows; j++) {
                a.get(j,i,ca);
                c.get(j,0,cc);

                EjmlUnitTests.assertEquals(ca,cc,UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void elementMaxAbs() {
        RowMatrix_C64 a = RandomMatrices_CD64.createRandom(10,12,-2,2,rand);
        a.set(5,6,10,12);

        double expected = Math.sqrt(10*10 + 12*12);
        double found = CommonOps_CD64.elementMaxAbs(a);
        assertEquals(expected,found,UtilEjml.TEST_F64);
    }
}