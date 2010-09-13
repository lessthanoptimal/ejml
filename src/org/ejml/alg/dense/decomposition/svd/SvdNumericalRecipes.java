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

package org.ejml.alg.dense.decomposition.svd;

import org.ejml.UtilEjml;
import org.ejml.data.DenseMatrix64F;

import static java.lang.Math.*;


/**
 * <p>
 * This is a specific implementation of a SVD decomposition and is
 * adapted from a webnote at http://www.nr.com/webnotes?2 from
 * "Numerical Recipes The Art of Scientific Computing", Third Edition.
 * </p>
 *
 * @author Peter Abeles
 */
public class SvdNumericalRecipes extends SingularValueDecompositionBase {
    // the maximum number of iterations it will perform when looking for the SVD
    private int maxIter;

    /**
     * Constructor that declares all the data structures used when decomposing.
     *
     */
    public SvdNumericalRecipes() {
        this(30);
    }

    /**
     * Constructor that declares all the data structures used when decomposing.
     * @param maxIter The maximum number of iterations it will perform while trying to find the SVD
     */
    public SvdNumericalRecipes(int maxIter ) {
        this.maxIter = maxIter;
    }

    @Override
    public boolean isCompact() {
        return true;
    }

    @Override
    public boolean decompose(DenseMatrix64F mat) {
        if( mat.numRows > maxRows || mat.numCols > maxCols ) {
            setExpectedMaxSize(mat.numRows,mat.numCols);
        }

        m = mat.numRows;
        n = mat.numCols;

        U.setReshape(mat);
        V.reshape(n,n, false);
        decompose();
        reorder();

        return true;
    }

    @Override
    public boolean modifyInput() {
        return false;
    }

    private boolean decompose() {
        int l = 0;
        double anorm, f, g, h, s, scale;
        double rv1[] = tmp;

        // Householder reduction to bidiagonal form.
        g = scale = anorm = 0.0;

        for (int i=0;i<n;i++) {
            l=i+2;
            rv1[i]=scale*g;
            g=s=scale=0.0;
            if (i < m) {
                for (int k=i;k<m;k++) scale += abs(u[k*n + i]);
                if (scale != 0.0) {
                    for (int k=i;k<m;k++) {
                        u[k*n + i] /= scale;
                        s += u[k*n + i]*u[k*n + i];
                    }
                    f=u[i*n + i];
                    g = -sign(sqrt(s),f);
                    h=f*g-s;
                    u[i*n + i]=f-g;
                    for (int j=l-1;j<n;j++) {
                        s=0.0;
                        for (int k=i;k<m;k++) s += u[k*n + i]*u[k*n + j];
                        f=s/h;
                        for (int k=i;k<m;k++) u[k*n + j] += f*u[k*n + i];
                    }
                    for (int k=i;k<m;k++) u[k*n + i] *= scale;
                }
            }
            w[i]=scale *g;
            g=s=scale=0.0;
            if (i+1 <= m && i+1 != n) {
                for (int k=l-1;k<n;k++) scale += abs(u[i*n + k]);
                if (scale != 0.0) {
                    for (int k=l-1;k<n;k++) {
                        u[i*n + k] /= scale;
                        s += u[i*n + k]*u[i*n + k];
                    }
                    f=u[i*n + l-1];
                    g = -sign(sqrt(s),f);
                    h=f*g-s;
                    u[i*n + l-1]=f-g;
                    for (int k=l-1;k<n;k++) rv1[k]=u[i*n + k]/h;
                    for (int j=l-1;j<m;j++) {
                        s=0.0;
                        for (int k=l-1;k<n;k++) s += u[j*n + k]*u[i*n + k];
                        for (int k=l-1;k<n;k++) u[j*n + k] += s*rv1[k];
                    }
                    for (int k=l-1;k<n;k++) u[i*n + k] *= scale;
                }
            }
            anorm=max(anorm,(abs(w[i])+abs(rv1[i])));
        }
        // Accumulation of right-hand transformations.
        for (int i=n-1;i>=0;i--) {
            if (i < n-1) {
                if (g != 0.0) {
                    // Double division to avoid possible underflow.
                    for (int j=l;j<n;j++)
                        v[j*n + i]=(u[i*n + j]/u[i*n + l])/g;
                    for (int j=l;j<n;j++) {
                        s=0.0;
                        for (int k=l;k<n;k++) s += u[i*n + k]*v[k*n + j];
                        for (int k=l;k<n;k++) v[k*n + j] += s*v[k*n + i];
                    }
                }
                for (int j=l;j<n;j++) v[i*n + j]=v[j*n + i]=0.0;
            }
            v[i*n + i]=1.0;
            g=rv1[i];
            l=i;
        }
        // Accumulation of left-hand transformations.
        for (int i=min(m,n)-1;i>=0;i--) {
            l=i+1;
            g=w[i];
            for (int j=l;j<n;j++) u[i*n + j]=0.0;
            if (g != 0.0) {
                g=1.0/g;
                for (int j=l;j<n;j++) {
                    s=0.0;
                    for (int k=l;k<m;k++) s += u[k*n + i]*u[k*n + j];
                    f=(s/u[i*n + i])*g;
                    for (int k=i;k<m;k++) u[k*n + j] += f*u[k*n + i];
                }
                for (int j=i;j<m;j++) u[j*n + i] *= g;
            } else for (int j=i;j<m;j++) u[j*n + i]=0.0;
            ++u[i*n + i];
        }

        return doDiagonalization(anorm, rv1);
    }

