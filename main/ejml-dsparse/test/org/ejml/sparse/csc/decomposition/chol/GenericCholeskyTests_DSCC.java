/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.interfaces.decomposition.CholeskySparseDecomposition_F64;
import org.ejml.interfaces.decomposition.DecompositionSparseInterface;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.NormOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.ejml.sparse.csc.decomposition.GenericDecompositionTests_DSCC;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public abstract class GenericCholeskyTests_DSCC extends GenericDecompositionTests_DSCC {
    boolean canL = true;
    boolean canR = true;

    public abstract CholeskySparseDecomposition_F64<DMatrixSparseCSC> create(boolean lower);

    @Override
    public DMatrixSparseCSC createMatrix(int N) {
        return RandomMatrices_DSCC.symmetricPosDef(N,0.25,rand);
    }

    @Override
    public DecompositionSparseInterface<DMatrixSparseCSC> createDecomposition() {
        return create(true);
    }

    @Override
    public List<DMatrixSparseCSC> decompose(DecompositionSparseInterface<DMatrixSparseCSC> d, DMatrixSparseCSC A) {
        CholeskySparseDecomposition_F64<DMatrixSparseCSC> chol =
                (CholeskySparseDecomposition_F64<DMatrixSparseCSC>)d;

        assertTrue(chol.decompose(A));

        List<DMatrixSparseCSC> list = new ArrayList<>();
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
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                     "1 2  4 " +
                        "2 13 23 "+
                        "4 23 90",3);

        DMatrixSparseCSC T = UtilEjml.parse_DSCC(
                     "1 0 0 " +
                        "2 3 0 "+
                        "4 5 7",3);

        CholeskySparseDecomposition_F64<DMatrixSparseCSC> cholesky = create(lower);

        assertTrue(cholesky.decompose(A));
        assertTrue(lower==cholesky.isLower());

        DMatrixSparseCSC found = cholesky.getT(null);

        if( !lower ) {
            DMatrixSparseCSC L = new DMatrixSparseCSC(3,3,found.nz_length);
            CommonOps_DSCC.transpose(found,L,null);
            found = L;
        }

        EjmlUnitTests.assertEquals(T,found,UtilEjml.TEST_F64);
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

        CholeskySparseDecomposition_F64<DMatrixSparseCSC> cholesky = create(lower);

        for (int width = 1; width <= 10; width++) {
            for (int mc = 0; mc < 30; mc++) {
                DMatrixSparseCSC A = RandomMatrices_DSCC.symmetricPosDef(width,0.25,rand);

                double before = NormOps_DSCC.fastNormF(A);

                cholesky.decompose(A);
                DMatrixSparseCSC L = cholesky.getT(null);
                assertTrue(CommonOps_DSCC.checkStructure(L));

                // make sure the input was not modified
                double after = NormOps_DSCC.fastNormF(A);
                assertEquals(before,after,UtilEjml.TEST_F64);


                // check it using the definition on a cholesky decomposition
                DMatrixSparseCSC R = new DMatrixSparseCSC(L.numRows,L.numCols,L.nz_length);
                CommonOps_DSCC.transpose(L,R,null);

                DMatrixSparseCSC found = new DMatrixSparseCSC(L.numRows,L.numCols,0);
                if( lower )
                    CommonOps_DSCC.mult(L,R,found);
                else
                    CommonOps_DSCC.mult(R,L,found);

                EjmlUnitTests.assertEquals(A,found, UtilEjml.TEST_F64);
            }
        }
    }


    /**
     * If it is not positive definate it should fail
     */
    @Test
    public void testNotPositiveDefinite() {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                     "1 -1 " +
                        "-1 -2 ",2);

        CholeskySparseDecomposition_F64<DMatrixSparseCSC> alg = create(true);
        assertFalse(alg.decompose(A));
    }

    /**
     * The correctness of getT(null) has been tested else where effectively. This
     * checks to see if it handles the case where an input is provided correctly.
     */
    @Test
    public void getT() {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                "1 2  4 " +
                        "2 13 23 " +
                        "4 23 90", 3);

        CholeskySparseDecomposition_F64<DMatrixSparseCSC> cholesky = create(true);

        assertTrue(cholesky.decompose(A));

        DMatrixSparseCSC L_null = cholesky.getT(null);
        DMatrixSparseCSC L_provided = RandomMatrices_DSCC.rectangle(3, 3, 7, rand);
        assertTrue(L_provided == cholesky.getT(L_provided));

        EjmlUnitTests.assertEquals(L_null, L_provided, UtilEjml.TEST_F64);
    }

    @Test
    public void checkDeterminant() {
        if (canL)
            checkDeterminant(true);
        if (canR)
                checkDeterminant(false);
    }

    private void checkDeterminant( boolean lower) {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                     "1 2  4 " +
                        "2 13 23 "+
                        "4 23 90",3);

        CholeskySparseDecomposition_F64<DMatrixSparseCSC> cholesky = create(lower);

        assertTrue(cholesky.decompose(A));

        // computed using Cctave
        assertEquals(441,cholesky.computeDeterminant().real, UtilEjml.TEST_F64);
    }
}
