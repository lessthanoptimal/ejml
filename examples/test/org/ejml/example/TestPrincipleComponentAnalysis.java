/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.example;

import org.ejml.ops.RandomMatrices;
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

        PrincipleComponentAnalysis pca = new PrincipleComponentAnalysis();

        // add observations
        pca.setup(M,N);

        for( int i = 0; i < M; i++ ) {
            obs[i] = RandomMatrices.createRandom(N,1,-1,1,rand).data;
            pca.addSample(obs[i]);
        }

        // as a more crude estimate is made of the input data the error should increase
        pca.computeBasis(N);
        double errorPrev = computeError(pca,obs);
        assertEquals(errorPrev,0,1e-8);

        for( int i = N-1; i >= 1; i-- ) {
            pca.computeBasis(i);
            double error = computeError(pca,obs);
            assertTrue(error > errorPrev );
            errorPrev = error;
        }
    }

    private double computeError(PrincipleComponentAnalysis pca, double[][] obs ) {
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

        PrincipleComponentAnalysis pca = new PrincipleComponentAnalysis();

        // add observations
        pca.setup(M,N);

        for( int i = 0; i < M; i++ ) {
            obs[i] = RandomMatrices.createRandom(N,1,-1,1,rand).data;
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

        PrincipleComponentAnalysis pca = new PrincipleComponentAnalysis();

        // add observations
        pca.setup(M,N);

        for( int i = 0; i < M; i++ ) {
            obs[i] = RandomMatrices.createRandom(N,1,-1,1,rand).data;
            pca.addSample(obs[i]);
        }

        pca.computeBasis(N-2);

        for( int i = 0; i < M; i++ ) {
            double responseObs = pca.response(obs[i]);

            assertTrue(responseObs > 0 );
        }
    }
}
