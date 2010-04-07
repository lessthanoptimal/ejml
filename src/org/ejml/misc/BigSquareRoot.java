/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

package org.ejml.misc;

import java.math.BigDecimal;
import java.math.BigInteger;


/**
 * <p>
 *                       Big Square Roots<br>
 *                     by Michael Gilleland<br>
 * <br>
 * http://www.merriampark.com/bigsqrt.htm   Copied on 2010-01-28<br>
 * <br>
 * The following source code (BigSquareRoot.java) is free for you to use in whatever way you wish,
 *  with no restrictions and no guarantees. Improvements and constructive suggestions are
 * welcome. Note that, when computing very large square roots, you should use a large scale.
 * For numbers of 100 digits, I use a scale of 50. 
 * </p>
 *
 * <p>
 * Some minor additional modifications by Peter Abeles.
 * </p>
 */
public class BigSquareRoot {

    private static BigDecimal ZERO = new BigDecimal ("0");
    private static BigDecimal ONE = new BigDecimal ("1");
    private static BigDecimal TWO = new BigDecimal ("2");
    public static final int DEFAULT_MAX_ITERATIONS = 50;
    public static final int DEFAULT_SCALE = 10;

    private BigDecimal error;
    private int iterations;
    private int scale = DEFAULT_SCALE;
    private int maxIterations = DEFAULT_MAX_ITERATIONS;


    public BigSquareRoot( int maxIterations , int scale ) {
        this.maxIterations = maxIterations;
        this.scale = scale;
    }

    public BigSquareRoot() {

    }

    //---------------------------------------
    // The error is the original number minus
    // (sqrt * sqrt). If the original number
    // was a perfect square, the error is 0.
    //---------------------------------------

    public BigDecimal getError () {
        return error;
    }

    //-------------------------------------------------------------
    // Number of iterations performed when square root was computed
    //-------------------------------------------------------------

    public int getIterations () {
        return iterations;
    }

    //------
    // Scale
    //------

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    //-------------------
    // Maximum iterations
    //-------------------

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    //--------------------------
    // Get initial approximation
    //--------------------------

    private static BigDecimal getInitialApproximation (BigDecimal n) {
        BigInteger integerPart = n.toBigInteger ();
        int length = integerPart.toString ().length ();
        if ((length % 2) == 0) {
            length--;
        }
        length /= 2;
        BigDecimal guess = ONE.movePointRight (length);
        return guess;
    }

    //----------------
    // Get square root
    //----------------

    public BigDecimal sqrt(BigInteger n) {
        return sqrt (new BigDecimal (n));
    }

    public BigDecimal sqrt(BigDecimal n) {

        // Make sure n is a positive number

        if (n.compareTo (ZERO) <= 0) {
            throw new IllegalArgumentException ();
        }

        BigDecimal initialGuess = getInitialApproximation (n);
        BigDecimal lastGuess = ZERO;
        BigDecimal guess = new BigDecimal (initialGuess.toString ());

        // Iterate

        iterations = 0;
        boolean more = true;
        while (more) {
            lastGuess = guess;
            guess = n.divide(guess, scale, BigDecimal.ROUND_HALF_UP);
            guess = guess.add(lastGuess);
            guess = guess.divide (TWO, scale, BigDecimal.ROUND_HALF_UP);
            error = n.subtract (guess.multiply (guess));
            if (++iterations >= maxIterations) {
                more = false;
            }
            else if (lastGuess.equals (guess)) {
                more = error.abs ().compareTo (ONE) >= 0;
            }
        }
        return guess;

    }


    //----------------------
    // Get random BigInteger
    //----------------------

    public static BigInteger getRandomBigInteger (int nDigits) {
        StringBuffer sb = new StringBuffer ();
        java.util.Random r = new java.util.Random ();
        for (int i = 0; i < nDigits; i++) {
            sb.append (r.nextInt (10));
        }
        return new BigInteger (sb.toString ());
    }

    //-----
    // Test
    //-----

    public static void main (String[] args) {

        BigInteger n;
        BigDecimal sqrt;
        BigSquareRoot app = new BigSquareRoot ();

        // Generate a random big integer with a hundred digits

        n = BigSquareRoot.getRandomBigInteger (100);

        // Build an array of test numbers

        String testNums[] = {"9", "30", "720", "1024", n.toString ()};

        for (int i = 0; i < testNums.length; i++) {
            n = new BigInteger (testNums[i]);
            if (i > 0) {
                System.out.println ("----------------------------");
            }
            System.out.println ("Computing the square root of");
            System.out.println (n.toString ());
            int length = n.toString ().length ();
            if (length > 20) {
                app.setScale (length / 2);
            }
            sqrt = app.sqrt(n);
            System.out.println ("Iterations " + app.getIterations ());
            System.out.println ("Sqrt " + sqrt.toString ());
            System.out.println (sqrt.multiply (sqrt).toString ());
            System.out.println (n.toString ());
            System.out.println ("Error " + app.getError ().toString ());
        }

    }

}
