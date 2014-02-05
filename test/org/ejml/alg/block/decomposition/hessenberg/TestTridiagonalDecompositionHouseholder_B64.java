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

package org.ejml.alg.block.decomposition.hessenberg;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.dense.decomposition.hessenberg.TridiagonalDecompositionHouseholderOrig_D64;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.D1Submatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.EjmlUnitTests;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
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
            
            DenseMatrix64F A = RandomMatrices.createSymmetric(width,-1,1,rand);
            BlockMatrix64F Ab = BlockMatrixOps.convert(A,r);

            TridiagonalDecompositionHouseholderOrig_D64 decomp = new TridiagonalDecompositionHouseholderOrig_D64();
            decomp.decompose(A);

            DenseMatrix64F expected = decomp.getQT();

            TridiagonalDecompositionHouseholder_B64 decompB = new TridiagonalDecompositionHouseholder_B64();
            assertTrue(decompB.decompose(Ab));

//            expected.print();
//            Ab.print();

            // see if the decomposed matrix is the same
            for( int i = 0; i < width; i++ ) {
                for( int j = i; j < width; j++ ) {
                    assertEquals(i+" "+j,expected.get(i,j),Ab.get(i,j),1e-8);
                }
            }
            // check the gammas
            for( int i = 0; i < width-1; i++ ) {
                assertEquals(decomp.getGamma(i+1),decompB.gammas[i],1e-8);
            }

            DenseMatrix64F Q = decomp.getQ(null);
            BlockMatrix64F Qb = decompB.getQ(null,false);

            EjmlUnitTests.assertEquals(Q,Qb,1e-8);
        }
    }

    @Test
    public void fullTest() {
        for( int width = 1; width <= r*3; width++ ) {
            SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices.createSymmetric(width,-1,1,rand));
            BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);

            TridiagonalDecompositionHouseholder_B64 alg = new TridiagonalDecompositionHouseholder_B64();

            assertTrue(alg.decompose(Ab));

            BlockMatrix64F Qb = alg.getQ(null,false);
            BlockMatrix64F Tb = alg.getT(null);

            SimpleMatrix Q = new SimpleMatrix(Qb);
            SimpleMatrix T = new SimpleMatrix(Tb);

            // reconstruct the original matrix
            SimpleMatrix A_found = Q.mult(T).mult(Q.transpose());

            assertTrue(MatrixFeatures.isIdentical(A.getMatrix(),A_found.getMatrix(),1e-8));
        }
    }

    @Test
    public void multPlusTransA() {
        for( int width = r+1; width <= r*3; width++ ) {
            SimpleMatrix A = SimpleMatrix.random(width,width,-1,1,rand);
            SimpleMatrix U = SimpleMatrix.random(r,width,-1,1,rand);
            SimpleMatrix V = SimpleMatrix.random(r,width,-1,1,rand);

            BlockMatrix64F Ab = BlockMatrixOps.convert(A.getMatrix(),r);
            BlockMatrix64F Ub = BlockMatrixOps.convert(U.getMatrix(),r);
            BlockMatrix64F Vb = BlockMatrixOps.convert(V.getMatrix(),r);

            SimpleMatrix expected = A.plus(U.transpose().mult(V));

            TridiagonalDecompositionHouseholder_B64.multPlusTransA(r, new D1Submatrix64F(Ub)
                    , new D1Submatrix64F(Vb), new D1Submatrix64F(Ab));


            for( int i = r; i < width; i++ ) {
                for( int j = i; j < width; j++ ) {
                    assertEquals(i+" "+j,expected.get(i,j),Ab.get(i,j),1e-8);
                }
            }
        }
    }
}
