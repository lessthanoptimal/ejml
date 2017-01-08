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

package org.ejml.alg.dense.decomposition.bidiagonal;

import org.ejml.data.RowMatrix_F64;
import org.ejml.interfaces.decomposition.BidiagonalDecomposition_F64;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.RandomMatrices_R64;
import org.ejml.simple.SimpleMatrix;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class StabilityBidiagonalDecomposition {


    public static double evaluate(BidiagonalDecomposition_F64<RowMatrix_F64> alg , RowMatrix_F64 orig ) {

        if( !alg.decompose(orig.copy())) {
            return Double.NaN;
        }

        SimpleMatrix U = SimpleMatrix.wrap(alg.getU(null,false,true));
        SimpleMatrix B = SimpleMatrix.wrap(alg.getB(null,true));
        SimpleMatrix Vt = SimpleMatrix.wrap(alg.getV(null,true,true));

        SimpleMatrix A_found = U.mult(B).mult(Vt);
        SimpleMatrix A = SimpleMatrix.wrap(orig);

        double top = A_found.minus(A).normF();
        double bottom = A.normF();

        return top/bottom;
    }

    private static void runAlgorithms( RowMatrix_F64 mat  )
    {
        System.out.println("row               = "+ evaluate(new BidiagonalDecompositionRow_R64(),mat));
        System.out.println("tall              = "+ evaluate(new BidiagonalDecompositionTall_R64(),mat));
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size = 10;
        double scales[] = new double[]{1,0.1,1e-20,1e-100,1e-200,1e-300,1e-304,1e-308,1e-319,1e-320,1e-321,Double.MIN_VALUE};

        System.out.println("Square matrix: Scale");
        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 0; i < scales.length; i++ ) {
            System.out.printf("Decomposition size %3d for %e scale\n",size,scales[i]);

            RowMatrix_F64 mat = RandomMatrices_R64.createRandom(size,size,-1,1,rand);
            CommonOps_R64.scale(scales[i],mat);
            runAlgorithms(mat);
        }

        System.out.println("Square Matrix: Singular");

        double sv[] = new double[size];
        for( int i = 0; i < size; i++ )
            sv[i] = 2*i+5;

        for( int i = 0; i < 10; i++ ) {
            sv[0] = (9.0-i)/10.0;

            System.out.printf("Decomposition size %3d for %e singular\n",size,sv[0]);

//            System.out.print("* Creating matrix ");
            RowMatrix_F64 mat = RandomMatrices_R64.createSingularValues(size,size,rand,sv);
            CommonOps_R64.scale(scales[i],mat);
//            System.out.println("  Done.");
            runAlgorithms(mat);
        }
    }
}