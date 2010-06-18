/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

package org.ejml.ops;

import org.ejml.EjmlParameters;
import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt;
import org.ejml.alg.dense.linsol.LinearSolver;
import org.ejml.alg.dense.linsol.LinearSolverFactory;
import org.ejml.alg.dense.linsol.lu.LinearSolverLu;
import org.ejml.alg.dense.misc.UnrolledDeterminantFromMinor;
import org.ejml.alg.dense.misc.UnrolledInverseFromMinor;
import org.ejml.alg.dense.mult.MatrixMatrixMult;
import org.ejml.alg.dense.mult.MatrixVectorMult;
import org.ejml.data.DenseMatrix64F;


/**
 * <p>
 * Common matrix operations are contained here.  Which specific underlying algorithm is used
 * is not specified just the out come of the operation.  Nor should calls to these functions
 * reply on the underlying implementation.  Which algorithm is used can depend on the matrix
 * being passed in.
 * </p>
 * <p>
 * For more exotic and specialized generic operations see {@link org.ejml.ops.SpecializedOps}.
 * </p>
 * @see org.ejml.alg.dense.mult.MatrixMatrixMult
 * @see org.ejml.alg.dense.mult.MatrixVectorMult
 * @see org.ejml.ops.SpecializedOps
 * @see org.ejml.ops.MatrixFeatures
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"ForLoopReplaceableByForEach"})
public class CommonOps {
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
    public static void mult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( b.numCols == 1 ) {
            MatrixVectorMult.mult(a,b,c);
        } else if( b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
            MatrixMatrixMult.mult_reorder(a,b,c);
        } else {
            MatrixMatrixMult.mult_small(a,b,c);
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
    public static void mult( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        // TODO add a matrix vectory multiply here
        if( b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
            MatrixMatrixMult.mult_reorder(alpha,a,b,c);
        } else {
            MatrixMatrixMult.mult_small(alpha,a,b,c);
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
    public static void multTransA( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( b.numCols == 1 ) {
            // todo check a.numCols == 1 and do inner product?
            // there are significantly faster algorithms when dealing with vectors
            if( a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
                MatrixVectorMult.multTransA_reorder(a,b,c);
            } else {
                MatrixVectorMult.multTransA_small(a,b,c);
            }
        } else if( a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ||
                b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH  ) {
            MatrixMatrixMult.multTransA_reorder(a,b,c);
        } else {
            MatrixMatrixMult.multTransA_small(a,b,c);
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
    public static void multTransA( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        // TODO add a matrix vectory multiply here
        if( a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ||
                b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
            MatrixMatrixMult.multTransA_reorder(alpha,a,b,c);
        } else {
            MatrixMatrixMult.multTransA_small(alpha,a,b,c);
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
    public static void multTransB( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( b.numRows == 1 ) {
            MatrixVectorMult.mult(a,b,c);
        } else {
            MatrixMatrixMult.multTransB(a,b,c);
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
    public static void multTransB( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        // TODO add a matrix vectory multiply here
        MatrixMatrixMult.multTransB(alpha,a,b,c);
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
    public static void multTransAB( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( b.numRows == 1) {
            // there are significantly faster algorithms when dealing with vectors
            if( a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
                MatrixVectorMult.multTransA_reorder(a,b,c);
            } else {
                MatrixVectorMult.multTransA_small(a,b,c);
            }
        } else if( a.numCols >= EjmlParameters.MULT_TRANAB_COLUMN_SWITCH ) {
            MatrixMatrixMult.multTransAB_aux(a,b,c,null);
        } else {
            MatrixMatrixMult.multTransAB(a,b,c);
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
    public static void multTransAB( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        // TODO add a matrix vectory multiply here
        if( a.numCols >= EjmlParameters.MULT_TRANAB_COLUMN_SWITCH ) {
            MatrixMatrixMult.multTransAB_aux(alpha,a,b,c,null);
        } else {
            MatrixMatrixMult.multTransAB(alpha,a,b,c);
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
    public static void multAdd( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( b.numCols == 1 ) {
            MatrixVectorMult.multAdd(a,b,c);
        } else {
            if( b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
                MatrixMatrixMult.multAdd_reorder(a,b,c);
            } else {
                MatrixMatrixMult.multAdd_small(a,b,c);
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
    public static void multAdd( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        // TODO add a matrix vectory multiply here
        if( b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
            MatrixMatrixMult.multAdd_reorder(alpha,a,b,c);
        } else {
            MatrixMatrixMult.multAdd_small(alpha,a,b,c);
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
    public static void multAddTransA( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( b.numCols == 1 ) {
            if( a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
                MatrixVectorMult.multAddTransA_reorder(a,b,c);
            } else {
                MatrixVectorMult.multAddTransA_small(a,b,c);
            }
        } else {
            if( a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ||
                    b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH  ) {
                MatrixMatrixMult.multAddTransA_reorder(a,b,c);
            } else {
                MatrixMatrixMult.multAddTransA_small(a,b,c);
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
    public static void multAddTransA( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        // TODO add a matrix vectory multiply here
        if( a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ||
                b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
            MatrixMatrixMult.multAddTransA_reorder(alpha,a,b,c);
        } else {
            MatrixMatrixMult.multAddTransA_small(alpha,a,b,c);
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
    public static void multAddTransB( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        MatrixMatrixMult.multAddTransB(a,b,c);
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
    public static void multAddTransB( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        // TODO add a matrix vectory multiply here
        MatrixMatrixMult.multAddTransB(alpha,a,b,c);
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * c = c + a<sup>T</sup> * b<sup>T</sup><br>
     * c<sub>ij</sub> = c<sub>ij</sub> + &sum;<sub>k=1:n</sub> { a<sub>ki</sub> * b<sub>jk</sub>}
     * </p>
     *
     * @param a The left matrix in the multiplication operation. Not Modifed.
     * @param b The right matrix in the multiplication operation. Not Modifed.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransAB( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( b.numRows == 1 ) {
            // there are significantly faster algorithms when dealing with vectors
            if( a.numCols >= EjmlParameters.MULT_COLUMN_SWITCH ) {
                MatrixVectorMult.multAddTransA_reorder(a,b,c);
            } else {
                MatrixVectorMult.multAddTransA_small(a,b,c);
            }
        } else if( a.numCols >= EjmlParameters.MULT_TRANAB_COLUMN_SWITCH ) {
            MatrixMatrixMult.multAddTransAB_aux(a,b,c,null);
        } else {
            MatrixMatrixMult.multAddTransAB(a,b,c);
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
     * @param a The left matrix in the multiplication operation. Not Modifed.
     * @param b The right matrix in the multiplication operation. Not Modifed.
     * @param c Where the results of the operation are stored. Modified.
     */
    public static void multAddTransAB( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        // TODO add a matrix vectory multiply here
        if( a.numCols >= EjmlParameters.MULT_TRANAB_COLUMN_SWITCH ) {
            MatrixMatrixMult.multAddTransAB_aux(alpha,a,b,c,null);
        } else {
            MatrixMatrixMult.multAddTransAB(alpha,a,b,c);
        }
    }

    /**
     * <p>Performs the an element by element multiplication operation:<br>
     * <br>
     * a<sub>ij</sub> = a<sub>ij</sub> * b<sub>ij</sub> <br>
     * </p>
     * @param a The left matrix in the multiplication operation. Modified.
     * @param b The right matrix in the multiplication operation. Not modified.
     */
    public static void elementMult( DenseMatrix64F a , DenseMatrix64F b )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows ) {
            throw new RuntimeException("The 'a' and 'b' matrices do not have compatable dimensions");
        }

        int length = a.getNumElements();
        double dataA[] = a.data;
        double dataB[] = b.data;

        for( int i = 0; i < length; i++ ) {
            dataA[i] *= dataB[i];
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
    public static void elementMult( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows
                || a.numRows != c.numRows || a.numCols != c.numCols ) {
            throw new RuntimeException("The 'a' and 'b' matrices do not have compatable dimensions");
        }

        int length = a.getNumElements();
        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        for( int i = 0; i < length; i++ ) {
            dataC[i] = dataA[i] * dataB[i];
        }
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
    public static void addEquals( DenseMatrix64F a , DenseMatrix64F b )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows ) {
            throw new RuntimeException("The 'a' and 'b' matrices do not have compatable dimensions");
        }

        final double dataA[] = a.data;
        final double dataB[] = b.data;

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            dataA[i] += dataB[i];
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
    public static void addEquals( DenseMatrix64F a , double beta, DenseMatrix64F b )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows ) {
            throw new RuntimeException("The 'a' and 'b' matrices do not have compatable dimensions");
        }

        final double dataA[] = a.data;
        final double dataB[] = b.data;

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            dataA[i] += beta * dataB[i];
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
    public static void add( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows
                || a.numCols != c.numCols || a.numRows != c.numRows ) {
            throw new RuntimeException("The matrices are not all the same dimension.");
        }

        final double dataA[] = a.data;
        final double dataB[] = b.data;
        final double dataC[] = c.data;

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            dataC[i] = dataA[i]+dataB[i];
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
    public static void add( DenseMatrix64F a , double beta , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows
                || a.numCols != c.numCols || a.numRows != c.numRows ) {
            throw new RuntimeException("The matrices are not all the same dimension.");
        }

        final double dataA[] = a.data;
        final double dataB[] = b.data;
        final double dataC[] = c.data;

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            dataC[i] = dataA[i]+beta*dataB[i];
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
    public static void add( double alpha , DenseMatrix64F a , double beta , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows
                || a.numCols != c.numCols || a.numRows != c.numRows ) {
            throw new RuntimeException("The matrices are not all the same dimension.");
        }

        final double dataA[] = a.data;
        final double dataB[] = b.data;
        final double dataC[] = c.data;

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            dataC[i] = alpha*dataA[i]+beta*dataB[i];
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
    public static void add( double alpha , DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows
                || a.numCols != c.numCols || a.numRows != c.numRows ) {
            throw new RuntimeException("The matrices are not all the same dimension.");
        }

        final double dataA[] = a.data;
        final double dataB[] = b.data;
        final double dataC[] = c.data;

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            dataC[i] = alpha*dataA[i]+dataB[i];
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
    public static void add( DenseMatrix64F a , double val ) {
        final double dataA[] = a.data;

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            dataA[i] += val;
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
    public static void add( DenseMatrix64F a , double val , DenseMatrix64F c ) {
        if( a.numRows != c.numRows || a.numCols != c.numCols ) {
            throw new IllegalArgumentException("Dimensions of a and c do not match.");
        }
        final double dataA[] = a.data;
        final double dataC[] = c.data;

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            dataC[i] = dataA[i]+val;
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
    public static void subEquals( DenseMatrix64F a , DenseMatrix64F b )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows ) {
            throw new RuntimeException("The 'a' and 'b' matrices do not have compatable dimensions");
        }

        final double dataA[] = a.data;
        final double dataB[] = b.data;

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            dataA[i] -= dataB[i];
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
    public static void sub( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F c )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows ) {
            throw new RuntimeException("The 'a' and 'b' matrices do not have compatable dimensions");
        }

        final double dataA[] = a.data;
        final double dataB[] = b.data;
        final double dataC[] = c.data;

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            dataC[i] = dataA[i] - dataB[i];
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
     * If repeat calls to solve are being made then one should consider using {@link LinearSolverFactory}
     * instead.
     * </p>
     *
     * <p>
     * It is ok for 'b' and 'x' to be the same matrix.
     * </p>
     *
     * @param a A matrix that is m by m. Not modified.
     * @param b A matrix that is m by n. Not modified.
     * @param x A matrix that is m by n. Modified.
     *
     * @return true if it could invert the matrix false if it could not.
     */
    public static boolean solve( DenseMatrix64F a , DenseMatrix64F b , DenseMatrix64F x )
    {
        LinearSolver solver;

        if( a.numRows == a.numCols ) {
            solver = LinearSolverFactory.linear();
        } else if( a.numRows > a.numCols ) {
            solver = LinearSolverFactory.leastSquares();
        } else {
            throw new IllegalArgumentException("Can't solve for under determined systems since there are an infinite number of solutions.");
        }

        if( !solver.setA(a) )
            return false;

        solver.solve(b,x);
        return true;
    }

    /**
     * <p>
     * Performs an in-place element by element scalar multiplation.<br>
     * <br>
     * a<sub>ij</sub> = &alpha;*a<sub>ij</sub>
     * </p>
     *
     * @param a The matrix that is to be scalled.  Modified.
     * @param alpha the amount each element is multiplied by.
     */
    public static void scale( double alpha , DenseMatrix64F a )
    {
        final double data[] = a.data;
        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            data[i] *= alpha;
        }
    }

    /**
     * <p>
     * Performs an element by element scalar multiplation.<br>
     * <br>
     * b<sub>ij</sub> = &alpha;*a<sub>ij</sub>
     * </p>
     *
     * @param a The matrix that is to be scalled.  Modified.
     * @param alpha the amount each element is multiplied by.
     */
    public static void scale( double alpha , DenseMatrix64F a , DenseMatrix64F b)
    {
        if( a.numRows != b.numRows || a.numCols != b.numCols )
            throw new IllegalArgumentException("Matrices must have the same shape");

        final double dataA[] = a.data;
        final double dataB[] = b.data;
        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            dataB[i] = dataA[i]*alpha;
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
    public static void changeSign( DenseMatrix64F a )
    {
        final double data[] = a.data;
        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            data[i] = -data[i];
        }
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
    public static void set( DenseMatrix64F a , double value )
    {
        final double data[] = a.data;
        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            data[i] = value;
        }
    }

    /**
     * Performs an in-place transpose.  This algorithm is only efficient for square
     * matrices.
     *
     * @param mat The matrix that is to be transposed. Modified.
     */
    public static void transpose( DenseMatrix64F mat ) {
        if( mat.numCols == mat.numRows ){
            final double data[] = mat.data;

            for( int i = 0; i < mat.numRows; i++ ) {
                int index = i*mat.numCols+i+1;
                for( int j = i+1; j < mat.numCols; j++ ) {
                    int otherIndex = j*mat.numCols+i;
                    double val = data[index];
                    data[index] = data[otherIndex];
                    data[otherIndex] = val;
                    index++;
                }
            }
        } else {
            DenseMatrix64F b = new DenseMatrix64F(mat.numCols,mat.numRows);
            transpose(mat,b);
            mat.setReshape(b);
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
     * @param a The original matrix.  Not modified.
     * @param b Where the transpose is stored. Modified.
     */
    public static void transpose( DenseMatrix64F a , DenseMatrix64F b )
    {
        if( a.numRows != b.numCols || a.numCols != b.numRows ) {
            throw new RuntimeException("Incompatible matrix dimensions");
        }
        final double rdata[] = b.data;
        final double data[] = a.data;

        int index = 0;
        for( int i = 0; i < b.numRows; i++ ) {
            int index2 = i;

            for( int j = 0; j < b.numCols; j++ ) {
                rdata[index++] = data[index2];
                index2 += a.numCols;
            }
        }
    }

    /**
     * <p>
     * Returns the value of the element in the matrix that has the largest value.<br>
     * <br>
     * Max{ a<sub>ij</sub> } for all i and j<br>
     * </p>
     *
     * @param a A matrix.
     * @return The max element value of the matrix.
     */
    public static double elementMax( DenseMatrix64F a ) {
        final int size = a.getNumElements();

        final double dataA[] = a.data;

        double max = dataA[0];
        for( int i = 1; i < size; i++ ) {
            double val = dataA[i];
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
     * @param a A matrix.
     * @return The max element value of the matrix.
     */
    public static double elementMaxAbs( DenseMatrix64F a ) {
        final int size = a.getNumElements();

        final double dataA[] = a.data;

        double max = 0;
        for( int i = 0; i < size; i++ ) {
            double val = Math.abs(dataA[i]);
            if( val >= max ) {
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
     * @param a A matrix.
     * @return The value of element in the matrix with the minimum value.
     */
    public static double elementMin( DenseMatrix64F a ) {
        final int size = a.getNumElements();

        final double dataA[] = a.data;

        double min = dataA[0];
        for( int i = 1; i < size; i++ ) {
            double val = dataA[i];
            if( val < min ) {
                min = val;
            }
        }

        return min;
    }

    /**
     * <p>
     * This computes the trace of the matrix:<br>
     * <br>
     * trace = &sum;<sub>i=1:n</sub> { a<sub>ii</sub> }
     * </p>
     * <p>
     * The trace is only defined for square matrices.
     * </p>
     *
     * @param a A square matrix.  Not modified.
     */
    public static double trace( DenseMatrix64F a ) {
        if( a.numRows != a.numCols ) {
            throw new IllegalArgumentException("The matrix must be square");
        }

        final double data[] = a.data;

        double sum = 0;
        int index = 0;
        for( int i = 0; i < a.numRows; i++ ) {
            sum += data[index];
            index += 1 + a.numCols;
        }

        return sum;
    }

    /**
     * Returns the determinant of the matrix.  If the inverse of the matrix is also
     * needed, then using {@link org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt} directly (or any
     * similar algorithm) can be more efficient.
     *
     * @param mat The matrix whose determinant is to be computed.  Not modified.
     * @return The determinant.
     */
    public static double det( DenseMatrix64F mat )
    {

        int numCol = mat.getNumCols();
        int numRow = mat.getNumRows();

        if( numCol != numRow ) {
            throw new IllegalArgumentException("Must be a square matrix.");
        } else if( numCol <= UnrolledDeterminantFromMinor.MAX ) {
            // slight performance boost overall by doing it this way
            // when it was the case statement the VM did some strange optimization
            // and made case 2 about 1/2 the speed
            if( numCol >= 2 ) {
                return UnrolledDeterminantFromMinor.det(mat);
            } else {
                return mat.data[0];
            }
        } else {
            LUDecompositionAlt alg = new LUDecompositionAlt();
            if( !alg.decompose(mat) )
                return 0.0;
            return alg.computeDeterminant();
        }
    }

    /**
     * <p>
     * Performs a matrix inversion operation on the specified matrix and stores the results
     * in the same matrix.<br>
     * <br>
     * a = a<sup>-1<sup>
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
    public static boolean invert( DenseMatrix64F mat) {
        if( mat.numCols <= UnrolledInverseFromMinor.MAX ) {
            if( mat.numCols != mat.numRows ) {
                throw new IllegalArgumentException("Must be a square matrix.");
            }

            if( mat.numCols >= 2 ) {
                UnrolledInverseFromMinor.inv(mat,mat);
            } else {
                mat.data[0] = 1.0/mat.data[0];
            }
        } else {
            LUDecompositionAlt alg = new LUDecompositionAlt();
            LinearSolverLu solver = new LinearSolverLu(alg);
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
     * b = a<sup>-1<sup>
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
     * {@link LinearSolverFactory} instead.
     * </p>
     *
     * @param mat The matrix that is to be inverted. Not modified.
     * @param result Where the inverse matrix is stored.  Modified.
     * @return true if it could invert the matrix false if it could not.
     */
    public static boolean invert( DenseMatrix64F mat, DenseMatrix64F result ) {
        if( mat.numCols <= UnrolledInverseFromMinor.MAX ) {
            if( mat.numCols != mat.numRows ) {
                throw new IllegalArgumentException("Must be a square matrix.");
            }
            if( result.numCols >= 2 ) {
                UnrolledInverseFromMinor.inv(mat,result);
            } else {
                result.data[0] = 1.0/mat.data[0];
            }
        } else {
            LUDecompositionAlt alg = new LUDecompositionAlt();
            LinearSolverLu solver = new LinearSolverLu(alg);

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
     * If one needs to solve a system where m != n, then {@link CommonOps#solve solve} should be used instead since it
     * will produce a more accurate answer faster than using the pinv.
     * </p>
     * @param A Non-singular m by n matrix.  Not modified.
     * @param invA Where the computed pseudo inverse is stored. n by m.  Modified.
     * @return
     */
    public static void pinv( DenseMatrix64F A , DenseMatrix64F invA ) {
        if( A.numRows == A.numCols ) {
            invert(A,invA);
        } else if( A.numRows > A.numCols ) {
            DenseMatrix64F ATA = new DenseMatrix64F(A.numCols,A.numCols);
            CommonOps.multTransA(A,A,ATA);
            if( !CommonOps.invert(ATA) )
                throw new IllegalArgumentException("ATA can't be inverted.");

            CommonOps.multTransB(ATA,A,invA);
        } else {
            DenseMatrix64F AAT = new DenseMatrix64F(A.numRows,A.numRows);
            CommonOps.multTransB(A,A, AAT);
            if( !CommonOps.invert(AAT) )
                throw new IllegalArgumentException("ATA can't be inverted.");

            CommonOps.multTransA(A,AAT,invA);
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
    public static double elementSum( DenseMatrix64F mat ) {
        double total = 0;

        int size = mat.getNumElements();

        for( int i = 0; i < size; i++ ) {
            total += mat.data[i];
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
    public static double elementSumAbs( DenseMatrix64F mat ) {
        double total = 0;

        int size = mat.getNumElements();

        for( int i = 0; i < size; i++ ) {
            total += Math.abs(mat.data[i]);
        }

        return total;
    }

    /**
     * Converts the columns in a matrix into a set of vectors.
     *
     * @param A Matrix.  Not modified.
     * @param v
     * @return An array of vectors.
     */
    public static DenseMatrix64F[] columnsToVector(DenseMatrix64F A, DenseMatrix64F[] v)
    {
        DenseMatrix64F []ret;
        if( v == null || v.length < A.numCols ) {
            ret = new DenseMatrix64F[ A.numCols ];
        } else {
            ret = v;
        }


        for( int i = 0; i < ret.length; i++ ) {
            if( ret[i] == null ) {
                ret[i] = new DenseMatrix64F(A.numRows,1);
            } else {
                ret[i].reshape(A.numRows,1, false);
            }
            
            DenseMatrix64F u = ret[i];

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
    public static DenseMatrix64F[] rowsToVector(DenseMatrix64F A, DenseMatrix64F[] v)
    {
        DenseMatrix64F []ret;
        if( v == null || v.length < A.numRows ) {
            ret = new DenseMatrix64F[ A.numRows ];
        } else {
            ret = v;
        }


        for( int i = 0; i < ret.length; i++ ) {
            if( ret[i] == null ) {
                ret[i] = new DenseMatrix64F(A.numCols,1);
            } else {
                ret[i].reshape(A.numCols,1, false);
            }

            DenseMatrix64F u = ret[i];

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
    public static void setIdentity( DenseMatrix64F mat )
    {
        int width = mat.numRows < mat.numCols ? mat.numRows : mat.numCols;

        double data[] = mat.data;
        int length = mat.getNumElements();

        for( int i = 0; i < length; i++ ) {
            data[i] = 0;
        }

        int index = 0;
        for( int i = 0; i < width; i++ , index += mat.numCols + 1) {
            data[index] = 1;
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
    public static DenseMatrix64F identity( int width )
    {
        DenseMatrix64F ret = new DenseMatrix64F(width,width);

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
    public static DenseMatrix64F identity( int numRows , int numCols )
    {
        DenseMatrix64F ret = new DenseMatrix64F(numRows,numCols);

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
    public static DenseMatrix64F diag( double ...diagEl )
    {
        return diag(null,diagEl.length,diagEl);
    }

    public static DenseMatrix64F diag( DenseMatrix64F ret , int width , double ...diagEl )
    {
        if( ret == null ) {
            ret = new DenseMatrix64F(width,width);
        } else {
            if( ret.numRows != width || ret.numCols != width )
                throw new IllegalArgumentException("Unexpected matrix size");

            CommonOps.set(ret,0);
        }

        for( int i = 0; i < width; i++ ) {
            ret.set(i,i,diagEl[i]);
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
    public static DenseMatrix64F diagR( int numRows , int numCols , double ...diagEl )
    {
        DenseMatrix64F ret = new DenseMatrix64F(numRows,numCols);

        int o = Math.min(numRows,numCols);

        for( int i = 0; i < o; i++ ) {
            ret.set(i,i,diagEl[i]);
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
     * @return The results of the operation.
     */
    public static DenseMatrix64F kron( DenseMatrix64F A , DenseMatrix64F B , DenseMatrix64F C )
    {
        int w = A.numCols*B.numCols;
        int h = A.numRows*B.numRows;
        if( C == null ) {
            C = new DenseMatrix64F(w,h);
        } else if( C.numCols != w || C.numRows != h) {
            throw new IllegalArgumentException("C does not have the expected dimensions");
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

        return C;
    }
}
