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

package org.ejml.alg.densed2.mult;

import org.ejml.data.DenseD2Matrix64F;
import org.ejml.ops.MatrixDimensionException;


/**
 * @author Peter Abeles
 */
public class MatrixMatrixMult_D2 {

    /**
     * @see org.ejml.ops.CommonOps#mult(org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void mult_small( DenseD2Matrix64F a , DenseD2Matrix64F b , DenseD2Matrix64F c )
    {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[][] = a.data;
        double dataB[][] = b.data;
        double dataR[][] = c.data;


        for( int i = 0; i < a.numRows; i++ ) {
            double dataAi[] = dataA[i];
            double dataRi[] = dataR[i];

            for( int j = 0; j < b.numCols; j++ ) {
                double total = 0;

                for( int k = 0; k < a.numCols; k++ ) {
                    total += dataAi[k] * dataB[k][j];
                }

                dataRi[j] = total;
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#mult(org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void mult_aux( DenseD2Matrix64F a , DenseD2Matrix64F b , DenseD2Matrix64F c , double []aux )
    {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        if( aux == null ) aux = new double[ b.numRows ];

        double dataA[][] = a.data;
        double dataB[][] = b.data;
        double dataR[][] = c.data;

        for( int j = 0; j < b.numCols; j++ ) {
            // create a copy of the column in B to avoid cache issues
            for( int k = 0; k < b.numRows; k++ ) {
                aux[k] = dataB[k][j];
            }

            for( int i = 0; i < a.numRows; i++ ) {
                double dataAi[] = dataA[i];

                double total = 0;
                for( int k = 0; k < b.numRows; ) {
                    total += dataAi[k]*aux[k++];
                }
                dataR[i][j] = total;
            }
        }
    }
}
