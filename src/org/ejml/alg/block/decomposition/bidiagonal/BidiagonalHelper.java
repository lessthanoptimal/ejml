/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.block.decomposition.bidiagonal;

import org.ejml.data.D1Submatrix64F;

import static org.ejml.alg.block.decomposition.qr.BlockHouseHolder.*;


/**
 * @author Peter Abeles
 */
public class BidiagonalHelper {

    /**
     * Performs a standard bidiagonal decomposition just on the outer blocks of the provided matrix
     *
     * @param blockLength
     * @param A
     * @param gammasU
     */

    public static boolean bidiagOuterBlocks( final int blockLength ,
                                             final D1Submatrix64F A ,
                                             final double gammasU[],
                                             final double gammasV[])
    {
//        System.out.println("---------- Orig");
//        A.original.print();

        int width = Math.min(blockLength,A.col1-A.col0);
        int height = Math.min(blockLength,A.row1-A.row0);

        int min = Math.min(width,height);

        for( int i = 0; i < min; i++ ) {
            //--- Apply reflector to the column

            // compute the householder vector
            if (!computeHouseHolderCol(blockLength, A, gammasU, i))
                return false;

            // apply to rest of the columns in the column block
            rank1UpdateMultR_Col(blockLength,A,i,gammasU[A.col0+i]);

            // apply to the top row block
            rank1UpdateMultR_TopRow(blockLength,A,i,gammasU[A.col0+i]);

            System.out.println("After column stuff");
            A.original.print();

            //-- Apply reflector to the row
            if(!computeHouseHolderRow(blockLength,A,gammasV,i))
                return false;
            
            // apply to rest of the rows in the row block
            rank1UpdateMultL_Row(blockLength,A,i,i+1,gammasV[A.row0+i]);

            System.out.println("After update row");
            A.original.print();

            // apply to the left column block
            // TODO THIS WON'T WORK!!!!!!!!!!!!!
            // Needs the whole matrix to have been updated by the left reflector to compute the correct solution
            rank1UpdateMultL_LeftCol(blockLength,A,i,i+1,gammasV[A.row0+i]);

            System.out.println("After row stuff");
            A.original.print();
        }

        return true;
    }
}
