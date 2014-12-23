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

package org.ejml.alg.dense.mult;

import org.ejml.data.D1Matrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.RowD1Matrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixDimensionException;


/**
 * <p>
 * This class contains various types of matrix vector multiplcation operations for {@link DenseMatrix64F}.
 * </p>
 * <p>
 * If a matrix has only one column or row then it is a vector.  There are faster algorithms
 * that can be used to multiply matrices by vectors.  Strangely, even though the operations
 * count smaller, the difference between this and a regular matrix multiply is insignificant
 * for large matrices.  The smaller matrices there is about a 40% speed improvement.  In
 * practice the speed improvement for smaller matrices is not noticeable unless 10s of millions
 * of matrix multiplications are being performed.
 * </p>
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"ForLoopReplaceableByForEach"})
public class MatrixVectorMult {

    /**
     * <p>
     * Performs a matrix vector multiply.<br>
     * <br>
     * c = A * b <br>
     * and<br>
     * c = A * b<sup>T</sup> <br>
     * <br>
     * c<sub>i</sub> = Sum{ j=1:n, a<sub>ij</sub> * b<sub>j</sub>}<br>
     * <br>
     * where A is a matrix, b is a column or transposed row vector, and c is a column vector.
     * </p>
     *
     * @param A A matrix that is m by n. Not modified.
     * @param B A vector that has length n. Not modified.
     * @param C A column vector that has length m. Modified.
     */
    public static void mult( RowD1Matrix64F A, D1Matrix64F B, D1Matrix64F C)
    {
        if( C.numCols != 1 ) {
            throw new MatrixDimensionException("C is not a column vector");
        } else if( C.numRows != A.numRows ) {
            throw new MatrixDimensionException("C is not the expected length");
        }
        
        if( B.numRows == 1 ) {
            if( A.numCols != B.numCols ) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else if( B.numCols == 1 ) {
            if( A.numCols != B.numRows ) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else {
            throw new MatrixDimensionException("B is not a vector");
        }

        if( A.numCols == 0 ) {
            CommonOps.fill(C,0);
            return;
        }

        int indexA = 0;
        int cIndex = 0;
        double b0 = B.get(0);
        for( int i = 0; i < A.numRows; i++ ) {
            double total = A.get(indexA++) * b0;

            for( int j = 1; j < A.numCols; j++ ) {
                total += A.get(indexA++) * B.get(j);
            }

            C.set(cIndex++, total);
        }
    }

    /**
     * <p>
     * Performs a matrix vector multiply.<br>
     * <br>
     * C = C + A * B <br>
     * or<br>
     * C = C + A * B<sup>T</sup> <br>
     * <br>
     * c<sub>i</sub> = Sum{ j=1:n, c<sub>i</sub> + a<sub>ij</sub> * b<sub>j</sub>}<br>
     * <br>
     * where A is a matrix, B is a column or transposed row vector, and C is a column vector.
     * </p>
     *
     * @param A A matrix that is m by n. Not modified.
     * @param B A vector that has length n. Not modified.
     * @param C A column vector that has length m. Modified.
     */
    public static void multAdd( RowD1Matrix64F A , D1Matrix64F B , D1Matrix64F C )
    {

        if( C.numCols != 1 ) {
            throw new MatrixDimensionException("C is not a column vector");
        } else if( C.numRows != A.numRows ) {
            throw new MatrixDimensionException("C is not the expected length");
        }
        if( B.numRows == 1 ) {
            if( A.numCols != B.numCols ) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else if( B.numCols == 1 ) {
            if( A.numCols != B.numRows ) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else {
            throw new MatrixDimensionException("B is not a vector");
        }

        if( A.numCols == 0 ) {
            return;
        }

        int indexA = 0;
        int cIndex = 0;
        for( int i = 0; i < A.numRows; i++ ) {
            double total = A.get(indexA++) * B.get(0);

            for( int j = 1; j < A.numCols; j++ ) {
                total += A.get(indexA++) * B.get(j);
            }

            C.plus(cIndex++ , total );
        }
    }

    /**
     * <p>
     * Performs a matrix vector multiply.<br>
     * <br>
     * C = A<sup>T</sup> * B <br>
     * where B is a column vector.<br>
     * or<br>
     * C = A<sup>T</sup> * B<sup>T</sup> <br>
     * where B is a row vector. <br>
     * <br>
     * c<sub>i</sub> = Sum{ j=1:n, a<sub>ji</sub> * b<sub>j</sub>}<br>
     * <br>
     * where A is a matrix, B is a column or transposed row vector, and C is a column vector.
     * </p>
     * <p>
     * This implementation is optimal for small matrices.  There is a huge performance hit when
     * used on large matrices due to CPU cache issues.
     * </p>
     *
     * @param A A matrix that is m by n. Not modified.
     * @param B A that has length m and is a column. Not modified.
     * @param C A column vector that has length n. Modified.
     */
    public static void multTransA_small( RowD1Matrix64F A , D1Matrix64F B , D1Matrix64F C )
    {
        if( C.numCols != 1 ) {
            throw new MatrixDimensionException("C is not a column vector");
        } else if( C.numRows != A.numCols ) {
            throw new MatrixDimensionException("C is not the expected length");
        }
        if( B.numRows == 1 ) {
            if( A.numRows != B.numCols ) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else if( B.numCols == 1 ) {
            if( A.numRows != B.numRows ) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else {
            throw new MatrixDimensionException("B is not a vector");
        }

        int cIndex = 0;
        for( int i = 0; i < A.numCols; i++ ) {
            double total = 0.0;

            int indexA = i;
            for( int j = 0; j < A.numRows; j++ ) {
                total += A.get(indexA) * B.get(j);
                indexA += A.numCols;
            }

            C.set(cIndex++ , total);
        }
    }

    /**
     * An alternative implementation of {@link #multTransA_small} that performs well on large
     * matrices.  There is a relative performance hit when used on small matrices.
     *
     * @param A A matrix that is m by n. Not modified.
     * @param B A Vector that has length m. Not modified.
     * @param C A column vector that has length n. Modified.
     */
    public static void multTransA_reorder( RowD1Matrix64F A , D1Matrix64F B , D1Matrix64F C )
    {
        if( C.numCols != 1 ) {
            throw new MatrixDimensionException("C is not a column vector");
        } else if( C.numRows != A.numCols ) {
            throw new MatrixDimensionException("C is not the expected length");
        }
        if( B.numRows == 1 ) {
            if( A.numRows != B.numCols ) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else if( B.numCols == 1 ) {
            if( A.numRows != B.numRows ) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else {
            throw new MatrixDimensionException("B is not a vector");
        }

        if( A.numRows == 0 ) {
            CommonOps.fill(C,0);
            return;
        }

        double B_val = B.get(0);
        for( int i = 0; i < A.numCols; i++ ) {
            C.set( i , A.get(i) * B_val );
        }

        int indexA = A.numCols;
        for( int i = 1; i < A.numRows; i++ ) {
            B_val = B.get(i);
            for( int j = 0; j < A.numCols; j++ ) {
                C.plus(  j , A.get(indexA++) * B_val );
            }
        }
    }

    /**
     * <p>
     * Performs a matrix vector multiply.<br>
     * <br>
     * C = C + A<sup>T</sup> * B <br>
     * or<br>
     * C = C<sup>T</sup> + A<sup>T</sup> * B<sup>T</sup> <br>
     * <br>
     * c<sub>i</sub> = Sum{ j=1:n, c<sub>i</sub> + a<sub>ji</sub> * b<sub>j</sub>}<br>
     * <br>
     * where A is a matrix, B is a column or transposed row vector, and C is a column vector.
     * </p>
     * <p>
     * This implementation is optimal for small matrices.  There is a huge performance hit when
     * used on large matrices due to CPU cache issues.
     * </p>
     *
     * @param A A matrix that is m by n. Not modified.
     * @param B A vector that has length m. Not modified.
     * @param C A column vector that has length n. Modified.
     */
    public static void multAddTransA_small( RowD1Matrix64F A , D1Matrix64F B , D1Matrix64F C )
    {
        if( C.numCols != 1 ) {
            throw new MatrixDimensionException("C is not a column vector");
        } else if( C.numRows != A.numCols ) {
            throw new MatrixDimensionException("C is not the expected length");
        }
        if( B.numRows == 1 ) {
            if( A.numRows != B.numCols ) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else if( B.numCols == 1 ) {
            if( A.numRows != B.numRows ) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else {
            throw new MatrixDimensionException("B is not a vector");
        }

        int cIndex = 0;
        for( int i = 0; i < A.numCols; i++ ) {
            double total = 0.0;

            int indexA = i;
            for( int j = 0; j < A.numRows; j++ ) {
                total += A.get(indexA) * B.get(j);
                indexA += A.numCols;
            }

            C.plus( cIndex++ , total );
        }
    }

    /**
     * An alternative implementation of {@link #multAddTransA_small} that performs well on large
     * matrices.  There is a relative performance hit when used on small matrices.
     *
     * @param A A matrix that is m by n. Not modified.
     * @param B A vector that has length m. Not modified.
     * @param C A column vector that has length n. Modified.
     */
    public static void multAddTransA_reorder( RowD1Matrix64F A , D1Matrix64F B , D1Matrix64F C )
    {
        if( C.numCols != 1 ) {
            throw new MatrixDimensionException("C is not a column vector");
        } else if( C.numRows != A.numCols ) {
            throw new MatrixDimensionException("C is not the expected length");
        }
        if( B.numRows == 1 ) {
            if( A.numRows != B.numCols ) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else if( B.numCols == 1 ) {
            if( A.numRows != B.numRows ) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else {
            throw new MatrixDimensionException("B is not a vector");
        }

        if( A.numRows == 0 ) {
            return;
        }

        int indexA = 0;
        for( int j = 0; j < A.numRows; j++ ) {
            double B_val = B.get(j);
            for( int i = 0; i < A.numCols; i++ ) {
                C.plus( i , A.get(indexA++) * B_val );
            }
        }
    }
}
