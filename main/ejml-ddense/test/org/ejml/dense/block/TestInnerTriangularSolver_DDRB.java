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

package org.ejml.dense.block;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.misc.UnrolledInverseFromMinor_DDRM;
import org.ejml.generic.GenericMatrixOps_F64;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public class TestInnerTriangularSolver_DDRB {

    Random rand = new Random(234534);


    @Test
    public void testInvertLower_two() {
        DMatrixRMaj A = RandomMatrices_DDRM.triangularUpper(5,0,-1,1,rand);
        CommonOps_DDRM.transpose(A);

        DMatrixRMaj A_inv = A.copy();

        InnerTriangularSolver_DDRB.invertLower(A.data,A_inv.data,5,0,0);

        DMatrixRMaj S = new DMatrixRMaj(5,5);
        CommonOps_DDRM.mult(A,A_inv,S);

        assertTrue(GenericMatrixOps_F64.isIdentity(S,UtilEjml.TEST_F64));

        // see if it works with the same input matrix
        InnerTriangularSolver_DDRB.invertLower(A.data,A.data,5,0,0);

        assertTrue(MatrixFeatures_DDRM.isIdentical(A,A_inv,UtilEjml.TEST_F64));
    }

    @Test
    public void testInvertLower_one() {
        DMatrixRMaj A = RandomMatrices_DDRM.triangularUpper(5,0,-1,1,rand);
        CommonOps_DDRM.transpose(A);

        DMatrixRMaj A_inv = A.copy();

        InnerTriangularSolver_DDRB.invertLower(A_inv.data,5,0);

        DMatrixRMaj S = new DMatrixRMaj(5,5);
        CommonOps_DDRM.mult(A,A_inv,S);

        assertTrue(GenericMatrixOps_F64.isIdentity(S, UtilEjml.TEST_F64));
    }

    /**
     * Test all inner block solvers using reflections to look up the functions
     */
    @Test
    public void testSolveArray() {
        Method methods[] = InnerTriangularSolver_DDRB.class.getMethods();

        int numFound = 0;
        for( Method m : methods) {
            String name = m.getName();

            if( !name.contains("solve") || name.compareTo("solve") == 0 || name.compareTo("solveBlock") == 0 )
                continue;

//            System.out.println("name = "+name);

            boolean solveL = name.contains("L");
            boolean transT;
            boolean transB = name.contains("TransB");

            if( solveL )
                transT = name.contains("TransL");
            else
                transT = name.contains("TransU");

            check_solve_array(m,solveL,transT,transB);

            numFound++;
        }

        // make sure all the functions were in fact tested
        assertEquals(5,numFound);
    }

    /**
     * Checks to see if solve functions that use arrays as input work correctly.
     */
    private void check_solve_array(Method m,
                                   boolean solveL, boolean transT, boolean transB) {
        int offsetL = 2;
        int offsetB = 3;

        DMatrixRMaj L = createRandomLowerTriangular(3);

        if( !solveL ) {
            CommonOps_DDRM.transpose(L);
        }

        if( transT ) {
            CommonOps_DDRM.transpose(L);
        }

        DMatrixRMaj L_inv = L.copy();
        UnrolledInverseFromMinor_DDRM.inv(L_inv,L_inv);

        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(3,4,rand);
        DMatrixRMaj expected = RandomMatrices_DDRM.rectangle(3,4,rand);
        DMatrixRMaj found = B.copy();

        // compute the expected solution
        CommonOps_DDRM.mult(L_inv,B,expected);

        if( transT ) {
            CommonOps_DDRM.transpose(L);
        }

        if( transB ) {
            CommonOps_DDRM.transpose(found);
            CommonOps_DDRM.transpose(expected);
        }

        // create arrays that are offset from the original
        // use two different offsets to make sure it doesn't confuse them internally
        double dataL[] = offsetArray(L.data,offsetL);
        double dataB[] = offsetArray(found.data,offsetB);

        try {
            m.invoke(null,dataL,dataB,3,4,3,offsetL,offsetB);
        } catch (IllegalAccessException e) {
            fail("invoke failed");
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }

        // put the solution into B, minus the offset
        System.arraycopy(dataB,offsetB,found.data,0,found.data.length);

        assertTrue(MatrixFeatures_DDRM.isIdentical(expected,found,UtilEjml.TEST_F64));
    }

    private DMatrixRMaj createRandomLowerTriangular(int N ) {
        DMatrixRMaj U = RandomMatrices_DDRM.triangularUpper(N,0,-1,1,rand);

        CommonOps_DDRM.transpose(U);

        return U;
    }

    private double[] offsetArray( double[] orig , int offset )
    {
        double[] ret = new double[ orig.length + offset ];

        System.arraycopy(orig,0,ret,offset,orig.length);

        return ret;
    }
}
