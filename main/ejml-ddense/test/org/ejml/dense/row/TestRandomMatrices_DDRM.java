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

package org.ejml.dense.row;

import org.ejml.UtilEjml;
import org.ejml.data.BMatrixRMaj;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.UtilTestMatrix;
import org.ejml.data.ZComplex;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;
import org.ejml.interfaces.decomposition.EigenDecompositionD;
import org.ejml.interfaces.decomposition.SingularValueDecompositionD;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestRandomMatrices_DDRM {

    Random rand = new Random(48757);

    /**
     * Checks to see if all the vectors are orthogonal and of unit length.
     */
    @Test
    public void createSpan() {
        // test with combinations of vectors and numbers
        for( int dimension = 3; dimension <= 5; dimension++ ) {
            for( int numVectors = 1; numVectors <= dimension; numVectors++ ) {
                DMatrixRMaj span[] = RandomMatrices_DDRM.createSpan(dimension,numVectors,rand);

                assertEquals(numVectors,span.length);

                for( int i = 0; i < span.length; i++ ) {
                    DMatrixRMaj a = span[i];

                    assertEquals(1, NormOps_DDRM.fastNormF(a), UtilEjml.TEST_F64);

                    for( int j = i+1; j < span.length; j++ ) {
                        double dot = VectorVectorMult_DDRM.innerProd(a,span[j]);
                        assertEquals(0,dot,UtilEjml.TEST_F64);
                    }
                }
            }
        }
    }

    @Test
    public void createInSpan() {
        DMatrixRMaj span[] = RandomMatrices_DDRM.createSpan(5,5,rand);

        DMatrixRMaj A = RandomMatrices_DDRM.createInSpan(span,-1,1,rand);

        // reconstructed matrix
        DMatrixRMaj R = new DMatrixRMaj(A.numRows,A.numCols);

        DMatrixRMaj tmp = new DMatrixRMaj(A.numRows,A.numCols);

        // project the matrix into the span and recreate the original matrix
        for( int i = 0; i < 5; i++ ) {
            double val =  VectorVectorMult_DDRM.innerProd(span[i],A);
            assertTrue( Math.abs(val) > 1e-10 );

            CommonOps_DDRM.scale(val,span[i],tmp);
            CommonOps_DDRM.add(R,tmp,R);
        }

        double error = SpecializedOps_DDRM.diffNormF(A,R);

        assertEquals( error , 0 , UtilEjml.TEST_F64);
    }

    @Test
    public void createOrthogonal() {
        for( int numRows = 3; numRows <= 5; numRows++ ) {
            for( int numCols = 1; numCols <= numRows; numCols++ ) {
                DMatrixRMaj Q = RandomMatrices_DDRM.createOrthogonal(numRows,numCols,rand);

                assertEquals(Q.numRows,numRows);
                assertEquals(Q.numCols,numCols);

                assertTrue(CommonOps_DDRM.elementSum(Q) != 0);
                assertTrue(MatrixFeatures_DDRM.isOrthogonal(Q,UtilEjml.TEST_F64));
            }
        }
    }

    @Test
    public void createDiagonal_square() {
        DMatrixRMaj A = RandomMatrices_DDRM.createDiagonal(5,1,10,rand);

        assertTrue(CommonOps_DDRM.elementSum(A) > 5 );
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
        DMatrixRMaj A = RandomMatrices_DDRM.createDiagonal(numRows,numCols,1,10,rand);

        assertEquals(A.getNumRows(), numRows);
        assertEquals(A.getNumCols(), numCols);

        assertTrue(CommonOps_DDRM.elementSum(A) > 5);
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
                DMatrixRMaj A = RandomMatrices_DDRM.createSingularValues(numRows,numCols, rand, sv);

                SingularValueDecompositionD<DMatrixRMaj> svd =
                        DecompositionFactory_DDRM.svd(A.numRows,A.numCols,true,true,false);
                assertTrue(svd.decompose(A));

                int o = Math.min(numRows,numCols);

                UtilTestMatrix.checkSameElements(UtilEjml.TEST_F64,o,sv,svd.getSingularValues());
            }
        }

        // see if it fills in zeros when it is smaller than the dimension
        DMatrixRMaj A = RandomMatrices_DDRM.createSingularValues(5,5, rand, sv);

        SingularValueDecompositionD<DMatrixRMaj> svd =
                DecompositionFactory_DDRM.svd(A.numRows, A.numCols, true, true, false);
        assertTrue(svd.decompose(A));

        UtilTestMatrix.checkSameElements(UtilEjml.TEST_F64, sv.length, sv, svd.getSingularValues());
        assertEquals(0, svd.getSingularValues()[4], UtilEjml.TEST_F64);
    }

    @Test
    public void createEigenvaluesSymm() {
        DMatrixRMaj A = RandomMatrices_DDRM.createEigenvaluesSymm(5,rand,1,2,3,4,5);

        // this should be symmetric
        assertTrue(MatrixFeatures_DDRM.isSymmetric(A,UtilEjml.TEST_F64));

        // decompose the matrix and extract its eigenvalues
        EigenDecompositionD<DMatrixRMaj> eig = DecompositionFactory_DDRM.eig(A.numRows, true);
        assertTrue(eig.decompose(A));

        double ev[] = new double[5];
        for( int i = 0; i < 5; i++ ) {
            ZComplex e = eig.getEigenvalue(i);
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
        DMatrixRMaj A = new DMatrixRMaj(3,4);

        CommonOps_DDRM.fill(A, -2.0);

        RandomMatrices_DDRM.addRandom(A, 1, 2, rand);

        for( int i = 0; i < A.getNumElements(); i++ ) {
            assertTrue(A.get(i) >= -1 && A.get(i) <= 0);
        }
    }

    @Test
    public void createRandom() {
        DMatrixRMaj A = RandomMatrices_DDRM.createRandom(5,4,rand);

        checkRandom1(A);
    }

    @Test
    public void createRandom_min_max() {
        DMatrixRMaj A = RandomMatrices_DDRM.createRandom(30,20,-1,1,rand);

        checkRandomRange(A);
    }

    @Test
    public void setRandom() {
        DMatrixRMaj A = new DMatrixRMaj(5,4);

        RandomMatrices_DDRM.setRandom(A,rand);

        checkRandom1(A);
    }

    private void checkRandom1(DMatrixRMaj a) {
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
        BMatrixRMaj A = new BMatrixRMaj(5,4);

        RandomMatrices_DDRM.setRandomB(A,rand);

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
        DMatrixRMaj A = new DMatrixRMaj(30,20);
        RandomMatrices_DDRM.setRandom(A,-1,1,rand);

        checkRandomRange(A);
    }

    private void checkRandomRange(DMatrixRMaj a) {
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
        DMatrixRMaj A = RandomMatrices_DDRM.createGaussian(30, 20, 2, 0.5, rand);

        checkGaussian(A);
    }

    @Test
    public void setGaussian() {
        DMatrixRMaj A = new DMatrixRMaj(30,20);

        RandomMatrices_DDRM.setGaussian(A, 2, 0.5, rand);

        checkGaussian(A);
    }

    private void checkGaussian(DMatrixRMaj a) {
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
            DMatrixRMaj A = RandomMatrices_DDRM.createSymmPosDef(6+i,rand);

            assertTrue(MatrixFeatures_DDRM.isPositiveDefinite(A));
        }
    }

    @Test
    public void createSymmetric() {
        DMatrixRMaj A = RandomMatrices_DDRM.createSymmetric(10,-1,1,rand);

        assertTrue(MatrixFeatures_DDRM.isSymmetric(A,UtilEjml.TEST_F64));

        // see if it has the expected range of elements
        double min = CommonOps_DDRM.elementMin(A);
        double max = CommonOps_DDRM.elementMax(A);

        assertTrue(min < 0 && min >= -1);
        assertTrue(max > 0 && max <= 1);
    }

    @Test
    public void createUpperTriangle() {
        for( int hess = 0; hess < 3; hess++ ) {
            DMatrixRMaj A = RandomMatrices_DDRM.createUpperTriangle(10,hess,-1,1,rand);

            assertTrue(MatrixFeatures_DDRM.isUpperTriangle(A,hess,UtilEjml.TEST_F64));

            // quick sanity check to make sure it could be proper
            assertTrue(A.get(hess,0) != 0 );

            // see if it has the expected range of elements
            double min = CommonOps_DDRM.elementMin(A);
            double max = CommonOps_DDRM.elementMax(A);

            assertTrue(min < 0 && min >= -1);
            assertTrue(max > 0 && max <= 1);
        }
    }
}
