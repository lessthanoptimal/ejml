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

package org.ejml.sparse.csc.decomposition.chol;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.FMatrixSparseCSC;
import org.ejml.interfaces.decomposition.CholeskySparseDecomposition_F32;
import org.ejml.interfaces.decomposition.DecompositionSparseInterface;
import org.ejml.sparse.csc.CommonOps_FSCC;
import org.ejml.sparse.csc.NormOps_FSCC;
import org.ejml.sparse.csc.RandomMatrices_FSCC;
import org.ejml.sparse.csc.decomposition.GenericDecompositionTests_FSCC;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public abstract class GenericCholeskyTests_FSCC extends GenericDecompositionTests_FSCC {
    boolean canL = true;
    boolean canR = true;

    public abstract CholeskySparseDecomposition_F32<FMatrixSparseCSC> create(boolean lower);

    @Override
    public FMatrixSparseCSC createMatrix(int N) {
        return RandomMatrices_FSCC.symmetricPosDef(N,0.25f,rand);
    }

    @Override
    public DecompositionSparseInterface<FMatrixSparseCSC> createDecomposition() {
        return create(true);
    }

    @Override
    public List<FMatrixSparseCSC> decompose(DecompositionSparseInterface<FMatrixSparseCSC> d, FMatrixSparseCSC A) {
        CholeskySparseDecomposition_F32<FMatrixSparseCSC> chol =
                (CholeskySparseDecomposition_F32<FMatrixSparseCSC>)d;

        assertTrue(chol.decompose(A));

        List<FMatrixSparseCSC> list = new ArrayList<>();
        list.add( chol.getT(null));

        return list;
    }

    /**
     * Test case with a hand constructed matrix
     */
    @Test
    public void checkHandConstructed() {
        if (canL)
            checkHandConstructed(true);
        if (canR)
            checkHandConstructed(false);
    }

    private void checkHandConstructed( boolean lower ) {
        FMatrixSparseCSC A = UtilEjml.parse_FSCC(
                     "1 2  4 " +
                        "2 13 23 "+
                        "4 23 90",3);

        FMatrixSparseCSC T = UtilEjml.parse_FSCC(
                     "1 0 0 " +
                        "2 3 0 "+
                        "4 5 7",3);

        CholeskySparseDecomposition_F32<FMatrixSparseCSC> cholesky = create(lower);

        assertTrue(cholesky.decompose(A));
        assertTrue(lower==cholesky.isLower());

        FMatrixSparseCSC found = cholesky.getT(null);

        if( !lower ) {
            FMatrixSparseCSC L = new FMatrixSparseCSC(3,3,found.nz_length);
            CommonOps_FSCC.transpose(found,L,null);
            found = L;
        }

        EjmlUnitTests.assertEquals(T,found,UtilEjml.TEST_F32);
    }

    /**
     * Test against various randomly generated matrices of different sizes
     */
    @Test
    public void checkMontiCarlo() {
        if (canL)
            checkMontiCarlo(true);
        if (canR)
            checkMontiCarlo(false);
    }

    private void checkMontiCarlo( boolean lower ) {

        CholeskySparseDecomposition_F32<FMatrixSparseCSC> cholesky = create(lower);

        for (int width = 1; width <= 10; width++) {
            for (int mc = 0; mc < 30; mc++) {
                FMatrixSparseCSC A = RandomMatrices_FSCC.symmetricPosDef(width,0.25f,rand);

                float before = NormOps_FSCC.fastNormF(A);

                cholesky.decompose(A);
                FMatrixSparseCSC L = cholesky.getT(null);
                assertTrue(CommonOps_FSCC.checkStructure(L));

                // make sure the input was not modified
                float after = NormOps_FSCC.fastNormF(A);
                assertEquals(before,after,UtilEjml.TEST_F32);


                // check it using the definition on a cholesky decomposition
                FMatrixSparseCSC R = new FMatrixSparseCSC(L.numRows,L.numCols,L.nz_length);
                CommonOps_FSCC.transpose(L,R,null);

                FMatrixSparseCSC found = new FMatrixSparseCSC(L.numRows,L.numCols,0);
                if( lower )
                    CommonOps_FSCC.mult(L,R,found);
                else
                    CommonOps_FSCC.mult(R,L,found);

                EjmlUnitTests.assertEquals(A,found, UtilEjml.TEST_F32);
            }
        }
    }


    /**
     * If it is not positive definate it should fail
     */
    @Test
    public void testNotPositiveDefinite() {
        FMatrixSparseCSC A = UtilEjml.parse_FSCC(
                     "1 -1 " +
                        "-1 -2 ",2);

        CholeskySparseDecomposition_F32<FMatrixSparseCSC> alg = create(true);
        assertFalse(alg.decompose(A));
    }

    /**
     * The correctness of getT(null) has been tested else where effectively.  This
     * checks to see if it handles the case where an input is provided correctly.
     */
    @Test
    public void getT() {
        FMatrixSparseCSC A = UtilEjml.parse_FSCC(
                "1 2  4 " +
                        "2 13 23 " +
                        "4 23 90", 3);

        CholeskySparseDecomposition_F32<FMatrixSparseCSC> cholesky = create(true);

        assertTrue(cholesky.decompose(A));

        FMatrixSparseCSC L_null = cholesky.getT(null);
        FMatrixSparseCSC L_provided = RandomMatrices_FSCC.rectangle(3, 3, 7, rand);
        assertTrue(L_provided == cholesky.getT(L_provided));

        EjmlUnitTests.assertEquals(L_null, L_provided, UtilEjml.TEST_F32);
    }

    @Test
    public void checkDeterminant() {
        if (canL)
            checkDeterminant(true);
        if (canR)
                checkDeterminant(false);
    }

    private void checkDeterminant( boolean lower) {
        FMatrixSparseCSC A = UtilEjml.parse_FSCC(
                     "1 2  4 " +
                        "2 13 23 "+
                        "4 23 90",3);

        CholeskySparseDecomposition_F32<FMatrixSparseCSC> cholesky = create(lower);

        assertTrue(cholesky.decompose(A));

        // computed using Cctave
        assertEquals(441,cholesky.computeDeterminant().real, UtilEjml.TEST_F32);
    }
}
