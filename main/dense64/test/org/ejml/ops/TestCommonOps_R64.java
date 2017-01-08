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

package org.ejml.ops;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt_R64;
import org.ejml.alg.dense.linsol.lu.LinearSolverLu_R64;
import org.ejml.alg.dense.mult.CheckMatrixMultShape_R64;
import org.ejml.alg.dense.mult.MatrixMatrixMult_R64;
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
public class TestCommonOps_R64 {

    Random rand = new Random(0xFF);
    double tol = UtilEjml.TEST_F64;

    @Test
    public void checkInputShape() {
        CheckMatrixMultShape_R64 check = new CheckMatrixMultShape_R64(CommonOps_R64.class);
        check.checkAll();
    }

    /**
     * Make sure the multiplication methods here have the same behavior as the ones in MatrixMatrixMult.
     */
    @Test
    public void checkAllMatrixMults() {
        int numChecked = 0;
        Method methods[] = CommonOps_R64.class.getMethods();

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
        Method methods[] = CommonOps_R64.class.getMethods();

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
                checkMethod = MatrixMatrixMult_R64.class.getMethod(
                        name,double.class,
                        RowD1Matrix_F64.class, RowD1Matrix_F64.class,RowD1Matrix_F64.class);
            else
                checkMethod = MatrixMatrixMult_R64.class.getMethod(
                        name, RowD1Matrix_F64.class, RowD1Matrix_F64.class,RowD1Matrix_F64.class);
        } catch (NoSuchMethodException e) {
            checkMethod = null;
        }
        if( checkMethod == null ) {
            try {
            if( hasAlpha )
                checkMethod = MatrixMatrixMult_R64.class.getMethod(
                        name+"_reorder",double.class,
                        RowD1Matrix_F64.class, RowD1Matrix_F64.class,RowD1Matrix_F64.class);
            else
                checkMethod = MatrixMatrixMult_R64.class.getMethod(
                        name+"_reorder", RowD1Matrix_F64.class, RowD1Matrix_F64.class,RowD1Matrix_F64.class);
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
            RowMatrix_F64 a;
            if( tranA ) a = RandomMatrices_R64.createRandom(i+1,i,rand);
            else  a = RandomMatrices_R64.createRandom(i,i+1,rand);

            RowMatrix_F64 b;
            if( tranB ) b = RandomMatrices_R64.createRandom(i,i+1,rand);
            else  b = RandomMatrices_R64.createRandom(i+1,i,rand);

            RowMatrix_F64 c = RandomMatrices_R64.createRandom(i,i,rand);
            RowMatrix_F64 c_alt = c.copy();

            if( hasAlpha ) {
                method.invoke(null,2.0,a,b,c);
                checkMethod.invoke(null,2.0,a,b,c_alt);
            } else {
                method.invoke(null,a,b,c);
                checkMethod.invoke(null,a,b,c_alt);
            }

            if( !MatrixFeatures_R64.isIdentical(c_alt,c,tol))
                return false;
        }

        // check various sizes column vector
        for( int i = 1; i < 4; i++ ) {
            RowMatrix_F64 a;
            if( tranA ) a = RandomMatrices_R64.createRandom(i,i+1,rand);
            else  a = RandomMatrices_R64.createRandom(i+1,i,rand);

            RowMatrix_F64 b;
            if( tranB ) b = RandomMatrices_R64.createRandom(1,i,rand);
            else  b = RandomMatrices_R64.createRandom(i,1,rand);

            RowMatrix_F64 c = RandomMatrices_R64.createRandom(i+1,1,rand);
            RowMatrix_F64 c_alt = c.copy();

            if( hasAlpha ) {
                method.invoke(null,2.0,a,b,c);
                checkMethod.invoke(null,2.0,a,b,c_alt);
            } else {
                method.invoke(null,a,b,c);
                checkMethod.invoke(null,a,b,c_alt);
            }

            if( !MatrixFeatures_R64.isIdentical(c_alt,c,tol))
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
        RowMatrix_F64 a = tranA ? new RowMatrix_F64(colsA,rowsA) : new RowMatrix_F64(rowsA,colsA);
        RowMatrix_F64 b = tranB ? new RowMatrix_F64(colsB,rowsB) : new RowMatrix_F64(rowsB,colsB);

        RowMatrix_F64 c = RandomMatrices_R64.createRandom(rowsA,colsB,rand);

        if( hasAlpha ) {
            method.invoke(null,2.0,a,b,c);
        } else {
            method.invoke(null,a,b,c);
        }

        if( add ) {
            RowMatrix_F64 corig = c.copy();
            assertTrue(MatrixFeatures_R64.isIdentical(corig, c, UtilEjml.TEST_F64));
        } else {
            assertTrue(MatrixFeatures_R64.isZeros(c, UtilEjml.TEST_F64));
        }

        return true;
    }

    @Test
    public void dot() {
        RowMatrix_F64 a = RandomMatrices_R64.createRandom(10, 1, rand);
        RowMatrix_F64 b = RandomMatrices_R64.createRandom(1,10,rand);

        double found = CommonOps_R64.dot(a, b);

        double expected = 0;
        for (int i = 0; i < 10; i++) {
            expected += a.data[i]*b.data[i];
        }

        assertEquals(expected, found, UtilEjml.TEST_F64);
    }

    @Test
    public void multInner() {
        RowMatrix_F64 a = RandomMatrices_R64.createRandom(10,4,rand);
        RowMatrix_F64 found = RandomMatrices_R64.createRandom(4,4,rand);
        RowMatrix_F64 expected = RandomMatrices_R64.createRandom(4, 4, rand);

        CommonOps_R64.multTransA(a, a, expected);
        CommonOps_R64.multInner(a,found);

        assertTrue(MatrixFeatures_R64.isIdentical(expected, found, tol));
    }

    @Test
    public void multOuter() {
        RowMatrix_F64 a = RandomMatrices_R64.createRandom(10,4,rand);
        RowMatrix_F64 found = RandomMatrices_R64.createRandom(10,10,rand);
        RowMatrix_F64 expected = RandomMatrices_R64.createRandom(10,10,rand);

        CommonOps_R64.multTransB(a, a, expected);
        CommonOps_R64.multOuter(a, found);

        assertTrue(MatrixFeatures_R64.isIdentical(expected, found, tol));
    }
    
    @Test
    public void elementMult_two() {
        RowMatrix_F64 a = RandomMatrices_R64.createRandom(5,4,rand);
        RowMatrix_F64 b = RandomMatrices_R64.createRandom(5,4,rand);
        RowMatrix_F64 a_orig = a.copy();

        CommonOps_R64.elementMult(a, b);

        for( int i = 0; i < 20; i++ ) {
            assertEquals(a.get(i),b.get(i)*a_orig.get(i),1e-6);
        }
    }

    @Test
    public void elementMult_three() {
        RowMatrix_F64 a = RandomMatrices_R64.createRandom(5,4,rand);
        RowMatrix_F64 b = RandomMatrices_R64.createRandom(5,4,rand);
        RowMatrix_F64 c = RandomMatrices_R64.createRandom(5, 4, rand);

        CommonOps_R64.elementMult(a, b, c);

        for( int i = 0; i < 20; i++ ) {
            assertEquals(c.get(i),b.get(i)*a.get(i),1e-6);
        }
    }

    @Test
    public void elementDiv_two() {
        RowMatrix_F64 a = RandomMatrices_R64.createRandom(5,4,rand);
        RowMatrix_F64 b = RandomMatrices_R64.createRandom(5,4,rand);
        RowMatrix_F64 a_orig = a.copy();

        CommonOps_R64.elementDiv(a, b);

        for( int i = 0; i < 20; i++ ) {
            assertEquals(a.get(i),a_orig.get(i)/b.get(i),1e-6);
        }
    }

    @Test
    public void elementDiv_three() {
        RowMatrix_F64 a = RandomMatrices_R64.createRandom(5,4,rand);
        RowMatrix_F64 b = RandomMatrices_R64.createRandom(5,4,rand);
        RowMatrix_F64 c = RandomMatrices_R64.createRandom(5, 4, rand);

        CommonOps_R64.elementDiv(a, b, c);

        for( int i = 0; i < 20; i++ ) {
            assertEquals(c.get(i),a.get(i)/b.get(i),1e-6);
        }
    }

    @Test
    public void solve() {
        RowMatrix_F64 a = new RowMatrix_F64(2,2, true, 1, 2, 7, -3);
        RowMatrix_F64 b = RandomMatrices_R64.createRandom(2,5,rand);
        RowMatrix_F64 c = RandomMatrices_R64.createRandom(2,5,rand);
        RowMatrix_F64 c_exp = RandomMatrices_R64.createRandom(2,5,rand);

        assertTrue(CommonOps_R64.solve(a,b,c));
        LUDecompositionAlt_R64 alg = new LUDecompositionAlt_R64();
        LinearSolverLu_R64 solver = new LinearSolverLu_R64(alg);
        assertTrue(solver.setA(a));

        solver.solve(b, c_exp);

        EjmlUnitTests.assertEquals(c_exp, c, UtilEjml.TEST_F64);
    }

    @Test
    public void transpose_inplace() {
        RowMatrix_F64 mat = new RowMatrix_F64(3,3, true, 0, 1, 2, 3, 4, 5, 6, 7, 8);
        RowMatrix_F64 matTran = new RowMatrix_F64(3,3);

        CommonOps_R64.transpose(mat, matTran);
        CommonOps_R64.transpose(mat);

        EjmlUnitTests.assertEquals(mat, matTran, UtilEjml.TEST_F64);
    }

    @Test
    public void transpose() {
        RowMatrix_F64 mat = new RowMatrix_F64(3,2, true, 0, 1, 2, 3, 4, 5);
        RowMatrix_F64 matTran = new RowMatrix_F64(2,3);

        CommonOps_R64.transpose(mat,matTran);

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
        RowMatrix_F64 mat = new RowMatrix_F64(3,3, true, 0, 1, 2, 3, 4, 5, 6, 7, 8);

        assertEquals(12, CommonOps_R64.trace(mat), 1e-6);

        // non square
        RowMatrix_F64 B = RandomMatrices_R64.createRandom(4,3,rand);
        CommonOps_R64.insert(mat, B, 0, 0);
        assertEquals(12, CommonOps_R64.trace(B), 1e-6);

        B = RandomMatrices_R64.createRandom(3,4,rand);
        CommonOps_R64.insert(mat, B, 0, 0);
        assertEquals(12, CommonOps_R64.trace(B), 1e-6);

    }

    @Test
    public void invert() {
        for( int i = 1; i <= 10; i++ ) {
            RowMatrix_F64 a = RandomMatrices_R64.createRandom(i,i,rand);

            LUDecompositionAlt_R64 lu = new LUDecompositionAlt_R64();
            LinearSolverLu_R64 solver = new LinearSolverLu_R64(lu);
            assertTrue(solver.setA(a));

            RowMatrix_F64 a_inv = new RowMatrix_F64(i,i);
            RowMatrix_F64 a_lu = new RowMatrix_F64(i,i);
            solver.invert(a_lu);

            CommonOps_R64.invert(a,a_inv);
            CommonOps_R64.invert(a);

            EjmlUnitTests.assertEquals(a, a_inv, UtilEjml.TEST_F64);
            EjmlUnitTests.assertEquals(a_lu, a, UtilEjml.TEST_F64);
        }
    }

    /**
     * Checked against by computing a solution to the linear system then
     * seeing if the solution produces the expected output
     */
    @Test
    public void pinv() {
        // check wide matrix
        RowMatrix_F64 A = new RowMatrix_F64(2,4,true,1,2,3,4,5,6,7,8);
        RowMatrix_F64 A_inv = new RowMatrix_F64(4,2);
        RowMatrix_F64 b = new RowMatrix_F64(2,1,true,3,4);
        RowMatrix_F64 x = new RowMatrix_F64(4,1);
        RowMatrix_F64 found = new RowMatrix_F64(2,1);
        
        CommonOps_R64.pinv(A,A_inv);

        CommonOps_R64.mult(A_inv,b,x);
        CommonOps_R64.mult(A,x,found);

        assertTrue(MatrixFeatures_R64.isIdentical(b,found,UtilEjml.TEST_F64_SQ));

        // check tall matrix
        CommonOps_R64.transpose(A);
        CommonOps_R64.transpose(A_inv);
        b = new RowMatrix_F64(4,1,true,3,4,5,6);
        x.reshape(2,1);
        found.reshape(4,1);

        CommonOps_R64.mult(A_inv,b,x);
        CommonOps_R64.mult(A, x, found);

        assertTrue(MatrixFeatures_R64.isIdentical(b,found,UtilEjml.TEST_F64_SQ));
    }

    @Test
    public void columnsToVectors() {
        RowMatrix_F64 M = RandomMatrices_R64.createRandom(4, 5, rand);

        RowMatrix_F64 v[] = CommonOps_R64.columnsToVector(M, null);

        assertEquals(M.numCols,v.length);

        for( int i = 0; i < v.length; i++ ) {
            RowMatrix_F64 a = v[i];

            assertEquals(M.numRows,a.numRows);
            assertEquals(1,a.numCols);

            for( int j = 0; j < M.numRows; j++ ) {
                assertEquals(a.get(j),M.get(j,i),UtilEjml.TEST_F64);
            }
        }
    }
    
    @Test
    public void identity() {
        RowMatrix_F64 A = CommonOps_R64.identity(4);

        assertEquals(4,A.numRows);
        assertEquals(4,A.numCols);

        assertEquals(4, CommonOps_R64.elementSum(A),UtilEjml.TEST_F64);
    }

    @Test
    public void identity_rect() {
        RowMatrix_F64 A = CommonOps_R64.identity(4, 6);

        assertEquals(4,A.numRows);
        assertEquals(6,A.numCols);

        assertEquals(4, CommonOps_R64.elementSum(A),UtilEjml.TEST_F64);
    }

    @Test
    public void setIdentity() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(4, 4, rand);

        CommonOps_R64.setIdentity(A);

        assertEquals(4,A.numRows);
        assertEquals(4,A.numCols);

        assertEquals(4, CommonOps_R64.elementSum(A),UtilEjml.TEST_F64);
    }

