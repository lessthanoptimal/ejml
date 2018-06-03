/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.*;

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
    public static void saveBin(DMatrix A, String fileName)
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
     * Loads a {@link DMatrix} which has been saved to file using Java binary
     * serialization.
     *
     * @param fileName The file being loaded.
     * @return  DMatrixRMaj
     * @throws IOException
     */
    public static <T extends DMatrix> T loadBin(String fileName)
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
     * description of the format see {@link MatrixIO#loadCSV(String,boolean)}.
     *
     * @param A The matrix being saved.
     * @param fileName Name of the file its being saved at.
     * @throws java.io.IOException
     */
    public static void saveDenseCSV(DMatrix A , String fileName )
        throws IOException
    {
        PrintStream fileStream = new PrintStream(fileName);

        fileStream.println(A.getNumRows() + " " + A.getNumCols() + " real");
        for( int i = 0; i < A.getNumRows(); i++ ) {
            for( int j = 0; j < A.getNumCols(); j++ ) {
                fileStream.print(A.get(i,j)+" ");
            }
            fileStream.println();
        }
        fileStream.close();
    }

    /**
     * Saves a matrix to disk using in a Column Space Value (CSV) format. For a
     * description of the format see {@link MatrixIO#loadCSV(String,boolean)}.
     *
     * @param A The matrix being saved.
     * @param fileName Name of the file its being saved at.
     * @throws java.io.IOException
     */
    public static void saveSparseCSV(DMatrixSparseTriplet A , String fileName )
            throws IOException
    {
        PrintStream fileStream = new PrintStream(fileName);

        fileStream.println(A.getNumRows() + " " + A.getNumCols() +" "+A.nz_length+ " real");
        for (int i = 0; i < A.nz_length; i++) {
            int row = A.nz_rowcol.data[i*2];
            int col = A.nz_rowcol.data[i*2+1];
            double value = A.nz_value.data[i];

            fileStream.println(row+" "+col+" "+value);
        }
        fileStream.close();
    }

    /**
     * Saves a matrix to disk using in a Column Space Value (CSV) format. For a
     * description of the format see {@link MatrixIO#loadCSV(String,boolean)}.
     *
     * @param A The matrix being saved.
     * @param fileName Name of the file its being saved at.
     * @throws java.io.IOException
     */
    public static void saveSparseCSV(FMatrixSparseTriplet A , String fileName )
            throws IOException
    {
        PrintStream fileStream = new PrintStream(fileName);

        fileStream.println(A.getNumRows() + " " + A.getNumCols() +" "+A.nz_length+ " real");
        for (int i = 0; i < A.nz_length; i++) {
            int row = A.nz_rowcol.data[i*2];
            int col = A.nz_rowcol.data[i*2+1];
            float value = A.nz_value.data[i];

            fileStream.println(row+" "+col+" "+value);
        }
        fileStream.close();
    }

    /**
     * Reads a matrix in which has been encoded using a Column Space Value (CSV)
     * file format. The number of rows and columns are read in on the first line. Then
     * each row is read in the subsequent lines.
     *
     * Works with dense and sparse matrices.
     *
     * @param fileName The file being loaded.
     * @return DMatrix
     * @throws IOException
     */
    public static <T extends DMatrix>T loadCSV(String fileName , boolean doublePrecision )
        throws IOException
    {
        FileInputStream fileStream = new FileInputStream(fileName);
        ReadMatrixCsv csv = new ReadMatrixCsv(fileStream);

        T ret;
        if( doublePrecision )
            ret = csv.read64();
        else
            ret = csv.read32();

        fileStream.close();

        return ret;
    }

    /**
     * Reads a matrix in which has been encoded using a Column Space Value (CSV)
     * file format.  For a description of the format see {@link MatrixIO#loadCSV(String,boolean)}.
     *
     * @param fileName The file being loaded.
     * @param numRows number of rows in the matrix.
     * @param numCols number of columns in the matrix.
     * @return DMatrixRMaj
     * @throws IOException
     */
    public static DMatrixRMaj loadCSV(String fileName , int numRows , int numCols )
        throws IOException
    {
        FileInputStream fileStream = new FileInputStream(fileName);
        ReadMatrixCsv csv = new ReadMatrixCsv(fileStream);

        DMatrixRMaj ret = csv.readDDRM(numRows, numCols);

        fileStream.close();

        return ret;
    }

    public static void print( PrintStream out , DMatrix mat ) {
        print(out,mat,6,3);
    }

    public static void print(PrintStream out, DMatrix mat , int numChar , int precision ) {
        String format = "%"+numChar+"."+precision+"f";

        print(out, mat,format);
    }

    public static void print(PrintStream out , DMatrix mat , String format ) {

        String type = ReshapeMatrix.class.isAssignableFrom(mat.getClass()) ? "dense64" : "dense64 fixed";

        out.println("Type = "+type+" real , numRows = "+mat.getNumRows()+" , numCols = "+mat.getNumCols());

        format += " ";

        for( int y = 0; y < mat.getNumRows(); y++ ) {
            for( int x = 0; x < mat.getNumCols(); x++ ) {
                out.printf(format,mat.get(y,x));
            }
            out.println();
        }
    }

    public static void printJava(PrintStream out , DMatrix mat , String format ) {

        String type = mat.getType().getBits() == 64 ? "double" : "float";

        out.println("new "+type+"[][]{");

        format += " ";

        for( int y = 0; y < mat.getNumRows(); y++ ) {
            out.print("{");
            for( int x = 0; x < mat.getNumCols(); x++ ) {
                out.printf(format,mat.get(y,x));
                if( x+1<mat.getNumCols())
                    out.print(", ");
            }
            if( y+1 < mat.getNumRows())
                out.println("},");
            else
                out.println("}};");
        }
    }

    public static void print( PrintStream out , FMatrix mat ) {
        print(out,mat,6,3);
    }

    public static void print(PrintStream out, FMatrix mat , int numChar , int precision ) {
        String format = "%"+numChar+"."+precision+"f ";

        print(out, mat,format);
    }

    public static void print(PrintStream out , FMatrix mat , String format ) {

        String type = mat.getClass().getSimpleName();

        out.println("Type = "+type+" , numRows = "+mat.getNumRows()+" , numCols = "+mat.getNumCols());

        format += " ";

        for( int y = 0; y < mat.getNumRows(); y++ ) {
            for( int x = 0; x < mat.getNumCols(); x++ ) {
                out.printf(format,mat.get(y,x));
            }
            out.println();
        }
    }

    public static void print(PrintStream out , DMatrix mat , String format ,
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

    public static void printJava(PrintStream out , FMatrix mat , String format ) {

        String type = mat.getType().getBits() == 64 ? "double" : "float";

        out.println("new "+type+"[][]{");

        format += " ";

        for( int y = 0; y < mat.getNumRows(); y++ ) {
            out.print("{");
            for( int x = 0; x < mat.getNumCols(); x++ ) {
                out.printf(format,mat.get(y,x));
                if( x+1<mat.getNumCols())
                    out.print(", ");
            }
            if( y+1 < mat.getNumRows())
                out.println("},");
            else
                out.println("}};");
        }
    }

    public static void print(PrintStream out , FMatrix mat , String format ,
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

    public static void print( PrintStream out , ZMatrix mat ) {
        print(out,mat,6,3);
    }

    public static void print( PrintStream out , CMatrix mat ) {
        print(out,mat,6,3);
    }

    public static void print(PrintStream out, ZMatrix mat , int numChar , int precision ) {
        String format = "%"+numChar+"."+precision+"f + %"+numChar+"."+precision+"fi";

        print(out, mat,format);
    }

    public static void print(PrintStream out, CMatrix mat , int numChar , int precision ) {
        String format = "%"+numChar+"."+precision+"f + %"+numChar+"."+precision+"fi";

        print(out, mat,format);
    }

    public static void print(PrintStream out , ZMatrix mat , String format ) {

        String type = "dense64";

        out.println("Type = "+type+" complex , numRows = "+mat.getNumRows()+" , numCols = "+mat.getNumCols());

        format += " ";

        Complex_F64 c = new Complex_F64();
        for( int y = 0; y < mat.getNumRows(); y++ ) {
            for( int x = 0; x < mat.getNumCols(); x++ ) {
                mat.get(y,x,c);
                out.printf(format,c.real,c.imaginary);
                if( x < mat.getNumCols()-1 ) {
                    out.print(" , ");
                }
            }
            out.println();
        }
    }

    public static void print(PrintStream out , CMatrix mat , String format ) {

        String type = "dense32";

        out.println("Type = "+type+" complex , numRows = "+mat.getNumRows()+" , numCols = "+mat.getNumCols());

        format += " ";

        Complex_F32 c = new Complex_F32();
        for( int y = 0; y < mat.getNumRows(); y++ ) {
            for( int x = 0; x < mat.getNumCols(); x++ ) {
                mat.get(y,x,c);
                out.printf(format,c.real,c.imaginary);
                if( x < mat.getNumCols()-1 ) {
                    out.print(" , ");
                }
            }
            out.println();
        }
    }

//    public static void main( String []args ) {
//        Random rand = new Random(234234);
//        DMatrixRMaj A = RandomMatrices.createRandom(50,70,rand);
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
