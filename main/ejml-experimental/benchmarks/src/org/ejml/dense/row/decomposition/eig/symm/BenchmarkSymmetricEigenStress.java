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

package org.ejml.dense.row.decomposition.eig.symm;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.SpecializedOps_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;


/**
 * Provides a more rigorous and time consuming series of tests to check the correctness of a
 * symmetric matrix eigenvalue decomposition.
 *
 * @author Peter Abeles
 */
public class BenchmarkSymmetricEigenStress {

    Random rand = new Random(234234);

    public void checkMatrix( int N , long seed ) {
        DMatrixRMaj A = new DMatrixRMaj(N,N);

        Random localRand = new Random(seed);
        RandomMatrices_DDRM.symmetric(A,-1,1,localRand);

        EigenDecomposition_F64<DMatrixRMaj> decomp = DecompositionFactory_DDRM.eig(A.numRows,true);

        System.out.println("Decomposing...");

        if( !decomp.decompose(A) ) {
            throw new RuntimeException("Decomposition failed");
        }

        DMatrixRMaj L = new DMatrixRMaj(N,1);
        DMatrixRMaj R = new DMatrixRMaj(N,1);

        for( int i = 0; i < N; i++ ) {
            Complex_F64 value = decomp.getEigenvalue(i);

            DMatrixRMaj vector = decomp.getEigenVector(i);

            if( !value.isReal())
                throw new RuntimeException("Complex eigenvalue");

            CommonOps_DDRM.mult(A,vector,L);
            CommonOps_DDRM.scale(value.real,vector,R);

            double diff = SpecializedOps_DDRM.diffNormF(L,R)/N;

            if( diff > UtilEjml.EPS*1000 )
                System.out.println("["+i+"] value = "+value.real+" error = "+diff);
        }
        System.out.println("done");
    }

    public void checkRandomMatrices( int N ) {
        System.out.println("N = "+N);
        EigenDecomposition_F64 decomp = DecompositionFactory_DDRM.eig(N,true);

        DMatrixRMaj A = new DMatrixRMaj(N,N);

        for( int i = 0; i < 1000; i++ ) {
            long seed = rand.nextLong();
            System.out.print("Date = "+ LocalDateTime.now(ZoneId.systemDefault())+" Seed = "+seed);

            Random localRand = new Random(seed);

            RandomMatrices_DDRM.symmetric(A,-1,1,localRand);

            if( !decomp.decompose(A) ) {
                System.out.println("Decomposition failed");
                return;
            }

            double error = DecompositionFactory_DDRM.quality(A,decomp);
            System.out.println("      error = "+error);
            if( error > 0.05 || Double.isNaN(error) || Double.isInfinite(error)) {
                System.out.println("   Large Error");
                return;
            }
        }
    }

    public static void main( String args[] ) {
        BenchmarkSymmetricEigenStress stress = new BenchmarkSymmetricEigenStress();

        stress.checkRandomMatrices(1000);

//        stress.checkMatrix(1000,5878413318033397987L);
    }
}
