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

package org.ejml.sparse.csc.decomposition.lu;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.interfaces.decomposition.DecompositionSparseInterface;
import org.ejml.interfaces.decomposition.LUSparseDecomposition_F64;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.ejml.sparse.csc.decomposition.GenericDecompositionTests_DSCC;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public abstract class GenericLuTests_DSCC extends GenericDecompositionTests_DSCC {

    private FillReducing permTests[] =
            new FillReducing[]{FillReducing.NONE, FillReducing.IDENTITY};

    public abstract LUSparseDecomposition_F64<DMatrixSparseCSC> create(FillReducing permutation );

    @Override
    public DMatrixSparseCSC createMatrix(int N) {
        return RandomMatrices_DSCC.symmetricPosDef(N,N/3+1,rand);
    }

    @Override
    public DecompositionSparseInterface<DMatrixSparseCSC> createDecomposition() {
        return create(FillReducing.NONE);
    }

    @Override
    public List<DMatrixSparseCSC> decompose(DecompositionSparseInterface<DMatrixSparseCSC> d, DMatrixSparseCSC A) {
        LUSparseDecomposition_F64<DMatrixSparseCSC> lu = (LUSparseDecomposition_F64<DMatrixSparseCSC>)d;

        assertTrue(lu.decompose(A));

        List<DMatrixSparseCSC> list = new ArrayList<>();
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
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                     "1 2  4 " +
                        "2 13 23 "+
                        "4 23 90",3);

        LUSparseDecomposition_F64<DMatrixSparseCSC> lu = create(perm);

        checkSolution(A, lu);
    }

    private void checkSolution(DMatrixSparseCSC A, LUSparseDecomposition_F64<DMatrixSparseCSC> lu) {

        DMatrixSparseCSC Acpy = A.copy();
        assertTrue(lu.decompose(A));
        assertFalse(lu.isSingular());

        if( !lu.inputModified() ) {
            EjmlUnitTests.assertEquals(A,Acpy, UtilEjml.TEST_F64);
        }

        DMatrixSparseCSC L = lu.getLower(null);
        DMatrixSparseCSC U = lu.getUpper(null);
        DMatrixSparseCSC P = lu.getRowPivot(null);

        DMatrixSparseCSC PL = new DMatrixSparseCSC(P.numRows,L.numCols,0);
        CommonOps_DSCC.multTransA(P,L,PL,null,null);
        DMatrixSparseCSC found = new DMatrixSparseCSC(PL.numCols,U.numCols,0);
        CommonOps_DSCC.mult(PL,U,found);

        EjmlUnitTests.assertEquals(Acpy,found,UtilEjml.TEST_F64);
    }

    @Test
    public void checkMontiCarlo() {
        for( FillReducing p : permTests) {
            checkMontiCarlo(p);
        }
    }

    private void checkMontiCarlo( FillReducing perm ) {

        LUSparseDecomposition_F64<DMatrixSparseCSC> lu = create(perm);

        for (int width = 1; width <= 10; width++) {
            for (int mc = 0; mc < 30; mc++) {
                int nz = (int)(width*width*(rand.nextDouble()*0.5+0.02));
                DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(width,width,nz,rand);
                RandomMatrices_DSCC.ensureNotSingular(A,rand);
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
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                "1 4  3 " +
                        "5 0 9 " +
                        "5 0 9", 3);

        LUSparseDecomposition_F64<DMatrixSparseCSC> lu = create(perm);

        if(lu.decompose(A)) {
            assertTrue(lu.isSingular());
        }
    }


    @Test
    public void getL_U_P_withMatrix() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(6,6,30,rand);
        DMatrixSparseCSC L = RandomMatrices_DSCC.rectangle(4,3,30,rand);
        DMatrixSparseCSC U = RandomMatrices_DSCC.rectangle(8,2,30,rand);
        DMatrixSparseCSC P = RandomMatrices_DSCC.rectangle(8,9,30,rand);

        for( FillReducing perm : permTests) {
            LUSparseDecomposition_F64<DMatrixSparseCSC> lu = create(perm);

            assertTrue(lu.decompose(A));

            lu.getLower(L);
            lu.getUpper(U);
            lu.getRowPivot(P);

            assertTrue(CommonOps_DSCC.checkStructure(L));
            assertTrue(CommonOps_DSCC.checkStructure(U));
            assertTrue(CommonOps_DSCC.checkStructure(P));

            assertTrue(L.numCols==6 && L.numRows == 6);
            assertTrue(U.numCols==6 && U.numRows == 6);
            assertTrue(P.numCols==6 && P.numRows == 6 && P.nz_length == 6);
        }
    }

    @Test
    public void checkDeterminant() {
        for( FillReducing p : permTests) {
            checkDeterminant(p);
        }
    }

    private void checkDeterminant(FillReducing perm ) {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                "1 4  3 " +
                        "5 0 9 "+
                        "2 2 2",3);

        LUSparseDecomposition_F64<DMatrixSparseCSC> lu = create(perm);

        assertTrue(lu.decompose(A));

        // computed using Cctave
        assertEquals(44,lu.computeDeterminant().real, UtilEjml.TEST_F64);
    }

    @Test
    public void testTall() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(5,4,10,rand);
        RandomMatrices_DSCC.ensureNotSingular(A,rand);

        LUSparseDecomposition_F64<DMatrixSparseCSC> alg = create(FillReducing.NONE);

        checkSolution(A,alg);
    }

    @Test
    public void testFat() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(4,5,10,rand);
        RandomMatrices_DSCC.ensureNotSingular(A,rand);

        LUSparseDecomposition_F64<DMatrixSparseCSC> alg = create(FillReducing.NONE);

        checkSolution(A,alg);
    }

    @Test
    public void testRowPivotVector() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(4,5,10,rand);
        RandomMatrices_DSCC.ensureNotSingular(A,rand);
        LUSparseDecomposition_F64<DMatrixSparseCSC> alg = create(FillReducing.NONE);

        assertTrue(alg.decompose(A));

        int []pivot = alg.getRowPivotV(null);
        DMatrixSparseCSC P = alg.getRowPivot(null);

        for (int i = 0; i < A.numRows; i++) {
            assertEquals(1,(int)P.get(i,pivot[i]));
        }
    }

}
