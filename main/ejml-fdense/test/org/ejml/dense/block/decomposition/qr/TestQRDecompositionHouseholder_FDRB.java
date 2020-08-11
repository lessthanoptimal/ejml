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

package org.ejml.dense.block.decomposition.qr;

import org.junit.jupiter.api.Test;

/**
 * @author Peter Abeles
 */
public class TestQRDecompositionHouseholder_FDRB {

    @Test
    public void generic() {
        QRDecompositionHouseholder_FDRB decomp = new QRDecompositionHouseholder_FDRB();

        GenericBlock64QrDecompositionTests_FDRB tests;
        tests = new GenericBlock64QrDecompositionTests_FDRB(decomp);

        tests.allTests();
    }

    @Test
    public void genericSaveW() {
        QRDecompositionHouseholder_FDRB decomp = new QRDecompositionHouseholder_FDRB();
        decomp.setSaveW(true);

        GenericBlock64QrDecompositionTests_FDRB tests;
        tests = new GenericBlock64QrDecompositionTests_FDRB(decomp);

        tests.allTests();
    }
}
