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

package org.ejml.alg.dense.mult;

import org.ejml.data.RowD1Matrix64F;

/**
 * <p>
 * This class contains various types of matrix matrix multiplication operations for {@link RowD1Matrix64F}.
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
 * <p>
 * <center>******** IMPORTANT **********</center>
 * This class was auto generated using {@link GeneratorMatrixMatrixMult}
 * If this code needs to be modified, please modify {@link GeneratorMatrixMatrixMult} instead
 * and regenerate the code by running that.
 * </p>
 * 
 * @author Peter Abeles
 */
public class MatrixMatrixMult {
    /**
     * @see org.ejml.ops.CommonOps#mult( org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void mult_reorder( RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double valA;
        int indexCbase= 0;
        int endOfKLoop = b.numRows*b.numCols;

        for( int i = 0; i < a.numRows; i++ ) {
            int indexA = i*a.numCols;

            // need to assign c.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + b.numCols;

            valA = a.get(indexA++);

            while( indexB < end ) {
                c.set(indexC++ , valA*b.get(indexB++));
            }

            // now add to it
            while( indexB != endOfKLoop ) { // k loop
                indexC = indexCbase;
                end = indexB + b.numCols;

                valA = a.get(indexA++);

                while( indexB < end ) { // j loop
                    c.plus(indexC++ , valA*b.get(indexB++));
                }
            }
            indexCbase += c.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#mult( org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void mult_small( RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        int aIndexStart = 0;
        int cIndex = 0;

        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + b.numRows;
                while( indexA < end ) {
                    total += a.get(indexA++) * b.get(indexB);
                    indexB += b.numCols;
                }

                c.set( cIndex++ , total );
            }
            aIndexStart += a.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#mult( org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void mult_aux( RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c , double []aux )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        if( aux == null ) aux = new double[ b.numRows ];

        for( int j = 0; j < b.numCols; j++ ) {
            // create a copy of the column in B to avoid cache issues
            for( int k = 0; k < b.numRows; k++ ) {
                aux[k] = b.unsafe_get(k,j);
            }

            int indexA = 0;
            for( int i = 0; i < a.numRows; i++ ) {
                double total = 0;
                for( int k = 0; k < b.numRows; ) {
                    total += a.get(indexA++)*aux[k++];
                }
                c.set( i*c.numCols+j , total );
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransA( org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multTransA_reorder( RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numCols != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double valA;

        for( int i = 0; i < a.numCols; i++ ) {
            int indexC_start = i*c.numCols;

            // first assign R
            valA = a.get(i);
            int indexB = 0;
            int end = indexB+b.numCols;
            int indexC = indexC_start;
            while( indexB<end ) {
                c.set( indexC++ , valA*b.get(indexB++));
            }
            // now increment it
            for( int k = 1; k < a.numRows; k++ ) {
                valA = a.unsafe_get(k,i);
                end = indexB+b.numCols;
                indexC = indexC_start;
                // this is the loop for j
                while( indexB<end ) {
                    c.plus( indexC++ , valA*b.get(indexB++));
                }
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransA( org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multTransA_small( RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numCols != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                int indexA = i;
                int indexB = j;
                int end = indexB + b.numRows*b.numCols;

                double total = 0;

                // loop for k
                for(; indexB < end; indexB += b.numCols ) {
                    total += a.get(indexA) * b.get(indexB);
                    indexA += a.numCols;
                }

                c.set( cIndex++ , total );
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransAB( org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multTransAB( RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numRows != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numCols != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            int indexB = 0;
            for( int j = 0; j < b.numRows; j++ ) {
                int indexA = i;
                int end = indexB + b.numCols;

                double total = 0;

                for( ;indexB<end; ) {
                    total += a.get(indexA) * b.get(indexB++);
                    indexA += a.numCols;
                }

                c.set( cIndex++ , total );
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransAB( org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multTransAB_aux( RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c , double []aux )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numRows != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numCols != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        if( aux == null ) aux = new double[ a.numRows ];

        int indexC = 0;
        for( int i = 0; i < a.numCols; i++ ) {
            for( int k = 0; k < b.numCols; k++ ) {
                aux[k] = a.unsafe_get(k,i);
            }

            for( int j = 0; j < b.numRows; j++ ) {
                double total = 0;

                for( int k = 0; k < b.numCols; k++ ) {
                    total += aux[k] * b.unsafe_get(j,k);
                }
                c.set( indexC++ , total );
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransB( org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multTransB( RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        int cIndex = 0;
        int aIndexStart = 0;

        for( int xA = 0; xA < a.numRows; xA++ ) {
            int end = aIndexStart + b.numCols;
            int indexB = 0;
            for( int xB = 0; xB < b.numRows; xB++ ) {
                int indexA = aIndexStart;

                double total = 0;

                while( indexA<end ) {
                    total += a.get(indexA++) * b.get(indexB++);
                }

                c.set( cIndex++ , total );
            }
            aIndexStart += a.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAdd( org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multAdd_reorder( RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double valA;
        int indexCbase= 0;
        int endOfKLoop = b.numRows*b.numCols;

        for( int i = 0; i < a.numRows; i++ ) {
            int indexA = i*a.numCols;

            // need to assign c.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + b.numCols;

            valA = a.get(indexA++);

            while( indexB < end ) {
                c.plus(indexC++ , valA*b.get(indexB++));
            }

            // now add to it
            while( indexB != endOfKLoop ) { // k loop
                indexC = indexCbase;
                end = indexB + b.numCols;

                valA = a.get(indexA++);

                while( indexB < end ) { // j loop
                    c.plus(indexC++ , valA*b.get(indexB++));
                }
            }
            indexCbase += c.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAdd( org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multAdd_small( RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        int aIndexStart = 0;
        int cIndex = 0;

        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + b.numRows;
                while( indexA < end ) {
                    total += a.get(indexA++) * b.get(indexB);
                    indexB += b.numCols;
                }

                c.plus( cIndex++ , total );
            }
            aIndexStart += a.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAdd( org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multAdd_aux( RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c , double []aux )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        if( aux == null ) aux = new double[ b.numRows ];

        for( int j = 0; j < b.numCols; j++ ) {
            // create a copy of the column in B to avoid cache issues
            for( int k = 0; k < b.numRows; k++ ) {
                aux[k] = b.unsafe_get(k,j);
            }

            int indexA = 0;
            for( int i = 0; i < a.numRows; i++ ) {
                double total = 0;
                for( int k = 0; k < b.numRows; ) {
                    total += a.get(indexA++)*aux[k++];
                }
                c.plus( i*c.numCols+j , total );
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransA( org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multAddTransA_reorder( RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numCols != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double valA;

        for( int i = 0; i < a.numCols; i++ ) {
            int indexC_start = i*c.numCols;

            // first assign R
            valA = a.get(i);
            int indexB = 0;
            int end = indexB+b.numCols;
            int indexC = indexC_start;
            while( indexB<end ) {
                c.plus( indexC++ , valA*b.get(indexB++));
            }
            // now increment it
            for( int k = 1; k < a.numRows; k++ ) {
                valA = a.unsafe_get(k,i);
                end = indexB+b.numCols;
                indexC = indexC_start;
                // this is the loop for j
                while( indexB<end ) {
                    c.plus( indexC++ , valA*b.get(indexB++));
                }
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransA( org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multAddTransA_small( RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numCols != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                int indexA = i;
                int indexB = j;
                int end = indexB + b.numRows*b.numCols;

                double total = 0;

                // loop for k
                for(; indexB < end; indexB += b.numCols ) {
                    total += a.get(indexA) * b.get(indexB);
                    indexA += a.numCols;
                }

                c.plus( cIndex++ , total );
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransAB( org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multAddTransAB( RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numRows != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numCols != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            int indexB = 0;
            for( int j = 0; j < b.numRows; j++ ) {
                int indexA = i;
                int end = indexB + b.numCols;

                double total = 0;

                for( ;indexB<end; ) {
                    total += a.get(indexA) * b.get(indexB++);
                    indexA += a.numCols;
                }

                c.plus( cIndex++ , total );
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransAB( org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multAddTransAB_aux( RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c , double []aux )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numRows != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numCols != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        if( aux == null ) aux = new double[ a.numRows ];

        int indexC = 0;
        for( int i = 0; i < a.numCols; i++ ) {
            for( int k = 0; k < b.numCols; k++ ) {
                aux[k] = a.unsafe_get(k,i);
            }

            for( int j = 0; j < b.numRows; j++ ) {
                double total = 0;

                for( int k = 0; k < b.numCols; k++ ) {
                    total += aux[k] * b.unsafe_get(j,k);
                }
                c.plus( indexC++ , total );
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransB( org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multAddTransB( RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        int cIndex = 0;
        int aIndexStart = 0;

        for( int xA = 0; xA < a.numRows; xA++ ) {
            int end = aIndexStart + b.numCols;
            int indexB = 0;
            for( int xB = 0; xB < b.numRows; xB++ ) {
                int indexA = aIndexStart;

                double total = 0;

                while( indexA<end ) {
                    total += a.get(indexA++) * b.get(indexB++);
                }

                c.plus( cIndex++ , total );
            }
            aIndexStart += a.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#mult(double,  org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void mult_reorder( double alpha , RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double valA;
        int indexCbase= 0;
        int endOfKLoop = b.numRows*b.numCols;

        for( int i = 0; i < a.numRows; i++ ) {
            int indexA = i*a.numCols;

            // need to assign c.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + b.numCols;

            valA = alpha*a.get(indexA++);

            while( indexB < end ) {
                c.set(indexC++ , valA*b.get(indexB++));
            }

            // now add to it
            while( indexB != endOfKLoop ) { // k loop
                indexC = indexCbase;
                end = indexB + b.numCols;

                valA = alpha*a.get(indexA++);

                while( indexB < end ) { // j loop
                    c.plus(indexC++ , valA*b.get(indexB++));
                }
            }
            indexCbase += c.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#mult(double,  org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void mult_small( double alpha , RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        int aIndexStart = 0;
        int cIndex = 0;

        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + b.numRows;
                while( indexA < end ) {
                    total += a.get(indexA++) * b.get(indexB);
                    indexB += b.numCols;
                }

                c.set( cIndex++ , alpha*total );
            }
            aIndexStart += a.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#mult(double,  org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void mult_aux( double alpha , RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c , double []aux )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        if( aux == null ) aux = new double[ b.numRows ];

        for( int j = 0; j < b.numCols; j++ ) {
            // create a copy of the column in B to avoid cache issues
            for( int k = 0; k < b.numRows; k++ ) {
                aux[k] = b.unsafe_get(k,j);
            }

            int indexA = 0;
            for( int i = 0; i < a.numRows; i++ ) {
                double total = 0;
                for( int k = 0; k < b.numRows; ) {
                    total += a.get(indexA++)*aux[k++];
                }
                c.set( i*c.numCols+j , alpha*total );
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransA(double,  org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multTransA_reorder( double alpha , RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numCols != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double valA;

        for( int i = 0; i < a.numCols; i++ ) {
            int indexC_start = i*c.numCols;

            // first assign R
            valA = alpha*a.get(i);
            int indexB = 0;
            int end = indexB+b.numCols;
            int indexC = indexC_start;
            while( indexB<end ) {
                c.set( indexC++ , valA*b.get(indexB++));
            }
            // now increment it
            for( int k = 1; k < a.numRows; k++ ) {
                valA = alpha*a.unsafe_get(k,i);
                end = indexB+b.numCols;
                indexC = indexC_start;
                // this is the loop for j
                while( indexB<end ) {
                    c.plus( indexC++ , valA*b.get(indexB++));
                }
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransA(double,  org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multTransA_small( double alpha , RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numCols != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                int indexA = i;
                int indexB = j;
                int end = indexB + b.numRows*b.numCols;

                double total = 0;

                // loop for k
                for(; indexB < end; indexB += b.numCols ) {
                    total += a.get(indexA) * b.get(indexB);
                    indexA += a.numCols;
                }

                c.set( cIndex++ , alpha*total );
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransAB(double,  org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multTransAB( double alpha , RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numRows != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numCols != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            int indexB = 0;
            for( int j = 0; j < b.numRows; j++ ) {
                int indexA = i;
                int end = indexB + b.numCols;

                double total = 0;

                for( ;indexB<end; ) {
                    total += a.get(indexA) * b.get(indexB++);
                    indexA += a.numCols;
                }

                c.set( cIndex++ , alpha*total );
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransAB(double,  org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multTransAB_aux( double alpha , RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c , double []aux )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numRows != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numCols != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        if( aux == null ) aux = new double[ a.numRows ];

        int indexC = 0;
        for( int i = 0; i < a.numCols; i++ ) {
            for( int k = 0; k < b.numCols; k++ ) {
                aux[k] = a.unsafe_get(k,i);
            }

            for( int j = 0; j < b.numRows; j++ ) {
                double total = 0;

                for( int k = 0; k < b.numCols; k++ ) {
                    total += aux[k] * b.unsafe_get(j,k);
                }
                c.set( indexC++ , alpha*total );
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransB(double,  org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multTransB( double alpha , RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        int cIndex = 0;
        int aIndexStart = 0;

        for( int xA = 0; xA < a.numRows; xA++ ) {
            int end = aIndexStart + b.numCols;
            int indexB = 0;
            for( int xB = 0; xB < b.numRows; xB++ ) {
                int indexA = aIndexStart;

                double total = 0;

                while( indexA<end ) {
                    total += a.get(indexA++) * b.get(indexB++);
                }

                c.set( cIndex++ , alpha*total );
            }
            aIndexStart += a.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAdd(double,  org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multAdd_reorder( double alpha , RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double valA;
        int indexCbase= 0;
        int endOfKLoop = b.numRows*b.numCols;

        for( int i = 0; i < a.numRows; i++ ) {
            int indexA = i*a.numCols;

            // need to assign c.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + b.numCols;

            valA = alpha*a.get(indexA++);

            while( indexB < end ) {
                c.plus(indexC++ , valA*b.get(indexB++));
            }

            // now add to it
            while( indexB != endOfKLoop ) { // k loop
                indexC = indexCbase;
                end = indexB + b.numCols;

                valA = alpha*a.get(indexA++);

                while( indexB < end ) { // j loop
                    c.plus(indexC++ , valA*b.get(indexB++));
                }
            }
            indexCbase += c.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAdd(double,  org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multAdd_small( double alpha , RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        int aIndexStart = 0;
        int cIndex = 0;

        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + b.numRows;
                while( indexA < end ) {
                    total += a.get(indexA++) * b.get(indexB);
                    indexB += b.numCols;
                }

                c.plus( cIndex++ , alpha*total );
            }
            aIndexStart += a.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAdd(double,  org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multAdd_aux( double alpha , RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c , double []aux )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        if( aux == null ) aux = new double[ b.numRows ];

        for( int j = 0; j < b.numCols; j++ ) {
            // create a copy of the column in B to avoid cache issues
            for( int k = 0; k < b.numRows; k++ ) {
                aux[k] = b.unsafe_get(k,j);
            }

            int indexA = 0;
            for( int i = 0; i < a.numRows; i++ ) {
                double total = 0;
                for( int k = 0; k < b.numRows; ) {
                    total += a.get(indexA++)*aux[k++];
                }
                c.plus( i*c.numCols+j , alpha*total );
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransA(double,  org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multAddTransA_reorder( double alpha , RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numCols != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double valA;

        for( int i = 0; i < a.numCols; i++ ) {
            int indexC_start = i*c.numCols;

            // first assign R
            valA = alpha*a.get(i);
            int indexB = 0;
            int end = indexB+b.numCols;
            int indexC = indexC_start;
            while( indexB<end ) {
                c.plus( indexC++ , valA*b.get(indexB++));
            }
            // now increment it
            for( int k = 1; k < a.numRows; k++ ) {
                valA = alpha*a.unsafe_get(k,i);
                end = indexB+b.numCols;
                indexC = indexC_start;
                // this is the loop for j
                while( indexB<end ) {
                    c.plus( indexC++ , valA*b.get(indexB++));
                }
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransA(double,  org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multAddTransA_small( double alpha , RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numCols != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                int indexA = i;
                int indexB = j;
                int end = indexB + b.numRows*b.numCols;

                double total = 0;

                // loop for k
                for(; indexB < end; indexB += b.numCols ) {
                    total += a.get(indexA) * b.get(indexB);
                    indexA += a.numCols;
                }

                c.plus( cIndex++ , alpha*total );
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransAB(double,  org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multAddTransAB( double alpha , RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numRows != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numCols != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            int indexB = 0;
            for( int j = 0; j < b.numRows; j++ ) {
                int indexA = i;
                int end = indexB + b.numCols;

                double total = 0;

                for( ;indexB<end; ) {
                    total += a.get(indexA) * b.get(indexB++);
                    indexA += a.numCols;
                }

                c.plus( cIndex++ , alpha*total );
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransAB(double,  org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multAddTransAB_aux( double alpha , RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c , double []aux )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numRows != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numCols != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        if( aux == null ) aux = new double[ a.numRows ];

        int indexC = 0;
        for( int i = 0; i < a.numCols; i++ ) {
            for( int k = 0; k < b.numCols; k++ ) {
                aux[k] = a.unsafe_get(k,i);
            }

            for( int j = 0; j < b.numRows; j++ ) {
                double total = 0;

                for( int k = 0; k < b.numCols; k++ ) {
                    total += aux[k] * b.unsafe_get(j,k);
                }
                c.plus( indexC++ , alpha*total );
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransB(double,  org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F, org.ejml.data.RowD1Matrix64F)
     */
    public static void multAddTransB( double alpha , RowD1Matrix64F a , RowD1Matrix64F b , RowD1Matrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        int cIndex = 0;
        int aIndexStart = 0;

        for( int xA = 0; xA < a.numRows; xA++ ) {
            int end = aIndexStart + b.numCols;
            int indexB = 0;
            for( int xB = 0; xB < b.numRows; xB++ ) {
                int indexA = aIndexStart;

                double total = 0;

                while( indexA<end ) {
                    total += a.get(indexA++) * b.get(indexB++);
                }

                c.plus( cIndex++ , alpha*total );
            }
            aIndexStart += a.numCols;
        }
    }

}
