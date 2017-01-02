/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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
import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt_D64;
import org.ejml.alg.dense.linsol.lu.LinearSolverLu_D64;
import org.ejml.alg.dense.mult.CheckMatrixMultShape_D64;
import org.ejml.alg.dense.mult.MatrixMatrixMult_D64;
import org.ejml.data.*;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestCommonOps_D64 {

    Random rand = new Random(0xFF);
    double tol = 1e-8;

    @Test
    public void checkInputShape() {
        CheckMatrixMultShape_D64 check = new CheckMatrixMultShape_D64(CommonOps_D64.class);
        check.checkAll();
    }

    /**
     * Make sure the multiplication methods here have the same behavior as the ones in MatrixMatrixMult.
     */
    @Test
    public void checkAllMatrixMults() {
        int numChecked = 0;
        Method methods[] = CommonOps_D64.class.getMethods();

        boolean oneFailed = false;

        for( Method method : methods ) {
            String name = method.getName();

            // only look at function which perform matrix multiplication
            if( !name.contains("mult") || name.contains("Element") || 
                    name.contains("Inner") || name.contains("Outer"))
                continue;

            boolean hasAlpha = method.getGenericParameterTypes().length==4;

            Method checkMethod = findCheck(name,hasAlpha);

            boolean tranA = false;
            boolean tranB = false;
            if( name.contains("TransAB")) {
                tranA = true;
                tranB = true;
            } else if( name.contains("TransA")) {
                tranA = true;
            } else if( name.contains("TransB")) {
                tranB = true;
            }
//            System.out.println("Function = "+name+"  alpha = "+hasAlpha);

            try {
                if( !checkMultMethod(method,checkMethod,hasAlpha,tranA,tranB) ) {
                    System.out.println("Failed: Function = "+name+"  alpha = "+hasAlpha);
                    oneFailed = true;
                }
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            numChecked++;
        }
        assertEquals(16,numChecked);
        assertTrue(!oneFailed);
    }
    /**
     * See if zeros in rows and columns are handled correctly.
     */
    @Test
    public void checkAllMatrixMult_Zeros() {
        int numChecked = 0;
        Method methods[] = CommonOps_D64.class.getMethods();

        boolean oneFailed = false;

        for( Method method : methods ) {
            String name = method.getName();

            // only look at function which perform matrix multiplication
            if( !name.contains("mult") || name.contains("Element") ||
                    name.contains("Inner") || name.contains("Outer"))
                continue;

            try {

                boolean failed = !checkMultMethod(method,6,0,0,5);
                failed |= !checkMultMethod(method,0,5,5,0);
                failed |= !checkMultMethod(method,1,0,0,5);
                failed |= !checkMultMethod(method,6,0,0,1);
                failed |= !checkMultMethod(method,0,1,1,5);
                failed |= !checkMultMethod(method,5,1,1,0);
                failed |= !checkMultMethod(method,0,0,0,0);

                if( failed ) {
                    System.out.println("Failed: Function = "+name);
                    oneFailed = true;
                }
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            numChecked++;
        }
        assertEquals(16,numChecked);
        assertTrue(!oneFailed);
    }


    private Method findCheck( String name , boolean hasAlpha ) {
        Method checkMethod;
        try {
            if( hasAlpha )
                checkMethod = MatrixMatrixMult_D64.class.getMethod(
                        name,double.class,
                        RowD1Matrix64F.class, RowD1Matrix64F.class,RowD1Matrix64F.class);
            else
                checkMethod = MatrixMatrixMult_D64.class.getMethod(
                        name, RowD1Matrix64F.class, RowD1Matrix64F.class,RowD1Matrix64F.class);
        } catch (NoSuchMethodException e) {
            checkMethod = null;
        }
        if( checkMethod == null ) {
            try {
            if( hasAlpha )
                checkMethod = MatrixMatrixMult_D64.class.getMethod(
                        name+"_reorder",double.class,
                        RowD1Matrix64F.class, RowD1Matrix64F.class,RowD1Matrix64F.class);
            else
                checkMethod = MatrixMatrixMult_D64.class.getMethod(
                        name+"_reorder", RowD1Matrix64F.class, RowD1Matrix64F.class,RowD1Matrix64F.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return checkMethod;
    }

    private boolean checkMultMethod(Method method, Method checkMethod, boolean hasAlpha,
                                    boolean tranA, boolean tranB ) throws InvocationTargetException, IllegalAccessException {


        // check various sizes
        for( int i = 1; i < 40; i++ ) {
            DenseMatrix64F a;
            if( tranA ) a = RandomMatrices_D64.createRandom(i+1,i,rand);
            else  a = RandomMatrices_D64.createRandom(i,i+1,rand);

            DenseMatrix64F b;
            if( tranB ) b = RandomMatrices_D64.createRandom(i,i+1,rand);
            else  b = RandomMatrices_D64.createRandom(i+1,i,rand);

            DenseMatrix64F c = RandomMatrices_D64.createRandom(i,i,rand);
            DenseMatrix64F c_alt = c.copy();

            if( hasAlpha ) {
                method.invoke(null,2.0,a,b,c);
                checkMethod.invoke(null,2.0,a,b,c_alt);
            } else {
                method.invoke(null,a,b,c);
                checkMethod.invoke(null,a,b,c_alt);
            }

            if( !MatrixFeatures_D64.isIdentical(c_alt,c,tol))
                return false;
        }

        // check various sizes column vector
        for( int i = 1; i < 4; i++ ) {
            DenseMatrix64F a;
            if( tranA ) a = RandomMatrices_D64.createRandom(i,i+1,rand);
            else  a = RandomMatrices_D64.createRandom(i+1,i,rand);

            DenseMatrix64F b;
            if( tranB ) b = RandomMatrices_D64.createRandom(1,i,rand);
            else  b = RandomMatrices_D64.createRandom(i,1,rand);

            DenseMatrix64F c = RandomMatrices_D64.createRandom(i+1,1,rand);
            DenseMatrix64F c_alt = c.copy();

            if( hasAlpha ) {
                method.invoke(null,2.0,a,b,c);
                checkMethod.invoke(null,2.0,a,b,c_alt);
            } else {
                method.invoke(null,a,b,c);
                checkMethod.invoke(null,a,b,c_alt);
            }

            if( !MatrixFeatures_D64.isIdentical(c_alt,c,tol))
                return false;
        }

        return true;
    }

    private boolean checkMultMethod(Method method, int rowsA , int colsA , int rowsB , int colsB ) throws InvocationTargetException, IllegalAccessException {

        String name = method.getName();

        boolean tranA = false;
        boolean tranB = false;
        if( name.contains("TransAB")) {
            tranA = true;
            tranB = true;
        } else if( name.contains("TransA")) {
            tranA = true;
        } else if( name.contains("TransB")) {
            tranB = true;
        }

        boolean add = name.contains("Add");
        boolean hasAlpha = method.getGenericParameterTypes().length==4;

        // check length zero rows and columns
        DenseMatrix64F a = tranA ? new DenseMatrix64F(colsA,rowsA) : new DenseMatrix64F(rowsA,colsA);
        DenseMatrix64F b = tranB ? new DenseMatrix64F(colsB,rowsB) : new DenseMatrix64F(rowsB,colsB);

        DenseMatrix64F c = RandomMatrices_D64.createRandom(rowsA,colsB,rand);

        if( hasAlpha ) {
            method.invoke(null,2.0,a,b,c);
        } else {
            method.invoke(null,a,b,c);
        }

        if( add ) {
            DenseMatrix64F corig = c.copy();
            assertTrue(MatrixFeatures_D64.isIdentical(corig, c, UtilEjml.TEST_64F));
        } else {
            assertTrue(MatrixFeatures_D64.isZeros(c, UtilEjml.TEST_64F));
        }

        return true;
    }

    @Test
    public void dot() {
        DenseMatrix64F a = RandomMatrices_D64.createRandom(10, 1, rand);
        DenseMatrix64F b = RandomMatrices_D64.createRandom(1,10,rand);

        double found = CommonOps_D64.dot(a, b);

        double expected = 0;
        for (int i = 0; i < 10; i++) {
            expected += a.data[i]*b.data[i];
        }

        assertEquals(expected, found, UtilEjml.TEST_64F);
    }

    @Test
    public void multInner() {
        DenseMatrix64F a = RandomMatrices_D64.createRandom(10,4,rand);
        DenseMatrix64F found = RandomMatrices_D64.createRandom(4,4,rand);
        DenseMatrix64F expected = RandomMatrices_D64.createRandom(4, 4, rand);

        CommonOps_D64.multTransA(a, a, expected);
        CommonOps_D64.multInner(a,found);

        assertTrue(MatrixFeatures_D64.isIdentical(expected, found, tol));
    }

    @Test
    public void multOuter() {
        DenseMatrix64F a = RandomMatrices_D64.createRandom(10,4,rand);
        DenseMatrix64F found = RandomMatrices_D64.createRandom(10,10,rand);
        DenseMatrix64F expected = RandomMatrices_D64.createRandom(10,10,rand);

        CommonOps_D64.multTransB(a, a, expected);
        CommonOps_D64.multOuter(a, found);

        assertTrue(MatrixFeatures_D64.isIdentical(expected, found, tol));
    }
    
    @Test
    public void elementMult_two() {
        DenseMatrix64F a = RandomMatrices_D64.createRandom(5,4,rand);
        DenseMatrix64F b = RandomMatrices_D64.createRandom(5,4,rand);
        DenseMatrix64F a_orig = a.copy();

        CommonOps_D64.elementMult(a, b);

        for( int i = 0; i < 20; i++ ) {
            assertEquals(a.get(i),b.get(i)*a_orig.get(i),1e-6);
        }
    }

    @Test
    public void elementMult_three() {
        DenseMatrix64F a = RandomMatrices_D64.createRandom(5,4,rand);
        DenseMatrix64F b = RandomMatrices_D64.createRandom(5,4,rand);
        DenseMatrix64F c = RandomMatrices_D64.createRandom(5, 4, rand);

        CommonOps_D64.elementMult(a, b, c);

        for( int i = 0; i < 20; i++ ) {
            assertEquals(c.get(i),b.get(i)*a.get(i),1e-6);
        }
    }

    @Test
    public void elementDiv_two() {
        DenseMatrix64F a = RandomMatrices_D64.createRandom(5,4,rand);
        DenseMatrix64F b = RandomMatrices_D64.createRandom(5,4,rand);
        DenseMatrix64F a_orig = a.copy();

        CommonOps_D64.elementDiv(a, b);

        for( int i = 0; i < 20; i++ ) {
            assertEquals(a.get(i),a_orig.get(i)/b.get(i),1e-6);
        }
    }

    @Test
    public void elementDiv_three() {
        DenseMatrix64F a = RandomMatrices_D64.createRandom(5,4,rand);
        DenseMatrix64F b = RandomMatrices_D64.createRandom(5,4,rand);
        DenseMatrix64F c = RandomMatrices_D64.createRandom(5, 4, rand);

        CommonOps_D64.elementDiv(a, b, c);

        for( int i = 0; i < 20; i++ ) {
            assertEquals(c.get(i),a.get(i)/b.get(i),1e-6);
        }
    }

    @Test
    public void solve() {
        DenseMatrix64F a = new DenseMatrix64F(2,2, true, 1, 2, 7, -3);
        DenseMatrix64F b = RandomMatrices_D64.createRandom(2,5,rand);
        DenseMatrix64F c = RandomMatrices_D64.createRandom(2,5,rand);
        DenseMatrix64F c_exp = RandomMatrices_D64.createRandom(2,5,rand);

        assertTrue(CommonOps_D64.solve(a,b,c));
        LUDecompositionAlt_D64 alg = new LUDecompositionAlt_D64();
        LinearSolverLu_D64 solver = new LinearSolverLu_D64(alg);
        assertTrue(solver.setA(a));

        solver.solve(b, c_exp);

        EjmlUnitTests.assertEquals(c_exp, c, UtilEjml.TEST_64F);
    }

    @Test
    public void transpose_inplace() {
        DenseMatrix64F mat = new DenseMatrix64F(3,3, true, 0, 1, 2, 3, 4, 5, 6, 7, 8);
        DenseMatrix64F matTran = new DenseMatrix64F(3,3);

        CommonOps_D64.transpose(mat, matTran);
        CommonOps_D64.transpose(mat);

        EjmlUnitTests.assertEquals(mat, matTran, UtilEjml.TEST_64F);
    }

    @Test
    public void transpose() {
        DenseMatrix64F mat = new DenseMatrix64F(3,2, true, 0, 1, 2, 3, 4, 5);
        DenseMatrix64F matTran = new DenseMatrix64F(2,3);

        CommonOps_D64.transpose(mat,matTran);

        assertEquals(mat.getNumCols(), matTran.getNumRows());
        assertEquals(mat.getNumRows(), matTran.getNumCols());

        for( int y = 0; y < mat.getNumRows(); y++ ){
            for( int x = 0; x < mat.getNumCols(); x++ ) {
                assertEquals(mat.get(y,x),matTran.get(x,y),1e-6);
            }
        }
    }

    @Test
    public void trace() {
        DenseMatrix64F mat = new DenseMatrix64F(3,3, true, 0, 1, 2, 3, 4, 5, 6, 7, 8);

        assertEquals(12, CommonOps_D64.trace(mat), 1e-6);

        // non square
        DenseMatrix64F B = RandomMatrices_D64.createRandom(4,3,rand);
        CommonOps_D64.insert(mat, B, 0, 0);
        assertEquals(12, CommonOps_D64.trace(B), 1e-6);

        B = RandomMatrices_D64.createRandom(3,4,rand);
        CommonOps_D64.insert(mat, B, 0, 0);
        assertEquals(12, CommonOps_D64.trace(B), 1e-6);

    }

    @Test
    public void invert() {
        for( int i = 1; i <= 10; i++ ) {
            DenseMatrix64F a = RandomMatrices_D64.createRandom(i,i,rand);

            LUDecompositionAlt_D64 lu = new LUDecompositionAlt_D64();
            LinearSolverLu_D64 solver = new LinearSolverLu_D64(lu);
            assertTrue(solver.setA(a));

            DenseMatrix64F a_inv = new DenseMatrix64F(i,i);
            DenseMatrix64F a_lu = new DenseMatrix64F(i,i);
            solver.invert(a_lu);

            CommonOps_D64.invert(a,a_inv);
            CommonOps_D64.invert(a);

            EjmlUnitTests.assertEquals(a, a_inv, UtilEjml.TEST_64F);
            EjmlUnitTests.assertEquals(a_lu, a, UtilEjml.TEST_64F);
        }
    }

    /**
     * Checked against by computing a solution to the linear system then
     * seeing if the solution produces the expected output
     */
    @Test
    public void pinv() {
        // check wide matrix
        DenseMatrix64F A = new DenseMatrix64F(2,4,true,1,2,3,4,5,6,7,8);
        DenseMatrix64F A_inv = new DenseMatrix64F(4,2);
        DenseMatrix64F b = new DenseMatrix64F(2,1,true,3,4);
        DenseMatrix64F x = new DenseMatrix64F(4,1);
        DenseMatrix64F found = new DenseMatrix64F(2,1);
        
        CommonOps_D64.pinv(A,A_inv);

        CommonOps_D64.mult(A_inv,b,x);
        CommonOps_D64.mult(A,x,found);

        assertTrue(MatrixFeatures_D64.isIdentical(b,found,1e-4));

        // check tall matrix
        CommonOps_D64.transpose(A);
        CommonOps_D64.transpose(A_inv);
        b = new DenseMatrix64F(4,1,true,3,4,5,6);
        x.reshape(2,1);
        found.reshape(4,1);

        CommonOps_D64.mult(A_inv,b,x);
        CommonOps_D64.mult(A, x, found);

        assertTrue(MatrixFeatures_D64.isIdentical(b,found,1e-4));
    }

    @Test
    public void columnsToVectors() {
        DenseMatrix64F M = RandomMatrices_D64.createRandom(4, 5, rand);

        DenseMatrix64F v[] = CommonOps_D64.columnsToVector(M, null);

        assertEquals(M.numCols,v.length);

        for( int i = 0; i < v.length; i++ ) {
            DenseMatrix64F a = v[i];

            assertEquals(M.numRows,a.numRows);
            assertEquals(1,a.numCols);

            for( int j = 0; j < M.numRows; j++ ) {
                assertEquals(a.get(j),M.get(j,i),UtilEjml.TEST_64F);
            }
        }
    }
    
    @Test
    public void identity() {
        DenseMatrix64F A = CommonOps_D64.identity(4);

        assertEquals(4,A.numRows);
        assertEquals(4,A.numCols);

        assertEquals(4, CommonOps_D64.elementSum(A),UtilEjml.TEST_64F);
    }

    @Test
    public void identity_rect() {
        DenseMatrix64F A = CommonOps_D64.identity(4, 6);

        assertEquals(4,A.numRows);
        assertEquals(6,A.numCols);

        assertEquals(4, CommonOps_D64.elementSum(A),UtilEjml.TEST_64F);
    }

    @Test
    public void setIdentity() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(4, 4, rand);

        CommonOps_D64.setIdentity(A);

        assertEquals(4,A.numRows);
        assertEquals(4,A.numCols);

        assertEquals(4, CommonOps_D64.elementSum(A),UtilEjml.TEST_64F);
    }

    @Test
    public void diag() {
        DenseMatrix64F A = CommonOps_D64.diag(2.0, 3.0, 6.0, 7.0);

        assertEquals(4,A.numRows);
        assertEquals(4,A.numCols);

        assertEquals(2,A.get(0,0),UtilEjml.TEST_64F);
        assertEquals(3, A.get(1, 1), UtilEjml.TEST_64F);
        assertEquals(6, A.get(2, 2), UtilEjml.TEST_64F);
        assertEquals(7, A.get(3, 3), UtilEjml.TEST_64F);

        assertEquals(18, CommonOps_D64.elementSum(A),UtilEjml.TEST_64F);
    }

    @Test
    public void diag_rect() {
        DenseMatrix64F A = CommonOps_D64.diagR(4, 6, 2.0, 3.0, 6.0, 7.0);

        assertEquals(4,A.numRows);
        assertEquals(6,A.numCols);

        assertEquals(2,A.get(0,0),UtilEjml.TEST_64F);
        assertEquals(3,A.get(1,1),UtilEjml.TEST_64F);
        assertEquals(6,A.get(2,2),UtilEjml.TEST_64F);
        assertEquals(7,A.get(3,3),UtilEjml.TEST_64F);

        assertEquals(18, CommonOps_D64.elementSum(A), UtilEjml.TEST_64F);
    }

    @Test
    public void kron() {
        DenseMatrix64F A = new DenseMatrix64F(2,2, true, 1, 2, 3, 4);
        DenseMatrix64F B = new DenseMatrix64F(1,2, true, 4, 5);

        DenseMatrix64F C = new DenseMatrix64F(2,4);
        DenseMatrix64F C_expected = new DenseMatrix64F(2,4, true, 4, 5, 8, 10, 12, 15, 16, 20);

        CommonOps_D64.kron(A, B, C);

        assertTrue(MatrixFeatures_D64.isIdentical(C, C_expected, UtilEjml.TEST_64F));

        // test various shapes for problems
        for( int i = 1; i <= 3; i++ ) {
            for( int j = 1; j <= 3; j++ ) {
                for( int k = 1; k <= 3; k++ ) {
                    for( int l = 1; l <= 3; l++ ) {
                        A = RandomMatrices_D64.createRandom(i,j,rand);
                        B = RandomMatrices_D64.createRandom(k,l,rand);
                        C = new DenseMatrix64F(A.numRows*B.numRows,A.numCols*B.numCols);

                        CommonOps_D64.kron(A,B,C);

                        assertEquals(i*k,C.numRows);
                        assertEquals(j*l,C.numCols);
                    }
                }
            }
        }
    }

    @Test
    public void extract() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(5,5, 0, 1, rand);

        DenseMatrix64F B = new DenseMatrix64F(2,3);

        CommonOps_D64.extract(A, 1, 3, 2, 5, B, 0, 0);

        for( int i = 1; i < 3; i++ ) {
            for( int j = 2; j < 5; j++ ) {
                assertEquals(A.get(i,j),B.get(i-1,j-2),UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void extract_ret() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(5,5, 0, 1, rand);

        DenseMatrix64F B = CommonOps_D64.extract(A,1,3,2,5);

        assertEquals(B.numRows,2);
        assertEquals(B.numCols, 3);

        for( int i = 1; i < 3; i++ ) {
            for( int j = 2; j < 5; j++ ) {
                assertEquals(A.get(i,j),B.get(i-1,j-2),UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void extract_array_two() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(5,5, 0, 1, rand);

        int rows[] = new int[6];
        rows[0] = 2;
        rows[1] = 4;

        int cols[] = new int[4];
        cols[0] = 1;
        DenseMatrix64F B = new DenseMatrix64F(2,1);
        CommonOps_D64.extract(A,rows,2,cols,1,B);

        assertEquals(B.numRows,2);
        assertEquals(B.numCols, 1);

        for( int i = 0; i < 2; i++ ) {
            for( int j = 0; j < 1; j++ ) {
                assertEquals(A.get(rows[i],cols[j]),B.get(i,j),UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void extract_array_one() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(5,5, 0, 1, rand);

        int indexes[] = new int[6];
        indexes[0] = 2;
        indexes[1] = 4;

        DenseMatrix64F B = new DenseMatrix64F(2,1);
        CommonOps_D64.extract(A,indexes,2,B);

        assertEquals(B.numRows,2);
        assertEquals(B.numCols, 1);

        for( int i = 0; i < 2; i++ ) {
            assertEquals(A.get(indexes[i]),B.get(i),UtilEjml.TEST_64F);
        }
    }

    @Test
    public void insert_array_two() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(2,1, 0, 1, rand);

        int rows[] = new int[6];
        rows[0] = 2;
        rows[1] = 4;

        int cols[] = new int[4];
        cols[0] = 1;
        DenseMatrix64F B = new DenseMatrix64F(5,5);
        CommonOps_D64.insert(A, B, rows, 2, cols, 1);


        for( int i = 0; i < 2; i++ ) {
            for( int j = 0; j < 1; j++ ) {
                assertEquals(A.get(i,j),B.get(rows[i],cols[j]),UtilEjml.TEST_64F);
            }
        }
    }

    @Test
    public void extractDiag() {
        DenseMatrix64F a = RandomMatrices_D64.createRandom(3,4, 0, 1, rand);

        for( int i = 0; i < 3; i++ ) {
            a.set(i,i,i+1);
        }

        DenseMatrix64F v = new DenseMatrix64F(3,1);
        CommonOps_D64.extractDiag(a, v);

        for( int i = 0; i < 3; i++ ) {
            assertEquals( i+1 , v.get(i) , 1e-8 );
        }
    }

    @Test
    public void extractRow() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(5,6, 0, 1, rand);

        DenseMatrix64F B = CommonOps_D64.extractRow(A, 3, null);

        assertEquals(B.numRows,1);
        assertEquals(B.numCols, 6);

        for( int i = 0; i < 6; i++ ) {
            assertEquals(A.get(3,i),B.get(0,i),UtilEjml.TEST_64F);
        }
    }

    @Test
    public void extractColumn() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(5,6, 0, 1, rand);

        DenseMatrix64F B = CommonOps_D64.extractColumn(A, 3, null);

        assertEquals(B.numRows, 5);
        assertEquals(B.numCols, 1);

        for( int i = 0; i < 5; i++ ) {
            assertEquals(A.get(i,3),B.get(i,0),UtilEjml.TEST_64F);
        }
    }

    @Test
    public void insert() {
        DenseMatrix64F A = new DenseMatrix64F(5,5);
        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < A.numCols; j++ ) {
                A.set(i,j,i*A.numRows+j);
            }
        }

        DenseMatrix64F B = new DenseMatrix64F(8,8);

        CommonOps_D64.insert(A, B, 1, 2);

        for( int i = 1; i < 6; i++ ) {
            for( int j = 2; j < 7; j++ ) {
                assertEquals(A.get(i-1,j-2),B.get(i,j),UtilEjml.TEST_64F);
            }
        }
    }

   @Test
    public void addEquals() {
        DenseMatrix64F a = new DenseMatrix64F(2,3, true, 0, 1, 2, 3, 4, 5);
        DenseMatrix64F b = new DenseMatrix64F(2,3, true, 5, 4, 3, 2, 1, 0);

        CommonOps_D64.addEquals(a, b);

        UtilTestMatrix.checkMat(a,5,5,5,5,5,5);
        UtilTestMatrix.checkMat(b, 5, 4, 3, 2, 1, 0);
    }

    @Test
    public void addEquals_beta() {
        DenseMatrix64F a = new DenseMatrix64F(2,3, true, 0, 1, 2, 3, 4, 5);
        DenseMatrix64F b = new DenseMatrix64F(2,3, true, 5, 4, 3, 2, 1, 0);

        CommonOps_D64.addEquals(a, 2.0, b);

        UtilTestMatrix.checkMat(a,10,9,8,7,6,5);
        UtilTestMatrix.checkMat(b,5,4,3,2,1,0);
    }

    @Test
    public void add() {
        DenseMatrix64F a = new DenseMatrix64F(2,3, true, 0, 1, 2, 3, 4, 5);
        DenseMatrix64F b = new DenseMatrix64F(2,3, true, 5, 4, 3, 2, 1, 0);
        DenseMatrix64F c = RandomMatrices_D64.createRandom(2,3,rand);

        CommonOps_D64.add(a,b,c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(b,5,4,3,2,1,0);
        UtilTestMatrix.checkMat(c,5,5,5,5,5,5);
    }

    @Test
    public void add_beta() {
        DenseMatrix64F a = new DenseMatrix64F(2,3, true, 0, 1, 2, 3, 4, 5);
        DenseMatrix64F b = new DenseMatrix64F(2,3, true, 5, 4, 3, 2, 1, 0);
        DenseMatrix64F c = RandomMatrices_D64.createRandom(2,3,rand);

        CommonOps_D64.add(a,2.0,b,c);

        UtilTestMatrix.checkMat(a,0,1,2,3,4,5);
        UtilTestMatrix.checkMat(b,5,4,3,2,1,0);
        UtilTestMatrix.checkMat(c,10,9,8,7,6,5);
    }

    @Test
    public void add_alpha_beta() {
        DenseMatrix64F a = new DenseMatrix64F(2,3, true, 0, 1, 2, 3, 4, 5);
        DenseMatrix64F b = new DenseMatrix64F(2,3, true, 5, 4, 3, 2, 1, 0);
        DenseMatrix64F c = RandomMatrices_D64.createRandom(2,3,rand);

        CommonOps_D64.add(2.0,a,2.0,b,c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(b,5,4,3,2,1,0);
        UtilTestMatrix.checkMat(c,10,10,10,10,10,10);
    }

    @Test
    public void add_alpha() {
        DenseMatrix64F a = new DenseMatrix64F(2,3, true, 0, 1, 2, 3, 4, 5);
        DenseMatrix64F b = new DenseMatrix64F(2,3, true, 5, 4, 3, 2, 1, 0);
        DenseMatrix64F c = RandomMatrices_D64.createRandom(2,3,rand);

        CommonOps_D64.add(2.0,a,b,c);

        UtilTestMatrix.checkMat(a,0,1,2,3,4,5);
        UtilTestMatrix.checkMat(b, 5, 4, 3, 2, 1, 0);
        UtilTestMatrix.checkMat(c,5,6,7,8,9,10);
    }

    @Test
    public void add_scalar_c() {
        DenseMatrix64F a = new DenseMatrix64F(2,3, true, 0, 1, 2, 3, 4, 5);
        DenseMatrix64F c = RandomMatrices_D64.createRandom(2,3,rand);

        CommonOps_D64.add(a, 2.0, c);

        UtilTestMatrix.checkMat(a,0,1,2,3,4,5);
        UtilTestMatrix.checkMat(c,2,3,4,5,6,7);
    }

    @Test
    public void add_scalar() {
        DenseMatrix64F a = new DenseMatrix64F(2,3, true, 0, 1, 2, 3, 4, 5);

        CommonOps_D64.add(a, 2.0);

        UtilTestMatrix.checkMat(a,2,3,4,5,6,7);
    }

    @Test
    public void subEquals() {
        DenseMatrix64F a = new DenseMatrix64F(2,3, true, 0, 1, 2, 3, 4, 5);
        DenseMatrix64F b = new DenseMatrix64F(2,3, true, 5, 5, 5, 5, 5, 5);

        CommonOps_D64.subtractEquals(a, b);

        UtilTestMatrix.checkMat(a, -5, -4, -3, -2, -1, 0);
        UtilTestMatrix.checkMat(b,5,5,5,5,5,5);
    }

    @Test
    public void subtract_matrix_matrix() {
        DenseMatrix64F a = new DenseMatrix64F(2,3, true, 0, 1, 2, 3, 4, 5);
        DenseMatrix64F b = new DenseMatrix64F(2,3, true, 5, 5, 5, 5, 5, 5);
        DenseMatrix64F c = RandomMatrices_D64.createRandom(2,3,rand);

        CommonOps_D64.subtract(a, b, c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(b,5,5,5,5,5,5);
        UtilTestMatrix.checkMat(c,-5,-4,-3,-2,-1,0);
    }

    @Test
    public void subtract_matrix_double() {
        DenseMatrix64F a = new DenseMatrix64F(2,3, true, 0, 1, 2, 3, 4, 5);
        DenseMatrix64F c = RandomMatrices_D64.createRandom(2,3,rand);

        CommonOps_D64.subtract(a, 2, c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(c, -2, -1, 0, 1, 2, 3);
    }

    @Test
    public void subtract_double_matrix() {
        DenseMatrix64F a = new DenseMatrix64F(2,3, true, 0, 1, 2, 3, 4, 5);
        DenseMatrix64F c = RandomMatrices_D64.createRandom(2,3,rand);

        CommonOps_D64.subtract(2, a, c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(c, 2, 1, 0, -1, -2, -3);
    }

    @Test
    public void scale() {
        double s = 2.5;
        double d[] = new double[]{10,12.5,-2,5.5};
        DenseMatrix64F mat = new DenseMatrix64F(2,2, true, d);

        CommonOps_D64.scale(s, mat);

        assertEquals(d[0]*s,mat.get(0,0),UtilEjml.TEST_64F);
        assertEquals(d[1] * s, mat.get(0, 1), UtilEjml.TEST_64F);
        assertEquals(d[2] * s, mat.get(1, 0), UtilEjml.TEST_64F);
        assertEquals(d[3] * s, mat.get(1, 1), UtilEjml.TEST_64F);
    }

    @Test
    public void scale_two_input() {
        double s = 2.5;
        double d[] = new double[]{10,12.5,-2,5.5};
        DenseMatrix64F mat = new DenseMatrix64F(2,2, true, d);
        DenseMatrix64F r = new DenseMatrix64F(2,2, true, d);

        CommonOps_D64.scale(s,mat,r);

        assertEquals(d[0],mat.get(0,0),UtilEjml.TEST_64F);
        assertEquals(d[1],mat.get(0,1),UtilEjml.TEST_64F);
        assertEquals(d[2],mat.get(1,0),UtilEjml.TEST_64F);
        assertEquals(d[3], mat.get(1, 1), UtilEjml.TEST_64F);

        assertEquals(d[0]*s,r.get(0,0),UtilEjml.TEST_64F);
        assertEquals(d[1]*s,r.get(0,1),UtilEjml.TEST_64F);
        assertEquals(d[2]*s,r.get(1,0),UtilEjml.TEST_64F);
        assertEquals(d[3]*s,r.get(1,1),UtilEjml.TEST_64F);
    }

    @Test
    public void div_scalar_mat() {
        double s = 2.5;
        double d[] = new double[]{10,12.5,-2,5.5};
        DenseMatrix64F mat = new DenseMatrix64F(2,2, true, d);

        CommonOps_D64.divide(s, mat);

        assertEquals(s/d[0],mat.get(0,0),UtilEjml.TEST_64F);
        assertEquals(s/d[1],mat.get(0,1),UtilEjml.TEST_64F);
        assertEquals(s/d[2],mat.get(1,0),UtilEjml.TEST_64F);
        assertEquals(s/d[3],mat.get(1,1),UtilEjml.TEST_64F);
    }

    @Test
    public void div_mat_scalar() {
        double s = 2.5;
        double d[] = new double[]{10,12.5,-2,5.5};
        DenseMatrix64F mat = new DenseMatrix64F(2,2, true, d);

        CommonOps_D64.divide(mat, s);

        assertEquals(mat.get(0,0),d[0]/s,UtilEjml.TEST_64F);
        assertEquals(mat.get(0,1),d[1]/s,UtilEjml.TEST_64F);
        assertEquals(mat.get(1,0),d[2]/s,UtilEjml.TEST_64F);
        assertEquals(mat.get(1,1),d[3]/s,UtilEjml.TEST_64F);
    }

    @Test
    public void div_mat_scalar_out() {
        double s = 2.5;
        double d[] = new double[]{10,12.5,-2,5.5};
        DenseMatrix64F mat = new DenseMatrix64F(2,2, true, d);
        DenseMatrix64F r = new DenseMatrix64F(2,2, true, d);

        CommonOps_D64.divide(mat,s,r);

        assertEquals(d[0],mat.get(0,0),UtilEjml.TEST_64F);
        assertEquals(d[1],mat.get(0,1),UtilEjml.TEST_64F);
        assertEquals(d[2],mat.get(1,0),UtilEjml.TEST_64F);
        assertEquals(d[3], mat.get(1, 1), UtilEjml.TEST_64F);

        assertEquals(d[0]/s,r.get(0,0),UtilEjml.TEST_64F);
        assertEquals(d[1]/s,r.get(0,1),UtilEjml.TEST_64F);
        assertEquals(d[2]/s,r.get(1,0),UtilEjml.TEST_64F);
        assertEquals(d[3] / s, r.get(1, 1), UtilEjml.TEST_64F);
    }

    @Test
    public void div_scalar_mat_out() {
        double s = 2.5;
        double d[] = new double[]{10,12.5,-2,5.5};
        DenseMatrix64F mat = new DenseMatrix64F(2,2, true, d);
        DenseMatrix64F r = new DenseMatrix64F(2,2, true, d);

        CommonOps_D64.divide(s,mat,r);

        assertEquals(d[0],mat.get(0,0),UtilEjml.TEST_64F);
        assertEquals(d[1],mat.get(0,1),UtilEjml.TEST_64F);
        assertEquals(d[2],mat.get(1,0),UtilEjml.TEST_64F);
        assertEquals(d[3],mat.get(1,1),UtilEjml.TEST_64F);

        assertEquals(s/d[0],r.get(0,0),UtilEjml.TEST_64F);
        assertEquals(s/d[1],r.get(0,1),UtilEjml.TEST_64F);
        assertEquals(s/d[2],r.get(1,0),UtilEjml.TEST_64F);
        assertEquals(s / d[3], r.get(1, 1), UtilEjml.TEST_64F);
    }

    @Test
    public void changeSign_one() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(2,3,rand);
        DenseMatrix64F A_orig = A.copy();

        CommonOps_D64.changeSign(A);

        for (int i = 0; i < A.getNumElements(); i++) {
            assertEquals(-A.get(i),A_orig.get(i),UtilEjml.TEST_64F);
        }
    }

    @Test
    public void changeSign_two() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(2,3,rand);
        DenseMatrix64F B = RandomMatrices_D64.createRandom(2, 3, rand);

        CommonOps_D64.changeSign(A, B);

        for (int i = 0; i < A.getNumElements(); i++) {
            assertEquals(A.get(i),-B.get(i),UtilEjml.TEST_64F);
        }
    }

    @Test
    public void fill_dense() {
        double d[] = new double[]{10,12.5,-2,5.5};
        DenseMatrix64F mat = new DenseMatrix64F(2,2, true, d);

        CommonOps_D64.fill(mat, 1);

        for( int i = 0; i < mat.getNumElements(); i++ ) {
            assertEquals(1,mat.get(i),UtilEjml.TEST_64F);
        }
    }

    @Test
    public void fill_block() {
        // pick the size such that it doesn't nicely line up along blocks
        BlockMatrix64F mat = new BlockMatrix64F(10,14,3);

        CommonOps_D64.fill(mat, 1.5);

        for( int i = 0; i < mat.getNumElements(); i++ ) {
            assertEquals(1.5,mat.get(i),UtilEjml.TEST_64F);
        }
    }

    @Test
    public void zero() {
        double d[] = new double[]{10,12.5,-2,5.5};
        DenseMatrix64F mat = new DenseMatrix64F(2,2, true, d);

        mat.zero();

        for( int i = 0; i < mat.getNumElements(); i++ ) {
            assertEquals(0,mat.get(i),UtilEjml.TEST_64F);
        }
    }

    @Test
    public void elementMax() {
        DenseMatrix64F mat = new DenseMatrix64F(3,3, true, 0, 1, -2, 3, 4, 5, 6, 7, -8);

        double m = CommonOps_D64.elementMax(mat);
        assertEquals(7, m, UtilEjml.TEST_64F);
    }

    @Test
    public void elementMin() {
        DenseMatrix64F mat = new DenseMatrix64F(3,3, true, 0, 1, 2, -3, 4, 5, 6, 7, 8);

        double m = CommonOps_D64.elementMin(mat);
        assertEquals(-3,m,UtilEjml.TEST_64F);
    }

    @Test
    public void elementMinAbs() {
        DenseMatrix64F mat = new DenseMatrix64F(3,3, true, 0, 1, -2, 3, 4, 5, 6, 7, -8);

        double m = CommonOps_D64.elementMinAbs(mat);
        assertEquals(0,m,UtilEjml.TEST_64F);
    }

    @Test
    public void elementMaxAbs() {
        DenseMatrix64F mat = new DenseMatrix64F(3,3, true, 0, 1, 2, 3, 4, 5, -6, 7, -8);

        double m = CommonOps_D64.elementMaxAbs(mat);
        assertEquals(8,m,UtilEjml.TEST_64F);
    }

    @Test
    public void elementSum() {
        DenseMatrix64F M = RandomMatrices_D64.createRandom(5,5,rand);
        // make it smaller than the original size to make sure it is bounding
        // the summation correctly
        M.reshape(4, 3, false);

        double sum = 0;
        for( int i = 0; i < M.numRows; i++ ) {
            for( int j = 0; j < M.numCols; j++ ) {
                sum += M.get(i,j);
            }
        }

        assertEquals(sum, CommonOps_D64.elementSum(M),UtilEjml.TEST_64F);
    }

    @Test
    public void elementSumAbs() {
        DenseMatrix64F M = RandomMatrices_D64.createRandom(5,5,rand);
        // make it smaller than the original size to make sure it is bounding
        // the summation correctly
        M.reshape(4, 3, false);

        double sum = 0;
        for( int i = 0; i < M.numRows; i++ ) {
            for( int j = 0; j < M.numCols; j++ ) {
                sum += Math.abs(M.get(i, j));
            }
        }

        assertEquals(sum, CommonOps_D64.elementSum(M),UtilEjml.TEST_64F);
    }

    @Test
    public void elementPower_mm() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(4, 5, rand);
        DenseMatrix64F B = RandomMatrices_D64.createRandom(4, 5, rand);
        DenseMatrix64F C = RandomMatrices_D64.createRandom(4, 5, rand);

        CommonOps_D64.elementPower(A, B, C);

        for (int i = 0; i < A.getNumElements(); i++) {
            double expected = Math.pow( A.get(i) , B.get(i));
            assertEquals(expected,C.get(i),UtilEjml.TEST_64F);
        }
    }

    @Test
    public void elementPower_ms() {
        double a = 1.3;
        DenseMatrix64F B = RandomMatrices_D64.createRandom(4, 5, rand);
        DenseMatrix64F C = RandomMatrices_D64.createRandom(4, 5, rand);

        CommonOps_D64.elementPower(a, B, C);

        for (int i = 0; i < C.getNumElements(); i++) {
            double expected = Math.pow(a, B.get(i));
            assertEquals(expected,C.get(i),UtilEjml.TEST_64F);
        }
    }

    @Test
    public void elementPower_sm() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(4, 5, rand);
        double b = 1.1;
        DenseMatrix64F C = RandomMatrices_D64.createRandom(4, 5, rand);

        CommonOps_D64.elementPower(A, b, C);

        for (int i = 0; i < A.getNumElements(); i++) {
            double expected = Math.pow(A.get(i), b);
            assertEquals(expected,C.get(i),UtilEjml.TEST_64F);
        }
    }

    @Test
    public void elementLog() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(4, 5, rand);
        DenseMatrix64F C = RandomMatrices_D64.createRandom(4, 5, rand);

        CommonOps_D64.elementLog(A, C);

        for (int i = 0; i < A.getNumElements(); i++) {
            double expected = Math.log(A.get(i));
            assertEquals(expected,C.get(i),UtilEjml.TEST_64F);
        }
    }

    @Test
    public void elementExp() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(4, 5, rand);
        DenseMatrix64F C = RandomMatrices_D64.createRandom(4, 5, rand);

        CommonOps_D64.elementExp(A, C);

        for (int i = 0; i < A.getNumElements(); i++) {
            double expected = Math.exp(A.get(i));
            assertEquals(expected,C.get(i),UtilEjml.TEST_64F);
        }
    }

    @Test
    public void sumRows() {
        DenseMatrix64F input = RandomMatrices_D64.createRandom(4,5,rand);
        DenseMatrix64F output = new DenseMatrix64F(4,1);

        assertTrue( output == CommonOps_D64.sumRows(input,output));

        for( int i = 0; i < input.numRows; i++ ) {
            double total = 0;
            for( int j = 0; j < input.numCols; j++ ) {
                total += input.get(i,j);
            }
            assertEquals( total, output.get(i),UtilEjml.TEST_64F);
        }

        // check with a null output
        DenseMatrix64F output2 = CommonOps_D64.sumRows(input, null);

        EjmlUnitTests.assertEquals(output, output2, UtilEjml.TEST_64F);
    }

    @Test
    public void sumCols() {
        DenseMatrix64F input = RandomMatrices_D64.createRandom(4,5,rand);
        DenseMatrix64F output = new DenseMatrix64F(1,5);

        assertTrue( output == CommonOps_D64.sumCols(input, output));

        for( int i = 0; i < input.numCols; i++ ) {
            double total = 0;
            for( int j = 0; j < input.numRows; j++ ) {
                total += input.get(j,i);
            }
            assertEquals( total, output.get(i),UtilEjml.TEST_64F);
        }

        // check with a null output
        DenseMatrix64F output2 = CommonOps_D64.sumCols(input, null);

        EjmlUnitTests.assertEquals(output, output2, UtilEjml.TEST_64F);
    }

    @Test
    public void rref() {
        DenseMatrix64F A = new DenseMatrix64F(4,6,true,
                0,0,1,-1,-1,4,
                2,4,2,4,2,4,
                2,4,3,3,3,4,
                3,6,6,3,6,6);

        DenseMatrix64F expected = new DenseMatrix64F(4,6,true,
                1,2,0,3,0,2,
                0,0,1,-1,0,2,
                0,0,0,0,1,-2,
                0,0,0,0,0,0);

        DenseMatrix64F found = CommonOps_D64.rref(A, 5, null);


        assertTrue(MatrixFeatures_D64.isEquals(found, expected));
    }

    @Test
    public void elementLessThan_double() {
        DenseMatrix64F A = new DenseMatrix64F(3,4);
        DenseMatrixBool expected = new DenseMatrixBool(3,4);
        DenseMatrixBool found = new DenseMatrixBool(3,4);

        double value = 5.0;

        for (int i = 0; i < A.getNumElements() ; i++) {
            A.data[i] = i;
            expected.data[i] = i < value;
        }

        CommonOps_D64.elementLessThan(A, value, found);
        assertTrue(MatrixFeatures_D64.isEquals(expected,found));
    }

    @Test
    public void elementLessThanOrEqual_double() {
        DenseMatrix64F A = new DenseMatrix64F(3,4);
        DenseMatrixBool expected = new DenseMatrixBool(3,4);
        DenseMatrixBool found = new DenseMatrixBool(3,4);

        double value = 5.0;

        for (int i = 0; i < A.getNumElements() ; i++) {
            A.data[i] = i;
            expected.data[i] = i <= value;
        }

        CommonOps_D64.elementLessThanOrEqual(A, value, found);
        assertTrue(MatrixFeatures_D64.isEquals(expected, found));
    }

    @Test
    public void elementMoreThan_double() {
        DenseMatrix64F A = new DenseMatrix64F(3,4);
        DenseMatrixBool expected = new DenseMatrixBool(3,4);
        DenseMatrixBool found = new DenseMatrixBool(3,4);

        double value = 5.0;

        for (int i = 0; i < A.getNumElements() ; i++) {
            A.data[i] = i;
            expected.data[i] = i > value;
        }

        CommonOps_D64.elementMoreThan(A, value, found);
        assertTrue(MatrixFeatures_D64.isEquals(expected, found));
    }

    @Test
    public void elementMoreThanOrEqual_double() {
        DenseMatrix64F A = new DenseMatrix64F(3,4);
        DenseMatrixBool expected = new DenseMatrixBool(3,4);
        DenseMatrixBool found = new DenseMatrixBool(3,4);

        double value = 5.0;

        for (int i = 0; i < A.getNumElements() ; i++) {
            A.data[i] = i;
            expected.data[i] = i >= value;
        }

        CommonOps_D64.elementMoreThanOrEqual(A, value, found);
        assertTrue(MatrixFeatures_D64.isEquals(expected, found));
    }

    @Test
    public void elementLessThan_matrix() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(3,4,rand);
        DenseMatrix64F B = RandomMatrices_D64.createRandom(3,4,rand);
        DenseMatrixBool expected = new DenseMatrixBool(3,4);
        DenseMatrixBool found = new DenseMatrixBool(3,4);

        A.data[6] = B.data[6];

        for (int i = 0; i < A.getNumElements() ; i++) {
            expected.data[i] = A.data[i] < B.data[i];
        }

        CommonOps_D64.elementLessThan(A, B, found);
        assertTrue(MatrixFeatures_D64.isEquals(expected, found));
    }

    @Test
    public void elementLessThanOrEqual_matrix() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(3,4,rand);
        DenseMatrix64F B = RandomMatrices_D64.createRandom(3,4,rand);
        DenseMatrixBool expected = new DenseMatrixBool(3,4);
        DenseMatrixBool found = new DenseMatrixBool(3,4);

        A.data[6] = B.data[6];

        for (int i = 0; i < A.getNumElements() ; i++) {
            expected.data[i] = A.data[i] <= B.data[i];
        }

        CommonOps_D64.elementLessThanOrEqual(A, B, found);
        assertTrue(MatrixFeatures_D64.isEquals(expected, found));
    }

    @Test
    public void elements() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(3,4,rand);

        DenseMatrixBool B = RandomMatrices_D64.createRandomB(3, 4, rand);

        DenseMatrix64F found = CommonOps_D64.elements(A, B, null);

        int index = 0;

        for (int i = 0; i < B.getNumElements(); i++) {
            if( B.get(i) ) {
                assertEquals(found.get(index++),A.get(i),UtilEjml.TEST_64F);
            }
        }

        assertEquals(index,found.getNumRows());
        assertEquals(1,found.getNumCols());
    }

    @Test
    public void countTrue() {
        DenseMatrixBool B = RandomMatrices_D64.createRandomB(4, 5, rand);


        int index = 0;

        for (int i = 0; i < B.getNumElements(); i++) {
            if( B.get(i) ) {
                index++;
            }
        }

        assertTrue(index>5);
        assertEquals(index, CommonOps_D64.countTrue(B));
    }
}
