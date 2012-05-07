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

package org.ejml.alg.dense.decomposition.eig.symm;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.decomposition.DecompositionFactory;
import org.ejml.alg.dense.decomposition.EigenDecomposition;
import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;
import org.ejml.ops.SpecializedOps;

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
        DenseMatrix64F A = new DenseMatrix64F(N,N);

        Random localRand = new Random(seed);
        RandomMatrices.createSymmetric(A,-1,1,localRand);

        EigenDecomposition<DenseMatrix64F> decomp = DecompositionFactory.eig(A.numRows);

        System.out.println("Decomposing...");

        if( !decomp.decompose(A) ) {
            throw new RuntimeException("Decomposition failed");
        }

        DenseMatrix64F L = new DenseMatrix64F(N,1);
        DenseMatrix64F R = new DenseMatrix64F(N,1);

        for( int i = 0; i < N; i++ ) {
            Complex64F value = decomp.getEigenvalue(i);

            DenseMatrix64F vector = decomp.getEigenVector(i);

            if( !value.isReal())
                throw new RuntimeException("Complex eigenvalue");

            CommonOps.mult(A,vector,L);
            CommonOps.scale(value.real,vector,R);

            double diff = SpecializedOps.diffNormF(L,R)/N;

            if( diff > UtilEjml.EPS*1000 )
                System.out.println("["+i+"] value = "+value.real+" error = "+diff);
        }
        System.out.println("done");
    }

    public void checkRandomMatrices( int N ) {
        System.out.println("N = "+N);
        EigenDecomposition decomp = DecompositionFactory.eig(N);

        DenseMatrix64F A = new DenseMatrix64F(N,N);

        for( int i = 0; i < 1000; i++ ) {
            long seed = rand.nextLong();
            System.out.print("Date = "+new Date()+" Seed = "+seed);

            Random localRand = new Random(seed);

            RandomMatrices.createSymmetric(A,-1,1,localRand);

            if( !decomp.decompose(A) ) {
                System.out.println("Decomposition failed");
                return;
            }

            double error = DecompositionFactory.quality(A,decomp);
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
