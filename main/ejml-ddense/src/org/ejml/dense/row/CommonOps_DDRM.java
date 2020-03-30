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

import org.ejml.EjmlParameters;
import org.ejml.LinearSolverSafe;
import org.ejml.MatrixDimensionException;
import org.ejml.UtilEjml;
import org.ejml.data.*;
import org.ejml.dense.row.decomposition.TriangularSolver_DDRM;
import org.ejml.dense.row.decomposition.lu.LUDecompositionAlt_DDRM;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.dense.row.linsol.chol.LinearSolverChol_DDRM;
import org.ejml.dense.row.linsol.lu.LinearSolverLu_DDRM;
import org.ejml.dense.row.linsol.svd.SolvePseudoInverseSvd_DDRM;
import org.ejml.dense.row.misc.*;
import org.ejml.dense.row.mult.MatrixMatrixMult_DDRM;
import org.ejml.dense.row.mult.MatrixMultProduct_DDRM;
import org.ejml.dense.row.mult.MatrixVectorMult_DDRM;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.ejml.interfaces.linsol.ReducedRowEchelonForm_F64;

import javax.annotation.Nullable;
import java.util.Arrays;

import static org.ejml.UtilEjml.stringShapes;

/**
 * <p>
 * Common matrix operations are contained here.  Which specific underlying algorithm is used
 * is not specified just the out come of the operation.  Nor should calls to these functions
 * reply on the underlying implementation.  Which algorithm is used can depend on the matrix
 * being passed in.
 * </p>
 * <p>
 * For more exotic and specialized generic operations see {@link SpecializedOps_DDRM}.
 * </p>
 * @see MatrixMatrixMult_DDRM
 * @see MatrixVectorMult_DDRM
 * @see SpecializedOps_DDRM
 * @see MatrixFeatures_DDRM
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"ForLoopReplaceableByForEach"})
public class CommonOps_DDRM {
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
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void mult(DMatrix1Row a , DMatrix1Row b , DMatrix1Row c )
    {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);

        if( b.numCols == 1 ) {
            MatrixVectorMult_DDRM.mult(a, b, c);
        } else if( b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
            MatrixMatrixMult_DDRM.mult_reorder(a,b,c);
        } else {
            MatrixMatrixMult_DDRM.mult_small(a,b,c);
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
     * @param alpha Scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void mult(double alpha , DMatrix1Row a , DMatrix1Row b , DMatrix1Row c )
    {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);

        // TODO add a matrix vectory multiply here
        if( b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
            MatrixMatrixMult_DDRM.mult_reorder(alpha, a, b, c);
        } else {
            MatrixMatrixMult_DDRM.mult_small(alpha,a,b,c);
        }
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
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransA(DMatrix1Row a , DMatrix1Row b , DMatrix1Row c )
    {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);

        if( b.numCols == 1 ) {
            // todo check a.numCols == 1 and do inner product?
            // there are significantly faster algorithms when dealing with vectors
            if( a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
                MatrixVectorMult_DDRM.multTransA_reorder(a,b,c);
            } else {
                MatrixVectorMult_DDRM.multTransA_small(a,b,c);
            }
        } else if( a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ||
                b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH  ) {
            MatrixMatrixMult_DDRM.multTransA_reorder(a, b, c);
        } else {
            MatrixMatrixMult_DDRM.multTransA_small(a, b, c);
        }
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
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransA(double alpha , DMatrix1Row a , DMatrix1Row b , DMatrix1Row c )
    {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);

        // TODO add a matrix vectory multiply here
        if( a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ||
                b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
            MatrixMatrixMult_DDRM.multTransA_reorder(alpha, a, b, c);
        } else {
            MatrixMatrixMult_DDRM.multTransA_small(alpha, a, b, c);
        }
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
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransB(DMatrix1Row a , DMatrix1Row b , DMatrix1Row c )
    {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);

        if( b.numRows == 1 ) {
            MatrixVectorMult_DDRM.mult(a, b, c);
        } else {
            MatrixMatrixMult_DDRM.multTransB(a, b, c);
        }
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
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransB(double alpha , DMatrix1Row a , DMatrix1Row b , DMatrix1Row c )
    {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);

        // TODO add a matrix vectory multiply here
        MatrixMatrixMult_DDRM.multTransB(alpha,a,b,c);
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
    public static void multTransAB(DMatrix1Row a , DMatrix1Row b , DMatrix1Row c )
    {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);

        if( b.numRows == 1) {
            // there are significantly faster algorithms when dealing with vectors
            if( a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
                MatrixVectorMult_DDRM.multTransA_reorder(a,b,c);
            } else {
                MatrixVectorMult_DDRM.multTransA_small(a,b,c);
            }
        } else if( a.numCols >= EjmlParameters.MULT_TRANAB_COLUMN_SWITCH ) {
            MatrixMatrixMult_DDRM.multTransAB_aux(a, b, c, null);
        } else {
            MatrixMatrixMult_DDRM.multTransAB(a, b, c);
        }
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
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multTransAB(double alpha , DMatrix1Row a , DMatrix1Row b , DMatrix1Row c )
    {
        UtilEjml.checkSameInstance(a,c);
        UtilEjml.checkSameInstance(b,c);

        // TODO add a matrix vectory multiply here
        if( a.numCols >= EjmlParameters.MULT_TRANAB_COLUMN_SWITCH ) {
            MatrixMatrixMult_DDRM.multTransAB_aux(alpha, a, b, c, null);
        } else {
            MatrixMatrixMult_DDRM.multTransAB(alpha, a, b, c);
        }
    }

    /**
     * <p>
     * Computes the dot product or inner product between two vectors.  If the two vectors are columns vectors
     * then it is defined as:<br>
     * {@code dot(a,b) = a<sup>T</sup> * b}<br>
     * If the vectors are column or row or both is ignored by this function.
     * </p>
     * @param a Vector
     * @param b Vector
     * @return Dot product of the two vectors
     */
    public static double dot(DMatrixD1 a , DMatrixD1 b ) {
        if( !MatrixFeatures_DDRM.isVector(a) || !MatrixFeatures_DDRM.isVector(b))
            throw new RuntimeException("Both inputs must be vectors");

        return VectorVectorMult_DDRM.innerProd(a,b);
    }

    /**
     * <p>Computes the matrix multiplication inner product:<br>
     * <br>
     * c = a<sup>T</sup> * a <br>
     * <br>
     * c<sub>ij</sub> = &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * a<sub>kj</sub>}
     * </p>
     * 
     * <p>
     * Is faster than using a generic matrix multiplication by taking advantage of symmetry.  For
     * vectors there is an even faster option, see {@link VectorVectorMult_DDRM#innerProd(DMatrixD1, DMatrixD1)}
     * </p>
     *
     * @param a The matrix being multiplied. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multInner(DMatrix1Row a , DMatrix1Row c )
    {
        c.reshape(a.numCols,a.numCols);

        if( a.numCols >= EjmlParameters.MULT_INNER_SWITCH ) {
            MatrixMultProduct_DDRM.inner_small(a, c);
        } else {
            MatrixMultProduct_DDRM.inner_reorder(a, c);
        }
    }

    /**
     * <p>Computes the matrix multiplication outer product:<br>
     * <br>
     * c = a * a<sup>T</sup> <br>
     * <br>
     * c<sub>ij</sub> = &sum;<sub>k=1:m</sub> { a<sub>ik</sub> * a<sub>jk</sub>}
     * </p>
     *
     * <p>
     * Is faster than using a generic matrix multiplication by taking advantage of symmetry.
     * </p>
     *
     * @param a The matrix being multiplied. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multOuter(DMatrix1Row a , DMatrix1Row c )
    {
        c.reshape(a.numRows,a.numRows);

        MatrixMultProduct_DDRM.outer(a, c);
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
    public static void multAdd(DMatrix1Row a , DMatrix1Row b , DMatrix1Row c )
    {
        if( b.numCols == 1 ) {
            MatrixVectorMult_DDRM.multAdd(a, b, c);
        } else {
            if( b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
                MatrixMatrixMult_DDRM.multAdd_reorder(a,b,c);
            } else {
                MatrixMatrixMult_DDRM.multAdd_small(a,b,c);
            }
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
     * @param alpha scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAdd(double alpha , DMatrix1Row a , DMatrix1Row b , DMatrix1Row c )
    {
        // TODO add a matrix vectory multiply here
        if( b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
            MatrixMatrixMult_DDRM.multAdd_reorder(alpha, a, b, c);
        } else {
            MatrixMatrixMult_DDRM.multAdd_small(alpha,a,b,c);
        }
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + a<sup>T</sup> * b<br>
     * c<sub>ij</sub> = c<sub>ij</sub> + &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransA(DMatrix1Row a , DMatrix1Row b , DMatrix1Row c )
    {
        if( b.numCols == 1 ) {
            if( a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
                MatrixVectorMult_DDRM.multAddTransA_reorder(a,b,c);
            } else {
                MatrixVectorMult_DDRM.multAddTransA_small(a,b,c);
            }
        } else {
            if( a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ||
                    b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH  ) {
                MatrixMatrixMult_DDRM.multAddTransA_reorder(a, b, c);
            } else {
                MatrixMatrixMult_DDRM.multAddTransA_small(a, b, c);
            }
        }
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + &alpha; * a<sup>T</sup> * b<br>
     * c<sub>ij</sub> =c<sub>ij</sub> +  &alpha; * &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>kj</sub>}
     * </p>
     *
     * @param alpha scaling factor
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransA(double alpha , DMatrix1Row a , DMatrix1Row b , DMatrix1Row c )
    {
        // TODO add a matrix vectory multiply here
        if( a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ||
                b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
            MatrixMatrixMult_DDRM.multAddTransA_reorder(alpha, a, b, c);
        } else {
            MatrixMatrixMult_DDRM.multAddTransA_small(alpha, a, b, c);
        }
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + a * b<sup>T</sup> <br>
     * c<sub>ij</sub> = c<sub>ij</sub> + &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransB(DMatrix1Row a , DMatrix1Row b , DMatrix1Row c )
    {
        MatrixMatrixMult_DDRM.multAddTransB(a,b,c);
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + &alpha; * a * b<sup>T</sup><br>
     * c<sub>ij</sub> = c<sub>ij</sub> + &alpha; * &sum;<sub>k=1:n</sub> { a<sub>ik</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param alpha Scaling factor.
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransB(double alpha , DMatrix1Row a , DMatrix1Row b , DMatrix1Row c )
    {
        // TODO add a matrix vectory multiply here
        MatrixMatrixMult_DDRM.multAddTransB(alpha,a,b,c);
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + a<sup>T</sup> * b<sup>T</sup><br>
     * c<sub>ij</sub> = c<sub>ij</sub> + &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not Modified.
     * @param b The right matrix in the multiplication operation. Not Modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransAB(DMatrix1Row a , DMatrix1Row b , DMatrix1Row c )
    {
        if( b.numRows == 1 ) {
            // there are significantly faster algorithms when dealing with vectors
            if( a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
                MatrixVectorMult_DDRM.multAddTransA_reorder(a,b,c);
            } else {
                MatrixVectorMult_DDRM.multAddTransA_small(a,b,c);
            }
        } else if( a.numCols >= EjmlParameters.MULT_TRANAB_COLUMN_SWITCH ) {
            MatrixMatrixMult_DDRM.multAddTransAB_aux(a,b,c,null);
        } else {
            MatrixMatrixMult_DDRM.multAddTransAB(a,b,c);
        }
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + &alpha; * a<sup>T</sup> * b<sup>T</sup><br>
     * c<sub>ij</sub> = c<sub>ij</sub> + &alpha; * &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param alpha Scaling factor.
     * @param a The left matrix in the multiplication operation. Not Modified.
     * @param b The right matrix in the multiplication operation. Not Modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransAB(double alpha , DMatrix1Row a , DMatrix1Row b , DMatrix1Row c )
    {
        // TODO add a matrix vectory multiply here
        if( a.numCols >= EjmlParameters.MULT_TRANAB_COLUMN_SWITCH ) {
            MatrixMatrixMult_DDRM.multAddTransAB_aux(alpha, a, b, c, null);
        } else {
            MatrixMatrixMult_DDRM.multAddTransAB(alpha, a, b, c);
        }
    }

    /**
     * <p>
     * Solves for x in the following equation:<br>
     * <br>
     * A*x = b
     * </p>
     *
     * <p>
     * If the system could not be solved then false is returned.  If it returns true
     * that just means the algorithm finished operating, but the results could still be bad
     * because 'A' is singular or nearly singular.
     * </p>
     *
     * <p>
     * If repeat calls to solve are being made then one should consider using {@link LinearSolverFactory_DDRM}
     * instead.
     * </p>
     *
     * <p>
     * It is ok for 'b' and 'x' to be the same matrix.
     * </p>
     *
     * @param a A matrix that is m by n. Not modified.
     * @param b A matrix that is n by k. Not modified.
     * @param x A matrix that is m by k. Modified.
     *
     * @return true if it could invert the matrix false if it could not.
     */
    public static boolean solve(DMatrixRMaj a , DMatrixRMaj b , DMatrixRMaj x )
    {
        x.reshape(a.numCols,b.numCols);

        LinearSolverDense<DMatrixRMaj> solver = LinearSolverFactory_DDRM.general(a.numRows,a.numCols);

        // make sure the inputs 'a' and 'b' are not modified
        solver = new LinearSolverSafe<>(solver);

        if( !solver.setA(a) )
            return false;

        solver.solve(b, x);
        return true;
    }

    /**
     * <p>
     * Linear solver for systems which are symmetric positive definite.<br>
     * A*x = b
     * </p>
     *
     * @see UnrolledCholesky_DDRM
     * @see LinearSolverFactory_DDRM
     *
     * @param A A matrix that is n by n and SPD. Not modified.
     * @param b A matrix that is n by k. Not modified.
     * @param x A matrix that is n by k. Modified.
     * @return true if it could invert the matrix false if it could not.
     */
    public static boolean solveSPD( DMatrixRMaj A , DMatrixRMaj b , DMatrixRMaj x )
    {
        if( A.numRows != A.numCols )
            throw new IllegalArgumentException("Must be a square matrix");

        x.reshape(A.numCols,b.numCols);

        if( A.numRows <= UnrolledCholesky_DDRM.MAX ) {
            DMatrixRMaj L = A.createLike();

            // L*L' = A
            if( !UnrolledCholesky_DDRM.lower(A,L) )
                return false;

            // if only one column then a faster method can be used
            if( x.numCols == 1 ) {
                x.set(b);
                TriangularSolver_DDRM.solveL(L.data,x.data,L.numCols);
                TriangularSolver_DDRM.solveTranL(L.data,x.data,L.numCols);
            } else {
                double vv[] = new double[A.numCols];
                LinearSolverChol_DDRM.solveLower(L, b, x, vv);
            }
        } else {
            LinearSolverDense<DMatrixRMaj> solver = LinearSolverFactory_DDRM.chol(A.numCols);
            solver = new LinearSolverSafe<>(solver);

            if( !solver.setA(A) )
                return false;

            solver.solve(b, x);
            return true;
        }

        return true;
    }

    /**
     * <p>Performs an "in-place" transpose.</p>
     *
     * <p>
     * For square matrices the transpose is truly in-place and does not require
     * additional memory.  For non-square matrices, internally a temporary matrix is declared and
     * {@link #transpose(DMatrixRMaj, DMatrixRMaj)} is invoked.
     * </p>
     *
     * @param mat The matrix that is to be transposed. Modified.
     */
    public static void transpose( DMatrixRMaj mat ) {
        if( mat.numCols == mat.numRows ){
            TransposeAlgs_DDRM.square(mat);
        } else {
            DMatrixRMaj b = new DMatrixRMaj(mat.numCols,mat.numRows);
            transpose(mat,b);
            mat.set(b);
        }
    }

    /**
     * <p>
     * Transposes matrix 'a' and stores the results in 'b':<br>
     * <br>
     * b<sub>ij</sub> = a<sub>ji</sub><br>
     * where 'b' is the transpose of 'a'.
     * </p>
     *
     * @param A The original matrix.  Not modified.
     * @param A_tran Where the transpose is stored. If null a new matrix is created. Modified.
     * @return The transposed matrix.
     */
    public static DMatrixRMaj transpose(DMatrixRMaj A, DMatrixRMaj A_tran)
    {
        if( A_tran == null ) {
            A_tran = new DMatrixRMaj(A.numCols,A.numRows);
        } else {
            if( A.numRows != A_tran.numCols || A.numCols != A_tran.numRows ) {
                throw new MatrixDimensionException("Incompatible matrix dimensions");
            }
        }

        if( A.numRows > EjmlParameters.TRANSPOSE_SWITCH &&
                A.numCols > EjmlParameters.TRANSPOSE_SWITCH )
            TransposeAlgs_DDRM.block(A,A_tran,EjmlParameters.BLOCK_WIDTH);
        else
            TransposeAlgs_DDRM.standard(A,A_tran);

        return A_tran;
    }


    /**
     * <p>
     * This computes the trace of the matrix:<br>
     * <br>
     * trace = &sum;<sub>i=1:n</sub> { a<sub>ii</sub> }<br>
     * where n = min(numRows,numCols)
     * </p>
     *
     * @param a A square matrix.  Not modified.
     */
    public static double trace( DMatrix1Row a ) {
        int N = Math.min(a.numRows, a.numCols);
        double sum = 0;
        int index = 0;
        for( int i = 0; i < N; i++ ) {
            sum += a.get(index);
            index += 1 + a.numCols;
        }

        return sum;
    }

    /**
     * Returns the determinant of the matrix.  If the inverse of the matrix is also
     * needed, then using {@link org.ejml.interfaces.decomposition.LUDecomposition_F64} directly (or any
     * similar algorithm) can be more efficient.
     *
     * @param mat The matrix whose determinant is to be computed.  Not modified.
     * @return The determinant.
     */
    public static double det( DMatrixRMaj mat )
    {
        int numCol = mat.getNumCols();
        int numRow = mat.getNumRows();

        if( numCol != numRow ) {
            throw new MatrixDimensionException("Must be a square matrix.");
        } else if( numCol <= UnrolledDeterminantFromMinor_DDRM.MAX ) {
            // slight performance boost overall by doing it this way
            // when it was the case statement the VM did some strange optimization
            // and made case 2 about 1/2 the speed
            if( numCol >= 2 ) {
                return UnrolledDeterminantFromMinor_DDRM.det(mat);
            } else {
                return mat.get(0);
            }
        } else {
            LUDecompositionAlt_DDRM alg = new LUDecompositionAlt_DDRM();

            if( alg.inputModified() ) {
                mat = mat.copy();
            }

            if( !alg.decompose(mat) )
                return 0.0;
            return alg.computeDeterminant().real;
        }
    }

    /**
     * <p>
     * Performs a matrix inversion operation on the specified matrix and stores the results
     * in the same matrix.<br>
     * <br>
     * a = a<sup>-1</sup>
     * </p>
     *
     * <p>
     * If the algorithm could not invert the matrix then false is returned.  If it returns true
     * that just means the algorithm finished.  The results could still be bad
     * because the matrix is singular or nearly singular.
     * </p>
     *
     * @param mat The matrix that is to be inverted.  Results are stored here.  Modified.
     * @return true if it could invert the matrix false if it could not.
     */
    public static boolean invert( DMatrixRMaj mat) {
        if( mat.numCols <= UnrolledInverseFromMinor_DDRM.MAX ) {
            if( mat.numCols != mat.numRows ) {
                throw new MatrixDimensionException("Must be a square matrix.");
            }

            if( mat.numCols >= 2 ) {
                UnrolledInverseFromMinor_DDRM.inv(mat,mat);
            } else {
                mat.set(0, 1.0/mat.get(0));
            }
        } else {
            LUDecompositionAlt_DDRM alg = new LUDecompositionAlt_DDRM();
            LinearSolverLu_DDRM solver = new LinearSolverLu_DDRM(alg);
            if( solver.setA(mat) ) {
                solver.invert(mat);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>
     * Performs a matrix inversion operation that does not modify the original
     * and stores the results in another matrix.  The two matrices must have the
     * same dimension.<br>
     * <br>
     * b = a<sup>-1</sup>
     * </p>
     *
     * <p>
     * If the algorithm could not invert the matrix then false is returned.  If it returns true
     * that just means the algorithm finished.  The results could still be bad
     * because the matrix is singular or nearly singular.
     * </p>
     *
     * <p>
     * For medium to large matrices there might be a slight performance boost to using
     * {@link LinearSolverFactory_DDRM} instead.
     * </p>
     *
     * @param mat The matrix that is to be inverted. Not modified.
     * @param result Where the inverse matrix is stored.  Modified.
     * @return true if it could invert the matrix false if it could not.
     */
    public static boolean invert(DMatrixRMaj mat, DMatrixRMaj result ) {
        result.reshape(mat.numRows,mat.numCols);

        if( mat.numCols <= UnrolledInverseFromMinor_DDRM.MAX ) {
            if( mat.numCols != mat.numRows ) {
                throw new MatrixDimensionException("Must be a square matrix.");
            }
            if( result.numCols >= 2 ) {
                UnrolledInverseFromMinor_DDRM.inv(mat,result);
            } else {
                result.set(0,  1.0/mat.get(0));
            }
        } else {
            LUDecompositionAlt_DDRM alg = new LUDecompositionAlt_DDRM();
            LinearSolverLu_DDRM solver = new LinearSolverLu_DDRM(alg);

            if( solver.modifiesA() )
                mat = mat.copy();

            if( !solver.setA(mat))
                return false;
            solver.invert(result);
        }
        return true;
    }

    /**
     * Matrix inverse for symmetric positive definite matrices. For small matrices an unrolled
     * cholesky is used. Otherwise a standard decomposition.
     *
     * @see UnrolledCholesky_DDRM
     * @see LinearSolverFactory_DDRM#chol(int)
     *
     * @param mat (Input) SPD matrix
     * @param result (Output) Inverted matrix.
     * @return true if it could invert the matrix false if it could not.
     */
    public static boolean invertSPD(DMatrixRMaj mat, DMatrixRMaj result ) {
        if( mat.numRows != mat.numCols )
            throw new IllegalArgumentException("Must be a square matrix");
        result.reshape(mat.numRows,mat.numRows);

        if( mat.numRows <= UnrolledCholesky_DDRM.MAX ) {
            // L*L' = A
            if( !UnrolledCholesky_DDRM.lower(mat,result) )
                return false;
            // L = inv(L)
            TriangularSolver_DDRM.invertLower(result.data,result.numCols);
            // inv(A) = inv(L')*inv(L)
            SpecializedOps_DDRM.multLowerTranA(result);
        } else {
            LinearSolverDense<DMatrixRMaj> solver = LinearSolverFactory_DDRM.chol(mat.numCols);
            if( solver.modifiesA() )
                mat = mat.copy();

            if( !solver.setA(mat))
                return false;
            solver.invert(result);
        }

        return true;
    }

    /**
     * <p>
     * Computes the Moore-Penrose pseudo-inverse:<br>
     * <br>
     * pinv(A) = (A<sup>T</sup>A)<sup>-1</sup> A<sup>T</sup><br>
     * or<br>
     * pinv(A) = A<sup>T</sup>(AA<sup>T</sup>)<sup>-1</sup><br>
     * </p>
     * <p>
     * Internally it uses {@link SolvePseudoInverseSvd_DDRM} to compute the inverse.  For performance reasons, this should only
     * be used when a matrix is singular or nearly singular.
     * </p>
     * @param A  A m by n Matrix.  Not modified.
     * @param invA Where the computed pseudo inverse is stored. n by m.  Modified.
     */
    public static void pinv(DMatrixRMaj A , DMatrixRMaj invA )
    {
        LinearSolverDense<DMatrixRMaj> solver = LinearSolverFactory_DDRM.pseudoInverse(true);
        if( solver.modifiesA())
            A = A.copy();

        if( !solver.setA(A) )
            throw new IllegalArgumentException("Invert failed, maybe a bug?");

        solver.invert(invA);
    }

    /**
     * Converts the columns in a matrix into a set of vectors.
     *
     * @param A Matrix.  Not modified.
     * @param v
     * @return An array of vectors.
     */
    public static DMatrixRMaj[] columnsToVector(DMatrixRMaj A, DMatrixRMaj[] v)
    {
        DMatrixRMaj[]ret;
        if( v == null || v.length < A.numCols ) {
            ret = new DMatrixRMaj[ A.numCols ];
        } else {
            ret = v;
        }

        for( int i = 0; i < ret.length; i++ ) {
            if( ret[i] == null ) {
                ret[i] = new DMatrixRMaj(A.numRows,1);
            } else {
                ret[i].reshape(A.numRows,1, false);
            }

            DMatrixRMaj u = ret[i];

            for( int j = 0; j < A.numRows; j++ ) {
                u.set(j,0, A.get(j,i));
            }
        }

        return ret;
    }

    /**
     * Converts the rows in a matrix into a set of vectors.
     *
     * @param A Matrix.  Not modified.
     * @param v
     * @return An array of vectors.
     */
    public static DMatrixRMaj[] rowsToVector(DMatrixRMaj A, DMatrixRMaj[] v)
    {
        DMatrixRMaj[]ret;
        if( v == null || v.length < A.numRows ) {
            ret = new DMatrixRMaj[ A.numRows ];
        } else {
            ret = v;
        }


        for( int i = 0; i < ret.length; i++ ) {
            if( ret[i] == null ) {
                ret[i] = new DMatrixRMaj(A.numCols,1);
            } else {
                ret[i].reshape(A.numCols,1, false);
            }

            DMatrixRMaj u = ret[i];

            for( int j = 0; j < A.numCols; j++ ) {
                u.set(j,0, A.get(i,j));
            }
        }

        return ret;
    }

    /**
     * Sets all the diagonal elements equal to one and everything else equal to zero.
     * If this is a square matrix then it will be an identity matrix.
     *
     * @see #identity(int)
     *
     * @param mat A square matrix.
     */
    public static void setIdentity( DMatrix1Row mat )
    {
        int width = mat.numRows < mat.numCols ? mat.numRows : mat.numCols;

        Arrays.fill(mat.data,0,mat.getNumElements(),0);

        int index = 0;
        for( int i = 0; i < width; i++ , index += mat.numCols + 1) {
            mat.data[index] = 1;
        }
    }

    /**
     * <p>
     * Creates an identity matrix of the specified size.<br>
     * <br>
     * a<sub>ij</sub> = 0   if i &ne; j<br>
     * a<sub>ij</sub> = 1   if i = j<br>
     * </p>
     *
     * @param width The width and height of the identity matrix.
     * @return A new instance of an identity matrix.
     */
    public static DMatrixRMaj identity(int width )
    {
        DMatrixRMaj ret = new DMatrixRMaj(width,width);

        for( int i = 0; i < width; i++ ) {
            ret.set(i,i,1.0);
        }

        return ret;
    }

    /**
     * Creates a rectangular matrix which is zero except along the diagonals.
     *
     * @param numRows Number of rows in the matrix.
     * @param numCols NUmber of columns in the matrix.
     * @return A matrix with diagonal elements equal to one.
     */
    public static DMatrixRMaj identity(int numRows , int numCols )
    {
        DMatrixRMaj ret = new DMatrixRMaj(numRows,numCols);

        int small = numRows < numCols ? numRows : numCols;

        for( int i = 0; i < small; i++ ) {
            ret.set(i,i,1.0);
        }

        return ret;
    }

    /**
     * <p>
     * Creates a new square matrix whose diagonal elements are specified by diagEl and all
     * the other elements are zero.<br>
     * <br>
     * a<sub>ij</sub> = 0         if i &le; j<br>
     * a<sub>ij</sub> = diag[i]   if i = j<br>
     * </p>
     *
     * @see #diagR
     *
     * @param diagEl Contains the values of the diagonal elements of the resulting matrix.
     * @return A new matrix.
     */
    public static DMatrixRMaj diag(double ...diagEl )
    {
        return diag(null,diagEl.length,diagEl);
    }

    /**
     * @see #diag(double...)
     */
    public static DMatrixRMaj diag(DMatrixRMaj ret , int width , double ...diagEl )
    {
        if( ret == null ) {
            ret = new DMatrixRMaj(width,width);
        } else {
            if( ret.numRows != width || ret.numCols != width )
                throw new IllegalArgumentException("Unexpected matrix size");

            CommonOps_DDRM.fill(ret, 0);
        }

        for( int i = 0; i < width; i++ ) {
            ret.unsafe_set(i, i, diagEl[i]);
        }

        return ret;
    }

    /**
     * <p>
     * Creates a new rectangular matrix whose diagonal elements are specified by diagEl and all
     * the other elements are zero.<br>
     * <br>
     * a<sub>ij</sub> = 0         if i &le; j<br>
     * a<sub>ij</sub> = diag[i]   if i = j<br>
     * </p>
     *
     * @see #diag
     *
     * @param numRows Number of rows in the matrix.
     * @param numCols Number of columns in the matrix.
     * @param diagEl Contains the values of the diagonal elements of the resulting matrix.
     * @return A new matrix.
     */
    public static DMatrixRMaj diagR(int numRows , int numCols , double ...diagEl )
    {
        DMatrixRMaj ret = new DMatrixRMaj(numRows,numCols);

        int o = Math.min(numRows,numCols);

        for( int i = 0; i < o; i++ ) {
            ret.set(i, i, diagEl[i]);
        }

        return ret;
    }

    /**
     * <p>
     * The Kronecker product of two matrices is defined as:<br>
     * C<sub>ij</sub> = a<sub>ij</sub>B<br>
     * where C<sub>ij</sub> is a sub matrix inside of C &isin; &real; <sup>m*k &times; n*l</sup>,
     * A &isin; &real; <sup>m &times; n</sup>, and B &isin; &real; <sup>k &times; l</sup>.
     * </p>
     *
     * @param A The left matrix in the operation. Not modified.
     * @param B The right matrix in the operation. Not modified.
     * @param C Where the results of the operation are stored. Modified.
     */
    public static void kron(DMatrixRMaj A , DMatrixRMaj B , DMatrixRMaj C )
    {
        int numColsC = A.numCols*B.numCols;
        int numRowsC = A.numRows*B.numRows;

        if( C.numCols != numColsC || C.numRows != numRowsC) {
            throw new MatrixDimensionException("C does not have the expected dimensions");
        }

        // TODO see comment below
        // this will work well for small matrices
        // but an alternative version should be made for large matrices
        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < A.numCols; j++ ) {
                double a = A.get(i,j);

                for( int rowB = 0; rowB < B.numRows; rowB++ ) {
                    for( int colB = 0; colB < B.numCols; colB++ ) {
                        double val = a*B.get(rowB,colB);
                        C.set(i*B.numRows+rowB,j*B.numCols+colB,val);
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Extracts a submatrix from 'src' and inserts it in a submatrix in 'dst'.
     * </p>
     * <p>
     * s<sub>i-y0 , j-x0</sub> = o<sub>ij</sub> for all y0 &le; i &lt; y1 and x0 &le; j &lt; x1 <br>
     * <br>
     * where 's<sub>ij</sub>' is an element in the submatrix and 'o<sub>ij</sub>' is an element in the
     * original matrix.
     * </p>
     *
     * @param src The original matrix which is to be copied.  Not modified.
     * @param srcX0 Start column.
     * @param srcX1 Stop column+1.
     * @param srcY0 Start row.
     * @param srcY1 Stop row+1.
     * @param dst Where the submatrix are stored.  Modified.
     * @param dstY0 Start row in dst.
     * @param dstX0 start column in dst.
     */
    public static void extract( DMatrix src,
                                int srcY0, int srcY1,
                                int srcX0, int srcX1,
                                DMatrix dst ,
                                int dstY0, int dstX0 )
    {
        if( srcY1 < srcY0 || srcY0 < 0 || srcY1 > src.getNumRows() )
            throw new MatrixDimensionException("srcY1 < srcY0 || srcY0 < 0 || srcY1 > src.numRows. "+stringShapes(src,dst));
        if( srcX1 < srcX0 || srcX0 < 0 || srcX1 > src.getNumCols() )
            throw new MatrixDimensionException("srcX1 < srcX0 || srcX0 < 0 || srcX1 > src.numCols. "+stringShapes(src,dst));

        int w = srcX1-srcX0;
        int h = srcY1-srcY0;

        if( dstY0+h > dst.getNumRows() )
            throw new MatrixDimensionException("dst is too small in rows. "+dst.getNumRows()+" < "+(dstY0+h));
        if( dstX0+w > dst.getNumCols() )
            throw new MatrixDimensionException("dst is too small in columns. "+dst.getNumCols()+" < "+(dstX0+w));

        // interestingly, the performance is only different for small matrices but identical for larger ones
        if( src instanceof DMatrixRMaj && dst instanceof DMatrixRMaj) {
            ImplCommonOps_DDRM.extract((DMatrixRMaj)src,srcY0,srcX0,(DMatrixRMaj)dst,dstY0,dstX0, h, w);
        } else {
            ImplCommonOps_DDMA.extract(src,srcY0,srcX0,dst,dstY0,dstX0, h, w);
        }
    }

    /**
     * Extract where the destination is reshaped to match the extracted region
     * @param src The original matrix which is to be copied.  Not modified.
     * @param srcX0 Start column.
     * @param srcX1 Stop column+1.
     * @param srcY0 Start row.
     * @param srcY1 Stop row+1.
     * @param dst Where the submatrix are stored.  Modified.
     */
    public static void extract( DMatrix src,
                                int srcY0, int srcY1,
                                int srcX0, int srcX1,
                                DMatrix dst ) {
        ((ReshapeMatrix)dst).reshape(srcY1-srcY0,srcX1-srcX0);
        extract(src,srcY0,srcY1,srcX0,srcX1,dst,0,0);
    }

    /**
     * <p>
     * Extracts a submatrix from 'src' and inserts it in a submatrix in 'dst'. Uses the shape of dst
     * to determine the size of the matrix extracted.
     * </p>
     *
     * @param src The original matrix which is to be copied.  Not modified.
     * @param srcY0 Start row in src.
     * @param srcX0 Start column in src.
     * @param dst Where the matrix is extracted into.
     */
    public static void extract( DMatrix src,
                                int srcY0, int srcX0,
                                DMatrix dst ) {
        extract(src,srcY0,srcY0+dst.getNumRows(),srcX0,srcX0+dst.getNumCols(),dst,0,0);
    }

    /**
     * <p>
     * Creates a new matrix which is the specified submatrix of 'src'
     * </p>
     * <p>
     * s<sub>i-y0 , j-x0</sub> = o<sub>ij</sub> for all y0 &le; i &lt; y1 and x0 &le; j &lt; x1 <br>
     * <br>
     * where 's<sub>ij</sub>' is an element in the submatrix and 'o<sub>ij</sub>' is an element in the
     * original matrix.
     * </p>
     *
     * @param src The original matrix which is to be copied.  Not modified.
     * @param srcX0 Start column.
     * @param srcX1 Stop column+1.
     * @param srcY0 Start row.
     * @param srcY1 Stop row+1.
     * @return Extracted submatrix.
     */
    public static DMatrixRMaj extract(DMatrixRMaj src,
                                        int srcY0, int srcY1,
                                        int srcX0, int srcX1 )
    {
        if( srcY1 <= srcY0 || srcY0 < 0 || srcY1 > src.numRows )
            throw new MatrixDimensionException("srcY1 <= srcY0 || srcY0 < 0 || srcY1 > src.numRows");
        if( srcX1 <= srcX0 || srcX0 < 0 || srcX1 > src.numCols )
            throw new MatrixDimensionException("srcX1 <= srcX0 || srcX0 < 0 || srcX1 > src.numCols");

        int w = srcX1-srcX0;
        int h = srcY1-srcY0;

        DMatrixRMaj dst = new DMatrixRMaj(h,w);

        ImplCommonOps_DDRM.extract(src,srcY0,srcX0,dst,0,0, h, w);

        return dst;
    }

    /**
     * Extracts out a matrix from source given a sub matrix with arbitrary rows and columns specified in
     * two array lists
     *
     * @param src Source matrix. Not modified.
     * @param rows array of row indexes
     * @param rowsSize maximum element in row array
     * @param cols array of column indexes
     * @param colsSize maximum element in column array
     * @param dst output matrix.  Must be correct shape.
     */
    public static void extract( DMatrixRMaj src,
                                int rows[] , int rowsSize ,
                                int cols[] , int colsSize , DMatrixRMaj dst ) {
        if( rowsSize != dst.numRows || colsSize != dst.numCols )
            throw new MatrixDimensionException("Unexpected number of rows and/or columns in dst matrix");

        int indexDst = 0;
        for (int i = 0; i < rowsSize; i++) {
            int indexSrcRow = src.numCols*rows[i];
            for (int j = 0; j < colsSize; j++) {
                dst.data[indexDst++] = src.data[indexSrcRow + cols[j]];
            }
        }
    }

    /**
     * Extracts the elements from the source matrix by their 1D index.
     *
     * @param src Source matrix. Not modified.
     * @param indexes array of row indexes
     * @param length maximum element in row array
     * @param dst output matrix.  Must be a vector of the correct length.
     */
    public static void extract(DMatrixRMaj src, int indexes[] , int length , DMatrixRMaj dst ) {
        if( !MatrixFeatures_DDRM.isVector(dst))
            throw new MatrixDimensionException("Dst must be a vector");
        if( length != dst.getNumElements())
            throw new MatrixDimensionException("Unexpected number of elements in dst vector");

        for (int i = 0; i < length; i++) {
            dst.data[i] = src.data[indexes[i]];
        }
    }

    /**
     * Inserts into the specified elements of dst the source matrix.
     * <pre>
     * for i in len(rows):
     *   for j in len(cols):
     *      dst(rows[i],cols[j]) = src(i,j)
     * </pre>
     *
     * @param src Source matrix. Not modified.
     * @param dst output matrix.  Must be correct shape.
     * @param rows array of row indexes
     * @param rowsSize maximum element in row array
     * @param cols array of column indexes
     * @param colsSize maximum element in column array
     */
    public static void insert( DMatrixRMaj src ,
                               DMatrixRMaj dst ,
                                int rows[] , int rowsSize ,
                                int cols[] , int colsSize ) {
        if( rowsSize != src.numRows || colsSize != src.numCols )
            throw new MatrixDimensionException("Unexpected number of rows and/or columns in dst matrix");

        int indexSrc = 0;
        for (int i = 0; i < rowsSize; i++) {
            int indexDstRow = dst.numCols*rows[i];
            for (int j = 0; j < colsSize; j++) {
                dst.data[indexDstRow + cols[j]] = src.data[indexSrc++];
            }
        }
    }

    /**
     * <p>
     * Extracts the diagonal elements 'src' write it to the 'dst' vector.  'dst'
     * can either be a row or column vector.
     * <p>
     *
     * @param src Matrix whose diagonal elements are being extracted. Not modified.
     * @param dst A vector the results will be written into. Modified.
     */
    public static void extractDiag(DMatrixRMaj src, DMatrixRMaj dst )
    {
        int N = Math.min(src.numRows, src.numCols);

        if( !MatrixFeatures_DDRM.isVector(dst) || dst.numCols*dst.numCols != N ) {
            dst.reshape(N,1);
        }

        for( int i = 0; i < N; i++ ) {
            dst.set( i , src.unsafe_get(i,i) );
        }
    }

    /**
     * Extracts the row from a matrix.
     * @param a Input matrix
     * @param row Which row is to be extracted
     * @param out output. Storage for the extracted row. If null then a new vector will be returned.
     * @return The extracted row.
     */
    public static DMatrixRMaj extractRow(DMatrixRMaj a , int row , DMatrixRMaj out ) {
        if( out == null)
            out = new DMatrixRMaj(1,a.numCols);
        else if( !MatrixFeatures_DDRM.isVector(out) || out.getNumElements() != a.numCols )
            throw new MatrixDimensionException("Output must be a vector of length "+a.numCols);

        System.arraycopy(a.data,a.getIndex(row,0),out.data,0,a.numCols);

        return out;
    }

    /**
     * Extracts the column from a matrix.
     * @param a Input matrix
     * @param column Which column is to be extracted
     * @param out output. Storage for the extracted column. If null then a new vector will be returned.
     * @return The extracted column.
     */
    public static DMatrixRMaj extractColumn(DMatrixRMaj a , int column , DMatrixRMaj out ) {
        if( out == null)
            out = new DMatrixRMaj(a.numRows,1);
        else if( !MatrixFeatures_DDRM.isVector(out) || out.getNumElements() != a.numRows )
            throw new MatrixDimensionException("Output must be a vector of length "+a.numRows);

        int index = column;
        for (int i = 0; i < a.numRows; i++, index += a.numCols ) {
            out.data[i] = a.data[index];
        }
        return out;
    }

    /**
     * Removes columns from the matrix.
     *
     * @param A Matrix. Modified
     * @param col0 First column
     * @param col1 Last column, inclusive.
     */
    public static void removeColumns( DMatrixRMaj A , int col0 , int col1 )
    {
        if( col1 < col0 ) {
            throw new IllegalArgumentException("col1 must be >= col0");
        } else if( col0 >= A.numCols || col1 >= A.numCols ) {
            throw new IllegalArgumentException("Columns which are to be removed must be in bounds");
        }

        int step = col1-col0+1;
        int offset = 0;
        for (int row = 0, idx=0; row < A.numRows; row++) {
            for (int i = 0; i < col0; i++,idx++) {
                A.data[idx] = A.data[idx+offset];
            }
            offset += step;
            for (int i = col1+1; i < A.numCols; i++,idx++) {
                A.data[idx] = A.data[idx+offset];
            }
        }
        A.numCols -= step;
    }

    /**
     * Inserts matrix 'src' into matrix 'dest' with the (0,0) of src at (row,col) in dest.
     * This is equivalent to calling extract(src,0,src.numRows,0,src.numCols,dest,destY0,destX0).
     *
     * @param src matrix that is being copied into dest. Not modified.
     * @param dest Where src is being copied into. Modified.
     * @param destY0 Start row for the copy into dest.
     * @param destX0 Start column for the copy into dest.
     */
    public static void insert(DMatrix src, DMatrix dest, int destY0, int destX0) {
        extract(src, 0, src.getNumRows(), 0, src.getNumCols(), dest, destY0, destX0);
    }

    /**
     * <p>
     * Returns the value of the element in the matrix that has the largest value.<br>
     * <br>
     * Max{ a<sub>ij</sub> } for all i and j<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The max element value of the matrix.
     */
    public static double elementMax( DMatrixD1 a ) {
        final int size = a.getNumElements();

        double max = a.get(0);
        for( int i = 1; i < size; i++ ) {
            double val = a.get(i);
            if( val >= max ) {
                max = val;
            }
        }

        return max;
    }

    /**
     * <p>
     * Returns the absolute value of the element in the matrix that has the largest absolute value.<br>
     * <br>
     * Max{ |a<sub>ij</sub>| } for all i and j<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The max abs element value of the matrix.
     */
    public static double elementMaxAbs( DMatrixD1 a ) {
        final int size = a.getNumElements();

        double max = 0;
        for( int i = 0; i < size; i++ ) {
            double val = Math.abs(a.get(i));
            if( val > max ) {
                max = val;
            }
        }

        return max;
    }

    /**
     * <p>
     * Returns the value of the element in the matrix that has the minimum value.<br>
     * <br>
     * Min{ a<sub>ij</sub> } for all i and j<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The value of element in the matrix with the minimum value.
     */
    public static double elementMin( DMatrixD1 a ) {
        final int size = a.getNumElements();

        double min = a.get(0);
        for( int i = 1; i < size; i++ ) {
            double val = a.get(i);
            if( val < min ) {
                min = val;
            }
        }

        return min;
    }

    /**
     * <p>
     * Returns the absolute value of the element in the matrix that has the smallest absolute value.<br>
     * <br>
     * Min{ |a<sub>ij</sub>| } for all i and j<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The max element value of the matrix.
     */
    public static double elementMinAbs( DMatrixD1 a ) {
        final int size = a.getNumElements();

        double min = Double.MAX_VALUE;
        for( int i = 0; i < size; i++ ) {
            double val = Math.abs(a.get(i));
            if( val < min ) {
                min = val;
            }
        }

        return min;
    }

    /**
     * <p>Performs the an element by element multiplication operation:<br>
     * <br>
     * a<sub>ij</sub> = a<sub>ij</sub> * b<sub>ij</sub> <br>
     * </p>
     * @param a The left matrix in the multiplication operation. Modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     */
    public static void elementMult(DMatrixD1 a , DMatrixD1 b )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        }

        int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            a.times(i, b.get(i));
        }
    }

    /**
     * <p>Performs the an element by element multiplication operation:<br>
     * <br>
     * c<sub>ij</sub> = a<sub>ij</sub> * b<sub>ij</sub> <br>
     * </p>
     * @param a The left matrix in the multiplication operation. Not modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void elementMult(DMatrixD1 a , DMatrixD1 b , DMatrixD1 c )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows
                || a.numRows != c.numRows || a.numCols != c.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        }

        int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.set(i, a.get(i) * b.get(i));
        }
    }

    /**
     * <p>Performs the an element by element division operation:<br>
     * <br>
     * a<sub>ij</sub> = a<sub>ij</sub> / b<sub>ij</sub> <br>
     * </p>
     * @param a The left matrix in the division operation. Modified.
     * @param b The right matrix in the division operation. Not modified.
     */
    public static void elementDiv(DMatrixD1 a , DMatrixD1 b )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        }

        int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            a.div(i, b.get(i));
        }
    }

    /**
     * <p>Performs the an element by element division operation:<br>
     * <br>
     * c<sub>ij</sub> = a<sub>ij</sub> / b<sub>ij</sub> <br>
     * </p>
     * @param a The left matrix in the division operation. Not modified.
     * @param b The right matrix in the division operation. Not modified.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void elementDiv(DMatrixD1 a , DMatrixD1 b , DMatrixD1 c )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows
                || a.numRows != c.numRows || a.numCols != c.numCols ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        }

        int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.set(i, a.get(i) / b.get(i));
        }
    }

    /**
     * <p>
     * Computes the sum of all the elements in the matrix:<br>
     * <br>
     * sum(i=1:m , j=1:n ; a<sub>ij</sub>)
     * <p>
     *
     * @param mat An m by n matrix. Not modified.
     * @return The sum of the elements.
     */
    public static double elementSum( DMatrixD1 mat ) {
        double total = 0;

        int size = mat.getNumElements();

        for( int i = 0; i < size; i++ ) {
            total += mat.get(i);
        }

        return total;
    }

    /**
     * <p>
     * Computes the sum of the absolute value all the elements in the matrix:<br>
     * <br>
     * sum(i=1:m , j=1:n ; |a<sub>ij</sub>|)
     * <p>
     *
     * @param mat An m by n matrix. Not modified.
     * @return The sum of the absolute value of each element.
     */
    public static double elementSumAbs( DMatrixD1 mat ) {
        double total = 0;

        int size = mat.getNumElements();

        for( int i = 0; i < size; i++ ) {
            total += Math.abs(mat.get(i));
        }

        return total;
    }

    /**
     * <p>
     * Element-wise power operation  <br>
     * c<sub>ij</sub> = a<sub>ij</sub> ^ b<sub>ij</sub>
     * <p>
     *
     * @param A left side
     * @param B right side
     * @param C output (modified)
     */
    public static void elementPower(DMatrixD1 A , DMatrixD1 B , DMatrixD1 C ) {

        if( A.numRows != B.numRows || A.numRows != C.numRows ||
                A.numCols != B.numCols || A.numCols != C.numCols ) {
            throw new MatrixDimensionException("All matrices must be the same shape");
        }

        int size = A.getNumElements();
        for( int i = 0; i < size; i++ ) {
            C.data[i] = Math.pow(A.data[i], B.data[i]);
        }
    }

    /**
     * <p>
     * Element-wise power operation  <br>
     * c<sub>ij</sub> = a ^ b<sub>ij</sub>
     * <p>
     *
     * @param a left scalar
     * @param B right side
     * @param C output (modified)
     */
    public static void elementPower(double a , DMatrixD1 B , DMatrixD1 C ) {

        if( B.numRows != C.numRows || B.numCols != C.numCols ) {
            throw new MatrixDimensionException("All matrices must be the same shape");
        }

        int size = B.getNumElements();
        for( int i = 0; i < size; i++ ) {
            C.data[i] = Math.pow(a, B.data[i]);
        }
    }

    /**
     * <p>
     * Element-wise power operation  <br>
     * c<sub>ij</sub> = a<sub>ij</sub> ^ b
     * <p>
     *
     * @param A left side
     * @param b right scalar
     * @param C output (modified)
     */
    public static void elementPower(DMatrixD1 A , double b, DMatrixD1 C ) {

        if( A.numRows != C.numRows || A.numCols != C.numCols ) {
            throw new MatrixDimensionException("All matrices must be the same shape");
        }

        int size = A.getNumElements();
        for( int i = 0; i < size; i++ ) {
            C.data[i] = Math.pow(A.data[i], b);
        }
    }

    /**
     * <p>
     * Element-wise log operation  <br>
     * c<sub>ij</sub> = Math.log(a<sub>ij</sub>)
     * <p>
     *
     * @param A input
     * @param C output (modified)
     */
    public static void elementLog(DMatrixD1 A , DMatrixD1 C ) {

        if( A.numCols != C.numCols || A.numRows != C.numRows ) {
            throw new MatrixDimensionException("All matrices must be the same shape");
        }

        int size = A.getNumElements();
        for( int i = 0; i < size; i++ ) {
            C.data[i] = Math.log(A.data[i]);
        }
    }

    /**
     * <p>
     * Element-wise exp operation  <br>
     * c<sub>ij</sub> = Math.log(a<sub>ij</sub>)
     * <p>
     *
     * @param A input
     * @param C output (modified)
     */
    public static void elementExp(DMatrixD1 A , DMatrixD1 C ) {

        if( A.numCols != C.numCols || A.numRows != C.numRows ) {
            throw new MatrixDimensionException("All matrices must be the same shape");
        }

        int size = A.getNumElements();
        for( int i = 0; i < size; i++ ) {
            C.data[i] = Math.exp(A.data[i]);
        }
    }

    /**
     * Multiplies every element in row i by value[i].
     *
     * @param values array. Not modified.
     * @param A Matrix. Modified.
     */
    public static void multRows(double[] values, DMatrixRMaj A) {
        if( values.length < A.numRows ) {
            throw new IllegalArgumentException("Not enough elements in values.");
        }

        int index = 0;
        for (int row = 0; row < A.numRows; row++) {
            double v = values[row];
            for (int col = 0; col < A.numCols; col++, index++) {
                A.data[index] *= v;
            }
        }
    }

    /**
     * Divides every element in row i by value[i].
     *
     * @param values array. Not modified.
     * @param A Matrix. Modified.
     */
    public static void divideRows(double[] values, DMatrixRMaj A) {
        if( values.length < A.numRows ) {
            throw new IllegalArgumentException("Not enough elements in values.");
        }

        int index = 0;
        for (int row = 0; row < A.numRows; row++) {
            double v = values[row];
            for (int col = 0; col < A.numCols; col++, index++) {
                A.data[index] /= v;
            }
        }
    }

    /**
     * Multiplies every element in column i by value[i].
     *
     * @param A Matrix. Modified.
     * @param values array. Not modified.
     */
    public static void multCols(DMatrixRMaj A , double values[] ) {
        if( values.length < A.numCols ) {
            throw new IllegalArgumentException("Not enough elements in values.");
        }

        int index = 0;
        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++, index++) {
                A.data[index] *= values[col];
            }
        }
    }

    /**
     * Divides every element in column i by value[i].
     *
     * @param A Matrix. Modified.
     * @param values array. Not modified.
     */
    public static void divideCols(DMatrixRMaj A , double values[] ) {
        if( values.length < A.numCols ) {
            throw new IllegalArgumentException("Not enough elements in values.");
        }

        int index = 0;
        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++, index++) {
                A.data[index] /= values[col];
            }
        }
    }

    /**
     * Equivalent to multiplying a matrix B by the inverse of two diagonal matrices.
     * B = inv(A)*B*inv(C), where A=diag(a) and C=diag(c).
     *
     * @param diagA Array of length offsteA + B.numRows
     * @param offsetA First index in A
     * @param B Rectangular matrix
     * @param diagC Array of length indexC + B.numCols
     * @param offsetC First index in C
     */
    public static void divideRowsCols( double []diagA , int offsetA ,
                                       DMatrixRMaj B ,
                                       double []diagC , int offsetC )
    {
        if( diagA.length-offsetA < B.numRows ) {
            throw new IllegalArgumentException("Not enough elements in diagA.");
        }
        if( diagC.length-offsetC < B.numCols ) {
            throw new IllegalArgumentException("Not enough elements in diagC.");
        }

        final int rows = B.numRows;
        final int cols = B.numCols;

        int index = 0;
        for (int row = 0; row < rows; row++) {
            double va = diagA[offsetA+row];
            for (int col = 0; col < cols; col++, index++) {
                B.data[index] /= va*diagC[offsetC+col];
            }
        }
    }

    /**
     * <p>
     * Computes the sum of each row in the input matrix and returns the results in a vector:<br>
     * <br>
     * b<sub>j</sub> = sum(i=1:n ; a<sub>ji</sub>)
     * </p>
     *
     * @param input INput matrix whose rows are summed.
     * @param output Optional storage for output. Reshaped into a column. Modified.
     * @return Vector containing the sum of each row in the input.
     */
    public static DMatrixRMaj sumRows(DMatrixRMaj input , DMatrixRMaj output ) {
        if( output == null ) {
            output = new DMatrixRMaj(input.numRows,1);
        } else {
            output.reshape(input.numRows,1);
        }

        for( int row = 0; row < input.numRows; row++ ) {
            double total = 0;

            int end = (row+1)*input.numCols;
            for( int index = row*input.numCols; index < end; index++ ) {
                total += input.data[index];
            }

            output.set(row,total);
        }
        return output;
    }

    /**
     * <p>
     * Finds the element with the minimum value along each row in the input matrix and returns the results in a vector:<br>
     * <br>
     * b<sub>j</sub> = min(i=1:n ; a<sub>ji</sub>)
     * </p>
     *
     * @param input Input matrix
     * @param output Optional storage for output.  Reshaped into a column. Modified.
     * @return Vector containing the sum of each row in the input.
     */
    public static DMatrixRMaj minRows(DMatrixRMaj input , DMatrixRMaj output ) {
        if( output == null ) {
            output = new DMatrixRMaj(input.numRows,1);
        } else {
            output.reshape(input.numRows,1);
        }

        for( int row = 0; row < input.numRows; row++ ) {
            double min = Double.MAX_VALUE;

            int end = (row+1)*input.numCols;
            for( int index = row*input.numCols; index < end; index++ ) {
                double v = input.data[index];
                if( v < min )
                    min = v;
            }

            output.set(row,min);
        }
        return output;
    }

    /**
     * <p>
     * Finds the element with the maximum value along each row in the input matrix and returns the results in a vector:<br>
     * <br>
     * b<sub>j</sub> = max(i=1:n ; a<sub>ji</sub>)
     * </p>
     *
     * @param input Input matrix
     * @param output Optional storage for output.  Reshaped into a column. Modified.
     * @return Vector containing the sum of each row in the input.
     */
    public static DMatrixRMaj maxRows(DMatrixRMaj input , DMatrixRMaj output ) {
        if( output == null ) {
            output = new DMatrixRMaj(input.numRows,1);
        } else {
            output.reshape(input.numRows,1);
        }

        for( int row = 0; row < input.numRows; row++ ) {
            double max = -Double.MAX_VALUE;

            int end = (row+1)*input.numCols;
            for( int index = row*input.numCols; index < end; index++ ) {
                double v = input.data[index];
                if( v > max )
                    max = v;
            }

            output.set(row,max);
        }
        return output;
    }

    /**
     * <p>
     * Computes the sum of each column in the input matrix and returns the results in a vector:<br>
     * <br>
     * b<sub>j</sub> = sum(i=1:m ; a<sub>ij</sub>)
     * </p>
     *
     * @param input Input matrix
     * @param output Optional storage for output. Reshaped into a row vector. Modified.
     * @return Vector containing the sum of each column
     */
    public static DMatrixRMaj sumCols(DMatrixRMaj input , DMatrixRMaj output ) {
        if( output == null ) {
            output = new DMatrixRMaj(1,input.numCols);
        } else {
            output.reshape(1,input.numCols);
        }

        for( int cols = 0; cols < input.numCols; cols++ ) {
            double total = 0;

            int index = cols;
            int end = index + input.numCols*input.numRows;
            for( ; index < end; index += input.numCols ) {
                total += input.data[index];
            }

            output.set(cols, total);
        }
        return output;
    }

    /**
     * <p>
     * Finds the element with the minimum value along column in the input matrix and returns the results in a vector:<br>
     * <br>
     * b<sub>j</sub> = min(i=1:m ; a<sub>ij</sub>)
     * </p>
     *
     * @param input Input matrix
     * @param output Optional storage for output. Reshaped into a row vector. Modified.
     * @return Vector containing the minimum of each column
     */
    public static DMatrixRMaj minCols(DMatrixRMaj input , DMatrixRMaj output ) {
        if( output == null ) {
            output = new DMatrixRMaj(1,input.numCols);
        } else {
            output.reshape(1,input.numCols);
        }
        for( int cols = 0; cols < input.numCols; cols++ ) {
            double minimum = Double.MAX_VALUE;

            int index = cols;
            int end = index + input.numCols*input.numRows;
            for( ; index < end; index += input.numCols ) {
                double v = input.data[index];
                if( v < minimum )
                    minimum = v;
            }

            output.set(cols, minimum);
        }
        return output;
    }

    /**
     * <p>
     * Finds the element with the minimum value along column in the input matrix and returns the results in a vector:<br>
     * <br>
     * b<sub>j</sub> = min(i=1:m ; a<sub>ij</sub>)
     * </p>
     *
     * @param input Input matrix
     * @param output Optional storage for output. Reshaped into a row vector. Modified.
     * @return Vector containing the maximum of each column
     */
    public static DMatrixRMaj maxCols(DMatrixRMaj input , DMatrixRMaj output ) {
        if( output == null ) {
            output = new DMatrixRMaj(1,input.numCols);
        } else {
            output.reshape(1,input.numCols);
        }
        for( int cols = 0; cols < input.numCols; cols++ ) {
            double maximum = -Double.MAX_VALUE;

            int index = cols;
            int end = index + input.numCols*input.numRows;
            for( ; index < end; index += input.numCols ) {
                double v = input.data[index];
                if( v > maximum )
                    maximum = v;
            }

            output.set(cols, maximum);
        }
        return output;
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * a = a + b <br>
     * a<sub>ij</sub> = a<sub>ij</sub> + b<sub>ij</sub> <br>
     * </p>
     *
     * @param a A Matrix. Modified.
     * @param b A Matrix. Not modified.
     */
    public static void addEquals(DMatrixD1 a , DMatrixD1 b )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        }

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            a.plus(i, b.get(i));
        }
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * a = a +  &beta; * b  <br>
     * a<sub>ij</sub> = a<sub>ij</sub> + &beta; * b<sub>ij</sub>
     * </p>
     *
     * @param beta The number that matrix 'b' is multiplied by.
     * @param a A Matrix. Modified.
     * @param b A Matrix. Not modified.
     */
    public static void addEquals(DMatrixD1 a , double beta, DMatrixD1 b )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        }

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            a.plus(i, beta * b.get(i));
        }
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = a + b <br>
     * c<sub>ij</sub> = a<sub>ij</sub> + b<sub>ij</sub> <br>
     * </p>
     *
     * <p>
     * Matrix C can be the same instance as Matrix A and/or B.
     * </p>
     *
     * @param a A Matrix. Not modified.
     * @param b A Matrix. Not modified.
     * @param c A Matrix where the results are stored. Modified.
     */
    public static void add(final DMatrixD1 a , final DMatrixD1 b , final DMatrixD1 c )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The matrices are not all the same dimension.");
        }

        c.reshape(a.numRows,a.numCols);

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.set(i, a.get(i) + b.get(i));
        }
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = a + &beta; * b <br>
     * c<sub>ij</sub> = a<sub>ij</sub> + &beta; * b<sub>ij</sub> <br>
     * </p>
     *
     * <p>
     * Matrix C can be the same instance as Matrix A and/or B.
     * </p>
     *
     * @param a A Matrix. Not modified.
     * @param beta Scaling factor for matrix b.
     * @param b A Matrix. Not modified.
     * @param c A Matrix where the results are stored. Modified.
     */
    public static void add(DMatrixD1 a , double beta , DMatrixD1 b , DMatrixD1 c )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The matrices are not all the same dimension.");
        }

        c.reshape(a.numRows,a.numCols);

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.set(i, a.get(i) + beta * b.get(i));
        }
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = &alpha; * a + &beta; * b <br>
     * c<sub>ij</sub> = &alpha; * a<sub>ij</sub> + &beta; * b<sub>ij</sub> <br>
     * </p>
     *
     * <p>
     * Matrix C can be the same instance as Matrix A and/or B.
     * </p>
     *
     * @param alpha A scaling factor for matrix a.
     * @param a A Matrix. Not modified.
     * @param beta A scaling factor for matrix b.
     * @param b A Matrix. Not modified.
     * @param c A Matrix where the results are stored. Modified.
     */
    public static void add(double alpha , DMatrixD1 a , double beta , DMatrixD1 b , DMatrixD1 c )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The matrices are not all the same dimension.");
        }

        c.reshape(a.numRows,a.numCols);

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.set(i, alpha * a.get(i) + beta * b.get(i));
        }
    }

    /**
     * <p>Performs the following operation:<br>
     * <br>
     * c = &alpha; * a + b <br>
     * c<sub>ij</sub> = &alpha; * a<sub>ij</sub> + b<sub>ij</sub> <br>
     * </p>
     *
     * <p>
     * Matrix C can be the same instance as Matrix A and/or B.
     * </p>
     *
     * @param alpha A scaling factor for matrix a.
     * @param a A Matrix. Not modified.
     * @param b A Matrix. Not modified.
     * @param c A Matrix where the results are stored. Modified.
     */
    public static void add(double alpha , DMatrixD1 a , DMatrixD1 b , DMatrixD1 c )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The matrices are not all the same dimension.");
        }

        c.reshape(a.numRows,a.numCols);
        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.set(i, alpha * a.get(i) + b.get(i));
        }
    }

    /**
     * <p>Performs an in-place scalar addition:<br>
     * <br>
     * a = a + val<br>
     * a<sub>ij</sub> = a<sub>ij</sub> + val<br>
     * </p>
     *
     * @param a A matrix.  Modified.
     * @param val The value that's added to each element.
     */
    public static void add(DMatrixD1 a , double val ) {
        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            a.plus(i, val);
        }
    }

    /**
     * <p>Performs scalar addition:<br>
     * <br>
     * c = a + val<br>
     * c<sub>ij</sub> = a<sub>ij</sub> + val<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @param c A matrix. Modified.
     * @param val The value that's added to each element.
     */
    public static void add(DMatrixD1 a , double val , DMatrixD1 c ) {
        c.reshape(a.numRows,a.numCols);

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.data[i] = a.data[i] + val;
        }
    }

    /**
     * <p>Performs matrix scalar subtraction:<br>
     * <br>
     * c = a - val<br>
     * c<sub>ij</sub> = a<sub>ij</sub> - val<br>
     * </p>
     *
     * @param a (input) A matrix. Not modified.
     * @param val (input) The value that's subtracted to each element.
     * @param c (Output) A matrix. Modified.
     */
    public static void subtract(DMatrixD1 a , double val , DMatrixD1 c ) {
        c.reshape(a.numRows,a.numCols);

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.data[i] = a.data[i] - val;
        }
    }

    /**
     * <p>Performs matrix scalar subtraction:<br>
     * <br>
     * c = val - a<br>
     * c<sub>ij</sub> = val - a<sub>ij</sub><br>
     * </p>
     *
     * @param val (input) The value that's subtracted to each element.
     * @param a (input) A matrix. Not modified.
     * @param c (Output) A matrix. Modified.
     */
    public static void subtract(double val , DMatrixD1 a , DMatrixD1 c ) {
        c.reshape(a.numRows,a.numCols);

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.data[i] = val - a.data[i];
        }
    }

    /**
     * <p>Performs the following subtraction operation:<br>
     * <br>
     * a = a - b  <br>
     * a<sub>ij</sub> = a<sub>ij</sub> - b<sub>ij</sub>
     * </p>
     *
     * @param a A Matrix. Modified.
     * @param b A Matrix. Not modified.
     */
    public static void subtractEquals(DMatrixD1 a, DMatrixD1 b)
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        }

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            a.data[i] -= b.data[i];
        }
    }

    /**
     * <p>Performs the following subtraction operation:<br>
     * <br>
     * c = a - b  <br>
     * c<sub>ij</sub> = a<sub>ij</sub> - b<sub>ij</sub>
     * </p>
     * <p>
     * Matrix C can be the same instance as Matrix A and/or B.
     * </p>
     *
     * @param a A Matrix. Not modified.
     * @param b A Matrix. Not modified.
     * @param c A Matrix. Modified.
     */
    public static void subtract(DMatrixD1 a, DMatrixD1 b, DMatrixD1 c)
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        }
        c.reshape(a.numRows,a.numCols);

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.data[i] = a.data[i] - b.data[i];
        }
    }

    /**
     * <p>
     * Performs an in-place element by element scalar multiplication.<br>
     * <br>
     * a<sub>ij</sub> = &alpha;*a<sub>ij</sub>
     * </p>
     *
     * @param a The matrix that is to be scaled.  Modified.
     * @param alpha the amount each element is multiplied by.
     */
    public static void scale( double alpha , DMatrixD1 a )
    {
        // on very small matrices (2 by 2) the call to getNumElements() can slow it down
        // slightly compared to other libraries since it involves an extra multiplication.
        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            a.data[i] *= alpha;
        }
    }

    /**
     * <p>
     * Performs an element by element scalar multiplication.<br>
     * <br>
     * b<sub>ij</sub> = &alpha;*a<sub>ij</sub>
     * </p>
     *
     * @param alpha the amount each element is multiplied by.
     * @param a The matrix that is to be scaled.  Not modified.
     * @param b Where the scaled matrix is stored. Modified.
     */
    public static void scale(double alpha , DMatrixD1 a , DMatrixD1 b)
    {
        b.reshape(a.numRows,a.numCols);

        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            b.data[i] = a.data[i]*alpha;
        }
    }

    /**
     * In-place scaling of a row in A
     *
     * @param alpha scale factor
     * @param A matrix
     * @param row which row in A
     */
    public static void scaleRow( double alpha , DMatrixRMaj A , int row ) {
        int idx = row*A.numCols;
        for (int col = 0; col < A.numCols; col++) {
            A.data[idx++] *= alpha;
        }
    }

    /**
     * In-place scaling of a column in A
     *
     * @param alpha scale factor
     * @param A matrix
     * @param col which row in A
     */
    public static void scaleCol( double alpha , DMatrixRMaj A , int col ) {
        int idx = col;
        for (int row = 0; row < A.numRows; row++, idx += A.numCols) {
            A.data[idx] *= alpha;
        }
    }

    /**
     * <p>
     * Performs an in-place element by element scalar division with the scalar on top.<br>
     * <br>
     * a<sub>ij</sub> = &alpha;/a<sub>ij</sub>
     * </p>
     *
     * @param a The matrix whose elements are divide the scalar.  Modified.
     * @param alpha top value in division
     */
    public static void divide( double alpha , DMatrixD1 a )
    {
        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            a.data[i] = alpha/a.data[i];
        }
    }

    /**
     * <p>
     * Performs an in-place element by element scalar division with the scalar on bottom.<br>
     * <br>
     * a<sub>ij</sub> = a<sub>ij</sub>/&alpha;
     * </p>
     *
     * @param a The matrix whose elements are to be divided.  Modified.
     * @param alpha the amount each element is divided by.
     */
    public static void divide(DMatrixD1 a , double alpha)
    {
        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            a.data[i] /= alpha;
        }
    }

    /**
     * <p>
     * Performs an element by element scalar division with the scalar on top.<br>
     * <br>
     * b<sub>ij</sub> = &alpha;/a<sub>ij</sub>
     * </p>
     *
     * @param alpha The numerator.
     * @param a The matrix whose elements are the divisor.  Not modified.
     * @param b Where the results are stored. Modified.
     */
    public static void divide(double alpha , DMatrixD1 a , DMatrixD1 b)
    {
        b.reshape(a.numRows,a.numCols);

        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            b.data[i] = alpha/a.data[i];
        }
    }

    /**
     * <p>
     * Performs an element by element scalar division with the scalar on botton.<br>
     * <br>
     * b<sub>ij</sub> = a<sub>ij</sub> /&alpha;
     * </p>
     *
     * @param a The matrix whose elements are to be divided.  Not modified.
     * @param alpha the amount each element is divided by.
     * @param b Where the results are stored. Modified.
     */
    public static void divide(DMatrixD1 a , double alpha  , DMatrixD1 b)
    {
        b.reshape(a.numRows,a.numCols);

        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            b.data[i] = a.data[i]/alpha;
        }
    }

    /**
     * <p>
     * Changes the sign of every element in the matrix.<br>
     * <br>
     * a<sub>ij</sub> = -a<sub>ij</sub>
     * </p>
     *
     * @param a A matrix. Modified.
     */
    public static void changeSign( DMatrixD1 a )
    {
        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            a.data[i] = -a.data[i];
        }
    }

    /**
     * <p>
     * Changes the sign of every element in the matrix.<br>
     * <br>
     * output<sub>ij</sub> = -input<sub>ij</sub>
     * </p>
     *
     * @param input A matrix. Modified.
     */
    public static <T extends DMatrixD1> T changeSign(T input , @Nullable T output)
    {
        if( output == null ) {
            output = input.createLike();
        } else {
            output.reshape(input.numRows, input.numCols);
        }

        final int size = input.getNumElements();

        for( int i = 0; i < size; i++ ) {
            output.data[i] = -input.data[i];
        }

        return output;
    }

    /**
     * <p>
     * Sets every element in the matrix to the specified value.<br>
     * <br>
     * a<sub>ij</sub> = value
     * <p>
     *
     * @param a A matrix whose elements are about to be set. Modified.
     * @param value The value each element will have.
     */
    public static void fill(DMatrixD1 a, double value)
    {
        Arrays.fill(a.data, 0, a.getNumElements(), value);
    }

    /**
     * <p>
     * Puts the augmented system matrix into reduced row echelon form (RREF) using Gauss-Jordan
     * elimination with row (partial) pivots.  A matrix is said to be in RREF is the following conditions are true:
     * </p>
     *
     * <ol>
     *     <li>If a row has non-zero entries, then the first non-zero entry is 1.  This is known as the leading one.</li>
     *     <li>If a column contains a leading one then all other entries in that column are zero.</li>
     *     <li>If a row contains a leading 1, then each row above contains a leading 1 further to the left.</li>
     * </ol>
     *
     * <p>
     * [1] Page 19 in, Otter Bretscherm "Linear Algebra with Applications" Prentice-Hall Inc, 1997
     * </p>
     *
     * @see RrefGaussJordanRowPivot_DDRM
     *
     * @param A Input matrix.  Unmodified.
     * @param numUnknowns Number of unknowns/columns that are reduced. Set to -1 to default to
     *                       Math.min(A.numRows,A.numCols), which works for most systems.
     * @param reduced Storage for reduced echelon matrix. If null then a new matrix is returned. Modified.
     * @return Reduced echelon form of A
     */
    public static DMatrixRMaj rref(DMatrixRMaj A , int numUnknowns, DMatrixRMaj reduced ) {
        if( reduced == null ) {
            reduced = new DMatrixRMaj(A.numRows,A.numCols);
        }
        reduced.reshape(A.numRows,A.numCols);

        if( numUnknowns <= 0 )
            numUnknowns = Math.min(A.numCols,A.numRows);

        ReducedRowEchelonForm_F64<DMatrixRMaj> alg = new RrefGaussJordanRowPivot_DDRM();
        alg.setTolerance(elementMaxAbs(A)* UtilEjml.EPS*Math.max(A.numRows,A.numCols));

        reduced.set(A);
        alg.reduce(reduced, numUnknowns);

        return reduced;
    }

    /**
     * Applies the &gt; operator to each element in A.  Results are stored in a boolean matrix.
     *
     * @param A Input matrx
     * @param value value each element is compared against
     * @param output (Optional) Storage for results.  Can be null. Is reshaped.
     * @return Boolean matrix with results
     */
    public static BMatrixRMaj elementLessThan(DMatrixRMaj A , double value , BMatrixRMaj output )
    {
        if( output == null ) {
            output = new BMatrixRMaj(A.numRows,A.numCols);
        }

        output.reshape(A.numRows, A.numCols);

        int N = A.getNumElements();

        for (int i = 0; i < N; i++) {
            output.data[i] = A.data[i] < value;
        }

        return output;
    }

    /**
     * Applies the &ge; operator to each element in A.  Results are stored in a boolean matrix.
     *
     * @param A Input matrix
     * @param value value each element is compared against
     * @param output (Optional) Storage for results.  Can be null. Is reshaped.
     * @return Boolean matrix with results
     */
    public static BMatrixRMaj elementLessThanOrEqual(DMatrixRMaj A , double value , BMatrixRMaj output )
    {
        if( output == null ) {
            output = new BMatrixRMaj(A.numRows,A.numCols);
        }

        output.reshape(A.numRows, A.numCols);

        int N = A.getNumElements();

        for (int i = 0; i < N; i++) {
            output.data[i] = A.data[i] <= value;
        }

        return output;
    }

    /**
     * Applies the &gt; operator to each element in A.  Results are stored in a boolean matrix.
     *
     * @param A Input matrix
     * @param value value each element is compared against
     * @param output (Optional) Storage for results.  Can be null. Is reshaped.
     * @return Boolean matrix with results
     */
    public static BMatrixRMaj elementMoreThan(DMatrixRMaj A , double value , BMatrixRMaj output )
    {
        if( output == null ) {
            output = new BMatrixRMaj(A.numRows,A.numCols);
        }

        output.reshape(A.numRows, A.numCols);

        int N = A.getNumElements();

        for (int i = 0; i < N; i++) {
            output.data[i] = A.data[i] > value;
        }

        return output;
    }

    /**
     * Applies the &ge; operator to each element in A.  Results are stored in a boolean matrix.
     *
     * @param A Input matrix
     * @param value value each element is compared against
     * @param output (Optional) Storage for results.  Can be null. Is reshaped.
     * @return Boolean matrix with results
     */
    public static BMatrixRMaj elementMoreThanOrEqual(DMatrixRMaj A , double value , BMatrixRMaj output )
    {
        if( output == null ) {
            output = new BMatrixRMaj(A.numRows,A.numCols);
        }

        output.reshape(A.numRows, A.numCols);

        int N = A.getNumElements();

        for (int i = 0; i < N; i++) {
            output.data[i] = A.data[i] >= value;
        }

        return output;
    }

    /**
     * Applies the &lt; operator to each element in A.  Results are stored in a boolean matrix.
     *
     * @param A Input matrix
     * @param B Input matrix
     * @param output (Optional) Storage for results.  Can be null. Is reshaped.
     * @return Boolean matrix with results
     */
    public static BMatrixRMaj elementLessThan(DMatrixRMaj A , DMatrixRMaj B , BMatrixRMaj output )
    {
        if( output == null ) {
            output = new BMatrixRMaj(A.numRows,A.numCols);
        }

        output.reshape(A.numRows, A.numCols);

        int N = A.getNumElements();

        for (int i = 0; i < N; i++) {
            output.data[i] = A.data[i] < B.data[i];
        }

        return output;
    }

    /**
     * Applies the A &le; B operator to each element.  Results are stored in a boolean matrix.
     *
     * @param A Input matrix
     * @param B Input matrix
     * @param output (Optional) Storage for results.  Can be null. Is reshaped.
     * @return Boolean matrix with results
     */
    public static BMatrixRMaj elementLessThanOrEqual(DMatrixRMaj A , DMatrixRMaj B , BMatrixRMaj output )
    {
        if( output == null ) {
            output = new BMatrixRMaj(A.numRows,A.numCols);
        }

        output.reshape(A.numRows, A.numCols);

        int N = A.getNumElements();

        for (int i = 0; i < N; i++) {
            output.data[i] = A.data[i] <= B.data[i];
        }

        return output;
    }

    /**
     * Returns a row matrix which contains all the elements in A which are flagged as true in 'marked'
     *
     * @param A Input matrix
     * @param marked Input matrix marking elements in A
     * @param output Storage for output row vector. Can be null.  Will be reshaped.
     * @return Row vector with marked elements
     */
    public static DMatrixRMaj elements(DMatrixRMaj A , BMatrixRMaj marked , DMatrixRMaj output ) {
        if( A.numRows != marked.numRows || A.numCols != marked.numCols )
            throw new MatrixDimensionException("Input matrices must have the same shape");
        if( output == null )
            output = new DMatrixRMaj(1,1);

        output.reshape(countTrue(marked),1);

        int N = A.getNumElements();

        int index = 0;
        for (int i = 0; i < N; i++) {
            if( marked.data[i] ) {
                output.data[index++] = A.data[i];
            }
        }

        return output;
    }

    /**
     * Counts the number of elements in A which are true
     * @param A input matrix
     * @return number of true elements
     */
    public static int countTrue(BMatrixRMaj A) {
        int total = 0;

        int N = A.getNumElements();

        for (int i = 0; i < N; i++) {
            if( A.data[i] )
                total++;
        }

        return total;
    }

    /**
     * output = [a , b]
     */
    public static void concatColumns(DMatrixRMaj a , DMatrixRMaj b , DMatrixRMaj output ) {
        int rows = Math.max(a.numRows , b.numRows);
        int cols = a.numCols + b.numCols;

        output.reshape(rows,cols);
        output.zero();

        insert(a,output,0,0);
        insert(b,output,0,a.numCols);
    }

    /**
     * <p>Concatinates all the matrices together along their columns.  If the rows do not match the upper elements
     * are set to zero.</p>
     *
     * A = [ m[0] , ... , m[n-1] ]
     *
     * @param m Set of matrices
     * @return Resulting matrix
     */
    public static DMatrixRMaj concatColumnsMulti(DMatrixRMaj ...m ) {
        int rows = 0;
        int cols = 0;

        for (int i = 0; i < m.length; i++) {
            rows = Math.max(rows,m[i].numRows);
            cols += m[i].numCols;
        }
        DMatrixRMaj R = new DMatrixRMaj(rows,cols);

        int col = 0;
        for (int i = 0; i < m.length; i++) {
            insert(m[i],R,0,col);
            col += m[i].numCols;
        }

        return R;
    }

    /**
     * output = [a ; b]
     */
    public static void concatRows(DMatrixRMaj a , DMatrixRMaj b , DMatrixRMaj output ) {
        int rows = a.numRows + b.numRows;
        int cols = Math.max(a.numCols , b.numCols);

        output.reshape(rows,cols);
        output.zero();

        insert(a,output,0,0);
        insert(b,output,a.numRows,0);
    }

    /**
     * <p>Concatinates all the matrices together along their columns.  If the rows do not match the upper elements
     * are set to zero.</p>
     *
     * A = [ m[0] ; ... ; m[n-1] ]
     *
     * @param m Set of matrices
     * @return Resulting matrix
     */
    public static DMatrixRMaj concatRowsMulti(DMatrixRMaj ...m ) {
        int rows = 0;
        int cols = 0;

        for (int i = 0; i < m.length; i++) {
            rows += m[i].numRows;
            cols = Math.max(cols,m[i].numCols);
        }
        DMatrixRMaj R = new DMatrixRMaj(rows,cols);

        int row = 0;
        for (int i = 0; i < m.length; i++) {
            insert(m[i],R,row,0);
            row += m[i].numRows;
        }

        return R;
    }

    /**
     * Applies the row permutation specified by the vector to the input matrix and save the results
     * in the output matrix.  output[perm[j],:] = input[j,:]
     *
     * @param pinv (Input) Inverse permutation vector.  Specifies new order of the rows.
     * @param input (Input) Matrix which is to be permuted
     * @param output (Output) Matrix which has the permutation stored in it.  Is reshaped.
     */
    public static DMatrixRMaj permuteRowInv( int pinv[] , DMatrixRMaj input , DMatrixRMaj output ) {
        if( input.numRows > pinv.length )
            throw new MatrixDimensionException("permutation vector must have at least as many elements as input has rows");

        if( output == null )
            output = new DMatrixRMaj(1,1);
        output.reshape(input.numRows,input.numCols);

        int m = input.numCols;
        for (int row = 0; row < input.numRows; row++) {
            System.arraycopy(input.data,row*m,output.data,pinv[row]*m,m);
        }
        return output;
    }

    /**
     * <p>Performs absolute value of a matrix:<br>
     * <br>
     * c = abs(a)<br>
     * c<sub>ij</sub> = abs(a<sub>ij</sub>)
     * </p>
     *
     * @param a A matrix. Not modified.
     * @param c A matrix. Modified.
     */
    public static void abs(DMatrixD1 a , DMatrixD1 c ) {
        c.reshape(a.numRows,a.numCols);

        final int length = a.getNumElements();

        for ( int i = 0; i < length; i++ ) {
            c.data[i] = Math.abs(a.data[i]);
        }
    }

    /**
     * <p>Performs absolute value of a matrix:<br>
     * <br>
     * a = abs(a)<br>
     * a<sub>ij</sub> = abs(a<sub>ij</sub>)
     * </p>
     *
     * @param a A matrix. Modified.
     */
    public static void abs(DMatrixD1 a ) {
        final int length = a.getNumElements();

        for ( int i = 0; i < length; i++ ) {
            a.data[i] = Math.abs(a.data[i]);
        }
    }

    /**
     * Given a symmetric matrix which is represented by a lower triangular matrix convert it back into
     * a full symmetric matrix.
     *
     * @param A (Input) Lower triangular matrix (Output) symmetric matrix
     */
    public static void symmLowerToFull( DMatrixRMaj A )
    {
        if( A.numRows != A.numCols )
            throw new MatrixDimensionException("Must be a square matrix");

        final int cols = A.numCols;

        for (int row = 0; row < A.numRows; row++) {
            for (int col = row+1; col < cols; col++) {
                A.data[row*cols+col] = A.data[col*cols+row];
            }
        }
    }

    /**
     * Given a symmetric matrix which is represented by a lower triangular matrix convert it back into
     * a full symmetric matrix.
     *
     * @param A (Input) Lower triangular matrix (Output) symmetric matrix
     */
    public static void symmUpperToFull( DMatrixRMaj A )
    {
        if( A.numRows != A.numCols )
            throw new MatrixDimensionException("Must be a square matrix");

        final int cols = A.numCols;

        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col <= row; col++) {
                A.data[row*cols+col] = A.data[col*cols+row];
            }
        }
    }
}
