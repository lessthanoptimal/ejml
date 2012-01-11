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

package org.ejml.ops;

import org.ejml.data.DenseMatrix64F;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestMatrixIO {

    Random rand = new Random(23424);

    @Test
    public void load_save_binary() throws IOException {
        DenseMatrix64F A = RandomMatrices.createRandom(6,3,rand);

        MatrixIO.saveBin(A, "temp.mat");

        DenseMatrix64F A_copy = MatrixIO.loadBin("temp.mat");

        assertTrue(A != A_copy);
        assertTrue(MatrixFeatures.isEquals(A,A_copy));

        // clean up
        File f = new File("temp.mat");
        assertTrue(f.exists());
        assertTrue(f.delete());
    }

    @Test
    public void load_save_csv() throws IOException {
        DenseMatrix64F A = RandomMatrices.createRandom(6,3,rand);

        MatrixIO.saveCSV(A,"temp.csv");

        DenseMatrix64F A_copy = MatrixIO.loadCSV("temp.csv");

        assertTrue(A != A_copy);
        assertTrue(MatrixFeatures.isEquals(A,A_copy));

        // clean up
        File f = new File("temp.csv");
        assertTrue(f.exists());
        assertTrue(f.delete());
    }
}
