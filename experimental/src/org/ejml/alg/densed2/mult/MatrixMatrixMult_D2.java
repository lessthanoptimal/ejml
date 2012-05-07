/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.densed2.mult;

import org.ejml.alg.dense.mult.MatrixDimensionException;
import org.ejml.data.DenseD2Matrix64F;


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
