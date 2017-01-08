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

package org.ejml.alg.dense.decomposition.eig.symm;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.RowMatrix_F64;
import org.ejml.factory.DecompositionFactory_R64;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.RandomMatrices_R64;
import org.ejml.ops.SpecializedOps_R64;

import java.util.Date;
import java.util.Random;


/**
 * Provides a more rigorous and time consuming series of tests to check the correctness of a
 * symmetric matrix eigenvalue decomposition.
 *
 * @author Peter Abeles
 */
public class SymmetricEigenStressTest {

    Random rand = new Random(234234);

    public void checkMatrix( int N , long seed ) {
        RowMatrix_F64 A = new RowMatrix_F64(N,N);

        Random localRand = new Random(seed);
        RandomMatrices_R64.createSymmetric(A,-1,1,localRand);

        EigenDecomposition_F64<RowMatrix_F64> decomp = DecompositionFactory_R64.eig(A.numRows,true);

        System.out.println("Decomposing...");

        if( !decomp.decompose(A) ) {
            throw new RuntimeException("Decomposition failed");
        }

        RowMatrix_F64 L = new RowMatrix_F64(N,1);
        RowMatrix_F64 R = new RowMatrix_F64(N,1);

        for( int i = 0; i < N; i++ ) {
            Complex_F64 value = decomp.getEigenvalue(i);

            RowMatrix_F64 vector = decomp.getEigenVector(i);

            if( !value.isReal())
                throw new RuntimeException("Complex eigenvalue");

            CommonOps_R64.mult(A,vector,L);
            CommonOps_R64.scale(value.real,vector,R);

            double diff = SpecializedOps_R64.diffNormF(L,R)/N;

            if( diff > UtilEjml.EPS*1000 )
                System.out.println("["+i+"] value = "+value.real+" error = "+diff);
        }
        System.out.println("done");
    }

    public void checkRandomMatrices( int N ) {
        System.out.println("N = "+N);
        EigenDecomposition_F64 decomp = DecompositionFactory_R64.eig(N,true);

        RowMatrix_F64 A = new RowMatrix_F64(N,N);

        for( int i = 0; i < 1000; i++ ) {
            long seed = rand.nextLong();
            System.out.print("Date = "+new Date()+" Seed = "+seed);

            Random localRand = new Random(seed);

            RandomMatrices_R64.createSymmetric(A,-1,1,localRand);

            if( !decomp.decompose(A) ) {
                System.out.println("Decomposition failed");
                return;
            }

            double error = DecompositionFactory_R64.quality(A,decomp);
            System.out.println("      error = "+error);
            if( error > 0.05 || Double.isNaN(error) || Double.isInfinite(error)) {
                System.out.println("   Large Error");
                return;
            }
        }
    }

    public static void main( String args[] ) {
        SymmetricEigenStressTest stress = new SymmetricEigenStressTest();

        stress.checkRandomMatrices(1000);

//        stress.checkMatrix(1000,5878413318033397987L);
    }
}
