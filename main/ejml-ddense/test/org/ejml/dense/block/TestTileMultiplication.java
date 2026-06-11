/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.block;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTileMultiplication extends EjmlStandardJUnit {
    private static final int BLOCK_LENGTH = 4;

    /// Check the inner block multiplication functions against various shapes of inputs.
    @Test void allBlockMult() {
        checkBlockMultCase(BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH);
        checkBlockMultCase(BLOCK_LENGTH - 1, BLOCK_LENGTH, BLOCK_LENGTH);
        checkBlockMultCase(BLOCK_LENGTH - 1, BLOCK_LENGTH - 1, BLOCK_LENGTH);
        checkBlockMultCase(BLOCK_LENGTH - 1, BLOCK_LENGTH - 1, BLOCK_LENGTH - 1);
        checkBlockMultCase(BLOCK_LENGTH, BLOCK_LENGTH - 1, BLOCK_LENGTH - 1);
        checkBlockMultCase(BLOCK_LENGTH, BLOCK_LENGTH, BLOCK_LENGTH - 1);
    }

    private void checkBlockMultCase( int heightA, int widthA, int widthB ) {
        int numFound = 0;
        for (Method m : TileMultiplication_F64.class.getDeclaredMethods()) {
            if (!Modifier.isStatic(m.getModifiers()))
                continue;
            String name = m.getName();
            int operationType = name.contains("Plus") ? 1 : name.contains("Minus") ? -1 : 0;
            checkBlockMult(operationType, name.contains("TransA"), name.contains("TransB"), m, heightA, widthA, widthB);
            numFound++;
        }
        assertEquals(30, numFound);
    }

    private void checkBlockMult( int operationType, boolean transA, boolean transB, Method method,
                                 int heightA, int widthA, int widthB ) {
        Class<?>[] params = method.getParameterTypes();
        boolean hasAlpha = params[0] == double.class;
        // Strided overloads take an explicit stride per matrix; the convenience overloads do not.
        boolean hasStride = params.length - (hasAlpha ? 1 : 0) == 12;
        double alpha = 2.0;

        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(heightA, widthA, rand);
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(widthA, widthB, rand);
        DMatrixRMaj C = new DMatrixRMaj(heightA, widthB);
        CommonOps_DDRM.mult(operationType == -1 ? -1 : 1, A, B, C);

        DMatrixRMaj C_found = new DMatrixRMaj(heightA, widthB);
        if (operationType == 0)
            RandomMatrices_DDRM.fillUniform(C_found, rand);

        if (transA) CommonOps_DDRM.transpose(A);
        if (transB) CommonOps_DDRM.transpose(B);
        if (hasAlpha) CommonOps_DDRM.scale(alpha, C);

        // Strided methods are exercised with a row stride strictly larger than the matrix width.
        int pad = hasStride ? 3 : 0;
        Embedded a = embed(A, pad, 1);
        Embedded b = embed(B, pad, 2);
        Embedded c = embed(C_found, pad, 3);

        invoke(method, hasAlpha, hasStride, alpha, a, b, c, A.numRows, A.numCols, C_found.numCols);

        extract(c, C_found);
        assertTrue(MatrixFeatures_DDRM.isIdentical(C, C_found, UtilEjml.TEST_F64), method.getName());
    }

    private void invoke( Method m, boolean hasAlpha, boolean hasStride, double alpha,
                         Embedded a, Embedded b, Embedded c, int heightA, int widthA, int widthC ) {
        List<Object> args = new ArrayList<>();
        if (hasAlpha) args.add(alpha);
        args.add(a.data); args.add(b.data); args.add(c.data);
        args.add(heightA); args.add(widthA); args.add(widthC);
        if (hasStride) { args.add(a.stride); args.add(b.stride); args.add(c.stride); }
        args.add(a.offset); args.add(b.offset); args.add(c.offset);
        try {
            m.invoke(null, args.toArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /// Lay a row major matrix into a backing array with row stride = cols + pad, starting at offset.
    /// Everything outside the matrix (offset region and padding) is junk the op must not read or write.
    private Embedded embed( DMatrixRMaj M, int pad, int offset ) {
        int stride = M.numCols + pad;
        double[] data = new double[offset + M.numRows*stride];
        for (int i = 0; i < data.length; i++) data[i] = rand.nextDouble();
        for (int i = 0; i < M.numRows; i++)
            for (int j = 0; j < M.numCols; j++)
                data[offset + i*stride + j] = M.get(i, j);
        return new Embedded(data, stride, offset);
    }

    private void extract( Embedded e, DMatrixRMaj M ) {
        for (int i = 0; i < M.numRows; i++)
            for (int j = 0; j < M.numCols; j++)
                M.set(i, j, e.data[e.offset + i*e.stride + j]);
    }

    private static final class Embedded {
        final double[] data;
        final int stride;
        final int offset;

        Embedded( double[] data, int stride, int offset ) {
            this.data = data;
            this.stride = stride;
            this.offset = offset;
        }
    }
}
