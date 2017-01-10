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

package org.ejml.ops;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.mult.VectorVectorMult_R64;
import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRow_B;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.data.UtilTestMatrix;
import org.ejml.factory.DecompositionFactory_R64;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F64;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestRandomMatrices_R64 {

    Random rand = new Random(48757);

    /**
     * Checks to see if all the vectors are orthogonal and of unit length.
     */
    @Test
    public void createSpan() {
        // test with combinations of vectors and numbers
        for( int dimension = 3; dimension <= 5; dimension++ ) {
            for( int numVectors = 1; numVectors <= dimension; numVectors++ ) {
                DMatrixRow_F64 span[] = RandomMatrices_R64.createSpan(dimension,numVectors,rand);

                assertEquals(numVectors,span.length);

                for( int i = 0; i < span.length; i++ ) {
                    DMatrixRow_F64 a = span[i];

                    assertEquals(1, NormOps_R64.fastNormF(a), UtilEjml.TEST_F64);

                    for( int j = i+1; j < span.length; j++ ) {
                        double dot = VectorVectorMult_R64.innerProd(a,span[j]);
                        assertEquals(0,dot,UtilEjml.TEST_F64);
                    }
                }
            }
        }
    }

    @Test
    public void createInSpan() {
        DMatrixRow_F64 span[] = RandomMatrices_R64.createSpan(5,5,rand);

        DMatrixRow_F64 A = RandomMatrices_R64.createInSpan(span,-1,1,rand);

        // reconstructed matrix
        DMatrixRow_F64 R = new DMatrixRow_F64(A.numRows,A.numCols);

        DMatrixRow_F64 tmp = new DMatrixRow_F64(A.numRows,A.numCols);

        // project the matrix into the span and recreate the original matrix
        for( int i = 0; i < 5; i++ ) {
            double val =  VectorVectorMult_R64.innerProd(span[i],A);
            assertTrue( Math.abs(val) > 1e-10 );

            CommonOps_R64.scale(val,span[i],tmp);
            CommonOps_R64.add(R,tmp,R);
        }

        double error = SpecializedOps_R64.diffNormF(A,R);

        assertEquals( error , 0 , UtilEjml.TEST_F64);
    }

    @Test
    public void createOrthogonal() {
        for( int numRows = 3; numRows <= 5; numRows++ ) {
            for( int numCols = 1; numCols <= numRows; numCols++ ) {
                DMatrixRow_F64 Q = RandomMatrices_R64.createOrthogonal(numRows,numCols,rand);

                assertEquals(Q.numRows,numRows);
                assertEquals(Q.numCols,numCols);

                assertTrue(CommonOps_R64.elementSum(Q) != 0);
                assertTrue(MatrixFeatures_R64.isOrthogonal(Q,UtilEjml.TEST_F64));
            }
        }
    }

    @Test
    public void createDiagonal_square() {
        DMatrixRow_F64 A = RandomMatrices_R64.createDiagonal(5,1,10,rand);

        assertTrue(CommonOps_R64.elementSum(A) > 5 );
        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < A.numCols; j++ ) {
                double v = A.get(i, j);

                if( i == j ) {
                    assertTrue(v >= 1 || v <= 10);
                } else {
                    assertTrue(v == 0);
                }
            }
        }
    }

    @Test
    public void createDiagonal_general() {
        testDiagonal(5,3);
        testDiagonal(3,5);
        testDiagonal(3, 3);
    }

    public void testDiagonal( int numRows , int numCols ) {
        DMatrixRow_F64 A = RandomMatrices_R64.createDiagonal(numRows,numCols,1,10,rand);

        assertEquals(A.getNumRows(), numRows);
        assertEquals(A.getNumCols(), numCols);

        assertTrue(CommonOps_R64.elementSum(A) > 5);
        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < A.numCols; j++ ) {
                double v = A.get(i,j);

                if( i == j ) {
                    assertTrue(v >= 1 || v <= 10);
                } else {
                    assertTrue(v == 0);
                }
            }
        }
    }

    @Test
    public void createSingularValues() {
        // check case when sv is more than or equal to the matrix dimension
        double sv[] = new double[]{8.2,6.2,4.1,2};

        for( int numRows = 1; numRows <= 4; numRows++ ) {
            for( int numCols = 1; numCols <= 4; numCols++ ) {
                DMatrixRow_F64 A = RandomMatrices_R64.createSingularValues(numRows,numCols, rand, sv);

                SingularValueDecomposition_F64<DMatrixRow_F64> svd =
                        DecompositionFactory_R64.svd(A.numRows,A.numCols,true,true,false);
                assertTrue(svd.decompose(A));

                int o = Math.min(numRows,numCols);

                UtilTestMatrix.checkSameElements(UtilEjml.TEST_F64,o,sv,svd.getSingularValues());
            }
        }

        // see if it fills in zeros when it is smaller than the dimension
        DMatrixRow_F64 A = RandomMatrices_R64.createSingularValues(5,5, rand, sv);

        SingularValueDecomposition_F64<DMatrixRow_F64> svd =
                DecompositionFactory_R64.svd(A.numRows, A.numCols, true, true, false);
        assertTrue(svd.decompose(A));

        UtilTestMatrix.checkSameElements(UtilEjml.TEST_F64, sv.length, sv, svd.getSingularValues());
        assertEquals(0, svd.getSingularValues()[4], UtilEjml.TEST_F64);
    }

    @Test
    public void createEigenvaluesSymm() {
        DMatrixRow_F64 A = RandomMatrices_R64.createEigenvaluesSymm(5,rand,1,2,3,4,5);

        // this should be symmetric
        assertTrue(MatrixFeatures_R64.isSymmetric(A,UtilEjml.TEST_F64));

        // decompose the matrix and extract its eigenvalues
        EigenDecomposition_F64<DMatrixRow_F64> eig = DecompositionFactory_R64.eig(A.numRows, true);
        assertTrue(eig.decompose(A));

        double ev[] = new double[5];
        for( int i = 0; i < 5; i++ ) {
            Complex_F64 e = eig.getEigenvalue(i);
            assertTrue(e.isReal());

            ev[i] = e.real;
        }

        // need to sort the eigenvalues so that I know where they are in the array
        Arrays.sort(ev);

        // see if they are what I expected them to be
        for( int i = 0; i < ev.length; i++ ) {
            assertEquals(i+1.0,ev[i],UtilEjml.TEST_F64);
        }
    }

    @Test
    public void addRandom() {
        DMatrixRow_F64 A = new DMatrixRow_F64(3,4);

        CommonOps_R64.fill(A, -2.0);

        RandomMatrices_R64.addRandom(A, 1, 2, rand);

        for( int i = 0; i < A.getNumElements(); i++ ) {
            assertTrue(A.get(i) >= -1 && A.get(i) <= 0);
        }
    }

    @Test
    public void createRandom() {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(5,4,rand);

        checkRandom1(A);
    }

    @Test
    public void createRandom_min_max() {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(30,20,-1,1,rand);

        checkRandomRange(A);
    }

    @Test
    public void setRandom() {
        DMatrixRow_F64 A = new DMatrixRow_F64(5,4);

        RandomMatrices_R64.setRandom(A,rand);

        checkRandom1(A);
    }

    private void checkRandom1(DMatrixRow_F64 a) {
        assertEquals(5, a.numRows);
        assertEquals(4, a.numCols);

        double total = 0;
        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < a.numCols; j++ ) {
                double val = a.get(i,j);

                assertTrue( val >= 0);
                assertTrue( val <= 1);
                total += val;
            }
        }

        assertTrue(total > 0);
    }

    @Test
    public void setRandomB() {
        DMatrixRow_B A = new DMatrixRow_B(5,4);

        RandomMatrices_R64.setRandomB(A,rand);

        int numTrue=0,numFalse=0;

        for (int i = 0; i < 20; i++) {
            if( A.data[i] )
                numTrue++;
            else
                numFalse++;
        }

        assertTrue(numTrue>6);
        assertTrue(numFalse>6);
        assertEquals(20,numTrue+numFalse);
    }

    @Test
    public void setRandom_min_max() {
        DMatrixRow_F64 A = new DMatrixRow_F64(30,20);
        RandomMatrices_R64.setRandom(A,-1,1,rand);

        checkRandomRange(A);
    }

    private void checkRandomRange(DMatrixRow_F64 a) {
        assertEquals(30, a.numRows);
        assertEquals(20, a.numCols);

        int numNeg = 0;
        int numPos = 0;
        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < a.numCols; j++ ) {
                double val = a.get(i,j);

                if( val < 0 )
                    numNeg++;
                else
                    numPos++;

                if( Math.abs(val) > 1 )
                    fail("Out of range");
            }
        }

        assertTrue(numNeg>0);
        assertTrue(numPos>0);
    }

    @Test
    public void createGaussian() {
        DMatrixRow_F64 A = RandomMatrices_R64.createGaussian(30, 20, 2, 0.5, rand);

        checkGaussian(A);
    }

    @Test
    public void setGaussian() {
        DMatrixRow_F64 A = new DMatrixRow_F64(30,20);

        RandomMatrices_R64.setGaussian(A, 2, 0.5, rand);

        checkGaussian(A);
    }

    private void checkGaussian(DMatrixRow_F64 a) {
        assertEquals(30, a.numRows);
        assertEquals(20, a.numCols);

        int numNeg = 0;
        int numPos = 0;
        double mean = 0;
        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < a.numCols; j++ ) {
                double val = a.get(i,j);

                if( val-2 < 0 )
                    numNeg++;
                else
                    numPos++;

                mean += val;
            }
        }

        mean /= a.numRows*a.numCols;

        assertEquals(mean,2,0.01);

        assertTrue(numNeg>0);
        assertTrue(numPos>0);
    }

    @Test
    public void createSymmPosDef() {
        for( int i = 0; i < 10; i++ ) {
            DMatrixRow_F64 A = RandomMatrices_R64.createSymmPosDef(6+i,rand);

            assertTrue(MatrixFeatures_R64.isPositiveDefinite(A));
        }
    }

    @Test
    public void createSymmetric() {
        DMatrixRow_F64 A = RandomMatrices_R64.createSymmetric(10,-1,1,rand);

        assertTrue(MatrixFeatures_R64.isSymmetric(A,UtilEjml.TEST_F64));

        // see if it has the expected range of elements
        double min = CommonOps_R64.elementMin(A);
        double max = CommonOps_R64.elementMax(A);

        assertTrue(min < 0 && min >= -1);
        assertTrue(max > 0 && max <= 1);
    }

    @Test
    public void createUpperTriangle() {
        for( int hess = 0; hess < 3; hess++ ) {
            DMatrixRow_F64 A = RandomMatrices_R64.createUpperTriangle(10,hess,-1,1,rand);

            assertTrue(MatrixFeatures_R64.isUpperTriangle(A,hess,UtilEjml.TEST_F64));

            // quick sanity check to make sure it could be proper
            assertTrue(A.get(hess,0) != 0 );

            // see if it has the expected range of elements
            double min = CommonOps_R64.elementMin(A);
            double max = CommonOps_R64.elementMax(A);

            assertTrue(min < 0 && min >= -1);
            assertTrue(max > 0 && max <= 1);
        }
    }
}
