/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.DenseMatrix64F;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * Reads in a matrix that is in a column-space-value (CSV) format.
 *
 * @author Peter Abeles
 */
public class ReadMatrixCsv extends ReadCsv {

    /**
     * Specifies where input comes from.
     *
     * @param in Where the input comes from.
     */
    public ReadMatrixCsv(InputStream in) {
        super(in);
    }

    /**
     * Reads in a DenseMatrix64F from the IO stream.
     * @return DenseMatrix64F
     * @throws IOException If anything goes wrong.
     */
    public DenseMatrix64F read() throws IOException {
        List<String> words = extractWords();
        if( words.size() != 2 )
            throw new IOException("Unexpected number of words on first line.");

        int numRows = Integer.parseInt(words.get(0));
        int numCols = Integer.parseInt(words.get(1));

        if( numRows < 0 || numCols < 0)
            throw new IOException("Invalid number of rows and/or columns: "+numRows+" "+numCols);

        return read(numRows,numCols);
    }

    public DenseMatrix64F read( int numRows , int numCols ) throws IOException {

        DenseMatrix64F A = new DenseMatrix64F(numRows,numCols);

        for( int i = 0; i < numRows; i++ ) {
            List<String> words = extractWords();

            if( words.size() != numCols )
                throw new IOException("Unexpected number of words in column. Found "+words.size()+" expected "+numCols);
            for( int j = 0; j < numCols; j++ ) {
                A.set(i,j,Double.parseDouble(words.get(j)));
            }
        }

        return A;
    }
}
