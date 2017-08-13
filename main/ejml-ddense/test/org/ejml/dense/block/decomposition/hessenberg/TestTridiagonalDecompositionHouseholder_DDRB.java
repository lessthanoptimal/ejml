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

package org.ejml.dense.block.decomposition.hessenberg;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DSubmatrixD1;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.hessenberg.TridiagonalDecompositionHouseholderOrig_DDRM;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestTridiagonalDecompositionHouseholder_DDRB {

    Random rand = new Random(23423);
    int r = 3;
    
    @Test
    public void compareToSimple() {

        for( int width = 1; width <= r*3; width++ ) {
//            System.out.println("width = "+width);
            
            DMatrixRMaj A = RandomMatrices_DDRM.symmetric(width,-1,1,rand);
            DMatrixRBlock Ab = MatrixOps_DDRB.convert(A,r);

            TridiagonalDecompositionHouseholderOrig_DDRM decomp = new TridiagonalDecompositionHouseholderOrig_DDRM();
            decomp.decompose(A);

            DMatrixRMaj expected = decomp.getQT();

            TridiagonalDecompositionHouseholder_DDRB decompB = new TridiagonalDecompositionHouseholder_DDRB();
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

            DMatrixRMaj Q = decomp.getQ(null);
            DMatrixRBlock Qb = decompB.getQ(null,false);

            EjmlUnitTests.assertEquals(Q,Qb,UtilEjml.TEST_F64);
        }
    }

    @Test
    public void fullTest() {
        for( int width = 1; width <= r*3; width++ ) {
            SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_DDRM.symmetric(width,-1,1,rand));
            DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.ddrm(),r);

            TridiagonalDecompositionHouseholder_DDRB alg = new TridiagonalDecompositionHouseholder_DDRB();

            assertTrue(alg.decompose(Ab));

            DMatrixRBlock Qb = alg.getQ(null,false);
            DMatrixRBlock Tb = alg.getT(null);

            SimpleMatrix Q = new SimpleMatrix(Qb);
            SimpleMatrix T = new SimpleMatrix(Tb);

            // reconstruct the original matrix
            SimpleMatrix A_found = Q.mult(T).mult(Q.transpose());

            assertTrue(MatrixFeatures_DDRM.isIdentical(A.ddrm(),A_found.ddrm(),UtilEjml.TEST_F64));
        }
    }

    @Test
    public void multPlusTransA() {
        for( int width = r+1; width <= r*3; width++ ) {
            SimpleMatrix A = SimpleMatrix.random64(width,width, -1.0, 1.0,rand);
            SimpleMatrix U = SimpleMatrix.random64(r,width, -1.0, 1.0 ,rand);
            SimpleMatrix V = SimpleMatrix.random64(r,width, -1.0, 1.0 ,rand);

            DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.ddrm(),r);
            DMatrixRBlock Ub = MatrixOps_DDRB.convert(U.ddrm(),r);
            DMatrixRBlock Vb = MatrixOps_DDRB.convert(V.ddrm(),r);

            SimpleMatrix expected = A.plus(U.transpose().mult(V));

            TridiagonalDecompositionHouseholder_DDRB.multPlusTransA(r, new DSubmatrixD1(Ub)
                    , new DSubmatrixD1(Vb), new DSubmatrixD1(Ab));


            for( int i = r; i < width; i++ ) {
                for( int j = i; j < width; j++ ) {
                    assertEquals(i+" "+j,expected.get(i,j),Ab.get(i,j),UtilEjml.TEST_F64);
                }
            }
        }
    }
}
