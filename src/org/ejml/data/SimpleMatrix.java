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

package org.ejml.data;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.decomposition.EigenDecomposition;
import org.ejml.alg.dense.decomposition.SingularMatrixException;
import org.ejml.alg.dense.decomposition.SingularValueDecomposition;
import org.ejml.ops.*;

import java.util.Random;


/**
 * <p>
 * {@link org.ejml.data.SimpleMatrix} is a wrapper around {@link org.ejml.data.DenseMatrix64F} that provides an
 * easy to use object oriented interface for performing matrix operations.  It is designed to be
 * more accessible to novice programmers and provide a way to rapidly code up solutions.  The disadvantage
 * of using this class is that it is more resource intensive.  While hard to generalize, in a KalmanFilter example there was
 * about a 30% performance hit when SimpleMatrix was used exclusively.
 * </p>
 *
 * <p>
 * It is easy to work with both {@link org.ejml.data.DenseMatrix64F} and SimpleMatrix in the same code base.
 * To access the internal DenseMatrix64F in a SimpleMatrix simply call {@link org.ejml.data.SimpleMatrix#getMatrix()}.
 * To turn a DenseMatrix64F into a SimpleMatrix use {@link SimpleMatrix#wrap(DenseMatrix64F)}.
 * </p>
 *
 * <p>
 * Most of the functions related to this class do not modify the original matrix.  Instead they
 * create a new instance which is modified and returned.  This greatly simplifies memory
 * management and writing of code in general.
 * </p>
 *
 * @author Peter Abeles
 */
public class SimpleMatrix {

    /**
     * The matrix data that this is a wrapper around.
     */
    protected DenseMatrix64F mat;

    /**
     * Creates a new matrix with the specified initial value.
     *
     * @see DenseMatrix64F#DenseMatrix64F(int,int,boolean,double...)
     *
     * @param numRows The number of rows.
     * @param numCols The number of columns.
     * @param data The row-major formatted 1D array. Not modified.
     */
    public SimpleMatrix( int numRows , int numCols , double data[] ) {
        mat = new DenseMatrix64F(numRows,numCols, true, data);
    }

    /**
     * Creates a new matrix with the specified initial value.
     *
     * @see org.ejml.data.DenseMatrix64F#DenseMatrix64F(double[][])
     *
     * @param data 2D array representation of the matrix. Not modified.
     */
    public SimpleMatrix(double data[][]) {
        mat = new DenseMatrix64F(data);
    }

    /**
     * Creates a new matrix that is initially set to zero with the specified dimenisions.
     *
     * @see org.ejml.data.DenseMatrix64F#DenseMatrix64F(int, int) 
     *
     * @param numRows The number of rows in the matrix.
     * @param numCols The number of columns in the matrix.
     */
    public SimpleMatrix(int numRows, int numCols) {
        mat = new DenseMatrix64F(numRows, numCols);
    }

    /**
     * Creats a new SimpleMatrix which is identical to the original.
     *
     * @param orig The matrix which is to be copied. Not modified.
     */
    public SimpleMatrix( SimpleMatrix orig ) {
        this.mat = orig.mat.copy();
    }

    /**
     * Creates a new SimpleMatrix which is a copy of the DenseMatrix64F.
     *
     * @param orig The original matrix whose value is copied.  Not modified.
     */
    public SimpleMatrix( DenseMatrix64F orig ) {
        this.mat = orig.copy();
    }

    protected SimpleMatrix(){}

    /**
     * Creates a new SimpleMatrix with the specified DenseMatrix64F used as its internal matrix.  This means
     * that the reference is saved and calls made to the returned SimpleMatrix will modify the passed in DenseMatrix64F.
     *
     * @param internalMat The internal DenseMatrix64F of the returned SimpleMatrix. Will be modified.
     */
    public static SimpleMatrix wrap( DenseMatrix64F internalMat ) {
        SimpleMatrix ret = new SimpleMatrix();
        ret.mat = internalMat;
        return ret;
    }

