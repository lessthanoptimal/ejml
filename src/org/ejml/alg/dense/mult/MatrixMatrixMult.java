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

package org.ejml.alg.dense.mult;

import org.ejml.data.DenseMatrix64F;

/**
 * <p>
 * This class contains various types of matrix matrix multiplcation operations for {@link DenseMatrix64F}.
 * </p>
 * <p>
 * Two algorithms that are equivalent can often have very different runtime performance.
 * This is because of how modern computers uses fast memory caches to speed up reading/writing to data.
 * Depending on the order in which variables are processed different algorithms can run much faster than others,
 * even if the number of operations is the same.
 * </p>
 *
 * <p>
 * Algorithms that are labled as 'reorder' are designed to avoid caching jumping issues, some times at the cost
 * of increasing the number of operations.  This is important for large matrices.  The straight forward 
 * implementation seems to be faster for small matrices.
 * </p>
 * 
 * <p>
 * Algorithms that are labled as 'aux' use an auxilary array of length n.  This array is used to create
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
 * This class was auto generated using {@link org.ejml.alg.dense.mult.CodeGeneratorMatrixMatrixMult}
 * If this code needs to be modified, please modify {@link org.ejml.alg.dense.mult.CodeGeneratorMatrixMatrixMult} instead
 * and regenerate the code by running that.
 * </p>
 * 
 * @author Peter Abeles
 */
