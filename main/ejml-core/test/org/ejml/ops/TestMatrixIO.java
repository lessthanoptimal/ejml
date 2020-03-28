/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.sparse.triplet.MatrixFeatures_DSTL;
import org.ejml.sparse.triplet.RandomMatrices_DSTL;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestMatrixIO {

    Random rand = new Random(23424);

    @Test
    public void load_save_binary() throws IOException {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(6,3,rand);

        MatrixIO.saveBin(A, "temp.mat");

        DMatrixRMaj A_copy = MatrixIO.loadBin("temp.mat");

        assertTrue(A != A_copy);
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

        assertTrue(A != A_copy);
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

        assertTrue(A != A_copy);
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

        MatrixIO.print(out,mat,MatrixIO.DEFAULT_FLOAT_FORMAT);;
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

        MatrixIO.print(out,mat,MatrixIO.DEFAULT_FLOAT_FORMAT);;
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

        MatrixIO.printFancy(out,mat,11);;
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

        MatrixIO.printFancy(out,mat,11);;
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
