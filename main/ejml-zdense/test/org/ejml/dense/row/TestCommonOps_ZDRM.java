/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row;

import org.ejml.EjmlStandardJUnit;
import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.mult.MatrixMatrixMult_ZDRM;
import org.ejml.dense.row.mult.TestMatrixMatrixMult_ZDRM;
import org.ejml.ops.ComplexMath_F64;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCommonOps_ZDRM extends EjmlStandardJUnit {
    @Test void identity_one() {
        ZMatrixRMaj I = CommonOps_ZDRM.identity(4);
        assertEquals(4, I.numRows);
        assertEquals(4, I.numCols);

        assertTrue(MatrixFeatures_ZDRM.isIdentity(I, UtilEjml.TEST_F64));
    }

    @Test void identity_two() {
        ZMatrixRMaj I = CommonOps_ZDRM.identity(4, 5);
        assertEquals(4, I.numRows);
        assertEquals(5, I.numCols);

        assertTrue(MatrixFeatures_ZDRM.isIdentity(I, UtilEjml.TEST_F64));

        I = CommonOps_ZDRM.identity(5, 4);
        assertEquals(5, I.numRows);
        assertEquals(4, I.numCols);

        assertTrue(MatrixFeatures_ZDRM.isIdentity(I, UtilEjml.TEST_F64));
    }

    @Test void diag() {
        ZMatrixRMaj m = CommonOps_ZDRM.diag(1, 2, 3, 4, 5, 6);

        assertEquals(3, m.numRows);
        assertEquals(3, m.numCols);

        Complex_F64 a = new Complex_F64();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                m.get(i, j, a);

                if (i == j) {
                    assertEquals(2*i + 1, a.real, UtilEjml.TEST_F64);
                    assertEquals(2*i + 2, a.imaginary, UtilEjml.TEST_F64);
                } else {
                    assertEquals(0, a.real, UtilEjml.TEST_F64);
                    assertEquals(0, a.imaginary, UtilEjml.TEST_F64);
                }
            }
        }
    }

    @Test void extractDiag() {
        ZMatrixRMaj a = RandomMatrices_ZDRM.rectangle(3, 4, 0, 1, rand);

        for (int i = 0; i < 3; i++) {
            a.set(i, i, i + 1, 0.1);
        }

        ZMatrixRMaj v = new ZMatrixRMaj(3, 1);
        CommonOps_ZDRM.extractDiag(a, v);

        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, v.getReal(i), UtilEjml.TEST_F64);
            assertEquals(0.1, v.getImag(i), UtilEjml.TEST_F64);
        }
    }

    @Test void convert() {
        DMatrixRMaj input = RandomMatrices_DDRM.rectangle(5, 7, -1, 1, rand);
        ZMatrixRMaj output = new ZMatrixRMaj(5, 7);

        Complex_F64 a = new Complex_F64();

        CommonOps_ZDRM.convert(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                output.get(i, j, a);

                assertEquals(input.get(i, j), a.getReal(), UtilEjml.TEST_F64);
                assertEquals(0, a.getImaginary(), UtilEjml.TEST_F64);
            }
        }
    }

    @Test void stripReal() {
        ZMatrixRMaj input = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);
        DMatrixRMaj output = new DMatrixRMaj(5, 7);

        Complex_F64 a = new Complex_F64();

        CommonOps_ZDRM.stripReal(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                input.get(i, j, a);

                assertEquals(a.getReal(), output.get(i, j), UtilEjml.TEST_F64);
            }
        }
    }

    @Test void stripImaginary() {
        ZMatrixRMaj input = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);
        DMatrixRMaj output = new DMatrixRMaj(5, 7);

        Complex_F64 a = new Complex_F64();

        CommonOps_ZDRM.stripImaginary(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                input.get(i, j, a);

                assertEquals(a.getImaginary(), output.get(i, j), UtilEjml.TEST_F64);
            }
        }
    }

    @Test void magnitude() {
        ZMatrixRMaj input = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);
        DMatrixRMaj output = new DMatrixRMaj(5, 7);

        Complex_F64 a = new Complex_F64();

        CommonOps_ZDRM.magnitude(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                input.get(i, j, a);

                assertEquals(a.getMagnitude(), output.get(i, j), UtilEjml.TEST_F64);
            }
        }
    }

    @Test void conjugate() {
        ZMatrixRMaj matrix = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);
        ZMatrixRMaj found = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);

        CommonOps_ZDRM.conjugate(matrix, found);

        for (int i = 0; i < matrix.getDataLength(); i += 2) {
            double real = matrix.data[i];
            double img = matrix.data[i + 1];

            assertEquals(real, found.data[i], UtilEjml.TEST_F64);
            assertEquals(img, -found.data[i + 1], UtilEjml.TEST_F64);
        }
    }

    @Test void fill() {
        ZMatrixRMaj matrix = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);

        CommonOps_ZDRM.fill(matrix, 2, -1);

        for (int i = 0; i < matrix.getDataLength(); i += 2) {
            double real = matrix.data[i];
            double img = matrix.data[i + 1];

            assertEquals(2, real, UtilEjml.TEST_F64);
            assertEquals(-1, img, UtilEjml.TEST_F64);
        }
    }

    @Test void add() {
        ZMatrixRMaj matrixA = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);
        ZMatrixRMaj matrixB = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);
        ZMatrixRMaj out = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);

        Complex_F64 a = new Complex_F64();
        Complex_F64 b = new Complex_F64();
        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        CommonOps_ZDRM.add(matrixA, matrixB, out);

        for (int i = 0; i < matrixA.numRows; i++) {
            for (int j = 0; j < matrixA.numCols; j++) {
                matrixA.get(i, j, a);
                matrixB.get(i, j, b);
                out.get(i, j, found);

                ComplexMath_F64.plus(a, b, expected);

                assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
                assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
            }
        }
    }

    @Test void subtract() {
        ZMatrixRMaj matrixA = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);
        ZMatrixRMaj matrixB = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);
        ZMatrixRMaj out = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);

        Complex_F64 a = new Complex_F64();
        Complex_F64 b = new Complex_F64();
        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        CommonOps_ZDRM.subtract(matrixA, matrixB, out);

        for (int i = 0; i < matrixA.numRows; i++) {
            for (int j = 0; j < matrixA.numCols; j++) {
                matrixA.get(i, j, a);
                matrixB.get(i, j, b);
                out.get(i, j, found);

                ComplexMath_F64.minus(a, b, expected);

                assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
                assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
            }
        }
    }

    @Test void scale() {
        Complex_F64 scale = new Complex_F64(2.5, 0.4);

        ZMatrixRMaj mat = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);
        ZMatrixRMaj orig = mat.copy();

        CommonOps_ZDRM.scale(scale.real, scale.imaginary, mat);

        Complex_F64 value = new Complex_F64();
        Complex_F64 expected = new Complex_F64();
        for (int i = 0; i < mat.numRows; i++) {
            for (int j = 0; j < mat.numCols; j++) {
//                System.out.println("i "+i+" j "+j);
                orig.get(i, j, value);

                ComplexMath_F64.multiply(scale, value, expected);
                assertEquals(expected.real, mat.getReal(i, j), UtilEjml.TEST_F64);
                assertEquals(expected.imaginary, mat.getImag(i, j), UtilEjml.TEST_F64);
            }
        }
    }

    /**
     * Make sure the multiplication methods here have the same behavior as the ones in MatrixMatrixMult.
     */
    @Test void checkAllMatrixMult() {
        int numChecked = 0;
        Method methods[] = CommonOps_ZDRM.class.getMethods();

        for (Method method : methods) {
            String name = method.getName();

            if (!name.startsWith("mult"))
                continue;

            //            System.out.println(name);

            Class[] params = method.getParameterTypes();

            boolean add = name.contains("Add");
            boolean hasAlpha = double.class == params[0];
            boolean transA = name.contains("TransA");
            boolean transB = name.contains("TransB");
            if (name.contains("TransAB"))
                transA = transB = true;

            try {
                TestMatrixMatrixMult_ZDRM.check(method, add, hasAlpha, transA, transB);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            numChecked++;
        }

        assertEquals(16, numChecked);
    }

    @Test void multiply() {
        for (int i = 1; i < 10; i++) {
            for (int j = 1; j < 10; j++) {
                ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(i, j, -1, 1, rand);
                for (int k = 1; k < 10; k++) {
                    ZMatrixRMaj B = RandomMatrices_ZDRM.rectangle(j, k, -1, 1, rand);
                    ZMatrixRMaj found = RandomMatrices_ZDRM.rectangle(i, k, -1, 1, rand);
                    ZMatrixRMaj expected = TestMatrixMatrixMult_ZDRM.multiply(A, B, false, false);

                    MatrixMatrixMult_ZDRM.mult_reorder(A, B, found);

                    assertTrue(MatrixFeatures_ZDRM.isEquals(expected, found, UtilEjml.TEST_F64), i + " " + j + " " + k);
                }
            }
        }
    }

    @Test void transpose_one() {

        ZMatrixRMaj a = RandomMatrices_ZDRM.rectangle(4, 4, -1, 1, rand);
        ZMatrixRMaj b = a.copy();

        CommonOps_ZDRM.transpose(b);

        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(i, j, expected);
                b.get(j, i, found);

                assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
                assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
            }
        }
    }

    @Test void transposeConjugate_one() {

        ZMatrixRMaj a = RandomMatrices_ZDRM.rectangle(4, 4, -1, 1, rand);
        ZMatrixRMaj b = a.copy();

        CommonOps_ZDRM.transposeConjugate(b);

        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(i, j, expected);
                b.get(j, i, found);

                assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
                assertEquals(-expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
            }
        }
    }

    @Test void transpose_two() {

        ZMatrixRMaj a = RandomMatrices_ZDRM.rectangle(4, 5, -1, 1, rand);
        ZMatrixRMaj b = RandomMatrices_ZDRM.rectangle(5, 4, -1, 1, rand);

        CommonOps_ZDRM.transpose(a, b);

        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(i, j, expected);
                b.get(j, i, found);

                assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
                assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
            }
        }
    }

    @Test void transposeConjugate_two() {

        ZMatrixRMaj a = RandomMatrices_ZDRM.rectangle(4, 5, -1, 1, rand);
        ZMatrixRMaj b = RandomMatrices_ZDRM.rectangle(5, 4, -1, 1, rand);

        CommonOps_ZDRM.transposeConjugate(a, b);

        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(i, j, expected);
                b.get(j, i, found);

                assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
                assertEquals(-expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
            }
        }
    }

    @Test void invert_1() {
        for (int i = 1; i < 10; i++) {
            ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(i, i, rand);
            ZMatrixRMaj A_orig = A.copy();

            ZMatrixRMaj I = RandomMatrices_ZDRM.rectangle(i, i, rand);

            assertTrue(CommonOps_ZDRM.invert(A));
            CommonOps_ZDRM.mult(A_orig, A, I);

            assertTrue(MatrixFeatures_ZDRM.isIdentity(I, UtilEjml.TEST_F64));
        }
    }

    @Test void invert_2() {
        for (int i = 1; i < 10; i++) {
            ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(i, i, rand);
            ZMatrixRMaj A_orig = A.copy();
            ZMatrixRMaj A_inv = new ZMatrixRMaj(i, i);

            ZMatrixRMaj I = RandomMatrices_ZDRM.rectangle(i, i, rand);

            assertTrue(CommonOps_ZDRM.invert(A, A_inv));
            CommonOps_ZDRM.mult(A, A_inv, I);

            assertTrue(MatrixFeatures_ZDRM.isIdentity(I, UtilEjml.TEST_F64));
            assertTrue(MatrixFeatures_ZDRM.isIdentical(A, A_orig, 0));
        }
    }

    @Test void solve() {
        // square
        for (int i = 1; i < 10; i++) {
            ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(i, i, rand);
            ZMatrixRMaj B = RandomMatrices_ZDRM.rectangle(i, 1, rand);

            ZMatrixRMaj A_orig = A.copy();
            ZMatrixRMaj B_orig = B.copy();

            ZMatrixRMaj X = new ZMatrixRMaj(i, 1);

            assertTrue(CommonOps_ZDRM.solve(A, B, X));

            ZMatrixRMaj found = new ZMatrixRMaj(i, 1);

            CommonOps_ZDRM.mult(A, X, found);

            assertTrue(MatrixFeatures_ZDRM.isIdentical(B, found, UtilEjml.TEST_F64));

            assertTrue(MatrixFeatures_ZDRM.isIdentical(A, A_orig, 0));
            assertTrue(MatrixFeatures_ZDRM.isIdentical(B, B_orig, 0));
        }

        // rectangular
        for (int i = 1; i < 10; i++) {
            ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(2*i, i, rand);
            ZMatrixRMaj X = RandomMatrices_ZDRM.rectangle(i, 1, rand);
            ZMatrixRMaj B = new ZMatrixRMaj(2*i, 1);

            CommonOps_ZDRM.mult(A, X, B);

            ZMatrixRMaj A_orig = A.copy();
            ZMatrixRMaj B_orig = B.copy();
            ZMatrixRMaj X_expected = X.copy();

            assertTrue(CommonOps_ZDRM.solve(A, B, X));

            assertTrue(MatrixFeatures_ZDRM.isIdentical(X, X_expected, UtilEjml.TEST_F64));

            assertTrue(MatrixFeatures_ZDRM.isIdentical(B, B_orig, 0));
            assertTrue(MatrixFeatures_ZDRM.isIdentical(A, A_orig, 0));
        }
    }

    @Test void det() {
        ZMatrixRMaj A = new ZMatrixRMaj(3, 3, true,
                0.854634, 0.445620, 0.082836, 0.212460, 0.623783, 0.037631,
                0.585408, 0.768956, 0.771067, 0.897763, 0.125793, 0.432187,
                0.303789, 0.044497, 0.151182, 0.034471, 0.526770, 0.570333);

        ZMatrixRMaj A_orig = A.copy();

        Complex_F64 found = CommonOps_ZDRM.det(A);
        // from octave
        Complex_F64 expected = new Complex_F64(-0.40548, 0.54188);

        assertEquals(expected.real, found.real, 1e-3);
        assertEquals(expected.imaginary, found.imaginary, 1e-3);

        assertTrue(MatrixFeatures_ZDRM.isIdentical(A, A_orig, 0));
    }

    @Test void elementMultiply_value() {
        ZMatrixRMaj in = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);
        ZMatrixRMaj out = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);

        Complex_F64 a = new Complex_F64(1.2, -0.3);
        Complex_F64 b = new Complex_F64();
        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        CommonOps_ZDRM.elementMultiply(in, a.real, a.imaginary, out);

        for (int i = 0; i < in.numRows; i++) {
            for (int j = 0; j < in.numCols; j++) {
                in.get(i, j, b);
                out.get(i, j, found);

                ComplexMath_F64.multiply(a, b, expected);

                assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
                assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
            }
        }
    }

    @Test void elementMultiply_matrix() {
        ZMatrixRMaj inA = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);
        ZMatrixRMaj inB = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);
        ZMatrixRMaj out = RandomMatrices_ZDRM.rectangle(2, 5, -1, 1, rand);

        Complex_F64 a = new Complex_F64(1.2, -0.3);
        Complex_F64 b = new Complex_F64();
        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        CommonOps_ZDRM.elementMultiply(inA, inB, out);

        for (int i = 0; i < inA.numRows; i++) {
            for (int j = 0; j < inA.numCols; j++) {
                inA.get(i, j, a);
                inB.get(i, j, b);
                out.get(i, j, found);

                ComplexMath_F64.multiply(a, b, expected);

                assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
                assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
            }
        }
    }

    @Test void elementDivide_right() {
        ZMatrixRMaj in = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);
        ZMatrixRMaj out = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);

        Complex_F64 a = new Complex_F64();
        Complex_F64 b = new Complex_F64(1.2, -0.3);
        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        CommonOps_ZDRM.elementDivide(in, b.real, b.imaginary, out);

        for (int i = 0; i < in.numRows; i++) {
            for (int j = 0; j < in.numCols; j++) {
                in.get(i, j, a);
                out.get(i, j, found);

                ComplexMath_F64.divide(a, b, expected);

                assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
                assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
            }
        }
    }

    @Test void elementDivide_left() {
        ZMatrixRMaj in = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);
        ZMatrixRMaj out = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);

        Complex_F64 a = new Complex_F64(1.2, -0.3);
        Complex_F64 b = new Complex_F64();
        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        CommonOps_ZDRM.elementDivide(a.real, a.imaginary, in, out);

        for (int i = 0; i < in.numRows; i++) {
            for (int j = 0; j < in.numCols; j++) {
                in.get(i, j, b);
                out.get(i, j, found);

                ComplexMath_F64.divide(a, b, expected);

                assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
                assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
            }
        }
    }

    @Test void elementDivide_matrix() {
        ZMatrixRMaj inA = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);
        ZMatrixRMaj inB = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);
        ZMatrixRMaj out = RandomMatrices_ZDRM.rectangle(2, 5, -1, 1, rand);

        Complex_F64 a = new Complex_F64(1.2, -0.3);
        Complex_F64 b = new Complex_F64();
        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        CommonOps_ZDRM.elementDivide(inA, inB, out);

        for (int i = 0; i < inA.numRows; i++) {
            for (int j = 0; j < inA.numCols; j++) {
                inA.get(i, j, a);
                inB.get(i, j, b);
                out.get(i, j, found);

                ComplexMath_F64.divide(a, b, expected);

                assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
                assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
            }
        }
    }

    @Test void elementPower_right() {
        ZMatrixRMaj in = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);
        ZMatrixRMaj out = RandomMatrices_ZDRM.rectangle(5, 7, -1, 1, rand);

        Complex_F64 a = new Complex_F64();
        Complex_F64 found = new Complex_F64();

        // power of 2 since it's easy to compute
        CommonOps_ZDRM.elementPower(in, 2.0, out);

        for (int i = 0; i < in.numRows; i++) {
            for (int j = 0; j < in.numCols; j++) {
                in.get(i, j, a);
                out.get(i, j, found);

                Complex_F64 expected = a.times(a);

                assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
                assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
            }
        }
    }

    @Test void elementMinReal() {
        ZMatrixRMaj m = new ZMatrixRMaj(3, 4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-6, CommonOps_ZDRM.elementMinReal(m), UtilEjml.TEST_F64);
    }

    @Test void elementMinImaginary() {
        ZMatrixRMaj m = new ZMatrixRMaj(3, 4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-5, CommonOps_ZDRM.elementMinImaginary(m), UtilEjml.TEST_F64);
    }

    @Test void elementMaxReal() {
        ZMatrixRMaj m = new ZMatrixRMaj(3, 4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-6 + 11*2, CommonOps_ZDRM.elementMaxReal(m), UtilEjml.TEST_F64);
    }

    @Test void elementMaxImaginary() {
        ZMatrixRMaj m = new ZMatrixRMaj(3, 4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-5 + 11*2, CommonOps_ZDRM.elementMaxImaginary(m), UtilEjml.TEST_F64);
    }

    @Test void elementMaxMagnitude2() {
        ZMatrixRMaj m = RandomMatrices_ZDRM.rectangle(4, 5, -2, 2, rand);
        DMatrixRMaj a = new DMatrixRMaj(m.numRows, m.numCols);

        CommonOps_ZDRM.magnitude(m, a);

        double expected = CommonOps_DDRM.elementMaxAbs(a);
        expected *= expected;

        double found = CommonOps_ZDRM.elementMaxMagnitude2(m);

        assertEquals(expected, found, UtilEjml.TEST_F64);
    }

    @Test void setIdentity() {
        ZMatrixRMaj a = RandomMatrices_ZDRM.rectangle(4, 5, -2, 2, rand);

        CommonOps_ZDRM.setIdentity(a);

        Complex_F64 c = new Complex_F64();
        for (int i = 0; i < a.numRows; i++) {
            for (int j = 0; j < a.numCols; j++) {
                a.get(i, j, c);
                if (i == j) {
                    assertEquals(1, c.real, UtilEjml.TEST_F64);
                    assertEquals(0, c.imaginary, UtilEjml.TEST_F64);
                } else {
                    assertEquals(0, c.real, UtilEjml.TEST_F64);
                    assertEquals(0, c.imaginary, UtilEjml.TEST_F64);
                }
            }
        }
    }

    @Test void extract_simplified() {
        ZMatrixRMaj a = RandomMatrices_ZDRM.rectangle(10, 12, -2, 2, rand);
        ZMatrixRMaj b = CommonOps_ZDRM.extract(a, 2, 5, 3, 8);

        Complex_F64 ca = new Complex_F64();
        Complex_F64 cb = new Complex_F64();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(2 + i, j + 3, ca);
                b.get(i, j, cb);

                assertEquals(ca.real, cb.real, UtilEjml.TEST_F64);
                assertEquals(ca.imaginary, cb.imaginary, UtilEjml.TEST_F64);
            }
        }
    }

    @Test void extract_complex() {
        ZMatrixRMaj a = RandomMatrices_ZDRM.rectangle(10, 12, -2, 2, rand);
        ZMatrixRMaj b = new ZMatrixRMaj(6, 7);

        Complex_F64 ca = new Complex_F64();
        Complex_F64 cb = new Complex_F64();

        CommonOps_ZDRM.extract(a, 2, 5, 3, 7, b, 1, 2);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(2 + i, j + 3, ca);
                b.get(1 + i, j + 2, cb);

                assertEquals(ca.real, cb.real, UtilEjml.TEST_F64);
                assertEquals(ca.imaginary, cb.imaginary, UtilEjml.TEST_F64);
            }
        }
    }

    @Test void columnsToVector() {
        ZMatrixRMaj a = RandomMatrices_ZDRM.rectangle(10, 12, -2, 2, rand);
        ZMatrixRMaj v[] = CommonOps_ZDRM.columnsToVector(a, null);

        Complex_F64 ca = new Complex_F64();
        Complex_F64 cc = new Complex_F64();

        for (int i = 0; i < a.numCols; i++) {
            ZMatrixRMaj c = v[i];

            assertEquals(c.numRows, a.numRows);
            assertEquals(1, c.numCols);

            for (int j = 0; j < a.numRows; j++) {
                a.get(j, i, ca);
                c.get(j, 0, cc);

                EjmlUnitTests.assertEquals(ca, cc, UtilEjml.TEST_F64);
            }
        }
    }

    @Test void elementMaxAbs() {
        ZMatrixRMaj a = RandomMatrices_ZDRM.rectangle(10, 12, -2, 2, rand);
        a.set(5, 6, 10, 12);

        double expected = Math.sqrt(10*10 + 12*12);
        double found = CommonOps_ZDRM.elementMaxAbs(a);
        assertEquals(expected, found, UtilEjml.TEST_F64);
    }

    @Test void elementMinAbs() {
        ZMatrixRMaj a = RandomMatrices_ZDRM.rectangle(10, 12, -8, -2, rand);
        a.set(5, 6, 1, 2);

        double expected = Math.sqrt(1 + 2*2);
        double found = CommonOps_ZDRM.elementMinAbs(a);
        assertEquals(expected, found, UtilEjml.TEST_F64);
    }
}