/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.chol;

import org.ejml.EjmlParameters;
import org.ejml.alg.dense.decomposition.CholeskyDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;
import org.ejml.simple.SimpleMatrix;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class StabilityCholeksyDecomposition {


    public static double evaluate( CholeskyDecomposition<DenseMatrix64F> alg , DenseMatrix64F orig ) {

        if( !alg.decompose(orig)) {
            return Double.NaN;
        }

        SimpleMatrix T = SimpleMatrix.wrap(alg.getT(null));

        SimpleMatrix A_found = T.mult(T.transpose());
        SimpleMatrix A = SimpleMatrix.wrap(orig);

        double top = A_found.minus(A).normF();
        double bottom = A.normF();

        return top/bottom;
    }

    private static void runAlgorithms( DenseMatrix64F mat  )
    {
        System.out.println("basic             = "+ evaluate(new CholeskyDecompositionInner(),mat));
        System.out.println("block             = "+ evaluate(new CholeskyDecompositionBlock(false, EjmlParameters.BLOCK_WIDTH_CHOL),mat));

    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size = 10;
        double scales[] = new double[]{1,0.1,1e-20,1e-100,1e-200,1e-300,1e-304,1e-308,1e-319,1e-320,1e-321,Double.MIN_VALUE};

        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 0; i < scales.length; i++ ) {
            System.out.printf("Decomposition size %3d for %e scale\n",size,scales[i]);

            System.out.print("* Creating matrix ");
            DenseMatrix64F mat = RandomMatrices.createSymmPosDef(size,rand);
            CommonOps.scale(scales[i],mat);
            System.out.println("  Done.");
            runAlgorithms(mat);
        }
    }
}