    /**
     * Creates a new idenity matrix with the specified size.
     *
     * @see org.ejml.ops.CommonOps#identity(int)
     *
     * @param width The width and height of the matrix.
     * @return An identity matrix.
     */
    public static SimpleMatrix identity( int width ) {
        SimpleMatrix ret = new SimpleMatrix(width,width);

        CommonOps.setIdentity(ret.mat);

        return ret;
    }

    /**
     * Creates a matrix where all but the diagonal elements are zero.  The values
     * of the diagonal elements are specified by the parameter 'vals'.
     *
     * @see org.ejml.ops.CommonOps#diag(double...)
     *
     * @param vals The values of the diagonal elements.
     * @return A diagonal matrix.
     */
    public static SimpleMatrix diag( double ...vals ) {
        DenseMatrix64F m = CommonOps.diag(vals);
        SimpleMatrix ret = wrap(m);
        return ret;
    }

    /**
     * <p>
     * Returns a reference to the matrix that it uses internally.  This is useful
     * when an operation is needed that is not provided by this class.
     * </p>
     *
     * @return Reference to the internal DenseMatrix64F.
     */
    public DenseMatrix64F getMatrix() {
        return mat;
    }

    /**
     * <p>
     * Returns the transpose of this matrix.<br>
     * a<sup>T</sup>
     * </p>
     *
     * @see CommonOps#transpose(DenseMatrix64F,DenseMatrix64F)
     *
     * @return A matrix that is n by m.
     */
    public SimpleMatrix transpose() {
        SimpleMatrix ret = new SimpleMatrix(mat.numCols,mat.numRows);

        CommonOps.transpose(mat,ret.mat);

        return ret;
    }

    /**
     * <p>
     * Performs a matrix multiplication operation.<br>
     * <br>
     * c = a * b <br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
     * </p>
     *
     * @see CommonOps#mult(DenseMatrix64F, DenseMatrix64F, DenseMatrix64F)
     *
     * @param b A matrix that is n by bn. Not modified.
     *
     * @return The results of this operation.
     */
    public SimpleMatrix mult( SimpleMatrix b ) {
        SimpleMatrix ret = new SimpleMatrix(mat.numRows,b.mat.numCols);

        CommonOps.mult(mat,b.mat,ret.mat);

        return ret;
    }

    // TODO should this function be added back?  It makes the code hard to read when its used
//    /**
//     * <p>
//     * Performs one of the following matrix multiplication operations:<br>
//     * <br>
//     * c = a * b <br>
//     * c = a<sup>T</sup> * b <br>
//     * c = a * b <sup>T</sup><br>
//     * c = a<sup>T</sup> * b <sup>T</sup><br>
//     * <br>
//     * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
//     * </p>
//     *
//     * @see CommonOps#mult(DenseMatrix64F, DenseMatrix64F, DenseMatrix64F)
//     * @see CommonOps#multTransA(DenseMatrix64F, DenseMatrix64F, DenseMatrix64F)
//     * @see CommonOps#multTransB(DenseMatrix64F, DenseMatrix64F, DenseMatrix64F)
//     * @see CommonOps#multTransAB(DenseMatrix64F, DenseMatrix64F, DenseMatrix64F)
//     *
//     * @param tranA If true matrix A is transposed.
//     * @param tranB If true matrix B is transposed.
//     * @param b A matrix that is n by bn. Not modified.
//     *
//     * @return The results of this operation.
//     */
//    public SimpleMatrix mult( boolean tranA , boolean tranB , SimpleMatrix b) {
//        SimpleMatrix ret;
//
//        if( tranA && tranB ) {
//            ret = new SimpleMatrix(mat.numCols,b.mat.numRows);
//            CommonOps.multTransAB(mat,b.mat,ret.mat);
//        } else if( tranA ) {
//            ret = new SimpleMatrix(mat.numCols,b.mat.numCols);
//            CommonOps.multTransA(mat,b.mat,ret.mat);
//        } else if( tranB ) {
//            ret = new SimpleMatrix(mat.numRows,b.mat.numRows);
//            CommonOps.multTransB(mat,b.mat,ret.mat);
//        }  else  {
//            ret = new SimpleMatrix(mat.numRows,b.mat.numCols);
//            CommonOps.mult(mat,b.mat,ret.mat);
//        }
//
//        return ret;
//    }

