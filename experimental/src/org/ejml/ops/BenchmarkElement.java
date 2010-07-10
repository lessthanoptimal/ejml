package org.ejml.ops;

import org.ejml.data.DenseMatrix64F;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkElement {
    static Random rand = new Random(234);

    public static void main( String args[] ) {
        long N = 10000000;

        double num = 2.5;

        DenseMatrix64F A = RandomMatrices.createRandom(10,10,rand);

        long timeBefore = System.currentTimeMillis();
        for( int i = 0; i < N; i++ ) {
            CommonOps.div(num,A);
        }
        long timeAfter = System.currentTimeMillis();

        System.out.println("div = "+(timeAfter-timeBefore));

        A = RandomMatrices.createRandom(10,10,rand);

        timeBefore = System.currentTimeMillis();
        for( int i = 0; i < N; i++ ) {
            CommonOps.scale(num,A);
        }
        timeAfter = System.currentTimeMillis();

        System.out.println("scale = "+(timeAfter-timeBefore));
    }
}
