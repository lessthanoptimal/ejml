/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

import org.ejml.UtilEjml;
import org.ejml.alg.dense.decomposition.DecompositionFactory;
import org.ejml.alg.dense.decomposition.SingularValueDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestSingularOps {

    Random rand = new Random(234234);

    @Test
    public void descendingOrder() {
        // test different shapes of input matrices
        testDescendingOrder(3, 4, false);
        testDescendingOrder(4, 3, false);
        testDescendingOrder(3, 4, true);
        testDescendingOrder(4, 3, true);
    }

    private void testDescendingOrder(int numRows, int numCols, boolean compact) {
        SimpleMatrix U,S,V;

        int minLength = Math.min(numRows,numCols);

        if( compact ) {
            U = SimpleMatrix.wrap(RandomMatrices.createOrthogonal(numRows,minLength,rand));
            S = SimpleMatrix.wrap(RandomMatrices.createDiagonal(minLength,minLength,0,1,rand));
            V = SimpleMatrix.wrap(RandomMatrices.createOrthogonal(numCols,minLength,rand));
        } else {
            U = SimpleMatrix.wrap(RandomMatrices.createOrthogonal(numRows,numRows,rand));
            S = SimpleMatrix.wrap(RandomMatrices.createDiagonal(numRows,numCols,0,1,rand));
            V = SimpleMatrix.wrap(RandomMatrices.createOrthogonal(numCols,numCols,rand));
        }

        // Compute A
        SimpleMatrix A=U.mult(S).mult(V.transpose());

        // put into ascending order
        SingularOps.descendingOrder(U.getMatrix(),S.getMatrix(),V.getMatrix());

        // see if it changed the results
        SimpleMatrix A_found = U.mult(S).mult(V.transpose());

        assertTrue(A.isIdentical(A_found,1e-8));

        // make sure singular values are descending
        for( int i = 1; i < minLength; i++ ) {
            assertTrue(S.get(i-1,i-1) >= S.get(i,i));
        }
    }

    @Test
    public void nullSpace() {

        for( int numRows = 2; numRows < 5; numRows++ ) {
            for( int numCols = 2; numCols < 5; numCols++ ) {

                // construct a matrix with a null space by decomposition a random matrix
                // and setting one of its singular values to zero
                SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices.createRandom(numRows,numCols,rand));

                SingularValueDecomposition svd = DecompositionFactory.svd(true,true,false);
                assertTrue(svd.decompose(A.getMatrix()));

                SimpleMatrix U = SimpleMatrix.wrap(svd.getU());
                SimpleMatrix S = SimpleMatrix.wrap(svd.getW(null));
                SimpleMatrix V = SimpleMatrix.wrap(svd.getV());

                S.set(1,1,0);
                svd.getSingularValues()[1] = 0;

                A=U.mult(S).mult(V.transpose());

                // now find the null space
                SimpleMatrix v = SimpleMatrix.wrap(SingularOps.nullSpace(svd,null));

                SimpleMatrix ns = A.mult(v);

                for( int i = 0; i < ns.numRows(); i++ ) {
                    assertEquals(0,ns.get(i,0),1e-8);
                }
            }
        }
    }

    /**
     * Decompose a singular matrix and see if it produces the expected result
     */
    @Test
    public void rank_and_nullity(){
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, -0.988228951897092, -1.086594333683141, -1.433160736952583, -3.190200029661606, 0.190459703263404, -6.475629910954768, 1.400596416735888, 7.158603907761226, -0.778109120408813);

        SingularValueDecomposition alg = DecompositionFactory.svd();
        alg.decompose(A);

        assertEquals(2,SingularOps.rank(alg, UtilEjml.EPS));
        assertEquals(1,SingularOps.nullity(alg, UtilEjml.EPS));
    }

}
