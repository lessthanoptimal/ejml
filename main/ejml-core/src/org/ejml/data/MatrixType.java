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

    DDRM(true,true,64,DMatrixRMaj.class),
    FDRM(true,true,32,FMatrixRMaj.class),
    ZDRM(false,true,64,ZMatrixRMaj.class),
    CDRM(false,true,32,CMatrixRMaj.class),
    DSCC(true,false,64,DMatrixSparseCSC.class),
    FSCC(true,false,32,FMatrixSparseCSC.class),
    ZSCC(false,false,64,null),
    CSCC(false,false,32,null),
    DTRIPLET(false,false,64,DMatrixSparseTriplet.class),
    FTRIPLET(false,false,64,FMatrixSparseTriplet.class),
    UNSPECIFIED(false,false,0,null);

    boolean fixed;
    boolean dense;
    boolean real;
    int bits;
    Class classType;

    MatrixType(boolean real, boolean dense, int bits, Class type) {
        this(false,real,dense,bits,type);
    }

    MatrixType(boolean fixed, boolean real, boolean dense, int bits, Class type ) {
        this.real = real;
        this.fixed = fixed;
        this.dense = dense;
        this.bits = bits;
        this.classType = type;
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
        else
            throw new IllegalArgumentException("Unknown class");
    }

    /**
     * Looks up the default matrix type for the specified features
     */
    public static MatrixType lookup( boolean dense, boolean real, int bits ) {
        if( dense ) {
            if( real ) {
                if( bits == 64 ) {
                    return DDRM;
                } else {
                    return FDRM;
                }
            } else {
                if( bits == 64 ) {
                    return ZDRM;
                } else {
                    return CDRM;
                }
            }
        } else {
            if( real ) {
                if( bits == 64 ) {
                    return DSCC;
                } else {
                    return FSCC;
                }
            } else {
                throw new IllegalArgumentException("Complex sparse not yet supported");
            }
        }
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

    public Class getClassType() {
        return classType;
    }

    public Matrix create(int rows , int cols ) {
        switch( this ) {
            case DDRM: return new DMatrixRMaj(rows,cols);
            case FDRM: return new FMatrixRMaj(rows,cols);
            case ZDRM: return new ZMatrixRMaj(rows,cols);
            case CDRM: return new CMatrixRMaj(rows,cols);
            case DSCC: return new DMatrixSparseCSC(rows,cols);
            case FSCC: return new FMatrixSparseCSC(rows,cols);
//            case ZSCC: return new ZMatrixSparseCSC(rows,cols);
//            case CSCC: return new CMatrixSparseCSC(rows,cols);
        }

        throw new RuntimeException("Unknown Matrix Type "+this);
    }
}
