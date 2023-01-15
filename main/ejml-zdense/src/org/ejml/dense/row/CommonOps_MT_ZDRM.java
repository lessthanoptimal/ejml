/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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
package org.ejml.dense.row;

import org.ejml.EjmlParameters;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.mult.MatrixMatrixMult_MT_ZDRM;

/**
 * Functions from {@link CommonOps_ZDRM} with concurrent implementations.
 *
 * @author Peter Abeles
 */
public class CommonOps_MT_ZDRM {
    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = a * b <br>
     * <br>
     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { * a<sub>ik</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void mult( ZMatrixRMaj a, ZMatrixRMaj b, ZMatrixRMaj c ) {
        if (b.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH) {
            MatrixMatrixMult_MT_ZDRM.mult_reorder(a, b, c);
        } else {
            MatrixMatrixMult_MT_ZDRM.mult_small(a, b, c);
        }
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = &alpha; * a * b <br>
     * <br>
     * c<sub>ij</sub> = &alpha; &sum;<sub>k=1:n</sub> { * a<sub>ik</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param realAlpha real component of scaling factor.
     * @param imgAlpha imaginary component of scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void mult( double realAlpha, double imgAlpha, ZMatrixRMaj a, ZMatrixRMaj b, ZMatrixRMaj c ) {
        if (b.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH) {
            MatrixMatrixMult_MT_ZDRM.mult_reorder(realAlpha, imgAlpha, a, b, c);
        } else {
            MatrixMatrixMult_MT_ZDRM.mult_small(realAlpha, imgAlpha, a, b, c);
        }
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + a * b<br>
     * c<sub>ij</sub> = c<sub>ij</sub> + &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAdd( ZMatrixRMaj a, ZMatrixRMaj b, ZMatrixRMaj c ) {
        if (b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
            MatrixMatrixMult_MT_ZDRM.multAdd_reorder(a, b, c);
        } else {
            MatrixMatrixMult_MT_ZDRM.multAdd_small(a, b, c);
        }
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + &alpha; * a * b<br>
     * c<sub>ij</sub> = c<sub>ij</sub> +  &alpha; * &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param realAlpha real component of scaling factor.
     * @param imgAlpha imaginary component of scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAdd( double realAlpha, double imgAlpha, ZMatrixRMaj a, ZMatrixRMaj b, ZMatrixRMaj c ) {
        if (b.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH) {
            MatrixMatrixMult_MT_ZDRM.multAdd_reorder(realAlpha, imgAlpha, a, b, c);
        } else {
            MatrixMatrixMult_MT_ZDRM.multAdd_small(realAlpha, imgAlpha, a, b, c);
        }
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = a<sup>H</sup> * b <br>
     * <br>
     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransA( ZMatrixRMaj a, ZMatrixRMaj b, ZMatrixRMaj c ) {
        if (a.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH ||
                b.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH) {
            MatrixMatrixMult_MT_ZDRM.multTransA_reorder(a, b, c);
        } else {
            MatrixMatrixMult_MT_ZDRM.multTransA_small(a, b, c);
        }
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = &alpha; * a<sup>H</sup> * b <br>
     * <br>
     * c<sub>ij</sub> = &alpha; &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param realAlpha Real component of scaling factor.
     * @param imagAlpha Imaginary component of scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransA( double realAlpha, double imagAlpha, ZMatrixRMaj a, ZMatrixRMaj b, ZMatrixRMaj c ) {
        // TODO add a matrix vectory multiply here
        if (a.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH ||
                b.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH) {
            MatrixMatrixMult_MT_ZDRM.multTransA_reorder(realAlpha, imagAlpha, a, b, c);
        } else {
            MatrixMatrixMult_MT_ZDRM.multTransA_small(realAlpha, imagAlpha, a, b, c);
        }
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = a * b<sup>H</sup> <br>
     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransB( ZMatrixRMaj a, ZMatrixRMaj b, ZMatrixRMaj c ) {
        MatrixMatrixMult_MT_ZDRM.multTransB(a, b, c);
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c =  &alpha; * a * b<sup>H</sup> <br>
     * c<sub>ij</sub> = &alpha; &sum;<sub>k=1:n</sub> {  a<sub>ik</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param realAlpha Real component of scaling factor.
     * @param imagAlpha Imaginary component of scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransB( double realAlpha, double imagAlpha, ZMatrixRMaj a, ZMatrixRMaj b, ZMatrixRMaj c ) {
        // TODO add a matrix vectory multiply here
        MatrixMatrixMult_MT_ZDRM.multTransB(realAlpha, imagAlpha, a, b, c);
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = a<sup>T</sup> * b<sup>T</sup><br>
     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransAB( ZMatrixRMaj a, ZMatrixRMaj b, ZMatrixRMaj c ) {
        MatrixMatrixMult_MT_ZDRM.multTransAB(a, b, c);
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = &alpha; * a<sup>H</sup> * b<sup>H</sup><br>
     * c<sub>ij</sub> = &alpha; &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param realAlpha Real component of scaling factor.
     * @param imagAlpha Imaginary component of scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransAB( double realAlpha, double imagAlpha, ZMatrixRMaj a, ZMatrixRMaj b, ZMatrixRMaj c ) {
        MatrixMatrixMult_MT_ZDRM.multTransAB(realAlpha, imagAlpha, a, b, c);
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + a<sup>H</sup> * b<br>
     * c<sub>ij</sub> = c<sub>ij</sub> + &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransA( ZMatrixRMaj a, ZMatrixRMaj b, ZMatrixRMaj c ) {
        if (a.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH ||
                b.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH) {
            MatrixMatrixMult_MT_ZDRM.multAddTransA_reorder(a, b, c);
        } else {
            MatrixMatrixMult_MT_ZDRM.multAddTransA_small(a, b, c);
        }
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + &alpha; * a<sup>H</sup> * b<br>
     * c<sub>ij</sub> =c<sub>ij</sub> +  &alpha; * &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param realAlpha Real component of scaling factor.
     * @param imagAlpha Imaginary component of scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransA( double realAlpha, double imagAlpha, ZMatrixRMaj a, ZMatrixRMaj b, ZMatrixRMaj c ) {
        // TODO add a matrix vectory multiply here
        if (a.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH ||
                b.numCols >= EjmlParameters.CMULT_COLUMN_SWITCH) {
            MatrixMatrixMult_MT_ZDRM.multAddTransA_reorder(realAlpha, imagAlpha, a, b, c);
        } else {
            MatrixMatrixMult_MT_ZDRM.multAddTransA_small(realAlpha, imagAlpha, a, b, c);
        }
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + a * b<sup>H</sup> <br>
     * c<sub>ij</sub> = c<sub>ij</sub> + &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransB( ZMatrixRMaj a, ZMatrixRMaj b, ZMatrixRMaj c ) {
        MatrixMatrixMult_MT_ZDRM.multAddTransB(a, b, c);
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + &alpha; * a * b<sup>H</sup><br>
     * c<sub>ij</sub> = c<sub>ij</sub> + &alpha; * &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param realAlpha Real component of scaling factor.
     * @param imagAlpha Imaginary component of scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransB( double realAlpha, double imagAlpha, ZMatrixRMaj a, ZMatrixRMaj b, ZMatrixRMaj c ) {
        // TODO add a matrix vectory multiply here
        MatrixMatrixMult_MT_ZDRM.multAddTransB(realAlpha, imagAlpha, a, b, c);
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + a<sup>H</sup> * b<sup>H</sup><br>
     * c<sub>ij</sub> = c<sub>ij</sub> + &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not Modified.
     * @param b The right matrix in the multiplication operation. Not Modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransAB( ZMatrixRMaj a, ZMatrixRMaj b, ZMatrixRMaj c ) {
        MatrixMatrixMult_MT_ZDRM.multAddTransAB(a, b, c);
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + &alpha; * a<sup>H</sup> * b<sup>H</sup><br>
     * c<sub>ij</sub> = c<sub>ij</sub> + &alpha; * &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param realAlpha Real component of scaling factor.
     * @param imagAlpha Imaginary component of scaling factor.
     * @param a The left matrix in the multiplication operation. Not Modified.
     * @param b The right matrix in the multiplication operation. Not Modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransAB( double realAlpha, double imagAlpha, ZMatrixRMaj a, ZMatrixRMaj b, ZMatrixRMaj c ) {
        MatrixMatrixMult_MT_ZDRM.multAddTransAB(realAlpha, imagAlpha, a, b, c);
    }
}
