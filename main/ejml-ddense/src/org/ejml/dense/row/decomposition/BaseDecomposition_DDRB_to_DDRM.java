/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.decomposition;

import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.interfaces.decomposition.DecompositionInterface;


/**
 * Generic interface for wrapping a {@link DMatrixRBlock} decomposition for
 * processing of {@link DMatrixRMaj}.
 *
 * @author Peter Abeles
 */
public class BaseDecomposition_DDRB_to_DDRM implements DecompositionInterface<DMatrixRMaj> {

    protected DecompositionInterface<DMatrixRBlock> alg;

    protected double[]tmp;
    protected DMatrixRBlock Ablock = new DMatrixRBlock();
    protected int blockLength;

    public BaseDecomposition_DDRB_to_DDRM(DecompositionInterface<DMatrixRBlock> alg,
                                        int blockLength) {
        this.alg = alg;
        this.blockLength = blockLength;
    }

    @Override
    public boolean decompose(DMatrixRMaj A) {
        Ablock.numRows = A.numRows;
        Ablock.numCols = A.numCols;
        Ablock.blockLength = blockLength;
        Ablock.data = A.data;

        int tmpLength = Math.min( Ablock.blockLength , A.numRows ) * A.numCols;

        if( tmp == null || tmp.length < tmpLength )
            tmp = new double[ tmpLength ];

        // doing an in-place convert is much more memory efficient at the cost of a little
        // but of CPU
        MatrixOps_DDRB.convertRowToBlock(A.numRows,A.numCols,Ablock.blockLength,A.data,tmp);

        boolean ret = alg.decompose(Ablock);

        // convert it back to the normal format if it wouldn't have been modified
        if( !alg.inputModified() ) {
            MatrixOps_DDRB.convertBlockToRow(A.numRows,A.numCols,Ablock.blockLength,A.data,tmp);
        }

        return ret;
    }

    public void convertBlockToRow(int numRows , int numCols , int blockLength ,
                                  double[] data) {
        int tmpLength = Math.min( blockLength , numRows ) * numCols;

        if( tmp == null || tmp.length < tmpLength )
            tmp = new double[ tmpLength ];

        MatrixOps_DDRB.convertBlockToRow(numRows,numCols,Ablock.blockLength,data,tmp);
    }

    @Override
    public boolean inputModified() {
        return alg.inputModified();
    }
}
