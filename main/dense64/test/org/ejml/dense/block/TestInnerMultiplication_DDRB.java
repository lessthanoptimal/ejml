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

package org.ejml.dense.block;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * @author Peter Abeles
 */
public class TestInnerMultiplication_DDRB {

    private static Random rand = new Random(234234);

    private static final int BLOCK_LENGTH = 4;

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
        Method methods[] = InnerMultiplication_DDRB.class.getDeclaredMethods();

        int numFound = 0;
        for( Method m : methods) {
            String name = m.getName();

//            System.out.println("name = "+name);

            boolean transA = false;
            boolean transB = false;

            if( name.contains("TransA"))
                transA = true;

            if( name.contains("TransB"))
                transB = true;

            // See if the results are added, subtracted, or set to the output matrix
            int operationType = 0;
            if( name.contains("Plus")) operationType = 1;
            else if ( name.contains("Minus")) operationType = -1;
            else if( name.contains("Set")) operationType = 0;

            checkBlockMult(operationType,transA,transB,m,heightA,widthA,widthB);
            numFound++;
        }

        // make sure all the functions were in fact tested
        assertEquals(15,numFound);
    }

    /**
     * The inner block multiplication is in a row major format.  Test it against
     * operations for DMatrixRMaj
     */
    private void checkBlockMult( int operationType , boolean transA , boolean transB , Method method,
                                 final int heightA, final int widthA, final int widthB )
    {
        boolean hasAlpha = method.getParameterTypes().length == 10;

        if( hasAlpha && operationType == -1 )
            fail("No point to minus and alpha");

        DMatrixRMaj A = RandomMatrices_DDRM.createRandom(heightA,widthA,rand);
        DMatrixRMaj B = RandomMatrices_DDRM.createRandom(widthA,widthB,rand);
        DMatrixRMaj C = new DMatrixRMaj(heightA,widthB);

        if( operationType == -1 )
            CommonOps_DDRM.mult(-1,A,B,C);
        else
            CommonOps_DDRM.mult(A,B,C);

        DMatrixRMaj C_found = new DMatrixRMaj(heightA,widthB);
        // if it is set then it should overwrite everything just fine
        if( operationType == 0)
            RandomMatrices_DDRM.setRandom(C_found,rand);

        if( transA )
            CommonOps_DDRM.transpose(A);
        if( transB )
            CommonOps_DDRM.transpose(B);

        double alpha = 2.0;

        if( hasAlpha ) {
            CommonOps_DDRM.scale(alpha,C);
        }

        invoke(method,alpha,A.data,B.data,C_found.data,0,0,0,A.numRows,A.numCols,C_found.numCols);

        if( !MatrixFeatures_DDRM.isIdentical(C,C_found, UtilEjml.TEST_F64) ) {
            C.print();
            C_found.print();
            System.out.println("Method "+method.getName());
            System.out.println("transA " +transA);
            System.out.println("transB " +transB);
            System.out.println("type   " +operationType);
            System.out.println("alpha  " +hasAlpha);
            fail("Not identical");
        }
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
