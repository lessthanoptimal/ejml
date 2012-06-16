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

package org.ejml.ops;

import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.factory.EigenDecomposition;
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
