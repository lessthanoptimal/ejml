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

package org.ejml.alg.fixed;

import org.ejml.UtilEjml;
import org.ejml.ops.CommonOps_D64;
import org.junit.Test;

/**
 * @author Peter Abeles
 */
public abstract class CompareFixedToCommonOps_D64 extends CompareFixed_D64 {

    public CompareFixedToCommonOps_D64(Class classFixed) {
        super(classFixed, CommonOps_D64.class);
    }

    /**
     * Compares equivalent functions in FixedOps to CommonOps.  Inputs are randomly generated
     */
    @Test
    public void compareToCommonOps() {
        int numExpected = 55;
        if( N > UtilEjml.maxInverseSize ) {
            numExpected -= 2;
        }
        compareToCommonOps(numExpected,1);
    }
}