    @Test
    public void diag() {
        RowMatrix_F64 A = CommonOps_R64.diag(2.0, 3.0, 6.0, 7.0);

        assertEquals(4,A.numRows);
        assertEquals(4,A.numCols);

        assertEquals(2,A.get(0,0),UtilEjml.TEST_F64);
        assertEquals(3, A.get(1, 1), UtilEjml.TEST_F64);
        assertEquals(6, A.get(2, 2), UtilEjml.TEST_F64);
        assertEquals(7, A.get(3, 3), UtilEjml.TEST_F64);

        assertEquals(18, CommonOps_R64.elementSum(A),UtilEjml.TEST_F64);
    }

    @Test
    public void diag_rect() {
        RowMatrix_F64 A = CommonOps_R64.diagR(4, 6, 2.0, 3.0, 6.0, 7.0);

        assertEquals(4,A.numRows);
        assertEquals(6,A.numCols);

        assertEquals(2,A.get(0,0),UtilEjml.TEST_F64);
        assertEquals(3,A.get(1,1),UtilEjml.TEST_F64);
        assertEquals(6,A.get(2,2),UtilEjml.TEST_F64);
        assertEquals(7,A.get(3,3),UtilEjml.TEST_F64);

        assertEquals(18, CommonOps_R64.elementSum(A), UtilEjml.TEST_F64);
    }

