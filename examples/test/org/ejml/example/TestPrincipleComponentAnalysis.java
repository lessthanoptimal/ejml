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

package org.ejml.example;

import org.ejml.UtilEjml;
import org.ejml.dense.row.RandomMatrices_R64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestPrincipleComponentAnalysis {
    Random rand = new Random(234345);

    /**
     * Sees if the projection error increases as the DOF decreases in the number of basis vectors.
     */
    @Test
    public void checkBasisError() {
        int M = 30;
        int N = 5;

        double obs[][] = new double[M][];

        PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();

        // add observations
        pca.setup(M,N);

        for( int i = 0; i < M; i++ ) {
            obs[i] = RandomMatrices_R64.createRandom(N,1,-1,1,rand).data;
            pca.addSample(obs[i]);
        }

        // as a more crude estimate is made of the input data the error should increase
        pca.computeBasis(N);
        double errorPrev = computeError(pca,obs);
        assertEquals(errorPrev,0, UtilEjml.TEST_F64);

        for( int i = N-1; i >= 1; i-- ) {
            pca.computeBasis(i);
            double error = computeError(pca,obs);
            assertTrue(error > errorPrev );
            errorPrev = error;
        }
    }

    private double computeError(PrincipalComponentAnalysis pca, double[][] obs ) {
        double error = 0;
        for (double[] o : obs) {
            error += pca.errorMembership(o);
        }
        return error;
    }

    /**
     * Checks sampleToEigenSpace and sampleToEigenSpace when the basis vectors can
     * fully describe the vector.
     */
    @Test
    public void sampleToEigenSpace() {
        int M = 30;
        int N = 5;

        double obs[][] = new double[M][];

        PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();

        // add observations
        pca.setup(M,N);

        for( int i = 0; i < M; i++ ) {
            obs[i] = RandomMatrices_R64.createRandom(N,1,-1,1,rand).data;
            pca.addSample(obs[i]);
        }

        // when the basis is N vectors it should perfectly describe the vector
        pca.computeBasis(N);

        for( int i = 0; i < M; i++ ) {
            double s[] = pca.sampleToEigenSpace(obs[i]);
            assertTrue(error(s,obs[i]) > 1e-8 );
            double o[] = pca.eigenToSampleSpace(s);
            assertTrue(error(o,obs[i]) <= 1e-8 );
        }
    }

    private double error( double[] a , double []b ) {
        double ret = 0;

        for( int i = 0; i < a.length; i++ ) {
            ret += Math.abs(a[i]-b[i]);
        }

        return ret;
    }

    /**
     * Makes sure the response is not zero.  Perhaps this is too simple of a test
     */
    @Test
    public void response() {
        int M = 30;
        int N = 5;

        double obs[][] = new double[M][];

        PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();

        // add observations
        pca.setup(M,N);

        for( int i = 0; i < M; i++ ) {
            obs[i] = RandomMatrices_R64.createRandom(N,1,-1,1,rand).data;
            pca.addSample(obs[i]);
        }

        pca.computeBasis(N-2);

        for( int i = 0; i < M; i++ ) {
            double responseObs = pca.response(obs[i]);

            assertTrue(responseObs > 0 );
        }
    }
}
