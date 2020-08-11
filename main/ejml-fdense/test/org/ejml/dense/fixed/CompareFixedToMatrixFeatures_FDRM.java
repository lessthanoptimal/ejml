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

package org.ejml.dense.fixed;

import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.junit.jupiter.api.Test;

/**
 * @author Peter Abeles
 */
// TODO this should be improved by creating custom matrices for many of these functions.
public abstract class CompareFixedToMatrixFeatures_FDRM extends CompareFixed_FDRM {

    public CompareFixedToMatrixFeatures_FDRM(Class classFixed) {
        super(classFixed, MatrixFeatures_FDRM.class);
    }

    /**
     * Compares equivalent functions in FixedOps to CommonOps.  Inputs are randomly generated
     */
    @Test
    public void compareToCommonOps() {
        compareToCommonOps(4,0);
    }
}
