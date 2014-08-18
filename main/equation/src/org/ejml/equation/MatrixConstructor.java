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

package org.ejml.equation;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import java.util.ArrayList;
import java.util.List;

/**
 * matrix used to construct a matrix from a sequence of concatenations.
 *
 * @author Peter Abeles
 */
public class MatrixConstructor {

    VariableMatrix output;
    List<Item> items = new ArrayList<Item>();

    List<VariableScalar> tmp = new ArrayList<VariableScalar>();

    public MatrixConstructor(ManagerTempVariables manager) {
        this.output = manager.createMatrix();
    }

    public void addToRow(Variable variable) {
       items.add( new Item(variable));
    }

    public void endRow() {
        items.add(new Item());
    }

    public void construct() {
        // make sure the last item is and end row
        if( !items.get(items.size()-1).endRow )
            endRow();

        setToRequiredSize(output.matrix);

        int matrixRow = 0;
        List<Item> row = new ArrayList<Item>();
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);

            if( item.endRow ) {
                Item v = row.get(0);
                int numRows = v.getRows();
                int numCols = v.getColumns();
                if( v.matrix ) {
                    CommonOps.insert(v.getMatrix(),output.matrix,matrixRow,0);
                } else {
                    output.matrix.set(matrixRow,0,v.getValue());
                }
                for (int j = 1; j < row.size(); j++) {
                    v = row.get(j);
                    if( v.getRows() != numRows)
                        throw new RuntimeException("Row miss-matched. "+numRows+" "+v.getRows());
                    if( v.matrix ) {
                        CommonOps.insert(v.getMatrix(),output.matrix,matrixRow,numCols);
                    } else {
                        output.matrix.set(matrixRow, numCols, v.getValue());
                    }
                    numCols += v.getColumns();
                }
                matrixRow += numRows;
                row.clear();
            } else {
                row.add(item);
            }
        }

    }

    public VariableMatrix getOutput() {
        return output;
    }

    protected void setToRequiredSize( DenseMatrix64F matrix ) {


        int matrixRow = 0;
        int matrixCol = 0;
        List<Item> row = new ArrayList<Item>();
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);

            if( item.endRow ) {
                Item v = row.get(0);
                int numRows = v.getRows();
                int numCols = v.getColumns();
                for (int j = 1; j < row.size(); j++) {
                    v = row.get(j);
                    if( v.getRows() != numRows)
                        throw new RuntimeException("Row miss-matched. "+numRows+" "+v.getRows());
                    numCols += v.getColumns();
                }
                matrixRow += numRows;

                if( matrixCol == 0 )
                    matrixCol = numCols;
                else if( matrixCol != numCols )
                    throw new RuntimeException("Unexpected number of columns");

                row.clear();
            } else {
                row.add(item);
            }
        }

        matrix.reshape(matrixRow,matrixCol);
    }


    private static class Item
    {
        Variable variable;
        boolean endRow;
        boolean matrix;

        private Item(Variable variable) {
            this.variable = variable;
            matrix = variable instanceof VariableMatrix;
        }

        private Item() {
            endRow = true;
        }

        public int getRows() {
            if( matrix ) {
                return ((VariableMatrix)variable).matrix.numRows;
            } else {
                return 1;
            }
        }

        public int getColumns() {
            if( matrix ) {
                return ((VariableMatrix)variable).matrix.numCols;
            } else {
                return 1;
            }
        }

        public DenseMatrix64F getMatrix() {
            return ((VariableMatrix)variable).matrix;
        }

        public double getValue() {
            return ((VariableScalar)variable).getDouble();
        }
    }
}
