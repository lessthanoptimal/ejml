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

package org.ejml.ops;

import org.ejml.data.D1Matrix64F;

import javax.swing.*;
import java.awt.*;


/**
 * <p>
 * Functions for visualizing matrices in a GUI matrices.
 * </p>
 *
 * <p>
 * NOTE: In some embedded applications there is no GUI or AWT is not supported (like in Android) so excluding
 * this class is necessary.
 * </p>
 *
 * @author Peter Abeles
 */
public class MatrixVisualization {
    /**
     * Creates a window visually showing the matrix's state.  Block means an element is zero.
     * Red positive and blue negative.  More intense the color larger the element's absolute value
     * is.
     *
     * @param A A matrix.
     * @param title Name of the window.
     */
    public static void show( D1Matrix64F A , String title ) {
        JFrame frame = new JFrame(title);

        int width = 300;
        int height = 300;

        if( A.numRows > A.numCols) {
            width = width*A.numCols/A.numRows;
        } else {
            height = height*A.numRows/A.numCols;
        }

        MatrixComponent panel = new MatrixComponent(width,height);
        panel.setMatrix(A);

        frame.add(panel, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);

    }
}
