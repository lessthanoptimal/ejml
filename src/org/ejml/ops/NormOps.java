/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

import org.ejml.UtilEjml;
import org.ejml.data.D1Matrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.RowD1Matrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;


/**
 * <p>
 * Norms are a measure of the size of a vector or a matrix.  One typical application is in error analysis.
 * </p>
 * <p>
 * Vector norms have the following properties:
 * <ol>
 * <li>||x|| > 0 if x &ne; 0 and ||0|| = 0</li>
 * <li>||&alpha;x|| = |&alpha;| ||x||</li>
 * <li>||x+y|| &le; ||x|| + ||y||</li>
 * </ol>
 * </p>
 *
 * <p>
 * Matrix norms have the following properties:
 * <ol>
 * <li>||A|| > 0 if A &ne; 0 where A &isin; &real; <sup>m &times; n</sup></li>
 * <li> || &alpha; A || = |&alpha;| ||A|| where A &isin; &real; <sup>m &times; n</sup></li>
 * <li>||A+B|| &le; ||A|| + ||B|| where A and B are &isin; &real; <sup>m &times; n</sup></li>
 * <li>||AB|| &le; ||A|| ||B|| where A and B are &isin; &real; <sup>m &times; m</sup></li>
 * </ol>
 * Note that the last item in the list only applies to square matrices.
 * </p>
 *
 * <p>
 * Matrix norms can be induced from vector norms as is shown below:<br>
 * <br>
 * ||A||<sub>M</sub> = max<sub>x&ne;0</sub>||Ax||<sub>v</sub>/||x||<sub>v</sub><br>
 * <br>
 * where ||.||<sub>M</sub> is the induced matrix norm for the vector norm ||.||<sub>v</sub>.
 * </p>
 *
 * <p>
 * By default implementations that try to mitigate overflow/underflow are used.  If the word fast is
 * found before a function's name that means it does not mitigate those issues, but runs a bit faster.
 * </p>
 *
 * @author Peter Abeles
 */
public class NormOps {

    /**
     * Normalizes the matrix such that the Frobenius norm is equal to one.
     *
     * @param A The matrix that is to be normalized.
     */
    public static void normalizeF( DenseMatrix64F A ) {
        double val = normF(A);

        if( val == 0 )
            return;

        int size = A.getNumElements();

        for( int i = 0; i < size; i++) {
            A.div(i , val);
        }
    }

    /**
     * <p>
     * The condition number of a matrix is used to measure the sensitivity of the linear
     * system <b>Ax=b</b>.  A value near one indicates that it is a well conditioned matrix.<br>
     * <br>
     * &kappa;<sub>p</sub> = ||A||<sub>p</sub>||A<sup>-1</sup>||<sub>p</sub>
     * </p>
     * <p>
     * If the matrix is not square then the condition of either A<sup>T</sup>A or AA<sup>T</sup> is computed. 
     * <p>
     * @param A The matrix.
     * @param p p-norm
     * @return The condition number.
     */
    public static double conditionP( DenseMatrix64F A , double p )
    {
        if( p == 2 ) {
            return conditionP2(A);
        } else if( A.numRows == A.numCols ){
            // square matrices are the typical case

            DenseMatrix64F A_inv = new DenseMatrix64F(A.numRows,A.numCols);

            if( !CommonOps.invert(A,A_inv) )
                throw new IllegalArgumentException("A can't be inverted.");

            return normP(A,p) * normP(A_inv,p);
        } else  {
            DenseMatrix64F pinv = new DenseMatrix64F(A.numCols,A.numRows);
            CommonOps.pinv(A,pinv);

            return normP(A,p) * normP(pinv,p);
        }
    }

    /**
     * <p>
     * The condition p = 2 number of a matrix is used to measure the sensitivity of the linear
     * system <b>Ax=b</b>.  A value near one indicates that it is a well conditioned matrix.<br>
     * <br>
     * &kappa;<sub>2</sub> = ||A||<sub>2</sub>||A<sup>-1</sup>||<sub>2</sub>
     * </p>
     * <p>
     * This is also known as the spectral condition number.
     * </p>
     *
     * @param A The matrix.
     * @return The condition number.
     */
    public static double conditionP2( DenseMatrix64F A )
    {
        SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(A.numRows,A.numCols,false,false,true);

        svd.decompose(A);

        double[] singularValues = svd.getSingularValues();

        int n = SingularOps.rank(svd,1e-12);

        if( n == 0 ) return 0;

        double smallest = Double.MAX_VALUE;
        double largest = Double.MIN_VALUE;

        for( double s : singularValues ) {
            if( s < smallest )
                smallest = s;
            if( s > largest )
                largest = s;
        }

        return largest/smallest;
    }

