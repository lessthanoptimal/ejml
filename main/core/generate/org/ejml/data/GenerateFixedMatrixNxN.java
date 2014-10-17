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

package org.ejml.data;

import org.ejml.CodeGeneratorBase;

import java.io.FileNotFoundException;

/**
 * @author Peter Abeles
 */
public class GenerateFixedMatrixNxN extends CodeGeneratorBase{

    String classPreamble = "FixedMatrix";

    @Override
    public void generate() throws FileNotFoundException {
        for( int dimension = 2; dimension <= 6; dimension++ ){
            print(dimension);
        }
    }

    public void print( int dimen ) throws FileNotFoundException {
        String className = classPreamble +dimen+"x"+dimen+"_64F";

        setOutputFile(className);

        out.println("import org.ejml.ops.MatrixIO;\n\n"+
                "/**\n" +
                " * Fixed sized "+dimen+" by "+className+" matrix.  The matrix is stored as class variables for very fast read/write.  aXY is the\n" +
                " * value of row = X and column = Y.\n" +
                " *\n" +
                " * @author Peter Abeles\n" +
                " */\n" +
                "public class "+className+" implements FixedMatrix64F {\n");
        printClassParam(dimen);
                out.print("\n" +
                "    public "+className+"() {\n" +
                "    }\n" +
                "\n" +
                "    public "+className);
        printFunctionParam(dimen);
        out.print("    {\n");
        printSetFromParam(dimen, "");
        out.print("    }\n" +
                "\n" +
                "    public " + className + "( " + className + " o ) {\n");
        printSetFromParam(dimen, "o.");
        out.print("    }\n" +
                "\n" +
                "    @Override\n" +
                "    public double get(int row, int col) {\n" +
                "        return unsafe_get(row,col);\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public double unsafe_get(int row, int col) {\n");
        setGetter(dimen);
        out.print("        throw new IllegalArgumentException(\"Row and/or column out of range. \"+row+\" \"+col);\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void set(int row, int col, double val) {\n" +
                "        unsafe_set(row,col,val);\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void unsafe_set(int row, int col, double val) {\n");
        setSetter(dimen);
        out.print("        throw new IllegalArgumentException(\"Row and/or column out of range. \"+row+\" \"+col);\n" +
                "    }\n" +
                "\n");
        printSetMatrix(dimen);
        out.print("    @Override\n" +
                "    public int getNumRows() {\n" +
                "        return "+dimen+";\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public int getNumCols() {\n" +
                "        return "+dimen+";\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public int getNumElements() {\n" +
                "        return "+(dimen*dimen)+";\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public <T extends Matrix> T copy() {\n" +
                "        return (T)new "+className+"(this);\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void print() {\n" +
                "        MatrixIO.print(System.out, this);\n" +
                "    }\n" +
                "}\n\n");
    }

    private void printClassParam( int dimen ) {
        for( int y = 1; y <= dimen; y++ ) {
            out.print("    public double ");
            for( int x = 1; x <= dimen; x++ ) {
                out.print("a"+y+""+x);
                if( x != dimen )
                    out.print(",");
                else
                    out.println(";");
            }
        }
    }

    private void printFunctionParam( int dimen ) {
        for( int y = 1; y <= dimen; y++ ) {
            if( y == 1 )
                out.print("( ");
            else
                out.print("                              ");
            for( int x = 1; x <= dimen; x++ ) {
                out.print("double a"+y+""+x);
                if( x != dimen )
                    out.print(",");
                else if( y != dimen )
                    out.println(",");
                else
                    out.println(")");
            }
        }
    }

    private void printSetFromParam(int dimen, String prefix) {
        for( int y = 1; y <= dimen; y++ ) {
            for( int x = 1; x <= dimen; x++ ) {
                out.println("        this.a"+y+""+x+" = "+prefix+"a"+y+""+x+";");
            }
        }
    }

    private void setGetter(int dimen) {
        for( int y = 1; y <= dimen; y++ ) {
            if( y == 1 )
                out.print("        if( row == 0 ) {\n");
            else
                out.print("        } else if( row == "+(y-1)+" ) {\n");
            for( int x = 1; x <= dimen; x++ ) {
                if( x == 1 )
                    out.print("            if( col == 0 ) {\n");
                else
                    out.print("            } else if( col == "+(x-1)+" ) {\n");
                out.print("                return a"+y+""+x+";\n");
            }
            out.print("            }\n");
        }
        out.print("        }\n");
    }

    private void setSetter(int dimen ) {
        for( int y = 1; y <= dimen; y++ ) {
            if( y == 1 )
                out.print("        if( row == 0 ) {\n");
            else
                out.print("        } else if( row == "+(y-1)+" ) {\n");
            for( int x = 1; x <= dimen; x++ ) {
                if( x == 1 )
                    out.print("            if( col == 0 ) {\n");
                else
                    out.print("            } else if( col == "+(x-1)+" ) {\n");
                out.print("                a"+y+""+x+" = val; return;\n");
            }
            out.print("            }\n");
        }
        out.print("        }\n");
    }

    private void printSetMatrix( int dimen ) {
        out.print("    @Override\n" +
                "    public void set(Matrix original) {\n" +
                "        if( original.getNumCols() != "+dimen+" || original.getNumRows() != "+dimen+" )\n" +
                "            throw new IllegalArgumentException(\"Rows and/or columns do not match\");\n" +
                "        RealMatrix64F m = (RealMatrix64F)original;\n" +
                "        \n");
        for( int y = 1; y <= dimen; y++ ) {
            for( int x = 1; x <= dimen; x++ ) {
                out.print("        a"+y+""+x+" = m.get("+(y-1)+","+(x-1)+");\n");
            }
        }
        out.print("    }\n\n");
    }

    public static void main( String args[] ) throws FileNotFoundException {
        GenerateFixedMatrixNxN app = new GenerateFixedMatrixNxN();

        app.generate();
    }
}
