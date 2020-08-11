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

package org.ejml.dense.block.decomposition.hessenberg;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRBlock;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.FSubmatrixD1;
import org.ejml.dense.block.MatrixOps_FDRB;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.decomposition.hessenberg.TridiagonalDecompositionHouseholderOrig_FDRM;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestTridiagonalDecompositionHouseholder_FDRB {

    Random rand = new Random(23423);
    int r = 3;
    
    @Test
    public void compareToSimple() {

        for( int width = 1; width <= r*3; width++ ) {
//            System.out.println("width = "+width);
            
            FMatrixRMaj A = RandomMatrices_FDRM.symmetric(width,-1,1,rand);
            FMatrixRBlock Ab = MatrixOps_FDRB.convert(A,r);

            TridiagonalDecompositionHouseholderOrig_FDRM decomp = new TridiagonalDecompositionHouseholderOrig_FDRM();
            decomp.decompose(A);

            FMatrixRMaj expected = decomp.getQT();

            TridiagonalDecompositionHouseholder_FDRB decompB = new TridiagonalDecompositionHouseholder_FDRB();
            assertTrue(decompB.decompose(Ab));

//            expected.print();
//            Ab.print();

            // see if the decomposed matrix is the same
            for( int i = 0; i < width; i++ ) {
                for( int j = i; j < width; j++ ) {
                    assertEquals(expected.get(i,j),Ab.get(i,j), UtilEjml.TEST_F32,i+" "+j);
                }
            }
            // check the gammas
            for( int i = 0; i < width-1; i++ ) {
                assertEquals(decomp.getGamma(i+1),decompB.gammas[i],UtilEjml.TEST_F32);
            }

            FMatrixRMaj Q = decomp.getQ(null);
            FMatrixRBlock Qb = decompB.getQ(null,false);

            EjmlUnitTests.assertEquals(Q,Qb,UtilEjml.TEST_F32);
        }
    }

    @Test
    public void fullTest() {
        for( int width = 1; width <= r*3; width++ ) {
            SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_FDRM.symmetric(width,-1,1,rand));
            FMatrixRBlock Ab = MatrixOps_FDRB.convert(A.getFDRM(),r);

            TridiagonalDecompositionHouseholder_FDRB alg = new TridiagonalDecompositionHouseholder_FDRB();

            assertTrue(alg.decompose(Ab));

            FMatrixRBlock Qb = alg.getQ(null,false);
            FMatrixRBlock Tb = alg.getT(null);

            SimpleMatrix Q = new SimpleMatrix(Qb);
            SimpleMatrix T = new SimpleMatrix(Tb);

            // reconstruct the original matrix
            SimpleMatrix A_found = Q.mult(T).mult(Q.transpose());

            assertTrue(MatrixFeatures_FDRM.isIdentical(A.getFDRM(),A_found.getFDRM(),UtilEjml.TEST_F32));
        }
    }

    @Test
    public void multPlusTransA() {
        for( int width = r+1; width <= r*3; width++ ) {
            SimpleMatrix A = SimpleMatrix.random_FDRM(width,width, -1.0f, 1.0f,rand);
            SimpleMatrix U = SimpleMatrix.random_FDRM(r,width, -1.0f, 1.0f ,rand);
            SimpleMatrix V = SimpleMatrix.random_FDRM(r,width, -1.0f, 1.0f ,rand);

            FMatrixRBlock Ab = MatrixOps_FDRB.convert(A.getFDRM(),r);
            FMatrixRBlock Ub = MatrixOps_FDRB.convert(U.getFDRM(),r);
            FMatrixRBlock Vb = MatrixOps_FDRB.convert(V.getFDRM(),r);

            SimpleMatrix expected = A.plus(U.transpose().mult(V));

            TridiagonalDecompositionHouseholder_FDRB.multPlusTransA(r, new FSubmatrixD1(Ub)
                    , new FSubmatrixD1(Vb), new FSubmatrixD1(Ab));


            for( int i = r; i < width; i++ ) {
                for( int j = i; j < width; j++ ) {
                    assertEquals(expected.get(i,j),Ab.get(i,j),UtilEjml.TEST_F32,i+" "+j);
                }
            }
        }
    }
}
