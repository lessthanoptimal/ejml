/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.data.MatrixType;
import org.ejml.ops.ConvertDMatrixStruct;
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
        DMatrixRMaj A = new DMatrixRMaj(2,3,true,new double[]{1,2,3,4,5,6});

        try {
            MatrixIO.saveDenseCSV(A, "matrix_file.csv");
            DMatrixRMaj B = MatrixIO.loadCSV("matrix_file.csv",true);
            B.print();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void csv_sparse() {
        DMatrixSparseCSC A = new DMatrixSparseCSC(5,4);
        A.set(1,2,4.5);

        try {
            // Use triplet as an intermediate step when working with sparse matrices
            DMatrixSparseTriplet A_triple = ConvertDMatrixStruct.convert(A,(DMatrixSparseTriplet)null);

            MatrixIO.saveSparseCSV(A_triple, "matrix_file.csv");
            DMatrixSparseTriplet B_triple = MatrixIO.loadCSV("matrix_file.csv",true);

            DMatrixSparseCSC B = ConvertDMatrixStruct.convert(B_triple,(DMatrixSparseCSC)null);
            B.print();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void csv_simple() {
        SimpleMatrix A = new SimpleMatrix(2,3,true,new double[]{1,2,3,4,5,6});

        try {
            A.saveToFileCSV("matrix_file.csv");
            SimpleMatrix B = new SimpleMatrix(1,1, MatrixType.DDRM).loadCSV("matrix_file.csv");
            B.print();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void serializedBinary() {
        DMatrixRMaj A = new DMatrixRMaj(2,3,true,new double[]{1,2,3,4,5,6});

        try {
            MatrixIO.saveBin(A, "matrix_file.data");
            DMatrixRMaj B = MatrixIO.loadBin("matrix_file.data");
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
        csv_sparse();
        serializedBinary();
    }
}
