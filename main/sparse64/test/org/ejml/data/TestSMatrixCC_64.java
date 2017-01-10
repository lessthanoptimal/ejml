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

package org.ejml.data;

import org.ejml.sparse.ConvertSparseMatrix_F64;

/**
 * @author Peter Abeles
 */
public class TestSMatrixCC_64 extends GenericTestsSparseMatrix_64 {

    @Override
    public Matrix_64 createSparse(SMatrixTriplet_64 orig, int numRows, int numCols) {
        SMatrixCC_64 dst = new SMatrixCC_64(numRows,numCols, numRows*numCols);
        ConvertSparseMatrix_F64.convert(orig,dst,null);
        return dst;
    }
}
