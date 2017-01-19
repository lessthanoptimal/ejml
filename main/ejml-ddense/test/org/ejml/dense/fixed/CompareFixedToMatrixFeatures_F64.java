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

package org.ejml.dense.fixed;

import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.junit.Test;

/**
 * @author Peter Abeles
 */
// TODO this should be improved by creating custom matrices for many of these functions.
public abstract class CompareFixedToMatrixFeatures_F64 extends CompareFixed_F64 {

    public CompareFixedToMatrixFeatures_F64(Class classFixed) {
        super(classFixed, MatrixFeatures_DDRM.class);
    }

    /**
     * Compares equivalent functions in FixedOps to CommonOps.  Inputs are randomly generated
     */
    @Test
    public void compareToCommonOps() {
        compareToCommonOps(4,0);
    }
}
