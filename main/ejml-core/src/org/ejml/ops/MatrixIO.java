/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlVersion;
import org.ejml.data.*;
import org.jetbrains.annotations.Nullable;
import us.hebi.matlab.mat.ejml.Mat5Ejml;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.MatFile;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import static org.ejml.UtilEjml.fancyString;
import static org.ejml.UtilEjml.fancyStringF;

/**
 * Provides simple to use routines for reading and writing matrices to and from files.
 *
 * @author Peter Abeles
 */
public class MatrixIO {

    /** Default printf float format */
    public static final String DEFAULT_FLOAT_FORMAT = "%11.4E";
    /** Number of digits in pretty format */
    public static final int DEFAULT_LENGTH = 11;
    /** Specified the printf format used when printing out in Matlab format */
    public static String MATLAB_FORMAT = "%.8E";

    /** Local used by printf(). In general you want this to be US so that it stays standard compliant */
    public static Locale local = Locale.US;

    /**
     * Converts a text string in matlab format into a DDRM matrix
     */
    public static DMatrixRMaj matlabToDDRM( String text ) {
        // String all white space and the two [ ] characters
        text = text.replaceAll("(\\s+|\\[|\\])", "");
        String[] stringRows = text.split(";");
        String[] words = stringRows[0].split(",");
        DMatrixRMaj output = new DMatrixRMaj(stringRows.length, words.length);
        for (int row = 0; row < output.numRows; row++) {
            words = stringRows[row].split(",");
            if (words.length != output.numCols)
                throw new IllegalArgumentException("Inconsistent column lengths. " + output.numCols + " " + words.length);
            for (int col = 0; col < output.numCols; col++) {
                double value = Double.parseDouble(words[col]);
                output.set(row, col, value);
            }
        }
        return output;
    }

    /**
     * Converts a text string in matlab format into a DDRM matrix
     */
    public static FMatrixRMaj matlabToFDRM( String text ) {
        // String all white space and the two [ ] characters
        text = text.replaceAll("(\\s+|\\[|\\])", "");
        String[] stringRows = text.split(";");
        String[] words = stringRows[0].split(",");
        FMatrixRMaj output = new FMatrixRMaj(stringRows.length, words.length);
        for (int row = 0; row < output.numRows; row++) {
            words = stringRows[row].split(",");
            if (words.length != output.numCols)
                throw new IllegalArgumentException("Inconsistent column lengths. " + output.numCols + " " + words.length);
            for (int col = 0; col < output.numCols; col++) {
                float value = Float.parseFloat(words[col]);
                output.set(row, col, value);
            }
        }
        return output;
    }

    /**
     * Writes a stream using the Matrix Market Coordinate format.
     *
     * https://math.nist.gov/MatrixMarket/formats.html
     *
     * @param matrix The matrix to be written
     * @param floatFormat The format used by printf. "%.4e" is suggested
     * @param writer The writer
     */
    public static void saveMatrixMarketD( DMatrixSparse matrix, String floatFormat, Writer writer ) {
        PrintWriter out = new PrintWriter(writer);
        out.println("%%MatrixMarket matrix coordinate real general");
        out.println("%=================================================================================");
        out.println("% Matrix Market Coordinate file written by EJML " + EjmlVersion.VERSION);
        out.println("% printf format used '" + floatFormat + "'");
        out.println("%");
        out.println("% Indices are 1-based, i.e. A(1,1) is the first element.");
        out.println("%=================================================================================");
        out.printf(local, "%9d %9d %9d\n", matrix.getNumRows(), matrix.getNumCols(), matrix.getNonZeroLength());

        String lineFormat = "%9d %9d " + floatFormat + "\n";

        Iterator<DMatrixSparse.CoordinateRealValue> iter = matrix.createCoordinateIterator();
        while (iter.hasNext()) {
            DMatrixSparse.CoordinateRealValue val = iter.next();
            // matrix market is 1 indexed
            out.printf(local, lineFormat, val.row + 1, val.col + 1, val.value);
        }
        out.flush();
    }