    /**
     * <p>
     * Performs a matrix addition operation.<br>
     * <br>
     * c = a + b <br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
     * </p>
     *
     * @see CommonOps#mult(DenseMatrix64F, DenseMatrix64F, DenseMatrix64F)
     *
     * @param b m by n matrix. Not modified.
     *
     * @return The results of this operation.
     */
    public SimpleMatrix plus( SimpleMatrix b ) {
        SimpleMatrix ret = new SimpleMatrix(this);

        CommonOps.addEquals(ret.mat,b.mat);

        return ret;
    }

    /**
     * <p>
     * Performs a matrix subtraction operation.<br>
     * <br>
     * c = a - b <br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
     * </p>
     *
     * @see CommonOps#sub(DenseMatrix64F, DenseMatrix64F, DenseMatrix64F)
     *
     * @param b m by n matrix. Not modified.
     *
     * @return The results of this operation.
     */
    public SimpleMatrix minus( SimpleMatrix b ) {
        SimpleMatrix ret = new SimpleMatrix(this);

        CommonOps.subEquals(ret.mat,b.mat);

        return ret;
    }

    /**
     * <p>
     * Performs a matrix addition and scale operation.<br>
     * <br>
     * c = a + &beta;*b <br>
     * <br>
     * where c is the returned matrix, a is this matrix, and b is the passed in matrix.
     * </p>
     *
     * @see CommonOps#add( DenseMatrix64F, double , DenseMatrix64F, DenseMatrix64F)
     *
     * @param b m by n matrix. Not modified.
     *
     * @return A matrix that contains the results.
     */
    public SimpleMatrix plus( double beta , SimpleMatrix b ) {
        SimpleMatrix ret = new SimpleMatrix(this);

        CommonOps.addEquals(ret.mat,beta,b.mat);

        return ret;
    }

    /**
     * Multiplies each element in this matrix by the specified value.
     *
     * @see CommonOps#scale(double, DenseMatrix64F)
     *
     * @param val The multiplication factor.
     * @return The scaled matrix.
     */
    public SimpleMatrix scale( double val ) {
        SimpleMatrix ret = new SimpleMatrix(this);

        CommonOps.scale(val,ret.mat);

        return ret;
    }

    /**
     * <p>
     * Returns the inverse of this matrix.<br>
     * <br>
     * a<sup>-1<sup><br>
     * </p>
     *
     * <p>
     * If the matrix could not be inverted then SingularMatrixException is thrown.  Even
     * if no exception is thrown the matrix could still be singular or nearly singular.
     * </p>
     *
     * @see CommonOps#invert(DenseMatrix64F, DenseMatrix64F)
     *
     * @throws SingularMatrixException
     *
     * @return The inverse of this matrix.
     */
    public SimpleMatrix invert() {
        SimpleMatrix ret = new SimpleMatrix(mat.numRows,mat.numCols);
        if( !CommonOps.invert(mat,ret.mat) ) {
            throw new SingularMatrixException();
        }
        return ret;
    }

    /**
     * <p>
     * Solves for X in the following equation:<br>
     * <br>
     * x = a<sup>-1</sup>b<br>
     * <br>
     * where 'a' is this matrix and 'b' is an n by p matrix.
     * </p>
     *
     * <p>
     * If the system could not be solved then SingularMatrixException is thrown.  Even
     * if no exception is thrown 'a' could still be singular or nearly singular.
     * </p>
     *
     * @see CommonOps#solve(DenseMatrix64F, DenseMatrix64F, DenseMatrix64F)
     *
     * @throws SingularMatrixException
     *
     * @param b n by p matrix. Not modified.
     * @return The solution for 'x' that is n by p.
     */
    public SimpleMatrix solve( SimpleMatrix b )
    {
        SimpleMatrix x = new SimpleMatrix(mat.numCols,b.mat.numCols);

        if( !CommonOps.solve(mat,b.mat,x.mat) )
            throw new SingularMatrixException();

        return x;
    }


