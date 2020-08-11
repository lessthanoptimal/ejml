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

package org.ejml.dense.row;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.Complex_F32;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.mult.MatrixMatrixMult_CDRM;
import org.ejml.dense.row.mult.TestMatrixMatrixMult_CDRM;
import org.ejml.ops.ComplexMath_F32;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestCommonOps_CDRM {

    Random rand = new Random(234);

    @Test
    public void identity_one() {
        CMatrixRMaj I = CommonOps_CDRM.identity(4);
        assertEquals(4,I.numRows);
        assertEquals(4,I.numCols);

        assertTrue(MatrixFeatures_CDRM.isIdentity(I, UtilEjml.TEST_F32));
    }

    @Test
    public void identity_two() {
        CMatrixRMaj I = CommonOps_CDRM.identity(4,5);
        assertEquals(4,I.numRows);
        assertEquals(5,I.numCols);

        assertTrue(MatrixFeatures_CDRM.isIdentity(I,UtilEjml.TEST_F32));

        I = CommonOps_CDRM.identity(5,4);
        assertEquals(5,I.numRows);
        assertEquals(4,I.numCols);

        assertTrue(MatrixFeatures_CDRM.isIdentity(I,UtilEjml.TEST_F32));
    }

    @Test
    public void diag() {
        CMatrixRMaj m = CommonOps_CDRM.diag(1,2,3,4,5,6);

        assertEquals(3,m.numRows);
        assertEquals(3,m.numCols);

        Complex_F32 a = new Complex_F32();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                m.get(i,j,a);

                if( i == j ) {
                    assertEquals(2*i+1,a.real,UtilEjml.TEST_F32);
                    assertEquals(2*i+2,a.imaginary,UtilEjml.TEST_F32);
                } else {
                    assertEquals(0,a.real,UtilEjml.TEST_F32);
                    assertEquals(0,a.imaginary,UtilEjml.TEST_F32);
                }
            }
        }
    }

    @Test
    public void extractDiag() {
        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(3,4, 0, 1, rand);

        for( int i = 0; i < 3; i++ ) {
            a.set(i,i,i+1,0.1f);
        }

        CMatrixRMaj v = new CMatrixRMaj(3,1);
        CommonOps_CDRM.extractDiag(a, v);

        for( int i = 0; i < 3; i++ ) {
            assertEquals( i+1 , v.getReal(i) , UtilEjml.TEST_F32 );
            assertEquals( 0.1f , v.getImag(i) , UtilEjml.TEST_F32 );
        }
    }

    @Test
    public void convert() {
        FMatrixRMaj input = RandomMatrices_FDRM.rectangle(5,7,-1,1,rand);
        CMatrixRMaj output = new CMatrixRMaj(5,7);

        Complex_F32 a = new Complex_F32();

        CommonOps_CDRM.convert(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                output.get(i,j,a);

                assertEquals(input.get(i,j),a.getReal(),UtilEjml.TEST_F32);
                assertEquals(0,a.getImaginary(),UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void stripReal() {
        CMatrixRMaj input = RandomMatrices_CDRM.rectangle(5,7,-1,1,rand);
        FMatrixRMaj output = new FMatrixRMaj(5,7);

        Complex_F32 a = new Complex_F32();

        CommonOps_CDRM.stripReal(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                input.get(i,j,a);

                assertEquals(a.getReal(),output.get(i,j),UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void stripImaginary() {
        CMatrixRMaj input = RandomMatrices_CDRM.rectangle(5,7,-1,1,rand);
        FMatrixRMaj output = new FMatrixRMaj(5,7);

        Complex_F32 a = new Complex_F32();

        CommonOps_CDRM.stripImaginary(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                input.get(i,j,a);

                assertEquals(a.getImaginary(),output.get(i,j),UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void magnitude() {
        CMatrixRMaj input = RandomMatrices_CDRM.rectangle(5,7,-1,1,rand);
        FMatrixRMaj output = new FMatrixRMaj(5,7);

        Complex_F32 a = new Complex_F32();

        CommonOps_CDRM.magnitude(input, output);

        for (int i = 0; i < input.numRows; i++) {
            for (int j = 0; j < input.numCols; j++) {
                input.get(i,j,a);

                assertEquals(a.getMagnitude(),output.get(i,j),UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void conjugate() {
        CMatrixRMaj matrix = RandomMatrices_CDRM.rectangle(5,7,-1,1,rand);
        CMatrixRMaj found = RandomMatrices_CDRM.rectangle(5,7,-1,1,rand);

        CommonOps_CDRM.conjugate(matrix,found);

        for (int i = 0; i < matrix.getDataLength(); i += 2) {
            float real = matrix.data[i];
            float img = matrix.data[i+1];

            assertEquals(real, found.data[i],UtilEjml.TEST_F32);
            assertEquals(img, -found.data[i+1],UtilEjml.TEST_F32);
        }
    }

    @Test
    public void fill() {
        CMatrixRMaj matrix = RandomMatrices_CDRM.rectangle(5,7,-1,1,rand);

        CommonOps_CDRM.fill(matrix,2,-1);

        for (int i = 0; i < matrix.getDataLength(); i += 2) {
            float real = matrix.data[i];
            float img = matrix.data[i+1];

            assertEquals(2,real,UtilEjml.TEST_F32);
            assertEquals(-1,img,UtilEjml.TEST_F32);
        }
    }

    @Test
    public void add() {
        CMatrixRMaj matrixA = RandomMatrices_CDRM.rectangle(5,7,-1,1,rand);
        CMatrixRMaj matrixB = RandomMatrices_CDRM.rectangle(5,7,-1,1,rand);
        CMatrixRMaj out = RandomMatrices_CDRM.rectangle(5,7,-1,1,rand);

        Complex_F32 a = new Complex_F32();
        Complex_F32 b = new Complex_F32();
        Complex_F32 found = new Complex_F32();
        Complex_F32 expected = new Complex_F32();

        CommonOps_CDRM.add(matrixA, matrixB, out);

        for (int i = 0; i < matrixA.numRows; i++) {
            for (int j = 0; j < matrixA.numCols; j++) {
                matrixA.get(i,j,a);
                matrixB.get(i,j,b);
                out.get(i,j,found);

                ComplexMath_F32.plus(a, b, expected);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F32);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void subtract() {
        CMatrixRMaj matrixA = RandomMatrices_CDRM.rectangle(5,7,-1,1,rand);
        CMatrixRMaj matrixB = RandomMatrices_CDRM.rectangle(5,7,-1,1,rand);
        CMatrixRMaj out = RandomMatrices_CDRM.rectangle(5,7,-1,1,rand);

        Complex_F32 a = new Complex_F32();
        Complex_F32 b = new Complex_F32();
        Complex_F32 found = new Complex_F32();
        Complex_F32 expected = new Complex_F32();

        CommonOps_CDRM.subtract(matrixA, matrixB, out);

        for (int i = 0; i < matrixA.numRows; i++) {
            for (int j = 0; j < matrixA.numCols; j++) {
                matrixA.get(i,j,a);
                matrixB.get(i,j,b);
                out.get(i,j,found);

                ComplexMath_F32.minus(a, b, expected);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F32);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void scale() {
        Complex_F32 scale = new Complex_F32(2.5f,0.4f);

        CMatrixRMaj mat = RandomMatrices_CDRM.rectangle(5,7,-1,1,rand);
        CMatrixRMaj orig = mat.copy();

        CommonOps_CDRM.scale(scale.real, scale.imaginary, mat);

        Complex_F32 value = new Complex_F32();
        Complex_F32 expected = new Complex_F32();
        for (int i = 0; i < mat.numRows; i++) {
            for (int j = 0; j < mat.numCols; j++) {
//                System.out.println("i "+i+" j "+j);
                orig.get(i,j,value);

                ComplexMath_F32.multiply(scale,value,expected);
                assertEquals(expected.real, mat.getReal(i,j), UtilEjml.TEST_F32);
                assertEquals(expected.imaginary, mat.getImag(i,j), UtilEjml.TEST_F32);
            }
        }
    }

    /**
     * Make sure the multiplication methods here have the same behavior as the ones in MatrixMatrixMult.
     */
    @Test
    public void checkAllMatrixMult() {
        int numChecked = 0;
        Method methods[] = CommonOps_CDRM.class.getMethods();

        for (Method method : methods) {
            String name = method.getName();

            if( !name.startsWith("mult"))
                continue;

            //            System.out.println(name);

            Class[] params = method.getParameterTypes();

            boolean add = name.contains("Add");
            boolean hasAlpha = float.class == params[0];
            boolean transA = name.contains("TransA");
            boolean transB = name.contains("TransB");
            if( name.contains("TransAB") )
                transA = transB = true;

            try {
                TestMatrixMatrixMult_CDRM.check(method, add, hasAlpha, transA, transB);
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
                CMatrixRMaj A = RandomMatrices_CDRM.rectangle(i,j,-1,1,rand);
                for (int k = 1; k < 10; k++) {
                    CMatrixRMaj B = RandomMatrices_CDRM.rectangle(j, k, -1, 1, rand);
                    CMatrixRMaj found = RandomMatrices_CDRM.rectangle(i, k, -1, 1, rand);
                    CMatrixRMaj expected = TestMatrixMatrixMult_CDRM.multiply(A, B, false, false);

                    MatrixMatrixMult_CDRM.mult_reorder(A, B, found);

                    assertTrue(MatrixFeatures_CDRM.isEquals(expected, found, UtilEjml.TEST_F32),i+" "+j+" "+k);
                }
            }
        }
    }

    @Test
    public void transpose_one() {

        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(4,4,-1,1,rand);
        CMatrixRMaj b = a.copy();

        CommonOps_CDRM.transpose(b);

        Complex_F32 found = new Complex_F32();
        Complex_F32 expected = new Complex_F32();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F32);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void transposeConjugate_one() {

        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(4,4,-1,1,rand);
        CMatrixRMaj b = a.copy();

        CommonOps_CDRM.transposeConjugate(b);

        Complex_F32 found = new Complex_F32();
        Complex_F32 expected = new Complex_F32();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F32);
                assertEquals(-expected.imaginary,found.imaginary,UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void transpose_two() {

        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(4,5,-1,1,rand);
        CMatrixRMaj b = RandomMatrices_CDRM.rectangle(5,4,-1,1,rand);

        CommonOps_CDRM.transpose(a, b);

        Complex_F32 found = new Complex_F32();
        Complex_F32 expected = new Complex_F32();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F32);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void transposeConjugate_two() {

        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(4,5,-1,1,rand);
        CMatrixRMaj b = RandomMatrices_CDRM.rectangle(5,4,-1,1,rand);

        CommonOps_CDRM.transposeConjugate(a, b);

        Complex_F32 found = new Complex_F32();
        Complex_F32 expected = new Complex_F32();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F32);
                assertEquals(-expected.imaginary,found.imaginary,UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void invert_1() {
        for (int i = 1; i < 10; i++) {
            CMatrixRMaj A = RandomMatrices_CDRM.rectangle(i,i,rand);
            CMatrixRMaj A_orig = A.copy();

            CMatrixRMaj I = RandomMatrices_CDRM.rectangle(i,i,rand);

            assertTrue(CommonOps_CDRM.invert(A));
            CommonOps_CDRM.mult(A_orig,A,I);

            assertTrue(MatrixFeatures_CDRM.isIdentity(I, UtilEjml.TEST_F32));
        }
    }

    @Test
    public void invert_2() {
        for (int i = 1; i < 10; i++) {
            CMatrixRMaj A = RandomMatrices_CDRM.rectangle(i, i, rand);
            CMatrixRMaj A_orig = A.copy();
            CMatrixRMaj A_inv = new CMatrixRMaj(i, i);

            CMatrixRMaj I = RandomMatrices_CDRM.rectangle(i, i, rand);

            assertTrue(CommonOps_CDRM.invert(A, A_inv));
            CommonOps_CDRM.mult(A, A_inv, I);

            assertTrue(MatrixFeatures_CDRM.isIdentity(I, UtilEjml.TEST_F32));
            assertTrue(MatrixFeatures_CDRM.isIdentical(A, A_orig, 0));
        }
    }

    @Test
    public void solve() {
        // square
        for (int i = 1; i < 10; i++) {
            CMatrixRMaj A = RandomMatrices_CDRM.rectangle(i, i, rand);
            CMatrixRMaj B = RandomMatrices_CDRM.rectangle(i, 1, rand);

            CMatrixRMaj A_orig = A.copy();
            CMatrixRMaj B_orig = B.copy();

            CMatrixRMaj X = new CMatrixRMaj(i, 1);

            assertTrue(CommonOps_CDRM.solve(A, B, X));

            CMatrixRMaj found = new CMatrixRMaj(i, 1);

            CommonOps_CDRM.mult(A, X, found);

            assertTrue(MatrixFeatures_CDRM.isIdentical(B, found, UtilEjml.TEST_F32));

            assertTrue(MatrixFeatures_CDRM.isIdentical(A, A_orig, 0));
            assertTrue(MatrixFeatures_CDRM.isIdentical(B, B_orig, 0));
        }

        // rectangular
        for (int i = 1; i < 10; i++) {
            CMatrixRMaj A = RandomMatrices_CDRM.rectangle(2*i, i, rand);
            CMatrixRMaj X = RandomMatrices_CDRM.rectangle(i, 1, rand);
            CMatrixRMaj B = new CMatrixRMaj(2*i,1);

            CommonOps_CDRM.mult(A,X,B);

            CMatrixRMaj A_orig = A.copy();
            CMatrixRMaj B_orig = B.copy();
            CMatrixRMaj X_expected = X.copy();

            assertTrue(CommonOps_CDRM.solve(A, B, X));

            assertTrue(MatrixFeatures_CDRM.isIdentical(X, X_expected, UtilEjml.TEST_F32));

            assertTrue(MatrixFeatures_CDRM.isIdentical(B, B_orig, 0));
            assertTrue(MatrixFeatures_CDRM.isIdentical(A, A_orig, 0));
        }
    }

    @Test
    public void det() {
        CMatrixRMaj A = new CMatrixRMaj(3,3,true,
                0.854634f , 0.445620f,  0.082836f , 0.212460f , 0.623783f , 0.037631f,
                0.585408f , 0.768956f , 0.771067f , 0.897763f , 0.125793f , 0.432187f,
                0.303789f , 0.044497f , 0.151182f , 0.034471f , 0.526770f , 0.570333f);

        CMatrixRMaj A_orig = A.copy();

        Complex_F32 found = CommonOps_CDRM.det(A);
        // from octave
        Complex_F32 expected = new Complex_F32(-0.40548f , 0.54188f);

        assertEquals(expected.real,found.real,1e-3);
        assertEquals(expected.imaginary,found.imaginary,1e-3);

        assertTrue(MatrixFeatures_CDRM.isIdentical(A,A_orig,0));
    }

    @Test
    public void elementMultiply() {
        CMatrixRMaj in = RandomMatrices_CDRM.rectangle(5,7,-1,1,rand);
        CMatrixRMaj out = RandomMatrices_CDRM.rectangle(5,7,-1,1,rand);

        Complex_F32 a = new Complex_F32(1.2f,-0.3f);
        Complex_F32 b = new Complex_F32();
        Complex_F32 found = new Complex_F32();
        Complex_F32 expected = new Complex_F32();

        CommonOps_CDRM.elementMultiply(in,a.real,a.imaginary,out);

        for (int i = 0; i < in.numRows; i++) {
            for (int j = 0; j < in.numCols; j++) {
                in.get(i,j,b);
                out.get(i,j,found);

                ComplexMath_F32.multiply(a,b,expected);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F32);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void elementDivide_right() {
        CMatrixRMaj in = RandomMatrices_CDRM.rectangle(5,7,-1,1,rand);
        CMatrixRMaj out = RandomMatrices_CDRM.rectangle(5,7,-1,1,rand);

        Complex_F32 a = new Complex_F32();
        Complex_F32 b = new Complex_F32(1.2f,-0.3f);
        Complex_F32 found = new Complex_F32();
        Complex_F32 expected = new Complex_F32();

        CommonOps_CDRM.elementDivide(in,b.real,b.imaginary,out);

        for (int i = 0; i < in.numRows; i++) {
            for (int j = 0; j < in.numCols; j++) {
                in.get(i,j,a);
                out.get(i,j,found);

                ComplexMath_F32.divide(a,b,expected);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F32);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void elementDivide_left() {
        CMatrixRMaj in = RandomMatrices_CDRM.rectangle(5,7,-1,1,rand);
        CMatrixRMaj out = RandomMatrices_CDRM.rectangle(5,7,-1,1,rand);

        Complex_F32 a = new Complex_F32(1.2f,-0.3f);
        Complex_F32 b = new Complex_F32();
        Complex_F32 found = new Complex_F32();
        Complex_F32 expected = new Complex_F32();

        CommonOps_CDRM.elementDivide(a.real,a.imaginary,in,out);

        for (int i = 0; i < in.numRows; i++) {
            for (int j = 0; j < in.numCols; j++) {
                in.get(i,j,b);
                out.get(i,j,found);

                ComplexMath_F32.divide(a,b,expected);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F32);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void elementMinReal() {
        CMatrixRMaj m = new CMatrixRMaj(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-6, CommonOps_CDRM.elementMinReal(m),UtilEjml.TEST_F32);
    }

    @Test
    public void elementMinImaginary() {
        CMatrixRMaj m = new CMatrixRMaj(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-5, CommonOps_CDRM.elementMinImaginary(m), UtilEjml.TEST_F32);
    }

    @Test
    public void elementMaxReal() {
        CMatrixRMaj m = new CMatrixRMaj(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-6 + 11 * 2, CommonOps_CDRM.elementMaxReal(m), UtilEjml.TEST_F32);
    }

    @Test
    public void elementMaxImaginary() {
        CMatrixRMaj m = new CMatrixRMaj(3,4);
        for (int i = 0; i < m.data.length; i++) {
            m.data[i] = -6 + i;
        }

        assertEquals(-5 + 11 * 2, CommonOps_CDRM.elementMaxImaginary(m), UtilEjml.TEST_F32);
    }

    @Test
    public void elementMaxMagnitude2() {
        CMatrixRMaj m = RandomMatrices_CDRM.rectangle(4,5,-2,2,rand);
        FMatrixRMaj a = new FMatrixRMaj(m.numRows,m.numCols);

        CommonOps_CDRM.magnitude(m,a);

        float expected = CommonOps_FDRM.elementMaxAbs(a);
        expected *= expected;

        float found = CommonOps_CDRM.elementMaxMagnitude2(m);

        assertEquals(expected,found,UtilEjml.TEST_F32);
    }

    @Test
    public void setIdentity() {
        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(4,5,-2,2,rand);

        CommonOps_CDRM.setIdentity(a);

        Complex_F32 c = new Complex_F32();
        for (int i = 0; i < a.numRows; i++) {
            for (int j = 0; j < a.numCols; j++) {
                a.get(i,j,c);
                if( i == j ) {
                    assertEquals(1,c.real,UtilEjml.TEST_F32);
                    assertEquals(0,c.imaginary,UtilEjml.TEST_F32);
                } else {
                    assertEquals(0,c.real,UtilEjml.TEST_F32);
                    assertEquals(0,c.imaginary,UtilEjml.TEST_F32);
                }
            }
        }
    }

    @Test
    public void extract_simplified() {
        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(10,12,-2,2,rand);
        CMatrixRMaj b = CommonOps_CDRM.extract(a,2,5,3,8);

        Complex_F32 ca = new Complex_F32();
        Complex_F32 cb = new Complex_F32();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(2+i,j+3,ca);
                b.get(  i,j  , cb);

                assertEquals(ca.real,cb.real,UtilEjml.TEST_F32);
                assertEquals(ca.imaginary,cb.imaginary,UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void extract_complex() {
        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(10,12,-2,2,rand);
        CMatrixRMaj b = new CMatrixRMaj(6,7);

        Complex_F32 ca = new Complex_F32();
        Complex_F32 cb = new Complex_F32();

        CommonOps_CDRM.extract(a,2,5,3,7,b,1,2);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(2+i,j+3,ca);
                b.get(1 + i, j + 2, cb);

                assertEquals(ca.real,cb.real,UtilEjml.TEST_F32);
                assertEquals(ca.imaginary,cb.imaginary,UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void columnsToVector() {
        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(10,12,-2,2,rand);
        CMatrixRMaj v[] = CommonOps_CDRM.columnsToVector(a,null);

        Complex_F32 ca = new Complex_F32();
        Complex_F32 cc = new Complex_F32();

        for (int i = 0; i < a.numCols; i++) {
            CMatrixRMaj c = v[i];

            assertEquals(c.numRows,a.numRows);
            assertEquals(1,c.numCols);

            for (int j = 0; j < a.numRows; j++) {
                a.get(j,i,ca);
                c.get(j,0,cc);

                EjmlUnitTests.assertEquals(ca,cc,UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void elementMaxAbs() {
        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(10,12,-2,2,rand);
        a.set(5,6,10,12);

        float expected = (float)Math.sqrt(10*10 + 12*12);
        float found = CommonOps_CDRM.elementMaxAbs(a);
        assertEquals(expected,found,UtilEjml.TEST_F32);
    }

    @Test
    public void elementMinAbs() {
        CMatrixRMaj a = RandomMatrices_CDRM.rectangle(10,12,-8,-2,rand);
        a.set(5,6,1,2);

        float expected = (float)Math.sqrt(1 + 2*2);
        float found = CommonOps_CDRM.elementMinAbs(a);
        assertEquals(expected,found,UtilEjml.TEST_F32);
    }
}