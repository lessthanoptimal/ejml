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

import org.ejml.data.FMatrixSparseCSC;
import org.ejml.interfaces.decomposition.LUSparseDecomposition_F32;
import org.ejml.sparse.ComputePermutation;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.factory.FillReductionFactory_FSCC;

/**
 * @author Peter Abeles
 */
public class TestLuUpLooking_FSCC extends GenericLuTests_FSCC {
    public TestLuUpLooking_FSCC() {
        this.canLockStructure = false;
    }

    @Override
    public LUSparseDecomposition_F32<FMatrixSparseCSC> create(FillReducing permutation) {
        ComputePermutation<FMatrixSparseCSC> cp = FillReductionFactory_FSCC.create(permutation);
        return new LuUpLooking_FSCC(cp);
    }
}