    /**
     * Writes a stream using the Matrix Market Coordinate format.
     *
     * https://math.nist.gov/MatrixMarket/formats.html
     *
     * @param matrix The matrix to be written
     * @param floatFormat The format used by printf. "%.4e" is suggested
     * @param writer The writer
     */
    public static void saveMatrixMarketF( FMatrixSparse matrix, String floatFormat, Writer writer ) {
        PrintWriter out = new PrintWriter(writer);
        out.println("%%MatrixMarket matrix coordinate real general");
        out.println("%=================================================================================");
        out.println("% Matrix Market Coordinate file written by EJML " + EjmlVersion.VERSION);
        out.println("% printf format used '" + floatFormat + "'");
        out.println("%");
        out.println("% Indices are 1-based, i.e. A(1,1) is the first element.");
        out.println("%=================================================================================");
        out.printf(local, "%9d %9d %9d\n", matrix.getNumRows(), matrix.getNumCols(), matrix.getNonZeroLength());

        String lineFormat = "%9d %9d " + floatFormat + "\n";

        Iterator<FMatrixSparse.CoordinateRealValue> iter = matrix.createCoordinateIterator();
        while (iter.hasNext()) {
            FMatrixSparse.CoordinateRealValue val = iter.next();
            // matrix market is 1 indexed
            out.printf(local, lineFormat, val.row + 1, val.col + 1, val.value);
        }
        out.flush();
    }

    /**
     * <p>Reads a stream in Matrix Market Coordinate format</p>
     *
     * <a href="https://math.nist.gov/MatrixMarket/formats.html">Matrix Market</a>
     *
     * @param reader Input reader
     * @return DMatrixSparseTriplet
     */
    public static DMatrixSparseTriplet loadMatrixMarketD( Reader reader ) {
        var output = new DMatrixSparseTriplet();
        loadMatrixMarket(reader,
                output::reshape,
                ( r, c, word ) -> output.addItem(r, c, Double.parseDouble(word)));
        return output;
    }

    /**
     * <p>Reads a stream in Matrix Market Coordinate format</p>
     *
     * <a href="https://math.nist.gov/MatrixMarket/formats.html">Matrix Market</a>
     *
     * @param reader Input reader
     * @return DMatrixRMaj
     */
    public static DMatrixRMaj loadMatrixMarketDenseD( Reader reader ) {
        var output = new DMatrixRMaj(0, 0);
        loadMatrixMarket(reader,
                ( r, c, l ) -> output.reshape(r, c),
                ( r, c, word ) -> output.set(r, c, Double.parseDouble(word)));
        return output;
    }

    /**
     * <p>Reads a stream in Matrix Market Coordinate format</p>
     *
     * <a href="https://math.nist.gov/MatrixMarket/formats.html">Matrix Market</a>
     *
     * @param reader Input reader
     * @return FMatrixSparseTriplet
     */
    public static FMatrixSparseTriplet loadMatrixMarketF( Reader reader ) {
        var output = new FMatrixSparseTriplet();
        loadMatrixMarket(reader,
                output::reshape,
                ( r, c, word ) -> output.addItem(r, c, Float.parseFloat(word)));
        return output;
    }

    /**
     * <p>Reads a stream in Matrix Market Coordinate format</p>
     *
     * <a href="https://math.nist.gov/MatrixMarket/formats.html">Matrix Market</a>
     *
     * @param reader Input reader
     * @return FMatrixRMaj
     */
    public static FMatrixRMaj loadMatrixMarketDenseF( Reader reader ) {
        var output = new FMatrixRMaj(0, 0);
        loadMatrixMarket(reader,
                ( r, c, l ) -> output.reshape(r, c),
                ( r, c, word ) -> output.set(r, c, Float.parseFloat(word)));
        return output;
    }

