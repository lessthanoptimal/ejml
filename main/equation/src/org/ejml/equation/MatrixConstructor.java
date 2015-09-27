/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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


    public MatrixConstructor(ManagerTempVariables manager) {
        this.output = manager.createMatrix();
    }

    public void addToRow(Variable variable) {
        if( variable.getType() == VariableType.INTEGER_SEQUENCE ) {
            if( ((VariableIntegerSequence)variable).sequence.requiresMaxIndex() )
                throw new ParseError("Trying to create a matrix with an unbounded integer range." +
                        " Forgot a value after a colon?");
        }
        items.add( new Item(variable));
    }

    public void endRow() {
        items.add(new Item());
    }

    public void construct() {
        // make sure the last item is and end row
        if( !items.get(items.size()-1).endRow )
            endRow();

        // have to initialize some variable types first to get the actual size
        for (int i = 0; i < items.size(); i++) {
            items.get(i).initialize();
        }

        setToRequiredSize(output.matrix);

        int matrixRow = 0;
        List<Item> row = new ArrayList<Item>();
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);

            if( item.endRow ) {
                int expectedRows = 0;
                int numCols = 0;
                for (int j = 0; j < row.size(); j++) {
                    Item v = row.get(j);

                    int numRows = v.getRows();

                    if( j == 0 ) {
                        expectedRows = numRows;
                    } else if( v.getRows() != expectedRows ){
                        throw new RuntimeException("Row miss-matched. "+numRows+" "+v.getRows());
                    }

                    if( v.matrix ) {
                        CommonOps.insert(v.getMatrix(),output.matrix,matrixRow,numCols);
                    } else if( v.variable.getType() == VariableType.SCALAR ){
                        output.matrix.set(matrixRow,numCols,v.getValue());
                    } else if( v.variable.getType() == VariableType.INTEGER_SEQUENCE ) {
                        IntegerSequence sequence = ((VariableIntegerSequence)v.variable).sequence;
                        int col = numCols;
                        while( sequence.hasNext() ) {
                            output.matrix.set(matrixRow,col++,sequence.next());
                        }
                    } else {
                        throw new ParseError("Can't insert a variable of type "+v.variable.getType()+" inside a matrix!");
                    }
                    numCols += v.getColumns();
                }

                matrixRow += expectedRows;
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
                    throw new ParseError("Row "+matrixRow+" has an unexpected number of columns; expected = "+matrixCol+" found = "+numCols);

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
            } else if( variable.getType() == VariableType.SCALAR ){
                return 1;
            } else if( variable.getType() == VariableType.INTEGER_SEQUENCE ) {
                return ((VariableIntegerSequence)variable).sequence.length();
            } else {
                throw new RuntimeException("BUG! Should have been caught earlier");
            }
        }

        public DenseMatrix64F getMatrix() {
            return ((VariableMatrix)variable).matrix;
        }

        public double getValue() {
            return ((VariableScalar)variable).getDouble();
        }

        public void initialize() {
            if( variable!=null && !matrix && variable.getType() == VariableType.INTEGER_SEQUENCE ) {
                ((VariableIntegerSequence)variable).sequence.initialize(-1);
            }
        }
    }
}
