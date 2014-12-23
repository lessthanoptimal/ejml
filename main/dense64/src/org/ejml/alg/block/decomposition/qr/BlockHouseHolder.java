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

package org.ejml.alg.block.decomposition.qr;

import org.ejml.alg.block.BlockInnerMultiplication;
import org.ejml.alg.block.BlockVectorOps;
import org.ejml.data.D1Submatrix64F;

/**
 *
 * <p>
 * Contains various helper functions for performing a block matrix QR decomposition.
 * </p>
 *
 * <p>
 * Assumptions:
 * <ul>
 *  <le> All submatrices are aligned along the inner blocks of the {@link org.ejml.data.BlockMatrix64F}.
 *  <le> Some times vectors are assumed to have leading zeros and a one.
 * </ul>
 *
 * @author Peter Abeles
 */
public class BlockHouseHolder {

    /**
     * Performs a standard QR decomposition on the specified submatrix that is one block wide.
     *
     * @param blockLength
     * @param Y
     * @param gamma
     */
    public static boolean decomposeQR_block_col( final int blockLength ,
                                                 final D1Submatrix64F Y ,
                                                 final double gamma[] )
    {
        int width = Y.col1-Y.col0;
        int height = Y.row1-Y.row0;
        int min = Math.min(width,height);
        for( int i = 0; i < min; i++ ) {
            // compute the householder vector
            if (!computeHouseHolderCol(blockLength, Y, gamma, i))
                return false;

            // apply to rest of the columns in the block
            rank1UpdateMultR_Col(blockLength,Y,i,gamma[Y.col0+i]);
        }

        return true;
    }

    /**
     * <p>
     * Computes the householder vector that is used to create reflector for the column.
     * The results are stored in the original matrix.
     * </p>
     *
     * <p>
     * The householder vector 'u' is computed as follows:<br>
     * <br>
     * u(1) = 1 <br>
     * u(i) = x(i)/(&tau; + x(1))<br>
     * </p>
     *
     * The first element is implicitly assumed to be one and not written.
     *
     * @return If there was any problems or not. true = no problem.
     */
    public static boolean computeHouseHolderCol( final int blockLength, final D1Submatrix64F Y,
                                                 final double[] gamma, final int i) {
        double max = BlockHouseHolder.findMaxCol(blockLength,Y,i);

        if( max == 0.0 ) {
            return false;
        } else {
            // computes tau and normalizes u by max
            double tau = computeTauAndDivideCol(blockLength, Y, i, max);

            // divide u by u_0
            double u_0 = Y.get(i,i) + tau;
            divideElementsCol(blockLength,Y,i, u_0 );

            gamma[Y.col0+i] = u_0/tau;
            tau *= max;

            // after the reflector is applied the column would be all zeros but be -tau in the first element
            Y.set(i,i,-tau);
        }
        return true;
    }

    /**
     * <p>
     * Computes the householder vector from the specified row
     * </p>
     *
     * <p>
     * The householder vector 'u' is computed as follows:<br>
     * <br>
     * u(1) = 1 <br>
     * u(i) = x(i)/(&tau; + x(1))<br>
     * </p>
     *
     * The first element is implicitly assumed to be one and not written.
     *
     * @return If there was any problems or not. true = no problem.
     */
    public static boolean computeHouseHolderRow( final int blockLength, final D1Submatrix64F Y,
                                                 final double[] gamma, final int i) {
        double max = BlockHouseHolder.findMaxRow(blockLength,Y,i,i+1);

        if( max == 0.0 ) {
            return false;
        } else {
            // computes tau and normalizes u by max
            double tau = computeTauAndDivideRow(blockLength, Y, i,i+1, max);

            // divide u by u_0
            double u_0 = Y.get(i,i+1) + tau;
            BlockVectorOps.div_row(blockLength,Y,i,u_0,Y,i,i+1,Y.col1-Y.col0);   

            gamma[Y.row0+i] = u_0/tau;

            // after the reflector is applied the column would be all zeros but be -tau in the first element
            Y.set(i,i+1,-tau*max);
        }
        return true;
    }

