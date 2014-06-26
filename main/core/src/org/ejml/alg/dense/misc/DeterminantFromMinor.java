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

package org.ejml.alg.dense.misc;

import org.ejml.data.DenseMatrix64F;
import org.ejml.data.RowD1Matrix64F;


/**
 * <p>
 * Computes the determinant of a matrix using Laplace expansion.  This is done
 * using minor matrices as is shown below:<br>
 * <br>
 * |A| = Sum{ i=1:k ; a<sub>ij</sub> C<sub>ij</sub> }<br>
 * <br>
 * C<sub>ij</sub> = (-1)<sup>i+j</sup> M<sub>ij</sub><br>
 * <br>
 * Where M_ij is the minor of matrix A formed by eliminating row i and column j from A.
 * </p>
 *
 * <p>
 * This is significantly more computationally expensive than using LU decomposition, but
 * its computation has the advantage being independent of the matrices value.
 * </p>
 *
 * @see org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt_D64
 *
 * @author Peter Abeles
 */
public class DeterminantFromMinor {

    // how wide the square matrix is
    private int width;

    // used to decide at which point it uses a direct algorithm to compute the determinant
    private int minWidth;

    // used to keep track of which submatrix it is computing the results for
    private int []levelIndexes;
    // the results at different levels of minor matrices
    private double []levelResults;
    // which columns where removed at what level
    private int []levelRemoved;

    // columns that are currently open
    private int open[];
    private int numOpen;
    // a minor matrix which is created at the lowest level
    private DenseMatrix64F tempMat;

    private boolean dirty = false;

    /**
     *
     * @param width The width of the matrices that it will be computing the determinant for
     */
    public DeterminantFromMinor( int width )
    {
        this(width,5);
    }

    /**
     *
     * @param width The width of the matrices that it will be computing the determinant for
     * @param minWidth At which point should it use a predefined function to compute the determinant.
     */
    public DeterminantFromMinor( int width , int minWidth )
    {
        if( minWidth > 5 || minWidth < 2 ) {
            throw new IllegalArgumentException("No direct function for that width");
        }

        if( width < minWidth )
            minWidth = width;

        this.minWidth = minWidth;
        this.width = width;

        int numLevels = width-(minWidth-2);

        levelResults = new double[numLevels];
        levelRemoved = new int[numLevels];
        levelIndexes = new int[numLevels];

        open = new int[ width ];

        tempMat = new DenseMatrix64F(minWidth-1,minWidth-1);
    }

    /**
     * Computes the determinant for the specified matrix.  It must be square and have
     * the same width and height as what was specified in the constructor.
     *
     * @param mat The matrix whose determinant is to be computed.
     * @return The determinant.
     */
    public double compute( RowD1Matrix64F mat ) {
        if( width != mat.numCols || width != mat.numRows ) {
            throw new RuntimeException("Unexpected matrix dimension");
        }

        // make sure everything is in the proper state before it starts
        initStructures();

//        System.arraycopy(mat.data,0,minorMatrix[0],0,mat.data.length);

        int level = 0;
        while( true ) {
            int levelWidth = width-level;
            int levelIndex = levelIndexes[level];

            if( levelIndex == levelWidth ) {
                if( level == 0 ) {
                    return levelResults[0];
                }
                int prevLevelIndex = levelIndexes[level-1]++;

                double val = mat.get((level-1)*width+levelRemoved[level-1]);
                if( prevLevelIndex % 2 == 0 ) {
                    levelResults[level-1] += val * levelResults[level];
                } else {
                    levelResults[level-1] -= val * levelResults[level];
                }

                putIntoOpen(level-1);

                levelResults[level] = 0;
                levelIndexes[level] = 0;
                level--;
            } else {
                int excluded = openRemove( levelIndex );

                levelRemoved[level] = excluded;

                if( levelWidth == minWidth ) {
                    createMinor(mat);
                    double subresult = mat.get(level*width+levelRemoved[level]);

                    subresult *= UnrolledDeterminantFromMinor.det(tempMat);

                    if( levelIndex % 2 == 0 ) {
                        levelResults[level] += subresult;
                    } else {
                        levelResults[level] -= subresult;
                    }

                    // put it back into the list
                    putIntoOpen(level);
                    levelIndexes[level]++;
                } else {
                    level++;
                }
            }
        }
    }

    private void initStructures() {
        for( int i = 0; i < width; i++ ) {
            open[i] = i;
        }
        numOpen = width;

        if( dirty ) {
            for( int i = 0; i < levelIndexes.length; i++ ) {
                levelIndexes[i] = 0;
                levelResults[i] = 0;
                levelRemoved[i] = 0;
            }
        }
        dirty = true;
    }

    private int openRemove( int where ) {
        int val = open[where];

        System.arraycopy(open,where+1,open,where,(numOpen-where-1));
        numOpen--;

        return val;
    }

    private void openAdd( int where, int val )
    {
        for( int i = numOpen; i > where; i-- ) {
            open[i] = open[i-1];
        }
        numOpen++;
        open[where] = val;
    }

    private void openAdd( int val ) {
        open[numOpen++] = val;
    }

    private void putIntoOpen(int level) {
        boolean added = false;
        for( int i = 0; i < numOpen; i++ ) {
            if( open[i] > levelRemoved[level]) {
                added = true;
                openAdd(i,levelRemoved[level]);
                break;
            }
        }
        if( !added ) {
            openAdd(levelRemoved[level]);
        }
    }

    private void createMinor( RowD1Matrix64F mat ) {

        int w = minWidth-1;
        int firstRow = (width-w)*width;

        for( int i = 0; i < numOpen; i++ ) {
            int col = open[i];
            int srcIndex = firstRow + col;
            int dstIndex = i;

            for( int j = 0; j < w; j++ ) {
                tempMat.set( dstIndex , mat.get( srcIndex ) );
                dstIndex += w;
                srcIndex += width;
            }
        }
    }
}
