package org.ejml.alg.dense.decomposition;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestDecompositionFactory {

    Random rand = new Random(234234);

    @Test
    public void quality_eig() {
        // I'm assuming it can process this matrix with no problems
        DenseMatrix64F A = RandomMatrices.createSymmetric(5,-1,1,rand);

        EigenDecomposition eig = DecompositionFactory.eig();

        assertTrue(eig.decompose(A));

        double origQuality = DecompositionFactory.quality(A,eig);

        // Mess up the EVD so that it will be of poor quality
        eig.getEigenVector(2).set(2,0,5);

        double modQuality = DecompositionFactory.quality(A,eig);

        assertTrue(origQuality < modQuality);
        assertTrue(origQuality < 1e-14);
    }

    @Test
    public void quality_svd() {
        // I'm assuming it can process this matrix with no problems
        DenseMatrix64F A = RandomMatrices.createRandom(4,5,rand);

        SingularValueDecomposition svd = DecompositionFactory.svd();

        assertTrue(svd.decompose(A));

        double origQuality = DecompositionFactory.quality(A,svd);

        // Mess up the SVD so that it will be of poor quality
        svd.getSingularValues()[2] = 5;

        double modQuality = DecompositionFactory.quality(A,svd);

        assertTrue(origQuality < modQuality);
        assertTrue(origQuality < 1e-14);
    }
}
