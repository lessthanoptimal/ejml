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

import org.ejml.data.RowD1Matrix64F;

/**
 * <p>
 * Specialized operations for performing inner and outer products for matrices.
 * </p>
 * 
 * <p>
 * inner product: B=A<sup>T</sup>*A<br>
 * outer product: B=A*A<sup>T</sup>
 * </p>
 * 
 * @author Peter Abeles
 */
public class MatrixMultProduct {

    public static void outer(RowD1Matrix64F a, RowD1Matrix64F c) {
        for( int i = 0; i < a.numRows; i++ ) {
            int indexC1 = i*c.numCols+i;
            int indexC2 = indexC1;
            for( int j = i; j < a.numRows; j++ , indexC2 += c.numCols) {
                int indexA = i*a.numCols;
                int indexB = j*a.numCols;
                double sum = 0;
                int end = indexA + a.numCols;
                for( ; indexA < end; indexA++,indexB++ ) {
                    sum += a.data[indexA]*a.data[indexB];
                }
                c.data[indexC2] = c.data[indexC1++] = sum;
            }
        }
//        for( int i = 0; i < a.numRows; i++ ) {
//            for( int j = 0; j < a.numRows; j++ ) {
//                double sum = 0;
//                for( int k = 0; k < a.numCols; k++ ) {
//                    sum += a.get(i,k)*a.get(j,k);
//                }
//                c.set(i,j,sum);
//            }
//        }
    }
    
    public static void inner_small(RowD1Matrix64F a, RowD1Matrix64F c) {

        for( int i = 0; i < a.numCols; i++ ) {
            for( int j = i; j < a.numCols; j++ ) {
                int indexC1 = i*c.numCols+j;
                int indexC2 = j*c.numCols+i;
                int indexA = i;
                int indexB = j;
                double sum = 0;
                int end = indexA + a.numRows*a.numCols;
                for( ; indexA < end; indexA += a.numCols, indexB += a.numCols ) {
                    sum += a.data[indexA]*a.data[indexB];
                }
                c.data[indexC1] = c.data[indexC2] = sum;
            }
        }
//        for( int i = 0; i < a.numCols; i++ ) {
//            for( int j = i; j < a.numCols; j++ ) {
//                double sum = 0;
//                for( int k = 0; k < a.numRows; k++ ) {
//                    sum += a.get(k,i)*a.get(k,j);
//                }
//                c.set(i,j,sum);
//                c.set(j,i,sum);
//            }
//        }
    }

    public static void inner_reorder(RowD1Matrix64F a, RowD1Matrix64F c) {

        for( int i = 0; i < a.numCols; i++ ) {
            int indexC = i*c.numCols+i;
            double valAi = a.data[i];
            for( int j = i; j < a.numCols; j++ ) {
                c.data[indexC++] =  valAi*a.data[j];
            }

            for( int k = 1; k < a.numRows; k++ ) {
                indexC = i*c.numCols+i;
                int indexB = k*a.numCols+i;
                valAi = a.data[indexB];
                for( int j = i; j < a.numCols; j++ ) {
                    c.data[indexC++] +=  valAi*a.data[indexB++];
                }
            }

            indexC = i*c.numCols+i;
            int indexC2 = indexC;
            for( int j = i; j < a.numCols; j++ , indexC2 += c.numCols) {
                c.data[indexC2] = c.data[indexC++];
            }
        }

//        for( int i = 0; i < a.numCols; i++ ) {
//            for( int j = i; j < a.numCols; j++ ) {
//                c.set(i,j,a.get(0,i)*a.get(0,j));
//            }
//
//            for( int k = 1; k < a.numRows; k++ ) {
//                for( int j = i; j < a.numCols; j++ ) {
//                    c.set(i,j, c.get(i,j)+ a.get(k,i)*a.get(k,j));
//                }
//            }
//            for( int j = i; j < a.numCols; j++ ) {
//                c.set(j,i,c.get(i,j));
//            }
//        }
    }

    public static void inner_reorder_upper(RowD1Matrix64F a, RowD1Matrix64F c) {
        for( int i = 0; i < a.numCols; i++ ) {
            int indexC = i*c.numCols+i;
            double valAi = a.data[i];
            for( int j = i; j < a.numCols; j++ ) {
                c.data[indexC++] =  valAi*a.data[j];
            }

            for( int k = 1; k < a.numRows; k++ ) {
                indexC = i*c.numCols+i;
                int indexB = k*a.numCols+i;
                valAi = a.data[indexB];
                for( int j = i; j < a.numCols; j++ ) {
                    c.data[indexC++] +=  valAi*a.data[indexB++];
                }
            }
        }
    }
}
