/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.misc;

import org.ejml.data.DMatrixD1;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.ElementLocation;
import org.jetbrains.annotations.Nullable;

import static org.ejml.UtilEjml.checkSameShape;
import static org.ejml.UtilEjml.reshapeOrDeclare;

/**
 * Implementations of common ops routines for {@link DMatrixRMaj}. In general
 * there is no need to directly invoke these functions.
 *
 * @author Peter Abeles
 */
public class ImplCommonOps_DDRM {
    public static void extract( DMatrixRMaj src,
                                int srcY0, int srcX0,
                                DMatrixRMaj dst,
                                int dstY0, int dstX0,
                                int numRows, int numCols ) {
        for (int y = 0; y < numRows; y++) {
            int indexSrc = src.getIndex(y + srcY0, srcX0);
            int indexDst = dst.getIndex(y + dstY0, dstX0);
            System.arraycopy(src.data, indexSrc, dst.data, indexDst, numCols);
        }
    }

    public static double elementMax( DMatrixD1 a, @Nullable ElementLocation loc ) {
        final int size = a.getNumElements();

        int bestIndex = 0;
        double max = a.get(0);
        for (int i = 1; i < size; i++) {
            double val = a.get(i);
            if (val >= max) {
                bestIndex = i;
                max = val;
            }
        }

        if (loc != null) {
            loc.row = bestIndex/a.numCols;
            loc.col = bestIndex%a.numCols;
        }

        return max;
    }

    public static double elementMaxAbs( DMatrixD1 a, @Nullable ElementLocation loc ) {
        final int size = a.getNumElements();

        int bestIndex = 0;
        double max = 0;
        for (int i = 0; i < size; i++) {
            double val = Math.abs(a.get(i));
            if (val > max) {
                bestIndex = i;
                max = val;
            }
        }

        if (loc != null) {
            loc.row = bestIndex/a.numCols;
            loc.col = bestIndex%a.numCols;
        }

        return max;
    }

    public static double elementMin( DMatrixD1 a, @Nullable ElementLocation loc ) {
        final int size = a.getNumElements();

        int bestIndex = 0;
        double min = a.get(0);
        for (int i = 1; i < size; i++) {
            double val = a.get(i);
            if (val < min) {
                bestIndex = i;
                min = val;
            }
        }

        if (loc != null) {
            loc.row = bestIndex/a.numCols;
            loc.col = bestIndex%a.numCols;
        }

        return min;
    }

    public static double elementMinAbs( DMatrixD1 a, @Nullable ElementLocation loc ) {
        final int size = a.getNumElements();

        int bestIndex = 0;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < size; i++) {
            double val = Math.abs(a.get(i));
            if (val < min) {
                bestIndex = i;
                min = val;
            }
        }

        if (loc != null) {
            loc.row = bestIndex/a.numCols;
            loc.col = bestIndex%a.numCols;
        }

        return min;
    }

    public static void elementMult( DMatrixD1 A, DMatrixD1 B ) {
        checkSameShape(A, B, true);

        int length = A.getNumElements();

        for (int i = 0; i < length; i++) {
            A.times(i, B.get(i));
        }
    }

    public static <T extends DMatrixD1> T elementMult( T A, T B, @Nullable T output ) {
        checkSameShape(A, B, true);
        output = reshapeOrDeclare(output, A);

        int length = A.getNumElements();

        for (int i = 0; i < length; i++) {
            output.set(i, A.get(i)*B.get(i));
        }

        return output;
    }

    public static void elementDiv( DMatrixD1 A, DMatrixD1 B ) {
        checkSameShape(A, B, true);

        int length = A.getNumElements();

        for (int i = 0; i < length; i++) {
            A.div(i, B.get(i));
        }
    }

    public static <T extends DMatrixD1> T elementDiv( T A, T B, @Nullable T output ) {
        checkSameShape(A, B, true);
        output = reshapeOrDeclare(output, A);

        int length = A.getNumElements();

        for (int i = 0; i < length; i++) {
            output.set(i, A.get(i)/B.get(i));
        }

        return output;
    }

    public static double elementSum( DMatrixD1 mat ) {
        double total = 0;

        int size = mat.getNumElements();

        for (int i = 0; i < size; i++) {
            total += mat.get(i);
        }

        return total;
    }

    public static double elementSumAbs( DMatrixD1 mat ) {
        double total = 0;

        int size = mat.getNumElements();

        for (int i = 0; i < size; i++) {
            total += Math.abs(mat.get(i));
        }

        return total;
    }

    public static <T extends DMatrixD1> T elementPower( T A, T B, @Nullable T output ) {
        checkSameShape(A, B, true);
        output = reshapeOrDeclare(output, A);

        int size = A.getNumElements();
        for (int i = 0; i < size; i++) {
            output.data[i] = Math.pow(A.data[i], B.data[i]);
        }

        return output;
    }

    public static <T extends DMatrixD1> T elementPower( double a, T B, @Nullable T output ) {
        output = reshapeOrDeclare(output, B);

        int size = B.getNumElements();
        for (int i = 0; i < size; i++) {
            output.data[i] = Math.pow(a, B.data[i]);
        }

        return output;
    }

    public static <T extends DMatrixD1> T elementPower( T A, double b, @Nullable T output ) {
        output = reshapeOrDeclare(output, A);

        int size = A.getNumElements();
        for (int i = 0; i < size; i++) {
            output.data[i] = Math.pow(A.data[i], b);
        }

        return output;
    }

    public static <T extends DMatrixD1> T elementLog( T A, @Nullable T output ) {
        output = reshapeOrDeclare(output, A);

        int size = A.getNumElements();
        for (int i = 0; i < size; i++) {
            output.data[i] = Math.log(A.data[i]);
        }

        return output;
    }

    public static <T extends DMatrixD1> T elementExp( T A, @Nullable T output ) {
        output = reshapeOrDeclare(output, A);

        int size = A.getNumElements();
        for (int i = 0; i < size; i++) {
            output.data[i] = Math.exp(A.data[i]);
        }

        return output;
    }
}
