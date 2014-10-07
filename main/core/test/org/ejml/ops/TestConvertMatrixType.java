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

package org.ejml.ops;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.FixedMatrix64F;
import org.ejml.data.RealMatrix64F;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestConvertMatrixType {

    Random rand = new Random(234);

    @Test
    public void any_to_any() {
        DenseMatrix64F a = new DenseMatrix64F(2,3,true,1,2,3,4,5,6);
        DenseMatrix64F b = new DenseMatrix64F(2,3);

        ConvertMatrixType.convert((RealMatrix64F)a,(RealMatrix64F)b);

        assertTrue(MatrixFeatures.isIdentical(a,b,1e-12));
    }

    @Test
    public void checkAll_Fixed_to_DM() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Method[] methods = ConvertMatrixType.class.getMethods();

        int numFound = 0;

        for( Method m : methods ) {
            if( !m.getName().equals("convert") )
                continue;
            Class[]param = m.getParameterTypes();

            if( !FixedMatrix64F.class.isAssignableFrom(param[0]) ) {
                continue;
            }

            FixedMatrix64F a = (FixedMatrix64F)param[0].newInstance();
            DenseMatrix64F b = new DenseMatrix64F(a.getNumRows(),a.getNumCols());

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
        Method[] methods = ConvertMatrixType.class.getMethods();

        int numFound = 0;

        for( Method m : methods ) {
            if( !m.getName().equals("convert") )
                continue;
            Class[]param = m.getParameterTypes();

            if( !FixedMatrix64F.class.isAssignableFrom(param[1]) ) {
                continue;
            }

            FixedMatrix64F b = (FixedMatrix64F)param[1].newInstance();
            DenseMatrix64F a = new DenseMatrix64F(b.getNumRows(),b.getNumCols());

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
                BlockMatrix64F a = BlockMatrixOps.createRandom(rows,cols,-1,2,rand);
                DenseMatrix64F b = new DenseMatrix64F(rows,cols);

                ConvertMatrixType.convert(a,b);

                checkIdentical(a,b);
            }
        }
    }

    @Test
    public void DM_to_BM() {
        for( int rows = 1; rows <= 8; rows++ ) {
            for( int cols = 1; cols <= 8; cols++ ) {
                DenseMatrix64F a = RandomMatrices.createRandom(rows,cols,rand);
                BlockMatrix64F b = new BlockMatrix64F(rows,cols,3);

                ConvertMatrixType.convert(a,b);

                checkIdentical(a,b);
            }
        }
    }


    private void checkIdentical( RealMatrix64F a , RealMatrix64F b ) {
        for( int i = 0; i < a.getNumRows(); i++  ) {
            for( int j = 0; j < a.getNumCols(); j++ ) {
                assertEquals(a.get(i,j),b.get(i,j),1e-8);
            }
        }
    }

    private void checkIdenticalV( RealMatrix64F a , RealMatrix64F b ) {
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

            assertEquals(valueA,valueB,1e-8);
        }



    }

}
