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

package org.ejml.data;

import java.io.Serializable;

/**
 * <p>
 * Represents a complex number using 64bit floating point numbers.  A complex number is composed of
 * a real and imaginary components.
 * </p>
 */
public class Complex64F implements Serializable {
    public double real;
    public double imaginary;

    public Complex64F(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    public Complex64F() {
    }

    public double getReal() {
        return real;
    }

    public void setReal(double real) {
        this.real = real;
    }

    public double getImaginary() {
        return imaginary;
    }

    public void setImaginary(double imaginary) {
        this.imaginary = imaginary;
    }

    public void set(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    public boolean isReal() {
        return imaginary == 0.0;
    }

    public String toString() {
        if( imaginary == 0 ) {
            return ""+real;
        } else {
            return real+" "+imaginary+"i";
        }
    }
}
