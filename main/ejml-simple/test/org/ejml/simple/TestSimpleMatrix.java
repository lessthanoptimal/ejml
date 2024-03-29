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

package org.ejml.simple;

import org.ejml.EjmlStandardJUnit;
import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.*;
import org.ejml.dense.row.*;
import org.ejml.ops.ConvertMatrixType;
import org.ejml.simple.ops.SimpleOperations_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestSimpleMatrix extends EjmlStandardJUnit {
    @Test void randomNormal() {
        SimpleMatrix Q = SimpleMatrix.diag(5, 3, 12);
        Q.set(0, 1, 0.5);
        Q.set(1, 0, 0.5);

        int N = 200;
        double[] sum = new double[3];
        for (int i = 0; i < N; i++) {
            SimpleMatrix x = SimpleMatrix.randomNormal(Q, rand);

            for (int j = 0; j < x.getNumElements(); j++) {
                sum[j] += x.get(j);
            }
        }
        for (int i = 0; i < sum.length; i++) {
            sum[i] /= N;
            assertTrue(sum[i] != 0);
            assertEquals(0, sum[i], 0.4);
        }
    }

    @Test void constructor_1d_array() {
        double[] d = new double[]{2, 5, 3, 9, -2, 6, 7, 4};
        SimpleMatrix s = new SimpleMatrix(3, 2, true, d);
        DMatrixRMaj m = new DMatrixRMaj(3, 2, true, d);

        EjmlUnitTests.assertEquals((DMatrixRMaj)m, (DMatrixRMaj)s.getMatrix(), UtilEjml.TEST_F64);
    }

    @Test void constructor_1d_array_simple() {
        double[] d = new double[]{2, 5, 3, 9, -2, 6, 7, 4};
        SimpleMatrix s = new SimpleMatrix(d);
        DMatrixRMaj m = new DMatrixRMaj(8, 1, true, d);

        EjmlUnitTests.assertEquals((DMatrixRMaj)m, (DMatrixRMaj)s.getMatrix(), UtilEjml.TEST_F64);
    }

    @Test void constructor_2d_array() {
        double[][] d = new double[][]{{1, 2}, {3, 4}, {5, 6}};

        SimpleMatrix s = new SimpleMatrix(d);
        DMatrixRMaj mat = new DMatrixRMaj(d);

        EjmlUnitTests.assertEquals((DMatrixRMaj)mat, (DMatrixRMaj)s.getMatrix(), UtilEjml.TEST_F64);
    }

    @Test void constructor_dense() {
        DMatrixRMaj mat = RandomMatrices_DDRM.rectangle(3, 2, rand);
        SimpleMatrix s = new SimpleMatrix(mat);

        assertNotSame(mat, s.getMatrix());
        EjmlUnitTests.assertEquals(mat, s.getMatrix());
    }

    @Test void constructor_simple() {
        SimpleMatrix orig = SimpleMatrix.random_DDRM(3, 2, 0, 1, rand);
        SimpleMatrix copy = new SimpleMatrix(orig);

        assertNotSame(orig.mat, copy.mat);
        EjmlUnitTests.assertEquals(orig.mat, copy.mat);
    }

    @Test void wrap() {
        DMatrixRMaj mat = RandomMatrices_DDRM.rectangle(3, 2, rand);

        SimpleMatrix s = SimpleMatrix.wrap(mat);

        assertSame(s.mat, mat);
    }

    @Test void identity() {
        SimpleMatrix s = SimpleMatrix.identity(3);

        DMatrixRMaj d = CommonOps_DDRM.identity(3);

        EjmlUnitTests.assertEquals(d, (DMatrixRMaj)s.mat, UtilEjml.TEST_F64);
    }

    @Test void ones() {
        SimpleMatrix s = SimpleMatrix.ones(3, 3);

        DMatrixRMaj d = new DMatrixRMaj(3, 3);
        d.fill(1);

        EjmlUnitTests.assertEquals(d, (DMatrixRMaj)s.mat, UtilEjml.TEST_F64);
    }

    @Test void filled() {
        SimpleMatrix s = SimpleMatrix.filled(3, 3, 2);

        DMatrixRMaj d = new DMatrixRMaj(3, 3);
        d.fill(2);

        EjmlUnitTests.assertEquals(d, (DMatrixRMaj)s.mat, UtilEjml.TEST_F64);
    }

    @Test void fillComplex() {
        List<SimpleMatrix> inputs = new ArrayList<>();
        inputs.add(SimpleMatrix.random_DDRM(3, 4, -1, 1, rand));
        inputs.add(SimpleMatrix.random_FDRM(3, 4, -1, 1, rand));
        inputs.add(SimpleMatrix.random_ZDRM(3, 4, -1, 1, rand));
        inputs.add(SimpleMatrix.random_CDRM(3, 4, -1, 1, rand));

        for (SimpleMatrix A : inputs) {
            boolean shouldChangeMatrix = A.mat.getType().isReal();
            Matrix originalMat = A.mat;

            A.fillComplex(1, 2);
            // Matrix must be complex now
            assertFalse(A.mat.getType().isReal());

            // if complex then the matrix shouldn't be modified
            assertEquals(shouldChangeMatrix, originalMat != A.mat);

            // Make sure it has the same value
            var value = new Complex_F64();
            for (int row = 0; row < A.getNumRows(); row++) {
                for (int col = 0; col < A.getNumCols(); col++) {
                    A.get(row, col, value);
                    assertEquals(1, value.real);
                    assertEquals(2, value.imaginary);
                }
            }
        }
    }

    @Test void getMatrix() {
        SimpleMatrix s = new SimpleMatrix(3, 2);

        // make sure a new instance isn't returned
        assertSame(s.mat, s.getMatrix());
    }

    @Test void convertToSparse() {
        var data = new double[]{0, 1, 0, 1};
        var simpleMatrix = new SimpleMatrix(2, 2, true, data);
        assertTrue(simpleMatrix.getType().isDense());
        var denseMatrix = simpleMatrix.mat.copy();

        simpleMatrix.convertToSparse();

        assertFalse(simpleMatrix.getType().isDense());
        EjmlUnitTests.assertEquals(denseMatrix, simpleMatrix.mat);
    }

    @Test void transformMatrix() {
        var data = new float[]{0, 1, 0, 1};
        var s = new SimpleMatrix(2, 2, true, data);

        var sparseDMatrix = s.getDSCC();
        var sparseFMatrix = s.getFSCC();
        var denseDMatrix = s.getDDRM();
        var denseFMatrix = s.getFDRM();

        EjmlUnitTests.assertEquals(sparseDMatrix, denseDMatrix);
        EjmlUnitTests.assertEquals(sparseDMatrix, sparseFMatrix);
        EjmlUnitTests.assertEquals(sparseDMatrix, denseFMatrix);
    }

    @Test void transpose() {
        SimpleMatrix orig = SimpleMatrix.random_DDRM(3, 2, 0, 1, rand);
        SimpleMatrix tran = orig.transpose();

        DMatrixRMaj dTran = new DMatrixRMaj(2, 3);
        CommonOps_DDRM.transpose((DMatrixRMaj)orig.mat, dTran);

        EjmlUnitTests.assertEquals(dTran, (DMatrixRMaj)tran.mat, UtilEjml.TEST_F64);
    }

    @Test void transposeConjugate_real() {
        SimpleMatrix orig = SimpleMatrix.random_DDRM(5, 2, -1, 1, rand);
        SimpleMatrix tran = orig.transposeConjugate();

        EjmlUnitTests.assertEquals(orig.transpose().getDDRM(), tran.getDDRM(), UtilEjml.TEST_F64);
    }

    @Test void transposeConjugate_complex() {
        SimpleMatrix orig = SimpleMatrix.random_ZDRM(5, 2, -1, 1, rand);
        SimpleMatrix tran = orig.transposeConjugate();

        ZMatrixRMaj expected = CommonOps_ZDRM.transposeConjugate(orig.getZDRM(), null);

        EjmlUnitTests.assertEquals(expected, tran.getZDRM(), UtilEjml.TEST_F64);
    }

    @Test void mult() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 2, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random_DDRM(2, 3, 0, 1, rand);
        SimpleMatrix c = a.mult(b);

        DMatrixRMaj c_dense = new DMatrixRMaj(3, 3);
        CommonOps_DDRM.mult((DMatrixRMaj)a.mat, (DMatrixRMaj)b.mat, c_dense);

        EjmlUnitTests.assertEquals(c_dense, (DMatrixRMaj)c.mat, UtilEjml.TEST_F64);
    }

    @Test void kron() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 2, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random_DDRM(2, 3, 0, 1, rand);
        SimpleMatrix c = a.kron(b);

        DMatrixRMaj c_dense = new DMatrixRMaj(6, 6);
        CommonOps_DDRM.kron((DMatrixRMaj)a.getMatrix(), (DMatrixRMaj)b.getMatrix(), c_dense);

        EjmlUnitTests.assertEquals(c_dense, c.mat);
    }

