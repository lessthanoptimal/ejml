package org.ejml;

import org.ejml.data.D1Matrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Benchmark that tests to see if referring the parent of the class versus the actual class
 * has any performance difference.
 *
 * @author Peter Abeles
 */
public class BenchmarkInheritanceCall {

    public static void elementMultA( D1Matrix64F a , D1Matrix64F b , D1Matrix64F c )
    {
        int length = a.getNumElements();
        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        for( int i = 0; i < length; i++ ) {
            dataC[i] = dataA[i] * dataB[i];
        }
    }

    public static void elementMultB( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        int length = a.getNumElements();
        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        for( int i = 0; i < length; i++ ) {
            dataC[i] = dataA[i] * dataB[i];
        }
    }

    public static void main( String args[] ) {
        Random rand = new Random(23234);

        DenseMatrix64F A = RandomMatrices.createRandom(2,2,rand);
        DenseMatrix64F B = RandomMatrices.createRandom(2,2,rand);
        DenseMatrix64F C = new DenseMatrix64F(2,2);

        int N = 200000000;

        long before = System.currentTimeMillis();
        for( int i = 0; i < N; i++ ) {
            elementMultA(A,B,C);
        }
        long after = System.currentTimeMillis();

        System.out.println("Parent:  "+(after-before));

        before = System.currentTimeMillis();
        for( int i = 0; i < N; i++ ) {
            elementMultB(A,B,C);
        }
        after = System.currentTimeMillis();

        System.out.println("Child:  "+(after-before));
    }
}
