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

package org.ejml.simple;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.row.CommonOps_R64;
import org.ejml.dense.row.NormOps_R64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestSimpleMatrix {

    Random rand = new Random(76343);

    @Test
    public void randomNormal() {
        SimpleMatrix Q = SimpleMatrix.diag(5,3,12);
        Q.set(0,1,0.5);
        Q.set(1,0,0.5);

        int N = 200;
        double sum[] = new double[3];
        for (int i = 0; i < N; i++) {
            SimpleMatrix x = SimpleMatrix.randomNormal(Q,rand);

            for (int j = 0; j < x.getNumElements(); j++) {
                sum[j] += x.get(j);
            }
        }
        for (int i = 0; i < sum.length; i++) {
            sum[i] /= N;
            assertTrue(sum[i]!=0);
            assertEquals(0,sum[i],0.3);
        }
    }

    @Test
    public void constructor_1d_array() {
        double d[] = new double[]{2,5,3,9,-2,6,7,4};
        SimpleMatrix s = new SimpleMatrix(3,2, true, d);
        DMatrixRow_F64 m = new DMatrixRow_F64(3,2, true, d);

        EjmlUnitTests.assertEquals((DMatrixRow_F64)m,(DMatrixRow_F64)s.getMatrix(), UtilEjml.TEST_F64);
    }

    @Test
    public void constructor_2d_array() {
        double d[][] = new double[][]{{1,2},{3,4},{5,6}};

        SimpleMatrix s = new SimpleMatrix(d);
        DMatrixRow_F64 mat = new DMatrixRow_F64(d);

        EjmlUnitTests.assertEquals((DMatrixRow_F64)mat,(DMatrixRow_F64)s.getMatrix(),UtilEjml.TEST_F64);
    }

    @Test
    public void constructor_dense() {
        DMatrixRow_F64 mat = RandomMatrices_R64.createRandom(3,2,rand);
        SimpleMatrix s = new SimpleMatrix(mat);

        assertTrue( mat != s.getMatrix() );
        EjmlUnitTests.assertEquals(mat,s.getMatrix());
    }

    @Test
    public void constructor_simple() {
        SimpleMatrix orig = SimpleMatrix.random_F64(3,2, 0, 1, rand);
        SimpleMatrix copy = new SimpleMatrix(orig);

        assertTrue(orig.mat != copy.mat);
        EjmlUnitTests.assertEquals(orig.mat,copy.mat);
    }

    @Test
    public void wrap() {
        DMatrixRow_F64 mat = RandomMatrices_R64.createRandom(3,2,rand);

        SimpleMatrix s = SimpleMatrix.wrap(mat);

        assertTrue(s.mat == mat);
    }

    @Test
    public void identity() {
        SimpleMatrix s = SimpleMatrix.identity(3);

        DMatrixRow_F64 d = CommonOps_R64.identity(3);

        EjmlUnitTests.assertEquals(d,(DMatrixRow_F64)s.mat,UtilEjml.TEST_F64);
    }

    @Test
    public void getMatrix() {
        SimpleMatrix s = new SimpleMatrix(3,2);

        // make sure a new instance isn't returned
        assertTrue(s.mat == s.getMatrix());
    }

    @Test
    public void transpose() {
        SimpleMatrix orig = SimpleMatrix.random_F64(3,2, 0, 1, rand);
        SimpleMatrix tran = orig.transpose();

        DMatrixRow_F64 dTran = new DMatrixRow_F64(2,3);
        CommonOps_R64.transpose((DMatrixRow_F64)orig.mat,dTran);

        EjmlUnitTests.assertEquals(dTran,(DMatrixRow_F64)tran.mat,UtilEjml.TEST_F64);
    }

    @Test
    public void mult() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,2, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random_F64(2,3, 0, 1, rand);
        SimpleMatrix c = a.mult(b);

        DMatrixRow_F64 c_dense = new DMatrixRow_F64(3,3);
        CommonOps_R64.mult((DMatrixRow_F64)a.mat,(DMatrixRow_F64)b.mat,c_dense);

        EjmlUnitTests.assertEquals(c_dense,(DMatrixRow_F64)c.mat,UtilEjml.TEST_F64);
    }

    @Test
    public void kron() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,2, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random_F64(2,3, 0, 1, rand);
        SimpleMatrix c = a.kron(b);

        DMatrixRow_F64 c_dense = new DMatrixRow_F64(6,6);
        CommonOps_R64.kron((DMatrixRow_F64)a.getMatrix(),(DMatrixRow_F64)b.getMatrix(),c_dense);

        EjmlUnitTests.assertEquals(c_dense,c.mat);
    }

