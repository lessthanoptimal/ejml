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
import org.ejml.data.Matrix64F;
import org.ejml.data.ReshapeMatrix64F;

import java.io.*;


/**
 * Provides simple to use routines for reading and writing matrices to and from files.
 *
 * @author Peter Abeles
 */
public class MatrixIO {

    /**
     * Saves a matrix to disk using Java binary serialization.
     *
     * @param A The matrix being saved.
     * @param fileName Name of the file its being saved at.
     * @throws java.io.IOException
     */
    public static void saveBin(ReshapeMatrix64F A, String fileName)
        throws IOException
    {
        FileOutputStream fileStream = new FileOutputStream(fileName);
        ObjectOutputStream stream = new ObjectOutputStream(fileStream);

        try {
            stream.writeObject(A);
            stream.flush();
        } finally {
            // clean up
            try {
                stream.close();
            } finally {
                fileStream.close();
            }
        }

    }

    /**
     * Loads a DeneMatrix64F which has been saved to file using Java binary
     * serialization.
     *
     * @param fileName The file being loaded.
     * @return  DenseMatrix64F
     * @throws IOException
     */
    public static <T extends ReshapeMatrix64F> T loadBin(String fileName)
        throws IOException
    {
        FileInputStream fileStream = new FileInputStream(fileName);
        ObjectInputStream stream = new ObjectInputStream(fileStream);

        T ret;
        try {
            ret = (T)stream.readObject();
            if( stream.available() !=  0 ) {
                throw new RuntimeException("File not completely read?");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        stream.close();
        return (T)ret;
    }

    /**
     * Saves a matrix to disk using in a Column Space Value (CSV) format. For a 
     * description of the format see {@link MatrixIO#loadCSV(String)}.
     *
     * @param A The matrix being saved.
     * @param fileName Name of the file its being saved at.
     * @throws java.io.IOException
     */
    public static void saveCSV( ReshapeMatrix64F A , String fileName )
        throws IOException
    {
        PrintStream fileStream = new PrintStream(fileName);

        fileStream.print(A.getNumRows()+" ");
        fileStream.println(A.getNumCols());
        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < A.numCols; j++ ) {
                fileStream.print(A.get(i,j)+" ");
            }
            fileStream.println();
        }
        fileStream.close();
    }

    /**
     * Reads a matrix in which has been encoded using a Column Space Value (CSV)
     * file format. The number of rows and columns are read in on the first line. Then
     * each row is read in the subsequent lines.
     *
     * @param fileName The file being loaded.
     * @return DenseMatrix64F
     * @throws IOException
     */
    public static DenseMatrix64F loadCSV( String fileName )
        throws IOException
    {
        FileInputStream fileStream = new FileInputStream(fileName);
        ReadMatrixCsv csv = new ReadMatrixCsv(fileStream);

        DenseMatrix64F ret = csv.read();

        fileStream.close();

        return ret;
    }

    /**
     * Reads a matrix in which has been encoded using a Column Space Value (CSV)
     * file format.  For a description of the format see {@link MatrixIO#loadCSV(String)}.
     *
     * @param fileName The file being loaded.
     * @param numRows number of rows in the matrix.
     * @param numCols number of columns in the matrix.
     * @return DenseMatrix64F
     * @throws IOException
     */
    public static DenseMatrix64F loadCSV( String fileName , int numRows , int numCols )
        throws IOException
    {
        FileInputStream fileStream = new FileInputStream(fileName);
        ReadMatrixCsv csv = new ReadMatrixCsv(fileStream);

        DenseMatrix64F ret = csv.read(numRows,numCols);

        fileStream.close();

        return ret;
    }

    public static void print( PrintStream out , Matrix64F mat ) {
        print(out,mat,6,3);
    }

    public static void print(PrintStream out, Matrix64F mat , int numChar , int precision ) {
        String format = "%"+numChar+"."+precision+"f ";

        print(out, mat,format);
    }

    public static void print(PrintStream out , Matrix64F mat , String format ) {

        String type = ReshapeMatrix64F.class.isAssignableFrom(mat.getClass()) ? "dense" : "dense fixed";

        out.println("Type = "+type+" , numRows = "+mat.getNumRows()+" , numCols = "+mat.getNumCols());

        format += " ";

        for( int y = 0; y < mat.getNumRows(); y++ ) {
            for( int x = 0; x < mat.getNumCols(); x++ ) {
                out.printf(format,mat.get(y,x));
            }
            out.println();
        }
    }

    public static void print( PrintStream out , ReshapeMatrix64F mat , String format ,
                              int row0 , int row1, int col0 , int col1 ) {
        out.println("Type = submatrix , rows "+row0+" to "+row1+"  columns "+col0+" to "+col1);

        format += " ";

        for( int y = row0; y < row1; y++ ) {
            for( int x = col0; x < col1; x++ ) {
                out.printf(format,mat.get(y,x));
            }
            out.println();
        }
    }

//    public static void main( String []args ) {
//        Random rand = new Random(234234);
//        DenseMatrix64F A = RandomMatrices.createRandom(50,70,rand);
//
//        SingularValueDecomposition decomp = DecompositionFactory.svd();
//
//        decomp.decompose(A);
//
//        displayMatrix(A,"Original");
//        displayMatrix(decomp.getU(false),"U");
//        displayMatrix(decomp.getV(false),"V");
//        displayMatrix(decomp.getW(null),"W");
//    }
}
