/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
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

package org.ejml.ops;

import org.ejml.EjmlStandardJUnit;
import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.*;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.sparse.triplet.MatrixFeatures_DSTL;
import org.ejml.sparse.triplet.RandomMatrices_DSTL;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestMatrixIO extends EjmlStandardJUnit {
    @Test
    public void matlabToDDRM() {
        DMatrixRMaj expected = new DMatrixRMaj(
                new double[][]
                {{6.00574613E-01 , 6.67556524E-01 , 7.30378628E-02},
                {2.99795449E-01 , 4.73318875E-01 , 1.65661752E-01},
                {1.80909991E-01 , 7.69749224E-01 , 2.40941823E-01},
                {3.18407416E-02 , 8.92113388E-01 , 2.63832986E-01},
                {6.72024488E-03 , 2.19532549E-01 , 9.71588373E-01}});

        String text =
                "[ 6.00574613E-01 , 6.67556524E-01 , 7.30378628E-02 ;\n" +
                "2.99795449E-01 , 4.73318875E-01 , 1.65661752E-01 ;\n" +
                "1.80909991E-01 , 7.69749224E-01 , 2.40941823E-01 ;\n" +
                "3.18407416E-02 , 8.92113388E-01 , 2.63832986E-01 ;\n" +
                "6.72024488E-03 , 2.19532549E-01 , 9.71588373E-01 ]";

        DMatrixRMaj found = MatrixIO.matlabToDDRM(text);
        assertTrue(MatrixFeatures_DDRM.isEquals(expected,found, UtilEjml.TEST_F64));
    }

    @Test
    public void load_save_matrix_market_F64() {
        var original = new DMatrixSparseTriplet(3,4,5);
        original.set(1,1,1.5);
        original.set(2,3,2.5);

        Writer output = new StringWriter();
        MatrixIO.saveMatrixMarket(original,"%.22f",output);
        Reader input = new CharArrayReader(output.toString().toCharArray());
        DMatrixSparseTriplet found = MatrixIO.loadMatrixMarketDSTR(input);

        EjmlUnitTests.assertEquals(original,found, UtilEjml.TEST_F64);
    }

    @Test
    public void load_save_matrix_market_F32() {
        var original = new FMatrixSparseTriplet(3,4,5);
        original.set(1,1,1.5f);
        original.set(2,3,2.5f);

        Writer output = new StringWriter();
        MatrixIO.saveMatrixMarket(original,"%.22f",output);
        Reader input = new CharArrayReader(output.toString().toCharArray());
        FMatrixSparseTriplet found = MatrixIO.loadMatrixMarketFSTR(input);

        EjmlUnitTests.assertEquals(original,found, UtilEjml.TEST_F32);
    }

    @Test
    public void load_save_matrix_market_DDRM() {
        var original = new DMatrixRMaj(3,4);
        RandomMatrices_DDRM.fillUniform(original, rand);

        var output = new StringWriter();
        MatrixIO.saveMatrixMarket(original,"%.22f",output);
        var input = new CharArrayReader(output.toString().toCharArray());
        DMatrixRMaj found = MatrixIO.loadMatrixMarketDDRM(input);

        EjmlUnitTests.assertEquals(original,found, UtilEjml.TEST_F64);
    }

    @Test
    public void load_save_matrix_market_FDRM() {
        var original = new FMatrixRMaj(3,4);
        RandomMatrices_FDRM.fillUniform(original, rand);

        var output = new StringWriter();
        MatrixIO.saveMatrixMarket(original,"%.22f",output);
        var input = new CharArrayReader(output.toString().toCharArray());
        FMatrixRMaj found = MatrixIO.loadMatrixMarketFDRM(input);

        EjmlUnitTests.assertEquals(original, found, UtilEjml.TEST_F32);
    }


    @Test
    public void load_matrix_market_vectorRow() {
        var text = """
                %%MatrixMarket matrix array real general
                %
                3 1
                1
                2
                3
                """;

        DMatrixSparseTriplet found = MatrixIO.loadMatrixMarketDSTR(new StringReader(text));
        assertEquals(3, found.numRows);
        assertEquals(1, found.numCols);
        assertEquals(1, found.nz_value.get(0));
        assertEquals(2, found.nz_value.get(1));
        assertEquals(3, found.nz_value.get(2));
    }

    @Test
    public void load_matrix_market_vectorCol() {
        var text = """
                %%MatrixMarket matrix array real general
                %
                1 3
                1
                2
                3
                """;
        DMatrixSparseTriplet found = MatrixIO.loadMatrixMarketDSTR(new StringReader(text));
        assertEquals(1, found.numRows);
        assertEquals(3, found.numCols);
        assertEquals(1, found.nz_value.get(0));
        assertEquals(2, found.nz_value.get(1));
        assertEquals(3, found.nz_value.get(2));
    }

    @Test
    public void load_save_matlab_missing_dep_msg() throws IOException {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(6, 3, rand);
        String msg = assertThrows(IllegalStateException.class, () -> MatrixIO.saveMatlab(A, "temp.mat"))
                .getMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("mfl-ejml"));
        assertEquals(msg, assertThrows(IllegalStateException.class, () -> MatrixIO.loadMatlab("temp.mat"))
                .getMessage());
    }

    @Test
    public void load_save_binary() throws IOException {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(6,3,rand);

        MatrixIO.saveBin(A, "temp.mat");

        DMatrixRMaj A_copy = MatrixIO.loadBin("temp.mat");

        assertNotSame(A, A_copy);
        assertTrue(MatrixFeatures_DDRM.isEquals(A,A_copy));

        // clean up
        File f = new File("temp.mat");
        assertTrue(f.exists());
        assertTrue(f.delete());
    }

    @Test
    public void load_save_dense_csv() throws IOException {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(6,3,rand);

        MatrixIO.saveDenseCSV(A,"temp.csv");

        DMatrixRMaj A_copy = MatrixIO.loadCSV("temp.csv",true);

        assertNotSame(A, A_copy);
        assertTrue(MatrixFeatures_DDRM.isEquals(A,A_copy));

        // clean up
        File f = new File("temp.csv");
        assertTrue(f.exists());
        assertTrue(f.delete());
    }

    @Test
    public void load_save_float_csv() throws IOException {
        DMatrixSparseTriplet A = RandomMatrices_DSTL.uniform(10,8,15,-1,1,rand);

        MatrixIO.saveSparseCSV(A,"temp.csv");

        DMatrixSparseTriplet A_copy = MatrixIO.loadCSV("temp.csv",true);

        assertNotSame(A, A_copy);
        assertTrue(MatrixFeatures_DSTL.isEquals(A,A_copy));

        // clean up
        File f = new File("temp.csv");
        assertTrue(f.exists());
        assertTrue(f.delete());
    }

    @Test
    public void print_DMatrix() {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bao);

        DMatrixRMaj mat = new DMatrixRMaj(2,1);

        mat.set(0,0,1.1);
        mat.set(1,0,-2.2);

        MatrixIO.print(out,mat,MatrixIO.DEFAULT_FLOAT_FORMAT);
        out.flush();

        // On windows \r is added to the string
        String found = bao.toString().replace("\r","");
        String expected =
                "Type = DDRM , rows = 2 , cols = 1\n" +
                " 1.1000E+00 \n" +
                "-2.2000E+00 \n";

        assertEquals(expected,found);
    }

    @Test
    public void print_ZMatrix() {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bao);

        ZMatrixRMaj mat = new ZMatrixRMaj(2,1);

        mat.set(0,0,1,1.5);
        mat.set(1,0,2.0,-2.5);

        MatrixIO.print(out,mat,MatrixIO.DEFAULT_FLOAT_FORMAT);
        out.flush();

        // On windows \r is added to the string
        String found = bao.toString().replace("\r","");
        String expected =
                "Type = ZDRM , rows = 2 , cols = 1\n" +
                " 1.0000E+00 +  1.5000E+00i\n" +
                " 2.0000E+00 + -2.5000E+00i\n";

        assertEquals(expected,found);
    }

    @Test
    public void printFancy_DMatrix() {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bao);

        DMatrixRMaj mat = new DMatrixRMaj(2,1);

        mat.set(0,0,1.1);
        mat.set(1,0,-2.2);

        MatrixIO.printFancy(out,mat,11);
        out.flush();

        // On windows \r is added to the string
        String found = bao.toString().replace("\r","");
        String expected =
                "Type = DDRM , rows = 2 , cols = 1\n" +
                " 1.1       \n" +
                "-2.2       \n";

        assertEquals(expected,found);
    }

    @Test
    public void printFancy_ZMatrix() {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bao);

        ZMatrixRMaj mat = new ZMatrixRMaj(2,1);

        mat.set(0,0,1,1.5);
        mat.set(1,0,2.0,-2.5);

        MatrixIO.printFancy(out,mat,11);
        out.flush();

        // On windows \r is added to the string
        String found = bao.toString().replace("\r","");
        String expected =
                "Type = ZDRM , rows = 2 , cols = 1\n" +
                " 1          +  1.5i       \n" +
                " 2          + -2.5i       \n";

        assertEquals(expected,found);

    }
}
