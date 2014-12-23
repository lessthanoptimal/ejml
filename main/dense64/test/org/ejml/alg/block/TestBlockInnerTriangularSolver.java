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

package org.ejml.alg.block;

import org.ejml.alg.dense.misc.UnrolledInverseFromMinor;
import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestBlockInnerTriangularSolver {

    Random rand = new Random(234534);


    @Test
    public void testInvertLower_two() {
        DenseMatrix64F A = RandomMatrices.createUpperTriangle(5,0,-1,1,rand);
        CommonOps.transpose(A);

        DenseMatrix64F A_inv = A.copy();

        BlockInnerTriangularSolver.invertLower(A.data,A_inv.data,5,0,0);

        DenseMatrix64F S = new DenseMatrix64F(5,5);
        CommonOps.mult(A,A_inv,S);

        assertTrue(GenericMatrixOps.isIdentity(S,1e-8));

        // see if it works with the same input matrix
        BlockInnerTriangularSolver.invertLower(A.data,A.data,5,0,0);

        assertTrue(MatrixFeatures.isIdentical(A,A_inv,1e-8));
    }

    @Test
    public void testInvertLower_one() {
        DenseMatrix64F A = RandomMatrices.createUpperTriangle(5,0,-1,1,rand);
        CommonOps.transpose(A);

        DenseMatrix64F A_inv = A.copy();

        BlockInnerTriangularSolver.invertLower(A_inv.data,5,0);

        DenseMatrix64F S = new DenseMatrix64F(5,5);
        CommonOps.mult(A,A_inv,S);

        assertTrue(GenericMatrixOps.isIdentity(S,1e-8));
    }

    /**
     * Test all inner block solvers using reflections to look up the functions
     */
    @Test
    public void testSolveArray() {
        Method methods[] = BlockInnerTriangularSolver.class.getMethods();

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

        DenseMatrix64F L = createRandomLowerTriangular(3);

        if( !solveL ) {
            CommonOps.transpose(L);
        }

        if( transT ) {
            CommonOps.transpose(L);
        }

        DenseMatrix64F L_inv = L.copy();
        UnrolledInverseFromMinor.inv(L_inv,L_inv);

        DenseMatrix64F B = RandomMatrices.createRandom(3,4,rand);
        DenseMatrix64F expected = RandomMatrices.createRandom(3,4,rand);
        DenseMatrix64F found = B.copy();

        // compute the expected solution
        CommonOps.mult(L_inv,B,expected);

        if( transT ) {
            CommonOps.transpose(L);
        }

        if( transB ) {
            CommonOps.transpose(found);
            CommonOps.transpose(expected);
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

        assertTrue(MatrixFeatures.isIdentical(expected,found,1e-8));
    }

    private DenseMatrix64F createRandomLowerTriangular( int N ) {
        DenseMatrix64F U = RandomMatrices.createUpperTriangle(N,0,-1,1,rand);

        CommonOps.transpose(U);

        return U;
    }

    private double[] offsetArray( double[] orig , int offset )
    {
        double[] ret = new double[ orig.length + offset ];

        System.arraycopy(orig,0,ret,offset,orig.length);

        return ret;
    }
}
