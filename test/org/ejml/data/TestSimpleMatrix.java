/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.data;

import org.ejml.ops.CommonOps;
import org.ejml.ops.NormOps;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestSimpleMatrix {

    Random rand = new Random(76343);

    @Test
    public void constructor_1d_array() {
        double d[] = new double[]{2,5,3,9,-2,6,7,4};
        SimpleMatrix s = new SimpleMatrix(3,2,d);
        DenseMatrix64F m = new DenseMatrix64F(3,2, true, d);

        UtilTestMatrix.checkEquals(m,s.getMatrix());
    }

    @Test
    public void constructor_2d_array() {
        double d[][] = new double[][]{{1,2},{3,4},{5,6}};

        SimpleMatrix s = new SimpleMatrix(d);
        DenseMatrix64F mat = new DenseMatrix64F(d);

        UtilTestMatrix.checkEquals(mat,s.getMatrix());
    }

    @Test
    public void constructor_dense() {
        DenseMatrix64F mat = RandomMatrices.createRandom(3,2,rand);
        SimpleMatrix s = new SimpleMatrix(mat);

        assertTrue( mat != s.getMatrix() );
        UtilTestMatrix.checkEquals(mat,s.getMatrix());
    }

    @Test
    public void constructor_simple() {
        SimpleMatrix orig = SimpleMatrix.random(3,2, 0, 1, rand);
        SimpleMatrix copy = new SimpleMatrix(orig);

        assertTrue(orig.mat != copy.mat);
        UtilTestMatrix.checkEquals(orig.mat,copy.mat);
    }

    @Test
    public void wrap() {
        DenseMatrix64F mat = RandomMatrices.createRandom(3,2,rand);

        SimpleMatrix s = SimpleMatrix.wrap(mat);

        assertTrue(s.mat == mat);
    }

    @Test
    public void identity() {
        SimpleMatrix s = SimpleMatrix.identity(3);

        DenseMatrix64F d = CommonOps.identity(3);

        UtilTestMatrix.checkEquals(d,s.mat);
    }

    @Test
    public void getMatrix() {
        SimpleMatrix s = new SimpleMatrix(3,2);

        // make sure a new instance isn't returned
        assertTrue(s.mat == s.getMatrix());
    }

    @Test
    public void transpose() {
        SimpleMatrix orig = SimpleMatrix.random(3,2, 0, 1, rand);
        SimpleMatrix tran = orig.transpose();

        DenseMatrix64F dTran = new DenseMatrix64F(2,3);
        CommonOps.transpose(orig.mat,dTran);

        UtilTestMatrix.checkEquals(dTran,tran.mat);
    }

    @Test
    public void mult() {
        SimpleMatrix a = SimpleMatrix.random(3,2, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random(2,3, 0, 1, rand);
        SimpleMatrix c = a.mult(b);

        DenseMatrix64F c_dense = new DenseMatrix64F(3,3);
        CommonOps.mult(a.mat,b.mat,c_dense);

        UtilTestMatrix.checkEquals(c_dense,c.mat);
    }

    @Test
    public void kron() {
        SimpleMatrix a = SimpleMatrix.random(3,2, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random(2,3, 0, 1, rand);
        SimpleMatrix c = a.kron(b);

        DenseMatrix64F c_dense = new DenseMatrix64F(6,6);
        CommonOps.kron(a.getMatrix(),b.getMatrix(),c_dense);

        UtilTestMatrix.checkEquals(c_dense,c.mat);
    }

//    @Test
//    public void mult_trans() {
//        SimpleMatrix a = SimpleMatrix.random(3,2,rand);
//        SimpleMatrix b = SimpleMatrix.random(2,3,rand);
//        SimpleMatrix c;
//
//        DenseMatrix64F c_dense = new DenseMatrix64F(3,3);
//        CommonOps.mult(a.mat,b.mat,c_dense);
//
//        c = a.mult(false,false,b);
//        UtilTestMatrix.checkEquals(c_dense,c.mat);
//        c = a.transpose().mult(true,false,b);
//        UtilTestMatrix.checkEquals(c_dense,c.mat);
//        c = a.mult(false,true,b.transpose());
//        UtilTestMatrix.checkEquals(c_dense,c.mat);
//        c = a.transpose().mult(true,true,b.transpose());
//        UtilTestMatrix.checkEquals(c_dense,c.mat);
//    }

    @Test
    public void plus() {
        SimpleMatrix a = SimpleMatrix.random(3,2, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random(3,2, 0, 1, rand);
        SimpleMatrix c = a.plus(b);

        DenseMatrix64F c_dense = new DenseMatrix64F(3,2);
        CommonOps.add(a.mat,b.mat,c_dense);

        UtilTestMatrix.checkEquals(c_dense,c.mat);
    }


    @Test
    public void minus() {
        SimpleMatrix a = SimpleMatrix.random(3,2, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random(3,2, 0, 1, rand);
        SimpleMatrix c = a.minus(b);

        DenseMatrix64F c_dense = new DenseMatrix64F(3,2);
        CommonOps.sub(a.mat,b.mat,c_dense);

        UtilTestMatrix.checkEquals(c_dense,c.mat);
    }


    @Test
    public void plus_beta() {
        SimpleMatrix a = SimpleMatrix.random(3,2, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random(3,2, 0, 1, rand);
        SimpleMatrix c = a.plus(2.5,b);

        DenseMatrix64F c_dense = new DenseMatrix64F(3,2);
        CommonOps.add(a.mat,2.5,b.mat,c_dense);

        UtilTestMatrix.checkEquals(c_dense,c.mat);
    }

    @Test
    public void invert() {
        SimpleMatrix a = SimpleMatrix.random(3,3, 0, 1, rand);
        SimpleMatrix inv = a.invert();

        DenseMatrix64F d_inv = new DenseMatrix64F(3,3);
        CommonOps.invert(a.mat,d_inv);

         UtilTestMatrix.checkEquals(d_inv,inv.mat);
    }

    @Test
    public void solve() {
        SimpleMatrix a = SimpleMatrix.random(3,3, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random(3,2, 0, 1, rand);
        SimpleMatrix c = a.solve(b);

        DenseMatrix64F c_dense = new DenseMatrix64F(3,2);
        CommonOps.solve(a.mat,b.mat,c_dense);

        UtilTestMatrix.checkEquals(c_dense,c.mat);
    }

    /**
     * See if it solves an over determined system correctly
     */
    @Test
    public void solve_notsquare() {
        SimpleMatrix a = SimpleMatrix.random(6,3, 0, 1, rand);
        SimpleMatrix b = SimpleMatrix.random(6,2, 0, 1, rand);
        SimpleMatrix c = a.solve(b);

        DenseMatrix64F c_dense = new DenseMatrix64F(3,2);
        CommonOps.solve(a.mat,b.mat,c_dense);

        UtilTestMatrix.checkEquals(c_dense,c.mat);
    }

    @Test
    public void set_double() {
        SimpleMatrix a = new SimpleMatrix(3,3);
        a.set(16.0);

        DenseMatrix64F d = new DenseMatrix64F(3,3);
        CommonOps.set(d,16.0);

        UtilTestMatrix.checkEquals(d,a.mat);
    }

    @Test
    public void zero() {
        SimpleMatrix a = SimpleMatrix.random(3,3, 0, 1, rand);
        a.zero();

        DenseMatrix64F d = new DenseMatrix64F(3,3);

        UtilTestMatrix.checkEquals(d,a.mat);
    }

    @Test
    public void normF() {
        SimpleMatrix a = SimpleMatrix.random(3,3, 0, 1, rand);

        double norm = a.normF();
        double dnorm = NormOps.fastNormF(a.mat);

        assertEquals(dnorm,norm,1e-10);
    }

    @Test
    public void conditionP2() {
        SimpleMatrix a = SimpleMatrix.random(3,3, 0, 1, rand);

        double cond = NormOps.conditionP2(a.getMatrix());
        double found = a.conditionP2();

        assertTrue(cond == found);
    }

    @Test
    public void determinant() {
        SimpleMatrix a = SimpleMatrix.random(3,3, 0, 1, rand);

        double det = a.determinant();
        double ddet = CommonOps.det(a.mat);

        assertEquals(ddet,det,1e-10);
    }

    @Test
    public void trace() {
        SimpleMatrix a = SimpleMatrix.random(3,3, 0, 1, rand);

        double trace = a.trace();
        double dtrace = CommonOps.trace(a.mat);

        assertEquals(dtrace,trace,1e-10);
    }

    @Test
    public void reshape() {
        SimpleMatrix a = SimpleMatrix.random(3,3, 0, 1, rand);
        DenseMatrix64F b = a.mat.copy();

        a.reshape(2,3);
        b.reshape(2,3, false);

        UtilTestMatrix.checkEquals(b,a.mat);
    }

    @Test
    public void set_element() {
        SimpleMatrix a = SimpleMatrix.random(3,3, 0, 1, rand);
        a.set(0,1,10.3);

        assertEquals(10.3,a.get(0,1),1e-6);
    }

    @Test
    public void get_2d() {
        SimpleMatrix a = SimpleMatrix.random(3,3, 0, 1, rand);

        assertEquals(a.mat.get(0,1),a.get(0,1),1e-6);
    }

    @Test
    public void get_1d() {
        SimpleMatrix a = SimpleMatrix.random(3,3, 0, 1, rand);

        assertEquals(a.mat.get(3),a.get(3),1e-6);
    }

    @Test
    public void getIndex() {
        SimpleMatrix a = SimpleMatrix.random(3,3, 0, 1, rand);

        assertEquals(a.mat.getIndex(0,2),a.getIndex(0,2),1e-6);
    }

    @Test
    public void copy() {
        SimpleMatrix a = SimpleMatrix.random(3,3, 0, 1, rand);
        SimpleMatrix b = a.copy();

        assertTrue(a.mat!=b.mat);
        UtilTestMatrix.checkEquals(b.mat,a.mat);
    }

    @Test
    public void svd() {
        SimpleMatrix a = SimpleMatrix.random(3,4, 0, 1, rand);

        SimpleMatrix.SVD svd = a.svd();

        SimpleMatrix U = svd.getU();
        SimpleMatrix W = svd.getW();
        SimpleMatrix V = svd.getV();

        SimpleMatrix a_found = U.mult(W).mult(V.transpose());

        UtilTestMatrix.checkEquals(a.mat,a_found.mat);
    }

    @Test
    public void eig() {
        SimpleMatrix a = SimpleMatrix.random(4,4, 0, 1, rand);

        SimpleMatrix.EVD evd = a.eig();

        assertEquals(4,evd.getNumberOfEigenvalues());

        for( int i = 0; i < 4; i++ ) {
            Complex64F c = evd.getEigenvalue(i);
            assertTrue(c != null );
            evd.getEigenVector(i);
        }
    }

    @Test
    public void insertIntoThis() {
        SimpleMatrix A = new SimpleMatrix(6,4);
        SimpleMatrix B = SimpleMatrix.random(3,2, 0, 1, rand);

        DenseMatrix64F A_ = A.getMatrix().copy();

        A.insertIntoThis(1,2,B);

        CommonOps.insert(B.getMatrix(), A_, 1,2);

        UtilTestMatrix.checkEquals(A_,A.getMatrix());
    }

    @Test
    public void combine() {
        SimpleMatrix A = new SimpleMatrix(6,4);
        SimpleMatrix B = SimpleMatrix.random(3,4, 0, 1, rand);

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
    public void elementMult() {
        SimpleMatrix a = SimpleMatrix.random(3,2, 0, 1, rand);
        SimpleMatrix b = a.elementMult(1.5);

        for( int i = 0; i < a.numRows(); i++ ) {
            for( int j = 0; j < a.numCols(); j++ ) {
                assertEquals( a.get(i,j)*1.5 , b.get(i,j),1e-10);
            }
        }
    }

    @Test
    public void elementDiv() {
        SimpleMatrix a = SimpleMatrix.random(3,2, 0, 1, rand);
        SimpleMatrix b = a.elementDiv(1.5);

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

        assertEquals( expectedSum , a.elementSum() , 1e-8);
    }

    @Test
    public void elementMaxAbs() {
        SimpleMatrix a = SimpleMatrix.random(7,5, 0, 1, rand);

        a.set(3,4,-5);
        a.set(4,4,4);

        assertTrue(5 == a.elementMaxAbs());
    }

    @Test
    public void extractMatrix() {
        SimpleMatrix a = SimpleMatrix.random(7,5, 0, 1, rand);

        SimpleMatrix b = a.extractMatrix(2,4,3,4);

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
        SimpleMatrix a = SimpleMatrix.random(3,4, 0, 1, rand);

        DenseMatrix64F found = a.extractDiag().getMatrix();
        DenseMatrix64F expected = new DenseMatrix64F(3,1);

        CommonOps.extractDiag(a.getMatrix(),expected);

        UtilTestMatrix.checkEquals(found,expected);
    }

    @Test
    public void extractVector() {
        SimpleMatrix A = SimpleMatrix.random(10,7, 0, 1, rand);

        SimpleMatrix c = A.extractVector(false,2);
        SimpleMatrix r = A.extractVector(true,2);

        assertEquals(A.numCols(),r.numRows());
        assertEquals(A.numRows(),c.numRows());

        for( int i = 0; i < A.numCols(); i++ ) {
            assertEquals(A.get(2,i),r.get(i,0),1e-10);
        }

        for( int i = 0; i < A.numRows(); i++ ) {
            assertEquals(A.get(i,2),c.get(i,0),1e-10);
        }
    }
}
