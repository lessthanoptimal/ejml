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

package org.ejml.alg.block.decomposition.hessenberg;

import org.ejml.alg.block.decomposition.qr.BlockHouseHolder;
import org.ejml.data.D1Submatrix64F;

import static org.ejml.alg.block.decomposition.qr.BlockHouseHolder.computeHouseHolderRow;


/**
 * @author Peter Abeles
 */
public class TridiagonalBlockHelper {

    /**
     * <p>
     * Performs a tridiagonal decomposition on the upper row only.
     * </p>
     *
     * <p>
     * For each row 'a' in 'A':
     * Compute 'u' the householder reflector.
     * y(:) = A*u
     * v(i) = y - (1/2)*(y^T*u)*u
     * a(i+1) = a(i) - u*v^T - v*u^t
     * </p>
     *
     * @param blockLength Size of a block
     * @param A is the row block being decomposed.  Modified.
     * @param gammas Householder gammas.
     * @param V Where computed 'v' are stored in a row block.  Modified.
     */
    public static void tridiagUpperRow( final int blockLength ,
                                        final D1Submatrix64F A ,
                                        final double gammas[] ,
                                        final D1Submatrix64F V )
    {
        int blockHeight = Math.min(blockLength,A.row1-A.row0);
        if( blockHeight <= 1 )
            return;
        int width = A.col1-A.col0;
        int num = Math.min(width-1,blockHeight);
        int applyIndex = Math.min(width,blockHeight);

        // step through rows in the block
        for( int i = 0; i < num; i++ ) {
            // compute the new reflector and save it in a row in 'A'
            computeHouseHolderRow(blockLength,A,gammas,i);
            double gamma = gammas[A.row0+i];

            // compute y
            computeY(blockLength,A,V,i,gamma);

            // compute v from y
            computeRowOfV(blockLength,A,V,i,gamma);

            // Apply the reflectors to the next row in 'A' only
            if( i+1 < applyIndex ) {
                applyReflectorsToRow( blockLength , A , V , i+1 );
            }
        }
    }

    /**
     * <p>
     * Applies the reflectors that have been computed previously to the specified row.
     * <br>
     * A = A + u*v^T + v*u^T only along the specified row in A.
     * </p>
     *
     * @param blockLength
     * @param A Contains the reflectors and the row being updated.
     * @param V Contains previously computed 'v' vectors.
     * @param row The row of 'A' that is to be updated.
     */
    public static void applyReflectorsToRow( final int blockLength ,
                                             final D1Submatrix64F A ,
                                             final D1Submatrix64F V ,
                                             int row )
    {
        int height = Math.min(blockLength, A.row1 - A.row0);

        double dataA[] = A.original.data;
        double dataV[] = V.original.data;

        int indexU,indexV;

        // for each previously computed reflector
        for( int i = 0; i < row; i++ ) {
            int width = Math.min(blockLength,A.col1 - A.col0);

            indexU = A.original.numCols*A.row0 + height*A.col0 + i*width + row;
            indexV = V.original.numCols*V.row0 + height*V.col0 + i*width + row;

            double u_row = (i+1 == row) ? 1.0 : dataA[ indexU ];
            double v_row = dataV[ indexV ];

            // take in account the leading one
            double before = A.get(i,i+1);
            A.set(i,i+1,1);

            // grab only the relevant row from A = A + u*v^T + v*u^T
            BlockHouseHolder.plusScale_row(blockLength,row,u_row,A,row,V,i);
            BlockHouseHolder.plusScale_row(blockLength,row,v_row,A,row,A,i);

            A.set(i,i+1,before);
        }
    }

    /**
     * <p>
     * Computes the 'y' vector and stores the result in 'v'<br>
     * <br>
     * y = -&gamma;(A + U*V^T + V*U^T)u
     * </p>
     *
     * @param blockLength
     * @param A Contains the reflectors and the row being updated.
     * @param V Contains previously computed 'v' vectors.
     * @param row The row of 'A' that is to be updated.
     */
    public static void computeY( final int blockLength ,
                                 final D1Submatrix64F A ,
                                 final D1Submatrix64F V ,
                                 int row ,
                                 double gamma )
    {
        // Elements in 'y' before 'row' are known to be zero and the element at 'row'
        // is not used. Thus only elements after row and after are computed.
        // y = A*u
        multA_u(blockLength,A,V,row);

        for( int i = 0; i < row; i++ ) {
            // height of the top block of A and V should be the same
            int height = Math.min(blockLength,A.row1-A.row0);

            // y = y + u_i*v_i^t*u + v_i*u_i^t*u

            // v_i^t*u
            double dot_v_u = BlockHouseHolder.innerProdRow(blockLength, row+1,
                    A,row,height,V,i,height);
            // u_i^t*u
            double dot_u_u = BlockHouseHolder.innerProdRow(blockLength, row+1,
                    A,row,height,A,i,height);

            // y = y - u_i*(v_i^t*u)
            // the ones in these 'u' are skipped over since A is only updated 
            BlockHouseHolder.plusScale_row(blockLength,row+1,dot_v_u,V,row,A,i);

            // y = y - v_i*(u_i^t*u)
            // the 1 in U is taken account above
            BlockHouseHolder.plusScale_row(blockLength,row+1,dot_u_u,V,row,V,i);
        }

        // y = gamma*y
        BlockHouseHolder.scaleElementsRow(blockLength,V,row,row+1,-gamma);
    }

    /**
     * <p>
     * Multiples the appropriate submatrix of A by the specified reflector and stores
     * the result ('y') in V.<br>
     * <br>
     * y = A*u<br>
     * </p>
     *
     * @param blockLength
     * @param A Contains the 'A' matrix and 'u' vector.
     * @param V Where resulting 'y' row vectors are stored.
     * @param row row in matrix 'A' that 'u' vector and the row in 'V' that 'y' is stored in.
     */
    // TODO does not take in account only the upper triangle being valid
    public static void multA_u( final int blockLength ,
                                final D1Submatrix64F A ,
                                final D1Submatrix64F V ,
                                int row )
    {
        int heightMatA = A.row1-A.row0;

        int heightU = Math.min(blockLength,A.row1-A.row0);

        for( int i = row+1; i < heightMatA; i++ ) {
            int heightA = Math.min(blockLength,heightMatA-(i-i%blockLength));
            double val = BlockHouseHolder.innerProdRow(blockLength, row+1, A,
                    row,heightU,A,i,heightA);

            V.set(row,i,val);
        }
    }

    /**
     * <p>
     * Final computation for a single row of 'v':<br>
     * <br>
     * v = y -(1/2)&gamma;(y^T*u)*u
     * </p>
     *
     * @param blockLength
     * @param A
     * @param V
     * @param row
     * @param gamma
     */
    public static void computeRowOfV( final int blockLength ,
                                      final D1Submatrix64F A ,
                                      final D1Submatrix64F V ,
                                      int row ,
                                      double gamma )
    {
        int height = Math.min(blockLength,A.row1-A.row0);

        // val=(y^T*u)
        double val = BlockHouseHolder.innerProdRow(blockLength, row+1,
                A,row,height,V,row,height);

        // take in account the one
        double before = A.get(row,row+1);
        A.set(row,row+1,1);

        // v = y - (1/2)gamma*val * u
        BlockHouseHolder.plusScale_row(blockLength,row+1,-0.5*gamma*val,V,row,A,row);

        A.set(row,row+1,before);
    }
}
