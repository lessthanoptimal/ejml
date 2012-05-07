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

package org.ejml.alg.dense.misc;

import org.ejml.data.DenseMatrix64F;

/**
 * Implementations of common ops routines for {@link org.ejml.data.DenseMatrix64F}.  In general
 * there is no need to directly invoke these functions.
 *
 * @author Peter Abeles
 */
public class ImplCommonOps_DenseMatrix64F {
    public static void extract(DenseMatrix64F src,
                               int srcY0, int srcX0,
                               DenseMatrix64F dst,
                               int dstY0, int dstX0,
                               int numRows, int numCols)
    {
         for( int y = 0; y < numRows; y++ ) {
             int indexSrc = src.getIndex(y+srcY0,srcX0);
             int indexDst = dst.getIndex(y+dstY0,dstX0);
             System.arraycopy(src.data,indexSrc,dst.data,indexDst, numCols);
         }
    }
}
