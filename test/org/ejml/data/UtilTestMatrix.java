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

package org.ejml.data;

import static org.junit.Assert.assertEquals;

/**
 * Contains functions useful for testing the results of matrices
 *
 * @author Peter Abeles
 */
public class UtilTestMatrix {

    public static void checkEquals( DenseMatrix64F matA , DenseMatrix64F matB )
    {
        checkEquals(matA,matB,1e-8);
    }

    public static void checkEquals( DenseMatrix64F matA , DenseMatrix64F matB , double tol )
    {
        assertEquals(matA.numCols,matB.numCols);
        assertEquals(matA.numRows,matB.numRows);

        int size = matA.getNumElements();

        for( int i = 0; i < size; i++ ) {
            if( Math.abs(matA.data[i] - matB.data[i]) > tol ) {
               System.out.println("ADASDASD");
            }

            assertEquals(matA.data[i],matB.data[i],tol);
        }
    }

    public static void checkMat( DenseMatrix64F mat , double ...d )
    {
        double data[] = mat.getData();

        for( int i = 0; i < mat.getNumElements(); i++ ) {
            assertEquals(d[i],data[i],1e-6);
        }
    }
}
