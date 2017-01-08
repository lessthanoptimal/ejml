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

import org.ejml.UtilEjml;
import org.ejml.alg.block.MatrixOps_B32;
import org.ejml.data.BlockMatrix_F32;
import org.ejml.data.FixedMatrix_F32;
import org.ejml.data.RealMatrix_F32;
import org.ejml.data.RowMatrix_F32;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestConvertMatrixStruct_F32 {

    Random rand = new Random(234);

    @Test
    public void any_to_any() {
        RowMatrix_F32 a = new RowMatrix_F32(2,3,true,1,2,3,4,5,6);
        RowMatrix_F32 b = new RowMatrix_F32(2,3);

        ConvertMatrixStruct_F32.convert((RealMatrix_F32)a,(RealMatrix_F32)b);

        assertTrue(MatrixFeatures_D32.isIdentical(a,b,UtilEjml.TEST_F32));
    }

    @Test
    public void checkAll_Fixed_to_DM() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Method[] methods = ConvertMatrixStruct_F32.class.getMethods();

        int numFound = 0;

        for( Method m : methods ) {
            if( !m.getName().equals("convert") )
                continue;
            Class[]param = m.getParameterTypes();

            if( !FixedMatrix_F32.class.isAssignableFrom(param[0]) ) {
                continue;
            }

            FixedMatrix_F32 a = (FixedMatrix_F32)param[0].newInstance();
            RowMatrix_F32 b = new RowMatrix_F32(a.getNumRows(),a.getNumCols());

            for( int i = 0; i < b.numRows; i++ ) {
                for( int j = 0; j < b.numCols; j++ ) {
                    a.set(i, j, rand.nextFloat());
                }
            }

            Object[] input = new Object[param.length];
            input[0] = a;
            input[1] = b;

            m.invoke(null,input);

            checkIdentical(a,b);

            numFound++;
        }

        assertEquals(5+5,numFound);
    }

    @Test
    public void checkAll_DM_to_Fixed() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Method[] methods = ConvertMatrixStruct_F32.class.getMethods();

        int numFound = 0;

        for( Method m : methods ) {
            if( !m.getName().equals("convert") )
                continue;
            Class[]param = m.getParameterTypes();

            if( !FixedMatrix_F32.class.isAssignableFrom(param[1]) ) {
                continue;
            }

            FixedMatrix_F32 b = (FixedMatrix_F32)param[1].newInstance();
            RowMatrix_F32 a = new RowMatrix_F32(b.getNumRows(),b.getNumCols());

            for( int i = 0; i < a.numRows; i++ ) {
                for( int j = 0; j < a.numCols; j++ ) {
                    a.set(i, j, rand.nextFloat());
                }
            }

            Object[] input = new Object[param.length];
            input[0] = a;
            input[1] = b;

            m.invoke(null,input);

            checkIdentical(a,b);

            numFound++;
        }

        assertEquals(5+5,numFound);
    }

    @Test
    public void BM_to_DM() {
        for( int rows = 1; rows <= 8; rows++ ) {
            for( int cols = 1; cols <= 8; cols++ ) {
                BlockMatrix_F32 a = MatrixOps_B32.createRandom(rows,cols,-1,2,rand);
                RowMatrix_F32 b = new RowMatrix_F32(rows,cols);

                ConvertMatrixStruct_F32.convert(a,b);

                checkIdentical(a,b);
            }
        }
    }

    @Test
    public void DM_to_BM() {
        for( int rows = 1; rows <= 8; rows++ ) {
            for( int cols = 1; cols <= 8; cols++ ) {
                RowMatrix_F32 a = RandomMatrices_D32.createRandom(rows,cols,rand);
                BlockMatrix_F32 b = new BlockMatrix_F32(rows,cols,3);

                ConvertMatrixStruct_F32.convert(a,b);

                checkIdentical(a,b);
            }
        }
    }


    private void checkIdentical(RealMatrix_F32 a , RealMatrix_F32 b ) {
        for( int i = 0; i < a.getNumRows(); i++  ) {
            for( int j = 0; j < a.getNumCols(); j++ ) {
                assertEquals(a.get(i,j),b.get(i,j), UtilEjml.TEST_F32);
            }
        }
    }

    private void checkIdenticalV(RealMatrix_F32 a , RealMatrix_F32 b ) {
        boolean columnVectorA = a.getNumRows() > a.getNumCols();
        boolean columnVectorB = b.getNumRows() > b.getNumCols();

        int length = Math.max(a.getNumRows(),b.getNumRows());

        for( int i = 0; i < length; i++  ) {

            float valueA,valueB;

            if( columnVectorA )
                valueA = a.get(i,0);
            else
                valueA = a.get(0,i);

            if( columnVectorB )
                valueB = b.get(i,0);
            else
                valueB = b.get(0,i);

            assertEquals(valueA,valueB,UtilEjml.TEST_F32);
        }



    }

}
