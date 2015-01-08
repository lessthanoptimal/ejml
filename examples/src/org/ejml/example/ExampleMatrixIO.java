/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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
            SimpleMatrix B = new SimpleMatrix().loadCSV("matrix_file.csv");
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
