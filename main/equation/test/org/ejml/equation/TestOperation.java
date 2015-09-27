/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

package org.ejml.equation;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestOperation {

    Random rand = new Random(234);

    @Test
    public void divide_matrix_scalar() {
        Equation eq = new Equation();

        SimpleMatrix x = SimpleMatrix.random(5, 3, -1, 1, rand);
        SimpleMatrix b = SimpleMatrix.random(5, 3, -1, 1, rand);

        eq.alias(2.5, "A");
        eq.alias(b, "b");
        eq.alias(x, "x");

        eq.process("x=b/A");

        assertTrue(b.divide(2.5).isIdentical(x, 1e-8));
    }

    @Test
    public void divide_scalar_matrix() {
        Equation eq = new Equation();

        SimpleMatrix x = SimpleMatrix.random(5, 3, -1, 1, rand);
        SimpleMatrix b = SimpleMatrix.random(5, 3, -1, 1, rand);

        eq.alias(2.5, "A");
        eq.alias(b, "b");
        eq.alias(x, "x");

        eq.process("x=A/b");

        DenseMatrix64F tmp = new DenseMatrix64F(5,3);
        CommonOps.divide(2.5, b.getMatrix(), tmp);

        assertTrue(MatrixFeatures.isIdentical(tmp, x.getMatrix(), 1e-8));
    }

    @Test
    public void divide_int_int() {
        Equation eq = new Equation();

        eq.alias(4, "A");
        eq.alias(13, "b");
        eq.alias(-1, "x");

        eq.process("x=b/A");

        int found = eq.lookupInteger("x");

        assertEquals(13 / 4, found, 1e-8);
    }

    @Test
    public void divide_scalar_scalar() {
        Equation eq = new Equation();

        eq.alias(5, "A");
        eq.alias(4.2, "b");
        eq.alias(-1.0, "x");

        eq.process("x=b/A");

        double found = eq.lookupDouble("x");

        assertEquals(4.2 / 5.0, found, 1e-8);
    }

    @Test
    public void divide_matrix_matrix() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 5, -1, 1, rand);
        SimpleMatrix x = SimpleMatrix.random(5, 3, -1, 1, rand);
        SimpleMatrix b = SimpleMatrix.random(6, 3, -1, 1, rand);

        eq.alias(A, "A");
        eq.alias(b, "b");
        eq.alias(x, "x");

        eq.process("x=b/A");

        assertTrue(A.solve(b).isIdentical(x, 1e-8));
    }

    @Test
    public void ldivide_matrix_matrix() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 5, -1, 1, rand);
        SimpleMatrix x = SimpleMatrix.random(5, 3, -1, 1, rand);
        SimpleMatrix b = SimpleMatrix.random(6, 3, -1, 1, rand);

        eq.alias(A, "A");
        eq.alias(b, "b");
        eq.alias(x, "x");

        eq.process("x=A\\b");

        assertTrue(A.solve(b).isIdentical(x, 1e-8));
    }

    @Test
    public void multiply_matrix_scalar() {
        Equation eq = new Equation();

        SimpleMatrix x = SimpleMatrix.random(5, 3, -1, 1, rand);
        SimpleMatrix b = SimpleMatrix.random(5, 3, -1, 1, rand);

        eq.alias(2.5, "A");
        eq.alias(b, "b");
        eq.alias(x, "x");

        eq.process("x=b*A");
        assertTrue(b.scale(2.5).isIdentical(x, 1e-8));
        eq.process("x=A*b");
        assertTrue(b.scale(2.5).isIdentical(x, 1e-8));
    }

    @Test
    public void multiply_int_int() {
        Equation eq = new Equation();

        eq.alias(4, "A");
        eq.alias(13, "b");
        eq.alias(-1, "x");

        eq.process("x=b*A");

        int found = eq.lookupInteger("x");

        assertEquals(13 * 4, found, 1e-8);
    }

    @Test
    public void multiply_scalar_scalar() {
        Equation eq = new Equation();

        eq.alias(5, "A");
        eq.alias(4.2, "b");
        eq.alias(-1.0, "x");

        eq.process("x=b*A");

        double found = eq.lookupDouble("x");

        assertEquals(4.2 * 5.0, found, 1e-8);
    }

    @Test
    public void multiply_matrix_matrix() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 5, -1, 1, rand);
        SimpleMatrix x = SimpleMatrix.random(5, 3, -1, 1, rand);
        SimpleMatrix b = SimpleMatrix.random(6, 3, -1, 1, rand);

        eq.alias(A, "A");
        eq.alias(b, "b");
        eq.alias(x, "x");

        eq.process("b=A*x");

        assertTrue(A.mult(x).isIdentical(b, 1e-8));
    }

    @Test
    public void elementMult_matrix() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(6, 5, -1, 1, rand);
        SimpleMatrix b = SimpleMatrix.random(6, 5, -1, 1, rand);
        SimpleMatrix c = SimpleMatrix.random(6, 5, -1, 1, rand);

        eq.alias(a, "a", b, "b", c, "c");

        eq.process("c=a.*b");

        assertTrue(a.elementMult(b).isIdentical(c, 1e-8));
    }

    @Test
    public void elementDivide_matrix() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(6, 5, -1, 1, rand);
        SimpleMatrix b = SimpleMatrix.random(6, 5, -1, 1, rand);
        SimpleMatrix c = SimpleMatrix.random(6, 5, -1, 1, rand);

        eq.alias(a,"a",b,"b",c,"c");

        eq.process("c=a./b");

        assertTrue(a.elementDiv(b).isIdentical(c, 1e-8));
    }

    @Test
    public void elementPower_mm() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(6, 5, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random(6, 5, 0, 1, rand);
        SimpleMatrix c = SimpleMatrix.random(6, 5, 0, 1, rand);

        eq.alias(a,"a",b,"b",c,"c");

        eq.process("c=a.^b");

        assertTrue(a.elementPower(b).isIdentical(c, 1e-8));
    }

    @Test
    public void elementPower_ms() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(6, 5, 0, 1, rand);
        double b = 1.1;
        SimpleMatrix c = SimpleMatrix.random(6, 5, 0, 1, rand);

        eq.alias(a,"a",b,"b",c,"c");

        eq.process("c=a.^b");

        assertTrue(a.elementPower(b).isIdentical(c, 1e-8));
    }

    @Test
    public void elementPower_sm() {
        Equation eq = new Equation();

        double a = 1.1;
        SimpleMatrix b = SimpleMatrix.random(6, 5, 0, 1, rand);
        SimpleMatrix c = SimpleMatrix.random(6, 5, 0, 1, rand);

        eq.alias(a,"a",b,"b",c,"c");

        eq.process("c=a.^b");

        SimpleMatrix expected = new SimpleMatrix(6,5);
        CommonOps.elementPower(a, b.getMatrix(), expected.getMatrix());
        assertTrue(expected.isIdentical(c, 1e-8));
    }

    @Test
    public void elementPower_ss() {
        Equation eq = new Equation();

        double a = 1.1;
        double b = 0.7;

        eq.alias(a,"a",b,"b");

        eq.process("c=a.^b");

        double found = eq.lookupDouble("c");

        assertEquals(Math.pow(a, b), found, 1e-8);
    }

    @Test
    public void kron_matrix_matrix() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(2, 3, -1, 1, rand);
        SimpleMatrix b = SimpleMatrix.random(3, 2, -1, 1, rand);
        SimpleMatrix c = SimpleMatrix.random(6, 5, -1, 1, rand);

        eq.alias(a,"a",b,"b",c,"c");

        eq.process("c=kron(a,b)");

        assertTrue(a.kron(b).isIdentical(c, 1e-8));
    }

    @Test
    public void power_double_double() {
        Equation eq = new Equation();

        eq.alias(1.1,"a");
        eq.process("a=2.3^4.2");

        assertEquals(Math.pow(2.3, 4.2), eq.lookupDouble("a"), 1e-8);
    }

    @Test
    public void power_int_int() {
        Equation eq = new Equation();

        eq.alias(1.1,"a");
        eq.process("a=2^4");

        assertEquals(Math.pow(2, 4), eq.lookupDouble("a"), 1e-8);
    }

    @Test
    public void sqrt_int() {
        Equation eq = new Equation();

        eq.process("a=sqrt(5)");

        assertEquals(Math.sqrt(5),eq.lookupDouble("a"),1e-8);
    }

    @Test
    public void sqrt_double() {
        Equation eq = new Equation();

        eq.process("a=sqrt(5.7)");

        assertEquals(Math.sqrt(5.7), eq.lookupDouble("a"), 1e-8);
    }


    @Test
    public void atan2_scalar() {
        Equation eq = new Equation();

        eq.alias(1.1,"a");
        eq.process("a=atan2(1.1,0.5)");

        assertEquals(Math.atan2(1.1, 0.5), eq.lookupDouble("a"), 1e-8);
    }

    @Test
    public void neg_int() {
        Equation eq = new Equation();

        eq.alias(2,"a");
        eq.alias(3,"b");
        eq.process("a=-b");

        assertEquals(-3, eq.lookupInteger("a"));
    }

    @Test
    public void neg_scalar() {
        Equation eq = new Equation();

        eq.alias(2.1,"a");
        eq.alias(3.1,"b");
        eq.process("a=-b");

        assertEquals(-3.1, eq.lookupDouble("a"), 1e-8);
    }

    @Test
    public void neg_matrix() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(1, 1, -1, 1, rand);
        SimpleMatrix B = SimpleMatrix.random(5, 3, -1, 1, rand);

        eq.alias(A, "A");
        eq.alias(B, "B");

        eq.process("A=-B");

        for (int i = 0; i < A.getNumElements(); i++) {
            assertEquals(-A.get(i),B.get(i),1e-8);
        }
    }

    @Test
    public void sin() {
        Equation eq = new Equation();

        eq.alias(1.1,"a");
        eq.process("a=sin(2.1)");

        assertEquals(Math.sin(2.1), eq.lookupDouble("a"), 1e-8);
    }

    @Test
    public void cos() {
        Equation eq = new Equation();

        eq.alias(1.1,"a");
        eq.process("a=cos(2.1)");

        assertEquals(Math.cos(2.1), eq.lookupDouble("a"), 1e-8);
    }

    @Test
    public void atan() {
        Equation eq = new Equation();

        eq.alias(1.1,"a");
        eq.process("a=atan(2.1)");

        assertEquals(Math.atan(2.1), eq.lookupDouble("a"), 1e-8);
    }

    @Test
    public void exp_s() {
        Equation eq = new Equation();

        eq.alias(1.1,"a");
        eq.process("a=exp(2.1)");

        assertEquals(Math.exp(2.1), eq.lookupDouble("a"), 1e-8);
    }

    @Test
    public void exp_m() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(3,4,0,1,rand);
        SimpleMatrix b = SimpleMatrix.random(3,4,0,1,rand);

        eq.alias(a,"a",b,"b");
        eq.process("b=exp(a)");

        SimpleMatrix expected = a.elementExp();

        assertTrue(expected.isIdentical(b, 1e-8));
    }

    @Test
    public void log_s() {
        Equation eq = new Equation();

        eq.alias(1.1,"a");
        eq.process("a=log(2.1)");

        assertEquals(Math.log(2.1), eq.lookupDouble("a"), 1e-8);
    }

    @Test
    public void log_m() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(3,4,0,1,rand);
        SimpleMatrix b = SimpleMatrix.random(3,4,0,1,rand);

        eq.alias(a,"a",b,"b");
        eq.process("b=log(a)");

        SimpleMatrix expected = a.elementLog();

        assertTrue(expected.isIdentical(b, 1e-8));
    }

    @Test
    public void add_int_int() {
        Equation eq = new Equation();

        eq.alias(1,"a");
        eq.process("a=2 + 3");

        assertEquals(5, eq.lookupInteger("a"), 1e-8);
    }

    @Test
    public void add_scalar_scalar() {
        Equation eq = new Equation();

        eq.alias(1.2,"a");
        eq.process("a= 2.3 + 3");

        assertEquals(5.3, eq.lookupDouble("a"), 1e-8);
    }

    @Test
    public void add_matrix_matrix() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(3,4,-1,1,rand);
        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);
        SimpleMatrix c = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(a,"a",b,"b",c,"c");
        eq.process("a=b+c");

        assertTrue(b.plus(c).isIdentical(a, 1e-8));
    }

    @Test
    public void add_matrix_scalar() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(3,4,-1,1,rand);
        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(a,"a",b,"b");

        eq.process("a=b+2.2");
        assertTrue(b.plus(2.2).isIdentical(a, 1e-8));

        eq.process("a=2.2+b");
        assertTrue(b.plus(2.2).isIdentical(a, 1e-8));
    }

    @Test
    public void subtract_int_int() {
        Equation eq = new Equation();

        eq.alias(1, "a");
        eq.process("a=2 - 3");

        assertEquals(-1, eq.lookupInteger("a"), 1e-8);
    }

    @Test
    public void subtract_scalar_scalar() {
        Equation eq = new Equation();

        eq.alias(1.2, "a");
        eq.process("a= 2.3 - 3");

        assertEquals(2.3 - 3.0, eq.lookupDouble("a"), 1e-8);
    }

    @Test
    public void subtract_matrix_matrix() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(3,4,-1,1,rand);
        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);
        SimpleMatrix c = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(a, "a", b, "b", c, "c");
        eq.process("a=b-c");

        assertTrue(b.minus(c).isIdentical(a, 1e-8));
    }

    @Test
    public void subtract_matrix_scalar() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(3,4,-1,1,rand);
        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(a,"a",b,"b");

        eq.process("a=b-2.2");
        assertTrue(b.plus(-2.2).isIdentical(a, 1e-8));

        eq.process("a=2.2-b");

        DenseMatrix64F expected = new DenseMatrix64F(3,4);
        CommonOps.subtract(2.2, b.getMatrix(), expected);
        assertTrue(SimpleMatrix.wrap(expected).isIdentical(a, 1e-8));
    }

    @Test
    public void copy_matrix_matrix() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(3,4,-1,1,rand);
        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(a,"a",b,"b");
        eq.process("b=a");

        assertTrue(a.isIdentical(b, 1e-8));
    }

    @Test
    public void copy_double_matrix() {
        Equation eq = new Equation();

        DenseMatrix64F src = new DenseMatrix64F(1,1,true,2.5);
        eq.alias(1.2,"a");
        eq.alias(src,"b");

        eq.process("a=b");

        assertEquals(2.5, eq.lookupDouble("a"), 1e-8);

        // pass in a none 1x1 matrix
        eq.alias(new DenseMatrix64F(2, 1), "b");
        try {
            eq.process("a=b");
            fail("Exception should have been thrown");
        } catch( RuntimeException e ){}
    }

    @Test
    public void copy_int_int() {
        Equation eq = new Equation();

        eq.alias(2,"a");
        eq.alias(3, "b");

        eq.process("a=b");

        assertEquals(3, eq.lookupInteger("a"));
    }

    @Test
    public void copy_double_scalar() {
        Equation eq = new Equation();

        // int to double
        eq.alias(2.2,"a");
        eq.alias(3,"b");

        eq.process("a=b");
        assertEquals(3, eq.lookupDouble("a"), 1e-8);

        // double to double
        eq.alias(3.5, "c");
        eq.process("a=c");
        assertEquals(3.5, eq.lookupDouble("a"), 1e-8);
    }

    @Test
    public void copy_submatrix_matrix_case0() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(2,3,-1,1,rand);
        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(a,"a",b,"b");
        eq.process("b(1:2,1:3)=a");

        assertTrue(a.isIdentical(b.extractMatrix(1, 3, 1, 4), 1e-8));
    }

    @Test
    public void copy_submatrix_matrix_case1() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(2,3,-1,1,rand);
        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(a, "a", b, "b");
        eq.process("b(0 1,3 2 0)=a");

        int rows[] = new int[]{0,1};
        int cols[] = new int[]{3,2,0};

        checkSubMatrixArraysInsert(a, b, rows, cols);
    }

    @Test
    public void copy_submatrix_matrix_case2() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(3,2,-1,1,rand);
        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(a, "a", b, "b");
        eq.process("b(:,2:)=a");

        int rows[] = new int[]{0,1,2};
        int cols[] = new int[]{2,3};

        checkSubMatrixArraysInsert(a, b, rows, cols);
    }

    @Test
    public void copy_submatrix_matrix_case3() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(6,1,-1,1,rand);
        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(a, "a", b, "b");
        eq.process("b(2 3 4 5 6 7)=a");

        for (int i = 0; i < 6; i++) {
            assertEquals(b.get(i+2),a.get(i),1e-8);
        }
    }

    @Test
    public void copy_submatrix_matrix_case4() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(7,1,-1,1,rand);
        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(a, "a", b, "b");
        eq.process("b(2:8)=a");

        for (int i = 0; i < 7; i++) {
            assertEquals(b.get(i+2),a.get(i),1e-8);
        }
    }

    @Test
    public void copy_submatrix_matrix_case5() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(3*4-2,1,-1,1,rand);
        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(a, "a", b, "b");
        eq.process("b(2:)=a");

        for (int i = 0; i < a.getNumElements(); i++) {
            assertEquals(b.get(i+2),a.get(i),1e-8);
        }
    }

    @Test
    public void copy_submatrix_matrix_case6() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(3*4-2,1,-1,1,rand);
        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(a, "a", b, "b");
        eq.process("b(2 3:)=a");

        for (int i = 0; i < a.getNumElements(); i++) {
            assertEquals(b.get(i+2),a.get(i),1e-8);
        }
    }

    @Test
    public void copy_submatrix_scalar_case0() {
        Equation eq = new Equation();

        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(b, "b");
        eq.process("b(2,3)=4.5");
        eq.process("b(0,0)=3.5");

        assertEquals(3.5, b.get(0, 0), 1e-8);
        assertEquals(4.5, b.get(2, 3), 1e-8);
    }

    @Test
    public void copy_submatrix_scalar_case1() {
        Equation eq = new Equation();

        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(b, "b");
        eq.process("b(0:1,1:3)=4.5");

        int rows[] = new int[]{0,1};
        int cols[] = new int[]{1,2,3};

        checkSubMatrixArraysInsert(4.5, b, rows, cols);
    }

    @Test
    public void copy_submatrix_scalar_case2() {
        Equation eq = new Equation();

        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(b, "b");
        eq.process("b(:,2:)=4.5");

        int rows[] = new int[]{0, 1, 2};
        int cols[] = new int[]{2,3};

        checkSubMatrixArraysInsert(4.5,b,rows,cols);
    }

    @Test
    public void copy_submatrix_scalar_case3() {
        Equation eq = new Equation();

        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(b, "b");
        eq.process("b(1 0 3)=4.5");

        int indexes[] = new int[]{1,0,3};
        for (int i = 0; i < indexes.length; i++) {
            assertEquals(b.get(indexes[i]),4.5,1e-8);
        }
    }

    @Test
    public void copy_submatrix_scalar_case4() {
        Equation eq = new Equation();

        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(b, "b");
        eq.process("b(1:3)=4.5");

        int indexes[] = new int[]{1,2,3};
        for (int i = 0; i < indexes.length; i++) {
            assertEquals(b.get(indexes[i]),4.5,1e-8);
        }
    }

    @Test
    public void copy_submatrix_scalar_case5() {
        Equation eq = new Equation();

        SimpleMatrix b = SimpleMatrix.random(2,3,-1,1,rand);

        eq.alias(b, "b");
        eq.process("b(2 3:)=4.5");

        int indexes[] = new int[]{2,3,4,5};
        for (int i = 0; i < indexes.length; i++) {
            assertEquals(b.get(indexes[i]),4.5,1e-8);
        }
    }

    @Test
    public void extract_one_case0() {
        Equation eq = new Equation();

        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(b,"b");
        eq.process("c=b(1 2)");
        DenseMatrix64F found = eq.lookupMatrix("c");

        assertTrue(found.numRows == 1 && found.numCols == 2);
        assertEquals(b.get(1), found.get(0), 1e-8);
        assertEquals(b.get(2), found.get(1), 1e-8);
    }

    @Test
    public void extract_one_case1() {
        Equation eq = new Equation();

        SimpleMatrix b = SimpleMatrix.random(3, 4, -1, 1, rand);

        eq.alias(b, "b");
        eq.process("c=b(1:3)");
        DenseMatrix64F found = eq.lookupMatrix("c");

        assertTrue(found.numRows == 1 && found.numCols == 3);
        for (int i = 0; i < found.numCols; i++) {
            assertEquals(b.get(i+1), found.get(i), 1e-8);
        }
    }

    @Test
    public void extract_one_case2() {
        Equation eq = new Equation();

        SimpleMatrix b = SimpleMatrix.random(3, 4, -1, 1, rand);

        eq.alias(b, "b");
        eq.process("c=b(4:)");
        DenseMatrix64F found = eq.lookupMatrix("c");

        assertTrue(found.numRows == 1 && found.numCols == b.getNumElements()-4);
        for (int i = 0; i < found.numCols; i++) {
            assertEquals(b.get(i+4), found.get(i), 1e-8);
        }
    }

    @Test
    public void extract_one_case3() {
        Equation eq = new Equation();

        SimpleMatrix b = SimpleMatrix.random(3, 4, -1, 1, rand);

        eq.alias(b, "b");
        eq.process("c=b(:)");
        DenseMatrix64F found = eq.lookupMatrix("c");

        assertTrue(found.numRows == 1 && found.numCols == b.getNumElements());
        for (int i = 0; i < found.numCols; i++) {
            assertEquals(b.get(i), found.get(i), 1e-8);
        }
    }

    @Test
    public void extract_two_case0() {
        Equation eq = new Equation();

        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(b,"b");
        eq.process("c=b(1 2,1 0 2)");
        DenseMatrix64F found = eq.lookupMatrix("c");

        int rows[] = new int[]{1,2};
        int cols[] = new int[]{1,0,2};

        checkSubMatrixArraysExtract(b, found, rows, cols);
    }

    private void checkSubMatrixArraysExtract(SimpleMatrix src, DenseMatrix64F dst, int[] rows, int[] cols) {
        assertTrue(dst.numRows == rows.length && dst.numCols == cols.length);
        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < cols.length; j++) {
                assertEquals(src.get(rows[i],cols[j]), dst.get(i,j), 1e-8);
            }
        }
    }

    private void checkSubMatrixArraysInsert(SimpleMatrix src, SimpleMatrix dst, int[] rows, int[] cols) {
        assertTrue(src.numRows() == rows.length && src.numCols() == cols.length);
        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < cols.length; j++) {
                assertEquals(src.get(i,j), dst.get(rows[i],cols[j]), 1e-8);
            }
        }
    }

    private void checkSubMatrixArraysInsert(double src, SimpleMatrix dst, int[] rows, int[] cols) {
        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < cols.length; j++) {
                assertEquals(src, dst.get(rows[i],cols[j]), 1e-8);
            }
        }
    }

    @Test
    public void extract_two_case1() {
        Equation eq = new Equation();

        SimpleMatrix b = SimpleMatrix.random(3, 4, -1, 1, rand);

        eq.alias(b,"b");
        eq.process("c=b(1:2,2:3)");
        DenseMatrix64F found = eq.lookupMatrix("c");

        int rows[] = new int[]{1,2};
        int cols[] = new int[]{2,3};

        checkSubMatrixArraysExtract(b, found, rows, cols);
    }

    @Test
    public void extract_two_case2() {
        Equation eq = new Equation();

        SimpleMatrix b = SimpleMatrix.random(3, 4, -1, 1, rand);

        eq.alias(b, "b");
        eq.process("c=b(2:,1:)");
        DenseMatrix64F found = eq.lookupMatrix("c");

        int rows[] = new int[]{2};
        int cols[] = new int[]{1,2,3};

        checkSubMatrixArraysExtract(b, found, rows, cols);
    }

    @Test
    public void extract_two_case3() {
        Equation eq = new Equation();

        SimpleMatrix b = SimpleMatrix.random(3, 4, -1, 1, rand);

        eq.alias(b, "b");
        eq.process("c=b(:,:)");
        DenseMatrix64F found = eq.lookupMatrix("c");

        int rows[] = new int[]{0,1,2};
        int cols[] = new int[]{0,1,2,3};

        checkSubMatrixArraysExtract(b, found, rows, cols);
    }

    @Test
    public void extractScalar_one() {
        Equation eq = new Equation();

        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(b,"b");
        eq.process("c=b(3)");
        double found = eq.lookupDouble("c");

        assertEquals(b.get(3), found, 1e-8);
    }

    @Test
    public void extractScalar_two() {
        Equation eq = new Equation();

        SimpleMatrix b = SimpleMatrix.random(3, 4, -1, 1, rand);

        eq.alias(b,"b");
        eq.process("c=b(2,3)");
        double found = eq.lookupDouble("c");

        assertEquals(b.get(2,3), found, 1e-8);
    }

    @Test
    public void transpose_matrix() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(3,4,-1,1,rand);
        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(a,"a",b,"b");
        eq.process("b=a'");

        assertTrue(a.transpose().isIdentical(b, 1e-8));
    }

    @Test
    public void inv_matrix() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(3,3,-1,1,rand);
        SimpleMatrix b = SimpleMatrix.random(3,3,-1,1,rand);

        eq.alias(a,"a",b,"b");
        eq.process("b=inv(a)");

        assertTrue(a.invert().isIdentical(b, 1e-8));
    }

    @Test
    public void inv_scalar() {
        Equation eq = new Equation();

        eq.alias(2.2,"a",3.3,"b");
        eq.process("b=inv(a)");

        assertEquals(1.0 / 2.2, eq.lookupDouble("b"), 1e-8);
    }

    @Test
    public void pinv_matrix() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(4,3,-1,1,rand);
        SimpleMatrix b = SimpleMatrix.random(1,1,-1,1,rand);

        eq.alias(a,"a",b,"b");
        eq.process("b=pinv(a)");

        assertTrue(a.pseudoInverse().isIdentical(b, 1e-8));
    }

    @Test
    public void pinv_scalar() {
        Equation eq = new Equation();

        eq.alias(2.2,"a",3.3,"b");
        eq.process("b=pinv(a)");

        assertEquals(1.0 / 2.2, eq.lookupDouble("b"), 1e-8);
    }

    @Test
    public void rref_matrix() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(4,3,-1,1,rand);
        SimpleMatrix b = SimpleMatrix.random(1,1,-1,1,rand);

        eq.alias(a,"a",b,"b");
        eq.process("b=rref(a)");

        DenseMatrix64F expected = new DenseMatrix64F(4,3);
        CommonOps.rref(a.getMatrix(),-1,expected);

        assertTrue(MatrixFeatures.isIdentical(expected,b.getMatrix(),1e-8));
    }

    @Test
    public void rref_scalar() {
        Equation eq = new Equation();

        eq.process("a=rref(2.3)");
        assertEquals(1,eq.lookupDouble("a"),1e-8);

        eq.process("a=rref(0)");
        assertEquals(0,eq.lookupDouble("a"),1e-8);

        eq.process("a=rref(-1.2)");
        assertEquals(1,eq.lookupDouble("a"),1e-8);
    }

    @Test
    public void det_matrix() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(4,4,-1,1,rand);

        eq.alias(a,"a");
        eq.process("b=det(a)");

        assertEquals(a.determinant(),eq.lookupDouble("b"),1e-8);
    }

    @Test
    public void det_scalar() {
        Equation eq = new Equation();

        eq.process("b=det(5.6)");

        assertEquals(5.6, eq.lookupDouble("b"), 1e-8);
    }

    @Test
    public void trace_matrix() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(a,"a");
        eq.process("b=trace(a)");

        assertEquals(a.trace(), eq.lookupDouble("b"), 1e-8);
    }

    @Test
    public void normF_matrix() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(a,"a");
        eq.process("b=normF(a)");

        assertEquals(a.normF(), eq.lookupDouble("b"), 1e-8);
    }

    @Test
    public void normF_scalar() {
        Equation eq = new Equation();

        eq.process("b=normF(5.6)");

        assertEquals(5.6, eq.lookupDouble("b"), 1e-8);
    }

    @Test
    public void eye() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(a,"a");
        eq.process("a=eye(3)");

        assertTrue(SimpleMatrix.identity(3).isIdentical(a, 1e-8));
    }

    @Test
    public void abs_matrix() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 5, -1, 1, rand);
        SimpleMatrix B = SimpleMatrix.random(6, 5, -1, 1, rand);

        eq.alias(A, "A");
        eq.alias(B, "B");

        eq.process("B=abs(A)");

        for (int i = 0; i < A.numRows(); i++) {
            for (int j = 0; j < A.numCols(); j++) {
                assertTrue(B.get(i,j)==Math.abs(A.get(i,j)));
            }
        }
    }

    @Test
    public void abs_int() {
        Equation eq = new Equation();

        eq.alias(-4, "A");
        eq.alias(1, "B");

        eq.process("B=abs(A)");

        int found = eq.lookupInteger("B");
        assertEquals(4,found,1e-8);
    }

    @Test
    public void abs_scalar() {
        Equation eq = new Equation();

        eq.alias(-4.6, "A");
        eq.alias(1.1, "B");

        eq.process("B=abs(A)");

        double found = eq.lookupDouble("B");
        assertEquals(4.6,found,1e-8);
    }

    @Test
    public void max_matrix() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 5, -1, 1, rand);

        eq.alias(A, "A");
        eq.alias(1.0, "B");

        eq.process("B=max(A)");

        double found = eq.lookupDouble("B");
        double expected = CommonOps.elementMax(A.getMatrix());
        assertEquals(expected,found,1e-8);
    }

    @Test
    public void max_int() {
        Equation eq = new Equation();

        eq.alias(4, "A");
        eq.alias(1, "B");

        eq.process("B=max(A)");

        int found = eq.lookupInteger("B");
        assertEquals(4,found,1e-8);
    }

    @Test
    public void max_scalar() {
        Equation eq = new Equation();

        eq.alias(4.6, "A");
        eq.alias(1.1, "B");

        eq.process("B=max(A)");

        double found = eq.lookupDouble("B");
        assertEquals(4.6,found,1e-8);
    }

    @Test
    public void min_matrix() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 5, -1, 1, rand);

        eq.alias(A, "A");
        eq.alias(1.0, "B");

        eq.process("B=min(A)");

        double found = eq.lookupDouble("B");
        double expected = CommonOps.elementMin(A.getMatrix());
        assertEquals(expected,found,1e-8);
    }

    @Test
    public void min_int() {
        Equation eq = new Equation();

        eq.alias(4, "A");
        eq.alias(1, "B");

        eq.process("B=min(A)");

        int found = eq.lookupInteger("B");
        assertEquals(4,found,1e-8);
    }

    @Test
    public void min_scalar() {
        Equation eq = new Equation();

        eq.alias(4.6, "A");
        eq.alias(1.1, "B");

        eq.process("B=min(A)");

        double found = eq.lookupDouble("B");
        assertEquals(4.6,found,1e-8);
    }

    @Test
    public void zeros() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 8, -1, 1, rand);

        eq.alias(A, "A");

        eq.process("A=zeros(6,8)");

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                assertEquals(0,A.get(i,j),1e-8);
            }
        }
    }

    @Test
    public void ones() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 8, -1, 1, rand);

        eq.alias(A, "A");

        eq.process("A=ones(6,8)");

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                assertEquals(1,A.get(i,j),1e-8);
            }
        }
    }

    @Test
    public void diag_vector() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 6, -1, 1, rand);
        SimpleMatrix B = SimpleMatrix.random(6, 1, -1, 1, rand);


        eq.alias(A, "A");
        eq.alias(B, "B");

        eq.process("A=diag(B)");

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if( i == j )
                    assertEquals(B.get(i,0),A.get(i,j),1e-8);
                else
                    assertEquals(0,A.get(i,j),1e-8);
            }
        }
    }

    @Test
    public void diag_matrix() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 8, -1, 1, rand);
        SimpleMatrix B = SimpleMatrix.random(6, 1, -1, 1, rand);

        eq.alias(A, "A");
        eq.alias(B, "B");

        eq.process("B=diag(A)");

        assertEquals(6,B.numRows());
        assertEquals(1,B.numCols());

        for (int i = 0; i < 6; i++) {
            assertEquals(A.get(i,i),B.get(i,0),1e-8);
        }
    }

    @Test
    public void dot() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 1, -1, 1, rand);
        SimpleMatrix B = SimpleMatrix.random(6, 1, -1, 1, rand);

        eq.alias(A, "A");
        eq.alias(B, "B");
        eq.alias(1.0, "found");

        eq.process("found=dot(A,B)");

        double found = ((VariableDouble)eq.lookupVariable("found")).value;

        assertEquals(A.dot(B),found,1e-8);
    }

    @Test
    public void solve() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 5, -1, 1, rand);
        SimpleMatrix x = SimpleMatrix.random(5, 3, -1, 1, rand);
        SimpleMatrix b = SimpleMatrix.random(6, 3, -1, 1, rand);

        eq.alias(A, "A");
        eq.alias(b, "b");
        eq.alias(x, "x");

        eq.process("x=solve(A,b)");

        assertTrue(A.solve(b).isIdentical(x, 1e-8));
    }
}
