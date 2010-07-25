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

        MatrixIO.save(A,"temp.mat");

        DenseMatrix64F A_copy = MatrixIO.load("temp.mat");

        assertTrue(A != A_copy);
        assertTrue(MatrixFeatures.isIdentical(A,A_copy));

        // clean up
        File f = new File("temp.mat");
        assertTrue(f.exists());
        assertTrue(f.delete());
    }
}
