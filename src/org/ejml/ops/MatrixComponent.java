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

import org.ejml.data.D1Matrix64F;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


/**
 * Renders a matrix as an image.  Positive elements are shades of red, negative shades of blue, 0 is black.
 *
 * @author Peter Abeles
 */
public class MatrixComponent extends JPanel {
    BufferedImage image;

    public MatrixComponent( int width , int height ) {
        image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        setPreferredSize(new Dimension(width,height));
        setMinimumSize(new Dimension(width,height));
    }

    public synchronized void setMatrix( D1Matrix64F A ) {
        double maxValue = CommonOps.elementMaxAbs(A);
        renderMatrix(A,image,maxValue);
        repaint();
    }

    public static void renderMatrix( D1Matrix64F M , BufferedImage image , double maxValue )
    {
        int w = image.getWidth();
        int h = image.getHeight();

        double widthStep = (double)M.numCols / image.getWidth();
        double heightStep = (double)M.numRows / image.getHeight();

        for( int i = 0; i < h; i++ ) {
            for( int j = 0; j < w; j++ ) {
                double value = M.get( (int)(i*heightStep) , (int)(j*widthStep) );

                if( value == 0 ){
                    image.setRGB(j,i,255 << 24);
                } else if( value > 0 ) {
                    int p = 255-(int)(255.0*(value/maxValue));
                    int rgb = 255 << 24 | 255 << 16 | p << 8 | p;

                    image.setRGB(j,i,rgb);
                } else {
                    int p = 255+(int)(255.0*(value/maxValue));
                    int rgb = 255 << 24 | p << 16 | p << 8 | 255;

                    image.setRGB(j,i,rgb);
                }
            }
        }


    }

    @Override
    public synchronized void paint( Graphics g ) {
        g.drawImage(image,0,0,this);
    }

}