    /**
     * <p>
     * Applies a householder reflector stored in column 'col' to the remainder of the columns
     * in the block after it.  Takes in account leading zeros and one.<br>
     * <br>
     * A = (I - &gamma;*u*u<sup>T</sup>)*A<br>
     * </p>
     *
     * @param A submatrix that is at most one block wide and aligned along inner blocks
     * @param col The column in A containing 'u'
     *
     */
    public static void rank1UpdateMultR_Col( final int blockLength ,
                                            final D1Submatrix64F A , final int col , final double gamma )
    {
        final int width = Math.min(blockLength,A.col1 - A.col0);

        final double dataA[] = A.original.data;

        for( int j = col+1; j < width; j++ ) {

            // total = U^T * A(:,j)
            double total = innerProdCol(blockLength, A, col, width, j, width);

            total *= gamma;
            // A(:,j) - gamma*U*total

            for( int i = A.row0; i < A.row1; i += blockLength ) {
                int height = Math.min( blockLength , A.row1 - i );

                int indexU = i*A.original.numCols + height*A.col0 + col;
                int indexA = i*A.original.numCols + height*A.col0 + j;

                if( i == A.row0 ) {
                    indexU += width*(col+1);
                    indexA += width*col;

                    dataA[ indexA ] -= total;

                    indexA += width;

                    for( int k = col+1; k < height; k++ , indexU += width, indexA += width ) {
                        dataA[ indexA ] -= total*dataA[ indexU ];
                    }
                } else {
                    int endU = indexU + width*height;
                    // for( int k = 0; k < height; k++
                    for( ; indexU != endU; indexU += width, indexA += width ) {
                        dataA[ indexA ] -= total*dataA[ indexU ];
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Applies a householder reflector stored in column 'col' to the top block row (excluding
     * the first column) of A.  Takes in account leading zeros and one.<br>
     * <br>
     * A = (I - &gamma;*u*u<sup>T</sup>)*A<br>
     * </p>
     *
     * @param A submatrix that is at most one block wide and aligned along inner blocks
     * @param col The column in A containing 'u'
     *
     */
    public static void rank1UpdateMultR_TopRow( final int blockLength ,
                                                final D1Submatrix64F A , final int col , final double gamma )
    {
        final double dataA[] = A.original.data;

        final int widthCol = Math.min( blockLength , A.col1 - col );

        // step through columns in top block, skipping over the first block
        for( int colStartJ = A.col0 + blockLength; colStartJ < A.col1; colStartJ += blockLength ) {
            final int widthJ = Math.min( blockLength , A.col1 - colStartJ);

            for( int j = 0; j < widthJ; j++ ) {
                // total = U^T * A(:,j) * gamma
                double total = innerProdCol(blockLength, A, col, widthCol, (colStartJ-A.col0)+j, widthJ)*gamma;

                // A(:,j) - gamma*U*total
                // just update the top most block
                int i = A.row0;
                int height = Math.min( blockLength , A.row1 - i );

                int indexU = i*A.original.numCols + height*A.col0 + col;
                int indexA = i*A.original.numCols + height*colStartJ + j;

                // take in account zeros and one
                indexU += widthCol*(col+1);
                indexA += widthJ*col;

                dataA[ indexA ] -= total;

                indexA += widthJ;

                for( int k = col+1; k < height; k++ , indexU += widthCol, indexA += widthJ ) {
                    dataA[ indexA ] -= total*dataA[ indexU ];
                }
            }
        }
    }

    /**
     * <p>
     * Applies a householder reflector stored in row 'row' to the remainder of the row
     * in the block after it.  Takes in account leading zeros and one.<br>
     * <br>
     * A = A*(I - &gamma;*u*u<sup>T</sup>)<br>
     * </p>
     *
     * @param A submatrix that is block aligned
     * @param row The row in A containing 'u'
     * @param colStart First index in 'u' that the reflector starts at
     *
     */
    public static void rank1UpdateMultL_Row( final int blockLength ,
                                             final D1Submatrix64F A ,
                                             final int row , final int colStart , final double gamma )
    {
        final int height = Math.min(blockLength,A.row1 - A.row0);

        final double dataA[] = A.original.data;

        int zeroOffset = colStart-row;

        for( int i = row+1; i < height; i++ ) {
            // total = U^T * A(i,:)
            double total = innerProdRow(blockLength, A, row, A , i, zeroOffset );

            total *= gamma;
            // A(i,:) - gamma*U*total

            for( int j = A.col0; j < A.col1; j += blockLength ) {
                int width = Math.min( blockLength , A.col1 - j );

                int indexU = A.row0*A.original.numCols + height*j + row*width;
                int indexA = A.row0*A.original.numCols + height*j + i*width;

                if( j == A.col0 ) {
                    indexU += colStart+1;
                    indexA += colStart;

                    dataA[indexA++] -= total;

                    for( int k = colStart+1; k < width; k++ ) {
                        dataA[ indexA++ ] -= total*dataA[ indexU++ ];
                    }
                } else {
                    for( int k = 0; k < width; k++ ) {
                        dataA[ indexA++ ] -= total*dataA[ indexU++ ];
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Applies a householder reflector stored in row 'row' to the left column block.
     * Takes in account leading zeros and one.<br>
     * <br>
     * A = A*(I - &gamma;*u*u<sup>T</sup>)<br>
     * </p>
     *
     * @param A submatrix that is block aligned
     * @param row The row in A containing 'u'
     * @param zeroOffset How far off the diagonal is the first element in 'u'
     *
     */
    public static void rank1UpdateMultL_LeftCol( final int blockLength ,
                                                 final D1Submatrix64F A ,
                                                 final int row , final double gamma , int zeroOffset )
    {
        final int heightU = Math.min(blockLength,A.row1 - A.row0);
        final int width = Math.min(blockLength,A.col1-A.col0);

        final double data[] = A.original.data;

        for( int blockStart = A.row0+blockLength; blockStart < A.row1; blockStart += blockLength) {
            final int heightA = Math.min(blockLength,A.row1 - blockStart);

            for( int i = 0; i < heightA; i++ ) {

                // total = U^T * A(i,:)
                double total = innerProdRow(blockLength, A, row, A, i+(blockStart-A.row0), zeroOffset);

                total *= gamma;

                // A(i,:) - gamma*U*total
//                plusScale_row(blockLength,);

                int indexU = A.row0*A.original.numCols + heightU*A.col0 + row*width;
                int indexA = blockStart*A.original.numCols + heightA*A.col0 + i*width;

                // skip over zeros and assume first element in U is 1
                indexU += zeroOffset+1;
                indexA += zeroOffset;

                data[indexA++] -= total;

                for( int k = zeroOffset+1; k < width; k++ ) {
                    data[ indexA++ ] -= total*data[ indexU++ ];
                }

            }
        }
    }

    /**
     * <p>
     * Computes the inner product of column vector 'colA' against column vector 'colB' while taking account leading zeros and one.<br>
     * <br>
     * ret = a<sup>T*b
     * </p>
     *
     * <p>
     * Column A is assumed to be a householder vector.  Element at 'colA' is one and previous ones are zero.
     * </p>
     *
     * @param blockLength
     * @param A block aligned submatrix.
     * @param colA Column inside the block of first column vector.
     * @param widthA how wide the column block that colA is inside of.
     * @param colB Column inside the block of second column vector.
     * @param widthB how wide the column block that colB is inside of.
     * @return dot product of the two vectors.
     */
    public static double innerProdCol( int blockLength, D1Submatrix64F A,
                                          int colA, int widthA,
                                          int colB, int widthB ) {
        double total = 0;

        final double data[] = A.original.data;
        // first column in the blocks
        final int colBlockA = A.col0 + colA - colA % blockLength;
        final int colBlockB = A.col0 + colB - colB % blockLength;
        colA = colA % blockLength;
        colB = colB % blockLength;

        // compute dot product down column vectors
        for( int i = A.row0; i < A.row1; i += blockLength ) {

            int height = Math.min( blockLength , A.row1 - i );

            int indexA = i*A.original.numCols + height*colBlockA + colA;
            int indexB = i*A.original.numCols + height*colBlockB + colB;

            if( i == A.row0 ) {
                // handle leading zeros
                indexA += widthA*(colA + 1);
                indexB += widthB*colA;

                // handle leading one
                total = data[indexB];

                indexB += widthB;

                // standard vector dot product
                int endA = indexA + (height - colA - 1)*widthA;
                for( ; indexA != endA; indexA += widthA, indexB += widthB ) {
//                    for( int k = col+1; k < height; k++ , indexU += width, indexA += width ) {
                    total += data[indexA] * data[indexB];
                }
            } else {
                // standard vector dot product
                int endA = indexA + widthA*height;
//                    for( int k = 0; k < height; k++ ) {
                for( ; indexA != endA; indexA += widthA, indexB += widthB ) {
                    total += data[indexA] * data[indexB];
                }
            }
        }
        return total;
    }

    /**
     * <p>
     * Computes the inner product of row vector 'rowA' against row vector 'rowB' while taking account leading zeros and one.<br>
     * <br>
     * ret = a<sup>T</sup>*b
     * </p>
     *
     * <p>
     * Row A is assumed to be a householder vector.  Element at 'colStartA' is one and previous elements are zero.
     * </p>
     *
     * @param blockLength
     * @param A block aligned submatrix.
     * @param rowA Row index inside the sub-matrix of first row vector has zeros and ones..
     * @param rowB Row index inside the sub-matrix of second row vector.
     * @return dot product of the two vectors.
     */
    public static double innerProdRow(int blockLength,
                                      D1Submatrix64F A,
                                      int rowA,
                                      D1Submatrix64F B,
                                      int rowB, int zeroOffset ) {
        int offset = rowA + zeroOffset;
        if( offset + B.col0 >= B.col1 )
            return 0;

        // take in account the one in 'A'
        double total = B.get(rowB,offset);

        total += BlockVectorOps.dot_row(blockLength,A,rowA,B,rowB,offset+1,A.col1-A.col0);

        return total;
    }

    public static void add_row( final int blockLength ,
                                D1Submatrix64F A , int rowA , double alpha ,
                                D1Submatrix64F B , int rowB , double beta ,
                                D1Submatrix64F C , int rowC ,
                                int zeroOffset , int end ) {
        int offset = rowA+zeroOffset;

        if( C.col0 + offset >= C.col1 )
            return;
        // handle leading one
        C.set(rowC,offset,alpha+B.get(rowB,offset)*beta);

        BlockVectorOps.add_row(blockLength,A,rowA,alpha,B,rowB,beta,C,rowC,offset+1,end);
    }

    /**
     * Divides the elements at the specified column by 'val'.  Takes in account
     * leading zeros and one.
     */
    public static void divideElementsCol( final int blockLength ,
                                       final D1Submatrix64F Y , final int col , final double val ) {
        final int width = Math.min(blockLength,Y.col1-Y.col0);

        final double dataY[] = Y.original.data;

        for( int i = Y.row0; i < Y.row1; i += blockLength ) {
            int height = Math.min( blockLength , Y.row1 - i );

            int index = i*Y.original.numCols + height*Y.col0 + col;

            if( i == Y.row0 ) {
                index += width*(col+1);

                for( int k = col+1; k < height; k++ , index += width ) {
                    dataY[index] /= val;
                }
            } else {
                int endIndex = index + width*height;
                //for( int k = 0; k < height; k++
                for( ; index != endIndex; index += width ) {
                    dataY[index] /= val;
                }
            }
        }
    }


    /**
     * Scales the elements in the specified row starting at element colStart by 'val'.<br>
     * W = val*Y
     *
     * Takes in account zeros and leading one automatically.
     *
     * @param zeroOffset How far off the diagonal is the first element in the vector.
     */
    public static void scale_row( final int blockLength ,
                                  final D1Submatrix64F Y ,
                                  final D1Submatrix64F W ,
                                  final int row ,
                                  final int zeroOffset,
                                  final double val ) {


        int offset = row+zeroOffset;

        if( offset >= W.col1-W.col0 )
            return;
        
        // handle the one
        W.set(row,offset,val);

        // scale rest of the vector
        BlockVectorOps.scale_row(blockLength,Y,row,val,W,row,offset+1,Y.col1-Y.col0);
    }

    /**
     * <p>
     * From the specified column of Y tau is computed and each element is divided by 'max'.
     * See code below:
     * </p>
     *
     * <pre>
     * for i=col:Y.numRows
     *   Y[i][col] = u[i][col] / max
     *   tau = tau + u[i][col]*u[i][col]
     * end
     * tau = sqrt(tau)
     * if( Y[col][col] < 0 )
     *    tau = -tau;
     * </pre>
     *
     */
    public static double computeTauAndDivideCol( final int blockLength ,
                                                 final D1Submatrix64F Y ,
                                                 final int col , final double max ) {
        final int width = Math.min(blockLength,Y.col1-Y.col0);

        final double dataY[] = Y.original.data;

        double top=0;
        double norm2 = 0;

        for( int i = Y.row0; i < Y.row1; i += blockLength ) {
            int height = Math.min( blockLength , Y.row1 - i );

            int index = i*Y.original.numCols + height*Y.col0 + col;

            if( i == Y.row0 ) {
                index += width*col;
                // save this value so that the sign can be determined later on
                top = dataY[index] /= max;
                norm2 += top*top;
                index += width;

                for( int k = col+1; k < height; k++ , index += width ) {
                    double val = dataY[index] /= max;
                    norm2 += val*val;
                }
            } else {
                for( int k = 0; k < height; k++ , index += width ) {
                    double val = dataY[index] /= max;
                    norm2 += val*val;
                }
            }
        }

        norm2 = Math.sqrt(norm2);

        if( top < 0 )
            norm2 = -norm2;

        return norm2;
    }

    /**
     * <p>
     * From the specified row of Y tau is computed and each element is divided by 'max'.
     * See code below:
     * </p>
     *
     * <pre>
     * for j=row:Y.numCols
     *   Y[row][j] = u[row][j] / max
     *   tau = tau + u[row][j]*u[row][j]
     * end
     * tau = sqrt(tau)
     * if( Y[row][row] < 0 )
     *    tau = -tau;
     * </pre>
     *
     * @param row Which row in the block will be processed
     * @param colStart The first column that computation of tau will start at
     * @param max used to normalize and prevent buffer over flow
     *
     */
    public static double computeTauAndDivideRow( final int blockLength ,
                                                 final D1Submatrix64F Y ,
                                                 final int row , int colStart , final double max ) {
        final int height = Math.min(blockLength , Y.row1-Y.row0);

        final double dataY[] = Y.original.data;

        double top=0;
        double norm2 = 0;

        int startJ = Y.col0 + colStart - colStart%blockLength;
        colStart = colStart%blockLength;

        for( int j = startJ; j < Y.col1; j += blockLength ) {
            int width = Math.min( blockLength , Y.col1 - j );

            int index = Y.row0*Y.original.numCols + height*j + row*width;

            if( j == startJ ) {
                index += colStart;
                // save this value so that the sign can be determined later on
                top = dataY[index] /= max;
                norm2 += top*top;
                index++;

                for( int k = colStart+1; k < width; k++ ) {
                    double val = dataY[index++] /= max;
                    norm2 += val*val;
                }
            } else {
                for( int k = 0; k < width; k++ ) {
                    double val = dataY[index++] /= max;
                    norm2 += val*val;
                }
            }
        }

        norm2 = Math.sqrt(norm2);

        if( top < 0 )
            norm2 = -norm2;

        return norm2;
    }

    /**
     * Finds the element in the column with the largest absolute value. The offset
     * from zero is automatically taken in account based on the column.
     */
    public static double findMaxCol( final int blockLength , final D1Submatrix64F Y , final int col )
    {
        final int width = Math.min(blockLength,Y.col1-Y.col0);

        final double dataY[] = Y.original.data;

        double max = 0;

        for( int i = Y.row0; i < Y.row1; i += blockLength ) {
            int height = Math.min( blockLength , Y.row1 - i );

            int index = i*Y.original.numCols + height*Y.col0 + col;

            if( i == Y.row0 ) {
                index += width*col;
                for( int k = col; k < height; k++ , index += width ) {
                    double v = Math.abs(dataY[index]);
                    if( v > max ) {
                        max = v;
                    }
                }
            } else {
                for( int k = 0; k < height; k++ , index += width ) {
                    double v = Math.abs(dataY[index]);
                    if( v > max ) {
                        max = v;
                    }
                }
            }
        }

        return max;
    }

    /**
     * Finds the element in the column with the largest absolute value. The offset
     * from zero is automatically taken in account based on the column.
     */
    public static double findMaxRow( final int blockLength ,
                                          final D1Submatrix64F Y ,
                                          final int row , final int colStart ) {
        final int height = Math.min(blockLength , Y.row1-Y.row0);

        final double dataY[] = Y.original.data;

        double max = 0;

        for( int j = Y.col0; j < Y.col1; j += blockLength ) {
            int width = Math.min( blockLength , Y.col1 - j );

            int index = Y.row0*Y.original.numCols + height*j + row*width;

            if( j == Y.col0 ) {
                index += colStart;

                for( int k = colStart; k < width; k++ ) {
                    double v = Math.abs(dataY[index++]);
                    if( v > max ) {
                        max = v;
                    }
                }
            } else {
                for( int k = 0; k < width; k++ ) {
                    double v = Math.abs(dataY[index++]);
                    if( v > max ) {
                        max = v;
                    }
                }
            }
        }

        return max;
    }

    /**
     * <p>
     * Computes W from the householder reflectors stored in the columns of the column block
     * submatrix Y.
     * </p>
     *
     * <p>
     * Y = v<sup>(1)</sup><br>
     * W = -&beta;<sub>1</sub>v<sup>(1)</sup><br>
     * for j=2:r<br>
     * &nbsp;&nbsp;z = -&beta;(I +WY<sup>T</sup>)v<sup>(j)</sup> <br>
     * &nbsp;&nbsp;W = [W z]<br>
     * &nbsp;&nbsp;Y = [Y v<sup>(j)</sup>]<br>
     * end<br>
     * <br>
     * where v<sup>(.)</sup> are the house holder vectors, and r is the block length.  Note that
     * Y already contains the householder vectors so it does not need to be modified.
     * </p>
     *
     * <p>
     * Y and W are assumed to have the same number of rows and columns.
     * </p>
     *
     * @param Y Input matrix containing householder vectors.  Not modified.
     * @param W Resulting W matrix. Modified.
     * @param temp Used internally.  Must have W.numCols elements.
     * @param beta Beta's for householder vectors.
     * @param betaIndex Index of first relevant beta.
     */
    public static void computeW_Column( final int blockLength ,
                                        final D1Submatrix64F Y , final D1Submatrix64F W ,
                                        final double temp[], final double beta[] , int betaIndex ) {

        final int widthB = W.col1-W.col0;

        // set the first column in W
        initializeW(blockLength, W, Y, widthB, beta[betaIndex++]);

        final int min = Math.min(widthB,W.row1-W.row0);

        // set up rest of the columns
        for( int j = 1; j < min; j++ ) {
            //compute the z vector and insert it into W
            computeY_t_V(blockLength,Y,j,temp);
            computeZ(blockLength,Y,W,j,temp,beta[betaIndex++]);
        }
    }

    /**
     * <p>
     * Sets W to its initial value using the first column of 'y' and the value of 'b':
     * <br>
     * W = -&beta;v<br>
     * <br>
     * where v = Y(:,0).
     * </p>
     *
     * @param blockLength size of the inner block
     * @param W Submatrix being initialized.
     * @param Y Contains householder vector
     * @param widthB How wide the W block matrix is.
     * @param b beta
     */
    public static void initializeW( final int blockLength,
                                    final D1Submatrix64F W, final D1Submatrix64F Y,
                                    final int widthB, final double b) {

        final double dataW[] = W.original.data;
        final double dataY[] = Y.original.data;

        for( int i = W.row0; i < W.row1; i += blockLength ) {
            int heightW = Math.min( blockLength , W.row1 - i );

            int indexW = i*W.original.numCols + heightW*W.col0;
            int indexY = i*Y.original.numCols + heightW*Y.col0;

            // take in account the first element in V being 1
            if( i == W.row0 ) {
                dataW[indexW] = -b;
                indexW += widthB;
                indexY += widthB;
                for( int k = 1; k < heightW; k++ , indexW += widthB , indexY += widthB ) {
                    dataW[indexW] = -b* dataY[indexY];
                }
            } else {
                for( int k = 0; k < heightW; k++ , indexW += widthB , indexY += widthB ) {
                    dataW[indexW] = -b* dataY[indexY];
                }
            }
        }
    }

    /**
     * Computes the vector z and inserts it into 'W':<br>
     * <br>
     * z = - &beta;<sub>j</sub>*(V<sup>j</sup> + W*h)<br>
     * <br>
     * where h is a vector of length 'col' and was computed using {@link #computeY_t_V}.
     * V is a column in the Y matrix. Z is a column in the W matrix.  Both Z and V are
     * column 'col'.
     */
    public static void computeZ( final int blockLength , final D1Submatrix64F Y , final D1Submatrix64F W,
                                 final int col , final double []temp , final double beta )
    {
        final int width = Y.col1-Y.col0;

        final double dataW[] = W.original.data;
        final double dataY[] = Y.original.data;

        final int colsW = W.original.numCols;

        final double beta_neg = -beta;

        for( int i = Y.row0; i < Y.row1; i += blockLength ) {
            int heightW = Math.min( blockLength , Y.row1 - i );

            int indexW = i*colsW + heightW*W.col0;
            int indexZ = i*colsW + heightW*W.col0 + col;
            int indexV = i*Y.original.numCols + heightW*Y.col0 + col;

            if( i == Y.row0 ) {
                // handle the triangular portion with the leading zeros and the one
                for( int k = 0; k < heightW; k++ , indexZ += width, indexW += width , indexV += width ) {
                    // compute the rows of W * h
                    double total = 0;

                    for( int j = 0; j < col; j++ ) {
                        total += dataW[indexW+j] * temp[j];
                    }

                    // add the two vectors together and multiply by -beta
                    if( k < col ) {  // zeros
                        dataW[indexZ] = -beta*total;
                    } else if( k == col ) { // one
                        dataW[indexZ] = beta_neg*(1.0 + total);
                    } else { // normal data
                        dataW[indexZ] = beta_neg*(dataY[indexV] + total);
                    }
                }
            } else {
                int endZ = indexZ + width*heightW;
//                for( int k = 0; k < heightW; k++ ,
                while( indexZ != endZ ) {
                    // compute the rows of W * h
                    double total = 0;

                    for( int j = 0; j < col; j++ ) {
                        total += dataW[indexW+j] * temp[j];
                    }

                    // add the two vectors together and multiply by -beta
                    dataW[indexZ] = beta_neg*(dataY[indexV] + total);

                    indexZ += width; indexW += width; indexV += width;
                }
            }
        }
    }

    /**
     * Computes Y<sup>T</sup>v<sup>(j)</sup>.  Where Y are the columns before 'col' and v is the column
     * at 'col'.  The zeros and ones are taken in account.  The solution is a vector with 'col' elements.
     *
     * width of Y must be along the block of original matrix A
     *
     * @param temp Temporary storage of least length 'col'
     */
    public static void computeY_t_V( final int blockLength , final D1Submatrix64F Y ,
                                     final int col , final double []temp )
    {
        final int widthB = Y.col1-Y.col0;

        for( int j = 0; j < col; j++ ) {
            temp[j] = innerProdCol(blockLength,Y,col,widthB,j,widthB);
        }
    }

    /**
     * Special multiplication that takes in account the zeros and one in Y, which
     * is the matrix that stores the householder vectors.
     *
     */
    public static void multAdd_zeros( final int blockLength ,
                                      final D1Submatrix64F Y , final D1Submatrix64F B ,
                                      final D1Submatrix64F C )
    {
        int widthY = Y.col1 - Y.col0;

        for( int i = Y.row0; i < Y.row1; i += blockLength ) {
            int heightY = Math.min( blockLength , Y.row1 - i );

            for( int j = B.col0; j < B.col1; j += blockLength ) {
                int widthB = Math.min( blockLength , B.col1 - j );

                int indexC = (i-Y.row0+C.row0)*C.original.numCols + (j-B.col0+C.col0)*heightY;

                for( int k = Y.col0; k < Y.col1; k += blockLength ) {
                    int indexY = i*Y.original.numCols + k*heightY;
                    int indexB = (k-Y.col0+B.row0)*B.original.numCols + j*widthY;

                    if( i == Y.row0 ) {
                        multBlockAdd_zerosone(Y.original.data,B.original.data,C.original.data,
                            indexY,indexB,indexC,heightY,widthY,widthB);
                    } else {
                        BlockInnerMultiplication.blockMultPlus(Y.original.data,B.original.data,C.original.data,
                                indexY,indexB,indexC,heightY,widthY,widthB);
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Inner block mult add operation that takes in account the zeros and on in dataA,
     * which is the top part of the Y block vector that has the householder vectors.<br>
     * <br>
     * C = C + A * B
     * </p>
     */
    public static void multBlockAdd_zerosone( double[] dataA, double []dataB, double []dataC,
                                              int indexA, int indexB, int indexC,
                                              final int heightA, final int widthA, final int widthC) {


        for( int i = 0; i < heightA; i++ ) {
            for( int j = 0; j < widthC; j++ ) {
                double val = i < widthA ? dataB[i*widthC+j+indexB] : 0;

                int end = Math.min(i,widthA);

                for( int k = 0; k < end; k++ ) {
                    val += dataA[i*widthA + k + indexA] * dataB[k*widthC + j + indexB];
                }

                dataC[ i*widthC + j + indexC ] += val;
            }
        }
    }

    /**
     * <p>
     * Performs a matrix multiplication on the block aligned submatrices.  A is
     * assumed to be block column vector that is lower triangular with diagonal elements set to 1.<br>
     * <br>
     * C = A^T * B
     * </p>
     */
    public static void multTransA_vecCol( final int blockLength ,
                                          D1Submatrix64F A , D1Submatrix64F B ,
                                          D1Submatrix64F C )
    {
        int widthA = A.col1 - A.col0;
        if( widthA > blockLength )
            throw new IllegalArgumentException("A is expected to be at most one block wide.");

        for( int j = B.col0; j < B.col1; j += blockLength ) {
            int widthB = Math.min( blockLength , B.col1 - j );

            int indexC = C.row0*C.original.numCols + (j-B.col0+C.col0)*widthA;

            for( int k = A.row0; k < A.row1; k += blockLength ) {
                int heightA = Math.min( blockLength , A.row1 - k );

                int indexA = k*A.original.numCols + A.col0*heightA;
                int indexB = (k-A.row0+B.row0)*B.original.numCols + j*heightA;

                if( k == A.row0 )
                    multTransABlockSet_lowerTriag(A.original.data,B.original.data,C.original.data,
                            indexA,indexB,indexC,heightA,widthA,widthB);
                else
                    BlockInnerMultiplication.blockMultPlusTransA(A.original.data,B.original.data,C.original.data,
                            indexA,indexB,indexC,heightA,widthA,widthB);
            }
        }
    }

    /**
     * Performs a matrix multiplication on an single inner block where A is assumed to be lower triangular with diagonal
     * elements equal to 1.<br>
     * <br>
     * C = A^T * B
     */
    protected static void multTransABlockSet_lowerTriag( double[] dataA, double []dataB, double []dataC,
                                                         int indexA, int indexB, int indexC,
                                                         final int heightA, final int widthA, final int widthC) {
        for( int i = 0; i < widthA; i++ ) {
            for( int j = 0; j < widthC; j++ ) {
                double val = i < heightA ? dataB[i*widthC + j + indexB] : 0;

                for( int k = i+1; k < heightA; k++ ) {
                    val += dataA[k*widthA + i + indexA] * dataB[k*widthC + j + indexB];
                }

                dataC[ i*widthC + j + indexC ] = val;
            }
        }
    }
}
