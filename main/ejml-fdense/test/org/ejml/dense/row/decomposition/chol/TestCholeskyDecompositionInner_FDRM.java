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

package org.ejml.dense.row.decomposition.chol;

import org.ejml.data.FMatrixRMaj;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F32;
import org.junit.jupiter.api.Test;

import static org.ejml.dense.row.decomposition.CheckDecompositionInterface_FDRM.checkModifiedInput;


/**
 * @author Peter Abeles
 */
public class TestCholeskyDecompositionInner_FDRM extends GenericCholeskyTests_FDRM {

    @Override
    public CholeskyDecomposition_F32<FMatrixRMaj> create(boolean lower) {
        return new CholeskyDecompositionInner_FDRM(lower);
    }

    @Test
    public void checkModifyInput() {
        checkModifiedInput(new CholeskyDecompositionInner_FDRM(true));
        checkModifiedInput(new CholeskyDecompositionInner_FDRM(false));
    }
}
