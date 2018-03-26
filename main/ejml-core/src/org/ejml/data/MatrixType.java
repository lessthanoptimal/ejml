/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

package org.ejml.data;

/**
 * @author Peter Abeles
 */
public enum MatrixType {

    DDRM(true,true,64),
    FDRM(true,true,32),
    ZDRM(false,true,64),
    CDRM(false,true,32),
    DSCC(true,false,64),
    FSCC(true,false,32),
    ZSCC(false,false,64),
    CSCC(false,false,32),
    DSDI(true,false,64),
    FSDI(true,false,64),
    UNSPECIFIED(false,false,0);

    boolean fixed;
    boolean dense;
    boolean real;
    int bits;

    MatrixType(boolean real, boolean dense, int bits) {
        this(false,real,dense,bits);
    }

    MatrixType(boolean fixed, boolean real, boolean dense, int bits) {
        this.real = real;
        this.fixed = fixed;
        this.dense = dense;
        this.bits = bits;
    }

    public static MatrixType lookup( Class type ) {
        if( type == DMatrixRMaj.class )
            return MatrixType.DDRM;
        else if( type == FMatrixRMaj.class )
            return MatrixType.FDRM;
        else if( type == ZMatrixRMaj.class )
            return MatrixType.ZDRM;
        else if( type == CMatrixRMaj.class )
            return MatrixType.CDRM;
        else if( type == DMatrixSparseCSC.class )
            return MatrixType.DSCC;
        else if( type == FMatrixSparseCSC.class )
            return MatrixType.FSCC;
        else if( type == DMatrixDiag.class )
            return MatrixType.DSDI;
        else
            throw new IllegalArgumentException("Unknown class");
    }

    public boolean isReal() {
        return real;
    }

    public boolean isFixed() {
        return fixed;
    }

    public boolean isDense() {
        return dense;
    }

    public int getBits() {
        return bits;
    }
}
