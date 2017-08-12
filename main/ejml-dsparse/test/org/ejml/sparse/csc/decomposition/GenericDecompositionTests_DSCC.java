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

package org.ejml.sparse.csc.decomposition;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.DecompositionSparseInterface;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Generic tests for sparse decomposition interface
 *
 * @author Peter Abeles
 */
public abstract class GenericDecompositionTests_DSCC {
    public boolean canLockStructure = true;

    /**
     * Create a matrix which can be decomposed.
     */
    public abstract DMatrixSparseCSC createMatrix( int N );

    /**
     * Create a decomposition
     */
    public abstract DecompositionSparseInterface<DMatrixSparseCSC> createDecomposition();

    /**
     * Decompose the matrix and store the results in a list
     */
    public abstract List<DMatrixSparseCSC> decompose( DecompositionSparseInterface<DMatrixSparseCSC> d ,
                                                      DMatrixSparseCSC A );

    @Test
    public void ifCanNotLockThrowException() {
        DecompositionSparseInterface<DMatrixSparseCSC> d = createDecomposition();
        if( canLockStructure ) {
            d.lockStructure();
        } else {
            try {
                d.lockStructure();
                fail("RuntimeException should have been thrown");
            } catch (RuntimeException ignore) {
            }
        }
    }

    @Test
    public void lockingDoesNotChangeSolution() {
        if( !canLockStructure )
            return;

        DecompositionSparseInterface<DMatrixSparseCSC> d = createDecomposition();

        DMatrixSparseCSC A = createMatrix(10);
        List<DMatrixSparseCSC> sol0 = decompose(d,(DMatrixSparseCSC)A.copy());

        assertFalse(d.isStructureLocked());
        d.lockStructure();
        assertTrue(d.isStructureLocked());

        List<DMatrixSparseCSC> sol1 = decompose(d,A);

        assertEquals(sol0.size(),sol1.size());
        for (int i = 0; i < sol0.size(); i++) {
            EjmlUnitTests.assertEquals(sol0.get(i),sol1.get(i), UtilEjml.TEST_F64);
        }
    }
}