    /**
     * Common logic for loading all matrix-market files
     *
     * @param reader reads input file
     * @param opReshape reshapes matrix
     * @param opAssign assigns values to each element
     */
    private static void loadMatrixMarket( Reader reader, ReshapeMatrix opReshape, AssignValue opAssign ) {
        var bufferedReader = new BufferedReader(reader);
        try {
            boolean vector = false;
            boolean hasHeader = false;
            // keeps track of number of elements read for vectors
            int count = 0;
            int rows = -1, cols = -1;
            String line = bufferedReader.readLine();
            while (line != null) {
                if (line.length() == 0 || line.charAt(0) == '%') {
                    line = bufferedReader.readLine();
                    continue;
                }
                String[] words = line.trim().split("\\s+");

                if (hasHeader) {
                    if (vector) {
                        if (words.length > 1)
                            throw new IOException("Expected only one word in each line for a vector");
                        if (rows == 1) {
                            opAssign.assign(0, count, words[0]);
                        } else {
                            opAssign.assign(count, 0, words[0]);
                        }
                        count++;
                    } else {
                        int row = Integer.parseInt(words[0]) - 1;
                        int col = Integer.parseInt(words[1]) - 1;
                        opAssign.assign(row, col, words[2]);
                    }
                } else {
                    if (words.length > 3)
                        throw new IOException("Too many words in header. '" + line + "'");
                    // read matrix shape
                    rows = Integer.parseInt(words[0]);
                    cols = Integer.parseInt(words[1]);

                    int nz_length;
                    if (words.length == 2) {
                        vector = true;
                        nz_length = Math.max(rows, cols);
                        if (Math.min(rows, cols) != 1)
                            throw new IOException("Vector matrix but neither size is equal to 1");
                    } else {
                        nz_length = Integer.parseInt(words[2]);
                    }
                    opReshape.reshape(rows, cols, nz_length);
                    hasHeader = true;
                }
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>Saves a matrix to disk using MATLAB's MAT-File Format (Level 5) binary serialization.</p>
     *
     * Notes:
     * <ul>
     *     <li>The matrix gets stored as a single root level entry named 'ejmlMatrix'</li>
     *     <li>{@link FMatrixSparseCSC} get stored as sparse double matrices</li>
     * </ul>
     *
     * See:
     * <a href="https://github.com/HebiRobotics/MFL">MAT File Library</a>
     * <a href="https://www.mathworks.com/help/pdf_doc/matlab/matfile_format.pdf">matfile_format.pdf</a>
     *
     * @param A The matrix being saved.
     * @param fileName Name of the file its being saved at.
     */
    public static void saveMatlab( Matrix A, String fileName ) throws IOException {
        MflAccess.verifyEnabled();
        MflAccess.IO.saveMatlab(A, fileName);
    }

    /**
     * Loads a {@link Matrix} which has been saved to file using MATLAB's MAT-File Format (Level 5) serialization.
     *
     * @param fileName The file being loaded. It is expected to contain a root level entry named 'ejmlMatrix'.
     * @return Matrix A matrix of the type that best matches the serialized data
     */
    public static <T extends Matrix> T loadMatlab( String fileName ) throws IOException {
        return loadMatlab(fileName, null);
    }

    /**
     * Loads a {@link Matrix} which has been saved to file using MATLAB's MAT-File Format (Level 5) serialization.
     *
     * @param fileName The file being loaded. It is expected to contain a root level entry named 'ejmlMatrix'.
     * @param output Output Matrix. Automatically matched if null. Modified.
     * @return Matrix
     */
    public static <T extends Matrix> T loadMatlab( String fileName, @Nullable T output ) throws IOException {
        MflAccess.verifyEnabled();
        return MflAccess.IO.loadMatlab(fileName, output);
    }

    /**
     * Lazily verify MFL availability without using reflections
     */
    static class MflAccess {

        private static void verifyEnabled() {
            if (!ENABLED) {
                throw new IllegalStateException("Missing dependency: add maven coordinates 'us.hebi.matlab.mat:mfl-ejml:0.5.7' or later. https://github.com/HebiRobotics/MFL");
            }
        }

        static {
            boolean foundInClasspath;
            try {
                @SuppressWarnings("unused")
                Class<Mat5Ejml> clazz = Mat5Ejml.class;
                foundInClasspath = true;
            } catch (NoClassDefFoundError cfe) {
                foundInClasspath = false;
            }
            ENABLED = foundInClasspath;
        }

        private static final boolean ENABLED;

        /**
         * Load external library once we have verified that it exists
         */
        static class IO {

            public static void saveMatlab( Matrix A, String fileName ) throws IOException {
                MatFile mat = Mat5.newMatFile().addArray(ENTRY_NAME, Mat5Ejml.asArray(A));
                Mat5.writeToFile(mat, fileName);
            }

            public static <T extends Matrix> T loadMatlab( String fileName, @Nullable T output ) throws IOException {
                MatFile mat = Mat5.readFromFile(fileName);
                for (MatFile.Entry entry : mat.getEntries()) {
                    if (ENTRY_NAME.matches(entry.getName())) {
                        return Mat5Ejml.convert(entry.getValue(), output);
                    }
                }
                throw new IllegalArgumentException("File does not have expected entry: '" + ENTRY_NAME + "'");
            }

            private static final String ENTRY_NAME = "ejmlMatrix";
        }
    }

    /**
     * Saves a matrix to disk using Java binary serialization.
     *
     * @param A The matrix being saved.
     * @param fileName Name of the file its being saved at.
     */
    public static void saveBin( DMatrix A, String fileName )
            throws IOException {
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
     * @return DMatrixRMaj
     * @deprecated This will be removed in the future due to how unsafe readObject() is.
     */
    @Deprecated
    @SuppressWarnings("BanSerializableRead")
    public static <T extends DMatrix> T loadBin( String fileName )
            throws IOException {
        FileInputStream fileStream = new FileInputStream(fileName);
        ObjectInputStream stream = new ObjectInputStream(fileStream);

        T ret;
        try {
            ret = (T)stream.readObject();
            if (stream.available() != 0) {
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
     * description of the format see {@link MatrixIO#loadCSV(String, boolean)}.
     *
     * @param A The matrix being saved.
     * @param fileName Name of the file its being saved at.
     */
    public static void saveDenseCSV( DMatrix A, String fileName )
            throws IOException {
        PrintStream fileStream = new PrintStream(fileName);

        fileStream.println(A.getNumRows() + " " + A.getNumCols() + " real");
        for (int i = 0; i < A.getNumRows(); i++) {
            for (int j = 0; j < A.getNumCols(); j++) {
                fileStream.print(A.get(i, j) + " ");
            }
            fileStream.println();
        }
        fileStream.close();
    }

    /**
     * Saves a matrix to disk using in a Column Space Value (CSV) format. For a
     * description of the format see {@link MatrixIO#loadCSV(String, boolean)}.
     *
     * @param A The matrix being saved.
     * @param fileName Name of the file its being saved at.
     */
    public static void saveSparseCSV( DMatrixSparseTriplet A, String fileName )
            throws IOException {
        PrintStream fileStream = new PrintStream(fileName);

        fileStream.println(A.getNumRows() + " " + A.getNumCols() + " " + A.nz_length + " real");
        for (int i = 0; i < A.nz_length; i++) {
            int row = A.nz_rowcol.data[i*2];
            int col = A.nz_rowcol.data[i*2 + 1];
            double value = A.nz_value.data[i];

            fileStream.println(row + " " + col + " " + value);
        }
        fileStream.close();
    }

    /**
     * Saves a matrix to disk using in a Column Space Value (CSV) format. For a
     * description of the format see {@link MatrixIO#loadCSV(String, boolean)}.
     *
     * @param A The matrix being saved.
     * @param fileName Name of the file its being saved at.
     */
    public static void saveSparseCSV( FMatrixSparseTriplet A, String fileName )
            throws IOException {
        PrintStream fileStream = new PrintStream(fileName);

        fileStream.println(A.getNumRows() + " " + A.getNumCols() + " " + A.nz_length + " real");
        for (int i = 0; i < A.nz_length; i++) {
            int row = A.nz_rowcol.data[i*2];
            int col = A.nz_rowcol.data[i*2 + 1];
            float value = A.nz_value.data[i];

            fileStream.println(row + " " + col + " " + value);
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
     */
    public static <T extends DMatrix> T loadCSV( String fileName, boolean doublePrecision )
            throws IOException {
        FileInputStream fileStream = new FileInputStream(fileName);
        ReadMatrixCsv csv = new ReadMatrixCsv(fileStream);

        T ret;
        if (doublePrecision)
            ret = csv.read64();
        else
            ret = csv.read32();

        fileStream.close();

        return ret;
    }

    /**
     * Reads a matrix in which has been encoded using a Column Space Value (CSV)
     * file format. For a description of the format see {@link MatrixIO#loadCSV(String, boolean)}.
     *
     * @param fileName The file being loaded.
     * @param numRows number of rows in the matrix.
     * @param numCols number of columns in the matrix.
     * @return DMatrixRMaj
     */
    public static DMatrixRMaj loadCSV( String fileName, int numRows, int numCols )
            throws IOException {
        FileInputStream fileStream = new FileInputStream(fileName);
        ReadMatrixCsv csv = new ReadMatrixCsv(fileStream);

        DMatrixRMaj ret = csv.readDDRM(numRows, numCols);

        fileStream.close();

        return ret;
    }

    public static void printFancy( PrintStream out, DMatrix mat, int length ) {
        printTypeSize(out, mat);
        DecimalFormat format = new DecimalFormat("#");

        final int cols = mat.getNumCols();

        for (int row = 0; row < mat.getNumRows(); row++) {
            for (int col = 0; col < cols; col++) {
                out.print(fancyStringF(mat.get(row, col), format, length, 4));
                if (col != cols - 1)
                    out.print(" ");
            }
            out.println();
        }
    }

    public static void printFancy( PrintStream out, FMatrix mat, int length ) {
        printTypeSize(out, mat);
        DecimalFormat format = new DecimalFormat("#");

        final int cols = mat.getNumCols();

        for (int row = 0; row < mat.getNumRows(); row++) {
            for (int col = 0; col < cols; col++) {
                out.print(fancyStringF(mat.get(row, col), format, length, 4));
                if (col != cols - 1)
                    out.print(" ");
            }
            out.println();
        }
    }

    public static void printFancy( PrintStream out, ZMatrix mat, int length ) {
        printTypeSize(out, mat);
        DecimalFormat format = new DecimalFormat("#");

        StringBuilder builder = new StringBuilder(length);
        final int cols = mat.getNumCols();

        Complex_F64 c = new Complex_F64();
        for (int y = 0; y < mat.getNumRows(); y++) {
            for (int x = 0; x < cols; x++) {
                mat.get(y, x, c);
                String real = fancyString(c.real, format, length, 4);
                String img = fancyString(c.imaginary, format, length, 4);
                real = real + padSpace(builder, length - real.length());
                img = img + "i" + padSpace(builder, length - img.length());

                out.print(real + " + " + img);
                if (x < mat.getNumCols() - 1) {
                    out.print(" , ");
                }
            }
            out.println();
        }
    }

    public static void printFancy( PrintStream out, CMatrix mat, int length ) {
        printTypeSize(out, mat);
        DecimalFormat format = new DecimalFormat("#");

        StringBuilder builder = new StringBuilder(length);
        final int cols = mat.getNumCols();

        Complex_F32 c = new Complex_F32();
        for (int y = 0; y < mat.getNumRows(); y++) {
            for (int x = 0; x < cols; x++) {
                mat.get(y, x, c);
                String real = fancyString(c.real, format, length, 4);
                String img = fancyString(c.imaginary, format, length, 4);
                real = real + padSpace(builder, length - real.length());
                img = img + padSpace(builder, length - img.length());

                out.print(real + " + " + img + "i ");
                if (x < mat.getNumCols() - 1) {
                    out.print(" , ");
                }
            }
            out.println();
        }
    }

    private static String padSpace( StringBuilder builder, int length ) {
        builder.delete(0, builder.length());
        for (int i = 0; i < length; i++) {
            builder.append(' ');
        }
        return builder.toString();
    }

    public static void printFancy( PrintStream out, DMatrixSparseCSC m, int length ) {
        DecimalFormat format = new DecimalFormat("#");
        printTypeSize(out, m);

        char[] zero = new char[length];
        Arrays.fill(zero, ' ');
        zero[length/2] = '*';

        for (int row = 0; row < m.numRows; row++) {
            for (int col = 0; col < m.numCols; col++) {
                int index = m.nz_index(row, col);
                if (index >= 0)
                    out.print(fancyStringF(m.nz_values[index], format, length, 4));
                else
                    out.print(zero);
                if (col != m.numCols - 1)
                    out.print(" ");
            }
            out.println();
        }
    }

    public static void print( PrintStream out, Matrix mat ) {
        String format = DEFAULT_FLOAT_FORMAT;

        switch (mat.getType()) {
            case DDRM -> print(out, (DMatrix)mat, format);
            case FDRM -> print(out, (FMatrix)mat, format);
            case ZDRM -> print(out, (ZMatrix)mat, format);
            case CDRM -> print(out, (CMatrix)mat, format);
            case DSCC -> print(out, (DMatrixSparseCSC)mat, format);
            case DTRIPLET -> print(out, (DMatrixSparseTriplet)mat, format);
            case FSCC -> print(out, (FMatrixSparseCSC)mat, format);
            case FTRIPLET -> print(out, (FMatrixSparseTriplet)mat, format);
            default -> throw new RuntimeException("Unknown type " + mat.getType());
        }
    }

    public static void print( PrintStream out, DMatrix mat ) {
        print(out, mat, DEFAULT_FLOAT_FORMAT);
    }

    /**
     * Prints the matrix out in a text format. The format is specified using notation from
     * {@link String#format(String, Object...)}. Unless the format is set to 'matlab' then it will print it out
     * in a format that's understood by Matlab. 'java' will print a java 2D array.
     *
     * @param out Output stream
     * @param mat Matrix to be printed
     * @param format printf style or 'matlab'
     */
    public static void print( PrintStream out, DMatrix mat, String format ) {
        if (format.equalsIgnoreCase("matlab")) {
            printMatlab(out, mat);
        } else if (format.equalsIgnoreCase("java")) {
            printJava(out, mat, format);
        } else {
            printTypeSize(out, mat);

            format += " ";

            for (int row = 0; row < mat.getNumRows(); row++) {
                for (int col = 0; col < mat.getNumCols(); col++) {
                    out.printf(local, format, mat.get(row, col));
                }
                out.println();
            }
        }
    }

    public static void printMatlab( PrintStream out, DMatrix mat ) {
        out.print("[ ");

        for (int row = 0; row < mat.getNumRows(); row++) {
            for (int col = 0; col < mat.getNumCols(); col++) {
                out.printf(local, "%.12E", mat.get(row, col));
                if (col + 1 < mat.getNumCols()) {
                    out.print(" , ");
                }
            }
            if (row + 1 < mat.getNumRows())
                out.println(" ;");
            else
                out.println(" ]");
        }
    }

    public static void printMatlab( PrintStream out, FMatrix mat ) {
        out.print("[ ");

        for (int row = 0; row < mat.getNumRows(); row++) {
            for (int col = 0; col < mat.getNumCols(); col++) {
                out.printf(local, MATLAB_FORMAT, mat.get(row, col));
                if (col + 1 < mat.getNumCols()) {
                    out.print(" , ");
                }
            }
            if (row + 1 < mat.getNumRows())
                out.println(" ;");
            else
                out.println(" ]");
        }
    }

    /**
     * Prints the matrix out in a text format. The format is specified using notation from
     * {@link String#format(String, Object...)}. Unless the format is set to 'matlab' then it will print it out
     * in a format that's understood by Matlab.
     *
     * @param out Output stream
     * @param m Matrix to be printed
     * @param format printf style or 'matlab'
     */
    public static void print( PrintStream out, DMatrixSparseCSC m, String format ) {
        if (format.equalsIgnoreCase("matlab")) {
            printMatlab(out, m);
        } else {
            printTypeSize(out, m);

            int length = String.format(format, -1.1123).length();
            char[] zero = new char[length];
            Arrays.fill(zero, ' ');
            zero[length/2] = '*';

            for (int row = 0; row < m.numRows; row++) {
                for (int col = 0; col < m.numCols; col++) {
                    int index = m.nz_index(row, col);
                    if (index >= 0)
                        out.printf(local, format, m.nz_values[index]);
                    else
                        out.print(zero);
                    if (col != m.numCols - 1)
                        out.print(" ");
                }
                out.println();
            }
        }
    }

    public static void print( PrintStream out, FMatrixSparseCSC m, String format ) {
        if (format.equalsIgnoreCase("matlab")) {
            printMatlab(out, m);
        } else {
            printTypeSize(out, m);

            int length = String.format(format, -1.1123).length();
            char[] zero = new char[length];
            Arrays.fill(zero, ' ');
            zero[length/2] = '*';

            for (int row = 0; row < m.numRows; row++) {
                for (int col = 0; col < m.numCols; col++) {
                    int index = m.nz_index(row, col);
                    if (index >= 0)
                        out.printf(local, format, m.nz_values[index]);
                    else
                        out.print(zero);
                    if (col != m.numCols - 1)
                        out.print(" ");
                }
                out.println();
            }
        }
    }

    public static void print( PrintStream out, DMatrixSparseTriplet m, String format ) {
        printTypeSize(out, m);

        for (int row = 0; row < m.numRows; row++) {
            for (int col = 0; col < m.numCols; col++) {
                int index = m.nz_index(row, col);
                if (index >= 0)
                    out.printf(local, format, m.nz_value.data[index]);
                else
                    out.print("   *  ");
                if (col != m.numCols - 1)
                    out.print(" ");
            }
            out.println();
        }
    }

    public static void print( PrintStream out, FMatrixSparseTriplet m, String format ) {
        printTypeSize(out, m);

        for (int row = 0; row < m.numRows; row++) {
            for (int col = 0; col < m.numCols; col++) {
                int index = m.nz_index(row, col);
                if (index >= 0)
                    out.printf(local, format, m.nz_value.data[index]);
                else
                    out.print("   *  ");
                if (col != m.numCols - 1)
                    out.print(" ");
            }
            out.println();
        }
    }

    public static void printJava( PrintStream out, DMatrix mat, String format ) {

        String type = mat.getType().getBits() == 64 ? "double" : "float";

        out.println("new " + type + "[][]{");

        format += " ";

        for (int y = 0; y < mat.getNumRows(); y++) {
            out.print("{");
            for (int x = 0; x < mat.getNumCols(); x++) {
                out.printf(local, format, mat.get(y, x));
                if (x + 1 < mat.getNumCols())
                    out.print(", ");
            }
            if (y + 1 < mat.getNumRows())
                out.println("},");
            else
                out.println("}};");
        }
    }

    public static void print( PrintStream out, FMatrix mat ) {
        print(out, mat, DEFAULT_FLOAT_FORMAT);
    }

    public static void print( PrintStream out, FMatrix mat, String format ) {
        if (format.equalsIgnoreCase("matlab")) {
            printMatlab(out, mat);
        } else if (format.equalsIgnoreCase("java")) {
            printJava(out, mat, format);
        } else {
            printTypeSize(out, mat);

            format += " ";

            for (int row = 0; row < mat.getNumRows(); row++) {
                for (int col = 0; col < mat.getNumCols(); col++) {
                    out.printf(local, format, mat.get(row, col));
                }
                out.println();
            }
        }
    }

    public static void print( PrintStream out, DMatrix mat, String format,
                              int row0, int row1, int col0, int col1 ) {
        out.println("Type = submatrix , rows " + row0 + " to " + row1 + "  columns " + col0 + " to " + col1);

        format += " ";

        for (int y = row0; y < row1; y++) {
            for (int x = col0; x < col1; x++) {
                out.printf(local, format, mat.get(y, x));
            }
            out.println();
        }
    }

    public static void printJava( PrintStream out, FMatrix mat, String format ) {
        String type = mat.getType().getBits() == 64 ? "double" : "float";

        out.println("new " + type + "[][]{");

        format += " ";

        for (int y = 0; y < mat.getNumRows(); y++) {
            out.print("{");
            for (int x = 0; x < mat.getNumCols(); x++) {
                out.printf(local, format, mat.get(y, x));
                if (x + 1 < mat.getNumCols())
                    out.print(", ");
            }
            if (y + 1 < mat.getNumRows())
                out.println("},");
            else
                out.println("}};");
        }
    }

    public static void print( PrintStream out, FMatrix mat, String format,
                              int row0, int row1, int col0, int col1 ) {
        out.println("Type = submatrix , rows " + row0 + " to " + row1 + "  columns " + col0 + " to " + col1);

        format = format + " + " + format + "i";

        for (int y = row0; y < row1; y++) {
            for (int x = col0; x < col1; x++) {
                out.printf(local, format, mat.get(y, x));
            }
            out.println();
        }
    }

    public static void print( PrintStream out, ZMatrix mat, String format ) {
        printTypeSize(out, mat);

        format = format + " + " + format + "i";

        Complex_F64 c = new Complex_F64();
        for (int y = 0; y < mat.getNumRows(); y++) {
            for (int x = 0; x < mat.getNumCols(); x++) {
                mat.get(y, x, c);
                out.printf(local, format, c.real, c.imaginary);
                if (x < mat.getNumCols() - 1) {
                    out.print(" , ");
                }
            }
            out.println();
        }
    }

    private static void printTypeSize( PrintStream out, Matrix mat ) {
        if (mat instanceof MatrixSparse) {
            MatrixSparse m = (MatrixSparse)mat;
            out.println("Type = " + getMatrixType(mat) + " , rows = " + mat.getNumRows() +
                    " , cols = " + mat.getNumCols() + " , nz_length = " + m.getNonZeroLength());
        } else {
            out.println("Type = " + getMatrixType(mat) + " , rows = " + mat.getNumRows() + " , cols = " + mat.getNumCols());
        }
    }

    public static void print( PrintStream out, CMatrix mat, String format ) {
        printTypeSize(out, mat);

        format += " ";

        Complex_F32 c = new Complex_F32();
        for (int y = 0; y < mat.getNumRows(); y++) {
            for (int x = 0; x < mat.getNumCols(); x++) {
                mat.get(y, x, c);
                out.printf(local, format, c.real, c.imaginary);
                if (x < mat.getNumCols() - 1) {
                    out.print(" , ");
                }
            }
            out.println();
        }
    }

    private static String getMatrixType( Matrix mat ) {
        String type;
        if (mat.getType() == MatrixType.UNSPECIFIED) {
            type = mat.getClass().getSimpleName();
        } else {
            type = mat.getType().name();
        }
        return type;
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

    @FunctionalInterface interface AssignValue {
        void assign( int row, int col, String word );
    }

    @FunctionalInterface interface ReshapeMatrix {
        void reshape( int rows, int cols, int nzLength );
    }
}
