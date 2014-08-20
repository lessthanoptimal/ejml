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

package org.ejml.equation;

import org.ejml.ops.CommonOps;
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
    public void divide_int_int() {
        Equation eq = new Equation();

        eq.alias(4, "A");
        eq.alias(13, "b");
        eq.alias(-1, "x");

        eq.process("x=b/A");

        int found = eq.lookupInteger("x");

        assertEquals(13/4,found,1e-8);
    }

    @Test
    public void divide_scalar_scalar() {
        Equation eq = new Equation();

        eq.alias(5, "A");
        eq.alias(4.2, "b");
        eq.alias(-1.0, "x");

        eq.process("x=b/A");

        double found = eq.lookupScalar("x");

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
        assertTrue(b.scale(2.5).isIdentical(x,1e-8));
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

        double found = eq.lookupScalar("x");

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

        assertTrue(A.mult(x).isIdentical(b,1e-8));
    }

    @Test
    public void power_double_double() {
        Equation eq = new Equation();

        eq.alias(1.1,"a");
        eq.process("a=2.3^4.2");

        assertEquals(Math.pow(2.3,4.2),eq.lookupScalar("a"),1e-8);
    }

    @Test
    public void power_int_int() {
        Equation eq = new Equation();

        eq.alias(1.1,"a");
        eq.process("a=2^4");

        assertEquals(Math.pow(2,4),eq.lookupScalar("a"),1e-8);
    }

    @Test
    public void atan2_scalar() {
        Equation eq = new Equation();

        eq.alias(1.1,"a");
        eq.process("a=atan2(1.1,0.5)");

        assertEquals(Math.atan2(1.1, 0.5), eq.lookupScalar("a"), 1e-8);
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

        assertEquals(-3.1, eq.lookupScalar("a"), 1e-8);
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

        assertEquals(Math.sin(2.1),eq.lookupScalar("a"),1e-8);
    }

    @Test
    public void cos() {
        Equation eq = new Equation();

        eq.alias(1.1,"a");
        eq.process("a=cos(2.1)");

        assertEquals(Math.cos(2.1),eq.lookupScalar("a"),1e-8);
    }

    @Test
    public void atan() {
        Equation eq = new Equation();

        eq.alias(1.1,"a");
        eq.process("a=atan(2.1)");

        assertEquals(Math.atan(2.1), eq.lookupScalar("a"), 1e-8);
    }

    @Test
    public void exp() {
        Equation eq = new Equation();

        eq.alias(1.1,"a");
        eq.process("a=exp(2.1)");

        assertEquals(Math.exp(2.1),eq.lookupScalar("a"),1e-8);
    }

    @Test
    public void log() {
        Equation eq = new Equation();

        eq.alias(1.1,"a");
        eq.process("a=log(2.1)");

        assertEquals(Math.log(2.1),eq.lookupScalar("a"),1e-8);
    }

    @Test
    public void add_int_int() {
        Equation eq = new Equation();

        eq.alias(1,"a");
        eq.process("a=2 + 3");

        assertEquals(5,eq.lookupInteger("a"),1e-8);
    }

    @Test
    public void add_scalar_scalar() {
        Equation eq = new Equation();

        eq.alias(1.2,"a");
        eq.process("a= 2.3 + 3");

        assertEquals(5.3, eq.lookupScalar("a"), 1e-8);
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
        assertTrue(b.plus(2.2).isIdentical(a,1e-8));

        eq.process("a=2.2+b");
        assertTrue(b.plus(2.2).isIdentical(a, 1e-8));
    }

    @Test
    public void subtract_int_int() {
        Equation eq = new Equation();

        eq.alias(1,"a");
        eq.process("a=2 - 3");

        assertEquals(-1,eq.lookupInteger("a"),1e-8);
    }

    @Test
    public void subtract_scalar_scalar() {
        Equation eq = new Equation();

        eq.alias(1.2,"a");
        eq.process("a= 2.3 - 3");

        assertEquals(2.3 - 3.0, eq.lookupScalar("a"), 1e-8);
    }

    @Test
    public void subtract_matrix_matrix() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(3,4,-1,1,rand);
        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);
        SimpleMatrix c = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(a, "a", b, "b", c, "c");
        eq.process("a=b-c");

        assertTrue(b.minus(c).isIdentical(a,1e-8));
    }

    @Test
    public void subtract_matrix_scalar() {
        Equation eq = new Equation();

        SimpleMatrix a = SimpleMatrix.random(3,4,-1,1,rand);
        SimpleMatrix b = SimpleMatrix.random(3,4,-1,1,rand);

        eq.alias(a,"a",b,"b");

        eq.process("a=b-2.2");
        assertTrue(b.plus(-2.2).isIdentical(a,1e-8));

        eq.process("a=2.2-b");
        assertTrue(b.plus(-2.2).isIdentical(a, 1e-8));
    }

    @Test
    public void copy() {
        fail("Implement");
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

        assertEquals(1.0/2.2,eq.lookupScalar("b"),1e-8);
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

        assertEquals(1.0/2.2,eq.lookupScalar("b"),1e-8);
    }

    @Test
    public void det() {
        fail("Implement");
    }

    @Test
    public void trace() {
        fail("Implement");
    }

    @Test
    public void normF() {
        fail("Implement");
    }

    @Test
    public void eye() {
        fail("Implement");
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

        double found = eq.lookupScalar("B");
        assertEquals(4.6,found,1e-8);
    }

    @Test
    public void max_matrix() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 5, -1, 1, rand);

        eq.alias(A, "A");
        eq.alias(1.0, "B");

        eq.process("B=max(A)");

        double found = eq.lookupScalar("B");
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

        double found = eq.lookupScalar("B");
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
