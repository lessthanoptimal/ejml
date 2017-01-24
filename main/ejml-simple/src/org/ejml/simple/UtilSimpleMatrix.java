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

package org.ejml.simple;

import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.ops.ConvertDMatrixStruct;

/**
 * @author Peter Abeles
 */
public class UtilSimpleMatrix {
    /**
     * <p>Converts the block matrix into a SimpleMatrix.</p>
     *
     * @param A Block matrix that is being converted.  Not modified.
     * @return Equivalent SimpleMatrix.
     */
    public static SimpleMatrix convertSimple( DMatrixRBlock A ) {
        DMatrixRMaj B = ConvertDMatrixStruct.convert(A, null);

        return SimpleMatrix.wrap(B);
    }
}