    /**
     * Sets the elements in this matrix to be equal to the elements in the passed in matrix.
     * Both matrix must have the same dimension.
     *
     * @param a The matrix whose value this matrix is being set to.
     */
    public void set( SimpleMatrix a ) {
        mat.set(a.getMatrix());
    }


    /**
     * <p>
     * Sets all the elements in the matrix equal to the specified value.<br>
     * <br>
     * a<sub>ij</sub> = val<br>
     * </p>
     *
     * @see CommonOps#set(DenseMatrix64F, double)
     *
     * @param val The value each element is set to.
     */
    public void set( double val ) {
        CommonOps.set(mat,val);
    }

    /**
     * Sets all the elements in the matrix equal to zero.
     *
     * @see CommonOps#set(DenseMatrix64F, double)
     */
    public void zero() {
        mat.zero();
    }

    /**
     * Computes the Frobenius normal of the matrix.
     *
     * @see NormOps#normF(DenseMatrix64F)
     *
     * @return The matrix's Frobenius normal.
     */
    public double normF() {
        return NormOps.normF(mat);
    }

    /**
     * Computes the determinant of the matrix.
     *
     * @see CommonOps#det(DenseMatrix64F)
     *
     * @return The determinant.
     */
    public double determinant() {
        return CommonOps.det(mat);
    }

    /**
     * <p>
     * Computes the trace of the matrix.
     * </p>
     *
     * @see CommonOps#trace(DenseMatrix64F)
     *
     * @return The trace of the matrix.
     */
    public double trace() {
        return CommonOps.trace(mat);
    }

    /**
     * <p>
     * Computes the singular value decomposition (SVD) of this matrix.  Where the SVD of a matrix
     * A is defined as: <br>
     * A = U W V <sup>T</sup> <br>
     * where A is m by n, and U,W,V are all n by n. U and V are orthogonal matrices.
     * W is a diagonal matrix.
     * </p>
     * <p>
     * The SVD can be used to invert and solve matrices with singular elements that other
     * methodes can not.  However, it is much more expensive than those other methods.
     * </p>
     *
     * @see org.ejml.alg.dense.decomposition.SingularValueDecomposition
     *
     * @return The SVD of this matrix.
     */
    public SingularValueDecomposition computeSVD() {
        SingularValueDecomposition alg = DecompositionOps.svd();

        alg.decompose(mat);

        return alg;
    }

    /**
     * Reshapes the matrix.
     *
     * @see Matrix64F#reshape(int,int,boolean)
     *
     * @param numRows The new number of rows in the matrix.
     * @param numCols The new number of columns in the matrix.
     */
    public void reshape( int numRows , int numCols ) {
        mat.reshape(numRows,numCols, false);
    }

    /**
     * Assigns the element in the Matrix to the specified value.  Performs a bounds check to make sure
     * the requested element is part of the matrix.
     *
     * @param row The row of the element.
     * @param col The column of the element.
     * @param value The element's new value.
     */
    public void set( int row , int col , double value ) {
        mat.set(row,col,value);
    }

    /**
     * Returns the value of the specified matrix element.  Performs a bounds check to make sure
     * the requested element is part of the matrix.
     *
     * @param row The row of the element.
     * @param col The column of the element.
     * @return The value of the element.
     */
    public double get( int row , int col ) {
        return mat.get(row,col);
    }

    /**
     * Returns the value of the matrix at the specified index of the 1D row major array.
     *
     * @see org.ejml.data.DenseMatrix64F#get(int)
     *
     * @param index The element's index whose value is to be returned
     * @return The value of the specified element.
     */
    public double get( int index ) {
        return mat.data[ index ];
    }

