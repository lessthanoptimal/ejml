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

package org.ejml.alg.block;

import org.ejml.alg.dense.misc.UnrolledInverseFromMinor;
import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.D1Submatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * @author Peter Abeles
 */
public class TestBlockInnerTriangularSolver {

    Random rand = new Random(234534);


    /**
     * Test all inner block solvers
     */
    @Test
    public void testSolveArray() {
        Method methods[] = BlockInnerTriangularSolver.class.getMethods();

        int numFound = 0;
        for( Method m : methods) {
            String name = m.getName();

            if( !name.contains("solve") || name.compareTo("solve") == 0 )
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
        assertEquals(4,numFound);
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
            m.invoke(null,dataL,dataB,3,4,offsetL,offsetB);
        } catch (IllegalAccessException e) {
            fail("invoke failed");
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }

        // put the solution into B, minus the offset
        System.arraycopy(dataB,offsetB,found.data,0,found.data.length);

        assertTrue(MatrixFeatures.isIdentical(expected,found,1e-8));
    }


    /**
     * Check all permutations of solve for submatrices
     */
    @Test
    public void testSolve() {
        check_solve_submatrix(false,false,false);
        check_solve_submatrix(true,false,false);
        check_solve_submatrix(false,true,false);
//        check_solve_submatrix(false,true,false);
//        check_solve_submatrix(false,false,true);
        check_solve_submatrix(true,false,true);
//        check_solve_submatrix(false,true,true);
//        check_solve_submatrix(false,true,true);
    }

    /**
     * Checks to see if solve functions that use sub matrices as input work correctly
     */
    private void check_solve_submatrix( boolean solveL , boolean transT , boolean transB ) {
        // compute expected solution
        DenseMatrix64F L = createRandomLowerTriangular(3);
        DenseMatrix64F B = RandomMatrices.createRandom(3,5,rand);
        DenseMatrix64F X = new DenseMatrix64F(3,5);

        if( !solveL ) {
            CommonOps.transpose(L);
        }

        if( transT ) {
           CommonOps.transpose(L);
        }

        CommonOps.solve(L,B,X);

        // do it again using block matrices
        BlockMatrix64F b_L = BlockMatrixOps.convert(L,3);
        BlockMatrix64F b_B = BlockMatrixOps.convert(B,3);

        D1Submatrix64F sub_L = new D1Submatrix64F(b_L,0, 3, 0, 3);
        D1Submatrix64F sub_B = new D1Submatrix64F(b_B,0, 3, 0, 5);

        if( transT ) {
            sub_L.original = BlockMatrixOps.transpose((BlockMatrix64F)sub_L.original,null);
            TestBlockInnerMultiplication.transposeSub(sub_L);
        }

        if( transB ) {
            sub_B.original = b_B = BlockMatrixOps.transpose((BlockMatrix64F)sub_B.original,null);
            TestBlockInnerMultiplication.transposeSub(sub_B);
            CommonOps.transpose(X);
        }

//        sub_L.original.print();
//        sub_B.original.print();

        BlockInnerTriangularSolver.solve(3,!solveL,sub_L,sub_B,transT,transB);

        assertTrue(GenericMatrixOps.isEquivalent(X,b_B,1e-10));
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
