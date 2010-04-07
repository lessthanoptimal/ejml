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

package org.ejml.alg.block.ops;

import org.ejml.data.BlockMatrix64F;


/**
 * @author Peter Abeles
 */
public class BlockCommonOps {

    public static void mult( BlockMatrix64F a , BlockMatrix64F b , BlockMatrix64F c ) {
        if( a.numCols != b.numRows || b.numCols != c.numCols || a.numRows != c.numRows ) {
            throw new IllegalArgumentException("Matrix shapes do not agree.");
        }

        if( a.blockWidth != b.blockWidth || b.blockWidth != c.blockWidth) {
            throw new IllegalArgumentException("All block widths must be the same");
        }

        
    }
}
