/*
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

/*
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
