/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.ejml.equation.TokenList.Type;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
public class TestEquation extends EjmlStandardJUnit {

    @Test void result() {
        var eq = new Equation();
        eq.process("A = 2");
        eq.process("B = 3");

        // Test scalar types
        assertEquals(5, (Integer)eq.result("A+B"), 0);
        assertEquals(5.5, (Double)eq.result("A+B+0.5"), 0.0);

        // Test matrix type
        eq.process("C = [2,1]");
        eq.process("D = [3,6]");
        DMatrixRMaj r = eq.result("C+D");
        assertEquals(2, r.getNumElements());
        assertEquals(5.0, r.get(0));
        assertEquals(7.0, r.get(1));
    }

    /// Basic test which checks ability parse basic operators and order of operation
    @Test void compile_basic() {
        var eq = new Equation();

        var A = new SimpleMatrix(5, 6);
        var B = SimpleMatrix.random_DDRM(5, 6, -1, 1, rand);
        var C = SimpleMatrix.random_DDRM(5, 4, -1, 1, rand);
        var D = SimpleMatrix.random_DDRM(4, 6, -1, 1, rand);

        eq.alias(A, "A");
        eq.alias(B, "B");
        eq.alias(C, "C");
        eq.alias(D, "D");

        Sequence sequence = eq.compile("A=B+C*D-B");
        var expected = C.mult(D);
        sequence.perform();
        assertTrue(expected.isIdentical(A, 1e-15));
    }

    /// Output is included in input
    @Test void compile_output() {
        var eq = new Equation();

        var A = SimpleMatrix.random_DDRM(6, 6, -1, 1, rand);
        var B = SimpleMatrix.random_DDRM(6, 6, -1, 1, rand);

        eq.alias(A, "A");
        eq.alias(B, "B");

        Sequence sequence = eq.compile("A=A*B");
        var expected = A.mult(B);
        sequence.perform();
        assertTrue(expected.isIdentical(A, 1e-15));
    }