    /**
     * Diagonalization of the bidiagonal form: Loop over singular values,
     * and over allowed iterations.
     */
    private boolean doDiagonalization(double anorm, double[] rv1) {
        int iterations;
        int l;
        double c,s,f,g,h,y,z,x;

        for (int k=n-1;k>=0;k--) {
            for (iterations=0;iterations<maxIter;iterations++) {
                int nm=0;
                boolean flag=true;
                // Test for splitting.
                for (l=k;l>=0;l--) {
                    nm=l-1;
                    if (l == 0 || abs(rv1[l]) <= UtilEjml.EPS*anorm) {
                        flag=false;
                        break;
                    }
                    if (abs(w[nm]) <= UtilEjml.EPS*anorm) break;
                }
                if (flag) {
                    // Cancellation of rv1[l], if l > 0.
                    c=0.0;
                    s=1.0;
                    for (int i=l;i<k+1;i++) {
                        f=s*rv1[i];
                        rv1[i]=c*rv1[i];
                        if (abs(f) <= UtilEjml.EPS*anorm) break;
                        g=w[i];
                        h=pythag(f,g);
                        w[i]=h;
                        h=1.0/h;
                        c=g*h;
                        s = -f*h;
                        for (int j=0;j<m;j++) {
                            y=u[j*n + nm];
                            z=u[j*n + i];
                            u[j*n + nm]=y*c+z*s;
                            u[j*n + i]=z*c-y*s;
                        }
                    }
                }
                z=w[k];
                // Convergence.
                if (l == k) {
                    // Singular value is made nonnegative.
                    if (z < 0.0) {
                        w[k] = -z;
                        for (int j=0;j<n;j++) v[j*n + k] = -v[j*n + k];
                    }
                    break;
                }

                //  Shift from bottom 2-by-2 minor.
                x=w[l];
                nm=k-1;
                y=w[nm];
                g=rv1[nm];
                h=rv1[k];
                f=((y-z)*(y+z)+(g-h)*(g+h))/(2.0*h*y);
                g=pythag(f,1.0);
                f=((x-z)*(x+z)+h*((y/(f+ sign(g,f)))-h))/x;
                //  Next QR transformation:
                c=s=1.0;
                for (int j=l;j<=nm;j++) {
                    int i=j+1;
                    g=rv1[i];
                    y=w[i];
                    h=s*g;
                    g=c*g;
                    z=pythag(f,h);
                    rv1[j]=z;
                    c=f/z;
                    s=h/z;
                    f=x*c+g*s;
                    g=g*c-x*s;
                    h=y*s;
                    y *= c;
                    for (int jj=0;jj<n;jj++) {
                        x=v[jj*n + j];
                        z=v[jj*n + i];
                        v[jj*n + j]=x*c+z*s;
                        v[jj*n + i]=z*c-x*s;
                    }
                    z=pythag(f,h);
                    // Rotation can be arbitrary if z D 0.
                    w[j]=z;
                    if (z != 0.0) {
                        z=1.0/z;
                        c=f*z;
                        s=h*z;
                    }
                    f=c*g+s*y;
                    x=c*y-s*g;
                    for (int jj=0;jj<m;jj++) {
                        y=u[jj*n + j];
                        z=u[jj*n + i];
                        u[jj*n + j]=y*c+z*s;
                        u[jj*n + i]=z*c-y*s;
                    }
                }
                rv1[l]=0.0;
                rv1[k]=f;
                w[k]=x;
            }

            // see if it has more iterations in which it can converge
            if (iterations >= maxIter)
                return false;
        }
        return true;
    }

