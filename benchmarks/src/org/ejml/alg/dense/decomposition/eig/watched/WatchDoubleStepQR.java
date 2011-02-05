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

package org.ejml.alg.dense.decomposition.eig.watched;

import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: pja
 * Date: Dec 26, 2009
 * Time: 6:20:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class WatchDoubleStepQR {

    public static void watchFindEigen( DenseMatrix64F A ) {
        WatchedDoubleStepQREigenvalue alg = new WatchedDoubleStepQREigenvalue();

//        alg.implicitQR.printFound = true;
//        alg.implicitQR.normalize = true;
        alg.implicitQR.checkHessenberg = true;

//        CommonOps.scale(1e305,A);
        alg.process(A);

        System.out.println("Eigenvalues.");
        for( int i = 0; i < A.numRows; i++ ) {
            Complex64F c = alg.implicitQR.eigenvalues[i];

            System.out.printf("(%8.5e  img = %8.5e ) in steps %3d \n",c.real,c.imaginary,alg.implicitQR.numStepsFind[i]);
        }

        System.out.println("Number of exceptional steps = "+alg.implicitQR.numExceptional);

        // finding eigen vectors now

        WatchedDoubleStepQREigenvector algVector = new WatchedDoubleStepQREigenvector();

        algVector.process(alg.implicitQR,A,null);

        System.out.println("Eigenvectors.");
        for( int i = 0; i < A.numRows; i++ ) {
            DenseMatrix64F v = algVector.eigenvectors[i];

            if( v != null )
                v.print("%8.3e");
            else
                System.out.println("i = "+i+"  is null");
        }
    }

    public static void watchImplicitDouble( DenseMatrix64F A ) {
        WatchedDoubleStepQREigen alg = new WatchedDoubleStepQREigen();

//        alg.printHumps = true;

        alg.setup(A);

        alg.implicitDoubleStep(0,4);

        for( int i = 0; i < 20; i++ ) {
            System.out.println("-----------------------------------");
            alg.A.print();
            System.out.println();
            alg.implicitDoubleStep(0,4);
            System.out.println();
        }
    }

    public static void watchImplicitSingle( DenseMatrix64F A ) {
        WatchedDoubleStepQREigen alg = new WatchedDoubleStepQREigen();

//        alg.printHumps = true;

        alg.setup(A);

        double ev =  -7.801;

//        alg.implicitSingleStep(0,4);
        alg.performImplicitSingleStep(0,4,ev);

        for( int i = 0; i < 20; i++ ) {
            System.out.println("-----------------------------------");
            alg.A.print();
            System.out.println();
//            alg.implicitSingleStep(0,4);
            alg.performImplicitSingleStep(0,4,ev);
//            System.out.println();
        }
    }

    public static void main( String args[]) {
        Random rand = new Random(23475);
//        Random rand = new Random(235);

        DenseMatrix64F A = RandomMatrices.createUpperTriangle(5,1,2,3,rand);
//        DenseMatrix64F A = RandomMatrices.createUpperTriangle(50,1,-2,2,rand);
//        DenseMatrix64F A = new DenseMatrix64F(5,5,new double[]{0,0,0,0,1,1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0},true);
//        DenseMatrix64F A = UtilEjml.parseMatrix("-0.951  0.845 -0.171 \n" +
//                " 0.573 -0.720  0.264 \n" +
//                " 0.000  0.552 -0.100",3);

        System.out.println("--------- Original Matrix -----------");
        A.print();
        System.out.println("-------------------------------------");

        watchFindEigen(A);
//        watchImplicitDouble(A);
//        watchImplicitSingle(A);
    }
}
