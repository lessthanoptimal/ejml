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

package org.ejml.dense.block;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRBlock;
import org.ejml.data.FSubmatrixD1;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestVectorOps_FDRB {

    Random rand = new Random(234);
    int r = 3;

    @Test
    public void scale_row() {
        int rowA=0;
        int rowB=1;

        float alpha = 1.5f;

        for( int width = 1; width <= 3*r; width++ ) {
//            System.out.println("width "+width);
            int end = width;
            int offset = width > 1 ? 1 : 0;

            SimpleMatrix A = SimpleMatrix.random_FDRM(r,width, -1.0f , 1.0f ,rand);
            FMatrixRBlock Ab = MatrixOps_FDRB.convert(A.getFDRM(),r);
            FMatrixRBlock Bb = Ab.copy();

            SimpleMatrix b = A.extractVector(true,rowA).scale(alpha);

            VectorOps_FDRB.scale_row(r,new FSubmatrixD1(Ab),rowA, alpha, new FSubmatrixD1(Bb),rowB,offset,end);

            checkVector_row(rowB, end, offset, A, Bb, b);
        }
    }

    @Test
    public void div_row() {
        int rowA=0;
        int rowB=1;

        float alpha = 1.5f;

        for( int width = 1; width <= 3*r; width++ ) {
//            System.out.println("width "+width);
            int end = width;
            int offset = width > 1 ? 1 : 0;

            SimpleMatrix A = SimpleMatrix.random_FDRM(r,width, -1.0f , 1.0f ,rand);
            FMatrixRBlock Ab = MatrixOps_FDRB.convert(A.getFDRM(),r);
            FMatrixRBlock Bb = Ab.copy();

            SimpleMatrix b = A.extractVector(true,rowA).divide(alpha);

            VectorOps_FDRB.div_row(r,new FSubmatrixD1(Ab),rowA, alpha, new FSubmatrixD1(Bb),rowB,offset,end);

            checkVector_row(rowB, end, offset, A, Bb, b);
        }
    }

    @Test
    public void add_row() {
        int rowA=0;
        int rowB=1;
        int rowC=2;

        float alpha = 1.5f;
        float beta = -0.7f;

        for( int width = 1; width <= 3*r; width++ ) {
//            System.out.println("width "+width);
            int end = width;
            int offset = width > 1 ? 1 : 0;

            SimpleMatrix A = SimpleMatrix.random_FDRM(r,width, -1.0f, 1.0f,rand);
            SimpleMatrix B = SimpleMatrix.random_FDRM(r,width, -1.0f, 1.0f,rand);
            FMatrixRBlock Ab = MatrixOps_FDRB.convert(A.getFDRM(),r);
            FMatrixRBlock Bb = MatrixOps_FDRB.convert(B.getFDRM(),r);
            FMatrixRBlock Cb = Ab.copy();

            SimpleMatrix a = A.extractVector(true,rowA).scale(alpha);
            SimpleMatrix b = B.extractVector(true,rowB).scale(beta);
            SimpleMatrix c = a.plus(b);

            VectorOps_FDRB.add_row(r,
                    new FSubmatrixD1(Ab),rowA, alpha,
                    new FSubmatrixD1(Bb),rowB, beta ,
                    new FSubmatrixD1(Cb),rowC, offset,end);

            checkVector_row(rowC, end, offset, A, Cb, c);
        }
    }

    @Test
    public void dot_row() {
        int rowA=0;
        int rowB=1;


        for( int width = 1; width <= 3*r; width++ ) {
//            System.out.println("width "+width);
            int end = width;
            int offset = width > 1 ? 1 : 0;

            SimpleMatrix A = SimpleMatrix.random_FDRM(r,width, -1.0f, 1.0f, rand);
            SimpleMatrix a = A.extractMatrix(rowA,rowA+1,offset,SimpleMatrix.END);
            SimpleMatrix B = SimpleMatrix.random_FDRM(r,width, -1.0f, 1.0f, rand);
            SimpleMatrix b = B.extractMatrix(rowB,rowB+1,offset,SimpleMatrix.END);

            FMatrixRBlock Ab = MatrixOps_FDRB.convert(A.getFDRM(),r);
            FMatrixRBlock Bb = MatrixOps_FDRB.convert(B.getFDRM(),r);

            float expected = (float)a.dot(b);

            float found = VectorOps_FDRB.dot_row(r,new FSubmatrixD1(Ab),rowA, new FSubmatrixD1(Bb),rowB,offset,end);

            assertEquals(expected,found, UtilEjml.TEST_F32);
        }
    }

    @Test
    public void dot_row_col() {
        int rowA=0;
        int colB=1;


        for( int width = 1; width <= 3*r; width++ ) {
//            System.out.println("width "+width);
            int end = width;
            int offset = width > 1 ? 1 : 0;
            if( colB >= width) colB = 0;

            SimpleMatrix A = SimpleMatrix.random_FDRM(width,width, -1.0f, 1.0f, rand);
            SimpleMatrix a = A.extractMatrix(rowA,rowA+1,offset,SimpleMatrix.END);
            SimpleMatrix B = SimpleMatrix.random_FDRM(width,width, -1.0f, 1.0f, rand);
            SimpleMatrix b = B.extractMatrix(offset,SimpleMatrix.END,colB,colB+1);

            FMatrixRBlock Ab = MatrixOps_FDRB.convert(A.getFDRM(),r);
            FMatrixRBlock Bb = MatrixOps_FDRB.convert(B.getFDRM(),r);

            float expected = (float)a.dot(b);

            float found = VectorOps_FDRB.dot_row_col(r,
                    new FSubmatrixD1(Ab),rowA,
                    new FSubmatrixD1(Bb),colB,
                    offset,end);

            assertEquals(expected,found,UtilEjml.TEST_F32);
        }
    }

    /**
     * Checks to see if only the anticipated parts of the matrix have been modified and that
     * they are the anticipated values.
     *
     * @param row The row modified in modMatrix.
     * @param end end of the vector.
     * @param offset start of the vector.
     * @param untouched Original values of the modMatrix.
     * @param modMatrix The matrix which have been modified by the function being tested.
     * @param modVector Vector that contains the anticipated values.
     */
    public static void checkVector_row(int row, int end, int offset,
                                       SimpleMatrix untouched,
                                       FMatrixRBlock modMatrix, SimpleMatrix modVector) {
        for( int i = 0; i < modMatrix.numRows; i++ ) {
            if( i == row ) {
                for( int j = 0; j < offset; j++ ) {
                    assertEquals(untouched.get(i,j), modMatrix.get(i,j),UtilEjml.TEST_F32);
                }
                for( int j = offset; j < end; j++ ) {
                    assertEquals(modVector.get(j), modMatrix.get(i,j),UtilEjml.TEST_F32);
                }
                for( int j = end; j < modMatrix.numCols; j++ ) {
                    assertEquals(untouched.get(i,j), modMatrix.get(i,j),UtilEjml.TEST_F32);
                }
            } else {
                for( int j = 0; j < modMatrix.numCols; j++ ) {
                    assertEquals(untouched.get(i,j), modMatrix.get(i,j),UtilEjml.TEST_F32,i+" "+j);
                }
            }
        }
    }
}
