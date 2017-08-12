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

package org.ejml.sparse.csc.factory;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.interfaces.decomposition.CholeskySparseDecomposition_F64;
import org.ejml.interfaces.decomposition.LUSparseDecomposition_F64;
import org.ejml.interfaces.decomposition.QRSparseDecomposition;
import org.ejml.sparse.ComputePermutation;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.decomposition.chol.CholeskyUpLooking_DSCC;
import org.ejml.sparse.csc.decomposition.lu.LuUpLooking_DSCC;
import org.ejml.sparse.csc.decomposition.qr.QrLeftLookingDecomposition_DSCC;

/**
 * Factory for sparse matrix decompositions
 *
 * @author Peter Abeles
 */
public class DecompositionFactory_DSCC {
    public static CholeskySparseDecomposition_F64 cholesky() {
        return new CholeskyUpLooking_DSCC();
    }

    public static QRSparseDecomposition<DMatrixSparseCSC> qr(FillReducing permutation) {
        ComputePermutation<DMatrixSparseCSC> cp = FillReductionFactory_DSCC.create(permutation);
        return new QrLeftLookingDecomposition_DSCC(cp);
    }

    public static LUSparseDecomposition_F64<DMatrixSparseCSC> lu(FillReducing permutation) {
        ComputePermutation<DMatrixSparseCSC> cp = FillReductionFactory_DSCC.create(permutation);
        return new LuUpLooking_DSCC(cp);
    }
}