    @Test
    public void kron() {
        RowMatrix_F64 A = new RowMatrix_F64(2,2, true, 1, 2, 3, 4);
        RowMatrix_F64 B = new RowMatrix_F64(1,2, true, 4, 5);

        RowMatrix_F64 C = new RowMatrix_F64(2,4);
        RowMatrix_F64 C_expected = new RowMatrix_F64(2,4, true, 4, 5, 8, 10, 12, 15, 16, 20);

        CommonOps_R64.kron(A, B, C);

        assertTrue(MatrixFeatures_R64.isIdentical(C, C_expected, UtilEjml.TEST_F64));

        // test various shapes for problems
        for( int i = 1; i <= 3; i++ ) {
            for( int j = 1; j <= 3; j++ ) {
                for( int k = 1; k <= 3; k++ ) {
                    for( int l = 1; l <= 3; l++ ) {
                        A = RandomMatrices_R64.createRandom(i,j,rand);
                        B = RandomMatrices_R64.createRandom(k,l,rand);
                        C = new RowMatrix_F64(A.numRows*B.numRows,A.numCols*B.numCols);

                        CommonOps_R64.kron(A,B,C);

                        assertEquals(i*k,C.numRows);
                        assertEquals(j*l,C.numCols);
                    }
                }
            }
        }
    }

