/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

package org.ejml.ops;

import org.ejml.data.DenseMatrix64F;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkVariousOps {

    static Random rand = new Random(0xffff);

    static int TRIALS_TRANSPOSE = 20000000;
    static int TRIALS_SCALE = 30000000;
    static int TRIALS_NORM = 10000000;
    static int TRIALS_DETERMINANT = 20000000;

    public static long transposeEml( DenseMatrix64F mat , int numTrials) {
        long prev = System.currentTimeMillis();

        DenseMatrix64F tran = new DenseMatrix64F(mat.numCols,mat.numRows);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps.transpose(mat,tran);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

//    public static long transposeMtj( DenseMatrix64F orig , int numTrials) {
//        DenseMatrix mat = UtilMatrixToolkitsJava.convertToMtj(orig);
//
//        long prev = System.currentTimeMillis();
//
//        DenseMatrix tran = new DenseMatrix(mat.numColumns(),mat.numRows());
//
//        for( int i = 0; i < numTrials; i++ ) {
//            mat.transpose(tran);
//        }
//
//        long curr = System.currentTimeMillis();
//        return curr-prev;
//    }

    public static long scale( DenseMatrix64F mat , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps.scale(10,mat);
            CommonOps.scale(0.1,mat);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long scale2( DenseMatrix64F mat , int numTrials) {
        DenseMatrix64F result = mat.copy();

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps.scale(10,mat,result);
            CommonOps.scale(0.1,mat,result);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

//    public static long scaleMtj( DenseMatrix64F orig , int numTrials) {
//        DenseMatrix mat = UtilMatrixToolkitsJava.convertToMtj(orig);
//
//        long prev = System.currentTimeMillis();
//
//        for( int i = 0; i < numTrials; i++ ) {
//            mat.scale(10);
//            mat.scale(0.1);
//        }
//
//        long curr = System.currentTimeMillis();
//        return curr-prev;
//    }

    public static long normEml( DenseMatrix64F mat , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            NormOps.normF(mat);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long determinant( DenseMatrix64F mat , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps.det(mat);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

//    public static long normMtj( DenseMatrix64F orig , int numTrials) {
//        DenseMatrix mat = UtilMatrixToolkitsJava.convertToMtj(orig);
//
//        long prev = System.currentTimeMillis();
//
//        for( int i = 0; i < numTrials; i++ ) {
//            mat.norm(Matrix.Norm.Frobenius);
//        }
//
//        long curr = System.currentTimeMillis();
//        return curr-prev;
//    }

    public static void main( String args[] ) {
        System.out.println("Small Matrix Results:") ;
        DenseMatrix64F mat = RandomMatrices.createRandom(4,4,rand);

//        System.out.printf("Transpose:         eml = %10d\n",
//                transposeEml(mat,TRIALS_TRANSPOSE));
        System.out.printf("Scale:             eml = %10d\n",
                scale(mat,TRIALS_SCALE));
        System.out.printf("Scale2:            eml = %10d\n",
                scale2(mat,TRIALS_SCALE));
//        System.out.printf("Norm:              eml = %10d\n",
//                normEml(mat,TRIALS_NORM));
//        System.out.printf("Determinant:       eml = %10d\n",
//                determinant(mat,TRIALS_DETERMINANT));

        System.out.println();
        System.out.println("Large Matrix Results:") ;
        mat = RandomMatrices.createRandom(2000,2000,rand);
        System.out.printf("Transpose:         eml = %10d\n",
                transposeEml(mat,100));
//        System.out.printf("Scale:             eml = %10d\n",
//                scaleEml(mat,100));
//        System.out.printf("Norm:              eml = %10d\n",
//                normEml(mat,100));
//        System.out.printf("Determinant:       eml = %10d\n",
//                determinant(mat,1));
    }
}
