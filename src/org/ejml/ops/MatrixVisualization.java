/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
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
