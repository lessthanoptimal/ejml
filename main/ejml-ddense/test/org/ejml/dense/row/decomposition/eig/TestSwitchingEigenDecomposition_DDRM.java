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

package org.ejml.dense.row.decomposition.eig;

import org.ejml.UtilEjml;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;
import org.junit.jupiter.api.Test;


/**
 * @author Peter Abeles
 */
public class TestSwitchingEigenDecomposition_DDRM extends GeneralEigenDecompositionCheck_DDRM {
    @Override
    public EigenDecomposition_F64 createDecomposition() {
        return new SwitchingEigenDecomposition_DDRM(0,computeVectors, UtilEjml.TEST_F64);
    }

    @Test
    public void allTests() {
        super.allTests();
//        super.justEigenValues();
    }
}