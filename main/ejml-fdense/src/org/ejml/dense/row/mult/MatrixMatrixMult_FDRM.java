/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.mult;

import org.ejml.MatrixDimensionException;
import org.ejml.data.FMatrix1Row;
import org.ejml.dense.row.CommonOps_FDRM;

/**
 * <p>
 * This class contains various types of matrix matrix multiplication operations for {@link FMatrix1Row}.
 * </p>
 * <p>
 * Two algorithms that are equivalent can often have very different runtime performance.
 * This is because of how modern computers uses fast memory caches to speed up reading/writing to data.
 * Depending on the order in which variables are processed different algorithms can run much faster than others,
 * even if the number of operations is the same.
 * </p>
 *
 * <p>
 * Algorithms that are labeled as 'reorder' are designed to avoid caching jumping issues, some times at the cost
 * of increasing the number of operations.  This is important for large matrices.  The straight forward 
 * implementation seems to be faster for small matrices.
 * </p>
 * 
 * <p>
 * Algorithms that are labeled as 'aux' use an auxiliary array of length n.  This array is used to create
 * a copy of an out of sequence column vector that is referenced several times.  This reduces the number
 * of cache misses.  If the 'aux' parameter passed in is null then the array is declared internally.
 * </p>
 *
 * <p>
 * Typically the straight forward implementation runs about 30% faster on smaller matrices and
 * about 5 times slower on larger matrices.  This is all computer architecture and matrix shape/size specific.
 * </p>
 * 
 * <center>******** IMPORTANT **********</center>
 * This class was auto generated using org.ejml.dense.row.mult.GeneratorMatrixMatrixMult_FDRM
 * 
 * @author Peter Abeles
 */
