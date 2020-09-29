/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.decomposition.chol;

import org.ejml.EjmlParameters;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;
import org.ejml.simple.SimpleMatrix;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class StabilityCholeksyDecomposition {


    public static double evaluate(CholeskyDecomposition_F64<DMatrixRMaj> alg , DMatrixRMaj orig ) {

        if( !DecompositionFactory_DDRM.decomposeSafe(alg,orig)) {
            return Double.NaN;
        }

        SimpleMatrix T = SimpleMatrix.wrap(alg.getT(null));

        SimpleMatrix A_found = T.mult(T.transpose());
        SimpleMatrix A = SimpleMatrix.wrap(orig);

        double top = A_found.minus(A).normF();
        double bottom = A.normF();

        return top/bottom;
    }

    private static void runAlgorithms( DMatrixRMaj mat  )
    {
        System.out.println("basic             = "+ evaluate(new CholeskyDecompositionInner_DDRM(),mat));
        System.out.println("block             = "+ evaluate(new CholeskyDecompositionBlock_DDRM(EjmlParameters.BLOCK_WIDTH_CHOL),mat));
        System.out.println("block64           = "+ evaluate(new CholeskyDecomposition_DDRB_to_DDRM(true),mat));

    }

    public static void main( String[] args ) {
        Random rand = new Random(23423);

        EjmlParameters.BLOCK_WIDTH = 5;

        for( int size = 5; size <= 15; size += 5 ) {
            double[] scales = new double[]{1,0.1,1e-20,1e-100,1e-200,1e-300,1e-304,1e-308,1e-319,1e-320,1e-321,Double.MIN_VALUE};

            // results vary significantly depending if it starts from a small or large matrix
            for( int i = 0; i < scales.length; i++ ) {
                System.out.printf("Decomposition size %3d for %e scale\n",size,scales[i]);
                DMatrixRMaj mat = RandomMatrices_DDRM.symmetricPosDef(size,rand);
                CommonOps_DDRM.scale(scales[i],mat);
                runAlgorithms(mat);
            }
        }
        System.out.println("  Done.");
    }
}