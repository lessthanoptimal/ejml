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

package org.ejml.alg.dense.decomposition.eig.symm;

import org.ejml.alg.dense.decomposition.hessenberg.TridiagonalDecompositionHouseholder_D64;
import org.ejml.data.DenseMatrix64F;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestSymmetricQrAlgorithm {

    /**
     * There should no need to do anything in this case.
     */
    @Test
    public void shouldNotChange() {
        double diag[] = new double[]{2,3,4,5,6};
        double off[] = new double[diag.length-1];

        SymmetricQrAlgorithm alg = new SymmetricQrAlgorithm();

        assertTrue(alg.process(diag.length,diag,off));

        for( int i = 0; i < diag.length; i++ ) {
            assertEquals(1,countNumFound(alg,diag[i],1e-4));
        }
    }

    /**
     * The tridiagonal matrix has off diagonal terms now
     */
    @Test
    public void hasOffDiagonal() {
        double diag[] = new double[]{2,3,4,5,6};
        double off[] = new double[diag.length-1];

        for( int i = 1; i < diag.length; i++ ) {
            off[i-1] = i+0.5;
        }

        SymmetricQrAlgorithm alg = new SymmetricQrAlgorithm();

        assertTrue(alg.process(diag.length,diag,off));

        assertEquals(1,countNumFound(alg,-1.26677,1e-4));
        assertEquals(1,countNumFound(alg,0.93171,1e-4));
        assertEquals(1,countNumFound(alg,3.11320,1e-4));
        assertEquals(1,countNumFound(alg,6.20897,1e-4));
        assertEquals(1,countNumFound(alg,11.01290,1e-4));

    }

    /**
     * Test it against a matrix that has zeros along the diagonal but non zero values along
     * the off diagonal elements.
     */
    @Test
    public void zeroDiagonalNotZeroOff() {
        int N = 5;
        double diag[] = new double[N];
        double off[] = new double[N-1];

        for( int i = 0; i < N-1; i++ ) {
            off[i] = i+0.5;
        }

//        A.print();

        SymmetricQrAlgorithm alg = new SymmetricQrAlgorithm();

        assertTrue(alg.process(N,diag,off));

        assertEquals(1,countNumFound(alg,-4.39719,1e-4));
        assertEquals(1,countNumFound(alg,-1.29023,1e-4));
        assertEquals(1,countNumFound(alg,0,1e-4));
        assertEquals(1,countNumFound(alg,1.29023,1e-4));
        assertEquals(1,countNumFound(alg,4.39719,1e-4));
    }

    /**
     * Provide a test case where the same eigenvalue is repeated a few times
     */
    @Test
    public void multipleEigenvalues() {
        DenseMatrix64F A = new DenseMatrix64F(5,5, true, 2.191140, -0.098491, -0.397037, 0.367426, -0.208338, -0.098491, 2.776741, 0.623341, 0.624798, 0.401906, -0.397037, 0.623341, 3.571302, -0.239631, -0.264573, 0.367426, 0.624798, -0.239631, 3.625034, -0.162896, -0.208338, 0.401906, -0.264573, -0.162896, 3.835783);

        TridiagonalDecompositionHouseholder_D64 tridiag = new TridiagonalDecompositionHouseholder_D64();
        tridiag.decompose(A);

        double diag[] = new double[5];
        double off[] = new double[4];

        tridiag.getDiagonal(diag,off);

        SymmetricQrAlgorithm alg = new SymmetricQrAlgorithm();

        assertTrue(alg.process(5,diag,off));

        assertEquals(3,countNumFound(alg,4,1e-4));
        assertEquals(2,countNumFound(alg,2,1e-4));
    }

    /**
     * Counts the number of times the specified eigenvalue appears.
     */
    public int countNumFound( SymmetricQrAlgorithm alg , double val , double tol ) {
        int total = 0;

        for( int i = 0; i < alg.getNumberOfEigenvalues(); i++ ) {
            double a = alg.getEigenvalue(i);

            if( Math.abs(a-val) <= tol ) {
                total++;
            }
        }

        return total;
    }

}
