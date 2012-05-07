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

package org.ejml.alg.block;

import org.ejml.data.BlockMatrix64F;
import org.ejml.data.D1Submatrix64F;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestBlockVectorOps {

    Random rand = new Random(234);
    int r = 3;

    @Test
    public void scale_row() {
        int rowA=0;
        int rowB=1;

        double alpha = 1.5;

        for( int width = 1; width <= 3*r; width++ ) {
//            System.out.println("width "+width);
            int end = width;
            int offset = width > 1 ? 1 : 0;

            SimpleMatrix A = SimpleMatrix.random(r,width,-1,1,rand);
            BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);
            BlockMatrix64F Bb = Ab.copy();

            SimpleMatrix b = A.extractVector(true,rowA).scale(alpha);

            BlockVectorOps.scale_row(r,new D1Submatrix64F(Ab),rowA, alpha, new D1Submatrix64F(Bb),rowB,offset,end);

            checkVector_row(rowB, end, offset, A, Bb, b);
        }
    }

    @Test
    public void div_row() {
        int rowA=0;
        int rowB=1;

        double alpha = 1.5;

        for( int width = 1; width <= 3*r; width++ ) {
//            System.out.println("width "+width);
            int end = width;
            int offset = width > 1 ? 1 : 0;

            SimpleMatrix A = SimpleMatrix.random(r,width,-1,1,rand);
            BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);
            BlockMatrix64F Bb = Ab.copy();

            SimpleMatrix b = A.extractVector(true,rowA).divide(alpha);

            BlockVectorOps.div_row(r,new D1Submatrix64F(Ab),rowA, alpha, new D1Submatrix64F(Bb),rowB,offset,end);

            checkVector_row(rowB, end, offset, A, Bb, b);
        }
    }

    @Test
    public void add_row() {
        int rowA=0;
        int rowB=1;
        int rowC=2;

        double alpha = 1.5;
        double beta = -0.7;

        for( int width = 1; width <= 3*r; width++ ) {
//            System.out.println("width "+width);
            int end = width;
            int offset = width > 1 ? 1 : 0;

            SimpleMatrix A = SimpleMatrix.random(r,width,-1,1,rand);
            SimpleMatrix B = SimpleMatrix.random(r,width,-1,1,rand);
            BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);
            BlockMatrix64F Bb = BlockMatrixOps.convert(B.getMatrix(),r);
            BlockMatrix64F Cb = Ab.copy();

            SimpleMatrix a = A.extractVector(true,rowA).scale(alpha);
            SimpleMatrix b = B.extractVector(true,rowB).scale(beta);
            SimpleMatrix c = a.plus(b);

            BlockVectorOps.add_row(r,
                    new D1Submatrix64F(Ab),rowA, alpha,
                    new D1Submatrix64F(Bb),rowB, beta ,
                    new D1Submatrix64F(Cb),rowC, offset,end);

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

            SimpleMatrix A = SimpleMatrix.random(r,width,-1,1,rand);
            SimpleMatrix a = A.extractMatrix(rowA,rowA+1,offset,SimpleMatrix.END);
            SimpleMatrix B = SimpleMatrix.random(r,width,-1,1,rand);
            SimpleMatrix b = B.extractMatrix(rowB,rowB+1,offset,SimpleMatrix.END);

            BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);
            BlockMatrix64F Bb = BlockMatrixOps.convert(B.getMatrix(),r);

            double expected = a.dot(b);

            double found = BlockVectorOps.dot_row(r,new D1Submatrix64F(Ab),rowA, new D1Submatrix64F(Bb),rowB,offset,end);

            assertEquals(expected,found,1e-8);
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

            SimpleMatrix A = SimpleMatrix.random(width,width,-1,1,rand);
            SimpleMatrix a = A.extractMatrix(rowA,rowA+1,offset,SimpleMatrix.END);
            SimpleMatrix B = SimpleMatrix.random(width,width,-1,1,rand);
            SimpleMatrix b = B.extractMatrix(offset,SimpleMatrix.END,colB,colB+1);

            BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);
            BlockMatrix64F Bb = BlockMatrixOps.convert(B.getMatrix(),r);

            double expected = a.dot(b);

            double found = BlockVectorOps.dot_row_col(r,
                    new D1Submatrix64F(Ab),rowA,
                    new D1Submatrix64F(Bb),colB,
                    offset,end);

            assertEquals(expected,found,1e-8);
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
                                       BlockMatrix64F modMatrix, SimpleMatrix modVector) {
        for( int i = 0; i < modMatrix.numRows; i++ ) {
            if( i == row ) {
                for( int j = 0; j < offset; j++ ) {
                    assertEquals(untouched.get(i,j), modMatrix.get(i,j),1e-8);
                }
                for( int j = offset; j < end; j++ ) {
                    assertEquals(modVector.get(j), modMatrix.get(i,j),1e-8);
                }
                for( int j = end; j < modMatrix.numCols; j++ ) {
                    assertEquals(untouched.get(i,j), modMatrix.get(i,j),1e-8);
                }
            } else {
                for( int j = 0; j < modMatrix.numCols; j++ ) {
                    assertEquals(i+" "+j, untouched.get(i,j), modMatrix.get(i,j),1e-8);
                }
            }
        }
    }
}
