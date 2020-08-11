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

package org.ejml.sparse.csc.decomposition.lu;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.FMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.interfaces.decomposition.DecompositionSparseInterface;
import org.ejml.interfaces.decomposition.LUSparseDecomposition_F32;
import org.ejml.ops.ConvertFMatrixStruct;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.CommonOps_FSCC;
import org.ejml.sparse.csc.RandomMatrices_FSCC;
import org.ejml.sparse.csc.decomposition.GenericDecompositionTests_FSCC;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public abstract class GenericLuTests_FSCC extends GenericDecompositionTests_FSCC {

    private FillReducing permTests[] =
            new FillReducing[]{FillReducing.NONE, FillReducing.IDENTITY};

    public abstract LUSparseDecomposition_F32<FMatrixSparseCSC> create(FillReducing permutation );

    @Override
    public FMatrixSparseCSC createMatrix(int N) {
        return RandomMatrices_FSCC.symmetricPosDef(N,0.25f,rand);
    }

    @Override
    public DecompositionSparseInterface<FMatrixSparseCSC> createDecomposition() {
        return create(FillReducing.NONE);
    }

    @Override
    public List<FMatrixSparseCSC> decompose(DecompositionSparseInterface<FMatrixSparseCSC> d, FMatrixSparseCSC A) {
        LUSparseDecomposition_F32<FMatrixSparseCSC> lu = (LUSparseDecomposition_F32<FMatrixSparseCSC>)d;

        assertTrue(lu.decompose(A));

        List<FMatrixSparseCSC> list = new ArrayList<>();
        list.add( lu.getLower(null));
        list.add( lu.getUpper(null));

        return list;
    }

    @Test
    public void checkHandConstructed() {
        for( FillReducing p : permTests) {
            checkHandConstructed(p);
        }
    }

    private void checkHandConstructed(FillReducing perm) {
        FMatrixSparseCSC A = UtilEjml.parse_FSCC(
                     "1 2  4 " +
                        "2 13 23 "+
                        "4 23 90",3);

        LUSparseDecomposition_F32<FMatrixSparseCSC> lu = create(perm);

        checkSolution(A, lu);
    }

    private void checkSolution(FMatrixSparseCSC A, LUSparseDecomposition_F32<FMatrixSparseCSC> lu) {

        FMatrixSparseCSC Acpy = A.copy();
        assertTrue(lu.decompose(A));
        assertFalse(lu.isSingular());

        if( !lu.inputModified() ) {
            EjmlUnitTests.assertEquals(A,Acpy, UtilEjml.TEST_F32);
        }

        FMatrixSparseCSC L = lu.getLower(null);
        FMatrixSparseCSC U = lu.getUpper(null);
        FMatrixSparseCSC P = lu.getRowPivot(null);

        FMatrixSparseCSC PL = new FMatrixSparseCSC(P.numRows,L.numCols,0);
        CommonOps_FSCC.multTransA(P,L,PL,null,null);
        FMatrixSparseCSC found = new FMatrixSparseCSC(PL.numCols,U.numCols,0);
        CommonOps_FSCC.mult(PL,U,found);

        EjmlUnitTests.assertEquals(Acpy,found,UtilEjml.TEST_F32);
    }

    @Test
    public void checkMontiCarlo() {
        for( FillReducing p : permTests) {
            checkMontiCarlo(p);
        }
    }

    private void checkMontiCarlo( FillReducing perm ) {

        LUSparseDecomposition_F32<FMatrixSparseCSC> lu = create(perm);

        for (int width = 1; width <= 10; width++) {
            for (int mc = 0; mc < 30; mc++) {
                int nz = (int)(width*width*(rand.nextFloat()*0.5f+0.02f));
                FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(width,width,nz,rand);
                RandomMatrices_FSCC.ensureNotSingular(A,rand);
                checkSolution(A, lu);
            }
        }
    }

    @Test
    public void testSingular() {
        for( FillReducing p : permTests) {
            testSingular(p);
        }
    }

    private void testSingular(FillReducing perm) {
        FMatrixSparseCSC A = UtilEjml.parse_FSCC(
                "1 4  3 " +
                        "5 0 9 " +
                        "5 0 9", 3);

        LUSparseDecomposition_F32<FMatrixSparseCSC> lu = create(perm);

        if(lu.decompose(A)) {
            assertTrue(lu.isSingular());
        }
    }


    @Test
    public void getL_U_P_withMatrix() {
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(6,6,30,rand);
        FMatrixSparseCSC L = RandomMatrices_FSCC.rectangle(4,3,30,rand);
        FMatrixSparseCSC U = RandomMatrices_FSCC.rectangle(8,2,30,rand);
        FMatrixSparseCSC P = RandomMatrices_FSCC.rectangle(8,9,30,rand);

        for( FillReducing perm : permTests) {
            LUSparseDecomposition_F32<FMatrixSparseCSC> lu = create(perm);

            assertTrue(lu.decompose(A));

            lu.getLower(L);
            lu.getUpper(U);
            lu.getRowPivot(P);

            assertTrue(CommonOps_FSCC.checkStructure(L));
            assertTrue(CommonOps_FSCC.checkStructure(U));
            assertTrue(CommonOps_FSCC.checkStructure(P));

            assertTrue(L.numCols==6 && L.numRows == 6);
            assertTrue(U.numCols==6 && U.numRows == 6);
            assertTrue(P.numCols==6 && P.numRows == 6 && P.nz_length == 6);
        }
    }

    @Test
    public void checkDeterminant() {
        for( FillReducing p : permTests) {
            checkDeterminant(p);
            checkDeterminantToDense(p);
        }
    }

    private void checkDeterminant(FillReducing perm ) {
        FMatrixSparseCSC A = UtilEjml.parse_FSCC(
                "1 4  3 " +
                        "5 0 9 "+
                        "2 2 2",3);

        LUSparseDecomposition_F32<FMatrixSparseCSC> lu = create(perm);

        assertTrue(lu.decompose(A));

        // computed using Octave
        assertEquals(44,lu.computeDeterminant().real, UtilEjml.TEST_F32);
    }

    private void checkDeterminantToDense(FillReducing perm ) {
        for (int trial = 0; trial < 60; trial++) {
            int N = rand.nextInt(10)+1;

            FMatrixRMaj A = RandomMatrices_FDRM.rectangle(N,N,rand);
            FMatrixSparseCSC A_sp = ConvertFMatrixStruct.convert(A,(FMatrixSparseCSC)null,UtilEjml.F_EPS);

            LUSparseDecomposition_F32<FMatrixSparseCSC> lu_sparse = create(perm);

            assertTrue(lu_sparse.decompose(A_sp));

            float expected = CommonOps_FDRM.det(A);
            float found = lu_sparse.computeDeterminant().real;

            assertEquals(expected,found,UtilEjml.TEST_F32);

        }
    }

//    @Disabled
//    @Test
//    public void testTall() {
//        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(5,4,10,rand);
//        RandomMatrices_FSCC.ensureNotSingular(A,rand);
//
//        LUSparseDecomposition_F32<FMatrixSparseCSC> alg = create(FillReducing.NONE);
//
//        checkSolution(A,alg);
//    }
//
//    @Disabled
//    @Test
//    public void testFat() {
//        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(4,5,10,rand);
//        RandomMatrices_FSCC.ensureNotSingular(A,rand);
//
//        LUSparseDecomposition_F32<FMatrixSparseCSC> alg = create(FillReducing.NONE);
//
//        checkSolution(A,alg);
//    }

    @Test
    public void testRowPivotVector() {
        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(4,4,10,rand);
        RandomMatrices_FSCC.ensureNotSingular(A,rand);
        LUSparseDecomposition_F32<FMatrixSparseCSC> alg = create(FillReducing.NONE);

        assertTrue(alg.decompose(A));

        int []pivot = alg.getRowPivotV(null);
        FMatrixSparseCSC P = alg.getRowPivot(null);

        for (int i = 0; i < A.numRows; i++) {
            assertEquals(1,(int)P.get(i,pivot[i]));
        }
    }
}
