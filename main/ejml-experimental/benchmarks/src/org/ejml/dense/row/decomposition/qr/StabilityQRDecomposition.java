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

package org.ejml.dense.row.decomposition.qr;

import org.ejml.EjmlParameters;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.interfaces.decomposition.QRPDecompositionD;
import org.ejml.simple.SimpleMatrix;

import java.util.Random;

import static org.ejml.dense.row.factory.DecompositionFactory_DDRM.decomposeSafe;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class StabilityQRDecomposition {


    public static double evaluate(QRDecomposition<DMatrixRMaj> alg , DMatrixRMaj orig ) {

        if( !decomposeSafe(alg,orig)) {
            return Double.NaN;
        }

        SimpleMatrix Q = SimpleMatrix.wrap(alg.getQ(null,true));
        SimpleMatrix R = SimpleMatrix.wrap(alg.getR(null,true));

        SimpleMatrix A_found = Q.mult(R);
        SimpleMatrix A = SimpleMatrix.wrap(orig);

        return A.minus(A_found).normF()/A.normF();
    }

    public static double evaluate(QRPDecompositionD<DMatrixRMaj> alg , DMatrixRMaj orig ) {

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

    private static void runAlgorithms( DMatrixRMaj mat  )
    {
        System.out.println("qr               = "+ evaluate(new QRDecompositionHouseholder_DDRM(),mat));
        System.out.println("qr col           = "+ evaluate(new QRDecompositionHouseholderColumn_DDRM(),mat));
        System.out.println("qr pivot col     = "+ evaluate(new QRColPivDecompositionHouseholderColumn_DDRM(),mat));
        System.out.println("qr tran          = "+ evaluate(new QRDecompositionHouseholderTran_DDRM(),mat));
        System.out.println("qr block         = "+ evaluate(new QRDecomposition_DDRB_to_DDRM(),mat));
    }

    public static void main( String args [] ) {

        // set the block size so that it will get triggered at a smaller size
        EjmlParameters.BLOCK_SIZE = 10;

        Random rand = new Random(239454923);

        for( int size = 5; size <= 15; size += 5 ) {
            double scales[] = new double[]{1,0.1,1e-20,1e-100,1e-200,1e-300,1e-304,1e-308,1e-310,1e-312,1e-319,1e-320,1e-321,Double.MIN_VALUE};

            System.out.println("Square matrix");
            DMatrixRMaj orig = RandomMatrices_DDRM.createRandom(2*size,size,-1,1,rand);
            DMatrixRMaj mat = orig.copy();
            // results vary significantly depending if it starts from a small or large matrix
            for( int i = 0; i < scales.length; i++ ) {
                System.out.printf("Decomposition size %3d for %e scale\n",size,scales[i]);
                CommonOps_DDRM.scale(scales[i],orig,mat);
                runAlgorithms(mat);
            }
        }

        System.out.println("  Done.");
    }
}