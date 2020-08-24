/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

import org.ejml.UtilEjml;
import org.ejml.data.DMatrix1Row;
import org.ejml.dense.row.mult.MatrixMatrixMult_MT_DDRM;
import org.jetbrains.annotations.Nullable;

import static org.ejml.UtilEjml.reshapeOrDeclare;

/**
 * Functions from {@link CommonOps_DDRM} with concurrent implementations.
 *
 * @author Peter Abeles
 */
public class CommonOps_MT_DDRM {
    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = a * b <br>
     * <br>
     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param output Where the results of the operation are stored. Modified.
     */
    public static <T extends DMatrix1Row> T mult(T a , T b , @Nullable T output )
    {
        output = reshapeOrDeclare(output,a,a.numRows,b.numCols);
        UtilEjml.checkSameInstance(a,output);
        UtilEjml.checkSameInstance(b,output);

        MatrixMatrixMult_MT_DDRM.mult_reorder(a,b,output);

        return output;
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = &alpha; * a * b <br>
     * <br>
     * c<sub>ij</sub> = &alpha; &sum;<sub>k=1:n</sub> { * a<sub>ik</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param alpha Scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param output Where the results of the operation are stored. Modified.
     */
    public static <T extends DMatrix1Row> T mult(double alpha , T a , T b , @Nullable T output )
    {
        output = reshapeOrDeclare(output,a,a.numRows,b.numCols);
        UtilEjml.checkSameInstance(a,output);
        UtilEjml.checkSameInstance(b,output);

        MatrixMatrixMult_MT_DDRM.mult_reorder(alpha,a,b,output);

        return output;
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = a<sup>T</sup> * b <br>
     * <br>
     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param output Where the results of the operation are stored. Modified.
     */
    public static <T extends DMatrix1Row> T multTransA(T a , T b , @Nullable T output )
    {
        output = reshapeOrDeclare(output,a,a.numCols,b.numCols);
        UtilEjml.checkSameInstance(a,output);
        UtilEjml.checkSameInstance(b,output);

        MatrixMatrixMult_MT_DDRM.multTransA_reorder(a,b,output);

        return output;
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = &alpha; * a<sup>T</sup> * b <br>
     * <br>
     * c<sub>ij</sub> = &alpha; &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param alpha Scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param output Where the results of the operation are stored. Modified.
     */
    public static <T extends DMatrix1Row> T multTransA(double alpha , T a , T b , @Nullable T output )
    {
        output = reshapeOrDeclare(output,a,a.numCols,b.numCols);
        UtilEjml.checkSameInstance(a,output);
        UtilEjml.checkSameInstance(b,output);

        MatrixMatrixMult_MT_DDRM.multTransA_reorder(alpha,a,b,output);

        return output;
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = a * b<sup>T</sup> <br>
     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param output Where the results of the operation are stored. Modified.
     */
    public static <T extends DMatrix1Row> T multTransB(T a , T b , @Nullable T output )
    {
        output = reshapeOrDeclare(output,a,a.numRows,b.numRows);
        UtilEjml.checkSameInstance(a,output);
        UtilEjml.checkSameInstance(b,output);

        MatrixMatrixMult_MT_DDRM.multTransB(a,b,output);

        return output;
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c =  &alpha; * a * b<sup>T</sup> <br>
     * c<sub>ij</sub> = &alpha; &sum;<sub>k=1:n</sub> {  a<sub>ik</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param alpha Scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param output Where the results of the operation are stored. Modified.
     */
    public static <T extends DMatrix1Row> T multTransB(double alpha , T a , T b , @Nullable T output )
    {
        output = reshapeOrDeclare(output,a,a.numRows,b.numRows);
        UtilEjml.checkSameInstance(a,output);
        UtilEjml.checkSameInstance(b,output);

        MatrixMatrixMult_MT_DDRM.multTransB(alpha,a,b,output);

        return output;
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
     * @param output Where the results of the operation are stored. Modified.
     */
    public static <T extends DMatrix1Row> T multTransAB(T a , T b , @Nullable T output )
    {
        output = reshapeOrDeclare(output,a,a.numCols,b.numRows);
        UtilEjml.checkSameInstance(a,output);
        UtilEjml.checkSameInstance(b,output);

        MatrixMatrixMult_MT_DDRM.multTransAB(a,b,output);

        return output;
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = &alpha; * a<sup>T</sup> * b<sup>T</sup><br>
     * c<sub>ij</sub> = &alpha; &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param alpha Scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param output Where the results of the operation are stored. Modified.
     */
    public static <T extends DMatrix1Row> T multTransAB(double alpha , T a , T b , @Nullable T output )
    {
        output = reshapeOrDeclare(output,a,a.numCols,b.numRows);
        UtilEjml.checkSameInstance(a,output);
        UtilEjml.checkSameInstance(b,output);

        MatrixMatrixMult_MT_DDRM.multTransAB(alpha,a,b,output);

        return output;
    }
}
