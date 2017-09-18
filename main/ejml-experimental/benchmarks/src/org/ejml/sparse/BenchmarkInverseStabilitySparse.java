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

package org.ejml.sparse;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.ops.ConvertDMatrixStruct;
import org.ejml.sparse.csc.factory.LinearSolverFactory_DSCC;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Feeds the algorithms matrices that are closer and closer to being singular
 * and sees at which point they break.
 *
 * @author Peter Abeles
 */
public class BenchmarkInverseStabilitySparse {

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
        System.out.println("Cholesky nearly singular results might not be accurate");

        List<CandidateInfo> candidates = new ArrayList<>();

        candidates.add( new CandidateInfo("LU",LinearSolverFactory_DSCC.lu(FillReducing.NONE),false) );
        candidates.add( new CandidateInfo("QR",LinearSolverFactory_DSCC.qr(FillReducing.NONE),false) );
        candidates.add( new CandidateInfo("Chol",LinearSolverFactory_DSCC.cholesky(FillReducing.NONE),true) );

        allTheBreaks(candidates);
    }

    private void allTheBreaks(List<CandidateInfo> candidates )
    {
        System.out.println("Testing singular:");
        for( int i = 0; i < candidates.size(); i++ ) {
            breakNearlySingluar(candidates.get(i));
        }

        System.out.println("Testing overflow:");
        for( int i = 0; i < candidates.size(); i++ ) {
            breakOverUnderFlow(candidates.get(i),true);
        }

        System.out.println("Testing underflow:");
        for( int i = 0; i < candidates.size(); i++ ) {
            breakOverUnderFlow(candidates.get(i),false);
        }
    }

    private void breakNearlySingluar( CandidateInfo candidate ) {
        double breakDelta = -1;
        int breakW = -1;

        escape: for( int i = 0; i < 40; i++ ) {
//            System.out.println("i = "+i);
            double delta = Math.pow(0.1,i);
            for( int w = 0; w < 6; w++ ) {
                DMatrixRMaj A;
//                if( candidate.psd )
//                    A = new DMatrixRMaj(3,3, true, 3, 2, 1, 2, 3, 2, 1, 2, 3);
//                else
                    A = new DMatrixRMaj(3,3, true, 1, 2, 3, 2, 4, 6, 4, 6, -2);
                DMatrixRMaj A_inv = new DMatrixRMaj(3,3);
                DMatrixRMaj I = CommonOps_DDRM.identity(3);

                A.plus(w, delta);

                if( candidate.psd ) {
                    // I'm not sure if this is a good way to make a PSD matrix....
                    DMatrixRMaj tmp = new DMatrixRMaj(3,3);
                    CommonOps_DDRM.elementPower(A,0.5,A);
                    CommonOps_DDRM.multTransB(A,A,tmp);
                    A = tmp;
                }
                DMatrixSparseCSC Asp = ConvertDMatrixStruct.convert(A,(DMatrixSparseCSC)null, UtilEjml.EPS);

                try {
                    if( !candidate.alg.setA(Asp) ) {
                        breakDelta = delta;
                        breakW = w;
                        break escape;
                    }
                    candidate.alg.solve(I,A_inv);
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

        System.out.printf("%20s broke at %E  w = %d\n",candidate.name,
                breakDelta,breakW);
    }

    private void breakOverUnderFlow( CandidateInfo candidate , boolean overflow ) {
        boolean madeBad= false;

        DMatrixRMaj A_orig;
        if( candidate.psd )
            A_orig = RandomMatrices_DDRM.symmetricPosDef(3,new Random(0x14));
        else
            A_orig = RandomMatrices_DDRM.rectangle(3,3,new Random(0x14));
        DMatrixRMaj I = CommonOps_DDRM.identity(3);

        int i;
        double scale=Double.NaN;
        for( i = 0; i < 3000; i++ ) {
//            System.out.println("i = "+i);
            DMatrixRMaj A = new DMatrixRMaj(A_orig);

            if( overflow )
                scale = Math.pow(2,i);
            else
                scale = Math.pow(1.0/2.0,i);

            CommonOps_DDRM.scale(scale,A);

            DMatrixSparseCSC A_sparse = ConvertDMatrixStruct.convert(A,(DMatrixSparseCSC)null,0);
            DMatrixRMaj A_inv = new DMatrixRMaj(A.numRows,A.numCols);

            if(MatrixFeatures_DDRM.hasUncountable(A)) {
                madeBad = true;
                break;
            }

            try {
                if( !candidate.alg.setA(A_sparse) ) {
                    break;
                }
                candidate.alg.solve(I,A_inv);
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
            System.out.printf("%20s never broke. (%d)\n",candidate.name,i);
        } else {
            System.out.printf("%20s broke at %d scale of %e.\n",candidate.name,i,scale);
        }
    }

    private static class CandidateInfo
    {
        String name;
        LinearSolverSparse<DMatrixSparseCSC,DMatrixRMaj> alg;
        boolean psd;

        public CandidateInfo(String name, LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> alg, boolean psd) {
            this.name = name;
            this.alg = alg;
            this.psd = psd;
        }
    }

    public static void main( String arg[] ) {
        BenchmarkInverseStabilitySparse eval = new BenchmarkInverseStabilitySparse();

        eval.evaluateAll();
    }
}
