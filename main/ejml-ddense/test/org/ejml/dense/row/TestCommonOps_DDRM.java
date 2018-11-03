/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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
import org.ejml.dense.row.decomposition.lu.LUDecompositionAlt_DDRM;
import org.ejml.dense.row.linsol.lu.LinearSolverLu_DDRM;
import org.ejml.dense.row.mult.CheckMatrixMultShape_DDRM;
import org.ejml.dense.row.mult.MatrixMatrixMult_DDRM;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestCommonOps_DDRM {

    Random rand = new Random(0xFF);
    double tol = UtilEjml.TEST_F64;

    @Test
    public void checkInputShape() {
        CheckMatrixMultShape_DDRM check = new CheckMatrixMultShape_DDRM(CommonOps_DDRM.class);
        check.checkAll();
    }

    /**
     * Make sure the multiplication methods here have the same behavior as the ones in MatrixMatrixMult.
     */
    @Test
    public void checkAllMatrixMults() {
        int numChecked = 0;
        Method methods[] = CommonOps_DDRM.class.getMethods();

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
        Method methods[] = CommonOps_DDRM.class.getMethods();

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
                checkMethod = MatrixMatrixMult_DDRM.class.getMethod(
                        name,double.class,
                        DMatrix1Row.class, DMatrix1Row.class,DMatrix1Row.class);
            else
                checkMethod = MatrixMatrixMult_DDRM.class.getMethod(
                        name, DMatrix1Row.class, DMatrix1Row.class,DMatrix1Row.class);
        } catch (NoSuchMethodException e) {
            checkMethod = null;
        }
        if( checkMethod == null ) {
            try {
            if( hasAlpha )
                checkMethod = MatrixMatrixMult_DDRM.class.getMethod(
                        name+"_reorder",double.class,
                        DMatrix1Row.class, DMatrix1Row.class,DMatrix1Row.class);
            else
                checkMethod = MatrixMatrixMult_DDRM.class.getMethod(
                        name+"_reorder", DMatrix1Row.class, DMatrix1Row.class,DMatrix1Row.class);
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
            DMatrixRMaj a;
            if( tranA ) a = RandomMatrices_DDRM.rectangle(i+1,i,rand);
            else  a = RandomMatrices_DDRM.rectangle(i,i+1,rand);

            DMatrixRMaj b;
            if( tranB ) b = RandomMatrices_DDRM.rectangle(i,i+1,rand);
            else  b = RandomMatrices_DDRM.rectangle(i+1,i,rand);

            DMatrixRMaj c = RandomMatrices_DDRM.rectangle(i,i,rand);
            DMatrixRMaj c_alt = c.copy();

            if( hasAlpha ) {
                method.invoke(null,2.0,a,b,c);
                checkMethod.invoke(null,2.0,a,b,c_alt);
            } else {
                method.invoke(null,a,b,c);
                checkMethod.invoke(null,a,b,c_alt);
            }

            if( !MatrixFeatures_DDRM.isIdentical(c_alt,c,tol))
                return false;
        }

        // check various sizes column vector
        for( int i = 1; i < 4; i++ ) {
            DMatrixRMaj a;
            if( tranA ) a = RandomMatrices_DDRM.rectangle(i,i+1,rand);
            else  a = RandomMatrices_DDRM.rectangle(i+1,i,rand);

            DMatrixRMaj b;
            if( tranB ) b = RandomMatrices_DDRM.rectangle(1,i,rand);
            else  b = RandomMatrices_DDRM.rectangle(i,1,rand);

            DMatrixRMaj c = RandomMatrices_DDRM.rectangle(i+1,1,rand);
            DMatrixRMaj c_alt = c.copy();

            if( hasAlpha ) {
                method.invoke(null,2.0,a,b,c);
                checkMethod.invoke(null,2.0,a,b,c_alt);
            } else {
                method.invoke(null,a,b,c);
                checkMethod.invoke(null,a,b,c_alt);
            }

            if( !MatrixFeatures_DDRM.isIdentical(c_alt,c,tol))
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
        DMatrixRMaj a = tranA ? new DMatrixRMaj(colsA,rowsA) : new DMatrixRMaj(rowsA,colsA);
        DMatrixRMaj b = tranB ? new DMatrixRMaj(colsB,rowsB) : new DMatrixRMaj(rowsB,colsB);

        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(rowsA,colsB,rand);

        if( hasAlpha ) {
            method.invoke(null,2.0,a,b,c);
        } else {
            method.invoke(null,a,b,c);
        }

        if( add ) {
            DMatrixRMaj corig = c.copy();
            assertTrue(MatrixFeatures_DDRM.isIdentical(corig, c, UtilEjml.TEST_F64));
        } else {
            assertTrue(MatrixFeatures_DDRM.isZeros(c, UtilEjml.TEST_F64));
        }

        return true;
    }

    @Test
    public void dot() {
        DMatrixRMaj a = RandomMatrices_DDRM.rectangle(10, 1, rand);
        DMatrixRMaj b = RandomMatrices_DDRM.rectangle(1,10,rand);

        double found = CommonOps_DDRM.dot(a, b);

        double expected = 0;
        for (int i = 0; i < 10; i++) {
            expected += a.data[i]*b.data[i];
        }

        assertEquals(expected, found, UtilEjml.TEST_F64);
    }

    @Test
    public void multInner() {
        DMatrixRMaj a = RandomMatrices_DDRM.rectangle(10,4,rand);
        DMatrixRMaj found = RandomMatrices_DDRM.rectangle(4,4,rand);
        DMatrixRMaj expected = RandomMatrices_DDRM.rectangle(4, 4, rand);

        CommonOps_DDRM.multTransA(a, a, expected);
        CommonOps_DDRM.multInner(a,found);

        assertTrue(MatrixFeatures_DDRM.isIdentical(expected, found, tol));
    }

    @Test
    public void multOuter() {
        DMatrixRMaj a = RandomMatrices_DDRM.rectangle(10,4,rand);
        DMatrixRMaj found = RandomMatrices_DDRM.rectangle(10,10,rand);
        DMatrixRMaj expected = RandomMatrices_DDRM.rectangle(10,10,rand);

        CommonOps_DDRM.multTransB(a, a, expected);
        CommonOps_DDRM.multOuter(a, found);

        assertTrue(MatrixFeatures_DDRM.isIdentical(expected, found, tol));
    }
    
    @Test
    public void elementMult_two() {
        DMatrixRMaj a = RandomMatrices_DDRM.rectangle(5,4,rand);
        DMatrixRMaj b = RandomMatrices_DDRM.rectangle(5,4,rand);
        DMatrixRMaj a_orig = a.copy();

        CommonOps_DDRM.elementMult(a, b);

        for( int i = 0; i < 20; i++ ) {
            assertEquals(a.get(i),b.get(i)*a_orig.get(i),1e-6);
        }
    }

    @Test
    public void elementMult_three() {
        DMatrixRMaj a = RandomMatrices_DDRM.rectangle(5,4,rand);
        DMatrixRMaj b = RandomMatrices_DDRM.rectangle(5,4,rand);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(5, 4, rand);

        CommonOps_DDRM.elementMult(a, b, c);

        for( int i = 0; i < 20; i++ ) {
            assertEquals(c.get(i),b.get(i)*a.get(i),1e-6);
        }
    }

    @Test
    public void elementDiv_two() {
        DMatrixRMaj a = RandomMatrices_DDRM.rectangle(5,4,rand);
        DMatrixRMaj b = RandomMatrices_DDRM.rectangle(5,4,rand);
        DMatrixRMaj a_orig = a.copy();

        CommonOps_DDRM.elementDiv(a, b);

        for( int i = 0; i < 20; i++ ) {
            assertEquals(a.get(i),a_orig.get(i)/b.get(i),1e-6);
        }
    }

    @Test
    public void elementDiv_three() {
        DMatrixRMaj a = RandomMatrices_DDRM.rectangle(5,4,rand);
        DMatrixRMaj b = RandomMatrices_DDRM.rectangle(5,4,rand);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(5, 4, rand);

        CommonOps_DDRM.elementDiv(a, b, c);

        for( int i = 0; i < 20; i++ ) {
            assertEquals(c.get(i),a.get(i)/b.get(i),1e-6);
        }
    }

    @Test
    public void multRows() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,4,rand);
        DMatrixRMaj found = A.copy();
        double values[] = UtilEjml.randomVector_F64(rand, 5);

        CommonOps_DDRM.multRows(values, found);

        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                assertEquals(A.get(row,col)*values[row], found.get(row,col), UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void divideRows() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,4,rand);
        DMatrixRMaj found = A.copy();
        double values[] = UtilEjml.randomVector_F64(rand, 5);

        CommonOps_DDRM.divideRows(values, found);

        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                assertEquals(A.get(row,col)/values[row], found.get(row,col), UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void multCols() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,4,rand);
        DMatrixRMaj found = A.copy();
        double values[] = UtilEjml.randomVector_F64(rand, 5);

        CommonOps_DDRM.multCols(found,values);

        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                assertEquals(A.get(row,col)*values[col], found.get(row,col), UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void divideCols() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,4,rand);
        DMatrixRMaj found = A.copy();
        double values[] = UtilEjml.randomVector_F64(rand, 5);

        CommonOps_DDRM.divideCols(found,values);

        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                assertEquals(A.get(row,col)/values[col], found.get(row,col), UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void solve() {
        DMatrixRMaj a = new DMatrixRMaj(2,2, true, 1, 2, 7, -3);
        DMatrixRMaj b = RandomMatrices_DDRM.rectangle(2,5,rand);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(2,5,rand);
        DMatrixRMaj c_exp = RandomMatrices_DDRM.rectangle(2,5,rand);

        assertTrue(CommonOps_DDRM.solve(a,b,c));
        LUDecompositionAlt_DDRM alg = new LUDecompositionAlt_DDRM();
        LinearSolverLu_DDRM solver = new LinearSolverLu_DDRM(alg);
        assertTrue(solver.setA(a));

        solver.solve(b, c_exp);

        EjmlUnitTests.assertEquals(c_exp, c, UtilEjml.TEST_F64);
    }

    @Test
    public void transpose_inplace() {
        DMatrixRMaj mat = new DMatrixRMaj(3,3, true, 0, 1, 2, 3, 4, 5, 6, 7, 8);
        DMatrixRMaj matTran = new DMatrixRMaj(3,3);

        CommonOps_DDRM.transpose(mat, matTran);
        CommonOps_DDRM.transpose(mat);

        EjmlUnitTests.assertEquals(mat, matTran, UtilEjml.TEST_F64);
    }

    @Test
    public void transpose() {
        DMatrixRMaj mat = new DMatrixRMaj(3,2, true, 0, 1, 2, 3, 4, 5);
        DMatrixRMaj matTran = new DMatrixRMaj(2,3);

        CommonOps_DDRM.transpose(mat,matTran);

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
        DMatrixRMaj mat = new DMatrixRMaj(3,3, true, 0, 1, 2, 3, 4, 5, 6, 7, 8);

        assertEquals(12, CommonOps_DDRM.trace(mat), 1e-6);

        // non square
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(4,3,rand);
        CommonOps_DDRM.insert(mat, B, 0, 0);
        assertEquals(12, CommonOps_DDRM.trace(B), 1e-6);

        B = RandomMatrices_DDRM.rectangle(3,4,rand);
        CommonOps_DDRM.insert(mat, B, 0, 0);
        assertEquals(12, CommonOps_DDRM.trace(B), 1e-6);

    }

    @Test
    public void invert() {
        for( int i = 1; i <= 10; i++ ) {
            DMatrixRMaj a = RandomMatrices_DDRM.rectangle(i,i,rand);

            LUDecompositionAlt_DDRM lu = new LUDecompositionAlt_DDRM();
            LinearSolverLu_DDRM solver = new LinearSolverLu_DDRM(lu);
            assertTrue(solver.setA(a));

            DMatrixRMaj a_inv = new DMatrixRMaj(i,i);
            DMatrixRMaj a_lu = new DMatrixRMaj(i,i);
            solver.invert(a_lu);

            CommonOps_DDRM.invert(a,a_inv);
            CommonOps_DDRM.invert(a);

            EjmlUnitTests.assertEquals(a, a_inv, UtilEjml.TEST_F64);
            EjmlUnitTests.assertEquals(a_lu, a, UtilEjml.TEST_F64);
        }
    }

    @Test
    public void invertSPD() {
        for( int i = 1; i <= 20; i++ ) {
            DMatrixRMaj A = RandomMatrices_DDRM.symmetricPosDef(i,rand);

            DMatrixRMaj invA = A.createLike();

            CommonOps_DDRM.invertSPD(A,invA);

            DMatrixRMaj I = A.createLike();
            CommonOps_DDRM.multTransB(A,invA,I);
            assertTrue(MatrixFeatures_DDRM.isIdentity(I, UtilEjml.TEST_F64));
        }
    }

    /**
     * Checked against by computing a solution to the linear system then
     * seeing if the solution produces the expected output
     */
    @Test
    public void pinv() {
        // check wide matrix
        DMatrixRMaj A = new DMatrixRMaj(2,4,true,1,2,3,4,5,6,7,8);
        DMatrixRMaj A_inv = new DMatrixRMaj(4,2);
        DMatrixRMaj b = new DMatrixRMaj(2,1,true,3,4);
        DMatrixRMaj x = new DMatrixRMaj(4,1);
        DMatrixRMaj found = new DMatrixRMaj(2,1);
        
        CommonOps_DDRM.pinv(A,A_inv);

        CommonOps_DDRM.mult(A_inv,b,x);
        CommonOps_DDRM.mult(A,x,found);

        assertTrue(MatrixFeatures_DDRM.isIdentical(b,found,UtilEjml.TEST_F64_SQ));

        // check tall matrix
        CommonOps_DDRM.transpose(A);
        CommonOps_DDRM.transpose(A_inv);
        b = new DMatrixRMaj(4,1,true,3,4,5,6);
        x.reshape(2,1);
        found.reshape(4,1);

        CommonOps_DDRM.mult(A_inv,b,x);
        CommonOps_DDRM.mult(A, x, found);

        assertTrue(MatrixFeatures_DDRM.isIdentical(b,found,UtilEjml.TEST_F64_SQ));
    }

    @Test
    public void columnsToVectors() {
        DMatrixRMaj M = RandomMatrices_DDRM.rectangle(4, 5, rand);

        DMatrixRMaj v[] = CommonOps_DDRM.columnsToVector(M, null);

        assertEquals(M.numCols,v.length);

        for( int i = 0; i < v.length; i++ ) {
            DMatrixRMaj a = v[i];

            assertEquals(M.numRows,a.numRows);
            assertEquals(1,a.numCols);

            for( int j = 0; j < M.numRows; j++ ) {
                assertEquals(a.get(j),M.get(j,i),UtilEjml.TEST_F64);
            }
        }
    }
    
    @Test
    public void identity() {
        DMatrixRMaj A = CommonOps_DDRM.identity(4);

        assertEquals(4,A.numRows);
        assertEquals(4,A.numCols);

        assertEquals(4, CommonOps_DDRM.elementSum(A),UtilEjml.TEST_F64);
    }

    @Test
    public void identity_rect() {
        DMatrixRMaj A = CommonOps_DDRM.identity(4, 6);

        assertEquals(4,A.numRows);
        assertEquals(6,A.numCols);

        assertEquals(4, CommonOps_DDRM.elementSum(A),UtilEjml.TEST_F64);
    }

    @Test
    public void setIdentity() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(4, 4, rand);

        CommonOps_DDRM.setIdentity(A);

        assertEquals(4,A.numRows);
        assertEquals(4,A.numCols);

        assertEquals(4, CommonOps_DDRM.elementSum(A),UtilEjml.TEST_F64);
    }

    @Test
    public void diag() {
        DMatrixRMaj A = CommonOps_DDRM.diag(2.0, 3.0, 6.0, 7.0);

        assertEquals(4,A.numRows);
        assertEquals(4,A.numCols);

        assertEquals(2,A.get(0,0),UtilEjml.TEST_F64);
        assertEquals(3, A.get(1, 1), UtilEjml.TEST_F64);
        assertEquals(6, A.get(2, 2), UtilEjml.TEST_F64);
        assertEquals(7, A.get(3, 3), UtilEjml.TEST_F64);

        assertEquals(18, CommonOps_DDRM.elementSum(A),UtilEjml.TEST_F64);
    }

    @Test
    public void diag_rect() {
        DMatrixRMaj A = CommonOps_DDRM.diagR(4, 6, 2.0, 3.0, 6.0, 7.0);

        assertEquals(4,A.numRows);
        assertEquals(6,A.numCols);

        assertEquals(2,A.get(0,0),UtilEjml.TEST_F64);
        assertEquals(3,A.get(1,1),UtilEjml.TEST_F64);
        assertEquals(6,A.get(2,2),UtilEjml.TEST_F64);
        assertEquals(7,A.get(3,3),UtilEjml.TEST_F64);

        assertEquals(18, CommonOps_DDRM.elementSum(A), UtilEjml.TEST_F64);
    }

    @Test
    public void kron() {
        DMatrixRMaj A = new DMatrixRMaj(2,2, true, 1, 2, 3, 4);
        DMatrixRMaj B = new DMatrixRMaj(1,2, true, 4, 5);

        DMatrixRMaj C = new DMatrixRMaj(2,4);
        DMatrixRMaj C_expected = new DMatrixRMaj(2,4, true, 4, 5, 8, 10, 12, 15, 16, 20);

        CommonOps_DDRM.kron(A, B, C);

        assertTrue(MatrixFeatures_DDRM.isIdentical(C, C_expected, UtilEjml.TEST_F64));

        // test various shapes for problems
        for( int i = 1; i <= 3; i++ ) {
            for( int j = 1; j <= 3; j++ ) {
                for( int k = 1; k <= 3; k++ ) {
                    for( int l = 1; l <= 3; l++ ) {
                        A = RandomMatrices_DDRM.rectangle(i,j,rand);
                        B = RandomMatrices_DDRM.rectangle(k,l,rand);
                        C = new DMatrixRMaj(A.numRows*B.numRows,A.numCols*B.numCols);

                        CommonOps_DDRM.kron(A,B,C);

                        assertEquals(i*k,C.numRows);
                        assertEquals(j*l,C.numCols);
                    }
                }
            }
        }
    }

    @Test
    public void extract() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,5, 0, 1, rand);

        DMatrixRMaj B = new DMatrixRMaj(2,3);

        CommonOps_DDRM.extract(A, 1, 3, 2, 5, B, 0, 0);

        for( int i = 1; i < 3; i++ ) {
            for( int j = 2; j < 5; j++ ) {
                assertEquals(A.get(i,j),B.get(i-1,j-2),UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void extract_ret() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,5, 0, 1, rand);

        DMatrixRMaj B = CommonOps_DDRM.extract(A,1,3,2,5);

        assertEquals(B.numRows,2);
        assertEquals(B.numCols, 3);

        for( int i = 1; i < 3; i++ ) {
            for( int j = 2; j < 5; j++ ) {
                assertEquals(A.get(i,j),B.get(i-1,j-2),UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void extract_no_limits() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,5, 0, 1, rand);
        DMatrixRMaj B = new DMatrixRMaj(3,4);

        CommonOps_DDRM.extract(A,1,1,B);

        assertEquals(B.numRows,3);
        assertEquals(B.numCols, 4);

        for( int i = 1; i < 4; i++ ) {
            for( int j = 1; j < 5; j++ ) {
                assertEquals(A.get(i,j),B.get(i-1,j-1),UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void extract_array_two() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,5, 0, 1, rand);

        int rows[] = new int[6];
        rows[0] = 2;
        rows[1] = 4;

        int cols[] = new int[4];
        cols[0] = 1;
        DMatrixRMaj B = new DMatrixRMaj(2,1);
        CommonOps_DDRM.extract(A,rows,2,cols,1,B);

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
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,5, 0, 1, rand);

        int indexes[] = new int[6];
        indexes[0] = 2;
        indexes[1] = 4;

        DMatrixRMaj B = new DMatrixRMaj(2,1);
        CommonOps_DDRM.extract(A,indexes,2,B);

        assertEquals(B.numRows,2);
        assertEquals(B.numCols, 1);

        for( int i = 0; i < 2; i++ ) {
            assertEquals(A.get(indexes[i]),B.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void insert_array_two() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(2,1, 0, 1, rand);

        int rows[] = new int[6];
        rows[0] = 2;
        rows[1] = 4;

        int cols[] = new int[4];
        cols[0] = 1;
        DMatrixRMaj B = new DMatrixRMaj(5,5);
        CommonOps_DDRM.insert(A, B, rows, 2, cols, 1);


        for( int i = 0; i < 2; i++ ) {
            for( int j = 0; j < 1; j++ ) {
                assertEquals(A.get(i,j),B.get(rows[i],cols[j]),UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void extractDiag() {
        DMatrixRMaj a = RandomMatrices_DDRM.rectangle(3,4, 0, 1, rand);

        for( int i = 0; i < 3; i++ ) {
            a.set(i,i,i+1);
        }

        DMatrixRMaj v = new DMatrixRMaj(3,1);
        CommonOps_DDRM.extractDiag(a, v);

        for( int i = 0; i < 3; i++ ) {
            assertEquals( i+1 , v.get(i) , 1e-8 );
        }
    }

    @Test
    public void extractRow() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,6, 0, 1, rand);

        DMatrixRMaj B = CommonOps_DDRM.extractRow(A, 3, null);

        assertEquals(B.numRows,1);
        assertEquals(B.numCols, 6);

        for( int i = 0; i < 6; i++ ) {
            assertEquals(A.get(3,i),B.get(0,i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void extractColumn() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,6, 0, 1, rand);

        DMatrixRMaj B = CommonOps_DDRM.extractColumn(A, 3, null);

        assertEquals(B.numRows, 5);
        assertEquals(B.numCols, 1);

        for( int i = 0; i < 5; i++ ) {
            assertEquals(A.get(i,3),B.get(i,0),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void insert() {
        DMatrixRMaj A = new DMatrixRMaj(5,5);
        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < A.numCols; j++ ) {
                A.set(i,j,i*A.numRows+j);
            }
        }

        DMatrixRMaj B = new DMatrixRMaj(8,8);

        CommonOps_DDRM.insert(A, B, 1, 2);

        for( int i = 1; i < 6; i++ ) {
            for( int j = 2; j < 7; j++ ) {
                assertEquals(A.get(i-1,j-2),B.get(i,j),UtilEjml.TEST_F64);
            }
        }
    }

   @Test
    public void addEquals() {
        DMatrixRMaj a = new DMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        DMatrixRMaj b = new DMatrixRMaj(2,3, true, 5, 4, 3, 2, 1, 0);

        CommonOps_DDRM.addEquals(a, b);

        UtilTestMatrix.checkMat(a,5,5,5,5,5,5);
        UtilTestMatrix.checkMat(b, 5, 4, 3, 2, 1, 0);
    }

    @Test
    public void addEquals_beta() {
        DMatrixRMaj a = new DMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        DMatrixRMaj b = new DMatrixRMaj(2,3, true, 5, 4, 3, 2, 1, 0);

        CommonOps_DDRM.addEquals(a, 2.0, b);

        UtilTestMatrix.checkMat(a,10,9,8,7,6,5);
        UtilTestMatrix.checkMat(b,5,4,3,2,1,0);
    }

    @Test
    public void add() {
        DMatrixRMaj a = new DMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        DMatrixRMaj b = new DMatrixRMaj(2,3, true, 5, 4, 3, 2, 1, 0);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(2,3,rand);

        CommonOps_DDRM.add(a,b,c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(b,5,4,3,2,1,0);
        UtilTestMatrix.checkMat(c,5,5,5,5,5,5);
    }

    @Test
    public void add_beta() {
        DMatrixRMaj a = new DMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        DMatrixRMaj b = new DMatrixRMaj(2,3, true, 5, 4, 3, 2, 1, 0);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(2,3,rand);

        CommonOps_DDRM.add(a,2.0,b,c);

        UtilTestMatrix.checkMat(a,0,1,2,3,4,5);
        UtilTestMatrix.checkMat(b,5,4,3,2,1,0);
        UtilTestMatrix.checkMat(c,10,9,8,7,6,5);
    }

    @Test
    public void add_alpha_beta() {
        DMatrixRMaj a = new DMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        DMatrixRMaj b = new DMatrixRMaj(2,3, true, 5, 4, 3, 2, 1, 0);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(2,3,rand);

        CommonOps_DDRM.add(2.0,a,2.0,b,c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(b,5,4,3,2,1,0);
        UtilTestMatrix.checkMat(c,10,10,10,10,10,10);
    }

    @Test
    public void add_alpha() {
        DMatrixRMaj a = new DMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        DMatrixRMaj b = new DMatrixRMaj(2,3, true, 5, 4, 3, 2, 1, 0);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(2,3,rand);

        CommonOps_DDRM.add(2.0,a,b,c);

        UtilTestMatrix.checkMat(a,0,1,2,3,4,5);
        UtilTestMatrix.checkMat(b, 5, 4, 3, 2, 1, 0);
        UtilTestMatrix.checkMat(c,5,6,7,8,9,10);
    }

    @Test
    public void add_scalar_c() {
        DMatrixRMaj a = new DMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(2,3,rand);

        CommonOps_DDRM.add(a, 2.0, c);

        UtilTestMatrix.checkMat(a,0,1,2,3,4,5);
        UtilTestMatrix.checkMat(c,2,3,4,5,6,7);
    }

    @Test
    public void add_scalar() {
        DMatrixRMaj a = new DMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);

        CommonOps_DDRM.add(a, 2.0);

        UtilTestMatrix.checkMat(a,2,3,4,5,6,7);
    }

    @Test
    public void subEquals() {
        DMatrixRMaj a = new DMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        DMatrixRMaj b = new DMatrixRMaj(2,3, true, 5, 5, 5, 5, 5, 5);

        CommonOps_DDRM.subtractEquals(a, b);

        UtilTestMatrix.checkMat(a, -5, -4, -3, -2, -1, 0);
        UtilTestMatrix.checkMat(b,5,5,5,5,5,5);
    }

    @Test
    public void subtract_matrix_matrix() {
        DMatrixRMaj a = new DMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        DMatrixRMaj b = new DMatrixRMaj(2,3, true, 5, 5, 5, 5, 5, 5);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(2,3,rand);

        CommonOps_DDRM.subtract(a, b, c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(b,5,5,5,5,5,5);
        UtilTestMatrix.checkMat(c,-5,-4,-3,-2,-1,0);
    }

    @Test
    public void subtract_matrix_double() {
        DMatrixRMaj a = new DMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(2,3,rand);

        CommonOps_DDRM.subtract(a, 2, c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(c, -2, -1, 0, 1, 2, 3);
    }

    @Test
    public void subtract_double_matrix() {
        DMatrixRMaj a = new DMatrixRMaj(2,3, true, 0, 1, 2, 3, 4, 5);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(2,3,rand);

        CommonOps_DDRM.subtract(2, a, c);

        UtilTestMatrix.checkMat(a, 0, 1, 2, 3, 4, 5);
        UtilTestMatrix.checkMat(c, 2, 1, 0, -1, -2, -3);
    }

    @Test
    public void scale() {
        double s = 2.5;
        double d[] = new double[]{10,12.5,-2,5.5};
        DMatrixRMaj mat = new DMatrixRMaj(2,2, true, d);

        CommonOps_DDRM.scale(s, mat);

        assertEquals(d[0]*s,mat.get(0,0),UtilEjml.TEST_F64);
        assertEquals(d[1] * s, mat.get(0, 1), UtilEjml.TEST_F64);
        assertEquals(d[2] * s, mat.get(1, 0), UtilEjml.TEST_F64);
        assertEquals(d[3] * s, mat.get(1, 1), UtilEjml.TEST_F64);
    }

    @Test
    public void scale_two_input() {
        double s = 2.5;
        double d[] = new double[]{10,12.5,-2,5.5};
        DMatrixRMaj mat = new DMatrixRMaj(2,2, true, d);
        DMatrixRMaj r = new DMatrixRMaj(2,2, true, d);

        CommonOps_DDRM.scale(s,mat,r);

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
        DMatrixRMaj mat = new DMatrixRMaj(2,2, true, d);

        CommonOps_DDRM.divide(s, mat);

        assertEquals(s/d[0],mat.get(0,0),UtilEjml.TEST_F64);
        assertEquals(s/d[1],mat.get(0,1),UtilEjml.TEST_F64);
        assertEquals(s/d[2],mat.get(1,0),UtilEjml.TEST_F64);
        assertEquals(s/d[3],mat.get(1,1),UtilEjml.TEST_F64);
    }

    @Test
    public void div_mat_scalar() {
        double s = 2.5;
        double d[] = new double[]{10,12.5,-2,5.5};
        DMatrixRMaj mat = new DMatrixRMaj(2,2, true, d);

        CommonOps_DDRM.divide(mat, s);

        assertEquals(mat.get(0,0),d[0]/s,UtilEjml.TEST_F64);
        assertEquals(mat.get(0,1),d[1]/s,UtilEjml.TEST_F64);
        assertEquals(mat.get(1,0),d[2]/s,UtilEjml.TEST_F64);
        assertEquals(mat.get(1,1),d[3]/s,UtilEjml.TEST_F64);
    }

    @Test
    public void div_mat_scalar_out() {
        double s = 2.5;
        double d[] = new double[]{10,12.5,-2,5.5};
        DMatrixRMaj mat = new DMatrixRMaj(2,2, true, d);
        DMatrixRMaj r = new DMatrixRMaj(2,2, true, d);

        CommonOps_DDRM.divide(mat,s,r);

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
        DMatrixRMaj mat = new DMatrixRMaj(2,2, true, d);
        DMatrixRMaj r = new DMatrixRMaj(2,2, true, d);

        CommonOps_DDRM.divide(s,mat,r);

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
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(2,3,rand);
        DMatrixRMaj A_orig = A.copy();

        CommonOps_DDRM.changeSign(A);

        for (int i = 0; i < A.getNumElements(); i++) {
            assertEquals(-A.get(i),A_orig.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void changeSign_two() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(2,3,rand);
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(2, 3, rand);

        CommonOps_DDRM.changeSign(A, B);

        for (int i = 0; i < A.getNumElements(); i++) {
            assertEquals(A.get(i),-B.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void fill_dense() {
        double d[] = new double[]{10,12.5,-2,5.5};
        DMatrixRMaj mat = new DMatrixRMaj(2,2, true, d);

        CommonOps_DDRM.fill(mat, 1);

        for( int i = 0; i < mat.getNumElements(); i++ ) {
            assertEquals(1,mat.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void fill_block() {
        // pick the size such that it doesn't nicely line up along blocks
        DMatrixRBlock mat = new DMatrixRBlock(10,14,3);

        CommonOps_DDRM.fill(mat, 1.5);

        for( int i = 0; i < mat.getNumElements(); i++ ) {
            assertEquals(1.5,mat.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void zero() {
        double d[] = new double[]{10,12.5,-2,5.5};
        DMatrixRMaj mat = new DMatrixRMaj(2,2, true, d);

        mat.zero();

        for( int i = 0; i < mat.getNumElements(); i++ ) {
            assertEquals(0,mat.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementMax() {
        DMatrixRMaj mat = new DMatrixRMaj(3,3, true, 0, 1, -2, 3, 4, 5, 6, 7, -8);

        double m = CommonOps_DDRM.elementMax(mat);
        assertEquals(7, m, UtilEjml.TEST_F64);
    }

    @Test
    public void elementMin() {
        DMatrixRMaj mat = new DMatrixRMaj(3,3, true, 0, 1, 2, -3, 4, 5, 6, 7, 8);

        double m = CommonOps_DDRM.elementMin(mat);
        assertEquals(-3,m,UtilEjml.TEST_F64);
    }

    @Test
    public void elementMinAbs() {
        DMatrixRMaj mat = new DMatrixRMaj(3,3, true, 0, 1, -2, 3, 4, 5, 6, 7, -8);

        double m = CommonOps_DDRM.elementMinAbs(mat);
        assertEquals(0,m,UtilEjml.TEST_F64);
    }

    @Test
    public void elementMaxAbs() {
        DMatrixRMaj mat = new DMatrixRMaj(3,3, true, 0, 1, 2, 3, 4, 5, -6, 7, -8);

        double m = CommonOps_DDRM.elementMaxAbs(mat);
        assertEquals(8,m,UtilEjml.TEST_F64);
    }

    @Test
    public void elementSum() {
        DMatrixRMaj M = RandomMatrices_DDRM.rectangle(5,5,rand);
        // make it smaller than the original size to make sure it is bounding
        // the summation correctly
        M.reshape(4, 3, false);

        double sum = 0;
        for( int i = 0; i < M.numRows; i++ ) {
            for( int j = 0; j < M.numCols; j++ ) {
                sum += M.get(i,j);
            }
        }

        assertEquals(sum, CommonOps_DDRM.elementSum(M),UtilEjml.TEST_F64);
    }

    @Test
    public void elementSumAbs() {
        DMatrixRMaj M = RandomMatrices_DDRM.rectangle(5,5,rand);
        // make it smaller than the original size to make sure it is bounding
        // the summation correctly
        M.reshape(4, 3, false);

        double sum = 0;
        for( int i = 0; i < M.numRows; i++ ) {
            for( int j = 0; j < M.numCols; j++ ) {
                sum += Math.abs(M.get(i, j));
            }
        }

        assertEquals(sum, CommonOps_DDRM.elementSum(M),UtilEjml.TEST_F64);
    }

    @Test
    public void elementPower_mm() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(4, 5, rand);
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(4, 5, rand);
        DMatrixRMaj C = RandomMatrices_DDRM.rectangle(4, 5, rand);

        CommonOps_DDRM.elementPower(A, B, C);

        for (int i = 0; i < A.getNumElements(); i++) {
            double expected = Math.pow( A.get(i) , B.get(i));
            assertEquals(expected,C.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementPower_ms() {
        double a = 1.3;
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(4, 5, rand);
        DMatrixRMaj C = RandomMatrices_DDRM.rectangle(4, 5, rand);

        CommonOps_DDRM.elementPower(a, B, C);

        for (int i = 0; i < C.getNumElements(); i++) {
            double expected = Math.pow(a, B.get(i));
            assertEquals(expected,C.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementPower_sm() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(4, 5, rand);
        double b = 1.1;
        DMatrixRMaj C = RandomMatrices_DDRM.rectangle(4, 5, rand);

        CommonOps_DDRM.elementPower(A, b, C);

        for (int i = 0; i < A.getNumElements(); i++) {
            double expected = Math.pow(A.get(i), b);
            assertEquals(expected,C.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementLog() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(4, 5, rand);
        DMatrixRMaj C = RandomMatrices_DDRM.rectangle(4, 5, rand);

        CommonOps_DDRM.elementLog(A, C);

        for (int i = 0; i < A.getNumElements(); i++) {
            double expected = Math.log(A.get(i));
            assertEquals(expected,C.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void elementExp() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(4, 5, rand);
        DMatrixRMaj C = RandomMatrices_DDRM.rectangle(4, 5, rand);

        CommonOps_DDRM.elementExp(A, C);

        for (int i = 0; i < A.getNumElements(); i++) {
            double expected = Math.exp(A.get(i));
            assertEquals(expected,C.get(i),UtilEjml.TEST_F64);
        }
    }

    @Test
    public void sumRows() {
        DMatrixRMaj input = RandomMatrices_DDRM.rectangle(4,5,rand);
        DMatrixRMaj output = new DMatrixRMaj(4,1);

        assertTrue( output == CommonOps_DDRM.sumRows(input,output));

        for( int i = 0; i < input.numRows; i++ ) {
            double total = 0;
            for( int j = 0; j < input.numCols; j++ ) {
                total += input.get(i,j);
            }
            assertEquals( total, output.get(i),UtilEjml.TEST_F64);
        }

        // check with a null output
        DMatrixRMaj output2 = CommonOps_DDRM.sumRows(input, null);

        EjmlUnitTests.assertEquals(output, output2, UtilEjml.TEST_F64);
    }

    @Test
    public void minRows() {
        DMatrixRMaj input = RandomMatrices_DDRM.rectangle(4,5,rand);
        DMatrixRMaj output = new DMatrixRMaj(4,1);

        assertTrue( output == CommonOps_DDRM.minRows(input, output));

        for( int i = 0; i < input.numRows; i++ ) {
            double min = input.get(i,0);
            for( int j = 0; j < input.numCols; j++ ) {
                min = Math.min(min,input.get(i,j));
            }
            assertEquals( min, output.get(i),UtilEjml.TEST_F64);
        }

        // check with a null output
        DMatrixRMaj output2 = CommonOps_DDRM.minRows(input, null);

        EjmlUnitTests.assertEquals(output, output2, UtilEjml.TEST_F64);
    }

    @Test
    public void maxRows() {
        DMatrixRMaj input = RandomMatrices_DDRM.rectangle(4,5,rand);
        DMatrixRMaj output = new DMatrixRMaj(4,1);

        assertTrue( output == CommonOps_DDRM.maxRows(input, output));

        for( int i = 0; i < input.numRows; i++ ) {
            double max = input.get(i,0);
            for( int j = 0; j < input.numCols; j++ ) {
                max = Math.max(max,input.get(i,j));
            }
            assertEquals( max, output.get(i),UtilEjml.TEST_F64);
        }

        // check with a null output
        DMatrixRMaj output2 = CommonOps_DDRM.maxRows(input, null);

        EjmlUnitTests.assertEquals(output, output2, UtilEjml.TEST_F64);
    }

    @Test
    public void sumCols() {
        DMatrixRMaj input = RandomMatrices_DDRM.rectangle(4,5,rand);
        DMatrixRMaj output = new DMatrixRMaj(1,5);

        assertTrue( output == CommonOps_DDRM.sumCols(input, output));

        for( int i = 0; i < input.numCols; i++ ) {
            double total = 0;
            for( int j = 0; j < input.numRows; j++ ) {
                total += input.get(j,i);
            }
            assertEquals( total, output.get(i),UtilEjml.TEST_F64);
        }

        // check with a null output
        DMatrixRMaj output2 = CommonOps_DDRM.sumCols(input, null);

        EjmlUnitTests.assertEquals(output, output2, UtilEjml.TEST_F64);
    }

    @Test
    public void minCols() {
        DMatrixRMaj input = RandomMatrices_DDRM.rectangle(4,5,rand);
        DMatrixRMaj output = new DMatrixRMaj(1,5);

        assertTrue( output == CommonOps_DDRM.minCols(input, output));

        for( int i = 0; i < input.numCols; i++ ) {
            double min = input.get(0,i);
            for( int j = 1; j < input.numRows; j++ ) {
                min = Math.min(min,input.get(j,i));
            }
            assertEquals( min, output.get(i),UtilEjml.TEST_F64);
        }

        // check with a null output
        DMatrixRMaj output2 = CommonOps_DDRM.minCols(input, null);

        EjmlUnitTests.assertEquals(output, output2, UtilEjml.TEST_F64);
    }

    @Test
    public void maxCols() {
        DMatrixRMaj input = RandomMatrices_DDRM.rectangle(4,5,rand);
        DMatrixRMaj output = new DMatrixRMaj(1,5);

        assertTrue( output == CommonOps_DDRM.maxCols(input, output));

        for( int i = 0; i < input.numCols; i++ ) {
            double max = input.get(0,i);
            for( int j = 1; j < input.numRows; j++ ) {
                max = Math.max(max,input.get(j,i));
            }
            assertEquals( max, output.get(i),UtilEjml.TEST_F64);
        }

        // check with a null output
        DMatrixRMaj output2 = CommonOps_DDRM.maxCols(input, null);

        EjmlUnitTests.assertEquals(output, output2, UtilEjml.TEST_F64);
    }

    @Test
    public void rref() {
        DMatrixRMaj A = new DMatrixRMaj(4,6,true,
                0,0,1,-1,-1,4,
                2,4,2,4,2,4,
                2,4,3,3,3,4,
                3,6,6,3,6,6);

        DMatrixRMaj expected = new DMatrixRMaj(4,6,true,
                1,2,0,3,0,2,
                0,0,1,-1,0,2,
                0,0,0,0,1,-2,
                0,0,0,0,0,0);

        DMatrixRMaj found = CommonOps_DDRM.rref(A, 5, null);


        assertTrue(MatrixFeatures_DDRM.isEquals(found, expected));
    }

    @Test
    public void elementLessThan_double() {
        DMatrixRMaj A = new DMatrixRMaj(3,4);
        BMatrixRMaj expected = new BMatrixRMaj(3,4);
        BMatrixRMaj found = new BMatrixRMaj(3,4);

        double value = 5.0;

        for (int i = 0; i < A.getNumElements() ; i++) {
            A.data[i] = i;
            expected.data[i] = i < value;
        }

        CommonOps_DDRM.elementLessThan(A, value, found);
        assertTrue(MatrixFeatures_DDRM.isEquals(expected,found));
    }

    @Test
    public void elementLessThanOrEqual_double() {
        DMatrixRMaj A = new DMatrixRMaj(3,4);
        BMatrixRMaj expected = new BMatrixRMaj(3,4);
        BMatrixRMaj found = new BMatrixRMaj(3,4);

        double value = 5.0;

        for (int i = 0; i < A.getNumElements() ; i++) {
            A.data[i] = i;
            expected.data[i] = i <= value;
        }

        CommonOps_DDRM.elementLessThanOrEqual(A, value, found);
        assertTrue(MatrixFeatures_DDRM.isEquals(expected, found));
    }

    @Test
    public void elementMoreThan_double() {
        DMatrixRMaj A = new DMatrixRMaj(3,4);
        BMatrixRMaj expected = new BMatrixRMaj(3,4);
        BMatrixRMaj found = new BMatrixRMaj(3,4);

        double value = 5.0;

        for (int i = 0; i < A.getNumElements() ; i++) {
            A.data[i] = i;
            expected.data[i] = i > value;
        }

        CommonOps_DDRM.elementMoreThan(A, value, found);
        assertTrue(MatrixFeatures_DDRM.isEquals(expected, found));
    }

    @Test
    public void elementMoreThanOrEqual_double() {
        DMatrixRMaj A = new DMatrixRMaj(3,4);
        BMatrixRMaj expected = new BMatrixRMaj(3,4);
        BMatrixRMaj found = new BMatrixRMaj(3,4);

        double value = 5.0;

        for (int i = 0; i < A.getNumElements() ; i++) {
            A.data[i] = i;
            expected.data[i] = i >= value;
        }

        CommonOps_DDRM.elementMoreThanOrEqual(A, value, found);
        assertTrue(MatrixFeatures_DDRM.isEquals(expected, found));
    }

    @Test
    public void elementLessThan_matrix() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(3,4,rand);
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(3,4,rand);
        BMatrixRMaj expected = new BMatrixRMaj(3,4);
        BMatrixRMaj found = new BMatrixRMaj(3,4);

        A.data[6] = B.data[6];

        for (int i = 0; i < A.getNumElements() ; i++) {
            expected.data[i] = A.data[i] < B.data[i];
        }

        CommonOps_DDRM.elementLessThan(A, B, found);
        assertTrue(MatrixFeatures_DDRM.isEquals(expected, found));
    }

    @Test
    public void elementLessThanOrEqual_matrix() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(3,4,rand);
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(3,4,rand);
        BMatrixRMaj expected = new BMatrixRMaj(3,4);
        BMatrixRMaj found = new BMatrixRMaj(3,4);

        A.data[6] = B.data[6];

        for (int i = 0; i < A.getNumElements() ; i++) {
            expected.data[i] = A.data[i] <= B.data[i];
        }

        CommonOps_DDRM.elementLessThanOrEqual(A, B, found);
        assertTrue(MatrixFeatures_DDRM.isEquals(expected, found));
    }

    @Test
    public void elements() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(3,4,rand);

        BMatrixRMaj B = RandomMatrices_DDRM.randomBinary(3, 4, rand);

        DMatrixRMaj found = CommonOps_DDRM.elements(A, B, null);

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
        BMatrixRMaj B = RandomMatrices_DDRM.randomBinary(4, 5, rand);

        int index = 0;

        for (int i = 0; i < B.getNumElements(); i++) {
            if( B.get(i) ) {
                index++;
            }
        }

        assertTrue(index>5);
        assertEquals(index, CommonOps_DDRM.countTrue(B));
    }

    @Test
    public void concatColumns() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(3,4,rand);
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(5,6,rand);

        DMatrixRMaj out = new DMatrixRMaj(1,1);
        CommonOps_DDRM.concatColumns(A,B,out);
        assertEquals(5,out.numRows);
        assertEquals(10,out.numCols);
        checkEquals(out,0,0,A);
        checkEquals(out,0,4,B);
    }

    @Test
    public void concatColumnsMulti() {
        DMatrixRMaj a = CommonOps_DDRM.concatColumnsMulti();
        assertEquals(0,a.numRows);
        assertEquals(0,a.numCols);

        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(3,4,rand);
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(5,6,rand);

        a = CommonOps_DDRM.concatColumnsMulti(A,B);
        assertEquals(5,a.numRows);
        assertEquals(10,a.numCols);
        checkEquals(a,0,0,A);
        checkEquals(a,0,4,B);
    }
    private static void checkEquals( DMatrixRMaj expected , int row0, int col0 , DMatrixRMaj inside ) {
        for (int i = 0; i < inside.numRows; i++) {
            for (int j = 0; j < inside.numCols; j++) {
                assertEquals(expected.get(i+row0,j+col0),inside.get(i,j), UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void concatRows() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(3,4,rand);
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(5,6,rand);

        DMatrixRMaj out = new DMatrixRMaj(1,1);

        CommonOps_DDRM.concatRows(A,B,out);
        assertEquals(8,out.numRows);
        assertEquals(6,out.numCols);
        checkEquals(out,0,0,A);
        checkEquals(out,3,0,B);
    }

    @Test
    public void concatRowsMulti() {
        DMatrixRMaj a = CommonOps_DDRM.concatRowsMulti();
        assertEquals(0,a.numRows);
        assertEquals(0,a.numCols);

        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(3,4,rand);
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(5,6,rand);

        a = CommonOps_DDRM.concatRowsMulti(A,B);
        assertEquals(8,a.numRows);
        assertEquals(6,a.numCols);
        checkEquals(a,0,0,A);
        checkEquals(a,3,0,B);
    }

    @Test
    public void permuteRowInv() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,4,rand);
        DMatrixRMaj B = new DMatrixRMaj(5,4);
        int pinv[] = new int[]{2,1,3,4,0};

        CommonOps_DDRM.permuteRowInv(pinv,A,B);

        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < A.numCols; j++) {
                assertEquals(A.get(i,j),B.get(pinv[i],j),UtilEjml.TEST_F64);
            }
        }
    }
}
