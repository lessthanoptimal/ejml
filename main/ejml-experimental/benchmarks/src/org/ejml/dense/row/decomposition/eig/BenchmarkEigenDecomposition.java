/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.decomposition.eig;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;

import java.util.Random;

/**
 * @author Peter Abeles
 */
public class BenchmarkEigenDecomposition {
    public static long watched( DMatrixRMaj orig, int numTrials ) {

        long prev = System.currentTimeMillis();

        WatchedDoubleStepQRDecomposition_DDRM alg = new WatchedDoubleStepQRDecomposition_DDRM(true);

        for (long i = 0; i < numTrials; i++) {
            if (!alg.decompose(orig)) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }

    private static void runAlgorithms( DMatrixRMaj mat, int numTrials ) {
        System.out.println("Watched            = " + watched(mat, numTrials));
    }

    public static void main( String[] args ) {
        Random rand = new Random(23423);

        int[] size = new int[]{2, 4, 10, 100, 200};
        int[] trials = new int[]{200000, 40000, 8000, 50, 10};

        // results vary significantly depending if it starts from a small or large matrix
        for (int i = 0; i < size.length; i++) {
            int w = size[i];

            System.out.printf("Decomposing size %3d for %12d trials\n", w, trials[i]);

            DMatrixRMaj symMat = RandomMatrices_DDRM.rectangle(w, w, rand);

            runAlgorithms(symMat, trials[i]);
        }
    }
}
