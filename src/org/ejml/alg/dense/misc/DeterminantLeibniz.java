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

package org.ejml.alg.dense.misc;

import org.ejml.data.DenseMatrix64F;


/**
 * <p>
 * A direct implementation of Leibniz determinant equation.  This is of little pratical use
 * because of its slow runtime of O(n!) where n is the width of the matrix. LU decomposition
 * should be used instead.  One advantage of Leibniz's equation is how simplistic it is.
 * </p>
 * <p>
 * det(A) = Sum( &sigma; in S<sub>n</sub> ; sgn(&sigma;) Prod( i = 1 to n ; a<sub>i,&sigma;(i)</sub>) )<br>
 * <ul>
 * <li>sgn is the sign function of permutations. +1 or -1 for even and odd permutations</li>
 * <li>a set of permutations. if n=3 then the possible permutations are (1,2,3) (1,3,2), (3,2,1), ... etc</li>
 * </ul>
 * </p>
 *
 * @see org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt
 * @see DeterminantFromMinor
 *
 * @author Peter Abeles
 */
public class DeterminantLeibniz {

    /**
     * Computes the determinant of the matrix using Leibniz's formula
     *
     * @param mat The matrix whose determinant is computed.
     * @return The value of the determinant
     */
    public static double det( DenseMatrix64F mat )
    {
        PermuteArray perm = new PermuteArray( mat.numCols );

        double total = 0;

        int p[] = perm.next();

        while( p != null ) {

            double prod = 1;

            for( int i = 0; i < mat.numRows; i++ ) {
                prod *= mat.get(i,p[i]);
            }

            total += perm.sgn()*prod;
            p = perm.next();
        }

        return total;
    }
}
