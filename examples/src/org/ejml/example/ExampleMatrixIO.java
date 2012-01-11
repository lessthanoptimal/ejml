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

package org.ejml.example;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.MatrixIO;
import org.ejml.simple.SimpleMatrix;

import java.io.IOException;

/**
 * Examples for reading and writing matrices to files in different formats
 *
 * @author Peter Abeles
 */
public class ExampleMatrixIO {

    public static void csv() {
        DenseMatrix64F A = new DenseMatrix64F(2,3,true,new double[]{1,2,3,4,5,6});

        try {
            MatrixIO.saveCSV(A, "matrix_file.csv");
            DenseMatrix64F B = MatrixIO.loadCSV("matrix_file.csv");
            B.print();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void csv_simple() {
        SimpleMatrix A = new SimpleMatrix(2,3,true,new double[]{1,2,3,4,5,6});

        try {
            A.saveToFileCSV("matrix_file.csv");
            SimpleMatrix B = SimpleMatrix.loadCSV("matrix_file.csv");
            B.print();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void serializedBinary() {
        DenseMatrix64F A = new DenseMatrix64F(2,3,true,new double[]{1,2,3,4,5,6});

        try {
            MatrixIO.saveBin(A, "matrix_file.data");
            DenseMatrix64F B = MatrixIO.loadBin("matrix_file.data");
            B.print();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void csv_serializedBinary() {
        SimpleMatrix A = new SimpleMatrix(2,3,true,new double[]{1,2,3,4,5,6});

        try {
            A.saveToFileBinary("matrix_file.data");
            SimpleMatrix B = SimpleMatrix.loadBinary("matrix_file.data");
            B.print();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void main( String args[] ) {
        csv();
        serializedBinary();
    }
}