//    @Test
//    public void mult_trans() {
//        SimpleMatrix a = SimpleMatrix.random(3,2,rand);
//        SimpleMatrix b = SimpleMatrix.random(2,3,rand);
//        SimpleMatrix c;
//
//        DMatrixRMaj c_dense = new DMatrixRMaj(3,3);
//        CommonOps.mult(a.mat,b.mat,c_dense);
//
//        c = a.mult(false,false,b);
//        EjmlUnitTests.assertEquals(c_dense,c.mat);
//        c = a.transpose().mult(true,false,b);
//        EjmlUnitTests.assertEquals(c_dense,c.mat);
//        c = a.mult(false,true,b.transpose());
//        EjmlUnitTests.assertEquals(c_dense,c.mat);
//        c = a.transpose().mult(true,true,b.transpose());
//        EjmlUnitTests.assertEquals(c_dense,c.mat);
//    }

    @Test void plus() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 2, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random_DDRM(3, 2, 0, 1, rand);
        SimpleMatrix c = a.plus(b);

        DMatrixRMaj c_dense = new DMatrixRMaj(3, 2);
        CommonOps_DDRM.add((DMatrixRMaj)a.mat, (DMatrixRMaj)b.mat, c_dense);

        EjmlUnitTests.assertEquals(c_dense, c.mat);
    }

    @Test void plus_scalar() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 2, 0, 1, rand);
        double b = 2.5;
        SimpleMatrix c = a.plus(b);

        DMatrixRMaj c_dense = new DMatrixRMaj(3, 2);
        CommonOps_DDRM.add((DMatrixRMaj)a.mat, b, c_dense);

        EjmlUnitTests.assertEquals(c_dense, c.mat);
    }

    @Test void dot() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(10, 1, -1, 1, rand);
        SimpleMatrix b = SimpleMatrix.random_DDRM(10, 1, -1, 1, rand);

        double expected = 0;
        for (int i = 0; i < 10; i++)
            expected += a.get(i)*b.get(i);

        double found = a.dot(b);

        assertEquals(expected, found, UtilEjml.TEST_F64);
    }

    @Test void isVector() {
        assertTrue(new SimpleMatrix(1, 1).isVector());
        assertTrue(new SimpleMatrix(1, 10).isVector());
        assertTrue(new SimpleMatrix(10, 1).isVector());
        assertFalse(new SimpleMatrix(6, 5).isVector());
    }

    @Test void minus_matrix_matrix() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 2, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random_DDRM(3, 2, 0, 1, rand);
        SimpleMatrix c = a.minus(b);

        DMatrixRMaj c_dense = new DMatrixRMaj(3, 2);
        CommonOps_DDRM.subtract((DMatrixRMaj)a.mat, (DMatrixRMaj)b.mat, c_dense);

        EjmlUnitTests.assertEquals(c_dense, c.mat);
    }

    @Test void minus_matrix_scalar() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 2, 0, 1, rand);
        double b = 0.14;
        SimpleMatrix c = a.minus(b);

        DMatrixRMaj c_dense = new DMatrixRMaj(3, 2);
        CommonOps_DDRM.subtract((DMatrixRMaj)a.mat, b, c_dense);

        EjmlUnitTests.assertEquals(c_dense, c.mat);
    }

    @Test void minus_matrixComplex_scalar() {
        SimpleMatrix a = SimpleMatrix.random_ZDRM(5, 6, 0, 1, rand);
        SimpleMatrix found = a.minus(2.5);
        SimpleMatrix expected = a.minusComplex(2.5, 0.0);

        EjmlUnitTests.assertEquals(found.getZDRM(), expected.getZDRM(), UtilEjml.TEST_F64);
    }

    @Test void minusComplex() {
        var list = new ArrayList<SimpleMatrix>();
        list.add(SimpleMatrix.random_DDRM(3, 2, 0, 1, rand));
        list.add(SimpleMatrix.random_ZDRM(3, 2, 0, 1, rand));

        for (SimpleMatrix a : list) {
            double real = -0.14;
            double imag = 0.07;

            SimpleMatrix c = a.minusComplex(real, imag);
            assertFalse(c.getType().isReal());

            for (int i = 0; i < a.getNumRows(); i++) {
                for (int j = 0; j < a.getNumCols(); j++) {
                    double expectedReal = a.getReal(i, j) - real;
                    double expectedImag = a.getImag(i, j) - imag;

                    assertEquals(expectedReal, c.getReal(i, j), UtilEjml.TEST_F64);
                    assertEquals(expectedImag, c.getImag(i, j), UtilEjml.TEST_F64);
                }
            }
        }
    }

    @Test void plus_beta_real_real() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 2, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random_DDRM(3, 2, 0, 1, rand);
        SimpleMatrix c = a.plus(2.5, b);

        DMatrixRMaj c_dense = new DMatrixRMaj(3, 2);
        CommonOps_DDRM.add((DMatrixRMaj)a.mat, 2.5, (DMatrixRMaj)b.mat, c_dense);

        EjmlUnitTests.assertEquals(c_dense, c.mat);
    }

    @Test void plus_beta_complex_complex() {
        SimpleMatrix a = SimpleMatrix.random_ZDRM(5, 6, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random_ZDRM(5, 6, 0, 1, rand);
        SimpleMatrix found = a.plus(2.5, b);
        SimpleMatrix expected = a.plus(b.scaleComplex(2.5, 0.0));

        EjmlUnitTests.assertEquals(found.getZDRM(), expected.getZDRM(), UtilEjml.TEST_F64);
    }

    @Test void plus_beta_complex_real() {
        SimpleMatrix a = SimpleMatrix.random_ZDRM(5, 6, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random_DDRM(5, 6, 0, 1, rand);
        SimpleMatrix found = a.plus(2.5, b);
        SimpleMatrix expected = a.plus(b.scaleComplex(2.5, 0.0));

        EjmlUnitTests.assertEquals(found.getZDRM(), expected.getZDRM(), UtilEjml.TEST_F64);
    }

    @Test void plusComplex() {
        var list = new ArrayList<SimpleMatrix>();
        list.add(SimpleMatrix.random_DDRM(3, 2, 0, 1, rand));
        list.add(SimpleMatrix.random_ZDRM(3, 2, 0, 1, rand));

        for (SimpleMatrix a : list) {
            double real = -0.14;
            double imag = 0.07;

            SimpleMatrix c = a.plusComplex(real, imag);
            assertFalse(c.getType().isReal());

            for (int i = 0; i < a.getNumRows(); i++) {
                for (int j = 0; j < a.getNumCols(); j++) {
                    double expectedReal = a.getReal(i, j) + real;
                    double expectedImag = a.getImag(i, j) + imag;

                    assertEquals(expectedReal, c.getReal(i, j), UtilEjml.TEST_F64);
                    assertEquals(expectedImag, c.getImag(i, j), UtilEjml.TEST_F64);
                }
            }
        }
    }

    @Test void invert() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 3, 0, 1, rand);
        SimpleMatrix inv = a.invert();

        DMatrixRMaj d_inv = new DMatrixRMaj(3, 3);
        CommonOps_DDRM.invert((DMatrixRMaj)a.mat, d_inv);

        EjmlUnitTests.assertEquals(d_inv, inv.mat);
    }

    @Test void invert_NaN_INFINITY() {
        SimpleMatrix a = new SimpleMatrix(3, 3);
        try {
            a.fill(Double.NaN);
            a.invert();
            fail("Should have thrown an exception");
        } catch (RuntimeException ignore) {
        }

        try {
            a.fill(Double.POSITIVE_INFINITY);
            a.invert();
            fail("Should have thrown an exception");
        } catch (RuntimeException ignore) {
        }
    }

    @Test void pseudoInverse() {
        // first test it against a non-square zero matrix
        SimpleMatrix inv = new SimpleMatrix(3, 4).pseudoInverse();
        assertEquals(0, inv.normF(), UtilEjml.TEST_F64);

        // now try it against a more standard matrix
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 3, 0, 1, rand);
        inv = a.pseudoInverse();

        DMatrixRMaj d_inv = new DMatrixRMaj(3, 3);
        CommonOps_DDRM.invert((DMatrixRMaj)a.mat, d_inv);

        EjmlUnitTests.assertEquals(d_inv, inv.mat);
    }

    @Test void solve() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 3, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random_DDRM(3, 2, 0, 1, rand);
        SimpleMatrix c = a.solve(b);

        DMatrixRMaj c_dense = new DMatrixRMaj(3, 2);
        CommonOps_DDRM.solve((DMatrixRMaj)a.mat, (DMatrixRMaj)b.mat, c_dense);

        EjmlUnitTests.assertEquals(c_dense, c.mat);
    }

    @Test void solve_NaN_INFINITY() {
        SimpleMatrix a = new SimpleMatrix(3, 3);
        SimpleMatrix b = SimpleMatrix.random_DDRM(3, 2, 0, 1, rand);
        try {
            a.fill(Double.NaN);
            a.solve(b);
            fail("Should have thrown an exception");
        } catch (RuntimeException ignore) {
        }

        try {
            a.fill(Double.POSITIVE_INFINITY);
            a.solve(b);
            fail("Should have thrown an exception");
        } catch (RuntimeException ignore) {
        }
    }

    /**
     * See if it solves an over determined system correctly
     */
    @Test void solve_notsquare() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(6, 3, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random_DDRM(6, 2, 0, 1, rand);
        SimpleMatrix c = a.solve(b);

        DMatrixRMaj c_dense = new DMatrixRMaj(3, 2);
        CommonOps_DDRM.solve((DMatrixRMaj)a.mat, (DMatrixRMaj)b.mat, c_dense);

        EjmlUnitTests.assertEquals(c_dense, c.mat);
    }

    @Test void set_double() {
        SimpleMatrix a = new SimpleMatrix(3, 3);
        a.fill(16.0);

        DMatrixRMaj d = new DMatrixRMaj(3, 3);
        CommonOps_DDRM.fill(d, 16.0);

        EjmlUnitTests.assertEquals(d, a.mat);
    }

    @Test void zero() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 3, 0, 1, rand);
        a.zero();

        DMatrixRMaj d = new DMatrixRMaj(3, 3);

        EjmlUnitTests.assertEquals(d, a.mat);
    }

    @Test void normF() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 3, 0, 1, rand);

        double norm = a.normF();
        double dnorm = NormOps_DDRM.fastNormF((DMatrixRMaj)a.mat);

        assertEquals(dnorm, norm, 1e-10);
    }

    @Test void conditionP2() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 3, 0, 1, rand);

        double cond = NormOps_DDRM.conditionP2((DMatrixRMaj)a.getMatrix());
        double found = a.conditionP2();

        assertEquals(cond, found);
    }

    @Test void determinant() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 3, 0, 1, rand);

        double det = a.determinant();
        double ddet = CommonOps_DDRM.det((DMatrixRMaj)a.mat);

        assertEquals(ddet, det, 1e-10);
    }

    @Test void determinantComplex_real() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 3, 0, 1, rand);

        Complex_F64 det = a.determinantComplex();

        assertEquals(a.determinant(), det.real, UtilEjml.TEST_F64);
        assertEquals(0.0, det.imaginary, UtilEjml.TEST_F64);
    }

    @Test void determinantComplex_complex() {
        SimpleMatrix a = SimpleMatrix.random_ZDRM(3, 3, 0, 1, rand);

        Complex_F64 found = a.determinantComplex();
        Complex_F64 expected = CommonOps_ZDRM.det(a.getZDRM());

        assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
        assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
    }

    @Test void trace() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 3, 0, 1, rand);

        double trace = a.trace();
        double dtrace = CommonOps_DDRM.trace((DMatrixRMaj)a.mat);

        assertEquals(dtrace, trace, 1e-10);
    }

    @Test void reshape() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 3, 0, 1, rand);
        DMatrixRMaj b = a.mat.copy();

        a.reshape(2, 3);
        b.reshape(2, 3, false);

        EjmlUnitTests.assertEquals(b, a.mat);
    }

    @Test void set_element() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 3, 0, 1, rand);
        a.set(0, 1, 10.3);

        assertEquals(10.3, a.get(0, 1), UtilEjml.TEST_F64);
    }

    @Test void setRow_array() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 3, 0, 1, rand);
        a.setRow(2, 1, 2, 3);

        assertEquals(2, a.get(2, 1), 1e-6);
        assertEquals(3, a.get(2, 2), 1e-6);
    }

    @Test void setRow_SimpleMatrix_real_real() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 4, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random_DDRM(1, 4, 0, 1, rand);
        a.setRow(2, b);

        for (int i = 0; i < a.getNumCols(); i++) {
            assertEquals(b.get(i), a.get(2, i), UtilEjml.TEST_F64);
        }

        // Make sure that B being a row or column vector doesn't matter
        b = SimpleMatrix.random_DDRM(4, 1, 0, 1, rand);
        a.setRow(2, b);

        for (int i = 0; i < a.getNumCols(); i++) {
            assertEquals(b.get(i), a.get(2, i), UtilEjml.TEST_F64);
        }
    }

    /**
     * If adding a complex vector to a real matrix, the matrix should be converted into a complex matrix
     */
    @Test void setRow_SimpleMatrix_real_complex() {
        setRow_SimpleMatrix_real_complex(
                SimpleMatrix.random_DDRM(3, 4, 0, 1, rand),
                SimpleMatrix.random_ZDRM(1, 4, 0, 1, rand));
        setRow_SimpleMatrix_real_complex(
                SimpleMatrix.random_ZDRM(3, 4, 0, 1, rand),
                SimpleMatrix.random_DDRM(1, 4, 0, 1, rand));
    }

    void setRow_SimpleMatrix_real_complex( SimpleMatrix a, SimpleMatrix b ) {
        a.setRow(2, b);

        assertFalse(a.getType().isReal());

        for (int i = 0; i < a.getNumCols(); i++) {
            assertEquals(b.getReal(0, i), a.getReal(2, i), UtilEjml.TEST_F64);
            assertEquals(b.getImag(0, i), a.getImag(2, i), UtilEjml.TEST_F64);
        }
    }

    @Test void setColumn_array() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 3, 0, 1, rand);
        a.setColumn(2, 1, 2, 3);

        assertEquals(2, a.get(1, 2), 1e-6);
        assertEquals(3, a.get(2, 2), 1e-6);
    }

    @Test void setColumn_SimpleMatrix_real_real() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 4, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random_DDRM(1, 3, 0, 1, rand);
        a.setColumn(2, b);

        for (int i = 0; i < a.getNumRows(); i++) {
            assertEquals(b.get(i), a.get(i, 2), UtilEjml.TEST_F64);
        }

        // Make sure that B being a row or column vector doesn't matter
        b = SimpleMatrix.random_DDRM(3, 1, 0, 1, rand);
        a.setColumn(2, b);

        for (int i = 0; i < a.getNumRows(); i++) {
            assertEquals(b.get(i), a.get(i, 2), UtilEjml.TEST_F64);
        }
    }

    @Test void setColumn_SimpleMatrix_real_complex() {
        setColumn_SimpleMatrix_real_complex(
                SimpleMatrix.random_DDRM(3, 4, 0, 1, rand),
                SimpleMatrix.random_ZDRM(1, 3, 0, 1, rand));
        setColumn_SimpleMatrix_real_complex(
                SimpleMatrix.random_ZDRM(3, 4, 0, 1, rand),
                SimpleMatrix.random_DDRM(1, 3, 0, 1, rand));
    }

    void setColumn_SimpleMatrix_real_complex( SimpleMatrix a, SimpleMatrix b ) {
        a.setColumn(2, b);

        assertFalse(a.getType().isReal());

        for (int i = 0; i < a.getNumRows(); i++) {
            assertEquals(b.getReal(0, i), a.getReal(i, 2), UtilEjml.TEST_F64);
            assertEquals(b.getImag(0, i), a.getImag(i, 2), UtilEjml.TEST_F64);
        }
    }

    @Test void get_2d() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 3, 0, 1, rand);

        assertEquals(((DMatrixRMaj)a.mat).get(0, 1), a.get(0, 1), 1e-6);
    }

    @Test void get_1d() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 3, 0, 1, rand);

        assertEquals(((DMatrixRMaj)a.mat).get(3), a.get(3), 1e-6);
    }

    @Test void getIndex() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 3, 0, 1, rand);

        assertEquals(((DMatrixRMaj)a.mat).getIndex(0, 2), a.getIndex(0, 2), 1e-6);
    }

    @Test void toArray2() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 2, 0, 1, rand);

        double[][] array2 = a.toArray2();
        assertEquals(a.getNumRows(), array2.length);
        assertEquals(a.getNumCols(), array2[0].length);

        for (int i = 0; i < array2.length; i++) {
            for (int j = 0; j < array2[0].length; j++) {
                assertEquals(array2[i][j], a.get(i, j), 0);
            }
        }
    }

    @Test void copy() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 3, 0, 1, rand);
        SimpleMatrix b = a.copy();

        assertNotSame(a.mat, b.mat);
        EjmlUnitTests.assertEquals(b.mat, a.mat);
    }

    @Test void svd() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 4, 0, 1, rand);

        SimpleSVD<SimpleMatrix> svd = a.svd();

        SimpleMatrix U = svd.getU();
        SimpleMatrix W = svd.getW();
        SimpleMatrix V = svd.getV();

        SimpleMatrix a_found = U.mult(W).mult(V.transpose());

        EjmlUnitTests.assertEquals(a.mat, a_found.mat);
    }

    @Test void eig() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(4, 4, 0, 1, rand);

        SimpleEVD evd = a.eig();

        assertEquals(4, evd.getNumberOfEigenvalues());

        for (int i = 0; i < 4; i++) {
            Complex_F64 c = evd.getEigenvalue(i);
            assertNotNull(c);
            evd.getEigenVector(i);
        }
    }

    @Test void insertIntoThis() {
        SimpleMatrix A = new SimpleMatrix(6, 4);
        SimpleMatrix B = SimpleMatrix.random_DDRM(3, 2, 0, 1, rand);

        DMatrixRMaj A_ = A.getMatrix().copy();

        A.insertIntoThis(1, 2, B);

        CommonOps_DDRM.insert((DMatrixRMaj)B.getMatrix(), A_, 1, 2);

        EjmlUnitTests.assertEquals(A_, A.getMatrix());
    }

    @Test void combine() {
        SimpleMatrix A = new SimpleMatrix(6, 4);
        SimpleMatrix B = SimpleMatrix.random_DDRM(3, 4, 0, 1, rand);

        SimpleMatrix C = A.combine(2, 2, B);

        assertEquals(6, C.getNumRows());
        assertEquals(6, C.getNumCols());

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (i >= 2 && i < 5 && j >= 2 && j < 6) {
                    // check to see if B was overlayed
                    assertEquals(B.get(i - 2, j - 2), C.get(i, j));
                } else if (i >= 5 || j >= 4) {
                    // check zero padding
                    assertEquals(0, C.get(i, j));
                } else {
                    // see if the parts of A remain there
                    assertEquals(A.get(i, j), C.get(i, j));
                }
            }
        }
    }

    @Test void scale() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 2, 0, 1, rand);
        SimpleMatrix b = a.scale(1.5);

        for (int i = 0; i < a.getNumRows(); i++) {
            for (int j = 0; j < a.getNumCols(); j++) {
                assertEquals(a.get(i, j)*1.5, b.get(i, j), 1e-10);
            }
        }
    }

    @Test void scaleComplex() {
        var list = new ArrayList<SimpleMatrix>();
        list.add(SimpleMatrix.random_DDRM(3, 2, 0, 1, rand));
        list.add(SimpleMatrix.random_ZDRM(3, 2, 0, 1, rand));

        for (SimpleMatrix a : list) {
            var scalar = new Complex_F64(-0.14, 0.6);
            var tmp = new Complex_F64();

            SimpleMatrix c = a.scaleComplex(scalar.real, scalar.imaginary);
            assertFalse(c.getType().isReal());

            for (int i = 0; i < a.getNumRows(); i++) {
                for (int j = 0; j < a.getNumCols(); j++) {
                    a.get(i, j, tmp);
                    Complex_F64 expected = tmp.times(scalar);
                    assertEquals(expected.real, c.getReal(i, j), UtilEjml.TEST_F64);
                    assertEquals(expected.imaginary, c.getImag(i, j), UtilEjml.TEST_F64);
                }
            }
        }
    }

    @Test void div_scalar() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 2, 0, 1, rand);
        SimpleMatrix b = a.divide(1.5);

        for (int i = 0; i < a.getNumRows(); i++) {
            for (int j = 0; j < a.getNumCols(); j++) {
                assertEquals(a.get(i, j)/1.5, b.get(i, j), 1e-10);
            }
        }
    }

    @Test void elementSum() {
        SimpleMatrix a = new SimpleMatrix(7, 4);

        double expectedSum = 0;

        int index = 0;
        for (int i = 0; i < a.getNumRows(); i++) {
            for (int j = 0; j < a.getNumCols(); j++, index++) {
                expectedSum += index;
                a.set(i, j, index);
            }
        }

        assertEquals(expectedSum, a.elementSum(), UtilEjml.TEST_F64);
    }

    @Test void elementSumComplex_real() {
        var a = SimpleMatrix.random_DDRM(3, 2, 0, 1, rand);

        double expected = a.elementSum();
        Complex_F64 found = a.elementSumComplex();
        assertEquals(expected, found.real, UtilEjml.TEST_F64);
        assertEquals(0.0, found.imaginary, UtilEjml.TEST_F64);
    }

    @Test void elementSumComplex_complex() {
        var a = SimpleMatrix.random_ZDRM(3, 2, 0, 1, rand);

        double expectedReal = a.real().elementSum();
        double expectedImag = a.imaginary().elementSum();

        Complex_F64 found = a.elementSumComplex();
        assertEquals(expectedReal, found.real, UtilEjml.TEST_F64);
        assertEquals(expectedImag, found.imaginary, UtilEjml.TEST_F64);
    }

    @Test void elementMax() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(7, 5, 0, 1, rand);

        a.set(3, 4, -5);
        a.set(4, 4, 4);

        assertEquals(4, a.elementMax(), 0.0);
    }

    @Test void elementMin() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(7, 5, 4, 10, rand);

        a.set(3, 4, -2);
        a.set(4, 4, 0.5);

        assertEquals(-2, a.elementMin(), 0.0);
    }

    @Test void elementMaxAbs() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(7, 5, 0, 1, rand);

        a.set(3, 4, -5);
        a.set(4, 4, 4);

        assertEquals(5, a.elementMaxAbs(), 0.0);
    }

    @Test void elementMinAbs() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(7, 5, 4, 10, rand);

        a.set(3, 4, -2);
        a.set(4, 4, 0.5);

        assertEquals(0.5, a.elementMinAbs(), 0.0);
    }

    @Test void elementMult() {
        SimpleMatrix A = SimpleMatrix.random_DDRM(4, 5, -1, 1, rand);
        SimpleMatrix B = SimpleMatrix.random_DDRM(4, 5, -1, 1, rand);

        SimpleMatrix C = A.elementMult(B);

        for (int i = 0; i < A.getNumRows(); i++) {
            for (int j = 0; j < A.getNumCols(); j++) {
                double expected = A.get(i, j)*B.get(i, j);

                assertEquals(expected, C.get(i, j));
            }
        }
    }

    @Test void elementDiv() {
        SimpleMatrix A = SimpleMatrix.random_DDRM(4, 5, -1, 1, rand);
        SimpleMatrix B = SimpleMatrix.random_DDRM(4, 5, -1, 1, rand);

        SimpleMatrix C = A.elementDiv(B);

        for (int i = 0; i < A.getNumRows(); i++) {
            for (int j = 0; j < A.getNumCols(); j++) {
                double expected = A.get(i, j)/B.get(i, j);

                assertEquals(expected, C.get(i, j));
            }
        }
    }

    @Test void elementPower_m() {
        SimpleMatrix A = SimpleMatrix.random_DDRM(4, 5, 0, 1, rand);
        SimpleMatrix B = SimpleMatrix.random_DDRM(4, 5, 0, 1, rand);

        SimpleMatrix C = A.elementPower(B);

        for (int i = 0; i < A.getNumRows(); i++) {
            for (int j = 0; j < A.getNumCols(); j++) {
                double expected = Math.pow(A.get(i, j), B.get(i, j));

                assertEquals(expected, C.get(i, j));
            }
        }
    }

    @Test void elementPower_s() {
        SimpleMatrix A = SimpleMatrix.random_DDRM(4, 5, 0, 1, rand);
        double b = 1.1;

        SimpleMatrix C = A.elementPower(b);

        for (int i = 0; i < A.getNumRows(); i++) {
            for (int j = 0; j < A.getNumCols(); j++) {
                double expected = Math.pow(A.get(i, j), b);

                assertEquals(expected, C.get(i, j));
            }
        }
    }

    @Test void elementExp() {
        SimpleMatrix A = SimpleMatrix.random_DDRM(4, 5, 0, 1, rand);

        SimpleMatrix C = A.elementExp();

        for (int i = 0; i < A.getNumRows(); i++) {
            for (int j = 0; j < A.getNumCols(); j++) {
                double expected = Math.exp(A.get(i, j));

                assertEquals(expected, C.get(i, j));
            }
        }
    }

    @Test void elementLog() {
        SimpleMatrix A = SimpleMatrix.random_DDRM(4, 5, 0, 1, rand);

        SimpleMatrix C = A.elementLog();

        for (int i = 0; i < A.getNumRows(); i++) {
            for (int j = 0; j < A.getNumCols(); j++) {
                double expected = Math.log(A.get(i, j));

                assertEquals(expected, C.get(i, j));
            }
        }
    }

    @Test void elementOp_real() {
        SimpleMatrix A = SimpleMatrix.random_DDRM(4, 5, 0, 1, rand);

        SimpleMatrix C = A.elementOp(( row, col, value ) -> -value);

        assertEquals(A.getNumRows(), C.getNumRows());
        assertEquals(A.getNumCols(), C.getNumCols());

        for (int i = 0; i < A.getNumRows(); i++) {
            for (int j = 0; j < A.getNumCols(); j++) {
                double expected = -A.get(i, j);

                assertEquals(expected, C.get(i, j));
            }
        }
    }

    @Test void elementOp_complex() {
        // Should be able to handle real and complex matrices
        List<SimpleMatrix> inputs = new ArrayList<>();
        inputs.add(SimpleMatrix.random_ZDRM(4, 5, 0, 1, rand));
        inputs.add(SimpleMatrix.random_CDRM(4, 5, 0, 1, rand));
        inputs.add(SimpleMatrix.random_DDRM(4, 5, 0, 1, rand));

        for (SimpleMatrix A : inputs) {
            SimpleMatrix C = A.elementOp(( row, col, value ) -> {value.setTo(value.real + row, -col);});

            assertEquals(A.getNumRows(), C.getNumRows());
            assertEquals(A.getNumCols(), C.getNumCols());

            double tol = A.getType().getBits() == 32 ? UtilEjml.TEST_F32 : UtilEjml.TEST_F64;

            for (int i = 0; i < A.getNumRows(); i++) {
                for (int j = 0; j < A.getNumCols(); j++) {
                    assertEquals(A.getReal(i, j) + i, C.getReal(i, j), tol);
                    assertEquals(-j, C.getImaginary(i, j), tol);
                }
            }
        }
    }

    @Test void extractMatrix() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(7, 5, 0, 1, rand);

        SimpleMatrix b = a.extractMatrix(2, 5, 3, 5);

        for (int i = 2; i <= 4; i++) {
            for (int j = 3; j <= 4; j++) {
                double expected = a.get(i, j);
                double found = b.get(i - 2, j - 3);

                assertEquals(expected, found);
            }
        }
    }

    @Test void diag_extract() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 4, 0, 1, rand);

        DMatrixRMaj found = a.diag().getMatrix();
        DMatrixRMaj expected = new DMatrixRMaj(3, 1);

        CommonOps_DDRM.extractDiag((DMatrixRMaj)a.getMatrix(), expected);

        EjmlUnitTests.assertEquals(found, expected);
    }

    @Test void diag_vector() {
        SimpleMatrix a = SimpleMatrix.random_DDRM(3, 1, 0, 1, rand);

        DMatrixRMaj found = a.diag().getMatrix();
        SimpleMatrix expected = SimpleMatrix.diag(((DMatrixRMaj)a.getMatrix()).data);

        EjmlUnitTests.assertEquals(found, expected.getMatrix());
    }

    @Test void extractVector() {
        SimpleMatrix A = SimpleMatrix.random_DDRM(10, 7, 0, 1, rand);

        SimpleMatrix c = A.extractVector(false, 2);
        SimpleMatrix r = A.extractVector(true, 2);

        assertEquals(A.getNumCols(), r.getNumCols());
        assertEquals(1, r.getNumRows());
        assertEquals(A.getNumRows(), c.getNumRows());
        assertEquals(1, c.getNumCols());

        for (int i = 0; i < A.getNumCols(); i++) {
            assertEquals(A.get(2, i), r.get(i), 1e-10);
        }

        for (int i = 0; i < A.getNumRows(); i++) {
            assertEquals(A.get(i, 2), c.get(i), 1e-10);
        }
    }

    @Test void negative() {
        SimpleMatrix A = SimpleMatrix.random_DDRM(5, 7, -1, 1, rand);

        SimpleMatrix A_neg = A.negative();

        double value = A.plus(A_neg).normF();

        assertEquals(0, value, UtilEjml.TEST_F64);
    }

    @Test void conjugate_DDRM() {
        SimpleMatrix A = SimpleMatrix.random_DDRM(5, 7, -1, 1, rand);
        SimpleMatrix found = A.conjugate();

        assertNotEquals(A.mat, found.mat);
        assertTrue(A.isIdentical(found, 0.0));
    }

    @Test void conjugate_FDRM() {
        SimpleMatrix A = SimpleMatrix.random_FDRM(5, 7, -1, 1, rand);
        SimpleMatrix found = A.conjugate();

        assertNotEquals(A.mat, found.mat);
        assertTrue(A.isIdentical(found, 0.0));
    }

    @Test void conjugate_ZDRM() {
        SimpleMatrix A = SimpleMatrix.random_ZDRM(5, 7, -1, 1, rand);
        SimpleMatrix found = A.conjugate();

        assertNotEquals(A.mat, found.mat);
        assertTrue(MatrixFeatures_ZDRM.isIdentical(CommonOps_ZDRM.conjugate(A.getZDRM(), null), found.getZDRM(), 0.0));
    }

    @Test void conjugate_CDRM() {
        SimpleMatrix A = SimpleMatrix.random_CDRM(5, 7, -1, 1, rand);
        SimpleMatrix found = A.conjugate();

        assertNotEquals(A.mat, found.mat);
        assertTrue(MatrixFeatures_CDRM.isIdentical(CommonOps_CDRM.conjugate(A.getCDRM(), null), found.getCDRM(), 0.0f));
    }

    @Test void magnitude_DDRM() {
        SimpleMatrix A = SimpleMatrix.random_DDRM(5, 7, -1, 1, rand);
        SimpleMatrix found = A.magnitude();

        assertNotEquals(A.mat, found.mat);
        CommonOps_DDRM.abs(A.getDDRM());
        assertTrue(MatrixFeatures_DDRM.isIdentical(A.getDDRM(), found.getDDRM(), 0.0));
    }

    @Test void magnitude_FDRM() {
        SimpleMatrix A = SimpleMatrix.random_FDRM(5, 7, -1, 1, rand);
        SimpleMatrix found = A.magnitude();

        assertNotEquals(A.mat, found.mat);
        CommonOps_FDRM.abs(A.getFDRM());
        assertTrue(MatrixFeatures_FDRM.isIdentical(A.getFDRM(), found.getFDRM(), 0.0f));
    }

    @Test void magnitude_ZDRM() {
        SimpleMatrix A = SimpleMatrix.random_ZDRM(5, 7, -1, 1, rand);
        SimpleMatrix found = A.magnitude();

        assertNotEquals(A.mat, found.mat);
        assertTrue(MatrixFeatures_DDRM.isIdentical(CommonOps_ZDRM.magnitude(A.getZDRM(), null), found.getDDRM(), 0.0));
    }

    @Test void magnitude_CDRM() {
        SimpleMatrix A = SimpleMatrix.random_CDRM(5, 7, -1, 1, rand);
        SimpleMatrix found = A.magnitude();

        assertNotEquals(A.mat, found.mat);
        assertTrue(MatrixFeatures_FDRM.isIdentical(CommonOps_CDRM.magnitude(A.getCDRM(), null), found.getFDRM(), 0.0f));
    }

    @Test void isInBounds() {
        SimpleMatrix A = new SimpleMatrix(10, 15);

        assertTrue(A.isInBounds(0, 0));
        assertTrue(A.isInBounds(9, 0));
        assertTrue(A.isInBounds(0, 14));
        assertTrue(A.isInBounds(3, 3));

        assertFalse(A.isInBounds(-1, 0));
        assertFalse(A.isInBounds(0, -1));
        assertFalse(A.isInBounds(10, 0));
        assertFalse(A.isInBounds(0, 15));
        assertFalse(A.isInBounds(3, 1000));
    }

    @Test void equation() {
        SimpleMatrix A = SimpleMatrix.random_DDRM(5, 7, -1, 1, rand);
        SimpleMatrix B = SimpleMatrix.random_DDRM(5, 7, -1, 1, rand);

        SimpleMatrix orig = A.copy();

        // test each variable type
        A.equation("A = A + B", 24.5, "B");
        assertEquals(orig.get(0, 0) + 24.5, A.get(0, 0), UtilEjml.TEST_F64);

        // test implicit A =
        A.setTo(orig);
        A.equation("A + B", 24.5, "B");
        assertEquals(orig.get(0, 0) + 24.5, A.get(0, 0), UtilEjml.TEST_F64);

        A.equation("A(B,5) = 4", 4, "B");
        assertEquals(4, A.get(4, 5), UtilEjml.TEST_F64);

        A.setTo(orig);
        A.equation("A = A+B", B, "B");
        assertEquals(orig.get(0, 0) + B.get(0, 0), A.get(0, 0), UtilEjml.TEST_F64);

        A.setTo(orig);
        A.equation("A = A+B", B.getDDRM(), "B");
        assertEquals(orig.get(0, 0) + B.get(0, 0), A.get(0, 0), UtilEjml.TEST_F64);

        A.setTo(orig);
        A.equation("B = B+B", "B");
        assertEquals(orig.get(0, 0)*2, A.get(0, 0), UtilEjml.TEST_F64);

        // test implicit B
        A.setTo(orig);
        A.equation("B+B", "B");
        assertEquals(orig.get(0, 0)*2, A.get(0, 0), UtilEjml.TEST_F64);
    }

    /**
     * More detailed test is done in the CommonOps functin
     */
    @Test void concatColumns() {
        SimpleMatrix A = SimpleMatrix.random_DDRM(5, 7, -1, 1, rand);
        SimpleMatrix B0 = SimpleMatrix.random_DDRM(4, 3, -1, 1, rand);
        SimpleMatrix B1 = SimpleMatrix.random_DDRM(4, 3, -1, 1, rand);

        SimpleMatrix C = A.concatColumns(B0, B1);
        SimpleMatrix D = new SimpleMatrix(5, 13);
        D.insertIntoThis(0, 0, A);
        D.insertIntoThis(0, 7, B0);
        D.insertIntoThis(0, 10, B1);

        assertTrue(C.isIdentical(D, UtilEjml.TEST_F64));
    }

    @Test void concatRows() {
        SimpleMatrix A = SimpleMatrix.random_DDRM(5, 7, -1, 1, rand);
        SimpleMatrix B0 = SimpleMatrix.random_DDRM(4, 3, -1, 1, rand);
        SimpleMatrix B1 = SimpleMatrix.random_DDRM(4, 3, -1, 1, rand);

        SimpleMatrix C = A.concatRows(B0, B1);
        SimpleMatrix D = new SimpleMatrix(13, 7);
        D.insertIntoThis(0, 0, A);
        D.insertIntoThis(5, 0, B0);
        D.insertIntoThis(9, 0, B1);

        assertTrue(C.isIdentical(D, UtilEjml.TEST_F64));
    }

    @Test void rows() {
        SimpleMatrix A = SimpleMatrix.random_DDRM(5, 7, -1, 1, rand);
        SimpleMatrix B = A.rows(1, 3);

        for (int i = 1; i < 3; i++) {
            for (int j = 0; j < 7; j++) {
                assertEquals(A.get(i, j), B.get(i - 1, j), UtilEjml.TEST_F64);
            }
        }
    }

    @Test void cols() {
        SimpleMatrix A = SimpleMatrix.random_DDRM(5, 7, -1, 1, rand);
        SimpleMatrix B = A.rows(1, 3);

        for (int i = 1; i < 3; i++) {
            for (int j = 0; j < 7; j++) {
                assertEquals(A.get(i, j), B.get(i - 1, j), UtilEjml.TEST_F64);
            }
        }
    }

    @Test void serialization() {
        List<Matrix> matrixTypes = new ArrayList<>();
        matrixTypes.add(new DMatrixRMaj(2, 3));
        matrixTypes.add(new FMatrixRMaj(2, 3));
        matrixTypes.add(new ZMatrixRMaj(2, 3));
        matrixTypes.add(new CMatrixRMaj(2, 3));
        matrixTypes.add(new DMatrixSparseCSC(2, 3));
//        matrixTypes.add( new FMatrixSparseCSC(2,3));

        DMatrixRMaj template = RandomMatrices_DDRM.rectangle(2, 3, rand);

        for (Matrix m : matrixTypes) {
            m.setTo(ConvertMatrixType.convert(template, m.getType()));

            SimpleMatrix A = new SimpleMatrix(m);
            try {
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                ObjectOutputStream stream = new ObjectOutputStream(byteStream);
                stream.writeObject(A);
                stream.flush();
                stream.close();

                ByteArrayInputStream inputByte = new ByteArrayInputStream(byteStream.toByteArray());
                ObjectInputStream inputStream = new ObjectInputStream(inputByte);
                SimpleMatrix B = (SimpleMatrix)inputStream.readObject();

                assertTrue(A.isIdentical(B, 1e-4));
                assertNotNull(B.convertType);

                // If properly constructed this shouldn't fail
                B.mult(A.transpose());

                // UnsupportedOperation is considered an acceptable response
                try {
                    assertTrue(B.plus(0.1).isIdentical(A.plus(0.1), 1e-4));
                } catch (UnsupportedOperation | ConvertToDenseException ignore) {
                }

                byteStream.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                fail("Serialization failed");
            }
        }
    }

    /**
     * See if it correctly calls a specialized function that can handle different matrix types
     */
    @Test void mult_specialized() {
        DMatrixSparse A = RandomMatrices_DSCC.rectangle(2, 2, 3, rand);
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(2, 3, rand);

        SimpleMatrix sA = SimpleMatrix.wrap(A);
        SimpleMatrix sB = SimpleMatrix.wrap(B);

        OpsCheckSpecial ops = new OpsCheckSpecial();
        sA.ops = ops;

        sA.mult(sB);
        assertTrue(ops.specalized);
    }

    /**
     * See if it correctly calls a specialized function that can handle different matrix types
     */
    @Test void solve_specialized() {
        DMatrixSparse A = RandomMatrices_DSCC.rectangle(2, 2, 3, rand);
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(2, 2, rand);

        SimpleMatrix sA = SimpleMatrix.wrap(A);
        SimpleMatrix sB = SimpleMatrix.wrap(B);

        OpsCheckSpecial ops = new OpsCheckSpecial();
        sA.ops = ops;

        sA.solve(sB);
        assertTrue(ops.specalized);
    }

    @Test void real_DDRM() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(2, 4, rand);
        SimpleMatrix sA = SimpleMatrix.wrap(A);

        // The real portion of a real matrix is a copy
        SimpleMatrix found = sA.real();
        assertNotEquals(A, found.mat);
        assertTrue(MatrixFeatures_DDRM.isIdentical(A, found.getDDRM(), 0.0));
    }

    @Test void real_FDRM() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(2, 4, rand);
        SimpleMatrix sA = SimpleMatrix.wrap(A);

        // The real portion of a real matrix is a copy
        SimpleMatrix found = sA.real();
        assertNotEquals(A, found.mat);
        assertTrue(MatrixFeatures_FDRM.isIdentical(A, found.getFDRM(), 0.0f));
    }

    @Test void real_ZDRM() {
        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(5, 8, rand);
        SimpleMatrix sA = SimpleMatrix.wrap(A);

        // Compare against the procedural implementation
        SimpleMatrix found = sA.real();
        assertTrue(found.mat.getType().isReal());

        DMatrixRMaj expected = CommonOps_ZDRM.real(A, null);
        assertTrue(MatrixFeatures_DDRM.isIdentical(expected, found.getDDRM(), 0.0));
    }

    @Test void real_CDRM() {
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(5, 8, rand);
        SimpleMatrix sA = SimpleMatrix.wrap(A);

        // Compare against the procedural implementation
        SimpleMatrix found = sA.real();
        assertTrue(found.mat.getType().isReal());

        FMatrixRMaj expected = CommonOps_CDRM.real(A, null);
        assertTrue(MatrixFeatures_FDRM.isIdentical(expected, found.getFDRM(), 0.0f));
    }

    @Test void imaginary_DDRM() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(2, 4, rand);
        SimpleMatrix sA = SimpleMatrix.wrap(A);

        // The imaginary portion of a real matrix is a zeros matrix
        SimpleMatrix found = sA.imaginary();
        assertNotEquals(A, found.mat);
        assertTrue(MatrixFeatures_DDRM.isIdentical(A.createLike(), found.getDDRM(), 0.0));
    }

    @Test void imaginary_FDRM() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(2, 4, rand);
        SimpleMatrix sA = SimpleMatrix.wrap(A);

        // The imaginary portion of a real matrix is a zeros matrix
        SimpleMatrix found = sA.imaginary();
        assertNotEquals(A, found.mat);
        assertTrue(MatrixFeatures_FDRM.isIdentical(A.createLike(), found.getFDRM(), 0.0f));
    }

    @Test void imaginary_ZDRM() {
        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(5, 8, rand);
        SimpleMatrix sA = SimpleMatrix.wrap(A);

        // Compare against the procedural implementation
        SimpleMatrix found = sA.imaginary();
        assertTrue(found.mat.getType().isReal());

        DMatrixRMaj expected = CommonOps_ZDRM.imaginary(A, null);
        assertTrue(MatrixFeatures_DDRM.isIdentical(expected, found.getDDRM(), 0.0));
    }

    @Test void imaginary_CDRM() {
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(5, 8, rand);
        SimpleMatrix sA = SimpleMatrix.wrap(A);

        // Compare against the procedural implementation
        SimpleMatrix found = sA.imaginary();
        assertTrue(found.mat.getType().isReal());

        FMatrixRMaj expected = CommonOps_CDRM.imaginary(A, null);
        assertTrue(MatrixFeatures_FDRM.isIdentical(expected, found.getFDRM(), 0.0f));
    }

    /**
     * Helper used to test to see if a specialized function was called
     */
    public static class OpsCheckSpecial extends SimpleOperations_DSCC {
        public boolean specalized = false;

        @Override
        public void mult( DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj output ) {
            specalized = true;
        }

        @Override
        public void mult( DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC output ) {
            fail("Shouldn't have been called");
        }

        @Override
        public boolean solve( DMatrixSparseCSC A, DMatrixSparseCSC X, DMatrixSparseCSC B ) {
            fail("Shouldn't have been called");
            return true;
        }

        @Override
        public boolean solve( DMatrixSparseCSC A, DMatrixRMaj X, DMatrixRMaj B ) {
            specalized = true;
            return true;
        }
    }
}
