/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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
package org.ejml.ops;

import org.ejml.data.*;

/**
 * Contains a function to convert from one matrix type into another
 *
 * @author Peter Abeles
 */
public class ConvertMatrixType {
    /**
     * Converts a matrix of one data type into another data type. If no conversion is known then an exception
     * is thrown.
     * The result is always a new matrix instance, even if the matrix already has the desired type.
     *
     * @return The converted matrix
     */
    @SuppressWarnings("MissingCasesInEnumSwitch")
    public static Matrix convert( Matrix matrix, MatrixType desired ) {
        Matrix m = null;

        switch (matrix.getType()) {
            case DDRM: {
                switch (desired) {
                    case DDRM: {
                        m = matrix.copy();
                    } break;
                    case FDRM: {
                        m = new FMatrixRMaj(matrix.getNumRows(), matrix.getNumCols());
                        ConvertMatrixData.convert((DMatrixRMaj)matrix, (FMatrixRMaj)m);
                    } break;

                    case ZDRM: {
                        m = new ZMatrixRMaj(matrix.getNumRows(), matrix.getNumCols());
                        ConvertMatrixData.convert((DMatrixRMaj)matrix, (ZMatrixRMaj)m);
                    } break;

                    case CDRM: {
                        m = new CMatrixRMaj(matrix.getNumRows(), matrix.getNumCols());
                        ConvertMatrixData.convert((DMatrixRMaj)matrix, (CMatrixRMaj)m);
                    } break;

                    case DSCC: {
                        m = new DMatrixSparseCSC(matrix.getNumRows(), matrix.getNumCols());
                        DConvertMatrixStruct.convert((DMatrixRMaj)matrix, (DMatrixSparseCSC)m);
                    } break;

                    case FSCC: {
                        m = new FMatrixSparseCSC(matrix.getNumRows(), matrix.getNumCols());
                        ConvertMatrixData.convert((DMatrixRMaj)matrix, (FMatrixSparseCSC)m);
                    } break;
                }
            }
            break;

            case FDRM: {
                switch (desired) {
                    case FDRM: {
                        m = matrix.copy();
                    } break;
                    case DDRM: {
                        m = new DMatrixRMaj(matrix.getNumRows(), matrix.getNumCols());
                        ConvertMatrixData.convert((FMatrixRMaj)matrix, (DMatrixRMaj)m);
                    } break;

                    case ZDRM: {
                        m = new ZMatrixRMaj(matrix.getNumRows(), matrix.getNumCols());
                        ConvertMatrixData.convert((FMatrixRMaj)matrix, (ZMatrixRMaj)m);
                    } break;

                    case CDRM: {
                        m = new CMatrixRMaj(matrix.getNumRows(), matrix.getNumCols());
                        ConvertMatrixData.convert((FMatrixRMaj)matrix, (CMatrixRMaj)m);
                    } break;

                    case DSCC: {
                        m = new DMatrixSparseCSC(matrix.getNumRows(), matrix.getNumCols());
                        ConvertMatrixData.convert((FMatrixRMaj)matrix, (DMatrixSparseCSC)m);
                    } break;

                    case FSCC: {
                        m = new FMatrixSparseCSC(matrix.getNumRows(), matrix.getNumCols());
                        FConvertMatrixStruct.convert((FMatrixRMaj)matrix, (FMatrixSparseCSC)m);
                    } break;
                }
            }
            break;

            case ZDRM: {
                switch (desired) {
                    case ZDRM: {
                        m = matrix.copy();
                    } break;
                    case CDRM: {
                        m = new CMatrixRMaj(matrix.getNumRows(), matrix.getNumCols());
                        ConvertMatrixData.convert((ZMatrixRMaj)matrix, (CMatrixRMaj)m);
                    } break;
                }
            }
            break;

            case CDRM: {
                switch (desired) {
                    case CDRM: {
                        m = matrix.copy();
                    } break;
                    case ZDRM: {
                        m = new ZMatrixRMaj(matrix.getNumRows(), matrix.getNumCols());
                        ConvertMatrixData.convert((CMatrixRMaj)matrix, (ZMatrixRMaj)m);
                    } break;
                }
            }
            break;

            case DSCC: {
                switch (desired) {
                    case DSCC: {
                        m = matrix.copy();
                    } break;
                    case DDRM: {
                        m = new DMatrixRMaj(matrix.getNumRows(), matrix.getNumCols());
                        DConvertMatrixStruct.convert((DMatrixSparseCSC)matrix, (DMatrixRMaj)m);
                    } break;

                    case FDRM: {
                        m = new FMatrixRMaj(matrix.getNumRows(), matrix.getNumCols());
                        ConvertMatrixData.convert((DMatrixSparseCSC)matrix, (FMatrixRMaj)m);
                    } break;

                    case ZDRM: {
                        m = new ZMatrixRMaj(matrix.getNumRows(), matrix.getNumCols());
                        ConvertMatrixData.convert((DMatrixSparseCSC)matrix, (ZMatrixRMaj)m);
                    } break;

                    case CDRM: {
                        m = new CMatrixRMaj(matrix.getNumRows(), matrix.getNumCols());
                        ConvertMatrixData.convert((DMatrixSparseCSC)matrix, (CMatrixRMaj)m);
                    } break;

                    case FSCC: {
                        m = new FMatrixSparseCSC(matrix.getNumRows(), matrix.getNumCols());
                        ConvertMatrixData.convert((DMatrixSparseCSC)matrix, (FMatrixSparseCSC)m);
                    } break;
                }
            }
            break;

            case FSCC: {
                switch (desired) {
                    case FSCC: {
                        m = matrix.copy();
                    } break;
                    case DDRM: {
                        m = new DMatrixRMaj(matrix.getNumRows(), matrix.getNumCols());
                        ConvertMatrixData.convert((FMatrixSparseCSC)matrix, (DMatrixRMaj)m);
                    } break;

                    case FDRM: {
                        m = new FMatrixRMaj(matrix.getNumRows(), matrix.getNumCols());
                        FConvertMatrixStruct.convert((FMatrixSparseCSC)matrix, (FMatrixRMaj)m);
                    } break;

                    case ZDRM: {
                        m = new ZMatrixRMaj(matrix.getNumRows(), matrix.getNumCols());
                        ConvertMatrixData.convert((FMatrixSparseCSC)matrix, (ZMatrixRMaj)m);
                    } break;

                    case CDRM: {
                        m = new CMatrixRMaj(matrix.getNumRows(), matrix.getNumCols());
                        ConvertMatrixData.convert((FMatrixSparseCSC)matrix, (CMatrixRMaj)m);
                    } break;

                    case DSCC: {
                        m = new DMatrixSparseCSC(matrix.getNumRows(), matrix.getNumCols());
                        ConvertMatrixData.convert((FMatrixSparseCSC)matrix, (DMatrixSparseCSC)m);
                    } break;
                }
            }
            break;
        }

        if (m == null) {
            throw new IllegalArgumentException("Unknown " + matrix.getType() + " " + desired);
        }

        return m;
    }
}
