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
import org.ejml.data.*;
import org.ejml.dense.row.decomposition.lu.LUDecompositionAlt_FDRM;
import org.ejml.dense.row.linsol.lu.LinearSolverLu_FDRM;
import org.ejml.dense.row.mult.CheckMatrixMultShape_FDRM;
import org.ejml.dense.row.mult.MatrixMatrixMult_FDRM;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;

import static org.ejml.UtilEjml.checkSameShape;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestCommonOps_FDRM {

    Random rand = new Random(0xFF);
    float tol = UtilEjml.TEST_F32;

    @Test
    public void checkInputShape() {
        CheckMatrixMultShape_FDRM check = new CheckMatrixMultShape_FDRM(CommonOps_FDRM.class);
        check.checkAll();
    }

    /**
     * Make sure the multiplication methods here have the same behavior as the ones in MatrixMatrixMult.
     */
    @Test
    public void checkAllMatrixMults() {
        int numChecked = 0;
        Method[] methods = CommonOps_FDRM.class.getMethods();

        boolean oneFailed = false;

        for( Method method : methods ) {
            String name = method.getName();

            // only look at function which perform matrix multiplication
            if( !name.contains("mult") || name.contains("Element") || 
                    name.contains("Inner") || name.contains("Outer"))
                continue;
            if( name.equals("multRows") || name.equals("multCols"))
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
        Method[] methods = CommonOps_FDRM.class.getMethods();

        boolean oneFailed = false;

        for( Method method : methods ) {
            String name = method.getName();

            // only look at function which perform matrix multiplication
            if( !name.contains("mult") || name.contains("Element") ||
                    name.contains("Inner") || name.contains("Outer"))
                continue;
            if( name.equals("multRows") || name.equals("multCols"))
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
                checkMethod = MatrixMatrixMult_FDRM.class.getMethod(
                        name,float.class,
                        FMatrix1Row.class, FMatrix1Row.class,FMatrix1Row.class);
            else
                checkMethod = MatrixMatrixMult_FDRM.class.getMethod(
                        name, FMatrix1Row.class, FMatrix1Row.class,FMatrix1Row.class);
        } catch (NoSuchMethodException e) {
            checkMethod = null;
        }
        if( checkMethod == null ) {
            try {
            if( hasAlpha )
                checkMethod = MatrixMatrixMult_FDRM.class.getMethod(
                        name+"_reorder",float.class,
                        FMatrix1Row.class, FMatrix1Row.class,FMatrix1Row.class);
            else
                checkMethod = MatrixMatrixMult_FDRM.class.getMethod(
                        name+"_reorder", FMatrix1Row.class, FMatrix1Row.class,FMatrix1Row.class);
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
            FMatrixRMaj a;
            if( tranA ) a = RandomMatrices_FDRM.rectangle(i+1,i,rand);
            else  a = RandomMatrices_FDRM.rectangle(i,i+1,rand);

            FMatrixRMaj b;
            if( tranB ) b = RandomMatrices_FDRM.rectangle(i,i+1,rand);
            else  b = RandomMatrices_FDRM.rectangle(i+1,i,rand);

            FMatrixRMaj c = RandomMatrices_FDRM.rectangle(i,i,rand);
            FMatrixRMaj c_alt = c.copy();

            if( hasAlpha ) {
                method.invoke(null,2.0f,a,b,c);
                checkMethod.invoke(null,2.0f,a,b,c_alt);
            } else {
                method.invoke(null,a,b,c);
                checkMethod.invoke(null,a,b,c_alt);
            }

            if( !MatrixFeatures_FDRM.isIdentical(c_alt,c,tol))
                return false;
        }

        // check various sizes column vector
        for( int i = 1; i < 4; i++ ) {
            FMatrixRMaj a;
            if( tranA ) a = RandomMatrices_FDRM.rectangle(i,i+1,rand);
            else  a = RandomMatrices_FDRM.rectangle(i+1,i,rand);

            FMatrixRMaj b;
            if( tranB ) b = RandomMatrices_FDRM.rectangle(1,i,rand);
            else  b = RandomMatrices_FDRM.rectangle(i,1,rand);

            FMatrixRMaj c = RandomMatrices_FDRM.rectangle(i+1,1,rand);
            FMatrixRMaj c_alt = c.copy();

            if( hasAlpha ) {
                method.invoke(null,2.0f,a,b,c);
                checkMethod.invoke(null,2.0f,a,b,c_alt);
            } else {
                method.invoke(null,a,b,c);
                checkMethod.invoke(null,a,b,c_alt);
            }

            if( !MatrixFeatures_FDRM.isIdentical(c_alt,c,tol))
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
        FMatrixRMaj a = tranA ? new FMatrixRMaj(colsA,rowsA) : new FMatrixRMaj(rowsA,colsA);
        FMatrixRMaj b = tranB ? new FMatrixRMaj(colsB,rowsB) : new FMatrixRMaj(rowsB,colsB);

        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(rowsA,colsB,rand);

        if( hasAlpha ) {
            method.invoke(null,2.0f,a,b,c);
        } else {
            method.invoke(null,a,b,c);
        }

        if( add ) {
            FMatrixRMaj corig = c.copy();
            assertTrue(MatrixFeatures_FDRM.isIdentical(corig, c, UtilEjml.TEST_F32));
        } else {
            assertTrue(MatrixFeatures_FDRM.isZeros(c, UtilEjml.TEST_F32));
        }

        return true;
    }

    @Test
    public void dot() {
        FMatrixRMaj a = RandomMatrices_FDRM.rectangle(10, 1, rand);
        FMatrixRMaj b = RandomMatrices_FDRM.rectangle(1,10,rand);

        float found = CommonOps_FDRM.dot(a, b);

        float expected = 0;
        for (int i = 0; i < 10; i++) {
            expected += a.data[i]*b.data[i];
        }

        assertEquals(expected, found, UtilEjml.TEST_F32);
    }

    @Test
    public void multInner() {
        FMatrixRMaj a = RandomMatrices_FDRM.rectangle(10,4,rand);
        FMatrixRMaj found = RandomMatrices_FDRM.rectangle(4,4,rand);
        FMatrixRMaj expected = RandomMatrices_FDRM.rectangle(4, 4, rand);

        CommonOps_FDRM.multTransA(a, a, expected);
        CommonOps_FDRM.multInner(a,found);

        assertTrue(MatrixFeatures_FDRM.isIdentical(expected, found, tol));
    }

    @Test
    public void multOuter() {
        FMatrixRMaj a = RandomMatrices_FDRM.rectangle(10,4,rand);
        FMatrixRMaj found = RandomMatrices_FDRM.rectangle(10,10,rand);
        FMatrixRMaj expected = RandomMatrices_FDRM.rectangle(10,10,rand);

        CommonOps_FDRM.multTransB(a, a, expected);
        CommonOps_FDRM.multOuter(a, found);

        assertTrue(MatrixFeatures_FDRM.isIdentical(expected, found, tol));
    }
    
    @Test
    public void elementMult_two() {
        FMatrixRMaj a = RandomMatrices_FDRM.rectangle(5,4,rand);
        FMatrixRMaj b = RandomMatrices_FDRM.rectangle(5,4,rand);
        FMatrixRMaj a_orig = a.copy();

        CommonOps_FDRM.elementMult(a, b);

        for( int i = 0; i < 20; i++ ) {
            assertEquals(a.get(i),b.get(i)*a_orig.get(i),1e-6);
        }
    }

    @Test
    public void elementMult_three() {
        FMatrixRMaj a = RandomMatrices_FDRM.rectangle(5,4,rand);
        FMatrixRMaj b = RandomMatrices_FDRM.rectangle(5,4,rand);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(5, 4, rand);

        CommonOps_FDRM.elementMult(a, b, c);

        for( int i = 0; i < 20; i++ ) {
            assertEquals(c.get(i),b.get(i)*a.get(i),1e-6);
        }
    }

    @Test
    public void elementDiv_two() {
        FMatrixRMaj a = RandomMatrices_FDRM.rectangle(5,4,rand);
        FMatrixRMaj b = RandomMatrices_FDRM.rectangle(5,4,rand);
        FMatrixRMaj a_orig = a.copy();

        CommonOps_FDRM.elementDiv(a, b);

        for( int i = 0; i < 20; i++ ) {
            assertEquals(a.get(i),a_orig.get(i)/b.get(i),1e-6);
        }
    }

    @Test
    public void elementDiv_three() {
        FMatrixRMaj a = RandomMatrices_FDRM.rectangle(5,4,rand);
        FMatrixRMaj b = RandomMatrices_FDRM.rectangle(5,4,rand);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(5, 4, rand);

        CommonOps_FDRM.elementDiv(a, b, c);

        for( int i = 0; i < 20; i++ ) {
            assertEquals(c.get(i),a.get(i)/b.get(i),1e-6);
        }
    }

    @Test
    public void multRows() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,4,rand);
        FMatrixRMaj found = A.copy();
        float[] values = UtilEjml.randomVector_F32(rand, 5);

        CommonOps_FDRM.multRows(values, found);

        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                assertEquals(A.get(row,col)*values[row], found.get(row,col), UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void divideRows() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,4,rand);
        FMatrixRMaj found = A.copy();
        float[] values = UtilEjml.randomVector_F32(rand, 5);

        CommonOps_FDRM.divideRows(values, found);

        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                assertEquals(A.get(row,col)/values[row], found.get(row,col), UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void multCols() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,4,rand);
        FMatrixRMaj found = A.copy();
        float[] values = UtilEjml.randomVector_F32(rand, 5);

        CommonOps_FDRM.multCols(found,values);

        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                assertEquals(A.get(row,col)*values[col], found.get(row,col), UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void divideCols() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,4,rand);
        FMatrixRMaj found = A.copy();
        float[] values = UtilEjml.randomVector_F32(rand, 5);

        CommonOps_FDRM.divideCols(found,values);

        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                assertEquals(A.get(row,col)/values[col], found.get(row,col), UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void divideRowsCols() {
        int rows = 5;
        int cols = 7;

        FMatrixRMaj B = RandomMatrices_FDRM.rectangle(rows,cols,rand);
        FMatrixRMaj found = B.copy();

        float[] diagA = new float[B.numRows];
        for (int i = 0; i < B.numRows; i++) {
            diagA[i] = rand.nextFloat()+0.1f;
        }

        float[] diagC = new float[B.numCols];
        for (int i = 0; i < B.numCols; i++) {
            diagC[i] = rand.nextFloat()+0.1f;
        }

        FMatrixRMaj A = CommonOps_FDRM.diag(diagA);
        FMatrixRMaj C = CommonOps_FDRM.diag(diagC);

        // invert the matrices
        for (int i = 0; i < A.numRows; i++) {
            A.set(i,i, 1.0f/A.get(i,i));
        }
        for (int i = 0; i < C.numCols; i++) {
            C.set(i,i, 1.0f/C.get(i,i));
        }

        FMatrixRMaj AB = new FMatrixRMaj(1,1);
        CommonOps_FDRM.mult(A,B,AB);
        FMatrixRMaj expected = new FMatrixRMaj(1,1);
        CommonOps_FDRM.mult(AB,C,expected);

        CommonOps_FDRM.divideRowsCols(diagA,0,found,diagC,0);

        assertTrue( MatrixFeatures_FDRM.isEquals(expected,found, UtilEjml.TEST_F32));
    }

    @Test
    public void solve() {
        FMatrixRMaj a = new FMatrixRMaj(2,2, true, 1, 2, 7, -3);
        FMatrixRMaj b = RandomMatrices_FDRM.rectangle(2,5,rand);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(2,5,rand);
        FMatrixRMaj c_exp = RandomMatrices_FDRM.rectangle(2,5,rand);

        assertTrue(CommonOps_FDRM.solve(a,b,c));
        LUDecompositionAlt_FDRM alg = new LUDecompositionAlt_FDRM();
        LinearSolverLu_FDRM solver = new LinearSolverLu_FDRM(alg);
        assertTrue(solver.setA(a));

        solver.solve(b, c_exp);

        EjmlUnitTests.assertEquals(c_exp, c, UtilEjml.TEST_F32);
    }

    @Test
    public void solveSPD() {
        for( int N = 1; N <= 20; N++ ) {
            FMatrixRMaj A = RandomMatrices_FDRM.symmetricPosDef(N,rand);

            for (int j = 1; j <= 2; j++) {
//                System.out.println(N+" "+j);
                FMatrixRMaj X = RandomMatrices_FDRM.rectangle(N,j,rand);
                FMatrixRMaj B = X.createLike();
                CommonOps_FDRM.mult(A,X,B);

                FMatrixRMaj found = X.createLike();

                assertTrue(CommonOps_FDRM.solveSPD(A,B,found));

                assertTrue(MatrixFeatures_FDRM.isIdentical(X,found, UtilEjml.TEST_F32));
            }
        }
    }

    @Test
    public void transpose_inplace() {
        FMatrixRMaj mat = new FMatrixRMaj(3,3, true, 0, 1, 2, 3, 4, 5, 6, 7, 8);
        FMatrixRMaj matTran = new FMatrixRMaj(3,3);

        CommonOps_FDRM.transpose(mat, matTran);
        CommonOps_FDRM.transpose(mat);

        EjmlUnitTests.assertEquals(mat, matTran, UtilEjml.TEST_F32);
    }

    @Test
    public void transpose() {
        FMatrixRMaj mat = new FMatrixRMaj(3,2, true, 0, 1, 2, 3, 4, 5);
        FMatrixRMaj matTran = new FMatrixRMaj(2,3);

        CommonOps_FDRM.transpose(mat,matTran);

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
        FMatrixRMaj mat = new FMatrixRMaj(3,3, true, 0, 1, 2, 3, 4, 5, 6, 7, 8);

        assertEquals(12, CommonOps_FDRM.trace(mat), 1e-6);

        // non square
        FMatrixRMaj B = RandomMatrices_FDRM.rectangle(4,3,rand);
        CommonOps_FDRM.insert(mat, B, 0, 0);
        assertEquals(12, CommonOps_FDRM.trace(B), 1e-6);

        B = RandomMatrices_FDRM.rectangle(3,4,rand);
        CommonOps_FDRM.insert(mat, B, 0, 0);
        assertEquals(12, CommonOps_FDRM.trace(B), 1e-6);

    }

    @Test
    public void invert() {
        for( int i = 1; i <= 10; i++ ) {
            FMatrixRMaj a = RandomMatrices_FDRM.rectangle(i,i,rand);

            LUDecompositionAlt_FDRM lu = new LUDecompositionAlt_FDRM();
            LinearSolverLu_FDRM solver = new LinearSolverLu_FDRM(lu);
            assertTrue(solver.setA(a));

            FMatrixRMaj a_inv = new FMatrixRMaj(i,i);
            FMatrixRMaj a_lu = new FMatrixRMaj(i,i);
            solver.invert(a_lu);

            CommonOps_FDRM.invert(a,a_inv);
            CommonOps_FDRM.invert(a);

            EjmlUnitTests.assertEquals(a, a_inv, UtilEjml.TEST_F32);
            EjmlUnitTests.assertEquals(a_lu, a, UtilEjml.TEST_F32);
        }
    }

    @Test
    public void invertSPD() {
        for( int i = 1; i <= 20; i++ ) {
            FMatrixRMaj A = RandomMatrices_FDRM.symmetricPosDef(i,rand);

            FMatrixRMaj invA = A.createLike();

            assertTrue(CommonOps_FDRM.invertSPD(A,invA));

            FMatrixRMaj I = A.createLike();
            CommonOps_FDRM.multTransB(A,invA,I);
            assertTrue(MatrixFeatures_FDRM.isIdentity(I, UtilEjml.TEST_F32));
        }
    }

    /**
     * Checked against by computing a solution to the linear system then
     * seeing if the solution produces the expected output
     */
    @Test
    public void pinv() {
        // check wide matrix
        FMatrixRMaj A = new FMatrixRMaj(2,4,true,1,2,3,4,5,6,7,8);
        FMatrixRMaj A_inv = new FMatrixRMaj(4,2);
        FMatrixRMaj b = new FMatrixRMaj(2,1,true,3,4);
        FMatrixRMaj x = new FMatrixRMaj(4,1);
        FMatrixRMaj found = new FMatrixRMaj(2,1);
        
        CommonOps_FDRM.pinv(A,A_inv);

        CommonOps_FDRM.mult(A_inv,b,x);
        CommonOps_FDRM.mult(A,x,found);

        assertTrue(MatrixFeatures_FDRM.isIdentical(b,found,UtilEjml.TEST_F32_SQ));

        // check tall matrix
        CommonOps_FDRM.transpose(A);
        CommonOps_FDRM.transpose(A_inv);
        b = new FMatrixRMaj(4,1,true,3,4,5,6);
        x.reshape(2,1);
        found.reshape(4,1);

        CommonOps_FDRM.mult(A_inv,b,x);
        CommonOps_FDRM.mult(A, x, found);

        assertTrue(MatrixFeatures_FDRM.isIdentical(b,found,UtilEjml.TEST_F32_SQ));
    }

    @Test
    public void columnsToVectors() {
        FMatrixRMaj M = RandomMatrices_FDRM.rectangle(4, 5, rand);

        FMatrixRMaj[] v = CommonOps_FDRM.columnsToVector(M, null);

        assertEquals(M.numCols,v.length);

        for( int i = 0; i < v.length; i++ ) {
            FMatrixRMaj a = v[i];

            assertEquals(M.numRows,a.numRows);
            assertEquals(1,a.numCols);

            for( int j = 0; j < M.numRows; j++ ) {
                assertEquals(a.get(j),M.get(j,i),UtilEjml.TEST_F32);
            }
        }
    }
    
    @Test
    public void identity() {
        FMatrixRMaj A = CommonOps_FDRM.identity(4);

        assertEquals(4,A.numRows);
        assertEquals(4,A.numCols);

        assertEquals(4, CommonOps_FDRM.elementSum(A),UtilEjml.TEST_F32);
    }

    @Test
    public void identity_rect() {
        FMatrixRMaj A = CommonOps_FDRM.identity(4, 6);

        assertEquals(4,A.numRows);
        assertEquals(6,A.numCols);

        assertEquals(4, CommonOps_FDRM.elementSum(A),UtilEjml.TEST_F32);
    }

    @Test
    public void setIdentity() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(4, 4, rand);

        CommonOps_FDRM.setIdentity(A);

        assertEquals(4,A.numRows);
        assertEquals(4,A.numCols);

        assertEquals(4, CommonOps_FDRM.elementSum(A),UtilEjml.TEST_F32);
    }

    @Test
    public void diag() {
        FMatrixRMaj A = CommonOps_FDRM.diag(2.0f, 3.0f, 6.0f, 7.0f);

        assertEquals(4,A.numRows);
        assertEquals(4,A.numCols);

        assertEquals(2,A.get(0,0),UtilEjml.TEST_F32);
        assertEquals(3, A.get(1, 1), UtilEjml.TEST_F32);
        assertEquals(6, A.get(2, 2), UtilEjml.TEST_F32);
        assertEquals(7, A.get(3, 3), UtilEjml.TEST_F32);

        assertEquals(18, CommonOps_FDRM.elementSum(A),UtilEjml.TEST_F32);
    }

    @Test
    public void diag_rect() {
        FMatrixRMaj A = CommonOps_FDRM.diagR(4, 6, 2.0f, 3.0f, 6.0f, 7.0f);

        assertEquals(4,A.numRows);
        assertEquals(6,A.numCols);

        assertEquals(2,A.get(0,0),UtilEjml.TEST_F32);
        assertEquals(3,A.get(1,1),UtilEjml.TEST_F32);
        assertEquals(6,A.get(2,2),UtilEjml.TEST_F32);
        assertEquals(7,A.get(3,3),UtilEjml.TEST_F32);

        assertEquals(18, CommonOps_FDRM.elementSum(A), UtilEjml.TEST_F32);
    }

    @Test
    public void kron() {
        FMatrixRMaj A = new FMatrixRMaj(2,2, true, 1, 2, 3, 4);
        FMatrixRMaj B = new FMatrixRMaj(1,2, true, 4, 5);

        FMatrixRMaj C = new FMatrixRMaj(2,4);
        FMatrixRMaj C_expected = new FMatrixRMaj(2,4, true, 4, 5, 8, 10, 12, 15, 16, 20);

        CommonOps_FDRM.kron(A, B, C);

        assertTrue(MatrixFeatures_FDRM.isIdentical(C, C_expected, UtilEjml.TEST_F32));

        // test various shapes for problems
        for( int i = 1; i <= 3; i++ ) {
            for( int j = 1; j <= 3; j++ ) {
                for( int k = 1; k <= 3; k++ ) {
                    for( int l = 1; l <= 3; l++ ) {
                        A = RandomMatrices_FDRM.rectangle(i,j,rand);
                        B = RandomMatrices_FDRM.rectangle(k,l,rand);
                        C = new FMatrixRMaj(A.numRows*B.numRows,A.numCols*B.numCols);

                        CommonOps_FDRM.kron(A,B,C);

                        assertEquals(i*k,C.numRows);
                        assertEquals(j*l,C.numCols);
                    }
                }
            }
        }
    }

    @Test
    public void extract() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,5, 0, 1, rand);

        FMatrixRMaj B = new FMatrixRMaj(2,3);

        CommonOps_FDRM.extract(A, 1, 3, 2, 5, B, 0, 0);

        for( int i = 1; i < 3; i++ ) {
            for( int j = 2; j < 5; j++ ) {
                assertEquals(A.get(i,j),B.get(i-1,j-2),UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void extract_ret() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,5, 0, 1, rand);

        FMatrixRMaj B = CommonOps_FDRM.extract(A,1,3,2,5);

        assertEquals(B.numRows,2);
        assertEquals(B.numCols, 3);

        for( int i = 1; i < 3; i++ ) {
            for( int j = 2; j < 5; j++ ) {
                assertEquals(A.get(i,j),B.get(i-1,j-2),UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void extract_no_limits() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,5, 0, 1, rand);
        FMatrixRMaj B = new FMatrixRMaj(3,4);

        CommonOps_FDRM.extract(A,1,1,B);

        assertEquals(B.numRows,3);
        assertEquals(B.numCols, 4);

        for( int i = 1; i < 4; i++ ) {
            for( int j = 1; j < 5; j++ ) {
                assertEquals(A.get(i,j),B.get(i-1,j-1),UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void extract_array_two() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,5, 0, 1, rand);

        int[] rows = new int[6];
        rows[0] = 2;
        rows[1] = 4;

        int[] cols = new int[4];
        cols[0] = 1;
        FMatrixRMaj B = new FMatrixRMaj(2,1);
        CommonOps_FDRM.extract(A,rows,2,cols,1,B);

        assertEquals(B.numRows,2);
        assertEquals(B.numCols, 1);

        for( int i = 0; i < 2; i++ ) {
            for( int j = 0; j < 1; j++ ) {
                assertEquals(A.get(rows[i],cols[j]),B.get(i,j),UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void extract_array_one() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,5, 0, 1, rand);

        int[] indexes = new int[6];
        indexes[0] = 2;
        indexes[1] = 4;

        FMatrixRMaj B = new FMatrixRMaj(2,1);
        CommonOps_FDRM.extract(A,indexes,2,B);

        assertEquals(B.numRows,2);
        assertEquals(B.numCols, 1);

        for( int i = 0; i < 2; i++ ) {
            assertEquals(A.get(indexes[i]),B.get(i),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void insert_array_two() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(2,1, 0, 1, rand);

        int[] rows = new int[6];
        rows[0] = 2;
        rows[1] = 4;

        int[] cols = new int[4];
        cols[0] = 1;
        FMatrixRMaj B = new FMatrixRMaj(5,5);
        CommonOps_FDRM.insert(A, B, rows, 2, cols, 1);


        for( int i = 0; i < 2; i++ ) {
            for( int j = 0; j < 1; j++ ) {
                assertEquals(A.get(i,j),B.get(rows[i],cols[j]),UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void extractDiag() {
        FMatrixRMaj a = RandomMatrices_FDRM.rectangle(3,4, 0, 1, rand);

        for( int i = 0; i < 3; i++ ) {
            a.set(i,i,i+1);
        }

        FMatrixRMaj v = new FMatrixRMaj(3,1);
        CommonOps_FDRM.extractDiag(a, v);

        for( int i = 0; i < 3; i++ ) {
            assertEquals( i+1 , v.get(i) , 1e-8 );
        }
    }

    @Test
    public void extractRow() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,6, 0, 1, rand);

        FMatrixRMaj B = CommonOps_FDRM.extractRow(A, 3, null);

        assertEquals(B.numRows,1);
        assertEquals(B.numCols, 6);

        for( int i = 0; i < 6; i++ ) {
            assertEquals(A.get(3,i),B.get(0,i),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void extractColumn() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,6, 0, 1, rand);

        FMatrixRMaj B = CommonOps_FDRM.extractColumn(A, 3, null);

        assertEquals(B.numRows, 5);
        assertEquals(B.numCols, 1);

        for( int i = 0; i < 5; i++ ) {
            assertEquals(A.get(i,3),B.get(i,0),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void removeColumns() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,6, 0, 1, rand);

        FMatrixRMaj B = A.copy();
        CommonOps_FDRM.removeColumns(B,3,4);

        assertEquals(B.numRows, A.numRows);
        assertEquals(B.numCols, A.numCols-2);

        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                if( col < 3 ){
                    assertEquals(A.get(row,col), B.get(row,col), UtilEjml.TEST_F32);
                } else if( col > 4 ) {
                    assertEquals(A.get(row,col), B.get(row,col-2), UtilEjml.TEST_F32);
                }
            }
        }
    }

    @Test
    public void insert() {
        FMatrixRMaj A = new FMatrixRMaj(5,5);
        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < A.numCols; j++ ) {
                A.set(i,j,i*A.numRows+j);
            }
        }

        FMatrixRMaj B = new FMatrixRMaj(8,8);

        CommonOps_FDRM.insert(A, B, 1, 2);

        for( int i = 1; i < 6; i++ ) {
            for( int j = 2; j < 7; j++ ) {
                assertEquals(A.get(i-1,j-2),B.get(i,j),UtilEjml.TEST_F32);
            }
        }
    }

   @Test
    public void addEquals() {
        FMatrixRMaj a = new FMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        FMatrixRMaj b = new FMatrixRMaj(2,3, true, 5, 4, 3, 2, 1, 0);

        CommonOps_FDRM.addEquals(a, b);

        UtilTestMatrix.checkMat(a,5,5,5,5,5,5);
        UtilTestMatrix.checkMat(b, 5, 4, 3, 2, 1, 0);
    }

    @Test
    public void addEquals_beta() {
        FMatrixRMaj a = new FMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        FMatrixRMaj b = new FMatrixRMaj(2,3, true, 5, 4, 3, 2, 1, 0);

        CommonOps_FDRM.addEquals(a, 2.0f, b);

        UtilTestMatrix.checkMat(a,10,9,8,7,6,5);
        UtilTestMatrix.checkMat(b,5,4,3,2,1,0);
    }

    @Test
    public void add() {
        FMatrixRMaj a = new FMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        FMatrixRMaj b = new FMatrixRMaj(2,3, true, 5, 4, 3, 2, 1, 0);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(2,3,rand);

        CommonOps_FDRM.add(a,b,c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(b,5,4,3,2,1,0);
        UtilTestMatrix.checkMat(c,5,5,5,5,5,5);
    }

    @Test
    public void add_beta() {
        FMatrixRMaj a = new FMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        FMatrixRMaj b = new FMatrixRMaj(2,3, true, 5, 4, 3, 2, 1, 0);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(2,3,rand);

        CommonOps_FDRM.add(a,2.0f,b,c);

        UtilTestMatrix.checkMat(a,0,1,2,3,4,5);
        UtilTestMatrix.checkMat(b,5,4,3,2,1,0);
        UtilTestMatrix.checkMat(c,10,9,8,7,6,5);
    }

    @Test
    public void add_alpha_beta() {
        FMatrixRMaj a = new FMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        FMatrixRMaj b = new FMatrixRMaj(2,3, true, 5, 4, 3, 2, 1, 0);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(2,3,rand);

        CommonOps_FDRM.add(2.0f,a,2.0f,b,c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(b,5,4,3,2,1,0);
        UtilTestMatrix.checkMat(c,10,10,10,10,10,10);
    }

    @Test
    public void add_alpha() {
        FMatrixRMaj a = new FMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        FMatrixRMaj b = new FMatrixRMaj(2,3, true, 5, 4, 3, 2, 1, 0);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(2,3,rand);

        CommonOps_FDRM.add(2.0f,a,b,c);

        UtilTestMatrix.checkMat(a,0,1,2,3,4,5);
        UtilTestMatrix.checkMat(b, 5, 4, 3, 2, 1, 0);
        UtilTestMatrix.checkMat(c,5,6,7,8,9,10);
    }

    @Test
    public void add_scalar_c() {
        FMatrixRMaj a = new FMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(2,3,rand);

        CommonOps_FDRM.add(a, 2.0f, c);

        UtilTestMatrix.checkMat(a,0,1,2,3,4,5);
        UtilTestMatrix.checkMat(c,2,3,4,5,6,7);
    }

    @Test
    public void add_scalar() {
        FMatrixRMaj a = new FMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);

        CommonOps_FDRM.add(a, 2.0f);

        UtilTestMatrix.checkMat(a,2,3,4,5,6,7);
    }

    @Test
    public void subEquals() {
        FMatrixRMaj a = new FMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        FMatrixRMaj b = new FMatrixRMaj(2,3, true, 5, 5, 5, 5, 5, 5);

        CommonOps_FDRM.subtractEquals(a, b);

        UtilTestMatrix.checkMat(a, -5, -4, -3, -2, -1, 0);
        UtilTestMatrix.checkMat(b,5,5,5,5,5,5);
    }

    @Test
    public void subtract_matrix_matrix() {
        FMatrixRMaj a = new FMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        FMatrixRMaj b = new FMatrixRMaj(2,3, true, 5, 5, 5, 5, 5, 5);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(2,3,rand);

        CommonOps_FDRM.subtract(a, b, c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(b,5,5,5,5,5,5);
        UtilTestMatrix.checkMat(c,-5,-4,-3,-2,-1,0);
    }

    @Test
    public void subtract_matrix_float() {
        FMatrixRMaj a = new FMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(2,3,rand);

        CommonOps_FDRM.subtract(a, 2, c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(c, -2, -1, 0, 1, 2, 3);
    }

    @Test
    public void subtract_float_matrix() {
        FMatrixRMaj a = new FMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(2,3,rand);

        CommonOps_FDRM.subtract(2, a, c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(c, 2, 1, 0, -1, -2, -3);
    }

    @Test
    public void scale() {
        float s = 2.5f;
        float[] d = new float[]{10,12.5f,-2,5.5f};
        FMatrixRMaj mat = new FMatrixRMaj(2,2, true, d);

        CommonOps_FDRM.scale(s, mat);

        assertEquals(d[0] * s, mat.get(0, 0), UtilEjml.TEST_F32);
        assertEquals(d[1] * s, mat.get(0, 1), UtilEjml.TEST_F32);
        assertEquals(d[2] * s, mat.get(1, 0), UtilEjml.TEST_F32);
        assertEquals(d[3] * s, mat.get(1, 1), UtilEjml.TEST_F32);
    }

    @Test
    public void scale_two_input() {
        float s = 2.5f;
        float[] d = new float[]{10,12.5f,-2,5.5f};
        FMatrixRMaj mat = new FMatrixRMaj(2,2, true, d);
        FMatrixRMaj r = new FMatrixRMaj(2,2, true, d);

        CommonOps_FDRM.scale(s,mat,r);

        assertEquals(d[0],mat.get(0,0),UtilEjml.TEST_F32);
        assertEquals(d[1],mat.get(0,1),UtilEjml.TEST_F32);
        assertEquals(d[2],mat.get(1,0),UtilEjml.TEST_F32);
        assertEquals(d[3], mat.get(1, 1), UtilEjml.TEST_F32);

        assertEquals(d[0]*s,r.get(0,0),UtilEjml.TEST_F32);
        assertEquals(d[1]*s,r.get(0,1),UtilEjml.TEST_F32);
        assertEquals(d[2]*s,r.get(1,0),UtilEjml.TEST_F32);
        assertEquals(d[3]*s,r.get(1,1),UtilEjml.TEST_F32);
    }

    @Test
    public void scaleRow() {
        float scale = 1.5f;
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(2,3,rand);
        FMatrixRMaj B = A.copy();

        for (int row = 0; row < A.numRows; row++) {
            A = B.copy();
            CommonOps_FDRM.scaleRow(scale,A,row);
            for (int i = 0; i < A.numRows; i++) {
                for (int j = 0; j < A.numCols; j++) {
                    if( i == row ) {
                        assertEquals(B.get(i,j)*scale,A.get(i,j), UtilEjml.TEST_F32);
                    } else {
                        assertEquals(B.get(i,j),A.get(i,j), UtilEjml.TEST_F32);

                    }
                }
            }
        }
    }

    @Test
    public void scaleCol() {
        float scale = 1.5f;
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(2,3,rand);
        FMatrixRMaj B = A.copy();

        for (int col = 0; col < A.numCols; col++) {
            A = B.copy();
            CommonOps_FDRM.scaleCol(scale,A,col);
            for (int i = 0; i < A.numRows; i++) {
                for (int j = 0; j < A.numCols; j++) {
                    if( j == col ) {
                        assertEquals(B.get(i,j)*scale,A.get(i,j), UtilEjml.TEST_F32);
                    } else {
                        assertEquals(B.get(i,j),A.get(i,j), UtilEjml.TEST_F32);

                    }
                }
            }
        }
    }


    @Test
    public void div_scalar_mat() {
        float s = 2.5f;
        float[] d = new float[]{10,12.5f,-2,5.5f};
        FMatrixRMaj mat = new FMatrixRMaj(2,2, true, d);

        CommonOps_FDRM.divide(s, mat);

        assertEquals(s/d[0],mat.get(0,0),UtilEjml.TEST_F32);
        assertEquals(s/d[1],mat.get(0,1),UtilEjml.TEST_F32);
        assertEquals(s/d[2],mat.get(1,0),UtilEjml.TEST_F32);
        assertEquals(s/d[3],mat.get(1,1),UtilEjml.TEST_F32);
    }

    @Test
    public void div_mat_scalar() {
        float s = 2.5f;
        float[] d = new float[]{10,12.5f,-2,5.5f};
        FMatrixRMaj mat = new FMatrixRMaj(2,2, true, d);

        CommonOps_FDRM.divide(mat, s);

        assertEquals(mat.get(0,0),d[0]/s,UtilEjml.TEST_F32);
        assertEquals(mat.get(0,1),d[1]/s,UtilEjml.TEST_F32);
        assertEquals(mat.get(1,0),d[2]/s,UtilEjml.TEST_F32);
        assertEquals(mat.get(1,1),d[3]/s,UtilEjml.TEST_F32);
    }

    @Test
    public void div_mat_scalar_out() {
        float s = 2.5f;
        float[] d = new float[]{10,12.5f,-2,5.5f};
        FMatrixRMaj mat = new FMatrixRMaj(2,2, true, d);
        FMatrixRMaj r = new FMatrixRMaj(2,2, true, d);

        CommonOps_FDRM.divide(mat,s,r);

        assertEquals(d[0],mat.get(0,0),UtilEjml.TEST_F32);
        assertEquals(d[1],mat.get(0,1),UtilEjml.TEST_F32);
        assertEquals(d[2],mat.get(1,0),UtilEjml.TEST_F32);
        assertEquals(d[3], mat.get(1, 1), UtilEjml.TEST_F32);

        assertEquals(d[0]/s,r.get(0,0),UtilEjml.TEST_F32);
        assertEquals(d[1]/s,r.get(0,1),UtilEjml.TEST_F32);
        assertEquals(d[2]/s,r.get(1,0),UtilEjml.TEST_F32);
        assertEquals(d[3] / s, r.get(1, 1), UtilEjml.TEST_F32);
    }

    @Test
    public void div_scalar_mat_out() {
        float s = 2.5f;
        float[] d = new float[]{10,12.5f,-2,5.5f};
        FMatrixRMaj mat = new FMatrixRMaj(2,2, true, d);
        FMatrixRMaj r = new FMatrixRMaj(2,2, true, d);

        CommonOps_FDRM.divide(s,mat,r);

        assertEquals(d[0],mat.get(0,0),UtilEjml.TEST_F32);
        assertEquals(d[1],mat.get(0,1),UtilEjml.TEST_F32);
        assertEquals(d[2],mat.get(1,0),UtilEjml.TEST_F32);
        assertEquals(d[3],mat.get(1,1),UtilEjml.TEST_F32);

        assertEquals(s/d[0],r.get(0,0),UtilEjml.TEST_F32);
        assertEquals(s/d[1],r.get(0,1),UtilEjml.TEST_F32);
        assertEquals(s/d[2],r.get(1,0),UtilEjml.TEST_F32);
        assertEquals(s / d[3], r.get(1, 1), UtilEjml.TEST_F32);
    }

    @Test
    public void changeSign_one() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(2,3,rand);
        FMatrixRMaj A_orig = A.copy();

        CommonOps_FDRM.changeSign(A);

        for (int i = 0; i < A.getNumElements(); i++) {
            assertEquals(-A.get(i),A_orig.get(i),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void changeSign_two() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(2,3,rand);
        FMatrixRMaj B = RandomMatrices_FDRM.rectangle(2, 3, rand);

        CommonOps_FDRM.changeSign(A, B);

        for (int i = 0; i < A.getNumElements(); i++) {
            assertEquals(A.get(i),-B.get(i),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void fill_dense() {
        float[] d = new float[]{10,12.5f,-2,5.5f};
        FMatrixRMaj mat = new FMatrixRMaj(2,2, true, d);

        CommonOps_FDRM.fill(mat, 1);

        for( int i = 0; i < mat.getNumElements(); i++ ) {
            assertEquals(1,mat.get(i),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void fill_block() {
        // pick the size such that it doesn't nicely line up along blocks
        FMatrixRBlock mat = new FMatrixRBlock(10,14,3);

        CommonOps_FDRM.fill(mat, 1.5f);

        for( int i = 0; i < mat.getNumElements(); i++ ) {
            assertEquals(1.5f,mat.get(i),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void zero() {
        float[] d = new float[]{10,12.5f,-2,5.5f};
        FMatrixRMaj mat = new FMatrixRMaj(2,2, true, d);

        mat.zero();

        for( int i = 0; i < mat.getNumElements(); i++ ) {
            assertEquals(0,mat.get(i),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void elementMax() {
        FMatrixRMaj mat = new FMatrixRMaj(3,3, true, 0, 1, -2, 3, 4, 5, 6, 7, -8);

        float m = CommonOps_FDRM.elementMax(mat);
        assertEquals(7, m, UtilEjml.TEST_F32);
    }

    @Test
    public void elementMin() {
        FMatrixRMaj mat = new FMatrixRMaj(3,3, true, 0, 1, 2, -3, 4, 5, 6, 7, 8);

        float m = CommonOps_FDRM.elementMin(mat);
        assertEquals(-3,m,UtilEjml.TEST_F32);
    }

    @Test
    public void elementMinAbs() {
        FMatrixRMaj mat = new FMatrixRMaj(3,3, true, 0, 1, -2, 3, 4, 5, 6, 7, -8);

        float m = CommonOps_FDRM.elementMinAbs(mat);
        assertEquals(0,m,UtilEjml.TEST_F32);
    }

    @Test
    public void elementMaxAbs() {
        FMatrixRMaj mat = new FMatrixRMaj(3,3, true, 0, 1, 2, 3, 4, 5, -6, 7, -8);

        float m = CommonOps_FDRM.elementMaxAbs(mat);
        assertEquals(8,m,UtilEjml.TEST_F32);
    }

    @Test
    public void elementSum() {
        FMatrixRMaj M = RandomMatrices_FDRM.rectangle(5,5,rand);
        // make it smaller than the original size to make sure it is bounding
        // the summation correctly
        M.reshape(4, 3, false);

        float sum = 0;
        for( int i = 0; i < M.numRows; i++ ) {
            for( int j = 0; j < M.numCols; j++ ) {
                sum += M.get(i,j);
            }
        }

        assertEquals(sum, CommonOps_FDRM.elementSum(M),UtilEjml.TEST_F32);
    }

    @Test
    public void elementSumAbs() {
        FMatrixRMaj M = RandomMatrices_FDRM.rectangle(5,5,rand);
        // make it smaller than the original size to make sure it is bounding
        // the summation correctly
        M.reshape(4, 3, false);

        float sum = 0;
        for( int i = 0; i < M.numRows; i++ ) {
            for( int j = 0; j < M.numCols; j++ ) {
                sum += Math.abs(M.get(i, j));
            }
        }

        assertEquals(sum, CommonOps_FDRM.elementSum(M),UtilEjml.TEST_F32);
    }

    @Test
    public void elementPower_mm() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(4, 5, rand);
        FMatrixRMaj B = RandomMatrices_FDRM.rectangle(4, 5, rand);
        FMatrixRMaj C = RandomMatrices_FDRM.rectangle(4, 5, rand);

        CommonOps_FDRM.elementPower(A, B, C);

        for (int i = 0; i < A.getNumElements(); i++) {
            float expected = (float)Math.pow( A.get(i) , B.get(i));
            assertEquals(expected,C.get(i),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void elementPower_ms() {
        float a = 1.3f;
        FMatrixRMaj B = RandomMatrices_FDRM.rectangle(4, 5, rand);
        FMatrixRMaj C = RandomMatrices_FDRM.rectangle(4, 5, rand);

        CommonOps_FDRM.elementPower(a, B, C);

        for (int i = 0; i < C.getNumElements(); i++) {
            float expected = (float)Math.pow(a, B.get(i));
            assertEquals(expected,C.get(i),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void elementPower_sm() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(4, 5, rand);
        float b = 1.1f;
        FMatrixRMaj C = RandomMatrices_FDRM.rectangle(4, 5, rand);

        CommonOps_FDRM.elementPower(A, b, C);

        for (int i = 0; i < A.getNumElements(); i++) {
            float expected = (float)Math.pow(A.get(i), b);
            assertEquals(expected,C.get(i),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void elementLog() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(4, 5, rand);
        FMatrixRMaj C = RandomMatrices_FDRM.rectangle(4, 5, rand);

        CommonOps_FDRM.elementLog(A, C);

        for (int i = 0; i < A.getNumElements(); i++) {
            float expected = (float)Math.log(A.get(i));
            assertEquals(expected,C.get(i),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void elementExp() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(4, 5, rand);
        FMatrixRMaj C = RandomMatrices_FDRM.rectangle(4, 5, rand);

        CommonOps_FDRM.elementExp(A, C);

        for (int i = 0; i < A.getNumElements(); i++) {
            float expected = (float)Math.exp(A.get(i));
            assertEquals(expected,C.get(i),UtilEjml.TEST_F32);
        }
    }

    @Test
    public void sumRows() {
        FMatrixRMaj input = RandomMatrices_FDRM.rectangle(4,5,rand);
        FMatrixRMaj output = new FMatrixRMaj(4,1);

        assertTrue( output == CommonOps_FDRM.sumRows(input,output));

        for( int i = 0; i < input.numRows; i++ ) {
            float total = 0;
            for( int j = 0; j < input.numCols; j++ ) {
                total += input.get(i,j);
            }
            assertEquals( total, output.get(i),UtilEjml.TEST_F32);
        }

        // check with a null output
        FMatrixRMaj output2 = CommonOps_FDRM.sumRows(input, null);

        EjmlUnitTests.assertEquals(output, output2, UtilEjml.TEST_F32);
    }

    @Test
    public void minRows() {
        FMatrixRMaj input = RandomMatrices_FDRM.rectangle(4,5,rand);
        FMatrixRMaj output = new FMatrixRMaj(4,1);

        assertTrue( output == CommonOps_FDRM.minRows(input, output));

        for( int i = 0; i < input.numRows; i++ ) {
            float min = input.get(i,0);
            for( int j = 0; j < input.numCols; j++ ) {
                min = Math.min(min,input.get(i,j));
            }
            assertEquals( min, output.get(i),UtilEjml.TEST_F32);
        }

        // check with a null output
        FMatrixRMaj output2 = CommonOps_FDRM.minRows(input, null);

        EjmlUnitTests.assertEquals(output, output2, UtilEjml.TEST_F32);
    }

    @Test
    public void maxRows() {
        FMatrixRMaj input = RandomMatrices_FDRM.rectangle(4,5,rand);
        FMatrixRMaj output = new FMatrixRMaj(4,1);

        assertTrue( output == CommonOps_FDRM.maxRows(input, output));

        for( int i = 0; i < input.numRows; i++ ) {
            float max = input.get(i,0);
            for( int j = 0; j < input.numCols; j++ ) {
                max = Math.max(max,input.get(i,j));
            }
            assertEquals( max, output.get(i),UtilEjml.TEST_F32);
        }

        // check with a null output
        FMatrixRMaj output2 = CommonOps_FDRM.maxRows(input, null);

        EjmlUnitTests.assertEquals(output, output2, UtilEjml.TEST_F32);
    }

    @Test
    public void sumCols() {
        FMatrixRMaj input = RandomMatrices_FDRM.rectangle(4,5,rand);
        FMatrixRMaj output = new FMatrixRMaj(1,5);

        assertTrue( output == CommonOps_FDRM.sumCols(input, output));

        for( int i = 0; i < input.numCols; i++ ) {
            float total = 0;
            for( int j = 0; j < input.numRows; j++ ) {
                total += input.get(j,i);
            }
            assertEquals( total, output.get(i),UtilEjml.TEST_F32);
        }

        // check with a null output
        FMatrixRMaj output2 = CommonOps_FDRM.sumCols(input, null);

        EjmlUnitTests.assertEquals(output, output2, UtilEjml.TEST_F32);
    }

    @Test
    public void minCols() {
        FMatrixRMaj input = RandomMatrices_FDRM.rectangle(4,5,rand);
        FMatrixRMaj output = new FMatrixRMaj(1,5);

        assertTrue( output == CommonOps_FDRM.minCols(input, output));

        for( int i = 0; i < input.numCols; i++ ) {
            float min = input.get(0,i);
            for( int j = 1; j < input.numRows; j++ ) {
                min = Math.min(min,input.get(j,i));
            }
            assertEquals( min, output.get(i),UtilEjml.TEST_F32);
        }

        // check with a null output
        FMatrixRMaj output2 = CommonOps_FDRM.minCols(input, null);

        EjmlUnitTests.assertEquals(output, output2, UtilEjml.TEST_F32);
    }

    @Test
    public void maxCols() {
        FMatrixRMaj input = RandomMatrices_FDRM.rectangle(4,5,rand);
        FMatrixRMaj output = new FMatrixRMaj(1,5);

        assertTrue( output == CommonOps_FDRM.maxCols(input, output));

        for( int i = 0; i < input.numCols; i++ ) {
            float max = input.get(0,i);
            for( int j = 1; j < input.numRows; j++ ) {
                max = Math.max(max,input.get(j,i));
            }
            assertEquals( max, output.get(i),UtilEjml.TEST_F32);
        }

        // check with a null output
        FMatrixRMaj output2 = CommonOps_FDRM.maxCols(input, null);

        EjmlUnitTests.assertEquals(output, output2, UtilEjml.TEST_F32);
    }

    @Test
    public void rref() {
        FMatrixRMaj A = new FMatrixRMaj(4,6,true,
                0,0,1,-1,-1,4,
                2,4,2,4,2,4,
                2,4,3,3,3,4,
                3,6,6,3,6,6);

        FMatrixRMaj expected = new FMatrixRMaj(4,6,true,
                1,2,0,3,0,2,
                0,0,1,-1,0,2,
                0,0,0,0,1,-2,
                0,0,0,0,0,0);

        FMatrixRMaj found = CommonOps_FDRM.rref(A, 5, null);


        assertTrue(MatrixFeatures_FDRM.isEquals(found, expected));
    }

    @Test
    public void elementLessThan_float() {
        FMatrixRMaj A = new FMatrixRMaj(3,4);
        BMatrixRMaj expected = new BMatrixRMaj(3,4);
        BMatrixRMaj found = new BMatrixRMaj(3,4);

        float value = 5.0f;

        for (int i = 0; i < A.getNumElements() ; i++) {
            A.data[i] = i;
            expected.data[i] = i < value;
        }

        CommonOps_FDRM.elementLessThan(A, value, found);
        assertTrue(MatrixFeatures_FDRM.isEquals(expected,found));
    }

    @Test
    public void elementLessThanOrEqual_float() {
        FMatrixRMaj A = new FMatrixRMaj(3,4);
        BMatrixRMaj expected = new BMatrixRMaj(3,4);
        BMatrixRMaj found = new BMatrixRMaj(3,4);

        float value = 5.0f;

        for (int i = 0; i < A.getNumElements() ; i++) {
            A.data[i] = i;
            expected.data[i] = i <= value;
        }

        CommonOps_FDRM.elementLessThanOrEqual(A, value, found);
        assertTrue(MatrixFeatures_FDRM.isEquals(expected, found));
    }

    @Test
    public void elementMoreThan_float() {
        FMatrixRMaj A = new FMatrixRMaj(3,4);
        BMatrixRMaj expected = new BMatrixRMaj(3,4);
        BMatrixRMaj found = new BMatrixRMaj(3,4);

        float value = 5.0f;

        for (int i = 0; i < A.getNumElements() ; i++) {
            A.data[i] = i;
            expected.data[i] = i > value;
        }

        CommonOps_FDRM.elementMoreThan(A, value, found);
        assertTrue(MatrixFeatures_FDRM.isEquals(expected, found));
    }

    @Test
    public void elementMoreThanOrEqual_float() {
        FMatrixRMaj A = new FMatrixRMaj(3,4);
        BMatrixRMaj expected = new BMatrixRMaj(3,4);
        BMatrixRMaj found = new BMatrixRMaj(3,4);

        float value = 5.0f;

        for (int i = 0; i < A.getNumElements() ; i++) {
            A.data[i] = i;
            expected.data[i] = i >= value;
        }

        CommonOps_FDRM.elementMoreThanOrEqual(A, value, found);
        assertTrue(MatrixFeatures_FDRM.isEquals(expected, found));
    }

    @Test
    public void elementLessThan_matrix() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(3,4,rand);
        FMatrixRMaj B = RandomMatrices_FDRM.rectangle(3,4,rand);
        BMatrixRMaj expected = new BMatrixRMaj(3,4);
        BMatrixRMaj found = new BMatrixRMaj(3,4);

        A.data[6] = B.data[6];

        for (int i = 0; i < A.getNumElements() ; i++) {
            expected.data[i] = A.data[i] < B.data[i];
        }

        CommonOps_FDRM.elementLessThan(A, B, found);
        assertTrue(MatrixFeatures_FDRM.isEquals(expected, found));
    }

    @Test
    public void elementLessThanOrEqual_matrix() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(3,4,rand);
        FMatrixRMaj B = RandomMatrices_FDRM.rectangle(3,4,rand);
        BMatrixRMaj expected = new BMatrixRMaj(3,4);
        BMatrixRMaj found = new BMatrixRMaj(3,4);

        A.data[6] = B.data[6];

        for (int i = 0; i < A.getNumElements() ; i++) {
            expected.data[i] = A.data[i] <= B.data[i];
        }

        CommonOps_FDRM.elementLessThanOrEqual(A, B, found);
        assertTrue(MatrixFeatures_FDRM.isEquals(expected, found));
    }

    @Test
    public void elements() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(3,4,rand);

        BMatrixRMaj B = RandomMatrices_FDRM.randomBinary(3, 4, rand);

        FMatrixRMaj found = CommonOps_FDRM.elements(A, B, null);

        int index = 0;

        for (int i = 0; i < B.getNumElements(); i++) {
            if( B.get(i) ) {
                assertEquals(found.get(index++),A.get(i),UtilEjml.TEST_F32);
            }
        }

        assertEquals(index,found.getNumRows());
        assertEquals(1,found.getNumCols());
    }

    @Test
    public void countTrue() {
        BMatrixRMaj B = RandomMatrices_FDRM.randomBinary(4, 5, rand);

        int index = 0;

        for (int i = 0; i < B.getNumElements(); i++) {
            if( B.get(i) ) {
                index++;
            }
        }

        assertTrue(index>5);
        assertEquals(index, CommonOps_FDRM.countTrue(B));
    }

    @Test
    public void concatColumns() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(3,4,rand);
        FMatrixRMaj B = RandomMatrices_FDRM.rectangle(5,6,rand);

        FMatrixRMaj out = new FMatrixRMaj(1,1);
        CommonOps_FDRM.concatColumns(A,B,out);
        assertEquals(5,out.numRows);
        assertEquals(10,out.numCols);
        checkEquals(out,0,0,A);
        checkEquals(out,0,4,B);
    }

    @Test
    public void concatColumnsMulti() {
        FMatrixRMaj a = CommonOps_FDRM.concatColumnsMulti();
        assertEquals(0,a.numRows);
        assertEquals(0,a.numCols);

        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(3,4,rand);
        FMatrixRMaj B = RandomMatrices_FDRM.rectangle(5,6,rand);

        a = CommonOps_FDRM.concatColumnsMulti(A,B);
        assertEquals(5,a.numRows);
        assertEquals(10,a.numCols);
        checkEquals(a,0,0,A);
        checkEquals(a,0,4,B);
    }
    private static void checkEquals( FMatrixRMaj expected , int row0, int col0 , FMatrixRMaj inside ) {
        for (int i = 0; i < inside.numRows; i++) {
            for (int j = 0; j < inside.numCols; j++) {
                assertEquals(expected.get(i+row0,j+col0),inside.get(i,j), UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void concatRows() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(3,4,rand);
        FMatrixRMaj B = RandomMatrices_FDRM.rectangle(5,6,rand);

        FMatrixRMaj out = new FMatrixRMaj(1,1);

        CommonOps_FDRM.concatRows(A,B,out);
        assertEquals(8,out.numRows);
        assertEquals(6,out.numCols);
        checkEquals(out,0,0,A);
        checkEquals(out,3,0,B);
    }

    @Test
    public void concatRowsMulti() {
        FMatrixRMaj a = CommonOps_FDRM.concatRowsMulti();
        assertEquals(0,a.numRows);
        assertEquals(0,a.numCols);

        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(3,4,rand);
        FMatrixRMaj B = RandomMatrices_FDRM.rectangle(5,6,rand);

        a = CommonOps_FDRM.concatRowsMulti(A,B);
        assertEquals(8,a.numRows);
        assertEquals(6,a.numCols);
        checkEquals(a,0,0,A);
        checkEquals(a,3,0,B);
    }

    @Test
    public void permuteRowInv() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,4,rand);
        FMatrixRMaj B = new FMatrixRMaj(5,4);
        int[] pinv = new int[]{2,1,3,4,0};

        CommonOps_FDRM.permuteRowInv(pinv,A,B);

        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < A.numCols; j++) {
                assertEquals(A.get(i,j),B.get(pinv[i],j),UtilEjml.TEST_F32);
            }
        }
    }


    @Test
    public void abs_one() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,4, -1, 1,rand);
        FMatrixRMaj C = A.copy();

        CommonOps_FDRM.abs(A);

        for (int i = 0; i < C.numRows; i++) {
            for (int j = 0; j < C.numCols; j++) {
                assertEquals(A.get(i,j), Math.abs(C.get(i, j)),0);
            }
        }
    }

    @Test
    public void abs_two() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,4,-1,1,rand);
        FMatrixRMaj C = new FMatrixRMaj(5,4);

        CommonOps_FDRM.abs(A, C);

        for (int i = 0; i < C.numRows; i++) {
            for (int j = 0; j < C.numCols; j++) {
                assertEquals(C.get(i,j), Math.abs(C.get(i, j)),0);
            }
        }
    }


    @Test
    public void symmLowerToFull() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,5,rand);
        FMatrixRMaj O = A.copy();

        CommonOps_FDRM.symmLowerToFull(A);
        for (int i = 0; i < 5; i++) {
            for (int j = i+1; j < 5; j++) {
                assertEquals(O.get(j,i),A.get(i,j), UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void symmUpperToFull() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,5,rand);
        FMatrixRMaj O = A.copy();

        CommonOps_FDRM.symmUpperToFull(A);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < i; j++) {
                assertEquals(O.get(j,i),A.get(i,j), UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void applyFunc() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(10, 10, rand);
        FMatrixRMaj B = A.copy();
        CommonOps_FDRM.apply(A, (float x) -> 2 * x + 1, B);

        float[] expectedResult = new float[A.getNumElements()];
        for (int i = 0; i < A.getNumElements(); i++) {
            expectedResult[i] = A.data[i] * 2 + 1;
        }

        checkSameShape(A, B, false);
        assertTrue(Arrays.equals(expectedResult, B.data));
    }
}