    /// Results are assigned to a sub-matrix
    @Test void compile_assign_submatrix() {
        var eq = new Equation();

        var A = SimpleMatrix.random_DDRM(6, 6, -1, 1, rand);
        var B = SimpleMatrix.random_DDRM(2, 5, -1, 1, rand);

        var A_orig = A.copy();

        eq.alias(A, "A");
        eq.alias(B, "B");

        Sequence sequence = eq.compile("A(2:3,0:4)=B");
        sequence.perform();

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 6; x++) {
                if (x < 5 && y >= 2 && y <= 3) {
                    assertEquals(A.get(y, x), B.get(y - 2, x));
                } else {
                    assertEquals(A.get(y, x), A_orig.get(y, x), x + " " + y);
                }
            }
        }
    }

    @Test void compile_assign_submatrix_special() {
        var eq = new Equation();

        var A = SimpleMatrix.random_DDRM(6, 5, -1, 1, rand);
        var B = SimpleMatrix.random_DDRM(4, 5, -1, 1, rand);

        var A_orig = A.copy();

        eq.alias(A, "A");
        eq.alias(B, "B");

        eq.process("A(2:,:)=B");

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 5; x++) {
                if (y >= 2) {
                    assertEquals(A.get(y, x), B.get(y - 2, x));
                } else {
                    assertEquals(A.get(y, x), A_orig.get(y, x), x + " " + y);
                }
            }
        }
    }

    @Test void compile_assign_submatrix_scalar() {
        var eq = new Equation();

        var A = SimpleMatrix.random_DDRM(6, 5, -1, 1, rand);

        eq.alias(A, "A");

        // single element
        eq.process("A(1,2)=0.5");

        assertEquals(0.5, A.get(1, 2), UtilEjml.TEST_F64);

        // multiple elements
        eq.process("A(1:2,2:4)=0.5");

        for (int i = 1; i <= 2; i++) {
            for (int j = 2; j <= 4; j++) {
                assertEquals(0.5, A.get(i, j), UtilEjml.TEST_F64);
            }
        }
    }

    @Test void compile_assign_submatrix_IndexMath() {
        var eq = new Equation();

        var A = SimpleMatrix.random_DDRM(6, 5, -1, 1, rand);

        eq.alias(A, "A");

        // single element
//        eq.process("A(1+2,2-1)=0.5");
//
//        assertEquals(A.get(3, 1), 0.5, UtilEjml.TEST_F64);

        // multiple elements
        eq.process("A((1-1):2,2:3)=0.5");

        for (int i = 0; i <= 2; i++) {
            for (int j = 2; j <= 3; j++) {
                assertEquals(0.5, A.get(i, j), UtilEjml.TEST_F64);
            }
        }
    }

    /// Lazily declare a variable. Which means it is not explicitly aliased
    @Test void assign_lazy() {
        var eq = new Equation();

        var A = SimpleMatrix.random_DDRM(6, 5, -1, 1, rand);
        eq.alias(A, "A");
        eq.process("B=A");

        DMatrixRMaj B = eq.lookupDDRM("B");
        assertNotSame(A.getMatrix(), B);
        assertTrue(MatrixFeatures_DDRM.isEquals(A.getMatrix(), B));
    }

    /// Place an unknown variable on the right and see if it blows up
    @Test void assign_lazy_right() {
        Assertions.assertThrows(RuntimeException.class, () -> new Equation().process("B=A"));
    }

    /// See if matrices are automatically resized when assinged a value
    @Test void assign_resize_lazy() {
        var eq = new Equation();

        var A = SimpleMatrix.random_DDRM(6, 5, -1, 1, rand);
        var B = SimpleMatrix.random_DDRM(2, 3, -1, 1, rand);
        eq.alias(A, "A");
        eq.alias(B, "B");
        eq.process("B=A");

        assertTrue(A.isIdentical(B, UtilEjml.TEST_F64));
    }

    @Test void compile_parentheses() {
        var eq = new Equation();

        var A = SimpleMatrix.random_DDRM(6, 6, -1, 1, rand);
        var B = SimpleMatrix.random_DDRM(6, 6, -1, 1, rand);
        var C = SimpleMatrix.random_DDRM(6, 6, -1, 1, rand);
        var R = new SimpleMatrix(6, 6);

        eq.alias(A, "A");
        eq.alias(B, "B");
        eq.alias(C, "C");
        eq.alias(R, "R");

        eq.process("R=A*(B+C)");
        var expected = A.mult(B.plus(C));
        assertTrue(expected.isIdentical(R, 1e-15));

        // try again with pointless ones
        eq.process("R=(A*((B+(C))))");
        assertTrue(expected.isIdentical(R, 1e-15));
    }

    @Test void compile_parentheses_extract() {
        var eq = new Equation();

        var A = SimpleMatrix.random_DDRM(6, 6, -1, 1, rand);
        var B = SimpleMatrix.random_DDRM(8, 8, -1, 1, rand);

        eq.alias(A, "A");
        eq.alias(B, "B");

        Sequence sequence = eq.compile("A=B(2:7,1:6)");
        sequence.perform();
        assertTrue(A.isIdentical(B.extractMatrix(2, 8, 1, 7), 1e-15));

        // get single values now
        A = SimpleMatrix.random_DDRM(6, 1, -1, 1, rand);
        eq.alias(A, "A");
        sequence = eq.compile("A=B(2:7,3)");
        sequence.perform();
        assertTrue(A.isIdentical(B.extractMatrix(2, 8, 3, 4), 1e-15));

        // multiple in a row
        A = SimpleMatrix.random_DDRM(1, 2, -1, 1, rand);
        eq.alias(A, "A");
        sequence = eq.compile("A=(B(2:7,3:6))(0:0,1:2)");
        sequence.perform();
        assertTrue(A.isIdentical(B.extractMatrix(2, 3, 4, 6), 1e-15));
    }

    @Test void compile_parentheses_extractSpecial() {
        var eq = new Equation();

        var A = SimpleMatrix.random_DDRM(6, 8, -1, 1, rand);
        var B = SimpleMatrix.random_DDRM(8, 8, -1, 1, rand);

        eq.alias(A, "A");
        eq.alias(B, "B");

        eq.process("A=B(2:,:)");
        assertTrue(A.isIdentical(B.extractMatrix(2, 8, 0, 8), 1e-15));

        B = SimpleMatrix.random_DDRM(6, 10, -1, 1, rand);
        eq.alias(B, "B");
        eq.process("A=B(:,2:)");
        assertTrue(A.isIdentical(B.extractMatrix(0, 6, 2, 10), 1e-15));
    }

    @Test void compile_parentheses_extractScalar_2D() {
        var eq = new Equation();

        var B = SimpleMatrix.random_DDRM(8, 8, -1, 1, rand);

        eq.alias(B, "B");

        eq.process("A=B(1,2)");
        Variable v = eq.lookupVariable("A");
        assertInstanceOf(VariableDouble.class, v);
        assertEquals(eq.lookupDouble("A"), B.get(1, 2), UtilEjml.TEST_F64);
    }

    @Test void compile_parentheses_extractScalar_1D() {
        var eq = new Equation();

        var B = SimpleMatrix.random_DDRM(8, 8, -1, 1, rand);

        eq.alias(B, "B");

        eq.process("A=B(1)");
        eq.process("C=B(10)");

        Variable va = eq.lookupVariable("A");
        assertInstanceOf(VariableDouble.class, va);
        assertEquals(eq.lookupDouble("A"), B.get(1), UtilEjml.TEST_F64);

        Variable vc = eq.lookupVariable("C");
        assertInstanceOf(VariableDouble.class, vc);
        assertEquals(eq.lookupDouble("C"), B.get(10), UtilEjml.TEST_F64);
        assertEquals(eq.lookupDouble("C"), B.get(1, 2), UtilEjml.TEST_F64);
    }

    @Test void compile_parentheses_extract_IndexMath() {
        var eq = new Equation();

        var A = SimpleMatrix.random_DDRM(6, 6, -1, 1, rand);
        var B = SimpleMatrix.random_DDRM(8, 8, -1, 1, rand);

        eq.alias(A, "A");
        eq.alias(B, "B");
        eq.alias(1, "i");

        Sequence sequence = eq.compile("A=B((2-i):7,1:(6+i))");
        sequence.perform();
        assertTrue(A.isIdentical(B.extractMatrix(1, 8, 1, 8), 1e-15));
    }

    @Test void compile_neg() {
        var eq = new Equation();

        eq.alias(1, "A", 2, "B");

        eq.process("A=-B");
        assertEquals(-2, eq.lookupInteger("A"));

        eq.process("A=B--B");
        assertEquals(4, eq.lookupInteger("A"));
        eq.process("A=B+-B");
        assertEquals(0, eq.lookupInteger("A"));
        eq.process("A=B---5");
        assertEquals(2 - 5, eq.lookupInteger("A"));
        eq.process("A=B--5");
        assertEquals(2 + 5, eq.lookupInteger("A"));
    }

    @Test void compile_constructMatrix_scalars() {
        var eq = new Equation();

        var expected = new SimpleMatrix(new double[][]{{0, 1, 2, 3}, {4, 5, 6, 7}, {8, 1, 1, 1}});
        var A = new SimpleMatrix(3, 4);

        eq.alias(A, "A");
        Sequence sequence = eq.compile("A=[0 1 2 3; 4 5 6 7;8 1 1 1]");
        sequence.perform();
        assertTrue(A.isIdentical(expected, UtilEjml.TEST_F64));
    }

    @Test void compile_constructMatrix_doubles() {
        var eq = new Equation();

        eq.process("A=[1 2 3 4.5 6 7.7 8.8 9]");
        DMatrixRMaj found = eq.lookupDDRM("A");

        double[] expected = new double[]{1, 2, 3, 4.5, 6, 7.7, 8.8, 9};

        for (int i = 0; i < expected.length; i++) {
            assertEquals(found.get(i), expected[i], UtilEjml.TEST_F64);
        }
    }

    @Test void compile_constructMatrix_for() {
        var eq = new Equation();

        eq.process("A=[ 2:2:10 12 14 ]");
        DMatrixRMaj found = eq.lookupDDRM("A");

        assertEquals(7, found.getNumCols());
        assertEquals(1, found.getNumRows());

        for (int i = 0; i < 7; i++) {
            assertEquals(found.get(i), 2 + 2*i, UtilEjml.TEST_F64);
        }
    }

    @Test void compile_constructMatrix_commas() {
        var eq = new Equation();

        eq.process("A=[1 2 , 3, 4.5,-6 7]");
        DMatrixRMaj found = eq.lookupDDRM("A");

        double[] expected = new double[]{1, 2, 3, 4.5, -6, 7};

        for (int i = 0; i < expected.length; i++) {
            assertEquals(found.get(i), expected[i], UtilEjml.TEST_F64);
        }
    }

    @Test void compile_constructMatrix_MatrixAndScalar() {
        var eq = new Equation();

        var A = new SimpleMatrix(new double[][]{{0, 1, 2, 3}});
        var found = new SimpleMatrix(1, 5);

        eq.alias(A, "A");
        eq.alias(found, "found");
        Sequence sequence = eq.compile("found=[A 4]");
        sequence.perform();
        for (int i = 0; i < 5; i++) {
            assertEquals(found.get(0, i), i, 1e-4);
        }
    }

    @Test void compile_constructMatrix_Operations() {
        var eq = new Equation();

        var A = new SimpleMatrix(new double[][]{{0, 1, 2, 3}});
        var found = new SimpleMatrix(5, 1);

        eq.alias(A, "A");
        eq.alias(found, "found");
        Sequence sequence = eq.compile("found=[A' ; 4]");
        sequence.perform();
        for (int i = 0; i < 5; i++) {
            assertEquals(found.get(i, 0), i, 1e-4);
        }
    }

    @Test void compile_constructMatrix_Inner() {
        var eq = new Equation();

        var found = new SimpleMatrix(3, 2);

        eq.alias(found, "found");
        Sequence sequence = eq.compile("found=[[1 2 3]' [4 5 [6]]']");
        sequence.perform();
        int index = 1;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 3; y++) {
                assertEquals(found.get(y, x), index++, UtilEjml.TEST_F64, x + " " + y);
            }
        }
    }

    @Test void compile_constructMatrix_ForSequence_Case0() {
        var eq = new Equation();

        eq.process("found=[[1:4]' [2:2:8]']");
        var found = SimpleMatrix.wrap(eq.lookupDDRM("found"));
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 2; x++) {
                assertEquals((x + 1)*(y + 1), found.get(y, x), UtilEjml.TEST_F64, x + " " + y);
            }
        }
    }

    @Test void compile_constructMatrix_ForSequence_Case1() {
        var eq = new Equation();

        eq.process("found=[1:4 5:1:8]");
        var found = SimpleMatrix.wrap(eq.lookupDDRM("found"));
        assertEquals(1, found.numRows());
        assertEquals(8, found.numCols());

        for (int x = 0; x < 8; x++) {
            assertEquals(x + 1, found.get(0, x), UtilEjml.TEST_F64);
        }
    }

    @Test void compile_constructMatrix_ForSequence_Case2() {
        var eq = new Equation();

        eq.process("found=[1 2 3 4 5:1:8]");
        var found = SimpleMatrix.wrap(eq.lookupDDRM("found"));
        assertEquals(1, found.numRows());
        assertEquals(8, found.numCols());

        for (int x = 0; x < 8; x++) {
            assertEquals(x + 1, found.get(0, x), UtilEjml.TEST_F64);
        }
    }

    @Test void compile_constructMatrix_ParenSubMatrixAndComma() {
        var eq = new Equation();

        eq.process("b=normF([[1 2],1])");
        double b = eq.lookupDouble("b");
        double expected = Math.sqrt(1 + 4 + 1);

        assertEquals(expected, b, UtilEjml.TEST_F64);
    }

    @Test void assignEmptyMatrix() {
        var eq = new Equation();
        eq.process("A = []");
        DMatrixRMaj A = eq.lookupDDRM("A");
        assertEquals(0, A.numRows);
        assertEquals(0, A.numCols);
    }

    @Test void removeAll() {
        var eq = new Equation();
        eq.process("A = [1 2 3 4 5]'*[1 2 3 4]");
        eq.process("A = []");
        assertEquals(0, eq.lookupDDRM("A").getNumElements());
    }

    @Test void removeRows_range() {
        var eq = new Equation();
        eq.process("A = [1 2 3 4 5]'*[1 2 3 4]");
        eq.process("B=A");
        eq.process("A(2:3,:) = []");
        var A = eq.lookupDDRM("A");
        assertTrue(3 == A.numRows && 4 == A.numCols);

        var B = eq.lookupDDRM("B");
        for (int i = 0; i < A.numRows; i++) {
            int srcI = i < 2 ? i : i + 2;
            for (int j = 0; j < A.numCols; j++) {
                assertEquals(B.get(srcI, j), A.get(i, j));
            }
        }
    }

    @Test void removeColumns_range() {
        var eq = new Equation();
        eq.process("A = [1 2 3 4]'*[1 2 3 4 5]");
        eq.process("B=A");
        eq.process("A(:,2:3) = []");
        var A = eq.lookupDDRM("A");
        assertTrue(4 == A.numRows && 3 == A.numCols);

        var B = eq.lookupDDRM("B");
        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < A.numCols; j++) {
                int srcJ = j < 2 ? j : j + 2;
                assertEquals(B.get(i, srcJ), A.get(i, j));
            }
        }
    }

    @Test void removeRows_array() {
        var eq = new Equation();
        eq.process("A = [1 2 3 4 5]'*[1 2 3 4]");
        eq.process("B=A");
        eq.process("A(3 2,:) = []");
        // Note that 3 is before 2, this is to ensure that it sorts the rows before removal

        var A = eq.lookupDDRM("A");
        assertTrue(3 == A.numRows && 4 == A.numCols);

        var B = eq.lookupDDRM("B");
        for (int i = 0; i < A.numRows; i++) {
            int srcI = i < 2 ? i : i + 2;
            for (int j = 0; j < A.numCols; j++) {
                assertEquals(B.get(srcI, j), A.get(i, j));
            }
        }
    }

    @Test void removeColumns_array() {
        var eq = new Equation();
        eq.process("A = [1 2 3 4]'*[1 2 3 4 5]");
        eq.process("B=A");
        eq.process("A(:,3 2) = []");
        var A = eq.lookupDDRM("A");
        assertTrue(4 == A.numRows && 3 == A.numCols);

        var B = eq.lookupDDRM("B");
        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < A.numCols; j++) {
                int srcJ = j < 2 ? j : j + 2;
                assertEquals(B.get(i, srcJ), A.get(i, j));
            }
        }
    }

    @Test void compile_assign_IntSequence_Case0() {
        var eq = new Equation();

        eq.process("a=5:1:8");
        eq.process("b=[a]");
        var found = SimpleMatrix.wrap(eq.lookupDDRM("b"));
        assertEquals(1, found.numRows());
        assertEquals(4, found.numCols());

        for (int x = 0; x < 4; x++) {
            assertEquals(x + 5, found.get(0, x), UtilEjml.TEST_F64);
        }
    }

    // not sure how I feel about this, but its better to explicity check this behavior
    @Test void compile_assign_IntSequence_Case1() {
        var eq = new Equation();

        eq.process("a=2 3 4 5 6");
        eq.process("b=[a]");
        var found = SimpleMatrix.wrap(eq.lookupDDRM("b"));
        assertEquals(1, found.numRows());
        assertEquals(5, found.numCols());

        for (int x = 0; x < 5; x++) {
            assertEquals(x + 2, found.get(0, x), UtilEjml.TEST_F64);
        }
    }

    @Test void compile_assign_IntSequence_Case2() {
        var eq = new Equation();

        String[] tests = new String[]{"2 3 4 5 6 7:9", "2:4 5 6 7 8 9"};

        for (String s : tests) {
            eq.process("a=" + s);
            eq.process("b=[a]");
            var found = SimpleMatrix.wrap(eq.lookupDDRM("b"));
            assertEquals(1, found.numRows());
            assertEquals(8, found.numCols());

            for (int x = 0; x < 8; x++) {
                assertEquals(x + 2, found.get(0, x), UtilEjml.TEST_F64);
            }
        }
    }

    @Test void compile_assign_IntSequence_Case3() {
        var eq = new Equation();

        String[] tests = new String[]{"2 3:5", "2:4 5"};

        for (String s : tests) {
            eq.process("a=" + s);
            eq.process("b=[a]");
            var found = SimpleMatrix.wrap(eq.lookupDDRM("b"));
            assertEquals(1, found.numRows());
            assertEquals(4, found.numCols());

            for (int x = 0; x < 4; x++) {
                assertEquals(x + 2, found.get(0, x), UtilEjml.TEST_F64);
            }
        }
    }

    @Test void compile_assign_IntSequence_Case4() {
        var eq = new Equation();

        // needs to realize () is not a function call
        eq.process("a=3 2 1 0 (-1) (-2)");
        eq.process("b=[a]");
        var found = SimpleMatrix.wrap(eq.lookupDDRM("b"));
        assertEquals(1, found.numRows());
        assertEquals(6, found.numCols());

        for (int x = 0; x < 6; x++) {
            assertEquals(3 - x, found.get(0, x), UtilEjml.TEST_F64);
        }
    }

    @Test void compile_assign_IntSequence_Case5() {
        var eq = new Equation();

        // subtraction should have a higher priority than explicit list
        eq.process("a=3 2 1 5 - 3 - 2");
        eq.process("b=[a]");
        var found = SimpleMatrix.wrap(eq.lookupDDRM("b"));
        assertEquals(1, found.numRows());
        assertEquals(4, found.numCols());

        for (int x = 0; x < 4; x++) {
            assertEquals(3 - x, found.get(0, x), UtilEjml.TEST_F64);
        }
    }

    @Test void compile_assign_IntSequence_Case6() {
        var eq = new Equation();

        // Use commas to clarify the meaning of negative
        eq.process("a=3 2 1,0,-1,0 - 2");
        eq.process("b=[a]");
        var found = SimpleMatrix.wrap(eq.lookupDDRM("b"));
        assertEquals(1, found.numRows());
        assertEquals(6, found.numCols());

        for (int x = 0; x < 6; x++) {
            assertEquals(3 - x, found.get(0, x), UtilEjml.TEST_F64);
        }
    }

    @Test void compile_transpose() {
        var eq = new Equation();

        var A = SimpleMatrix.random_DDRM(6, 6, -1, 1, rand);
        var B = SimpleMatrix.random_DDRM(6, 6, -1, 1, rand);
        var C = SimpleMatrix.random_DDRM(6, 6, -1, 1, rand);
        var R = new SimpleMatrix(6, 6);

        eq.alias(A, "A");
        eq.alias(B, "B");
        eq.alias(C, "C");
        eq.alias(R, "R");

        Sequence sequence = eq.compile("R=A'*(B'+C)'+inv(B)'");
        var expected = A.transpose().mult(B.transpose().plus(C).transpose()).plus(B.invert().transpose());
        sequence.perform();
        assertTrue(expected.isIdentical(R, 1e-15));
    }

    @Test void compile_elementWise() {
        var eq = new Equation();

        var A = SimpleMatrix.random_DDRM(6, 6, -1, 1, rand);
        var B = SimpleMatrix.random_DDRM(6, 6, -1, 1, rand);
        var C = SimpleMatrix.random_DDRM(6, 6, -1, 1, rand);
        var R = new SimpleMatrix(6, 6);

        eq.alias(A, "A");
        eq.alias(B, "B");
        eq.alias(C, "C");
        eq.alias(R, "R");

        Sequence sequence = eq.compile("R=A.*(B./C)");
        var expected = A.elementMult(B.elementDiv(C));
        sequence.perform();
        assertTrue(expected.isIdentical(R, 1e-15));
    }

    @Test void compile_double() {
        var eq = new Equation();

        var A = SimpleMatrix.random_DDRM(6, 6, -1, 1, rand);
        var B = SimpleMatrix.random_DDRM(6, 6, -1, 1, rand);
        double C = 2.5;
        double D = 1.7;

        eq.alias(A, "A");
        eq.alias(B, "B");
        eq.alias(D, "D");
        eq.alias(0.0, "E");

        VariableDouble E = eq.lookupVariable("E");

        Sequence sequence = eq.compile("A=2.5*B");
        var expected = B.scale(C);
        sequence.perform();
        assertTrue(expected.isIdentical(A, 1e-15));

        sequence = eq.compile("A=B*2.5");
        sequence.perform();
        assertTrue(expected.isIdentical(A, 1e-15));

        sequence = eq.compile("E=2.5*D");
        sequence.perform();
        assertEquals(C*D, E.value, UtilEjml.TEST_F64);

        // try exponential formats
        sequence = eq.compile("E=2.001e-6*1e3");
        sequence.perform();
        assertEquals(2.001e-6*1e3, E.value, UtilEjml.TEST_F64);
    }

    /// Function with one input
    @Test void compile_function_one() {
        var eq = new Equation();

        var A = SimpleMatrix.random_DDRM(6, 6, -1, 1, rand);
        var B = SimpleMatrix.random_DDRM(6, 6, -1, 1, rand);
        var C = SimpleMatrix.random_DDRM(6, 6, -1, 1, rand);
        var R = new SimpleMatrix(6, 6);

        eq.alias(A, "A");
        eq.alias(B, "B");
        eq.alias(C, "C");
        eq.alias(R, "R");

        // easy case
        Sequence sequence = eq.compile("R=inv(A)");
        var expected = A.invert();
        sequence.perform();
        assertTrue(expected.isIdentical(R, 1e-15));

        // harder case
        sequence = eq.compile("R=inv(A)+det((A+B)*C)*B");
        expected = A.invert().plus(B.scale(A.plus(B).mult(C).determinant()));
        sequence.perform();
        assertTrue(expected.isIdentical(R, 1e-15));

        // this should throw an exception
        try {
            eq.compile("R=inv*B");
            fail("Implement");
        } catch (RuntimeException ignore) {
        }
    }

    /// Function with two input
    @Test void compile_function_N() {
        var eq = new Equation();

        var A = SimpleMatrix.random_DDRM(3, 4, -1, 1, rand);
        var B = SimpleMatrix.random_DDRM(4, 5, -1, 1, rand);
        var R = new SimpleMatrix(12, 20);

        eq.alias(A, "A");
        eq.alias(B, "B");
        eq.alias(R, "R");

        eq.process("R=kron(A,B)");
        var expected = A.kron(B);
        assertTrue(expected.isIdentical(R, 1e-15));

        eq.process("R=kron(A+(A')',(B+B))");
        expected = A.plus(A).kron(B.plus(B));
        assertTrue(expected.isIdentical(R, 1e-15));
    }

    @Test void handleParentheses() {
        var eq = new Equation();
        ManagerTempVariables managerTemp = new ManagerTempVariables();
        eq.functions.setManagerTemp(managerTemp);

        eq.alias(new DMatrixRMaj(1, 1), "A");
        eq.alias(new DMatrixRMaj(1, 1), "B");
        eq.alias(new DMatrixRMaj(1, 1), "C");

        // handle empty case
        Sequence sequence = new Sequence();
        TokenList tokens = eq.extractTokens("((()))()", managerTemp);
        eq.handleParentheses(tokens, sequence);
        assertEquals(0, sequence.operations.size());
        assertEquals(0, tokens.size);

        // embedded with just one variable
        sequence = new Sequence();
        tokens = eq.extractTokens("(((A)))", managerTemp);
        eq.insertFunctionsAndVariables(tokens);
        eq.handleParentheses(tokens, sequence);
        assertEquals(0, sequence.operations.size());
        assertEquals(1, tokens.size);
        assertSame(Type.VARIABLE, tokens.first.getType());

        // pointless
        sequence = new Sequence();
        tokens = eq.extractTokens("((A)*(B)+(C))", managerTemp);
        eq.insertFunctionsAndVariables(tokens);
        eq.handleParentheses(tokens, sequence);
        assertEquals(2, sequence.operations.size());
        assertEquals(1, tokens.size);
        assertSame(Type.VARIABLE, tokens.first.getType());
    }

    @Test void parseOperations() {
        var eq = new Equation();
        ManagerTempVariables managerTemp = new ManagerTempVariables();
        eq.functions.setManagerTemp(managerTemp);

        eq.alias(new DMatrixRMaj(1, 1), "A");
        eq.alias(new DMatrixRMaj(1, 1), "B");
        eq.alias(new DMatrixRMaj(1, 1), "C");

        // give it an empty list
        TokenList tokens = eq.extractTokens("", managerTemp);
        eq.insertFunctionsAndVariables(tokens);

        Sequence sequence = new Sequence();

        eq.parseOperationsLR(new Symbol[]{Symbol.TIMES}, tokens, sequence);
        assertEquals(0, sequence.operations.size());
        assertEquals(0, tokens.size);

        // other cases
        tokens = eq.extractTokens("B+B-A*B*A", managerTemp);
        eq.insertFunctionsAndVariables(tokens);
        sequence = new Sequence();

        eq.parseOperationsLR(new Symbol[]{Symbol.TIMES}, tokens, sequence);

        assertEquals(2, sequence.operations.size());
        assertEquals(5, tokens.size);
        assertSame(Type.VARIABLE, tokens.last.getType());
        assertSame(Symbol.MINUS, tokens.last.previous.getSymbol());

        tokens = eq.extractTokens("B+B*B*A-B", managerTemp);
        eq.insertFunctionsAndVariables(tokens);
        sequence = new Sequence();

        eq.parseOperationsLR(new Symbol[]{Symbol.PLUS, Symbol.MINUS}, tokens, sequence);

        assertEquals(2, sequence.operations.size());
        assertEquals(5, tokens.size);
        assertSame(Type.VARIABLE, tokens.last.getType());
        assertSame(Symbol.TIMES, tokens.last.previous.getSymbol());
        assertSame(Symbol.TIMES, tokens.first.next.next.next.getSymbol());
    }

    @Test void createOp() {
        var eq = new Equation();
        ManagerTempVariables managerTemp = new ManagerTempVariables();
        eq.functions.setManagerTemp(managerTemp);

        eq.alias(new DMatrixRMaj(1, 1), "A");
        eq.alias(new DMatrixRMaj(1, 1), "B");

        TokenList tokens = eq.extractTokens("A=A*B", managerTemp);

        TokenList.Token t0 = tokens.first.next.next;
        TokenList.Token t1 = t0.next;
        TokenList.Token t2 = t1.next;

        Sequence sequence = new Sequence();

        TokenList.Token found = eq.createOp(t0, t1, t2, tokens, sequence);
        assertSame(Type.VARIABLE, found.getType());
        assertEquals(3, tokens.size);
        assertSame(Symbol.ASSIGN, tokens.first.next.getSymbol());
        assertSame(found, tokens.last);
        assertEquals(1, sequence.operations.size());
    }

    @Test void lookupVariable() {
        var eq = new Equation();
        eq.alias(new DMatrixRMaj(1, 1), "A");
        eq.alias(new DMatrixRMaj(1, 1), "BSD");

        eq.lookupVariable("A");
        eq.lookupVariable("BSD");
        assertNull(eq.lookupVariable("dDD"));
    }

    @Test void extractTokens() {
        var eq = new Equation();
        ManagerTempVariables managerTemp = new ManagerTempVariables();

        TokenList list = eq.extractTokens("A = A*A + BSD*(A+BSD) -A*BSD", managerTemp);

        TokenList.Token t = list.getFirst();

        assertEquals("A", t.word);
        t = t.next;
        assertSame(Symbol.ASSIGN, t.getSymbol());
        t = t.next;
        assertEquals("A", t.word);
        t = t.next;
        assertSame(Symbol.TIMES, t.getSymbol());
        t = t.next;
        assertEquals("A", t.word);
        t = t.next;
        assertSame(Symbol.PLUS, t.getSymbol());
        t = t.next;
        assertEquals("BSD", t.word);
        t = t.next;
        assertSame(Symbol.TIMES, t.getSymbol());
        t = t.next;
        assertSame(Symbol.PAREN_LEFT, t.getSymbol());
        t = t.next;
        assertEquals("A", t.word);
        t = t.next;
        assertSame(Symbol.PLUS, t.getSymbol());
        t = t.next;
        assertEquals("BSD", t.word);
        t = t.next;
        assertSame(Symbol.PAREN_RIGHT, t.getSymbol());
        t = t.next;
        assertSame(Symbol.MINUS, t.getSymbol());
        t = t.next;
        assertEquals("A", t.word);
        t = t.next;
        assertSame(Symbol.TIMES, t.getSymbol());
        t = t.next;
        assertEquals("BSD", t.word);
        assertNull(t.next);
    }

    @Test void extractTokens_elementWise() {
        var eq = new Equation();
        ManagerTempVariables managerTemp = new ManagerTempVariables();

        TokenList list = eq.extractTokens("A = (A.*A)./BSD", managerTemp);

        TokenList.Token t = list.getFirst();

        assertEquals("A", t.word);
        t = t.next;
        assertSame(Symbol.ASSIGN, t.getSymbol());
        t = t.next;
        assertSame(Symbol.PAREN_LEFT, t.getSymbol());
        t = t.next;
        assertEquals("A", t.word);
        t = t.next;
        assertSame(Symbol.ELEMENT_TIMES, t.getSymbol());
        t = t.next;
        assertEquals("A", t.word);
        t = t.next;
        assertSame(Symbol.PAREN_RIGHT, t.getSymbol());
        t = t.next;
        assertSame(Symbol.ELEMENT_DIVIDE, t.getSymbol());
        t = t.next;
        assertEquals("BSD", t.word);
        assertNull(t.next);
    }

    @Test void extractTokens_integers() {
        var eq = new Equation();
        ManagerTempVariables managerTemp = new ManagerTempVariables();

        TokenList list = eq.extractTokens("A*2 + 345 + 56*BSD*934", managerTemp);

        TokenList.Token t = list.getFirst();

        assertEquals("A", t.word);
        t = t.next;
        assertSame(Symbol.TIMES, t.getSymbol());
        t = t.next;
        assertEquals(2, ((VariableInteger)t.getVariable()).value);
        t = t.next;
        assertSame(Symbol.PLUS, t.getSymbol());
        t = t.next;
        assertEquals(345, ((VariableInteger)t.getVariable()).value);
        t = t.next;
        assertSame(Symbol.PLUS, t.getSymbol());
        t = t.next;
        assertEquals(56, ((VariableInteger)t.getVariable()).value);
        t = t.next;
        assertSame(Symbol.TIMES, t.getSymbol());
        t = t.next;
        assertEquals("BSD", t.word);
        t = t.next;
        assertSame(Symbol.TIMES, t.getSymbol());
        t = t.next;
        assertEquals(934, ((VariableInteger)t.getVariable()).value);
        assertNull(t.next);
    }

    @Test void extractTokens_doubles() {
        var eq = new Equation();
        ManagerTempVariables managerTemp = new ManagerTempVariables();

        TokenList list = eq.extractTokens("A*2. + 345.034 + 0.123*BSD*5.1", managerTemp);

        TokenList.Token t = list.getFirst();

        assertEquals("A", t.word);
        t = t.next;
        assertSame(Symbol.TIMES, t.getSymbol());
        t = t.next;
        assertEquals(2, ((VariableDouble)t.getVariable()).value);
        t = t.next;
        assertSame(Symbol.PLUS, t.getSymbol());
        t = t.next;
        assertEquals(345.034, ((VariableDouble)t.getVariable()).value);
        t = t.next;
        assertSame(Symbol.PLUS, t.getSymbol());
        t = t.next;
        assertEquals(0.123, ((VariableDouble)t.getVariable()).value);
        t = t.next;
        assertSame(Symbol.TIMES, t.getSymbol());
        t = t.next;
        assertEquals("BSD", t.word);
        t = t.next;
        assertSame(Symbol.TIMES, t.getSymbol());
        t = t.next;
        assertEquals(5.1, ((VariableDouble)t.getVariable()).value);
        assertNull(t.next);
    }

    /// See if the minus symbol is handled correctly. It's meaning can very depending on the situation.
    @Test void extractTokens_minus() {
        var eq = new Equation();
        ManagerTempVariables managerTemp = new ManagerTempVariables();

        TokenList list = eq.extractTokens("- 1.2", managerTemp);
        TokenList.Token t = list.getFirst();
        assertSame(Symbol.MINUS, t.getSymbol());
        t = t.next;
        assertEquals(1.2, ((VariableDouble)t.getVariable()).value);
        assertNull(t.next);

        list = eq.extractTokens("-1.2", managerTemp);
        t = list.getFirst();
        assertEquals(-1.2, ((VariableDouble)t.getVariable()).value);
        assertNull(t.next);

        list = eq.extractTokens("2.1-1.2", managerTemp);
        t = list.getFirst();
        assertEquals(2.1, ((VariableDouble)t.getVariable()).value);
        t = t.next;
        assertSame(Symbol.MINUS, t.getSymbol());
        t = t.next;
        assertEquals(1.2, ((VariableDouble)t.getVariable()).value);
        assertNull(t.next);

        list = eq.extractTokens("2.1 -1.2", managerTemp);
        t = list.getFirst();
        assertEquals(2.1, ((VariableDouble)t.getVariable()).value);
        t = t.next;
        assertSame(Symbol.MINUS, t.getSymbol());
        t = t.next;
        assertEquals(1.2, ((VariableDouble)t.getVariable()).value);
        assertNull(t.next);

        list = eq.extractTokens("2.1 - -1.2", managerTemp);
        t = list.getFirst();
        assertEquals(2.1, ((VariableDouble)t.getVariable()).value);
        t = t.next;
        assertSame(Symbol.MINUS, t.getSymbol());
        t = t.next;
        assertEquals(-1.2, ((VariableDouble)t.getVariable()).value);
        assertNull(t.next);

        list = eq.extractTokens("inv(2.1) -1.2", managerTemp);
        t = list.getFirst();
        assertEquals("inv", t.word);
        t = t.next;
        assertSame(Symbol.PAREN_LEFT, t.getSymbol());
        t = t.next;
        assertEquals(2.1, ((VariableDouble)t.getVariable()).value);
        t = t.next;
        assertSame(Symbol.PAREN_RIGHT, t.getSymbol());
        t = t.next;
        assertSame(Symbol.MINUS, t.getSymbol());
        t = t.next;
        assertEquals(1.2, ((VariableDouble)t.getVariable()).value);
        assertNull(t.next);

        list = eq.extractTokens("= -1.2", managerTemp);
        t = list.getFirst();
        assertSame(Symbol.ASSIGN, t.getSymbol());
        t = t.next;
        assertEquals(-1.2, ((VariableDouble)t.getVariable()).value);
        assertNull(t.next);

        list = eq.extractTokens("= - 1.2", managerTemp);
        t = list.getFirst();
        assertSame(Symbol.ASSIGN, t.getSymbol());
        t = t.next;
        assertSame(Symbol.MINUS, t.getSymbol());
        t = t.next;
        assertEquals(1.2, ((VariableDouble)t.getVariable()).value);
        assertNull(t.next);
    }

    @Test void insertFunctionsAndVariables() {
        var eq = new Equation();
        ManagerTempVariables managerTemp = new ManagerTempVariables();

        eq.alias(new DMatrixRMaj(1, 1), "A");
        eq.alias(new DMatrixRMaj(1, 1), "BSD");

        Variable v0 = eq.lookupVariable("A");
        Variable v1 = eq.lookupVariable("BSD");

        TokenList list = eq.extractTokens("A = inv(A.*A)./BSD", managerTemp);
        eq.insertFunctionsAndVariables(list);

        TokenList.Token t = list.getFirst();

        assertSame(v0, t.getVariable());
        t = t.next;
        assertSame(Symbol.ASSIGN, t.getSymbol());
        t = t.next;
        assertSame(Type.FUNCTION, t.getType());
        t = t.next;
        assertSame(Symbol.PAREN_LEFT, t.getSymbol());
        t = t.next;
        assertSame(v0, t.getVariable());
        t = t.next;
        assertSame(Symbol.ELEMENT_TIMES, t.getSymbol());
        t = t.next;
        assertSame(v0, t.getVariable());
        t = t.next;
        assertSame(Symbol.PAREN_RIGHT, t.getSymbol());
        t = t.next;
        assertSame(Symbol.ELEMENT_DIVIDE, t.getSymbol());
        t = t.next;
        assertSame(v1, t.getVariable());
        assertNull(t.next);
    }

    @Test void isTargetOp() {
        Symbol[] targets = new Symbol[]{Symbol.PERIOD, Symbol.TIMES, Symbol.TRANSPOSE};
        assertTrue(Equation.isTargetOp(new TokenList.Token(Symbol.TIMES), targets));
        assertFalse(Equation.isTargetOp(new TokenList.Token(Symbol.RDIVIDE), targets));
    }

    @Test void isLetter() {
        assertTrue(Equation.isLetter('a'));
        assertTrue(Equation.isLetter('_'));
        assertTrue(Equation.isLetter('5'));

        assertFalse(Equation.isLetter(' '));
        assertFalse(Equation.isLetter('\t'));
        assertFalse(Equation.isLetter('*'));
        assertFalse(Equation.isLetter('+'));
        assertFalse(Equation.isLetter('-'));
        assertFalse(Equation.isLetter('('));
        assertFalse(Equation.isLetter(')'));
        assertFalse(Equation.isLetter('='));
    }

    @Test void gracefullyHandleBadCode() {
        checkForParseException("a(2,4:5)");
        checkForParseException("a(2,4:5");
        checkForParseException("m=[3:4:]");
        checkForParseException("m=[1:5;2,3,4]");
    }

    private void checkForParseException( String code ) {
        Assertions.assertThrows(ParseError.class, () -> {
            var eq = new Equation();
            eq.process(code);
        });
    }

    @Test void macro() {
        ManagerTempVariables managerTemp = new ManagerTempVariables();
        var eq = new Equation();

        eq.process("macro foo( a , b , c) = c*(a*b)");
        TokenList tokens = eq.extractTokens("H=foo(1,2,3)", managerTemp);
        eq.insertMacros(tokens);

        TokenList.Token t = tokens.getFirst();

        assertEquals("H", t.word);
        t = t.next;
        assertSame(Symbol.ASSIGN, t.getSymbol());
        t = t.next;
        assertEquals(3, ((VariableInteger)t.variable).value);
        t = t.next;
        assertSame(Symbol.TIMES, t.getSymbol());
        t = t.next;
        assertSame(Symbol.PAREN_LEFT, t.getSymbol());
        t = t.next;
        assertEquals(1, ((VariableInteger)t.variable).value);
        t = t.next;
        assertSame(Symbol.TIMES, t.getSymbol());
        t = t.next;
        assertEquals(2, ((VariableInteger)t.variable).value);
        t = t.next;
        assertSame(Symbol.PAREN_RIGHT, t.getSymbol());
        t = t.next;
        assertNull(t);
    }

    /// Hard to test output for correctness. Basically just checsk to see if it crashes
    @Test void print() {
        // Disable unit test
        System.setOut(super.systemOut);
        var eq = new Equation();
        eq.print("[1 2;3 4]");
        eq.process("A=[1 2;3 4;5 6]");
        eq.print("A");
        eq.print("B=5");
    }

    @Test void isUncountable() {
        var eq = new Equation();
        eq.process("A=1");
        eq.process("B=1.1");
        eq.process("C=[1 2;3 4;5 6]");
        eq.process("D=[1 2;NaN 4;5 6]");
        eq.process("E=NaN");

        List<String> found = eq.isUncountable("A,B,C,D,E");
        assertEquals(2, found.size());
        assertTrue(found.contains("D"));
        assertTrue(found.contains("E"));
    }

    @Test void isTrueMatrix() {
        var eq = new Equation();
        eq.process("C=[1 2;3 4;5 6]");
        eq.process("D=[1 2;NaN 4;5 6]");

        List<String> found = eq.isTrueMatrix("C,D", m -> !MatrixFeatures_DDRM.hasUncountable(m));
        assertEquals(1, found.size());
        assertTrue(found.contains("D"));
    }
}
