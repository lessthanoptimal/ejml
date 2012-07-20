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

package org.ejml.alg.dense.decomposition.svd;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.factory.SingularValueDecomposition;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class StabilitySvdlDecomposition {

    private static boolean compact = false;
    // These two options need to be true to compute the quality
    private static boolean computeU = true;
    private static boolean computeV = true;


    public static double evaluate( SingularValueDecomposition<DenseMatrix64F> alg , DenseMatrix64F orig ) {

        DenseMatrix64F U=null;
        DenseMatrix64F W;
        DenseMatrix64F Vt=null;

        if( !alg.decompose(orig.copy())) {
            return Double.NaN;
        }

        W = alg.getW(null);
        if( computeU )
            U = alg.getU(false).copy();
        if( computeV )
            Vt = alg.getV(true).copy();

        // I'm not sure how to test quality of U or V is not computed.

        return DecompositionFactory.quality(orig, U,W,Vt);
    }

    private static void runAlgorithms( DenseMatrix64F mat )
    {
        System.out.println("qr               = "+ evaluate(new SvdImplicitQrDecompose(compact,computeU,computeV,false),mat));
        System.out.println("qr ult           = "+ evaluate(new SvdImplicitQrDecompose_Ultimate(compact,computeU,computeV),mat));
    }

    public static void main( String args [] ) {
        Random rand = new Random(239454923);

        int numCols = 10;
        int numRows = 30;
        double scales[] = new double[]{1,0.1,1e-20,1e-100,1e-200,1e-300,1e-304,1e-308,1e-310,1e-312,1e-319,1e-320,1e-321,Double.MIN_VALUE};


        DenseMatrix64F orig = RandomMatrices.createRandom(numRows,numCols,-1,1,rand);
        DenseMatrix64F mat = orig.copy();
        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 0; i < scales.length; i++ ) {
            System.out.printf("  Decomposition size %3d %d for %e scale\n",numRows,numCols,scales[i]);
            CommonOps.scale(scales[i],orig,mat);
            runAlgorithms(mat);
        }

        System.out.println();
        System.out.println("Done.");
    }
}