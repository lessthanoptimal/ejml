/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.qr;

import org.ejml.EjmlParameters;
import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.interfaces.decomposition.QRPDecomposition;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;
import org.ejml.simple.SimpleMatrix;

import java.util.Random;

import static org.ejml.factory.DecompositionFactory.decomposeSafe;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class StabilityQRDecomposition {


    public static double evaluate( QRDecomposition<DenseMatrix64F> alg , DenseMatrix64F orig ) {

        if( !decomposeSafe(alg,orig)) {
            return Double.NaN;
        }

        SimpleMatrix Q = SimpleMatrix.wrap(alg.getQ(null,true));
        SimpleMatrix R = SimpleMatrix.wrap(alg.getR(null,true));

        SimpleMatrix A_found = Q.mult(R);
        SimpleMatrix A = SimpleMatrix.wrap(orig);

        return A.minus(A_found).normF()/A.normF();
    }

    public static double evaluate( QRPDecomposition<DenseMatrix64F> alg , DenseMatrix64F orig ) {

        if( !decomposeSafe(alg,orig)) {
            return Double.NaN;
        }

        SimpleMatrix Q = SimpleMatrix.wrap(alg.getQ(null,true));
        SimpleMatrix R = SimpleMatrix.wrap(alg.getR(null,true));
        SimpleMatrix P = SimpleMatrix.wrap(alg.getPivotMatrix(null));

        SimpleMatrix A_found = Q.mult(R);
        SimpleMatrix A = SimpleMatrix.wrap(orig);

        return A.mult(P).minus(A_found).normF()/A.normF();
    }

    private static void runAlgorithms( DenseMatrix64F mat  )
    {
        System.out.println("qr               = "+ evaluate(new QRDecompositionHouseholder_D64(),mat));
        System.out.println("qr col           = "+ evaluate(new QRDecompositionHouseholderColumn_D64(),mat));
        System.out.println("qr pivot col     = "+ evaluate(new QRColPivDecompositionHouseholderColumn_D64(),mat));
        System.out.println("qr tran          = "+ evaluate(new QRDecompositionHouseholderTran_D64(),mat));
        System.out.println("qr block         = "+ evaluate(new QRDecomposition_B64_to_D64(),mat));
    }

    public static void main( String args [] ) {

        // set the block size so that it will get triggered at a smaller size
        EjmlParameters.BLOCK_SIZE = 10;

        Random rand = new Random(239454923);

        for( int size = 5; size <= 15; size += 5 ) {
            double scales[] = new double[]{1,0.1,1e-20,1e-100,1e-200,1e-300,1e-304,1e-308,1e-310,1e-312,1e-319,1e-320,1e-321,Double.MIN_VALUE};

            System.out.println("Square matrix");
            DenseMatrix64F orig = RandomMatrices.createRandom(2*size,size,-1,1,rand);
            DenseMatrix64F mat = orig.copy();
            // results vary significantly depending if it starts from a small or large matrix
            for( int i = 0; i < scales.length; i++ ) {
                System.out.printf("Decomposition size %3d for %e scale\n",size,scales[i]);
                CommonOps.scale(scales[i],orig,mat);
                runAlgorithms(mat);
            }
        }

        System.out.println("  Done.");
    }
}