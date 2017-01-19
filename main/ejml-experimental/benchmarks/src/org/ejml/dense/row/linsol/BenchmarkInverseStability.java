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

package org.ejml.dense.row.linsol;

import org.ejml.LinearSolverSafe;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.dense.row.linsol.qr.LinearSolverQrHouseCol_DDRM;
import org.ejml.interfaces.linsol.LinearSolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Feeds the algorithms matrices that are closer and closer to being singular
 * and sees at which point they break.
 *
 * @author Peter Abeles
 */
public class BenchmarkInverseStability {

    private DMatrixRMaj b = new DMatrixRMaj(3,3);


    private double evaluateInverse(DMatrixRMaj A , DMatrixRMaj A_inv )
    {
        CommonOps_DDRM.mult(A,A_inv,b);

        double total = 0;

        for( int y = 0; y < 3; y++ ) {
            for( int x = 0; x < 3; x++ ) {
                if( x == y ) {
                    total += Math.abs(b.get(x,y)-1.0);
                } else {
                    total += Math.abs(b.get(x,y));
                }
            }
        }

        return total;
    }

    public void evaluateAll()
    {
        List<LinearSolver<DMatrixRMaj>> solvers = new ArrayList<LinearSolver<DMatrixRMaj>>();
        List<String> names = new ArrayList<String>();

//        solvers.add(new GaussJordanNoPivot());
//        names.add("GJ NP");
//        solvers.add(new GaussJordan(3));
//        names.add("GJ");
//        solvers.add(new LinearSolverLu(new LUDecompositionAlt()));
//        names.add("LU ALT");
//        solvers.add(new LinearSolverLu(new LUDecompositionNR()));
//        names.add("LU NR");
//        solvers.add(new LinearSolverLuKJI(new LUDecompositionAlt()));
//        names.add("LU B");
//        solvers.add(new LinearSolverLu(new LUDecompositionAlt(),true));
//        names.add("LU A Imp");
        solvers.add(new LinearSolverQrHouseCol_DDRM());
        names.add("QR");
//        solvers.add(new LinearSolverSvdNR(new SvdNumericalRecipes()));
//        names.add("SVD NR");
//        solvers.add(new LinearSolverUnrolled());
//        names.add("Unrolled");
        solvers.add(LinearSolverFactory_DDRM.leastSquaresQrPivot(true, true));
        names.add("P'QR compute Q");
        solvers.add(LinearSolverFactory_DDRM.leastSquaresQrPivot(true,false));
        names.add("P'QR householder");
        solvers.add(LinearSolverFactory_DDRM.pseudoInverse(true));
        names.add("PINV SVD");

        allTheBreaks(solvers,names);
    }

    private void allTheBreaks(List<LinearSolver<DMatrixRMaj>> solvers , List<String> names )
    {
        System.out.println("Testing singular:");
        for( int i = 0; i < solvers.size(); i++ ) {
            breakNearlySingluar(names.get(i),solvers.get(i));
        }

        System.out.println("Testing overflow:");
        for( int i = 0; i < solvers.size(); i++ ) {
            breakOverUnderFlow(names.get(i),solvers.get(i),true);
        }

        System.out.println("Testing underflow:");
        for( int i = 0; i < solvers.size(); i++ ) {
            breakOverUnderFlow(names.get(i),solvers.get(i),false);
        }
    }

    private void breakNearlySingluar( String name , LinearSolver<DMatrixRMaj> alg ) {
        double breakDelta = -1;
        int breakW = -1;

        alg = new LinearSolverSafe<DMatrixRMaj>(alg);

        escape: for( int i = 0; i < 40; i++ ) {
//            System.out.println("i = "+i);
            double delta = Math.pow(0.1,i);
            for( int w = 0; w < 6; w++ ) {
                DMatrixRMaj A = new DMatrixRMaj(3,3, true, 1, 2, 3, 2, 4, 6, 4, 6, -2);
                DMatrixRMaj A_inv = new DMatrixRMaj(3,3);

                A.plus(w,  delta);

                try {
                    if( !alg.setA(A) ) {
                        breakDelta = delta;
                        breakW = w;
                        break escape;
                    }
                    alg.invert(A_inv);
                    if(evaluateInverse(A,A_inv) > 1.0 ) {
                        breakDelta = delta;
                        breakW = w;
                        break escape;
                    }
                } catch( RuntimeException e ) {
                    breakDelta = delta;
                    breakW = w;
                    break escape;
                }
            }
        }

        System.out.printf("%20s broke at %E  w = %d\n",name,
                breakDelta,breakW);
//        System.out.println(dense.getClass().getSimpleName()+" broke at "+breakDelta+" w = "+breakW);
    }

    private void breakOverUnderFlow(String name , LinearSolver<DMatrixRMaj> alg , boolean overflow ) {
        boolean madeBad= false;

        DMatrixRMaj A_orig = RandomMatrices_DDRM.rectangle(3,3,new Random(0x14));

        int i;
        for( i = 0; i < 3000; i++ ) {
//            System.out.println("i = "+i);
            DMatrixRMaj A = new DMatrixRMaj(A_orig);
            if( overflow )
                CommonOps_DDRM.scale(Math.pow(2,i),A);
            else
                CommonOps_DDRM.scale(Math.pow(1.0/2,i),A);

            DMatrixRMaj A_inv = new DMatrixRMaj(A.numRows,A.numCols);

            if(MatrixFeatures_DDRM.hasUncountable(A)) {
                madeBad = true;
                break;
            }

            try {
                if( !alg.setA(A) ) {
                    break;
                }
                alg.invert(A_inv);
                if(MatrixFeatures_DDRM.hasUncountable(A_inv)) {
                    break;
                }
            } catch( RuntimeException e ) {
                break;
            }

            if(evaluateInverse(A,A_inv) > 1.0 ) {
                break;
            }
        }

        if( madeBad ) {
            System.out.printf("%20s never broke. (%d)\n",name,i);
        } else {
            System.out.printf("%20s broke at %d.\n",name,i);
        }
    }

    public static void main( String arg[] ) {
        BenchmarkInverseStability eval = new BenchmarkInverseStability();

        eval.evaluateAll();
    }
}
