/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
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
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


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

        testDescendingInputTransposed(4,5,true,true);
    }

    /**
     * Creates a random SVD that is highly unlikely to be in the correct order.  Adjust its order
     * and see if it produces the same matrix.
     */
    private void testDescendingOrder(int numRows, int numCols, boolean compact) {
        SimpleMatrix U,W,V;

        int minLength = Math.min(numRows,numCols);

        if( compact ) {
            U = SimpleMatrix.wrap(RandomMatrices.createOrthogonal(numRows,minLength,rand));
            W = SimpleMatrix.wrap(RandomMatrices.createDiagonal(minLength,minLength,0,1,rand));
            V = SimpleMatrix.wrap(RandomMatrices.createOrthogonal(numCols,minLength,rand));
        } else {
            U = SimpleMatrix.wrap(RandomMatrices.createOrthogonal(numRows,numRows,rand));
            W = SimpleMatrix.wrap(RandomMatrices.createDiagonal(numRows,numCols,0,1,rand));
            V = SimpleMatrix.wrap(RandomMatrices.createOrthogonal(numCols,numCols,rand));
        }

        // Compute A
        SimpleMatrix A=U.mult(W).mult(V.transpose());

        // put into ascending order
        SingularOps.descendingOrder(U.getMatrix(),false,W.getMatrix(),V.getMatrix(),false);

        // see if it changed the results
        SimpleMatrix A_found = U.mult(W).mult(V.transpose());

        assertTrue(A.isIdentical(A_found,1e-8));

        // make sure singular values are descending
        for( int i = 1; i < minLength; i++ ) {
            assertTrue(W.get(i-1,i-1) >= W.get(i,i));
        }
    }

    /**
     * Use the transpose flags and see what happens
     */
    private void testDescendingInputTransposed(int numRows, int numCols, boolean tranU , boolean tranV ) {
        SimpleMatrix U,S,V;

        int minLength = Math.min(numRows,numCols);

        U = SimpleMatrix.wrap(RandomMatrices.createOrthogonal(numRows,minLength,rand));
        S = SimpleMatrix.wrap(RandomMatrices.createDiagonal(minLength,minLength,0,1,rand));
        V = SimpleMatrix.wrap(RandomMatrices.createOrthogonal(numCols,minLength,rand));

        // Compute A
        SimpleMatrix A=U.mult(S).mult(V.transpose());

        // put into ascending order
        if( tranU ) U = U.transpose();
        if( tranV ) V = V.transpose();

        SingularOps.descendingOrder(U.getMatrix(),tranU,S.getMatrix(),V.getMatrix(),tranV);

        // see if it changed the results
        if( tranU ) U = U.transpose();
        if( tranV ) V = V.transpose();
        SimpleMatrix A_found = U.mult(S).mult(V.transpose());

        assertTrue(A.isIdentical(A_found,1e-8));

        // make sure singular values are descending
        for( int i = 1; i < minLength; i++ ) {
            assertTrue(S.get(i-1,i-1) >= S.get(i,i));
        }
    }

    /**
     * See if it blows up with uncountable numbers
     */
    @Test
    public void descendingOrder_NaN() {
        int numRows = 5;
        int numCols = 7;
        int minLength = Math.min(numRows,numCols);

        SimpleMatrix U,S,V;

        U = SimpleMatrix.wrap(RandomMatrices.createOrthogonal(numRows,minLength,rand));
        S = SimpleMatrix.wrap(RandomMatrices.createDiagonal(minLength,minLength,0,1,rand));
        V = SimpleMatrix.wrap(RandomMatrices.createOrthogonal(numCols,minLength,rand));

        // put in a NaN
        S.set(2,2,Double.NaN);

        SingularOps.descendingOrder(U.getMatrix(),false,S.getMatrix(),V.getMatrix(),false);

        assertTrue( Double.isNaN(S.get(minLength-1,minLength-1)));

        // put in an Inf
        S.set(2,2,Double.POSITIVE_INFINITY);

        SingularOps.descendingOrder(U.getMatrix(),false,S.getMatrix(),V.getMatrix(),false);

        assertTrue( Double.isInfinite(S.get(0,0)));
    }


    /**
     * Gives it correct input matrices and makes sure no exceptions are thrown.  All permutations
     * are tested.
     */
    @Test
    public void checkSvdMatrixSize_positive() {
        checkSvdMatrixSize_positive(4,5);
        checkSvdMatrixSize_positive(5,4);
    }

    /**
     * Checks a few of the many possible bad inputs
     */
    @Test
    public void checkSvdMatrixSize_negative() {
        checkSvdMatrixSize_negative(4,5);
        checkSvdMatrixSize_negative(5,4);
    }

    private void checkSvdMatrixSize_positive( int numRows , int numCols )
    {
        int s = Math.min(numRows,numCols);

        // create a none compact SVD
        DenseMatrix64F U = new DenseMatrix64F(numRows,numRows);
        DenseMatrix64F W = new DenseMatrix64F(numRows,numCols);
        DenseMatrix64F V = new DenseMatrix64F(numCols,numCols);

        SingularOps.checkSvdMatrixSize(U,false,W,V,false);
        CommonOps.transpose(U);CommonOps.transpose(V);
        SingularOps.checkSvdMatrixSize(U,true,W,V,true);

        // compact SVD
        U = new DenseMatrix64F(numRows,s);
        W = new DenseMatrix64F(s,s);
        V = new DenseMatrix64F(numCols,s);

        SingularOps.checkSvdMatrixSize(U,false,W,V,false);
        CommonOps.transpose(U);CommonOps.transpose(V);
        SingularOps.checkSvdMatrixSize(U,true,W,V,true);

        // see what happens if you throw in some null matrices
        SingularOps.checkSvdMatrixSize(null,false,W,null,false);
        SingularOps.checkSvdMatrixSize(null,true,W,V,true);
        SingularOps.checkSvdMatrixSize(U,true,W,null,true);
    }

    private void checkSvdMatrixSize_negative( int numRows , int numCols )
    {
        int s = Math.min(numRows,numCols);

        // create a none compact SVD
        DenseMatrix64F U = new DenseMatrix64F(numRows,s);
        DenseMatrix64F W = new DenseMatrix64F(numRows,numCols);
        DenseMatrix64F V = new DenseMatrix64F(numCols,s);

        try {
            SingularOps.checkSvdMatrixSize(U,false,W,V,false);
            fail("An exception should have been thrown");
        } catch( RuntimeException e) {}


        // compact SVD
        U = new DenseMatrix64F(numRows,s);
        W = new DenseMatrix64F(s,s);
        V = new DenseMatrix64F(numCols,s);

        try {
            SingularOps.checkSvdMatrixSize(U,true,W,V,true);
            fail("An exception should have been thrown");
        } catch( RuntimeException e) {}
        CommonOps.transpose(U);CommonOps.transpose(V);
        try {
            SingularOps.checkSvdMatrixSize(U,false,W,V,false);
            fail("An exception should have been thrown");
        } catch( RuntimeException e) {}
    }

    @Test
    public void nullSpace() {

        for( int numRows = 2; numRows < 5; numRows++ ) {
            for( int numCols = 2; numCols < 5; numCols++ ) {

                // construct a matrix with a null space by decomposition a random matrix
                // and setting one of its singular values to zero
                SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices.createRandom(numRows,numCols,rand));

                SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(A.numRows(), A.numCols(),true,true,false);
                assertTrue(svd.decompose(A.getMatrix()));

                SimpleMatrix U = SimpleMatrix.wrap(svd.getU(false));
                SimpleMatrix S = SimpleMatrix.wrap(svd.getW(null));
                SimpleMatrix Vt = SimpleMatrix.wrap(svd.getV(true));

                // pick an element inconveniently in the middle to be the null space
                S.set(1,1,0);
                svd.getSingularValues()[1] = 0;

                A=U.mult(S).mult(Vt);

                // now find the null space
                SimpleMatrix v = SimpleMatrix.wrap(SingularOps.nullSpace(svd,null));

                // see if the returned vector really is the null space
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

        SingularValueDecomposition<DenseMatrix64F> alg = DecompositionFactory.svd(A.numRows,A.numCols);
        assertTrue(alg.decompose(A));

        assertEquals(2,SingularOps.rank(alg, UtilEjml.EPS));
        assertEquals(1,SingularOps.nullity(alg, UtilEjml.EPS));
    }

}
