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

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.sparse.ComputePermutation;
import org.ejml.sparse.FillReducing;

import java.util.Random;

/**
 * @author Peter Abeles
 */
public class FillReductionFactory_DSCC {
    public static final Random rand = new Random(234234);

    public static ComputePermutation<DMatrixSparseCSC> create(FillReducing type ) {
        switch( type ) {
            case NONE:
                return null;

            case RANDOM:
                return new ComputePermutation<DMatrixSparseCSC>(true,true) {
                    @Override
                    public void process(DMatrixSparseCSC m ) {
                        prow.reshape(m.numRows);
                        pcol.reshape(m.numCols);
                        fillSequence(prow);
                        fillSequence(pcol);
                        Random _rand;
                        synchronized (rand) {
                            _rand = new Random(rand.nextInt());
                        }
                        UtilEjml.shuffle(prow.data,prow.length,0,prow.length,_rand);
                        UtilEjml.shuffle(pcol.data,pcol.length,0,pcol.length,_rand);
                    }
                };

            case IDENTITY:
                return new ComputePermutation<DMatrixSparseCSC>(true,true) {
                    @Override
                    public void process(DMatrixSparseCSC m) {
                        prow.reshape(m.numRows);
                        pcol.reshape(m.numCols);
                        fillSequence(prow);
                        fillSequence(pcol);
                    }
                };

            default:
                throw new RuntimeException("Unknown "+type);
        }
    }

    private static void fillSequence(IGrowArray perm) {
        for (int i = 0; i <perm.length; i++) {
            perm.data[i] = i;
        }
    }
}
