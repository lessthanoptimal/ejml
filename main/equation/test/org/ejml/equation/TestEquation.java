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

import org.ejml.data.DenseMatrix64F;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.ejml.equation.TokenList.Type;
import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestEquation {

    Random rand = new Random(234);

    /**
     * Basic test which checks ability parse basic operators and order of operation
     */
    @Test
    public void compile_basic() {
        Equation eq = new Equation();

        SimpleMatrix A = new SimpleMatrix(5, 6);
        SimpleMatrix B = SimpleMatrix.random(5, 6, -1, 1, rand);
        SimpleMatrix C = SimpleMatrix.random(5, 4, -1, 1, rand);
        SimpleMatrix D = SimpleMatrix.random(4, 6, -1, 1, rand);

        eq.alias(A.getMatrix(), "A");
        eq.alias(B.getMatrix(), "B");
        eq.alias(C.getMatrix(), "C");
        eq.alias(D.getMatrix(), "D");

        Sequence sequence = eq.compile("A=B+C*D-B");
        SimpleMatrix expected = C.mult(D);
        sequence.perform();
        assertTrue(expected.isIdentical(A,1e-15));
    }

    /**
     * Output is included in input
     */
    @Test
    public void compile_output() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 6, -1, 1, rand);
        SimpleMatrix B = SimpleMatrix.random(6, 6, -1, 1, rand);

        eq.alias(A.getMatrix(), "A");
        eq.alias(B.getMatrix(), "B");

        Sequence sequence = eq.compile("A=A*B");
        SimpleMatrix expected = A.mult(B);
        sequence.perform();
        assertTrue(expected.isIdentical(A,1e-15));
    }

    @Test
    public void compile_parentheses() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 6, -1, 1, rand);
        SimpleMatrix B = SimpleMatrix.random(6, 6, -1, 1, rand);
        SimpleMatrix C = SimpleMatrix.random(6, 6, -1, 1, rand);
        SimpleMatrix R = new SimpleMatrix(6, 6);

        eq.alias(A.getMatrix(), "A");
        eq.alias(B.getMatrix(), "B");
        eq.alias(C.getMatrix(), "C");
        eq.alias(R.getMatrix(), "R");

        Sequence sequence = eq.compile("R=A*(B+C)");
        SimpleMatrix expected = A.mult(B.plus(C));
        sequence.perform();
        assertTrue(expected.isIdentical(R, 1e-15));

        // try again with pointless ones
        sequence = eq.compile("R=(A*((B+(C))))");
        sequence.perform();
        assertTrue(expected.isIdentical(R,1e-15));
    }

    @Test
    public void compile_parentheses_extract() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 6, -1, 1, rand);
        SimpleMatrix B = SimpleMatrix.random(8, 8, -1, 1, rand);

        eq.alias(A.getMatrix(), "A");
        eq.alias(B.getMatrix(), "B");

        Sequence sequence = eq.compile("A=B(2:7,1:6)");
        sequence.perform();
        assertTrue(A.isIdentical(B.extractMatrix(2,8,1,7), 1e-15));

        // get single values now
        A = SimpleMatrix.random(6, 1, -1, 1, rand);
        eq.alias(A.getMatrix(), "A");
        sequence = eq.compile("A=B(2:7,3)");
        sequence.perform();
        assertTrue(A.isIdentical(B.extractMatrix(2,8,3,4), 1e-15));

        // multiple in a row
        A = SimpleMatrix.random(1, 2, -1, 1, rand);
        eq.alias(A.getMatrix(), "A");
        sequence = eq.compile("A=(B(2:7,3:6))(0:0,1:2)");
        sequence.perform();
        assertTrue(A.isIdentical(B.extractMatrix(2,3,4,6), 1e-15));
    }

    @Test
    public void compile_constructMatrix_scalars() {
        Equation eq = new Equation();

        SimpleMatrix expected = new SimpleMatrix(new double[][]{{0,1,2,3},{4,5,6,7},{8,1,1,1}});
        SimpleMatrix A = new SimpleMatrix(3,4);

        eq.alias(A.getMatrix(), "A");
        Sequence sequence = eq.compile("A=[0 1 2 3; 4 5 6 7;8 1 1 1]");
        sequence.perform();
        assertTrue(A.isIdentical(expected,1e-8));
    }

    @Test
    public void compile_constructMatrix_MatrixAndScalar() {
        Equation eq = new Equation();

        SimpleMatrix A = new SimpleMatrix(new double[][]{{0,1,2,3}});
        SimpleMatrix found = new SimpleMatrix(1,5);

        eq.alias(A.getMatrix(), "A");
        eq.alias(found.getMatrix(), "found");
        Sequence sequence = eq.compile("found=[A 4]");
        sequence.perform();
        for (int i = 0; i < 5; i++) {
            assertEquals(found.get(0,i),i,1e-4);
        }
    }

    @Test
    public void compile_constructMatrix_Operations() {
        Equation eq = new Equation();

        SimpleMatrix A = new SimpleMatrix(new double[][]{{0,1,2,3}});
        SimpleMatrix found = new SimpleMatrix(5,1);

        eq.alias(A.getMatrix(), "A");
        eq.alias(found.getMatrix(), "found");
        Sequence sequence = eq.compile("found=[A' ; 4]");
        sequence.perform();
        for (int i = 0; i < 5; i++) {
            assertEquals(found.get(i,0),i,1e-4);
        }
    }

    @Test
    public void compile_constructMatrix_Inner() {
        Equation eq = new Equation();

        SimpleMatrix found = new SimpleMatrix(3,2);

        eq.alias(found.getMatrix(), "found");
        Sequence sequence = eq.compile("found=[[1 2 3]' [4 5 [6]]']");
        sequence.perform();
        int index = 1;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 3; y++) {
                assertEquals(x+" "+y,found.get(y,x),index++,1e-8);
            }
        }
    }

    @Test
    public void compile_transpose() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 6, -1, 1, rand);
        SimpleMatrix B = SimpleMatrix.random(6, 6, -1, 1, rand);
        SimpleMatrix C = SimpleMatrix.random(6, 6, -1, 1, rand);
        SimpleMatrix R = new SimpleMatrix(6, 6);

        eq.alias(A.getMatrix(), "A");
        eq.alias(B.getMatrix(), "B");
        eq.alias(C.getMatrix(), "C");
        eq.alias(R.getMatrix(), "R");

        Sequence sequence = eq.compile("R=A'*(B'+C)'+inv(B)'");
        SimpleMatrix expected = A.transpose().mult(B.transpose().plus(C).transpose()).plus(B.invert().transpose());
        sequence.perform();
        assertTrue(expected.isIdentical(R, 1e-15));
    }

    @Test
    public void compile_elementWise() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 6, -1, 1, rand);
        SimpleMatrix B = SimpleMatrix.random(6, 6, -1, 1, rand);
        SimpleMatrix C = SimpleMatrix.random(6, 6, -1, 1, rand);
        SimpleMatrix R = new SimpleMatrix(6, 6);

        eq.alias(A.getMatrix(), "A");
        eq.alias(B.getMatrix(), "B");
        eq.alias(C.getMatrix(), "C");
        eq.alias(R.getMatrix(), "R");

        Sequence sequence = eq.compile("R=A.*(B./C)");
        SimpleMatrix expected = A.elementMult(B.elementDiv(C));
        sequence.perform();
        assertTrue(expected.isIdentical(R, 1e-15));
    }

    @Test
    public void compile_double() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 6, -1, 1, rand);
        SimpleMatrix B = SimpleMatrix.random(6, 6, -1, 1, rand);
        double C = 2.5;
        double D = 1.7;

        eq.alias(A.getMatrix(), "A");
        eq.alias(B.getMatrix(), "B");
        eq.alias(D, "D");
        eq.alias(0, "E");

        VariableDouble E = eq.lookupVariable("E");

        Sequence sequence = eq.compile("A=2.5*B");
        SimpleMatrix expected = B.scale(C);
        sequence.perform();
        assertTrue(expected.isIdentical(A, 1e-15));

        sequence = eq.compile("A=B*2.5");
        sequence.perform();
        assertTrue(expected.isIdentical(A, 1e-15));

        sequence = eq.compile("E=2.5*D");
        sequence.perform();
        assertEquals(C * D, E.value, 1e-8);

        // try exponential formats
        sequence = eq.compile("E=2.001e-6*1e3");
        sequence.perform();
        assertEquals(2.001e-6*1e3, E.value, 1e-8);
    }

    /**
     * Function with one input
     */
    @Test
    public void compile_function_one() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 6, -1, 1, rand);
        SimpleMatrix B = SimpleMatrix.random(6, 6, -1, 1, rand);
        SimpleMatrix C = SimpleMatrix.random(6, 6, -1, 1, rand);
        SimpleMatrix R = new SimpleMatrix(6, 6);

        eq.alias(A.getMatrix(), "A");
        eq.alias(B.getMatrix(), "B");
        eq.alias(C.getMatrix(), "C");
        eq.alias(R.getMatrix(), "R");

        // easy case
        Sequence sequence = eq.compile("R=inv(A)");
        SimpleMatrix expected = A.invert();
        sequence.perform();
        assertTrue(expected.isIdentical(R, 1e-15));

        // harder case
        sequence = eq.compile("R=inv(A)+det((A+B)*C)*B");
        expected = A.invert().plus( B.scale(A.plus(B).mult(C).determinant()));
        sequence.perform();
        assertTrue(expected.isIdentical(R, 1e-15));

        // this should throw an exception
        try {
            eq.compile("R=inv*B");
            fail("Implement");
        } catch( RuntimeException ignore ){}
    }

    /**
     * Function with two input
     */
    @Test
    public void compile_function_N() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(3, 4, -1, 1, rand);
        SimpleMatrix B = SimpleMatrix.random(4, 5, -1, 1, rand);
        SimpleMatrix R = new SimpleMatrix(12, 20);

        eq.alias(A.getMatrix(), "A");
        eq.alias(B.getMatrix(), "B");
        eq.alias(R.getMatrix(), "R");

        eq.process("R=kron(A,B)");
        SimpleMatrix expected = A.kron(B);
        assertTrue(expected.isIdentical(R, 1e-15));

        eq.process("R=kron(A+(A')',(B+B))");
        expected = A.plus(A).kron(B.plus(B));
        assertTrue(expected.isIdentical(R, 1e-15));
    }

    /**
     * Test concatenate functions
     */
    @Test
    public void compile_concat() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(1, 1, -1, 1, rand);
        SimpleMatrix B = SimpleMatrix.random(2, 1, -1, 1, rand);
        SimpleMatrix C = SimpleMatrix.random(3, 1, -1, 1, rand);
        SimpleMatrix V = new SimpleMatrix(6,1);
        SimpleMatrix H = new SimpleMatrix(1,6);

        eq.alias(A.getMatrix(), "A");
        eq.alias(B.getMatrix(), "B");
        eq.alias(C.getMatrix(), "C");
        eq.alias(V.getMatrix(), "V");
        eq.alias(H.getMatrix(), "H");

        eq.process("H=catH(A',B',C')");
        assertEquals(A.get(0, 0), H.get(0, 0), 1e-8);
        assertEquals(B.get(0,0),H.get(0,1),1e-8);
        assertEquals(C.get(0,0),H.get(0,3),1e-8);

        eq.process("V=catV(A,B,C)");
        assertEquals(A.get(0,0),H.get(0,0),1e-8);
        assertEquals(B.get(0, 0), H.get(0, 1), 1e-8);
        assertEquals(C.get(0, 0), H.get(0, 3), 1e-8);
    }

    @Test
    public void handleParentheses() {
        Equation eq = new Equation();
        ManagerTempVariables managerTemp = new ManagerTempVariables();
        eq.functions.setManagerTemp( managerTemp );

        eq.alias(new DenseMatrix64F(1, 1), "A");
        eq.alias(new DenseMatrix64F(1, 1), "B");
        eq.alias(new DenseMatrix64F(1, 1), "C");

        // handle empty case
        Sequence sequence = new Sequence();
        TokenList tokens = eq.extractTokens("((()))()",managerTemp);
        eq.handleParentheses(tokens,sequence);
        assertEquals(0,sequence.operations.size());
        assertEquals(0,tokens.size);

        // embedded with just one variable
        sequence = new Sequence();
        tokens = eq.extractTokens("(((A)))",managerTemp);
        eq.handleParentheses(tokens,sequence);
        assertEquals(0,sequence.operations.size());
        assertEquals(1,tokens.size);
        assertTrue(tokens.first.getType() == Type.VARIABLE);

        // pointless
        sequence = new Sequence();
        tokens = eq.extractTokens("((A)*(B)+(C))",managerTemp);
        eq.handleParentheses(tokens,sequence);
        assertEquals(2,sequence.operations.size());
        assertEquals(1,tokens.size);
        assertTrue(tokens.first.getType() == Type.VARIABLE);
    }

    @Test
    public void parseOperations() {
        Equation eq = new Equation();
        ManagerTempVariables managerTemp = new ManagerTempVariables();
        eq.functions.setManagerTemp( managerTemp );

        eq.alias(new DenseMatrix64F(1, 1), "A");
        eq.alias(new DenseMatrix64F(1, 1), "B");
        eq.alias(new DenseMatrix64F(1, 1), "C");

        // give it an empty list
        TokenList tokens = eq.extractTokens("",managerTemp);
        Sequence sequence = new Sequence();

        eq.parseOperationsLR(new Symbol[]{Symbol.TIMES}, tokens, sequence);
        assertEquals(0,sequence.operations.size());
        assertEquals(0,tokens.size);

        // other cases
        tokens = eq.extractTokens("B+B-A*B*A",managerTemp);
        sequence = new Sequence();

        eq.parseOperationsLR(new Symbol[]{Symbol.TIMES}, tokens, sequence);

        assertEquals(2,sequence.operations.size());
        assertEquals(5,tokens.size);
        assertTrue(tokens.last.getType() == Type.VARIABLE);
        assertTrue(Symbol.MINUS==tokens.last.previous.getSymbol());

        tokens = eq.extractTokens("B+B*B*A-B",managerTemp);
        sequence = new Sequence();

        eq.parseOperationsLR(new Symbol[]{Symbol.PLUS, Symbol.MINUS}, tokens, sequence);

        assertEquals(2,sequence.operations.size());
        assertEquals(5,tokens.size);
        assertTrue(tokens.last.getType() == Type.VARIABLE);
        assertTrue(Symbol.TIMES == tokens.last.previous.getSymbol());
        assertTrue(Symbol.TIMES == tokens.first.next.next.next.getSymbol());
    }

    @Test
    public void createOp() {
        Equation eq = new Equation();
        ManagerTempVariables managerTemp = new ManagerTempVariables();
        eq.functions.setManagerTemp( managerTemp );

        eq.alias(new DenseMatrix64F(1, 1), "A");
        eq.alias(new DenseMatrix64F(1, 1), "B");

        TokenList tokens = eq.extractTokens("A=A*B",managerTemp);

        TokenList.Token t0 = tokens.first.next.next;
        TokenList.Token t1 = t0.next;
        TokenList.Token t2 = t1.next;

        Sequence sequence = new Sequence();

        TokenList.Token found = eq.createOp(t0,t1,t2,tokens,sequence);
        assertTrue(found.getType() == Type.VARIABLE);
        assertEquals(3, tokens.size);
        assertTrue(Symbol.ASSIGN == tokens.first.next.getSymbol());
        assertTrue(found==tokens.last);
        assertEquals(1, sequence.operations.size());
    }

    @Test
    public void lookupVariable() {
        Equation eq = new Equation();
        eq.alias(new DenseMatrix64F(1,1),"A");
        eq.alias(new DenseMatrix64F(1,1),"BSD");

        eq.lookupVariable("A");
        eq.lookupVariable("BSD");
        assertTrue( null == eq.lookupVariable("dDD") );
    }

    @Test
    public void extractTokens() {
        Equation eq = new Equation();
        ManagerTempVariables managerTemp = new ManagerTempVariables();

        eq.alias(new DenseMatrix64F(1,1),"A");
        eq.alias(new DenseMatrix64F(1,1),"BSD");

        Variable v0 = eq.lookupVariable("A");
        Variable v1 = eq.lookupVariable("BSD");


        TokenList list = eq.extractTokens("A = A*A + BSD*(A+BSD) -A*BSD",managerTemp);

        TokenList.Token t = list.getFirst();

        assertTrue(v0==t.getVariable()); t = t.next;
        assertTrue(Symbol.ASSIGN==t.getSymbol()); t = t.next;
        assertTrue(v0==t.getVariable()); t = t.next;
        assertTrue(Symbol.TIMES==t.getSymbol()); t = t.next;
        assertTrue(v0==t.getVariable()); t = t.next;
        assertTrue(Symbol.PLUS==t.getSymbol()); t = t.next;
        assertTrue(v1==t.getVariable()); t = t.next;
        assertTrue(Symbol.TIMES==t.getSymbol()); t = t.next;
        assertTrue(Symbol.PAREN_LEFT==t.getSymbol()); t = t.next;
        assertTrue(v0==t.getVariable()); t = t.next;
        assertTrue(Symbol.PLUS==t.getSymbol()); t = t.next;
        assertTrue(v1==t.getVariable()); t = t.next;
        assertTrue(Symbol.PAREN_RIGHT==t.getSymbol()); t = t.next;
        assertTrue(Symbol.MINUS==t.getSymbol()); t = t.next;
        assertTrue(v0==t.getVariable()); t = t.next;
        assertTrue(Symbol.TIMES==t.getSymbol()); t = t.next;
        assertTrue(v1==t.getVariable()); t = t.next;
    }

    @Test
    public void extractTokens_elementWise() {
        Equation eq = new Equation();
        ManagerTempVariables managerTemp = new ManagerTempVariables();

        eq.alias(new DenseMatrix64F(1,1),"A");
        eq.alias(new DenseMatrix64F(1,1),"BSD");

        Variable v0 = eq.lookupVariable("A");
        Variable v1 = eq.lookupVariable("BSD");


        TokenList list = eq.extractTokens("A = (A.*A)./BSD",managerTemp);

        TokenList.Token t = list.getFirst();

        assertTrue(v0==t.getVariable()); t = t.next;
        assertTrue(Symbol.ASSIGN==t.getSymbol()); t = t.next;
        assertTrue(Symbol.PAREN_LEFT==t.getSymbol()); t = t.next;
        assertTrue(v0==t.getVariable()); t = t.next;
        assertTrue(Symbol.ELEMENT_TIMES==t.getSymbol()); t = t.next;
        assertTrue(v0==t.getVariable()); t = t.next;
        assertTrue(Symbol.PAREN_RIGHT==t.getSymbol()); t = t.next;
        assertTrue(Symbol.ELEMENT_DIVIDE==t.getSymbol()); t = t.next;
        assertTrue(v1==t.getVariable()); t = t.next;
    }

    @Test
    public void extractTokens_integers() {
        Equation eq = new Equation();
        ManagerTempVariables managerTemp = new ManagerTempVariables();

        eq.alias(new DenseMatrix64F(1,1),"A");
        eq.alias(new DenseMatrix64F(1,1),"BSD");

        Variable v0 = eq.lookupVariable("A");
        Variable v1 = eq.lookupVariable("BSD");

        TokenList list = eq.extractTokens("A*2 + 345 + 56*BSD*934",managerTemp);

        TokenList.Token t = list.getFirst();

        assertTrue(v0 == t.getVariable()); t = t.next;
        assertTrue(Symbol.TIMES == t.getSymbol()); t = t.next;
        assertEquals(2, ((VariableInteger) t.getVariable()).value); t = t.next;
        assertTrue(Symbol.PLUS == t.getSymbol()); t = t.next;
        assertEquals(345, ((VariableInteger) t.getVariable()).value); t = t.next;
        assertTrue(Symbol.PLUS==t.getSymbol()); t = t.next;
        assertEquals(56, ((VariableInteger) t.getVariable()).value); t = t.next;
        assertTrue(Symbol.TIMES==t.getSymbol()); t = t.next;
        assertTrue(v1==t.getVariable()); t = t.next;
        assertTrue(Symbol.TIMES==t.getSymbol()); t = t.next;
        assertEquals(934, ((VariableInteger) t.getVariable()).value); t = t.next;
    }

    @Test
    public void extractTokens_doubles() {
        Equation eq = new Equation();
        ManagerTempVariables managerTemp = new ManagerTempVariables();

        eq.alias(new DenseMatrix64F(1,1),"A");
        eq.alias(new DenseMatrix64F(1,1),"BSD");

        Variable v0 = eq.lookupVariable("A");
        Variable v1 = eq.lookupVariable("BSD");

        TokenList list = eq.extractTokens("A*2. + 345.034 + 0.123*BSD*5.1",managerTemp);

        TokenList.Token t = list.getFirst();

        assertTrue(v0==t.getVariable()); t = t.next;
        assertTrue(Symbol.TIMES==t.getSymbol()); t = t.next;
        assertTrue(2 == ((VariableDouble) t.getVariable()).value); t = t.next;
        assertTrue(Symbol.PLUS==t.getSymbol()); t = t.next;
        assertTrue(345.034 == ((VariableDouble) t.getVariable()).value); t = t.next;
        assertTrue(Symbol.PLUS==t.getSymbol()); t = t.next;
        assertTrue(0.123 == ((VariableDouble) t.getVariable()).value); t = t.next;
        assertTrue(Symbol.TIMES==t.getSymbol()); t = t.next;
        assertTrue(v1==t.getVariable()); t = t.next;
        assertTrue(Symbol.TIMES==t.getSymbol()); t = t.next;
        assertTrue(5.1==((VariableDouble)t.getVariable()).value); t = t.next;
    }

    @Test
    public void isTargetOp() {
        Symbol[] targets = new Symbol[]{Symbol.PERIOD,Symbol.TIMES,Symbol.TRANSPOSE};
        assertTrue(Equation.isTargetOp(new TokenList.Token(Symbol.TIMES),targets));
        assertFalse(Equation.isTargetOp(new TokenList.Token(Symbol.DIVIDE), targets));
    }

    @Test
    public void isLetter() {
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

}