public class MatrixMatrixMult {
    /**
     * @see org.ejml.ops.CommonOps#mult( org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void mult_reorder( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        double valA;
        int indexCbase= 0;

        for( int i = 0; i < a.numRows; i++ ) {
            int indexA = i*a.numCols;

            // need to assign dataC to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + b.numCols;

            valA = dataA[indexA++];

            for( ; indexB < end; indexB++ ) {
                dataC[indexC++] = valA*dataB[indexB];
            }

            // now add to it
            for( int k = 1; k < a.numCols; k++ ) {
                indexB = k*b.numCols;
                indexC = indexCbase;
                end = indexB + b.numCols;

                valA = dataA[indexA++];

                for( ; indexB < end; ) {
                    dataC[indexC++] += valA*dataB[indexB++];
                }
            }
            indexCbase += c.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#mult( org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void mult_small( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        int aIndexStart = 0;
        int cIndex = 0;

        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + b.numRows;
                for( ;indexA < end; ) {
                    total += dataA[indexA++] * dataB[indexB];
                    indexB += b.numCols;
                }

                dataC[cIndex++] = total;
            }
            aIndexStart += a.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#mult( org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void mult_aux( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c , double []aux )
    {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        if( aux == null ) aux = new double[ b.numRows ];

        for( int j = 0; j < b.numCols; j++ ) {
            // create a copy of the column in B to avoid cache issues
            for( int k = 0; k < b.numRows; k++ ) {
                aux[k] = dataB[k*b.numCols+j];
            }

            int indexA = 0;
            for( int i = 0; i < a.numRows; i++ ) {
                double total = 0;
                for( int k = 0; k < b.numRows; ) {
                    total += dataA[indexA++]*aux[k++];
                }
                dataC[i*c.numCols+j] = total;
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransA( org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multTransA_reorder( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numCols != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        double valA;

        for( int i = 0; i < a.numCols; i++ ) {
            int indexC_start = i*c.numCols;

            // first assign R
            valA = dataA[i];
            int indexB = 0;
            int end = indexB+b.numCols;
            int indexC = indexC_start;
            for( ; indexB<end; ) {
                dataC[indexC++] = valA*dataB[indexB++];
            }
            // now increment it
            for( int k = 1; k < a.numRows; k++ ) {
                valA = dataA[k*a.numCols+i];
                end = indexB+b.numCols;
                indexC = indexC_start;
                // this is the loop for j
                for( ; indexB<end; ) {
                    dataC[indexC++] += valA*dataB[indexB++];
                }
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransA( org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multTransA_small( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numCols != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                int indexA = i;
                int indexB = j;
                int end = indexB + b.numRows*b.numCols;

                double total = 0;

                // loop for k
                for(; indexB < end; indexB += b.numCols ) {
                    total += dataA[indexA] * dataB[indexB];
                    indexA += a.numCols;
                }

                dataC[cIndex++] = total;
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransAB( org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multTransAB( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numRows != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numCols != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            for( int j = 0; j < b.numRows; j++ ) {
                int indexA = i;
                int indexB = j*b.numCols;
                int end = indexB + b.numCols;

                double total = 0;

                for( ;indexB<end; ) {
                    total += dataA[indexA] * dataB[indexB++];
                    indexA += a.numCols;
                }

                dataC[cIndex++] = total;
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransAB( org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multTransAB_aux( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c , double []aux )
    {
        if( a.numRows != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numCols != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        if( aux == null ) aux = new double[ a.numRows ];

        int indexC = 0;
        for( int i = 0; i < a.numCols; i++ ) {
            for( int k = 0; k < b.numCols; k++ ) {
                aux[k] = dataA[k*a.numCols+i];
            }

            for( int j = 0; j < b.numRows; j++ ) {
                double total = 0;

                for( int k = 0; k < b.numCols; k++ ) {
                    total += aux[k] * dataB[j*b.numCols+k];
                }
                dataC[indexC++] = total;
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransB( org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multTransB( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numRows != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        int cIndex = 0;
        int aIndexStart = 0;

        for( int xA = 0; xA < a.numRows; xA++ ) {
            int end = aIndexStart + b.numCols;
            for( int xB = 0; xB < b.numRows; xB++ ) {
                int indexA = aIndexStart;
                int indexB = xB*b.numCols;

                double total = 0;

                for( ;indexA<end; ) {
                    total += dataA[indexA++] * dataB[indexB++];
                }

                dataC[cIndex++] = total;
            }
            aIndexStart += a.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAdd( org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multAdd_reorder( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        double valA;
        int indexCbase= 0;

        for( int i = 0; i < a.numRows; i++ ) {
            int indexA = i*a.numCols;

            // need to assign dataC to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + b.numCols;

            valA = dataA[indexA++];

            for( ; indexB < end; indexB++ ) {
                dataC[indexC++] += valA*dataB[indexB];
            }

            // now add to it
            for( int k = 1; k < a.numCols; k++ ) {
                indexB = k*b.numCols;
                indexC = indexCbase;
                end = indexB + b.numCols;

                valA = dataA[indexA++];

                for( ; indexB < end; ) {
                    dataC[indexC++] += valA*dataB[indexB++];
                }
            }
            indexCbase += c.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAdd( org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multAdd_small( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        int aIndexStart = 0;
        int cIndex = 0;

        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + b.numRows;
                for( ;indexA < end; ) {
                    total += dataA[indexA++] * dataB[indexB];
                    indexB += b.numCols;
                }

                dataC[cIndex++] += total;
            }
            aIndexStart += a.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAdd( org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multAdd_aux( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c , double []aux )
    {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        if( aux == null ) aux = new double[ b.numRows ];

        for( int j = 0; j < b.numCols; j++ ) {
            // create a copy of the column in B to avoid cache issues
            for( int k = 0; k < b.numRows; k++ ) {
                aux[k] = dataB[k*b.numCols+j];
            }

            int indexA = 0;
            for( int i = 0; i < a.numRows; i++ ) {
                double total = 0;
                for( int k = 0; k < b.numRows; ) {
                    total += dataA[indexA++]*aux[k++];
                }
                dataC[i*c.numCols+j] += total;
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransA( org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multAddTransA_reorder( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numCols != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        double valA;

        for( int i = 0; i < a.numCols; i++ ) {
            int indexC_start = i*c.numCols;

            // first assign R
            valA = dataA[i];
            int indexB = 0;
            int end = indexB+b.numCols;
            int indexC = indexC_start;
            for( ; indexB<end; ) {
                dataC[indexC++] += valA*dataB[indexB++];
            }
            // now increment it
            for( int k = 1; k < a.numRows; k++ ) {
                valA = dataA[k*a.numCols+i];
                end = indexB+b.numCols;
                indexC = indexC_start;
                // this is the loop for j
                for( ; indexB<end; ) {
                    dataC[indexC++] += valA*dataB[indexB++];
                }
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransA( org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multAddTransA_small( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numCols != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                int indexA = i;
                int indexB = j;
                int end = indexB + b.numRows*b.numCols;

                double total = 0;

                // loop for k
                for(; indexB < end; indexB += b.numCols ) {
                    total += dataA[indexA] * dataB[indexB];
                    indexA += a.numCols;
                }

                dataC[cIndex++] += total;
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransAB( org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multAddTransAB( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numRows != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numCols != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            for( int j = 0; j < b.numRows; j++ ) {
                int indexA = i;
                int indexB = j*b.numCols;
                int end = indexB + b.numCols;

                double total = 0;

                for( ;indexB<end; ) {
                    total += dataA[indexA] * dataB[indexB++];
                    indexA += a.numCols;
                }

                dataC[cIndex++] += total;
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransAB( org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multAddTransAB_aux( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c , double []aux )
    {
        if( a.numRows != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numCols != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        if( aux == null ) aux = new double[ a.numRows ];

        int indexC = 0;
        for( int i = 0; i < a.numCols; i++ ) {
            for( int k = 0; k < b.numCols; k++ ) {
                aux[k] = dataA[k*a.numCols+i];
            }

            for( int j = 0; j < b.numRows; j++ ) {
                double total = 0;

                for( int k = 0; k < b.numCols; k++ ) {
                    total += aux[k] * dataB[j*b.numCols+k];
                }
                dataC[indexC++] += total;
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransB( org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multAddTransB( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numRows != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        int cIndex = 0;
        int aIndexStart = 0;

        for( int xA = 0; xA < a.numRows; xA++ ) {
            int end = aIndexStart + b.numCols;
            for( int xB = 0; xB < b.numRows; xB++ ) {
                int indexA = aIndexStart;
                int indexB = xB*b.numCols;

                double total = 0;

                for( ;indexA<end; ) {
                    total += dataA[indexA++] * dataB[indexB++];
                }

                dataC[cIndex++] += total;
            }
            aIndexStart += a.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#mult(double,  org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void mult_reorder( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        double valA;
        int indexCbase= 0;

        for( int i = 0; i < a.numRows; i++ ) {
            int indexA = i*a.numCols;

            // need to assign dataC to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + b.numCols;

            valA = alpha*dataA[indexA++];

            for( ; indexB < end; indexB++ ) {
                dataC[indexC++] = valA*dataB[indexB];
            }

            // now add to it
            for( int k = 1; k < a.numCols; k++ ) {
                indexB = k*b.numCols;
                indexC = indexCbase;
                end = indexB + b.numCols;

                valA = alpha*dataA[indexA++];

                for( ; indexB < end; ) {
                    dataC[indexC++] += valA*dataB[indexB++];
                }
            }
            indexCbase += c.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#mult(double,  org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void mult_small( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        int aIndexStart = 0;
        int cIndex = 0;

        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + b.numRows;
                for( ;indexA < end; ) {
                    total += dataA[indexA++] * dataB[indexB];
                    indexB += b.numCols;
                }

                dataC[cIndex++] = alpha*total;
            }
            aIndexStart += a.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#mult(double,  org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void mult_aux( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c , double []aux )
    {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        if( aux == null ) aux = new double[ b.numRows ];

        for( int j = 0; j < b.numCols; j++ ) {
            // create a copy of the column in B to avoid cache issues
            for( int k = 0; k < b.numRows; k++ ) {
                aux[k] = dataB[k*b.numCols+j];
            }

            int indexA = 0;
            for( int i = 0; i < a.numRows; i++ ) {
                double total = 0;
                for( int k = 0; k < b.numRows; ) {
                    total += dataA[indexA++]*aux[k++];
                }
                dataC[i*c.numCols+j] = alpha*total;
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransA(double,  org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multTransA_reorder( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numCols != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        double valA;

        for( int i = 0; i < a.numCols; i++ ) {
            int indexC_start = i*c.numCols;

            // first assign R
            valA = alpha*dataA[i];
            int indexB = 0;
            int end = indexB+b.numCols;
            int indexC = indexC_start;
            for( ; indexB<end; ) {
                dataC[indexC++] = valA*dataB[indexB++];
            }
            // now increment it
            for( int k = 1; k < a.numRows; k++ ) {
                valA = alpha*dataA[k*a.numCols+i];
                end = indexB+b.numCols;
                indexC = indexC_start;
                // this is the loop for j
                for( ; indexB<end; ) {
                    dataC[indexC++] += valA*dataB[indexB++];
                }
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransA(double,  org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multTransA_small( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numCols != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                int indexA = i;
                int indexB = j;
                int end = indexB + b.numRows*b.numCols;

                double total = 0;

                // loop for k
                for(; indexB < end; indexB += b.numCols ) {
                    total += dataA[indexA] * dataB[indexB];
                    indexA += a.numCols;
                }

                dataC[cIndex++] = alpha*total;
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransAB(double,  org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multTransAB( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numRows != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numCols != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            for( int j = 0; j < b.numRows; j++ ) {
                int indexA = i;
                int indexB = j*b.numCols;
                int end = indexB + b.numCols;

                double total = 0;

                for( ;indexB<end; ) {
                    total += dataA[indexA] * dataB[indexB++];
                    indexA += a.numCols;
                }

                dataC[cIndex++] = alpha*total;
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransAB(double,  org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multTransAB_aux( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c , double []aux )
    {
        if( a.numRows != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numCols != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        if( aux == null ) aux = new double[ a.numRows ];

        int indexC = 0;
        for( int i = 0; i < a.numCols; i++ ) {
            for( int k = 0; k < b.numCols; k++ ) {
                aux[k] = dataA[k*a.numCols+i];
            }

            for( int j = 0; j < b.numRows; j++ ) {
                double total = 0;

                for( int k = 0; k < b.numCols; k++ ) {
                    total += aux[k] * dataB[j*b.numCols+k];
                }
                dataC[indexC++] = alpha*total;
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multTransB(double,  org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multTransB( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numRows != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        int cIndex = 0;
        int aIndexStart = 0;

        for( int xA = 0; xA < a.numRows; xA++ ) {
            int end = aIndexStart + b.numCols;
            for( int xB = 0; xB < b.numRows; xB++ ) {
                int indexA = aIndexStart;
                int indexB = xB*b.numCols;

                double total = 0;

                for( ;indexA<end; ) {
                    total += dataA[indexA++] * dataB[indexB++];
                }

                dataC[cIndex++] = alpha*total;
            }
            aIndexStart += a.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAdd(double,  org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multAdd_reorder( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        double valA;
        int indexCbase= 0;

        for( int i = 0; i < a.numRows; i++ ) {
            int indexA = i*a.numCols;

            // need to assign dataC to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + b.numCols;

            valA = alpha*dataA[indexA++];

            for( ; indexB < end; indexB++ ) {
                dataC[indexC++] += valA*dataB[indexB];
            }

            // now add to it
            for( int k = 1; k < a.numCols; k++ ) {
                indexB = k*b.numCols;
                indexC = indexCbase;
                end = indexB + b.numCols;

                valA = alpha*dataA[indexA++];

                for( ; indexB < end; ) {
                    dataC[indexC++] += valA*dataB[indexB++];
                }
            }
            indexCbase += c.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAdd(double,  org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multAdd_small( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        int aIndexStart = 0;
        int cIndex = 0;

        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                double total = 0;

                int indexA = aIndexStart;
                int indexB = j;
                int end = indexA + b.numRows;
                for( ;indexA < end; ) {
                    total += dataA[indexA++] * dataB[indexB];
                    indexB += b.numCols;
                }

                dataC[cIndex++] += alpha*total;
            }
            aIndexStart += a.numCols;
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAdd(double,  org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multAdd_aux( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c , double []aux )
    {
        if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        if( aux == null ) aux = new double[ b.numRows ];

        for( int j = 0; j < b.numCols; j++ ) {
            // create a copy of the column in B to avoid cache issues
            for( int k = 0; k < b.numRows; k++ ) {
                aux[k] = dataB[k*b.numCols+j];
            }

            int indexA = 0;
            for( int i = 0; i < a.numRows; i++ ) {
                double total = 0;
                for( int k = 0; k < b.numRows; ) {
                    total += dataA[indexA++]*aux[k++];
                }
                dataC[i*c.numCols+j] += alpha*total;
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransA(double,  org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multAddTransA_reorder( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numCols != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        double valA;

        for( int i = 0; i < a.numCols; i++ ) {
            int indexC_start = i*c.numCols;

            // first assign R
            valA = alpha*dataA[i];
            int indexB = 0;
            int end = indexB+b.numCols;
            int indexC = indexC_start;
            for( ; indexB<end; ) {
                dataC[indexC++] += valA*dataB[indexB++];
            }
            // now increment it
            for( int k = 1; k < a.numRows; k++ ) {
                valA = alpha*dataA[k*a.numCols+i];
                end = indexB+b.numCols;
                indexC = indexC_start;
                // this is the loop for j
                for( ; indexB<end; ) {
                    dataC[indexC++] += valA*dataB[indexB++];
                }
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransA(double,  org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multAddTransA_small( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numCols != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                int indexA = i;
                int indexB = j;
                int end = indexB + b.numRows*b.numCols;

                double total = 0;

                // loop for k
                for(; indexB < end; indexB += b.numCols ) {
                    total += dataA[indexA] * dataB[indexB];
                    indexA += a.numCols;
                }

                dataC[cIndex++] += alpha*total;
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransAB(double,  org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multAddTransAB( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numRows != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numCols != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        int cIndex = 0;

        for( int i = 0; i < a.numCols; i++ ) {
            for( int j = 0; j < b.numRows; j++ ) {
                int indexA = i;
                int indexB = j*b.numCols;
                int end = indexB + b.numCols;

                double total = 0;

                for( ;indexB<end; ) {
                    total += dataA[indexA] * dataB[indexB++];
                    indexA += a.numCols;
                }

                dataC[cIndex++] += alpha*total;
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransAB(double,  org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multAddTransAB_aux( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c , double []aux )
    {
        if( a.numRows != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numCols != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        if( aux == null ) aux = new double[ a.numRows ];

        int indexC = 0;
        for( int i = 0; i < a.numCols; i++ ) {
            for( int k = 0; k < b.numCols; k++ ) {
                aux[k] = dataA[k*a.numCols+i];
            }

            for( int j = 0; j < b.numRows; j++ ) {
                double total = 0;

                for( int k = 0; k < b.numCols; k++ ) {
                    total += aux[k] * dataB[j*b.numCols+k];
                }
                dataC[indexC++] += alpha*total;
            }
        }
    }

    /**
     * @see org.ejml.ops.CommonOps#multAddTransB(double,  org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F)
     */
    public static void multAddTransB( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols != b.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatable dimensions");
        } else if( a.numRows != c.numRows || b.numRows != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        int cIndex = 0;
        int aIndexStart = 0;

        for( int xA = 0; xA < a.numRows; xA++ ) {
            int end = aIndexStart + b.numCols;
            for( int xB = 0; xB < b.numRows; xB++ ) {
                int indexA = aIndexStart;
                int indexB = xB*b.numCols;

                double total = 0;

                for( ;indexA<end; ) {
                    total += dataA[indexA++] * dataB[indexB++];
                }

                dataC[cIndex++] += alpha*total;
            }
            aIndexStart += a.numCols;
        }
    }

}