    @Test
    public void extract() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(5,5, 0, 1, rand);

        RowMatrix_F64 B = new RowMatrix_F64(2,3);

        CommonOps_R64.extract(A, 1, 3, 2, 5, B, 0, 0);

        for( int i = 1; i < 3; i++ ) {
            for( int j = 2; j < 5; j++ ) {
                assertEquals(A.get(i,j),B.get(i-1,j-2),UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void extract_ret() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(5,5, 0, 1, rand);

        RowMatrix_F64 B = CommonOps_R64.extract(A,1,3,2,5);

        assertEquals(B.numRows,2);
        assertEquals(B.numCols, 3);

        for( int i = 1; i < 3; i++ ) {
            for( int j = 2; j < 5; j++ ) {
                assertEquals(A.get(i,j),B.get(i-1,j-2),UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void extract_array_two() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(5,5, 0, 1, rand);

        int rows[] = new int[6];
        rows[0] = 2;
        rows[1] = 4;

        int cols[] = new int[4];
        cols[0] = 1;
        RowMatrix_F64 B = new RowMatrix_F64(2,1);
        CommonOps_R64.extract(A,rows,2,cols,1,B);

        assertEquals(B.numRows,2);
        assertEquals(B.numCols, 1);

        for( int i = 0; i < 2; i++ ) {
            for( int j = 0; j < 1; j++ ) {
                assertEquals(A.get(rows[i],cols[j]),B.get(i,j),UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void extract_array_one() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(5,5, 0, 1, rand);

        int indexes[] = new int[6];
        indexes[0] = 2;
        indexes[1] = 4;

        RowMatrix_F64 B = new RowMatrix_F64(2,1);
        CommonOps_R64.extract(A,indexes,2,B);

        assertEquals(B.numRows,2);
        assertEquals(B.numCols, 1);

        for( int i = 0; i < 2; i++ ) {
            assertEquals(A.get(indexes[i]),B.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void insert_array_two() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(2,1, 0, 1, rand);

        int rows[] = new int[6];
        rows[0] = 2;
        rows[1] = 4;

        int cols[] = new int[4];
        cols[0] = 1;
        RowMatrix_F64 B = new RowMatrix_F64(5,5);
        CommonOps_R64.insert(A, B, rows, 2, cols, 1);


        for( int i = 0; i < 2; i++ ) {
            for( int j = 0; j < 1; j++ ) {
                assertEquals(A.get(i,j),B.get(rows[i],cols[j]),UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void extractDiag() {
        RowMatrix_F64 a = RandomMatrices_R64.createRandom(3,4, 0, 1, rand);

        for( int i = 0; i < 3; i++ ) {
            a.set(i,i,i+1);
        }

        RowMatrix_F64 v = new RowMatrix_F64(3,1);
        CommonOps_R64.extractDiag(a, v);

        for( int i = 0; i < 3; i++ ) {
            assertEquals( i+1 , v.get(i) , 1e-8 );
        }
    }

    @Test
    public void extractRow() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(5,6, 0, 1, rand);

        RowMatrix_F64 B = CommonOps_R64.extractRow(A, 3, null);

        assertEquals(B.numRows,1);
        assertEquals(B.numCols, 6);

        for( int i = 0; i < 6; i++ ) {
            assertEquals(A.get(3,i),B.get(0,i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void extractColumn() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(5,6, 0, 1, rand);

        RowMatrix_F64 B = CommonOps_R64.extractColumn(A, 3, null);

        assertEquals(B.numRows, 5);
        assertEquals(B.numCols, 1);

        for( int i = 0; i < 5; i++ ) {
            assertEquals(A.get(i,3),B.get(i,0),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void insert() {
        RowMatrix_F64 A = new RowMatrix_F64(5,5);
        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < A.numCols; j++ ) {
                A.set(i,j,i*A.numRows+j);
            }
        }

        RowMatrix_F64 B = new RowMatrix_F64(8,8);

        CommonOps_R64.insert(A, B, 1, 2);

        for( int i = 1; i < 6; i++ ) {
            for( int j = 2; j < 7; j++ ) {
                assertEquals(A.get(i-1,j-2),B.get(i,j),UtilEjml.TEST_F64);
            }
        }
    }

   @Test
    public void addEquals() {
        RowMatrix_F64 a = new RowMatrix_F64(2,3, true, 0, 1, 2, 3, 4, 5);
        RowMatrix_F64 b = new RowMatrix_F64(2,3, true, 5, 4, 3, 2, 1, 0);

        CommonOps_R64.addEquals(a, b);

        UtilTestMatrix.checkMat(a,5,5,5,5,5,5);
        UtilTestMatrix.checkMat(b, 5, 4, 3, 2, 1, 0);
    }

    @Test
    public void addEquals_beta() {
        RowMatrix_F64 a = new RowMatrix_F64(2,3, true, 0, 1, 2, 3, 4, 5);
        RowMatrix_F64 b = new RowMatrix_F64(2,3, true, 5, 4, 3, 2, 1, 0);

        CommonOps_R64.addEquals(a, 2.0, b);

        UtilTestMatrix.checkMat(a,10,9,8,7,6,5);
        UtilTestMatrix.checkMat(b,5,4,3,2,1,0);
    }

    @Test
    public void add() {
        RowMatrix_F64 a = new RowMatrix_F64(2,3, true, 0, 1, 2, 3, 4, 5);
        RowMatrix_F64 b = new RowMatrix_F64(2,3, true, 5, 4, 3, 2, 1, 0);
        RowMatrix_F64 c = RandomMatrices_R64.createRandom(2,3,rand);

        CommonOps_R64.add(a,b,c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(b,5,4,3,2,1,0);
        UtilTestMatrix.checkMat(c,5,5,5,5,5,5);
    }

    @Test
    public void add_beta() {
        RowMatrix_F64 a = new RowMatrix_F64(2,3, true, 0, 1, 2, 3, 4, 5);
        RowMatrix_F64 b = new RowMatrix_F64(2,3, true, 5, 4, 3, 2, 1, 0);
        RowMatrix_F64 c = RandomMatrices_R64.createRandom(2,3,rand);

        CommonOps_R64.add(a,2.0,b,c);

        UtilTestMatrix.checkMat(a,0,1,2,3,4,5);
        UtilTestMatrix.checkMat(b,5,4,3,2,1,0);
        UtilTestMatrix.checkMat(c,10,9,8,7,6,5);
    }

    @Test
    public void add_alpha_beta() {
        RowMatrix_F64 a = new RowMatrix_F64(2,3, true, 0, 1, 2, 3, 4, 5);
        RowMatrix_F64 b = new RowMatrix_F64(2,3, true, 5, 4, 3, 2, 1, 0);
        RowMatrix_F64 c = RandomMatrices_R64.createRandom(2,3,rand);

        CommonOps_R64.add(2.0,a,2.0,b,c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(b,5,4,3,2,1,0);
        UtilTestMatrix.checkMat(c,10,10,10,10,10,10);
    }

    @Test
    public void add_alpha() {
        RowMatrix_F64 a = new RowMatrix_F64(2,3, true, 0, 1, 2, 3, 4, 5);
        RowMatrix_F64 b = new RowMatrix_F64(2,3, true, 5, 4, 3, 2, 1, 0);
        RowMatrix_F64 c = RandomMatrices_R64.createRandom(2,3,rand);

        CommonOps_R64.add(2.0,a,b,c);

        UtilTestMatrix.checkMat(a,0,1,2,3,4,5);
        UtilTestMatrix.checkMat(b, 5, 4, 3, 2, 1, 0);
        UtilTestMatrix.checkMat(c,5,6,7,8,9,10);
    }

    @Test
    public void add_scalar_c() {
        RowMatrix_F64 a = new RowMatrix_F64(2,3, true, 0, 1, 2, 3, 4, 5);
        RowMatrix_F64 c = RandomMatrices_R64.createRandom(2,3,rand);

        CommonOps_R64.add(a, 2.0, c);

        UtilTestMatrix.checkMat(a,0,1,2,3,4,5);
        UtilTestMatrix.checkMat(c,2,3,4,5,6,7);
    }

    @Test
    public void add_scalar() {
        RowMatrix_F64 a = new RowMatrix_F64(2,3, true, 0, 1, 2, 3, 4, 5);

        CommonOps_R64.add(a, 2.0);

        UtilTestMatrix.checkMat(a,2,3,4,5,6,7);
    }

    @Test
    public void subEquals() {
        RowMatrix_F64 a = new RowMatrix_F64(2,3, true, 0, 1, 2, 3, 4, 5);
        RowMatrix_F64 b = new RowMatrix_F64(2,3, true, 5, 5, 5, 5, 5, 5);

        CommonOps_R64.subtractEquals(a, b);

        UtilTestMatrix.checkMat(a, -5, -4, -3, -2, -1, 0);
        UtilTestMatrix.checkMat(b,5,5,5,5,5,5);
    }

    @Test
    public void subtract_matrix_matrix() {
        RowMatrix_F64 a = new RowMatrix_F64(2,3, true, 0, 1, 2, 3, 4, 5);
        RowMatrix_F64 b = new RowMatrix_F64(2,3, true, 5, 5, 5, 5, 5, 5);
        RowMatrix_F64 c = RandomMatrices_R64.createRandom(2,3,rand);

        CommonOps_R64.subtract(a, b, c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(b,5,5,5,5,5,5);
        UtilTestMatrix.checkMat(c,-5,-4,-3,-2,-1,0);
    }

    @Test
    public void subtract_matrix_double() {
        RowMatrix_F64 a = new RowMatrix_F64(2,3, true, 0, 1, 2, 3, 4, 5);
        RowMatrix_F64 c = RandomMatrices_R64.createRandom(2,3,rand);

        CommonOps_R64.subtract(a, 2, c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(c, -2, -1, 0, 1, 2, 3);
    }

    @Test
    public void subtract_double_matrix() {
        RowMatrix_F64 a = new RowMatrix_F64(2,3, true, 0, 1, 2, 3, 4, 5);
        RowMatrix_F64 c = RandomMatrices_R64.createRandom(2,3,rand);

        CommonOps_R64.subtract(2, a, c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(c, 2, 1, 0, -1, -2, -3);
    }

    @Test
    public void scale() {
        double s = 2.5;
        double d[] = new double[]{10,12.5,-2,5.5};
        RowMatrix_F64 mat = new RowMatrix_F64(2,2, true, d);

        CommonOps_R64.scale(s, mat);

        assertEquals(d[0]*s,mat.get(0,0),UtilEjml.TEST_F64);
        assertEquals(d[1] * s, mat.get(0, 1), UtilEjml.TEST_F64);
        assertEquals(d[2] * s, mat.get(1, 0), UtilEjml.TEST_F64);
        assertEquals(d[3] * s, mat.get(1, 1), UtilEjml.TEST_F64);
    }

    @Test
    public void scale_two_input() {
        double s = 2.5;
        double d[] = new double[]{10,12.5,-2,5.5};
        RowMatrix_F64 mat = new RowMatrix_F64(2,2, true, d);
        RowMatrix_F64 r = new RowMatrix_F64(2,2, true, d);

        CommonOps_R64.scale(s,mat,r);

        assertEquals(d[0],mat.get(0,0),UtilEjml.TEST_F64);
        assertEquals(d[1],mat.get(0,1),UtilEjml.TEST_F64);
        assertEquals(d[2],mat.get(1,0),UtilEjml.TEST_F64);
        assertEquals(d[3], mat.get(1, 1), UtilEjml.TEST_F64);

        assertEquals(d[0]*s,r.get(0,0),UtilEjml.TEST_F64);
        assertEquals(d[1]*s,r.get(0,1),UtilEjml.TEST_F64);
        assertEquals(d[2]*s,r.get(1,0),UtilEjml.TEST_F64);
        assertEquals(d[3]*s,r.get(1,1),UtilEjml.TEST_F64);
    }

    @Test
    public void div_scalar_mat() {
        double s = 2.5;
        double d[] = new double[]{10,12.5,-2,5.5};
        RowMatrix_F64 mat = new RowMatrix_F64(2,2, true, d);

        CommonOps_R64.divide(s, mat);

        assertEquals(s/d[0],mat.get(0,0),UtilEjml.TEST_F64);
        assertEquals(s/d[1],mat.get(0,1),UtilEjml.TEST_F64);
        assertEquals(s/d[2],mat.get(1,0),UtilEjml.TEST_F64);
        assertEquals(s/d[3],mat.get(1,1),UtilEjml.TEST_F64);
    }

    @Test
    public void div_mat_scalar() {
        double s = 2.5;
        double d[] = new double[]{10,12.5,-2,5.5};
        RowMatrix_F64 mat = new RowMatrix_F64(2,2, true, d);

        CommonOps_R64.divide(mat, s);

        assertEquals(mat.get(0,0),d[0]/s,UtilEjml.TEST_F64);
        assertEquals(mat.get(0,1),d[1]/s,UtilEjml.TEST_F64);
        assertEquals(mat.get(1,0),d[2]/s,UtilEjml.TEST_F64);
        assertEquals(mat.get(1,1),d[3]/s,UtilEjml.TEST_F64);
    }

    @Test
    public void div_mat_scalar_out() {
        double s = 2.5;
        double d[] = new double[]{10,12.5,-2,5.5};
        RowMatrix_F64 mat = new RowMatrix_F64(2,2, true, d);
        RowMatrix_F64 r = new RowMatrix_F64(2,2, true, d);

        CommonOps_R64.divide(mat,s,r);

        assertEquals(d[0],mat.get(0,0),UtilEjml.TEST_F64);
        assertEquals(d[1],mat.get(0,1),UtilEjml.TEST_F64);
        assertEquals(d[2],mat.get(1,0),UtilEjml.TEST_F64);
        assertEquals(d[3], mat.get(1, 1), UtilEjml.TEST_F64);

        assertEquals(d[0]/s,r.get(0,0),UtilEjml.TEST_F64);
        assertEquals(d[1]/s,r.get(0,1),UtilEjml.TEST_F64);
        assertEquals(d[2]/s,r.get(1,0),UtilEjml.TEST_F64);
        assertEquals(d[3] / s, r.get(1, 1), UtilEjml.TEST_F64);
    }

    @Test
    public void div_scalar_mat_out() {
        double s = 2.5;
        double d[] = new double[]{10,12.5,-2,5.5};
        RowMatrix_F64 mat = new RowMatrix_F64(2,2, true, d);
        RowMatrix_F64 r = new RowMatrix_F64(2,2, true, d);

        CommonOps_R64.divide(s,mat,r);

        assertEquals(d[0],mat.get(0,0),UtilEjml.TEST_F64);
        assertEquals(d[1],mat.get(0,1),UtilEjml.TEST_F64);
        assertEquals(d[2],mat.get(1,0),UtilEjml.TEST_F64);
        assertEquals(d[3],mat.get(1,1),UtilEjml.TEST_F64);

        assertEquals(s/d[0],r.get(0,0),UtilEjml.TEST_F64);
        assertEquals(s/d[1],r.get(0,1),UtilEjml.TEST_F64);
        assertEquals(s/d[2],r.get(1,0),UtilEjml.TEST_F64);
        assertEquals(s / d[3], r.get(1, 1), UtilEjml.TEST_F64);
    }

    @Test
    public void changeSign_one() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(2,3,rand);
        RowMatrix_F64 A_orig = A.copy();

        CommonOps_R64.changeSign(A);

        for (int i = 0; i < A.getNumElements(); i++) {
            assertEquals(-A.get(i),A_orig.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void changeSign_two() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(2,3,rand);
        RowMatrix_F64 B = RandomMatrices_R64.createRandom(2, 3, rand);

        CommonOps_R64.changeSign(A, B);

        for (int i = 0; i < A.getNumElements(); i++) {
            assertEquals(A.get(i),-B.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void fill_dense() {
        double d[] = new double[]{10,12.5,-2,5.5};
        RowMatrix_F64 mat = new RowMatrix_F64(2,2, true, d);

        CommonOps_R64.fill(mat, 1);

        for( int i = 0; i < mat.getNumElements(); i++ ) {
            assertEquals(1,mat.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void fill_block() {
        // pick the size such that it doesn't nicely line up along blocks
        BlockMatrix_F64 mat = new BlockMatrix_F64(10,14,3);

        CommonOps_R64.fill(mat, 1.5);

        for( int i = 0; i < mat.getNumElements(); i++ ) {
            assertEquals(1.5,mat.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void zero() {
        double d[] = new double[]{10,12.5,-2,5.5};
        RowMatrix_F64 mat = new RowMatrix_F64(2,2, true, d);

        mat.zero();

        for( int i = 0; i < mat.getNumElements(); i++ ) {
            assertEquals(0,mat.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementMax() {
        RowMatrix_F64 mat = new RowMatrix_F64(3,3, true, 0, 1, -2, 3, 4, 5, 6, 7, -8);

        double m = CommonOps_R64.elementMax(mat);
        assertEquals(7, m, UtilEjml.TEST_F64);
    }

    @Test
    public void elementMin() {
        RowMatrix_F64 mat = new RowMatrix_F64(3,3, true, 0, 1, 2, -3, 4, 5, 6, 7, 8);

        double m = CommonOps_R64.elementMin(mat);
        assertEquals(-3,m,UtilEjml.TEST_F64);
    }

    @Test
    public void elementMinAbs() {
        RowMatrix_F64 mat = new RowMatrix_F64(3,3, true, 0, 1, -2, 3, 4, 5, 6, 7, -8);

        double m = CommonOps_R64.elementMinAbs(mat);
        assertEquals(0,m,UtilEjml.TEST_F64);
    }

    @Test
    public void elementMaxAbs() {
        RowMatrix_F64 mat = new RowMatrix_F64(3,3, true, 0, 1, 2, 3, 4, 5, -6, 7, -8);

        double m = CommonOps_R64.elementMaxAbs(mat);
        assertEquals(8,m,UtilEjml.TEST_F64);
    }

    @Test
    public void elementSum() {
        RowMatrix_F64 M = RandomMatrices_R64.createRandom(5,5,rand);
        // make it smaller than the original size to make sure it is bounding
        // the summation correctly
        M.reshape(4, 3, false);

        double sum = 0;
        for( int i = 0; i < M.numRows; i++ ) {
            for( int j = 0; j < M.numCols; j++ ) {
                sum += M.get(i,j);
            }
        }

        assertEquals(sum, CommonOps_R64.elementSum(M),UtilEjml.TEST_F64);
    }

    @Test
    public void elementSumAbs() {
        RowMatrix_F64 M = RandomMatrices_R64.createRandom(5,5,rand);
        // make it smaller than the original size to make sure it is bounding
        // the summation correctly
        M.reshape(4, 3, false);

        double sum = 0;
        for( int i = 0; i < M.numRows; i++ ) {
            for( int j = 0; j < M.numCols; j++ ) {
                sum += Math.abs(M.get(i, j));
            }
        }

        assertEquals(sum, CommonOps_R64.elementSum(M),UtilEjml.TEST_F64);
    }

    @Test
    public void elementPower_mm() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(4, 5, rand);
        RowMatrix_F64 B = RandomMatrices_R64.createRandom(4, 5, rand);
        RowMatrix_F64 C = RandomMatrices_R64.createRandom(4, 5, rand);

        CommonOps_R64.elementPower(A, B, C);

        for (int i = 0; i < A.getNumElements(); i++) {
            double expected = Math.pow( A.get(i) , B.get(i));
            assertEquals(expected,C.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementPower_ms() {
        double a = 1.3;
        RowMatrix_F64 B = RandomMatrices_R64.createRandom(4, 5, rand);
        RowMatrix_F64 C = RandomMatrices_R64.createRandom(4, 5, rand);

        CommonOps_R64.elementPower(a, B, C);

        for (int i = 0; i < C.getNumElements(); i++) {
            double expected = Math.pow(a, B.get(i));
            assertEquals(expected,C.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementPower_sm() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(4, 5, rand);
        double b = 1.1;
        RowMatrix_F64 C = RandomMatrices_R64.createRandom(4, 5, rand);

        CommonOps_R64.elementPower(A, b, C);

        for (int i = 0; i < A.getNumElements(); i++) {
            double expected = Math.pow(A.get(i), b);
            assertEquals(expected,C.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementLog() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(4, 5, rand);
        RowMatrix_F64 C = RandomMatrices_R64.createRandom(4, 5, rand);

        CommonOps_R64.elementLog(A, C);

        for (int i = 0; i < A.getNumElements(); i++) {
            double expected = Math.log(A.get(i));
            assertEquals(expected,C.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementExp() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(4, 5, rand);
        RowMatrix_F64 C = RandomMatrices_R64.createRandom(4, 5, rand);

        CommonOps_R64.elementExp(A, C);

        for (int i = 0; i < A.getNumElements(); i++) {
            double expected = Math.exp(A.get(i));
            assertEquals(expected,C.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void sumRows() {
        RowMatrix_F64 input = RandomMatrices_R64.createRandom(4,5,rand);
        RowMatrix_F64 output = new RowMatrix_F64(4,1);

        assertTrue( output == CommonOps_R64.sumRows(input,output));

        for( int i = 0; i < input.numRows; i++ ) {
            double total = 0;
            for( int j = 0; j < input.numCols; j++ ) {
                total += input.get(i,j);
            }
            assertEquals( total, output.get(i),UtilEjml.TEST_F64);
        }

        // check with a null output
        RowMatrix_F64 output2 = CommonOps_R64.sumRows(input, null);

        EjmlUnitTests.assertEquals(output, output2, UtilEjml.TEST_F64);
    }

    @Test
    public void sumCols() {
        RowMatrix_F64 input = RandomMatrices_R64.createRandom(4,5,rand);
        RowMatrix_F64 output = new RowMatrix_F64(1,5);

        assertTrue( output == CommonOps_R64.sumCols(input, output));

        for( int i = 0; i < input.numCols; i++ ) {
            double total = 0;
            for( int j = 0; j < input.numRows; j++ ) {
                total += input.get(j,i);
            }
            assertEquals( total, output.get(i),UtilEjml.TEST_F64);
        }

        // check with a null output
        RowMatrix_F64 output2 = CommonOps_R64.sumCols(input, null);

        EjmlUnitTests.assertEquals(output, output2, UtilEjml.TEST_F64);
    }

    @Test
    public void rref() {
        RowMatrix_F64 A = new RowMatrix_F64(4,6,true,
                0,0,1,-1,-1,4,
                2,4,2,4,2,4,
                2,4,3,3,3,4,
                3,6,6,3,6,6);

        RowMatrix_F64 expected = new RowMatrix_F64(4,6,true,
                1,2,0,3,0,2,
                0,0,1,-1,0,2,
                0,0,0,0,1,-2,
                0,0,0,0,0,0);

        RowMatrix_F64 found = CommonOps_R64.rref(A, 5, null);


        assertTrue(MatrixFeatures_R64.isEquals(found, expected));
    }

    @Test
    public void elementLessThan_double() {
        RowMatrix_F64 A = new RowMatrix_F64(3,4);
        RowMatrix_B expected = new RowMatrix_B(3,4);
        RowMatrix_B found = new RowMatrix_B(3,4);

        double value = 5.0;

        for (int i = 0; i < A.getNumElements() ; i++) {
            A.data[i] = i;
            expected.data[i] = i < value;
        }

        CommonOps_R64.elementLessThan(A, value, found);
        assertTrue(MatrixFeatures_R64.isEquals(expected,found));
    }

    @Test
    public void elementLessThanOrEqual_double() {
        RowMatrix_F64 A = new RowMatrix_F64(3,4);
        RowMatrix_B expected = new RowMatrix_B(3,4);
        RowMatrix_B found = new RowMatrix_B(3,4);

        double value = 5.0;

        for (int i = 0; i < A.getNumElements() ; i++) {
            A.data[i] = i;
            expected.data[i] = i <= value;
        }

        CommonOps_R64.elementLessThanOrEqual(A, value, found);
        assertTrue(MatrixFeatures_R64.isEquals(expected, found));
    }

    @Test
    public void elementMoreThan_double() {
        RowMatrix_F64 A = new RowMatrix_F64(3,4);
        RowMatrix_B expected = new RowMatrix_B(3,4);
        RowMatrix_B found = new RowMatrix_B(3,4);

        double value = 5.0;

        for (int i = 0; i < A.getNumElements() ; i++) {
            A.data[i] = i;
            expected.data[i] = i > value;
        }

        CommonOps_R64.elementMoreThan(A, value, found);
        assertTrue(MatrixFeatures_R64.isEquals(expected, found));
    }

    @Test
    public void elementMoreThanOrEqual_double() {
        RowMatrix_F64 A = new RowMatrix_F64(3,4);
        RowMatrix_B expected = new RowMatrix_B(3,4);
        RowMatrix_B found = new RowMatrix_B(3,4);

        double value = 5.0;

        for (int i = 0; i < A.getNumElements() ; i++) {
            A.data[i] = i;
            expected.data[i] = i >= value;
        }

        CommonOps_R64.elementMoreThanOrEqual(A, value, found);
        assertTrue(MatrixFeatures_R64.isEquals(expected, found));
    }

    @Test
    public void elementLessThan_matrix() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(3,4,rand);
        RowMatrix_F64 B = RandomMatrices_R64.createRandom(3,4,rand);
        RowMatrix_B expected = new RowMatrix_B(3,4);
        RowMatrix_B found = new RowMatrix_B(3,4);

        A.data[6] = B.data[6];

        for (int i = 0; i < A.getNumElements() ; i++) {
            expected.data[i] = A.data[i] < B.data[i];
        }

        CommonOps_R64.elementLessThan(A, B, found);
        assertTrue(MatrixFeatures_R64.isEquals(expected, found));
    }

    @Test
    public void elementLessThanOrEqual_matrix() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(3,4,rand);
        RowMatrix_F64 B = RandomMatrices_R64.createRandom(3,4,rand);
        RowMatrix_B expected = new RowMatrix_B(3,4);
        RowMatrix_B found = new RowMatrix_B(3,4);

        A.data[6] = B.data[6];

        for (int i = 0; i < A.getNumElements() ; i++) {
            expected.data[i] = A.data[i] <= B.data[i];
        }

        CommonOps_R64.elementLessThanOrEqual(A, B, found);
        assertTrue(MatrixFeatures_R64.isEquals(expected, found));
    }

    @Test
    public void elements() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(3,4,rand);

        RowMatrix_B B = RandomMatrices_R64.createRandomB(3, 4, rand);

        RowMatrix_F64 found = CommonOps_R64.elements(A, B, null);

        int index = 0;

        for (int i = 0; i < B.getNumElements(); i++) {
            if( B.get(i) ) {
                assertEquals(found.get(index++),A.get(i),UtilEjml.TEST_F64);
            }
        }

        assertEquals(index,found.getNumRows());
        assertEquals(1,found.getNumCols());
    }

    @Test
    public void countTrue() {
        RowMatrix_B B = RandomMatrices_R64.createRandomB(4, 5, rand);


        int index = 0;

        for (int i = 0; i < B.getNumElements(); i++) {
            if( B.get(i) ) {
                index++;
            }
        }

        assertTrue(index>5);
        assertEquals(index, CommonOps_R64.countTrue(B));
    }
}
