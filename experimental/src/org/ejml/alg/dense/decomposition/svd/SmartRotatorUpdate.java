package org.ejml.alg.dense.decomposition.svd;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;


/**
 * @author Peter Abeles
 */
public class SmartRotatorUpdate {

    DenseMatrix64F R;
    int mod[] = new int[ 1 ];

    public SmartRotatorUpdate() {
         
    }

    public DenseMatrix64F getR() {
        return R;
    }

    public void init( DenseMatrix64F R ) {
        this.R = R;
        CommonOps.setIdentity(R);

        int a = Math.min(R.numRows,R.numCols);

        if( mod.length < a ) {
            mod = new int[ a ];
        }

        for( int i = 0; i < a; i++ ) {
            mod[i] = i;
        }
    }

    public void update( int rowA , int rowB , double c , double s )
    {
        int l = Math.max( mod[rowA] , mod[rowB] );
        mod[rowA] = l;
        mod[rowB] = l;

        int indexA = rowA*R.numCols;
        int indexB = rowB*R.numCols;

        for( int i = 0; i < l; i++ , indexA++,indexB++) {
            double a = R.data[indexA];
            double b = R.data[indexB];
            R.data[indexA] = c*a + s*b;
            R.data[indexB] = -s*a + c*b;
        }
    }
}
