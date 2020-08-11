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
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.misc.UnrolledInverseFromMinor_FDRM;
import org.ejml.generic.GenericMatrixOps_F32;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public class TestInnerTriangularSolver_FDRB {

    Random rand = new Random(234534);


    @Test
    public void testInvertLower_two() {
        FMatrixRMaj A = RandomMatrices_FDRM.triangularUpper(5,0,-1,1,rand);
        CommonOps_FDRM.transpose(A);

        FMatrixRMaj A_inv = A.copy();

        InnerTriangularSolver_FDRB.invertLower(A.data,A_inv.data,5,0,0);

        FMatrixRMaj S = new FMatrixRMaj(5,5);
        CommonOps_FDRM.mult(A,A_inv,S);

        assertTrue(GenericMatrixOps_F32.isIdentity(S,UtilEjml.TEST_F32));

        // see if it works with the same input matrix
        InnerTriangularSolver_FDRB.invertLower(A.data,A.data,5,0,0);

        assertTrue(MatrixFeatures_FDRM.isIdentical(A,A_inv,UtilEjml.TEST_F32));
    }

    @Test
    public void testInvertLower_one() {
        FMatrixRMaj A = RandomMatrices_FDRM.triangularUpper(5,0,-1,1,rand);
        CommonOps_FDRM.transpose(A);

        FMatrixRMaj A_inv = A.copy();

        InnerTriangularSolver_FDRB.invertLower(A_inv.data,5,0);

        FMatrixRMaj S = new FMatrixRMaj(5,5);
        CommonOps_FDRM.mult(A,A_inv,S);

        assertTrue(GenericMatrixOps_F32.isIdentity(S, UtilEjml.TEST_F32));
    }

    /**
     * Test all inner block solvers using reflections to look up the functions
     */
    @Test
    public void testSolveArray() {
        Method methods[] = InnerTriangularSolver_FDRB.class.getMethods();

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

        FMatrixRMaj L = createRandomLowerTriangular(3);

        if( !solveL ) {
            CommonOps_FDRM.transpose(L);
        }

        if( transT ) {
            CommonOps_FDRM.transpose(L);
        }

        FMatrixRMaj L_inv = L.copy();
        UnrolledInverseFromMinor_FDRM.inv(L_inv,L_inv);

        FMatrixRMaj B = RandomMatrices_FDRM.rectangle(3,4,rand);
        FMatrixRMaj expected = RandomMatrices_FDRM.rectangle(3,4,rand);
        FMatrixRMaj found = B.copy();

        // compute the expected solution
        CommonOps_FDRM.mult(L_inv,B,expected);

        if( transT ) {
            CommonOps_FDRM.transpose(L);
        }

        if( transB ) {
            CommonOps_FDRM.transpose(found);
            CommonOps_FDRM.transpose(expected);
        }

        // create arrays that are offset from the original
        // use two different offsets to make sure it doesn't confuse them internally
        float dataL[] = offsetArray(L.data,offsetL);
        float dataB[] = offsetArray(found.data,offsetB);

        try {
            m.invoke(null,dataL,dataB,3,4,3,offsetL,offsetB);
        } catch (IllegalAccessException e) {
            fail("invoke failed");
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }

        // put the solution into B, minus the offset
        System.arraycopy(dataB,offsetB,found.data,0,found.data.length);

        assertTrue(MatrixFeatures_FDRM.isIdentical(expected,found,UtilEjml.TEST_F32));
    }

    private FMatrixRMaj createRandomLowerTriangular(int N ) {
        FMatrixRMaj U = RandomMatrices_FDRM.triangularUpper(N,0,-1,1,rand);

        CommonOps_FDRM.transpose(U);

        return U;
    }

    private float[] offsetArray( float[] orig , int offset )
    {
        float[] ret = new float[ orig.length + offset ];

        System.arraycopy(orig,0,ret,offset,orig.length);

        return ret;
    }
}
