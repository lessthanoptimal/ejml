/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.eig;

import org.ejml.data.Complex64F;


/**
 * @author Peter Abeles
 */
public class EigenvalueSmall {

    public Complex64F value0 = new Complex64F();
    public Complex64F value1 = new Complex64F();

    // if |a11-a22| >> |a12+a21| there might be a better way.  see pg371
    public void value2x2( double a11 , double a12, double a21 , double a22 )
    {
        // apply a rotators such that th a11 and a22 elements are the same
        double c,s;

        if( a12 + a21 == 0 ) { // is this pointless since
            c = s = 1.0/Math.sqrt(2);
        } else {
            double aa = (a11-a22);
            double bb = (a12+a21);

            double t_hat = aa/bb;
            double t = t_hat/(1.0+Math.sqrt(1.0+t_hat*t_hat));

            c = 1.0/Math.sqrt(1.0+t*t);
            s = c*t;
        }

        double c2 = c*c;
        double s2 = s*s;
        double cs = c*s;

        double b11 = c2*a11 + s2*a22 - cs*(a12+a21);
        double b12 = c2*a12 - s2*a21 + cs*(a11-a22);
        double b21 = c2*a21 - s2*a12 + cs*(a11-a22);
//        double b22 = c2*a22 + s2*a11 + cs*(a12+a21);

        // apply second rotator to make A upper triangular if real eigenvalues
        if( b21*b12 >= 0 ) {
            if( b12 == 0 ) {
                c = 0;
                s = 1;
            } else {
                s = Math.sqrt(b21/(b12+b21));
                c = Math.sqrt(b12/(b12+b21));
            }

//            c2 = b12;//c*c;
//            s2 = b21;//s*s;
            cs = c*s;

            a11 = b11 - cs*(b12 + b21);
//            a12 = c2*b12 - s2*b21;
//            a21 = c2*b21 - s2*b12;
            a22 = b11 + cs*(b12 + b21);

            value0.real = a11;
            value1.real = a22;

            value0.imaginary = value1.imaginary = 0;

        } else {
            value0.real = value1.real = b11;
            value0.imaginary = Math.sqrt(-b21*b12);
            value1.imaginary = -value0.imaginary;
        }
    }

    /**
     * Computes the eigenvalues of a 2 by 2 matrix using a faster but more prone to errors method.  This
     * is the typical method.
     */
    public void value2x2_fast( double a11 , double a12, double a21 , double a22 )
    {
        double left = (a11+a22)/2.0;
        double inside = 4.0*a12*a21 + (a11-a22)*(a11-a22);

        if( inside < 0 ) {
            value0.real = value1.real = left;
            value0.imaginary = Math.sqrt(-inside)/2.0;
            value1.imaginary = -value0.imaginary;
        } else {
            double right = Math.sqrt(inside)/2.0;
            value0.real = (left+right);
            value1.real = (left-right);
            value0.imaginary = value1.imaginary = 0.0;
        }
    }

    /**
     * Compute the symmetric eigenvalue using a slightly safer technique
     */
    // See page 385 of Fundamentals of Matrix Computations 2nd
    public void symm2x2_fast( double a11 , double a12, double a22 )
    {
//        double p = (a11 - a22)*0.5;
//        double r = Math.sqrt(p*p + a12*a12);
//
//        value0.real = a22 + a12*a12/(r-p);
//        value1.real = a22 - a12*a12/(r+p);
//    }
//
//    public void symm2x2_std( double a11 , double a12, double a22 )
//    {
        double left  = (a11+a22)*0.5;
        double b     = (a11-a22)*0.5;
        double right = Math.sqrt(b*b+a12*a12);
        value0.real = left + right;
        value1.real = left - right;
    }

}
