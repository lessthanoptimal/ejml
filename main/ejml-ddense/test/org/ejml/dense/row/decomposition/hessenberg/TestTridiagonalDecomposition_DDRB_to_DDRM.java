/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.decomposition.hessenberg;

import org.ejml.data.DMatrixRMaj;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition_F64;


/**
 * @author Peter Abeles
 */
public class TestTridiagonalDecomposition_DDRB_to_DDRM extends StandardTridiagonalTests_DDRM {
    @Override
    protected TridiagonalSimilarDecomposition_F64<DMatrixRMaj> createDecomposition() {
        return new TridiagonalDecomposition_DDRB_to_DDRM(3);
    }
}