    /**
     * Returns the index in the matrix's array.
     *
     * @see org.ejml.data.DenseMatrix64F#getIndex(int, int)
     *
     * @param row The row number.
     * @param col The column number.
     * @return The index of the specified element.
     */
    public int getIndex( int row , int col ) {
        return row * mat.numCols + col;
    }

    /**
     * Creates and returns a matrix which is idential to this one.
     *
     * @return A new identical matrix.
     */
    public SimpleMatrix copy() {
        return new SimpleMatrix(this);
    }

    /**
     * Creates a new SimpleMatrix with random elements.
     *
     * @see org.ejml.ops.RandomMatrices#setRandom(DenseMatrix64F,java.util.Random)
     *
     * @param numRows The number of rows in the new matrix
     * @param numCols The number of columns in the new matrix
     * @param rand The random number generator that's used to fill the matrix.
     * @return The new random matrix.
     */
    public static SimpleMatrix random(int numRows, int numCols, Random rand) {
        SimpleMatrix ret = new SimpleMatrix(numRows,numCols);
        RandomMatrices.setRandom(ret.mat,rand);
        return ret;
    }

    /**
     * Returns the number of rows in this matrix.
     *
     * @return number of rows.
     */
    public int numRows() {
        return mat.numRows;
    }

    /**
     * Returns the number of columns in this matrix.
     *
     * @return number of columns.
     */
    public int numCols() {
        return mat.numCols;
    }

    /**
     * Prints the matrix to standard out.
     */
    public void print() {
        UtilEjml.print(mat);
    }

    /**
     * Prints the matrix to standard out.
     */
    public void print(int numChar , int precision) {
        UtilEjml.print(mat,numChar,precision);
    }

    /**
     * <p>
     * Creates a new SimpleMatrix which is a submatrix of this matrix.
     * </p>
     * <p>
     * s<sub>i-y0 , j-x0</sub> = o<sub>ij</sub> for all y0 &le; i &le; y1 and x0 &le; j &le; x1<br>
     * <br>
     * where 's<sub>ij</sub>' is an element in the submatrix and 'o<sub>ij</sub>' is an element in the
     * original matrix.
     * </p>
     *
     * @param x0 Start column.
     * @param x1 Stop column.
     * @param y0 Start row.
     * @param y1 Stop row.
     * @return The submatrix.
     */
    public SimpleMatrix submatrix(int y0 , int y1,
                                  int x0 , int x1 ) {
        SimpleMatrix ret = new SimpleMatrix(y1-y0+1,x1-x0+1);

        SpecializedOps.extract(mat,y0,y1,x0,x1,ret.mat);

        return ret;
    }

    /**
     * Checks to see if matrix 'a' is the same as this matrix within the specified
     * tolerance.
     *
     * @param a The matrix it is being compared against.
     * @param tol How similar they must be to be equals.
     * @return If they are equal within tolerance of each other.
     */
    public boolean isIdentical(SimpleMatrix a, double tol) {
        return MatrixFeatures.isIdentical(mat,a.mat,tol);
    }

    /**
     * Checks to see if any of the elements in this matrix are either NaN or infinite.
     *
     * @return True of an element is NaN or infinite.  False otherwise.
     */
    public boolean hasUncountable() {
        return MatrixFeatures.hasUncountable(mat);
    }

    /**
     * Returns the Singular Value Decomposition (SVD) of this matrix.
     * @return SVD of this matrix.
     */
    public SVD svd() {
        return new SVD();
    }

    /**
     * Returns the Eigen Value Decomposition (EVD) of this matrix.
     */
    public EVD eig() {
        return new EVD();
    }

    /**
     * Wrapper around SVD for SimpleMatrix
     */
    public class SVD
    {
        SingularValueDecomposition svd;
        SimpleMatrix U;
        SimpleMatrix W;
        SimpleMatrix V;

