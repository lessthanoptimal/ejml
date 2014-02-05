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

package org.ejml.ops;

import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.EigenDecomposition;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestEigenOps {

    Random rand = new Random(12344);

    /**
     * Compute an eigen value and compare against a known solution from octave.
     */
    @Test
    public void computeEigenValue() {
        DenseMatrix64F A = new DenseMatrix64F(3,3,
                true, 0.053610, 0.030405, 0.892620, 0.090954, 0.074065, 0.875797, 0.105369, 0.928981, 0.965506);

        DenseMatrix64F u = new DenseMatrix64F(3,1,
                true, -0.4502917, -0.4655377, -0.7619134);

        double value = EigenOps.computeEigenValue(A,u);

        assertEquals(1.59540,value,1e-4);
    }

    /**
     * Give it a matrix that describes a Markov process and see if it produces 1
     */
    @Test
    public void boundLargestEigenValue_markov() {
        // create the matrix
        DenseMatrix64F A = RandomMatrices.createRandom(3,3,rand);

        for( int i = 0; i < 3; i++ ) {
            double total = 0;
            for( int j = 0; j < 3; j++ ) {
                total += A.get(i,j);
            }

            for( int j = 0; j < 3; j++ ) {
                A.set(i,j,A.get(i,j)/total);
            }
        }

        double[] val = EigenOps.boundLargestEigenValue(A,null);

        assertEquals(1.0,val[0],1e-8);
        assertEquals(1.0,val[1],1e-8);
    }

    @Test
    public void createMatrixV() {
        DenseMatrix64F A = RandomMatrices.createSymmetric(3,-1,1,rand);

        EigenDecomposition<DenseMatrix64F> decomp = DecompositionFactory.eig(A.numRows,true);
        assertTrue(decomp.decompose(A));

        DenseMatrix64F V = EigenOps.createMatrixV(decomp);

        for( int i = 0; i < 3; i++ ) {
            DenseMatrix64F v = decomp.getEigenVector(i);

            for( int j = 0; j < 3; j++ ) {
                assertEquals(V.get(j,i),v.get(j),1e-8);
            }
        }
    }

    @Test
    public void createMatrixD() {
        DenseMatrix64F A = RandomMatrices.createSymmetric(3,-1,1,rand);

        EigenDecomposition<DenseMatrix64F> decomp = DecompositionFactory.eig(A.numRows,true);
        assertTrue(decomp.decompose(A));

        DenseMatrix64F D = EigenOps.createMatrixD(decomp);

        for( int i = 0; i < 3; i++ ) {
            Complex64F e = decomp.getEigenvalue(i);

            if( e.isReal() ) {
                assertEquals(e.real,D.get(i,i),1e-10);
            }
        }
    }
}
