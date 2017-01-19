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
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixFixed;
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestConvertMatrixStruct_F64 {

    Random rand = new Random(234);

    @Test
    public void any_to_any() {
        DMatrixRMaj a = new DMatrixRMaj(2,3,true,1,2,3,4,5,6);
        DMatrixRMaj b = new DMatrixRMaj(2,3);

        ConvertMatrixStruct_F64.convert((DMatrix)a,(DMatrix)b);

        assertTrue(MatrixFeatures_DDRM.isIdentical(a,b,UtilEjml.TEST_F64));
    }

    @Test
    public void checkAll_Fixed_to_DM() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Method[] methods = ConvertMatrixStruct_F64.class.getMethods();

        int numFound = 0;

        for( Method m : methods ) {
            if( !m.getName().equals("convert") )
                continue;
            Class[]param = m.getParameterTypes();

            if( !DMatrixFixed.class.isAssignableFrom(param[0]) ) {
                continue;
            }

            DMatrixFixed a = (DMatrixFixed)param[0].newInstance();
            DMatrixRMaj b = new DMatrixRMaj(a.getNumRows(),a.getNumCols());

            for( int i = 0; i < b.numRows; i++ ) {
                for( int j = 0; j < b.numCols; j++ ) {
                    a.set(i, j, rand.nextDouble());
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
        Method[] methods = ConvertMatrixStruct_F64.class.getMethods();

        int numFound = 0;

        for( Method m : methods ) {
            if( !m.getName().equals("convert") )
                continue;
            Class[]param = m.getParameterTypes();

            if( !DMatrixFixed.class.isAssignableFrom(param[1]) ) {
                continue;
            }

            DMatrixFixed b = (DMatrixFixed)param[1].newInstance();
            DMatrixRMaj a = new DMatrixRMaj(b.getNumRows(),b.getNumCols());

            for( int i = 0; i < a.numRows; i++ ) {
                for( int j = 0; j < a.numCols; j++ ) {
                    a.set(i, j, rand.nextDouble());
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
                DMatrixRBlock a = MatrixOps_DDRB.createRandom(rows,cols,-1,2,rand);
                DMatrixRMaj b = new DMatrixRMaj(rows,cols);

                ConvertMatrixStruct_F64.convert(a,b);

                checkIdentical(a,b);
            }
        }
    }

    @Test
    public void DM_to_BM() {
        for( int rows = 1; rows <= 8; rows++ ) {
            for( int cols = 1; cols <= 8; cols++ ) {
                DMatrixRMaj a = RandomMatrices_DDRM.createRandom(rows,cols,rand);
                DMatrixRBlock b = new DMatrixRBlock(rows,cols,3);

                ConvertMatrixStruct_F64.convert(a,b);

                checkIdentical(a,b);
            }
        }
    }


    private void checkIdentical(DMatrix a , DMatrix b ) {
        for( int i = 0; i < a.getNumRows(); i++  ) {
            for( int j = 0; j < a.getNumCols(); j++ ) {
                assertEquals(a.get(i,j),b.get(i,j), UtilEjml.TEST_F64);
            }
        }
    }

    private void checkIdenticalV(DMatrix a , DMatrix b ) {
        boolean columnVectorA = a.getNumRows() > a.getNumCols();
        boolean columnVectorB = b.getNumRows() > b.getNumCols();

        int length = Math.max(a.getNumRows(),b.getNumRows());

        for( int i = 0; i < length; i++  ) {

            double valueA,valueB;

            if( columnVectorA )
                valueA = a.get(i,0);
            else
                valueA = a.get(0,i);

            if( columnVectorB )
                valueB = b.get(i,0);
            else
                valueB = b.get(0,i);

            assertEquals(valueA,valueB,UtilEjml.TEST_F64);
        }



    }

}
