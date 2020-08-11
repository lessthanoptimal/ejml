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
import org.ejml.data.BMatrixRMaj;
import org.ejml.data.Complex_F32;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.UtilTestMatrix;
import org.ejml.dense.row.factory.DecompositionFactory_FDRM;
import org.ejml.dense.row.mult.VectorVectorMult_FDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition_F32;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F32;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public class TestRandomMatrices_FDRM {

    Random rand = new Random(48757);

    /**
     * Checks to see if all the vectors are orthogonal and of unit length.
     */
    @Test
    public void span() {
        // test with combinations of vectors and numbers
        for( int dimension = 3; dimension <= 5; dimension++ ) {
            for( int numVectors = 1; numVectors <= dimension; numVectors++ ) {
                FMatrixRMaj span[] = RandomMatrices_FDRM.span(dimension,numVectors,rand);

                assertEquals(numVectors,span.length);

                for( int i = 0; i < span.length; i++ ) {
                    FMatrixRMaj a = span[i];

                    assertEquals(1, NormOps_FDRM.fastNormF(a), UtilEjml.TEST_F32);

                    for( int j = i+1; j < span.length; j++ ) {
                        float dot = VectorVectorMult_FDRM.innerProd(a,span[j]);
                        assertEquals(0,dot,UtilEjml.TEST_F32);
                    }
                }
            }
        }
    }

    @Test
    public void insideSpan() {
        FMatrixRMaj span[] = RandomMatrices_FDRM.span(5,5,rand);

        FMatrixRMaj A = RandomMatrices_FDRM.insideSpan(span,-1,1,rand);

        // reconstructed matrix
        FMatrixRMaj R = new FMatrixRMaj(A.numRows,A.numCols);

        FMatrixRMaj tmp = new FMatrixRMaj(A.numRows,A.numCols);

        // project the matrix into the span and recreate the original matrix
        for( int i = 0; i < 5; i++ ) {
            float val =  VectorVectorMult_FDRM.innerProd(span[i],A);
            assertTrue( Math.abs(val) > 1e-10 );

            CommonOps_FDRM.scale(val,span[i],tmp);
            CommonOps_FDRM.add(R,tmp,R);
        }

        float error = SpecializedOps_FDRM.diffNormF(A,R);

        assertEquals( error , 0 , UtilEjml.TEST_F32);
    }

    @Test
    public void orthogonal() {
        for( int numRows = 3; numRows <= 5; numRows++ ) {
            for( int numCols = 1; numCols <= numRows; numCols++ ) {
                FMatrixRMaj Q = RandomMatrices_FDRM.orthogonal(numRows,numCols,rand);

                assertEquals(Q.numRows,numRows);
                assertEquals(Q.numCols,numCols);

                assertTrue(CommonOps_FDRM.elementSum(Q) != 0);
                assertTrue(MatrixFeatures_FDRM.isOrthogonal(Q,UtilEjml.TEST_F32));
            }
        }
    }

    @Test
    public void diagonal_square() {
        FMatrixRMaj A = RandomMatrices_FDRM.diagonal(5,1,10,rand);

        assertTrue(CommonOps_FDRM.elementSum(A) > 5 );
        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < A.numCols; j++ ) {
                float v = A.get(i, j);

                if( i == j ) {
                    assertTrue(v >= 1 || v <= 10);
                } else {
                    assertTrue(v == 0);
                }
            }
        }
    }

    @Test
    public void diagonal_general() {
        testDiagonal(5,3);
        testDiagonal(3,5);
        testDiagonal(3, 3);
    }

    public void testDiagonal( int numRows , int numCols ) {
        FMatrixRMaj A = RandomMatrices_FDRM.diagonal(numRows,numCols,1,10,rand);

        assertEquals(A.getNumRows(), numRows);
        assertEquals(A.getNumCols(), numCols);

        assertTrue(CommonOps_FDRM.elementSum(A) > 5);
        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < A.numCols; j++ ) {
                float v = A.get(i,j);

                if( i == j ) {
                    assertTrue(v >= 1 || v <= 10);
                } else {
                    assertTrue(v == 0);
                }
            }
        }
    }

    @Test
    public void singleValues() {
        // check case when sv is more than or equal to the matrix dimension
        float sv[] = new float[]{8.2f,6.2f,4.1f,2};

        for( int numRows = 1; numRows <= 4; numRows++ ) {
            for( int numCols = 1; numCols <= 4; numCols++ ) {
                FMatrixRMaj A = RandomMatrices_FDRM.singular(numRows,numCols, rand, sv);

                SingularValueDecomposition_F32<FMatrixRMaj> svd =
                        DecompositionFactory_FDRM.svd(A.numRows,A.numCols,true,true,false);
                assertTrue(svd.decompose(A));

                int o = Math.min(numRows,numCols);

                UtilTestMatrix.checkSameElements(UtilEjml.TEST_F32,o,sv,svd.getSingularValues());
            }
        }

        // see if it fills in zeros when it is smaller than the dimension
        FMatrixRMaj A = RandomMatrices_FDRM.singular(5,5, rand, sv);

        SingularValueDecomposition_F32<FMatrixRMaj> svd =
                DecompositionFactory_FDRM.svd(A.numRows, A.numCols, true, true, false);
        assertTrue(svd.decompose(A));

        UtilTestMatrix.checkSameElements(UtilEjml.TEST_F32, sv.length, sv, svd.getSingularValues());
        assertEquals(0, svd.getSingularValues()[4], UtilEjml.TEST_F32);
    }

    @Test
    public void symmetricWithEigenvalues() {
        FMatrixRMaj A = RandomMatrices_FDRM.symmetricWithEigenvalues(5,rand,1,2,3,4,5);

        // this should be symmetric
        assertTrue(MatrixFeatures_FDRM.isSymmetric(A,UtilEjml.TEST_F32));

        // decompose the matrix and extract its eigenvalues
        EigenDecomposition_F32<FMatrixRMaj> eig = DecompositionFactory_FDRM.eig(A.numRows, true);
        assertTrue(eig.decompose(A));

        float ev[] = new float[5];
        for( int i = 0; i < 5; i++ ) {
            Complex_F32 e = eig.getEigenvalue(i);
            assertTrue(e.isReal());

            ev[i] = e.real;
        }

        // need to sort the eigenvalues so that I know where they are in the array
        Arrays.sort(ev);

        // see if they are what I expected them to be
        for( int i = 0; i < ev.length; i++ ) {
            assertEquals(i+1.0f,ev[i],UtilEjml.TEST_F32);
        }
    }

    @Test
    public void addUniform() {
        FMatrixRMaj A = new FMatrixRMaj(3,4);

        CommonOps_FDRM.fill(A, -2.0f);

        RandomMatrices_FDRM.addUniform(A, 1, 2, rand);

        for( int i = 0; i < A.getNumElements(); i++ ) {
            assertTrue(A.get(i) >= -1 && A.get(i) <= 0);
        }
    }

    @Test
    public void rectangle() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,4,rand);

        fillUniform1(A);
    }

    @Test
    public void rectangle_min_max() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(30,20,-1,1,rand);

        checkRandomRange(A);
    }

    @Test
    public void fillUniform() {
        FMatrixRMaj A = new FMatrixRMaj(5,4);

        RandomMatrices_FDRM.fillUniform(A,rand);

        fillUniform1(A);
    }

    private void fillUniform1(FMatrixRMaj a) {
        assertEquals(5, a.numRows);
        assertEquals(4, a.numCols);

        float total = 0;
        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < a.numCols; j++ ) {
                float val = a.get(i,j);

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

        RandomMatrices_FDRM.setRandomB(A,rand);

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
    public void fillUniform_min_max() {
        FMatrixRMaj A = new FMatrixRMaj(30,20);
        RandomMatrices_FDRM.fillUniform(A,-1,1,rand);

        checkRandomRange(A);
    }

    private void checkRandomRange(FMatrixRMaj a) {
        assertEquals(30, a.numRows);
        assertEquals(20, a.numCols);

        int numNeg = 0;
        int numPos = 0;
        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < a.numCols; j++ ) {
                float val = a.get(i,j);

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
    public void rectangleGaussian() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangleGaussian(30, 20, 2, 0.5f, rand);

        checkGaussian(A);
    }

    @Test
    public void fillGaussian() {
        FMatrixRMaj A = new FMatrixRMaj(30,20);

        RandomMatrices_FDRM.fillGaussian(A, 2, 0.5f, rand);

        checkGaussian(A);
    }

    private void checkGaussian(FMatrixRMaj a) {
        assertEquals(30, a.numRows);
        assertEquals(20, a.numCols);

        int numNeg = 0;
        int numPos = 0;
        float mean = 0;
        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < a.numCols; j++ ) {
                float val = a.get(i,j);

                if( val-2 < 0 )
                    numNeg++;
                else
                    numPos++;

                mean += val;
            }
        }

        mean /= a.numRows*a.numCols;

        assertEquals(mean,2,0.01f);

        assertTrue(numNeg>0);
        assertTrue(numPos>0);
    }

    @Test
    public void symmetricPosDef() {
        for( int i = 0; i < 10; i++ ) {
            FMatrixRMaj A = RandomMatrices_FDRM.symmetricPosDef(6+i,rand);

            assertTrue(MatrixFeatures_FDRM.isPositiveDefinite(A));
        }
    }

    @Test
    public void symmetric() {
        FMatrixRMaj A = RandomMatrices_FDRM.symmetric(10,-1,1,rand);

        assertTrue(MatrixFeatures_FDRM.isSymmetric(A,UtilEjml.TEST_F32));

        // see if it has the expected range of elements
        float min = CommonOps_FDRM.elementMin(A);
        float max = CommonOps_FDRM.elementMax(A);

        assertTrue(min < 0 && min >= -1);
        assertTrue(max > 0 && max <= 1);
    }

    @Test
    public void triangularUpper() {
        for( int hess = 0; hess < 3; hess++ ) {
            FMatrixRMaj A = RandomMatrices_FDRM.triangularUpper(10,hess,-1,1,rand);

            assertTrue(MatrixFeatures_FDRM.isUpperTriangle(A,hess,UtilEjml.TEST_F32));

            // quick sanity check to make sure it could be proper
            assertTrue(A.get(hess,0) != 0 );

            // see if it has the expected range of elements
            float min = CommonOps_FDRM.elementMin(A);
            float max = CommonOps_FDRM.elementMax(A);

            assertTrue(min < 0 && min >= -1);
            assertTrue(max > 0 && max <= 1);
        }
    }

    @Test
    public void triangularLower() {
        for( int hess = 0; hess < 3; hess++ ) {
            FMatrixRMaj A = RandomMatrices_FDRM.triangularLower(10,hess,-1,1,rand);
            assertTrue(MatrixFeatures_FDRM.isLowerTriangle(A,hess,UtilEjml.TEST_F32));

            // quick sanity check to make sure it could be proper
            assertTrue(A.get(hess,0) != 0 );

            // see if it has the expected range of elements
            float min = CommonOps_FDRM.elementMin(A);
            float max = CommonOps_FDRM.elementMax(A);

            assertTrue(min < 0 && min >= -1);
            assertTrue(max > 0 && max <= 1);
        }
    }
}