    /**
     * Given the output of decompose, this routine sorts the singular values, and corresponding columns
     * of u and v, by decreasing magnitude. Also, signs of corresponding columns are flipped so as to
     * maximize the number of positive elements.
     */
    private void reorder() {
        int i, j, k, s, inc = 1;
        double sw;
        double[] su = new double[m];
        double[] sv = new double[n];


        do {
            inc *= 3;
            inc++;
        } while (inc <= n);
        // Sort. The method is Shell’s sort.
        // (The work is negligible as comared to that already done in decompose.)
        do {

            inc /= 3;
            for (i = inc; i < n; i++) {
                sw = w[i];
                for (k = 0; k < m; k++) su[k] = u[k*n + i];
                for (k = 0; k < n; k++) sv[k] = v[k*n + i];
                j = i;
                while (w[j - inc] < sw) {
                    w[j] = w[j - inc];
                    for (k = 0; k < m; k++) u[k*n + j] = u[k*n + (j - inc)];
                    for (k = 0; k < n; k++) v[k*n + j] = v[k*n + (j - inc)];
                    j -= inc;
                    if (j < inc) break;
                }
                w[j] = sw;
                for (k = 0; k < m; k++) u[k*n + j] = su[k];
                for (k = 0; k < n; k++) v[k*n + j] = sv[k];
            }
        } while (inc > 1);
        // Flip signs.
        for (k = 0; k < n; k++) {
            s = 0;
            for (i = 0; i < m; i++) if (u[i*n + k] < 0.) s++;
            for (j = 0; j < n; j++) if (v[j*n + k] < 0.) s++;
            if (s > (m + n) / 2) {
                for (i = 0; i < m; i++) u[i*n + k] = -u[i*n + k];
                for (j = 0; j < n; j++) v[j*n + k] = -v[j*n + k];
            }
        }
    }

    private static double sign(double a, double b) {
        return ((b) >= 0. ? abs(a) : -abs(a));
    }

    /**
     * Computes (a<sup>2</sup> + b<sup>2</sup>) <sup>1/2</sup> without destructive underflow or overflow.
     */
    private static double pythag(final double a, final double b) {
        double absa = abs(a);
        double absb = abs(b);

        if( absa > absb ) {
            double c = absb/absa;
            return absa*sqrt(1.0+c*c);
        } else if( absb == 0.0 ) {
            return 0.0;
        } else {
            double c = absa/absb;
            return absb*sqrt(1.0+c*c);
        }
    }

    @Override
    public int numberOfSingularValues() {
        return m<n?m:n;
    }

    @Override
    public int numRows() {
        return m;
    }

    @Override
    public int numCols() {
        return n;
    }
}