public class MatrixMatrixMult_FDRM {
    /**
     * @see CommonOps_FDRM#mult( org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void mult_reorder( FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numCols != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        C.reshape(A.numRows,B.numCols);

        if( A.numCols == 0 || A.numRows == 0 ) {
            CommonOps_FDRM.fill(C,0);
            return;
        }
        float valA;
        int indexCbase= 0;
        int endOfKLoop = B.numRows*B.numCols;

        for( int i = 0; i < A.numRows; i++ ) {
            int indexA = i*A.numCols;

            // need to assign C.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + B.numCols;

            valA = A.data[indexA++];

            while( indexB < end ) {
                C.set(indexC++ , valA*B.data[indexB++]);
            }

            // now add to it
            while( indexB != endOfKLoop ) { // k loop
                indexC = indexCbase;
                end = indexB + B.numCols;

                valA = A.data[indexA++];

                while( indexB < end ) { // j loop
                    C.data[indexC++] += valA*B.data[indexB++];
                }
            }
            indexCbase += C.numCols;
        }
    }

    /**
     * @see CommonOps_FDRM#mult( org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void mult_small( FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numCols != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        C.reshape(A.numRows,B.numCols);

        int aIndexStart = 0;
        int cIndex = 0;

        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < B.numCols; j++ ) {
                float total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + B.numRows;
                while( indexA < end ) {
                    total += A.data[indexA++] * B.data[indexB];
                    indexB += B.numCols;
                }

                C.set( cIndex++ , total );
            }
            aIndexStart += A.numCols;
        }
    }

    /**
     * @see CommonOps_FDRM#mult( org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void mult_aux( FMatrix1Row A , FMatrix1Row B , FMatrix1Row C , float []aux )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numCols != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        C.reshape(A.numRows,B.numCols);

        if( aux == null ) aux = new float[ B.numRows ];

        for( int j = 0; j < B.numCols; j++ ) {
            // create a copy of the column in B to avoid cache issues
            for( int k = 0; k < B.numRows; k++ ) {
                aux[k] = B.unsafe_get(k,j);
            }

            int indexA = 0;
            for( int i = 0; i < A.numRows; i++ ) {
                float total = 0;
                for( int k = 0; k < B.numRows; ) {
                    total += A.data[indexA++]*aux[k++];
                }
                C.set( i*C.numCols+j , total );
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multTransA( org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multTransA_reorder( FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numRows != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        C.reshape(A.numCols,B.numCols);

        if( A.numCols == 0 || A.numRows == 0 ) {
            CommonOps_FDRM.fill(C,0);
            return;
        }
        float valA;

        for( int i = 0; i < A.numCols; i++ ) {
            int indexC_start = i*C.numCols;

            // first assign R
            valA = A.data[i];
            int indexB = 0;
            int end = indexB+B.numCols;
            int indexC = indexC_start;
            while( indexB<end ) {
                C.set( indexC++ , valA*B.data[indexB++]);
            }
            // now increment it
            for( int k = 1; k < A.numRows; k++ ) {
                valA = A.unsafe_get(k,i);
                end = indexB+B.numCols;
                indexC = indexC_start;
                // this is the loop for j
                while( indexB<end ) {
                    C.data[indexC++] += valA*B.data[indexB++];
                }
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multTransA( org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multTransA_small( FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numRows != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        C.reshape(A.numCols,B.numCols);

        int cIndex = 0;

        for( int i = 0; i < A.numCols; i++ ) {
            for( int j = 0; j < B.numCols; j++ ) {
                int indexA = i;
                int indexB = j;
                int end = indexB + B.numRows*B.numCols;

                float total = 0;

                // loop for k
                for(; indexB < end; indexB += B.numCols ) {
                    total += A.data[indexA] * B.data[indexB];
                    indexA += A.numCols;
                }

                C.set( cIndex++ , total );
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multTransAB( org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multTransAB( FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numRows != B.numCols ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        C.reshape(A.numCols,B.numRows);

        int cIndex = 0;

        for( int i = 0; i < A.numCols; i++ ) {
            int indexB = 0;
            for( int j = 0; j < B.numRows; j++ ) {
                int indexA = i;
                int end = indexB + B.numCols;

                float total = 0;

                for( ;indexB<end; ) {
                    total += A.data[indexA] * B.data[indexB++];
                    indexA += A.numCols;
                }

                C.set( cIndex++ , total );
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multTransAB( org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multTransAB_aux( FMatrix1Row A , FMatrix1Row B , FMatrix1Row C , float []aux )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numRows != B.numCols ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        C.reshape(A.numCols,B.numRows);

        if( aux == null ) aux = new float[ A.numRows ];

        if( A.numCols == 0 || A.numRows == 0 ) {
            CommonOps_FDRM.fill(C,0);
            return;
        }
        int indexC = 0;
        for( int i = 0; i < A.numCols; i++ ) {
            for( int k = 0; k < B.numCols; k++ ) {
                aux[k] = A.unsafe_get(k,i);
            }

            for( int j = 0; j < B.numRows; j++ ) {
                float total = 0;

                for( int k = 0; k < B.numCols; k++ ) {
                    total += aux[k] * B.unsafe_get(j,k);
                }
                C.set( indexC++ , total );
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multTransB( org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multTransB( FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numCols != B.numCols ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        C.reshape(A.numRows,B.numRows);

        int cIndex = 0;
        int aIndexStart = 0;

        for( int xA = 0; xA < A.numRows; xA++ ) {
            int end = aIndexStart + B.numCols;
            int indexB = 0;
            for( int xB = 0; xB < B.numRows; xB++ ) {
                int indexA = aIndexStart;

                float total = 0;

                while( indexA<end ) {
                    total += A.data[indexA++] * B.data[indexB++];
                }

                C.set( cIndex++ , total );
            }
            aIndexStart += A.numCols;
        }
    }

    /**
     * @see CommonOps_FDRM#multAdd( org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multAdd_reorder( FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numCols != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        if( A.numRows != C.numRows || B.numCols != C.numCols)
            throw new MatrixDimensionException("C is not compatible with A and B");
        if( A.numCols == 0 || A.numRows == 0 ) {
            return;
        }
        float valA;
        int indexCbase= 0;
        int endOfKLoop = B.numRows*B.numCols;

        for( int i = 0; i < A.numRows; i++ ) {
            int indexA = i*A.numCols;

            // need to assign C.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + B.numCols;

            valA = A.data[indexA++];

            while( indexB < end ) {
                C.plus(indexC++ , valA*B.data[indexB++]);
            }

            // now add to it
            while( indexB != endOfKLoop ) { // k loop
                indexC = indexCbase;
                end = indexB + B.numCols;

                valA = A.data[indexA++];

                while( indexB < end ) { // j loop
                    C.data[indexC++] += valA*B.data[indexB++];
                }
            }
            indexCbase += C.numCols;
        }
    }

    /**
     * @see CommonOps_FDRM#multAdd( org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multAdd_small( FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numCols != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        if( A.numRows != C.numRows || B.numCols != C.numCols)
            throw new MatrixDimensionException("C is not compatible with A and B");
        int aIndexStart = 0;
        int cIndex = 0;

        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < B.numCols; j++ ) {
                float total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + B.numRows;
                while( indexA < end ) {
                    total += A.data[indexA++] * B.data[indexB];
                    indexB += B.numCols;
                }

                C.plus( cIndex++ , total );
            }
            aIndexStart += A.numCols;
        }
    }

    /**
     * @see CommonOps_FDRM#multAdd( org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multAdd_aux( FMatrix1Row A , FMatrix1Row B , FMatrix1Row C , float []aux )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numCols != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        if( A.numRows != C.numRows || B.numCols != C.numCols)
            throw new MatrixDimensionException("C is not compatible with A and B");
        if( aux == null ) aux = new float[ B.numRows ];

        for( int j = 0; j < B.numCols; j++ ) {
            // create a copy of the column in B to avoid cache issues
            for( int k = 0; k < B.numRows; k++ ) {
                aux[k] = B.unsafe_get(k,j);
            }

            int indexA = 0;
            for( int i = 0; i < A.numRows; i++ ) {
                float total = 0;
                for( int k = 0; k < B.numRows; ) {
                    total += A.data[indexA++]*aux[k++];
                }
                C.plus( i*C.numCols+j , total );
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multAddTransA( org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multAddTransA_reorder( FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numRows != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        if( A.numCols != C.numRows || B.numCols != C.numCols)
            throw new MatrixDimensionException("C is not compatible with A and B");
        if( A.numCols == 0 || A.numRows == 0 ) {
            return;
        }
        float valA;

        for( int i = 0; i < A.numCols; i++ ) {
            int indexC_start = i*C.numCols;

            // first assign R
            valA = A.data[i];
            int indexB = 0;
            int end = indexB+B.numCols;
            int indexC = indexC_start;
            while( indexB<end ) {
                C.plus( indexC++ , valA*B.data[indexB++]);
            }
            // now increment it
            for( int k = 1; k < A.numRows; k++ ) {
                valA = A.unsafe_get(k,i);
                end = indexB+B.numCols;
                indexC = indexC_start;
                // this is the loop for j
                while( indexB<end ) {
                    C.data[indexC++] += valA*B.data[indexB++];
                }
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multAddTransA( org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multAddTransA_small( FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numRows != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        if( A.numCols != C.numRows || B.numCols != C.numCols)
            throw new MatrixDimensionException("C is not compatible with A and B");
        int cIndex = 0;

        for( int i = 0; i < A.numCols; i++ ) {
            for( int j = 0; j < B.numCols; j++ ) {
                int indexA = i;
                int indexB = j;
                int end = indexB + B.numRows*B.numCols;

                float total = 0;

                // loop for k
                for(; indexB < end; indexB += B.numCols ) {
                    total += A.data[indexA] * B.data[indexB];
                    indexA += A.numCols;
                }

                C.plus( cIndex++ , total );
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multAddTransAB( org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multAddTransAB( FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numRows != B.numCols ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        if( A.numCols != C.numRows || B.numRows != C.numCols)
            throw new MatrixDimensionException("C is not compatible with A and B");
        int cIndex = 0;

        for( int i = 0; i < A.numCols; i++ ) {
            int indexB = 0;
            for( int j = 0; j < B.numRows; j++ ) {
                int indexA = i;
                int end = indexB + B.numCols;

                float total = 0;

                for( ;indexB<end; ) {
                    total += A.data[indexA] * B.data[indexB++];
                    indexA += A.numCols;
                }

                C.plus( cIndex++ , total );
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multAddTransAB( org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multAddTransAB_aux( FMatrix1Row A , FMatrix1Row B , FMatrix1Row C , float []aux )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numRows != B.numCols ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        if( A.numCols != C.numRows || B.numRows != C.numCols)
            throw new MatrixDimensionException("C is not compatible with A and B");
        if( aux == null ) aux = new float[ A.numRows ];

        if( A.numCols == 0 || A.numRows == 0 ) {
            return;
        }
        int indexC = 0;
        for( int i = 0; i < A.numCols; i++ ) {
            for( int k = 0; k < B.numCols; k++ ) {
                aux[k] = A.unsafe_get(k,i);
            }

            for( int j = 0; j < B.numRows; j++ ) {
                float total = 0;

                for( int k = 0; k < B.numCols; k++ ) {
                    total += aux[k] * B.unsafe_get(j,k);
                }
                C.plus( indexC++ , total );
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multAddTransB( org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multAddTransB( FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numCols != B.numCols ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        if( A.numRows != C.numRows || B.numRows != C.numCols)
            throw new MatrixDimensionException("C is not compatible with A and B");
        int cIndex = 0;
        int aIndexStart = 0;

        for( int xA = 0; xA < A.numRows; xA++ ) {
            int end = aIndexStart + B.numCols;
            int indexB = 0;
            for( int xB = 0; xB < B.numRows; xB++ ) {
                int indexA = aIndexStart;

                float total = 0;

                while( indexA<end ) {
                    total += A.data[indexA++] * B.data[indexB++];
                }

                C.plus( cIndex++ , total );
            }
            aIndexStart += A.numCols;
        }
    }

    /**
     * @see CommonOps_FDRM#mult(float,  org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void mult_reorder( float alpha , FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numCols != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        C.reshape(A.numRows,B.numCols);

        if( A.numCols == 0 || A.numRows == 0 ) {
            CommonOps_FDRM.fill(C,0);
            return;
        }
        float valA;
        int indexCbase= 0;
        int endOfKLoop = B.numRows*B.numCols;

        for( int i = 0; i < A.numRows; i++ ) {
            int indexA = i*A.numCols;

            // need to assign C.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + B.numCols;

            valA = alpha*A.data[indexA++];

            while( indexB < end ) {
                C.set(indexC++ , valA*B.data[indexB++]);
            }

            // now add to it
            while( indexB != endOfKLoop ) { // k loop
                indexC = indexCbase;
                end = indexB + B.numCols;

                valA = alpha*A.data[indexA++];

                while( indexB < end ) { // j loop
                    C.data[indexC++] += valA*B.data[indexB++];
                }
            }
            indexCbase += C.numCols;
        }
    }

    /**
     * @see CommonOps_FDRM#mult(float,  org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void mult_small( float alpha , FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numCols != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        C.reshape(A.numRows,B.numCols);

        int aIndexStart = 0;
        int cIndex = 0;

        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < B.numCols; j++ ) {
                float total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + B.numRows;
                while( indexA < end ) {
                    total += A.data[indexA++] * B.data[indexB];
                    indexB += B.numCols;
                }

                C.set( cIndex++ , alpha*total );
            }
            aIndexStart += A.numCols;
        }
    }

    /**
     * @see CommonOps_FDRM#mult(float,  org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void mult_aux( float alpha , FMatrix1Row A , FMatrix1Row B , FMatrix1Row C , float []aux )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numCols != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        C.reshape(A.numRows,B.numCols);

        if( aux == null ) aux = new float[ B.numRows ];

        for( int j = 0; j < B.numCols; j++ ) {
            // create a copy of the column in B to avoid cache issues
            for( int k = 0; k < B.numRows; k++ ) {
                aux[k] = B.unsafe_get(k,j);
            }

            int indexA = 0;
            for( int i = 0; i < A.numRows; i++ ) {
                float total = 0;
                for( int k = 0; k < B.numRows; ) {
                    total += A.data[indexA++]*aux[k++];
                }
                C.set( i*C.numCols+j , alpha*total );
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multTransA(float,  org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multTransA_reorder( float alpha , FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numRows != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        C.reshape(A.numCols,B.numCols);

        if( A.numCols == 0 || A.numRows == 0 ) {
            CommonOps_FDRM.fill(C,0);
            return;
        }
        float valA;

        for( int i = 0; i < A.numCols; i++ ) {
            int indexC_start = i*C.numCols;

            // first assign R
            valA = alpha*A.data[i];
            int indexB = 0;
            int end = indexB+B.numCols;
            int indexC = indexC_start;
            while( indexB<end ) {
                C.set( indexC++ , valA*B.data[indexB++]);
            }
            // now increment it
            for( int k = 1; k < A.numRows; k++ ) {
                valA = alpha*A.unsafe_get(k,i);
                end = indexB+B.numCols;
                indexC = indexC_start;
                // this is the loop for j
                while( indexB<end ) {
                    C.data[indexC++] += valA*B.data[indexB++];
                }
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multTransA(float,  org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multTransA_small( float alpha , FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numRows != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        C.reshape(A.numCols,B.numCols);

        int cIndex = 0;

        for( int i = 0; i < A.numCols; i++ ) {
            for( int j = 0; j < B.numCols; j++ ) {
                int indexA = i;
                int indexB = j;
                int end = indexB + B.numRows*B.numCols;

                float total = 0;

                // loop for k
                for(; indexB < end; indexB += B.numCols ) {
                    total += A.data[indexA] * B.data[indexB];
                    indexA += A.numCols;
                }

                C.set( cIndex++ , alpha*total );
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multTransAB(float,  org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multTransAB( float alpha , FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numRows != B.numCols ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        C.reshape(A.numCols,B.numRows);

        int cIndex = 0;

        for( int i = 0; i < A.numCols; i++ ) {
            int indexB = 0;
            for( int j = 0; j < B.numRows; j++ ) {
                int indexA = i;
                int end = indexB + B.numCols;

                float total = 0;

                for( ;indexB<end; ) {
                    total += A.data[indexA] * B.data[indexB++];
                    indexA += A.numCols;
                }

                C.set( cIndex++ , alpha*total );
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multTransAB(float,  org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multTransAB_aux( float alpha , FMatrix1Row A , FMatrix1Row B , FMatrix1Row C , float []aux )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numRows != B.numCols ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        C.reshape(A.numCols,B.numRows);

        if( aux == null ) aux = new float[ A.numRows ];

        if( A.numCols == 0 || A.numRows == 0 ) {
            CommonOps_FDRM.fill(C,0);
            return;
        }
        int indexC = 0;
        for( int i = 0; i < A.numCols; i++ ) {
            for( int k = 0; k < B.numCols; k++ ) {
                aux[k] = A.unsafe_get(k,i);
            }

            for( int j = 0; j < B.numRows; j++ ) {
                float total = 0;

                for( int k = 0; k < B.numCols; k++ ) {
                    total += aux[k] * B.unsafe_get(j,k);
                }
                C.set( indexC++ , alpha*total );
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multTransB(float,  org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multTransB( float alpha , FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numCols != B.numCols ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        C.reshape(A.numRows,B.numRows);

        int cIndex = 0;
        int aIndexStart = 0;

        for( int xA = 0; xA < A.numRows; xA++ ) {
            int end = aIndexStart + B.numCols;
            int indexB = 0;
            for( int xB = 0; xB < B.numRows; xB++ ) {
                int indexA = aIndexStart;

                float total = 0;

                while( indexA<end ) {
                    total += A.data[indexA++] * B.data[indexB++];
                }

                C.set( cIndex++ , alpha*total );
            }
            aIndexStart += A.numCols;
        }
    }

    /**
     * @see CommonOps_FDRM#multAdd(float,  org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multAdd_reorder( float alpha , FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numCols != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        if( A.numRows != C.numRows || B.numCols != C.numCols)
            throw new MatrixDimensionException("C is not compatible with A and B");
        if( A.numCols == 0 || A.numRows == 0 ) {
            return;
        }
        float valA;
        int indexCbase= 0;
        int endOfKLoop = B.numRows*B.numCols;

        for( int i = 0; i < A.numRows; i++ ) {
            int indexA = i*A.numCols;

            // need to assign C.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + B.numCols;

            valA = alpha*A.data[indexA++];

            while( indexB < end ) {
                C.plus(indexC++ , valA*B.data[indexB++]);
            }

            // now add to it
            while( indexB != endOfKLoop ) { // k loop
                indexC = indexCbase;
                end = indexB + B.numCols;

                valA = alpha*A.data[indexA++];

                while( indexB < end ) { // j loop
                    C.data[indexC++] += valA*B.data[indexB++];
                }
            }
            indexCbase += C.numCols;
        }
    }

    /**
     * @see CommonOps_FDRM#multAdd(float,  org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multAdd_small( float alpha , FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numCols != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        if( A.numRows != C.numRows || B.numCols != C.numCols)
            throw new MatrixDimensionException("C is not compatible with A and B");
        int aIndexStart = 0;
        int cIndex = 0;

        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < B.numCols; j++ ) {
                float total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + B.numRows;
                while( indexA < end ) {
                    total += A.data[indexA++] * B.data[indexB];
                    indexB += B.numCols;
                }

                C.plus( cIndex++ , alpha*total );
            }
            aIndexStart += A.numCols;
        }
    }

    /**
     * @see CommonOps_FDRM#multAdd(float,  org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multAdd_aux( float alpha , FMatrix1Row A , FMatrix1Row B , FMatrix1Row C , float []aux )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numCols != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        if( A.numRows != C.numRows || B.numCols != C.numCols)
            throw new MatrixDimensionException("C is not compatible with A and B");
        if( aux == null ) aux = new float[ B.numRows ];

        for( int j = 0; j < B.numCols; j++ ) {
            // create a copy of the column in B to avoid cache issues
            for( int k = 0; k < B.numRows; k++ ) {
                aux[k] = B.unsafe_get(k,j);
            }

            int indexA = 0;
            for( int i = 0; i < A.numRows; i++ ) {
                float total = 0;
                for( int k = 0; k < B.numRows; ) {
                    total += A.data[indexA++]*aux[k++];
                }
                C.plus( i*C.numCols+j , alpha*total );
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multAddTransA(float,  org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multAddTransA_reorder( float alpha , FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numRows != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        if( A.numCols != C.numRows || B.numCols != C.numCols)
            throw new MatrixDimensionException("C is not compatible with A and B");
        if( A.numCols == 0 || A.numRows == 0 ) {
            return;
        }
        float valA;

        for( int i = 0; i < A.numCols; i++ ) {
            int indexC_start = i*C.numCols;

            // first assign R
            valA = alpha*A.data[i];
            int indexB = 0;
            int end = indexB+B.numCols;
            int indexC = indexC_start;
            while( indexB<end ) {
                C.plus( indexC++ , valA*B.data[indexB++]);
            }
            // now increment it
            for( int k = 1; k < A.numRows; k++ ) {
                valA = alpha*A.unsafe_get(k,i);
                end = indexB+B.numCols;
                indexC = indexC_start;
                // this is the loop for j
                while( indexB<end ) {
                    C.data[indexC++] += valA*B.data[indexB++];
                }
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multAddTransA(float,  org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multAddTransA_small( float alpha , FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numRows != B.numRows ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        if( A.numCols != C.numRows || B.numCols != C.numCols)
            throw new MatrixDimensionException("C is not compatible with A and B");
        int cIndex = 0;

        for( int i = 0; i < A.numCols; i++ ) {
            for( int j = 0; j < B.numCols; j++ ) {
                int indexA = i;
                int indexB = j;
                int end = indexB + B.numRows*B.numCols;

                float total = 0;

                // loop for k
                for(; indexB < end; indexB += B.numCols ) {
                    total += A.data[indexA] * B.data[indexB];
                    indexA += A.numCols;
                }

                C.plus( cIndex++ , alpha*total );
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multAddTransAB(float,  org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multAddTransAB( float alpha , FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numRows != B.numCols ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        if( A.numCols != C.numRows || B.numRows != C.numCols)
            throw new MatrixDimensionException("C is not compatible with A and B");
        int cIndex = 0;

        for( int i = 0; i < A.numCols; i++ ) {
            int indexB = 0;
            for( int j = 0; j < B.numRows; j++ ) {
                int indexA = i;
                int end = indexB + B.numCols;

                float total = 0;

                for( ;indexB<end; ) {
                    total += A.data[indexA] * B.data[indexB++];
                    indexA += A.numCols;
                }

                C.plus( cIndex++ , alpha*total );
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multAddTransAB(float,  org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multAddTransAB_aux( float alpha , FMatrix1Row A , FMatrix1Row B , FMatrix1Row C , float []aux )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numRows != B.numCols ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        if( A.numCols != C.numRows || B.numRows != C.numCols)
            throw new MatrixDimensionException("C is not compatible with A and B");
        if( aux == null ) aux = new float[ A.numRows ];

        if( A.numCols == 0 || A.numRows == 0 ) {
            return;
        }
        int indexC = 0;
        for( int i = 0; i < A.numCols; i++ ) {
            for( int k = 0; k < B.numCols; k++ ) {
                aux[k] = A.unsafe_get(k,i);
            }

            for( int j = 0; j < B.numRows; j++ ) {
                float total = 0;

                for( int k = 0; k < B.numCols; k++ ) {
                    total += aux[k] * B.unsafe_get(j,k);
                }
                C.plus( indexC++ , alpha*total );
            }
        }
    }

    /**
     * @see CommonOps_FDRM#multAddTransB(float,  org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row, org.ejml.data.FMatrix1Row)
     */
    public static void multAddTransB( float alpha , FMatrix1Row A , FMatrix1Row B , FMatrix1Row C )
    {
        if( A == C || B == C )
            throw new IllegalArgumentException("Neither 'A' or 'B' can be the same matrix as 'C'");
        else if( A.numCols != B.numCols ) {
            throw new MatrixDimensionException("The 'A' and 'B' matrices do not have compatible dimensions");
        }
        if( A.numRows != C.numRows || B.numRows != C.numCols)
            throw new MatrixDimensionException("C is not compatible with A and B");
        int cIndex = 0;
        int aIndexStart = 0;

        for( int xA = 0; xA < A.numRows; xA++ ) {
            int end = aIndexStart + B.numCols;
            int indexB = 0;
            for( int xB = 0; xB < B.numRows; xB++ ) {
                int indexA = aIndexStart;

                float total = 0;

                while( indexA<end ) {
                    total += A.data[indexA++] * B.data[indexB++];
                }

                C.plus( cIndex++ , alpha*total );
            }
            aIndexStart += A.numCols;
        }
    }

}

