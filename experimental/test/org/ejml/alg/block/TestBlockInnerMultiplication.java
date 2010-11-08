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

import org.ejml.data.BlockMatrix64F;
import org.ejml.data.D1Submatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.SimpleMatrix;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestBlockInnerMultiplication {

    private static Random rand = new Random(234234);

    private static final int BLOCK_LENGTH = 4;
    
    private static final int numRows = 10;
    private static final int numCols = 13;


    /**
     * Checks to see if matrix multiplication variants handles submatrices correctly
     */
    @Test
    public void mult_submatrix() {
        Method methods[] = BlockInnerMultiplication.class.getDeclaredMethods();

        int numFound = 0;
        for( Method m : methods) {
            String name = m.getName();

            if( name.contains("Block") || !name.contains("mult") )
                continue;

//            System.out.println("name = "+name);

            boolean transA = name.contains("TransA");
            boolean transB = name.contains("TransB");

            boolean add = name.contains("Add");

            checkMult_submatrix(m,add,transA,transB);
            numFound++;
        }

        // make sure all the functions were in fact tested
        assertEquals(4,numFound);
    }

    private static void checkMult_submatrix( Method func , boolean add , boolean transA , boolean transB )
    {
        // the submatrix is the same size as the originals
        checkMult_submatrix( func , add , transA , transB , sub(0,0,numRows,numCols),sub(0,0,numCols,numRows));

        // submatrix has a size in multiples of the block
        checkMult_submatrix( func , add , transA , transB , sub(BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH *2, BLOCK_LENGTH *2),
                sub(BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH *2, BLOCK_LENGTH *2));

        // submatrix row and column ends at a fraction of a block
        checkMult_submatrix( func , add , transA , transB , sub(BLOCK_LENGTH, BLOCK_LENGTH,numRows,numCols),
                sub(BLOCK_LENGTH, BLOCK_LENGTH,numCols,numRows));

        // the previous tests have some symmetry in it which can mask errors
        checkMult_submatrix( func , add , transA , transB , sub(0, BLOCK_LENGTH,BLOCK_LENGTH,2*BLOCK_LENGTH),
                sub(0, BLOCK_LENGTH,BLOCK_LENGTH,numRows));
    }

    /**
     * Multiplies the two sub-matrices together.  Checks to see if the same result
     * is found when multiplied using the normal algorithm versus the submatrix one.
     */
    private static void checkMult_submatrix( Method func , boolean add , boolean transA , boolean transB ,
                                             D1Submatrix64F A , D1Submatrix64F B ) {
        if( A.col0 % BLOCK_LENGTH != 0 || A.row0 % BLOCK_LENGTH != 0)
            throw new IllegalArgumentException("Submatrix A is not block aligned");
        if( B.col0 % BLOCK_LENGTH != 0 || B.row0 % BLOCK_LENGTH != 0)
            throw new IllegalArgumentException("Submatrix B is not block aligned");

        BlockMatrix64F origA = BlockMatrixOps.createRandom(numRows,numCols,-1,1, rand, BLOCK_LENGTH);
        BlockMatrix64F origB = BlockMatrixOps.createRandom(numCols,numRows,-1,1, rand, BLOCK_LENGTH);

        A.original = origA;
        B.original = origB;
        int w = B.col1-B.col0;
        int h = A.row1-A.row0;

        // offset it to make the test harder
        // randomize to see if its set or adding
        BlockMatrix64F subC = BlockMatrixOps.createRandom(BLOCK_LENGTH +h, BLOCK_LENGTH +w, -1,1,rand, BLOCK_LENGTH);
        D1Submatrix64F C = new D1Submatrix64F(subC, BLOCK_LENGTH, subC.numRows, BLOCK_LENGTH, subC.numCols);

        DenseMatrix64F rmC = multByExtract(add,A,B,C);

        if( transA ) {
            origA = BlockMatrixOps.transpose(origA,null);
            transposeSub(A);
            A.original = origA;
        }

        if( transB ) {
            origB = BlockMatrixOps.transpose(origB,null);
            transposeSub(B);
            B.original = origB;
        }

        try {
            func.invoke(null,BLOCK_LENGTH,A,B,C);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

//        subC.print();
//        rmC.print();

        for( int i = C.row0; i < C.row1; i++ ) {
            for( int j = C.col0; j < C.col1; j++ ) {
//                System.out.println(i+" "+j);
                double diff = Math.abs(subC.get(i,j) - rmC.get(i-C.row0,j-C.col0));
//                System.out.println(subC.get(i,j)+" "+rmC.get(i-C.row0,j-C.col0));
                assertTrue(diff < 1e-12);
            }
        }
    }

    public static void transposeSub(D1Submatrix64F A) {
        int temp = A.col0;
        A.col0 = A.row0;
        A.row0 = temp;
        temp = A.col1;
        A.col1 = A.row1;
        A.row1 = temp;
    }

    private static D1Submatrix64F sub( int row0 , int col0 , int row1 , int col1 ) {
        return new D1Submatrix64F(null,row0, row1, col0, col1);
    }

    private static DenseMatrix64F multByExtract( boolean add ,
                                                 D1Submatrix64F subA , D1Submatrix64F subB ,
                                                 D1Submatrix64F subC )
    {
        SimpleMatrix A = subA.extract();
        SimpleMatrix B = subB.extract();
        SimpleMatrix C = subC.extract();

        if( add )
            return A.mult(B).plus(C).getMatrix();
        else
            return A.mult(B).getMatrix();
    }

    /**
     * Check the inner block multiplication functions against various shapes of inputs
     */
    @Test
    public void testAllBlockMult()
    {
        checkBlockMultCase(BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH);
        checkBlockMultCase(BLOCK_LENGTH -1, BLOCK_LENGTH, BLOCK_LENGTH);
        checkBlockMultCase(BLOCK_LENGTH -1, BLOCK_LENGTH -1, BLOCK_LENGTH);
        checkBlockMultCase(BLOCK_LENGTH -1, BLOCK_LENGTH -1, BLOCK_LENGTH -1);
        checkBlockMultCase(BLOCK_LENGTH,   BLOCK_LENGTH -1, BLOCK_LENGTH -1);
        checkBlockMultCase(BLOCK_LENGTH, BLOCK_LENGTH,   BLOCK_LENGTH -1);
    }

    /**
     * Searches for all inner block matrix operations and tests their correctness.
     */
    private void checkBlockMultCase(final int heightA, final int widthA, final int widthB) {
        Method methods[] = BlockInnerMultiplication.class.getDeclaredMethods();

        int numFound = 0;
        for( Method m : methods) {
            String name = m.getName();

            if( !name.contains("Block"))
                continue;

//            System.out.println("name = "+name);

            boolean transA = false;
            boolean transB = false;

            if( name.contains("TransA"))
                transA = true;

            if( name.contains("TransB"))
                transB = true;

            boolean isAdd = name.contains("Add");

            checkBlockMult(isAdd,transA,transB,m,heightA,widthA,widthB);
            numFound++;
        }

        // make sure all the functions were in fact tested
        assertEquals(7,numFound);
    }

    /**
     * The inner block multiplication is in a row major format.  Test it against
     * operations for DenseMatrix64F
     */
    private void checkBlockMult( boolean isAdd , boolean transA , boolean transB , Method method,
                                 final int heightA, final int widthA, final int widthB )
    {
        DenseMatrix64F A = RandomMatrices.createRandom(heightA,widthA,rand);
        DenseMatrix64F B = RandomMatrices.createRandom(widthA,widthB,rand);
        DenseMatrix64F C = new DenseMatrix64F(heightA,widthB);

        CommonOps.mult(A,B,C);

        DenseMatrix64F C_found = new DenseMatrix64F(heightA,widthB);
        // if it is set then it should overwrite everything just fine
        if( !isAdd )
            RandomMatrices.setRandom(C_found,rand);

        if( transA )
            CommonOps.transpose(A);
        if( transB )
            CommonOps.transpose(B);

        double alpha = 2.0;

        if( method.getParameterTypes().length == 10 ) {
            CommonOps.scale(alpha,C);
        }

        invoke(method,alpha,A.data,B.data,C_found.data,0,0,0,A.numRows,A.numCols,C_found.numCols);

        assertTrue(MatrixFeatures.isIdentical(C,C_found,1e-10));

    }

    public static void invoke(Method func,
                              double alpha ,
                              double[] dataA, double []dataB, double []dataC,
                              int indexA, int indexB, int indexC,
                              final int heightA, final int widthA, final int widthB )
    {
        try {
            if( func.getParameterTypes().length == 9 ) {
                func.invoke(null, dataA, dataB, dataC,
                        indexA,indexB,indexC,
                        heightA,widthA,widthB);
            } else {
                func.invoke(null, alpha , dataA, dataB, dataC,
                        indexA,indexB,indexC,
                        heightA,widthA,widthB);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

}
