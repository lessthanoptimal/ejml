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

package org.ejml.alg.dense.decomposition.svd;

import org.ejml.alg.dense.decomposition.DecompositionFactory;
import org.ejml.alg.dense.decomposition.SingularValueDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class StabilitySvdlDecomposition {


    public static double evaluate( SingularValueDecomposition alg , DenseMatrix64F orig ) {

        if( !alg.decompose(orig)) {
            return Double.NaN;
        }

        return DecompositionFactory.quality(orig,alg);
    }

    private static void runAlgorithms( DenseMatrix64F mat  )
    {
        System.out.println("qr               = "+ evaluate(new SvdImplicitQrDecompose(true,true,true),mat));
        System.out.println("qr ult           = "+ evaluate(new SvdImplicitQrDecompose_Ultimate(true,true,true),mat));
    }

    public static void main( String args [] ) {
        Random rand = new Random(239454923);

        int size = 10;
        double scales[] = new double[]{1,0.1,1e-20,1e-100,1e-200,1e-300,1e-304,1e-308,1e-310,1e-312,1e-319,1e-320,1e-321,Double.MIN_VALUE};

        System.out.println("Square matrix");
        DenseMatrix64F orig = RandomMatrices.createRandom(size,size,-1,1,rand);
        DenseMatrix64F mat = orig.copy();
        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 0; i < scales.length; i++ ) {
            System.out.printf("Decomposition size %3d for %e scale\n",size,scales[i]);
            CommonOps.scale(scales[i],orig,mat);
            runAlgorithms(mat);
        }

        System.out.println("  Done.");        
    }
}