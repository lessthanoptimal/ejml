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
import org.ejml.data.FMatrixRBlock;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.FSubmatrixD1;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * @author Peter Abeles
 */
public class TestMatrixMult_FDRB {

    private static Random rand = new Random(234234);

    private static final int BLOCK_LENGTH = 4;
    
    private static final int numRows = 10;
    private static final int numCols = 13;

    /**
     * Checks to see if matrix multiplication variants handles submatrices correctly
     */
    @Test
    public void mult_submatrix() {
        Method methods[] = MatrixMult_FDRB.class.getDeclaredMethods();

        int numFound = 0;
        for( Method m : methods) {
            String name = m.getName();

            if( name.contains("Block") || !name.contains("mult") )
                continue;

//            System.out.println("name = "+name);

            boolean transA = name.contains("TransA");
            boolean transB = name.contains("TransB");

            int operationType = 0;
            if( name.contains("Plus")) operationType = 1;
            else if ( name.contains("Minus")) operationType = -1;
            else if( name.contains("Set")) operationType = 0;

            checkMult_submatrix(m,operationType,transA,transB);
            numFound++;
        }

        // make sure all the functions were in fact tested
        assertEquals(7,numFound);
    }

    private static void checkMult_submatrix( Method func , int operationType , boolean transA , boolean transB )
    {
        // the submatrix is the same size as the originals
        checkMult_submatrix( func , operationType , transA , transB , sub(0,0,numRows,numCols),sub(0,0,numCols,numRows));

        // submatrix has a size in multiples of the block
        checkMult_submatrix( func , operationType , transA , transB , sub(BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH *2, BLOCK_LENGTH *2),
                sub(BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH *2, BLOCK_LENGTH *2));

        // submatrix row and column ends at a fraction of a block
        checkMult_submatrix( func , operationType , transA , transB , sub(BLOCK_LENGTH, BLOCK_LENGTH,numRows,numCols),
                sub(BLOCK_LENGTH, BLOCK_LENGTH,numCols,numRows));

        // the previous tests have some symmetry in it which can mask errors
        checkMult_submatrix( func , operationType , transA , transB , sub(0, BLOCK_LENGTH,BLOCK_LENGTH,2*BLOCK_LENGTH),
                sub(0, BLOCK_LENGTH,BLOCK_LENGTH,numRows));
    }

    /**
     * Multiplies the two sub-matrices together.  Checks to see if the same result
     * is found when multiplied using the normal algorithm versus the submatrix one.
     */
    private static void checkMult_submatrix(Method func , int operationType , boolean transA , boolean transB ,
                                            FSubmatrixD1 A , FSubmatrixD1 B ) {
        if( A.col0 % BLOCK_LENGTH != 0 || A.row0 % BLOCK_LENGTH != 0)
            throw new IllegalArgumentException("Submatrix A is not block aligned");
        if( B.col0 % BLOCK_LENGTH != 0 || B.row0 % BLOCK_LENGTH != 0)
            throw new IllegalArgumentException("Submatrix B is not block aligned");

        FMatrixRBlock origA = MatrixOps_FDRB.createRandom(numRows,numCols,-1,1, rand, BLOCK_LENGTH);
        FMatrixRBlock origB = MatrixOps_FDRB.createRandom(numCols,numRows,-1,1, rand, BLOCK_LENGTH);

        A.original = origA;
        B.original = origB;
        int w = B.col1-B.col0;
        int h = A.row1-A.row0;

        // offset it to make the test harder
        // randomize to see if its set or adding
        FMatrixRBlock subC = MatrixOps_FDRB.createRandom(BLOCK_LENGTH +h, BLOCK_LENGTH +w, -1,1,rand, BLOCK_LENGTH);
        FSubmatrixD1 C = new FSubmatrixD1(subC, BLOCK_LENGTH, subC.numRows, BLOCK_LENGTH, subC.numCols);

        FMatrixRMaj rmC = multByExtract(operationType,A,B,C);

        if( transA ) {
            origA = MatrixOps_FDRB.transpose(origA,null);
            transposeSub(A);
            A.original = origA;
        }

        if( transB ) {
            origB = MatrixOps_FDRB.transpose(origB,null);
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



        for( int i = C.row0; i < C.row1; i++ ) {
            for( int j = C.col0; j < C.col1; j++ ) {
//                System.out.println(i+" "+j);
                float diff = Math.abs(subC.get(i,j) - rmC.get(i-C.row0,j-C.col0));
//                System.out.println(subC.get(i,j)+" "+rmC.get(i-C.row0,j-C.col0));
                if( diff >= UtilEjml.TEST_F32) {
                    subC.print();
                    rmC.print();
                    System.out.println(func.getName());
                    System.out.println("transA    "+transA);
                    System.out.println("transB    "+transB);
                    System.out.println("type      "+operationType);
                    fail("Error too large");
                }
            }
        }
    }

    public static void transposeSub(FSubmatrixD1 A) {
        int temp = A.col0;
        A.col0 = A.row0;
        A.row0 = temp;
        temp = A.col1;
        A.col1 = A.row1;
        A.row1 = temp;
    }

    private static FSubmatrixD1 sub(int row0 , int col0 , int row1 , int col1 ) {
        return new FSubmatrixD1(null,row0, row1, col0, col1);
    }

    private static FMatrixRMaj multByExtract(int operationType ,
                                               FSubmatrixD1 subA , FSubmatrixD1 subB ,
                                               FSubmatrixD1 subC )
    {
        SimpleMatrix A = SimpleMatrix.wrap(subA.extract());
        SimpleMatrix B = SimpleMatrix.wrap(subB.extract());
        SimpleMatrix C = SimpleMatrix.wrap(subC.extract());

        if( operationType > 0 )
            return A.mult(B).plus(C).getMatrix();
        else if( operationType < 0 )
            return C.minus(A.mult(B)).getMatrix();
        else
            return A.mult(B).getMatrix();
    }


}