        public SVD() {
            svd = DecompositionOps.svd();
            if( !svd.decompose(mat) )
                throw new RuntimeException("Decomposition failed");
            U = SimpleMatrix.wrap(svd.getU(false));
            W = SimpleMatrix.wrap(svd.getW(null));
            V = SimpleMatrix.wrap(svd.getV(false));

            // order singular values from largest to smallest
            SingularOps.descendingOrder(U.getMatrix(),false,W.getMatrix(),V.getMatrix(),false);
        }

        /**
         * <p>
         * Returns the orthogonal 'U' matrix.
         * </p>
         *
         * @return An orthogonal m by m matrix.
         */
        public SimpleMatrix getU() {
            return U;
        }

        /**
         * Returns a diagonal matrix with the singular values.  The singular values are ordered
         * from largest to smallest.
         *
         * @return Diagonal matrix with singular values along the diagonal.
         */
        public SimpleMatrix getW() {
            return W;
        }

        /**
         * <p>
         * Returns the orthogonal 'V' matrix.
         * </p>
         *
         * @return An orthogonal n by n matrix.
         */
        public SimpleMatrix getV() {
            return V;
        }

        /**
         * <p>
         * Computes the quality of the computed decomposition.  A value close to or less than 1e-15
         * is considered to be within machine precision.
         * </p>
         *
         * <p>
         * This function must be called before the original matrix has been modified or else it will
         * produce meaningless results.
         * </p>
         *
         * @return Quality of the decomposition.
         */
        public double quality() {
            return DecompositionOps.quality(mat,U.getMatrix(),W.getMatrix(),V.transpose().getMatrix());
        }

        /**
         * Computes the null space from an SVD.  For more information see {@link SingularOps#nullSpace}.
         * @return Null space vector.
         */
        public SimpleMatrix nullSpace() {
            return SimpleMatrix.wrap(SingularOps.nullSpace(svd,null));
        }

        public int rank() {
            return SingularOps.rank(svd,10.0*UtilEjml.EPS);
        }

        public int nullity() {
            return SingularOps.nullity(svd,10.0*UtilEjml.EPS);
        }

        public SingularValueDecomposition getSVD() {
            return svd;
        }
    }

    /**
     * Wrapper around EigenDecomposition for SimpleMatrix
     */
    public class EVD
    {
        EigenDecomposition eig;

        public EVD()
        {
            eig = DecompositionOps.eig();
            if( !eig.decompose(mat))
                throw new RuntimeException("Eigenvalue Decomposition failed");
        }

        /**
         * Returns the number of eigenvalues/eigenvectors.  This is the matrix's dimension.
         *
         * @return number of eigenvalues/eigenvectors.
         */
        public int getNumberOfEigenvalues() {
            return eig.getNumberOfEigenvalues();
        }

        /**
         * <p>
         * Returns the value of an individual eigenvalue.  The eigenvalue maybe a complex number.  It is
         * a real number of the imaginary component is equal to exactly one.
         * </p>
         *
         * @param index Index of the eigenvalue eigenvector pair.
         * @return An eigenvalue.
         */
        public Complex64F getEigenvalue( int index ) {
            return eig.getEigenvalue(index);
        }

        /**
         * <p>
         * Used to retrieve real valued eigenvectors.  If an eigenvector is associated with a complex eigenvalue
         * then null is returned instead.
         * </p>
         *
         * @param index Index of the eigenvalue eigenvector pair.
         * @return If the associated eigenvalue is real then an eigenvector is returned, null otherwise.
         */
        public SimpleMatrix getEigenVector( int index ) {
            return SimpleMatrix.wrap(eig.getEigenVector(index));
        }

        /**
         * <p>
         * Computes the quality of the computed decomposition.  A value close to or less than 1e-15
         * is considered to be within machine precision.
         * </p>
         *
         * <p>
         * This function must be called before the original matrix has been modified or else it will
         * produce meaningless results.
         * </p>
         *
         * @return Quality of the decomposition.
         */
        public double quality() {
            return DecompositionOps.quality(mat,eig);
        }

        public EigenDecomposition getEVD() {
            return eig;
        }
    }
}