    /**
     * <p>
     * This implementation of the Frobenius norm is a straight forward implementation and can
     * be susceptible for overflow/underflow issues.  A more resilient implementation is
     * {@link #normF}.
     * </p>
     *
     * @param a The matrix whose norm is computed.  Not modified.
     */
    public static double fastNormF( D1Matrix64F a ) {
        double total = 0;

        int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            double val = a.get(i);
            total += val*val;
        }

        return Math.sqrt(total);
    }

    /**
     * <p>
     * Computes the Frobenius matrix norm:<br>
     * <br>
     * normF = Sqrt{  &sum;<sub>i=1:m</sub> &sum;<sub>j=1:n</sub> { a<sub>ij</sub><sup>2</sup>}   }
     * </p>
     * <p>
     * This is equivalent to the element wise p=2 norm.  See {@link #fastNormF} for another implementation
     * that is faster, but more prone to underflow/overflow errors.
     * </p>
     *
     * @param a The matrix whose norm is computed.  Not modified.
     * @return The norm's value.
     */
    public static double normF( D1Matrix64F a ) {
        double total = 0;

        double scale = CommonOps.elementMaxAbs(a);

        if( scale == 0.0 )
            return 0.0;

        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            double val = a.get(i)/scale;
            total += val*val;
        }

        return scale*Math.sqrt(total);
    }

    /**
     * <p>
     * Element wise p-norm:<br>
     * <br>
     * norm = {&sum;<sub>i=1:m</sub> &sum;<sub>j=1:n</sub> { |a<sub>ij</sub>|<sup>p</sup>}}<sup>1/p</sup>
     * </p>
     *
     * <p>
     * This is not the same as the induced p-norm used on matrices, but is the same as the vector p-norm.
     * </p>
     *
     * @param A Matrix. Not modified.
     * @param p p value.
     * @return The norm's value.
     */
    public static double elementP( RowD1Matrix64F A , double p ) {
        if( p == 1 ) {
            return CommonOps.elementSumAbs(A);
        } if( p == 2 ) {
            return normF(A);
        } else {
            double max = CommonOps.elementMaxAbs(A);

            if( max == 0.0 )
                return 0.0;

            double total = 0;

            int size = A.getNumElements();

            for( int i = 0; i < size; i++ ) {
                double a = A.get(i)/max;

                total += Math.pow(Math.abs(a),p);
            }

            return max*Math.pow(total,1.0/p);
        }
    }

    /**
     * Same as {@link #elementP} but runs faster by not mitigating overflow/underflow related problems.
     *
     * @param A Matrix. Not modified.
     * @param p p value.
     * @return The norm's value.
     */
    public static double fastElementP( D1Matrix64F A , double p ) {
        if( p == 2 ) {
            return fastNormF(A);
        } else {
            double total = 0;

            int size = A.getNumElements();

            for( int i = 0; i < size; i++ ) {
                double a = A.get(i);

                total += Math.pow(Math.abs(a),p);
            }

            return Math.pow(total,1.0/p);
        }
    }

    /**
     * Computes either the vector p-norm or the induced matrix p-norm depending on A
     * being a vector or a matrix respectively.
     *
     * @param A Vector or matrix whose norm is to be computed.
     * @param p The p value of the p-norm.
     * @return The computed norm.
     */
    public static double normP( DenseMatrix64F A , double p ) {
        if( p == 1 ) {
            return normP1(A);
        } else if( p == 2 ) {
            return normP2(A);
        } else if( Double.isInfinite(p)) {
            return normPInf(A);
        }
        if( MatrixFeatures.isVector(A) ) {
            return elementP(A,p);
        } else {
            throw new IllegalArgumentException("Doesn't support induced norms yet.");
        }
    }

    /**
     * An unsafe but faster version of {@link #normP} that calls routines which are faster
     * but more prone to overflow/underflow problems.
     *
     * @param A Vector or matrix whose norm is to be computed.
     * @param p The p value of the p-norm.
     * @return The computed norm.
     */
    public static double fastNormP( DenseMatrix64F A , double p ) {
        if( p == 1 ) {
            return normP1(A);
        } else if( p == 2 ) {
            return fastNormP2(A);
        } else if( Double.isInfinite(p)) {
            return normPInf(A);
        }
        if( MatrixFeatures.isVector(A) ) {
            return fastElementP(A,p);
        } else {
            throw new IllegalArgumentException("Doesn't support induced norms yet.");
        }
    }

    /**
     * Computes the p=1 norm.  If A is a matrix then the induced norm is computed.
     *
     * @param A Matrix or vector.
     * @return The norm.
     */
    public static double normP1( DenseMatrix64F A ) {
        if( MatrixFeatures.isVector(A)) {
            return CommonOps.elementSumAbs(A);
        } else {
            return inducedP1(A);
        }
    }

    /**
     * Computes the p=2 norm.  If A is a matrix then the induced norm is computed.
     *
     * @param A Matrix or vector.
     * @return The norm.
     */
    public static double normP2( DenseMatrix64F A ) {
        if( MatrixFeatures.isVector(A)) {
            return normF(A);
        } else {
            return inducedP2(A);
        }
    }

    /**
     * Computes the p=2 norm.  If A is a matrix then the induced norm is computed. This
     * implementation is faster, but more prone to buffer overflow or underflow problems.
     *
     * @param A Matrix or vector.
     * @return The norm.
     */
    public static double fastNormP2( DenseMatrix64F A ) {
        if( MatrixFeatures.isVector(A)) {
            return fastNormF(A);
        } else {
            return inducedP2(A);
        }
    }

    /**
     * Computes the p=&#8734; norm.  If A is a matrix then the induced norm is computed.
     *
     * @param A Matrix or vector.
     * @return The norm.
     */
    public static double normPInf( DenseMatrix64F A ) {
        if( MatrixFeatures.isVector(A)) {
            return CommonOps.elementMaxAbs(A);
        } else {
            return inducedPInf(A);
        }
    }

    /**
     * <p>
     * Computes the induced p = 1 matrix norm.<br>
     * <br>
     * ||A||<sub>1</sub>= max(j=1 to n; sum(i=1 to m; |a<sub>ij</sub>|))
     * </p>
     *
     * @param A Matrix. Not modified.
     * @return The norm.
     */
    public static double inducedP1( DenseMatrix64F A ) {
        double max = 0;

        int m = A.numRows;
        int n = A.numCols;

        for( int j = 0; j < n; j++ ) {
            double total = 0;
            for( int i = 0; i < m; i++ ) {
                total += Math.abs(A.get(i,j));
            }
            if( total > max ) {
                max = total;
            }
        }

        return max;
    }

    /**
     * <p>
     * Computes the induced p = 2 matrix norm, which is the largest singular value.
     * </p>
     *
     * @param A Matrix. Not modified.
     * @return The norm.
     */
    public static double inducedP2( DenseMatrix64F A ) {
        SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(A.numRows,A.numCols,false,false,true);

        if( !svd.decompose(A) )
            throw new RuntimeException("Decomposition failed");

        double[] singularValues = svd.getSingularValues();

        // the largest singular value is the induced p2 norm
        return UtilEjml.max(singularValues,0,singularValues.length);
    }

    /**
     * <p>
     * Induced matrix p = infinity norm.<br>
     * <br>
     * ||A||<sub>&#8734;</sub> = max(i=1 to m; sum(j=1 to n; |a<sub>ij</sub>|))
     * </p>
     *
     * @param A A matrix.
     * @return the norm.
     */
    public static double inducedPInf( DenseMatrix64F A ) {
        double max = 0;

        int m = A.numRows;
        int n = A.numCols;

        for( int i = 0; i < m; i++ ) {
            double total = 0;
            for( int j = 0; j < n; j++ ) {
                total += Math.abs(A.get(i,j));
            }
            if( total > max ) {
                max = total;
            }
        }

        return max;
    }

}
