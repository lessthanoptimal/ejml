/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.block.decomposition.hessenberg;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.alg.block.MatrixOps_B64;
import org.ejml.alg.dense.decomposition.hessenberg.TridiagonalDecompositionHouseholderOrig_D64;
import org.ejml.data.BlockMatrix_F64;
import org.ejml.data.D1Submatrix_F64;
import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.MatrixFeatures_D64;
import org.ejml.ops.RandomMatrices_D64;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestTridiagonalDecompositionHouseholder_B64 {

    Random rand = new Random(23423);
    int r = 3;
    
    @Test
    public void compareToSimple() {

        for( int width = 1; width <= r*3; width++ ) {
//            System.out.println("width = "+width);
            
            RowMatrix_F64 A = RandomMatrices_D64.createSymmetric(width,-1,1,rand);
            BlockMatrix_F64 Ab = MatrixOps_B64.convert(A,r);

            TridiagonalDecompositionHouseholderOrig_D64 decomp = new TridiagonalDecompositionHouseholderOrig_D64();
            decomp.decompose(A);

            RowMatrix_F64 expected = decomp.getQT();

            TridiagonalDecompositionHouseholder_B64 decompB = new TridiagonalDecompositionHouseholder_B64();
            assertTrue(decompB.decompose(Ab));

//            expected.print();
//            Ab.print();

            // see if the decomposed matrix is the same
            for( int i = 0; i < width; i++ ) {
                for( int j = i; j < width; j++ ) {
                    assertEquals(i+" "+j,expected.get(i,j),Ab.get(i,j), UtilEjml.TEST_F64);
                }
            }
            // check the gammas
            for( int i = 0; i < width-1; i++ ) {
                assertEquals(decomp.getGamma(i+1),decompB.gammas[i],UtilEjml.TEST_F64);
            }

            RowMatrix_F64 Q = decomp.getQ(null);
            BlockMatrix_F64 Qb = decompB.getQ(null,false);

            EjmlUnitTests.assertEquals(Q,Qb,UtilEjml.TEST_F64);
        }
    }

    @Test
    public void fullTest() {
        for( int width = 1; width <= r*3; width++ ) {
            SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_D64.createSymmetric(width,-1,1,rand));
            BlockMatrix_F64 Ab = MatrixOps_B64.convert(A.matrix_F64(),r);

            TridiagonalDecompositionHouseholder_B64 alg = new TridiagonalDecompositionHouseholder_B64();

            assertTrue(alg.decompose(Ab));

            BlockMatrix_F64 Qb = alg.getQ(null,false);
            BlockMatrix_F64 Tb = alg.getT(null);

            SimpleMatrix Q = new SimpleMatrix(Qb);
            SimpleMatrix T = new SimpleMatrix(Tb);

            // reconstruct the original matrix
            SimpleMatrix A_found = Q.mult(T).mult(Q.transpose());

            assertTrue(MatrixFeatures_D64.isIdentical(A.matrix_F64(),A_found.matrix_F64(),UtilEjml.TEST_F64));
        }
    }

    @Test
    public void multPlusTransA() {
        for( int width = r+1; width <= r*3; width++ ) {
            SimpleMatrix A = SimpleMatrix.random_F64(width,width, -1.0, 1.0,rand);
            SimpleMatrix U = SimpleMatrix.random_F64(r,width, -1.0, 1.0 ,rand);
            SimpleMatrix V = SimpleMatrix.random_F64(r,width, -1.0, 1.0 ,rand);

            BlockMatrix_F64 Ab = MatrixOps_B64.convert(A.matrix_F64(),r);
            BlockMatrix_F64 Ub = MatrixOps_B64.convert(U.matrix_F64(),r);
            BlockMatrix_F64 Vb = MatrixOps_B64.convert(V.matrix_F64(),r);

            SimpleMatrix expected = A.plus(U.transpose().mult(V));

            TridiagonalDecompositionHouseholder_B64.multPlusTransA(r, new D1Submatrix_F64(Ub)
                    , new D1Submatrix_F64(Vb), new D1Submatrix_F64(Ab));


            for( int i = r; i < width; i++ ) {
                for( int j = i; j < width; j++ ) {
                    assertEquals(i+" "+j,expected.get(i,j),Ab.get(i,j),UtilEjml.TEST_F64);
                }
            }
        }
    }
}
