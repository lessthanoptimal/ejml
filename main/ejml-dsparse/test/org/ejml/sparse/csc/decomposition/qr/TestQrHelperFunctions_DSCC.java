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

package org.ejml.sparse.csc.decomposition.qr;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparseCSC;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestQrHelperFunctions_DSCC {
    @Test
    public void applyHouseholder() {
        DMatrixSparseCSC V = UtilEjml.parse_DSCC(
                      "1 0 0 0 0 " +
                        "2 1 0 0 0 "+
                        "0 3 1 0 0 " +
                        "0 0 0 1 0 " +
                        "4 1 4 5 1",5);

        double []x = new double[]{1,2,3,4,5};
        QrHelperFunctions_DSCC.applyHouseholder(V,1,2.1,x);

        // hand computed solution
        double []expected = new double[]{1,-31.6,-97.8,4,-28.6};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i],x[i],UtilEjml.TEST_F64);
        }
    }
}
