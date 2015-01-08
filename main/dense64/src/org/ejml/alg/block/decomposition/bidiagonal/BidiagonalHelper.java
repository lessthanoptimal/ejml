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
//            rank1UpdateMultL_LeftCol(blockLength,A,i,i+1,gammasV[A.row0+i]);

            System.out.println("After row stuff");
            A.original.print();
        }

        return true;
    }
}
