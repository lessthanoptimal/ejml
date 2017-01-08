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

package org.ejml.alg.dense.decomposition.eig.watched;

import org.ejml.data.Complex_F64;
import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.RandomMatrices_R64;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: pja
 * Date: Dec 26, 2009
 * Time: 6:20:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class WatchDoubleStepQR {

    public static void watchFindEigen( RowMatrix_F64 A ) {
        WatchedDoubleStepQREigenvalue_R64 alg = new WatchedDoubleStepQREigenvalue_R64();

//        alg.implicitQR.printFound = true;
//        alg.implicitQR.normalize = true;
        alg.implicitQR.checkHessenberg = true;

//        CommonOps.scale(1e305,A);
        alg.process(A);

        System.out.println("Eigenvalues.");
        for( int i = 0; i < A.numRows; i++ ) {
            Complex_F64 c = alg.implicitQR.eigenvalues[i];

            System.out.printf("(%8.5e  img = %8.5e ) in steps %3d \n",c.real,c.imaginary,alg.implicitQR.numStepsFind[i]);
        }

        System.out.println("Number of exceptional steps = "+alg.implicitQR.numExceptional);

        // finding eigen vectors now

        WatchedDoubleStepQREigenvector_R64 algVector = new WatchedDoubleStepQREigenvector_R64();

        algVector.process(alg.implicitQR,A,null);

        System.out.println("Eigenvectors.");
        for( int i = 0; i < A.numRows; i++ ) {
            RowMatrix_F64 v = algVector.eigenvectors[i];

            if( v != null )
                v.print("%8.3e");
            else
                System.out.println("i = "+i+"  is null");
        }
    }

    public static void watchImplicitDouble( RowMatrix_F64 A ) {
        WatchedDoubleStepQREigen_R64 alg = new WatchedDoubleStepQREigen_R64();

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

    public static void watchImplicitSingle( RowMatrix_F64 A ) {
        WatchedDoubleStepQREigen_R64 alg = new WatchedDoubleStepQREigen_R64();

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

        RowMatrix_F64 A = RandomMatrices_R64.createUpperTriangle(5,1,2,3,rand);
//        RowMatrix_F64 A = RandomMatrices.createUpperTriangle(50,1,-2,2,rand);
//        RowMatrix_F64 A = new RowMatrix_F64(5,5,new double[]{0,0,0,0,1,1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0},true);
//        RowMatrix_F64 A = UtilEjml.parseMatrix("-0.951  0.845 -0.171 \n" +
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
