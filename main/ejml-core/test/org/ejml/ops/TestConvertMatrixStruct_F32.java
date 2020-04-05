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

package org.ejml.ops;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrix;
import org.ejml.data.FMatrixFixed;
import org.ejml.data.FMatrixRBlock;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.block.MatrixOps_FDRB;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestConvertMatrixStruct_F32 {

    Random rand = new Random(234);

    @Test
    public void any_to_any() {
        FMatrixRMaj a = new FMatrixRMaj(2,3,true,1,2,3,4,5,6);
        FMatrixRMaj b = new FMatrixRMaj(2,3);

        ConvertFMatrixStruct.convert((FMatrix)a,(FMatrix)b);

        assertTrue(MatrixFeatures_FDRM.isIdentical(a,b,UtilEjml.TEST_F32));
    }

    @Test
    public void checkAll_Fixed_to_DM() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Method[] methods = ConvertFMatrixStruct.class.getMethods();

        int numFound = 0;

        for( Method m : methods ) {
            if( !m.getName().equals("convert") )
                continue;
            Class[]param = m.getParameterTypes();

            if( !FMatrixFixed.class.isAssignableFrom(param[0]) ) {
                continue;
            }

            FMatrixFixed a = (FMatrixFixed)param[0].newInstance();
            FMatrixRMaj b = new FMatrixRMaj(a.getNumRows(),a.getNumCols());

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
        Method[] methods = ConvertFMatrixStruct.class.getMethods();

        int numFound = 0;

        for( Method m : methods ) {
            if( !m.getName().equals("convert") )
                continue;
            Class[]param = m.getParameterTypes();

            if( !FMatrixFixed.class.isAssignableFrom(param[1]) ) {
                continue;
            }

            FMatrixFixed b = (FMatrixFixed)param[1].newInstance();
            FMatrixRMaj a = new FMatrixRMaj(b.getNumRows(),b.getNumCols());

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
                FMatrixRBlock a = MatrixOps_FDRB.createRandom(rows,cols,-1,2,rand);
                FMatrixRMaj b = new FMatrixRMaj(rows,cols);

                ConvertFMatrixStruct.convert(a,b);

                checkIdentical(a,b);
            }
        }
    }

    @Test
    public void DM_to_BM() {
        for( int rows = 1; rows <= 8; rows++ ) {
            for( int cols = 1; cols <= 8; cols++ ) {
                FMatrixRMaj a = RandomMatrices_FDRM.rectangle(rows,cols,rand);
                FMatrixRBlock b = new FMatrixRBlock(rows,cols,3);

                ConvertFMatrixStruct.convert(a,b);

                checkIdentical(a,b);
            }
        }
    }


    private void checkIdentical(FMatrix a , FMatrix b ) {
        for( int i = 0; i < a.getNumRows(); i++  ) {
            for( int j = 0; j < a.getNumCols(); j++ ) {
                assertEquals(a.get(i,j),b.get(i,j), UtilEjml.TEST_F32);
            }
        }
    }

    private void checkIdenticalV(FMatrix a , FMatrix b ) {
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
