/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.decomposition.svd;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.SingularOps_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.dense.row.linsol.qr.SolveNullSpaceQRP_DDRM;
import org.ejml.dense.row.linsol.qr.SolveNullSpaceQR_DDRM;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F64;

import java.util.Random;

/**
 * @author Peter Abeles
 */
public class BenchmarkNullspace {

    Random rand = new Random(234);
    int numIterations = 100;


    int rows = 100000;

    DMatrixRMaj A = new DMatrixRMaj(rows,9);
    DMatrixRMaj A_copy = new DMatrixRMaj(rows,9);
    DMatrixRMaj nullspace = new DMatrixRMaj(9,1);

    SolveNullSpaceQR_DDRM solverQR = new SolveNullSpaceQR_DDRM();
    SolveNullSpaceQRP_DDRM solverQRP = new SolveNullSpaceQRP_DDRM();
    SingularValueDecomposition_F64<DMatrixRMaj> svd = DecompositionFactory_DDRM.svd(rows,9,false,true,true);

    public BenchmarkNullspace() {
        double sv[] = new double[A.numCols];
        for (int i = 0; i < sv.length; i++) {
            sv[i] = rand.nextDouble()*4;
        }
        sv[rand.nextInt(sv.length)] = 0;
        A=RandomMatrices_DDRM.singular(A.numRows,A.numCols,rand,sv);
        System.out.println("Finished creating random matrix");
    }

    public long testQR() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < numIterations; i++) {
            if( solverQR.inputModified() )
                A_copy.setTo(A);
            solverQR.process(A_copy,1,nullspace);
        }
//        DMatrixRMaj hrm = new DMatrixRMaj(A.numRows,1);
//        CommonOps_DDRM.mult(A,nullspace,hrm);
//        System.out.println("norm = "+ NormOps_DDRM.normF(hrm));
        return System.currentTimeMillis()-start;
    }

    public long testQRP() {
        A_copy.setTo(A);
        long start = System.currentTimeMillis();
        for (int i = 0; i < numIterations; i++) {
            if( solverQRP.inputModified() )
                A_copy.setTo(A);
            solverQRP.process(A_copy,1,nullspace);
        }
//        DMatrixRMaj hrm = new DMatrixRMaj(A.numRows,1);
//        CommonOps_DDRM.mult(A,nullspace,hrm);
//        System.out.println("norm = "+ NormOps_DDRM.normF(hrm));
        return System.currentTimeMillis()-start;
    }

    public long testSVD() {
        if( svd.inputModified())
            throw new RuntimeException("Update");

        long start = System.currentTimeMillis();
        for (int i = 0; i < numIterations; i++) {
            svd.decompose(A);
            SingularOps_DDRM.nullSpace(svd,nullspace,1e-4);
        }
        return System.currentTimeMillis()-start;
    }

    public static void main(String[] args) {
        BenchmarkNullspace benchmark = new BenchmarkNullspace();

        System.out.println("QR  = "+benchmark.testQR());
        System.out.println("QRP = "+benchmark.testQRP());
        System.out.println("SVD = "+benchmark.testSVD());
    }

}
