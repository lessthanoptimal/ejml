package org.ejml.alg.dense.decomposition.svd;

import org.ejml.alg.dense.decomposition.svd.implicitqr.SvdImplicitQrAlgorithm;
import org.ejml.data.DenseMatrix64F;


/**
 * @author Peter Abeles
 */
public class SvdImplicitQrAlgorithmSmart extends SvdImplicitQrAlgorithm {

    SmartRotatorUpdate smartU = new SmartRotatorUpdate();
    SmartRotatorUpdate smartV = new SmartRotatorUpdate();

    @Override
    public void setUt(DenseMatrix64F ut) {
        super.setUt(ut);
        if(Ut != null )
            smartU.init(Ut);
    }

    @Override
    public void setVt(DenseMatrix64F vt) {
        super.setVt(vt);
        if(Vt != null )
            smartV.init(Vt);
    }


    @Override
    protected void updateRotator( DenseMatrix64F Q , int m, int n, double c, double s) {
        if( Q == smartU.getR() ) {
            smartU.update(m,n,c,s);
        } else if( Q == smartV.getR() ) {
            smartV.update(m,n,c,s);
        } else {
            throw new RuntimeException("Unknown");
        }
    }
}
