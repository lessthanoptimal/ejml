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

package org.ejml.data;

import org.ejml.CodeGeneratorBase;

import java.io.FileNotFoundException;

/**
 * @author Peter Abeles
 */
public class GenerateFixedMatrixN extends CodeGeneratorBase{

    String classPreamble = "FixedMatrix";

    @Override
    public void generate() throws FileNotFoundException {
        for( int dimension = 2; dimension <= 6; dimension++ ){
            print(dimension);
        }
    }

    public void print( int dimen ) throws FileNotFoundException {
        String className = classPreamble +dimen+"_64F";

        setOutputFile(className);

        out.print("import org.ejml.ops.MatrixIO;\n" +
                "\n" +
                "/**\n" +
                " * Fixed sized vector with "+dimen+" elements.  Can represent a "+dimen+" x 1 or 1 x "+dimen+" matrix, context dependent.\n" +
                " * <p>DO NOT MODIFY.  Automatically generated code created by "+getClass().getSimpleName()+"</p>\n" +
                " *\n" +
                " * @author Peter Abeles\n" +
                " */\n" +
                "public class "+className+" implements FixedMatrix64F {\n");
        printClassParam(dimen);
        out.print("\n" +
                "    public "+className+"() {\n" +
                "    }\n" +
                "\n" +
                "    public "+className+"(");
        printFunctionParam(dimen);
        out.print(")\n" +
                "    {\n");
        printSetFromParam(dimen,"");
        out.print("    }\n" +
                "\n" +
                "    public "+className+"("+className+" o) {\n");
        printSetFromParam(dimen,"o.");
        out.print("    }\n" +
                "\n" +
                "    @Override\n" +
                "    public double get(int row, int col) {\n" +
                "        return unsafe_get(row,col);\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public double unsafe_get(int row, int col) {\n" +
                "        if( row != 0 && col != 0 )\n" +
                "            throw new IllegalArgumentException(\"Row or column must be zero since this is a vector\");\n" +
                "\n" +
                "        int w = Math.max(row,col);\n" +
                "\n");
        setGetter(dimen);
        out.print("        } else {\n" +
                "            throw new IllegalArgumentException(\"Out of range.  \"+w);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void set(int row, int col, double val) {\n" +
                "        unsafe_set(row,col,val);\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void unsafe_set(int row, int col, double val) {\n" +
                "        if( row != 0 && col != 0 )\n" +
                "            throw new IllegalArgumentException(\"Row or column must be zero since this is a vector\");\n" +
                "\n" +
                "        int w = Math.max(row,col);\n" +
                "\n");
        setSetter(dimen);
        out.print("        } else {\n" +
                "            throw new IllegalArgumentException(\"Out of range.  \"+w);\n" +
                "        }\n" +
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
                "        return 1;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public int getNumElements() {\n" +
                "        return "+dimen+";\n" +
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
        out.print("    public double ");
        for( int i = 1; i <= dimen; i++ ) {
           out.print("a"+i);
            if( i < dimen )
                out.print(",");
            else
                out.print(";\n");
        }
    }

    private void printFunctionParam( int dimen ) {
        for( int i = 1; i <= dimen; i++ ) {
            out.print("double a"+i);
            if( i < dimen )
                out.print(",");
        }
    }

    private void printSetFromParam(int dimen, String prefix) {
        for( int i = 1; i <= dimen; i++ ) {
            out.println("        this.a"+i+" = "+prefix+"a"+i+";");
        }
    }

    private void printSetMatrix(int dimen) {
        out.print("    @Override\n" +
                "    public void set(Matrix original) {\n" +
                "        RealMatrix64F m = (RealMatrix64F)original;\n" +
                "\n" +
                "        if( m.getNumCols() == 1 && m.getNumRows() == "+dimen+" ) {\n");
        for (int i = 0; i < dimen; i++) {
            out.print("            a"+(i+1)+" = m.get("+i+",0);\n");
        }
        out.print("        } else if( m.getNumRows() == 1 && m.getNumCols() == "+dimen+" ){\n");
        for (int i = 0; i < dimen; i++) {
            out.print("            a"+(i+1)+" = m.get(0,"+i+");\n");
        }
        out.print("        } else {\n" +
                "            throw new IllegalArgumentException(\"Incompatible shape\");\n" +
                "        }\n" +
                "    }\n\n");
    }

    private void setGetter(int dimen) {
        for( int i = 0; i < dimen; i++ ) {
            if( i == 0 )
                out.print("        if( w == 0 ) {\n");
            else
                out.print("        } else if( w == "+i+" ) {\n");
            out.print("            return a"+(i+1)+";\n");
        }
    }

    private void setSetter(int dimen ) {
        for( int i = 0; i < dimen; i++ ) {
            if( i == 0 )
                out.print("        if( w == 0 ) {\n");
            else
                out.print("        } else if( w == "+i+" ) {\n");
            out.print("            a"+(i+1)+" = val;\n");
        }
    }

    public static void main( String args[] ) throws FileNotFoundException {
        GenerateFixedMatrixN app = new GenerateFixedMatrixN();

        app.generate();
    }
}
