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
package org.ejml.dense.row.mult;

import java.util.Iterator;

import org.ejml.MatrixDimensionException;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrix1Row;
import org.ejml.data.DMatrixD1;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparse;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;

/**
 * <p>
 * This class contains various types of matrix vector multiplcation operations for {@link DMatrixRMaj}.
 * </p>
 * <p>
 * If a matrix has only one column or row then it is a vector. There are faster algorithms
 * that can be used to multiply matrices by vectors. Strangely, even though the operations
 * count smaller, the difference between this and a regular matrix multiply is insignificant
 * for large matrices. The smaller matrices there is about a 40% speed improvement. In
 * practice the speed improvement for smaller matrices is not noticeable unless 10s of millions
 * of matrix multiplications are being performed.
 * </p>
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"ForLoopReplaceableByForEach"})
public class MatrixVectorMult_DDRM {

    /**
     * <p>
     * Performs a matrix vector multiply.<br>
     * <br>
     * c = A * b <br>
     * and<br>
     * c = A * b<sup>T</sup> <br>
     * <br>
     * c<sub>i</sub> = Sum{ j=1:n, a<sub>ij</sub> * b<sub>j</sub>}<br>
     * <br>
     * where A is a matrix, b is a column or transposed row vector, and c is a column vector.
     * </p>
     *
     * @param A A matrix that is m by n. Not modified.
     * @param B A vector that has length n. Not modified.
     * @param C A column vector that has length m. Modified.
     */
    public static void mult( DMatrix1Row A, DMatrixD1 B, DMatrixD1 C ) {
        if (B.numRows == 1) {
            if (A.numCols != B.numCols) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else if (B.numCols == 1) {
            if (A.numCols != B.numRows) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else {
            throw new MatrixDimensionException("B is not a vector");
        }
        C.reshape(A.numRows, 1);

        if (A.numCols == 0) {
            CommonOps_DDRM.fill(C, 0);
            return;
        }

        int indexA = 0;
        int cIndex = 0;
        double b0 = B.get(0);
        for (int i = 0; i < A.numRows; i++) {
            double total = A.get(indexA++)*b0;

            for (int j = 1; j < A.numCols; j++) {
                total += A.get(indexA++)*B.get(j);
            }

            C.set(cIndex++, total);
        }
    }

    /**
     * <p>
     * Performs a matrix vector multiply.<br>
     * <br>
     * C = C + A * B <br>
     * or<br>
     * C = C + A * B<sup>T</sup> <br>
     * <br>
     * c<sub>i</sub> = Sum{ j=1:n, c<sub>i</sub> + a<sub>ij</sub> * b<sub>j</sub>}<br>
     * <br>
     * where A is a matrix, B is a column or transposed row vector, and C is a column vector.
     * </p>
     *
     * @param A A matrix that is m by n. Not modified.
     * @param B A vector that has length n. Not modified.
     * @param C A column vector that has length m. Modified.
     */
    public static void multAdd( DMatrix1Row A, DMatrixD1 B, DMatrixD1 C ) {
        if (B.numRows == 1) {
            if (A.numCols != B.numCols) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else if (B.numCols == 1) {
            if (A.numCols != B.numRows) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else {
            throw new MatrixDimensionException("B is not a vector");
        }
        if (A.numRows != C.getNumElements())
            throw new MatrixDimensionException("C is not compatible with A");

        if (A.numCols == 0) {
            return;
        }

        int indexA = 0;
        int cIndex = 0;
        for (int i = 0; i < A.numRows; i++) {
            double total = A.get(indexA++)*B.get(0);

            for (int j = 1; j < A.numCols; j++) {
                total += A.get(indexA++)*B.get(j);
            }

            C.plus(cIndex++, total);
        }
    }

    /**
     * <p>
     * Performs a matrix vector multiply.<br>
     * <br>
     * C = A<sup>T</sup> * B <br>
     * where B is a column vector.<br>
     * or<br>
     * C = A<sup>T</sup> * B<sup>T</sup> <br>
     * where B is a row vector. <br>
     * <br>
     * c<sub>i</sub> = Sum{ j=1:n, a<sub>ji</sub> * b<sub>j</sub>}<br>
     * <br>
     * where A is a matrix, B is a column or transposed row vector, and C is a column vector.
     * </p>
     * <p>
     * This implementation is optimal for small matrices. There is a huge performance hit when
     * used on large matrices due to CPU cache issues.
     * </p>
     *
     * @param A A matrix that is m by n. Not modified.
     * @param B A that has length m and is a column. Not modified.
     * @param C A column vector that has length n. Modified.
     */
    public static void multTransA_small( DMatrix1Row A, DMatrixD1 B, DMatrixD1 C ) {
        if (B.numRows == 1) {
            if (A.numRows != B.numCols) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else if (B.numCols == 1) {
            if (A.numRows != B.numRows) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else {
            throw new MatrixDimensionException("B is not a vector");
        }

        C.reshape(A.numCols, 1);

        int cIndex = 0;
        for (int i = 0; i < A.numCols; i++) {
            double total = 0.0;

            int indexA = i;
            for (int j = 0; j < A.numRows; j++) {
                total += A.get(indexA)*B.get(j);
                indexA += A.numCols;
            }

            C.set(cIndex++, total);
        }
    }

    /**
     * An alternative implementation of {@link #multTransA_small} that performs well on large
     * matrices. There is a relative performance hit when used on small matrices.
     *
     * @param A A matrix that is m by n. Not modified.
     * @param B A Vector that has length m. Not modified.
     * @param C A column vector that has length n. Modified.
     */
    public static void multTransA_reorder( DMatrix1Row A, DMatrixD1 B, DMatrixD1 C ) {
        if (B.numRows == 1) {
            if (A.numRows != B.numCols) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else if (B.numCols == 1) {
            if (A.numRows != B.numRows) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else {
            throw new MatrixDimensionException("B is not a vector");
        }
        C.reshape(A.numCols, 1);

        if (A.numRows == 0) {
            CommonOps_DDRM.fill(C, 0);
            return;
        }

        double B_val = B.get(0);
        for (int i = 0; i < A.numCols; i++) {
            C.set(i, A.get(i)*B_val);
        }

        int indexA = A.numCols;
        for (int i = 1; i < A.numRows; i++) {
            B_val = B.get(i);
            for (int j = 0; j < A.numCols; j++) {
                C.plus(j, A.get(indexA++)*B_val);
            }
        }
    }

    /**
     * <p>
     * Performs a matrix vector multiply.<br>
     * <br>
     * C = C + A<sup>T</sup> * B <br>
     * or<br>
     * C = C<sup>T</sup> + A<sup>T</sup> * B<sup>T</sup> <br>
     * <br>
     * c<sub>i</sub> = Sum{ j=1:n, c<sub>i</sub> + a<sub>ji</sub> * b<sub>j</sub>}<br>
     * <br>
     * where A is a matrix, B is a column or transposed row vector, and C is a column vector.
     * </p>
     * <p>
     * This implementation is optimal for small matrices. There is a huge performance hit when
     * used on large matrices due to CPU cache issues.
     * </p>
     *
     * @param A A matrix that is m by n. Not modified.
     * @param B A vector that has length m. Not modified.
     * @param C A column vector that has length n. Modified.
     */
    public static void multAddTransA_small( DMatrix1Row A, DMatrixD1 B, DMatrixD1 C ) {
        if (B.numRows == 1) {
            if (A.numRows != B.numCols) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else if (B.numCols == 1) {
            if (A.numRows != B.numRows) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else {
            throw new MatrixDimensionException("B is not a vector");
        }
        if (A.numCols != C.getNumElements())
            throw new MatrixDimensionException("C is not compatible with A");

        int cIndex = 0;
        for (int i = 0; i < A.numCols; i++) {
            double total = 0.0;

            int indexA = i;
            for (int j = 0; j < A.numRows; j++) {
                total += A.get(indexA)*B.get(j);
                indexA += A.numCols;
            }

            C.plus(cIndex++, total);
        }
    }

    /**
     * An alternative implementation of {@link #multAddTransA_small} that performs well on large
     * matrices. There is a relative performance hit when used on small matrices.
     *
     * @param A A matrix that is m by n. Not modified.
     * @param B A vector that has length m. Not modified.
     * @param C A column vector that has length n. Modified.
     */
    public static void multAddTransA_reorder( DMatrix1Row A, DMatrixD1 B, DMatrixD1 C ) {
        if (B.numRows == 1) {
            if (A.numRows != B.numCols) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else if (B.numCols == 1) {
            if (A.numRows != B.numRows) {
                throw new MatrixDimensionException("A and B are not compatible");
            }
        } else {
            throw new MatrixDimensionException("B is not a vector");
        }
        if (A.numCols != C.getNumElements())
            throw new MatrixDimensionException("C is not compatible with A");

        int indexA = 0;
        for (int j = 0; j < A.numRows; j++) {
            double B_val = B.get(j);
            for (int i = 0; i < A.numCols; i++) {
                C.plus(i, A.get(indexA++)*B_val);
            }
        }
    }

    /**
     * scalar = A<sup>T</sup>*B*C
     *
     * @param a (Input) vector
     * @param offsetA Input) first index in vector a
     * @param B (Input) Matrix
     * @param c (Output) vector
     * @param offsetC (Output) first index in vector c
     */
    public static double innerProduct( double a[], int offsetA,
                                       DMatrix1Row B,
                                       double c[], int offsetC ) {
        if (a.length - offsetA < B.numRows)
            throw new IllegalArgumentException("Length of 'a' isn't long enough");
        if (c.length - offsetC < B.numCols)
            throw new IllegalArgumentException("Length of 'c' isn't long enough");

        int cols = B.numCols;
        double output = 0;

        for (int k = 0; k < B.numCols; k++) {
            double sum = 0;
            for (int i = 0; i < B.numRows; i++) {
                sum += a[offsetA + i]*B.data[k + i*cols];
            }
            output += sum*c[offsetC + k];
        }

        return output;
    }

    /**
     * scalar = A<sup>T</sup>*B*C
     *
     * @param A (Input) A vector that has length m.
     * @param B (Input) A matrix that is m by n.
     * @param C (Input)  A vector that has length n.
     */
    public static double innerProduct(DMatrixSparse A, DMatrix1Row B, DMatrixSparse C) {
        checkInnerProductArguments(A, B, C);

        VectorEntry[] nzValuesC = nzValues(C);
        int sizeC = nzValuesC.length;

        double output = 0.0;
        for (Iterator<DMatrixSparse.CoordinateRealValue> i = A.createCoordinateIterator(); i.hasNext(); ) {
            DMatrixSparse.CoordinateRealValue c1 = i.next();
            double sum = 0.0;
            for (int j=0; j<sizeC; j++) {
                VectorEntry e2 = nzValuesC[j];
                sum += e2.value * B.unsafe_get(c1.row, e2.index);
            }
            output += c1.value * sum;
        }
        return output;
    }

    private static void checkInnerProductArguments(DMatrixSparse A, DMatrix1Row B, DMatrixSparse C) {
        UtilEjml.assertTrue(MatrixFeatures_DDRM.isVector(A), "'A' must be a vector");
        UtilEjml.assertShape(A.getNumElements(), B.numRows, "Length of 'A' vector not equal to number of rows in 'B' matrix");
        UtilEjml.assertTrue(MatrixFeatures_DDRM.isVector(C), "'C' must be a vector");
        UtilEjml.assertShape(C.getNumElements(), B.numCols, "Length of 'C' vector not equal to number of columns in 'B' matrix");
    }

    /**
     * scalar = A<sup>T</sup>*B*C
     *
     * @param A (Input) A vector that has length m.
     * @param B (Input) A matrix that is m by n.
     * @param C (Input)  A vector that has length n.
     */
    public static double innerProduct( DMatrixSparseCSC A, DMatrixRMaj B, DMatrixSparseCSC C) {
        checkInnerProductArguments(A, B, C);

        double output = 0.0;
        for (int i = 0; i < A.nz_length; i++) {
            int b_offset = A.nz_rows[i] * B.numCols;
            double sum = 0.0;
            for (int j = 0; j < C.nz_length; j++) {
                sum += C.nz_values[j] * B.data[b_offset + C.nz_rows[j]];
            }
            output += A.nz_values[i] * sum;
        }
        return output;
    }

    /**
     * scalar = A<sup>T</sup>*B*A
     *
     * @param A (Input) A vector that has length n.
     * @param B (Input) A matrix that is n by n and symmetrical.
     */
    public static double innerProductSelfSymmetrical(DMatrixSparse A, DMatrix1Row B) {
        checkInnerProductSelfSymmetricalArguments(A, B);

        VectorEntry[] nzValues = nzValues(A);

        double output = 0.0;
        int size = nzValues.length;
        for (int i = 0; i < size; i++) {
            VectorEntry e1 = nzValues[i];
            int index1 = e1.index;
            double value1 = e1.value;
            // matrix diagonal
            double diagonalValue = B.unsafe_get(index1, index1);
            double sum = 0.0;
            for (int j = i + 1; j < size; j++) {
                VectorEntry e2 = nzValues[j];
                sum += e2.value * B.unsafe_get(index1, e2.index);
            }
            output += Math.pow(value1, 2) * diagonalValue + value1 * (sum + sum);
        }
        return output;
    }

    private static void checkInnerProductSelfSymmetricalArguments(DMatrixSparse A, DMatrix1Row B) {
        UtilEjml.assertTrue(MatrixFeatures_DDRM.isVector(A), "'A' must be a vector");
        UtilEjml.assertTrue(MatrixFeatures_DDRM.isSquare(B), "'B' must be a square matrix");
        UtilEjml.assertShape(A.getNumElements(), B.numRows, "Length of 'A' vector not equal to number of rows / columns in 'B' matrix");
    }

    /**
     * scalar = A<sup>T</sup>*B*A
     *
     * @param A (Input) A vector that has length n.
     * @param B (Input) A matrix that is n by n and symmetrical.
     */
    public static double innerProductSelfSymmetrical(DMatrixSparseCSC A, DMatrixRMaj B) {
        checkInnerProductSelfSymmetricalArguments(A, B);

        double output = 0.0;
        for (int i = 0; i < A.nz_length; i++) {
            int index1 = A.nz_rows[i];
            double value1 = A.nz_values[i];
            int b_offset = index1 * B.numCols;
            double diagonalValue = B.data[b_offset + index1];
            double sum = 0.0;
            for (int j = i + 1; j < A.nz_length; j++) {
                sum += A.nz_values[j] * B.data[b_offset + A.nz_rows[j]];
            }
            output += Math.pow(value1, 2) * diagonalValue + value1 * (sum + sum);
        }
        return output;
    }

    private static class VectorEntry {
        public final int index;
        public final double value;

        public VectorEntry(int index, double value) {
            this.index = index;
            this.value = value;
        }
    }

    private static VectorEntry[] nzValues(DMatrixSparse A) {
        VectorEntry[] nzValues = new VectorEntry[A.getNonZeroLength()];
        int i = 0;
        for (Iterator<DMatrixSparse.CoordinateRealValue> it = A.createCoordinateIterator(); it.hasNext(); ) {
            DMatrixSparse.CoordinateRealValue crv = it.next();
            nzValues[i++] = new VectorEntry(crv.row, crv.value);
        }
        return nzValues;
    }
}
