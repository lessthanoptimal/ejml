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

package org.ejml.dense.row;

import org.ejml.UtilEjml;
import org.ejml.data.FGrowArray;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.factory.DecompositionFactory_FDRM;
import org.ejml.equation.Equation;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F32;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public class TestSingularOps_FDRM {

    Random rand = new Random(234234);

    @Test
    public void singularValues() {
        for (int rows = 1; rows < 8; rows++) {
            for (int cols = 1; cols < 8; cols++) {
                FMatrixRMaj A = RandomMatrices_FDRM.rectangle(rows,cols,rand);
                FMatrixRMaj A_orig = A.copy();

                float found[] = SingularOps_FDRM.singularValues(A);

                assertTrue( MatrixFeatures_FDRM.isIdentical(A_orig,A,0));

                int N = Math.min(rows,cols);
                assertEquals(N,found.length);
                for (int i = 1; i < N; i++) {
                    assertTrue(found[i-1]>found[i]);
                }
            }
        }
    }

    @Test
    public void rank_tol() {
        FMatrixRMaj A = CommonOps_FDRM.diag(1,1,2, 0.001f );
        assertEquals(4,SingularOps_FDRM.rank(A,UtilEjml.F_EPS ));
        assertEquals(3,SingularOps_FDRM.rank(A, 0.01f ));
    }

    @Test
    public void rank() {
        FMatrixRMaj A = CommonOps_FDRM.diag(1,1,2, 0.001f );
        assertEquals(4,SingularOps_FDRM.rank(A));
        A = CommonOps_FDRM.diag(1,1,2, (float)1e-24 );
        assertEquals(3,SingularOps_FDRM.rank(A));
    }

    @Test
    public void svd() {
        for (int rows = 1; rows < 8; rows++) {
            for (int cols = 1; cols < 8; cols++) {
                FMatrixRMaj A = RandomMatrices_FDRM.rectangle(rows,cols,rand);
                FMatrixRMaj A_orig = A.copy();

                FMatrixRMaj U = new FMatrixRMaj(1,1);
                FGrowArray sv = new FGrowArray();
                FMatrixRMaj Vt = new FMatrixRMaj(1,1);

                SingularOps_FDRM.svd(A,U,sv,Vt);

                assertTrue( MatrixFeatures_FDRM.isIdentical(A_orig,A,0));

                int N = Math.min(rows,cols);
                assertEquals(N,sv.length);
                for (int i = 1; i < N; i++) {
                    assertTrue(sv.data[i-1]>sv.data[i]);
                }

                FMatrixRMaj W = CommonOps_FDRM.diag(sv.data);

                FMatrixRMaj found =new Equation(U,"U",Vt,"Vt",W,"W").
                                process("A=U*W*Vt").lookupFDRM("A");

                assertTrue( MatrixFeatures_FDRM.isIdentical(A,found,UtilEjml.TEST_F32));
            }
        }
    }

    @Test
    public void descendingOrder() {
        // test different shapes of input matrices
        testDescendingOrder(3, 4, false,false);
        testDescendingOrder(4, 3, false,false);
        testDescendingOrder(3, 4, true,false);
        testDescendingOrder(4, 3, true,false);

        testDescendingInputTransposed(4,5,true,true,false);
    }


    @Test
    public void descendingOrder_array() {
        // test different shapes of input matrices
        testDescendingOrder(3, 4, false, true);
        testDescendingOrder(4, 3, false, true);
        testDescendingOrder(3, 4, true, true);
        testDescendingOrder(4, 3, true, true);

        testDescendingInputTransposed(4,5,true,true,true);
    }

    /**
     * Creates a random SVD that is highly unlikely to be in the correct order.  Adjust its order
     * and see if it produces the same matrix.
     */
    private void testDescendingOrder(int numRows, int numCols, boolean compact, boolean testArray ) {
        SimpleMatrix U,W,V;

        int minLength = Math.min(numRows,numCols);
        float singularValues[] = new float[minLength];

        if( compact ) {
            U = SimpleMatrix.wrap(RandomMatrices_FDRM.orthogonal(numRows,minLength,rand));
            W = SimpleMatrix.wrap(RandomMatrices_FDRM.diagonal(minLength,minLength,0,1,rand));
            V = SimpleMatrix.wrap(RandomMatrices_FDRM.orthogonal(numCols,minLength,rand));
        } else {
            U = SimpleMatrix.wrap(RandomMatrices_FDRM.orthogonal(numRows,numRows,rand));
            W = SimpleMatrix.wrap(RandomMatrices_FDRM.diagonal(numRows,numCols,0,1,rand));
            V = SimpleMatrix.wrap(RandomMatrices_FDRM.orthogonal(numCols,numCols,rand));
        }

        // Compute A
        SimpleMatrix A=U.mult(W).mult(V.transpose());

        // extract array of singular values
        for( int i = 0; i < singularValues.length; i++ )
            singularValues[i] = (float)W.get(i,i);
        
        // put into descending order
        if( testArray ) {
            SingularOps_FDRM.descendingOrder(U.getFDRM(),false,singularValues,minLength,V.getFDRM(),false);
            // put back into W
            for( int i = 0; i < singularValues.length; i++ )
                W.set(i,i,singularValues[i]);
        } else {
            SingularOps_FDRM.descendingOrder(U.getFDRM(),false,W.getFDRM(),V.getFDRM(),false);
        }

        // see if it changed the results
        SimpleMatrix A_found = U.mult(W).mult(V.transpose());

        assertTrue(A.isIdentical(A_found,UtilEjml.TEST_F32));

        // make sure singular values are descending
        if( testArray ) {
            for( int i = 1; i < minLength; i++ ) {
                assertTrue(singularValues[i-1] >= singularValues[i]);
            }
        } else {
            for( int i = 1; i < minLength; i++ ) {
                assertTrue(W.get(i-1,i-1) >= W.get(i,i));
            }
        }
    }

    /**
     * Use the transpose flags and see what happens
     */
    private void testDescendingInputTransposed(int numRows, int numCols,
                                               boolean tranU , boolean tranV , boolean testArray ) {
        SimpleMatrix U,S,V;

        int minLength = Math.min(numRows,numCols);
        float singularValues[] = new float[minLength];

        U = SimpleMatrix.wrap(RandomMatrices_FDRM.orthogonal(numRows,minLength,rand));
        S = SimpleMatrix.wrap(RandomMatrices_FDRM.diagonal(minLength,minLength,0,1,rand));
        V = SimpleMatrix.wrap(RandomMatrices_FDRM.orthogonal(numCols,minLength,rand));

        // Compute A
        SimpleMatrix A=U.mult(S).mult(V.transpose());

        // extract array of singular values
        for( int i = 0; i < singularValues.length; i++ )
            singularValues[i] = (float)S.get(i,i);

        // put into ascending order
        if( tranU ) U = U.transpose();
        if( tranV ) V = V.transpose();

        // put into descending order
        if( testArray ) {
            SingularOps_FDRM.descendingOrder(U.getFDRM(),tranU,singularValues,minLength,V.getFDRM(),tranV);
            // put back into S
            for( int i = 0; i < singularValues.length; i++ )
                S.set(i,i,singularValues[i]);
        } else {
            SingularOps_FDRM.descendingOrder(U.getFDRM(),tranU,S.getFDRM(),V.getFDRM(),tranV);
        }

        // see if it changed the results
        if( tranU ) U = U.transpose();
        if( tranV ) V = V.transpose();
        SimpleMatrix A_found = U.mult(S).mult(V.transpose());

        assertTrue(A.isIdentical(A_found,UtilEjml.TEST_F32));

        // make sure singular values are descending
        if( testArray ) {
            for( int i = 1; i < minLength; i++ ) {
                assertTrue(singularValues[i-1] >= singularValues[i]);
            }
        } else {
            for( int i = 1; i < minLength; i++ ) {
                assertTrue(S.get(i-1,i-1) >= S.get(i,i));
            }
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

        U = SimpleMatrix.wrap(RandomMatrices_FDRM.orthogonal(numRows,minLength,rand));
        S = SimpleMatrix.wrap(RandomMatrices_FDRM.diagonal(minLength,minLength,0,1,rand));
        V = SimpleMatrix.wrap(RandomMatrices_FDRM.orthogonal(numCols,minLength,rand));

        // put in a NaN
        S.set(2,2,Float.NaN);

        SingularOps_FDRM.descendingOrder(U.getFDRM(),false,S.getFDRM(),V.getFDRM(),false);

        assertTrue( Float.isNaN((float)S.get(minLength-1,minLength-1)));

        // put in an Inf
        S.set(2,2,Float.POSITIVE_INFINITY);

        SingularOps_FDRM.descendingOrder(U.getFDRM(),false,S.getFDRM(),V.getFDRM(),false);

        assertTrue( Float.isInfinite((float)S.get(0,0)));
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
        FMatrixRMaj U = new FMatrixRMaj(numRows,numRows);
        FMatrixRMaj W = new FMatrixRMaj(numRows,numCols);
        FMatrixRMaj V = new FMatrixRMaj(numCols,numCols);

        SingularOps_FDRM.checkSvdMatrixSize(U,false,W,V,false);
        CommonOps_FDRM.transpose(U);
        CommonOps_FDRM.transpose(V);
        SingularOps_FDRM.checkSvdMatrixSize(U,true,W,V,true);

        // compact SVD
        U = new FMatrixRMaj(numRows,s);
        W = new FMatrixRMaj(s,s);
        V = new FMatrixRMaj(numCols,s);

        SingularOps_FDRM.checkSvdMatrixSize(U,false,W,V,false);
        CommonOps_FDRM.transpose(U);
        CommonOps_FDRM.transpose(V);
        SingularOps_FDRM.checkSvdMatrixSize(U,true,W,V,true);

        // see what happens if you throw in some null matrices
        SingularOps_FDRM.checkSvdMatrixSize(null,false,W,null,false);
        SingularOps_FDRM.checkSvdMatrixSize(null,true,W,V,true);
        SingularOps_FDRM.checkSvdMatrixSize(U,true,W,null,true);
    }

    private void checkSvdMatrixSize_negative( int numRows , int numCols )
    {
        int s = Math.min(numRows,numCols);

        // create a none compact SVD
        FMatrixRMaj U = new FMatrixRMaj(numRows,s);
        FMatrixRMaj W = new FMatrixRMaj(numRows,numCols);
        FMatrixRMaj V = new FMatrixRMaj(numCols,s);

        try {
            SingularOps_FDRM.checkSvdMatrixSize(U,false,W,V,false);
            fail("An exception should have been thrown");
        } catch( RuntimeException e) {}


        // compact SVD
        U = new FMatrixRMaj(numRows,s);
        W = new FMatrixRMaj(s,s);
        V = new FMatrixRMaj(numCols,s);

        try {
            SingularOps_FDRM.checkSvdMatrixSize(U,true,W,V,true);
            fail("An exception should have been thrown");
        } catch( RuntimeException e) {}
        CommonOps_FDRM.transpose(U);
        CommonOps_FDRM.transpose(V);
        try {
            SingularOps_FDRM.checkSvdMatrixSize(U,false,W,V,false);
            fail("An exception should have been thrown");
        } catch( RuntimeException e) {}
    }

    @Test
    public void nullVector() {
        for( int numRows = 2; numRows < 10; numRows++ ) {
            for( int numCols = 2; numCols < 10; numCols++ ) {
                // construct a matrix with a null space by decomposition a random matrix
                // and setting one of its singular values to zero
                SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_FDRM.rectangle(numRows,numCols,rand));

                SingularValueDecomposition_F32<FMatrixRMaj> svd = DecompositionFactory_FDRM.svd(A.numRows(), A.numCols(),true,true,false);
                assertTrue(svd.decompose(A.getFDRM()));

                SimpleMatrix U = SimpleMatrix.wrap(svd.getU(null,false));
                SimpleMatrix S = SimpleMatrix.wrap(svd.getW(null));
                SimpleMatrix Vt = SimpleMatrix.wrap(svd.getV(null,true));

                // pick an element inconveniently in the middle to be the null space
                S.set(1,1,0);
                svd.getSingularValues()[1] = 0;

                A=U.mult(S).mult(Vt);

                // Find the right null space
                SimpleMatrix v = SimpleMatrix.wrap(SingularOps_FDRM.nullVector(svd, true , null));

                // see if the returned vector really is the null space
                SimpleMatrix ns = A.mult(v);

                for( int i = 0; i < ns.numRows(); i++ ) {
                    assertEquals(0,ns.get(i),UtilEjml.TEST_F32);
                }

                // Find the left null space
                v = SimpleMatrix.wrap(SingularOps_FDRM.nullVector(svd, false , null));

                // see if the returned vector really is the null space
                ns = v.transpose().mult(A);

                for( int i = 0; i < ns.numRows(); i++ ) {
                    assertEquals(0,ns.get(i),UtilEjml.TEST_F32);
                }
            }
        }
    }

    @Test
    public void nullSpace() {
        for( int numRows = 2; numRows < 5; numRows++ ) {
            for( int numCols = 2; numCols < 5; numCols++ ) {

                // construct a matrix with a null space by decomposition a random matrix
                // and setting one of its singular values to zero
                SimpleMatrix A = SimpleMatrix.wrap(RandomMatrices_FDRM.rectangle(numRows,numCols,rand));

                SingularValueDecomposition_F32<FMatrixRMaj> svd = DecompositionFactory_FDRM.svd(A.numRows(), A.numCols(),true,true,false);
                assertTrue(svd.decompose(A.getFDRM()));

                SimpleMatrix U = SimpleMatrix.wrap(svd.getU(null,false));
                SimpleMatrix S = SimpleMatrix.wrap(svd.getW(null));
                SimpleMatrix Vt = SimpleMatrix.wrap(svd.getV(null,true));

                // pick an element inconveniently in the middle to be the null space
                S.set(1,1,0);
                svd.getSingularValues()[1] = 0;

                A=U.mult(S).mult(Vt);

                // now find the null space
                SimpleMatrix ns = SimpleMatrix.wrap(SingularOps_FDRM.nullSpace(svd,null,UtilEjml.F_EPS));

                // make sure the null space is not all zero
                assertTrue( Math.abs(CommonOps_FDRM.elementMaxAbs(ns.getFDRM())) > 0 );

                // check the null space's size
                assertEquals(ns.numRows(),A.numCols());
                assertEquals(ns.numCols(),1+Math.max(numCols-numRows,0));

                // see if the results are null
                SimpleMatrix found = A.mult(ns);
                assertTrue( Math.abs(CommonOps_FDRM.elementMaxAbs(found.getFDRM())) <= 10*UtilEjml.F_EPS );
            }
        }
    }

    /**
     * Decompose a singular matrix and see if it produces the expected result
     */
    @Test
    public void rank_and_nullity(){
        FMatrixRMaj A = new FMatrixRMaj(3,3, true,
                -0.988228951897092f, -1.086594333683141f, -1.433160736952583f,
                -3.190200029661606f, 0.190459703263404f, -6.475629910954768f,
                1.400596416735888f, 7.158603907761226f, -0.778109120408813f);
        rank_and_nullity(A,2,1);

        //wide matrix
        A = new FMatrixRMaj(1,3,true,1,0,0);
        rank_and_nullity(A,1,2);

        // tall matrix
        A = new FMatrixRMaj(3,1,true,1,0,0);
        rank_and_nullity(A,1,0);
    }

    public void rank_and_nullity(FMatrixRMaj A , int rank , int nullity ) {
        SingularValueDecomposition_F32<FMatrixRMaj> alg = DecompositionFactory_FDRM.svd(A.numRows,A.numCols,true,true,false);
        assertTrue(alg.decompose(A));

        assertEquals(rank, SingularOps_FDRM.rank(alg, UtilEjml.F_EPS));
        assertEquals(nullity, SingularOps_FDRM.nullity(alg, UtilEjml.F_EPS));
    }

    /**
     * Decompose a singular matrix and see if it produces the expected result
     */
    @Test
    public void rank_and_nullity_noArgument(){
        FMatrixRMaj A = new FMatrixRMaj(3,3, true,
                -0.988228951897092f, -1.086594333683141f, -1.433160736952583f,
                -3.190200029661606f, 0.190459703263404f, -6.475629910954768f,
                1.400596416735888f, 7.158603907761226f, -0.778109120408813f);
        rank_and_nullity_noArgument(A, 2, 1);

        //wide matrix
        A = new FMatrixRMaj(1,3,true,1,0,0);
        rank_and_nullity_noArgument(A,1,2);

        // tall matrix
        A = new FMatrixRMaj(3,1,true,1,0,0);
        rank_and_nullity_noArgument(A,1,0);
    }

    public void rank_and_nullity_noArgument(FMatrixRMaj A , int rank , int nullity ) {
        SingularValueDecomposition_F32<FMatrixRMaj> alg = DecompositionFactory_FDRM.svd(A.numRows,A.numCols,true,true,false);
        assertTrue(alg.decompose(A));

        assertEquals(rank, SingularOps_FDRM.rank(alg));
        assertEquals(nullity, SingularOps_FDRM.nullity(alg));
    }
}