//    @Test
//    public void mult_trans() {
//        SimpleMatrix a = SimpleMatrix.random(3,2,rand);
//        SimpleMatrix b = SimpleMatrix.random(2,3,rand);
//        SimpleMatrix c;
//
//        DMatrixRow_F64 c_dense = new DMatrixRow_F64(3,3);
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

    @Test
    public void plus() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,2, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random_F64(3,2, 0, 1, rand);
        SimpleMatrix c = a.plus(b);

        DMatrixRow_F64 c_dense = new DMatrixRow_F64(3,2);
        CommonOps_R64.add((DMatrixRow_F64)a.mat,(DMatrixRow_F64)b.mat,c_dense);

        EjmlUnitTests.assertEquals(c_dense,c.mat);
    }

    @Test
    public void plus_scalar() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,2, 0, 1, rand);
        double b = 2.5;
        SimpleMatrix c = a.plus(b);

        DMatrixRow_F64 c_dense = new DMatrixRow_F64(3,2);
        CommonOps_R64.add((DMatrixRow_F64)a.mat,b,c_dense);

        EjmlUnitTests.assertEquals(c_dense,c.mat);
    }

    @Test
    public void dot() {
        SimpleMatrix a = SimpleMatrix.random_F64(10,1,-1,1,rand);
        SimpleMatrix b = SimpleMatrix.random_F64(10,1,-1,1,rand);

        double expected = 0;
        for( int i = 0; i < 10; i++ )
            expected += a.get(i)*b.get(i);

        double found = a.dot(b);

        assertEquals(expected,found,UtilEjml.TEST_F64);
    }

    @Test
    public void isVector() {
        assertTrue(new SimpleMatrix(1,1).isVector());
        assertTrue(new SimpleMatrix(1,10).isVector());
        assertTrue(new SimpleMatrix(10,1).isVector());
        assertFalse(new SimpleMatrix(6,5).isVector());
    }

    @Test
    public void minus_matrix_matrix() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,2, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random_F64(3,2, 0, 1, rand);
        SimpleMatrix c = a.minus(b);

        DMatrixRow_F64 c_dense = new DMatrixRow_F64(3,2);
        CommonOps_R64.subtract((DMatrixRow_F64)a.mat, (DMatrixRow_F64)b.mat, c_dense);

        EjmlUnitTests.assertEquals(c_dense,c.mat);
    }

    @Test
    public void minus_matrix_scalar() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,2, 0, 1, rand);
        double b = 0.14;
        SimpleMatrix c = a.minus(b);

        DMatrixRow_F64 c_dense = new DMatrixRow_F64(3,2);
        CommonOps_R64.subtract((DMatrixRow_F64)a.mat, b, c_dense);

        EjmlUnitTests.assertEquals(c_dense,c.mat);
    }

    @Test
    public void plus_beta() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,2, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random_F64(3,2, 0, 1, rand);
        SimpleMatrix c = a.plus(2.5, b);

        DMatrixRow_F64 c_dense = new DMatrixRow_F64(3,2);
        CommonOps_R64.add((DMatrixRow_F64)a.mat, 2.5, (DMatrixRow_F64)b.mat, c_dense);

        EjmlUnitTests.assertEquals(c_dense,c.mat);
    }

    @Test
    public void invert() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,3, 0, 1, rand);
        SimpleMatrix inv = a.invert();

        DMatrixRow_F64 d_inv = new DMatrixRow_F64(3,3);
        CommonOps_R64.invert((DMatrixRow_F64)a.mat,d_inv);

        EjmlUnitTests.assertEquals(d_inv,inv.mat);
    }

    @Test
    public void invert_NaN_INFINITY() {
        SimpleMatrix a = new SimpleMatrix(3,3);
        try {
            a.set(Double.NaN);
            a.invert();
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            a.set(Double.POSITIVE_INFINITY);
            a.invert();
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    @Test
    public void pseudoInverse() {
        // first test it against a non-square zero matrix
        SimpleMatrix inv = new SimpleMatrix(3,4).pseudoInverse();
        assertEquals(0,inv.normF(),UtilEjml.TEST_F64);

        // now try it against a more standard matrix
        SimpleMatrix a = SimpleMatrix.random_F64(3,3, 0, 1, rand);
        inv = a.pseudoInverse();

        DMatrixRow_F64 d_inv = new DMatrixRow_F64(3,3);
        CommonOps_R64.invert((DMatrixRow_F64)a.mat,d_inv);

        EjmlUnitTests.assertEquals(d_inv,inv.mat);
    }

    @Test
    public void solve() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,3, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random_F64(3,2, 0, 1, rand);
        SimpleMatrix c = a.solve(b);

        DMatrixRow_F64 c_dense = new DMatrixRow_F64(3,2);
        CommonOps_R64.solve((DMatrixRow_F64)a.mat,(DMatrixRow_F64)b.mat,c_dense);

        EjmlUnitTests.assertEquals(c_dense,c.mat);
    }

    @Test
    public void solve_NaN_INFINITY() {
        SimpleMatrix a = new SimpleMatrix(3,3);
        SimpleMatrix b = SimpleMatrix.random_F64(3,2, 0, 1, rand);
        try {
            a.set(Double.NaN);
            a.solve(b);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            a.set(Double.POSITIVE_INFINITY);
            a.solve(b);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    /**
     * See if it solves an over determined system correctly
     */
    @Test
    public void solve_notsquare() {
        SimpleMatrix a = SimpleMatrix.random_F64(6,3, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random_F64(6,2, 0, 1, rand);
        SimpleMatrix c = a.solve(b);

        DMatrixRow_F64 c_dense = new DMatrixRow_F64(3,2);
        CommonOps_R64.solve((DMatrixRow_F64)a.mat,(DMatrixRow_F64)b.mat,c_dense);

        EjmlUnitTests.assertEquals(c_dense,c.mat);
    }

    @Test
    public void set_double() {
        SimpleMatrix a = new SimpleMatrix(3,3);
        a.set(16.0);

        DMatrixRow_F64 d = new DMatrixRow_F64(3,3);
        CommonOps_R64.fill(d, 16.0);

        EjmlUnitTests.assertEquals(d,a.mat);
    }

    @Test
    public void zero() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,3, 0, 1, rand);
        a.zero();

        DMatrixRow_F64 d = new DMatrixRow_F64(3,3);

        EjmlUnitTests.assertEquals(d,a.mat);
    }

    @Test
    public void normF() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,3, 0, 1, rand);

        double norm = a.normF();
        double dnorm = NormOps_R64.fastNormF((DMatrixRow_F64)a.mat);

        assertEquals(dnorm,norm,1e-10);
    }

    @Test
    public void conditionP2() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,3, 0, 1, rand);

        double cond = NormOps_R64.conditionP2((DMatrixRow_F64)a.getMatrix());
        double found = a.conditionP2();

        assertTrue(cond == found);
    }

    @Test
    public void determinant() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,3, 0, 1, rand);

        double det = a.determinant();
        double ddet = CommonOps_R64.det((DMatrixRow_F64)a.mat);

        assertEquals(ddet,det,1e-10);
    }

    @Test
    public void trace() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,3, 0, 1, rand);

        double trace = a.trace();
        double dtrace = CommonOps_R64.trace((DMatrixRow_F64)a.mat);

        assertEquals(dtrace,trace,1e-10);
    }

    @Test
    public void reshape() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,3, 0, 1, rand);
        DMatrixRow_F64 b = a.mat.copy();

        a.reshape(2,3);
        b.reshape(2,3, false);

        EjmlUnitTests.assertEquals(b,a.mat);
    }

    @Test
    public void set_element() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,3, 0, 1, rand);
        a.set(0,1,10.3);

        assertEquals(10.3,a.get(0,1),1e-6);
    }

    @Test
    public void setRow() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,3, 0, 1, rand);
        a.setRow(2,1,2,3);

        assertEquals(2,a.get(2,1),1e-6);
        assertEquals(3,a.get(2,2),1e-6);
    }

    @Test
    public void setColumn() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,3, 0, 1, rand);
        a.setColumn(2,1,2,3);

        assertEquals(2,a.get(1,2),1e-6);
        assertEquals(3,a.get(2,2),1e-6);
    }

    @Test
    public void get_2d() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,3, 0, 1, rand);

        assertEquals(((DMatrixRow_F64)a.mat).get(0,1),a.get(0,1),1e-6);
    }

    @Test
    public void get_1d() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,3, 0, 1, rand);

        assertEquals(((DMatrixRow_F64)a.mat).get(3),a.get(3),1e-6);
    }

    @Test
    public void getIndex() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,3, 0, 1, rand);

        assertEquals(((DMatrixRow_F64)a.mat).getIndex(0,2),a.getIndex(0,2),1e-6);
    }

    @Test
    public void copy() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,3, 0, 1, rand);
        SimpleMatrix b = a.copy();

        assertTrue(a.mat!=b.mat);
        EjmlUnitTests.assertEquals(b.mat,a.mat);
    }

    @Test
    public void svd() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,4, 0, 1, rand);

        SimpleSVD<SimpleMatrix> svd = a.svd();

        SimpleMatrix U = svd.getU();
        SimpleMatrix W = svd.getW();
        SimpleMatrix V = svd.getV();

        SimpleMatrix a_found = U.mult(W).mult(V.transpose());

        EjmlUnitTests.assertEquals(a.mat,a_found.mat);
    }

    @Test
    public void eig() {
        SimpleMatrix a = SimpleMatrix.random_F64(4,4, 0, 1, rand);

        SimpleEVD evd = a.eig();

        assertEquals(4,evd.getNumberOfEigenvalues());

        for( int i = 0; i < 4; i++ ) {
            Complex_F64 c = evd.getEigenvalue(i);
            assertTrue(c != null );
            evd.getEigenVector(i);
        }
    }

    @Test
    public void insertIntoThis() {
        SimpleMatrix A = new SimpleMatrix(6,4);
        SimpleMatrix B = SimpleMatrix.random_F64(3,2, 0, 1, rand);

        DMatrixRow_F64 A_ = A.getMatrix().copy();

        A.insertIntoThis(1,2,B);

        CommonOps_R64.insert((DMatrixRow_F64)B.getMatrix(), A_, 1,2);

        EjmlUnitTests.assertEquals(A_,A.getMatrix());
    }

    @Test
    public void combine() {
        SimpleMatrix A = new SimpleMatrix(6,4);
        SimpleMatrix B = SimpleMatrix.random_F64(3,4, 0, 1, rand);

        SimpleMatrix C = A.combine(2,2,B);

        assertEquals(6,C.numRows());
        assertEquals(6,C.numCols());

        for( int i = 0; i < 6; i++ ) {
            for( int j = 0; j < 6; j++ ) {
                if( i >= 2 && i < 5 && j >= 2 && j < 6 ) {
                    // check to see if B was overlayed
                    assertTrue( B.get(i-2,j-2) == C.get(i,j));
                } else if( i >= 5 || j >= 4 ) {
                    // check zero padding
                    assertTrue( C.get(i,j) == 0 );
                } else {
                    // see if the parts of A remain there
                    assertTrue( A.get(i,j) == C.get(i,j));
                }
            }
        }
    }

    @Test
    public void scale() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,2, 0, 1, rand);
        SimpleMatrix b = a.scale(1.5);

        for( int i = 0; i < a.numRows(); i++ ) {
            for( int j = 0; j < a.numCols(); j++ ) {
                assertEquals( a.get(i,j)*1.5 , b.get(i,j),1e-10);
            }
        }
    }

    @Test
    public void div_scalar() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,2, 0, 1, rand);
        SimpleMatrix b = a.divide(1.5);

        for( int i = 0; i < a.numRows(); i++ ) {
            for( int j = 0; j < a.numCols(); j++ ) {
                assertEquals( a.get(i,j)/1.5 , b.get(i,j),1e-10);
            }
        }
    }

    @Test
    public void elementSum() {
        SimpleMatrix a = new SimpleMatrix(7,4);

        double expectedSum = 0;

        int index = 0;
        for( int i = 0; i < a.numRows(); i++ ) {
            for( int j = 0; j < a.numCols(); j++ , index++ ) {
                expectedSum += index;
                a.set(i,j,index);
            }
        }

        assertEquals( expectedSum , a.elementSum() , UtilEjml.TEST_F64);
    }

    @Test
    public void elementMaxAbs() {
        SimpleMatrix a = SimpleMatrix.random_F64(7,5, 0, 1, rand);

        a.set(3,4,-5);
        a.set(4,4,4);

        assertTrue(5 == a.elementMaxAbs());
    }

    @Test
    public void elementMult() {
        SimpleMatrix A = SimpleMatrix.random_F64(4,5,-1,1,rand);
        SimpleMatrix B = SimpleMatrix.random_F64(4,5,-1,1,rand);

        SimpleMatrix C = A.elementMult(B);

        for( int i = 0; i < A.numRows(); i++ ) {
            for( int j = 0; j < A.numCols(); j++ ) {
                double expected = A.get(i,j)*B.get(i,j);

                assertTrue(expected == C.get(i,j));
            }
        }
    }

    @Test
    public void elementDiv() {
        SimpleMatrix A = SimpleMatrix.random_F64(4,5,-1,1,rand);
        SimpleMatrix B = SimpleMatrix.random_F64(4,5,-1,1,rand);

        SimpleMatrix C = A.elementDiv(B);

        for( int i = 0; i < A.numRows(); i++ ) {
            for( int j = 0; j < A.numCols(); j++ ) {
                double expected = A.get(i,j)/B.get(i,j);

                assertTrue(expected == C.get(i,j));
            }
        }
    }

    @Test
    public void elementPower_m() {
        SimpleMatrix A = SimpleMatrix.random_F64(4,5,0,1,rand);
        SimpleMatrix B = SimpleMatrix.random_F64(4,5,0,1,rand);

        SimpleMatrix C = A.elementPower(B);

        for( int i = 0; i < A.numRows(); i++ ) {
            for( int j = 0; j < A.numCols(); j++ ) {
                double expected = Math.pow(A.get(i,j),B.get(i,j));

                assertTrue(expected == C.get(i,j));
            }
        }
    }

    @Test
    public void elementPower_s() {
        SimpleMatrix A = SimpleMatrix.random_F64(4,5,0,1,rand);
        double b = 1.1;

        SimpleMatrix C = A.elementPower(b);

        for( int i = 0; i < A.numRows(); i++ ) {
            for( int j = 0; j < A.numCols(); j++ ) {
                double expected = Math.pow(A.get(i,j),b);

                assertTrue(expected == C.get(i,j));
            }
        }
    }

    @Test
    public void elementLog() {
        SimpleMatrix A = SimpleMatrix.random_F64(4,5,0,1,rand);

        SimpleMatrix C = A.elementLog();

        for( int i = 0; i < A.numRows(); i++ ) {
            for( int j = 0; j < A.numCols(); j++ ) {
                double expected = Math.log(A.get(i, j));

                assertTrue(expected == C.get(i,j));
            }
        }
    }

    @Test
    public void elementExp() {
        SimpleMatrix A = SimpleMatrix.random_F64(4,5,0,1,rand);

        SimpleMatrix C = A.elementExp();

        for( int i = 0; i < A.numRows(); i++ ) {
            for( int j = 0; j < A.numCols(); j++ ) {
                double expected = Math.exp(A.get(i, j));

                assertTrue(expected == C.get(i,j));
            }
        }
    }

    @Test
    public void extractMatrix() {
        SimpleMatrix a = SimpleMatrix.random_F64(7,5, 0, 1, rand);

        SimpleMatrix b = a.extractMatrix(2,5,3,5);

        for( int i = 2; i <= 4; i++ ) {
            for( int j = 3; j <= 4; j++ ) {
                double expected = a.get(i,j);
                double found = b.get(i-2,j-3);

                assertTrue( expected == found );
            }
        }
    }

    @Test
    public void extractDiag() {
        SimpleMatrix a = SimpleMatrix.random_F64(3,4, 0, 1, rand);

        DMatrixRow_F64 found = a.extractDiag().getMatrix();
        DMatrixRow_F64 expected = new DMatrixRow_F64(3,1);

        CommonOps_R64.extractDiag((DMatrixRow_F64)a.getMatrix(),expected);

        EjmlUnitTests.assertEquals(found,expected);
    }

    @Test
    public void extractVector() {
        SimpleMatrix A = SimpleMatrix.random_F64(10,7, 0, 1, rand);

        SimpleMatrix c = A.extractVector(false,2);
        SimpleMatrix r = A.extractVector(true,2);

        assertEquals(A.numCols(),r.numCols());
        assertEquals(1,r.numRows());
        assertEquals(A.numRows(),c.numRows());
        assertEquals(1,c.numCols());

        for( int i = 0; i < A.numCols(); i++ ) {
            assertEquals(A.get(2,i),r.get(i),1e-10);
        }

        for( int i = 0; i < A.numRows(); i++ ) {
            assertEquals(A.get(i,2),c.get(i),1e-10);
        }
    }

    @Test
    public void negative() {
        SimpleMatrix A = SimpleMatrix.random_F64(5,7,-1,1,rand);

        SimpleMatrix A_neg = A.negative();

        double value = A.plus(A_neg).normF();

        assertEquals(0,value,UtilEjml.TEST_F64);
    }

    @Test
    public void isInBounds() {
        SimpleMatrix A = new SimpleMatrix(10,15);

        assertTrue(A.isInBounds(0,0));
        assertTrue(A.isInBounds(9,0));
        assertTrue(A.isInBounds(0,14));
        assertTrue(A.isInBounds(3,3));

        assertFalse(A.isInBounds(-1,0));
        assertFalse(A.isInBounds(0,-1));
        assertFalse(A.isInBounds(10,0));
        assertFalse(A.isInBounds(0,15));
        assertFalse(A.isInBounds(3,1000));
    }
}
